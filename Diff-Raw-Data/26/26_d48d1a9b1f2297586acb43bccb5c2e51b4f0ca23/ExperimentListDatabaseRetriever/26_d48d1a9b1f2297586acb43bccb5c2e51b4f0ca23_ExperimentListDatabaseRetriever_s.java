 package uk.ac.ebi.arrayexpress.utils.db;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.sql.DataSource;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ExperimentListDatabaseRetriever extends SqlStatementExecutor
 {
     // logging facility
     private final Logger logger = LoggerFactory.getLogger(getClass());
     // sql to get a list of experiments from the database
     // (the parameter is either 0 for all experiments and 1 for public only)
     private final static String getExperimentListSql = "select distinct e.id" +
             " from tt_experiment e" +
            //"  where e.id in (348637031, 352682122, 366346722)" +
             " order by" +
             "  e.id asc";
 
     // experiment list
     private List<Long> experimentList;
 
     public ExperimentListDatabaseRetriever( DataSource ds )
     {
         super(ds, getExperimentListSql);
         experimentList = new ArrayList<Long>();
     }
 
     public List<Long> getExperimentList()
     {
         if (!execute(false)) {
             logger.error("There was a problem retrieving the list of experiments, check log for errors or exceptions");
         }
         return experimentList;
     }
 
     protected void setParameters( PreparedStatement stmt ) throws SQLException
     {
     }
 
     protected void processResultSet( ResultSet resultSet ) throws SQLException
     {
         while ( resultSet.next() ) {
             experimentList.add(resultSet.getLong(1));
         }
     }
 }
 
