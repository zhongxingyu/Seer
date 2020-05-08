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
 public class FriendRequest
 {
 	private String reqSender;
 	private String reqReciever;
 	private String msg;
 	private String reqdate;
 	private String status;
 	private String reqid;
 	private String name;
 	private String email;
         private String image;
 	
 	public String getEmail()
 	{
 		return email;
 	}
 	public void setEmail(String email)
 	{
 		this.email = email;
 	}
 	public String getName()
 	{
	return name;
 	}
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	public String getReqid()
 	{
 		return reqid;
     }
 	public void setReqid(String reqid)
 	{
 		this.reqid = reqid;
 	}
 	public String getReqdate()
 	{
 		return reqdate;
 	}
 	public void setReqdate(String reqdate)
 	{
 		this.reqdate = reqdate;
 	}
 	public void setMsg(String msg)
 	{
 		this.msg = msg;
 	}
 	public void setReqReciever(String reqReciever)
 	{
 		this.reqReciever = reqReciever;
 	}
 	public void setReqSender(String reqSender)
 	{
 		this.reqSender = reqSender;
 	}
 	public String getMsg()
 	{
 		return msg;
 	}
 	public String getReqReciever()
 	{
 		return reqReciever;
 	}
 	public String getReqSender()
 	{
 		return reqSender;
 	}
 	public String getStatus()
 	{
 		return status;
 	}
         public String getImage()
         {
             return image;
         }
         public void setImage(String image)
         {
             this.image = image;
         }
         
 	public void setStatus(String status)
 	{
             this.status=status;
         }
 	
 	public boolean sendRequest()
 	{
 		boolean ret_val = false;
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
                         query = "insert into friendrequest values(?,?,?,?,?,?)";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,this.getReqid());
 			ps.setString(2, this.getReqSender());
 			ps.setString(3,this.getReqReciever());
 			try
 			{
 				ps.setDate(4,DbContainor.toSQLDate(this.getReqdate()));
 			}
 			catch (ParseException ex)
 			{
 				System.out.println("can not convert date : "+ex.getMessage());
 			}
 			ps.setString(5,this.getMsg());
 			ps.setString(6,this.getStatus());
 			if(ps.executeUpdate()>0)
 			{
 				System.out.println("Data Succesfully updated into FriendRequest table  ");
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
 			System.out.println("SQL Error in sendRequest() of FriendRequest.java : "+sqle.getMessage());
 		}
 		return ret_val;  
 	}
     
 	public ArrayList<FriendRequest> findReceivedRequest()
 	{
                 String query = null;
                 ArrayList<FriendRequest> frnd_req_list = new ArrayList<FriendRequest>();
 		DbContainor.loadDbDriver();
 		try
 		{
 			query = "select fname, mname, lname, email, userimage from userinfo where email in (select reqsender from friendrequest where REQRECEIVER=? and status='Pending')";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,reqReciever);
 			ResultSet rs = ps.executeQuery();
 			while(rs.next())
 			{
                                 FriendRequest frnd_req = new FriendRequest();	
                                 String mname = rs.getString("mname");
 				/* System.out.println("mname is  :" +mname); */
 				if(mname==null)
 				{
 					mname=" ";
 				}
 				frnd_req.setName(rs.getString("fname")+" "+mname+" "+rs.getString("lname"));
 				frnd_req.setEmail(rs.getString("email"));
                                 frnd_req.setImage(rs.getString("userimage"));
                                 frnd_req_list.add(frnd_req);
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("SQL Error in findRecievedRequest() of FriendRequest.java  :"+ sqle.getMessage());
 		}		
 		return frnd_req_list;
 	}
      
 	public boolean updateRequest()
 	{
 		boolean ret_val = false;
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
 			query = "update friendrequest set status='confirmed' where reqsender=? and reqreceiver=?";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,reqSender);
 			ps.setString(2,reqReciever);
 			if(ps.executeUpdate()>0)
 			{
 				System.out.println("friendrequest table updated successfully in updateRequest() in class FriendRequest.java");
 				ret_val = true;
 			}
 			else
 			{
 				System.out.println("c'ldn't update friendrequest table  in updateRequest() in class FriendRequest.java");
 			}
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("SQL Error in updateRequest() of FriendRequest.java  :"+ sqle.getMessage());
 		}			
 		return ret_val;
 	}
 }
