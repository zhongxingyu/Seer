 package ro.finsiel.eunis.jrfTables;
 
 
 import net.sf.jrf.column.columnspecs.StringColumnSpec;
 import net.sf.jrf.domain.AbstractDomain;
 import net.sf.jrf.domain.PersistentObject;
 
 
 /**
  * JRF table for CHM62EDT_REGION_CODES.
  * @author finsiel
  **/
 public class Chm62edtRegionCodesDomain extends AbstractDomain {
 
     /**
      * Implements newPersistentObject from AbstractDomain.
      * @return New persistent object (table row).
      */
     public PersistentObject newPersistentObject() {
         return new Chm62edtRegionCodesPersist();
     }
 
     /**
      * Implements setup from AbstractDomain.
      */
     public void setup() {
         // These setters could be used to override the default.
         // this.setDatabasePolicy(new null());
         // this.setJDBCHelper(JDBCHelperFactory.create());
         this.setTableName("CHM62EDT_REGION_CODES");
         this.setReadOnly(true);
 
         this.addColumnSpec(
                 new StringColumnSpec("ID_REGION_CODE", "getIdRegionCode",
                "setIdRegionCode", DEFAULT_TO_NULL));
         this.addColumnSpec(
                 new StringColumnSpec("NAME", "getName", "setName",
                 DEFAULT_TO_NULL));
         this.addColumnSpec(
                 new StringColumnSpec("DESCRIPTION", "getDescription",
                 "setDescription", DEFAULT_TO_NULL));
     }
 }
