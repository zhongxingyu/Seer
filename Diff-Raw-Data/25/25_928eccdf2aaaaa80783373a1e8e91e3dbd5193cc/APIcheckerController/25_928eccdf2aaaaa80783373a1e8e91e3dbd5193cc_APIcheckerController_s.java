 package tk.ludva.restfulchecker;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.logging.Logger;
 
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import tk.ludva.restfulchecker.model.ErrsAndWarns;
 import tk.ludva.restfulchecker.validators.HttpValidator;
 import tk.ludva.restfulchecker.validators.RestValidator;
 /**
  * Class used as controller for checkapi, checkapi/repeatedtesting, client.html
  * @author Lu2
  *
  */
 @Controller
 public class APIcheckerController
 {
 
 	/**
 	 * Name of JSP for checkapi view.
 	 */
 	private static final String JSP = "checkapi";
 	
 	/**
 	 * Name of JSP for view one or less resource.
 	 */
 	private static final String JSPBadResponse = "checkapi_bad";
 	
 	/**
 	 * Name of JSP for view apitesting results.
 	 */
 	private static final String JSPOkResponse = "checkapi_ok";
 
 	/**
 	 * Logger for this class.
 	 */
 	private static final Logger log = Logger.getLogger(APIcheckerController.class.getName());
 
 	/**
 	 * Handles GET requests for /
 	 * @param remoteResource the object with form data about entry point.
 	 * @return name of JSP page which will be shown.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String showCheckAPI(ApiEntry remoteResource)
 	{
 		return "checkapi";
 	}
 
 	/**
 	 * Handles GET requests for /repeatedtesting
 	 * @param remoteResource the object with form data about entry point.
 	 * @return JSP page which will be shown.
 	 */
 	@RequestMapping(value = "/repeatedtesting", method = RequestMethod.GET)
 	public String showCheckAPIRepeatedTesting(ApiEntry remoteResource)
 	{
 		return "checkapirepeatedtesting";
 	}
 
 	/**
 	 * Handles POST requests for /
 	 * @param apiEntry the object with form data about entry point.
 	 * @param result BindingResult with data about validation of input data.
 	 * @return JSP page which will be shown.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.POST)
 	public String doCheckAPI(ApiEntry apiEntry, BindingResult result)
 	{
 		if (result.hasErrors())
 		{
 			return JSP;
 		}
 		if (!apiEntry.getUrl().startsWith("http://") && !apiEntry.getUrl().startsWith("https://"))
 		{
 			apiEntry.setUrl("http://" + apiEntry.getUrl());
 		}
 		if (apiEntry.getUrl().length() < 10)
 		{
 			apiEntry.setMessage("Invalid URL");
 			return JSP;
 		}
 
 		createTree(apiEntry);
 		if (HttpValidator.responseOk(apiEntry) && apiEntry.getResourceNodes().getDescendants().size() > 0)
 		{
 			validateTree(apiEntry);
 			generateViewOfQuestionnaires(apiEntry);
 			generateViewOfResources(apiEntry);
 			generateViewOfTree(apiEntry);
 			return JSPOkResponse;
 		}
 		else
 		{
 			return JSPBadResponse;
 		}
 	}
 
 	/**
 	 * Handles POST requests for /repeatedtesting
 	 * @param apiEntry the object with form data about entry point.
 	 * @return ResponseEntity with resource body.
 	 */
 	@RequestMapping(value = "/repeatedtesting", method = RequestMethod.POST)
 	public ResponseEntity<String> doCheckAPIRepeatedTesting(ApiEntry apiEntry)
 	{
 		if (!apiEntry.getUrl().startsWith("http://") && !apiEntry.getUrl().startsWith("https://"))
 		{
 			apiEntry.setUrl("http://" + apiEntry.getUrl());
 		}
 		if (apiEntry.getUrl().length() < 10)
 		{
 			apiEntry.setMessage("Invalid URL");
 			return new ResponseEntity<String>(apiEntry.getMessage(), HttpStatus.BAD_REQUEST);
 		}
 		createTree(apiEntry);
 		apiEntry.setMessage("<RESTfulChecker_report>");
 		generateViewOfEntryPointXML(apiEntry);
 		validateTreeXML(apiEntry);
 		generateViewOfTreeXML(apiEntry);
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.setContentType(MediaType.TEXT_XML);
 		apiEntry.setMessage(apiEntry.getMessage() + "</RESTfulChecker_report>");
 		return new ResponseEntity<String>(apiEntry.getMessage(), responseHeaders, HttpStatus.OK);
 	}
 
 	/**
 	 * Appends XML information about entrypoint into entrypoints message.
 	 * @param apiEntry data about entrypoint.
 	 */
 	private void generateViewOfEntryPointXML(ApiEntry apiEntry)
 	{
 		StringBuilder sb = new StringBuilder(apiEntry.getMessage());
 		sb.append("<test_definition>");
 
 		sb.append("<request>");
 		sb.append("<API_entry_URL>");
 		sb.append(apiEntry.getUrl());
 		sb.append("</API_entry_URL>");
 		if (apiEntry.getRequestHeaders().size() > 0)
 		{
 			sb.append("<headers>");
 			for (Header header : apiEntry.getRequestHeaders())
 			{
 				sb.append("<header>");
 				sb.append("<key>");
 				sb.append(header.getHeaderKey());
 				sb.append("</key>");
 				sb.append("<value>");
 				sb.append(header.getHeaderValue());
 				sb.append("</value>");
 				sb.append("</header>");
 			}
 			sb.append("</headers>");
 		}
 
 		sb.append("</request>");
 
 		sb.append("<limits>");
 		sb.append("<max_siblings>");
 		sb.append(apiEntry.getMaxSiblings());
 		sb.append("</max_siblings>");
 		sb.append("<base_url>");
 		sb.append(apiEntry.getBaseUrl());
 		sb.append("</base_url>");
 		sb.append("</limits>");
 
 		sb.append("</test_definition>");
 
 		apiEntry.setMessage(sb.toString());
 	}
 
 	/**
 	 * Counts REST errors and warnings in tree taken from apiEntry.
 	 * @param apiEntry apiEntry information.
 	 * @return ErrsAndWarns object with counts.
 	 */
 	private ErrsAndWarns countErrsAndWarns(ApiEntry apiEntry)
 	{
 		ErrsAndWarns errsAndWarns = new ErrsAndWarns();
 		countErrsAndWarns(apiEntry.getResourceNodes(), errsAndWarns);
 
 		return errsAndWarns;
 	}
 
 	/**
 	 * Counts REST errors and warnings in specified resource and its descendants.
 	 * @param rs ResourceNode for which the count will be done.
 	 * @param eaw ErrsAndWarns instance for storing the counts.
 	 */
 	private void countErrsAndWarns(ResourceNode rs, ErrsAndWarns eaw)
 	{
 		eaw.addErr(rs.getViolationMessages().size());
 		eaw.addWarn(rs.getNonViolationMessages().size());
 
 		for (ResourceNode resourceNode : rs.getDescendants())
 		{
 			countErrsAndWarns(resourceNode, eaw);
 		}
 
 	}
 
 	/**
 	 * Generates view of questionnaire for JSP and stores it into apiEntry.
 	 * @param apiEntry for which the view is generated.
 	 */
 	private void generateViewOfQuestionnaires(ApiEntry apiEntry)
 	{
 		String evaluation = apiEntry.getQuestionnaires().evaluate();
 		StringBuilder sb = new StringBuilder(apiEntry.getMessage());
 		sb.append("<div id=\"questionnairesEvaluation\">");
 		sb.append("From answers in questionnaire, we can determine if your API comply with following two REST constraints: ");
 		sb.append("<ul><li>client-server,</li><li>stateless.</li></ul>\n");
 		if (evaluation.equals(""))
 		{
 			sb.append("Your answers indicates, that both of these constraints are met.");
 		}
 		else
 		{
 			sb.append("Your answers indicates that these constraints are not met because:\n");
 			sb.append(evaluation);
 		}
 		sb.append("</div>\n");
 		apiEntry.setMessage(sb.toString());
 	}
 
 	/**
 	 * Generates view of HTTP responses of resources in apiEntry and stores it into it.
 	 * @param apiEntry with information about resource tree.
 	 */
 	private void generateViewOfResources(ApiEntry apiEntry)
 	{
 		StringBuilder sb = new StringBuilder(apiEntry.getMessage());
 		sb.append("<div id=\"resourceView\">");
 		writeResourceNodeView(apiEntry.getResourceNodes(), apiEntry.getBaseUrl(), sb);
 
 		sb.append("</div>\n");
 
 		apiEntry.setMessage(sb.toString());
 	}
 
 	/**
 	 * Writes ResourceNode tree representation into specified sb.
 	 * @param resourceNode for which the representation is written.
 	 * @param baseUrl Used for shortening the URI id's of resources.
 	 * @param sb StringBuilder in which the representation is written.
 	 */
 	private void writeResourceNodeView(ResourceNode resourceNode, String baseUrl, StringBuilder sb)
 	{
 		if (resourceNode.getCurrentResource().getResponseCode() != 0)
 		{
 			sb.append(resourceNode.toStringResponse());
 		}
 		for (ResourceNode rs : resourceNode.getDescendants())
 		{
 			writeResourceNodeView(rs, baseUrl, sb);
 		}
 	}
 
 	/**
 	 * Generates the View of APIs resource tree.
 	 * @param apiEntry for which the view is generated and stored into.
 	 */
 	private void generateViewOfTree(ApiEntry apiEntry)
 	{
 		StringBuilder sb = new StringBuilder(apiEntry.getMessage());
 		sb.append("This API structure was found: ");
 		sb.append("<ul>\n");
 		writeResourceNodeTreeView(apiEntry.getResourceNodes(), apiEntry.getBaseUrl(), apiEntry.getMaxSiblings(), sb);
 
 		sb.append("</ul>\n");
 
 		apiEntry.setMessage(sb.toString());
 	}
 
 	/**
 	 * Writes the View of APIs resource tree for specified resourceNode and its descendants.
 	 * @param resourceNode for which the view is written.
 	 * @param baseUrl Used for shortening the URI id's of resources and limiting API scope.
 	 * @param maxSiblings limits number of children for one resource.
 	 * @param sb StringBuilder in which the view is written.
 	 */
 	private void writeResourceNodeTreeView(ResourceNode resourceNode, String baseUrl, int maxSiblings, StringBuilder sb)
 	{
 		String temp;
 		sb.append("<li>");
 		temp = resourceNode.getCurrentResource().getUrl().replaceFirst(baseUrl, "");
 		if (temp.length() == 0)
 		{
 			temp = "/";
 		}
 		sb.append("<a href=\"#\" onclick=\"toggleVisibility(document.getElementById(\'"
 				+ resourceNode.getCurrentResource().getUrl() + "\')); return false\" >#</a> ");
 		sb.append(temp);
 
 		if (resourceNode.getViolationMessages().size() > 0)
 		{
 			for (String violationKey : resourceNode.getViolationMessages().keySet())
 			{
 				String messagesDisp = "";
 				for (String vilationMessages : resourceNode.getViolationMessages().get(violationKey).getMessages())
 				{
 					messagesDisp = messagesDisp + ", " + vilationMessages;
 				}
 				if (messagesDisp.startsWith(", "))
 				{
 					messagesDisp = messagesDisp.substring(2);
 				}
 				sb.append(" <img src=\"../redDot.gif\" alt=\" x \" title=\"" + violationKey + ": " + messagesDisp
 						+ "\" height=\"13\" width=\"13\" />");
 			}
 		}
 
 		if (resourceNode.getNonViolationMessages().size() > 0)
 		{
 			for (String violationKey : resourceNode.getNonViolationMessages().keySet())
 			{
 				String messagesDisp = "";
 				for (String vilationMessages : resourceNode.getNonViolationMessages().get(violationKey).getMessages())
 				{
 					messagesDisp = messagesDisp + ", " + vilationMessages;
 				}
 				if (messagesDisp.startsWith(", "))
 				{
 					messagesDisp = messagesDisp.substring(2);
 				}
 				sb.append(" <img src=\"../orangeDot.gif\" alt=\" ? \" title=\"" + violationKey + ": " + messagesDisp
 						+ "\" height=\"13\" width=\"13\" />");
 			}
 		}
 
 		if (resourceNode.getDescendants().size() > 0)
 		{
 			sb.append("\n<ul>\n");
 			try
 			{
 				for (ResourceNode rs : resourceNode.getDescendants().subList(0, maxSiblings))
 				{
 					writeResourceNodeTreeView(rs, baseUrl, maxSiblings, sb);
 				}
 				int overSize = resourceNode.getDescendants().size() - maxSiblings;
 				if (overSize > 0)
 				{
 					sb.append("<li>...and " + overSize + " more.</li>");
 				}
 			}
 			catch (java.lang.IndexOutOfBoundsException e)
 			{
 				for (ResourceNode rs : resourceNode.getDescendants())
 				{
 					writeResourceNodeTreeView(rs, baseUrl, maxSiblings, sb);
 				}
 			}
 			sb.append("</ul>\n");
 		}
 
 		sb.append("</li>");
 		sb.append("\n");
 	}
 
 	/**
 	 * Generates the XML view of API tree from apiEntry and saves it to it.
 	 * @param apiEntry Informations about tree.
 	 */
 	private void generateViewOfTreeXML(ApiEntry apiEntry)
 	{
 		StringBuilder sb = new StringBuilder(apiEntry.getMessage());
 		sb.append("<api_tree>\n");
 		writeResourceNodeTreeViewXML(apiEntry.getResourceNodes(), apiEntry.getBaseUrl(), apiEntry.getMaxSiblings(), sb);
 
 		sb.append("</api_tree>\n");
 
 		apiEntry.setMessage(sb.toString());
 	}
 
 	/**
 	 * Writes the XML view of API resources for specified ResourceNode.
 	 * @param resourceNode from which the tree is generated.
 	 * @param baseUrl Used for limiting API scope.
 	 * @param maxSiblings limits number of children for one resource.
 	 * @param sb StringBuilder in which the view is written.
 	 */
 	private void writeResourceNodeTreeViewXML(ResourceNode resourceNode, String baseUrl, int maxSiblings, StringBuilder sb)
 	{
 		sb.append("<resource id=\"" + resourceNode.getCurrentResource().getUrl() + "\">");
 		sb.append("<url>");
 		sb.append(resourceNode.getCurrentResource().getUrl());
 		sb.append("</url>");
 		sb.append(resourceNode.toStringResponseXML());
 
 		if (resourceNode.getViolationMessages().size() > 0)
 		{
 			sb.append("<errors>");
 			for (String violationKey : resourceNode.getViolationMessages().keySet())
 			{
 				String messagesDisp = "";
 				for (String vilationMessages : resourceNode.getViolationMessages().get(violationKey).getMessages())
 				{
 					messagesDisp = messagesDisp + ", " + vilationMessages;
 				}
 				if (messagesDisp.startsWith(", "))
 				{
 					messagesDisp = messagesDisp.substring(2);
 				}
 				sb.append("<error>");
 				sb.append("<key>");
 				sb.append(violationKey);
 				sb.append("</key>");
 				sb.append("<value>");
 				sb.append(messagesDisp);
 				sb.append("</value>");
 				sb.append("</error>");
 			}
 			sb.append("</errors>");
 		}
 
 		if (resourceNode.getNonViolationMessages().size() > 0)
 		{
 			sb.append("<warnings>");
 			for (String violationKey : resourceNode.getNonViolationMessages().keySet())
 			{
 				String messagesDisp = "";
 				for (String vilationMessages : resourceNode.getNonViolationMessages().get(violationKey).getMessages())
 				{
 					messagesDisp = messagesDisp + ", " + vilationMessages;
 				}
 				if (messagesDisp.startsWith(", "))
 				{
 					messagesDisp = messagesDisp.substring(2);
 				}
 				sb.append("<warning>");
 				sb.append("<key>");
 				sb.append(violationKey);
 				sb.append("</key>");
 				sb.append("<value>");
 				sb.append(messagesDisp);
 				sb.append("</value>");
 				sb.append("</warning>");
 			}
 			sb.append("</warnings>");
 		}
 
 		if (resourceNode.getDescendants().size() > 0)
 		{
 			sb.append("<resources>");
 			try
 			{
 				for (ResourceNode rs : resourceNode.getDescendants().subList(0, maxSiblings))
 				{
 					writeResourceNodeTreeViewXML(rs, baseUrl, maxSiblings, sb);
 				}
 				int overSize = resourceNode.getDescendants().size() - maxSiblings;
 				if (overSize > 0)
 				{
 					sb.append("<resource><note>...and " + overSize + " more.</note></resource>");
 				}
 			}
 			catch (java.lang.IndexOutOfBoundsException e)
 			{
 				for (ResourceNode rs : resourceNode.getDescendants())
 				{
 					writeResourceNodeTreeViewXML(rs, baseUrl, maxSiblings, sb);
 				}
 			}
 			sb.append("</resources>");
 		}
 
 		sb.append("</resource>");
 		sb.append("\n");
 	}
 
 	/**
 	 * Run REST validation on API, stores results into apiEntry.
 	 * @param apiEntry informations about API.
 	 */
 	private void validateTree(ApiEntry apiEntry)
 	{
 		RestValidator restValidator = new RestValidator(apiEntry.getResourceNodes());
 		String validation;
 		if (restValidator.validateApi())
 		{
			validation = "<h3>Your API is RESTful.</h3>";
 		}
 		else
 		{
 			ErrsAndWarns errsAndWarns = countErrsAndWarns(apiEntry);
 			if (errsAndWarns.getErrorsCount() == 0)
 			{
				validation = "<h3>Your API is RESTful, but found " + errsAndWarns.getWarnsCount()
 						+ " warnings - see yellow marks below for details.</h3>";
 			}
 			else
 			{
				validation = "<h3>Your API is not RESTful, found " + errsAndWarns.getErrorsCount() + " errors and "
 						+ errsAndWarns.getWarnsCount() + " warnings - see colored marks below for details.</h3>";
 			}
 		}
 		apiEntry.setMessage(validation);
 	}
 
 	/**
 	 * Runs REST validation on API, stores XML results into apiEntry.
 	 * @param apiEntry informations about API.
 	 */
 	private void validateTreeXML(ApiEntry apiEntry)
 	{
 		StringBuilder sb = new StringBuilder(apiEntry.getMessage());
 		sb.append("<results_of_REST_checking>");
 		RestValidator restValidator = new RestValidator(apiEntry.getResourceNodes());
 		String validation;
 		if (restValidator.validateApi())
 		{
 			validation = "Your API is RESTful.";
 		}
 		else
 		{
 			ErrsAndWarns errsAndWarns = countErrsAndWarns(apiEntry);
 			if (errsAndWarns.getErrorsCount() == 0)
 			{
 				validation = "Your API is RESTful, but found " + errsAndWarns.getWarnsCount()
 						+ " warnings - see details of each resource.";
 			}
 			else
 			{
 				validation = "Your API is not RESTful, found " + errsAndWarns.getErrorsCount() + " errors and "
 						+ errsAndWarns.getWarnsCount() + " warnings - see details of each resource.";
 			}
 		}
 		sb.append(validation);
 		sb.append("</results_of_REST_checking>");
 		apiEntry.setMessage(sb.toString());
 	}
 
 	/**
 	 * Creates API tree from entryPoint specified in apiEntry. Stores the tree into apiEntry.
 	 * @param apiEntry information about API entry point.
 	 */
 	private void createTree(ApiEntry apiEntry)
 	{
 		Map<String, ResourceNode> discoveredResources = new HashMap<String, ResourceNode>();
 		Queue<ResourceNode> toVisit = new LinkedList<ResourceNode>();
 		ResourceNode currentResourceNode = new ResourceNode((RemoteResource) apiEntry);
 		apiEntry.setResourceNodes(currentResourceNode);
 		while (currentResourceNode != null)
 		{
 			doCrawle(currentResourceNode, apiEntry, discoveredResources, toVisit);
 			currentResourceNode = toVisit.poll();
 		}
 	}
 
 	/**
 	 * Discovers and crawls in currentResourceNode for aditional resources.
 	 * @param currentResourceNode resource which will be crawled.
 	 * @param apiEntry Informations about api entrypoint.
 	 * @param discoveredResources Map which holds informations about already crawled resources. 
 	 * @param toVisit queue used to store discovered resources for aditional crawling.
 	 */
 	private void doCrawle(ResourceNode currentResourceNode, ApiEntry apiEntry, Map<String, ResourceNode> discoveredResources,
 			Queue<ResourceNode> toVisit)
 	{
 		if (discoveredResources.containsKey(currentResourceNode.getCurrentResource().getUrl()))
 		{
 			currentResourceNode.setCurrentResource((RemoteResource) discoveredResources.get(
 					currentResourceNode.getCurrentResource().getUrl()).getCurrentResource());
 			currentResourceNode.setCurrentResourceOptions((RemoteResource) discoveredResources.get(
 					currentResourceNode.getCurrentResource().getUrl()).getCurrentResourceOptions());
 			return;
 		}
 		currentResourceNode.sendOptions();
 		currentResourceNode.sendRequest();
 		LinkExtrator links = new LinkExtrator();
 		if (currentResourceNode.getCurrentResource().getResponseBody() == null)
 		{
 			return;
 		}
 		List<String> urls = UrlWorker.getUrls(currentResourceNode.getCurrentResource().getUrl(),
 				links.grabHTMLLinks(currentResourceNode.getCurrentResource().getResponseBody()));
 		List<String> validUrls = new ArrayList<String>();
 		for (String url : urls)
 		{
 			if (url.startsWith(apiEntry.getBaseUrl()))
 			{
 				validUrls.add(url);
 			}
 		}
 		if (validUrls.size() > 0)
 		{
 			RemoteResource nextResource;
 			int maxResourcesToLoad = apiEntry.getMaxSiblings();
 			for (String url : validUrls)
 			{
 				try
 				{
 					nextResource = (RemoteResource) currentResourceNode.getCurrentResource().clone();
 					nextResource.deletePreviousResponse();
 					if (apiEntry.getUrl().contains("?") && !url.contains("?"))
 					{
 						String append = currentResourceNode.getCurrentResource().getUrl();
 						append = currentResourceNode.getCurrentResource().getUrl()
 								.substring(currentResourceNode.getCurrentResource().getUrl().lastIndexOf('?'));
 						nextResource.setUrl(url + append);
 					}
 					else
 					{
 						nextResource.setUrl(url);
 					}
 					ResourceNode nextResourceNode = new ResourceNode(nextResource);
 					currentResourceNode.getDescendants().add(nextResourceNode);
 					if (--maxResourcesToLoad < 0)
 					{
 						// No more crawling for this resource's childs.
 					}
 					else
 					{
 						toVisit.add(nextResourceNode);
 					}
 				}
 				catch (CloneNotSupportedException e)
 				{
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		discoveredResources.put(currentResourceNode.getCurrentResource().getUrl(), currentResourceNode);
 	}
 
 	/**
 	 * Handles GET requests for /client.html
 	 * @param remoteResource the object with form data about entry point.
 	 * @return name of JSP page which will be shown.
 	 */
 	@RequestMapping(value = "/client.html", method = RequestMethod.GET)
 	public String showClient(RemoteResource remoteResource)
 	{
 		return "client";
 	}
 
 	/**
 	 * Handles POST requests for /client.html
 	 * @param remoteResource the object with form data about entry point.
 	 * @return name of JSP page which will be shown.
 	 */
 	@RequestMapping(value = "/client.html", method = RequestMethod.POST)
 	public String doClient(RemoteResource remoteResource)
 	{
 		remoteResource.sendRequest();
 		return "client";
 	}
 
 }
