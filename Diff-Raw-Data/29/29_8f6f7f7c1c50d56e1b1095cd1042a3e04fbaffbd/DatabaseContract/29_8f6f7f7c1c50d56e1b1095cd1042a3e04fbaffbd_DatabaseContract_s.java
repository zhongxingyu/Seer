 package put.medicallocator.io.sqlite;
 
 
 /**
  * Contract class to be used when interacting with {@link DatabaseProvider}.
  */
 public class DatabaseContract {
 
     protected static final String DATABASE_NAME = "locator.db";
    protected static final int DATABASE_VERSION = 1;
     
 	interface Tables {
 		String FACILITY = "facility";
 	}
 	
 	interface FacilityColumnsParams {
 		String PARAM_ID = "INTEGER PRIMARY KEY AUTOINCREMENT";
 		String PARAM_NAME = "TEXT NOT NULL";
 		String PARAM_ADDRESS = "TEXT NOT NULL";
 		String PARAM_PHONE = "TEXT";
 		String PARAM_EMAIL = "TEXT";
 		String PARAM_LATITUDE = "REAL";
 		String PARAM_LONGITUDE  = "REAL";
 	}
 	
 	private DatabaseContract() {
 	}
 }
