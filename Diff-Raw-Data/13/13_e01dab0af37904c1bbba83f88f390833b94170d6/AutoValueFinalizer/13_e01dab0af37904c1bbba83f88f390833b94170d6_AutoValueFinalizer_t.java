 /*
  * JDBCImportExecutor.java
  *
  * Created on 28. Oktober 2003, 11:40
  */
 package de.cismet.jpresso.core.finalizer;
 
 import de.cismet.jpresso.core.finalizer.autovalue.AutoValueStrategy;
 import de.cismet.jpresso.core.kernel.FieldDescription;
 import de.cismet.jpresso.core.serviceprovider.exceptions.JPressoException;
 import de.cismet.jpresso.core.kernel.Finalizer;
 import de.cismet.jpresso.core.kernel.ImportMetaInfo;
 import de.cismet.jpresso.core.kernel.IntermedTable;
 import de.cismet.jpresso.core.serviceprovider.exceptions.FinalizerException;
 import de.cismet.jpresso.core.utils.TypeSafeCollections;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 
 /**
  * @author  srichter
  */
 public final class AutoValueFinalizer extends Finalizer {
 
     /** Logger */
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private boolean debug = log.isDebugEnabled();
     public static final int MAX_LOG_ERROR = 20;
     /** Holds value of property param. */
     private final StringBuilder buff = new StringBuilder();
     private boolean rb = true;
     private boolean force = false;
     //fields to update in a currently inserting table
     private final Map<String, List<Integer>> ownFieldPositions = TypeSafeCollections.newHashMap();
     //fields to update in the master tables of a just inserted table
     private final Map<String, Map<FieldDescription, Integer>> masterFieldPositions = TypeSafeCollections.newHashMap();
     private static final String AUTOVAL_PROPERTY_PREFIX = "AVS.";
     private String configuration = "";
 //    private String[] tabStrings = null;
 
     /**
      *
      * @param param
      * @return
      */
     private boolean evalString(final String param) {
         if (param.equalsIgnoreCase("true")) {
             return true;
         } else if (param.equalsIgnoreCase("false")) {
             return false;
         } else {
             throw new IllegalArgumentException("Illegal Rollback argument. Found " + param + "! Please provide 'true' or 'false'!");
         }
     }
 
     /**
      *
      * @param in
      * @throws de.cismet.jpressocore.serviceprovider.exceptions.JPressoException
      */
     public void setConfiguration(final String in) throws JPressoException {
         this.configuration = in;
     }
 
     /**
      *
      * @param in
      * @throws de.cismet.jpressocore.serviceprovider.exceptions.JPressoException
      */
     public void setForceWrite(final String in) throws JPressoException {
         force = evalString(in);
     }
 
     /** Setter for property param.
      * @param param New value of property param.
      *
      */
     public void setRollback(final String rollback) {
         log.debug("Rollback got: " + rollback);
         if (evalString(rollback)) {
             log.info("Rollback was set true. The transcation will be rolled back!");
             rb = true;
         } else {
             rb = false;
         }
     }
 
     /**
      * The method that actually performs all the writing to DB.
      */
     @Override
     public long finalise() throws Exception {
         log.debug("finalise");
         long errorCounter = 0;
         final Connection connection = getIntermedTables().getTargetConn();
         AutoValueStrategy strategy = null;
         try {
             final String drivername = connection.getMetaData().getDatabaseProductName();
            final ResourceBundle res = ResourceBundle.getBundle("de.cismet.jpresso.core.finalizer.finalizer");
             for (final String key : res.keySet()) {
                 if (key.equalsIgnoreCase(AUTOVAL_PROPERTY_PREFIX + drivername)) {
                     final Class<?> avc = Class.forName(res.getString(key));
                     if (avc.isAssignableFrom(AutoValueStrategy.class)) {
                         final Object tmp = avc.newInstance();
                         strategy = (AutoValueStrategy) tmp;
                         break;
                     }
                 }
             }
             if (strategy == null) {
                 throw new FinalizerException("Unsupported driver! Can not find an AutoValueStragegy for " + drivername + "!");
             }
         } catch (Exception ex) {
             throw new FinalizerException("Can not create AutoValueFinalizer!", ex);
         }
         try {
             strategy.configure(connection, configuration);
             final ImportMetaInfo imi = getIntermedTables().getMetaInfo();
             //if no information about sequences where set, create them from all auoinc-fields
             final List<String> autoIncFields = TypeSafeCollections.newArrayList();
             for (final String tab : imi.getTopologicalTableSequence()) {
                 final IntermedTable itab = getIntermedTables().getIntermedTable(tab);
                 final List<Integer> fieldNos = imi.getAutoIncFieldNos(tab);
                 //every table may have only 1 autoinc field here, matching the one sequence on the table
                 if (fieldNos.size() > 1) {
                     throw new IllegalStateException("Multiple autoincrement fields for table " + tab + "!");
                 } else if (fieldNos.size() == 1) {
                     autoIncFields.add(tab + "." + itab.getColumnName(fieldNos.get(0)));
                 }
             }
 
             for (final String cur : autoIncFields) {
                 final String[] tabfield = cur.trim().split("\\.");
                 final String tab = tabfield[0];
                 final String fld = tabfield[1];
                 final int autoIncFieldPos = imi.getPositionInTable(tab, fld);
                 final List<Integer> ownPos = TypeSafeCollections.newArrayList(1);
                 //the autoinc-field itself must be updated for sure
                 ownPos.add(autoIncFieldPos);
                 ownFieldPositions.put(tab, ownPos);
 
                 final Iterable<FieldDescription> referencedFields = imi.getAllMasterFields(new FieldDescription(tab, fld));
                 if (referencedFields != null) {
                     //all referenced fields are stored here, to know where to insert generaded ids later
                     for (final FieldDescription fd : referencedFields) {
                         //we distinguish between self-references from one field to another in the same table...
                         if (imi.getPureTabName(fd.getTableName()).equals(tab)) {
                             final int mPos = imi.getPositionInTable(fd.getTableName(), fd.getFieldName());
                             ownPos.add(mPos);
                         //...and foreign key relations, which are references from another (=master) table
                         } else {
                             Map<FieldDescription, Integer> toUpdate = masterFieldPositions.get(tab);
                             if (toUpdate == null) {
                                 toUpdate = TypeSafeCollections.newHashMap();
                                 masterFieldPositions.put(tab, toUpdate);
                             }
                             toUpdate.put(fd, imi.getPositionInTable(fd.getTableName(), fd.getFieldName()));
                         }
                     }
                 }
             }
             String stmnt = "";
             connection.setAutoCommit(force);
             //now we start inserting, table after table in topological order
             for (final String tableName : getIntermedTables().getMetaInfo().getTopologicalTableSequence()) {
                 final IntermedTable itab = getIntermedTables().getIntermedTable(tableName);
                 final List<Integer> replaceList = ownFieldPositions.get(tableName);
                 final Statement insertStmt = connection.createStatement();
                 final int tableRowCount = itab.getRowCount();
                 strategy.setCurrentFields(tableName, null);
                 //our autoinc-field-position was inserted first in the list, so...
                 final int autoIncFieldPos = replaceList.get(0);
                 System.out.println("finalizing ---> " + tableName);
                 if (debug) {
                     String debugString = "Import into table: " + tableName + " (" + tableRowCount + " rows)\n";
                     log.debug(debugString);
                 }
                 buff.append("\n" + "Import into table: " + tableName + " (" + tableRowCount + " rows)\n");
                 int logErrorCounter = 0;
                 //we get the offset (=the value that our counter-function created during import)
                 final int offset = itab.getAutoIncOffsetForColumn(autoIncFieldPos);
                 //inserting row after row...
                 String genKey = null;
                 for (int j = 0; j < tableRowCount; ++j) {
                     //check for cancelation
                     if (isCanceled()) {
                         connection.rollback();
                         log.info("cancel -> rollback");
                         logs += buff.toString();
                         setProgressCanceled(tableName);
                         return errorCounter;
                     }
                     try {
                         genKey = strategy.nextValue();
                         //fill the retrieved sequence-value into all appropriate fields
                         for (final int replPos : replaceList) {
                             itab.setValueAt(genKey, j, replPos);
                         }
                         //get insert statement from extracted data
                         stmnt = getFixedPartOfInsertStatement(itab) + getValuesForInsert(itab, j);
                         //execute insertion
                         insertStmt.execute(stmnt);
                         if (debug) {
                             log.debug("Statement: " + stmnt);
                         }
                         //update progress(bar)
                         setProgressValue(tableName, j + 1, logErrorCounter);
                     } catch (SQLException ex) {
                         ++errorCounter;
                         ++logErrorCounter;
                         final String msg = "Error at:" + stmnt + ": " + ex;
                         log.error(msg);
                         log.debug(msg + stmnt, ex);
                         setProgressValue(tableName, j + 1, logErrorCounter);
                         //switch to param as error occured
                         rb = true;
                         if (logErrorCounter < MAX_LOG_ERROR) {
                             logs += "    Import error @ statement:" + stmnt + "\n" + ex.toString() + "\n";
                         } else if (logErrorCounter == MAX_LOG_ERROR) {
                             logs += "    ************** more errors (output stopped)\n";
                         }
                     }
                 }
                 //update all references in mastertables
                 final Map<FieldDescription, Integer> toUpdate = masterFieldPositions.get(tableName);
                 if (toUpdate != null && !toUpdate.isEmpty()) {
                     for (final Entry<FieldDescription,Integer> entry : toUpdate.entrySet()) {
                         final IntermedTable masterTab = getIntermedTables().getIntermedTable(entry.getKey().getTableName());
                         final int updatePos = entry.getValue();
                         String toLookup = null;
                         //update all rows in this master-table
                         for (int i = 0; i < masterTab.getRowCount(); ++i) {
                             //get the counter-value of the reference
                             toLookup = masterTab.getValueAt(i, updatePos);
                             if (toLookup != null) {
                                 //calculate in which row the real value for the reference
                                 //can be found in the detail-table. this capitalizes on
                                 //the always gapless ids, which the counter-function creates
                                 final int lkpPos = Integer.parseInt(toLookup) - offset;
                                 //if the value is in the table (not removed by normalization)...
                                 if (lkpPos > -1 && lkpPos < tableRowCount) {
                                     //...update a single reference in the current row of the mastertable
                                     masterTab.setValueAt(itab.getValueAt(lkpPos, autoIncFieldPos), i, updatePos);
                                 }
                             }
                         }
                     }
                 }
                 //adjust the offsets of the intermed-tables to the new values. (this is important for multiple finalizations)
                 if (tableRowCount > 0) {
                     itab.setAutoIncOffsetForColumn(autoIncFieldPos, Integer.parseInt(itab.getValueAt(0, autoIncFieldPos)));
                 }
             }
         } finally {
             try {
                 if (rb && !force) {
                     connection.rollback();
                 } else {
                     connection.commit();
                 }
                 strategy.finish(rb && !force);
             } catch (SQLException ex) {
                 final String msg = "Error on: ROLLBACK: " + ex;
                 log.error(msg);
                 log.debug(msg);
                 logs += "    Import error .. rollback statement\n" + ex.toString() + "\n";
             }
         }
         log.info("Import finished");
         buff.append("\n\n-----------------Import finished");
         logs += buff.toString();
         return errorCounter;
     }
 
     protected String getValuesForInsert(final IntermedTable itab, final int position) throws JPressoException {
         return "(" + itab.getRowStringWithGivenEnclosingChar(position, ",") + ")";
     }
 
     protected String getFixedPartOfInsertStatement(final IntermedTable itab) throws JPressoException {
         return "INSERT INTO " + itab.getTableName() + "(" + getFieldList(itab) + ") VALUES";
     }
 
     protected String getFieldList(final IntermedTable itab) throws JPressoException {
         final StringBuilder sBuff = new StringBuilder();
         for (int i = 0; i < itab.getColumnCount() - 1; ++i) {
             sBuff.append(itab.getColumnName(i)).append(",");
         }
         sBuff.append(itab.getColumnName(itab.getColumnCount() - 1));
         return sBuff.toString();
     }
 
     @Override
     protected void processCancelCommand() {
         setCanceled(true);
     }
 }
