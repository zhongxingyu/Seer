 package debug;
 
 import interfaces.Model;
 
 import java.util.ArrayList;
 
 import dao.Comments;
 import dao.Receipes;
 import dao.Users;
 import beans.Comment;
 import beans.Receipe;
 import beans.User;
 
 public class TestUser {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//int id, String title, String sumup, String desc, String imgRef, Integer nbPers, String cookType, Integer cookExp, Integer duration
 		Users Dao = new Users();
		ArrayList<User> users;
 		User i = new User(42, "aaaaaaaarLogin", "aPwd", "aName", "aForname", 24, "awesome@mail.adress");
 		
 		Dao.create(i);
 		
 		System.out.println("-------");
 		i = Dao.fromRow(Dao.getLast());
 		System.out.println(i);
 		System.out.println("#######");
		i.setRating(2);
 		Dao.update(i);
 		i = Dao.fromRow(Dao.getLast());
 		System.out.println(i);
 		System.out.println("#######");
 		
 		
 		
		comments = Dao.getCommentsByReceipe(1);	
 		Dao.delete(i);
 		//users = Dao.getItems();
		//*
		for(Model item : comments) {
 			System.out.println(item);
 		}
 		//*/
		
 	}
 	
 	
 
 }
