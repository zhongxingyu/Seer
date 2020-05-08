 package story.book;
 
 import android.app.Application;
 import android.content.Context;
 
 import story.book.dataclient.IOClient;
 import story.book.dataclient.ESClient;
 
 /**
  * Application class for aggregating the Singleton IOClient, ESClient, and
  * the active instance of Story. The current instance of Story, 
  * <code>currentStory</code>, is the active Story object which can be read, 
  * edited or published by an application controller.
  * 
  * @author 	Alexander Cheung
  * @see 	Story
  * @see		IOClient
  * @see		ESClient
  */
 public class StoryApplication extends Application {
 	
     // Singleton IO and ES clients
     transient private static IOClient io = null;
     transient private static ESClient es = null;
     
     private static Story currentStory;
     private static Context context;
     
     private static String nickname;
 
     public void onCreate(){
         super.onCreate();
         context = getApplicationContext();
         
         // DEBUG
         
 		StoryInfo info = new StoryInfo();
 		info.setAuthor("Daniel Andy");
 		info.setTitle("Broken Star");
 		info.setGenre("Science Fiction");
 		info.setSynopsis("The princess of a destroyed kingdom is left with no one to guide her, until she finds a fallen star with a secret inside....");
 		info.setPublishDate(new Date());
 		info.setSID(600);
 		
 		Story story = new Story(info);
 		StoryFragment fragment1 = new StoryFragment("Finding the Star");
 		// TODO
 		//TextIllustration text = new TextIllustration("It was a dark, clear night.");
 		//fragment1.addIllustration(text);
 		StoryFragment fragment2 = new StoryFragment("Preparing for the Journey");
 		//TextIllustration text2 = new TextIllustration("She ventured into the locked dungeons to retrieve some potions.");
 		//TextIllustration text3 = new TextIllustration("She could not carry everything, she had to choose between potion A and potion B.");
 		//fragment2.addIllustration(text2);
 		//fragment2.addIllustration(text3);
 		DecisionBranch branch = new DecisionBranch("She decides she must find the star.", fragment2);
 		//DecisionBranch branch2 = new DecisionBranch("She declares she is too weak to find the star.", fragment1);
 		fragment1.addDecisionBranch(branch);
 		//fragment2.addDecisionBranch(branch2);
 		
 		story.addFragment(fragment1);
 		story.addFragment(fragment2);
 		
 		setCurrentStory(story);
     }
     
     public static IOClient getIOClient() {
     	if (io == null) {
     		io = new IOClient(context);
     	}
     	return io;
     }
     
     public static ESClient getESClient() {
     	if (es == null) {
     		es = new ESClient();
     	}
     	return es;
     }
     
     public static Story getCurrentStory() {
     	return currentStory;
     }
     
     public static void setCurrentStory(Story story) {
     	currentStory = story;
     }
     
     public static Context getContext() {
     	return context;
     }
     
     public static void setNickname(String name) {
     	nickname = name;
     }
     
     public static String getNickname() {
     	if (nickname == null) {
     		return "Anonymous";
     	} else {
     		return nickname;
     	}
     }
 }
