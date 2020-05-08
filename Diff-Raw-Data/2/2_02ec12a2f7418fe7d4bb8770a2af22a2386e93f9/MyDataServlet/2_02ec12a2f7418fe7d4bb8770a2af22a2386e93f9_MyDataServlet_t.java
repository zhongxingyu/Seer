 package main.java.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.*;
 
 import java.util.List;
 import java.util.ArrayList;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 @WebServlet(
     name = "MyDataServlet",
     urlPatterns = {"/mydata"}
 )
 public class MyDataServlet extends HttpServlet {
 
     // Database Connection
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String user_id = request.getParameter("user_id");
 
         UserData userData = getData(user_id);
         //Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
         Gson gson = new Gson();
         String json =  gson.toJson(userData);
         //System.out.println(json);
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         response.getWriter().write(json);
 
     }
 
     private UserData getData(String user_id) {
         UserData data = new UserData(user_id);
         List<Hub> hubs = data.hubs;
         try {
             Connection connection = DbManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM hubs WHERE users_user_id = '" + user_id + "'");
             while (rs.next()) {
                 Integer hub_id = rs.getInt("hub_id");
                 String name = rs.getString("name");
                 String api_key = rs.getString("api_key");
                 Integer pan_id = rs.getInt("pan_id");
                hubs.add(new Hub(hub_id, api_key, name, pan_id));
             }
             rs.close();
             stmt.close();
             List<Node> nodes = new ArrayList<Node> ();
             for (Hub hub:hubs) {
                 stmt = connection.createStatement();
                 rs = stmt.executeQuery("SELECT * FROM nodes WHERE hubs_hub_id = '" + hub.hub_id + "'");
                 while (rs.next()) {
                     Integer node_id = rs.getInt("node_id");
                     String address = rs.getString("address");
                     String name = rs.getString("name");
                     String type = rs.getString("type");
                     Node node = new Node(node_id, address, name, type);
                     hub.nodes.add(node);
                     nodes.add(node);
                 }
                 rs.close();
                 stmt.close();
             }
             List<Pin> pins = new ArrayList<Pin> ();
             for (Node node:nodes) {
                 stmt = connection.createStatement();
                 rs = stmt.executeQuery("SELECT * FROM pins WHERE nodes_node_id = '" + node.node_id + "'");
                 while (rs.next()) {
                     Integer pin_id = rs.getInt("pin_id");
                     String data_type = rs.getString("data_type");
                     String name = rs.getString("name");
                     Pin pin = new Pin(pin_id, data_type, name);
                     node.pins.add(pin);
                     pins.add(pin);
                 }
                 rs.close();
                 stmt.close();
             }
 
             for (Pin pin:pins) {
                 stmt = connection.createStatement();
                 rs = stmt.executeQuery("SELECT * FROM tags WHERE pins_pin_id = '" + pin.pin_id + "'");            
                 while (rs.next()) {
                     String tag = rs.getString("tag");
                     Tag t = new Tag(tag);
                     pin.tags.add(t);
                 }
                 rs.close();
                 stmt.close();
             }
             
             for (Pin pin:pins) {
                 stmt = connection.createStatement();
                 rs = stmt.executeQuery("SELECT * FROM pin_data WHERE pins_pin_id = '" + pin.pin_id + "' ORDER BY time");
                 while (rs.next()) {
                     Timestamp time = rs.getTimestamp("time");
                     String pin_type = rs.getString("pin_type");
                     String pin_value = rs.getString("pin_value");
                     PinData pinData = new PinData(time, pin_type, pin_value);
                     pin.pin_data.add(pinData);
                 }       
             }
         } catch (SQLException e) {
             return null;
         } catch (URISyntaxException e) {
             return null;
         }
         return data;
     }
     
     class UserData {
         public String user_id;
         public List<Hub> hubs = new ArrayList<Hub>();
         public UserData(String user_id) {
             this.user_id = user_id;
         }
     }
 
     class Hub {
         public Integer hub_id;
         public String api_key;
         public String name;
         public Integer pan_id;
 
         public List<Node> nodes = new ArrayList<Node>();
         public Hub(Integer hub_id, String api_key, String name, Integer pan_id) {
             this.hub_id = hub_id;
             this.api_key = api_key;
             this.name = name;
             this.pan_id = pan_id;
         }
     }
     
     class Node {
         public Integer node_id;
         public String address;
         public String name;
         public String type;
         public List<Pin> pins = new ArrayList<Pin> ();
         public Node(Integer node_id, String address, String name, String type) {
             this.node_id = node_id; 
             this.address = address;
             this.name = name;
             this.type = type;
         }
     }
     
     class Pin {
         public Integer pin_id;
         public String data_type;
         public String name;
         public List<Tag> tags = new ArrayList<Tag> ();
         public List<PinData> pin_data = new ArrayList<PinData> ();
         public Pin(Integer pin_id, String data_type, String name) {
             this.pin_id = pin_id;            
             this.data_type = data_type;
             this.name = name;
         }
     }
 
     class Tag {
         public String tag;
         public Tag(String tag) {
             this.tag = tag;
         }
     }
     
     class PinData {
         public Timestamp time;
         public String pin_value;
         public String pin_type;
 
         public PinData(Timestamp time, String pin_value, String pin_type) {
             this.time = time;            
             this.pin_value = pin_value;
             this.pin_type = pin_type; 
         }
     }
 };
 
