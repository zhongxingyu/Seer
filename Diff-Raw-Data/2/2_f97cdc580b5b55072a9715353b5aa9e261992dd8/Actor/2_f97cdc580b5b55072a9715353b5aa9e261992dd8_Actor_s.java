 package achievements;
 
 import org.pircbotx.hooks.events.ActionEvent;
 
 import database.Database;
 
 public class Actor extends Achievement {
 
 	public Actor(Database db) {
 		super(db);
 	}
 
 	@Override
 	protected int getAchievementId() {
 		return 14;
 	}
 	
 	public void onAction(ActionEvent event) {
 		String nick = event.getUser().getNick();
 		db.increaseCount(nick, getAchievementId());
 		if(db.getCount(nick, getAchievementId()) >= 10) {
 			if(!db.hasAchievement(nick, getAchievementId())) {
 				db.giveAchievement(nick, getAchievementId());
				event.respond(getAwardString(nick));
 			}
 		}
 	}
 	
 }
