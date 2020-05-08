 package pe.edu.pucp.teleprocesamiento.server.http;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Hashtable;
 import javax.microedition.io.Connector;
 import javax.microedition.io.StreamConnection;
 import javax.microedition.io.StreamConnectionNotifier;
 import pe.edu.pucp.teleprocesamiento.server.form.ServerForm;
 import pe.edu.pucp.teleprocesamiento.server.status.RoomStatus;
 
 /**
  *
  * Handles HTTP requests.
  *
  * @author Carlos G. Gavidia
  */
 public class HttpConnectionProcessor extends Thread {
 
     public static final String ROOM_ID_PARAM = "roomId";
     public static final String ACTION_PARAM = "action";
     public static final String STATUS_ACTION = "STATUS";
     public static final String TURN_ON_ACTION = "ON";
     public static final String TURN_OFF_ACTION = "OFF";
    public static final String HTTP_PORT = "8000";
     private final ServerForm serverForm;
 
     public HttpConnectionProcessor(ServerForm serverForm) {
         this.serverForm = serverForm;
     }
 
     public void run() {
 
         while (true) {
             System.out.println("Starting request ...");
             InputStream inputStream = null;
             PrintStream printStream = null;
             StringBuffer buffer = new StringBuffer();
 
             StreamConnection connection = null;
             StreamConnectionNotifier connectionNotifier = null;
             try {
                 connectionNotifier =
                        (StreamConnectionNotifier) Connector.open("socket://:" 
                        + HTTP_PORT);
                 connection = connectionNotifier.acceptAndOpen();
                 inputStream = connection.openInputStream();
                 int character;
                 while ((character = inputStream.read()) != -1) {
                     final char letter = (char) character;
                     if (letter == '\n') {
                         break;
                     }
                     buffer.append(letter);
                 }
                 String queryString = buffer.toString().substring(buffer.toString().indexOf(' '),
                         buffer.toString().lastIndexOf(' '));
                 Hashtable requestParameters = getRequestParameters(queryString);
                 String response = "";
                 String roomCode = (String) requestParameters.get(ROOM_ID_PARAM);
                 System.out.println("roomCode: " + roomCode);
                 String action = (String) requestParameters.get(ACTION_PARAM);
                 System.out.println("action: " + action);
 
                 RoomStatus status = RoomStatus.getInstance();
 
                 if (STATUS_ACTION.equals(action)) {
                     response = status.getLightStatus(Integer.parseInt(roomCode));
                 } else if (TURN_ON_ACTION.equals(action)) {
                     response = status.turnLightOn(Integer.parseInt(roomCode));
                 } else if (TURN_OFF_ACTION.equals(action)) {
                     response = status.turnLightOff(Integer.parseInt(roomCode));
                 }
                 System.out.println("response: " + response);
                 serverForm.refreshForm();
 
                 printStream = new PrintStream(connection.openOutputStream());
                 printStream.println("HTTP/1.0 200 OK\n");
                 printStream.println(response);
                 System.out.println("Sending response ...");
             } catch (IOException ex) {
                 ex.printStackTrace();
             } finally {
                 try {
                     if (inputStream != null) {
                         inputStream.close();
                     }
                     if (printStream != null) {
                         printStream.close();
                     }
                     if (connection != null) {
                         connection.close();
                     }
                     if (connectionNotifier != null) {
                         connectionNotifier.close();
                     }
                 } catch (IOException ioee) {
                     ioee.printStackTrace();
                 }
             }
 
         }
     }
 
     private Hashtable getRequestParameters(String url) {
         Hashtable values = new Hashtable();
 
         int s = url.indexOf("?");
         int e = 0;
 
         while (s != -1) {
             e = url.indexOf("=", s);
             String name = url.substring(s + 1, e);
             s = e + 1;
             e = url.indexOf("&", s);
 
             if (e < 0) {
                 values.put(name, url.substring(s, url.length()));
             } else {
                 values.put(name, url.substring(s, e));
             }
             s = e;
         }
         return values;
     }
 }
