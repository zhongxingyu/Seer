 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pojo;
 import java.sql.*;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Ajit Gupta 
  */
 public class FriendList 
 {
 	protected String userid;
 	protected String friendid;
 	protected String name;
 	protected String userimage;
 
 	public String getUserimage()
 	{
 		return userimage;
 	}
 	public void setUserimage(String userimage)
 	{
 		this.userimage = userimage;
 	}
 	public String getFriendid()
 	{
 		return friendid;
 	}
 	public void setFriendid(String friendid)
 	{
 		this.friendid = friendid;
 	}
 	public String getName()
 	{
 		return name;
 	}
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	public String getUserid()
 	{
 		return userid;
 	}
 	public void setUserid(String userid)
 	{
 		this.userid = userid;
 	}
     
 	public FriendList()
 	{
 		userid = new String();
 		friendid = new String();
 		name = new String();
 	}
 	public FriendList(String userid, String friendid,String name)
 	{
 		this.userid = userid;
 		this.friendid = friendid;
 		this.name = name;
 	}
 	
 	public ArrayList<FriendList> getFriendList()
 	{
 		ArrayList<FriendList> f_list = new ArrayList();
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
 			
 			query = "Select fname, mname, lname,EMail ,USERIMAGE from userinfo where EMail in (Select friendid from friendlist where userid=?)";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1, userid);
 			ResultSet rs=ps.executeQuery();
 			
 			while(rs.next())
 			{
 				FriendList flst = new FriendList();
 				String mname = rs.getString(2);
 				/* System.out.println("mname is  :" +mname); */
 				if(mname==null)
 				{
 					mname=" ";
 				}
 				flst.setName(rs.getString(1)+" "+mname+" "+rs.getString(3));
 				flst.setFriendid(rs.getString(4));
 				flst.setUserimage(rs.getString(5));
 				f_list.add(flst);
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("sql error in getFriendlist of FrienList.java : " + sqle.getMessage());
 		}
 		return f_list;
 	}
     
 	public boolean updateFriendList()
 	{
 		boolean ret_val =false;
 		String query = null;
 		DbContainor.loadDbDriver();
 		try
 		{
 			
 			query = "insert into friendlist values(?,?)";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,userid);
 			ps.setString(2, friendid);
 			if(ps.executeUpdate()>0)
 			{
 				System.out.println("Data Succesfully inserted into FriendList table  ");
 				ret_val = true;
 			}
 			else
 			{
 				System.out.println("Could not insert data into FriendList table.");
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("sql error in createFriendList() of FriendList.java : " + sqle.getMessage());
 		}
 		return ret_val;
 	}
        public boolean isFriend()
        {
            boolean ret_val = false;    
            String query = null;
            DbContainor.loadDbDriver();
            System.out.println("in isFriend ...");
            try
            {
                query = "select * from friendlist where USERID=? and FRIENDID=? ";
                Connection con = DbContainor.createConnection();
                PreparedStatement ps = con.prepareStatement(query);  
                ps.setString(1, this.userid);
                ps.setString(2, this.friendid);
                ResultSet rs = ps.executeQuery();
                if(rs.next())
                    ret_val = true;
            }
            catch(NullPointerException npe)
            {
		System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
            }
            catch(SQLException sqle)
            {
		System.out.println("SQL Error in isFriend() of FriendRequest.java  :"+ sqle.getMessage());
            }
            return ret_val;
         }
 }
