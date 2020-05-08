 package org.bluemagic.config.service.dao;
 
 import org.bluemagic.config.api.service.CompletePropertyDetails;
 import org.bluemagic.config.api.service.PropertyDetails;

 
 public interface HistoricalPropertiesDao {
 
 	public String gethistoricalpropertiesById(int property_id);
 	
 	public boolean insertHistoricalproperty(String key, String value);
 	
 	public String getHistoricalPropertyValue(String HistoricalpropertyKey);
 
     public PropertyDetails getPropertyDetails(String propertyWithTags);
 
     public CompletePropertyDetails getCompletePropertyDetails(String propertyWithTags);
 	
     // Delete method is omitted
 	
 }
