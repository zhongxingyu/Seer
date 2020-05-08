 package PropertyReader;
 
 
 import java.io.IOException;
 import java.util.Properties;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author daniela
  */
 public abstract class PropertyReader {
     
     protected Properties props;
 
     protected PropertyReader(String path) {
         readPropertyFile(path);
     }
 
     private void readPropertyFile(String path) {
 
         java.io.InputStream is = ClassLoader.getSystemResourceAsStream(path);
         if (is != null) {
             props = new java.util.Properties();
             try {
                 props.load(is);
             } catch(IOException e) {
                 System.err.println("Couldn't read Properties file + " + path);
             }
         } else {
             System.err.println("Properties file not found! " + path);
         }
 
     }
 }
