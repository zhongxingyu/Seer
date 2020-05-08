 package ch.hszt.mdp.dao;
 
 import java.util.List;
 
 import ch.hszt.mdp.domain.Activity;
 import ch.hszt.mdp.domain.Activity.ActivityType;
 import ch.hszt.mdp.domain.User;
 import ch.hszt.mdp.service.ActivityService;
 
 public class TaskBirthdayDao {
 	
 	private Activity activity;
 	private ActivityService activityService;
 	
 	public TaskBirthdayDao(){
 	}
 	
 	public TaskBirthdayDao(Activity activity){
 		this.activity=activity;
 	}
 	
 	public void postHappyBirthday(List<User> friends){
 		
 		for (User friend : friends) {
 			activity.setContent("HAPPY BIRTHDAY");
			activity.setTyp(ActivityType.STATUS);
 			activity.setUser(friend);
 			
 			activityService.create(activity);
 			
 		}
 
 	}
 
 }
