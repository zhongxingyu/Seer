 package org.rhq.plugins.sqlserver;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.rhq.core.domain.measurement.*;
 import org.rhq.core.pluginapi.availability.AvailabilityFacet;
 import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
 import org.rhq.core.pluginapi.inventory.ResourceComponent;
 import org.rhq.core.pluginapi.inventory.ResourceContext;
 import org.rhq.core.pluginapi.measurement.MeasurementFacet;
 import org.rhq.plugins.database.DatabaseComponent;
 import org.rhq.plugins.database.DatabaseQueryUtility;
 
 public class MSSQLDatabaseComponent<T extends ResourceComponent<?>> implements DatabaseComponent<MSSQLServerComponent<?>>, AvailabilityFacet, MeasurementFacet {
 
     private static Log log = LogFactory.getLog(MSSQLDatabaseComponent.class);

    private ResourceContext<MSSQLServerComponent<?>> context;

 	private MSSQLServerComponent<?> serverComponent;
 	private String databaseId;
     private String databaseName;
 	private static String STATUS_COLUMN = "state_desc";
 	private static String AVAILABLE_STATUS = "ONLINE";
 
     private static String STATUS_QUERY = "SELECT state_desc FROM sys.databases WHERE database_id = ?";
     private static String TRAIT_QUERY = "SELECT d.create_date, d.compatibility_level, d.collation_name, d.state_desc, d.recovery_model_desc FROM sys.databases AS d WHERE d.database_id = ?";
     private static String METRIC_QUERY = "SELECT SUM(m.size) * 8 AS size FROM sys.master_files AS m WHERE m.database_id = ?";
 
     @Override
 	public void start(ResourceContext<MSSQLServerComponent<?>> context) throws InvalidPluginConfigurationException {
 		serverComponent = context.getParentResourceComponent();
 		databaseId = context.getResourceKey();
        this.context = context;
 	}
 

 	@Override
 	public void stop() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public AvailabilityType getAvailability() {
 
 		AvailabilityType result = AvailabilityType.DOWN;
 		
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 
 		try {
 			Connection conn = getConnection();
 			statement = conn.prepareStatement(STATUS_QUERY);
 			statement.setString(1, databaseId);
 			resultSet = statement.executeQuery();
 
 			if(resultSet.next()) {
 				String status = resultSet.getString(STATUS_COLUMN);
 				if(status.equals(AVAILABLE_STATUS)) {
 					result = AvailabilityType.UP;
 				}
 			}
 		} catch (SQLException e) {
 			log.error("Received SQLException while executing status, ", e);
 		} finally {
 			DatabaseQueryUtility.close(statement, resultSet);
 		}                                                                                     /**/
 		
 		return result;
 	}
 
     public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) {
 
         Map<String, Double> numericResults = null;
         Map<String, Object> traitResults = null;
 
         try {
             for (MeasurementScheduleRequest request : metrics) {
 
                 String property = request.getName();
 
                 switch(request.getDataType()) {
                     case TRAIT:
                         if(traitResults == null) {
                             traitResults = DatabaseQueryUtility.getGridValues(this, TRAIT_QUERY).get(0);
                         }
                         report.addData(new MeasurementDataTrait(request, (String) traitResults.get(property)));
                         break;
                     case MEASUREMENT:
                         if(numericResults == null) {
                             numericResults = DatabaseQueryUtility.getNumericQueryValues(this, METRIC_QUERY);
                         }
                         report.addData(new MeasurementDataNumeric(request, numericResults.get(property)));
                         break;
                     default:
                         // Not supported here
                         break;
                 }
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
 	public String getDatabaseName() {
        return context.getPluginConfiguration().getSimpleValue("databaseName");
 	}
 
     @Override
     public Connection getConnection() {
         Connection conn = serverComponent.getConnection();
         try {
             conn.setCatalog(getDatabaseName());
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
         return conn;
     }
 
     @Override
     public void removeConnection() {
     	serverComponent.removeConnection();
     }
 
 }
