 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DB;
 
 import DataStructur.DuengUndWasser;
 import DataStructur.Duenger;
 import DataStructur.PflanzenHoehe;
 import java.awt.List;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  * @author Jung-Ho Choi
  */
 public class DB {
         
     private Connection con = null;
     private Statement stmt = null;
     private ResultSet rslt = null;
     private PreparedStatement pstmt = null;
     private boolean userValidity = false;
     
    private SimpleDateFormat myformatter = new SimpleDateFormat("dd.MM.yyyy");
     
     private Connection ConnectDB () throws Exception {
         
         // Informationen zusammensuchen
         String dbHost = "localhost";
         String dbPort = "3306";
         String dbName = "projekt";
         String dbUser = "root";
         String dbPassword = "test";
         
         // Treiberklasse laden
         Class.forName("com.mysql.jdbc.Driver");
         
         // URL zusammenbauen
         StringBuilder url = new StringBuilder ();
         url.append ("jdbc:mysql://").append(dbHost);
         url.append (":").append(dbPort).append("/");
         url.append (dbName);
         url.append ("?user=").append(dbUser);
         url.append ("&password=").append(dbPassword);
         
         con = DriverManager.getConnection(url.toString());
         
         return con;
         
     }
     
     private void CloseDBConnection() throws SQLException {
         if (rslt != null) {
             rslt.close();
         }
         
         if (stmt != null) {
             stmt.close();
         }
         
         if (con != null) {
             con.close();
         }
         
         if (pstmt != null) {
             pstmt.close();
         }
     }
 
     public boolean getuserValidity() {
         return userValidity;
     }
     
     public void InsertIntoBenutzer (String benutzername, String passwort) throws Exception{
         
         try {
             // Zur Datenbank verbinden
             con = ConnectDB();
             // Statement erstellen
             pstmt = con.prepareStatement("INSERT INTO benutzer VALUES(?,?)");
             //Query erstellen
             pstmt.setString(1, benutzername);
             pstmt.setString(2, passwort);
             pstmt.executeUpdate();
         }
         catch (Exception e) {
             
         }
         finally {
             this.CloseDBConnection();
         }
     }
     
     public void InsertIntoPflanzen_hohe(PflanzenHoehe h) throws Exception{   
         try { 
             // Zur Datenbank verbinden
             con = ConnectDB();
             // Statement erstellen                   
             pstmt = con.prepareStatement("INSERT INTO pflanzen_hoehe VALUES(?,?,?);");
             //Query erstellen 
             pstmt.setString(1, h.getSorte());  
             pstmt.setString(2, String.valueOf(myformatter.format((Date)h.getDatum()))); 
             pstmt.setString(3, String.valueOf(h.getHoehe()));
             pstmt.executeUpdate();
             }
         catch (Exception e) {}
         
         finally{this.CloseDBConnection();}    
     }
  
     public void InsertIntoDuengvorgang(DuengUndWasser dw) throws Exception{   
         try { 
             // Zur Datenbank verbinden
             con = ConnectDB();
             // Statement erstellen                   
             pstmt = con.prepareStatement("INSERT INTO duengvorgang VALUES(?,?,?);");
             //Query erstellen 
             pstmt.setString(1, String.valueOf(myformatter.format((Date)dw.getDatum())));  
             pstmt.setString(2, dw.getDuenger()); 
             pstmt.setString(3, String.valueOf(dw.getMenge()));
             pstmt.executeUpdate();
             }
         catch (Exception e) {}
         
         finally{this.CloseDBConnection();}    
     }    
     
     public void InsertIntoDuenger(Duenger duenger) throws Exception{
       
         try { 
             // Zur Datenbank verbinden
             con = ConnectDB();
             // Statement erstellen
             pstmt = con.prepareStatement("INSERT INTO duenger VALUES(?,?,?,?,?)");
             //Query erstellen
             pstmt.setString(1, duenger.getName());  
             pstmt.setString(2, String.valueOf(duenger.getStickstoff())); 
             pstmt.setString(3, String.valueOf(duenger.getPhosphat()));
             pstmt.setString(4, String.valueOf(duenger.getKalium()));
             pstmt.setString(5, String.valueOf(duenger.getKalium()));
             pstmt.executeUpdate();
         }
     
         catch (Exception e) {}
         
         finally{this.CloseDBConnection();}
     }
     
     public boolean UsernameExists (String username) throws Exception {
         try {
             con = ConnectDB();
             pstmt = con.prepareStatement("SELECT * FROM benutzer WHERE username = (?);",
                                     ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             pstmt.setString(1, username);
             rslt = pstmt.executeQuery();
             
             while (rslt.next()) {
                 if (rslt.getString(1).equals(username)) {
                 return true;
                 }
             }       
         }
         
         catch (Exception e) {
             
         }
         
         finally {
             this.CloseDBConnection();
         }
     return false;
     }
     
     public Duenger getDuengerInfo(String name) throws Exception{
         con = ConnectDB();
         pstmt = con.prepareStatement("SELECT * FROM duenger WHERE name like (?);");
         pstmt.setString(1, name);
         rslt = pstmt.executeQuery();
         
         if(rslt.next()){   
             Duenger d =  new Duenger(rslt.getString(1), rslt.getInt(2), rslt.getInt(3), rslt.getInt(4), rslt.getInt(5));
             this.CloseDBConnection();
             return d;
             }
         
         else {
             this.CloseDBConnection();
             return null;
             } 
 
     }
     
     public ArrayList getDuengerList() throws Exception{
     con = ConnectDB();
     pstmt = con.prepareStatement("SELECT name FROM duenger ORDER BY name DESC");
     rslt = pstmt.executeQuery();
     
     ArrayList DuengerList= new ArrayList();
     while (rslt.next()) 
         {DuengerList.add(rslt.getString(1));}
     
     this.CloseDBConnection();
     return DuengerList;
     }
     
     
     public Object[][] getTblPflanzenFromDB() throws Exception{
     con = ConnectDB();
     pstmt = con.prepareStatement("SELECT sorte, art, herkunft, datum_aussaat FROM pflanzen");
     rslt = pstmt.executeQuery();
     ArrayList row = new ArrayList();
     ArrayList<Object[]> column = new ArrayList();
     
     while (rslt.next()) {
         for(int i=1 ; i<=4;i++){row.add(rslt.getObject(i));}
         column.add(row.toArray());
         row.clear();
         }
     
     Object[][] result = new Object[column.size()][4];
     for(int i=0;i<column.size();i++)
         {result[i]=column.get(i);}
 
     this.CloseDBConnection();
     return result;
     }
     
     
     public void CheckLogOn(String name, String pw) throws Exception {
         try{
             con = ConnectDB();
             pstmt = con.prepareStatement("SELECT * FROM benutzer WHERE username like (?) AND pass like (?);", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             pstmt.setString(1, name);
             pstmt.setString(2, pw);
             rslt = pstmt.executeQuery();
         
             // if Table size is >=1 -> username with this password 
             if(rslt.next()){userValidity = true;}
             
             /* //
             while (rslt.next()) {
                 System.out.println(rslt.getString(1));
                 if (rslt.getString(1).equals(name) && rslt.getString(2).equals("test")) {
                 userValidity = true;
                 }
             }*/
         }
         catch (Exception e) {
             
         }
         finally {
             this.CloseDBConnection();
         }
     }
 }
