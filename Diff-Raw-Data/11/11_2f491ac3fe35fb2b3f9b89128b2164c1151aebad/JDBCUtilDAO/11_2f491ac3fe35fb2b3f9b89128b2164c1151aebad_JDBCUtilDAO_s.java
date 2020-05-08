 package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;
 import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
 import gov.nih.nci.ncicb.cadsr.common.persistence.dao.UtilDAO;
 import gov.nih.nci.ncicb.cadsr.common.servicelocator.ServiceLocator;
 import gov.nih.nci.ncicb.cadsr.common.servicelocator.SimpleServiceLocator;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Locale;
 import java.util.Properties;
 import org.springframework.jdbc.core.SqlParameter;
 import org.springframework.jdbc.object.MappingSqlQuery;
 
 public class JDBCUtilDAO extends JDBCBaseDAO implements UtilDAO
 {
   public JDBCUtilDAO(ServiceLocator locator)
   {
     super(locator);
   }
   
     
     /**
    * Utility method to get the Application properties
    *
    * @param <b>Application Name<b> corresponds to the target record whose 
    *        display order is to be updated
    * @param <b>Locale<b> of the user
    *
    * @return <b>properties</b> containing application properties
    *
    * @throws <b>DMLException</b>
    */
    public Properties getApplicationProperties(String applicationName, String locale) throws DMLException 
    {
      ApplicationPropertiesQuery query = new ApplicationPropertiesQuery();
      try{
      query.setDataSource(getDataSource());
      query.setSql(applicationName,locale);
      query.execute();
      }
      catch(Exception exp)
      {
        throw new DMLException("Could not retreive application properties",exp);
      }
      return query.getProperties();
    }  
    
    /**
     * Utility method to get the Application properties
     *
     * @param <b>Application Name<b> corresponds to the target record whose 
     *        display order is to be updated
     * @param <b>Locale<b> of the user
     *
     * @param <b>property<b> specific property to search for
     *
     * @return <b>properties</b> containing application properties
     *
     * @throws <b>DMLException</b>
     */
     public Properties getApplicationURLProperties(String locale) throws DMLException 
     {
       ApplicationPropertiesQuery query = new ApplicationPropertiesQuery();
       try{
       query.setDataSource(getDataSource());
       query.setSqlURL(locale);
       query.execute();
       }
       catch(Exception exp)
       {
         throw new DMLException("Could not retreive application properties",exp);
       }
       return query.getProperties();
     }  
     
 
   /**
    * Inner class to get Properties
    */
   private class ApplicationPropertiesQuery extends MappingSqlQuery {
     private Properties properties =  new Properties();
     ApplicationPropertiesQuery() {
       super();
     }
 
    public Properties getProperties()
    {
      return properties;
    }
     public void setSql(String toolName,String locale) {
       super.setSql("select PROPERTY, VALUE from TOOL_OPTIONS_VIEW_EXT " +
         " where tool_name = '"+toolName +"' and locale = '" +locale +"'");
     }
 
     public void setSqlURL(String locale) {
         super.setSql("select TOOL_NAME || '_URL', VALUE from TOOL_OPTIONS_VIEW_EXT " +
          " where property = 'URL' and (locale = '" +locale +"' or locale = '')");
       }
 
     protected Object mapRow(
       ResultSet rs,
       int rownum) throws SQLException {
       String key = rs.getString(1);
       String value = rs.getString(2);
       properties.put(key,value);
       return null;
     }
   }   
   
     public static void main(String[] args) {
     ServiceLocator locator = new SimpleServiceLocator();
     JDBCDAOFactory factory =
       (JDBCDAOFactory) new JDBCDAOFactory().getDAOFactory(locator);
       UtilDAO dao = factory.getUtilDAO();
       System.out.println(dao.getApplicationProperties("CDEBrowser","US"));
     
     }
 }
