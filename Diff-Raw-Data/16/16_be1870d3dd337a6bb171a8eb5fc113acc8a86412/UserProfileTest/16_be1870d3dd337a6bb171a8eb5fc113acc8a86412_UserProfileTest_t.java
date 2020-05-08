 
 import org.junit.*;
 
 import groovy.ui.text.FindReplaceUtility;
 
 import java.util.*;
 
 import play.db.jpa.JPABase;
 import play.test.*;
 import models.*;
 
 public class UserProfileTest extends UnitTest {
 
 	static String username = "telsbot";
 	static String password = "secret";
 	static String fullname = "Mr. Tels J. Bot";
 	static String email = "telsbot@robot.com";
 	static String description = "I am me";
 	static String status = "Working on the railroad.";
   	static String firstName = "tels j.";
 	static String lastName = "bot";
 	static String gender = "male";
 	static String phonenumber = "98989988";
 	
 	@Before
 	public void setup() {
 		 Fixtures.deleteAll();
 	}
 	 
 	@Test
 	public void createAndRetrieveUser() {
 	    // Create a new user and save it
 	    new User(username,password).save();
 	    
 	    // Retrieve the user with e-mail address bob@gmail.com
 	    User telsbot = User.findUserByUsername(username);
 	    
 	    // Test 
 	    assertNotNull(telsbot);
 	}
 
     @Test
     public void tryConnectAsUser() {
         // Create a new user and save it
         new User(email, password).save();
         
         // Test 
         assertNotNull(User.connect(email, password));
         assertNull(User.connect(email, "badpassword"));
         assertNull(User.connect("tom@gmail.com", "secret"));
     }
     
   
     
     @Test
     public void createBasicProfileWithUser() {
         // Create a new user and save it
     	
     	Profile p = getProfile();
         // Create a new post
       
         p.save();
         
         // Test that the post has been created
         assertEquals(1, Profile.count());
         
         // Retrieve all posts created by Bob
         Profile userProfile = Profile.findProfileByUser(p.user);
         
         // Tests
         assertNotNull(userProfile);
         assertEquals(p.user, userProfile.user);
     
     }
     
     @Test
     public void createUserWithQuestionaires() {
 //    	User user = getUser();
 //    	
 //    	List<Question> questions = QuestionTest.getQuestions();
 //    	
 //    	Questionaire qa = new Questionaire("user experiences");
 //    	for (Question question : questions) {
 //			qa.questions.add(question);
 //		}
 //    	qa.save();
 //    	
 //    	
 //    	QuestionaireAnswer answers = new QuestionaireAnswer(qa);
 //    	
 //    	String a1 = "this is good";
 //    	String a2 = "yo yo yo";
 //    	
 //    	answers.questionAnswers.put(qa.questions.get(0), a1);
 //    	answers.questionAnswers.put(qa.questions.get(1), a2);
 //    	
 //    	answers.hasAnswered = true;
 //    	answers.save();
 //    	
 //    	QuestionaireAnswer found = QuestionaireAnswer.findById(answers.id);
 //    	
 //    	assertEquals(a1, found.questionAnswers.get(found.questionaire.questions.get(0)));
 //    	assertEquals(a2, found.questionAnswers.get(found.questionaire.questions.get(1)));
 //    	assertEquals(true, found.hasAnswered);
 //    	
 //    	user.questionaireAnswers.add(found);
 //    	user.save();
 //    	
 //    	User foundUser = user.findById(user.id);
 //    	
 //    	assertEquals(foundUser.questionaireAnswers.get(0), found);
 //    	user.questionaires.add(qa);
 //    	user.save();
 //    	
 //    	User found = User.findById(user.id);
 //    	
 //    	assertEquals(found.questionaires.size(), 1);
     	
     	
     	
     }
     
    @Test
     public void profilePropertiesWithUser() {
 		Profile profile = getProfile();
 		profile.gender = gender;
 		profile.phonenumber = phonenumber;
 		profile.description = description;
 		profile.status = status;
 		profile.isComplete = true;
 		profile.save();
 		
 		Profile found = Profile.findById(profile.id);
 		
 	    assertEquals(firstName, found.firstname);
         assertEquals(lastName, found.lastname);
         assertEquals(description, found.description);
         assertEquals(status, found.status);
         
         assertEquals(gender, found.gender);
         assertEquals(phonenumber, found.phonenumber);
         
         assertNotNull(found.timestamp);
         assertTrue(found.isComplete);
     }
     
     public static User getUser() {
         User user = new User(username, password).save();
         return user;
     }
     
     public static Profile getProfile() {
         Profile p = new Profile(getUser(), firstName,lastName).save() ;
         return p;
     }
 }
