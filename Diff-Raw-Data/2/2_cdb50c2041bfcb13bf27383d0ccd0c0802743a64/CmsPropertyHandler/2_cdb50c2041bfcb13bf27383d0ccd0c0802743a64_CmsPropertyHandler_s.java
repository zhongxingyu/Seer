 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.cms.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.InetAddress;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.infoglue.cms.controllers.kernel.impl.simple.ServerNodeController;
 import org.infoglue.cms.entities.management.ServerNodeVO;
 import org.infoglue.deliver.util.CacheController;
 
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.module.propertyset.PropertySetManager;
 
 
 
 /**
  * CMSPropertyHandler.java
  * Created on 2002-sep-12 
  * 
  * This class is used to get properties for the system in a transparent way.
  * The second evolution of this class made it possible for properties to be fetched from the propertyset if there instead. Fallback to file.
  * 
  * @author Stefan Sik, ss@frovi.com
  * @author Mattias Bogeblad 
  */
 
 public class CmsPropertyHandler
 {
     private final static Logger logger = Logger.getLogger(CmsPropertyHandler.class.getName());
 
 	private static Properties cachedProperties 		= null;
 	private static PropertySet propertySet			= null; 
 
 	private static String serverNodeName			= null;
 	
 	private static String globalSettingsServerNodeId= "-1";
 	private static String localSettingsServerNodeId	= null;
 	
 	private static String applicationName 			= null;
 	private static File propertyFile 				= null;
 	
 	public static void setApplicationName(String theApplicationName)
 	{
 		applicationName = theApplicationName;
 	}
 	
 	public static String getApplicationName()
 	{
 		return applicationName;
 	}
 	
 	public static void setPropertyFile(File aPropertyFile)
 	{
 		propertyFile = aPropertyFile;
 	}
 	
 	/**
 	 * This method initializes the parameter hash with values.
 	 */
 
 	private static void initializeProperties()
 	{
 	    try
 		{
 			System.out.println("**************************************");
 			System.out.println("Initializing properties from file.....");
 			System.out.println("**************************************");
 			
 			cachedProperties = new Properties();
 			if(propertyFile != null)
 			    cachedProperties.load(new FileInputStream(propertyFile));
 			else
 			    cachedProperties.load(CmsPropertyHandler.class.getResourceAsStream("/" + applicationName + ".properties"));
 			
 			Enumeration enumeration = cachedProperties.keys();
 			while(enumeration.hasMoreElements())
 			{
 				String key = (String)enumeration.nextElement();
 				if(key.indexOf("webwork.") > 0)
 				{
 					webwork.config.Configuration.set(key, cachedProperties.getProperty(key)); 
 				}
 			}
 			
 	        Map args = new HashMap();
 		    args.put("globalKey", "infoglue");
 		    propertySet = PropertySetManager.getInstance("jdbc", args);
 		    
 		    serverNodeName = cachedProperties.getProperty("serverNodeName");
 		    
 		    if(serverNodeName == null || serverNodeName.length() == 0)
 		    {
 			    InetAddress localhost = InetAddress.getLocalHost();
 			    serverNodeName = localhost.getHostName();
 		    }
 		    
 		    System.out.println("serverNodeName:" + serverNodeName);
 		    
 		    initializeLocalServerNodeId();
 		}	
 		catch(Exception e)
 		{
 			cachedProperties = null;
 			logger.error("Error loading properties from file " + "/" + applicationName + ".properties" + ". Reason:" + e.getMessage());
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**
 	 * This method gets the local server node id if available.
 	 */
 
 	public static void initializeLocalServerNodeId()
 	{
         try
 	    {
 	        List serverNodeVOList = ServerNodeController.getController().getServerNodeVOList();
 	        Iterator serverNodeVOListIterator = serverNodeVOList.iterator();
 	        while(serverNodeVOListIterator.hasNext())
 	        {
 	            ServerNodeVO serverNodeVO = (ServerNodeVO)serverNodeVOListIterator.next();
 	            if(serverNodeVO.getName().equalsIgnoreCase(serverNodeName))
 	            {
 	                localSettingsServerNodeId = serverNodeVO.getId().toString();
 	                break;
 	            }
 	        }
 	    }
 	    catch(Exception e)
 	    {
 	        logger.warn("An error occurred trying to get localSettingsServerNodeId: " + e.getMessage(), e);
 	    }
 	    
 	    System.out.println("localSettingsServerNodeId:" + localSettingsServerNodeId);
 	}
 	
 	/**
 	 * This method returns all properties .
 	 */
 
 	public static Properties getProperties()
 	{
 		if(cachedProperties == null)
 			initializeProperties();
 				
 		return cachedProperties;
 	}	
 
  
 	/**
 	 * This method returns a propertyValue corresponding to the key supplied.
 	 */
 
 	public static String getProperty(String key)
 	{
 		String value;
 		if(cachedProperties == null)
 			initializeProperties();
 		
 		value = cachedProperties.getProperty(key);
 		if (value != null)
 			value = value.trim();
 				
 		return value;
 	}	
 
 
 	/**
 	 * This method sets a property during runtime.
 	 */
 
 	public static void setProperty(String key, String value)
 	{
 		if(cachedProperties == null)
 			initializeProperties();
 		
 		cachedProperties.setProperty(key, value);
 		
 		CacheController.clearCache("serverNodePropertiesCache");
 	}	
 
 	public static String getServerNodeProperty(String key, boolean inherit)
 	{
 		return getServerNodeProperty(null, key, inherit);
 	}
 	
 	/**
 	 * This method gets the serverNodeProperty but also fallbacks to the old propertyfile if not found in the propertyset.
 	 * 
 	 * @param key
 	 * @param inherit
 	 * @return
 	 */
 	
 	public static String getServerNodeProperty(String prefix, String key, boolean inherit)
 	{
 	    String value = null;
 	    
         String cacheKey = "" + prefix + "_" + key + "_" + inherit;
         String cacheName = "serverNodePropertiesCache";
 		logger.info("cacheKey:" + cacheKey);
 		value = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(value != null)
 		{
 			return value;
 		}
 	    
 	    if(localSettingsServerNodeId != null)
 	    {
 	    	if(prefix != null)
 	    	{
 		        value = propertySet.getString("serverNode_" + localSettingsServerNodeId + "_" + prefix + "_" + key);
 		        //System.out.println("Local value: " + value);
 		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 		        {
 		            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
 			        //System.out.println("Global value: " + value);
 			        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 			        {
 			            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
 				        //System.out.println("Global value: " + value);
 			        }
 
 		        }
 	    	}
 	    	else
 	    	{
 		        value = propertySet.getString("serverNode_" + localSettingsServerNodeId + "_" + key);
 		        //System.out.println("Local value: " + value);
 		        if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 		        {
 		            value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
 			        //System.out.println("Global value: " + value);
 		        }	    		
 	    	}
 	    }
 		else
 		{
 			if(prefix != null)
 	    	{
 				value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + prefix + "_" + key);
 				//System.out.println("Global value immediately: " + value);
 	    	}
 			else
 			{
 				value = propertySet.getString("serverNode_" + globalSettingsServerNodeId + "_" + key);
 				//System.out.println("Global value immediately: " + value);				
 			}
 	    }
 	    
 	    if(value == null || value.equals("") || value.equalsIgnoreCase("inherit") && inherit)
 	    {
 	        value = getProperty(key);
 	        //System.out.println("Property value: " + value);
 	    }
 	    
 	    CacheController.cacheObject(cacheName, cacheKey, value);
 	    
 	    return value;
 	}
 
 	public static String getContextRootPath()
 	{
 	    return getProperty("contextRootPath");
 	}
 
 	public static String getOperatingMode()
 	{
 	    return getProperty("operatingMode");
 	}
 	
 	public static String getUp2dateUrl()
 	{
 	    return getProperty("up2dateUrl");
 	}
 
 	public static String getURLComposerClass()
 	{
 	    return getProperty("URLComposerClass");
 	}
 
 	public static String getMaxClients()
 	{
 	    return getProperty("maxClients");
 	}
 
 	public static String getAdministratorUserName()
 	{
 	    return getProperty("administratorUserName");
 	}
 
 	public static String getAdministratorPassword()
 	{
 	    return getProperty("administratorPassword");
 	}
 
 	public static String getAdministratorEmail()
 	{
 	    return getProperty("administratorEmail");
 	}
 
 	public static String getDbRelease()
 	{
 	    return getProperty("dbRelease");
 	}
 
 	public static String getDbUser()
 	{
 	    return getProperty("dbUser");
 	}
 
 	public static String getDbPassword()
 	{
 	    return getProperty("dbPassword");
 	}
 
 	public static String getMasterServer()
 	{
 	    return getProperty("masterServer");
 	}
 	public static String getSlaveServer()
 	{
 	    return getProperty("slaveServer");
 	}
 
 	public static String getBuildName()
 	{
 	    return getProperty("buildName");
 	}
 	public static String getAdminToolsPath()
 	{
 	    return getProperty("adminToolsPath");
 	}
 	public static String getDbScriptPath()
 	{
 	    return getProperty("dbScriptPath");
 	}
 
 	public static String getDigitalAssetUploadPath()
 	{
 	    return getServerNodeProperty("digitalAssetUploadPath", true);
 	}
 	
 	public static String getExtranetCookieTimeout()
 	{
 	    return getServerNodeProperty("extranetCookieTimeout", true);
 	}
 
 	public static String getWebServicesBaseUrl()
 	{
 	    return getServerNodeProperty("webServicesBaseUrl", true);
 	}
 
 	public static String getPublicationThreadDelay()
 	{
 	    return getServerNodeProperty("publicationThreadDelay", true);
 	}
 
 	public static String getPathsToRecacheOnPublishing()
 	{
 	    return getServerNodeProperty("pathsToRecacheOnPublishing", true);
 	}
 
 	public static String getDisableTemplateDebug()
 	{
 	    return getServerNodeProperty("disableTemplateDebug", true);
 	}
 
 	public static String getTree()
 	{
 	    return getServerNodeProperty("tree", true);
 	}
 
 	public static String getTreeMode()
 	{
 	    return getServerNodeProperty("treemode", true);
 	}
 
 	public static String getPreviewDeliveryUrl()
 	{
 	    return getServerNodeProperty("previewDeliveryUrl", true);
 	}
 
 	public static String getStagingDeliveryUrl()
 	{
 	    return getServerNodeProperty("stagingDeliveryUrl", true);
 	}
 
 	public static String getEditionPageSize()
 	{
 		return getServerNodeProperty("edition.pageSize", true);
 	}
 	
 	public static String getContentTreeSort()
 	{
 		return getServerNodeProperty("content.tree.sort", true);
 	}
 
 	public static String getStructureTreeSort()
 	{
 		return getServerNodeProperty("structure.tree.sort", true);
 	}
 
 	public static String getDisableEmptyUrls()
 	{
 		return getServerNodeProperty("disableEmptyUrls", true);
 	}
 
 	public static String getCacheUpdateAction()
 	{
 		return getServerNodeProperty("cacheUpdateAction", true);
 	}
 
 	public static String getLogPath()
 	{
 		return getServerNodeProperty("logPath", true);
 	}
 
 	public static String getLogTransactions()
 	{
 		return getServerNodeProperty("logTransactions", true);
 	}
 	
 	public static String getEnableExtranetCookies()
 	{
 		return getServerNodeProperty("enableExtranetCookies", true);
 	}
 	
 	public static String getUseAlternativeBrowserLanguageCheck()
 	{
 		return getServerNodeProperty("useAlternativeBrowserLanguageCheck", true);
 	}
 	
 	public static String getCaseSensitiveRedirects()
 	{
 		return getServerNodeProperty("caseSensitiveRedirects", true);
 	}
 	
 	public static String getUseDNSNameInURI()
 	{
 		return getServerNodeProperty("useDNSNameInURI", true);
 	}
     
 	public static String getWysiwygEditor()
 	{
 		return getServerNodeProperty("wysiwygEditor", true);
 	}
 
 	public static String getProtectContentTypes()
 	{
 		return getServerNodeProperty("protectContentTypes", true);
 	}
 
 	public static String getProtectWorkflows()
 	{
 		return getServerNodeProperty("protectWorkflows", true);
 	}
 
 	public static String getProtectCategories()
 	{
 		return getServerNodeProperty("protectCategories", true);
 	}
 
 	public static String getMaxRows()
 	{
 		return getServerNodeProperty("maxRows", true);
 	}
 	
 	public static String getShowContentVersionFirst()
 	{
 		return getServerNodeProperty("showContentVersionFirst", true);
 	}
 	
 	public static String getShowComponentsFirst()
 	{
 		return getServerNodeProperty("showComponentsFirst", true);
 	}
 	
 	public static String getShowAllWorkflows()
 	{
 		return getServerNodeProperty("showAllWorkflows", true);
 	}
 	
 	public static String getIsPageCacheOn()
 	{
 	    return getServerNodeProperty("isPageCacheOn", true);
 	}
 
 	public static String getEditOnSite()
 	{
 	    return getServerNodeProperty("editOnSite", true);
 	}
 
 	public static String getUseSelectivePageCacheUpdate()
 	{
 	    return getServerNodeProperty("useSelectivePageCacheUpdate", true);
 	}
 
 	public static String getExpireCacheAutomatically()
 	{
 	    return getServerNodeProperty("expireCacheAutomatically", true);
 	}
 
 	public static String getCacheExpireInterval()
 	{
 	    return getServerNodeProperty("cacheExpireInterval", true);
 	}
 
 	public static String getSessionTimeout()
 	{
 	    return getServerNodeProperty("session.timeout", true);
 	}
 
 	public static String getCompressPageCache()
 	{
 	    return getServerNodeProperty("compressPageCache", true);
 	}
 
 	public static String getSiteNodesToRecacheOnPublishing()
 	{
 	    return getServerNodeProperty("siteNodesToRecacheOnPublishing", true);
 	}
 
 	public static String getRecachePublishingMethod()
 	{
 	    return getServerNodeProperty("recachePublishingMethod", true);
 	}
 
 	public static String getRecacheUrl()
 	{
 	    return getServerNodeProperty("recacheUrl", true);
 	}
 
 	public static String getUseUpdateSecurity()
 	{
 	    return getServerNodeProperty("useUpdateSecurity", true);
 	}
 	
 	public static String getAllowedAdminIP()
 	{
 	    return getServerNodeProperty("allowedAdminIP", true);
 	}
 
 	public static String getPageKey()
 	{
 	    return getServerNodeProperty("pageKey", true);
 	}
 	
 	public static String getCmsBaseUrl()
 	{
 	    return getServerNodeProperty("cmsBaseUrl", true);
 	}
 
 	public static String getComponentEditorUrl()
 	{
 	    return getServerNodeProperty("componentEditorUrl", true);
 	}
 
 	public static String getComponentRendererUrl()
 	{
	    return getServerNodeProperty("pacomponentRendererUrlgeKey", true);
 	}
 
 	public static String getComponentRendererAction()
 	{
 	    return getServerNodeProperty("componentRendererAction", true);
 	}
 
 	public static String getEditOnSiteUrl()
 	{
 	    return getServerNodeProperty("editOnSiteUrl", true);
 	}
 
 	public static String getUseFreeMarker()
 	{
 	    return getServerNodeProperty("useFreeMarker", true);
 	}
 
 	public static String getWebServerAddress()
 	{
 	    return getServerNodeProperty("webServerAddress", true);
 	}
 
 	public static String getApplicationBaseAction()
 	{
 	    return getServerNodeProperty("applicationBaseAction", true);
 	}
 
 	public static String getDigitalAssetBaseUrl()
 	{
 	    return getServerNodeProperty("digitalAssetBaseUrl", true);
 	}
 
 	public static String getImagesBaseUrl()
 	{
 	    return getServerNodeProperty("imagesBaseUrl", true);
 	}
 
 	public static String getDigitalAssetPath()
 	{
 	    return getServerNodeProperty("digitalAssetPath", true);
 	}
 
 	public static String getEnableNiceURI()
 	{
 	    return getServerNodeProperty("enableNiceURI", true);
 	}
 
 	public static String getNiceURIEncoding()
 	{
 	    return getServerNodeProperty("niceURIEncoding", true);
 	}
 
 	public static String getNiceURIAttributeName()
 	{
 	    return getServerNodeProperty("niceURIAttributeName", true);
 	}
 
 	public static String getRequestArgumentDelimiter()
 	{
 	    return getServerNodeProperty("requestArgumentDelimiter", true);
 	}
 
 	public static String getErrorUrl()
 	{
 	    return getServerNodeProperty("errorUrl", true);
 	}
 
 	public static String getErrorBusyUrl()
 	{
 	    return getServerNodeProperty("errorBusyUrl", true);
 	}
 
 	public static String getExternalThumbnailGeneration()
 	{
 	    return getServerNodeProperty("externalThumbnailGeneration", true);
 	}
 
 	public static String getURIEncoding()
 	{
 	    return getServerNodeProperty("URIEncoding", true);
 	}
 
 	public static String getWorkflowEncoding()
 	{
 	    return getServerNodeProperty("workflowEncoding", true);
 	}
 
 	public static String getFormsEncoding()
 	{
 	    return getServerNodeProperty("formsEncoding", true);
 	}
 
 	public static String getUseShortTableNames()
 	{
 	    return getServerNodeProperty("useShortTableNames", true);
 	}
 
 	public static String getLogDatabaseMessages()
 	{
 	    return getServerNodeProperty("logDatabaseMessages", true);
 	}
 
 	public static String getStatisticsEnabled()
 	{
 	    return getServerNodeProperty("statistics.enabled", true);
 	}
 
 	public static String getStatisticsLogPath()
 	{
 	    return getServerNodeProperty("statisticsLogPath", true);
 	}
 
 	public static String getStatisticsLogOneFilePerDay()
 	{
 	    return getServerNodeProperty("statisticsLogOneFilePerDay", true);
 	}
 
 	public static String getStatisticsLogger()
 	{
 	    return getServerNodeProperty("statisticsLogger", true);
 	}
 
 	public static String getEnablePortal()
 	{
 	    return getServerNodeProperty("enablePortal", true);
 	}
 
 	public static String getPortletBase()
 	{
 	    return getServerNodeProperty("portletBase", true);
 	}
 
 	public static String getMailSmtpHost()
 	{
 	    return getServerNodeProperty("mail.smtp.host", true);
 	}
 
 	public static String getMailSmtpAuth()
 	{
 	    return getServerNodeProperty("mail.smtp.auth", true);
 	}
 
 	public static String getMailSmtpUser()
 	{
 	    return getServerNodeProperty("mail.smtp.user", true);
 	}
 
 	public static String getMailSmtpPassword()
 	{
 	    return getServerNodeProperty("mail.smtp.password", true);
 	}
 
 	public static String getMailContentType()
 	{
 	    return getServerNodeProperty("mail.contentType", true);
 	}
 	
 	public static String getSystemEmailSender()
 	{
 	    return getServerNodeProperty("systemEmailSender", true);
 	}
 
 	public static String getHelpUrl()
 	{
 	    return getServerNodeProperty("helpUrl", true);
 	}
 
 	public static String getPreferredLanguageCode(String userName)
 	{
         return propertySet.getString("principal_" + userName + "_languageCode");
 	}
 
 	public static String getPreferredToolId(String userName)
 	{
 	    return propertySet.getString("principal_" + userName + "_defaultToolId");
 	}
 
 	public static String getPropertySetValue(String key)
 	{
 	    String value = null;
 	    
         String cacheKey = "" + key;
         String cacheName = "propertySetCache";
 		//logger.info("cacheKey:" + cacheKey);
 		value = (String)CacheController.getCachedObject(cacheName, cacheKey);
 		if(value != null)
 		{
 			return value;
 		}
 	    
 		value = propertySet.getString(key);
 		logger.info("propertySetCache did not have value... refetched:" + value);
 	    
 	    CacheController.cacheObject(cacheName, cacheKey, value);
 	    
 	    return value;
 	}
 
 	public static String getAnonymousPassword()
 	{
 		String password = "anonymous";
 		String specifiedPassword = getProperty("security.anonymous.password");
 		if(specifiedPassword != null && !specifiedPassword.equalsIgnoreCase("") && specifiedPassword.indexOf("security.anonymous.password") == -1)
 			password = specifiedPassword;
 		
 		return password;
 	}
 
 	public static String getAnonymousUser()
 	{
 		String userName = "anonymous";
 		String specifiedUserName = getProperty("security.anonymous.username");
 		if(specifiedUserName != null && !specifiedUserName.equalsIgnoreCase("") && specifiedUserName.indexOf("security.anonymous.username") == -1)
 			userName = specifiedUserName;
 		
 		return userName;
 	}
 
 }
