 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.totsp.gwittir.service;
 
 import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.server.rpc.RPC;
 import com.google.gwt.user.server.rpc.RPCRequest;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.google.gwt.user.server.rpc.SerializationPolicy;
 
 import com.totsp.gwittir.client.stream.StreamServiceIterator;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import java.net.URLEncoder;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 
 /**
  *
  * @author kebernet
  */
 public class StreamServiceServlet extends RemoteServiceServlet {
     /**
          *
          */
     private static final long serialVersionUID = 9185972219157797813L;
     public static final String SCRIPT_CLOSE = "</script>";
     public static final String UTF8 = "UTF-8";
     public static final String WINDOW_PARENT = "window.parent.";
     protected static final String SCRIPT_OPEN = "<script type=\"text/javascript\">";
 
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response){
         System.out.println("Termination Request.");
         request.getSession(true).removeAttribute(request.getParameter("c"));
     }
 
     @Override
     public String processCall(String payload) throws SerializationException {
         try {
             RPCRequest rpcRequest = StreamServiceUtils.decodeRequest(
                     payload,
                     this.getClass(),
                     this);
 
             //onAfterRequestDeserialized(rpcRequest);
             StreamServiceIterator iterator = null;
 
             try {
                 iterator = (StreamServiceIterator) rpcRequest.getMethod()
                                                              .invoke(
                         this,
                         rpcRequest.getParameters());
 
                 try {
                     this.sendResults(
                         rpcRequest.getMethod(),
                         iterator,
                         rpcRequest.getSerializationPolicy());
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 } finally {
                     if (iterator != null) {
                         iterator.close();
                     }
                 }
             } catch (IllegalArgumentException ex) {
                 throw new IncompatibleRemoteServiceException("", ex);
             } catch (InvocationTargetException ex) {
                 throw new IncompatibleRemoteServiceException("", ex);
             } catch (IllegalAccessException ex) {
                 throw new IncompatibleRemoteServiceException("", ex);
             }
 
             return "";
         } catch (IncompatibleRemoteServiceException ex) {
             log("An IncompatibleRemoteServiceException was thrown while processing this call.", ex);
 
             return RPC.encodeResponseForFailure(null, ex);
         }
     }
 
     @Override
     public String readContent(HttpServletRequest request) {
         return request.getParameter("r");
     }
 
     protected boolean flushExtraAfterEachPush() {
         return true;
     }
 
     @Override
     protected boolean shouldCompressResponse(HttpServletRequest request, HttpServletResponse response, String responsePayload) {
         return false;
     }
 
     private void sendResults(Method method, StreamServiceIterator iterator, SerializationPolicy policy) {
         try {
             HttpServletResponse response = this.getThreadLocalResponse();
             HttpServletRequest request = this.getThreadLocalRequest();
             HttpSession session = request.getSession(true);
             session.setAttribute(
                 request.getParameter("c"),
                 Boolean.TRUE);
             response.setContentType("text/html");
 
             PrintWriter out = response.getWriter();
             out.print("<html>");
 
             while (iterator.hasNext()) {
                 try {
                     Object next = iterator.next();
                     String obj = StreamServiceUtils.encodeResponse(
                             next.getClass(),
                             next,
                             false,
                             policy);
 
                     System.out.println("Encoded response: " + obj);
                     out.println(SCRIPT_OPEN);
                     out.print(WINDOW_PARENT);
                     out.print(request.getParameter("c"));
                     out.print("(\"");
                     out.print(URLEncoder.encode(obj, UTF8).replaceAll("\\+", "%20"));
                     out.println("\");");
                     out.println(SCRIPT_CLOSE);
 
                     if (this.flushExtraAfterEachPush()) {
                         out.print("<!--");
 
                         for (int i = 0; i < 512; i++)
                             out.print(" ");
 
                         out.println("-->");
                     }
 
                     out.flush();
 
                     if (session.getAttribute(request.getParameter("c")) == null) {
                         System.out.println("Terminated on request.");
                         break;
                     }
                 } catch (SerializationException se) {
                     out.println(SCRIPT_OPEN);
                     out.print(WINDOW_PARENT);
                     out.print(request.getParameter("c"));
                     out.print("E(\"");
                     out.print(URLEncoder.encode(
                             se.toString(),
                             UTF8).replaceAll("\\+", "%20"));
                     out.println("\");");
                     out.println(SCRIPT_CLOSE);
                     out.flush();
                     this.log("Serialization exception", se);
                 }
             }
 
             out.print(SCRIPT_OPEN);
             out.print(WINDOW_PARENT);
             out.print(request.getParameter("c"));
             out.println("C();");
             out.println(SCRIPT_CLOSE);
            
         } catch (IOException ioe) {
             throw new RuntimeException(ioe);
         }
     }
 }
