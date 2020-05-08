 package com.mob.plugin.dal;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.sql.*;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
 
 import com.mob.commons.plugins.servicemodel.ExternalAttribution;
 import com.mob.commons.plugins.servicemodel.MainMenuItem;
 import com.mob.commons.plugins.servicemodel.PluginArt;
 import com.mob.commons.plugins.servicemodel.PluginDataCall;
 import com.mob.commons.plugins.servicemodel.PluginScript;
 import com.mob.commons.plugins.servicemodel.ServiceAlias;
 
 public class PluginDeploymentSqlAccessLayer extends SqlMapClientDaoSupport implements IPluginDeploymentAccessLayer {
 	public static final Integer INVALID_PLUGIN_ID = null;
 	public static final Integer INVALID_SCRIPT_ID = null;
 	private Logger logger = Logger.getLogger(this.getClass());
 	
 	
 	@Override
 	public boolean canConnect() {
 		boolean retval = false;
 		
 		try {
 			super.getSqlMapClient().queryForObject("canConnect");
 			retval = true;
 		} catch (SQLException e) {
 			logger.error("Could not connect to database");
 			logger.info(e);
 		}
 		
 		return retval;
 	}
 	
 	@Override
 	public Integer addPlugin(String pluginName, String company, String version, String role, String tags, String deployIdentity, String externalServices, ServiceAlias[] serviceAliases, String description, String icon, Integer priority, ExternalAttribution[] attributions)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginName", pluginName);
 		params.put("company", company);
 		params.put("version", version);
 		params.put("role", role);
 		params.put("tags", tags);
 		params.put("deployIdentity", deployIdentity);
 		params.put("externalServices", externalServices);
 		params.put("serviceAliases", getObjectBlob(serviceAliases, "service aliases"));
 		params.put("description", description);
 		params.put("icon", icon);
 		params.put("priority", priority);
 		params.put("attributions", getObjectBlob(attributions, "attributions"));
 		
 		return queryForObject("addPlugin", params, company + "-" + pluginName);
 	}
 	
 	@Override
 	public boolean deletePlugin(int pluginId)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		
 		return updateCall("deletePlugin", params, "Plugin-Id: " + pluginId);
 	}
 	
 	@Override
 	public boolean updatePluginData(int pluginId, String role, String externalServices, ServiceAlias[] serviceAliases, String description, String icon, String tags, Integer priority, ExternalAttribution[] attributions)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		params.put("role", role);
 		params.put("externalServices", externalServices);
 		params.put("serviceAliases", getObjectBlob(serviceAliases, "service aliases"));
 		params.put("description", description);
 		params.put("icon", icon);
 		params.put("tags", tags);
 		params.put("priority", priority);
 		params.put("attributions", getObjectBlob(attributions, "attributions"));
 		
 		return updateCall("updatePlugin", params, "Plugin-Id: " + pluginId);
 	}
 	
 	@Override
 	public Integer addScript(int pluginId, int orderId, String script, String type, String pageName, String scriptName)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		params.put("orderId", orderId);
 		params.put("script", script);
 		params.put("type", type);
 		params.put("pageName", pageName);
 		params.put("scriptName", scriptName);
 		
 		return queryForObject("addScript", params, "Plugin-Id: " + pluginId);
 	}
 	
 	@Override
 	public boolean deleteScript(int scriptId)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("scriptId", scriptId);
 		
 		return updateCall("deleteScript", params, "Script-Id: " + scriptId);
 	}
 	
 	@Override
 	public PluginScript[] getPluginScripts(int pluginId)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		
 		return queryForArray("getPluginScripts", params, "Plugin-Id: " + pluginId, PluginScript.class);
 	}
 	
 	@Override
 	public String getCompanyName(String companykey)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("companykey", companykey);
 		
 		return queryForObject("getCompanyName", params, "CompanyKey: " + companykey);
 	}
 
 	@Override
 	public Integer addMenuItem(int pluginId, String displayName, String icon, String reference, int defaultPriority) {
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		params.put("displayName", displayName);
 		params.put("icon", icon);
 		params.put("reference", reference);
 		params.put("defaultPriority", defaultPriority);
 		
 		return queryForObject("addMenuItem", params, "Plugin-Id: " + pluginId);
 	}
 	
 	@Override
 	public boolean deleteMenuItem(int menuItemId)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("menuItemId", menuItemId);
 		
 		return updateCall("deleteMenuItem", params, "MenuItem-Id: " + menuItemId);
 	}
 	
 	@Override
 	public MainMenuItem[] getPluginMenuItems(int pluginId) {
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		
		return queryForArray("getPageScripts", params, "Plugin-Id: " + pluginId, MainMenuItem.class);
 	}
 	
 	@Override
 	public Integer getPluginByCompanyNameVersionToken(String companyName, String pluginName, String version, String userToken) 
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("companyName", companyName);
 		params.put("pluginName", pluginName);
 		params.put("version", version);
 		params.put("userToken", userToken);
 		
 		return queryForObject("getPluginByCompanyNameVersionToken", params, "User Token: " + userToken);
 	}
 	
 	@Override
 	public Integer addDataCall(int pluginId, String page, String method, String uri, String content, String contentType, String pageVariable) {
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		params.put("page", page);
 		params.put("method", method);
 		params.put("uri", uri);
 		params.put("content", content);
 		params.put("contentType", contentType);
 		params.put("pageVariable", pageVariable);
 		
 		return queryForObject("addDataCall", params, "Plugin-Id: " + pluginId);
 	}
 	
 	@Override
 	public PluginDataCall[] getPluginDataCalls(int pluginId) {
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		
 		return queryForArray("getPluginDataCall", params, "Plugin-Id: " + pluginId, PluginDataCall.class);
 	}
 
 	@Override
 	public boolean deleteDataCall(int dataCallId)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("dataCallId", dataCallId);
 		
 		return updateCall("deleteDataCall", params, "DataCall-Id: " + dataCallId);
 	}
 	
 	@Override
 	public Integer addArt(int pluginId, String artPath, String artData, String contentType)
 	{
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		params.put("artPath", artPath);
 		params.put("artData", artData);
 		params.put("contentType", contentType);
 		
 		return queryForObject("addArt", params, "Plugin-Id: " + pluginId);
 	}
 
 	@Override
 	public PluginArt[] getPluginArt(int pluginId) {
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("pluginId", pluginId);
 		
 		return queryForArray("getPluginArt", params, "Plugin-Id: " + pluginId, PluginArt.class);
 	}
 
 	@Override
 	public boolean deleteArt(int artId) {
 		Map<String,Object> params = new HashMap<String,Object>();
 		params.put("artId", artId);
 		
 		return updateCall("deleteDataCall", params, "Art-Id: " + artId);
 	}
 	
 	private String getObjectBlob(Object obj, String objectTypeName)
 	{
 		if(obj == null)
 		{
 			return null;
 		}
 		
 		ObjectMapper mapper = new ObjectMapper();
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		try {
 			mapper.writeValue(stream, obj);
 		} catch (JsonGenerationException e) {
 			Logger.getLogger(this.getClass()).error("There was an error serializing the " + objectTypeName + ".");
 			Logger.getLogger(this.getClass()).debug(e);
 		} catch (JsonMappingException e) {
 			Logger.getLogger(this.getClass()).error("There was an error serializing the " + objectTypeName + ".");
 			Logger.getLogger(this.getClass()).debug(e);
 		} catch (IOException e) {
 			Logger.getLogger(this.getClass()).error("There was an error serializing the " + objectTypeName + ".");
 			Logger.getLogger(this.getClass()).debug(e);
 		}
 		return new String(stream.toByteArray());
 	}
 	
 	@SuppressWarnings("unchecked")
 	private <T> T queryForObject(String queryId, Object params, String userIdentifier)
 	{
 		T retval = null;
 		try {
 			retval = (T)super.getSqlMapClient().queryForObject(queryId, params);
 		} catch (SQLException e) {
 			final String warningFormat = "An exception occured while trying to %s for user %s"; 
 			logger.warn(String.format(warningFormat, queryId, userIdentifier));
 			logger.debug(e);
 		}
 		
 		return retval;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private <T> T[] queryForArray(String queryId, Object params, String userIdentifier, Class<T> resultClass)
 	{
 		List<T> retval = null;
 		try {
 			retval = (List<T>)super.getSqlMapClient().queryForList(queryId, params);
 		} catch (SQLException e) {
 			final String warningFormat = "An exception occured while trying to %s for user %s"; 
 			logger.warn(String.format(warningFormat, queryId, userIdentifier));
 			logger.debug(e);
 		}
 		
 		if(retval == null)
 		{
 			return null;
 		}
 
 		return retval.toArray((T[])java.lang.reflect.Array.newInstance(resultClass, retval.size()));
 	}
 	
 	private <T> boolean updateCall(String queryId, Map<String,Object> params, String userIdentifier)
 	{
 		boolean retval = false;
 		
 		int updateResult = 0;
 		try {
 			updateResult = super.getSqlMapClient().update(queryId, params);
 		} catch (SQLException e) {
 			logger.warn(String.format("An error occured while trying to %s for user %s", queryId, userIdentifier));
 			logger.debug(e);
 		}
 		if(updateResult > 0)
 		{
 			retval = true;
 		}
 		
 		return retval;
 	}
 }
