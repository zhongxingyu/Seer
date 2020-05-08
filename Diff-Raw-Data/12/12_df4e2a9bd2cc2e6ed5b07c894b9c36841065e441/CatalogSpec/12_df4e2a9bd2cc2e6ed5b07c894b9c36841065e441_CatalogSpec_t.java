 package gov.usgs.cida.ncetl.spec;
 
 import com.google.common.collect.Maps;
 import gov.usgs.webservices.jdbc.routing.ActionType;
 import gov.usgs.webservices.jdbc.spec.Spec;
 import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
 import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
 import gov.usgs.webservices.jdbc.spec.validator.ValidationFailEvent;
 import gov.usgs.webservices.jdbc.spec.validator.Validator;
 import java.net.URI;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.naming.NamingException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import thredds.catalog.CatalogHelper;
 import thredds.catalog.InvCatalog;
 import thredds.catalog.InvCatalogModifier;
 import thredds.catalog.InvService;
 import ucar.nc2.units.DateType;
 
 /**
  *
  * @author Ivan Suftin <isuftin@usgs.gov>
  */
 public class CatalogSpec extends AbstractNcetlSpec {
     
     private static final Logger LOG = LoggerFactory.getLogger(CatalogSpec.class);
     private static final long serialVersionUID = 1L;
     private static final String TABLE_NAME = "catalog";
     public static final String LOCATION = "location";
     public static final String NAME = "name";
     public static final String EXPIRES = "expires";
     public static final String VERSION = "version";
     
     @Override
     public ColumnMapping[] setupColumnMap() {
         return new ColumnMapping[] {
                     new ColumnMapping(ID, ID),
                     new ColumnMapping(LOCATION, LOCATION),
                     new ColumnMapping(NAME, NAME),
                     new ColumnMapping(EXPIRES, EXPIRES),
                     new ColumnMapping(VERSION, VERSION),
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
                     new SearchMapping(ID, ID, null, WhereClauseType.equals, null,
                                       null, null),
                     new SearchMapping("s_" + LOCATION, LOCATION, LOCATION,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + NAME, NAME, NAME,
                                       WhereClauseType.equals, null, null, null,
                                       new Validator(ActionType.create, "CREATE") {
                 
                 @Override
                 public ValidationFailEvent validate(String field, String value,
                                                     Spec spec,
                                                     Connection con) {
                     URI catURI = CatalogHelper.setupDatasetDirectory(value);
                     
                     Map<String, String[]> params = Maps.newHashMap();
                     params.put(CatalogSpec.LOCATION,
                                new String[] { catURI.toString() });
                     Spec.loadParameters(spec, params);
//                    try {
//                        Spec.insertRow(spec, con);
//                    }
//                    catch (SQLException ex) {
//                        LOG.debug("update failed: " + ex);
//                    }
                     return null;
                 }
             }),
                     new SearchMapping("s_" + EXPIRES, EXPIRES, EXPIRES,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + VERSION, VERSION, VERSION,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + INSERTED, INSERTED, INSERTED,
                                       WhereClauseType.equals, null, null, null),
                     new SearchMapping("s_" + UPDATED, UPDATED, UPDATED,
                                       WhereClauseType.equals, null, null, null)
                 };
     }
     
     public static InvCatalog unmarshal(URI location, Connection con)
             throws SQLException, NamingException, ClassNotFoundException,
                    ParseException {
         InvCatalog cat = CatalogHelper.readCatalog(location);
         InvCatalogModifier modifyCat = new InvCatalogModifier(cat);
         Spec spec = new CatalogSpec();
         Map<String, String[]> params = Maps.newHashMap();
         params.put("s_location", new String[] { location.toString() });
         Spec.loadParameters(spec, params);
         ResultSet result = Spec.getResultSet(spec, con);
 
 //        ResultSetMetaData metaData = result.getMetaData();
         int catalog_id = -1;
         if (result.next()) {
             catalog_id = result.getInt(ID);
             String name = result.getString(NAME);
             modifyCat.setName(name);
             
             Date date = result.getDate(EXPIRES);
             modifyCat.setExpires(new DateType(false, date));
             
             String version = result.getString(VERSION);
             modifyCat.setVersion(version);
         }
         
         List<InvService> services = ServiceSpec.unmarshal(catalog_id, con);
         modifyCat.setServices(services);
 
         // DatasetSpec.unmarshal is the only one which edits in place
         DatasetSpec.unmarshal(catalog_id, cat, con);
         
         return cat;
     }
     
     /**
      * index.jsp lists "Datasets" but they are really Catalogs
      */
     public static String lookupName(int id, Connection con) throws SQLException {
         Spec spec = new CatalogSpec();
         Map<String, String[]> params = new HashMap<String, String[]>(1);
         params.put(ID, new String[] { "" + id });
         Spec.loadParameters(spec, params);
         ResultSet rs = Spec.getResultSet(spec, con);
 
         String name = "unknown";
         if (rs.next()) {
             name = rs.getString(NAME);
         }
         return name;
     }
 }
