 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pojo;
 
 import java.sql.*;
 /**
  *
  * @author ANOOP
  */
 public class UserLogin
 {
 	private String uid;
 	private String pwd;
	PreparedStatement ps = null;
 	ResultSet rs = null;
	Connection con = null;
 
 	public void setPwd(String pwd)
 	{
         this.pwd = pwd;
 	}
 
 	public void setUid(String uid) 
 	{
 		this.uid = uid;
 	}
 
 	public String getUid()
 	{
 		return uid;
 	}
 
 	public String getPwd()
 	{
 		return pwd;
 	}
 
 	public boolean isValidUser()
 	{
 		boolean ret_val = false;
 		System.out.println("in isvalidusr methos of userlogin class.");
 		DbContainor.loadDbDriver();
 
 		try
 		{
			con = DriverManager.getConnection(DbContainor.dburl,DbContainor.dbuser,DbContainor.dbpwd);
			ps  = con.prepareStatement("select EMail,Password from userinfo where EMail=? and Password=?");
 			ps.setString(1,uid);
 			ps.setString(2, pwd);
 			ps.execute();
               
 			System.out.println("command is successfully executed");
 			while(ps.executeQuery().next())
 			{
 				ret_val = true;
 			}
 		con.close();
 		}
 
 		catch(SQLException sqle)
 		{
 			System.out.println("SQl Error Occured : "+sqle.getMessage());
              
 		}
        
 		return ret_val;
 	}
 }
