 package DB;
 
 import Java.Order;
 import Java.OrderStatus;
 import java.sql.*;
 import java.util.ArrayList;
 import javax.annotation.Resource;
 import javax.faces.context.FacesContext;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import user.user;
 
 public class Database {
 
     @Resource(name = "jdbc/hc_realm")
     private DataSource ds;
     private Connection connection;
     private String user = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
 
     public Database() {
         System.out.println("FARDIN!");
         try {
             Context con = new InitialContext();
             ds = (DataSource) con.lookup("jdbc/hc_realm");
         } catch (NamingException e) {
             System.out.println(e.getMessage());
         }
     }
     public ArrayList<Order> getPendingOrders(String query){ 
         ArrayList<Order> orders = new ArrayList();
         ResultSet res = null;
         Statement stm = null;
         openConnection();
         try {
             stm = connection.createStatement();
             res = stm.executeQuery(query);
             while (res.next()) {
                 java.sql.Date date = res.getDate("DATES");
                 int timeOfDelivery = res.getInt("TIMEOFDELIVERY");
                 String deliveryAddress = res.getString("DELIVERYADDRESS");
                String status = res.getString("STATUS");
                orders.add(new Order(date, timeOfDelivery, deliveryAddress));
             }
         } catch (SQLException e) {
         } finally {
             Cleaner.closeConnection(connection);
             Cleaner.closeResSet(res);
             Cleaner.closeSentence(stm);
         }
         return orders;
         
 }
 
     public boolean logIn(user user) {
         PreparedStatement sqlLogIn = null;
         openConnection();
         boolean ok = false;
         try {
             sqlLogIn = connection.prepareStatement("SELECT * FROM BRUKER WHERE BRUKERNAVN = '" + user.getUsername() + "' AND PASSORD = '" + user.getPassword() + "' ");
             ResultSet res = sqlLogIn.executeQuery();
             connection.commit();
             if (res.next()) {
                 ok = true;
             }
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
 
     public boolean changePassword(user user) {
         PreparedStatement sqlLogIn = null;
         openConnection();
         boolean ok = false;
         try {
             sqlLogIn = connection.prepareStatement("UPDATE BRUKER SET PASSORD = ? WHERE BRUKERNAVN = ?");
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
 
     public boolean newUser(user user) {
         PreparedStatement sqlRegNewuser = null;
         PreparedStatement sqlRegNewRole = null;
         openConnection();
         boolean ok = false;
         try {
             sqlRegNewuser = connection.prepareStatement("insert into BRUKER(BRUKERNAVN,PASSORD) values(?, ?)");
             sqlRegNewuser.setString(1, user.getUsername());
             sqlRegNewuser.setString(2, user.getPassword());
             sqlRegNewuser.executeUpdate();
             
             sqlRegNewRole = connection.prepareStatement("INSERT INTO ROLLE(BRUKERNAVN,ROLLE) VALUES(?,'bruker')");
             sqlRegNewRole.setString(1, user.getUsername());
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
     
       public boolean userExist(user user) {
         PreparedStatement sqlLogIn = null;
         openConnection();
         boolean exist = false;
         try {
             sqlLogIn = connection.prepareStatement("SELECT * FROM BRUKER WHERE BRUKERNAVN = '" + user.getUsername() + "'");
             ResultSet res = sqlLogIn.executeQuery();
             connection.commit();
             if (res.next()) {
                 exist = true;
             }
         } catch (SQLException e) {
             System.out.println(e.getMessage());
             Cleaner.rollback(connection);
 
         } finally {
             Cleaner.setAutoCommit(connection);
             Cleaner.closeSentence(sqlLogIn);
         }
         closeConnection();
         return exist;
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
 /*
     public boolean regNew(user user) {
         PreparedStatement sqlRegNew = null;
         openConnection();
         boolean ok = false;
         try {
             sqlRegNew = connection.prepareStatement("insert into trening(dato,varighet,kategorinavn,tekst,brukernavn) values(?, ?, ?,?,?)");
             try {
                 sqlRegNew.setDate(1, new java.sql.Date(newWorkout.getDate().getTime()));
             } catch (NullPointerException e) {
                 sqlRegNew.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
             }
             sqlRegNew.setInt(2, newWorkout.getDuration());
             sqlRegNew.setString(3, newWorkout.getCategory());
             sqlRegNew.setString(4, newWorkout.getText());
             sqlRegNew.setString(5, user);
             sqlRegNew.executeUpdate();
 
             connection.commit();
 
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
 */
     /*
     public boolean deleteWorkout(user user) {
         boolean ok = false;
         PreparedStatement sqlUpdWorkout = null;
 
         System.out.println(workout.getNumber());
         openConnection();
         try {
             sqlUpdWorkout = connection.prepareStatement("DELETE FROM TRENING WHERE oktnr = ?");
             sqlUpdWorkout.setInt(1, workout.getNumber());
             sqlUpdWorkout.executeUpdate();
             connection.commit();
             ok = true;
 
         } catch (SQLException e) {
             Cleaner.writeMessage(e, "deleteWorkout()");
         } finally {
             Cleaner.closeSentence(sqlUpdWorkout);
         }
         closeConnection();
         return ok;
 
 
 
     }
 
     public boolean changeData(Workout workout) {
         PreparedStatement sqlUpdWorkout = null;
         boolean ok = false;
         openConnection();
         try {
             //String sql = "update eksemplar set laant_av = '" + navn + "' where isbn = '" + isbn + "' and eks_nr = " + eksNr;
             sqlUpdWorkout = connection.prepareStatement("update trening set dato = ?,varighet = ?,kategorinavn = ?, tekst = ? where oktnr = ? and brukernavn = ?");
             try {
                 sqlUpdWorkout.setDate(1, new java.sql.Date(workout.getDate().getTime()));
             } catch (NullPointerException e) {
                 sqlUpdWorkout.setDate(1, new java.sql.Date(new java.util.Date().getTime()));
             }
             sqlUpdWorkout.setInt(2, workout.getDuration());
             sqlUpdWorkout.setString(3, workout.getCategory());
             sqlUpdWorkout.setString(4, workout.getText());
             sqlUpdWorkout.setInt(5, workout.getNumber());
             sqlUpdWorkout.setString(6, user);
             ok = true;
             sqlUpdWorkout.executeUpdate();
             connection.commit();
         } catch (SQLException e) {
             Cleaner.writeMessage(e, "changeData()");
         } finally {
             Cleaner.closeSentence(sqlUpdWorkout);
         }
         closeConnection();
         return ok;
     }
     * 
     */
 }
