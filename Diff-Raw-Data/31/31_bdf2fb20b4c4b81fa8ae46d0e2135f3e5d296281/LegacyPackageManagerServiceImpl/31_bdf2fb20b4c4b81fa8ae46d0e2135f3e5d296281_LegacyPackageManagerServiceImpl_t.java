 package com.sixdimensions.wcm.cq.pack.service.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.maven.plugin.logging.Log;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.sixdimensions.wcm.cq.pack.dao.PackageManagerAPIDAO;
 import com.sixdimensions.wcm.cq.pack.service.PackageManagerConfig;
 import com.sixdimensions.wcm.cq.pack.service.PackageManagerService;
 
 public class LegacyPackageManagerServiceImpl implements PackageManagerService {
 
 	private static enum COMMAND {
		DELETE("rm"), INSTALL("ins"), UPLOAD("upload");
 
 		private final String cmd;
 
 		COMMAND(String cmd) {
 			this.cmd = cmd;
 		}
 
 		public String getCmd() {
 			return cmd;
 		}
 	}

 	private static final String COMMAND_KEY = "?cmd=";
 	private static final String GROUP_KEY = "&group=";
 	private static final String NAME_KEY = "&name=";
 	private static final String SERVICE_PATH = "/crx/packmgr/service.jsp";
 	private static final String SUCCESS_KEY = "200";
 	private PackageManagerConfig config;
 	private Log log;
 	private PackageManagerAPIDAO pmAPI;
 
 	public LegacyPackageManagerServiceImpl(PackageManagerConfig config) {
 		log = config.getLog();
 		this.config = config;
 		pmAPI = new PackageManagerAPIDAO(config);
 	}
 
 	/**
 	 * Assembles a URL based on the current configuration, the path and the
 	 * command to execute.
 	 * 
 	 * @param path
 	 * @param command
 	 * @return
 	 */
 	private String assembleUrl(String path, COMMAND command) {
 		log.debug("assembleUrl");
 		String url = config.getHost() + ":" + config.getPort() + SERVICE_PATH;
 		url += COMMAND_KEY + command.getCmd();
 
 		if (path.contains("/")) {
			String name = path.substring(path.indexOf("/") + 1);
			String group = path.substring(0, path.indexOf("/"));
 			url += NAME_KEY + name + GROUP_KEY + group;
 		} else {
 			url += NAME_KEY + path;
 		}
 		return url;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#delete(java
 	 * .lang.String)
 	 */
 	public void delete(String path) throws Exception {
 		log.debug("delete");
 
 		byte[] result = pmAPI.doGet(assembleUrl(path, COMMAND.DELETE));
 
 		log.debug("Result of command: " + new String(result, "UTF-8"));
 
 		try {
 			Response response = parseResponse(result);
 			if (!response.isSucceeded() && config.isErrorOnFailure()) {
 				throw new Exception("Failed to delete package, code: "
 						+ response.getCode() + " message: "
 						+ response.getMessage());
 			}
 		} catch (Exception e) {
 			throw new Exception(
 					"Exception parsing response, unable to determine command success or failure",
 					e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#dryRun(java
 	 * .lang.String)
 	 */
 	public void dryRun(String path) throws Exception {
 		throw new Exception("Dry Run is not supported in Legacy API");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#install(java
 	 * .lang.String)
 	 */
 	public void install(String path) throws Exception {
 		log.debug("install");
 
 		byte[] result = pmAPI.doGet(assembleUrl(path, COMMAND.INSTALL));
 
 		log.debug("Result of command: " + new String(result, "UTF-8"));
 
 		try {
 			Response response = parseResponse(result);
 			if (!response.isSucceeded() && config.isErrorOnFailure()) {
 				throw new Exception("Failed to install package, code: "
 						+ response.getCode() + " message: "
 						+ response.getMessage());
 			}
 		} catch (Exception e) {
 			throw new Exception(
 					"Exception parsing response, unable to determine command success or failure",
 					e);
 		}
 	}
 
 	/**
 	 * Parses the response from the server using XPath.
 	 * 
 	 * @param response
 	 *            the response to parse
 	 * @return a response object representing the response data
 	 * @throws XPathExpressionException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws ParserConfigurationException
 	 */
 	private Response parseResponse(byte[] response)
 			throws XPathExpressionException, SAXException, IOException,
 			ParserConfigurationException {
 		log.debug("parseResponse");
 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true); // never forget this!
 		Document doc = factory.newDocumentBuilder().parse(
 				new ByteArrayInputStream(response));
 
 		XPath xpath = XPathFactory.newInstance().newXPath();
 
 		// Sample response
 		//
 		// <crx version="2.0" user="admin" workspace="crx.default">
 		// <request>
 		// <param name="cmd" value="rm"/>
 		// <param name="name" value="myPackage"/>
 		// </request>
 		// <response>
 		// <status code="200">ok</status>
 		// </response>
 		// </crx>
 
 		log.debug("Parsing response code");
 		XPathExpression codeXpr = xpath.compile("/crx/response/status/@code");
 		String responseCode = ((NodeList) codeXpr.evaluate(doc,
 				XPathConstants.NODESET)).item(0).getNodeValue();
 
 		log.debug("Parsing response message");
 		XPathExpression messageXpr = xpath.compile("/crx/response/status");
 		String responseMessage = ((NodeList) messageXpr.evaluate(doc,
 				XPathConstants.NODESET)).item(0).getChildNodes().item(0)
 				.getNodeValue();
 
 		Response responseObj = new Response(SUCCESS_KEY.equals(responseCode),
 				responseCode, responseMessage);
 		log.info("Response Code: " + responseCode);
 		if (SUCCESS_KEY.equals(responseCode)) {
 			log.info("Response Message: " + responseMessage);
 		} else {
 			log.warn("Error Message: " + responseMessage);
 		}
 
 		return responseObj;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#preview(java
 	 * .lang.String)
 	 */
 	public void preview(String path) throws Exception {
 		throw new RuntimeException("Preview is not supported in Legacy API");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#upload(java
 	 * .lang.String, java.io.File)
 	 */
 	public void upload(String path, File pkg) throws Exception {
		log.debug("install");
 
		byte[] result = pmAPI.postFile(
				"http://poc.crownpartners.com:4502/crx/packmgr/service.jsp",
				pkg);

		log.debug("Result of command: " + new String(result, "UTF-8"));

		try {
			Response response = parseResponse(result);
			if (!response.isSucceeded() && config.isErrorOnFailure()) {
				throw new Exception("Failed to upload package, code: "
						+ response.getCode() + " message: "
						+ response.getMessage());
			}
		} catch (Exception e) {
			throw new Exception(
					"Exception parsing response, unable to determine command success or failure",
					e);
		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#validatePath
 	 * (java.lang.String)
 	 */
 	public boolean validatePath(String path) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
 
 /**
  * Represents a response from the Legacy API.
  * 
  * @author klcodanr
  */
 class Response {
 	private String code;
 	private String message;
 	boolean succeeded;
 
 	/**
 	 * Construct a new Response
 	 * 
 	 * @param succeeded
 	 *            whether or not the request succeeded aka the response code
 	 *            equals 200
 	 * @param code
 	 *            the response code returned by the server
 	 * @param message
 	 *            the message returned by the server usually either 'ok' or some
 	 *            sort of error
 	 */
 	protected Response(boolean succeeded, String code, String message) {
 		this.succeeded = succeeded;
 		this.code = code;
 		this.message = message;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public boolean isSucceeded() {
 		return succeeded;
 	}
 
 }
