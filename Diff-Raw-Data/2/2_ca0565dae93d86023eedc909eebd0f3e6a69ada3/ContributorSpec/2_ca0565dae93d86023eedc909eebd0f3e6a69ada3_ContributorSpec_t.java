 package gov.usgs.cida.ncetl.spec;
 
 import com.google.common.collect.Maps;
 import gov.usgs.webservices.jdbc.spec.Spec;
 import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map;
 import thredds.catalog.ThreddsMetadata.Contributor;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class ContributorSpec extends AbstractNcetlSpec {
     private static final long serialVersionUID = 1L;
     private static final String TABLE_NAME = "contributor";
     public static final String ROLE = "role";
     public static final String TEXT = "text";
 
 
     @Override
     public String setupTableName() {
         return TABLE_NAME;
     }
     
     @Override
     public ColumnMapping[] setupColumnMap() {
         return new ColumnMapping[] {
                     new ColumnMapping(ID, ID),
                     new ColumnMapping(ROLE, ROLE),
                     new ColumnMapping(TEXT, TEXT),
                     new ColumnMapping(INSERTED, null),
                     new ColumnMapping(UPDATED, null)
                 };
     }
 
     @Override
     public SearchMapping[] setupSearchMap() {
         return new SearchMapping[] {
             new SearchMapping(ID, ID, null, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + ROLE, ROLE, ROLE, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + TEXT, TEXT, TEXT, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + INSERTED, INSERTED, INSERTED, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + UPDATED, UPDATED, UPDATED, WhereClauseType.equals, null, null, null)
         };
     }
     
     public static Contributor lookup(int id, Connection con) throws SQLException {
         Spec spec = new ContributorSpec();
         Map<String, String[]> params = Maps.newHashMap();
        params.put(ID, new String[] { "" + id });
         Spec.loadParameters(spec, params);
         ResultSet rs = Spec.getResultSet(spec, con);
 
         if (rs.next()) {
             String name = rs.getString(TEXT);
             String role = rs.getString(ROLE);
             return new Contributor(name, role);
         }
         else {
             return null;
         }
     }
 
 }
