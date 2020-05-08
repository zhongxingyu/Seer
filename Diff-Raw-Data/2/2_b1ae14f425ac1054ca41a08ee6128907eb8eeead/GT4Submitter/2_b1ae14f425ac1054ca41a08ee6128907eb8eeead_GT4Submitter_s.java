 package org.vpac.grisu.js.control.job.gt4;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Date;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.axis.message.addressing.EndpointReferenceType;
 import org.apache.log4j.Logger;
 import org.globus.exec.client.GramJob;
 import org.globus.exec.generated.JobDescriptionType;
 import org.globus.exec.utils.client.ManagedJobFactoryClientHelper;
 import org.globus.exec.utils.rsl.RSLHelper;
 import org.globus.exec.utils.rsl.RSLParseException;
 import org.globus.wsrf.impl.security.authentication.Constants;
 import org.globus.wsrf.impl.security.authorization.Authorization;
 import org.globus.wsrf.impl.security.authorization.HostAuthorization;
 import org.ietf.jgss.GSSCredential;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.SeveralXMLHelpers;
 import org.vpac.grisu.control.exceptions.NoValidCredentialException;
 import org.vpac.grisu.control.exceptions.ServerJobSubmissionException;
 import org.vpac.grisu.control.info.CachedMdsInformationManager;
 import org.vpac.grisu.control.info.InformationManager;
 import org.vpac.grisu.control.utils.DebugUtils;
 import org.vpac.grisu.control.utils.ServerPropertiesManager;
 import org.vpac.grisu.credential.model.ProxyCredential;
 import org.vpac.grisu.js.control.job.JobSubmitter;
 import org.vpac.grisu.js.model.Job;
 import org.vpac.grisu.js.model.utils.JsdlHelpers;
 import org.vpac.security.light.CredentialHelpers;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * This class is the connector class between grisu and our GT4 gateways. It
  * translates the jsdl document into the rsl format and also knows how to submit
  * a job to a GT4 endpoint using WS-GRAM.
  * 
  * @author Markus Binsteiner
  * 
  */
 public class GT4Submitter extends JobSubmitter {
 
 	static final Logger myLogger = Logger.getLogger(GT4Submitter.class
 			.getName());
 
 	protected InformationManager informationManager = CachedMdsInformationManager
 			.getDefaultCachedMdsInformationManager();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.js.control.job.JobSubmitter#createJobSubmissionDescription
 	 * (org.w3c.dom.Document)
 	 */
 	private String createJobSubmissionDescription(
 			ServiceInterface serviceInterface, Document jsdl) throws ServerJobSubmissionException {
 
 		DebugUtils.jsdlDebugOutput("Before translating into rsl: ", jsdl);
 
 		Document output = null;
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			output = docBuilder.newDocument();
 		} catch (ParserConfigurationException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		// Add root element
 		Element job = output.createElement("job");
 		output.appendChild(job);
 
 		// Add "executable" node
 		Element executable = output.createElement("executable");
 		executable.setTextContent(JsdlHelpers.getPosixApplication(jsdl));
 		job.appendChild(executable);
 
 		// Add "argument"s
 		String[] arguments = JsdlHelpers.getPosixApplicationArguments(jsdl);
 		for (String argument : arguments) {
 			if (argument != null && !"".equals(argument.trim())) {
 				Element argument_node = output.createElement("argument");
 				argument_node.setTextContent(argument);
 				job.appendChild(argument_node);
 			}
 		}
 
 		// Add "directory"
 		Element directory = output.createElement("directory");
 		directory.setTextContent(JsdlHelpers.getWorkingDirectory(jsdl));
 		job.appendChild(directory);
 
 		// "stdin" element if available
 		String stdinValue = JsdlHelpers.getPosixStandardInput(jsdl);
 		if (stdinValue != null && !"".equals(stdinValue)) {
 			Element stdin = output.createElement("stdin");
 			stdin.setTextContent(stdinValue);
 			job.appendChild(stdin);
 		}
 
 		// Add "stdout"
 		Element stdout = output.createElement("stdout");
 		stdout.setTextContent(JsdlHelpers.getPosixStandardOutput(jsdl));
 		job.appendChild(stdout);
 
 		// Add "stderr"
 		Element stderr = output.createElement("stderr");
 		stderr.setTextContent(JsdlHelpers.getPosixStandardError(jsdl));
 		job.appendChild(stderr);
 
 		// Add "queue" node
 		// TODO change that once I know how to specify queues in jsdl
 		String queue = JsdlHelpers.getCandidateHosts(jsdl)[0]; // TODO this
 		// always uses
 		// the first
 		// candidate
 		// host - not
 		// good
 		if (queue.indexOf(":") != -1) {
 			queue = queue.substring(0, queue.indexOf(":"));
 			Element queue_node = output.createElement("queue");
 			queue_node.setTextContent(queue);
 			job.appendChild(queue_node);
 		}
 
 		// Add "jobtype" if mpi
 		int processorCount = JsdlHelpers.getProcessorCount(jsdl);
 
 		Element jobType = output.createElement("jobType");
 		String jobTypeString = JsdlHelpers.getJobType(jsdl);
 
 		if (processorCount > 1) {
 			Element count = output.createElement("count");
 			count.setTextContent(new Integer(processorCount).toString());
 			job.appendChild(count);
 			if (jobTypeString == null) {
 				jobType.setTextContent("mpi");
 			} else {
 				jobType.setTextContent(jobTypeString);
 			}
 		} else {
 			if (jobTypeString == null) {
 				jobType.setTextContent("single");
 			} else {
 				jobType.setTextContent(jobTypeString);
 			}
 
 		}
 		job.appendChild(jobType);
 
 		// total memory
 		Long memory = JsdlHelpers.getTotalMemoryRequirement(jsdl);
 
 		if (memory != null && memory >= 0) {
 			Element totalMemory = output.createElement("maxMemory");
 			// convert from bytes to mb
 			memory = memory / 1024;
 			totalMemory.setTextContent(memory.toString());
 			job.appendChild(totalMemory);
 		}
 
 		// Add "maxWallTime" node
 		int walltime = JsdlHelpers.getWalltime(jsdl);
 		if (walltime > 0) {
 			Element maxWallTime = output.createElement("maxWallTime");
 			int wt = new Integer(JsdlHelpers.getWalltime(jsdl));
 			// convert to minutes
 			wt = wt / 60;
 			maxWallTime.setTextContent(new Integer(wt).toString());
 			job.appendChild(maxWallTime);
 		}
 
 		Element fileStageIn = output.createElement("fileStageIn");
 		// stage ins
 		// Map<String, String> stageIns = JsdlHelpers.getStageIns(jsdl);
 		// // only append stageIns element if not 0 because globus will reject
 		// the job
 		// if there is an empyt <stageIns> tag
 		// if ( stageIns.size() > 0 ) {
 		// for ( String source : stageIns.keySet() ) {
 		// Element stageIn = output.createElement("transfer");
 		// Element sourceURL = output.createElement("sourceUrl");
 		// sourceURL.setTextContent(source);
 		// stageIn.appendChild(sourceURL);
 		// Element targetURL = output.createElement("destinationUrl");
 		// targetURL.setTextContent(stageIns.get(source));
 		// stageIn.appendChild(targetURL);
 		//			
 		// fileStageIn.appendChild(stageIn);
 		// }
 		// job.appendChild(fileStageIn);
 		// }
 		// Extensions
 		Element extensions = output.createElement("extensions");
 
 		// jobname
 		Element jobname = output.createElement("jobname");
 		String jobname_string = JsdlHelpers.getJobname(jsdl);
 		// because of some pbs restrictions we have to keep the jobname to 6
 		// chars
 		if (jobname_string.length() > 6) {
 			jobname.setTextContent(jobname_string.substring(jobname_string
 					.length() - 6));
 		}
 		// jobname.setTextContent(jobname_string);
 		extensions.appendChild(jobname);
 
 		// module -- old style
 		String[] modules_string = null;
 		try {
 			modules_string = JsdlHelpers.getModules(jsdl);
 		} catch (Exception e) {
 			// doesn't matter
 		}
		if (modules_string != null && modules_string.length == 0) {
 			for (String module_string : modules_string) {
 				if (!"".equals(module_string)) {
 					Element module = output.createElement("module");
 					module.setTextContent(module_string);
 					extensions.appendChild(module);
 				}
 			}
 		} else {
 
 			// try to determine module to load from mds -- this will be the
 			// default way of doing it later on and the module element will
 			// disappear
 			// it was stupid in the first place to have it...
 
 			String application = JsdlHelpers.getApplicationName(jsdl);
 			String version = JsdlHelpers.getApplicationVersion(jsdl);
 			String subLoc = JsdlHelpers.getCandidateHosts(jsdl)[0];
 
 			if (application != null || version != null || subLoc != null) {
 				Map<String, String> appDetails = serviceInterface
 						.getApplicationDetails(application, version, subLoc);
 
 				String modulesString = appDetails
 						.get(JobConstants.MDS_MODULES_KEY);
 
 				if ( modules_string == null || "".equals(modules_string) ) {
 					myLogger.warn("No module for this application/version/submissionLocation found. Submitting nonetheless...");
 				}
 
 				if (modulesString != null && modulesString.length() > 0) {
 					modules_string = appDetails.get(
 							JobConstants.MDS_MODULES_KEY).split(",");
 
 					
 					for (String module_string : modules_string) {
 						if (!"".equals(module_string)) {
 							Element module = output.createElement("module");
 							module.setTextContent(module_string);
 							extensions.appendChild(module);
 						}
 					}
 				}
 			} else {
 				throw new ServerJobSubmissionException("Can't determine either application, version or submissionLocation.");
 			}
 		}
 
 		// email
 		String email = JsdlHelpers.getEmail(jsdl);
 
 		if (email != null && !"".equals(email)) {
 			Element email_address = output.createElement("email_address");
 			email_address.setTextContent(email);
 			extensions.appendChild(email_address);
 
 			if (JsdlHelpers.sendEmailOnJobStart(jsdl)) {
 				Element emailonexecution = output
 						.createElement("emailonexecution");
 				emailonexecution.setTextContent("yes");
 				extensions.appendChild(emailonexecution);
 			}
 
 			if (JsdlHelpers.sendEmailOnJobFinish(jsdl)) {
 				Element emailonabort = output.createElement("emailonabort");
 				emailonabort.setTextContent("yes");
 				Element emailontermination = output
 						.createElement("emailontermination");
 				emailontermination.setTextContent("yes");
 				extensions.appendChild(emailonabort);
 				extensions.appendChild(emailontermination);
 			}
 
 		}
 
 		job.appendChild(extensions);
 
 		// initialize StreamResult with InputFile object to save to file
 		StreamResult result = null;
 		try {
 			Transformer transformer = TransformerFactory.newInstance()
 					.newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
 			result = new StreamResult(new StringWriter());
 			DOMSource source = new DOMSource(output);
 
 			transformer.transform(source, result);
 		} catch (TransformerConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return result.getWriter().toString();
 	}
 
 	// // this method is just for testing. Do not use!!!
 	// protected String submit(String host, String factoryType, Document jsdl,
 	// GSSCredential credential) {
 	//		
 	// JobDescriptionType jobDesc = null;
 	// String submittedJobDesc = null;
 	// try {
 	// submittedJobDesc = createJobSubmissionDescription(jsdl);
 	// jobDesc = RSLHelper.readRSL(submittedJobDesc);
 	//
 	// } catch (RSLParseException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// return null;
 	// }
 	//		
 	// /*
 	// * Job test parameters (adjust to your needs)
 	// */
 	// // remote host
 	// //String contact = "ng2.vpac.org";
 	//		
 	// // Factory type: Fork, Condor, PBS, LSF
 	// //String factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.FORK;
 	// // String factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.PBS;
 	//		
 	// // Deafult Security: Host authorization + XML encryption
 	// Authorization authz = HostAuthorization.getInstance();
 	// Integer xmlSecurity = Constants.ENCRYPTION;
 	//		
 	// // Submission mode: batch = will not wait
 	// boolean batchMode = true;
 	//		
 	// // a Simple command executable (if no job file)
 	// String simpleJobCommandLine = null;
 	//		
 	// // Job timeout values: duration, termination times
 	// Date serviceDuration = null;
 	// Date serviceTermination = null;
 	// int timeout = GramJob.DEFAULT_TIMEOUT;
 	//		
 	// String handle = null;
 	// try {
 	//			
 	// if ( credential == null || credential.getRemainingLifetime() < 1 ) {
 	// throw new NoValidCredentialException("Credential is not valid.");
 	// }
 	//			
 	// GramClient gram = new GramClient(credential);
 	//
 	// handle = gram.submitRSL(getFactoryEPR(host,factoryType)
 	// , simpleJobCommandLine, jobDesc
 	// , authz, xmlSecurity
 	// , batchMode, false, false
 	// , serviceDuration, serviceTermination, timeout );
 	//			
 	// } catch (Exception e) {
 	// //TODO handle that
 	// e.printStackTrace();
 	// }
 	//		
 	// //job.setSubmittedJobDescription(submittedJobDesc);
 	//		
 	// myLogger.debug("Submitted rsl job
 	// description:\n--------------------------------");
 	// myLogger.debug(submittedJobDesc);
 	//
 	// return handle;
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.js.control.job.JobSubmitter#submit(java.lang.String,
 	 * org.vpac.grisu.js.model.Job)
 	 */
 	protected String submit(ServiceInterface serviceInterface, String host,
 			String factoryType, Job job) throws ServerJobSubmissionException {
 
 		JobDescriptionType jobDesc = null;
 		String submittedJobDesc = null;
 		try {
 			// String site = informationManager.getSiteForHostOrUrl(host);
 			submittedJobDesc = createJobSubmissionDescription(serviceInterface,
 					job.getJobDescription());
 			jobDesc = RSLHelper.readRSL(submittedJobDesc);
 
 		} catch (RSLParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 
 		/*
 		 * Job test parameters (adjust to your needs)
 		 */
 		// remote host
 		// String contact = "ng2.vpac.org";
 		// Factory type: Fork, Condor, PBS, LSF
 		// String factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.FORK;
 		// String factoryType = ManagedJobFactoryConstants.FACTORY_TYPE.PBS;
 		// Deafult Security: Host authorization + XML encryption
 		Authorization authz = HostAuthorization.getInstance();
 		Integer xmlSecurity = Constants.ENCRYPTION;
 
 		// Submission mode: batch = will not wait
 		boolean batchMode = true;
 
 		// a Simple command executable (if no job file)
 		String simpleJobCommandLine = null;
 
 		// Job timeout values: duration, termination times
 		Date serviceDuration = null;
 		Date serviceTermination = null;
 		int timeout = GramJob.DEFAULT_TIMEOUT;
 
 		String handle = null;
 		try {
 
 			GSSCredential credential = null;
 			credential = CredentialHelpers.convertByteArrayToGSSCredential(job
 					.getCredential().getCredentialData());
 
 			if (credential == null || credential.getRemainingLifetime() < 1) {
 				throw new NoValidCredentialException(
 						"Credential associated with job: " + job.getDn()
 								+ " / " + job.getJobname() + " is not valid.");
 			}
 
 			GramClient gram = new GramClient(credential);
 
 			handle = gram.submitRSL(getFactoryEPR(host, factoryType),
 					simpleJobCommandLine, jobDesc, authz, xmlSecurity,
 					batchMode, false, false, serviceDuration,
 					serviceTermination, timeout);
 
 		} catch (Exception e) {
 			// TODO handle that
 			e.printStackTrace();
 			if (handle == null) {
 				// TODO
 			}
 		}
 
 		job.setSubmittedJobDescription(submittedJobDesc);
 		// for debug purposes
 
 		if (ServerPropertiesManager.getDebugModeOn()) {
 
 			String uid = handle.substring(handle.indexOf("?") + 1);
 			String hostname = host.substring(0, host
 					.indexOf(":8443/wsrf/services/ManagedJobFactoryService"));
 			String eprString = "<ns00:EndpointReferenceType xmlns:ns00=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">\n"
 					+ "<ns00:Address>"
 					+ hostname
 					+ ":8443/wsrf/services/ManagedExecutableJobService</ns00:Address>\n"
 					+ "<ns00:ReferenceProperties><ResourceID xmlns=\"http://www.globus.org/namespaces/2004/10/gram/job\">"
 					+ uid
 					+ "</ResourceID></ns00:ReferenceProperties>\n"
 					+ "<wsa:ReferenceParameters xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"/>\n"
 					+ "</ns00:EndpointReferenceType>";
 			try {
 				myLogger.debug("Writing out epr file.");
 				String vo = job.getFqan();
 				if (vo == null || "".equals(vo))
 					vo = "non_vo";
 				else
 					vo = vo.replace("/", "_");
 
 				String uFileName = ServerPropertiesManager.getDebugDirectory()
 						+ "/"
 						+ job.getDn().replace("=", "_").replace(",", "_")
 								.replace(" ", "_") + "_" + job.getJobname()
 						+ "_" + vo + "_" + job.hashCode();
 				FileWriter fileWriter = new FileWriter(uFileName + ".epr");
 				BufferedWriter buffWriter = new BufferedWriter(fileWriter);
 				buffWriter.write(eprString);
 
 				buffWriter.close();
 
 				FileWriter fileWriter2 = new FileWriter(uFileName + ".rsl");
 				buffWriter = new BufferedWriter(fileWriter2);
 				buffWriter.write(submittedJobDesc);
 				buffWriter.close();
 
 				FileWriter fileWriter3 = new FileWriter(uFileName + ".jsdl");
 				buffWriter = new BufferedWriter(fileWriter3);
 				buffWriter.write(SeveralXMLHelpers
 						.toStringWithoutAnnoyingExceptions(job
 								.getJobDescription()));
 				buffWriter.close();
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 		}
 
 		myLogger
 				.debug("Submitted rsl job description:\n--------------------------------");
 		myLogger.debug(submittedJobDesc);
 
 		return handle;
 
 	}
 
 	static private EndpointReferenceType getFactoryEPR(String contact,
 			String factoryType) throws Exception {
 		URL factoryUrl = ManagedJobFactoryClientHelper.getServiceURL(contact)
 				.getURL();
 
 		myLogger.debug("Factory Url: " + factoryUrl);
 		return ManagedJobFactoryClientHelper.getFactoryEndpoint(factoryUrl,
 				factoryType);
 	}
 
 	@Override
 	public String getServerEndpoint(String server) {
 		return "https://" + server
 				+ ":8443/wsrf/services/ManagedJobFactoryService";
 
 	}
 
 	private int translateToGrisuStatus(String status) {
 
 		int grisu_status = Integer.MIN_VALUE;
 		if ("Done".equals(status)) {
 			grisu_status = JobConstants.DONE;
 		} else if (status.startsWith("Done")) {
 			int error = Integer.parseInt(status.substring(4));
 			grisu_status = JobConstants.DONE + error;
 		} else if ("StageIn".equals(status)) {
 			grisu_status = JobConstants.STAGE_IN;
 		} else if ("Pending".equals(status)) {
 			grisu_status = JobConstants.PENDING;
 		} else if ("Unsubmitted".equals(status)) {
 			grisu_status = JobConstants.UNSUBMITTED;
 		} else if ("Active".equals(status)) {
 			grisu_status = JobConstants.ACTIVE;
 		} else if ("CleanUp".equals(status)) {
 			grisu_status = JobConstants.CLEAN_UP;
 		} else if ("NoSuchJob".equals(status)) {
 			grisu_status = JobConstants.NO_SUCH_JOB;
 		} else if (status != null && status.startsWith("Failed")) {
 			grisu_status = JobConstants.FAILED;
 		} else {
 			grisu_status = Integer.MAX_VALUE;
 		}
 		return grisu_status;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.vpac.grisu.js.control.job.JobSubmitter#getJobStatus(java.lang.String,
 	 * org.vpac.grisu.credential.model.ProxyCredential)
 	 */
 	public int getJobStatus(String endPointReference, ProxyCredential cred) {
 
 		String status = null;
 		int grisu_status = Integer.MIN_VALUE;
 		status = GramClient.getJobStatus(endPointReference, cred
 				.getGssCredential());
 
 		grisu_status = translateToGrisuStatus(status);
 
 		return grisu_status;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.vpac.grisu.js.control.job.JobSubmitter#killJob(java.lang.String,
 	 * org.vpac.grisu.credential.model.ProxyCredential)
 	 */
 	public int killJob(String endPointReference, ProxyCredential cred) {
 
 		String status = null;
 		int grisu_status = Integer.MIN_VALUE;
 		status = GramClient.destroyJob(endPointReference, cred
 				.getGssCredential());
 
 		grisu_status = translateToGrisuStatus(status);
 
 		if (grisu_status == JobConstants.NO_SUCH_JOB)
 			return JobConstants.KILLED;
 
 		return grisu_status;
 
 	}
 
 	// public static void main (String[] args) {
 	//		
 	// GT4Submitter submitter = new GT4Submitter();
 	// Document jsdl = SeveralXMLHelpers.loadXMLFile(new
 	// File("/home/markus/Desktop/sleep.jsdl"));
 	//		
 	// String rsl = submitter.createJobSubmissionDescription(jsdl);
 	//		
 	// GSSCredential credential = null;
 	// try {
 	// GlobusCredential proxy = CredentialHelpers.loadGlobusCredential(new
 	// File("/tmp/x509up_u1000"));
 	// credential = CredentialHelpers.wrapGlobusCredential(proxy);
 	// } catch (Exception e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	//		
 	// // String handle = submitter.submit("ng2.sapac.edu.au",
 	// ManagedJobFactoryConstants.FACTORY_TYPE.PBS, jsdl, credential);
 	// // String handle = submitter.submit("ng2dev.vpac.monash.edu.au",
 	// ManagedJobFactoryConstants.FACTORY_TYPE.PBS, jsdl, credential);
 	// // String handle = submitter.submit("ng2dev.vpac.org",
 	// ManagedJobFactoryConstants.FACTORY_TYPE.PBS, jsdl, credential);
 	// // String handle = submitter.submit("ng2.hpcu.uq.edu.au",
 	// ManagedJobFactoryConstants.FACTORY_TYPE.PBS, jsdl, credential);
 	// System.out.println("Handle of job: "+handle);
 	//		
 	// String uid = handle.substring(handle.indexOf("?")+1);
 	// System.out.println("Uid: "+uid);
 	// // String eprString = "<ns00:EndpointReferenceType
 	// xmlns:ns00=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\">\n<ns00:Address>https://ng2dev.vpac.org:8443/wsrf/services/ManagedExecutableJobService</ns00:Address>\n<ns00:ReferenceProperties><ResourceID
 	// xmlns=\"http://www.globus.org/namespaces/2004/10/gram/job\">"+uid+"</ResourceID></ns00:ReferenceProperties>\n<wsa:ReferenceParameters
 	// xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"/>\n</ns00:EndpointReferenceType>";
 	// // String eprString = "<ns00:EndpointReferenceType
 	// xmlns:ns00=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"><ns00:Address>https://ng2.hpcu.uq.edu.au:8443/wsrf/services/ManagedExecutableJobService</ns00:Address><ns00:ReferenceProperties><ResourceID
 	// xmlns=\"http://www.globus.org/namespaces/2004/10/gram/job\">"+uid+"</ResourceID></ns00:ReferenceProperties><wsa:ReferenceParameters
 	// xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"/></ns00:EndpointReferenceType>";
 	// // String eprString = "<ns00:EndpointReferenceType
 	// xmlns:ns00=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"><ns00:Address>https://ng2.sapac.edu.au:8443/wsrf/services/ManagedExecutableJobService</ns00:Address><ns00:ReferenceProperties><ResourceID
 	// xmlns=\"http://www.globus.org/namespaces/2004/10/gram/job\">"+uid+"</ResourceID></ns00:ReferenceProperties><wsa:ReferenceParameters
 	// xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"/></ns00:EndpointReferenceType>";
 	// String eprString = "<ns00:EndpointReferenceType
 	// xmlns:ns00=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"><ns00:Address>https://ng2dev.vpac.monash.edu.au:8443/wsrf/services/ManagedExecutableJobService</ns00:Address><ns00:ReferenceProperties><ResourceID
 	// xmlns=\"http://www.globus.org/namespaces/2004/10/gram/job\">"+uid+"</ResourceID></ns00:ReferenceProperties><wsa:ReferenceParameters
 	// xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/03/addressing\"/></ns00:EndpointReferenceType>";
 	//		
 	// try {
 	// FileWriter fileWriter = new
 	// FileWriter("/home/markus/Desktop/test44.epr");
 	// BufferedWriter buffWriter = new BufferedWriter(fileWriter);
 	// buffWriter.write(eprString);
 	//
 	// buffWriter.close();
 	// } catch (Exception e) {
 	// e.printStackTrace();
 	// }
 	//		
 	// }
 
 }
