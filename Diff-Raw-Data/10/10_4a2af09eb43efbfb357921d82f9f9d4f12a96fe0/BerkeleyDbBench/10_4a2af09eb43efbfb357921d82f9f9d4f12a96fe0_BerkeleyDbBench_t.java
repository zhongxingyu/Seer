 package net.wrap_trap.collections.fsm.bench;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 
 import com.sleepycat.je.Database;
 import com.sleepycat.je.DatabaseConfig;
 import com.sleepycat.je.DatabaseEntry;
 import com.sleepycat.je.DatabaseException;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.EnvironmentConfig;
 import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
 
 public class BerkeleyDbBench extends ReadWriteBench {
 
     private Database db;
     private Environment env;
 
     @Override
     protected void prepare(int times) {
         TestUtils.deleteDirectoryQuietly("tmp");
         File tmpDir = new File("tmp");
         tmpDir.mkdir();
         EnvironmentConfig envConfig = new EnvironmentConfig();
         envConfig.setAllowCreate(true);
         env = new Environment(new File("tmp"), envConfig);
         DatabaseConfig dbConfig = new DatabaseConfig();
         dbConfig.setAllowCreate(true);
         db = env.openDatabase(null, "sampleDatabase", dbConfig);
     }
 
     @Override
     protected void cleanUp() {
         db.close();
         env.close();
     }
 
     @Override
     protected void write(int times) {
         try {
             StringBuilder sb = new StringBuilder();
             for (int i = 1; i <= times; i++) {
                 sb.append("a");
                 db.put(null, new DatabaseEntry(String.valueOf(i).getBytes("UTF-8")),
                        new DatabaseEntry(sb.toString().getBytes("UTF-8")));
             }
         } catch (DatabaseException e) {
             throw new RuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     protected void read(int times) {
         try {
             for (int i = 1; i <= times; i++) {
                OperationStatus status = db.get(null, new DatabaseEntry(String.valueOf(i).getBytes("UTF-8")),
                                                new DatabaseEntry(), LockMode.READ_UNCOMMITTED);
                if (status != OperationStatus.SUCCESS) {
                    throw new RuntimeException("status != OperationStatus.SUCCESS");
                 }
             }
         } catch (DatabaseException e) {
             throw new RuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 }
