 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package airport.flights;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import org.xml.sax.SAXException;
 
 /**
  *<code>DataCreator</code> represents an object that combines a XMLCreator, 
  * a XMLLoader and a DataProcess object capabilities to create xml, to load xml
  * and to safe the database necessary data.   
  */
 class DataCreator {
 
     DataCreator() throws IOException, URISyntaxException  {
         String f_names_path =
                 DataBaseProcess.FConfigProps.getProperty("pl.names_path");
         String f_surnames_path =
                 DataBaseProcess.FConfigProps.getProperty("pl.surnames_path");
         String curr_dir_path = new File(".").getCanonicalPath();
         //
         FXmlCreator = getXmlCreator(curr_dir_path + f_names_path,
                                     curr_dir_path + f_surnames_path);
     }
     //
     /**
      * Generate an xml-doc and safe it in the file
      * @throws Exception 
      */
     public void generateXML() throws IOException, URISyntaxException, 
                                      ParserConfigurationException, 
                                      TransformerConfigurationException, 
                                      TransformerException {
         String f_xml_path =
                 DataBaseProcess.FConfigProps.getProperty("pl.xml_path");
         String curr_dir_path = new File(".").getCanonicalPath();
         //
         FXmlCreator.wrXMLTree(curr_dir_path + f_xml_path);
         //
     }
     //
     /**
      * Using generated xml-doc data create database and return 
      * map object with passengers data. 
      * @return
      * @throws Exception 
      */
     public Map<String , PassengerInfo > createDataBase() 
                                         throws IOException,
                                                ClassNotFoundException, 
                                                SQLException, 
                                                URISyntaxException, 
                                                ParserConfigurationException, 
                                                SAXException {
         String f_xml_path =
                 DataBaseProcess.FConfigProps.getProperty("pl.xml_path");
         String curr_dir_path = new File(".").getCanonicalPath();
         //
         XMLLoader xml_loader = new XMLLoader(curr_dir_path + f_xml_path);
         //
         return(creatTable(xml_loader));
     }
     //
     
     private Map<String , PassengerInfo > creatTable(XMLLoader __xml_loader) 
             throws ClassNotFoundException, 
                    SQLException, 
                    IOException, 
                    URISyntaxException, 
                    ParserConfigurationException, 
                    SAXException {
         //
         Connection conn = DataBaseProcess.loadDB();
         String db_driver =
                 DataBaseProcess.FConfigProps.getProperty("jdbc.driver");
         String db_url =
                 DataBaseProcess.FConfigProps.getProperty("jdbc.url");
         String tb_name =
                 DataBaseProcess.FConfigProps.getProperty("table.name");
         String create_tb_query = "CREATE TABLE " + tb_name
                 + " ( id INTEGER NOT NULL PRIMARY KEY, "
                 + "   checked_weight INTEGER )";
         Random weight_rand = new Random();
         Map<String , PassengerInfo > xml_data_map=null;
         //
         try {
             //
             xml_data_map = __xml_loader.loadDoc(false);
             if (!DataBaseProcess.isTbExist(conn)) {
                 //
                 Statement stmnt;
                 stmnt = conn.createStatement();
                 stmnt.execute(create_tb_query);
                 //
                 Iterator<String> keyIt = xml_data_map.keySet().iterator();
                 //
                 while (keyIt.hasNext()) {
                     String key = keyIt.next();
                     String id = xml_data_map.get(key).Id;
                     int checked_weight = weight_rand.nextInt(30);
                     //
                     String insert_tb_query = "INSERT INTO " + tb_name  
                             + " VALUES ("+ id + ", "
                             + checked_weight + ")";
                     stmnt.executeUpdate(insert_tb_query);
                 }
                 //
             }
         }
         catch (SQLException ex) {
             for (Throwable t : ex) {
                 t.printStackTrace();
             }
         }
         finally {
             conn.close();
         }
         //
         return(xml_data_map);
     }
     //
 
     private XMLCreator getXmlCreator(String f_nms_path,
             String f_surnms_path) throws IOException, URISyntaxException {
         String char_set =
                 DataBaseProcess.FConfigProps.getProperty("pl.charset");
         return ((FXmlCreator == null)
                ? new XMLCreator(f_nms_path, f_surnms_path, char_set)
                 : FXmlCreator);
     }
     //
     
     private XMLCreator FXmlCreator;
     //
 }
