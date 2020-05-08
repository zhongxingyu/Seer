 package controllers;
 
 import java.util.Collections;
 import java.util.List;
 
 import com.google.gson.JsonObject;
 
 import models.Choice;
 import models.LikeFrequency;
 import models.LikeFrequencyComparator;
 import models.Reason;
 import models.Recommendation;
 import models.Topic;
 import models.User;
 
 import play.modules.facebook.FbGraph;
 import play.modules.facebook.FbGraphException;
 import play.mvc.Controller;
 import play.mvc.Scope.Session;
 
 public class RecommendationEngine extends Controller{
 
 	public static void index() {
 		RSSEngine.fetchNews();
     	Topic topic1 = fetchTopic();
     	Topic topic2 = fetchTopic();
  
     	Recommendation rec1 = new Recommendation(topic1);
     	Reason likeCategoryReason = Reason.getLikeCategoryReason();
     	rec1.addReason(likeCategoryReason);
     	rec1.save();
     	
     	Recommendation rec2 = new Recommendation(topic2);
     	rec2.addReason(likeCategoryReason);
     	rec2.save();
     	
     	Choice choice = new Choice();
     	choice.addRecommendation(rec1);
     	choice.addRecommendation(rec2);
     	choice.save();
     	
     	renderArgs.put("choice", choice);
     	renderTemplate("Recommendation/index.html");
 	}
 	
 	/* Choice Generation */
 	public static Choice getChoice() {
 		return getChoice(2);
 	}
 	
 	public static Choice getChoice(int numRecommendations) {
 		Choice choice = new Choice();
 		return choice;
 	}
 	
 	public static void fetchTopics() {
     	Topic topic1 = fetchTopic();
     	Topic topic2 = fetchTopic();
     }
     
     public static Topic fetchTopic() {
     	JsonObject profile;
    	User user = null;
 		
 		if (user != null) {
 			List<LikeFrequency> likeFrequencies = user.frequencyOfLikes;
 			Collections.sort(likeFrequencies, new LikeFrequencyComparator());
 			LikeFrequency lf = likeFrequencies.get(0);
 			String tag2 = lf.likeCategory;
			System.out.println(tag2);
 		}
     	
     	String tag = "Technology";
 		
 		
     	List<Topic> topics = Topic.find("select t from Topic t join t.tags as tag where tag = ?", tag).fetch();
    
         return topics.get(0);
     }
     
     
     /* Response */
     public static void processChoice(long choiceId, int selection) {
    	System.out.println("Choice : "+choiceId+" and Selection: "+selection);
     	Choice choice = Choice.findById(choiceId);
     	if (choice == null) {
     		System.out.println("ERROR: Could not find choice with id "+choiceId);
     		return;
     	}
     	
     	choice.selection = selection;
     	index();
     }
 }
