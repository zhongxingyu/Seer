 package com.atlassian.sal.crowd.pluginsettings;
 
 import com.atlassian.crowd.model.salproperty.SALPropertyDAO;
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
 
 public class CrowdPluginSettingsFactory implements PluginSettingsFactory
 {
 	private SALPropertyDAO salPropertyDAO;
 	
 	public PluginSettings createGlobalSettings()
 	{
		return new CrowdPluginSettings(null, getSALPropertyDAO());
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
