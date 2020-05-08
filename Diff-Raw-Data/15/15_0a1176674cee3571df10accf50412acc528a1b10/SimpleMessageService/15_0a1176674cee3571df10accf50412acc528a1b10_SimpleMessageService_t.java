package org.skyblue.sms;

 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 import java.io.ObjectOutputStream;
 import java.io.ObjectInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.ClassNotFoundException;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 public class SimpleMessageService extends AbstractHandler {
     static public final String FILE_MSG_QUEUE = "/Users/amitjoshi/programming/java/msgQueue.ser";
     private MessageQueue msgQueue;
 
     public SimpleMessageService() {
         try {
             ObjectInputStream input = new ObjectInputStream(new FileInputStream(FILE_MSG_QUEUE));
             msgQueue = (MessageQueue) input.readObject();
             if (msgQueue == null) {
                 msgQueue = new MessageQueue();
             }
         } catch(IOException ex) {
             msgQueue = new MessageQueue();
         } catch(ClassNotFoundException ex) {
             msgQueue = new MessageQueue();
         }
     }
 
     public void handle(String target,
             Request baseRequest,
             HttpServletRequest request,
             HttpServletResponse response) 
         throws IOException, ServletException {
 
         final String requestURI = request.getRequestURI();
         if (! requestURI.startsWith("/sms/")) {
             sendError(baseRequest, response, null);
             baseRequest.setHandled(true);
             return;
         }
 
         final String msg = request.getParameter("msg");
         final String topic = request.getParameter("topic");
         String msgAction = requestURI.replace("/sms/","");
         msgAction = msgAction.replace("/","");
 
         try {
             if (msgAction.equals("send")) {
                 msgQueue.send(topic, msg);
                 final String respBody = "Msg = " + msg + " : queued for topic = " + topic;
                 sendResponse(baseRequest, response, respBody);
             } else if (msgAction.equals("receive")) {
                 final String respBody = msgQueue.receive(topic);
                 sendResponse(baseRequest, response, respBody);
             } else {
                 sendError(baseRequest, response, "Unknown Action");
             }
         } catch(Exception e) {
             sendError(baseRequest, response, "Exception in execution of action - " + e.getMessage());
         }
         
     }
 
     private void sendResponse(Request baseRequest,  HttpServletResponse response, String msg) throws IOException {
         response.setContentType("text/html;charset=utf-8");
         response.setStatus(HttpServletResponse.SC_OK);
         response.getWriter().println(msg);
         baseRequest.setHandled(true);
     }
 
     private void sendError(Request baseRequest, HttpServletResponse response, String error) throws IOException {
         response.setContentType("text/html;charset=utf-8");
         response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         if (error == null) {
             response.getWriter().println("<h1>Bad Request</h1>");
         } else{
             response.getWriter().println(error);
         }
         baseRequest.setHandled(true);
     }
 
     public void saveQueue() {
         try {
             if (msgQueue != null) {
                 FileOutputStream fileOut = new FileOutputStream(FILE_MSG_QUEUE);
                 ObjectOutputStream out = new ObjectOutputStream(fileOut);
                 out.writeObject(msgQueue);
                 out.close();
                 fileOut.close();
                 System.out.println("Object serialized in " + FILE_MSG_QUEUE);
             }
         } catch(IOException i) {
             i.printStackTrace();
         }
     }
 
     public static void main(String[] args) throws Exception {
         final Server server = new Server(5080);
         final SimpleMessageService sms = new SimpleMessageService();
         server.setHandler(sms);
 
         System.out.println("Starting server");
         server.start();
 
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
             @Override
             public void run() {
                 System.out.println("Shutdown hook");
                 if(server.isStarted()) {
                     try {
                         sms.saveQueue();
                         server.stop();
                     } catch (Exception exception) {
                         exception.printStackTrace();
                     }
                 }
             }
         },"Stop Jetty Hook"));
 
         server.join();
 
     }
 }
