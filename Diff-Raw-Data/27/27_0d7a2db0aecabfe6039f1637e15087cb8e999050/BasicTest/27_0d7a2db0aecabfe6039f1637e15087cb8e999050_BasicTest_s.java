 import models.*;
 
 import org.junit.*;
 
 import controllers.Application;
 
 import java.util.*;
 
 import play.test.*;
 
 public class BasicTest extends UnitTest {
 
     @Test
     public void aVeryImportantThingToTest() {
         assertEquals(2, 1 + 1);
     }
     
     @Test
     public void CreateUserTest(){
     	User user = new User("Marcel", "Altendeitering", "m.altendeitering@tum.de", "012345678", "Massi1000", "5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5");
     	User retrievedUser = User.findById(user.id);
     	assertEquals("Marcel", retrievedUser.first_name);
     	assertEquals("m.altendeitering@tum.de", retrievedUser.email);
     	assertEquals("5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5", retrievedUser.password);
     }
     
     @Test
     public void CheckModel(){
     	short stateShort = 1;
     	
     	// Fill the test with some data:
     	User user1 = new User("Sebastian", "Knoll", "sebastianknoll@mytum.de", "0987654321", "skTUM", "5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5");
     	User user2 = new User("Naitsabes", "Llonk", "naitsabesllonk@test.de", "9999911111", "naitsab", Application.getHash("54321", "SHA-256"));
     	Category topCategory1 = new Category ("Winter Sports", null);
     	Category topCategory2 = new Category ("Summer Sports", null);
     	Category category1 = new Category ("Skiing", topCategory1);
     	Category category2 = new Category ("Racebikes", topCategory2);
    	Article article1 = new Article("Atomic Ski REV 85 Pro", "This is a great ski.... . .. . .. . . .. . . .. . . .... ......... . ... .. .. .... . .... .. .... .. .. .. . ... .. ", user1, category1);
    	Article article2 = new Article("Red Bull Carbon Pro SL (2010)", "A top bike made of carbon with Shimano Ultegra and Mavic Cosmic Carbon wheeles.", user2, category2);
     	Offer offer1 = new Offer("TUM Campus Garching", false, stateShort, 10, "I'm renting this ski during the semester because I have no time to go skiing.", new Date(1371417204236l), new Date(1371427572236l), article1);
     	Offer offer2 = new Offer("TUM Campus Garching", false, stateShort, 10, "I'm renting this ski during the semester because I have no time to go skiing.", new Date(1371427572236l), new Date(1371437940236l), article1);
     	Offer offer3 = new Offer("TUM Campus Garching", false, stateShort, 10, "I'm renting this ski during the semester because I have no time to go skiing.", new Date(1371448308236l), new Date(1371458676236l), article1);
     	Offer offer4 = new Offer("TUM Campus Hochbrück", true, stateShort, 30, "Unfortunatly I broke my leg and can't ride my bike the next months. So if you're interessted in riding a real racebike, here's your chance.", new Date(1371417204236l), new Date(1371427572236l), article2);
     	Request request1 = new Request (stateShort,100,new Date(1371417204236l),new Date(1371407700236l),offer1, user2);
     	Request request2 = new Request (stateShort,100,new Date(1371427485836l),new Date(1371427572236l),offer1, user2);
     	Request request3 = new Request (stateShort,50,new Date(1371427572236l),new Date(1371407268236l),offer2, user2);
     	Request request4 = new Request (stateShort,60,new Date(1371417204236l),new Date(1371417377036l),offer4, user1);
     	Request request5 = new Request (stateShort,60,new Date(1371417549836l),new Date(1371417722636l),offer4, user1);
     	Request request6 = new Request (stateShort,60,new Date(1371418068236l),new Date(1371418241036l),offer4, user1);
     	Request request7 = new Request (stateShort,60,new Date(1371418759436l),new Date(1371418932236l),offer4, user1);
     	
     	// Retrieve the generated data
     	User retrievedUser1 = User.findById(user1.id);
     	User retrievedUser2 = User.findById(user2.id);
     	Category retrievedCategory1 = Category.findById(category1.id);
     	Category retrievedCategory2 = Category.findById(category2.id);
     	Article retrievedArticle1 = Article.findById(article1.id);
     	Article retrievedArticle2 = Article.findById(article2.id);
     	Offer retrievedOffer1 = Offer.findById(offer1.id);
     	Offer retrievedOffer2 = Offer.findById(offer2.id);
     	Offer retrievedOffer3 = Offer.findById(offer3.id);
     	Offer retrievedOffer4 = Offer.findById(offer4.id);
     	Request retrievedRequest1 = Request.findById(request1.id);
     	Request retrievedRequest2 = Request.findById(request2.id);
     	Request retrievedRequest3 = Request.findById(request3.id);
     	Request retrievedRequest4 = Request.findById(request4.id);
     	Request retrievedRequest5 = Request.findById(request5.id);
     	Request retrievedRequest6 = Request.findById(request6.id);
     	Request retrievedRequest7 = Request.findById(request7.id);
     	
     	// Run the tests:
     	// --------------------------------------------------------------------------------
     	// Check if relationships are working:
     	assertEquals("skTUM",retrievedRequest1.offer.article.owner.nick_name);
 
     	// Find all offers of one user:
     	int i = 0;
     	for (Offer offer : User.getAllOffers(user1)){
     		assertEquals("skTUM", offer.article.owner.nick_name);
     		i++;
     	}
     	assertEquals(3, i);
     	
     	// Find all requested offers of one user:
     	i = 0;
     	for (Request request : User.getAllOfferRequests(user1)){
     		assertEquals("skTUM", request.offer.article.owner.nick_name);
     		assertEquals("naitsab", request.user.nick_name);
     		i++;
     	}
     	assertEquals(3, i);
     	
     }
     
     @Test
     public void CheckHashFunction(){
     	String correctHashOf12345 = "5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5";
     	String actualHash = Application.getHash("12345", "SHA-256");
     	assertEquals(correctHashOf12345, actualHash);
     }
 
 }
