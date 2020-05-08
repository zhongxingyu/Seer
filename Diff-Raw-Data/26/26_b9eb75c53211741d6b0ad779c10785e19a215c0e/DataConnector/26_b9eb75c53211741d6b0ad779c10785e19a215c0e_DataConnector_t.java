 package com.scurab.gwt.rlw.server.data.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.lang.reflect.Array;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.hibernate.Session;
 
 import com.scurab.gwt.rlw.server.Application;
 import com.scurab.gwt.rlw.server.data.Database;
 import com.scurab.gwt.rlw.shared.model.Respond;
 
 /**
  * Class for base Post handling
  * @author Joe Scurab
  *
  * @param <T>
  */
 public abstract class DataConnector<T> extends Connector {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1009792762610851131L;
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         doNotImplementedRequest("GET",req,resp);
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         Respond<?> respond = null;
         Session s = null;
         try {
             s = Database.openSession();
             respond = onPostRequest(s, req.getInputStream());             
         } catch (Exception e) {
            e.printStackTrace();
             respond = new Respond<Void>(e);
         } finally {            
             String result = Application.toJson(respond);
             resp.getOutputStream().write(result.getBytes());
             resp.getOutputStream().close();
             if(s != null && s.isOpen()){
                 s.close();
             }
         }
     }
 
     @Override
     protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         doNotImplementedRequest("PUT",req,resp);
     }
 
     protected Respond<?> onPostRequest(Session s, InputStream is) throws Exception{
         throw new IllegalStateException("Not overrided onRequestMethod!");
     }
 
     @SuppressWarnings("unchecked")
     //    protected abstract T[] parse(String obj, boolean isArray);       
 
     protected T[] parse(String obj, boolean isArray) {
         T[] toWrite = null;
         if (isArray) {
             toWrite = (T[]) Application.fromJson(obj, getArrayGenericClass());
         } else {
             T d = (T) Application.fromJson(obj, getGenericClass());   
             toWrite = (T[]) Array.newInstance(getGenericClass(),1);
             toWrite[0] = d;
         }
         return toWrite;
     }
 
     public abstract Class<?> getGenericClass();
 
     public abstract Class<?> getArrayGenericClass();
 
 
     protected T[] onWrite(Session s, T[] data) {
         s.beginTransaction();
         for (T d : data) {
             s.saveOrUpdate(d);
         }
         s.getTransaction().commit();
         return data;
     }
 
     protected String read(InputStream is) throws IOException {
         StringWriter writer = new StringWriter();
         IOUtils.copy(is, writer, "utf-8");
         String theString = writer.toString();
         return theString;
     }
 
 }
