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
 public class NgoLogin
 {
 	private String nid;
 	private String pwd;
 
 	public void setPwd(String pwd)
 	{
 		this.pwd = pwd;
 	}
 	public void setNid(String nid)
 	{
 		this.nid = nid;
 	}
 	public String getPwd()
 	{
 		return pwd;
 	}
 	public String getNid()
 	{
 		return nid;
 	}
 	
 	PreparedStatement ps=null; 
 	ResultSet rs=null;
 	Connection con=null;
 	
 	public boolean isValidNgo()
 	{ 
 		boolean ret_val=false;
 		System.out.println("in is validNgo  methos of userlogin class.");
 		DbContainor.loadDbDriver();
         
 		try
 		{
 			con=DriverManager.getConnection(DbContainor.dburl,DbContainor.dbuser,DbContainor.dbpwd);
 			ps=con.prepareStatement("select EMail,Password from ngoinfo where EMail=? and Password=?");
 			ps.setString(1,nid);
 			ps.setString(2, pwd);
 			ps.execute();
 			System.out.println("command is successfully executed");
			if(ps.executeQuery().next())
 			{
				ret_val=true;
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
