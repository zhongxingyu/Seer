 package eu.neq.mais.technicalservice.storage;
 
 import org.tmatesoft.sqljet.core.SqlJetException;
 import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
 import org.tmatesoft.sqljet.core.table.ISqlJetTable;
 import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
 import org.tmatesoft.sqljet.core.table.SqlJetDb;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 
 public class DbHandler {
 
     public static String LOCATION = "./resources/";
     public static String DB_NAME = "MAIS_INTERNAL_DB2";
     private SqlJetDb db;
 
     private Login login;
     private VitalData vitalData;
     private LabTestRequest labTestRequest;
 
 
     public DbHandler() {
         File f = new File(LOCATION + DB_NAME);
         try {
             this.db = SqlJetDb.open(f, true);
             //this.insertVitalData("test");
         } catch (SqlJetException e) {
             e.printStackTrace();
         } finally {
             vitalData = new VitalData();
             vitalData.initialize(this.getDb());
             login = new Login();
             login.initialize(this.getDb());
 
             labTestRequest = new LabTestRequest();
             labTestRequest.initialize(this.getDb());
         }
 
     }
 
 
     public void close() {
         try {
             this.db.close();
         } catch (SqlJetException e) {
             e.printStackTrace();
         }
     }
 
     public Login getLatestLogin(final String user_id) {
         Login result = null;
 
         try {
             return (Login) db.runReadTransaction(new ISqlJetTransaction() {
 
                 public Object run(SqlJetDb db) throws SqlJetException {
                     ISqlJetTable table = db.getTable(Login.TABLE_NAME);
                     ISqlJetCursor cursor = table.lookup(Login.INDEX_USER_ID, user_id);
 
                     cursor.last();
                     return new Login(cursor);
                 }
             });
         } catch (SqlJetException e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
     public boolean removeLabTestRequest(final String request_id) {
         try {
             db.runWriteTransaction(new ISqlJetTransaction() {
 
                 public Object run(SqlJetDb db) throws SqlJetException {
                     ISqlJetTable table = db.getTable(LabTestRequest.TABLE_NAME);
                     ISqlJetCursor cursor = table.lookup(LabTestRequest.INDEX_REQUEST_ID, request_id);
                     cursor.delete();
 
                     return true;
                 }
             });
         } catch (SqlJetException e) {
             e.printStackTrace();
             return false;
         }
 
         return true;
     }
 
     public boolean saveLabTestRequests(final String user_id, final String request_id) {
 
         try {
             db.runWriteTransaction(new ISqlJetTransaction() {
 
                 public Object run(SqlJetDb db) throws SqlJetException {
                     ISqlJetTable table = db.getTable(LabTestRequest.TABLE_NAME);
                     table.insert(user_id, request_id);
 
                     return true;
                 }
             });
         } catch (SqlJetException e) {
             e.printStackTrace();
             return false;
         }
 
         return true;
     }
 
     @SuppressWarnings("unchecked")
     public List<LabTestRequest> getLabTestRequests(final String user_id) {
         final List<LabTestRequest> result = new ArrayList<LabTestRequest>();
 
         try {
             return (List<LabTestRequest>) db.runReadTransaction(new ISqlJetTransaction() {
 
                 public Object run(SqlJetDb db) throws SqlJetException {
                     ISqlJetTable table = db.getTable(LabTestRequest.TABLE_NAME);
                     ISqlJetCursor cursor = table.lookup(LabTestRequest.INDEX_USER_ID, user_id);
 
                     do {
                         LabTestRequest tmp = new LabTestRequest();
                         tmp.read(cursor);
                         result.add(tmp);
 
                     } while (cursor.next());
 
                     return result;
                 }
             });
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
     public void saveLogin(final String user_id) {
         try {
             db.runWriteTransaction(new ISqlJetTransaction() {
                 public Object run(SqlJetDb db) throws SqlJetException {
                     ISqlJetTable table = db.getTable(Login.TABLE_NAME);
                     table.insert(user_id, System.currentTimeMillis());
                     return true;
                 }
             });
         } catch (SqlJetException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
         DbHandler dbh = new DbHandler();
 
     }
 
     public SqlJetDb getDb() {
         return db;
     }
 
     public void setDb(SqlJetDb db) {
         this.db = db;
     }
 
     /*
     VitalData methods
      */
 
 
     public int getUserItemsCount(final String user_id) {
 
         try {
             return (Integer) db.runReadTransaction(new ISqlJetTransaction() {
 
                 public Object run(SqlJetDb db) throws SqlJetException {
                     ISqlJetTable table = db.getTable(VitalData.TABLE_NAME);
 
 
                     ISqlJetCursor cursor = table.lookup(VitalData.INDEX_USER_ID,
                             user_id);
 
                     return (int) cursor.getRowCount();
                 }
             });
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return 0;
     }
 
 
     public List<VitalData> getVitalData(final String user_id, final Calendar startDate, final Calendar endDate) {
         final List<VitalData> result = new ArrayList<VitalData>();
         final Calendar startDate_DB = Calendar.getInstance();
         startDate_DB.setTime(startDate.getTime());
 
         final Calendar endDate_DB = Calendar.getInstance();
         endDate_DB.setTime(endDate.getTime());
 
 
         try {
             return (List<VitalData>) db.runReadTransaction(new ISqlJetTransaction() {
 
                 public Object run(SqlJetDb db) throws SqlJetException {
                     ISqlJetTable table = db.getTable(VitalData.TABLE_NAME);
 
 
                     ISqlJetCursor cursor = table.scope(VitalData.INDEX_VITALDATA_ITEM_ID,
                            new Object[]{startDate_DB.getTimeInMillis()},
                            new Object[]{endDate.getTimeInMillis()});
 
                     do {
 
                         VitalData tmp = new VitalData();
                         tmp.read(cursor);
                        result.add(tmp);
                        //if (tmp.getUser_id() == user_id)
                     } while (cursor.next());
 
                     return result;
                 }
             });
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
 
     public void insertVitalData(final String user_id) {
 
         try {
             final eu.neq.mais.domain.gnuhealth.VitalDataGnu vitalDataGnu = new eu.neq.mais.domain.gnuhealth.VitalDataGnu(user_id);
             final Calendar calendar = Calendar.getInstance();
             calendar.add(Calendar.DATE, -100);
             //System.out.println(calendar.getTime());
 
 
             db.runWriteTransaction(new ISqlJetTransaction() {
 
                 public Object run(SqlJetDb db) throws SqlJetException {
                     ISqlJetTable table = db.getTable(VitalData.TABLE_NAME);
                     for (int i = 1; i < 100; i++) {
                         vitalDataGnu.generateVitalData();
                         calendar.add(Calendar.DATE, 1);
                         //System.out.println(calendar.getTime());
                         table.insert(user_id, calendar.getTimeInMillis(),
                                 vitalDataGnu.getBmi(),
                                 vitalDataGnu.getTemprature(),
                                 vitalDataGnu.getBlood_pressure(),
                                 vitalDataGnu.getFluid_balace()
                         );
                     }
                     return true;
                 }
             });
         } catch (SqlJetException e) {
             e.printStackTrace();
         }
     }
 
 
 }
