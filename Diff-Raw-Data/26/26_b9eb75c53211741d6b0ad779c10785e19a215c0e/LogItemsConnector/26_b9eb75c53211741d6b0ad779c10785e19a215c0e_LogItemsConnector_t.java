 package com.scurab.gwt.rlw.server.data.web;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URLDecoder;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.hibernate.Session;
 
 import com.scurab.gwt.rlw.server.Application;
 import com.scurab.gwt.rlw.server.data.Database;
 import com.scurab.gwt.rlw.shared.model.LogItem;
 import com.scurab.gwt.rlw.shared.model.LogItemBlob;
 import com.scurab.gwt.rlw.shared.model.LogItemBlobRequest;
 import com.scurab.gwt.rlw.shared.model.LogItemBlobRespond;
 import com.scurab.gwt.rlw.shared.model.LogItemRespond;
 import com.scurab.gwt.rlw.shared.model.Respond;
 
 /**
  * Servlet for handling LogItems events<br/>
  * Supported methods:<br/>
  * <code>POST</code> - Save logItem<br/>
  * <code>PUT</code> - Put save blob
  *  
  * @author Joe Scurab
  *
  */
 public class LogItemsConnector extends DataConnector<LogItem> {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     @Override
     protected Respond<?> onPostRequest(Session s, InputStream is) throws Exception {
         LogItemRespond response = null;
         String json = read(is);
         //parse
         LogItem[] data = parse(json, json.charAt(0) == '[');
         LogItem[] saved = onWrite(s, data);
         //save
         if (saved.length == 0) {
             response = new LogItemRespond("Nothing saved!?", 0);
         } else if (saved.length == 1) {
             response = new LogItemRespond(saved[0]);
         } else {
             response = new LogItemRespond(data.length);
         }
         return response;
     }
 
     @Override
     public Class<?> getGenericClass() {
         return LogItem.class;
     }
 
     @Override
     public Class<?> getArrayGenericClass() {
         return LogItem[].class;
     }
 
     @Override
     protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         Respond<?> respond = null;
         try {            
             respond = onPutRequest(req);
         } catch (Exception e) {
            e.printStackTrace();
             respond = new LogItemBlobRespond(e);            
         } finally {
             String r = Application.toJson(respond);
             resp.getOutputStream().write(r.getBytes());
             resp.getOutputStream().close();
         }
     }
 
     protected LogItemBlobRespond onPutRequest(HttpServletRequest req) throws IOException{
         String reqJson = URLDecoder.decode(req.getQueryString(),"UTF-8");
         LogItemBlobRequest libr = Application.fromJson(reqJson, LogItemBlobRequest.class);
 
         //check id
         int id = libr.getLogItemID();
         if(id == 0){
             throw new IllegalArgumentException("LogItemID is 0!");
         }
         //check filename
         String fileName = libr.getFileName();
         if(fileName == null || fileName.length() == 0){
             fileName = "" + id;
         }
         //check mime
         String mime = libr.getMimeType();
         if(mime == null || mime.length() == 0){
             mime = "application/octet-stream";
         }
 
         int written = onWrite(id, mime, fileName, req.getInputStream());
         LogItemBlobRespond res = new LogItemBlobRespond(libr, written);
         return res;
     }
 
     private LogItemBlobRequest parse(InputStream is) throws IOException{
         StringBuilder sb = new StringBuilder();
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
         String line = null;
 
         while((line = br.readLine()) != null){
             if(sb.length() != 0 && line.length() == 0){//second empty line                
                 break;
             }
             sb.append(line);
             if(sb.length() > 1024){
                 throw new IllegalArgumentException("Data for json > 1024?! => wrong request");
             }
         }
         LogItemBlobRequest item = Application.fromJson(sb.toString(), LogItemBlobRequest.class);
         return item;
     }
 
     private byte[] getData(InputStream is) throws IOException{
         byte[] bytes = IOUtils.toByteArray(is);
         return bytes;
     }
 
     /** save blob **/
     private int onWrite(int logItemId, String mime, String fileName, InputStream is) throws IOException{
         Session s = Database.openSession();
         //get LogItem
         LogItem li = null;
         Exception err = null;
         try{
             li = (LogItem) s.get(LogItem.class, logItemId);
         }
         catch(Exception e){
             e.printStackTrace();
             err = e;
         }
         if(li == null){
             throw new IllegalArgumentException(String.format("LogItem with ID:%s not found!", logItemId), err);
         }
 
         byte[] data = getData(is);
 
         li.setBlobMime(mime);
         LogItemBlob lib = new LogItemBlob(logItemId, data, fileName);
 
         //save data to DB
         s.beginTransaction();
         s.update(li);
         s.save(lib);
         s.getTransaction().commit();
 
         return data.length;
     }
 }
