 package appchk;
 
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.apache.log4j.Logger;
 
 public class Pinger {
     
     private static Logger log = Logger.getLogger(Pinger.class);
     
     public static PingResponse ping(PingUrl pingUrl){
         String url = pingUrl.getUrl();
         boolean hasError = false;
         Map headers = null;
         String status = null;
         if (log.isDebugEnabled()){
             log.debug("ping "+url);
         }
         try {
             URL test = new URL(url);
             HttpURLConnection con = (HttpURLConnection)test.openConnection();
             con.setRequestMethod("HEAD");
             headers = con.getHeaderFields();
             int resp = con.getResponseCode();
             status = Integer.toString(resp);
             if ( resp == HttpURLConnection.HTTP_OK) {
                 log.debug("ALL OK!");
             } else {
                 log.debug("FAIL!");
                 hasError = true;
             }
         } /*catch (ProtocolException e) {
            log.error("", e);
             hasError = true;
             status = e.getMessage();
         } catch (IOException e) {
             log.error("", e);
             hasError = true;
             status = e.getMessage();
         } */catch (Exception e) {
             log.error("", e);
             hasError = true;
            status = e.getMessage();
         }
 
         return new PingResponse(status, headers, hasError);
     }
 
 }
