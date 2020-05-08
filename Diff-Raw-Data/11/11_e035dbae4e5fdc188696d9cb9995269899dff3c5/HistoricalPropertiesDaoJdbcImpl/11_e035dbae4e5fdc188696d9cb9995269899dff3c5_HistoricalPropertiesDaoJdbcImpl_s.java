 package org.bluemagic.config.service.dao.impl;
 
 import org.bluemagic.config.api.service.CompletePropertyDetails;
 import org.bluemagic.config.api.service.PropertyDetails;
 import org.bluemagic.config.service.dao.HistoricalPropertiesDao;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.support.JdbcDaoSupport;
 
 public class HistoricalPropertiesDaoJdbcImpl extends JdbcDaoSupport implements HistoricalPropertiesDao {
 
 	
 	private static final String INSERT_HISTORICAL_PROPERTIES_VALUE = "INSERT INTO HISTORICAL PROPERTIES (KEY, VALUE) VALUES (?,?)";
 	
 	private static final String SELECT_HISTORICAL_PROPERTIES_BY_ID = "SELECT PROPERTY FROM HISTORICAL PROPERTIES WHERE ID=?";
 	
         
 	
 	@Override
 	public int gethistoricalpropertiesById(int property_id) {
 		
 		try {
 			
 			// Try to select the property from the table.  If the property is not found, the result will be null.
			return getJdbcTemplate().queryForObject(SELECT_ID_FOR_PROPERTY, new Object[] { propertyid }, Integer.class);
 		} catch (EmptyResultDataAccessException erdae) {
 			
 			// Means the property did not exist.
 			return -1;
 		}
 	}
 	
 	
 	@Override
 	public boolean insertHistoricalProperty(String HistoricalpropertyKey, String HistoricalpropertyValue) {
 	    
	    int rowsUpdated = getJdbcTemplate().update(INSERT_PROPERTY_VALUE, propertyKey, propertyValue);
 	    if (rowsUpdated == 1) {
 		return true;
 	    } else {
 		return false;
 	    }
 	}
 
 	@Override
	public String getPropertyValue(String propertyKey){
 
 		try{
 			// Try to select the propertyKey from the table. If the propertyKey is not found, the result will be null.
			return getJdbcTemplate().queryForObject(SELECT_PROPERTY_VALUE, new Object[] { propertyKey }, String.class);
 		} catch (Throwable t) {
 
 			// Means the propertyKey did not exist.
 			return null;
 		}
 	}
 
 	@Override
 	public PropertyDetails getPropertyDetails(String propertyWithTags) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public CompletePropertyDetails getCompletePropertyDetails(String propertyWithTags) {
 		throw new UnsupportedOperationException();
 	}
 }
