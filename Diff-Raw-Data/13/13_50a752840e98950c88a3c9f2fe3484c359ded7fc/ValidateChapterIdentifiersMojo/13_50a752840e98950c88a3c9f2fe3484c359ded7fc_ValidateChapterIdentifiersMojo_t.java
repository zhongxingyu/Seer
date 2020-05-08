 package com.discursive.plugins;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.dom4j.io.SAXReader;
 
 /**
  * This Mojo simply verifies that each chapter has an appropriate identifer.
  * 
  * @goal validate-chapter-ids
  * @phase test
  * @requiresProject
  */
 public class ValidateChapterIdentifiersMojo extends AbstractMojo {
 
 	/**
 	 * @parameter expression="${project}"
 	 * @required
 	 * @readonly
 	 */
 	protected MavenProject project;
 
 	/**
 	 * @parameter
 	 */
 	protected String wildcardFilter = "*.xml";
 
 	/**
 	 * @parameter expression="${project.build.outputDirectory}"
 	 * @required
 	 */
 	protected File outputDir;
 
 	/**
 	 * @parameter
 	 */
 	protected Boolean validateElementIds = true;
 
 	/**
 	 * @parameter
 	 */
 	protected Boolean validateSectionIds = true;
 
 	/**
 	 * @parameter
 	 */
 	protected Boolean validateExampleIds = true;
 
 	/**
 	 * @parameter
 	 */
 	protected Boolean validateFigureIds = true;
 
 	/**
 	 * @parameter
 	 */
 	protected String filenamePattern = "${elementName}-${elementId}.xml";
 
 	/**
 	 * @parameter
 	 */
 	protected String figureIdPattern = "${elementId}-fig-.*";
 
 	/**
 	 * @parameter
 	 */
 	protected String sectionIdPattern = "${elementId}-sect-.*";
 
 	/**
 	 * @parameter
 	 */
 	protected String exampleIdPattern = "${elementId}-ex-.*";
 
 	/**
 	 * @parameter
 	 */
 	protected String figureXPath = "//figure";
 
 	/**
 	 * @parameter
 	 */
 	protected String sectionXPath = "//section";
 
 	/**
 	 * @parameter
 	 */
 	protected String exampleXPath = "//example";
 
 	private List<String> validationFailures = new ArrayList<String>();
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 
 		FileFilter fileFilter = new WildcardFileFilter(wildcardFilter);
 		File[] xmlFiles = outputDir.listFiles(fileFilter);
 		for (int i = 0; i < xmlFiles.length; i++) {
 
 			try {
 				processXml(xmlFiles[i]);
 			} catch (Exception e) {
 				throw new MojoExecutionException("Problem parsing XML file: "
 						+ xmlFiles[i], e);
 			}
 
 		}
 
 		if (validationFailures.size() > 0) {
 			throw new MojoFailureException("\n"
 					+ StringUtils.join(validationFailures.iterator(), "\n"));
 		}
 
 	}
 
 	private void processXml(File file) throws MalformedURLException,
 			DocumentException {
 
 		String fileName = file.getName();
 		Document doc = parse(file.toURL());
 		Element rootElement = doc.getRootElement();
 
 		if (!fileName.startsWith(rootElement.getName())) {
 			String failure = "File: " + fileName
 					+ " contains a root element of type "
 					+ rootElement.getName()
 					+ ", but file name does not start with "
 					+ rootElement.getName();
 			validationFailures.add(failure);
 		}
 
 		String fileNameRegex = StringUtils.replace(filenamePattern,
 				"${elementName}", rootElement.getName());
 		fileNameRegex = StringUtils.replace(fileNameRegex, "${elementId}",
 				"(.*)");
 		Pattern p = Pattern.compile(fileNameRegex.trim());
 		Matcher m = p.matcher(fileName.trim());
 		if (!m.matches()) {
 			String failure = "File: " + fileName
 					+ " does not follow the regex: " + fileNameRegex;
 			validationFailures.add(failure);
 			return;
 		}
 
 		String fileId = m.group(1);
 
 		Attribute idAttr = rootElement.attribute("id");
 		String elementId = idAttr.getValue();
 
 		if (!fileId.equals(elementId)) {
 			String failure = "File: " + fileName
 					+ " the identfier from the filename '" + fileId
 					+ "' does not match the root element id of '" + elementId
 					+ "'";
 			validationFailures.add(failure);
 		}
 
 		if (validateSectionIds) {
 			validateIds(fileName, doc, elementId, sectionXPath, sectionIdPattern, "Section");
 		}
 
 		if (validateExampleIds) {
 			validateIds(fileName, doc, elementId, exampleXPath, exampleIdPattern, "Example");
 		}
 
 		if (validateFigureIds) {
 			validateIds(fileName, doc, elementId, figureXPath, figureIdPattern, "Figure");
 		}
 	}
 
 	private void validateIds(String fileName, Document doc, String elementId,
 			String xpath, String idPattern, String type) {
 		List selectNodes = doc.selectNodes(xpath);
 		List<Node> nodes = new ArrayList<Node>(selectNodes);
 		int i = 1;
 		for (Node node : nodes) {
 			Element section = (Element) node;
 			Attribute sectIdAttr = section.attribute("id");
 			if (sectIdAttr == null) {
 				String failure = "File: " + fileName + " " + type + " #" + i
 						+ " lacks an identifier";
 				validationFailures.add(failure);
 			} else {
 
 				String sectId = sectIdAttr.getValue();
 
 				String regex = StringUtils.replace(idPattern, "${elementId}",
 						elementId);
 				if (!sectId.matches(regex)) {
 					String failure = "File: " + fileName + " " + type + " "
 							+ sectId + " does not match regex " + regex;
 					validationFailures.add(failure);
 				}
 			}
 
 			i++;
 		}
 	}
 
 	public Document parse(URL url) throws DocumentException {
 		SAXReader reader = new SAXReader();
		reader.setValidation( false );
 		Document document = reader.read(url);
 		return document;
 	}
 
 }
