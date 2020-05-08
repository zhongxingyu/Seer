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
 public class PublishStatus
 {
 	private String Name;
 	private String unid;
 	private String updateDate;
 	private String content;
 	private String report;
 	private int likes;
 	private String statusId;
 
 	public String getName()
 	{
 		return Name;
 	}
 	public void setName(String Name)
 	{
 		this.Name = Name;
 	}
 	public int getLikes()
 	{
 		return likes;
 	}
 	public void setLikes(int likes)
 	{
 		this.likes = likes;
 	}
 	public String getStatusId()
 	{
 		return statusId;
 	}
 	public void setStatusId(String statusId)
 	{
 		this.statusId = statusId;
 	}
 	public void setContent(String content)
 	{
 		this.content =content;
 	}
 	public String getContent()
 	{
 		return content;
 	}
 
 	public String getReport()
 	{
 		return report;
 	}
 	public void setReport(String report)
 	{
 		this.report = report;
 	}
 
 	public String getUpdateDate()
 	{
 		return updateDate;
 	}
 	public void setUpdateDate(String updateDate)
 	{
 		this.updateDate = updateDate;
 	}
 	public void setUnid(String unid)
 	{
 		this.unid = unid;
 	}
 	public String getUnid()
 	{
 		return unid;
 	}
   
 	public PublishStatus()
 	{
 		this.report = null;
 		this.unid = null;
 		this.updateDate = null;
 		this.likes = 0;
 		this.content = null;
 		this.statusId = null;
 	}
 	public PublishStatus(String statusid,String unid, String updateDate, String content, String report)
 	{
 		this.statusId=statusid;
 		this.unid = unid;
 		this.updateDate = updateDate;
 		this.content = content;
 		this.report = report;
     }  
 	public boolean saveStatus()
 	{
 		boolean ret_val = false;
 		String query = null;
 		DbContainor.loadDbDriver();
         
 		try
 		{
 			query = "insert into publishstatus values(?,?,?,?,?,?)";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1, unid);
             
 			try
 			{
                 ps.setDate(2, DbContainor.toSQLDate(updateDate));
 			}
 			catch (ParseException ex)
 			{
 				System.out.println("can not convert date in saveStatus() of publishStatus.java : "+ex.getMessage());
 			}
 			ps.setString(3,report);
 			ps.setInt(4, likes);
 			ps.setString(5, statusId);
 			ps.setString(6,content);
 
             if(ps.executeUpdate()>0)
 			{
 				System.out.println("status published succefully.");
 				ret_val = true;
 			}
 			else
 			{
 				System.out.println("can not publish status");
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqe)
 		{
 			System.out.println("Sql error : "+sqe.getMessage());
 		}
 		return ret_val;
 	}
      
 	public boolean delStatus()
 	{
 		boolean ret_val = false;
 		String query = null;
 		DbContainor.loadDbDriver();
         
 		try
 		{
 			query = "delete  from publishstatus where statusId=?";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1, statusId);
 
 			if(ps.executeUpdate()>0)
 			{
 				System.out.println("suceefully deleted from publishstatus table.");
 				ret_val = true;
 			}
 			else
 			{
 				System.out.println("can not delete data from publishstatus table");
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("SQL Error in delStatus() of Staus.java : "+sqle.getMessage());
 		}
 		return ret_val;
 	}
  
 	public   ArrayList<PublishStatus> findAllStatus()
 	{
 		ArrayList<PublishStatus> status_list = new ArrayList<PublishStatus>();
 		String query = null;
 		DbContainor.loadDbDriver();
          
 		try
 		{
			query = "select statusId,status,likes,updatedate from publishstatus where unid in(select friendlist.friendid from friendlist where"+ "friendlist.userid=?)";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,unid);
 			ResultSet rs = ps.executeQuery();
             
 			while(rs.next())
 			{
 				PublishStatus pb_status = new  PublishStatus();
 				pb_status.setStatusId(rs.getString(1));
 				pb_status.setContent(rs.getString(2));
 				pb_status.setLikes(rs.getInt(3));
 				pb_status.setUpdateDate(rs.getDate(4).toString());
 				status_list.add(pb_status);
 			}
 			/* System.out.println("array list prepared");*/
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("SQL error in findAllStatus() :"+sqle.getMessage());
 		}
 		return status_list;
 	}
 }
