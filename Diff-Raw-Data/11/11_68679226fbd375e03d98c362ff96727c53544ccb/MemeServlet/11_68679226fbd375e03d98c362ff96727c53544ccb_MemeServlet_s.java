 package com.epam.memegen;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.gson.stream.JsonWriter;
 
 @SuppressWarnings("serial")
 public class MemeServlet extends HttpServlet {
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     resp.setContentType("application/json");
 
     String idStr = req.getPathInfo().replaceAll("[^0-9]+", "");
     if (idStr.equals("")) {
       resp.sendError(404);
       return;
     }
     long id = Long.valueOf(idStr);
 
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     Key key = KeyFactory.createKey("Meme", id);
     Entity entity;
     try {
       entity = datastore.get(key);
       Util.memeToJson(entity, new JsonWriter(resp.getWriter()));
     } catch (EntityNotFoundException e) {
       resp.sendError(404);
     }
   }
 }
