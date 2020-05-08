 package com.atlassian.sal.crowd.pluginsettings;
 
 import org.apache.log4j.Logger;
 import org.springframework.dao.DataAccessException;
 
 import com.atlassian.crowd.integration.exception.ObjectNotFoundException;
 import com.atlassian.crowd.model.salproperty.SALProperty;
 import com.atlassian.crowd.model.salproperty.SALPropertyDAO;
 import com.atlassian.sal.core.pluginsettings.AbstractStringPluginSettings;
 
 /**
  * Crowd implementation of PluginSettings interface. It stores properties using crowd's salPropertyDAO.
  * There is a problem with hibernate that can't store (AFAIK) null values for a primary key fields.
  * That's why we need to translate null <code>key</code> and null <code>propertyName</code> values to String value "null".
  *
  */
 public class CrowdPluginSettings extends AbstractStringPluginSettings
 {
 	private static final Logger log = Logger.getLogger(CrowdPluginSettings.class);
 
 	private static final String NULL_STRING = "null";
 
 	private final String key;
 	private final SALPropertyDAO salPropertyDAO;
 
 	public CrowdPluginSettings(String key, SALPropertyDAO salPropertyDAO)
 	{
 		this.key = key == null ? NULL_STRING : key;
 		this.salPropertyDAO = salPropertyDAO;
 	}
 
 	@Override
 	protected String getActual(String propertyName)
 	{
 		SALProperty salProperty;
 		try
 		{
 			salProperty = salPropertyDAO.find(key, propertyName == null ? NULL_STRING : propertyName);
 			return salProperty.getStringValue();
 		} catch (final DataAccessException e)
 		{
 			log.warn(e, e);
 		} catch (final ObjectNotFoundException e)
 		{
 			// return null
 		}
 		return null;
 	}
 
 	@Override
 	protected void putActual(String propertyName, String val)
 	{
        SALProperty salProperty = null;
 		final String notNullPropertyName = propertyName == null ? NULL_STRING : propertyName;
        try
        {
            salProperty = salPropertyDAO.find(key, propertyName == null ? NULL_STRING : propertyName);
        }
        catch (final DataAccessException e)
        {
            log.warn(e, e);
        }
        catch (final ObjectNotFoundException e)
        {
            // return null
        }
        if (salProperty == null)
        {
            salProperty = new SALProperty(key, notNullPropertyName, val);
            salPropertyDAO.saveOrUpdate(salProperty);
        }
        else
        {
            salProperty.setStringValue(val);
        }
 	}
 
 	@Override
 	protected Object removeActual(String propertyName)
 	{
 		final String notNullPropertyName = propertyName == null ? NULL_STRING : propertyName;
 		salPropertyDAO.remove(key, notNullPropertyName);
 		return propertyName;
 	}
 }
