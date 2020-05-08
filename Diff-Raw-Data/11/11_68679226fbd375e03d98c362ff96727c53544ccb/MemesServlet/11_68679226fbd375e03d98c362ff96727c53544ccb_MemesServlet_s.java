 package com.epam.memegen;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
 import com.google.gson.stream.JsonWriter;
 
 @SuppressWarnings("serial")
 public class MemesServlet extends HttpServlet {
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     Query q = new Query("Meme");
     q.addSort("__key__", SortDirection.DESCENDING);
     PreparedQuery prepared = datastore.prepare(q);
     Iterable<Entity> iterable = prepared.asIterable();
 
     resp.setContentType("application/json");
 
     PrintWriter writer = resp.getWriter();
     JsonWriter w = new JsonWriter(writer);
     w.beginArray();
     for (Entity entity : iterable) {
       Util.memeToJson(entity, w);
     }
     w.endArray();
     w.close();
   }
 }
