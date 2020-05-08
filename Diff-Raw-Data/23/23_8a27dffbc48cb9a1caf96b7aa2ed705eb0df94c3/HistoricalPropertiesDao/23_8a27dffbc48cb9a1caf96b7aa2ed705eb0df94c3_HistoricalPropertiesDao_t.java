 package org.bluemagic.config.service.dao;
 

 import org.bluemagic.config.api.service.CompletePropertyDetails;
 import org.bluemagic.config.api.service.PropertyDetails;
import java.lang.String;  	
 
 public interface HistoricalPropertiesDao {
 
	public boolean gethistoricalpropertiesById(int property_id);
 	
	public String insertHistoricalproperty(String key, String value);
 	
	public String getHistoricalPropertyValue(String historical_property_key);
 
     public PropertyDetails getPropertyDetails(String propertyWithTags);
 
     public CompletePropertyDetails getCompletePropertyDetails(String propertyWithTags);
 	
     // Delete method is omitted
 	
 }
