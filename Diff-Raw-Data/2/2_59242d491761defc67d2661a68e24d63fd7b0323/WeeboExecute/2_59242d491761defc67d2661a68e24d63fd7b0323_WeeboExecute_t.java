 package weebo;
 
 import java.util.List;
 
 import vo.*;
 
 public interface WeeboExecute {
 	
	
	
 	//typebuzzsina
 	public String type = "";
 	
 	//getHomePostȡûҳ΢, num ָض΢
 	public List<MicroBlog> getHomePost(int num);
 	
 	//getUserPostȡָû΢, num ָض΢
 	public List<MicroBlog> getUserPost(int num, int userID);
 	
 	//getPublicPostȡǰҳ΢, num ָض΢
 	public List<MicroBlog> getPublicPost(int num);
 
 	//postһ΢userIDʶûIDcontentʶ
 	public void post(int userID, String content);
 	
 	//post͵΢postͼƬ΢ݲͳһӿ
 	
 	//getUserInfoȡûϢ
 	public UserInfo getUserInfo(int userID);
 	
 	//getFollowersȡûfollower
 	public List<UserInfo> getFollowers(int userID);
 	
 	public List<UserInfo> getFollowings(int userID);
 	
 	//getCommentsȡָblogĻظ
 	public List<Comment> getComments(int blogID);
 }	
