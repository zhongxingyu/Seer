 package standup.connector.rally;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.util.JAXBResult;
 import javax.xml.bind.util.JAXBSource;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 
 import org.apache.commons.codec.EncoderException;
 import org.apache.commons.codec.net.URLCodec;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 
 import standup.connector.ConnectorException;
 import standup.connector.HttpClientFactory;
 import standup.connector.UnexpectedResponseException;
 import standup.utility.Utilities;
 import standup.xml.Description;
 import standup.xml.Links;
 import standup.xml.Links.Link;
 import standup.xml.StoryList;
 import standup.xml.StoryType;
 import standup.xml.TaskList;
 import standup.xml.TopLevelObject;
 
 import com.rallydev.xml.ArtifactType;
 import com.rallydev.xml.DefectType;
 import com.rallydev.xml.DomainObjectType;
 import com.rallydev.xml.HierarchicalRequirementType;
 import com.rallydev.xml.QueryResultType;
 import com.rallydev.xml.TaskType;
 
 
 /**
  * A connection to the Rally Server.
  * <p>
  * The connection maintains the authorization information for the
  * session along with the working set of HTTP headers.
  */
 public class ServerConnection
 	implements standup.connector.ServerConnection,
 	           org.apache.http.client.CredentialsProvider
 {
 	private static final String RALLY_QUERY_REL = "Rally Query";
 	private static final String RALLY_PARENT_URL_REL = "Parent URL";
 	private static final String RALLY_OBJECT_URL_REL = "Object URL";
 	private static final Logger logger = Logger.getLogger(ServerConnection.class);
 	private static final Pattern ltPattern = Pattern.compile("&lt;");
 	private static final Pattern gtPattern = Pattern.compile("&gt;");
 	private static final Pattern ampPattern = Pattern.compile("&amp;");
 	private static final Pattern nbspPattern = Pattern.compile("&nbsp;");
 	private static final Pattern brPattern = Pattern.compile("<br\\s*>");
 	private static final Pattern ampPattern2 = Pattern.compile("&");
 
 	private String userName;
 	private String password;
 	private final HttpHost host;
 	private final HttpClientFactory clientFactory;
 	private JAXBContext jaxb;
 	private Unmarshaller unmarshaller;
 	private final TransformerFactory xformFactory;
 	private final standup.xml.ObjectFactory standupFactory;
 
 	/**
 	 * Creates a server connection that connects to a specific Rally server.
 	 *
 	 * @param serverName the Rally server instance to connect to
 	 * @param clientFactory the factory to fetch HTTP clients from
 	 * 
 	 * @throws Error when the JAXB context cannot be created.  The underlying
 	 *         {@link JAXBException} is attached as the cause.
 	 */
 	public ServerConnection(String serverName, HttpClientFactory clientFactory)
 	{
 		this.userName = "";
 		this.password = "";
 		this.host = new HttpHost(serverName, 443, "https");
 		this.clientFactory = clientFactory;
 		this.xformFactory = TransformerFactory.newInstance();
 		this.standupFactory = new standup.xml.ObjectFactory();
 		try {
 			this.jaxb = JAXBContext.newInstance("com.rallydev.xml:standup.xml");
 			this.unmarshaller = jaxb.createUnmarshaller();
 		} catch (JAXBException e) {
 			throw new Error("failed to initialize XML bindings", e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see standup.connector.ServerConnection#listIterationsForProject(java.lang.String)
 	 */
 	@Override
 	public List<IterationStatus> listIterationsForProject(String project)
 		throws IOException, ClientProtocolException, ConnectorException, JAXBException
 	{
 		QueryResultType result = doSimpleQuery("iteration", "Project.Name", project);
 		ArrayList<IterationStatus> iterations = new ArrayList<IterationStatus>(
 				Math.min(result.getPageSize().intValue(),
 						 result.getTotalResultCount().intValue()));
 		for (DomainObjectType domainObj : result.getResults().getObject()) {
 			IterationStatus iterStatus = new IterationStatus();
 			iterStatus.iterationName = domainObj.getRefObjectName();
 			try {
 				iterStatus.iterationURI = new URI(domainObj.getRef());
 			} catch (URISyntaxException e) {
 				logger.error(String.format("iteration %s has invalid URI %s",
 						iterStatus.iterationName, domainObj.getRef()), e);
 				iterStatus.iterationURI = null;
 			}
 			iterations.add(iterStatus);
 		}
 		return iterations;
 	}
 
 	/**
 	 * Process a Rally <code>QueryResult</code> into a list of user stories.
 	 * <p>
 	 * This is used internally to process most responses.  It will do whatever
 	 * is necessary to build {@link StoryType} objects based on the possibly
 	 * abbreviated response.  Rally responses come in both abbreviated and
 	 * full responses.  This method contains the intelligence to handle either
 	 * response type and build complete object instances from.
 	 * <p>
 	 * A full response contains the full content of each object in the
 	 * response.  The <code>type</code> attribute of the object element
 	 * indicates the actual type of the element.
 	 * <p>
 	 * An abbreviated response only includes the object type, its name, and
 	 * a URI to retrieve the full contents from.  If an abbreviated response
 	 * is encountered, then a separate request is made to retrieve the full
 	 * representation.
 	 * 
 	 * @param stories list to place the user stories into.  Each story is
 	 *                added to the list by calling {@link StoryList#getStory()}
 	 *                and then calling {@link List#add(Object)} on the result.
 	 * @param result  the query result to process.
 	 * 
 	 * @throws ConnectorException when the {@code result} contains one or more
 	 *         errors.  Both errors and warnings will be logged and any errors
 	 *         result in this exception being generated.
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 * @throws JAXBException
  	 * @throws TransformerException when an XSLT exception is thrown while
  	 *         transforming the backend result into the model.
 	 */
 	protected void processQueryResult(StoryList stories, QueryResultType result)
 		throws ClientProtocolException, IOException,
 		       URISyntaxException, JAXBException, TransformerException,
 		       ConnectorException
 	{
 		List<String> errors = result.getErrors().getOperationResultError();
 		if (errors.size() > 0) {
 			int errorCount = errors.size();
 			for (String err: errors) {
 				logger.error(err);
 			}
 			if (errorCount == 1) {
 				throw Utilities.generateException(ConnectorException.class,
 						"query resulted in a single error", errors.get(0));
 			}
 			throw Utilities.generateException(ConnectorException.class,
 					String.format("query resulted in %d errors", errorCount),
 					errors);
 		}
 		for (String warning: result.getWarnings().getOperationResultWarning()) {
 			logger.warn(warning);
 		}
 
 		com.rallydev.xml.ObjectFactory rallyFactory = new com.rallydev.xml.ObjectFactory();
 		List<DomainObjectType> domainObjects = result.getResults().getObject();
 		for (DomainObjectType domainObj: domainObjects) {
 			StoryType story = null;
 			ArtifactType artifact = null;
 			JAXBElement<? extends ArtifactType> obj = null;
 			String stringType = domainObj.getType();
 			if (stringType.equalsIgnoreCase("HierarchicalRequirement")) {
 				if (domainObj instanceof HierarchicalRequirementType) { // xsi:type in use!
 					obj = rallyFactory.createHierarchicalRequirement((HierarchicalRequirementType) domainObj);
 					artifact = (HierarchicalRequirementType) domainObj;
 				} else { // we need to fetch this explicitly
 					obj = retrieveJAXBElement(HierarchicalRequirementType.class, new URI(domainObj.getRef()));
 					artifact = obj.getValue();
 				}
 			} else if (stringType.equalsIgnoreCase("Defect")) {
 				if (domainObj instanceof DefectType) {
 					obj = rallyFactory.createDefect((DefectType) domainObj);
 					artifact = (DefectType) domainObj;
 				} else {
 					obj = retrieveJAXBElement(DefectType.class, new URI(domainObj.getRef()));
 					artifact = obj.getValue();
 				}
 			}
 	
 			if (artifact != null) {
 				story = this.transformResultInto(StoryType.class, obj);
 				story.setDescription(fixDescription(artifact));
 				addLink(story, obj.getValue().getRef(), RALLY_OBJECT_URL_REL);
 				stories.getStory().add(story);
 			} else {
 				logger.debug(String.format("ignoring DomainObject %d of type %s", domainObj.getObjectID(), stringType));
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see standup.connector.ServerConnection#retrieveStories(java.lang.String[])
 	 */
 	@Override
 	public StoryList retrieveStories(String[] stories)
 		throws IOException, ClientProtocolException, ConnectorException,
 		       TransformerException
 	{
 		StoryList storyList = this.standupFactory.createStoryList();
 		String[] segments = new String[stories.length];
 		for (int index=0; index<stories.length; ++index) {
 			segments[index] = String.format("FormattedID = \"%s\"",
 					                        stories[index]);
 		}
 
 		try {
 			URI uri = buildQuery("artifact", "OR", segments);
 			QueryResultType result = retrieveURI(QueryResultType.class, uri);
 			processQueryResult(storyList, result);
 			addLink(storyList, uri.toString(), RALLY_QUERY_REL);
 		} catch (JAXBException e) {
 			logger.error("JAXB related error while retrieving multiple stories", e);
 		} catch (URISyntaxException e) {
 			logger.error(e.getClass().getCanonicalName(), e);
 		}
 		
 		return storyList;
 	}
 
 	/* (non-Javadoc)
 	 * @see standup.connector.ServerConnection#retrieveStoriesForIteration(java.lang.String)
 	 */
 	@Override
 	public StoryList retrieveStoriesForIteration(String iteration)
 		throws IOException, ClientProtocolException, ConnectorException,
 		       TransformerException
 	{
 		StoryList storyList = this.standupFactory.createStoryList();
 		try {
 			NDC.push("retrieving stories for iteration "+iteration);
 			QueryResultType result = doSimpleQuery("hierarchicalrequirement",
 					"Iteration.Name", iteration);
 			processQueryResult(storyList, result);
 			NDC.pop();
 
 			NDC.push("retrieving defects for iteration "+iteration);
 			result = doSimpleQuery("defect", "Iteration.Name", iteration);
 			processQueryResult(storyList, result);
 		} catch (JAXBException e) {
 			logger.error("JAXB related error while processing iteration "+iteration, e);
 		} catch (URISyntaxException e) {
 			logger.error(e.getClass().getCanonicalName(), e);
 		} finally {
 			NDC.pop();
 		}
 		return storyList;
 	}
 
 	/* (non-Javadoc)
 	 * @see standup.connector.ServerConnection#retrieveTasks(standup.xml.StoryList)
 	 */
 	@Override
 	public TaskList retrieveTasks(StoryList stories)
 		throws IOException, ClientProtocolException, ConnectorException,
 		       TransformerException
 	{
 		TaskList taskList = this.standupFactory.createTaskList();
 		for (StoryType story: stories.getStory()) {
 			String storyID = story.getIdentifier();
 			Link storyLink = findLinkByRel(story, RALLY_OBJECT_URL_REL);
 			if (storyLink != null) {
 				storyLink.setRel(RALLY_PARENT_URL_REL);
 			}
 			try {
 				NDC.push("retrieving tasks for "+story.getIdentifier());
 				logger.debug(NDC.peek());
 				QueryResultType result = doSimpleQuery("task", "WorkProduct.FormattedID", storyID);
 				for (DomainObjectType domainObj: result.getResults().getObject()) {
 					JAXBElement<TaskType> taskObj = this.retrieveJAXBElement(TaskType.class, new URI(domainObj.getRef()));
 					standup.xml.TaskType task = this.transformResultInto(standup.xml.TaskType.class, taskObj);
 					task.setParentIdentifier(storyID);
 					addLink(task, taskObj.getValue().getRef(), RALLY_OBJECT_URL_REL);
 					addLink(task, storyLink);
 					taskList.getTask().add(task);
 				}
 			} catch (JAXBException e) {
 				logger.error("JAXB related error while processing story "+storyID, e);
 			} catch (URISyntaxException e) {
 				logger.error(e.getClass().getCanonicalName(), e);
 			} finally {
 				NDC.pop();
 			}
 		}
 		return taskList;
 	}
 
 	/**
 	 * Stores the user name that is sent to the server in response to
 	 * an authorization challenge.  The user name is conventionally the email
 	 * address of the user.
 	 * 
 	 * @param userName the name to present to the server when challenged.
 	 */
 	public void setUsername(String userName) {
 		this.userName = userName;
 	}
 
 	/**
 	 * Stores the password that is sent to the server in response to
 	 * an authentication challenge.
 	 * 
 	 * @param password the password to present to the server when challenged.
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	//=========================================================================
 	// CredentialsProvider implementation
 	//
 	/* (non-Javadoc)
 	 * @see org.apache.http.client.CredentialsProvider#clear()
 	 */
 	@Override
 	public void clear() {
 		this.userName = "";
 		this.password = "";
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.http.client.CredentialsProvider#getCredentials(org.apache.http.auth.AuthScope)
 	 */
 	@Override
 	public Credentials getCredentials(AuthScope scope) {
 		return new UsernamePasswordCredentials(this.userName, this.password);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.http.client.CredentialsProvider#setCredentials(org.apache.http.auth.AuthScope, org.apache.http.auth.Credentials)
 	 */
 	@Override
 	public void setCredentials(AuthScope scope, Credentials credentials) {
 		setUsername(credentials.getUserPrincipal().getName());
 		setPassword(credentials.getPassword());
 	}
 
 	//=========================================================================
 	// Internal utility methods
 	//
 
 	/**
 	 * Constructs a URI query according to the Rally Grammar.
 	 * <p>
 	 * Rally queries are essentially <code>Attribute OPERATOR Value</code>
 	 * triples strung together with AND and OR.  The expression is fully
 	 * parenthesized according to a very specific grammar described on
 	 * <a href="https://rally1.rallydev.com/slm/doc/webservice/introduction.jsp">
 	 * Rally's Web Service API Introduction</a>.
 	 * <p>
 	 * This method receives the query contents as a list of segments where
 	 * each segment is of the form <code><i>Name</i> <i>op</i> <i>Value</i>
 	 * </code>. The caller is required to insert quotes around <i>Value</i>
 	 * as needed. The Rally query syntax requires double quotes if the value
 	 * contains spaces.  The best bet is to always include them.
 	 * <p>
 	 * The <code>joiner</code> parameter specifies the Boolean operator that
 	 * is used to string together the individual expressions.  The only
 	 * supported operators are <code>AND</code> and <code>OR</code>.
 	 * 
 	 * @param objectType    the type of object to query for
 	 * @param joiner        the operation to join the query segments using
 	 * @param querySegments a list of primitive <code>"name op value"</code>
 	 *                      strings.  See the Rally documentation for supported
 	 *                      names and operators.
 	 * 
 	 * @return the query represented as a Rally URI
 	 * 
 	 * @throws MalformedURLException when a URI object cannot be created using
 	 *         the constructed query
 	 */
 	private URI buildQuery(String objectType, String joiner,
 			               String... querySegments)
 		throws MalformedURLException
 	{
 		// The most difficult part of the query syntax is the
 		// parenthesization.  The query syntax is quite rigid in that all
 		// queries are of the form `(LEFT OP RIGHT)` where `LEFT` and `RIGHT`
 		// are either single tokens or full parenthesized expressions.  If
 		// either of the expressions are parenthesized, then both must be.
 		//
 		// So... what follows is an interesting algorithm that builds fully
 		// parenthesized expressions.  Since Java does not include any useful
 		// mechanism for creating strings of repeated characters or building
 		// a string by appending and prefixing a single buffer, we use to
 		// string builders - one for the leading series of parenthesis and
 		// another for the actual query.
 		String separator = String.format(" %s (", joiner);
 		StringBuilder prefixBuilder = new StringBuilder();
 		StringBuilder builder = new StringBuilder();
 		int index = 0;
 
 		prefixBuilder.append("(");
 		builder.append(querySegments[index++]).append(")");
 		while (index < querySegments.length) {
 			prefixBuilder.append("(");
 			builder.append(separator).append(querySegments[index++]).append("))");
 		}
 		String query = prefixBuilder.toString() + builder.toString();
 		String path = Utilities.join("/", Constants.RALLY_BASE_RESOURCE,
 				Constants.RALLY_API_VERSION, objectType);
 		try {
 			URLCodec codec = new URLCodec("US-ASCII");
 			return Utilities.createURI(this.host, path,
 					"query="+codec.encode(query));
 		} catch (EncoderException e) {
 			throw Utilities.generateException(MalformedURLException.class, e,
 					"failed to encode URL", "object type", objectType,
 					"query was", query);
 		} catch (URISyntaxException e) {
 			throw Utilities.generateException(MalformedURLException.class, e,
 					"failed to build URL", "object type", objectType,
 					"query was", query);
 		}
 	}
 
 	private QueryResultType doSimpleQuery(String objectType, String attributeName, String attributeValue) throws ClientProtocolException, UnexpectedResponseException, MalformedURLException, IOException {
 		return retrieveURI(QueryResultType.class,
 				           buildQuery(objectType, "AND",
 				        		     String.format("%s = \"%s\"",
 				        		    		       attributeName, attributeValue)));
 	}
 
 	@Override
 	public <T> T retrieveURI(Class<T> klass, URI uri)
 		throws ClientProtocolException, IOException, UnexpectedResponseException
 	{
 		JAXBElement<T> jaxbElm = retrieveJAXBElement(klass, uri);
 		return jaxbElm.getValue();
 	}
 
 	protected <T> JAXBElement<T> retrieveJAXBElement(Class<T> klass, URI uri)
 		throws ClientProtocolException, IOException, UnexpectedResponseException
 	{
 		logger.debug(String.format("retrieving %s from %s", klass.toString(), uri.toString()));
 		HttpGet get = new HttpGet(uri);
 		AbstractHttpClient httpClient = clientFactory.getHttpClient(this);
 		HttpResponse response = httpClient.execute(host, get);
 		StatusLine status = response.getStatusLine();
 		if (status.getStatusCode() == 200) {
 			HttpEntity entity = response.getEntity();
 			try {
 				JAXBElement<?> responseObj = (JAXBElement<?>) unmarshaller.unmarshal(entity.getContent());
 				if (responseObj.getDeclaredType() == klass) {
 					@SuppressWarnings("unchecked")
 					JAXBElement<T> elm = (JAXBElement<T>) responseObj;
 					return elm;
 				} else {
 					throw Utilities.generateException(UnexpectedResponseException.class,
 							"unexpected response type", "expected", klass.toString(),
 							"got", responseObj.getDeclaredType().toString());
 				}
 			} catch (JAXBException e) {
 				throw Utilities.generateException(UnexpectedResponseException.class, e,
 						"failed to unmarshal response");
 			}
 		} else {
 			String msg = String.format("request for '%s' failed: %d %s",
 					uri.toString(), status.getStatusCode(), status.getReasonPhrase());
 			throw new ClientProtocolException(msg);
 		}
 	}
 
 	protected <T,U> U transformResultInto(Class<U> klass, T result)
 		throws JAXBException, TransformerException, UnexpectedResponseException
 	{
 		JAXBResult resultDoc = Utilities.runXSLT(new JAXBResult(this.jaxb),
 				"xslt/rally.xsl", logger, new JAXBSource(this.jaxb, result),
 				this.xformFactory);
 		Object resultObj = resultDoc.getResult();
 		String resultType = resultObj.getClass().toString();
 		if (resultObj instanceof JAXBElement<?>) {
 			JAXBElement<?> elm = (JAXBElement<?>) resultObj;
 			if (elm.getDeclaredType() == klass) {
 				@SuppressWarnings("unchecked")
 				U outputObj = (U) elm.getValue();
 				return outputObj;
 			}
 			resultType = elm.getDeclaredType().toString();
 		}
 		throw Utilities.generateException(UnexpectedResponseException.class,
 				"unexpected response type", "expected", klass.toString(),
 				"got",resultType);
 	}
 
 	protected Description fixDescription(ArtifactType artifact) {
 		String descString = artifact.getDescription();
 		descString = ltPattern.matcher(descString).replaceAll("<");		// &lt; -> "<"
 		descString = gtPattern.matcher(descString).replaceAll(">");		// &gt; -> ">"
 		descString = ampPattern.matcher(descString).replaceAll("&");	// necessary to catch &nbsp;
 		descString = nbspPattern.matcher(descString).replaceAll(" ");	// &nbsp; -> " "
 		descString = brPattern.matcher(descString).replaceAll("<br/>");	// <br> -> <br/>
 		descString = ampPattern2.matcher(descString).replaceAll("&amp;"); // & -> &amp;
 		descString = String.format("<description>%s</description>", descString);
 
 		try {
 			Object obj = unmarshaller.unmarshal(new StringReader(descString));
 			if (obj instanceof Description) {
 				return (Description) obj;
 			}
 		} catch (JAXBException e) {
 			logger.error("failed to unmarshal description <<"+descString+">>", e);
 		}
 		return this.standupFactory.createDescription();
 	}
 
 	private void addLink(TopLevelObject obj, String linkURI, String linkRel) {
 		Link l = this.standupFactory.createLinksLink();
 		l.setOwner(this.getClass().getCanonicalName());
 		l.setValue(linkURI);
 		l.setRel(linkRel);
 		addLink(obj, l);
 	}
 
 	private void addLink(TopLevelObject obj, Link l) {
 		if (l != null) {
 			if (obj.getLinks() == null) {
 				obj.setLinks(this.standupFactory.createLinks());
 			}
 			obj.getLinks().getLink().add(l);
 		}
 	}
 
 	private Link findLinkByRel(TopLevelObject obj, String linkRel) {
 		Links links = obj.getLinks();
 		if (links != null) {
 			for (Link l: links.getLink()) {
 				if (l.getRel().equalsIgnoreCase(linkRel)) {
 					Link cloned = this.standupFactory.createLinksLink();
 					cloned.setOwner(l.getOwner());
 					cloned.setRel(l.getRel());
 					cloned.setValue(l.getValue());
 					return cloned;
 				}
 			}
 		}
 		return null;
 	}
 
 }
