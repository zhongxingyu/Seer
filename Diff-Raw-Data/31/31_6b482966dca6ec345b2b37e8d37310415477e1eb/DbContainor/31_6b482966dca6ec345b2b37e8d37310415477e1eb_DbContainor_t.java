 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pojo;
 
 import java.sql.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 /**
  *
  * @author ANOOP
  */

 public abstract class DbContainor {
     public static String dbuser="socioware";
     public static String dbpwd="social";
     public static String dburl="jdbc:oracle:thin:@localhost:1521:XE";
       
     public static void loadDbDriver(){
         try{
             Class.forName("oracle.jdbc.OracleDriver");
         }
         catch(ClassNotFoundException cnfe){
         System.out.println("Can not load Driver class : "+cnfe.getMessage());
        }
     }
     public static java.sql.Date toSQLDate(String date) throws ParseException{
           java.sql.Date sqdate;
           java.util.Date temp;
           SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
           long t;
           temp=sdf.parse(date);
           t=temp.getTime();
           sqdate=new java.sql.Date(t);      
           return sqdate;
       }
     
     public static String getDate(){
         Calendar td = Calendar.getInstance();
                 int day=td.get(Calendar.DATE);
                 int mon=td.get(Calendar.MONTH)+1;
                 int year=td.get(Calendar.YEAR);
                 String date=day+"-"+mon+"-"+year;
                 return date;
     }
     public static String revString(String str){
         String revStr=null;
         String dd,mm,yyyy;
         int ln=str.length();
         yyyy=str.substring(0,str.indexOf("-"));
         mm=str.substring(str.indexOf("-"), str.indexOf("-"));
         dd=str.substring(str.indexOf("-"), ln);
         revStr=dd+mm+yyyy;
         return revStr;
     }
      public static String arrangePath(String path){
         String rpath=null;
         rpath=path.substring(0,path.indexOf("build"));
         System.out.println("rpath is : "+rpath);
         rpath+="web/uploads/";
        return rpath;
     }
 }
