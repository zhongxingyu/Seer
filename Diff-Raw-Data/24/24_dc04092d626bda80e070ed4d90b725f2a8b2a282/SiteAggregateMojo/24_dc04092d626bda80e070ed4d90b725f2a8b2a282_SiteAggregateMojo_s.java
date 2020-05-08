 package org.jboss.maven.plugins.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.util.FileUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Given a list of folders within a base URL, fetch their contents and collect
  * everything into a single published site TODO: get lists of zipped p2 repos,
  * metadata, etc for each component job / folder TODO: produce HTML output
  * representing aggregated information
  * 
  * @goal run
  * 
  * @phase validate
  * 
  */
 public class SiteAggregateMojo extends AbstractMojo {
 
 	/**
	 * @parameter expression="${doWarn}" default-value="false"
 	 */
 	private boolean verbose = false;
 
 	public boolean getVerbose() {
 		return verbose;
 	}
 
 	public void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	/**
	 * @parameter expression="${stagingDirectory}"
 	 *            default-value="http://download.jboss.org/jbosstools/builds/staging/"
 	 */
 	private String sourceURL = "http://download.jboss.org/jbosstools/builds/staging/";
 
 	public String getSourceURL() {
 		return sourceURL;
 	}
 
 	public void setSourceURL(String sourceURL) {
 		this.sourceURL = sourceURL;
 	}
 
 	/**
	 * @parameter expression="${stagingDirectory}" default-value="target/"
 	 */
 	private String targetDir = "target/";
 
 	public String getTargetDir() {
 		return targetDir;
 	}
 
 	public void setTargetDir(String targetDir) {
 		this.targetDir = targetDir;
 	}
 
 	private Hashtable<String, String> subfolders = new Hashtable<String, String>();
 
 	public Hashtable<String, String> getSubfolders() {
 		return subfolders;
 	}
 
 	public void execute() throws MojoExecutionException {
 		Log log = getLog();
 
 		// run against a local or remote URL
 		setSourceURL(sourceURL);
 		if (verbose) {
 			log.info("Aggregating from " + sourceURL);
 		}
 
 		// get subfolders from sourceURL
 		fetchSubfolders();
 
 		// now that we have a list of subfolders...
 
 		// 1. create composite site p2 metadata

 		createCompositeSiteMetadata("JBoss Tools Staging Repository", sourceURL
 				+ (sourceURL.endsWith("/") ? "" : "/"),
 				sourceURL.indexOf("file:/") == 0 ? "" : "all/repo/", targetDir);
 
		// 2. TODO: collect site results (non-p2 metadata)
 		for (Enumeration e = subfolders.elements(); e.hasMoreElements();) {
 			String URL = (String) e.nextElement();
 			System.out.println("Fetch metadata from " + URL);
 		}
 
 		// TODO: figure out how to publish this metadata to the site over sftp?
 		// (or just push w/ shell script publish.sh)
 	}
 
 	/**
 	 * Can fetch sub dirs from Apache dir listing URL or local file:/ folder URL
 	 */
 	public void fetchSubfolders() {
 		Log log = getLog();
 		if (sourceURL.indexOf("file:/") == 0) {
 			String sourceFolder = "/" + sourceURL.replaceAll("file:/+", "");
 			try {
 				List directoryNames = FileUtils.getDirectoryNames(new File(
 						sourceFolder), "*", ".svn, .git, CVS", true);
 				for (Iterator<String> i = directoryNames.iterator(); i
 						.hasNext();) {
 					String folder = ((String) i.next());
 					// System.out.println(sourceFolder + " :: " + folder);
 					if (!subfolders.containsKey(folder)) {
 						// store as k: /home/nboldt/tru/jmx/tests, v: tests
 						subfolders
 								.put(folder, folder.replace(sourceFolder, ""));
 					}
 				}
 			} catch (IOException e) {
 				log.error("Cound not load file " + sourceFolder);
 				e.printStackTrace();
 			}
 		} else {
 			String responseBody = getResponseFromURL(sourceURL);
 
 			Document dom = null;
 			try {
 				File tmpfile = File.createTempFile(getClass().getSimpleName(),
 						"");
 				// if (verbose) log.info("Write to " + tmpfile);
 				writeCleanXMLToFile(tmpfile, responseBody);
 				dom = DocumentBuilderFactory.newInstance().newDocumentBuilder()
 						.parse(tmpfile);
 				tmpfile.delete();
 				subfolders = getSubfoldersFromXML(dom);
 			} catch (SAXException e) {
 				log.error("Error parsing HTML from '" + sourceURL + "'");
 				e.printStackTrace();
 			} catch (ParserConfigurationException e) {
 				log.error("Error parsing '" + sourceURL + "'");
 				e.printStackTrace();
 			} catch (IOException e) {
 				log.error("IOException: " + e.getLocalizedMessage());
 				e.printStackTrace();
 			} catch (MojoExecutionException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * convenience method for remote repos in staging dir (using all/repo/
 	 * suffix)
 	 * 
 	 * @throws MojoExecutionException
 	 */
 	public void createCompositeSiteMetadata() throws MojoExecutionException {
 		createCompositeSiteMetadata("JBoss Tools Staging Repository", sourceURL
 				+ (sourceURL.endsWith("/") ? "" : "/"), "all/repo/", targetDir);
 	}
 
 	public void createCompositeSiteMetadata(String repoName, String prefix,
 			String suffix, String targetDir) throws MojoExecutionException {
 		createCompositeSiteMetadata(repoName, prefix, suffix, new File(
 				targetDir));
 	}
 
 	public void createCompositeSiteMetadata(String repoName, String prefix,
 			String suffix, File targetDir) throws MojoExecutionException {
 		Log log = getLog();
 		long repoDate = new Date().getTime();
 		StringBuilder sb = new StringBuilder();
 		sb.append("<?xml version='1.0' encoding='UTF-8'?>\n<?compositeMetadataRepository version='1.0.0'?>\n");
 		sb.append("<repository name='"
 				+ repoName
 				+ "' type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>\n");
 		sb.append("<properties size='2'>\n");
 		sb.append("<property name='p2.compressed' value='true'/>\n");
 		sb.append("<property name='p2.timestamp' value='" + repoDate + "'/>\n");
 		sb.append("</properties>\n");
 		sb.append("<children size='" + subfolders.size() + "'>\n");
 		for (Enumeration<String> e = subfolders.elements(); e.hasMoreElements();) {
 			String subfolder = (String) e.nextElement();
 			sb.append("<child location='"
 					+ prefix
 					+ (prefix.endsWith("/") || subfolder.indexOf("/") == 0 ? ""
 							: "/")
 					+ subfolder
 					+ (subfolder.endsWith("/") || suffix.indexOf("/") == 0 ? ""
 							: "/") + suffix + "'/>\n");
 			if (verbose)
 				log.info("Add to composite metadata: " + prefix + subfolder);
 		}
 		sb.append("</children>\n");
 		sb.append("</repository>\n");
 
 		// print to file, compositeContent.xml
 		writeToFile(new File(targetDir, "compositeContent.xml"), sb.toString());
 
 		sb = new StringBuilder();
 		sb.append("<?xml version='1.0' encoding='UTF-8'?>\n<?compositeArtifactRepository version='1.0.0'?>\n");
 		sb.append("<repository name='"
 				+ repoName
 				+ "' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>\n");
 		sb.append("<properties size='2'>\n");
 		sb.append("<property name='p2.compressed' value='true'/>\n");
 		sb.append("<property name='p2.timestamp' value='" + repoDate + "'/>\n");
 		sb.append("</properties>\n");
 		sb.append("<children size='" + subfolders.size() + "'>\n");
 		for (Enumeration<String> e = subfolders.elements(); e.hasMoreElements();) {
 			String subfolder = (String) e.nextElement();
 			sb.append("<child location='"
 					+ prefix
 					+ (prefix.endsWith("/") || subfolder.indexOf("/") == 0 ? ""
 							: "/")
 					+ subfolder
 					+ (subfolder.endsWith("/") || suffix.indexOf("/") == 0 ? ""
 							: "/") + suffix + "'/>\n");
 			if (verbose)
 				log.info("Add to composite artifact: " + prefix + subfolder);
 		}
 		sb.append("</children>\n");
 		sb.append("</repository>\n");
 
 		// print to file, compositeArtifacts.xml
 		writeToFile(new File(targetDir, "compositeArtifacts.xml"),
 				sb.toString());
 	}
 
 	private Hashtable<String, String> getSubfoldersFromXML(Document dom) {
 		Log log = getLog();
 		Hashtable<String, String> subfolders = new Hashtable<String, String>();
 		// collect all <tr> tags from the dom tree
 		Node table = dom.getElementsByTagName("table").item(0);
 		// System.out.println("Table node: " + table.toString());
 
 		NodeList nl = table.getChildNodes();
 		for (int j = 0; j < nl.getLength(); j++) {
 			Node n = nl.item(j);
 			if (n.getNodeName() == "tr") {
 				NodeList nl2 = n.getChildNodes();
 				// for each <tr>
 				for (int k = 0; k < nl2.getLength(); k++) {
 					Node n2 = nl2.item(k);
 					if (n2.getNodeName() == "td") {
 						// System.out.println(n2.getTextContent());
 						NodeList nl3 = n2.getChildNodes();
 						for (int l = 0; l < nl3.getLength(); l++) {
 							Node n3 = nl3.item(l);
 							if (n3.getNodeName() == "img" && n3.hasAttributes()) {
 								// System.out.println(j + ":" + k + ":" + l
 								// + ": " + n3.getNodeName());
 								NamedNodeMap attribs = n3.getAttributes();
 								for (int m = 0; m < attribs.getLength(); m++) {
 									Node attrib = attribs.item(m);
 									// if there's a child <td><img
 									// alt="[DIR]"/>
 									// System.out.println(" " +
 									// attrib.getNodeName() + "=\"" +
 									// attrib.getTextContent() + "\"");
 									if (attrib.getNodeName() == "alt"
 											&& attrib.getTextContent().equals(
 													"[DIR]")) {
 										// then get increment j and get this
 										// node's href attrib: <td><a
 										// href="">
 										// if (verbose) log.debug("TR("+j +
 										// ":" + k + ":" + l + "): " +
 										// n.getTextContent());
 										NodeList nl21 = n.getChildNodes();
 										// for each <tr>
 										for (int k1 = 0; k1 < nl21.getLength(); k1++) {
 											Node n21 = nl21.item(k1);
 											if (n21.getNodeName() == "td") {
 												NodeList nl31 = n21
 														.getChildNodes();
 												for (int l1 = 0; l1 < nl31
 														.getLength(); l1++) {
 													Node n31 = nl31.item(l1);
 													if (n31.getNodeName() == "a"
 															&& n31.hasAttributes()) {
 														NamedNodeMap attribs1 = n31
 																.getAttributes();
 														for (int m1 = 0; m1 < attribs1
 																.getLength(); m1++) {
 															Node attrib1 = attribs1
 																	.item(m1);
 															// if there's a
 															// child
 															// <td><a
 															// href="/some/path"/>
 															if (attrib1
 																	.getNodeName() == "href"
 																	&& !n31.getTextContent()
 																			.equals("Parent Directory")) {
 																// get
 																// this
 																// node's
 																// href
 																// attrib:
 																// <td><a
 																// href="">
 																// System.out
 																// .println("TR("
 																// + j
 																// + ":"
 																// + k1
 																// + ":"
 																// + l1
 																// + "): "
 																// + attrib1
 																// .getNodeValue()
 																// + ", "
 																// +
 																// n31.getTextContent());
 																if (verbose)
 																	log.debug("<a href=\""
 																			+ attrib1
 																					.getNodeValue()
 																			+ "\">");
 																// check for
 																// duplicates
 																String subfolder = attrib1
 																		.getNodeValue();
 																if (!subfolders
 																		.containsKey(subfolder)) {
 																	subfolders
 																			.put(sourceURL
 																					+ (sourceURL
 																							.endsWith("/")
 																							|| subfolder
 																									.indexOf("/") == 0 ? ""
 																							: "/")
 																					+ subfolder,
 																					subfolder);
 																}
 															}
 														}
 													}
 												}
 											}
 										}
 									}
 								}
 							}
 						}
 
 					}
 				}
 			}
 		}
 		return subfolders;
 
 	}
 
 	public String getResponseFromURL(String url) {
 		Log log = getLog();
 		String responseBody = null;
 
 		// establish a connection within 5 seconds
 		HttpClient client = new HttpClient();
 		client.getHttpConnectionManager().getParams()
 				.setConnectionTimeout(5000);
 
 		Credentials creds = new UsernamePasswordCredentials("username",
 				"password");
 		if (creds != null) {
 			client.getState().setCredentials(AuthScope.ANY, creds);
 		}
 
 		HttpMethod method = null;
 		method = new GetMethod(url);
 		method.setFollowRedirects(true);
 		// } catch (MalformedURLException murle) {
 		// System.out.println("<url> argument '" + url
 		// + "' is not a valid URL");
 		// System.exit(-2);
 		// }
 
 		// execute the method
 		try {
 			client.executeMethod(method);
 			responseBody = method.getResponseBodyAsString();
 		} catch (HttpException he) {
 			log.error("Http error connecting to '" + url + "'");
 			log.error(he.getMessage());
 			System.exit(-4);
 		} catch (IOException ioe) {
 			log.error("Unable to connect to '" + url + "'");
 			System.exit(-3);
 		}
 		// write out the request headers
 		if (verbose) {
 			log.debug("*** Request ***");
 			log.debug("Request Path: " + method.getPath());
 			log.debug("Request Query: " + method.getQueryString());
 			Header[] requestHeaders = method.getRequestHeaders();
 			for (int i = 0; i < requestHeaders.length; i++) {
 				log.debug(requestHeaders[i].toString());
 			}
 
 			// write out the response headers
 			log.debug("*** Response ***");
 			log.debug("Status Line: " + method.getStatusLine());
 			Header[] responseHeaders = method.getResponseHeaders();
 			for (int i = 0; i < responseHeaders.length; i++) {
 				log.debug(responseHeaders[i].toString());
 			}
 
 			// write out the response body
 			log.debug("*** Response Body ***");
 			log.debug(responseBody);
 		}
 
 		// clean up the connection resources
 		method.releaseConnection();
 		return responseBody;
 	}
 
 	// local file URL
 	private static String readFileAsString(String filePath)
 			throws java.io.IOException {
 		byte[] buffer = new byte[(int) new File(filePath).length()];
 		FileInputStream f = new FileInputStream(filePath);
 		f.read(buffer);
 		return new String(buffer);
 	}
 
 	public void writeDomToFile(File pomFile, Document dom)
 			throws MojoExecutionException {
 		writeDomToFile(pomFile.toString(), dom);
 	}
 
 	public void writeDomToFile(String pomFile, Document dom)
 			throws MojoExecutionException {
 		FileWriter w = null;
 		try {
 			w = new FileWriter(pomFile);
 			w.write(domToString(dom));
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error updating file " + pomFile,
 					e);
 		} finally {
 			if (w != null) {
 				try {
 					w.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 
 		}
 	}
 
 	public void writeCleanXMLToFile(File file, String string)
 			throws MojoExecutionException {
 		FileWriter w = null;
 		try {
 			w = new FileWriter(file);
 			string = cleanApacheHTML(string);
 			w.write(string);
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error updating file " + file, e);
 		} finally {
 			if (w != null) {
 				try {
 					w.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 
 		}
 	}
 
 	public void writeToFile(File file, String string)
 			throws MojoExecutionException {
 		FileWriter w = null;
 		try {
 			w = new FileWriter(file);
 			w.write(string);
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error updating file " + file, e);
 		} finally {
 			if (w != null) {
 				try {
 					w.close();
 				} catch (IOException e) {
 					// ignore
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * Remove DOCTYPE and non-breaking spaces; close <img> and
 	 * <hr>
 	 * tags for valid XML
 	 * 
 	 * @param string
 	 * @return String
 	 */
 	public String cleanApacheHTML(String string) {
 		return string
 				.replaceAll(
 						"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n",
 						"").replaceAll("(<img[^>]+)>", "$1/>")
 				.replaceAll("(<hr[^>]*)>", "$1/>").replaceAll("&nbsp;", " ");
 	}
 
 	private String domToString(Document dom) {
 		StringBuffer s = new StringBuffer();
 		Node project = dom.getElementsByTagName("project").item(0);
 		s.append("<project\n");
 		s.append(nodeAttributesToString(project));
 		s.append(">");
 		s.append(childNodesToString(project));
 		s.append("\n</project>\n");
 		return s.toString();
 	}
 
 	public String nodeAttributesToString(Node aNode) {
 		StringBuffer s = new StringBuffer();
 		NamedNodeMap nnm = aNode.getAttributes();
 		for (int j = 0; j < nnm.getLength(); j++) {
 			Node n = nnm.item(j);
 			s.append(" " + n.getNodeName() + "=\"" + n.getTextContent() + "\"");
 		}
 		return s.toString();
 	}
 
 	public String childNodesToString(Node aNode) {
 		final StringBuffer s = new StringBuffer();
 		final NodeList nl = aNode.getChildNodes();
 		for (int j = 0; j < nl.getLength(); j++) {
 			Node n = nl.item(j);
 			if (n.getNodeName() != "#text") {
 				s.append("<" + n.getNodeName() + nodeAttributesToString(n)
 						+ ">");
 				if (n.hasChildNodes()) {
 					s.append(childNodesToString(n));
 				} else {
 					s.append(n.getTextContent());
 				}
 				s.append("</" + n.getNodeName() + ">");
 			} else {
 				if (!n.getTextContent().replaceAll("[\n\r ]+", "").equals("")) {
 					s.append(n.getTextContent());
 				}
 			}
 		}
 
 		return s.toString();
 	}
 
 }
