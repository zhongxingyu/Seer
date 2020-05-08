 package DB;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.Resource;
 import javax.faces.context.FacesContext;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import logikk.AdminMessage;
 import logikk.Dish;
 import logikk.Order;
 import logikk.Status;
 import logikk.StoredOrders;
 import logikk.SubscriptionPlan;
 import logikk.User;
 
 public class Database {
 
     @Resource(name = "jdbc/hc_realm")
     private DataSource ds;
     private Connection connection;
     private String currentUser;
 
     public Database() {
         try {
             Context con = new InitialContext();
             ds = (DataSource) con.lookup("jdbc/hc_realm");
         } catch (NamingException e) {
             System.out.println(e.getMessage());
         }
     }
 
     public ArrayList<Order> getTurnoverstatistics(String query) {
         ArrayList<Order> orders = new ArrayList();
         ResultSet res = null;
         Statement stm = null;
         openConnection();
         try {
             stm = connection.createStatement();
             res = stm.executeQuery(query);
             while (res.next()) {
                 java.sql.Date date = res.getDate("dates");
                 java.sql.Time timeOfDelivery = res.getTime("TIMEOFDELIVERY");
                 String deliveryAddress = res.getString("DELIVERYADDRESS");
                 int status = res.getInt("STATUS");
                 int orderId = res.getInt("ORDERID");
                 double totalPrice = res.getDouble("TOTALPRICE");
                 Order orderToBeAdded = new Order(date, timeOfDelivery, deliveryAddress, status, totalPrice);
                 orderToBeAdded.setOrderId(orderId);
                 orders.add(orderToBeAdded);
             }
         } catch (SQLException e) {
         } finally {
             Cleaner.closeConnection(connection);
             Cleaner.closeResSet(res);
             Cleaner.closeSentence(stm);
         }
         return orders;
     }
 
     public ArrayList<Order> getPendingOrders(String query) {
         ArrayList<Order> orders = new ArrayList();
         ResultSet res = null;
         Statement stm = null;
         openConnection();
         try {
             stm = connection.createStatement();
             res = stm.executeQuery(query);
             while (res.next()) {
                 java.sql.Date date = res.getDate("dates");
                 java.sql.Time timeOfDelivery = res.getTime("TIMEOFDELIVERY");
                 String deliveryAddress = res.getString("DELIVERYADDRESS");
                 int status = res.getInt("STATUS");
                 int orderId = res.getInt("ORDERID");
                 Order orderToBeAdded = new Order(date, timeOfDelivery, deliveryAddress, status);
                 orderToBeAdded.setOrderId(orderId);
                 orderToBeAdded.setPostalcode(res.getInt("postalcode"));
                 orders.add(orderToBeAdded);
             }
         } catch (SQLException e) {
         } finally {
             Cleaner.closeConnection(connection);
             Cleaner.closeResSet(res);
             Cleaner.closeSentence(stm);
         }
         return orders;
     } 
     public ArrayList<Order> initializeDishes(ArrayList order){
         ResultSet res = null;
         Statement stm = null;
         openConnection();
         try {
             stm = connection.createStatement();
             res = stm.executeQuery("SELECT * from dishes_orders");
             while (res.next()) {
                 for(int i = 0; i < order.size();i++){
                     
                 }
             }
         } catch (SQLException e) {
         } finally {
             Cleaner.closeConnection(connection);
             Cleaner.closeResSet(res);
             Cleaner.closeSentence(stm);
         }
         return order;
     }
 
     //FOR ADMIN
     public void updateOrder(Order s) {
         PreparedStatement sqlGet = null;
         PreparedStatement sqlUpdate = null;
         ResultSet res = null;
         openConnection();
         try {
             if (s.getStatusNumeric() == 5) {
                 connection.setAutoCommit(false);
                 sqlGet = connection.prepareStatement("SELECT * FROM dishes_ordered WHERE orderid=?");
                 sqlGet.setInt(1, s.getOrderId());
                 res = sqlGet.executeQuery();
                 StoredOrders storedOrders = new StoredOrders();
                 while (res.next()) {
                     storedOrders.setDishId(res.getInt("dishid"));
                     storedOrders.setDishCount(res.getInt("dishcount"));
                     storedOrders.setSalesmanUsername(res.getString("salesmanusername"));
                 }
                 storedOrders.setOrderId(s.getOrderId());
                 storedOrders.setTotalPrice(s.getTotalprice());
                 storedOrders.setDate(s.getDate());
                 storedOrders.setPostalcode(s.getPostalcode());
                 insertDishesOrdered(storedOrders);
                 deleteOrder(s);
             } else {
                 sqlUpdate = connection.prepareStatement("UPDATE ORDERS set STATUS=? where ORDERID=?");
                 sqlUpdate.setInt(1, s.getStatusNumeric());
                 sqlUpdate.setInt(2, s.getOrderId());
                 sqlUpdate.executeUpdate();
                 connection.commit();
             }
         } catch (Exception e) {
             e.toString();
         } finally {
             Cleaner.closeSentence(sqlGet);
             Cleaner.closeSentence(sqlUpdate);
             Cleaner.closeResSet(res);
             Cleaner.setAutoCommit(connection);
         }
         closeConnection();
     }
 
     private void deleteOrder(Order s) {
         PreparedStatement ps = null;
         try {
             deleteFromDishesOrdered(s);
             ps = connection.prepareStatement("DELETE FROM orders where orderid=?");
             ps.setInt(1, s.getOrderId());
             ps.executeUpdate();
             connection.commit();
         } catch (SQLException ex) {
             ex.toString();
         } finally {
             Cleaner.closeSentence(ps);
         }
     }
 
     private void deleteFromDishesOrdered(Order s) {
         PreparedStatement ps = null;
         try {
             ps = connection.prepareStatement("DELETE FROM dishes_ordered WHERE orderid=?");
             ps.setInt(1, s.getOrderId());
             ps.executeUpdate();
             connection.commit();
         } catch (SQLException ex) {
             ex.toString();
         } finally {
             Cleaner.closeSentence(ps);
         }
     }
 
     private void insertDishesOrdered(StoredOrders s) {
         PreparedStatement ps = null;
         try {
             ps = connection.prepareStatement("INSERT INTO dishes_stored (dishId,orderId,dishCount,totalPrice,dates,postalcode,salesmanusername) VALUES (?,?,?,?,?,?,?)");
             ps.setInt(1, s.getDishId());
             ps.setInt(3, s.getDishCount());
             ps.setString(7, s.getSalesmanUsername());
             ps.setInt(2, s.getOrderId());
             ps.setDouble(4, s.getTotalPrice());
             java.sql.Date sDate = new java.sql.Date(s.getDate().getTime());
             ps.setString(5, sDate.toString());
             ps.setInt(6, s.getPostalcode());
             ps.executeUpdate();
             connection.commit();
         } catch (SQLException ex) {
             System.out.println(ex.toString());
         } finally {
             Cleaner.closeSentence(ps);
         }
     }
 
     public ArrayList<Order> getOrderOverview() {
         ArrayList<Order> orders = new ArrayList();
         PreparedStatement sqlRead = null;
         ResultSet res = null;
         openConnection();
 
         try {
             sqlRead = connection.prepareStatement("SELECT * FROM ORDERS");
             res = sqlRead.executeQuery();
             while (res.next()) {
                 java.sql.Date date = res.getDate("dates");
                 String deliveryAddress = res.getString("DELIVERYADDRESS");
                 java.sql.Time timeOfDelivery = res.getTime("TIMEOFDELIVERY");
                 int status = res.getInt("STATUS");
                 Order orderToBeAdded = new Order(date, timeOfDelivery, deliveryAddress, status);
                 orders.add(orderToBeAdded);
             }
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
         } finally {
             Cleaner.closeConnection(connection);
             Cleaner.closeResSet(res);
             Cleaner.closeSentence(sqlRead);
         }
         return orders;
     }
 
     public ArrayList<SubscriptionPlan> removeExpiredSubs() {
         ArrayList<SubscriptionPlan> result = new ArrayList<SubscriptionPlan>();
         ArrayList<Integer> subremove = new ArrayList<Integer>();
         ArrayList<Integer> orderremove = new ArrayList<Integer>();
         PreparedStatement sqlRead = null;
         ResultSet res = null;
         openConnection();
         try {
             sqlRead = connection.prepareStatement("SELECT subscriptionplan.*, orders.orderid "
                     + "FROM ORDERS, SUBSCRIPTIONPLAN WHERE orders.subscriptionid = "
                     + "subscriptionplan.subscriptionid AND subscriptionplan.enddate <= CURRENT DATE");
             res = sqlRead.executeQuery();
             while (res.next()) {
                 int orderid = res.getInt("orderid");
                 orderremove.add(orderid);
                 int subid = res.getInt("subscriptionid");
                 Date startdate = res.getDate("startdate");
                 Date enddate = res.getDate("enddate");
                 Time time = res.getTime("timeofdelivery");
                 int day = res.getInt("weekday");
                 String username = res.getString("companyusername");
                 java.util.Date utilstart = startdate;
                 java.util.Date utilend = enddate;
                 SubscriptionPlan plan = new SubscriptionPlan(subid, utilstart, utilend, time, day, username);
                 if (!subremove.contains(subid)) {
                     subremove.add(subid);
                 }
                 result.add(plan);
             }
             for (int i = 0; i < orderremove.size(); i++) {
                 PreparedStatement sqlRemove1 = connection.prepareStatement("DELETE FROM dishes_ordered WHERE orderid = ?");
                 sqlRemove1.setInt(1, orderremove.get(i));
                 sqlRemove1.executeUpdate();
 
             }
             for (int i = 0; i < orderremove.size(); i++) {
                 PreparedStatement sqlRemove2 = connection.prepareStatement("DELETE FROM orders WHERE orderid = ?");
                 sqlRemove2.setInt(1, orderremove.get(i));
                 sqlRemove2.executeUpdate();
             }
             System.out.println("Subremovelength: " + subremove.size());
             System.out.println("Orderremovelength: " + orderremove.size());
             for (int i = 0; i < subremove.size(); i++) {
                 PreparedStatement sqlRemove3 = connection.prepareStatement("DELETE FROM subscriptionplan WHERE subscriptionid = ?");
                 sqlRemove3.setInt(1, subremove.get(i));
                 sqlRemove3.executeUpdate();
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
         } finally {
             Cleaner.closeConnection(connection);
             Cleaner.closeResSet(res);
             Cleaner.closeSentence(sqlRead);
         }
         return result;
     }
 
     public boolean changePassword(User user) {
         PreparedStatement sqlLogIn = null;
         openConnection();
         boolean ok = false;
         try {
             sqlLogIn = connection.prepareStatement("UPDATE users SET PASSWORD = ? WHERE username = ?");
             sqlLogIn.setString(1, user.getPassword());
             sqlLogIn.setString(2, user.getUsername());
             sqlLogIn.executeUpdate();
             connection.commit();
             ok = true;
 
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sqlLogIn);
         }
         closeConnection();
         return ok;
     }
 
     public boolean newUser(User user) {
         PreparedStatement sqlRegNewuser = null;
         PreparedStatement sqlRegNewRole = null;
         openConnection();
         boolean ok = false;
         try {
             sqlRegNewuser = connection.prepareStatement("INSERT INTO users VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
             sqlRegNewuser.setString(1, user.getUsername());
             sqlRegNewuser.setString(2, user.getPassword());
             sqlRegNewuser.setString(3, user.getFirstName());
             sqlRegNewuser.setString(4, user.getSurname());
             sqlRegNewuser.setString(5, user.getAddress());
             sqlRegNewuser.setString(6, user.getPhone());
             sqlRegNewuser.setString(7, user.getEmail());
             sqlRegNewuser.setInt(8, user.getPostnumber());
             sqlRegNewuser.executeUpdate();
 
             sqlRegNewRole = connection.prepareStatement("INSERT INTO roles VALUES(?,?)");
             sqlRegNewRole.setString(1, "customer");
             sqlRegNewRole.setString(2, user.getUsername());
             sqlRegNewRole.executeUpdate();
             connection.commit();
             ok = true;
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sqlRegNewuser);
         }
         closeConnection();
         return ok;
     }
 
     public int getNumberOfSalesmen() {
         int result = 0;
         PreparedStatement statement = null;
         openConnection();
         try {
             statement = connection.prepareStatement("SELECT COUNT(*) as number FROM SALESMAN");
             ResultSet res = statement.executeQuery();
             while (res.next()) {
                 result = res.getInt("number");
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(statement);
         }
         return result;
     }
 
     public ArrayList<StoredOrders> getStoredOrders(String query) {
         ArrayList<StoredOrders> result = new ArrayList();
         PreparedStatement statement = null;
         openConnection();
         try {
             statement = connection.prepareStatement(query);
             ResultSet res = statement.executeQuery();
             while (res.next()) {
                 int dishId = res.getInt("dishid");
                 int orderId = res.getInt("orderId");
                 int dishCount = res.getInt("dishCount");
                 int totalPrice = res.getInt("totalPrice");
                 int postalCode = res.getInt("postalcode");
                 String un = res.getString("salesmanusername");
                 Date d = res.getDate("dates");
                 result.add(new StoredOrders(dishId, orderId, dishCount, totalPrice, postalCode, d, un));
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(statement);
         }
         return result;
     }
     //FOR MENU
 
     public ArrayList<Dish> getDishes() {
         PreparedStatement sentence = null;
         openConnection();
         ArrayList<Dish> dishes = new ArrayList<Dish>();
         try {
            sentence = connection.prepareStatement("select * from dish");
             ResultSet res = sentence.executeQuery();
             while (res.next()) {
                 int dishid = res.getInt("DISHID");
                 String dishname = res.getString("DISHNAME");
                 double dishprice = res.getDouble("DISHPRICE");
                 Dish newdish = new Dish(dishid, dishname, dishprice, 1);
                 dishes.add(newdish);
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.closeSentence(sentence);
         }
         closeConnection();
         return dishes;
     }
 
     public boolean order(Order order) {
         PreparedStatement statement = null;
         PreparedStatement statement2 = null;
         boolean customer = false;
         if (getRole().equals("customer")) {
             customer = true;
         }
         openConnection();
         boolean result = false;
         try {
             connection.setAutoCommit(false);
             if (customer) {
                 statement = connection.prepareStatement("insert into orders(timeofdelivery,"
                         + " deliveryaddress, status, usernamecustomer, postalcode, dates, description, totalprice)"
                         + "values(?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             } else {
                 statement = connection.prepareStatement("insert into orders(timeofdelivery,"
                         + " deliveryaddress, status, usernamesalesman, postalcode, dates, description, totalprice)"
                         + "values(?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             }
             statement.setTime(1, new Time(order.getDate().getHours(), order.getDate().getMinutes(), order.getDate().getSeconds()));
             statement.setString(2, order.getDeliveryAddress());
             statement.setInt(3, 7);
             statement.setString(4, getCurrentUser());
             statement.setInt(5, order.getPostalcode());
             java.sql.Date sqldate = new java.sql.Date(order.getDate().getTime());
             statement.setDate(6, sqldate);
             statement.setString(7, order.getDescription());
             statement.setDouble(8, order.getTotalprice());
 
 
             statement.executeUpdate();
             connection.commit();
             int key = 0;
             ResultSet res = statement.getGeneratedKeys();
             if (res.next()) {
                 key = res.getInt(1);
             }
 
             for (int i = 0; i < order.getOrderedDish().size(); i++) {
                 statement2 = connection.prepareStatement("insert into dishes_ordered(dishid, orderid, dishcount) values(?, ?, ?)");
                 statement2.setInt(1, getDishId(order.getOrderedDish().get(i).getDishName()));
                 statement2.setInt(2, key);
                 statement2.setInt(3, order.getOrderedDish().get(i).getCount());
                 statement2.executeUpdate();
             }
             connection.commit();
             result = true;
         } catch (SQLException e) {
             System.out.println(e);
             Cleaner.rollback(connection);
             result = false;
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(statement);
             Cleaner.closeSentence(statement2);
         }
         closeConnection();
         return result;
     }
 
     public int getDishId(String dishname) {
         PreparedStatement statement = null;
         int result = 0;
         try {
             statement = connection.prepareStatement("SELECT dishid FROM dish WHERE dishname = ?");
             statement.setString(1, dishname);
             ResultSet res = statement.executeQuery();
             if (res.next()) {
                 result = res.getInt("dishid");
             } else {
                 result = -1;
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(statement);
         }
         return result;
     }
 
     //FOR SUBSCRIPTION
     public boolean subscription(SubscriptionPlan plan, Order order) {
         PreparedStatement statement = null;
         PreparedStatement statement2 = null;
         openConnection();
         boolean result = true;
         int key = 0;
         try {
             connection.setAutoCommit(false);
             statement = connection.prepareStatement("insert into subscriptionplan(startdate, enddate, "
                     + "timeofdelivery, deliveryaddress, totalprice, weekday, postalcode, description, companyusername)"
                     + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             java.sql.Date sqldate = new java.sql.Date(plan.startdate.getTime());
             java.sql.Date sqldate2 = new java.sql.Date(plan.enddate.getTime());
             statement.setDate(1, sqldate);
             statement.setDate(2, sqldate2);
             statement.setTime(3, plan.timeofdelivery);
             statement.setString(4, order.getDeliveryAddress());
             statement.setDouble(5, order.getTotalprice());
             statement.setInt(6, plan.weekday);
             statement.setInt(7, order.getPostalcode());
             statement.setString(8, order.getDescription());
             statement.setString(9, getCurrentUser());
             statement.executeUpdate();
 
             ResultSet res = statement.getGeneratedKeys();
             if (res.next()) {
                 key = res.getInt(1);
             }
             for (int i = 0; i < order.getOrderedDish().size(); i++) {
                 statement2 = connection.prepareStatement("insert into sub_dish(dishid, subid, dishcount) values(?, ?, ?)");
                 statement2.setInt(1, getDishId(order.getOrderedDish().get(i).getDishName()));
                 statement2.setInt(2, key);
                 statement2.setInt(3, order.getOrderedDish().get(i).getCount());
                 statement2.executeUpdate();
             }
             connection.commit();
             result = true;
         } catch (SQLException e) {
             System.out.println(e);
             Cleaner.rollback(connection);
             result = false;
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(statement);
             Cleaner.closeSentence(statement2);
         }
         plan.setSubid(key);
         closeConnection();
         checkSubscription();
         return result;
     }
 
     public void checkSubscription() {
         PreparedStatement statement = null;
         java.util.Date current = new java.util.Date();
         openConnection();
         try {
             statement = connection.prepareStatement("SELECT subscriptionid, weekday FROM subscriptionplan");
             ResultSet res = statement.executeQuery();
             while (res.next()) {
                 if (res.getInt("weekday") == current.getDay()) {
                     int subid = res.getInt("subscriptionid");
                     PreparedStatement statement2 = connection.prepareStatement("SELECT * FROM subscriptionplan WHERE subscriptionid ="+subid+" AND subscriptionid NOT IN (SELECT s.subscriptionid FROM subscriptionplan s, orders o WHERE s.subscriptionid = o.subscriptionid AND current date=o.dates)");
                     ResultSet res2 = statement2.executeQuery();
                     while (res2.next()) {
                         ArrayList<Dish> dishes = new ArrayList<Dish>();
                         PreparedStatement statement3 = connection.prepareStatement("SELECT s.dishid, d.dishname, s.dishcount, d.dishprice FROM sub_dish s, dish d WHERE s.subid = "
                                 + subid + " AND s.dishid = d.dishid");
                         ResultSet res3 = statement3.executeQuery();
                         while (res3.next()) {
                             Dish newdish = new Dish(res3.getInt("dishid"), res3.getString("dishname"),
                                     res3.getInt("dishprice"), res3.getInt("dishcount"));
                             dishes.add(newdish);
                         }
                         Order order = new Order(current, res2.getString("deliveryaddress"), 7, dishes,
                                 res2.getString("description"), res2.getInt("postalcode"), res2.getDouble("totalprice"));
                         PreparedStatement statement4 = connection.prepareStatement("insert into orders(timeofdelivery,"
                             + " deliveryaddress, status, usernamecustomer, subscriptionid, postalcode, dates, description, totalprice)"
                             + "values(?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                         statement4.setTime(1, res2.getTime("timeofdelivery"));
                         statement4.setString(2, order.getDeliveryAddress());
                         statement4.setInt(3, 7);
                         statement4.setString(4, getCurrentUser());
                         statement4.setInt(5, subid);
                         statement4.setInt(6, order.getPostalcode());
                         java.sql.Date sqldate = new java.sql.Date(order.getDate().getTime());
                         statement4.setDate(7, sqldate);
                         statement4.setString(8, order.getDescription());
                         statement4.setDouble(9, order.getTotalprice());
                         statement4.executeUpdate();
                         connection.commit();
                         int key = 0;
                         ResultSet res4 = statement4.getGeneratedKeys();
                         if (res4.next()) {
                             key = res4.getInt(1);
                         }
 
                         for (int i = 0; i < order.getOrderedDish().size(); i++) {
                             PreparedStatement statement5 = connection.prepareStatement("insert into dishes_ordered(dishid, orderid, dishcount) values(?, ?, ?)");
                             statement5.setInt(1, getDishId(order.getOrderedDish().get(i).getDishName()));
                             statement5.setInt(2, key);
                             statement5.setInt(3, order.getOrderedDish().get(i).getCount());
                             statement5.executeUpdate();
                         }
                         connection.commit();
                     }
                 }
             }
         } catch (SQLException e) {
             System.out.println(e);
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.closeSentence(statement);
             closeConnection();
         }
     }
     //
 
     public boolean userExist(String username) {
         PreparedStatement sqlLogIn = null;
         openConnection();
         boolean exist = false;
         try {
             sqlLogIn = connection.prepareStatement("SELECT * FROM users WHERE username = '" + username + "'");
             ResultSet res = sqlLogIn.executeQuery();
             if (res.next()) {
                 exist = true;
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
 
         } finally {
             Cleaner.closeSentence(sqlLogIn);
         }
         closeConnection();
         return exist;
     }
 
     public User getUser() {
         PreparedStatement statement = null;
         openConnection();
         User newUser = new User();
         try {
             statement = connection.prepareStatement("SELECT * FROM users WHERE username = '" + getCurrentUser() + "'");
             ResultSet res = statement.executeQuery();
             connection.commit();
             while (res.next()) {
                 String username = res.getString("username");
                 String password = res.getString("password");
                 String firstname = res.getString("firstname");
                 String surname = res.getString("surname");
                 String address = res.getString("address");
                 String mobilenr = res.getString("mobilenr");
                 int postalcode = res.getInt("postalcode");
                 String email = res.getString("email");
                 newUser = new User(username, password, firstname, surname, address, mobilenr, postalcode);
                 newUser.setEmail(email);
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(statement);
         }
         try {
             statement = connection.prepareStatement("SELECT * FROM POSTAL_NO WHERE zip=?");
             statement.setInt(1, newUser.getPostnumber());
             ResultSet res = statement.executeQuery();
             connection.commit();
             while (res.next()) {
                 String postalArea = res.getString("place");
                 newUser.setCity(postalArea);
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(statement);
         }
         closeConnection();
         return newUser;
     }
 
     private void openConnection() {
         try {
             if (ds == null) {
                 throw new SQLException("No connection");
             }
             connection = ds.getConnection();
             System.out.println("Databaseconnection established");
         } catch (SQLException e) {
             Cleaner.writeMessage(e, "Construktor");
         }
     }
 
     private void closeConnection() {
         System.out.println("Closing databaseconnection");
         Cleaner.closeConnection(connection);
     }
 
     public boolean changeData(User user) {
         PreparedStatement sqlUpdProfile = null;
         currentUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
         boolean ok = false;
         openConnection();
         try {
             sqlUpdProfile = connection.prepareStatement("update users set firstname = ?,surname = ?, address = ?, mobilenr = ?, email = ?,postalcode = ?, password = ? where username = ?");
             sqlUpdProfile.setString(1, user.getFirstName());
             sqlUpdProfile.setString(2, user.getSurname());
             sqlUpdProfile.setString(3, user.getAddress());
             sqlUpdProfile.setString(4, user.getPhone());
             sqlUpdProfile.setString(5, user.getEmail());
             sqlUpdProfile.setInt(6, user.getPostnumber());
             sqlUpdProfile.setString(7, user.getPassword());
             sqlUpdProfile.setString(8, getCurrentUser());
             ok = true;
             sqlUpdProfile.executeUpdate();
             connection.commit();
         } catch (SQLException e) {
             Cleaner.writeMessage(e, "changeData()");
         } finally {
             Cleaner.closeSentence(sqlUpdProfile);
         }
         closeConnection();
         return ok;
     }
 
     public boolean regDish(Dish dish) {
         PreparedStatement sqlRegNew = null;
         openConnection();
         boolean ok = false;
         try {
             sqlRegNew = connection.prepareStatement("insert into dish(dishname,dishprice) values(?, ?)");
             sqlRegNew.setString(1, dish.getDishName());
             sqlRegNew.setDouble(2, dish.getPrice());
             sqlRegNew.executeUpdate();
 
             connection.commit();
 
             /*
              * Transaksjon slutt
              */
             ok = true;
 
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sqlRegNew);
         }
         closeConnection();
         return ok;
     }
 
     public boolean deleteDish(Dish dish) {
         boolean ok = false;
         PreparedStatement sqlDel = null;
         openConnection();
         try {
             sqlDel = connection.prepareStatement("DELETE FROM DISH WHERE dishid = ?");
             sqlDel.setInt(1, dish.getDishId());
             sqlDel.executeUpdate();
             connection.commit();
             ok = true;
 
         } catch (SQLException e) {
             Cleaner.writeMessage(e, "deleteWorkout()");
         } finally {
             Cleaner.closeSentence(sqlDel);
         }
         closeConnection();
         return ok;
     }
 
     public boolean changeDishData(Dish dish) {
         PreparedStatement sqlUpdDish = null;
         boolean ok = false;
         openConnection();
         try {
             //String sql = "update eksemplar set laant_av = '" + navn + "' where isbn = '" + isbn + "' and eks_nr = " + eksNr;
             sqlUpdDish = connection.prepareStatement("update dish set dishname = ?,dishprice = ? where dishid = ?");
             sqlUpdDish.setString(1, dish.getDishName());
             sqlUpdDish.setDouble(2, dish.getPrice());
             sqlUpdDish.setInt(3, dish.getDishId());
 
             ok = true;
             sqlUpdDish.executeUpdate();
             connection.commit();
         } catch (SQLException e) {
             Cleaner.writeMessage(e, "changeData()");
         } finally {
             Cleaner.closeSentence(sqlUpdDish);
         }
         closeConnection();
         return ok;
     }
     public boolean deleteMessage(AdminMessage message){
          boolean ok = false;
          PreparedStatement sentence = null;
         openConnection();
          try{
             sentence = connection.prepareStatement("DELETE from message WHERE messageid = ?");
             sentence.setInt(1, message.getID());
             sentence.executeUpdate();
             connection.commit();
             ok = true;
          
         }catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sentence);
         }
         closeConnection();
         return ok;
     }
     public boolean addMessage(AdminMessage message){
         boolean ok = false;
          PreparedStatement sentence = null;
         openConnection();
          try{
             sentence = connection.prepareStatement("INSERT into message(message)VALUES(?)");
             sentence.setString(1, message.getMessage());
             sentence.executeUpdate();
             connection.commit();
             ok = true;
          
         }catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sentence);
         }
         closeConnection();
         return ok;
     }
     public boolean changeMessage(AdminMessage message){
            boolean ok = false;
          PreparedStatement sentence = null;
         openConnection();
          try{
             sentence = connection.prepareStatement("update message set message = ? where messageid = ?");
             sentence.setString(1, message.getMessage());
             sentence.setInt(2, message.getID());
             sentence.executeUpdate();
             connection.commit();
             ok = true;
          
         }catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sentence);
         }
         closeConnection();
         return ok;
     }
     public ArrayList<AdminMessage>getMessages(){
         PreparedStatement sentence = null;
         openConnection();
         ArrayList<AdminMessage>messages = new ArrayList<AdminMessage>();
         try{
             sentence = connection.prepareStatement("Select * from Message");
             ResultSet res = sentence.executeQuery();
             connection.commit();
             while(res.next()){
                 String message = res.getString("message");
                 int ID = res.getInt("MESSAGEID");
                 messages.add(new AdminMessage(message,ID));
             }
         }catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sentence);
         }
         closeConnection();
         return messages;
     }
 
     public ArrayList<Dish> getAdminDishes() {
         PreparedStatement sentence = null;
         openConnection();
         ArrayList<Dish> dishes = new ArrayList<Dish>();
         try {
             sentence = connection.prepareStatement("select * from DISH");
             ResultSet res = sentence.executeQuery();
             connection.commit();
             while (res.next()) {
                 int dishid = res.getInt("DISHID");
                 String dishname = res.getString("DISHNAME");
                 double dishprice = res.getDouble("DISHPRICE");
                 Dish newdish = new Dish(dishname, dishprice);
                 newdish.setDishId(dishid);
                 dishes.add(newdish);
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sentence);
         }
         closeConnection();
         return dishes;
     }
 
     public String getRole() {
         PreparedStatement statement = null;
         openConnection();
         String role = "";
         try {
             statement = connection.prepareStatement("SELECT * FROM roles WHERE username=?");
             getCurrentUser();
             statement.setString(1, this.currentUser);
             ResultSet res = statement.executeQuery();
             while (res.next()) {
                 role = res.getString("rolename");
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.closeSentence(statement);
         }
         closeConnection();
         return role;
     }
 
     public User emailExist(String inputEmail) {
         PreparedStatement sqlLogIn = null;
         openConnection();
         User newUser = new User();
         try {
             sqlLogIn = connection.prepareStatement("SELECT * FROM users WHERE email = '" + inputEmail + "'");
             ResultSet res = sqlLogIn.executeQuery();
             connection.commit();
             while (res.next()) {
                 String username = res.getString("username");
                 String password = res.getString("password");
                 String firstname = res.getString("firstname");
                 String surname = res.getString("surname");
                 String address = res.getString("address");
                 String mobilenr = res.getString("mobilenr");
                 int postalcode = res.getInt("postalcode");
                 String email = res.getString("email");
                 newUser = new User(username, password, firstname, surname, address, mobilenr, postalcode);
                 newUser.setEmail(email);
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sqlLogIn);
         }
         closeConnection();
         return newUser;
     }
 
     public String getCurrentUser() {
         currentUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
         return currentUser;
     }
 }
