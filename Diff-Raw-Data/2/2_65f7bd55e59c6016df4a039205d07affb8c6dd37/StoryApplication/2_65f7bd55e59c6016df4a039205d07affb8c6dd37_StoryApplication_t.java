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
         
        setCurrentStory(getESClient().getStory(600)); // DEBUG
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
