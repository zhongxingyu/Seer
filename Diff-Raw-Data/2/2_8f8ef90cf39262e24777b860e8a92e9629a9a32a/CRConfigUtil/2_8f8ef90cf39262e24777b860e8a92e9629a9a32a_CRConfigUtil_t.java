 package com.gentics.cr;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.gentics.api.lib.datasource.WriteableDatasource;
 import com.gentics.api.portalnode.connector.PortalConnectorFactory;
 import com.gentics.cr.plink.PathResolver;
 import com.gentics.cr.template.ITemplateManager;
 import com.gentics.cr.template.VelocityTemplateManager;
 import com.gentics.cr.util.CRUtil;
 
 /**
  * 
  * Last changed: $Date$
  * @version $Revision$
  * @author $Author$
  *
  */
 public class CRConfigUtil implements CRConfig {
 
 	private Logger log;
 
 	private Properties props = new Properties();
 
 	private String name = null;
 	
 	protected ITemplateManager tmplmanager=null;
 	
 	private String response_encoding="utf-8";
 
 	private String plinktemplate = null;
 	
 	private String binaryType=null;
 	
 	private String folderType=null;
 	
 	private String pageType=null;
 	
 	private String applicationRule=null;
 
 	private WriteableDatasource ds = null;
 
 	private PathResolver pathResolver = null;
 	
 	private String portalNodeCompMode = null;
 	
 	private HashMap<String, CRConfigUtil> requestProcessors = null;
 	
 	private ArrayList<String> filterChain = null;
 	
 	private String requestProcessorClass = null;
 
 	private String xmlUrl = null;
 	
 	private String xsltUrl = null;
 	
 	private String contentid_regex = null;
 	
 	private String objectpermissionattribute = null;
 	
 	private String userpermissionattribute = null;
 	
 	protected Properties dsprops = new Properties();
 	
 	protected Properties handle_props = new Properties();
 	
 	protected boolean sharedCache = false;
 	
 	private boolean contentidurl = false;
 
 	
 	/**
 	 * Create new instance of CRConfigUtil
 	 */
 	public CRConfigUtil() {
 		Properties logprops = new Properties();
 		try {
 			if(CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}").equals("")){
 				System.setProperty("com.gentics.portalnode.confpath", System.getProperty("catalina.base")+File.separator+"conf"+File.separator+"gentics"+File.separator);
 			}
 			String confpath = CRUtil.resolveSystemProperties("${com.gentics.portalnode.confpath}/nodelog.properties");
 			//System.out.println("TRYING TO LOAD NODELOGPROPS FROM: "+confpath);
 			logprops.load(new FileInputStream(confpath));
 			PropertyConfigurator.configure(logprops);
 		} catch (IOException e) {
 			Logger.getRootLogger().error("Could not find nodelog.properties.");
 			//e.printStackTrace();
 		}catch (NullPointerException e) {
 			Logger.getRootLogger().error("Could not find nodelog.properties.");
 			//e.printStackTrace();
 		}
 		this.log = Logger.getLogger(this.getClass().getName());
 		this.log.debug("Initialized logger...");
 	}
 	
 	/**
 	 * Init Datasource
 	 */
 	public void initDS()
 	{
 		initDS(null,null);
 	}
 
 	/**
 	 * Init Datasource with Handle Properties
 	 * @param hp Handle Properties
 	 */
 	public void initDS(Properties hp)
 	{
 		initDS(hp,null);
 	}
 
 	/**
 	 * Init Datasource with Handle Properties and Datasource Properties
 	 * @param hp Handle Properties
 	 * @param dp Datasource Properties
 	 */
 	public void initDS(Properties hp, Properties dp) {
 		
 		
 		this.log.debug("Creating Writable Datasource for " + this.getName());
 
 		// Connect Content Respository
 		if(dp!=null && dp.size()!=0)
 		{
 			this.ds = PortalConnectorFactory.createWriteableDatasource(hp,dp);
 		}
 		else if(hp!=null && hp.size()!=0)
 		{
 			this.ds = PortalConnectorFactory.createWriteableDatasource(hp);
 		}
 		else{
 			for(int i = 1; i <= getRequestProcessorSize(); i++){
 				CRConfigUtil requestProcessorConfig = getRequestProcessorConfig(i);
 				if(requestProcessorConfig.handle_props!= null && requestProcessorConfig.dsprops!=null)
 				{
 					requestProcessorConfig.ds = PortalConnectorFactory.createWriteableDatasource(requestProcessorConfig.handle_props,requestProcessorConfig.dsprops);
 					this.log.debug("Created Datasource for RequestProcessor"+i);
 					if(requestProcessorConfig.ds!=null)
 					{
 						//Init PathResolver for RP-Config
 						requestProcessorConfig.pathResolver = new PathResolver(this.ds, this.applicationRule);
 					}
 				}
 				else {
 
					this.log.debug("No Datasource loaded for RequestProcessor"+i+" in "+ this.getName());
 
 				}
 			}
 		}
 		
 		if (this.ds != null) {
 
 			this.log.debug("Loaded Gentics Datasource for Content Repository Servlet for "
 							+ this.getName());
 
 			this.pathResolver = new PathResolver(this.ds, this.applicationRule);
 
 			if (this.pathResolver != null) {
 
 				this.log.debug("Loaded Pathresolver for " + this.getName());
 
 			} else {
 
 				this.log.error("Could not initialize Pathresolver for "
 						+ this.getName());
 
 			}
 
 		} 
 
 	}
 	
 	/**
 	 * Sets the Datasource to use
 	 * @param dataSource
 	 */
 	public void setDatasource(WriteableDatasource dataSource)
 	{
 		this.ds = dataSource;	
 	}
 
 	/**
 	 * Set Name of Config
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.log.debug("Set name: "+ name);
 		this.name = name;
 	}
 	
 	/**
 	 * @return 
 	 *	
 	 */
 	public String getBinaryType()
 	{
 		return(this.binaryType);
 	}
 	/**
 	 * @param type
 	 */
 	public void setBinaryType(String type)
 	{
 		this.binaryType=type;
 	}
 	
 	
 	public String getFolderType()
 	{
 		return(this.folderType);
 	}
 	public void setFolderType(String type)
 	{
 		this.folderType=type;
 	}
 	public String getPageType()
 	{
 		return(this.pageType);
 	}
 	public void setPageType(String type)
 	{
 		this.pageType=type;
 	}
 	
 	public String getApplicationRule()
 	{
 		return(this.applicationRule);
 	}
 	public void setApplicationRule(String rule)
 	{
 		this.applicationRule=rule;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 	
 	public Properties getProps()
 	{
 		return(this.props);
 	}
 
 	public void setPlinkTemplate(String template) {
 		this.plinktemplate = template;
 	}
 
 	public String getPlinkTemplate() {
 
 		if (this.plinktemplate != null && !"".equals(this.plinktemplate)) {
 			return this.plinktemplate;
 		}
 
 		this.log.warn("No plinktemplate set. Using default template '$url'.");
 
 		return "$url";
 	}
 
 	public WriteableDatasource getDatasource() {
 		return this.ds;
 	}
 
 	public PathResolver getPathResolver() {
 		return this.pathResolver;
 	}
 
 	public String toString() {
 		return this.getClass().getName() + ":" + this.props.getProperty("url");
 	}
 
 	public void setEncoding(String response_encoding) {
 		this.response_encoding = response_encoding;
 	}
 
 	public String getEncoding() {
 		return response_encoding;
 	}
 	
 	public void setRequestProcessorClass(String requestProcessorClass) {
 		this.requestProcessorClass = requestProcessorClass;
 	}
 
 	public String getRequestProcessorClass() {
 		return requestProcessorClass;
 	}
 	public void setObjectPermissionAttribute(String objectpermissionattribute) {
 		this.objectpermissionattribute = objectpermissionattribute;
 	}
 
 	public String getObjectPermissionAttribute() {
 		return objectpermissionattribute;
 	}
 	
 	public void setUserPermissionAttribute(String userpermissionattribute) {
 		this.userpermissionattribute = userpermissionattribute;
 	}
 
 	public String getUserPermissionAttribute() {
 		return userpermissionattribute;
 	}
 
 	public void setXmlUrl(String xmlUrl) {
 		this.xmlUrl = xmlUrl;
 	}
 
 	public String getXmlUrl() {
 		return xmlUrl;
 	}
 	
 	public void setXsltUrl(String xsltUrl) {
  		this.xsltUrl = xsltUrl;
 	}
 
 	public String getXsltUrl() {
 		return xsltUrl;
 	}
 	
 	public void setContentidRegex(String contentid_regex) {
 		this.contentid_regex = contentid_regex;
 	}
 
 	public String getContentidRegex() {
 		return contentid_regex;
 	}
 	
 	
 	
 	public ArrayList<String> getFilterChain(){
 		return this.filterChain;
 	}
 	
 	public int getRequestProcessorSize(){
 		return requestProcessors.size();
 	}
 	public CRConfigUtil getRequestProcessorConfig(int requestProcessorId){
 		return (CRConfigUtil) requestProcessors.get(requestProcessorId + "");
 	}
 	public CRConfigUtil getRequestProcessorConfig(String requestProcessorId){
 		return (CRConfigUtil) requestProcessors.get(requestProcessorId);
 	}
 	
 	/**
 	 * Returns a new instance of the requestprocessor configured in the config with the given requestProcessorId
 	 * @param requestProcessorId
 	 * @return RequestProcessor
 	 * @throws CRException
 	 */
 	public RequestProcessor getNewRequestProcessorInstance(int requestProcessorId) throws CRException
 	{
 		
 		CRConfigUtil requestProcessorConfig = this.getRequestProcessorConfig(requestProcessorId);
 		String requestProcessorClass = requestProcessorConfig.getRequestProcessorClass();
 		
 		this.log.debug("Instanciate RequestProcessor"+requestProcessorId+" from class "+requestProcessorClass);
 		RequestProcessor rp=null;
 		try {
 			rp = (RequestProcessor) Class.forName(requestProcessorClass).getConstructor(new Class[] {CRConfig.class}).newInstance(requestProcessorConfig);
 		} catch (Exception e) {
 			throw new CRException(e);
 		}
 		return(rp);
 	}
 	
 	public boolean getPortalNodeCompMode() {
 		if(this.portalNodeCompMode==null)
 		{
 			return(false);
 		}
 		else
 		{
 			if(this.portalNodeCompMode.toUpperCase().equals("TRUE"))
 			{
 				return(true);
 			}
 			else
 			{
 				return(false);
 			}
 		}
 	}
 	public void setPortalNodeCompMode(String mode) {
 		this.portalNodeCompMode=mode;
 	}
 	
 	public ITemplateManager getTemplateManager() {
 		if(this.tmplmanager==null && !this.getPortalNodeCompMode())
 		{
 			try
 			{
 				this.tmplmanager = new VelocityTemplateManager(this);
 			}catch(Exception e)
 			{
 				CRException ex = new CRException(e);
 				this.log.error(ex.getMessage()+ex.getStringStackTrace());
 			}
 		}
 		return this.tmplmanager;
 	}
 	public boolean useSharedCache(){
 		return sharedCache;
 	}
 	public boolean useSharedCache(boolean sharedCache){
 		this.sharedCache = sharedCache;
 		return useSharedCache();
 	}
 	
 	/**
 	 * Set property according to the name/value set
 	 * @param name
 	 * @param value
 	 * @return true if property has been set properly
 	 */
 	public boolean setProperty(String name, String value)
 	{
 		boolean valueSet = false;
 		if (value instanceof String) {
 			//RESOLVE SYSTEM PROPERTIES
 			String newvalue = CRUtil.resolveSystemProperties((String)value);
 			String key = name.toString();
 						
 			this.log.debug("Checking property '" + key + "': "+newvalue);
 			
 			if(key.equalsIgnoreCase("usecontentidurl")){
 				if("TRUE".equalsIgnoreCase(newvalue))this.contentidurl=true;
 				this.log.debug("Setting usecontentidurl to "+newvalue);
 				valueSet=true;
 			}else if(key.equalsIgnoreCase("binarytype")){
 				this.setBinaryType(newvalue);
 				this.log.debug("The binary type in this servlet will be set to the object type "+this.getBinaryType());
 				valueSet=true;
 			}else if(key.equalsIgnoreCase("usesharedcaches")){
 				this.useSharedCache(Boolean.parseBoolean(newvalue));
 				this.log.debug("The servlet will use shared caches "+this.useSharedCache());
 				valueSet=true;
 			}else if(key.equalsIgnoreCase("foldertype")){
 				this.setFolderType(newvalue);
 				this.log.debug("The folder type in this servlet will be set to the object type "+this.getFolderType());
 				valueSet=true;
 			}else if(key.equalsIgnoreCase("portalnodecompatibility")){
 				this.setPortalNodeCompMode(newvalue);
 				this.log.debug("Portalnode compatibiliti mode is set to "+newvalue);
 				valueSet=true;
 			}else if(key.equalsIgnoreCase("pagetype")){
 				this.setPageType(newvalue);
 				this.log.debug("The page type in this servlet will be set to the object type "+this.getPageType());
 				valueSet=true;
 			}else if(key.equalsIgnoreCase("applicationrule")){
 				this.setApplicationRule(newvalue);
 				this.log.debug("This servlet is working with the application rule: "+this.getApplicationRule());
 				valueSet=true;
 			}else if (key.equalsIgnoreCase("plinktemplate")) {
 				this.setPlinkTemplate(newvalue);
 				this.log.debug("Plinktemplate is set to "+newvalue);
 				valueSet=true;
 			}else if (key.equalsIgnoreCase("response-charset")){
 				this.setEncoding(newvalue);
 				this.log.debug("The response charset is set to "+this.getEncoding());
 				valueSet=true;
 			}else if (key.equalsIgnoreCase("rpClass")){
 				this.setRequestProcessorClass(newvalue);
 				this.log.debug("The RequestProcessorClass is set to "+this.getRequestProcessorClass());
 				valueSet=true;
 			}else if (key.equalsIgnoreCase("xmlUrl")){
 				this.setXmlUrl(newvalue);
 				this.log.debug("The xmlUrl is set to "+this.getXmlUrl());
 				valueSet=true;
 			}else if (key.equalsIgnoreCase("xsltUrl")){
 				this.setXsltUrl(newvalue);
 				this.log.debug("The xsltUrl is set to "+this.getXsltUrl());
 				valueSet=true;
 			}else if (key.equalsIgnoreCase("contentid_regex")){
 				this.setContentidRegex(newvalue);
 				this.log.debug("The contentid regex is set to "+this.getContentidRegex());
 				valueSet=true;
 			}else if (key.equalsIgnoreCase("objectpermissionattribute")){
 				this.setObjectPermissionAttribute(newvalue);
 				this.log.debug("The objectpermissionattribute is set to "+this.getObjectPermissionAttribute());
 				valueSet=true;
 			}else if (key.equalsIgnoreCase("userpermissionattribute")){
 				this.setUserPermissionAttribute(newvalue);
 				this.log.debug("The userpermissionattribute is set to "+this.getUserPermissionAttribute());
 				valueSet=true;
 			
 				
 			}else if (key.equalsIgnoreCase("contentid_regex")){
 				this.setContentidRegex(newvalue);
 				this.log.debug("The contentid regex is set to "+this.getContentidRegex());
 				valueSet=true;
 			}else if (key.toUpperCase().startsWith("RP.")){
 				//getRequestProcessorClasses
 				String requestProcessorNumber = key.substring(3, 4);
 				String requestProcessorAttribute = key.substring(5);
 				if(requestProcessors==null)requestProcessors = new HashMap<String, CRConfigUtil>(2);
 				if(!requestProcessors.containsKey(requestProcessorNumber))requestProcessors.put(requestProcessorNumber, new CRConfigUtil());
 				this.log.debug("Property '"+requestProcessorAttribute+"' will be added to RequestProcessor '"+requestProcessorNumber+"'");
 				CRConfigUtil requestProcessorConfig = (CRConfigUtil) requestProcessors.get(requestProcessorNumber);
 				if(requestProcessorConfig.getName()==null)requestProcessorConfig.setName(this.getName());
 				if(requestProcessorAttribute.toUpperCase().startsWith("DS.")){
 					requestProcessorConfig.dsprops.put(requestProcessorAttribute.substring(3), newvalue);
 					this.log.debug("Property '" + requestProcessorAttribute + "' passed to Datasource as '"+newvalue+"'.");
 					valueSet=true;
 				}else if(requestProcessorAttribute.toUpperCase().startsWith("DS-HANDLE.")){
 					requestProcessorConfig.handle_props.put(requestProcessorAttribute.substring(10), newvalue);
 					this.log.debug("Property '" + requestProcessorAttribute + "' passed to DatasourceHandle as '"+newvalue+"'.");
 					valueSet=true;
 				}else{
 					valueSet=requestProcessorConfig.setProperty(requestProcessorAttribute, newvalue);
 					if(valueSet)this.log.debug("Property '" + requestProcessorAttribute + "' passed to RequestProcessor as '"+newvalue+"'.");
 				}
 			}else if (key.toUpperCase().startsWith("FILTERCHAIN.")){
 				//getRequestProcessorClasses
 				String filterNumber = key.substring(12);
 				if(filterChain==null)filterChain = new ArrayList<String>(1);
 				try{
 					int index = Integer.parseInt(filterNumber)-1;
 					if(filterChain.size()>index){
 						filterChain.set(index, newvalue);
 					}
 					else
 						filterChain.add(index, newvalue);
 					this.log.debug("filterChain was extended by '"+newvalue+"' at Position '"+filterNumber+"'");
 					valueSet=true;
 				}
 				catch(NumberFormatException ex){
 					this.log.error("filterChain."+filterNumber+"="+value+" contains an illegal filterNumber ("+filterNumber+") please use Integers only");
 					ex.printStackTrace();
 					valueSet=false;
 				}
 			}
 		}
 		return valueSet;
 	
 	}
 
 	/**
 	 * defindes if to use contentidurl
 	 * @return true if using conteitdurl
 	 */
 	public boolean usesContentidUrl() {
 		
 		return this.contentidurl;
 	}
 }
