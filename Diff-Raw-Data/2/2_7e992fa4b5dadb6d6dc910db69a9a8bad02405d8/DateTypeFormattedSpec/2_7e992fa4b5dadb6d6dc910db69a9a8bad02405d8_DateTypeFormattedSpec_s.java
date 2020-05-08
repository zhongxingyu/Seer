 package gov.usgs.cida.ncetl.spec;
 
 import com.google.common.collect.Maps;
 import gov.usgs.webservices.jdbc.spec.Spec;
 import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.Map;
 import ucar.nc2.units.DateType;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class DateTypeFormattedSpec extends AbstractNcetlSpec {
 
     private static final long serialVersionUID = 1L;
     private static final String TABLE_NAME = "date_type_formatted";
     public static final String FORMAT = "format";
     public static final String VALUE = "value";
     public static final String DATE_TYPE_ENUM_ID = "date_type_enum_id";
 
     @Override
     public String setupTableName() {
         return TABLE_NAME;
     }
     
     @Override
     public ColumnMapping[] setupColumnMap() {
         return new ColumnMapping[] {
                     new ColumnMapping(ID, ID),
                     new ColumnMapping(FORMAT, FORMAT),
                     new ColumnMapping(VALUE, VALUE),
                     new ColumnMapping(DATE_TYPE_ENUM_ID, DATE_TYPE_ENUM_ID),
                     new ColumnMapping(INSERTED, null),
                     new ColumnMapping(UPDATED, null)
                 };
     }
 
     @Override
     public SearchMapping[] setupSearchMap() {
         return new SearchMapping[] {
                     new SearchMapping(ID, ID, null, WhereClauseType.equals, null,
                                       null, null),
                     new SearchMapping("s_" + FORMAT, FORMAT, FORMAT,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + VALUE, VALUE,
                                       VALUE, WhereClauseType.equals, null,
                                       null, null),
                     new SearchMapping("s_" + DATE_TYPE_ENUM_ID, DATE_TYPE_ENUM_ID,
                                       DATE_TYPE_ENUM_ID, WhereClauseType.equals,
                                       null, null, null),
                     new SearchMapping("s_" + INSERTED, INSERTED, INSERTED,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + UPDATED, UPDATED, UPDATED,
                                       WhereClauseType.equals, null, null, null)
                 };
     }
     
     
     public static DateType lookup(int id, Connection con) throws SQLException, ParseException {
         Spec spec = new DateTypeFormattedSpec();
         Map<String, String[]> params = Maps.newHashMap();
        params.put("s_" + ID, new String[] { "" + id });
         Spec.loadParameters(spec, params);
         ResultSet rs = Spec.getResultSet(spec, con);
         DateType dt = null;
         if (rs.next()) {
             String format = rs.getString(FORMAT);
             String value = rs.getString(VALUE);
             String type = DateTypeEnumSpec.lookup(rs.getInt(DATE_TYPE_ENUM_ID), con);
             
             dt = new DateType(value, format, type);
         }
         return dt;
     }
 
 }
