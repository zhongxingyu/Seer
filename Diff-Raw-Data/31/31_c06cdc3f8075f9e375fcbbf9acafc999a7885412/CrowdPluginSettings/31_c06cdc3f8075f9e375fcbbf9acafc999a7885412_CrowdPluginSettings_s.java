 package com.atlassian.sal.crowd.pluginsettings;
 
 import org.apache.log4j.Logger;
 import org.springframework.dao.DataAccessException;
 
 import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
 import com.atlassian.crowd.model.salproperty.SALProperty;
 import com.atlassian.crowd.model.salproperty.SALPropertyDAO;
 import com.atlassian.sal.core.pluginsettings.AbstractStringPluginSettings;
 
 public class CrowdPluginSettings extends AbstractStringPluginSettings
 {
 	private static final Logger log = Logger.getLogger(CrowdPluginSettings.class);
	
 	private final String key;
 	private final SALPropertyDAO salPropertyDAO;
 
 	public CrowdPluginSettings(String key, SALPropertyDAO salPropertyDAO)
 	{
		this.key = key;
 		this.salPropertyDAO = salPropertyDAO;
 	}
 
 	@Override
 	protected String getActual(String propertyName)
 	{
 		SALProperty salProperty;
 		try
 		{
			salProperty = salPropertyDAO.find(key, propertyName);
 			return salProperty.getStringValue();
 		} catch (final DataAccessException e)
 		{
			log.warn(e,e);
 		} catch (final ObjectNotFoundException e)
 		{
 			// return null
 		}
 		return null;
 	}
 
 	@Override
 	protected void putActual(String propertyName, String val)
 	{
		salPropertyDAO.saveOrUpdate(new SALProperty(key, propertyName, val));
 	}
 
 	@Override
 	protected Object removeActual(String propertyName)
 	{
		salPropertyDAO.remove(key, propertyName);
 		return propertyName;
 	}
 }
