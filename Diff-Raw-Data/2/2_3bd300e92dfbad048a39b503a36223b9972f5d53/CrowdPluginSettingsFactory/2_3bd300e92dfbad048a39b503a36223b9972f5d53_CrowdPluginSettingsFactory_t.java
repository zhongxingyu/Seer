 package com.atlassian.sal.crowd.pluginsettings;
 
 import com.atlassian.crowd.model.salproperty.SALPropertyDAO;
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
 
 public class CrowdPluginSettingsFactory implements PluginSettingsFactory
 {
 	private static final String GLOBAL_SETTINGS = "";
 
 	private SALPropertyDAO salPropertyDAO;
 	
 	public PluginSettings createGlobalSettings()
 	{
		return new CrowdPluginSettings(GLOBAL_SETTINGS, getSALPropertyDAO());
 	}
 
 	public PluginSettings createSettingsForKey(String key)
 	{
 		return new CrowdPluginSettings(key, getSALPropertyDAO());
 	}
 
 	public SALPropertyDAO getSALPropertyDAO()
 	{
 		if (salPropertyDAO==null)
 			salPropertyDAO = ComponentLocator.getComponent(SALPropertyDAO.class);
 		return salPropertyDAO;
 	}
 
 }
