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
 public class PublishStatus {
     private String Name;
     private String unid;
     private String updateDate;
     private String content;
     private String report;
     private int likes;
     private String statusId;
 
     public String getName() {
         return Name;
     }
 
     public void setName(String Name) {
         this.Name = Name;
     }
     
     public int getLikes() {
         return likes;
     }
 
     public void setLikes(int likes) {
         //System.out.println("like is : "+likes);
         this.likes = likes;
     }
 
     public String getStatusId() {
         return statusId;
     }
 
     public void setStatusId(String statusId) {
         this.statusId = statusId;
     }
     
     Connection con=null;
     PreparedStatement ps=null;
     Statement stmt1;
     ResultSet rs=null;
     
     public void setContent(String content) {
     //  System.out.println("content is : "+content);
         this.content =content;
     }
     public String getContent() {
     
         return content;
     }
 
  public PublishStatus(){
    
      report=null;
      unid=null;
      updateDate=null;
      likes=0;
      content=null;
      statusId=null;
             }
     public PublishStatus(String statusid,String unid, String updateDate, String content, String report) {
         
         this.statusId=statusid;
         this.unid = unid;
         this.updateDate = updateDate;
         this.content = content;
         this.report = report;
     }
     
 
     public String getReport() {
         return report;
     }
 
     public void setReport(String report) {
         this.report = report;
     }
 
     public String getUpdateDate() {
         return updateDate;
     }
 
     public void setUpdateDate(String updateDate) {
        // System.out.println("ypdate date is : "+updateDate);
         this.updateDate = updateDate;
     }
 
     public void setUnid(String unid) {
         this.unid = unid;
     }
   
 
     public String getUnid() {
         return unid;
     }
     
     
     public boolean saveStatus(){
         boolean flag=false;
         DbContainor.loadDbDriver();
         
        try{
           con=DriverManager.getConnection(DbContainor.dburl,DbContainor.dbuser,DbContainor.dbpwd);
            ps=con.prepareStatement("insert into publishstatus values(?,?,?,?,?,?)");
            ps.setString(1, unid);
             try {
                 ps.setDate(2, DbContainor.toSQLDate(updateDate));
             } catch (ParseException ex) {
                 System.out.println("can not convert date in saveStatus() of publishStatus.java : "+ex.getMessage());
             }
             ps.setString(3,report);
            
             ps.setInt(4, likes);
             ps.setString(5, statusId);
              ps.setString(6,content);
             int res=ps.executeUpdate();
             if(res>0){
                 System.out.println("status published succefully.");
                 flag=true;
             }
             else
                 System.out.println("can not publish status");
             con.close();
        }
        catch(SQLException sqe){
            System.out.println("Sql error : "+sqe.getMessage());
        }
        return flag;
     }
      public boolean delStatus(){
          boolean flag=false;
          DbContainor.loadDbDriver();
          try{
              con=DriverManager.getConnection(DbContainor.dburl ,DbContainor.dbuser,DbContainor.dbpwd);
              ps=con.prepareStatement("delete  from publishstatus where statusId=?");
              ps.setString(1, statusId);
              int res=ps.executeUpdate();
              if(res>0){
                  System.out.println("suceefully deleted from publishstatus table.");
                  flag=true;
              }
              else
                  System.out.println("can not delete data from publishstatus table");
              con.close();
          }
          catch(SQLException sqle){
              System.out.println("SQL Error in delStatus() of Staus.java : "+sqle.getMessage());
          }
          return flag;
      }
  
      public   ArrayList<PublishStatus> findAllStatus(){
         
         ArrayList<PublishStatus> pbls=new ArrayList<PublishStatus>();
          DbContainor.loadDbDriver();
          try{
              con=DriverManager.getConnection(DbContainor.dburl,DbContainor.dbuser,DbContainor.dbpwd);
    
         // String qry="select statusId,status,likes,updatedate from publishstatus where unid in(select friendlist.friendid,joins.ngoid from friendlist,joins where friendlist.userid=? and joins.unid=?)";
          String qry="select statusId,status,likes,updatedate from publishstatus where unid in(select friendlist.friendid from friendlist where friendlist.userid=?)";
              ps=con.prepareStatement(qry);
              //System.out.println("unid is  : "+unid);
              ps.setString(1,unid);
             //ps.setString(2, unid);
             rs=ps.executeQuery();
             while(rs.next()){
                  PublishStatus pb=new  PublishStatus();
                 
                 pb.setStatusId(rs.getString(1)); 
                 //String content=rs.getString(2);
                 //System.out.println("data frm db is : "+content);
                
               //pb.setContent(rs.getString(2));
                pb.setContent(rs.getString(2));
                 pb.setLikes(rs.getInt(3));
                 pb.setUpdateDate(rs.getDate(4).toString());
                 pbls.add(pb);
                }
             // System.out.println("araay list prepared");
             con.close();            
          }
          catch(SQLException sqle){
              System.out.println("SQL error in findAllStatus() :"+sqle.getMessage());
             }
          return pbls;
      }
     
 }
