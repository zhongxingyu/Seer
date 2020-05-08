 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package databases;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.*;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 /**
  *
  * @author VARDAKIS
  */
 public class Databases {
 
   
     static Connection conn = null;
     static Statement st;
     static ResultSet rs ;
     public int currentId = -1;
     public Databases() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
         String sql;
        String url = "jdbc:mysql://localhost:3306/";//2723/";
         String dbName = "mysqldatabase";
         String driver = "com.mysql.jdbc.Driver";
         String userName = "root"; 
        String password = "hard96361992";//"17561770gv";
         Class.forName(driver).newInstance();
         conn = DriverManager.getConnection(url+dbName,userName,password);
     //System.out.println("Connected to the database");
   //conn.close();
         st = conn.createStatement();
 }
     public void doThings() throws InstantiationException {
         System.out.println("MySQL Connect Example.");
   int i=0,choice=0,it=0;
   boolean exit=false;
   String sql;
   String url = "jdbc:mysql://localhost:2723/";
   String dbName = "mysqldatabase";
   String driver = "com.mysql.jdbc.Driver";
   String userName = "root"; 
   String password = "17561770gv";
   try {
    //   Connect();
   /*Class.forName(driver).newInstance();
   conn = DriverManager.getConnection(url+dbName,userName,password);
   System.out.println("Connected to the database");*/
   //conn.close();
   st = conn.createStatement();
   while(exit==false){
   System.out.println("Hello!!Type 1 to log in to an existing account or type 2 to sign in");
   Welcome();
  while(i!=1){
      try {
             System.out.println("Menu");
             System.out.println("0.Exit");
             System.out.println("1.All Photos");
             System.out.println("2.All Photographers");
             System.out.println("3.Coloured Photos");
             System.out.println("4.Black-White photos");
             System.out.println("5.Portraits");
             System.out.println("6.Landscapes");
             System.out.println("7.Other photos");
             System.out.println("8.Available Photos");
             System.out.println("9.Photos by place");
             System.out.println("10.Total amount of customers");
             System.out.println("11.Sign in new customer");
             System.out.println("12.Log out");
             //System.out.println("3.Coloured Photos");
             //InputStreamReader isr = new InputStreamReader(System.in);
            // BufferedReader br = new BufferedReader(isr);
             //System.out.println("Enter choice : ");
             String s = read("Enter choice:");
             choice=Integer.parseInt(s);
        
             
             switch(choice){
                 case 0:
                     i=1;
                     exit=true;
                     break;
                 case 1:
                     
                  //   System.out.println("Mphka");
                    sql = "SELECT * FROM photos "; 
                     rs = st.executeQuery(sql);
                     System.out.println("Format:PhotoId,Type(Coloured-Black),Width,Height,Availability,Price,Category,Place,Person");
                     while (rs.next()) {
                        // System.out.println("Mphka1");
                         for(it=1;it<10;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     break;
                 case 2:
                     
                     sql = "SELECT * FROM photographers "; 
                     rs = st.executeQuery(sql);
                     System.out.println("Format:PhotographerId,Name,Address,BirthDate,Country,Influences,Photos");
                     while (rs.next()) {
                        // System.out.println("Mphka1");
                         for(it=1;it<8;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     break;
                 case 3:
                     
                     sql = "SELECT Id,Width,Height,Availability,Price,Category,Place,Person FROM photos WHERE Type='Coloured'"; 
                     rs = st.executeQuery(sql);
                      
                     System.out.println("Format:PhotoId,Width,Height,Availability,Price,Category,Place,Person");
                     while (rs.next()) {
                         
                        // System.out.println("Mphka1");
                         for(it=1;it<9;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     break;
                 case 4:
                     sql = "SELECT Id,Width,Height,Availability,Price,Category,Place,Person FROM photos WHERE Type='Black'"; 
                     rs = st.executeQuery(sql);
                      
                     System.out.println("Format:PhotoId,Width,Height,Availability,Price,Category,Place,Person");
                     while (rs.next()) {
                         
                        // System.out.println("Mphka1");
                         for(it=1;it<9;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     break;
                 case 5:
                     sql = "SELECT Id,Type,Width,Height,Availability,Price,Place,Person FROM photos WHERE Category='Portrait'"; 
                     rs = st.executeQuery(sql);
                      
                     System.out.println("Format:PhotoId,Type,Width,Height,Availability,Price,Place,Person");
                     while (rs.next()) {
                         
                        // System.out.println("Mphka1");
                         for(it=1;it<9;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     break;
                 case 6:
                     sql = "SELECT Id,Type,Width,Height,Availability,Price,Place,Person FROM photos WHERE Category='Landscape'"; 
                     rs = st.executeQuery(sql);
                      
                     System.out.println("Format:PhotoId,Type,Width,Height,Availability,Price,Place,Person");
                     while (rs.next()) {
                         
                        // System.out.println("Mphka1");
                         for(it=1;it<9;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     break;
                 case 7:
                     sql = "SELECT Id,Type,Width,Height,Availability,Price,Place,Person FROM photos WHERE Category='Other'"; 
                     rs = st.executeQuery(sql);
                      
                     System.out.println("Format:PhotoId,Type,Width,Height,Availability,Price,Place,Person");
                     while (rs.next()) {
                         
                        // System.out.println("Mphka1");
                         for(it=1;it<9;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     break;
                 case 8:
                     sql = "SELECT Id,Type,Width,Height,Availability,Price,Place,Person FROM photos WHERE Availability='1'"; 
                     rs = st.executeQuery(sql);
                      
                     System.out.println("Format:PhotoId,Type,Width,Height,Price,Category,Place,Person");
                     while (rs.next()) {
                         
                        // System.out.println("Mphka1");
                         for(it=1;it<9;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     break;
                 case 9:
                     String place=read("Give country name");
                    // System.out.println("Geia");
                     sql = "SELECT Id,Type,Width,Height,Availability,Price,Category,Place,Person FROM photos WHERE Place='"+place+"'"; 
                     
                     rs = st.executeQuery(sql);
                     
                      if(rs.next()!=false){
                         System.out.println("Format:PhotoId,Type,Width,Height,Availability,Price,Category,Place,Person");
                      }
                      else{
                          System.out.println("No matches found");
                          place="";
                          sql="";
                          break;
                      }
                     while (rs.next()) {
                        
                        // System.out.println("Mphka1");
                         for(it=1;it<10;it++){
                             userName = rs.getString(it);
                             if (userName!=null){
                             System.out.print(userName);
                             }
                             else System.out.print("none");
                             System.out.print(" ");
                         }
                         System.out.println();
                     }
                     place="";
                     sql="";
                     break;
                 case 10:
                     sql = "Select count(*) from customers"; 
                     rs = st.executeQuery(sql);
                      
                    // System.out.println("Format:PhotoId,Type,Width,Height,Price,Category,Place,Person");
                     while (rs.next()) {
                             userName = rs.getString(1);
                             System.out.println("Total number of customers= "+userName);
                     }
                     break;
 
                 case 11: //Xreiazetai elegxo an o xrhsths edwse int
                     boolean exists=true;
                     while(exists==true){
                         String id=read("Enter photoId");
                         if(id.equals("cancel")) break;
                         sql="SELECT Availability,Price FROM photos WHERE Id='"+id+"'";
                         rs=st.executeQuery(sql);
                         exists=rs.next();
                         if(exists==true){//Elegxos an h fwto yparxei
                             System.out.println("Photo id not found.Type cancel for abortion or give another Id");
                         }
                         else {
                             if(Integer.parseInt(rs.getString(1))==1){
                                 Transaction(Integer.parseInt(id),currentId,Integer.parseInt(rs.getString(2)));
                             }
                             else{//Elegxos an fwtografia einai available
                                 System.out.println("Photo not available.Type cancel for abortion or give another Id");
                                 exists=true;
                                 break;
                             }
                         }
                     }
                     break;
                 case 12:
                     currentId=0;
                     i=1;
                     break;
             }
  
         } catch (IOException e) {
            // System.out.println("Ooops");
             e.printStackTrace();
         }
  }
  }
   
   
   } catch (Exception e) {
   }
     }
 public ResultSet executeStatement(String sql) throws SQLException{
     rs=st.executeQuery(sql);
     return rs;
 }
 public int Update(String sql) throws SQLException{
     return st.executeUpdate(sql);
 }
 public void Welcome() throws IOException, SQLException{
     boolean exit=false;
     int i=0;
     String userName="",sql="",password="";
     while(exit==false){
     i=Integer.parseInt(read("Give your choice:"));
     if(i==1){
         userName=read("Give your username");
         sql="SELECT Name,CustomerId,Password FROM customers WHERE Username='"+userName+"'";
         rs=st.executeQuery(sql);
         exit=rs.next();
         if(exit==false){
             System.out.println("Username doesnt exist.Sign in or give correct username");
         }
         else{
             password=read("Give your password");
             if (password.equals(rs.getString(3))){
                 currentId=Integer.parseInt(rs.getString(2));
                 System.out.println("Welcome "+rs.getString(1));
                 return;
             }
             else{
                 exit=false;
                 System.out.println("Wrong combination of Username and password.Try again ");
             }
         }
     }
   
     else if (i==2){
       newSign();
       return;
     }
     else{
         System.out.println("Type again");
     }
   }
     
 }
 public static void Transaction(int photoId,int customerId,int price) throws SQLException{
     DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
     Date date = new Date();
     String Date=dateFormat.format(date);
     String sql="INSERT INTO transactions(`CustomerId`,`PhotoId`,`Date`,`Cost`)VALUES(customerId,photoId,Date,price)";
     st.executeUpdate(sql);
 }
 public static void newSign() throws IOException, SQLException{
     String name,sql,address,Interests,Username="",password="";
     int number,total=0;
     boolean uniqueUsername=true;
     sql = "Select count(*) from customers"; 
             rs = st.executeQuery(sql);
         
         try {
             // System.out.println("Format:PhotoId,Type,Width,Height,Price,Category,Place,Person");
             while (rs.next()) {
                 total =Integer.parseInt( rs.getString(1));
     
             }       
         } catch (SQLException ex) {
             Logger.getLogger(Databases.class.getName()).log(Level.SEVERE, null, ex);
         }
     total=total+1;
     name=read("Give your name");
     address=read("Give your address");
     Interests=read("Give your interests(Portrait-Landscape-Other)");
     number=Integer.parseInt(read("Give your card number"));
     while(uniqueUsername==true){
         Username=read("Select your username");
         if (Username.equals("cancel")){
             System.exit(1);
         }
         if(Username.equals("")){return ;}
         sql="SELECT Name FROM customers WHERE Username='"+Username+"'";
         rs=st.executeQuery(sql);
         uniqueUsername=rs.next();
         if(uniqueUsername==true){System.out.println("Username already exists,type cancel for abortion or give another");}
     }
     password=read("Give your password");
     sql="INSERT INTO mysqldatabase.customers (`CustomerId`,`Name`,`Address`,`Interests`,`CardNumber`,`Username`,`Password`) VALUES ('"+total+"','"+name+"','"+address+"','"+Interests+"','"+number+"','"+Username+"','"+password+"')"; 
    
             int i = st.executeUpdate(sql);
       
     System.out.println("Welcome "+name);
 }
 
 private static String read(String message) throws IOException{
     InputStreamReader isr = new InputStreamReader(System.in);
     BufferedReader br = new BufferedReader(isr);
     System.out.println(message);
     String s = br.readLine();
     //System.out.println("Read reporting with S= "+s);
     return s;
     
 }
 }
 
