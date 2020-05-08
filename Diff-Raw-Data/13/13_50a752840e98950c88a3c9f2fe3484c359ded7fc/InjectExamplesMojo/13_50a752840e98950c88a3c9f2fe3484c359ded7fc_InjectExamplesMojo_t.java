 package com.discursive.plugins;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
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
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.UnArchiver;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * This Mojo simply verifies that each chapter has an appropriate identifer.
  * 
  * @goal inject-examples
  * @phase process-classes
  * @requiresProject
  */
 public class InjectExamplesMojo extends AbstractMojo {
 
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
 	 * @parameter expression="${localRepository}"
 	 * @readonly
 	 */
 	private ArtifactRepository localRepository;
 
 	/**
 	 * @component
 	 * @readonly
 	 */
 	private ArtifactFactory artifactFactory;
 
 	/**
 	 * To look up Archiver/UnArchiver implementations
 	 * 
 	 * @component
 	 */
 	protected ArchiverManager archiverManager;
 
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
 		Element rootElement = doc.getDocumentElement();
 
 		XPathFactory factory = XPathFactory.newInstance();
 		XPath xpath = factory.newXPath();
 		XPathExpression expr = xpath.compile("//programlisting");
 		Object result = expr.evaluate(doc, XPathConstants.NODESET);
 		NodeList nodes = (NodeList) result;
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Element pl = (Element) nodes.item(i);
 			String artifact = pl.getAttributeNS(attributeNamespace, "artifact");
 
 			if (artifact != null && !artifact.equals("")) {
 				File artifactFile = getArtifact(artifact);
 
 				String type = pl.getAttributeNS(attributeNamespace, "type");
 
 				if (type != null && !StringUtils.isEmpty(type)
 						&& type.equalsIgnoreCase("exec")) {
 
 					String mainClass = pl.getAttributeNS(attributeNamespace, "main-class");
 					
 					Process p = Runtime.getRuntime().exec("java -cp " + artifactFile.getPath() + " " + mainClass );
 
 					ByteArrayOutputStream baos = new ByteArrayOutputStream();
 					IOUtils.copy( p.getInputStream(), baos );
 					String content = baos.toString("UTF-8");
 					System.out.println(content);
 					pl.setTextContent(content);
 				} else {
 
 					String filename = pl.getAttributeNS(attributeNamespace,
 							"file");
 					
 					getLog().info( "Working on File: " + filename );
 
 					UnArchiver unArchiver;
 
 					unArchiver = archiverManager.getUnArchiver(artifactFile);
 
 					unArchiver.setSourceFile(artifactFile);
 
 					String property = "java.io.tmpdir";
 					// Get the temporary directory and print it.
 					String tempDirPath = System.getProperty(property);
 
 					File tempDir = new File(tempDirPath);
 					try {
 						unArchiver.extract(filename, tempDir);
 					} catch (ArchiverException e) {
 						throw new MojoExecutionException(
 								"Error finding archiver for file");
 					}
 
 					File workingFile = new File(tempDir, filename);
 					System.out.println("Working File: "
 							+ workingFile.toString());
 
 					String xpath1 = pl.getAttributeNS(attributeNamespace,
 							"xpath");
 					String excerpt = pl.getAttributeNS(attributeNamespace,
 							"excerpt");
 
 					if (xpath1 != null && !StringUtils.isEmpty(xpath1)) {
 						Document workingDoc = parse(workingFile.toURL(), false);
 						XPath xpath2 = factory.newXPath();
 						XPathExpression expr2 = xpath2.compile(xpath1);
 						System.out.println(xpath1);
 						Object result2 = expr2.evaluate(workingDoc,
 								XPathConstants.NODESET);
 						NodeList result2List = (NodeList) result2;
 						System.out.println();
 
 						ByteArrayOutputStream baos = new ByteArrayOutputStream();
 						StreamResult result1 = new StreamResult(baos);
 						DOMSource domSource = new DOMSource(result2List.item(0));
 						Transformer transformer = TransformerFactory
 								.newInstance().newTransformer();
 						transformer.setOutputProperty("omit-xml-declaration",
 								"yes");
 						transformer.transform(domSource, result1);
 						String content = baos.toString("UTF-8");
 						System.out.println(content);
 						pl.setTextContent(content);
 					} else if (excerpt != null && !StringUtils.isEmpty(excerpt)) {
 
 						getLog().info( "Working on excerpt: " + excerpt);
 						String fileContent = IOUtils
 								.toString(new FileInputStream(workingFile));
 
 						String startString = "// START " + excerpt;
 						String endString = "// END " + excerpt;
 						String omitString = "// OMIT " + excerpt;
 						String endOmitString = "// END OMIT " + excerpt;
 
 						String excerptContent = fileContent.substring(
 								fileContent.indexOf(startString)
 										+ startString.length(), fileContent
 										.indexOf(endString));
 						excerptContent = StringUtils.replace(excerptContent,
 								"\r\n", "\n");
 						excerptContent = StringUtils.replace(excerptContent,
 								"\t", "  ");
 
 						int omitIndex = excerptContent.indexOf(omitString);
 						while (omitIndex != -1) {
 
 							String extracted = excerptContent.substring(0,
 									excerptContent.indexOf(omitString));
 							extracted += excerptContent.substring(
 									excerptContent.indexOf(endOmitString)
 											+ endOmitString.length(),
 									excerptContent.length());
 							excerptContent = extracted;
 							omitIndex = excerptContent.indexOf(omitString);
 						}
 
 						pl.setTextContent(excerptContent);
 
 					}
 				}
 
 			}
 
 		}
 
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
 
 	private File getArtifact(String artifact) throws MojoFailureException {
 		String groupId, artifactId, version, classifier, packaging;
 		String[] tokens = StringUtils.split(artifact, ":");
 		if (tokens.length != 3 && tokens.length != 5)
 			throw new MojoFailureException(
 					"Invalid artifact, you must specify "
 							+ "groupId:artifactId:version:classifier:packaging"
 							+ artifact);
 		groupId = tokens[0];
 		artifactId = tokens[1];
 		version = tokens[2];
 
 		Artifact toDownload = null;
 		if (tokens.length == 5) {
 			classifier = tokens[3];
 			packaging = tokens[4];
 			toDownload = artifactFactory
 					.createArtifactWithClassifier(groupId, artifactId,
 							version, packaging, classifier);
 		} else {
 			packaging = "jar";
 			toDownload = artifactFactory.createBuildArtifact(groupId,
 					artifactId, version, packaging);
 
 		}
 
 		System.out.println(toDownload.toString());
 
 		String path = localRepository.pathOf(toDownload);
 		System.out.println(path);
 		
 		File artifactFile = new File(localRepository.getBasedir(), path);
 		return artifactFile;
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
