 package org.alskor.redmine.internal;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.alskor.redmine.RedmineManager;
 import org.alskor.redmine.beans.Issue;
 import org.alskor.redmine.beans.Project;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.mapping.MappingException;
 import org.exolab.castor.xml.Unmarshaller;
 import org.xml.sax.InputSource;
 
 public class RedmineXMLParser {
 
 	public static Issue parseIssueFromXML(String xml) throws RuntimeException {
		verifyStartsAsXML(xml);
 		Unmarshaller unmarshaller = RedmineXMLParser.getUnmarshaller(
 				RedmineXMLParser.MAPPING_ISSUES, Issue.class);
 
 		Issue issue = null;
 		StringReader reader = null;
 		try {
 			 System.err.println(xml);
 			reader = new StringReader(xml);
 			issue = (Issue) unmarshaller.unmarshal(reader);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 		return issue;
 	}
 
 	public static Project parseProjectFromXML(String xml)
 			throws RuntimeException {
 //		System.out.println("parseProjectFromXML:" + xml);
 		Unmarshaller unmarshaller = RedmineXMLParser.getUnmarshaller(
 				RedmineXMLParser.MAPPING_PROJECTS_LIST, Project.class);
 
 		Project project = null;
 		StringReader reader = null;
 		try {
 			reader = new StringReader(xml);
 			project = (Project) unmarshaller.unmarshal(reader);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 		return project;
 	}
 
 	public static List<Issue> parseIssuesFromXML(String xml)
 			throws RuntimeException {
 //		System.out.println("parseIssuesFromXML:"+xml);
 		verifyStartsAsXML(xml);
 		
 		xml = RedmineXMLParser.removeBadTags(xml);
 		Unmarshaller unmarshaller = RedmineXMLParser.getUnmarshaller(
 				RedmineXMLParser.MAPPING_ISSUES, ArrayList.class);
 
 		List<Issue> resultList = null;
 		StringReader reader = null;
 		try {
 			reader = new StringReader(xml);
 			resultList = (List) unmarshaller.unmarshal(reader);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 		return resultList;
 	}
 
 	// see bug https://www.hostedredmine.com/issues/8240
 	private static String removeBadTags(String xml) {
 		return xml.replaceAll("<estimated_hours></estimated_hours>", "");
 	}
 
 	/**
 	 * XML contains this line near the top: <issues type="array" limit="25"
 	 * total_count="103" offset="0"> need to parse "total_count" value
 	 * 
 	 * @return -1 (UNKNOWN) if can't parse - which means that the string is
 	 *         invalid / generated by an old Redmine version
 	 */
 	public static int parseIssuesTotalCount(String issuesXML) {
 		String reg = "<issues type=\"array\" limit=.+ total_count=\""; // \\d+
 																		// \" offset=\".+";
 		// System.out.println(issuesXML);
 		// System.out.println(reg);
 		Pattern pattern = Pattern.compile(reg);
 		Matcher matcher = pattern.matcher(issuesXML);
 		int result = RedmineXMLParser.UNKNOWN;
 		if (matcher.find()) {
 
 			int indexBeginNumber = matcher.end();
 
 			String tmp1 = issuesXML.substring(indexBeginNumber);
 			int end = tmp1.indexOf('"');
 			String numStr = tmp1.substring(0, end);
 			result = Integer.parseInt(numStr);
 		}
 		return result;
 
 	}
 
 	public static List<Project> parseProjectsFromXML(String xml) {
 //		System.out.println("parseProjectsFromXML:" + xml);
 		Unmarshaller unmarshaller = RedmineXMLParser.getUnmarshaller(
 				RedmineXMLParser.MAPPING_PROJECTS_LIST, ArrayList.class);
 
 		List<Project> list = null;
 		StringReader reader = null;
 		try {
 			reader = new StringReader(xml);
 			list = (ArrayList<Project>) unmarshaller.unmarshal(reader);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 		return list;
 	}
 
 	private static Unmarshaller getUnmarshaller(String configFile,
 			Class classToUse) {
 		InputSource inputSource = new InputSource(
 				RedmineManager.class.getResourceAsStream(configFile));
 		Mapping mapping = new Mapping();
 		mapping.loadMapping(inputSource);
 
 		Unmarshaller unmarshaller;
 		try {
 			unmarshaller = new Unmarshaller(mapping);
 		} catch (MappingException e) {
 			throw new RuntimeException(e);
 		}
 		unmarshaller.setClass(classToUse);
 		return unmarshaller;
 	}
 
 	public static final int UNKNOWN = -1;
 	public static final String MAPPING_PROJECTS_LIST = "/mapping_projects_list.xml";
 	public static final String MAPPING_ISSUES = "/mapping_issues_list.xml";
 
 	/**
 	 * @throws  RuntimeException if the text does not start with a valid XML tag.
 	 */
 	private static boolean verifyStartsAsXML(String text) {
 		String XML_START_PATTERN = "<?xml version="; // "1.0"
 														// encoding="UTF-8"?>";
 		String lines[] = text.split("\\r?\\n");
 		if ((lines.length > 0) && lines[0].startsWith(XML_START_PATTERN)) {
 			return true;
 		} else {
 			// show not more than 500 chars
 			int charsToShow = text.length() < 500 ? text.length() : 500;
 			throw new RuntimeException(
 					"RedmineXMLParser: can't parse the response. This is not a valid XML:\n\n"
 							+ text.substring(0, charsToShow) + "...");
 		}
 
 	}
 }
