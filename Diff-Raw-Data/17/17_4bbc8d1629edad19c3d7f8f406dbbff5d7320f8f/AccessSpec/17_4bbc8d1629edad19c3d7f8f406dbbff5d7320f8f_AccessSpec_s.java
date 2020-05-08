 package gov.usgs.cida.ncetl.spec;
 
import com.google.common.collect.Maps;
import gov.usgs.webservices.jdbc.spec.Spec;
 import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
 import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Map;
 import thredds.catalog.InvAccess;
 
 /**
  * 
  *
  * @author Ivan Suftin <isuftin@usgs.gov>
  */
 public class AccessSpec extends AbstractNcetlSpec {
     private static final long serialVersionUID = 1L;
 
     private static final String TABLE_NAME = "access";
     public static final String DATASET_ID = "dataset_id";
     public static final String SERVICE_ID = "service_id";
     public static final String DATAFORMAT_ID = "dataformat_id";
     public static final String URL_PATH = "url_path";
 
     @Override
     public ColumnMapping[] setupColumnMap() {
         return new ColumnMapping[] {
                     new ColumnMapping(ID, ID),
                     new ColumnMapping(DATASET_ID, DATASET_ID),
                     new ColumnMapping(SERVICE_ID, SERVICE_ID),
                     new ColumnMapping(DATAFORMAT_ID, DATAFORMAT_ID),
                     new ColumnMapping(URL_PATH, URL_PATH),
                     new ColumnMapping(INSERTED, null),
                     new ColumnMapping(UPDATED, null)
                 };
     }
 
     @Override
     public String setupTableName() {
         return TABLE_NAME;
     }
     
     @Override
     public SearchMapping[] setupSearchMap() {
         return new SearchMapping[] {
             new SearchMapping(ID, ID, null, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + DATASET_ID, DATASET_ID, DATASET_ID, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + SERVICE_ID, SERVICE_ID, SERVICE_ID, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + DATAFORMAT_ID, DATAFORMAT_ID, DATAFORMAT_ID, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + URL_PATH, URL_PATH, URL_PATH, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + INSERTED, INSERTED, INSERTED, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + UPDATED, UPDATED, UPDATED, WhereClauseType.equals, null, null, null)
         };
     }
     
     public static InvAccess unmarshal(int datasetId, Connection con) {
//        Spec spec = new AccessSpec();
//        Map<String, String[]> params = Maps.newHashMap();
//        params.put("s_" + DATASET_ID, new String[] { "" + id });
//        Spec.loadParameters(spec, params);
//        ResultSet rs = Spec.getResultSet(spec, con);
//        
//        if (rs.next())
        
        
        // Leaving this unimplemented for now, it's complicated
        // may need to do this after all.
         return null;
     }
 }
