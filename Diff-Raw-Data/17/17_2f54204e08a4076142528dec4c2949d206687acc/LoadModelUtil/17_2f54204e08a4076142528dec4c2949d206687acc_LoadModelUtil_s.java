 package edu.wustl.cab2b.server.path;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.util.Properties;
 
 /**
  * 
  * @author chandrakant_talele
  *
  */
 public class LoadModelUtil {
 
     /* Format of properties file
     db.driver=com.mysql.jdbc.Driver;
     db.server=10.99.88.100
     db.port=8080
     db.name=test_database
     db.userName=root
     db.password=password
     max.path.length=6
     #   If you want to load metadata for applications say "app1" and "app2" then 
     #   entity.group.names=app1,app2
     #   app1.file=<Model path for app1>
     #   app2.file=<Model path for app2>
     #
     #   For example, to load metadata for caArray and caTissue, the cab2b.properties will look like
     #   entity.group.names=caArray,caTissue
     #   caArray.file=c:/caArrayModel.xml
     #   caTissue.file=c:/caTissueModel.xml
     entity.group.names=model1,model2
     model1.file=file1.xml
     model2.file=file2.xml 
      */
 	/**
 	 * Main method to load load model
 	 * @param args command line args
 	 * @throws Exception
 	 */
     public static void main(String[] args) throws Exception {
         if (args.length != 1 || args[0].trim().equals("")) {
            System.out.println("Please provide a property file");
             return;
         }
         Properties p = new Properties();
         p.load(new FileInputStream(new File(args[0])));
 
         String driver = p.getProperty("db.driver");
         String server = p.getProperty("db.server");
         String port = p.getProperty("db.port");
         String dbName = p.getProperty("db.name");
         String userName = p.getProperty("db.userName");
         String password = p.getProperty("db.password");
         int maxPathlength = Integer.parseInt(p.getProperty("max.path.length"));
         String[] entityGroupNames = p.getProperty("entity.group.names").split(",");
 
         String url = "jdbc:mysql://" + server + ":" + port + "/" + dbName;
         Connection con = null;
         try {
             Class.forName(driver).newInstance();
             con = DriverManager.getConnection(url, userName, password);
             for (String entityGroupName : entityGroupNames) {
                 String xmlFilePath = p.getProperty(entityGroupName + ".file").trim();
                System.out.println("Loading model : " + xmlFilePath);
                 PathBuilder.loadSingleModel(con, xmlFilePath, entityGroupName, maxPathlength);
             }
         } finally {
             if (con != null) {
                 con.close();
             }
         }
     }
 }
