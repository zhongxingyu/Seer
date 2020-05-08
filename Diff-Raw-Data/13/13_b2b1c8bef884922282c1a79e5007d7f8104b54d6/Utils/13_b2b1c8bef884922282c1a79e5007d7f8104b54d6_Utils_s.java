 /*
  */
 package prosjekt.utils;
 
 import com.cedarsoftware.util.io.JsonReader;
 import com.cedarsoftware.util.io.JsonWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Kristoffer Berdal <web@flexd.net>
  * @studnr 180212
  * @date Mar 26, 2012
  */
 public class Utils {
   
   public static void save(Object o, String filename) {
     {
       JsonWriter jw = null;
       try {
         OutputStream out;
         out = new FileOutputStream(new File(filename));
         jw = new JsonWriter(out);
         jw.write(o);
       } catch (IOException ex) {
         Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
       } finally {
         jw.close();
       }
     }
   }
   public static Object load(String filename) {
     {
       JsonReader jr = null;
       Object out = null;
       try {
         InputStream in;
         in = new FileInputStream(new File(filename));
         jr = new JsonReader(in);
         out = jr.readObject();
       } catch (IOException ex) {
         Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
         jr.close();
       }
       return out;
     }
   }
 //  public void save() {
 //    FileOutputStream out = null;
 //    try {
 //      // Open the login window
 //      //new LoginWindow(
 //      out = new FileOutputStream(new File("roomRegistry.json"));
 //    } catch (FileNotFoundException ex) {
 //      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 //    }
 //    try {
 //      Utils.save(this, out);
 //    } catch (IOException ex) {
 //      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 //    }
 //  }
 }
