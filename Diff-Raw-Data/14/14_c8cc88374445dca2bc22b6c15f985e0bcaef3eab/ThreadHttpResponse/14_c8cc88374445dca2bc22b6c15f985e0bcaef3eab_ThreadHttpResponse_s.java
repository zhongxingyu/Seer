 
 package me.heldplayer.ModeratorGui.WebGui;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.util.TreeMap;
 
 import me.heldplayer.ModeratorGui.ModeratorGui;
 import me.heldplayer.ModeratorGui.WebGui.ErrorResponse.ErrorType;
 
 public class ThreadHttpResponse extends Thread {
     private final Socket socket;
     private DataOutputStream out;
     private BufferedReader in;
     private long timeout = 0;
 
     public ThreadHttpResponse(Socket socket) {
         super("Thread HTTP Responder");
         this.socket = socket;
 
         this.setDaemon(true);
         this.start();
     }
 
     @Override
     public void run() {
         RequestFlags flags = new RequestFlags();
 
         main:
         {
             try {
                 out = new DataOutputStream(socket.getOutputStream());
                 in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
                 TreeMap<Integer, String> input = new TreeMap<Integer, String>();
                 Integer i = 0;
 
                 while (!in.ready()) {
                     timeout++;
 
                     if (timeout > 30000L) {
                         break;
                     }
                     try {
                         Thread.sleep(1L);
                     }
                     catch (InterruptedException e) {
                         break;
                     }
                 }
 
                 while (in.ready()) {
                     String line = in.readLine();
                     input.put(i++, line);
                 }
 
                 if (input.size() <= 0) {
                     new ErrorResponse(ErrorType.BadRequest).writeResponse(flags).flush(out);
 
                     break main;
                 }
 
                 String[] split = input.get(0).split(" ");
                 String method = split[0];
                String location = split[1];
                 String version = split[2];
 
                 flags.method = RequestFlags.Method.fromString(method);
 
                 if (flags.method == RequestFlags.Method.NULL) {
                     new ErrorResponse(ErrorType.NotImplemented).writeResponse(flags).flush(out);
 
                     break main;
                 }
 
                 if (!version.split("/")[1].equalsIgnoreCase("1.0") && !version.split("/")[1].equalsIgnoreCase("1.1")) {
                     new ErrorResponse(ErrorType.HTTPVersionNotSupported).writeResponse(flags).flush(out);
 
                     break main;
                 }
 
                 location = URLDecoder.decode(location, "UTF-8");
 
                 if (location.startsWith("/GENERATED/")) {
                     if (location.startsWith("/GENERATED/LOGIN/")) {
                         String[] sepperated = location.substring(11 + 6).split("/");
 
                         new LoginResponse(sepperated[0], sepperated[1]).writeResponse(flags).flush(out);
 
                         break main;
                     }
                     if (location.startsWith("/GENERATED/LIST/")) {
                         String[] sepperated = location.substring(11 + 5).split("/");
 
                         if (ThreadWebserver.instance.sessionAllowed(sepperated[0])) {
                             new ListResponse(sepperated[1]).writeResponse(flags).flush(out);
 
                             break main;
                         }
                         else {
                             new ErrorResponse(ErrorType.Forbidden).writeResponse(flags).flush(out);
 
                             break main;
                         }
                     }
                     if (location.startsWith("/GENERATED/REPORTER/")) {
                         String[] sepperated = location.substring(11 + 9).split("/");
 
                         if (ThreadWebserver.instance.sessionAllowed(sepperated[0])) {
                             new ReporterResponse(sepperated[1], sepperated[2]).writeResponse(flags).flush(out);
 
                             break main;
                         }
                         else {
                             new ErrorResponse(ErrorType.Forbidden).writeResponse(flags).flush(out);
 
                             break main;
                         }
                     }
                     if (location.startsWith("/GENERATED/REPORTED/")) {
                         String[] sepperated = location.substring(11 + 9).split("/");
 
                         if (ThreadWebserver.instance.sessionAllowed(sepperated[0])) {
                             new ReportedResponse(sepperated[1], sepperated[2]).writeResponse(flags).flush(out);
 
                             break main;
                         }
                         else {
                             new ErrorResponse(ErrorType.Forbidden).writeResponse(flags).flush(out);
 
                             break main;
                         }
                     }
                     if (location.startsWith("/GENERATED/REPORT/")) {
                         String[] sepperated = location.substring(11 + 7).split("/");
 
                         if (ThreadWebserver.instance.sessionAllowed(sepperated[0])) {
                             new ReportResponse(sepperated[0], sepperated[1], sepperated[2], sepperated[3], sepperated[4], sepperated[5]).writeResponse(flags).flush(out);
 
                             break main;
                         }
                         else {
                             new ErrorResponse(ErrorType.Forbidden).writeResponse(flags).flush(out);
 
                             break main;
                         }
                     }
                 }
                 else {
                     while (!(location.indexOf("..") < 0)) {
                         location = location.replaceAll("..", ".");
                     }
 
                     if (location.endsWith("/")) {
                         location = location.concat("index.htm");
                     }
 
                     File root = new File(ModeratorGui.instance.getDataFolder(), "web");
                     File file = new File(root, location).getAbsoluteFile();
 
                     if (file.isDirectory()) {
                         new ErrorResponse(ErrorType.Forbidden).writeResponse(flags).flush(out);
 
                         break main;
                     }
                     else if (file.exists()) {
                         new FileResponse(file).writeResponse(flags).flush(out);
 
                         break main;
                     }
                     else {
                         new ErrorResponse(ErrorType.NotFound).writeResponse(flags).flush(out);
 
                         break main;
                     }
                 }
             }
             catch (WebGuiException ex) {
                 try {
                     ex.response.writeResponse(flags).flush(out);
                 }
                 catch (IOException e) {
                    e.printStackTrace();
                 }
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
             catch (RuntimeException ex) {
                 if (!ex.getMessage().equalsIgnoreCase("break")) {
                    ex.printStackTrace();
                 }
 
                 try {
                     new ErrorResponse(ErrorType.InternalServerError).writeResponse(flags).flush(out);
                 }
                 catch (IOException e) {}
             }
             finally {
                 try {
                     out.close();
                     in.close();
                     socket.close();
                     return;
                 }
                 catch (IOException e) {}
             }
         }
 
         try {
             out.close();
             in.close();
             socket.close();
             return;
         }
         catch (IOException e) {}
     }
 }
