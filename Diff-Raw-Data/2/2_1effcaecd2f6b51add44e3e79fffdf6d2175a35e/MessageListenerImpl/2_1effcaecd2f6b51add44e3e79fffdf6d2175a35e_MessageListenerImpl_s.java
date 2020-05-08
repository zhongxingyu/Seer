 package amq;
 
 import java.io.*;
 
 import java.lang.Integer;
 
 import java.util.Scanner;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import java.net.URLEncoder;
 import java.net.URL;
 import java.net.HttpURLConnection;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 
 import org.apache.log4j.Logger;
 import org.apache.commons.lang.StringUtils;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 public class MessageListenerImpl implements MessageListener {
 
     private static final Logger LOG = Logger.getLogger(MessageListenerImpl.class);
 
     public void onMessage(Message message) {
         if ((message instanceof TextMessage)) {
             try {
                 LOG.debug(((TextMessage)message).getText());
 
                 // Load message into DOM
                 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder builder = factory.newDocumentBuilder();
                 InputSource is = new InputSource(new StringReader(((TextMessage)message).getText()));
                 Document doc = builder.parse(is);
                 doc.getDocumentElement().normalize();
 
                 // Find the PID
                 String pid = doc.getElementsByTagName("summary").item(0).getChildNodes().item(0).getNodeValue();
                 LOG.debug("PID: " + pid);
 
                 // Find the type of action
                 String title = doc.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue();
                 LOG.debug("Action title: " + title);
 
                 // Split the subdirectory (usually 'fedora') off the URL and replace with 'oac_web_service/rebuild'
                 Properties configFile = new Properties();
                 String config_path = System.getenv("FEDORA_SETTINGS");
                 if (config_path == null) {
                     config_path = "../../oac_web_service/fedora_settings.py";
                 }
                 LOG.info("Config Path: " + config_path);
                configFile.load(this.getClass().getClassLoader().getResourceAsStream(config_path));
 
                 String baseUrl = configFile.getProperty("OAC_ENDPOINT");
                 if (baseUrl.equals("") || baseUrl.equals("\"\"") || baseUrl.equals("''")) {
                     // Find the URL to Fedora
                     String url = doc.getElementsByTagName("uri").item(0).getChildNodes().item(0).getNodeValue();
                     LOG.info("Fedora URL: " + url);
                     List url_list = Arrays.asList(url.split("/"));
                     baseUrl = StringUtils.join(url_list.subList(0,url_list.size() - 1), '/');
                 }
                 LOG.debug("OAC Base URL: " + baseUrl);
                 String rebuild_url = baseUrl + "/rebuild_one";
                 LOG.info("Rebuild one URL : " + rebuild_url);
 
                 LOG.info("Recieved message with title: '" + title + "'");
                 // We are going to rebuild on all actions EXCEPT ingest.
                 if (!title.equals("ingest")) {
                     // POST the PID to the rebuild service
                     String data = URLEncoder.encode("pid", "UTF-8") + "=" + URLEncoder.encode(pid, "UTF-8");
                     URL u = new URL(rebuild_url);
                     HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                     conn.setDoOutput(true);
                     conn.setDoInput(true);
                     conn.setRequestMethod( "POST" );
                     conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
                     conn.setRequestProperty( "charset", "utf-8");
                     conn.setRequestProperty( "Content-Length", Integer.toString(data.getBytes().length) );
                     conn.setUseCaches (false);
 
                     OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
                     os.write( data );
                     os.flush();
 
                     // Get the response
                     BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                     String line;
                     while ((line = rd.readLine()) != null) {
                         LOG.debug("Response: " + line);
                     }
                     os.close();
                     rd.close();
                 }
             } catch(Exception e) {
                 e.printStackTrace();
             }
         }
     }
 }
