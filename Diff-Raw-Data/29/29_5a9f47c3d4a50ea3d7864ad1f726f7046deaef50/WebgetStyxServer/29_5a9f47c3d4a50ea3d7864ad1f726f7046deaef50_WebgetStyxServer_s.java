/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package wonder.styx;
 
import java.io.BufferedInputStream;
import java.io.BufferedReader;
 import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
 import java.nio.charset.StandardCharsets;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.mina.common.ByteBuffer;
 import uk.ac.rdg.resc.jstyx.StyxException;
 import uk.ac.rdg.resc.jstyx.server.StyxDirectory;
 import uk.ac.rdg.resc.jstyx.server.StyxFile;
 import uk.ac.rdg.resc.jstyx.server.StyxFileClient;
 import uk.ac.rdg.resc.jstyx.server.StyxServer;
 
 /**
 *
  * @author zubr
  */
 public class WebgetStyxServer {
     private static class WebgetFile extends StyxFile {
         private static final String INFERNO_USER_AGENT = "Inferno-webget/1.0";
        private static final int READ_CHUNK_SIZE = 8192;
         private Map<Long, String> results;
         
         WebgetFile() throws StyxException {
             super("webget");
             this.setPermissions(0777);
             this.results = new HashMap<Long, String>();
         }
         
         private String convertStreamToString(java.io.InputStream is) {
             java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
             return s.hasNext() ? s.next() : "";
         }
         
         private void processRequest(long fid, String request) {
             try {
                 System.out.println(request);
                 
                 StringTokenizer tokenizer = new StringTokenizer(request);
                 if (tokenizer.countTokens() < 6)
                     throw new IllegalArgumentException("not enought parameters provided");
                 
                 String method = tokenizer.nextToken();
                 String bodyLength = tokenizer.nextToken();
                 String requestId = tokenizer.nextToken();
                 String url = tokenizer.nextToken();
                 String accept = tokenizer.nextToken();
                 String cacheControl = tokenizer.nextToken();
                 String authCookie = "";
                 if (tokenizer.hasMoreTokens())
                     authCookie = tokenizer.nextToken();
                 
                 // check for some request errors
                 if (!method.equals("GET"))
                     throw new IllegalArgumentException("method " + method + " is not implemented");
                 if (!bodyLength.equals("0"))
                     throw new IllegalArgumentException("body length must be 0 for GET request");
                 
                 // make a real connection
                 URL realUrl = new URL(url);
                 URLConnection connection = realUrl.openConnection();
                 connection.setRequestProperty("User-Agent", INFERNO_USER_AGENT);
                 String answer = convertStreamToString(connection.getInputStream());
                 
                 // form an answer
                 StringBuilder result = new StringBuilder();
                 result.append("OK ").append(answer.length()).append(' ');
                 result.append(requestId).append(' ').append(accept).append(' ');
                 result.append(url).append('\n');
                 result.append(answer);
                 results.put(fid, result.toString());
             }
             catch (IOException | IllegalArgumentException ex) {
                 results.put(fid, "ERROR 0 " + ex.getMessage());
             }
         }
 
         @Override
         public void read(StyxFileClient client, long offset, int count, int tag) throws StyxException {
             if (results.containsKey(client.getFid()))
                 this.processAndReplyRead(results.get(client.getFid()), client, offset, count, tag);
             this.processAndReplyRead("make a request first", client, offset, count, tag);
         }
 
         @Override
         public void write(StyxFileClient client, long offset, int count, ByteBuffer data, boolean truncate, int tag) throws StyxException {
             try {
                 processRequest(client.getFid(), data.getString(StandardCharsets.UTF_8.newDecoder()));
                 this.replyWrite(client, count, tag);
             } catch (CharacterCodingException ex) {
                 Logger.getLogger(WebgetStyxServer.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         @Override
         protected void clientConnected(StyxFileClient client) {
             // do something useful...
         }
 
         @Override
         protected void clientDisconnected(StyxFileClient client) {
             results.remove(client.getFid());
         }
     }
     
     /**
      * Start everything.
      * @param args 
      */
     public static void main(String[] args) throws Exception {
         // Create the root directory
         StyxDirectory root = new StyxDirectory("/");
        // Create a WhoAmI file and add it to the root directory
         root.addChild(new WebgetFile());
         // Create and start a Styx server, listening on port 9876
         new StyxServer(9876, root).start();
     }
 }
