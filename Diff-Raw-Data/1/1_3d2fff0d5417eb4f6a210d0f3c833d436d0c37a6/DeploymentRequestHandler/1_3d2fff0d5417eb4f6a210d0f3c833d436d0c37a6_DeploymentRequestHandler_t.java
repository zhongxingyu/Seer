 package om.devservlet.deployment;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import om.DisplayUtils;
 import om.Log;
 import om.RenderedOutput;
 import om.RequestAssociates;
 import om.RequestHandler;
 import om.RequestHandlingException;
 import om.RequestResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.w3c.dom.Element;
 
 import util.misc.FinalizedResponse;
 import util.misc.GeneralUtils;
 import util.misc.StandardFinalizedResponse;
 import util.misc.UtilityException;
 import util.xml.XML;
 
 /**
  * Used to handle the actions requested by the Question Developer for the
  *  actual deployment of the question(s) to the configured location(s). 
  * @author Trevor Hinson
  */
 
 public class DeploymentRequestHandler implements RequestHandler {
 
 	public static String TRANSPORTER = "QuestionTransporter";
 
 	private static final long serialVersionUID = 732317370986475718L;
 
 	private static String LOCATION = "location";
 
 	private static String QUESTIONS_PATH = "/questions";
 
 	private static String QUERY_STRING_CLEARANCE = "clear=true";
 
 	private static String FILE_KEY_PREFIX = "FILE_";
 
 	private static String DEPLOYMENT_CHOICES = "deploymentChoices";
 
 	private static String DEPLOY = "deploy";
 
 	private RequestAssociates requestAssociates;
 
 	private HttpServletRequest request;
 
 	private QuestionDeploymentRenderer questionDeploymentRenderer;
 
 	private String deployableQuestionLocation;
 
 	private Log log;
 
 	/**
 	 * Caters for the deploy option within the Question Developer.  Based on
 	 *  what the Question Developer has actually done we either display the
 	 *  question selection OR actually carry out the deploy of the selected
 	 *  Questions to the configured location. 
 	 * @param context
 	 * @param request
 	 * @param response
 	 * @param associates
 	 * @throws RequestHandlingException
 	 * @author Trevor Hinson
 	 */
 	public RequestResponse handle(HttpServletRequest req,
 		HttpServletResponse res, RequestAssociates associates)
 		throws RequestHandlingException {
 		if (null != req && null != res && null != associates ? associates.valid() : false) {
 			requestAssociates = associates;
 			setUpDeployableQuestionLocation(associates);
			request = req;
 			try {
 				setUpLog();
 				questionDeploymentRenderer = new QuestionDeploymentRenderer(
 					deployableQuestionLocation);
 				establishChoices();
 				List<String> names = pickUpDeploymentChoicesFromSession();
 				return doDeploy() ? deploy(names) : renderPage(names);
 			} catch (QuestionDeploymentException x) {
 				throw new RequestHandlingException(x);
 			}
 		} else {
 			throw new RequestHandlingException("The arguments did not match"
 				+ " what is required by this method.  No object can be null"
 				+ " and the RequestAssociate must be valid : "
 				+ "\n HttpServletRequest = " + req
 				+ "\n HttpServletResponse = " + res
 				+ "\n RequestAssociates = " + associates);
 		}
 	}
 
 	private void setUpDeployableQuestionLocation(RequestAssociates associates)
 		throws RequestHandlingException {
 		if (null != associates ? null != associates.getServletContext() : false) {
 			deployableQuestionLocation = associates.getServletContext()
 				.getRealPath(QUESTIONS_PATH);
 		} else {
 			throw new RequestHandlingException("Unable to continue :"
 				+ " RequestAssociates : " + associates);
 		}
 	}
 
 	private void setUpLog() throws QuestionDeploymentException {
 		Map<String, String> metaData = retrieveDeployMetaData();
 		if (null != metaData) {
 			String path = metaData.get(DeploymentEnum.HandleDeployLogTo.toString());
 			String debug = metaData.get(DeploymentEnum.HandleDeployShowDebug.toString());
 			if (StringUtils.isNotEmpty(path)) {
 				try {
 					log = GeneralUtils.getLog(getClass(), path,
 						"true".equalsIgnoreCase(debug) ? true : false);
 				} catch (UtilityException x) {
 					x.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Wraps the logging in order to first check that the Log object has been
 	 *  setup properly.
 	 * @param message
 	 * @param t
 	 * @param debug
 	 * @author Trevor Hinson
 	 */
 	private void log(String message, Throwable t, boolean debug) {
 		if (null != log) {
 			if (null != t) {
 				log.logError(message, t);
 			} else {
 				if (debug) {
 					log.logDebug(message);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determines the next page to show the Question builder based on their
 	 *  previous action.
 	 * @param names
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	private RenderedOutput renderPage(List<String> names)
 		throws QuestionDeploymentException {
 		return hasSelectionRequiringConfirmation()
 			? questionDeploymentRenderer.requestConfirmation(names,retrieveDeployMetaData())
 				: questionDeploymentRenderer.renderSelection();
 	}
 
 	/**
 	 * Simple check to see if we have any choices within the Session.
 	 * @return true if there are.
 	 * @author Trevor Hinson
 	 */
 	private boolean hasSelectionRequiringConfirmation() {
 		boolean hasSelection = false;
 		List<String> choices = pickUpDeploymentChoicesFromSession();
 		if (null != choices ? choices.size() > 0 : false) {
 			hasSelection = true;
 		}
 		return hasSelection;
 	}
 
 	/**
 	 * Here we simply identify if we are carrying out the actual deploy with
 	 *  this request or not.
 	 * @param post
 	 * @param sPath
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	private boolean doDeploy() {
 		boolean deploy = false;
 		if (hasConfirmedDeployment()) {
 			log("Confirmation has been made for the deployment so continuing",
 				null, true);
 			if (requestAssociates.getPost()) {
 				deploy = true;
 			} else {
 				deploy = null != determineQuestionFromPath();
 			}
 		} else {
 			log("We have not confirmed the deployment so not continuing.",
 				null, true);
 		}
 		return deploy;
 	}
 
 	/**
 	 * Simply determines the Question name from the URL itself.
 	 * @param sPath
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	String determineQuestionFromPath() {
 		String questionFromPath = null;
 		String sPath = requestAssociates.getPath();
 		if (StringUtils.isNotEmpty(sPath)) {
 			String s = sPath;
 			if (s.endsWith("/")) {
 				s = s.substring(0, s.length() -1);
 			}
 			int n = s.lastIndexOf("/");
 			if (n > -1 ? s.length() > n + 1 : false) {
 				s = s.substring(n + 1, s.length());
 				if (!DEPLOY.equalsIgnoreCase(s)) {
 					questionFromPath = s;
 				}
 			}
 		}
 		return questionFromPath;
 	}
 
 	/**
 	 * Determines the Question(s) that the developer wishes to deploy from the
 	 *  request.
 	 * @param sPath
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	@SuppressWarnings("unchecked")
 	private List<String> pickUpDeploymentChoices() {
 		List<String> names = new ArrayList<String>();
 		Map m = request.getParameterMap();
 		String queryString = request.getQueryString();
 		if (!QUERY_STRING_CLEARANCE.equalsIgnoreCase(queryString)) {
 			if (null != m) {
 				for (Object key : m.keySet()) {
 					if (null != key) {
 						if (key instanceof String) {
 							if (((String) key).startsWith(FILE_KEY_PREFIX)) {
 								names.add(request.getParameter((String) key));
 							}
 						}
 					}
 				}
 			}
 			String name = determineQuestionFromPath();
 			if (StringUtils.isNotEmpty(name)
 				? !DEPLOY.equalsIgnoreCase(name) : false) {
 				names.add(name);
 			}
 		} else {
 			clearDeploymentChoices();
 		}
 		return names;
 	}
 
 	/**
 	 * Based on the selected Question(s) we then actually try deploying the
 	 *  Question(s) using the associated QuestionDeployment.
 	 * @param qd
 	 * @param sPath
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	private RenderedOutput deploy(List<String> names)
 		throws RequestHandlingException {
 		try {
 			return hasConfirmedDeployment()
 				? doDeployment(names)
 					: questionDeploymentRenderer.requestConfirmation(names,
 						retrieveDeployMetaData());
 		} catch (QuestionDeploymentException x) {
 			throw new RequestHandlingException(x);
 		}
 	}
 
 	/**
 	 * Handles delegating to the actual deployment handler.  Before doing so 
 	 *  we tidy up the Session.
 	 * @param names
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	private RenderedOutput doDeployment(List<String> names)
 		throws QuestionDeploymentException {
 		clearDeploymentChoices();
 		return doDeployment(names, retrieveDeployMetaData());
 	}
 
 	/**
 	 * Iterates over the Questions selected by the Question developer for
 	 *  actual deployment.  For each question we try to deploy it.
 	 * @param names The questions selected for deployment.
 	 * @param metaData
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	RenderedOutput doDeployment(List<String> names,
 		Map<String, String> metaData) throws QuestionDeploymentException {
 		RenderedOutput ro = new RenderedOutput();
 		log("Carrying out the deployment of " + names, null, true);
 		ro.append(DisplayUtils.header())
 			.append(QuestionDeploymentRenderer.DEPLOYMENT_RESULTS_PAGE_HEADING);
 		if (null != names ? names.size() > 0 && null != metaData : false) {
 			for (String name : names) {
 				if (StringUtils.isNotEmpty(name)) {
 					log("Handling : " + name, null, true);
 					QuestionHolder qh = generateQuestionHolder(name, metaData);
 					if (null != qh) {
 						RenderedOutput output = deploy(qh, metaData);
 						ro.append(output.toString())
 							.append(QuestionDeploymentRenderer.BRS);
 					}
 				}
 			}
 		}
 		ro.append(QuestionDeploymentRenderer.BRS)
 			.append(DisplayUtils.applyListingLinkDisplay())
 			.append(DisplayUtils.footer());
 		return ro;
 	}
 
 	/**
 	 * Instantiates the configured QuestionTransporter and then invokes the
 	 *  deploy method on it passing though the QuestionHolder argument and
 	 *  metaData.  Returning the rendering to display to the Question Developer.
 	 * @param qh
 	 * @param metaData
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	private RenderedOutput deploy(QuestionHolder qh,
 		Map<String, String> metaData) throws QuestionDeploymentException {
 		RenderedOutput or = new RenderedOutput();
 		try {
 			log("Picking up the QuestionTransporter ...", null, true);
 			QuestionTransporter qt = GeneralUtils.loadComponent(
 				QuestionTransporter.class, metaData.get(TRANSPORTER));
 			qt.deploy(qh, metaData, or);
 			qt.close(null);
 		} catch (UtilityException x) {
 			throw new QuestionDeploymentException(x);
 		}
 		return or;
 	}
 
 	/**
 	 * Creates a QuestionHolder from the details of a given Question picked
 	 *  up from the Question Developers selection.
 	 * @param name
 	 * @param metaData
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	private QuestionHolder generateQuestionHolder(String name,
 		Map<String, String> metaData) throws QuestionDeploymentException {
 		QuestionHolder qh = null;
 		String file = deployableQuestionLocation + "/" + name;
 		String xml = file + ".xml";
 		String jar = file + ".jar";
 		File x = new File(xml);
 		File j = new File(jar);
 		if (x.exists() && j.exists()) {
 			qh = new QuestionHolder(x, j, metaData, name,
 				new File(deployableQuestionLocation + "/"));
 		} else {
 			throw new QuestionDeploymentException("Unable to deploy the question : " 
 				+ name + " as either the associated Jar or the Xml file was missing.");
 		}
 		return qh;
 	}
 
 	/**
 	 * Simply removes the users choices from the Session.
 	 * @author Trevor Hinson
 	 */
 	private void clearDeploymentChoices() {
 		request.getSession().removeAttribute(DEPLOYMENT_CHOICES);
 	}
 
 	/**
 	 * Picks up the deployment choices from the Session.  We check the contents
 	 *  of the Session object also in case they have change from what we expect.
 	 * @param request
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	private List<String> pickUpDeploymentChoicesFromSession() {
 		List<String> choices = null;
 		Object obj = request.getSession().getAttribute(DEPLOYMENT_CHOICES);
 		// because things change in shared objects ... we check things ...
 		if (null != obj ? obj instanceof List<?> : false) {
 			for (Object o : (List<?>) obj) {
 				if (null != o ? o instanceof String
 					? StringUtils.isNotEmpty((String) o) : false : false) {
 					if (null == choices) {
 						choices = new ArrayList<String>();
 					}
 					choices.add((String) o);
 				}
 			}
 		}
 		return choices;
 	}
 
 	/**
 	 * Check that the user has confirmed the deployment action.
 	 * @return true IF confirmed
 	 * @author Trevor Hinson
 	 */
 	private boolean hasConfirmedDeployment() {
 		Object value = request.getParameter(QuestionDeploymentRenderer.CONFIRMED_DEPLOYMENT);
 		boolean confirmed = false;
 		if (null != value ? value instanceof String : false)
 			if("true".equalsIgnoreCase((String) value)) {
 			confirmed = true;
 		}
 		return confirmed;
 	}
 
 	/**
 	 * Simply places the users choices into the session for retrieval on the
 	 *  confirmation page and for working with later on.
 	 * @param request
 	 * @param choices
 	 * @author Trevor Hinson
 	 */
 	private void establishChoices() {
 		List<String> choices = pickUpDeploymentChoices();
 		if (null != choices ? choices.size() > 0 : false) {
 			request.getSession().setAttribute(DEPLOYMENT_CHOICES, choices);
 		}
 	}
 
 	/**
 	 * Puts together the necessary items for deploying Questions.
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	private Map<String, String> retrieveDeployMetaData()
 		throws QuestionDeploymentException {
 		Map<String, String> metaData = new HashMap<String, String>();
 		for (String key : requestAssociates.getConfiguration().keySet()) {
 			Object obj = requestAssociates.getConfiguration().get(key);
 			if (StringUtils.isNotEmpty(key)) {
 				if (null != obj ? obj instanceof Element : false) {
 					if (XML.hasChild((Element) obj, LOCATION)) {
 						Element[] e = XML.getChildren((Element) obj, LOCATION);
 						if (null != e? e.length > 0 : false) {
 							for (int i = 0; i < e.length; i++) {
 								Element ele = e[i];
 								if (null != ele) {
 									String txt = XML.getText(ele);
 									metaData.put(LOCATION + i, txt);
 								}
 							}
 						}
 					} else {
 						String txt = XML.getText((Element) obj);
 						metaData.put(key, txt);
 					}
 				}
 			}
 		}
 		return metaData;
 	}
 
 	@Override
 	public FinalizedResponse close(Object o) throws UtilityException {
 		if (null != log) {
 			log.close();
 		}
 		return new StandardFinalizedResponse(true);
 	}
 }
