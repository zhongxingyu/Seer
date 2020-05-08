 package gov.usgs.cida.ncetl.spec;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import gov.usgs.webservices.jdbc.spec.Spec;
 import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 import thredds.catalog.ThreddsMetadata.Source;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class CreatorJoinSpec  extends AbstractNcetlSpec {
     private static final long serialVersionUID = 1L;
     private static final String TABLE_NAME = "creator_join";
     public static final String DATASET_ID = "dataset_id";
     public static final String CREATOR_ID = "creator_id";
 
 
     @Override
     public String setupTableName() {
         return TABLE_NAME;
     }
     
     @Override
     public ColumnMapping[] setupColumnMap() {
         return new ColumnMapping[] {
                     new ColumnMapping(DATASET_ID, DATASET_ID),
                     new ColumnMapping(CREATOR_ID, CREATOR_ID),
                     new ColumnMapping(INSERTED, null),
                     new ColumnMapping(UPDATED, null)
                 };
     }
 
     @Override
     public SearchMapping[] setupSearchMap() {
         return new SearchMapping[] {
             new SearchMapping("s_" + DATASET_ID, DATASET_ID, DATASET_ID, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + CREATOR_ID, CREATOR_ID, CREATOR_ID, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + INSERTED, INSERTED, INSERTED, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + UPDATED, UPDATED, UPDATED, WhereClauseType.equals, null, null, null)
         };
     }
     
     public static List<Source> unmarshal(int datasetId, Connection con) throws SQLException {
         List<Source> result = Lists.newLinkedList();
        Spec spec = new CreatorJoinSpec();
         Map<String, String[]> params = Maps.newHashMap();
         params.put("s_" + DATASET_ID, new String[] { "" + datasetId });
         Spec.loadParameters(spec, params);
         ResultSet rs = Spec.getResultSet(spec, con);
 
         while (rs.next()) {
             int creator_id = rs.getInt(CREATOR_ID);
             result.add(CreatorSpec.unmarshal(creator_id, con));
         }
         return result;
     }
 
 }
