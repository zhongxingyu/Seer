 package ms.gundam.astparser;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sleepycat.bind.EntryBinding;
 import com.sleepycat.bind.tuple.TupleBinding;
 import com.sleepycat.je.Cursor;
 import com.sleepycat.je.Database;
 import com.sleepycat.je.DatabaseConfig;
 import com.sleepycat.je.DatabaseEntry;
 import com.sleepycat.je.DatabaseException;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.EnvironmentConfig;
 import com.sleepycat.je.LockMode;
 import com.sleepycat.je.OperationStatus;
 
 public class DB {
 	public static final String ARRAYNAME = "@ARRAY";
     private static final String RelationSeparator = "!";
     private static final String MethodSeparator = "#";
 	private Environment myEnv;
     private Database afterDb;
     private Database beforeDb;
     private Database classCatalogDb;
     
     // Needed for object serialization
//    private StoredClassCatalog classCatalog;
//    private EntryBinding<Value> dataBinding;
 
     public boolean open(File envHome, boolean readOnly) {
         // Database and environment configuration omitted for brevity
         // Instantiate an environment and database configuration object
         EnvironmentConfig myEnvConfig = new EnvironmentConfig();
         DatabaseConfig myDbConfig = new DatabaseConfig();
 
         // Configure the environment and databases for the read-only
         // state as identified by the readOnly parameter on this 
         // method call.
         myEnvConfig.setReadOnly(readOnly);
         myDbConfig.setReadOnly(readOnly);
 
         // If the environment is opened for write, then we want to be
         // able to create the environment and databases if 
         // they do not exist.
         myEnvConfig.setAllowCreate(!readOnly);
         myDbConfig.setAllowCreate(!readOnly);
         
 //        myDbConfig.setSortedDuplicates(true);
 
         // Instantiate the Environment. This opens it and also possibly
         // creates it.
         myEnv = new Environment(envHome, myEnvConfig);
 
 		// Now create and open our databases.
         afterDb = myEnv.openDatabase(null, "AfterDB", myDbConfig);
         beforeDb = myEnv.openDatabase(null, "BeforeDB", myDbConfig);
 
         // Open the class catalog db. This is used to
         // optimize class serialization.
         myDbConfig.setSortedDuplicates(false);
         classCatalogDb = myEnv.openDatabase(null, "ClassCatalogDB", myDbConfig);
 
         // Create our class catalog
//        classCatalog = new StoredClassCatalog(classCatalogDb);
//        dataBinding = new SerialBinding<Value>(classCatalog, Value.class);
         
         return true;
 	}
 	
     private boolean put(Database database, String keyName) {
         final EntryBinding<Integer> IntegerBinding = TupleBinding.getPrimitiveBinding(Integer.class);
 
         DatabaseEntry theKey = null;
         DatabaseEntry theData = new DatabaseEntry();
         Integer count = 1;
 		try {
         	theKey = new DatabaseEntry(keyName.getBytes("UTF-8"));
         } catch (IOException willNeverOccur) {}
 
 		// 
 		if (afterDb.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
 			// Recreate the data String.
 			 count = IntegerBinding.entryToObject(theData);
 			 count++;
 		}
 
 		// Convert the Vendor object to a DatabaseEntry object
         // using our SerialBinding
 //        dataBinding.objectToEntry(theValue, theData);
         Integer myInt = new Integer(count);
         IntegerBinding.objectToEntry(myInt, theData);
         
         OperationStatus ret = database.put(null, theKey, theData);
 
         if (ret == OperationStatus.SUCCESS)
         	return true;
         else
         	return false;
     }
 	public boolean put(String keyclassname, String keymethodname, String classname, String methodname) {
         String keyName = keyclassname + MethodSeparator + keymethodname + RelationSeparator + classname + MethodSeparator+ methodname;
         boolean ret = put(afterDb, keyName);
         keyName = classname + MethodSeparator + methodname + RelationSeparator + keyclassname + MethodSeparator + keymethodname;
         boolean ret2 = put(beforeDb, keyName);
         if (ret && ret2)
         	return true;
         else
         	return false;
 	}
 	
 	/**
 	 * @param keyclassname クラス名
 	 * @param keymethodname メソッド名
 	 * @param which どっちのデータベースを使うかを指定する．trueならafter，falseならbefore
 	 * @return Valueのリスト
 	 */
 	public List<Value> get(String keyclassname, String keymethodname, boolean which) {
 		List<Value> list = new ArrayList<Value>();
 		final String keyName = keyclassname + MethodSeparator + keymethodname;
         final EntryBinding<Integer> IntegerBinding = TupleBinding.getPrimitiveBinding(Integer.class);
 		Cursor cursor = null;
 		try {
 			if (which) {
 				cursor = afterDb.openCursor(null, null);
 			} else {
 				cursor = beforeDb.openCursor(null, null);
 			}
 			if (cursor == null) {
 				System.err.println("Cannot get cursor");
 				return list;
 			}
 			DatabaseEntry theKey = new DatabaseEntry(keyName.getBytes("UTF-8"));
 			DatabaseEntry theData = new DatabaseEntry();
     
 			OperationStatus ret = cursor.getSearchKeyRange(theKey, theData, LockMode.DEFAULT);
 			if (ret != OperationStatus.SUCCESS) {
 				System.err.println("No such Key in the DB " + keyName);
 				return list;
 			}
 //System.out.println(cursor.count());
 			if (cursor.count() > 0) {
 				while (ret == OperationStatus.SUCCESS) {
 					String keyString = new String(theKey.getData(), "UTF-8");
 					int count = IntegerBinding.entryToObject(theData);
 //System.out.println(count + keyString);
 					String[] valueString = keyString.split(RelationSeparator);
 					Value value = new Value();
 					if (!valueString[0].equals(keyName))
 						break;
 					if (valueString.length == 2) {
 						String[] name = valueString[1].split(MethodSeparator);
 						value.setClassname(name[0]);
 						value.setMethodname(name[1]);
 						list.add(value);
 					}
 					value.setCount(count);
 					ret = cursor.getNext(theKey, theData, LockMode.DEFAULT);
 				}
 			} 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			cursor.close();
 		}
 		return list;
 	}
 
 	public void close() {
         if (myEnv != null) {
             try {
                 afterDb.close();
                 beforeDb.close();
                 classCatalogDb.close();
                 myEnv.close();
             } catch(DatabaseException dbe) {
                 System.err.println("Error closing MyDbEnv: " + dbe.toString());
                 System.exit(-1);
             }
         }		
 	}
 }
