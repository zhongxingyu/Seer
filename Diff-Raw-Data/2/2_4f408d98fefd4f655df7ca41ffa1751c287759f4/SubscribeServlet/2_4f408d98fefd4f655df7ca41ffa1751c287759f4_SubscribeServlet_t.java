 package com.ibm.opensocial.landos;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
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
 
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     resp.setHeader("CACHE-CONTROL", "no-cache");
     resp.setContentType("application/json");
     
     String user = getActionUser(req);
     JSONWriter writer = new JSONWriter(resp.getWriter());
     try {
       writer.object()
         .key("id").value(user)
         .key("subscribed").value(isSubscribed(user))
       .endObject();
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doGet", e.getMessage(), e);
       throw new ServletException(e);
     } finally {
       writer.close();
     }
   }
   
   @Override
   protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     resp.setHeader("CACHE-CONTROL", "no-cache");
     resp.setContentType("application/json");
 
     String user = getActionUser(req);
     JSONWriter writer = new JSONWriter(resp.getWriter());
     try {
       boolean isSubscribed = isSubscribed(user) || subscribe(user);
       
       writer.object()
         .key("id").value(user)
         .key("subscribed").value(isSubscribed)
       .endObject();
       resp.setStatus(isSubscribed ? 200 : 500);
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doGet", e.getMessage(), e);
       throw new ServletException(e);
     } finally {
       writer.close();
     }
   }
   
   @Override
   protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     resp.setHeader("CACHE-CONTROL", "no-cache");
     resp.setContentType("application/json");
     
     String user = getActionUser(req);
     JSONWriter writer = new JSONWriter(resp.getWriter());
     try {
       boolean isSubscribed = isSubscribed(user) && unsubscribe(user);
       
       writer.object()
         .key("id").value(user)
         .key("subscribed").value(isSubscribed)
       .endObject();
      resp.setStatus(isSubscribed ? 500 : 200);
     } catch (Exception e) {
       LOGGER.logp(Level.SEVERE, CLAZZ, "doGet", e.getMessage(), e);
       throw new ServletException(e);
     } finally {
       writer.close();
     }
   }
   
   private boolean subscribe(String user) throws SQLException {
     if (user != null) {
       Connection connection = null;
       PreparedStatement stmt = null;
       try {
         connection = dbSource.getConnection();
         stmt = connection.prepareStatement("INSERT INTO `subscribed` VALUES(?)");
         stmt.setString(1, user);
         stmt.executeUpdate();
       } finally {
         close(stmt, connection);
       }
     }
     return isSubscribed(user);
   }
   
   private boolean unsubscribe(String user) throws SQLException {
     if (user != null) {
       Connection connection = null;
       PreparedStatement stmt = null;
       try {
         connection = dbSource.getConnection();
         stmt = connection.prepareStatement("DELETE FROM `subscribed` WHERE `user`=?");
         stmt.setString(1, user);
         stmt.executeUpdate();
       } finally {
         close(stmt, connection);
       }
     }
     return isSubscribed(user);
   }
   
   private boolean isSubscribed(String user) throws SQLException {
     boolean ret = false;
     if (user != null) {
       Connection connection = null;
       PreparedStatement stmt = null;
       ResultSet result = null;
       try {
         connection = dbSource.getConnection();
         stmt = connection.prepareStatement("SELECT COUNT(*) from `subscribed` WHERE `user`=?");
         stmt.setString(1, user);
         result = stmt.executeQuery();
         if (result.first()) {
           ret = result.getInt(1) > 0;
         }
       } finally {
         close(result, stmt, connection);
       }
     }
     return ret;
   }
   
   private String getActionUser(HttpServletRequest req) throws UnsupportedEncodingException {
     String user = URLDecoder.decode(req.getPathInfo(), "UTF-8");
     if (user.startsWith("/"))
       user = user.substring(1);
     return user;
   }
 }
 
