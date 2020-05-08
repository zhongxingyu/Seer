 package om.devservlet.deployment;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import om.DisplayUtils;
 import om.RenderedOutput;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Provides the visual represenation of the different options avaliable for the
  *  deployment of the Question(s) selected. 
  * @author Trevor Hinson
  */
 
 public class QuestionDeploymentRenderer {
 
 	public static String DEPLOY_FILE_SUFFIX = ".jar";
 
 	public static String START_SELECTION_FORM = "<form id='deploy' method='post' action='deploy'>";
 
 	public static String END_SELECTION_FORM = "</form>";
 
 	public static String BRS = "<br /><br />";
 
 	public static String CONFIRMED_DEPLOYMENT = "confirmedDeployment";
 	
 	public static String DEPLOYMENT_RESULTS_PAGE_HEADING = "<h1>Deployment results.</h1><br /><br />";
 
 	public static String SUBMIT_BUTTON = "<br /><br /><input type='submit' name='action' id='submit' value='Copy To Server' />";
 
 	private static String NO_QUESTIONS_TO_DEPLOY = "There are currently no questions to deploy.";
 
 	private static String SELECTION_PAGE_HEADING = "<h1>Select the questions you would like deployed.</h1><br /><br />";
 
 	private static String DEPLOY_TO_HEADING = "<h3>Deploying To ...</h3>";
 
 	private static String CONFIRMATION_PAGE_HEADER = "<h1>Confirmation Page</h1>";
	
	private static String COPY_TO_SERVER="Click copy to server below to confirm.";
 
 	private RenderedOutput outputRendering;
 
 	private String deployableQuestionLocation;
 
 	public QuestionDeploymentRenderer(String location) {
 		deployableQuestionLocation = location;
 		outputRendering = new RenderedOutput();
 	}
 
 	/**
 	 * Runs through the questions that have been added by the Question developer
 	 *  into the configured location and then returns their name (minus the
 	 *  ".jar" suffix so that the Question Developer can choose which they 
 	 *  would like to deploy.
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	private Set<String> identifyDeployableQuestions() {
 		Set<String> deployable = new HashSet<String>();
 		if (StringUtils.isNotEmpty(deployableQuestionLocation)) {
 			File f = new File(deployableQuestionLocation);
 			if (f.exists() ? f.isDirectory() : false) {
 				File[] deploys = f.listFiles(new TestQuestionFilter());
 				for (int i = 0; i < deploys.length; i++) {
 					File fi = deploys[i];
 					if (null != fi ? fi.canRead() : false) {
 						String name = fi.getName().substring(0,
 							fi.getName().length() - 4);
 						deployable.add(name);
 					}
 				}
 			}
 		}
 		return deployable;
 	}
 
 	/**
 	 * Provides a html form rendering of the Questions that the Question
 	 *  Developer can select to deploy. 
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	private StringBuffer renderQuestionDefinitionCheckBoxes()
 		throws QuestionDeploymentException {
 		StringBuffer sb = new StringBuffer();
 		Set<String> quest = identifyDeployableQuestions();
 		if (null != quest ? quest.size() > 0 : false) {
 			outputRendering.append(START_SELECTION_FORM);
 			for (int i = 0; i < quest.size(); i++) {
 				String s = (String) quest.toArray()[i];
 				if (StringUtils.isNotEmpty(s)) {
 					outputRendering.append("<div><span class='fields'>")
 						.append("<input type=\"checkbox\" name=\"FILE_")
 						.append(i).append(" \" value=\"")
 						.append(s).append("\" />").append("</span>")						
 						.append("<span>").append(s).append("</span></div>");
 				}
 			}
 			outputRendering.append(SUBMIT_BUTTON);
 			outputRendering.append(END_SELECTION_FORM);
 		} else {
 			outputRendering.append(NO_QUESTIONS_TO_DEPLOY);
 		}
 		return sb;
 	}
 
 	/**
 	 * Used to render the actual page display where the user can select multiple
 	 *  questions to deploy.
 	 * @return
 	 * @throws QuestionDeploymentException
 	 * @author Trevor Hinson
 	 */
 	public RenderedOutput renderSelection() throws QuestionDeploymentException {
 		outputRendering.append(DisplayUtils.header())
 			.append(SELECTION_PAGE_HEADING)
 			.append(renderQuestionDefinitionCheckBoxes()).append(BRS)
 			.append(DisplayUtils.applyListingLinkDisplay())
 			.append(DisplayUtils.footer());
 		return outputRendering;
 	}
 
 
 
 	/**
 	 * Simple filter for the types of files we are dealing with.
 	 * @author Trevor Hinson
 	 */
 	class TestQuestionFilter implements FileFilter {
 
 		@Override
 		public boolean accept(File f) {
 			return null != f ? f.getName().endsWith(DEPLOY_FILE_SUFFIX) : false;
 		}
 		
 	}
 
 	/**
 	 * Renders out the confirmation page to the Question Developer requesting
 	 *  confirmation of the action before it is carried out.
 	 * @param names
 	 * @param metaData
 	 * @return
 	 * @author Trevor Hinson
 	 */
 	public RenderedOutput requestConfirmation(List<String> names,
 		Map<String, String> metaData) {
 		RenderedOutput or = new RenderedOutput();
 		or.append(DisplayUtils.header()).append(CONFIRMATION_PAGE_HEADER);
 		or.append(renderConfirmationRequest());
 		boolean renderSubmitButton = false;
 		if (null != names) {
 			renderSubmitButton = true;
 			or.append(renderSelectedQuestions(names));
 		} else {
 			or.append(renderNoQuestionsSelected());
 		}
		//or.append(DEPLOY_TO_HEADING).append(QuestionDeploymentRenderer.BRS);
		// lose the extra breaks
		or.append(DEPLOY_TO_HEADING);
 		List<String> locations = DisplayUtils.getLocations(metaData);
 		if (null != locations ? locations.size() > 0 : false) {
 			or.append(renderDeployToLocations(locations));
 		} else {
 			renderSubmitButton = false;
 			or.append(renderNoLocations());
 		}
 		if (renderSubmitButton) {
 			or.append(renderConfirmationSubmission());
 		}
 		or.append(QuestionDeploymentRenderer.BRS)
 			.append(DisplayUtils.applyListingLinkDisplay()).append(DisplayUtils.footer());
 		return or;
 	}
 
 	private String renderConfirmationRequest() {
 		return new StringBuffer().append(QuestionDeploymentRenderer.BRS)
 			.append("Please confirm that you")
 			.append(" wish to deploy the following to the specified locations : ")
 			.append(QuestionDeploymentRenderer.BRS).toString();
 	}
 
 	private String renderNoQuestionsSelected() {
 		return new StringBuffer().append("<b>Unable to identify the")
 			.append(" Questions you wish to deploy.")
 			.append("Please try again.</b>")
 			.append(QuestionDeploymentRenderer.BRS).toString();
 	}
 
 	private String renderNoLocations() {
 		return new StringBuffer().append("<b>There are no deployment locations")
 			.append(" specified at this time.")
 			.append(" Please check the configuration.</b>").toString();
 	}
 
 	private String renderConfirmationSubmission() {
 		return new StringBuffer()
			//.append("<div class=\"alert\">Click copy to server below to confirm.</div>")
			.append(COPY_TO_SERVER)
 			.append(QuestionDeploymentRenderer.START_SELECTION_FORM)
 			.append("<input type=\"hidden\" name=\"")
 			.append(CONFIRMED_DEPLOYMENT)
 			.append("\" value=\"true\" />")
 			.append(QuestionDeploymentRenderer.SUBMIT_BUTTON)
 			.append(QuestionDeploymentRenderer.END_SELECTION_FORM).toString();
 	}
 
 	private String renderDeployToLocations(List<String> locations) {
 		StringBuffer sb = new StringBuffer("<ul>");
 		for (String location : locations) {
 			sb.append("<li>").append(location).append("</li>");
 		}
 		sb.append("</ul>");
 		return sb.toString();
 	}
 
 	private String renderSelectedQuestions(List<String> names) {
 		StringBuffer sb = new StringBuffer("<ul>");
 		for (String name : names) {
 			sb.append("<li>").append(name).append("</li>");
 		}
 		sb.append("</ul>");
 		return sb.toString();
 	}
 
 }
