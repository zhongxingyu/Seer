 package com.discursive.plugins;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * This Mojo simply verifies that each chapter has an appropriate identifer.
  * 
  * @goal wrappify
  * @phase process-classes
  * @requiresProject
  */
 public class WrappifyMojo extends AbstractMojo {
 
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
 	protected String attributeNamespace = "http://discursive.com/plugins/docbook";
 
 	/**
 	 * 
 	 * @parameter
 	 */
 	private int columnLimit = 100;
 
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
 
 	private void processXml(File file) throws MojoFailureException,
 			NoSuchArchiverException, MojoExecutionException,
 			ParserConfigurationException, SAXException, IOException,
 			XPathExpressionException, TransformerFactoryConfigurationError,
 			TransformerException {
 
 		String fileName = file.getName();
 		Document doc = parse(file.toURL(), true);
 
 		String plXPATH = "//programlisting";
 		String elementName = "Program Listing";
 
 		checkLines(fileName, doc, plXPATH, elementName);
 
 		String sXPATH = "//screen";
 		String sElementName = "Screen";
 
 		checkLines(fileName, doc, sXPATH, sElementName);
 
 		StreamResult result1 = new StreamResult(file.toURI().getPath());
 		DOMSource domSource = new DOMSource(doc);
 		Transformer transformer = TransformerFactory.newInstance()
 				.newTransformer();
 
 		transformer.setOutputProperty("doctype-public",
 				"-//OASIS//DTD DocBook XML V4.5//EN");
 		transformer.setOutputProperty("doctype-system",
 				"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd");
 		transformer.transform(domSource, result1);
 
 	}
 
 	private void checkLines(String fileName, Document doc, String plXPATH,
 			String elementName) throws XPathExpressionException {
 		XPathFactory factory = XPathFactory.newInstance();
 		XPath xpath = factory.newXPath();
 		XPathExpression expr = xpath.compile(plXPATH);
 		Object result = expr.evaluate(doc, XPathConstants.NODESET);
 		NodeList nodes = (NodeList) result;
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Element pl = (Element) nodes.item(i);
 
 			String listing = pl.getTextContent();
 			String[] lines = StringUtils.split(listing, "\n");
 			List<String> newLines = new ArrayList<String>();
 			int j = 1;
 			for (String line : lines) {
 
 				String wrap = pl.getAttributeNS(attributeNamespace, "wrap");
 
 				if (line.length() > columnLimit) {
 					if (wrap != null && !StringUtils.isEmpty(wrap)) {
 
 						if (wrap.equalsIgnoreCase("force")) {
 							getLog().info( "Forcing a wrap" );
 							while(line.length() > columnLimit) {
 								newLines.add( line.substring( 0, columnLimit ) );
 								line = line.substring( columnLimit );
 							} 
 							
 							newLines.add( line );
 							
 						}
 
 					} else {
 
 						validationFailures.add("File: " + fileName + " "
 								+ elementName + " #" + i + " contains a "
 								+ line.length() + " column line at line " + j
 								+ ".  Greater than " + columnLimit + " limit.");
 					}
 				} else {
 					newLines.add( line );
 				}
 				j++;
 			}
 			
 			pl.setTextContent( StringUtils.join( newLines.toArray(), "\n") );
 
 		}
 	}
 
 	public Document parse(URL url, boolean namespaceAware)
 			throws ParserConfigurationException, SAXException, IOException {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(namespaceAware); // never forget this!
		factory.setValidating( false );
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		Document doc = builder.parse(url.openStream(), "test");
 		return doc;
 	}
 
 }
