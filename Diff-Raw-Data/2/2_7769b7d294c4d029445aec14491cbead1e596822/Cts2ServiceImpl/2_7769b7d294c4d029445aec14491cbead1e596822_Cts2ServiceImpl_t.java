 package edu.mayo.cts2Viewer.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.mayo.bsi.cts.cts2connector.cts2search.CTS2Config;
 import edu.mayo.bsi.cts.cts2connector.cts2search.ConvenienceMethods;
 import edu.mayo.bsi.cts.cts2connector.cts2search.RESTContext;
 import edu.mayo.bsi.cts.cts2connector.cts2search.aux.CTS2Utils;
 import edu.mayo.bsi.cts.cts2connector.cts2search.aux.SearchException;
 import edu.mayo.bsi.cts.cts2connector.cts2search.aux.ServiceResultFormat;
 import edu.mayo.bsi.cts.cts2connector.cts2search.aux.VocabularyId;
 import edu.mayo.cts2Viewer.client.Cts2Service;
 import edu.mayo.cts2Viewer.server.properties.PropertiesHelper;
 import edu.mayo.cts2Viewer.shared.Credentials;
 import edu.mayo.cts2Viewer.shared.ResolvedValueSetInfo;
 import edu.mayo.cts2Viewer.shared.ServerProperties;
 import edu.mayo.cts2Viewer.shared.ValueSetInfo;
 
 /**
  * The server side implementation of the RPC service.
  */
 public class Cts2ServiceImpl extends RemoteServiceServlet implements Cts2Service {
 
 	private static final long serialVersionUID = 1L;
 
 	private static Logger logger = Logger.getLogger(Cts2ServiceImpl.class.getName());
 
 	//private static final int RESULT_LIMIT = 100;
 	private static final String ANY_NQF_NUMBER_TEXT = "Any NQF Number";
 	private static final String ANY_EMEASURE_ID_TEXT = "Any Measure ID";
 	private ConvenienceMethods cm = null;
 
 	/**
 	 * Get ValueSets that match the criteria
 	 */
 	@Override
 	public String getValueSets(String serviceName, String searchText, Map<String, String> filters)
 	        throws IllegalArgumentException {
 		String results = "";
 
 		RESTContext context = initCM(serviceName);
 
 		/* populate the parameter list with the new filters */
 		int numFilters = 0;
 		for (String filter : filters.keySet()) {
 			String value = filters.get(filter);
 			if (value != null && !value.trim().equals("")) {
 				context.setUserParameter(filter, value);
 				numFilters++;
 			} else {
 				context.removeUserParameter(filter);
 			}
 		}
 
 		try {
 
 			if (CTS2Utils.isNull(searchText) && numFilters == 0) {
 				results = cm.getAvailableValueSets(false, false, false, context);
 			} else {
 				results = cm.getMatchingValueSets(searchText, false, false, false, context);
 			}
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error retrieving ValueSets: " + e);
 			throw new IllegalArgumentException(e.getMessage());
 		}
 
 		return results;
 	}
 
 	/**
 	 * Get information on a specific ValueSet
 	 */
 	@Override
 	public ValueSetInfo getValueSetInfo(String serviceName, String valueSetName) throws IllegalArgumentException {
 
 		String results = "";
 		// RestExecuter restExecuter = RestExecuter.getInstance();
 		ValueSetInfo vsi = new ValueSetInfo();
 		RESTContext context = initCM(serviceName);
 		
 		try 
 		{
 			VocabularyId valueSetId = new VocabularyId();
 			valueSetId.name = valueSetName;
 			results = cm.getValueSetInformation(valueSetId, context);
 			vsi = getValueSetGeneralInfo(results);
 
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error retrieving ValueSets" + e);
 		}
 
 		return vsi;
 	}
 
 	/**
 	 * Get resolved ValueSet information
 	 */
 	@Override
 	public ResolvedValueSetInfo getResolvedValueSetInfo(String serviceName, String valueSetName)
 	        throws IllegalArgumentException {
 
 		String results = "";
 		ResolvedValueSetInfo rvsi = new ResolvedValueSetInfo();
 
 		try {
 				RESTContext context = initCM(serviceName);
 				results = cm.getValueSetMembers(valueSetName, context);
 
 			if (results != null && results.length() > 0) {
 				rvsi = getResolvedValueSetGeneralInfo(results);
 			}
 
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error retrieving ValueSets" + e);
 		}
 
 		return rvsi;
 	}
 
 	/**
 	 * Parse the ValueSet to get the general information from it and put it into
 	 * a container object.
 	 * 
 	 * @param xmlStr
 	 * @return
 	 */
 	private ValueSetInfo getValueSetGeneralInfo(String xmlStr) {
 
 		ValueSetInfo vsi = new ValueSetInfo();
 
 		try {
 			DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
 			xmlFact.setNamespaceAware(true);
 			DocumentBuilder builder = xmlFact.newDocumentBuilder();
 			Document document = builder.parse(new java.io.ByteArrayInputStream(xmlStr.getBytes()));
 
 			NamespaceContext namespaceContext = new NamespaceContext() {
 
 				@Override
 				public String getNamespaceURI(String prefix) {
 					String uri;
 					if (prefix.equals("cts2")) {
 						uri = "http://schema.omg.org/spec/CTS2/1.0/ValueSet";
 					} else if (prefix.equals("core")) {
 						uri = "http://schema.omg.org/spec/CTS2/1.0/Core";
 					} else if (prefix.equals("xsi")) {
 						uri = "http://www.w3.org/2001/XMLSchema-instance";
 					} else {
 						uri = null;
 					}
 					return uri;
 				}
 
 				@Override
 				public Iterator getPrefixes(String arg0) {
 					return null;
 				}
 
 				@Override
 				public String getPrefix(String arg0) {
 					return null;
 				}
 			};
 
 			XPathFactory xpathFact = XPathFactory.newInstance();
 			XPath xpath = xpathFact.newXPath();
 			xpath.setNamespaceContext(namespaceContext);
 
 			vsi.setState(xpath.evaluate(ValueSetInfo.X_PATH_RESOURCE_STATE, document));
 			vsi.setResourceName(xpath.evaluate(ValueSetInfo.X_PATH_RESOURCE_VS_NAME, document));
 			vsi.setFormalName(xpath.evaluate(ValueSetInfo.X_PATH_RESOURCE_FORMAL_NAME, document));
 			vsi.setDescription(xpath.evaluate(ValueSetInfo.X_PATH_RESOURCE_SYNOPSIS, document));
 			vsi.setRole(xpath.evaluate(ValueSetInfo.X_PATH_RESOURCE_ROLE, document));
 			vsi.setSource(xpath.evaluate(ValueSetInfo.X_PATH_RESOURCE_SOURCE, document));
 			vsi.setSourceUri(xpath.evaluate(ValueSetInfo.X_PATH_RESOURCE_SOURCE_URI, document));
 
 			// set the entire xml in the object as well.
 			vsi.setXml(xmlStr);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return vsi;
 	}
 
 	/**
 	 * Parse the ResolvedValueSet to get the general information from it and put
 	 * it into a container object.
 	 * 
 	 * @param xmlStr
 	 * @return
 	 */
 	private ResolvedValueSetInfo getResolvedValueSetGeneralInfo(String xmlStr) {
 
 		ResolvedValueSetInfo rvsi = new ResolvedValueSetInfo();
 
 		try {
 			DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
 			xmlFact.setNamespaceAware(true);
 			DocumentBuilder builder = xmlFact.newDocumentBuilder();
 			Document document = builder.parse(new java.io.ByteArrayInputStream(xmlStr.getBytes()));
 
 			NamespaceContext namespaceContext = new NamespaceContext() {
 
 				@Override
 				public String getNamespaceURI(String prefix) {
 					String uri;
 					if (prefix.equals("cts2")) {
 						uri = "http://schema.omg.org/spec/CTS2/1.0/ValueSetDefinition";
 					} else if (prefix.equals("core")) {
 						uri = "http://schema.omg.org/spec/CTS2/1.0/Core";
 					} else if (prefix.equals("xsi")) {
 						uri = "http://www.w3.org/2001/XMLSchema-instance";
 					} else {
 						uri = null;
 					}
 					return uri;
 				}
 
 				@Override
 				public Iterator getPrefixes(String arg0) {
 					return null;
 				}
 
 				@Override
 				public String getPrefix(String arg0) {
 					return null;
 				}
 			};
 
 			XPathFactory xpathFact = XPathFactory.newInstance();
 			XPath xpath = xpathFact.newXPath();
 			xpath.setNamespaceContext(namespaceContext);
 
 			rvsi.setValueSetDefinition(xpath.evaluate(ResolvedValueSetInfo.X_PATH_VS_DEFINITION, document));
 			rvsi.setValueSetDefinitionUri(xpath.evaluate(ResolvedValueSetInfo.X_PATH_VS_DEFINITION_URI, document));
 			rvsi.setValueSetDefinitionHref(xpath.evaluate(ResolvedValueSetInfo.X_PATH_VS_DEFINITION_HREF, document));
 			rvsi.setCodeSystem(xpath.evaluate(ResolvedValueSetInfo.X_PATH_CODE_SYSTEM, document));
 			rvsi.setCodeSystemUri(xpath.evaluate(ResolvedValueSetInfo.X_PATH_CODE_SYSTEM_URI, document));
 			rvsi.setCodeSystemHref(xpath.evaluate(ResolvedValueSetInfo.X_PATH_CODE_SYSTEM_HREF, document));
 			rvsi.setCodeSystemVersion(xpath.evaluate(ResolvedValueSetInfo.X_PATH_CODE_SYSTEM_VERSION, document));
 			rvsi.setCodeSystemVersionHref(xpath
 			        .evaluate(ResolvedValueSetInfo.X_PATH_CODE_SYSTEM_VERSION_HREF, document));
 
 			// set the entire xml in the object as well.
 			rvsi.setXml(xmlStr);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return rvsi;
 	}
 
 	private RESTContext initCM(String serviceName) {
 		try {
 			if (this.cm == null) 
 			{
 				this.cm = ConvenienceMethods.instance(PropertiesHelper.getInstance().getPropertiesDirectory());
 			}
 
 			if (CTS2Utils.isNull(serviceName)) 
 			{
 				//logger.log(Level.WARNING,
 				    //    "(CTS2 Service):Requested CTS2 Service Name is either initializing, null or undefined! REST Context unchanged!");
 				return null;
 			}
 
 			RESTContext context = cm.getContext(serviceName);
 			if (context != null)
 			{
 				context.setOutputFormat(ServiceResultFormat.XML);
 				//context.resultLimit = RESULT_LIMIT;
 			}
 			
 			return context;
 		} catch (Exception ex) {
 			logger.log(Level.SEVERE, ex.getMessage(), ex);
 		}
 		
 		return null;
 	}
 
 	@Override
 	public LinkedHashMap<String, String> getAvailableServices() throws IllegalArgumentException {
 		LinkedHashMap<String, String> serverOptions = new LinkedHashMap<String, String>();
 		Set<String> services = null;
 		String selectedService = null;
 
 		try 
 		{
 			RESTContext context = initCM(null);
 			services = cm.getAvailableProfiles();
 			//selectedService = cm.getCurrentProfileName();
 
 		} catch (Exception ex) {
 			logger.log(Level.SEVERE, ex.getMessage(), ex);
 		}
 
 		for (String service : services) {
 			//if (selectedService != null && selectedService.endsWith(service)) {
 			//	serverOptions.put(service, service + CTS2Utils.SELECTED_TAG);
 			//} else {
 				serverOptions.put(service, service);
 			//}
 		}
 
 		return serverOptions;
 	}
 
 	@Override
 	public LinkedHashMap<String, String> getNqfNumbers() throws IOException {
 		LinkedHashMap<String, String> nqfNumbers = new LinkedHashMap<String, String>();
 		nqfNumbers.put("", ANY_NQF_NUMBER_TEXT);
 		nqfNumbers.putAll(loadMap(PropertiesHelper.getInstance().getNqfNumbersPath()));
 		return nqfNumbers;
 	}
 
 	@Override
 	public LinkedHashMap<Integer, String> geteMeasureIds() throws IOException {
 		LinkedHashMap<Integer, String> eMeasureIds = new LinkedHashMap<Integer, String>();
 		eMeasureIds.put(-1, ANY_EMEASURE_ID_TEXT);
 		BufferedReader reader = null;
 		try {
 			String line = "";
 			reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader()
 			  .getResourceAsStream(PropertiesHelper.getInstance().getEmeasureIdsPath())));
 			while ((line = reader.readLine()) != null) {
 				if (!line.trim().equals("")) {
 					eMeasureIds.put(Integer.parseInt(line), line);
 				}
 			}
 
 		} catch (IOException ioe) {
 
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 		return eMeasureIds;
 	}
 
 	private TreeMap<String, String> loadMap(String path) throws IOException {
 		BufferedReader reader = null;
 		TreeMap<String, String> idMap = new TreeMap<String, String>();
 		try {
 			String line = "";
 			reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader()
 			        .getResourceAsStream(path)));
 			while ((line = reader.readLine()) != null) {
 				if (!line.trim().equals("")) {
 					idMap.put(line, line);
 				}
 			}
 
 		} catch (IOException ioe) {
 
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 		return idMap;
 	}
 
 	@Override
 	public String getDefaultService() throws IllegalArgumentException {
 		String defaultService = null;
 
 		try {
 			initCM(null);
 			defaultService = cm.getDefaultProfileName();
 
 		} catch (Exception ex) {
 			logger.log(Level.SEVERE, "Failed to get the defualt service. " + ex.getMessage());
 		}
 
 		return defaultService;
 	}
 
 	@Override
 	public String getEntity(String serviceName, String url) 
 	{
 		RESTContext context = initCM(serviceName);
 		String existingValue = context.getUserParameterValue(CTS2Config.REQUIRES_CREDENTIALS);
 		try 
 		{
 			if (CTS2Utils.isNull(url)) {
 				return "request url is null!!";
 			}
 
			//initCM(serviceName);
 
 			context.setUserParameter(CTS2Config.REQUIRES_CREDENTIALS, "false");
 			String result =  cm.getVocabularyEntityByURI(url, context);
 			return result;
 		} catch (SearchException e) 
 		{
 			e.printStackTrace();
 			return e.getMessage();
 		}
 		
 		finally
 		{
 			context.setUserParameter(CTS2Config.REQUIRES_CREDENTIALS, existingValue);
 		}
 	}
 
 	/**
 	 * Determine if the user needs to provide credentials for the selected
 	 * server.
 	 */
 	@Override
 	public Boolean getCredentialsRequired(String serviceName) throws IllegalArgumentException {
 
 		Boolean required;
 
 		try {
 			RESTContext context = initCM(serviceName);
 			required = new Boolean(context.secure);
 		} catch (Exception e) {
 			required = new Boolean(true);
 			logger.log(Level.SEVERE, "Error determining if credentials are required" + e);
 		}
 		return required;
 	}
 
 	/**
 	 * Validate the credentials for the given service
 	 */
 	@Override
 	public Boolean validateCredentials(edu.mayo.cts2Viewer.shared.Credentials credentials)
 	        throws IllegalArgumentException {
 
 		Boolean valid;
 
 		// just do a simple search that will return quick.
 		String searchText = "abcd";
 		RESTContext context = initCM(credentials.getServer());
 		int prevLimit = context.resultLimit;
 		
 		try {
 			context.userName = credentials.getUser();
 			context.password = credentials.getPassword();
 			
 			context.resultLimit = 1;
 
 			String result = cm.getMatchingValueSets(searchText, false, false, false, context);
 
 			
 			// if the result is null, then the login failed.
 			valid = result == null ? new Boolean(false) : new Boolean(true);
 
 		} catch (Exception e) 
 		{
 			valid = new Boolean(false);
 			logger.log(Level.WARNING, e.getMessage(), e);
 		}
 
 		finally
 		{
 			context.resultLimit = prevLimit;
 		}
 		// log an invalid login attempt.
 		if (!valid) {
 			logger.log(Level.WARNING, "User " + credentials.getUser() + " on server " + credentials.getServer()
 			        + " entered invalid credentials.");
 		}
 
 		return valid;
 	}
 
 	@Override
 	public Boolean logout(Credentials credentials) {
 		//cm.removeCurrentContext();
 		return new Boolean(true);
 	}
 
 	@Override
 	public ServerProperties getServerProperties(String serviceName) throws IllegalArgumentException {
 
 		ServerProperties serverProperties = new ServerProperties();
 
 		try {
 			// get all of the server properties needed by the client here.
 			RESTContext context = initCM(serviceName);
 
 			serverProperties.setRequireCredentials(Boolean.valueOf(context.getUserParameterValue(
 			        CTS2Config.REQUIRES_CREDENTIALS)));
 
 			String muEnabledStr = context.getUserParameterValue(CTS2Config.MUENABLED);
 			serverProperties.setShowFilters(Boolean.valueOf(muEnabledStr));
 			
 			String entityTransformService = context.transforms.get("entity");
 			serverProperties.setEntityTransformService(entityTransformService);
 		} catch (Exception e) {
 
 			logger.log(Level.SEVERE, "Error retrieving server properties for " + serviceName + ".  " + e);
 		}
 
 		return serverProperties;
 	}
 
 }
