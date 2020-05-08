 package grisu.utils;
 
 import grisu.jcommons.constants.JobSubmissionProperty;
 import grisu.model.FileManager;
 
 import java.io.InputStream;
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 
 
 /**
  * This class creats a job description document out of the job properties using
  * String replacements on an xml file.
  * 
  * I know I should probably write a class that uses builds an xml file from
  * scratch, but I just don't have the time for that.
  * 
  * @author markus
  * 
  */
 public class SimpleJsdlBuilder {
 
 	/**
 	 * Builds a jsdl file using the provided job properties.
 	 * 
 	 * For a list of key names, have a look at the JobSubmissionProperty enum
 	 * class.
 	 * 
 	 * You need at least the {@link JobSubmissionProperty#COMMANDLINE} property
 	 * to be set.
 	 * 
 	 * @param jobProperties
 	 *            the job properties
 	 * @return the jsdl document
 	 */
	
 	static final Logger myLogger = Logger.getLogger(SimpleJsdlBuilder.class
 			.getName());
 
	
 	public static Document buildJsdl(
 			final Map<JobSubmissionProperty, String> jobProperties) {
 
 		final InputStream in = SimpleJsdlBuilder.class
 				.getResourceAsStream("/generic.xml");
 
 		String jsdlTemplateString = SeveralStringHelpers.fromInputStream(in);
 
 		for (final JobSubmissionProperty jp : JobSubmissionProperty.values()) {
 
 			if (jp.equals(JobSubmissionProperty.FORCE_MPI)) {
 
 				final Boolean force_MPI = Boolean.parseBoolean(jobProperties
 						.get(jp));
 				if (force_MPI) {
 					jsdlTemplateString = jsdlTemplateString.replaceAll(
 							"XXX_jobType_XXX", "mpi");
 				}
 			} else if (jp.equals(JobSubmissionProperty.FORCE_SINGLE)) {
 
 				if (jp.equals(JobSubmissionProperty.FORCE_SINGLE)) {
 
 					final Boolean force_single = Boolean
 							.parseBoolean(jobProperties.get(jp));
 					if (force_single) {
 						jsdlTemplateString = jsdlTemplateString.replaceAll(
 								"XXX_jobType_XXX", "single");
 					}
 				}
 
 			} else if (jp.equals(JobSubmissionProperty.SUBMISSIONLOCATION)) {
 
 				if ((jobProperties.get(jp) == null)
 						|| (jobProperties.get(jp).length() == 0)) {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", "");
 				} else {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", "<HostName>"
 									+ jobProperties.get(jp) + "</HostName>");
 				}
 
 			} else if (jp.equals(JobSubmissionProperty.WALLTIME_IN_MINUTES)) {
 
 				final String walltime = jobProperties.get(jp);
 				final int wallTimeInSeconds = Integer.parseInt(walltime) * 60;
 				final int cpus = Integer.parseInt(jobProperties
 						.get(JobSubmissionProperty.NO_CPUS));
 
 				final int totalCpuTime = wallTimeInSeconds * cpus;
 				jsdlTemplateString = jsdlTemplateString.replaceAll(
 						"XXX_" + jp.toString() + "_XXX", new Integer(
 								totalCpuTime).toString());
 
 			} else if (jp.equals(JobSubmissionProperty.COMMANDLINE)) {
 
 				final String executable = CommandlineHelpers
 						.extractExecutable(jobProperties.get(jp));
 				final StringBuffer exeAndArgsElements = new StringBuffer();
 				exeAndArgsElements.append("<Executable>");
 				exeAndArgsElements.append(StringEscapeUtils.escapeXml(executable));
 				exeAndArgsElements.append("</Executable>");
 
 				for (final String arg : CommandlineHelpers
 						.extractArgumentsFromCommandline(jobProperties.get(jp))) {
 					exeAndArgsElements.append("<Argument>");
 					exeAndArgsElements.append(StringEscapeUtils.escapeXml(arg));
 					exeAndArgsElements.append("</Argument>");
 				}

 				jsdlTemplateString = jsdlTemplateString.replaceAll(
						"XXX_" + jp.toString() + "_XXX",
						exeAndArgsElements.toString());
 
 			} else if (jp.equals(JobSubmissionProperty.MODULES)) {
 
 				final String modulesString = jobProperties.get(jp);
 				if (StringUtils.isBlank(modulesString)) {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", "");
 					continue;
 				}
 				final StringBuffer modulesElements = new StringBuffer();
 				for (final String module : modulesString.split(",")) {
 					modulesElements
 					.append("<Module xmlns=\"http://arcs.org.au/jsdl/jsdl-grisu\">"
 							+ module + "</Module>");
 				}
 				jsdlTemplateString = jsdlTemplateString.replaceAll(
 						"XXX_" + jp.toString() + "_XXX",
 						modulesElements.toString());
 
 			} else if (jp.equals(JobSubmissionProperty.INPUT_FILE_URLS)) {
 
 				final Map<String, String> inputFileUrls = StringHelpers
 						.StringToMap(jobProperties.get(jp));
 				if ((inputFileUrls == null) || (inputFileUrls.size() == 0)) {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", "");
 					continue;
 				}
 
 				final StringBuffer dataStagingElements = new StringBuffer();
 				for (final String inputFileUrl : inputFileUrls.keySet()) {
 					if (!FileManager.isLocal(inputFileUrl)) {
 						dataStagingElements.append("<DataStaging>\n<FileName>"
 								+ inputFileUrls.get(inputFileUrl)
 								+ "</FileName>\n");
 						dataStagingElements
 						.append("\n<FileSystemName></FileSystemName>\n"
 								+ "<Source>\n<URI>");
 						dataStagingElements.append(inputFileUrl);
 						dataStagingElements
 						.append("</URI>\n</Source>\n</DataStaging>\n");
 					}
 				}
 
 				jsdlTemplateString = jsdlTemplateString.replaceAll(
 						"XXX_" + jp.toString() + "_XXX",
 						dataStagingElements.toString());
 
 			} else {
 				if (jobProperties.get(jp) == null) {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", jp.defaultValue());
 				} else {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", jobProperties.get(jp));
 				}
 			}
 		}
 
 		jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_jobType_XXX",
 				"");
 
 		Document result;
 		try {
 			result = SeveralXMLHelpers.fromString(jsdlTemplateString);
 		} catch (final Exception e) {
 			// TODO Auto-generated catch block
 			myLogger.error(e);
 			throw new RuntimeException("Couldn't create jsdl document");
 		}
 		return result;
 	}
 
 	private SimpleJsdlBuilder() {
 	}
 
 }
