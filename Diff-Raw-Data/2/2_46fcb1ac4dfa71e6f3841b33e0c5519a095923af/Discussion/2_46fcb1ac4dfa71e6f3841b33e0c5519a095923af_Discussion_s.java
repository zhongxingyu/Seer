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
  * @author HP
  */
 public class Discussion
 {
 	protected String discid;
 	protected String topic;
 	protected String topicdate;
 	protected String topicdesc;
 	
 	public String getDiscid()
 	{
         return discid;
     }
 
 	public void setDiscid(String discid)
 	{
 		this.discid = discid;
 	}
 	public String getTopic()
 	{
 		return topic;
 	}
 	public void setTopic(String topic)
 	{
 		this.topic = topic;
 	}
 	public String getTopicdate()
 	{
 		return topicdate;
 	}
 	public void setTopicdate(String topicdate)
 	{
 		this.topicdate = topicdate;
 	}
 	public String getTopicdesc()
 	{
 		return topicdesc;
 	}
 	public void setTopicdesc(String topicdesc)
 	{
 		this.topicdesc = topicdesc;
 	}
 
 	public Discussion()
 	{
 		discid = new String();
 		topic = new String();
 		topicdate = new String();
 		topicdesc = new String();
 	}
 	public Discussion(String discid, String topic, String topicdate, String topicdesc)
 	{
 		this.discid = discid;
 		this.topic = topic;
 		this.topicdate = topicdate;
 		this.topicdesc = topicdesc;
 	}
     
 	public boolean createDiscussion()
 	{
 		boolean ret_val = false;
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
 			query = "insert into discussion values(?,?,?,?)";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,discid);
 			ps.setString(2, topic);
 			try
 			{
 				ps.setDate(3,DbContainor.toSQLDate(topicdate));
 			}
 			catch (ParseException ex)
 			{
 				System.out.println("can not convert date : "+ex.getMessage());
 			}
 			ps.setString(4,topicdesc);
 
 			if(ps.executeUpdate()>0)
 			{
 				System.out.println("Data Succesfully inserted into discussion table  ");
 				ret_val = true;
 			}
 			else
 			{
 				System.out.println("Could not insert data into discussion table.");
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("sql error in createDiscussion() of Discussion.java : " + sqle.getMessage());
 		}
 		return ret_val;
 	}
     
 	public boolean editDiscussion()
 	{
 		boolean ret_val = false;
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
 			query = "update discussion set topic=?, topicdate=?, topicdesc=? where discid=?";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1, topic);
 			try
 			{
 				ps.setDate(2,DbContainor.toSQLDate(topicdate));
 			}
 			catch (ParseException ex)
 			{
 				System.out.println("can not convert date : "+ex.getMessage());
 			}
 			ps.setString(3,topicdesc);
 			ps.setString(4,discid);
 			if(ps.executeUpdate()>0)
 			{
 				System.out.println("Data Succesfully updated into discussion table  ");
 				ret_val = true;
 			}
 			else
 			{
 				System.out.println("Could not update data into discussion table.");
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("sql error in editDiscussion() of Discussion.java : " + sqle.getMessage());
 		}
 		return ret_val;
 	}
      
 	public boolean deleteDiscussion()
 	{
 		boolean ret_val = false;
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
 			query = "delete from discussion where discid=?";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,discid);
 			if(ps.executeUpdate()>0)
 			{
 				System.out.println("Data Succesfully deleted into discussion table  ");
 				ret_val = true;
 			}
 			else
 			{
 				System.out.println("Could not update data into discussion table.");
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("sql error in editDiscussion() of Discussion.java : " + sqle.getMessage());
 		}
 		return ret_val;            
 	}
     
 	public  ArrayList<Discussion> findAllDiscussion()
 	{
 		ArrayList<Discussion> disc_list = new ArrayList<Discussion>();
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
 			query = "Select * from discussion";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);

 			while(rs.next())
 			{
 				Discussion disc = new Discussion();
 				disc.setDiscid(rs.getString("discid"));
 				disc.setTopic(rs.getString("topic"));
 				disc.setTopicdate(rs.getString("topicdate"));
 				disc.setTopicdesc(rs.getString("topicdesc"));
 				disc_list.add(disc);
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("ql error in findAllDiscussion() of Discussion.java : " + sqle.getMessage());
 		}
 		return disc_list;
 	}
     
 	public Discussion findDiscussion()
 	{
 		Discussion disc = new Discussion();
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
 			
 			query = "Select * from discussion where discid=?";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1, discid);
 			ResultSet rs = ps.executeQuery();
 			if(rs.next())
 			{
 				disc.setDiscid(rs.getString("discid"));
 				disc.setTopic(rs.getString("topic"));
 				disc.setTopicdate(rs.getString("topicdate"));
 				disc.setTopicdesc(rs.getString("topicdesc"));
 			}
 		con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("sql error in findDiscussion() of Discussion.java : " + sqle.getMessage());
 		}           
 		return disc;
 	}
 }
