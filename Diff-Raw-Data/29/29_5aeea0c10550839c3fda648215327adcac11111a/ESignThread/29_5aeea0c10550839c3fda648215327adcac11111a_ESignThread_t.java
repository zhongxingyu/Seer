 
 import java.io.IOException;
 import java.io.OutputStream;
import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import org.json.JSONObject;
 
 
 /**
  * Creates new thread for esigning the data
  * @author TeamG
  */
 public class ESignThread extends Thread {
    private String JSONString;
    private String responseMessage;
     
     public ESignThread(){
         
     }
    public String getResponseMessage(){
        return this.responseMessage;
    }
    public String setJSONString(JSONObject object){
        JSONString = object.toString();
        return JSONString;
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
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(JSONString);
            writer.close();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String returnString = conn.getResponseMessage();
                this.responseMessage = returnString;
            } else {
            // Server returned HTTP error code.
                System.out.println("There's a problem with the response");
            }
              } catch (IOException ex) {
                 Logger.getLogger(ESignThread.class.getName()).log(Level.SEVERE, null, ex);
             } } catch (MalformedURLException ex) {
             Logger.getLogger(ESignThread.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
