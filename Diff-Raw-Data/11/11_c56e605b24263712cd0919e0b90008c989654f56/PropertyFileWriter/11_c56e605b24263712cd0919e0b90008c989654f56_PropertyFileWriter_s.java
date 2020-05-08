 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gmailmonitor.utils;
 
 import gmailmonitor.gui.GUI;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author CodeBeast
  */
 public class PropertyFileWriter {
     private final static String PROPERTY_FILE_NAME="configuration.properties";
     public final static Properties CONNECTION_PROPERTIES = new Properties();
     //private static File propFile;
     private static File f;
     
     static {
         try {
             System.out.println("In Static");
             loadProperties();
         } catch (IOException ex) {
             GUI.getLoggerFrame().log("ERROR!"+ex.getMessage());
         }
     }
     
     public static void writeToPropertyFile(final String propertyName,final String propertyValue) {
         try {
             CONNECTION_PROPERTIES.setProperty(propertyName, propertyValue);
             //PropertyFileWriter.class.getResource("/resources/connections.properties").
             CONNECTION_PROPERTIES.store(new FileOutputStream(f), null);
             //System.out.println("Successfully Saved to property file:"+propertyName+" - "+propertyValue);
             GUI.getLoggerFrame().log("Wrote to property file:"+propertyName+" - "+propertyValue);
         } catch (Exception ex) {
             GUI.getLoggerFrame().log("ERROR!"+ex.getMessage());
         }
     }
     
     private static void createPropertyFileIfAbsent() {
         
         FileWriter fw=null;
         try {
             f = new File(System.getProperty("user.home")+File.separatorChar+PROPERTY_FILE_NAME);
             if (f.exists()) {
                 GUI.getLoggerFrame().log(f.getAbsolutePath()+" is present at USER HOME");
                 System.out.println(f.getAbsolutePath()+" is present at USER HOME");
                 return;
             }
             //create the stuff.
             GUI.getLoggerFrame().log("Warning! Config does not exist. Creating at:"+System.getProperty("user.home")+File.separatorChar+PROPERTY_FILE_NAME);
             System.out.println("Warning! Config does not exist. Creating at:"+System.getProperty("user.home")+File.separatorChar+PROPERTY_FILE_NAME);
             f.createNewFile();
             fw = new FileWriter(f);
            fw.write(GeneralUtils.CONFIG_FILE_TEXT);
             fw.flush();
             
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(null, "We cannot create working file in location:"+System.getProperty("user.home")+"\n Exiting!", "OOPS!", JOptionPane.ERROR_MESSAGE);
             GUI.getLoggerFrame().log("ERROR!!"+ex.getMessage());
             try {
                 fw.close();
             } catch (IOException ex1) {
                 //swallow
             }
             
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         
         
         
     }
 
     public static void loadProperties() throws IOException {
         createPropertyFileIfAbsent();
         CONNECTION_PROPERTIES.load(new FileReader(f));
         System.out.println("Loaded Properties...");
         //JOptionPane.showMessageDialog(null,);
 //        GUI.getLoggerFrame().log("Loaded the Properties file...");
     }
     
     public static void main(String[] args) {
         System.out.println(PropertyFileWriter.CONNECTION_PROPERTIES.getProperty("password"));
     }
 }
