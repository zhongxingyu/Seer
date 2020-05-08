 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URL;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.mged.magetab.error.ErrorItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
 import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.parser.SampleTabParser;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 
 import junit.framework.TestCase;
 
 
 public class TestSampleTabAccessioner extends TestCase {
 	
 	private URL resource;
     private SampleTabParser parser;
 
     private String host;
     private int port;
     private String database;
     private String username;
     private String password;
     
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public void setUp() {
         resource = getClass().getClassLoader().getResource("GVA-estd1/sampletab.pre.txt");
         parser = new SampleTabParser();
         Properties mysqlProperties = new Properties();
         try {
            InputStream is = getClass().getResourceAsStream("/mysql.properties");
             mysqlProperties.load(is);
         } catch (IOException e) {
             e.printStackTrace();
             fail();
             return;
         }
         
         host = mysqlProperties.getProperty("hostname");
         port = new Integer(mysqlProperties.getProperty("port"));
         database = mysqlProperties.getProperty("database");
         username = mysqlProperties.getProperty("username");
         password = mysqlProperties.getProperty("password");
     }
 
     public void tearDown() {
         resource = null;
         parser = null;
     }
 
     public void testAccessioning() {
     	try {
             Accessioner accessioner;
             try {
                 accessioner = new Accessioner(host, port, database, username, password);
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
                 fail();
                 return;
             } catch (SQLException e) {
                 e.printStackTrace();
                 fail();
                 return;
             }
             
     	    SampleData st = parser.parse(resource);
     	    accessioner.convert(st);
     	    
 			StringWriter out = new StringWriter();
 			SampleTabWriter sampletabwriter = new SampleTabWriter(out);
 			try {
 				sampletabwriter.write(st);
 			} catch (IOException e) {
 				fail();
				e.printStackTrace();
 			}
 			//System.out.println(out.toString());
 		} catch (ParseException e) {
            e.printStackTrace();
             fail();
 		} catch (SQLException e) {
		    log.error(e.getSQLState());
            e.printStackTrace();
             fail();
         } 
     }
 
 }
