 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pojo;
 import java.sql.*;
 import java.text.ParseException;
 import java.util.ArrayList;
 
 /**
  *
  * @author Ashish
  */
public class Message 
{
     private String msgid;
     private String senderid;
     private String receiverid;    
     private String msgdate;
     private String message;
     private String status;
 
     public Message(String msgid, String senderid, String receiverid, String msgdate, String message, String status) {
         this.msgid = msgid;
         this.senderid = senderid;
         this.receiverid = receiverid;
         this.msgdate = msgdate;
         
         this.message = message;
         this.status = status;
     }
 
     public Message() {
         this.msgid =new String();
         this.senderid =new String();
         this.receiverid =new String();
         this.msgdate = new String();
         
         this.message = new String();
         this.status = new String();
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(String message) {
         this.message = message;
     }
 
     public String getMsgDate() {
         return msgdate;
     }
 
     public void setMsgDate(String msgdate) {
         this.msgdate = msgdate;
     }
 
     public String getMsgid() {
         return msgid;
     }
 
     public void setMsgid(String msgid) {
         this.msgid = msgid;
     }
 
     public String getReceiverid() {
         return receiverid;
     }
 
     public void setReceiverid(String rceiverid) {
         this.receiverid = rceiverid;
     }
 
     public String getSenderid() {
         return senderid;
     }
 
     public void setSenderid(String senderid) {
         this.senderid = senderid;
     }
 
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
   
     Connection con=null;
     PreparedStatement ps=null;
     ResultSet rs1,rs=null;
     
  
     public boolean sendMessage(){
         boolean flag=false;
        DbContainor.loadDbDriver();
         try{
             con=DriverManager.getConnection(DbContainor.dburl,DbContainor.dbuser,DbContainor.dbpwd);
             ps=con.prepareStatement("insert into message values(?,?,?,?,?,?)");
             ps.setString(1, msgid);
             ps.setString(2, senderid);
             ps.setString(3,this.receiverid);
             try {
                 ps.setDate(4,DbContainor.toSQLDate(msgdate));
             } catch (ParseException ex) {
                 System.out.println("can not convert date in saveMessage() of Message : "+ex.getMessage());
             }
             
             ps.setString(6, this.message);
             ps.setString(5,this.status);
             int res=ps.executeUpdate();
             if(res>0){
                 System.out.println("Data Succefully inserted in message table");
                 flag=true;
                 con.close();
             }
             else
                 System.out.println("can not insert data in message table");
             con.close();
         }
         catch(SQLException sqle){
             System.out.println("sql error in saveMessage() : "+sqle.getMessage());
         }
             
         return flag;
     }
     
     public Message findMessageById(){
         Message msg=new Message();
         DbContainor.loadDbDriver();
         try{
             con=DriverManager.getConnection(DbContainor.dburl,DbContainor.dbuser,DbContainor.dbpwd);
             ps=con.prepareStatement("select * from message where msgid=?");
             ps.setString(1, msgid);
             rs=ps.executeQuery();
             msg.setMsgid(rs.getString(1));
             msg.setSenderid(rs.getString(2));
             msg.setReceiverid(rs.getString(3));
             msg.setMsgDate(rs.getDate(4).toString());
            
             msg.setMessage(rs.getString(6));
             msg.setStatus(rs.getString(5));
             con.close();
         }
         catch(SQLException sqle){
             System.out.println("sql error in findMessageById() : "+sqle.getMessage());
         }
         return msg;
     }
     
     public ArrayList<Message> findAllMessages(){
         ArrayList<Message> almsg=new ArrayList<Message>();
         DbContainor.loadDbDriver();
         try{
             con=DriverManager.getConnection(DbContainor.dburl,DbContainor.dbuser,DbContainor.dbpwd);
           
             ps=con.prepareStatement("select * from message where receiverid=?");
             ps.setString(1, this.receiverid);
             rs=ps.executeQuery();
              String qry="select fname,mname,lname,email from userinfo where email in (select SENDERID from message where RECEIVERID=?)";
             while(rs.next()){
                 Message msg=new Message();
                 msg.setMsgid(rs.getString(1));
                 
                 msg.setSenderid(rs.getString(2));
                 msg.setReceiverid(rs.getString(3));
                 msg.setMsgDate(rs.getDate(4).toString());
                
                 msg.setMessage(rs.getString(6));
                 msg.setStatus(rs.getString(5));
                 almsg.add(msg);
                    }
                    con.close();
          
         }
             catch(SQLException sqle){
             System.out.println("sql error in findAllMessages() : "+sqle.getMessage());
         }
         return almsg;
            }
     
     public boolean delMessage(){
         boolean flag=false;
         DbContainor.loadDbDriver();
         try{
             con=DriverManager.getConnection(DbContainor.dburl,DbContainor.dbuser,DbContainor.dbpwd);
             ps=con.prepareStatement("delete * from message where msgid=?");
             ps.setString(1, msgid);
             int res=ps.executeUpdate();
             if(res>0){
                 System.out.println("Record deleted from message table");
                 flag=true;
             }
             else
                 System.out.println("can not delete record form message.");
         }
          catch(SQLException sqle){
             System.out.println("sql error in delMessage() :"+sqle.getMessage());
         }
         return flag;
     }
 }
