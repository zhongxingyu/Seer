 package com.ibm.opensocial.landos;
 
 import org.apache.wink.json4j.JSONException;
 import org.apache.wink.json4j.JSONWriter;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class OrdersServlet extends BaseServlet {
   /**
   * GET /orders/runid[?user=<user>[&item=<bar>]]
    * @throws IOException 
    */
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
     // Set headers
     setCacheAndTypeHeaders(res);
     
     // Get the run id
     int rid = Integer.parseInt(getPathSegment(req, 0));
     
     // Get the user and item parameters (if any)
     String user = req.getParameter("user");
     String item = req.getParameter("item");
     
     // Prepare database variables
     Connection conn = null;
     PreparedStatement stmt = null;
     ResultSet results = null;
     
     // Writer
     JSONWriter writer = getJSONWriter(res);
     
     // Query
     String query = "SELECT * FROM orders WHERE rid = ?";
     
     try {
       conn = getDataSource(req).getConnection();
       // Parameter control forks
       if (user == null) {
         // Neither user nor item are set
         stmt = conn.prepareStatement(query);
         stmt.setInt(1, rid);
       } else {
         // User is set
         query += " AND uid = ?";
         int uid = Integer.parseInt(user);
         if (item == null) {
           // Item is set
           query += " AND item = ?";
           stmt = conn.prepareStatement(query);
           stmt.setInt(1, rid);
           stmt.setInt(2, uid);
           stmt.setString(3, item);
         } else {
           // Only user is set
           stmt = conn.prepareStatement(query);
           stmt.setInt(1, rid);
           stmt.setInt(2, uid);
         }
       }
       // Prepared statement is now ready for execution
       results = stmt.executeQuery();
       writer.array();
       while (results.next()) {
         writeJSONObjectOrder(writer, results.getInt(1), results.getString(2), results.getString(3), results.getInt(4), results.getInt(5), results.getString(6));
       }
       writer.endArray();
     } catch (Exception e) {
      //TODO Set up logging
      e.printStackTrace();
     } finally {
       close(writer, conn);
     }
   }
   
   /**
   * DELETE /orders/runid?user=<user>&item=<bar>
    * @throws IOException 
    */
   @Override
   public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
     // Set headers
     setCacheAndTypeHeaders(res);
     
     // Get the user and item parameters
     String user = req.getParameter("user");
     String item = req.getParameter("item");
     
     // Writer
     JSONWriter writer = getJSONWriter(res);
     
     // Check for required parameters
     if (numSegments(req) < 1 || user == null || item == null) {
       try {
         writer.object().key("error").value("Deleting requires a run id, user id, and an item.");
       } catch (Exception e) {
        // TODO Set up logging
        e.printStackTrace();
       } finally {
         close(writer);
       }
       return;
     }
     
     // Get the run id
     int rid = Integer.parseInt(getPathSegment(req, 0));
     
     // Prepare database variables
     Connection conn = null;
     PreparedStatement stmt = null;
     
     // Query
     String query = "SELECT * FROM orders WHERE rid = ? AND uid = ? AND item = ?";
     
     try {
       conn = getDataSource(req).getConnection();
       stmt = conn.prepareStatement(query);
      // Prepared statement is now ready for execution
      int result = stmt.executeUpdate();
       stmt.setInt(1, rid);
       stmt.setInt(2, Integer.parseInt(user));
       stmt.setString(3, item);
       writer.object();
       writer.key("delete").value(result);
       writer.endObject();
     } catch (Exception e) {
      //TODO Set up logging
      e.printStackTrace();
     } finally {
       close(writer, conn);
     }
   }
   
   /**
    * Writes the values of an order to a JSON object
    * @param writer
    * @param uid
    * @param item
    * @param size
    * @param qty
    * @param price
    * @param comments
    * @throws JSONException 
    * @throws IOException 
    * @throws NullPointerException 
    * @throws IllegalStateException 
    */
   private void writeJSONObjectOrder(JSONWriter writer, int uid, String item, String size, int qty, int price, String comments) throws IllegalStateException, NullPointerException, IOException, JSONException {
     writer.object();
     writer.key("uid").value(uid);
     writer.key("item").value(item);
     writer.key("size").value(size);
     writer.key("qty").value(qty);
     writer.key("price").value(price);
     writer.key("comments").value(comments);
     writer.endObject();
   }
 }
