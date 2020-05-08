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
 import thredds.catalog.SpatialRangeType;
 import thredds.catalog.ThreddsMetadata.Range;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class SpatialRangeSpec  extends AbstractNcetlSpec {
     private static final long serialVersionUID = 1L;
     
     private static final String TABLE_NAME = "spatial_range";
     public static final String GEOSPATIAL_COVERAGE_ID = "geospatial_coverage_id";
     public static final String SPATIAL_RANGE_TYPE_ID = "spatial_range_type_id";
     public static final String START = "start";
     public static final String SIZE = "size";
     public static final String RESOLUTION = "resolution";
     public static final String UNITS = "units";
 
     @Override
     public String setupTableName() {
         return TABLE_NAME;
     }
     
     @Override
     public ColumnMapping[] setupColumnMap() {
         return new ColumnMapping[] {
                     new ColumnMapping(ID, ID),
                     new ColumnMapping(GEOSPATIAL_COVERAGE_ID, GEOSPATIAL_COVERAGE_ID),
                     new ColumnMapping(SPATIAL_RANGE_TYPE_ID, SPATIAL_RANGE_TYPE_ID),
                     new ColumnMapping(START, START),
                     new ColumnMapping(SIZE, SIZE),
                     new ColumnMapping(RESOLUTION, RESOLUTION),
                     new ColumnMapping(UNITS, UNITS),
                     new ColumnMapping(INSERTED, null),
                     new ColumnMapping(UPDATED, null)
                 };
     }
 
     @Override
     public SearchMapping[] setupSearchMap() {
         return new SearchMapping[] {
             new SearchMapping(ID, ID, null, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + GEOSPATIAL_COVERAGE_ID, GEOSPATIAL_COVERAGE_ID, GEOSPATIAL_COVERAGE_ID, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + SPATIAL_RANGE_TYPE_ID, SPATIAL_RANGE_TYPE_ID, SPATIAL_RANGE_TYPE_ID, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + START, START, START, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + SIZE, SIZE, SIZE, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + RESOLUTION, RESOLUTION, RESOLUTION, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + UNITS, UNITS, UNITS, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + INSERTED, INSERTED, INSERTED, WhereClauseType.equals, null, null, null),
             new SearchMapping("s_" + UPDATED, UPDATED, UPDATED, WhereClauseType.equals, null, null, null)
         };
     }
 
     static Map<SpatialRangeType, Range> unmarshal(int id, Connection con) throws SQLException {
         Map<SpatialRangeType, Range> ranges = Maps.newTreeMap();
         Spec spec = new SpatialRangeSpec();
         Map<String, String[]> params = Maps.newHashMap();
         params.put("s_" + GEOSPATIAL_COVERAGE_ID, new String[] { "" + id });
         Spec.loadParameters(spec, params);
         ResultSet rs = Spec.getResultSet(spec, con);
         
         while (rs.next()) {
             double start = rs.getDouble(START);
             double size = rs.getDouble(SIZE);
             double resolution = rs.getDouble(RESOLUTION);
             String units = rs.getString(UNITS);
             Range range = new Range(start, size, resolution, units);
             
             int type_id = rs.getInt(SPATIAL_RANGE_TYPE_ID);
             SpatialRangeType type = SpatialRangeTypeSpec.unmarshal(type_id, con);
             ranges.put(type, range);
         }
         return ranges;
     }
     
 }
