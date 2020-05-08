 package directi.androidteam.training.chatclient.Authentication;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 import directi.androidteam.training.TagStore.IQTag;
 import directi.androidteam.training.TagStore.JIDTag;
 import directi.androidteam.training.chatclient.Chat.ChatBox;
 import directi.androidteam.training.chatclient.Util.ConnectionHandler;
 import directi.androidteam.training.lib.xml.XMLHelper;
 
 import javax.net.ssl.SSLSocketFactory;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rajat
  * Date: 8/30/12
  * Time: 12:22 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class ConnectGTalk extends AsyncTask<String, Void, Boolean> {
     private static String username;
     private static String password;
     private Context context;
     private Socket s;
     private PrintWriter w;
     private BufferedReader r;
 
     public ConnectGTalk(Context context) {
         this.context = context;
     }
 
     private String getOpenStreamStanza() {
         return "<stream:stream" +
                 " to='gmail.com'" +
                 " xmlns='jabber:client'" +
                 " xmlns:stream='http://etherx.jabber.org/streams'" +
                 " version='1.0'>";
     }
 
     private String getAuthStanza(String auth) {
         return "<auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>" + Base64.encodeBytes(auth.getBytes()) + "</auth>";
     }
 
     private String getResourcePartStanza() {
         return "<iq id='tn281v37' type='set'>" +
                 " <bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/>" +
                 " </iq>";
     }
 
     private String getStartSessionStanza() {
         return "<iq to='talk.google.com'" +
                 " type='set'" +
                 " id='sess_1'>" +
                 " <session xmlns='urn:ietf:params:xml:ns:xmpp-session'/>" +
                 "</iq>";
     }
 
     private String readWhile(String endsWith, BufferedReader reader) throws IOException {
         String response = "";
         int c;
         while (!(response.contains(endsWith))) {
             c = reader.read();
             response = response + (char)c;
             System.out.print((char)c);
         }
         System.out.println();
         return response;
     }
 
     private String readWhileOr(String endsWith1, String endsWith2, BufferedReader reader) throws IOException {
         String response = "";
         int c;
         while (!(response.contains(endsWith1) || response.contains(endsWith2))) {
             c = reader.read();
             response = response + (char)c;
             System.out.print((char)c);
         }
         System.out.println();
         return response;
     }
 
     private void extractJID(String response) {
         XMLHelper helper = new XMLHelper();
         IQTag iqTag = new IQTag(helper.tearPacket(response));
         JIDTag jidTag  = new JIDTag(iqTag.getChildTags().get(0).getChildTags().get(0));
         Log.d("JID intialize", jidTag.getContent());
     }
 
     private boolean checkSASLSuccess(String response) {
         return response.substring(1, 8).equals("success");
     }
 
     private boolean checkSASLFailure(String response) {
         return response.substring(1, 8).equals("failure");
     }
 
     public boolean authenticate(String username, String password) {
         try {
             SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
             Socket socket = sslsocketfactory.createSocket("talk.google.com", 5223);
             socket.setSoTimeout(0);
             socket.setKeepAlive(true);
 
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
             out.print(getOpenStreamStanza());
             out.flush();
             readWhile("</stream:features>", reader);
 
             out.print(getAuthStanza('\0' + username + '\0' + password));
             out.flush();
             String response = readWhileOr("sasl\"/>", "</failure>", reader);
             if (checkSASLSuccess(response) == true) {
             } else if (checkSASLFailure(response) == true) {
                 return false;
             }
 
             out.print(getOpenStreamStanza());
             out.flush();
             readWhile("</stream:features>", reader);
 
             out.print(getResourcePartStanza());
             out.flush();
             extractJID(readWhile("</iq>", reader));
 
             out.print(getStartSessionStanza());
             out.flush();
             readWhile("/>", reader);
 
             this.s = socket;
             this.r = reader;
             this.w = out;
             return true;
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     @Override
     public Boolean doInBackground (String ...params) {
         username = params[0];
         password = params[1];
         Boolean result = authenticate(username, password);
         if (result) {
              ConnectionHandler.init(this.s, this.w, this.r);
         }
         return result;
     }
 
     @Override
     protected void onPostExecute(Boolean result) {
         Intent intent;
 
         if(result)   {
             UserDatabaseHandler db = new UserDatabaseHandler(context);
             db.addUser(new User(username, password));
 
             Intent serviceIntent = new Intent(context,MyService.class);
             context.startService(serviceIntent);
 
             intent = new Intent(this.context, ChatBox.class);
             intent.putExtra(LoginActivity.USERNAME, username);
             intent.putExtra("buddyid","vinayak.bhavnani@gmail.com");
             context.startActivity(intent);
         }
         else {
             intent = new Intent(this.context, LoginErrorActivity.class);
             context.startActivity(intent);
         }
     }
 }
