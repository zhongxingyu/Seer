 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import models.Question;
 import models.TimeTracker;
 import models.User;
 import models.database.Database;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 
 	@Override
 	public void doJob() {
 
 		// User
 
 		User jack = Database.get().users().register("Jack", "jack");
 		User john = Database.get().users().register("John", "john");
 		User bill = Database.get().users().register("Bill", "bill");
 		User kate = Database.get().users().register("Kate", "kate");
 
 		jack.setEmail("jack@jack.jk");
 		jack.setFullname("Jack Daniel");
		jack.setWebsite("http://www.jack.jk");
 		jack.setProfession("Brewer");
 		jack.setBiography("Oh well, ...");
 		jack.setModerator(true);
 
 		// Questions
 		Question question = Database.get().questions().add(jack,
 				"Why did the chicken cross the road?");
 		question.answer(bill, "To get to the other side.");
 
 		question = Database.get().questions().add(john,
 				"What is the answer to life the universe and everything?");
 
 		question.answer(kate, "42");
 		question.answer(kate, "1337");
 
 		// Comments
 		question.comment(jack, "What a strange question");
 
 		// Tags
 		question.setTagString("numb3rs");
 
 		// TimeTracker
 		GregorianCalendar g = new GregorianCalendar(2010, Calendar.OCTOBER, 25);
 		TimeTracker.setRealTimeTracker(g);
 
 	}
 }
