 package ro.finsiel.eunis.jrfTables.species.references;
 
 
 /**
  * Date: Jul 15, 2003
  * Time: 11:14:50 AM
  */
 
 import net.sf.jrf.column.columnoptions.NullableColumnOption;
 import net.sf.jrf.column.columnspecs.DateColumnSpec;
 import net.sf.jrf.column.columnspecs.IntegerColumnSpec;
 import net.sf.jrf.column.columnspecs.StringColumnSpec;
 import net.sf.jrf.domain.AbstractDomain;
 import net.sf.jrf.domain.PersistentObject;
 
 
 /**
  *
  * @version $Revision$ $Date$
  **/
 public class ReferencesJoinDomain extends AbstractDomain {
 
     /**
      **/
     public PersistentObject newPersistentObject() {
         return new ReferencesJoinPersist();
     }
 
     /**
      **/
     public void setup() {
         // These setters could be used to override the default.
         // this.setDatabasePolicy(new null());
         // this.setJDBCHelper(JDBCHelperFactory.create());
 
         this.setTableName("DC_INDEX");
         this.setReadOnly(true);
 
         this.addColumnSpec(new IntegerColumnSpec("ID_DC", "getIdDc", "setIdDc", DEFAULT_TO_ZERO, NATURAL_PRIMARY_KEY));
         this.addColumnSpec(new StringColumnSpec("COMMENT", "getComment", "setComment", DEFAULT_TO_NULL));
         this.addColumnSpec(new StringColumnSpec("SOURCE", "getSource", "setSource", DEFAULT_TO_NULL));
         this.addColumnSpec(new StringColumnSpec("EDITOR", "getEditor", "setEditor", DEFAULT_TO_NULL));
         this.addColumnSpec(new StringColumnSpec("URL", "getUrl", "setUrl", DEFAULT_TO_NULL));
        this.addColumnSpec(new DateColumnSpec("CREATED", new NullableColumnOption(), "getCreated", "setCreated", DEFAULT_TO_NULL));
         this.addColumnSpec(new StringColumnSpec("TITLE", "getTitle", "setTitle", DEFAULT_TO_NULL));
         this.addColumnSpec(new StringColumnSpec("PUBLISHER", "getPublisher", "setPublisher", DEFAULT_TO_NULL));
 
     }
 
 }
