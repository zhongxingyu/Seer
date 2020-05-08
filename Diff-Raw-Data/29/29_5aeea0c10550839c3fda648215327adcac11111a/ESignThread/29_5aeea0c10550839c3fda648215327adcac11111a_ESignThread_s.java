 
 import java.io.IOException;
import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * Creates new thread for esigning the data
  * @author TeamG
  */
 public class ESignThread extends Thread {
     
     public ESignThread(){
         
     }
     public void run(){
         String type = "application/json";
         URL u;
         try {
             u = new URL("https://stage-api.e-signlive.com/aws/rest/services/codejam");
             HttpURLConnection conn;
         try {
             conn = (HttpURLConnection) u.openConnection();
             conn.setDoOutput(true);
             conn.setRequestMethod( "POST" );
             conn.setRequestProperty("Authorization", "Basic Y29kZWphbTpBRkxpdGw0TEEyQWQx"); 
             conn.setRequestProperty( "Content-Type", type );
            OutputStream os = conn.getOutputStream();
            InputStream is = conn.getInputStream();
              } catch (IOException ex) {
                 Logger.getLogger(ESignThread.class.getName()).log(Level.SEVERE, null, ex);
             } } catch (MalformedURLException ex) {
             Logger.getLogger(ESignThread.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
