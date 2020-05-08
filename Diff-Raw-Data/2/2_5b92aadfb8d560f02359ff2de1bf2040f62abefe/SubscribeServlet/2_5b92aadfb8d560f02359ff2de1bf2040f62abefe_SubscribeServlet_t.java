 package com.ibm.opensocial.landos;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.wink.json4j.JSONWriter;
 
 public class SubscribeServlet extends BaseServlet {
   private static final long serialVersionUID = 8636321669435402465L;
   private static final String CLAZZ = SubscribeServlet.class.getName();
   private static final Logger LOGGER = Logger.getLogger(CLAZZ);
   
   public static final String ACTION_USER = "com.ibm.opensocial.landos.servlets.actionuser";
 
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     resp.setHeader("Cache-Control", "no-cache");
     resp.setContentType("application/json");
     
     JSONWriter writer = new JSONWriter(resp.getWriter());
     try {
       writer.object()
         .key("id").value(getActionUser(req))
         .key("subscribed").value(isSubscribed(req))
       .endObject();
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doGet", e.getMessage(), e);
       throw new ServletException(e);
     } finally {
       close(writer);
     }
   }
   
   @Override
   protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     resp.setHeader("Cache-Control", "no-cache");
     resp.setContentType("application/json");
 
     JSONWriter writer = new JSONWriter(resp.getWriter());
     try {
       boolean isSubscribed = isSubscribed(req) || subscribe(req);
       
       writer.object()
         .key("id").value(getActionUser(req))
         .key("subscribed").value(isSubscribed)
       .endObject();
       resp.setStatus(isSubscribed ? 200 : 500);
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doGet", e.getMessage(), e);
       throw new ServletException(e);
     } finally {
       close(writer);
     }
   }
   
   @Override
   protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     resp.setHeader("Cache-Control", "no-cache");
     resp.setContentType("application/json");
     
     JSONWriter writer = new JSONWriter(resp.getWriter());
     try {
       boolean isSubscribed = isSubscribed(req) && unsubscribe(req);
       
       writer.object()
         .key("id").value(getActionUser(req))
         .key("subscribed").value(isSubscribed)
       .endObject();
       resp.setStatus(isSubscribed ? 500 : 200);
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doGet", e.getMessage(), e);
       throw new ServletException(e);
     } finally {
       close(writer);
     }
   }
   
   private boolean subscribe(HttpServletRequest req) throws Exception {
     String user = getActionUser(req);
     if (!"".equals(user)) {
       Connection connection = null;
       PreparedStatement stmt = null;
       try {
         connection = getDataSource(req).getConnection();
        stmt = connection.prepareStatement("INSERT INTO `subscribed` (`user`) VALUES(?)");
         stmt.setString(1, user);
         stmt.executeUpdate();
       } finally {
         close(stmt, connection);
       }
     }
     return isSubscribed(req);
   }
   
   private boolean unsubscribe(HttpServletRequest req) throws Exception {
     String user = getActionUser(req); // user can not be empty here.
     Connection connection = null;
     PreparedStatement stmt = null;
     try {
       connection = getDataSource(req).getConnection();
       stmt = connection.prepareStatement("DELETE FROM `subscribed` WHERE `user`=?");
       stmt.setString(1, user);
       stmt.executeUpdate();
     } finally {
       close(stmt, connection);
     }
     return isSubscribed(req);
   }
   
   private boolean isSubscribed(HttpServletRequest req) throws Exception {
     String user = getActionUser(req);
     boolean ret = false;
     if (!"".equals(user)) {
       Connection connection = null;
       PreparedStatement stmt = null;
       ResultSet result = null;
       try {
         connection = getDataSource(req).getConnection();
         stmt = connection.prepareStatement("SELECT COUNT(*) from `subscribed` WHERE `user`=?");
         stmt.setString(1, user);
         result = stmt.executeQuery();
         result.first(); // Should never return no rows.
         ret = result.getInt(1) > 0;
       } finally {
         close(result, stmt, connection);
       }
     }
     return ret;
   }
   
   private String getActionUser(HttpServletRequest req) throws UnsupportedEncodingException {
     String user = (String)req.getAttribute(ACTION_USER);
     if (user == null) {
       user = getPathSegment(req, 0);
       if (user == null)
         user = "";
       else if (user.startsWith("/"))
         user = user.substring(1);
       req.setAttribute(ACTION_USER, user.trim());
     }
     return user;
   }
 }
 
