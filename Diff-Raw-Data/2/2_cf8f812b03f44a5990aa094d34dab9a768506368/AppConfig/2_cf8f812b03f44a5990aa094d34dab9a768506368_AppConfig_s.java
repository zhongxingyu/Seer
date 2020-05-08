 package edu.upc.dsbw.spring;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import edu.upc.dsbw.spring.business.BooksController;
 import edu.upc.dsbw.spring.business.LoginController;
 
 import java.util.LinkedList;
 
 @Configuration
 public class AppConfig {
 
 	@Bean
 	public BooksController getBooksController(){ // atribut class=BooksController name=booksController (sense get i amb minuscules)
 		BooksController booksController = new BooksController();
 		long martinFowlerId = booksController.addAuthor("Martin", "Fowler");
                 
         //LinkedList<Integer> l_ratings = new LinkedList<Integer>();
         //l_ratings.add(4);l_ratings.add(2);l_ratings.add(3);l_ratings.add(1);
         String description = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?";
                 
		booksController.addBook(martinFowlerId, "Patterns of Enterprise Application Architecture", 2003, description, l_ratings);
 		booksController.addBook(martinFowlerId, "Refactoring: Improving the Design of Existing Code", 1999, description);
 		booksController.addBook(martinFowlerId, "UML distilled: a brief guide to the standard object modeling language", 2004, description);
 		long kentBeckId = booksController.addAuthor("Kent", "Beck");
 		booksController.addBook(kentBeckId, "Test-driven Development by example", 2003, "waffle, pancake");
 		
 		booksController.addUser("johnnychoke", "Johnny Choke");
 		booksController.addUser("paulanka", "Paul Anka");
 		
 		booksController.addVote(new Long(0), "johnnychoke", 4);
 		booksController.addVote(new Long(0), "paulanka", 2);
   
 		booksController.addVote(new Long(1), "johnnychoke", 4);
 		booksController.addVote(new Long(1), "paulanka", 2);
 		booksController.addVote(new Long(1), "paulanka1", 2);
 		booksController.addVote(new Long(1), "paulanka2", 3);
 		booksController.addVote(new Long(1), "paulanka3", 5);
 		booksController.addVote(new Long(1), "paulanka4", 1);
 		booksController.addVote(new Long(1), "paulanka5", 2);
 		booksController.addVote(new Long(1), "paulanka6", 1);
 		
 		return booksController;
 	}
 	
 	@Bean
 	public LoginController getLogin(){
 		LoginController login = new LoginController();
 		return login;
 	}
         
 }
