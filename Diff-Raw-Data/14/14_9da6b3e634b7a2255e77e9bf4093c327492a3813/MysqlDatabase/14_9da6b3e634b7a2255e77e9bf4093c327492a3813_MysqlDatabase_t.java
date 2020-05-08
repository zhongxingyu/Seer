 package utils;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Desclaux Christophe <christophe@zouig.org>
  */
 public class MysqlDatabase {
     
     private static Statement st = null;
     private static Connection con = null;
     
     public static Statement getStatement(){
         try {
             if(st == null){
                 String url = Config.getVar("mysql-server")+"/"+Config.getVar("mysql-bdd");
                 String user = Config.getVar("mysql-user");
                 String password = Config.getVar("mysql-password");
                        
                 con = DriverManager.getConnection(url, user, password);
                 st = con.createStatement();
             }
             return st;
         } catch (SQLException ex) {
             Logger.getLogger(MysqlDatabase.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     private static String query(String query) {
         ResultSet rs = null;
         try {
             rs = MysqlDatabase.getStatement().executeQuery(query);
             if (rs.next()) {
                 return rs.getString(1);
             }
 
         } catch (SQLException ex) {
             Logger lgr = Logger.getLogger(MysqlDatabase.class.getName());
             lgr.log(Level.SEVERE, ex.getMessage(), ex);
 
         } finally {
             try {
                 if (rs != null) {
                     rs.close();
                 }
 
             } catch (SQLException ex) {
                 Logger lgr = Logger.getLogger(MysqlDatabase.class.getName());
                 lgr.log(Level.WARNING, ex.getMessage(), ex);
             }
         }
         return null;
     }
 
     private static void Exec(String query) {
         try {
             MysqlDatabase.getStatement().execute(query);
 
         } catch (SQLException ex) {
             Logger lgr = Logger.getLogger(MysqlDatabase.class.getName());
             lgr.log(Level.SEVERE, ex.getMessage(), ex);
 
         }
     }
     public static void close(){
         try {
             if (st != null) {
                 st.close();
             }
             if (con != null) {
                 con.close();
             }
             con = null;
             st = null;
 
         } catch (SQLException ex) {
             Logger lgr = Logger.getLogger(MysqlDatabase.class.getName());
             lgr.log(Level.WARNING, ex.getMessage(), ex);
         }        
     }
     private static void createItem(String type, String value,int inc){
        MysqlDatabase.Exec("INSERT INTO  `"+Config.getVar("mysql-bdd")+"`.`items` (`typeItem` ,`value` ,`inc`, `created_at`, `updated_at`)VALUES ('"+type+"',  '"+value+"',  '"+inc+"', NOW(), NOW())");
     }
     private static void createItem(Property type, String value){
         int nbOcc = MysqlDatabase.getNbOccurences(type.getURI(), value);
         if(nbOcc < 0 ){
             MysqlDatabase.createItem(type.getURI(), value,1);
         }else{
             nbOcc++;
            MysqlDatabase.Exec("UPDATE  `"+Config.getVar("mysql-bdd")+"`.`items` SET  `inc` =  '"+nbOcc+"' WHERE  `items`.`typeItem` =  '"+type.getURI()+"' AND  `items`.`value` =  '"+value+"';");
         }
     }
     private static int getNbOccurences(String type, String value){
         String res = MysqlDatabase.query("SELECT inc FROM `items` WHERE typeItem='"+type+"' AND value='"+value+"'");
         if(res == null)return -1;
         return Integer.parseInt(res);
     }
     
     public static void createItemFromProp(Prop prop){
         if(!prop.isLiteral()){
             MysqlDatabase.createItem(prop.getType(), prop.getValue());
         }
     }
     
     public static void createItemFromProps(ArrayList<Prop> props){
         for(int i=0; i < props.size();i++){
             
             createItemFromProp(props.get(i));
         }
     }
     
     
     public static void createItemFromItems(ArrayList<Item> items){
         for(int i=0; i < items.size();i++){
             createItemFromProps(items.get(i).values);
         }
    }   
    
    public static void main(String[] args){
        createItemFromProp(new Prop("toto","tati",true));
    }
 }
