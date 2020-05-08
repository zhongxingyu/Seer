 package org.jailsframework.database;
 
import org.jailsframework.exceptions.JailsException;

 /**
  * @author <a href="mailto:sanjusoftware@gmail.com">Sanjeev Mishra</a>
  * @version $Revision: 0.1
  *          Date: Apr 3, 2010
  *          Time: 9:42:17 AM
  */
 public class DatabaseFactory {
     private String adapter;
 
     public DatabaseFactory(String adapterName) {
         this.adapter = adapterName;
     }
 
     public IDatabase getDatabase(String url, String driver, String databaseName, String userName, String password) {
         if ("mysql".equals(adapter)) {
             return new MysqlDatabase(url, driver, databaseName, userName, password);
         } else {
            throw new JailsException("The \"" + adapter + "\" name is not yet supported !!");
         }
     }
 }
