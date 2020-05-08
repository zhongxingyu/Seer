 package org.vpac.grisu.utils;
 
 import java.io.InputStream;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.model.FileManager;
 import org.w3c.dom.Document;
 
 import au.org.arcs.jcommons.constants.JobSubmissionProperty;
 
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
 public final class SimpleJsdlBuilder {
 	
 	private SimpleJsdlBuilder() {
 	}
 
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
 	public static Document buildJsdl(
 			final Map<JobSubmissionProperty, String> jobProperties) {
 
 		InputStream in = SimpleJsdlBuilder.class
 				.getResourceAsStream("/generic.xml");
 
 		String jsdlTemplateString = SeveralStringHelpers.fromInputStream(in);
 
 		for (JobSubmissionProperty jp : JobSubmissionProperty.values()) {
 
 			if (jp.equals(JobSubmissionProperty.SUBMISSIONLOCATION)) {
 
 				if (jobProperties.get(jp) == null
 						|| jobProperties.get(jp).length() == 0) {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", "");
 				} else {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", "<HostName>"
 							+ jobProperties.get(jp) + "</HostName>");
 				}
 
 			} else if (jp.equals(JobSubmissionProperty.WALLTIME_IN_MINUTES)) {
 
 				String walltime = jobProperties.get(jp);
				int wallTimeInSeconds = Integer.parseInt(walltime)*60;
 				int cpus = Integer.parseInt(jobProperties
 						.get(JobSubmissionProperty.NO_CPUS));
 
 				int totalCpuTime = wallTimeInSeconds * cpus;
 				jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 						+ jp.toString() + "_XXX", new Integer(totalCpuTime)
 						.toString());
 
 			} else if (jp.equals(JobSubmissionProperty.COMMANDLINE)) {
 
 				String executable = CommandlineHelpers
 						.extractExecutable(jobProperties.get(jp));
 				StringBuffer exeAndArgsElements = new StringBuffer();
 				exeAndArgsElements.append("<Executable>");
 				exeAndArgsElements.append(executable);
 				exeAndArgsElements.append("</Executable>");
 
 				for (String arg : CommandlineHelpers
 						.extractArgumentsFromCommandline(jobProperties.get(jp))) {
 					exeAndArgsElements.append("<Argument>");
 					exeAndArgsElements.append(arg);
 					exeAndArgsElements.append("</Argument>");
 				}
 
 				jsdlTemplateString = jsdlTemplateString
 						.replaceAll("XXX_" + jp.toString() + "_XXX",
 								exeAndArgsElements.toString());
 
 			} else if (jp.equals(JobSubmissionProperty.MODULES)) {
 
 				String modulesString = jobProperties.get(jp);
 				if (StringUtils.isBlank(modulesString)) {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", "");
 					continue;
 				}
 				StringBuffer modulesElements = new StringBuffer();
 				for (String module : modulesString.split(",")) {
 					modulesElements
 							.append("<Module xmlns=\"http://arcs.org.au/jsdl/jsdl-grisu\">"
 									+ module + "</Module>");
 				}
 				jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 						+ jp.toString() + "_XXX", modulesElements.toString());
 
 			} else if (jp.equals(JobSubmissionProperty.INPUT_FILE_URLS)) {
 
 				String inputFileUrls = jobProperties.get(jp);
 				if (inputFileUrls == null || "".equals(inputFileUrls)) {
 					jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 							+ jp.toString() + "_XXX", "");
 					continue;
 				}
 
 				StringBuffer dataStagingElements = new StringBuffer();
 				for (String inputFileUrl : inputFileUrls.split(",")) {
 					if (!FileManager.isLocal(inputFileUrl)) {
 						dataStagingElements
 								.append("<DataStaging>\n<FileName />\n<FileSystemName></FileSystemName>\n"
 										+ "<Source>\n<URI>");
 						dataStagingElements.append(inputFileUrl);
 						dataStagingElements
 								.append("</URI>\n</Source>\n</DataStaging>\n");
 					}
 				}
 
 				jsdlTemplateString = jsdlTemplateString.replaceAll("XXX_"
 						+ jp.toString() + "_XXX", dataStagingElements
 						.toString());
 
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
 
 		Document result;
 		try {
 			result = SeveralXMLHelpers.fromString(jsdlTemplateString);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new RuntimeException("Couldn't create jsdl document");
 		}
 		return result;
 	}
 
 }
