 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.uniriotec.tracker.dao;
 
 import static br.uniriotec.tracker.dao.DAOMysqlConector.abrirConexao;
 import static br.uniriotec.tracker.dao.DAOMysqlConector.conn;
 import br.uniriotec.tracker.model.Ticket;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  *
  * @author helanio
  */
 public class DAOTicket extends DAOMysqlConector{
     
     public boolean existSystem (String systemName){
         return this.ExistColumnElementInTable("SYSTEM", "systemName", systemName);
     }
     
     public boolean existComponent(String componentName){
         return this.ExistColumnElementInTable("COMPONENT", "componentName", componentName);
     }
     
     public boolean addTicket(String title, String system, String component, String description, String operator){
         abrirConexao();
         
         String sqlCreatTiket = "INSERT INTO ticketManager.TICKET (title, systemKey, componentKey,"
                 + " description, status, operador)";
         sqlCreatTiket += "VALUES (" + "'"
                 + title + "','"
                 + system + "','"
                 + component + "','"
                + description + "',"
                + 0 + ",'"
                 + operator + "')";
         
          try {
                 Statement addTicket = conn.createStatement();
                 int rs = addTicket.executeUpdate(sqlCreatTiket);
                 if (rs == 1) {
                     return true;
                 } else {
                     return false;
                 }
                 } catch (Exception e) {
                     System.err.println(e);
            }
          fecharConexao();
          return false;
     }
     
     
    public Ticket getTicket(String ticketTitle){
         abrirConexao();
         
        String sqlQuery = "SELECT * FROM TICKET WHERE title = '" + ticketTitle + "'";
         
          try {
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlQuery);
             if (rs.first()){
                 int id = rs.getInt("idTicket");
                 String system = rs.getString("systemKey");
                 String component = rs.getString("componentKey");
                 String description = rs.getString("description");
                return new Ticket(id, ticketTitle, system, component, description);
             }
         } catch (Exception e) {
                 System.err.println(e);
         }
         fecharConexao();
         return null;
     }
     
     public boolean updateTicket (int idTicket, String system, String component, String description, String title){
         abrirConexao();
         
         String sqlQuery = "UPDATE TICKET SET title = '"
                 + title + "', "
                 + "systemKey = '" + system + "', "
                 + "componentKey = '" + component + "', "
                 + "description = '" + description + "' "
                 + "WHERE idTicket = " + idTicket;
         System.out.println(sqlQuery);
         try {
                 Statement addTicket = conn.createStatement();
                 int rs = addTicket.executeUpdate(sqlQuery);
                 if (rs == 1) {
                     return true;
                 } else {
                     return false;
                 }
                 } catch (Exception e) {
                     System.err.println(e);
            }
          fecharConexao();
          return false;
     }
     
     public List getAllTickets(){
         abrirConexao();
         
         String sqlQuery = "SELECT * FROM TICKET";
         List tickets = new ArrayList<Ticket>();
         
         try {
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sqlQuery);
             
             while(rs.next()){
                 int id = rs.getInt("idTicket");
                 String title = rs.getString("title");
                 String system = rs.getString("systemKey");
                 String component = rs.getString("componentKey");
                 String description = rs.getString("description");
                 String status = rs.getString("status");
                 String operator = rs.getString("operador");
                 tickets.add(new Ticket(id, title, system, component, description, status, operator));
             }
         } catch (Exception e) {
                 System.err.println(e);
         }
         fecharConexao();
         return tickets;
     }
     
 
     
 }
