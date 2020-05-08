 package com.rmkane.dominion;
 
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.rmkane.dominion.domain.User;
 import com.rmkane.dominion.manager.UserManager;
 
 /**
  * Based of:
  *   http://www.byteslounge.com/tutorials/spring-with-hibernate-persistence-and-transactions-example
  *   
  * @author ryan
  */
 public class Main {
 	public void test() {
 		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
 		UserManager userManager = (UserManager) ctx.getBean("userManagerImpl");
 		User user = (User) ctx.getBean("dummyUser");
 
 		// Insert a user.
 		userManager.insert(user);
 		System.out.println("User inserted!");
 
 		// Retrieve user by username.
 		user = userManager.retrieveUserByUsername(user.getUsername());
 		System.out.printf("\nUser fetched by username!\nId: %d\nUsername: %s\nName: %s\n",
 				user.getId(), user.getUsername(), user.getName());
 
		// Retrieve user by user id.
		user = userManager.retrieve(user.getId());
		System.out.printf("\nUser fetched by ID!\nId: %d\nUsername: %s\nName: %s\n",
				user.getId(), user.getUsername(), user.getName());
 
 		// Retrieve all users.
 		List<User> users = userManager.retrieveAll();
 		System.out.println("\nUser list fetched!\nUser count: " + users.size());
 
 		// Remove user.
 		userManager.deleteUserByUsername(user.getUsername());
 		System.out.println("User removed!");
 		
 		// Reset the auto_increment.
 		userManager.resetAutoIncrement();
 	}
 	
 	public static void main(String[] args) {
 		new Main().test();
 	}
 }
