 package bootstrap;
 
import models.Calendar;
 import models.User;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	@Override
 	public void doJob() {
 		if (User.count() == 0) {
			User user1 = new User("wuschu", "Domi Jason", "secret",
					"domi@gmail.com").save();
			User user2 = new User("lexy", "lex McColin", "secret",
					"lex@gmail.com").save();
			User user3 = new User("king_julien", "Julien Peterson", "secret",
					"king@gmail.com").save();
			User user4 = new User("joe", "Joe Gargamel", "secret",
					"gargamel@gmail.com").save();

			new Calendar("Home", user1).save();
			new Calendar("Work", user1).save();

			new Calendar("Home", user2).save();
			new Calendar("Work", user2).save();

			new Calendar("Sports", user3).save();
			new Calendar("Uni", user3).save();

			new Calendar("My Calendar", user4).save();
			new Calendar("My Second Calendar", user4).save();

 		}
 	}
 }
