 package org.computer.knauss.reqtDiscussion.io.jazz.rest;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.http.HttpResponse;
 import org.computer.knauss.reqtDiscussion.Util;
 import org.computer.knauss.reqtDiscussion.io.DAOException;
 import org.computer.knauss.reqtDiscussion.io.IDAOProgressMonitor;
 import org.computer.knauss.reqtDiscussion.io.IDiscussionDAO;
 import org.computer.knauss.reqtDiscussion.io.IDiscussionEventDAO;
 import org.computer.knauss.reqtDiscussion.io.IIncidentDAO;
 import org.computer.knauss.reqtDiscussion.io.jazz.IJazzDAO;
 import org.computer.knauss.reqtDiscussion.io.util.XPathHelper;
 import org.computer.knauss.reqtDiscussion.model.Discussion;
 import org.computer.knauss.reqtDiscussion.model.DiscussionEvent;
 import org.computer.knauss.reqtDiscussion.model.DiscussionFactory;
 import org.computer.knauss.reqtDiscussion.model.Incident;
 import org.jdom2.Attribute;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 
 /**
  * Get the discussions from jazz.net by using the change management query API:
  * http://open-services.net/bin/view/Main/CmQuerySyntaxV1
  * 
  * @author eknauss
  * 
  */
 public class JazzJDOMDAO implements IJazzDAO, IDiscussionEventDAO,
 		IDiscussionDAO, IIncidentDAO {
 
 	private static final String STORY_QUERY = "?oslc_cm.query=dc%3Atype=%22com.ibm.team.apt.workItemType.story%22&oslc_cm./sort=dc:created&oslc_cm.pageSize=";
 	private static final String STORY_BY_ID_QUERY = "?oslc_cm.query=dc%3Aid=";
 
 	private IWebConnector webConnector;
 	private XPathHelper xpathHelper;
 	private XPathHelper changeRequestsXML;
 	private XPathHelper commentsXML;
 	private List<Object> projectAreaList;
 	private String selectedProjectArea;
 	private int limit = 50;
 	private String moreQuery;
 	private IJazzAccessConfiguration config;
 
 	public JazzJDOMDAO(IJazzAccessConfiguration config) {
 		this.config = config;
 		this.xpathHelper = new XPathHelper();
 	}
 
 	JazzJDOMDAO(IWebConnector connector, IJazzAccessConfiguration config) {
 		this(config);
 		this.webConnector = connector;
 	}
 
 	void setWebConnector(IWebConnector connector) {
 		this.webConnector = connector;
 	}
 
 	IWebConnector getWebConnector() {
 		return this.webConnector;
 	}
 
 	public void readDocument(InputStream documentStream) {
 		try {
 			this.xpathHelper.setDocument(documentStream);
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getChildAttribute(String childName, String attributeName) {
 		return getAttributeValue(getChild(childName), attributeName);
 	}
 
 	private Object getChild(String name) {
 
 		List<Object> selection = this.xpathHelper.select(name);
 		if (selection.size() == 0)
 			return null;
 		return selection.get(0);
 
 	}
 
 	private String getAttributeValue(Object context, String attributeName) {
 		List<Object> selection = this.xpathHelper.select(context, "@"
 				+ attributeName);
 		if (selection.size() == 0)
 			return null;
 		return selection.get(0).toString();
 	}
 
 	@Override
 	public String[] getProjectAreas() throws Exception {
 		List<Object> projectAreaElements = getProjectAreaList();
 
 		String[] ret = new String[projectAreaElements.size()];
 		for (int i = 0; i < ret.length; i++) {
 			Element e = (Element) projectAreaElements.get(i);
 			ret[i] = e.getChild("title").getValue();
 			// ret[i] = ((Element) this.xpathHelper.select(e, "/title").get(0))
 			// .getValue();
 		}
 		return ret;
 	}
 
 	private List<Object> getProjectAreaList() throws Exception, JDOMException,
 			IOException {
 		if (this.projectAreaList != null) {
 			return this.projectAreaList;
 		}
 		HttpResponse response = this.webConnector
 				.performHTTPSRequestXML(this.config.getRootservicesURL());
 		this.xpathHelper.setDocument(response.getEntity().getContent());
 		String catalogURI = ((Attribute) this.xpathHelper.select(
 				"//cmServiceProviders/@resource").get(0)).getValue();
 		response.getEntity().getContent().close();
 
 		// 2. get the catalog and extract the project areas
 		HttpResponse response2 = this.webConnector
 				.performHTTPSRequestXML(catalogURI);
 		this.xpathHelper.setDocument(response2.getEntity().getContent());
 		this.projectAreaList = this.xpathHelper.select("//ServiceProvider");
 		return this.projectAreaList;
 	}
 
 	@Override
 	public void setProjectArea(String projArea) {
 		this.selectedProjectArea = projArea;
 	}
 
 	@Override
 	public void setLimit(int limit) {
 		// if we have a change, we need to reset the results from a previous
 		// query.
 		if (this.limit != limit)
 			this.changeRequestsXML = null;
 
 		if (limit < 0)
 			this.limit = 0;
 		else
 			this.limit = limit;
 	}
 
 	@Override
 	public int getLimit() {
 		return this.limit;
 	}
 
 	public synchronized String[] getWorkitemsForType(String query)
 			throws JDOMException, IOException, Exception {
 		// 0. check if project area is set
 		if (this.selectedProjectArea == null) {
 			StringBuffer b = new StringBuffer();
 			for (String s : getProjectAreas()) {
 				b.append("<");
 				b.append(s);
 				b.append(">,");
 			}
 			throw new IllegalArgumentException(
 					"Select a project area first! Available: "
 							+ b.toString().substring(0, b.length() - 1));
 		}
 
 		// 1. get the services.xml for project area and obtain the query URI
 		String servicesURI = "";
 		for (Object o : getProjectAreaList()) {
 			Element e = ((Element) o);
 			if (this.selectedProjectArea.equals(e.getChild("title").getValue())) {
 				servicesURI = e.getChild("services").getAttribute("resource")
 						.getValue();
 				// System.out.println(servicesURI);
 			}
 		}
 		HttpResponse r = this.webConnector.performHTTPSRequestXML(servicesURI);
 		this.xpathHelper.setDocument(r.getEntity().getContent());
 
 		// 2. create a simple query according to
 		// http://open-services.net/bin/view/Main/CmQuerySyntaxV1
 		String simpleQueryURI = ((Element) this.xpathHelper.select(
 				"//simpleQuery/url").get(0)).getValue();
 		simpleQueryURI = simpleQueryURI.trim() + query;
 		// System.out.println("Query URL: "
 		// + URLDecoder.decode(simpleQueryURI, "UTF-8"));
 
 		// 3. make sense of the result and return it
 		String[] ret = getWorkitemsForQuery(simpleQueryURI);
 
 		return ret;
 	}
 
 	private String[] getWorkitemsForQuery(String simpleQueryURI)
 			throws Exception, JDOMException, IOException {
 		// System.out.println(simpleQueryURI);
 		HttpResponse r = this.webConnector
 				.performHTTPSRequestXML(simpleQueryURI);
 		this.changeRequestsXML = new XPathHelper();
 		this.changeRequestsXML.setDocument(r.getEntity().getContent());
 		List<Object> crElements = this.changeRequestsXML
 				.select("//ChangeRequest");
 		List<Object> moreQueryAttributes = this.changeRequestsXML
 				.select("//Collection/@next");
 		if (moreQueryAttributes.size() > 0)
 			this.moreQuery = ((Attribute) moreQueryAttributes.get(0))
 					.getValue();
 		else
 			this.moreQuery = null;
 		String[] ret = new String[crElements.size()];
 		for (int i = 0; i < ret.length; i++) {
 			Element e = (Element) crElements.get(i);
 			// System.out.println(e.getChild("type").getAttribute("resource").getValue());
 			ret[i] = convertElementToString(e);
 		}
 		return ret;
 	}
 
 	String convertElementToString(Element e) {
 		return convertElementToString("", e);
 	}
 
 	private String convertElementToString(String offset, Element e) {
 		StringBuffer ret = new StringBuffer(offset);
 		ret.append("<");
 		ret.append(e.getName());
 		for (Attribute a : e.getAttributes()) {
 			ret.append(' ');
 			ret.append(a.getName());
 			ret.append("=\"");
 			ret.append(a.getValue());
 			ret.append('"');
 		}
 		List<Element> childs = e.getChildren();
 		if (childs.size() == 0)
 			ret.append("/>");
 		else {
 			ret.append(">\n");
 			for (Element c : childs) {
 				ret.append(convertElementToString(offset + "  ", c));
 				ret.append('\n');
 			}
 			ret.append(offset);
 			ret.append("</");
 			ret.append(e.getName());
 			ret.append(">");
 		}
 
 		return ret.toString();
 	}
 
 	@Override
 	public void configure(Properties p) {
 		this.webConnector.configure(p);
 	}
 
 	@Override
 	public DiscussionEvent[] getDiscussionEventsOfDiscussion(int discussionId)
 			throws DAOException {
 		try {
 			// 1. make sure that the change requests are already loaded.
 			// Otherwise query them.
 			if (this.changeRequestsXML == null) {
 				getWorkitemsForType(STORY_BY_ID_QUERY + discussionId);
 			}
 
 			// 2. identify the change request with the discussion id and get the
 			// comment-url
 			String commentURL = ((Attribute) this.changeRequestsXML.select(
 					"//ChangeRequest[identifier=" + discussionId
 							+ "]/comments/@collref").get(0)).getValue();
 			// System.out.println(commentURL);
 
 			// 3. query the comments and transform them into DiscussionEvents
 			if (this.commentsXML == null) {
 				this.commentsXML = new XPathHelper();
 			}
 			this.commentsXML.setDocument(this.webConnector
 					.performHTTPSRequestXML(commentURL).getEntity()
 					.getContent());
 
 			List<Object> comments = this.commentsXML.select("//Comment");
 			DiscussionEvent[] ret = new DiscussionEvent[comments.size()];
 			for (int i = 0; i < ret.length; i++) {
 				ret[i] = new DiscussionEvent();
 				ret[i].setID(i);
 				ret[i].setDiscussionID(discussionId);
 				ret[i].setCreator(parseUserName(((Attribute) this.commentsXML
 						.select(comments.get(i), ".//creator/@resource").get(0))
 						.getValue()));
 				ret[i].setContent(((Element) this.commentsXML.select(
 						comments.get(i), ".//description").get(0)).getValue());
 				String dateString = ((Element) this.commentsXML.select(
 						comments.get(i), ".//created").get(0)).getValue();
 				ret[i].setCreationDate(Util.parseDate(dateString));
 			}
 
 			return ret;
 
 		} catch (JDOMException e) {
 			throw new DAOException("Could not parse XML [" + e.getMessage()
 					+ "]", e);
 		} catch (IOException e) {
 			throw new DAOException(
 					"Could read stream [" + e.getMessage() + "]", e);
 		} catch (Exception e) {
 			throw new DAOException("General error reading discussion events ["
 					+ e.getMessage() + "]", e);
 		}
 	}
 
 	private String parseUserName(String userString) {
 		return userString.substring(userString.lastIndexOf("/") + 1);
 	}
 
 	@Override
 	public DiscussionEvent getDiscussionEvent(int id) throws DAOException {
 		// that is not true, the have a resource-identifier. I am jsut not sure
 		// how to deal with that
 		throw new UnsupportedOperationException(
 				"Not supported: Jazz does not have comment ids.");
 	}
 
 	@Override
 	public void storeDiscussionEvent(DiscussionEvent de) throws DAOException {
 		throw new UnsupportedOperationException("Read only DAO!");
 	}
 
 	@Override
 	public void storeDiscussionEvents(DiscussionEvent[] des)
 			throws DAOException {
 		throw new UnsupportedOperationException("Read only DAO!");
 	}
 
 	@Override
 	public Discussion getDiscussion(int discussionID) throws DAOException {
 		try {
 			// 1. make sure that the changerequests are already loaded.
 			// Otherwise
 			// query them.
 			if (this.changeRequestsXML == null) {
 				getWorkitemsForType(STORY_BY_ID_QUERY + discussionID);
 			}
 
 			// 2. identify the changerequest with the discussionid and get the
 			// comment-url
 			Object discussionElement = this.changeRequestsXML.select(
 					"//ChangeRequest[identifier=" + discussionID + "]").get(0);
 
 			Discussion d = createDiscussion(discussionElement);
 			d.addDiscussionEvents(getDiscussionEventsOfDiscussion(d.getID()));
 
 			return d;
 		} catch (JDOMException e) {
 			throw new DAOException("Could not parse XML [" + e.getMessage()
 					+ "]", e);
 		} catch (IOException e) {
 			throw new DAOException(
 					"Could read stream [" + e.getMessage() + "]", e);
 		} catch (Exception e) {
 			throw new DAOException("General error reading discussion events ["
 					+ e.getMessage() + "]", e);
 		}
 	}
 
 	private Discussion createDiscussion(Object discussionElement)
 			throws DAOException {
 		Discussion d = DiscussionFactory.getInstance().getDiscussion(
 				Integer.parseInt(((Element) this.xpathHelper.select(
 						discussionElement, "identifier").get(0)).getValue()));
 
 		d.setCreationDate(Util.parseDate(((Element) this.xpathHelper.select(
 				discussionElement, "created").get(0)).getValue()));
 		String creator = ((Attribute) this.xpathHelper.select(
 				discussionElement, "creator/@resource").get(0)).getValue();
 		d.setCreator(parseUserName(creator));
 		d.setDescription(((Element) this.xpathHelper.select(discussionElement,
 				"description").get(0)).getValue());
 		d.setSummary(((Element) this.xpathHelper.select(discussionElement,
 				"title").get(0)).getValue());
 		d.setType(((Attribute) this.xpathHelper.select(discussionElement,
 				"type/@resource").get(0)).getValue());
 
 		return d;
 	}
 
 	@Override
 	public Discussion[] getDiscussions() throws DAOException {
 		return getDiscussions(IDAOProgressMonitor.NULL_PROGRESS_MONITOR);
 	}
 
 	@Override
 	public Discussion[] getDiscussions(IDAOProgressMonitor progressMonitor)
 			throws DAOException {
 		try {
 			progressMonitor.setTotalSteps(-1);
 			int step = 0;
 
 			// 1. make sure that the changerequests are already loaded.
			// Otherwise
			// query them.
 			progressMonitor.setStep(step++,
 					"Loading change requests from jazz.net");
 			if (this.changeRequestsXML == null) {
 				getWorkitemsForType(STORY_QUERY + getLimit());
 			}
 
 			// 2. identify the changerequest with the discussionid and get the
 			// comment-url
 			progressMonitor.setStep(step++, "Parsing change requests");
 			List<Object> discussionElements = this.changeRequestsXML
 					.select("//ChangeRequest");
 
 			Discussion[] ret = new Discussion[discussionElements.size()];
 			progressMonitor.setTotalSteps(2 * ret.length + 2);
 			for (int i = 0; i < ret.length; i++) {
 				progressMonitor.setStep(step++, "Creating Discussions");
 				ret[i] = createDiscussion(discussionElements.get(i));
 			}
 
 			for (Discussion d : ret) {
 				progressMonitor.setStep(step++, "Adding DiscussionEvents");
 				d.addDiscussionEvents(getDiscussionEventsOfDiscussion(d.getID()));
 			}
 
 			return ret;
 		} catch (JDOMException e) {
 			throw new DAOException("Could not parse XML [" + e.getMessage()
 					+ "]", e);
 		} catch (IOException e) {
 			throw new DAOException(
 					"Could read stream [" + e.getMessage() + "]", e);
 		} catch (Exception e) {
 			throw new DAOException("General error reading discussion events ["
 					+ e.getMessage() + "]", e);
 		}
 	}
 
 	@Override
 	public void storeDiscussion(Discussion d) {
 		throw new UnsupportedOperationException("Read only DAO!");
 	}
 
 	@Override
 	public void storeDiscussions(Discussion[] ds) {
 		throw new UnsupportedOperationException("Read only DAO!");
 	}
 
 	@Override
 	public Incident[] getIncidentsForDiscussion(int discussionID)
 			throws DAOException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void storeIncidents(Incident[] incidents) throws DAOException {
 		throw new UnsupportedOperationException("Read only DAO!");
 	}
 
 	@Override
 	public Discussion[] getMoreDiscussions(IDAOProgressMonitor progressMonitor)
 			throws DAOException {
 		try {
 			if (!hasMoreDiscussions())
 				return new Discussion[0];
 			progressMonitor.setTotalSteps(-1);
 			int step = 0;
 
 			// 1. execute the more-query
 			progressMonitor.setStep(step++,
 					"Loading change requests from jazz.net");
 			getWorkitemsForQuery(this.moreQuery);
 
 			// 2. identify the changerequest with the discussionid and get the
 			// comment-url
 			progressMonitor.setStep(step++, "Parsing change requests");
 			List<Object> discussionElements = this.changeRequestsXML
 					.select("//ChangeRequest");
 
 			Discussion[] ret = new Discussion[discussionElements.size()];
 			progressMonitor.setTotalSteps(2 * ret.length + 2);
 			for (int i = 0; i < ret.length; i++) {
 				progressMonitor.setStep(step++, "Creating Discussions");
 				ret[i] = createDiscussion(discussionElements.get(i));
 			}
 
 			for (Discussion d : ret) {
 				progressMonitor.setStep(step++, "Adding DiscussionEvents");
 				d.addDiscussionEvents(getDiscussionEventsOfDiscussion(d.getID()));
 			}
 
 			return ret;
 		} catch (JDOMException e) {
 			throw new DAOException("Could not parse XML [" + e.getMessage()
 					+ "]", e);
 		} catch (IOException e) {
 			throw new DAOException(
 					"Could read stream [" + e.getMessage() + "]", e);
 		} catch (Exception e) {
 			throw new DAOException("General error reading discussion events ["
 					+ e.getMessage() + "]", e);
 		}
 	}
 
 	@Override
 	public boolean hasMoreDiscussions() {
 		return this.moreQuery != null;
 	}
 
 	@Override
 	public Properties getConfiguration() {
 		return this.webConnector.getConfiguration();
 	}
 
 	@Override
 	public Map<String, String> checkConfiguration() {
 		// return this.webConnector.checkConfiguration();
 		// We have a different way to get the information from the user.
 		return new HashMap<String, String>();
 	}
 
 }
