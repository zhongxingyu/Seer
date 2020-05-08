 package swarm;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 
 import weibo4j.Friendships;
 import weibo4j.Users;
 import weibo4j.model.Paging;
 import weibo4j.model.User;
 import weibo4j.model.WeiboException;
 
 public class GetRelationship implements Runnable {
 	
 	public boolean getUserRelationship() 
 	{
 		boolean isRelationshipDone = true; 
 		Connection conUser = null;
 		Statement stmt;
 		ResultSet rset = null;
 		try {
 			conUser = PublicMethods.getConnection();
 			stmt = conUser.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
 			System.out.println(" Okay the connnection to mysql has been established..we are going to select id from users.....");
 			rset = stmt.executeQuery("select id from users where isRelationshipDone = 0 limit 80;");
 		} catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		Long userId = (long)0;
 		boolean isUserExist = true;
 		
 		try {
 				while(rset.next())
 				{			
 					isRelationshipDone = false;
 					isUserExist = true;
 					userId = rset.getLong(1);
 					System.out.println("userId: "+ userId); 
 					
 					Users um = new Users();
 					try{
 						User user = um.showUserById(userId+"");
 						//System.out.println(user.toString());
 					} catch(WeiboException e){
 						//e.printStackTrace();
 						String errorMsg = null;
 						errorMsg = e.getError();
 						System.out.println("User: "+errorMsg);
 						if((errorMsg != null) && (errorMsg.isEmpty() == false) && errorMsg.contains("User does not exist"))
 						{
 							isUserExist = false;
 						} 
 					}
 					
 					if(isUserExist)
 					{
 						String[] friendsList = null;
 						Friendships fm = new Friendships();
 						try {
 							friendsList = fm.getFriendsIds(userId+"",2000,0);
 							if(friendsList != null)		//just in case
 							{
 								int number = 0;
 								for(String friendId : friendsList){
									if((friendId != null) && (!friendId.isEmpty())) {
										number++;
										System.out.println(friendId+"");
										PublicMethods.insertRelationshipSql(conUser, Long.parseLong(friendId), userId);
									}					
 								}
 								System.out.println("in total friends number: "+number);
 							}					
 	
 						} catch (WeiboException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 							continue;
 						}
 						
 						PublicMethods.updateUsersRelationship(conUser, userId);
 						
 						try {
 							Thread.sleep(551);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					else		//if this user no longer exists, delete it from database
 					{
 						System.out.println("ooops, this user not exists any more, deleting~~~");
 						PublicMethods.deleteUser(conUser, userId);
 					} 
 				} 
 			} catch (SQLException e) {
 							// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 		try {
 			conUser.close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return isRelationshipDone;
 	}
 
 	@Override
 	public void run() {
 		while(getUserRelationship() == false);
 		
 		System.out.println("lalalala, the relationship of all the users have been stored");
 	}
 
 }
