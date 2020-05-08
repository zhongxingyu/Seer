 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  *     Jan Philipp Fiedler
  ******************************************************************************/
 package org.sociotech.communitymashup.interfaceprovider.restinterface;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPOutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.emf.common.util.AbstractEList;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.eclipselabs.emfjson.EMFJs;
 import org.eclipselabs.emfjson.resource.JsResourceFactoryImpl;
 import org.eclipselabs.emfjson.resource.JsResourceImpl;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.application.RESTInterface;
 import org.sociotech.communitymashup.data.DataPackage;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.data.Item;
 import org.sociotech.communitymashup.interfaceprovider.restinterface.html.HTMLTemplateParser;
 import org.sociotech.communitymashup.interfaceprovider.restinterface.properties.HTMLProperties;
 import org.sociotech.communitymashup.interfaceprovider.restinterface.properties.RESTInterfaceProperties;
 import org.sociotech.communitymashup.rest.ArgNotFoundException;
 import org.sociotech.communitymashup.rest.ProxyUtil;
 import org.sociotech.communitymashup.rest.RequestType;
 import org.sociotech.communitymashup.rest.RestCommand;
 import org.sociotech.communitymashup.rest.UnknownOperationException;
 import org.sociotech.communitymashup.rest.WrongArgCountException;
 import org.sociotech.communitymashup.rest.WrongArgException;
 import org.sociotech.communitymashup.security.facade.SecurityServiceFacade;
 
 // TODO !!! completely rewrite this servlet
 
 /**
  * A Servlet providing the REST-Interface of the CommunityMashup.
  * 
  * @author Jan Philipp Fiedler, Peter Lachenmaier
  */
 public class RESTServlet extends HttpServlet {
 
 	/**
 	 * Serial version uid
 	 */
 	private static final long serialVersionUID = -954341726885779928L;
 
 	/**
 	 * Local reference to the data set
 	 */
 	private DataSet dataSet;
 
 	private String serverName;
 	private int serverPort;
 	private String serverAlias = "mashup";
 	private String resourceName = "";
 
 	private List<Object> emfCacheList = null;
 	
 	public static final int TYPE_XML    = 0;
 	public static final int TYPE_JSON   = 1;
 	public static final int TYPE_JSON_P = 2;
 	public static final int TYPE_HTML 	= 3;
 	
 	private int type = TYPE_XML;
 	
 	/**
 	 * Keeps the date of the data sets modification date when cached.
 	 */
 	private Date cacheDate;
 	
 	/**
 	 * Keeps serialized data (request url -> cached data)  
 	 */
 	private Map<String, String> frontEndCache = new HashMap<String, String>();
 
 	/**
 	 * If set to true, the result will be formatted. 
 	 */
 	private boolean indentResult = true;
 	
 	/**
 	 * Name of the jsonp operation attribute. 
 	 */
 	public static final String JSONP_OPERATION_ATTRIBUTE_NAME = "jsonp";
 	
 	/**
 	 * Regular expressions to remove jsonp parameter 
 	 */
 	private static final String JSONP_MIDDLE_PARAMETER_REGEX = "&" + JSONP_OPERATION_ATTRIBUTE_NAME +"=\\S*&";
 	private static final String JSONP_FIRST_PARAMETER_REGEX = "(\\?)" + JSONP_OPERATION_ATTRIBUTE_NAME +"=\\S*&";
 	private static final String JSONP_LAST_PARAMETER_REGEX = "((\\?)|(&))" + JSONP_OPERATION_ATTRIBUTE_NAME +"=\\S*$";
 
 	
 	/**
 	 * Name of the html template operation attribute. 
 	 */
 	public static final String HTML_TEMPLATE_ATTRIBUTE_NAME = "tpl";
 	
 	/**
 	 * Regular expressions to remove html template parameter 
 	 */
 	private static final String HTML_TEMPLATE_MIDDLE_PARAMETER_REGEX = "&" + HTML_TEMPLATE_ATTRIBUTE_NAME +"=\\S*&";
 	private static final String HTML_TEMPLATE_FIRST_PARAMETER_REGEX = "(\\?)" + HTML_TEMPLATE_ATTRIBUTE_NAME +"=\\S*&";
 	private static final String HTML_TEMPLATE_LAST_PARAMETER_REGEX = "((\\?)|(&))" + HTML_TEMPLATE_ATTRIBUTE_NAME +"=\\S*$";
 
 	/**
 	 * Name of the html wrap attribute. 
 	 */
 	public static final String HTML_WRAP_ATTRIBUTE_NAME = "wrap";
 	
 	/**
 	 * Regular expressions to remove html template parameter 
 	 */
 	private static final String HTML_WRAP_MIDDLE_PARAMETER_REGEX = "&" + HTML_WRAP_ATTRIBUTE_NAME +"=\\S*&";
 	private static final String HTML_WRAP_FIRST_PARAMETER_REGEX = "(\\?)" + HTML_WRAP_ATTRIBUTE_NAME +"=\\S*&";
 	private static final String HTML_WRAP_LAST_PARAMETER_REGEX = "((\\?)|(&))" + HTML_WRAP_ATTRIBUTE_NAME +"=\\S*$";
 	
 	/**
 	 * Flag to show if a security service is needed based on the configuration.
 	 * If true and no security service is available then no data will be served.
 	 */
 	private boolean securityServiceNeeded = false;
 
 	/**
 	 * Local reference to the security service
 	 */
 	private SecurityServiceFacade securityService;
 
 	/**
 	 * The url suffix where this rest interface is available.
 	 */
 	private String urlSuffix;
 
 	/**
 	 * Reference to the rest services configuration 
 	 */
 	private RESTInterface configuration = null;
 	
 	/**
 	 * FreeMarker Template Parser for HTML-Requests
 	 */
 	private HTMLTemplateParser fmParser = null;
 	/**
 	 * Variables for HTML-Requests, will be set in constructor from configuration
 	 */
 	private String templatePath = "";
 	private String stylePath = "";
 	private String defaultCustomHtmlTemplate = "";
 	private Boolean defaultWrap = true;
 
 	private RESTInterfaceService restInterfaceService;
 
 	/**
 	 * Whether to use ident references instead of fully qualified urls.
 	 */
 	private boolean shortReferences;
 
 	/**
 	 * Whether to compress the output or not
 	 */
 	private boolean zipOutput;
 
 	/**
 	 * Whether to write zipped content length to repose header
 	 */
 	private boolean writeZipContentLength;
 
 	// Serialize Dates in ISO 8601
 	private static SimpleDateFormat sdf = new SimpleDateFormat(
 			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
 
 	/**
 	 * The default constructor.
 	 * 
 	 * @param dataSet The DataSet to be used by the REST-Interface.
 	 * @param urlSuffix The url suffix where this rest interface is available.
 	 * @param securityServiceNeeded Wheter a security service is needed or not.
 	 * @param type Serialization type {@link RESTServlet#TYPE_JSON}, {@link RESTServlet#TYPE_JSON_P} or {@link RESTServlet#TYPE_XML}
 	 * @param configuration The configuration with additional type specific properties
 	 */
 	public RESTServlet(RESTInterfaceService interfaceService, DataSet dataSet, String urlSuffix, boolean securityServiceNeeded, int type, RESTInterface configuration) {
 		this.restInterfaceService = interfaceService;
 		this.dataSet = dataSet;
 		this.securityServiceNeeded = securityServiceNeeded;
 		this.configuration  = configuration;
 
 		// url suffix is always in the form of "/" or "/suffix/"
 		this.urlSuffix = urlSuffix;
 		this.type = type;
 		
 		this.indentResult = !configuration.isPropertyTrueElseDefault(RESTInterfaceProperties.NO_FORMATTING_PROPERTY,
 																	 RESTInterfaceProperties.NO_FORMATTING_DEFAULT);
 		
 		this.shortReferences = configuration.isPropertyTrueElseDefault(RESTInterfaceProperties.SHORT_REFERENCES_PROPERTY,
 																	   RESTInterfaceProperties.SHORT_REFERENCES_DEFAULT);
 		
 		this.zipOutput = configuration.isPropertyTrueElseDefault(RESTInterfaceProperties.ZIP_OUTPUT_PROPERTY,
 				   												 RESTInterfaceProperties.ZIP_OUTPUT_DEFAULT);
 
 		this.writeZipContentLength = configuration.isPropertyTrueElseDefault(RESTInterfaceProperties.WRITE_ZIPPED_LENGTH_PROPERTY,
 					 														 RESTInterfaceProperties.WRITE_ZIPPED_LENGTH_DEFAULT);
 
 		if(type == TYPE_JSON_P)
 		{
 			// switch of indentation in jsonp case
 			indentResult = false;
 		}
 		
 		Resource resource = dataSet.eResource();
 		if (resource != null) {
 			resourceName = resource.getURI().toString();
 		}
 
 		// create cache list
 		emfCacheList = new ArrayList<Object>();
 		
 		if(type == TYPE_HTML)
 		{	
 			fmParser = new HTMLTemplateParser(restInterfaceService);
 			if(configuration.getPropertyValue(HTMLProperties.CUSTOM_TEMPLATES_PROPERTY) != null)
 				fmParser.setUseCustomTemplates(configuration.getPropertyValue(HTMLProperties.CUSTOM_TEMPLATES_PROPERTY).toLowerCase().equals("true"));
 			
 			// html type needs additional properties
 			// set path variables for HTML-Requests and update Template Parser
 			templatePath = configuration.getPropertyValue(HTMLProperties.TEMPLATE_PATH_PROPERTY);
 			stylePath = configuration.getPropertyValue(HTMLProperties.STYLE_PATH_PROPERTY);
 			defaultCustomHtmlTemplate = configuration.getPropertyValueElseDefault(HTMLProperties.DEFAULT_CUSTOM_TEMPLATE_PROPERTY, HTMLProperties.DEFAULT_CUSTOM_TEMPLATE_PROPERTY);
 			defaultWrap = configuration.isPropertyTrueElseDefault(HTMLProperties.DEFAULT_WRAP_PROPERTY, HTMLProperties.DEFAULT_WRAP_PROPERTY_DEFAULT);
 			
 			if(templatePath != null) {
 				fmParser.setTplPath(templatePath);
 			}
 			if(stylePath != null) {
 				fmParser.setStylePath(stylePath);
 			}
 			fmParser.setDefaultWrap(defaultWrap);
 			
 		}
 	}
 
 	/**
 	 * Serializes an Object using the EMF Serialization.
 	 * 
 	 * Only EObjects and ELists containing EObjects can be serialized.
 	 * 
 	 * @param o
 	 *            An EObject or EList to be serialized.
 	 * @param respEncoding
 	 *            The character encoding requested by the client.
 	 * 
 	 * @return A String containing the serialized Object or null.
 	 */
 	@SuppressWarnings("unchecked")
 	private String xml(Object o, String respEncoding) {
 
 		if (o instanceof DataSet) {
 			// access to data set should be complete without hrefs
 			return xmlDataSet((DataSet) o, respEncoding);
 		}
 
 		BasicEList<EObject> objectList;
 
 		// add the EObject(s) to the Resource
 		if (o instanceof EObject) {
 			// create a list with this
 			objectList = new BasicEList<EObject>();
 			objectList.add((EObject) o);
 
 		} else if (o instanceof BasicEList<?>) {
 			objectList = (BasicEList<EObject>) o;
 		} else {
 			return o.toString();
 		}
 
 		// create map for options
 		Map<Object, Object> options = new HashMap<Object, Object>();
 		options.put(XMLResource.OPTION_ENCODING, respEncoding);
 		
 		// add cache list as option
 		options.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, emfCacheList);
 		// enable caching
 		options.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
 
 		// force xsi type output to enable the creation of proxy object with the correct type
 		options.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.TRUE);
 
 		options.put(XMLResource.OPTION_FORMATTED, indentResult);
 				String result = "";
 	
 		try {
 			result = XMLHelperImpl.saveString(options, objectList,
 					respEncoding, null);
 			// log("XML: "+ result, LogService.LOG_DEBUG);
 		} catch (Exception e1) {
 			log("Exception while serializing to xml (" + e1.getMessage() + ")", LogService.LOG_WARNING);
 		}
 
 		// cleanup xml and replace references
 		result = postProcessXML(result);
 
 		// done
 		return result;
 	}
 
 	/**
 	 * Replaces references and cleanes up xml.
 	 * 
 	 * @param xmlInput
 	 *            Input in xml format
 	 * @return Cleaned XML
 	 */
 	private String postProcessXML(String xmlInput) {
 
 		// replace references to local items
 		String result = replaceLocalReferencesXML(xmlInput);
 
 		// replace references to local files
 		result = replaceFileReferences(result);
 
 		return result;
 	}
 	
 	/**
 	 * Replaces references in json content.
 	 * 
 	 * @param jsonInput
 	 *            Input in json format
 	 * @return Cleaned JSON
 	 */
 	private String postProcessJSON(String jsonInput) {
 
 		// replace references to local items
 		String result = replaceLocalReferencesJSON(jsonInput);
 
 		// replace references to local files
 		result = replaceFileReferences(result);
 
 		return result;
 	}
 
 	/**
 	 * Replaces all references to local files by fully qualified urls.
 	 * 
 	 * @param xmlInput
 	 *            Serialized items
 	 * @return Xml without local file references
 	 */
 	private String replaceFileReferences(String xmlInput) {
 		// "flatten" the filepaths and redirect them to the fileserver
 		// file path ends with "
 		Matcher m = Pattern.compile("file:/.*?\"").matcher(xmlInput);
 		StringBuffer sb = new StringBuffer();
 		while (m.find()) {
 			String url = m.group();
 			String[] s = url.split("/");
 			m.appendReplacement(sb, "http://" + serverName + ":" + serverPort 
 								+ urlSuffix + "files/" + s[s.length - 1]);
 		}
 		m.appendTail(sb);
 
 		xmlInput = sb.toString();
 		return xmlInput;
 	}
 
 	/**
 	 * Replaces all local resource references by fully qualified reference.
 	 * 
 	 * @param xmlInput
 	 *            Xml serialized items
 	 * @return Cleaned XML
 	 */
 	private String replaceLocalReferencesXML(String xmlInput) {
 		// TODO use URIConverter to do this
 		// unfortunately the resulting file is not yet ready: all references are
 		// local -> within the Resource
 		// but the references should point to the respective files on the
 		// FileServer/REST-Interface
 
 		// redirect references to the REST-Interface or shorten if property set
 		String replaceString = shortReferences?"href=\"":"href=\"http://" + serverName + ":" + serverPort + urlSuffix
 				+ serverAlias + "/getItemsWithIdent?ident=";
 		
		log("Replacing " + "href=\"" + resourceName + "#" + " by " + replaceString, LogService.LOG_DEBUG);
		
 		String result = xmlInput.replaceAll("href=\"" + resourceName + "#",replaceString);
 		return result;
 	}
 
 	
 	/**
 	 * Replaces all local resource references by fully qualified reference.
 	 * 
 	 * @param jsonInput
 	 *            JSON serialized items
 	 * @return input with replaced local references
 	 */
 	private String replaceLocalReferencesJSON(String jsonInput) {
 		// TODO use URIConverter to do this
 		// unfortunately the resulting file is not yet ready: all references are
 		// local -> within the Resource
 		// but the references should point to the respective files on the
 		// FileServer/REST-Interface
 
 		
 		// redirect references to the REST-Interface
 		String replaceString = shortReferences?"ref\" : \"":"ref\" : \"http://" + serverName + ":" + serverPort + urlSuffix
 				+ serverAlias + "/getItemsWithIdent?ident=";
 		
 		String result = jsonInput.replaceAll("ref\" ?: ?\"(" + resourceName + "\\#)?"  ,replaceString);
 		
 		return result;
 	}
 	/**
 	 * Replaces file references.
 	 * 
 	 * @param input
 	 *            Input string
 	 * @return Cleaned string
 	 */
 	private String postProcessString(String input) {
 
 		// replace file references
 		String result = input.replaceAll("file:/*.*/", "http://" + serverName
 				+ ":" + serverPort + urlSuffix + "files/");
 		return result;
 	}
 
 	/**
 	 * Serialized a given data set to XML
 	 * 
 	 * @param dataSet
 	 *            Data set to be serialized
 	 * @param respEncoding
 	 *            The character encoding requested by the client.
 	 * @return Xml serialized dataSet or null in error case.
 	 */
 	private String xmlDataSet(DataSet dataSet, String respEncoding) {
 
 		if (dataSet == null) {
 			return null;
 		}
 
 		// create a new ResourceSet and Resource
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
 				.put("*", new XMIResourceFactoryImpl());
 		Resource resource = resourceSet.createResource(URI
 				.createURI("http://127.0.0.1/mashup/item.xmi"));
 
 		// set the requested character encoding
 		((XMLResourceImpl) resource).setEncoding(respEncoding);
 
 		// copy the dataset
 		resource.getContents().add(EcoreUtil.copy(dataSet));
 
 		// Important for correctly encoding in UTF-8 or ASCII as Java String is
 		// UTF-16
 		// see: http://wiki.eclipse.org/EMF/FAQ
 		StringWriter sw = new StringWriter();
 		URIConverter.WriteableOutputStream outputStream = new URIConverter.WriteableOutputStream(
 				sw, respEncoding);
 
 		try {
 			// Important for correctly encoding in UTF-8 or ASCII as Java String
 			// is UTF-16
 			// see: http://wiki.eclipse.org/EMF/FAQ
 			// TODO create map only once, be aware of encoding
 			// create map for options
 			Map<Object, Object> options = new HashMap<Object, Object>();
 			options.put(XMLResource.OPTION_ENCODING, respEncoding);
 
 			// set formating
 			options.put(XMLResource.OPTION_FORMATTED, indentResult);
 			
 			// add cache list as option
 			options.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE,
 					emfCacheList);
 			// enable caching
 			options.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
 
 			// save the Resources contents
 			resource.save(outputStream, options);
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 
 		return replaceFileReferences(sw.toString());
 	}
 
 	/**
 	 * Loads all contained EObjects from a String.
 	 * 
 	 * @param s
 	 *            The String containing the EMF-serialized EObjects.
 	 * @return A list containing the deserialized EObjects.
 	 */
 	public List<EObject> load(String s) {
 		// TODO extend to hanlde json payload
 		
 		// create resource set and resource
 		ResourceSet resourceSet = new ResourceSetImpl();
 		// Register XML resource factory
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put("*", new XMIResourceFactoryImpl());
 		Resource resource = resourceSet.createResource(URI.createURI(s));
 
 		// register package in local resource registry
 		String nsURI = DataPackage.eINSTANCE.getNsURI();
 		resourceSet.getPackageRegistry().put(nsURI, DataPackage.eINSTANCE);
 		// load resource
 		try {
 			resource.load(null);
 		} catch (IOException e) {
 			log("Could not load emf objects from " + s + " due to exception (" + e.getMessage() + ")", LogService.LOG_WARNING);
 			return null;
 		}
 		List<EObject> result = new LinkedList<EObject>();
 		for (EObject c : resource.getContents()) {
 			result.add(c);
 		}
 		return result;
 	}
 
 	/**
 	 * Loads all contained EObjects from an InputStream.
 	 * 
 	 * @param is
 	 *            The InputStream containing the EMF-serialized EObjects.
 	 * @return A list containing the deserialized EObjects.
 	 */
 	public List<EObject> load(InputStream is) {
 		// create resource set and resource
 		ResourceSet resourceSet = new ResourceSetImpl();
 
 		// Register XML resource factory
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put("*", new XMIResourceFactoryImpl());
 
 		Resource resource = resourceSet.createResource(URI
 				.createURI("test.xml"));
 
 		// Resource resource = resourceSet.createResource(URI.createURI(s));
 		// register package in local resource registry
 
 		String nsURI = DataPackage.eINSTANCE.getNsURI();
 		resourceSet.getPackageRegistry().put(nsURI, DataPackage.eINSTANCE);
 		
 		List<EObject> result = new LinkedList<EObject>();
 		
 		// load resource
 		try {
 			resource.load(is, null);
 		} catch (IOException e) {
 			log("Could not parse payload due to exception (" + e.getMessage() + ")", LogService.LOG_WARNING);
 			//e.printStackTrace();
 			// return empty result
 			return result;
 		}
 		for (EObject c : resource.getContents()) {
 			result.add(c);
 		}
 		return result;
 	}
 
 	/**
 	 * Parses the RESTCommand contained in a String.
 	 * 
 	 * This Method tries to replace @ident values with the responding EObject.
 	 * 
 	 * @param string
 	 *            The String containing the RESTCommand.
 	 * @param eObjects
 	 *            The EObjects provided with the RESTCommand.
 	 * @return The parsed RESTCommand.
 	 */
 	private RestCommand parseCommand(String string, List<EObject> eObjects) {
 //		if (!string
 //				.matches("[^\\?&=]+?(\\?[^\\?&=]+?=[^\\?&=]+?(&[^\\?&=]+?=[^\\?&=]+?)*?)?"))
 //			return null;
 		String[] splitted = string.split("\\?", 2);
 
 		RestCommand command = new RestCommand(splitted[0]);
 
 		if (splitted.length <= 1) {
 			// no parameters
 			return command;
 		}
 			
 		
 		List<String> commandParts = Arrays.asList(splitted[1].split("&"));
 		String[] a;
 		Object attribute;
 		for (String s : commandParts) {
 			a = s.split("=", 2);
 			// decode attribute
 			try {
 				attribute = URLDecoder.decode(a[1], "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				attribute = null;
 			}
 			
 			if (a[1].startsWith("@")) {
 				// @ indicates an ident of the data set
 				attribute = resolveDataSetItem(a[1]);
 			} else if (a[1].startsWith("*")) {
 				// # indicates the index of a object in the post payload (eObjects parameter)
 				attribute = resolvePostPayloadItem(a[1], eObjects);
 
 			} else if (a[1].startsWith("$")) {
 				// comma separated list of idents or post payload inexes
 				List<String> refList = Arrays.asList(a[1].substring(1)
 						.split(","));
 
 				// create parameter list
 				BasicEList<Item> listParameter = new BasicEList<Item>();
 
 				// resolve every single item in the payload
 				for(String ref : refList)
 				{
 					Item singleItem = null;
 					if(ref.startsWith("*"))
 					{
 						singleItem = resolvePostPayloadItem(ref, eObjects);
 					}
 					else if(ref.startsWith("@"))
 					{
 						singleItem = resolveDataSetItem(ref);
 					}
 
 					if (singleItem != null)
 					{
 						// successfully resolved
 						listParameter.add(singleItem);
 					}
 				}
 
 				attribute = listParameter;
 			}
 
 			// add the resolved paramters to the command
 			command.addArg(a[0], attribute);
 		}
 
 		return command;
 	}
 
 	/**
 	 * Returns the item in the server side data set reference by the given reference.
 	 * 
 	 * @param ref Reference to a server side item
 	 * 
 	 * @return The item referenced by ref
 	 */
 	private Item resolveDataSetItem(String ref) {
 		return dataSet.getItemsWithIdent(ref.substring(1));
 	}
 
 	/**
 	 * Gets the item from the given eObjects list referenced by ref.
 	 * 
 	 * @param ref Reference to an item in the given list of eObjects
 	 * @param eObjects List of eObjects representing the deserialized post payload
 	 * @return The referenced item or null in error case.
 	 */
 	private Item resolvePostPayloadItem(String ref, List<EObject> eObjects) {
 
 		int index;
 		try {
 			index = new Integer(ref.substring(1));
 		} catch (Exception e) {
 			log("Could not parse index parameter: " + ref, LogService.LOG_DEBUG);
 			return null;
 		}
 		
 		Item item = null;
 		
 		if(index < eObjects.size())
 		{
 			// get the object at the index position
 			Object obj = eObjects.get(index);
 			if(!(obj instanceof Item))
 			{
 				// no item
 				return null;
 			}
 			item = (Item) obj;
 		}
 		
 		Item resolvedItem = resolveProxies(item);
 		
 		// add the resolved item to the data set
 		resolvedItem = dataSet.add(resolvedItem);
 		
 		return resolvedItem;
 	}
 
 	/**
 	 * Resolves all referenced proxy items and replaces them with items from the server side
 	 * data set.
 	 * 
 	 * @param item Item to resolve proxies for
 	 * 
 	 * @return The item with resolved proxies or null in error case
 	 */
 	private Item resolveProxies(Item item) {
 		return ProxyUtil.resolveProxies(item, dataSet);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
 	 * , javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		try {
 			processRequest(req, resp, RequestType.rtGet);
 		} catch (Exception e) {
			this.log("Error while processing GET request " + req.getRequestURI() + req.getQueryString() + " with exception: " + e.getMessage(), LogService.LOG_WARNING);
 			resp.sendError(404, "Internal Error");
			//e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		try {
 			processRequest(req, resp, RequestType.rtPost);
 		} catch (Exception e) {
			this.log("Error while processing GET request " + req.getRequestURI() + req.getQueryString() + " with exception: " + e.getMessage(), LogService.LOG_WARNING);
 			resp.sendError(404, "Internal Error");
 		}
 	}
 
 	/**
 	 * Chooses the best fitting character encoding style from the ones provided
 	 * by the client.
 	 * 
 	 * Fallback solution: ASCII.
 	 * 
 	 * @param charset
 	 *            The Charsets to choose from as provided by the
 	 *            HTTP-Accept-Charset-Header.
 	 * 
 	 * @return The most fitting charset or "ASCII" as fallback solution.
 	 */
 	private String chooseCharset(String charset) {
 		// TODO chooseCharset could be better but works
 		if (charset == null)
 			return "UTF-8";
 		String[] result = charset.split(",");
 		double max_q = 0;
 		// default charset
 		String bestCharset = "ASCII";
 		for (int i = 0; i < result.length; i++) {
 			String s = result[i];
 			// remove possible leading space
 			if (s.startsWith(" "))
 				s = s.substring(1);
 
 			double q = 0;
 			if (s.contains(";")) {
 				String[] ss = s.split(";");
 				s = ss[0];
 				if (ss.length > 2)
 					q = Integer.parseInt(ss[1].split("=")[1]);
 				if (q > max_q) {
 					max_q = q;
 					bestCharset = s;
 				}
 			}
 			// check for preferred charsets
 			if (s.equalsIgnoreCase("utf-8"))
 				return "UTF-8";
 			if (s.equalsIgnoreCase("ascii"))
 				return "ASCII";
 			result[i] = s;
 		}
 		return bestCharset;
 	}
 
 	/**
 	 * Processes a HTTP-Request passed to the Servlet.
 	 * 
 	 * @param request
 	 *            The HTTP-Request.
 	 * @param resp
 	 *            The HTTP-Response.
 	 * @param requestType
 	 *            The HTTP-Request-Method used by the client.
 	 * 
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	private void processRequest(HttpServletRequest request,
 			HttpServletResponse resp, RequestType requestType)
 			throws ServletException, IOException {
 
 		long startTime = System.currentTimeMillis();
 
 		// security check
 		if ((securityServiceNeeded && securityService == null)
 				|| (securityServiceNeeded && !securityService.isRequestAlowed(request))) {
 			// send error
 			resp.setContentType("text/plain");
 			resp.sendError(403, "Forbidden: Authentication failed.");
 			return;
 		}
 
 		// gather information about the server
 		serverName = request.getServerName();
 		serverPort = request.getServerPort();
 		if (serverAlias == null) {
 			String urls[] = request.getRequestURL().toString().split("/");
 			serverAlias = urls[urls.length - 1];
 		}
 		
 		String reqUrl = "";
 		if(request.getRequestURL() != null)
 			reqUrl = request.getRequestURL().toString();
 		if(request.getQueryString() != null && (!(request.getQueryString().equals(""))))
 			reqUrl += "?" + request.getQueryString();
 		if(!reqUrl.endsWith("/")) {
 			reqUrl += "/";
 			if(type == TYPE_HTML && request.getQueryString() == null)
 				resp.sendRedirect(resp.encodeRedirectURL(reqUrl));
 		}
 				
 
 		// reconstruct requested url
 		String requestUrl = request.getPathInfo();
 		if (requestUrl == null) {
 			// to always get a valid request
 			requestUrl = "";
 		}
 
 		String queryString = request.getQueryString();
 		if (queryString != null && !queryString.isEmpty()) {
 			// add query string to request url
 			requestUrl += "?" + queryString;
 		}
 
 		// decode request url
 		//requestUrl = URLDecoder.decode(requestUrl, "UTF-8");
 
 		String jsonpOperation = extractJsonpOperation(request);
 		if(jsonpOperation != null)
 		{
 			//  remove from request url
 			requestUrl = removeJsonpAttribute(requestUrl);
 		}
 			
 		String contentType = null;
 		String result = null;
 
 		// prepare result
 		if(type == TYPE_JSON)
 		{
 			contentType = "application/json";
 		}
 		else if(type == TYPE_XML)
 		{
 			contentType = "application/xml";
 		}
 		else if(type == TYPE_JSON_P)
 		{
 			contentType = "application/javascript";
 		}
 		else if(type == TYPE_HTML)
 		{
 			contentType = "text/html";
 			
 			// extract template and wrap parameters from url
 			String htmlTemplate = extractHtmlTemplate(request);
 			String wrap = extractHtmlWrap(request);
 			
 			fmParser.setCustomTemplate(defaultCustomHtmlTemplate);
 			if(htmlTemplate != null || wrap != null)
 			{
 				// remove html parameters from url
 				requestUrl = removeHtmlAttributes(requestUrl);
 
 				if(htmlTemplate != null)
 				{
 					fmParser.setCustomTemplate(htmlTemplate);
 				}
 				
 				if(wrap != null) {
 					Boolean wrapBoolean = wrap.toLowerCase().equals("false") ? false : true;
 					fmParser.setWrapping(wrapBoolean);
 				}
 			}
 		}
 		
 		// remove possible security parameters
 		if (securityServiceNeeded && securityService != null) {
 			requestUrl = securityService.cleanUpRequestURL(requestUrl);
 		}
 
 		if (requestUrl.isEmpty()) {
 			requestUrl = "/";
 		}
 
 		// retrieve expected character encoding
 		String respEncoding = chooseCharset(request.getHeader("Accept-Charset"));
 
 		log("Rest Request at: " + this.urlSuffix + " : " + requestUrl, LogService.LOG_INFO);
 
 		// Log the time it took to preprocess request
 		long duration = System.currentTimeMillis() - startTime;
 		log("Request preprocessing takes " + duration
 				+ "ms", LogService.LOG_DEBUG);
 		// count again
 		startTime = System.currentTimeMillis();
 
 		// get possible cached result
 		result = getCachedResult(requestUrl);
 		if(result == null)
 		{
 			// process request
 			if (requestUrl.equals("/")) {
 				// no commands -> direct access of the DataSet
 
 				// temporary switch of caching cause data set will be copied at
 				// write
 				boolean caching = dataSet.getCacheFileAttachements();
 
 				boolean delivering = dataSet.eDeliver();
 				// switch of delivering for the cache state change
 				dataSet.eSetDeliver(false);
 				dataSet.setCacheFileAttachements(false);
 
 				result = serializeDataSet(dataSet, respEncoding);
 
 				// reset caching and delivering state
 				dataSet.setCacheFileAttachements(caching);
 				dataSet.eSetDeliver(delivering);
 
 				// cache result if needed
 				cacheResult(requestUrl, result);
 
 			} else {
 				// found commands -> process them
 				LinkedList<RestCommand> input = new LinkedList<RestCommand>();
 
 				// get payload if any
 				List<EObject> eos = null;
 
 				// reconstruct list of submitted EObjects
 				if ((requestType != RequestType.rtGet)
 						&& (request.getInputStream() != null)
 						&& (request.getInputStream().available() != 0)) {
 					eos = load(request.getInputStream());
 				}
 
 				// extract RESTcommands
 				List<String> l = Arrays.asList(requestUrl.substring(1).split("/"));
 				boolean canCache = true;
 				for (String sl : l) {
 					RestCommand r = parseCommand(sl, eos);
 					if (r != null)
 					{
 						input.add(r);
 						// can only cache get commands
 						canCache &= r.isGet();
 					}						
 					else {
 						resp.sendError(404, "IncompleteCommandException: " + sl);
 						return;
 					}
 				}
 
 				// process commands
 				Object o = null;
 				try {
 
 					// TODO workaround to allow HTTP.GET (for debugging purposes)
 
 					o = dataSet.process(input, RequestType.rtPost);
 					//filtering the data if there are forbidden meta tags
 					if (securityServiceNeeded && securityService != null) {
 						o = securityService.filterRequestResults(request,o);
 					}
 
 					// o = dataSet.process(input, requestType);
 				} catch (WrongArgException e) {
 					resp.sendError(404, "WrongArgException:" + e.getMessage());
 					return;
 				} catch (WrongArgCountException e) {
 					resp.sendError(404, "WrongArgCountException: " + e.getMessage());
 					return;
 				} catch (UnknownOperationException e) {
 					resp.sendError(404,
 							"UnknownOperationException" + e.getMessage());
 					return;
 				} catch (ArgNotFoundException e) {
 					resp.sendError(404, "ArgNotFoundException" + e.getMessage());
 					return;
 				}
 
 				// prepare result to be delivered
 				if (o == null) {
 					// create empty list to be serialized as empty document
 					o = new BasicEList<Item>();
 				}
 
 				// check the type of the result
 				if (o instanceof EObject || o instanceof AbstractEList<?>) {
 					// great: it's either an EObject or a list of EObjects: just
 					// serialize it...
 
 					// create result
 					result = serialize(o, respEncoding);
 					// cache result if needed
 					// only cache emf results
 					if(canCache)
 					{
 						cacheResult(requestUrl, result);
 					}
 
 				} else if (o instanceof Date) {
 					Date d = (Date) o;
 
 					contentType = "text/plain";
 					result = sdf.format(d);
 				} else {
 					contentType = "text/plain";
 					result = postProcessString(o.toString());
 				}
 			}
 		}
 
 		if(type == TYPE_JSON_P && jsonpOperation != null)
 		{
 			if(contentType.equals("text/plain"))
 			{
 				// wrap plaintext in apostrophes for jsonp methods
 				result = "\"" + result + "\"";
 			}
 			
 			// wrap result with jsonp operation
 			result = URLDecoder.decode(jsonpOperation, "UTF-8") + "(" + result + ");";
 		}
 		
 		// Log the time it took to execute the request
 		duration = System.currentTimeMillis() - startTime;
 		log("Request execution takes " + duration
 				+ "ms", LogService.LOG_DEBUG);
 		// count again
 		startTime = System.currentTimeMillis();
 		
 		// write result
 		resp.setCharacterEncoding(respEncoding);
 		resp.setContentType(contentType);
 
 		// TODO: make configurable
 		resp.addHeader("Access-Control-Allow-Origin", "*"); // http://en.wikipedia.org/wiki/Cross-origin_resource_sharing
 					
 		// compress result if needed
 		if(zipOutput && result != null && !result.isEmpty()) {
 			resp.addHeader("Content-Encoding", "gzip");
 			
 			byte[] bytes = result.getBytes();
 			int length = bytes.length;
 			
 			if(writeZipContentLength) {
 				resp.setContentLength(bytes.length);
 			}
 			
 			GZIPOutputStream gzip = new GZIPOutputStream(resp.getOutputStream(), length);
 			gzip.write(bytes, 0, length);	
 			
 			// flush everything
 			gzip.finish();
 			gzip.flush();
 				
 		} else {
 			resp.getWriter().write(result);
 		}
 		
 		resp.flushBuffer();
 		
 		// Log the time it took to write the request
 		duration = System.currentTimeMillis() - startTime;
 		log("Request write of " + result.length() + " characters takes " + duration
 				+ "ms", LogService.LOG_DEBUG);
 		
 	}
 
 	
 	
 
 	/**
 	 * If caching is switched on the result is put to the cache.
 	 * 
 	 * @param requestUrl Url defining the request.
 	 * @param result Result for the request.
 	 */
 	private void cacheResult(String requestUrl, String result) {
 		if(!configuration.getFrontEndCaching() || requestUrl == null || result == null)
 		{
 			return;
 		}
 		
 		// put result in cache map
 		frontEndCache.put(requestUrl, result);
 	}
 
 	/**
 	 * Returns the cached result for the given request. Null if it does not exist.
 	 * 
 	 * @param requestUrl Url identifying the request.
 	 * @return The cached result or null if it does not exist.
 	 */
 	private String getCachedResult(String requestUrl) {
 		if(!configuration.getFrontEndCaching() || requestUrl == null)
 		{
 			return null;
 		}
 		
 		// check and clear cache if not up to date
 		checkCacheDate();
 		
 		// retrieve from cache map
 		return frontEndCache.get(requestUrl);
 	}
 
 	/**
 	 * Checks if the cache is up to date, otherwise clears it.
 	 */
 	private void checkCacheDate() {
 		Date lastModification = dataSet.getLastModified();
 		// check if cache is up to date (no modification since cache)
 		if(cacheDate != null && cacheDate.before(lastModification))
 		{
 			// clear cache
 			frontEndCache.clear();
 		}
 		cacheDate = lastModification;
 	}
 
 	/**
 	 * Extracts the Jsonp Operation from the request url
 	 * 
 	 * @param request Request url
 	 * @return The Jsonp Operation or null if not set
 	 */
 	private String extractJsonpOperation(HttpServletRequest request) {
 		if(type != TYPE_JSON_P)
 		{
 			return null;
 		}
 		
 		Object appKeyAttribute = request.getParameter(JSONP_OPERATION_ATTRIBUTE_NAME);
 		
 		if(appKeyAttribute instanceof String)
 		{
 			return (String)appKeyAttribute;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Extracts the Html Template from the request url
 	 * 
 	 * @param request Request url
 	 * @return The Html Template or null if not set
 	 */
 	private String extractHtmlTemplate(HttpServletRequest request) {
 		if(type != TYPE_HTML)
 		{
 			return null;
 		}
 		
 		Object appKeyAttribute = request.getParameter(HTML_TEMPLATE_ATTRIBUTE_NAME);
 		
 		if(appKeyAttribute instanceof String)
 		{
 			return (String)appKeyAttribute;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Extracts the Html Wrap Attribute from the request url
 	 * 
 	 * @param request Request url
 	 * @return The Html Wrap Attribute or null if not set
 	 */
 	private String extractHtmlWrap(HttpServletRequest request) {
 		if(type != TYPE_HTML)
 		{
 			return null;
 		}
 		
 		Object appKeyAttribute = request.getParameter(HTML_WRAP_ATTRIBUTE_NAME);
 		
 		if(appKeyAttribute instanceof String)
 		{
 			return (String)appKeyAttribute;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Serializes the given data set, depending on the needed type
 	 * 
 	 * @param dataSet Dataset to serialize
 	 * @param respEncoding needed encoding
 	 * @return The serialized result
 	 */
 	private String serializeDataSet(DataSet dataSet, String respEncoding) {
 		if(this.type == TYPE_XML)
 		{
 			return xmlDataSet(dataSet, respEncoding);
 		}
 		else if(this.type == TYPE_JSON)
 		{
 			return jsonDataSet(dataSet, respEncoding);
 		}
 		else if(this.type == TYPE_JSON_P)
 		{
 			return jsonDataSet(dataSet, respEncoding);
 		}
 		else if(this.type == TYPE_HTML)
 		{
 			return htmlDataSet(dataSet, respEncoding);
 		}
 		else
 		{
 			// return empty result
 			this.log("Unknown result type, can not serialize", LogService.LOG_WARNING);
 			return "";
 		}
 	}
 
 	/**
 	 * Serializes the given object, depending on the needed type
 	 * 
 	 * @param dataSet Dataset to serialize
 	 * @param respEncoding needed encoding
 	 * @return The serialized result
 	 */
 	private String serialize(Object object, String respEncoding) {
 		if(this.type == TYPE_XML)
 		{
 			return xml(object, respEncoding);
 		}
 		else if(this.type == TYPE_JSON)
 		{
 			return json(object, respEncoding);
 		}
 		else if(this.type == TYPE_JSON_P)
 		{
 			return json(object, respEncoding);
 		}
 		else if(this.type == TYPE_HTML)
 		{
 			return html(object, respEncoding);
 		}
 		else
 		{
 			// return empty result
 			this.log("Unknown result type, can not serialize", LogService.LOG_WARNING);
 			return "";
 		}
 	}
 	
 	/**
 	 * Post-process html-code by replacing local / file references
 	 * 
 	 * @param htmlInput The Input to post-process
 	 * @return The post-processed input
 	 */
 	private String postProcessHtml(String htmlInput) {
 
 		// replace references to local items
 		String result = replaceLocalReferencesXML(htmlInput);
 
 		// replace references to local files
 		result = replaceFileReferences(result);
 
 		return result;
 	}
 	
 	/**
 	 * Serializes the given object to html output.
 	 * 
 	 * @param object Object to generate html for
 	 * @param respEncoding needed encoding
 	 * @return The html output
 	 */
 	private String html(Object object, String respEncoding) {
 		String rooturl = "http://" + serverName + ":" + serverPort;
 		String baseurl = rooturl + urlSuffix + serverAlias;
 		if(!baseurl.endsWith("/"))
 			baseurl += "/";
 		
 		return postProcessHtml(fmParser.generate(object, rooturl, baseurl));
 	}
 	
 	/**
 	 * Serializes the given DataSet to html output.
 	 * 
 	 * @param dataSet DataSet to generate html for.
 	 * @param respEncoding needed encoding
 	 * @return The html output.
 	 */
 	private String htmlDataSet(DataSet dataSet, String respEncoding) {
 		String rooturl = "http://" + serverName + ":" + serverPort;
 		String baseurl = rooturl + urlSuffix + serverAlias;
 		if(!baseurl.endsWith("/"))
 			baseurl += "/";
 		
 		return postProcessHtml(fmParser.generate(dataSet, rooturl, baseurl));
 	}
 
 	/**
 	 * Serializes the complete data set to json.
 	 * 
 	 * @param dataSet2 Data set to serialize
 	 * @param respEncoding Encoding
 	 * @return The serialized data set as json string, null in error case.
 	 */
 	private String jsonDataSet(DataSet dataSet2, String respEncoding) {
 		if (dataSet == null) {
 			return null;
 		}
 
 		// create a new ResourceSet and Resource
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put("json", new JsResourceFactoryImpl());
 		Resource resource = resourceSet.createResource(URI
 				.createURI("http://127.0.0.1/mashup/item.json"));
 
 		// copy the dataset
 		resource.getContents().add(EcoreUtil.copy(dataSet));
 
 		// Important for correctly encoding in UTF-8 or ASCII as Java String is
 		// UTF-16
 		// see: http://wiki.eclipse.org/EMF/FAQ
 		StringWriter sw = new StringWriter();
 		URIConverter.WriteableOutputStream outputStream = new URIConverter.WriteableOutputStream(
 				sw, respEncoding);
 
 		try {
 			// Important for correctly encoding in UTF-8 or ASCII as Java String
 			// is UTF-16
 			// see: http://wiki.eclipse.org/EMF/FAQ
 			// TODO create map only once, be aware of encoding
 			// create map for options
 			Map<Object, Object> options = new HashMap<Object, Object>();
 			
 			options.put(EMFJs.OPTION_INDENT_OUTPUT, indentResult);
 			options.put(EMFJs.OPTION_SERIALIZE_TYPE, false);
 
 			// save the Resources contents
 			resource.save(outputStream, options);
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		// flush output
 		try {
 			outputStream.flush();
 		} catch (IOException e) {
 			log("Error while flushing output stream.", LogService.LOG_ERROR);
 		}
 				
 		sw.flush();
 		
 		return replaceFileReferences(sw.toString());
 	}
 	
 	/**
 	 * Serializes the given object to json
 	 * 
 	 * @param o Object to serialize
 	 * @param respEncoding Needed encoding
 	 * @return The serialized object as json
 	 */
 	@SuppressWarnings("unchecked")
 	private String json(Object o, String respEncoding) {
 		if (o instanceof DataSet) {
 			// access to data set should be complete without hrefs
 			return jsonDataSet((DataSet) o, respEncoding);
 		}
 
 		Collection<EObject> objectList;
 
 		// add the EObject(s) to the Resource
 		if (o instanceof EObject) {
 			// create a list with this
 			objectList = new BasicEList<EObject>();
 			objectList.add((EObject) o);
 
 		} else if (o instanceof BasicEList<?>) {
 			objectList = (BasicEList<EObject>) o;
 		} else {
 			return o.toString();
 		}
 
 		// create a new ResourceSet and Resource
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
 				.put("json", new JsResourceFactoryImpl());
 		Resource resource = resourceSet.createResource(URI
 				.createURI("http://127.0.0.1/mashup/item.json"));
 
 		JsResourceImpl jsRes = (JsResourceImpl) resource;
 		
 		// add objects to resource
 		// TODO copying results in loosing reference
 		// but not copying results in disappearing in old resource and therefore the data set 
 		resource.getContents().addAll(EcoreUtil.copyAll(objectList));
 		
 		if(jsRes.getErrors() != null)
 		{
 			log("" + jsRes.getErrors(), LogService.LOG_ERROR);
 		}
 		
 		// Important for correctly encoding in UTF-8 or ASCII as Java String is
 		// UTF-16
 		// see: http://wiki.eclipse.org/EMF/FAQ
 		StringWriter sw = new StringWriter(1000);
 		URIConverter.WriteableOutputStream outputStream = new URIConverter.WriteableOutputStream(
 				sw, respEncoding);
 		
 		
 
 		try {
 			// Important for correctly encoding in UTF-8 or ASCII as Java String
 			// is UTF-16
 			// see: http://wiki.eclipse.org/EMF/FAQ
 			// TODO create map only once, be aware of encoding
 			// create map for options
 			Map<Object, Object> options = new HashMap<Object, Object>();
 			
 			options.put(EMFJs.OPTION_INDENT_OUTPUT, indentResult);
 			options.put(EMFJs.OPTION_SERIALIZE_TYPE, true);
 			
 			// save the Resources contents
 			resource.save(outputStream, options);
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		
 		// flush output
 		try {
 			outputStream.flush();
 		} catch (IOException e) {
 			log("Error while flushing output stream.", LogService.LOG_ERROR);
 		}
 		
 		sw.flush();
 
 		// cleanup json and replace references
 		return postProcessJSON(sw.toString());
 	}
 
 	/**
 	 * Logs the given message using the rest interface service.
 	 * 
 	 * @param message Message to log
 	 * @param level log level
 	 */
 	private void log(String message, int level) {
 		
 		// use restinterface service to log
 		this.restInterfaceService.log(message, level);
 	}
 
 	/**
 	 * Sets the security service for this rest servlet.
 	 * 
 	 * @param securityService
 	 *            Security service.
 	 */
 	public void setSecurityService(SecurityServiceFacade securityService) {
 		this.securityService = securityService;
 	}
 	
 	private String removeJsonpAttribute(String requestURL) {
 		if(requestURL == null)
 		{
 			return null;
 		}
 
 		// remove the application key parameter
 		
 		// parameter can be on three different positions
 		
 		// last parameter
 		String result = requestURL.replaceAll(JSONP_LAST_PARAMETER_REGEX, "");
 		
 		// first parameter with following parameters
 		result = result.replaceAll(JSONP_FIRST_PARAMETER_REGEX, "?");
 		
 		// parameter in the middle with following parameters
 		result = result.replaceAll(JSONP_MIDDLE_PARAMETER_REGEX, "&");
 				
 		return result;
 	}
 	
 	/**
 	 * Removes html parameters from a url
 	 * 
 	 * @param requestURL The target url
 	 * @return The url without html parameters
 	 */
 	private String removeHtmlAttributes(String requestURL) {
 		//System.out.println(requestURL);
 		if(requestURL == null)
 		{
 			return null;
 		}
 
 		// remove the application key parameter
 		// parameter can be on three different positions
 		
 		// last parameter
 		String result = requestURL.replaceAll(HTML_TEMPLATE_LAST_PARAMETER_REGEX, "");
 		
 		// first parameter with following parameters
 		result = result.replaceAll(HTML_TEMPLATE_FIRST_PARAMETER_REGEX, "?");
 		
 		// parameter in the middle with following parameters
 		result = result.replaceAll(HTML_TEMPLATE_MIDDLE_PARAMETER_REGEX, "&");
 		
 		if(result.endsWith("?") || result.endsWith("&"))
 			result = result.substring(0, result.length()-1);
 		
 		// last parameter
 		result = result.replaceAll(HTML_WRAP_LAST_PARAMETER_REGEX, "");
 		
 		// first parameter with following parameters
 		result = result.replaceAll(HTML_WRAP_FIRST_PARAMETER_REGEX, "?");
 		
 		// parameter in the middle with following parameters
 		result = result.replaceAll(HTML_WRAP_MIDDLE_PARAMETER_REGEX, "&");
 			
 		if(result.endsWith("?") || result.endsWith("&"))
 			result = result.substring(0, result.length()-1);
 		
 		return result;
 	}
 }
