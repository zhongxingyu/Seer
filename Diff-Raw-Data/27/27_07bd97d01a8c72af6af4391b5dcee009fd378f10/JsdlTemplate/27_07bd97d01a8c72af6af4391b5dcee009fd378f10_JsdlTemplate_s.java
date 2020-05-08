 
 
 package org.vpac.grisu.client.model.template;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.client.control.EnvironmentManager;
 import org.vpac.grisu.client.control.ServiceInterfaceFactoryOld;
 import org.vpac.grisu.client.control.exceptions.JobSubmissionException;
 import org.vpac.grisu.client.model.template.modules.TemplateModule;
 import org.vpac.grisu.client.model.template.modules.TemplateModuleFactory;
 import org.vpac.grisu.client.model.template.modules.TemplateModuleProcessingException;
 import org.vpac.grisu.client.model.template.nodes.DefaultTemplateNodeValueSetter;
 import org.vpac.grisu.client.model.template.nodes.TemplateNode;
 import org.vpac.grisu.client.model.template.nodes.TemplateNodeEvent;
 import org.vpac.grisu.client.model.template.nodes.TemplateNodeListener;
 import org.vpac.grisu.client.model.template.nodes.templatePreProcessor.TemplatePreProcessorException;
 import org.vpac.grisu.client.model.template.postprocessor.ElementPostprocessor;
 import org.vpac.grisu.client.model.template.postprocessor.PostProcessException;
 import org.vpac.grisu.client.model.template.postprocessor.PostprocessorFactory;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.JobPropertiesException;
 import org.vpac.grisu.control.exceptions.NoSuchJobException;
 import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
 import org.vpac.grisu.frontend.control.login.LoginParams;
 import org.vpac.grisu.model.dto.DtoJob;
 import org.vpac.grisu.utils.DebugUtils;
 import org.vpac.grisu.utils.SeveralXMLHelpers;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import au.org.arcs.mds.Constants;
 import au.org.arcs.mds.JSDLNamespaceContext;
 import au.org.arcs.mds.JsdlHelpers;
 
 /**
  * A JsdlTemplate represents the logic that is behind a template file that can
  * be found in $HOME/.grisu/template (on the client) or
  * $TOMCAT_USER/.grisu/templates_available (on the server). It's main elements
  * are {@link TemplateNode}s, which represent non-fixed parameters of a job
  * submission (like submission host, walltime, etc). It can also contain
  * {@link TemplateModule}s, which are a logical grouping of several
  * {@link TemplateNode}s, for example to display all of them on one panel that
  * is specifically written for a certain application.
  * 
  * @author Markus Binsteiner
  * 
  */
 public class JsdlTemplate implements TemplateNodeListener {
 
 	public static final int DEFAULT_JOBNAME_CREATION_METHOD = JobConstants.DONT_ACCEPT_NEW_JOB_WITH_EXISTING_JOBNAME;
 
 	private Thread submissionThread = null;
 	
 	public static final String NO_VALID_FQAN = "No valid fqan";
 	
 	public static final int STATUS_SUBMISSION_FAILED = -1;
 	public static final int STATUS_JSDL_CREATED = 0;
 	public static final int STATUS_MODULES_CREATED = 1;
 	public static final int STATUS_TEMPLATENODES_CREATED = 2;
 	public static final int STATUS_SUBMISSION_STARTED = 10;
 	public static final int STATUS_TEMPLATE_NODES_FILLED = 11;
 	public static final int STATUS_TEMPLATENODES_PROCESSED = 12;
 	public static final int STATUS_JOB_CREATED = 13;
 	// from now on changes to the xml docment don't affect any template nodes or 
 	// template modules anymore, only the raw xml document is changed
 	public static final int STATUS_MODULES_PROCESSED = 14;
 	public static final int STATUS_POSTPROCESSORS_EXECUTED = 15;
 	public static final int STATUS_JOB_JSDL_DESCRIPTION_STORED = 16;
 	public static final int STATUS_JOB_SUBMITTED = 17;
 	public static final int STATUS_JOB_PROPERTIES_STORED = 18;
 	public static final int STATUS_JOB_SUBMISSION_SUCCESSFUL = 19;
 
 	private int status = -1;
 
 	private static XPath xpath = getXPath();
 
 	private static final String MODULEBASEPATH = "org.vpac.grisu.client.model.template.modules";
 
 	private static final XPath getXPath() {
 		XPath xpath = XPathFactory.newInstance().newXPath();
 		xpath.setNamespaceContext(new JSDLNamespaceContext());
 		return xpath;
 	}
 
 	static final Logger myLogger = Logger.getLogger(JsdlTemplate.class
 			.getName());
 
 	private EnvironmentManager em = null;
 
 	private Document template = null;
 	private Map<String, TemplateNode> templateNodes = null;
 	private Map<String, TemplateModule> templateModules = null;
 	private List<ElementPostprocessor> elementPostprocessors = null;
 
 	private String currentJobname = null;
 	private String currentSubmissionSite = null;
 	private String currentRemoteJobDirectory = null;
 	private String currentFqan = NO_VALID_FQAN;
 	private String currentExecutionHostFileSystem = null;
 	private String currentRelativeJobDirectory = null;
 	private String currentSubmissionLocation = null;
 
 	private int jobCreationMethod = DEFAULT_JOBNAME_CREATION_METHOD;
 
 	// store properties in this variable. they get stored to the remote database
 	// after successfully submitting the job
 	private Map<String, String> jobProperties = null;
 
 	public JsdlTemplate(EnvironmentManager em, Document template) {
 		
 		this.em = em;
 		this.template = template;
 		this.jobProperties = new TreeMap<String, String>();
 		setStatus(STATUS_JSDL_CREATED);
 		
 	}
 
 	public Document getTemplateDocument() {
 		return this.template;
 	}
 	
 
 	/**
 	 * Returns the name of the application this template submits job for
 	 * 
 	 * @return the name of the application
 	 */
 	public String getApplicationName() {
 		return JsdlHelpers.getApplicationName(template);
 	}
 
 	/**
 	 * This method returns a list of all modules that should be used to render
 	 * this jsdl template on the ui. A module claims responsible for a certain
 	 * amount of (most of the times special) template tags and displays it in a
 	 * more consistent and user friendly way. It's basically a jpanel that is
 	 * heavily costumized for a certain application.
 	 * 
 	 * @return a list of all the modules that should be used to render this jsdl
 	 *         template on the ui
 	 */
 	public Map<String, TemplateModule> getModules() {
 
 		if (templateModules == null) {
 			templateModules = TemplateModuleFactory.createTemplateModules(this,
 					MODULEBASEPATH);
 			myLogger.debug("Found modules: ");
 			for ( String mod : templateModules.keySet() ) {
 				myLogger.debug(mod);
 			}
 			setStatus(STATUS_MODULES_CREATED);
 		}
 		return templateModules;
 	}
 
 	/**
 	 * Returns a list of all nodes with template tags in them. These have to be
 	 * displayed to the user and the input may have to be processed after user
 	 * input.
 	 * 
 	 * @param jsdl
 	 *            the jsdl template document
 	 * @return a map of all elements that have template tags in them
 	 */
 	public Map<String, TemplateNode> getTemplateNodes() {
 
 		if (templateNodes == null) {
 
 			String expression = "//@template";
 			NodeList resultNodes = null;
 			try {
 				resultNodes = (NodeList) xpath.evaluate(expression, template,
 						XPathConstants.NODESET);
 			} catch (XPathExpressionException e) {
 				myLogger.warn("No JobName node in jsdl file yet.");
 				// that's ok if we want to set the jobname
 			}
 
 			templateNodes = new TreeMap<String, TemplateNode>();
 			for (int i = 0; i < resultNodes.getLength(); i++) {
 
 				Attr node = (Attr) resultNodes.item(i);
 
 				Element element = node.getOwnerElement();
 				TemplateNode tempNode = new TemplateNode(this, element);
 				tempNode.addTemplateNodeListener(this);
 				templateNodes.put(tempNode.getName(), tempNode);
 			}
 
 			setStatus(STATUS_TEMPLATENODES_CREATED);
 		}
 		return templateNodes;
 	}
 
 	/**
 	 * Returns a list of all the postprocessors that are referenced in this
 	 * template. Only call this method after the status is STATUS_POSTPROCESSORS_EXECUTED - 1
 	 * 
 	 * @return a list of all postprocessors
 	 * @throws JobSubmissionException 
 	 */
 	private List<ElementPostprocessor> getPostprocessors() throws JobSubmissionException {
 
 		// we only allow postprocessors to be created when they are needed
 //		if (status < STATUS_JOB_CREATED ) {
 //			myLogger.warn("Job not yet created on the server. Why do you need postprocessors? Canceling job submission.");
 //			elementPostprocessors = null;
 //			throw new JobSubmissionException("Postprocessors created before job is created on the server. Cancelling job sumbission.", null);
 //		}
 		
 		if ( elementPostprocessors == null ) {
 
 		String expression = "//@postprocess";
 		NodeList resultNodes = null;
 		try {
 			resultNodes = (NodeList) xpath.evaluate(expression, template,
 					XPathConstants.NODESET);
 		} catch (XPathExpressionException e) {
 			myLogger.warn("No postprocessor attributes in jsdl file yet.");
 			// that's ok if we want to set the jobname
 		}
 
 		elementPostprocessors = new ArrayList<ElementPostprocessor>();
 		for (int i = 0; i < resultNodes.getLength(); i++) {
 
 			Attr node = (Attr) resultNodes.item(i);
 
 			Element element = node.getOwnerElement();
 			ArrayList<ElementPostprocessor> tempPostprocessors = PostprocessorFactory
 					.createPostprocessors(this, element);
 			if (tempPostprocessors != null)
 				elementPostprocessors.addAll(tempPostprocessors);
 		}
 		}
 
 		return elementPostprocessors;
 	}
 	
 	/**
 	 * Returns the number of postprocessors if the status of the submission >= STATUS_JOB_CREATED. Else -1.
 	 * @return the number of postprocessors of this template or -1 if they are not yet parsed.
 	 */
 	public int getNumberOfPostprocessors() {
 		if ( this.status >= STATUS_JOB_CREATED ) {
 			try {
 				return getPostprocessors().size();
 			} catch (JobSubmissionException e) {
 				return -1;
 			}
 		} else {
 			return -1;
 		}
 	}
 	/**
 	 * This is the central method of this class. Use it to submit the job.
 	 * 
 	 * It is wrapped in a thread so it can be cancelled.
 	 * 
 	 * The class will try to fill the required values in the
 	 * {@link TemplateNode}s, preprocess them if needed, substitute the
 	 * variables, create the job on the remote grisu service, postprocess
 	 * everything that needs postprocessing, stores the job description remotely
 	 * and finally submitts the job. The last thing this method does is storing
 	 * possible job properties in the remote job database.
 	 * 
 	 * @param fqan
 	 *            the fqan that should be used to submit the job
 	 * @throws JobSubmissionException if a submission using this template is already in progress
 	 * 
 	 * @throws JobSubmissionException
 	 *             if the job is not ready or something else goes wrong.
 	 */
 	public void startSubmission(String fqan) throws JobSubmissionException {
 		
 		if ( this.status >= STATUS_SUBMISSION_STARTED ) 
 			throw new JobSubmissionException("Submission already in progress.", null);
 		
 		setStatus(STATUS_SUBMISSION_STARTED);
 		
 		if ( NO_VALID_FQAN.equals(fqan) )
 			throw new JobSubmissionException("Could not set a valid submission fqan.", null);
 			
 		this.currentFqan = fqan;
 	    
 //		if ( newThread ) {
 		if (submissionThread == null) {
 	        submissionThread = new Thread() {
 	          public void run() {
 	        	  try {
 
 					submit();
 
 					setStatus(STATUS_JOB_SUBMISSION_SUCCESSFUL);
 
 //					em.getJobManagement().refreshJobList();
 //					resetStatus();
 				} catch (JobSubmissionException e) {
 					setSubmissionErrorStatus(e);
 //					setSubmissionErrorStatus(e);
 					
 					return;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 	          }
 	        };
 			submissionThread.start();
 
 	    } 
 //		} else {
 //			submit();
 //		}
 		
 //		try {
 //			submit(fqan);
 //		} catch (JobSubmissionException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 		
 	}
 
 
 	private void submit() throws JobSubmissionException {
 		
 		try {
 		
 		if ( NO_VALID_FQAN.equals(this.currentFqan) ) 
 			throw new JobSubmissionException("Could not find a valid submission fqan.", null);
 		
 		ArrayList<TemplateNode> notReadyNodes = new ArrayList<TemplateNode>();
 
 		for (TemplateNode node : getTemplateNodes().values()) {
 			if ( Thread.interrupted() ) {
 				throw new JobSubmissionException("Job cancelled.", null);
 			}
 			if (!node.isReady()) {
 				notReadyNodes.add(node);
 			}
 		}
 
 		if (notReadyNodes.size() > 0) {
 
 			throw new JobSubmissionException(
 					"Could not submit job because of missing user input.",
 					new TemplateFillingException(notReadyNodes));
 		}
 
 		setStatus(STATUS_TEMPLATE_NODES_FILLED);
 		myLogger.info("All template nodes filled. preprocessing them now...");
 		// preprocess all nodes
 
 		ArrayList<TemplateNode> failedNodes = new ArrayList<TemplateNode>();
 
 		for (TemplateNode node : getTemplateNodes().values()) {
 			if ( Thread.interrupted() ) {
 				throw new JobSubmissionException("Job cancelled.", null);
 			}
 			try {
 				node.process();
 			} catch (TemplatePreProcessorException e) {
 				myLogger.error("Can't process node: " + node.getName());
 				failedNodes.add(node);
 			}
 		}
 
 		if (failedNodes.size() > 0) {
 
 			throw new JobSubmissionException(
 					"Could not submit job because of error while filling the template.",
 					new TemplateFillingException(failedNodes));
 		}
 
 		setStatus(STATUS_TEMPLATENODES_PROCESSED);
 
 		myLogger
 				.info("Preprocessing successfully finished. Substituting variables now...");
 
 		if ( Thread.interrupted() ) {
 			throw new JobSubmissionException("Job cancelled.", null);
 		}
 		
 		myLogger.info("Processing postprocessor, part 1.");
 		for (ElementPostprocessor postprocessor : getPostprocessors()) {
 			if ( Thread.interrupted() ) {
 				throw new JobSubmissionException("Job cancelled.", null);
 			}
 			
 			if ( postprocessor.processBeforeJobCreation() ) {
 			try {
 				postprocessor.process(this.currentFqan);
 			} catch (PostProcessException e) {
 
 				throw new JobSubmissionException(
 						"Could not submit job because postprocessing of the template failed.",
 						e);
 			}
 			}
 		}
 
 		// create job on server
 		myLogger.info("Creating job on server backend...");
 		currentJobname = JsdlHelpers.getJobname(template);
 		try {
 			currentJobname = em.getServiceInterface().createJobUsingJsdl(SeveralXMLHelpers.toString(getTemplateDocument()),
 					this.currentFqan, ServiceInterface.FORCE_NAME_METHOD);
 		} catch (RuntimeException e1) {
 
 			throw new JobSubmissionException("Could not create job: "
 					+ e1.getLocalizedMessage(), e1);
 		} catch (JobPropertiesException e) {
 			throw new JobSubmissionException("Could not create job on backend: "+e.getLocalizedMessage(), e);
 		}
 
 		try {
 			JsdlHelpers.setJobname(template, currentJobname);
 		} catch (XPathExpressionException e1) {
 			try {
 
 				myLogger
 						.error("Could not set new Jobname which is odd. Clearing newly created job from remote service.");
 				em.getServiceInterface().kill(currentJobname, true);
 			} catch (Exception e) {
 				// not that important
 			}
 		}
 
 		setStatus(STATUS_JOB_CREATED);
 		
 		for ( TemplateModule mod : getModules().values() ) {
 			try {
 				mod.process();
 			} catch (TemplateModuleProcessingException e) {
 				throw new JobSubmissionException("Could not process module "+mod.getModuleName()+".", e);
 			}
 		}
 		setStatus(STATUS_MODULES_PROCESSED);
 //		substituteVariables();
 //		myLogger.info("Substitution finished.");
 //
 //		this.status = STATUS_VARIABLES_SUBSTITUTED;
 
 		// this is so that postprocessors have easy access to site, fqan and
 		// jobdirectory. only possible when status >
 		// STATUS_VARIABLES_SUBSTITUTED
 		setSubmissionLocation(this.currentFqan);
 		
 		for (ElementPostprocessor postprocessor : getPostprocessors()) {
 			if ( Thread.interrupted() ) {
 				throw new JobSubmissionException("Job cancelled.", null);
 			}
 			if ( ! postprocessor.processBeforeJobCreation() ) {
 			try {
 				postprocessor.process(this.currentFqan);
 			} catch (PostProcessException e) {
 
 				throw new JobSubmissionException(
 						"Could not submit job because postprocessing of the template failed.",
 						e);
 			}
 			}
 		}
 		
 //		try {
 //			myLogger.debug("Final jsdl (after postprocessing:\n----------------------------\n\n"+(SeveralXMLHelpers.toString(getTemplateDocument())));
 //		} catch (Exception e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		}
 		
 		if ( Thread.interrupted() ) {
 			throw new JobSubmissionException("Job cancelled.", null);
 		}
 		
 		setStatus(STATUS_POSTPROCESSORS_EXECUTED);
 
 		try {
 			String tempFqan = this.currentFqan;
 			if (Constants.NON_VO_FQAN.equals(tempFqan) )
 				tempFqan = null;
 			
 			em.getServiceInterface().submitJob(currentJobname);
 		} catch (Exception e) {
 
 			throw new JobSubmissionException(
 					"Could not submit job to endpoint.", e);
 		}
 		
 		if ( Thread.interrupted() ) {
 			throw new JobSubmissionException("Job cancelled.", null);
 		}
 		
 		setStatus(STATUS_JOB_SUBMITTED);
 
 		try {
 			em.getServiceInterface().addJobProperties(currentJobname,
 					DtoJob.createJob(getStatus(), getJobProperties()));
 		} catch (NoSuchJobException e) {
 
 			// throwing an exception. The job will still run.
 			throw new JobSubmissionException(
 					"Could not store job properties. Hopefully the job will still run properly...",
 					e);
 		}
 		setStatus(STATUS_JOB_PROPERTIES_STORED);
 		} catch (RuntimeException runtimeExceptionMostLikely) {
 			runtimeExceptionMostLikely.printStackTrace();
 			throw new JobSubmissionException("Unknown cause for job submission failure.", runtimeExceptionMostLikely);
 		}
 		
 		// now update the internal job list
 		//TODO does that really belong here?
 		em.getJobManager().newJobSubmitted(currentJobname);
 		
 	}
 	
 	/**
 	 * Resets the JsdlTemplate to it's initial state, without user input etc...
 	 *
 	 * @param deletePossiblyCreatedJob deletes a job that may have been created remotely before resetting the status
 	 * @throws TemplatePreProcessorException if the templatePreprocessor couldn't clean up
 	 */
 	public void reset(boolean deletePossiblyCreatedJob) throws TemplatePreProcessorException {
 		
  		if ( deletePossiblyCreatedJob ) {
 			if ( this.status >= STATUS_JOB_CREATED || this.status == STATUS_SUBMISSION_FAILED ) {
 
 				try {
 					myLogger.debug("Deleting directory: "+currentRemoteJobDirectory);
					em.getServiceInterface().deleteFile(currentRemoteJobDirectory);
 					myLogger.debug("Clearing newly created job: "+this.currentJobname);
 					em.getServiceInterface().kill(this.currentJobname, true);

 				} catch (Exception e) {
 					myLogger.warn("Couldn't clear half-submitted job: "+this.currentJobname);
 				}
 			}
 		}
 		
 		resetStatus();
 	}
 
 	private void resetStatus() throws TemplatePreProcessorException {
 
 		DebugUtils.jsdlDebugOutput("before template reset", this.template);
 		
 		// revert the template document to it's original state
 		for ( TemplateNode node : getTemplateNodes().values() ) {
 			node.reset();
 		}
 		
 //		if ( clearInput ) {
 //			for (TemplateModule mod : getModules().values() ) {
 //				mod.reset();
 //			}
 //		}
 		this.jobProperties = new TreeMap<String, String>();
 		
 		currentJobname = null;		
 
 		this.currentFqan = NO_VALID_FQAN;
 		//TODO check whether this line is needed.
 		// If I put it in it may cause problems because 
 		// the postprocessor need to rememeber what they did in
 		// a possible failed first run
 		this.elementPostprocessors = null;
 		unsetSubmissionLocation();
 		//TODO check submissionThread and halt if necessary
 		submissionThread = null;
 		
 		setStatus(STATUS_JSDL_CREATED);
 
 		DebugUtils.jsdlDebugOutput("after template reset", this.template);
 		
 	}
 
 	private void unsetSubmissionLocation() {
 
 		currentRemoteJobDirectory = null;
 		currentSubmissionSite = null;
 		currentExecutionHostFileSystem = null;
 		currentRelativeJobDirectory = null;
 		currentSubmissionLocation = null;
 	}
 
 	/**
 	 * Sets variables for commonly used locations for a job:<br>
 	 * currentExecutionHostFileSystem is the root of the mountpoint that is used<br>
 	 * currentSubmissionSite is the site the job is submitted to<br>
 	 * currentRemoteJobDirectory is the url to the actualy job (working) directory (as gridftp url).
 	 * currentRelativeJobDirectory is the relative path to the working directory from the root of the executionhostFileSystem (aka MountPoint)<br>
 	 * 
 	 * 
 	 * 
 	 * @param fqan
 	 * @throws JobSubmissionException
 	 */
 	private void setSubmissionLocation(String fqan)
 			throws JobSubmissionException {
 
 		if (this.status < STATUS_JOB_CREATED) {
 			myLogger.error("SetSubmissionLocation called when job is not yet created on the server. Resetting everything.");
 			throw new JobSubmissionException("SetSubmissionLocation called when job is not yet created on the server. Resetting everything.", null);
 		}
 
 		myLogger
 				.debug("Setting submissionsite, submissionfqan and jobdirectory for template "
 						+ getApplicationName() + "...");
 
 		currentExecutionHostFileSystem = JsdlHelpers
 				.getUserExecutionHostFs(getTemplateDocument());
 		currentSubmissionSite = getEnvironmentManager().lookupSite(
 				EnvironmentManager.FILE_URL_TYPE,
 				currentExecutionHostFileSystem);
 		
 		currentSubmissionLocation = JsdlHelpers.getCandidateHosts(getTemplateDocument())[0];
 		// because the backend doesn't know about NON_VO_FQAN, it uses null for non-vo
 		String tempFqan = null;
 		if ( Constants.NON_VO_FQAN.equals(fqan) ) 
 			tempFqan = null;
 		else 
 			tempFqan = fqan;
 		
 //		currentRemoteJobDirectory = em.getServiceInterface()
 //				.calculateAbsoluteJobDirectory(
 //						JsdlHelpers.getJobname(getTemplateDocument()),
 //						currentSubmissionLocation, tempFqan);
 		
 		try {
 			currentRemoteJobDirectory = em.getServiceInterface().getJobProperty(
 					JsdlHelpers.getJobname(getTemplateDocument()),
 					Constants.JOBDIRECTORY_KEY);
 		} catch (NoSuchJobException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new JobSubmissionException("Could not get jobdirectory for job.", e);
 		}
 
 		if (currentRemoteJobDirectory == null) 
 			throw new JobSubmissionException("Could not calculate remote job directory. Cancelling Job submission.", null);
 		
 
 		int i = 1;
 		if (currentExecutionHostFileSystem.endsWith("/"))
 			i = 2;
 
 		currentRelativeJobDirectory = currentRemoteJobDirectory.substring(currentExecutionHostFileSystem.length() + i);
 		myLogger.debug("Current relative jobdir: "
 				+ currentRelativeJobDirectory);
 		myLogger.debug("Successfully set.");
 
 	}
 
 //	public void substituteVariables() {
 //		template = Substitution.substituteVariables(template);
 //	}
 
 	// public void preprocess(String fqan) {
 	// NodeList elements_to_preprocess =
 	// Preprocessor.getPreprocessorElements(template);
 	//		
 	// for ( int i=0; i<elements_to_preprocess.getLength(); i++ ) {
 	// try {
 	// Preprocessor.preprocessElement(elements_to_preprocess.item(i), fqan,
 	// em.getServiceInterface());
 	// } catch (PreprocessorException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	//			
 	// }
 	// }
 
 	public TemplateModule getModule(String name) {
 		return getModules().get(name);
 	}
 	
 	/**
 	 * This is a convenience method. If your client just uses
 	 * a default methods of setting values, you don't need specialised
 	 * value setters. This methods adds a {@link DefaultTemplateNodeValueSetter}
 	 * to every TemplateNode in this tmeplate.
 	 */
 	public void setDefaultTemplateNodeValueSetters() {
 		for ( TemplateNode node : getTemplateNodes().values() ) {
 			node.setTemplateNodeValueSetter(new DefaultTemplateNodeValueSetter());
 		}
 	}
 
 //	public String getCommandLineOptions() {
 //
 //		StringBuffer options = new StringBuffer();
 //
 //		for (TemplateNode node : getTemplateNodes().values()) {
 //			options.append("--" + node.getName() + "\t" + node.getDescription()
 //					+ "\n");
 //		}
 //
 //		return options.toString();
 //	}
 
 	public String getTemplateInfo() {
 
 		StringBuffer info = new StringBuffer();
 		info.append("---------------------------------------------------\n");
 		info.append("---------------------------------------------------\n");
 
 		for (String moduleName : getModules().keySet()) {
 			info.append("Module: " + moduleName + "\n");
 			info.append("----------------------------\n");
 			TemplateModule module = getModule(moduleName);
 
 			for (TemplateNode node : module.getTemplateNodes().values()) {
 				info.append("\tName: " + node.getName() + "\n");
 				info.append("\tType: " + node.getType() + "\n");
 				info.append("\t\tDescription: " + node.getDescription() + "\n");
 				info.append("\t\tDefault value: " + node.getValue() + "\n");
 
 				info.append("\t\tMultiplicy: " + node.getMultiplicity() + "\n");
 				String[] prefills = node.getPrefills();
 				if (prefills != null) {
 					for (String prefill : node.getPrefills()) {
 						info.append("\t\tPrefill: " + prefill + "\n");
 					}
 				}
 			}
 			info
 					.append("---------------------------------------------------\n");
 			info
 					.append("---------------------------------------------------\n");
 
 		}
 		return info.toString();
 	}
 
 	public static void main(String args[]) throws ServiceInterfaceException {
 
 		LoginParams params = new LoginParams(
 				"http://localhost:8080/grisu-ws/services/grisu", "markus",
 				"xxx".toCharArray(), "myproxy.arcs.org.au", "443");
 
 		ServiceInterface serviceInterface = ServiceInterfaceFactoryOld
 				.createInterface(params);
 
 		
 		EnvironmentManager em = new EnvironmentManager(serviceInterface);
 
 		Document xmlTemplate = SeveralXMLHelpers.loadXMLFile(new File(
 				"/home/markus/workspace/grisu-core/templates/diff_new.xml"));
 		JsdlTemplate template = new JsdlTemplate(em, xmlTemplate);
 
 		Map<String, TemplateModule> modules = template.getModules();
 
 		System.out
 				.println("---------------------------------------------------");
 		System.out
 				.println("---------------------------------------------------");
 
 		for (String moduleName : modules.keySet()) {
 			System.out.println("Module: " + moduleName);
 			System.out.println("----------------------------");
 			TemplateModule module = modules.get(moduleName);
 
 			for (TemplateNode node : module.getTemplateNodes().values()) {
 				System.out.println("\tName: " + node.getName());
 				System.out.println("\tType: " + node.getType());
 				System.out.println("\t\tDescription: " + node.getDescription());
 				System.out.println("\t\tDefault value: " + node.getValue());
 
 				System.out.println("\t\tMultiplicy: " + node.getMultiplicity());
 				String[] prefills = node.getPrefills();
 				if (prefills != null) {
 					for (String prefill : node.getPrefills()) {
 						System.out.println("\t\tPrefill: " + prefill);
 					}
 				}
 			}
 			System.out
 					.println("---------------------------------------------------");
 			System.out
 					.println("---------------------------------------------------");
 
 		}
 		// for ( TemplateNode node : template.getTemplateNodes().values() ) {
 		// System.out.println("Please enter input for: "+node.getName());
 		// BufferedReader br = new BufferedReader(new
 		// InputStreamReader(System.in));
 		// try {
 		// node.setValue(br.readLine());
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// System.out.println("Could not set value for. Exiting.");
 		// System.exit(-1);
 		// }
 		// }
 		String[] input = new String[] { "1", "/vpac.home.no_vo",
 				"ng2.vpac.org", "new_job",
 				"file:///home/markus/temp/test1.txt",
 				"file:///home/markus/temp/test2.txt", "0" };
 		int i = 0;
 		for (TemplateNode node : template.getTemplateNodes().values()) {
 			System.out.println("Setting input for \"" + node.getName() + "\": "
 					+ input[i]);
 			// try {
 			// node.setValue(input[i]);
 			// } catch (TemplateValidateException e) {
 			// TODO Auto-generated catch block
 			// e.printStackTrace();
 			// }
 			i++;
 		}
 
 //		try {
 ////			template.submit(null);
 //		} catch (JobSubmissionException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		}
 
 	}
 
 	public void templateNodeUpdated(TemplateNodeEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	// public Options getOptions() {
 	//		
 	// Options options = new Options();
 	//		
 	// for ( TemplateNode node : getTemplateNodes().values() ) {
 	// myLogger.debug("Adding option: "+node.getName());
 	// Option option =
 	// OptionBuilder.withArgName(node.getName()).hasArg().withDescription(node.getDescription()).create(node.getName());
 	// options.addOption(option);
 	// }
 	// return options;
 	// }
 
 	// public void setOption(String keyPart, String valuePart) throws
 	// TemplateValidateException {
 	//		
 	// getTemplateNodes().get(keyPart).setValue(valuePart);
 	//		
 	// }
 
 	public Map<String, String> getJobProperties() {
 		return jobProperties;
 	}
 
 	public EnvironmentManager getEnvironmentManager() {
 		return em;
 	}
 
 	/**
 	 * Returns the method which this template will use when creating a job
 	 * remotely. Have a look at the static fields in {@link JobConstants} for
 	 * available options.
 	 * 
 	 * @return the job creation method.
 	 */
 	public int getJobCreationMethod() {
 		return jobCreationMethod;
 	}
 
 	/**
 	 * @param jobCreationMethod
 	 */
 	public void setJobCreationMethod(int jobCreationMethod) {
 		if (jobCreationMethod >= JobConstants.DONT_ACCEPT_NEW_JOB_WITH_EXISTING_JOBNAME
 				&& jobCreationMethod <= JobConstants.OVERWRITE_EXISTING_JOB)
 			this.jobCreationMethod = jobCreationMethod;
 	}
 
 	/**
 	 * Gets the submission site for this template.
 	 * 
 	 * @return the site if status > {@link #STATUS_VARIABLES_SUBSTITUTED}, else
 	 *         null
 	 */
 	public String getCurrentSubmissionSite() {
 		return currentSubmissionSite;
 	}
 
 	/**
 	 * Gets the currently set job directory for this template.
 	 * 
 	 * @return the job directory if status >
 	 *         {@link #STATUS_VARIABLES_SUBSTITUTED}, else null
 	 */
 	public String getCurrentRemoteJobDirectory() {
 		return currentRemoteJobDirectory;
 	}
 
 	/**
 	 * Gets the currently set fqan for this template.
 	 * 
 	 * @return the site if status > {@link #STATUS_VARIABLES_SUBSTITUTED}, else
 	 *         null
 	 */
 	public String getCurrentFqan() {
 		return currentFqan;
 	}
 
 	/**
 	 * Gets the currently set execution file system for this template.
 	 * 
 	 * @return the execution file system if status >
 	 *         {@link #STATUS_VARIABLES_SUBSTITUTED}, else null
 	 */
 	public String getCurrentExecutionHostFileSystem() {
 		return currentExecutionHostFileSystem;
 	}
 	
 	public String getCurrentSubmissionLocation() {
 		return currentSubmissionLocation;
 	}
 
 	/**
 	 * Gets the currently set relative job directory for this template (relative
 	 * to execution file system).
 	 * 
 	 * @return the relative job directory if status >=
 	 *         {@link #STATUS_POSTPROCESSORS_EXECUTED}-1, else null
 	 */
 	public String getCurrentRelativeJobDirectory() {
 		return currentRelativeJobDirectory;
 	}
 
 	/**
 	 * The current status of the submission of this JsdlTemplate. Only really
 	 * important for internal purposes. May be used to display a progress bar or
 	 * something...
 	 * 
 	 * @return the current status of the submission
 	 */
 	public int getStatus() {
 		return status;
 	}
 	
 	private void setSubmissionErrorStatus(JobSubmissionException e) {
 		
 		this.status = STATUS_SUBMISSION_FAILED;
 		fireJsdlEvent("Job submission failed", e);
 	}
 	
 	private void setStatus(int status) {
 		this.status = status;
 		
 		String statusMessage = null;
 		switch (status) {
 		case STATUS_JSDL_CREATED: statusMessage = "Jsdl template created."; break;
 		case STATUS_MODULES_CREATED: statusMessage = "Modules created."; break;
 		case STATUS_TEMPLATENODES_CREATED: statusMessage = "TemplateNodes created."; break;
 		case STATUS_SUBMISSION_STARTED: statusMessage = "Submission started."; break;
 		case STATUS_TEMPLATE_NODES_FILLED: statusMessage = "TemplateNodes filled."; break;
 		case STATUS_TEMPLATENODES_PROCESSED: statusMessage = "TemplateNodes processed."; break;
 		case STATUS_JOB_CREATED: statusMessage = "Job created on server."; break;
 		case STATUS_MODULES_PROCESSED: statusMessage = "Modules processed."; break;
 		// from now on changes to the xml docment don't affect any template nodes or 
 		// template modules anymore, only the raw xml document is changed
 		case STATUS_POSTPROCESSORS_EXECUTED: statusMessage = "PostProcessors executed successfully."; break;
 		case STATUS_JOB_JSDL_DESCRIPTION_STORED: statusMessage = "Job description stored on server."; break;
 		case STATUS_JOB_SUBMITTED: statusMessage = "Job submitted to endpoint."; break;
 		case STATUS_JOB_PROPERTIES_STORED: statusMessage = "Additional job properties stored on server."; break;
 		case STATUS_JOB_SUBMISSION_SUCCESSFUL: statusMessage = "Job submission successful."; break;
 		default: statusMessage = "Unknown status.";
 		}
 		fireJsdlEvent(statusMessage, null);
 	}
 	
 	public void waitForSubmissionToFinish() {
 		
 //		if ( status < STATUS_SUBMISSION_STARTED ) { 
 //			// nothing to cancel
 //			return;
 //		}
 		
 		if ( submissionThread != null && submissionThread.isAlive() ) {
 			try {
 				submissionThread.join();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Cancels a job submission if there is one ongoing at the moment.
 	 */
 	public void cancelSubmission() {
 		if ( status < STATUS_SUBMISSION_STARTED ) { 
 			// nothing to cancel
 			return;
 		}
 		
 		if ( submissionThread != null && submissionThread.isAlive() ) {
 			submissionThread.interrupt();
 			
 		}
 //		int i = 0;
 //		while (submissionThread.isAlive()) {
 //			i = i + 1;
 //			if ( i > 10 ) {
 //				submissionThread.stop();
 //			}
 //			myLogger.debug("Thread alive! i="+i);
 //			try {
 //				Thread.sleep(500);
 //			} catch (InterruptedException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //
 //		myLogger.debug("Thread dead.");
 	}
 	
 	
 	// listerner stuff 
 	// --------------------------------------------------------------
 	
 	private Vector<JsdlTemplateListener> templateEventListeners;
 
 	/**
 	 * Fires either a status event when the submission status changed or a message
 	 * that the submission failed with the according {@link JobSubmissionException}
 	 * @param exception the exception to pass or null if you only want to announce a new
 	 * status
 	 */
 	public void fireJsdlEvent(String message, JobSubmissionException exception) {
 		// if we have no templateEventListeners, do nothing...
 		if (templateEventListeners != null && !templateEventListeners.isEmpty()) {
 			// create the event object to send
 			JsdlTemplateEvent event = new JsdlTemplateEvent(this, message);
 
 			// make a copy of the listener list in case
 			// anyone adds/removes templateEventListeners
 			Vector<JsdlTemplateListener> targets;
 			synchronized (this) {
 				targets = (Vector<JsdlTemplateListener>) templateEventListeners.clone();
 			}
 
 			// walk through the listener list and
 			// call the gridproxychanged method in each
 			Enumeration<JsdlTemplateListener> e = targets.elements();
 			while (e.hasMoreElements()) {
 				JsdlTemplateListener l = (JsdlTemplateListener) e.nextElement();
 				try {
 					if ( exception == null ) 
 						l.templateStatusChanged(event);
 					else 
 						l.submissionExceptionOccured(event, exception);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
 
 	// register a listener
 	synchronized public void addJsdlTemplateListener(JsdlTemplateListener l) {
 		if (templateEventListeners == null)
 			templateEventListeners = new Vector<JsdlTemplateListener>();
 		templateEventListeners.addElement(l);
 	}
 
 	// remove a listener
 	synchronized public void removeJsdlTemplateListener(JsdlTemplateListener l) {
 		if (templateEventListeners == null) {
 			templateEventListeners = new Vector<JsdlTemplateListener>();
 		}
 		templateEventListeners.removeElement(l);
 	}
 
 	
 
 }
