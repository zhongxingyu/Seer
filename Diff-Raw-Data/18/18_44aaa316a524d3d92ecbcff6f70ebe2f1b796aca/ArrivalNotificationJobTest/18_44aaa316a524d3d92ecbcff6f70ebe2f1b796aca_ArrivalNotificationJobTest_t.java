 import java.lang.reflect.Field;
 import java.util.Date;
 import java.util.Map;
 
 import jobs.ArrivalNotificationJob;
 import models.FacebookAccount;
 import models.Note;
 import models.User;
 
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.Play;
 import play.libs.Mail;
 import play.test.Fixtures;
 import play.test.UnitTest;
 
 public class ArrivalNotificationJobTest extends UnitTest {
     String arrivalNotificationSize;
 
     @Before
     public void before() {
 	arrivalNotificationSize = (String) Play.configuration
 		.get("mail.arrivalNotification.size");
	Fixtures.deleteAllModels();
 	MailHelper.clear();
     }
 
     @After
     public void after() {
 	Play.configuration.put("mail.arrivalNotification.size",
 		arrivalNotificationSize);
     }
 
     @Test
     public void onlySendNotSent() throws Exception {
 	User sender = new User();
 	sender.email = "test@test.com";
 	sender.language = "en";
 	sender.save();
 
 	Date today = new Date();
 
 	Note c1 = new Note();
 	c1.message = "m1";
 	c1.sendDate = today;
 	c1.setReceiverEmails(new String[] { "foo@example.com" });
 	c1.sender = sender;
 	c1.save();
 
 	Note c2 = new Note();
 	c2.message = "m2";
 	c2.sendDate = today;
 	c2.setReceiverEmails(new String[] { "bar@example.com" });
 	c2.sent = true;
 	c2.sender = sender;
 	c2.save();
 
 	ArrivalNotificationJob job = new ArrivalNotificationJob();
 	job.doJob();
 
 	String mailFoo = Mail.Mock.getLastMessageReceivedBy("foo@example.com");
 	String mailBar = Mail.Mock.getLastMessageReceivedBy("bar@example.com");
 
 	assertNotNull(mailFoo);
 	assertTrue(c1.sent);
 
 	assertNull(mailBar);
     }
 
     @Test
     public void onlySendWhenOld() throws Exception {
 	User sender = new User();
 	sender.email = "test@test.com";
 	sender.language = "en";
 	sender.save();
 
 	Date now = new Date();
 	Date tomorrow = new DateTime().plusDays(1).toDate();
 	Date inAnHour = new DateTime().plusHours(1).toDate();
 
 	Note c1 = new Note();
 	c1.message = "m1";
 	c1.sendDate = now;
 	c1.setReceiverEmails(new String[] { "foo@example.com" });
 	c1.sender = sender;
 	c1.save();
 
 	Note c2 = new Note();
 	c2.message = "m2";
 	c2.sendDate = tomorrow;
 	c2.setReceiverEmails(new String[] { "bar@example.com" });
 	c2.sender = sender;
 	c2.save();
 
 	Note c3 = new Note();
 	c3.message = "m3";
 	c3.sendDate = inAnHour;
 	c3.setReceiverEmails(new String[] { "seb@example.com" });
 	c3.sender = sender;
 	c3.save();
 
 	ArrivalNotificationJob job = new ArrivalNotificationJob();
 	job.doJob();
 
 	String mailFoo = Mail.Mock.getLastMessageReceivedBy("foo@example.com");
 	String mailBar = Mail.Mock.getLastMessageReceivedBy("bar@example.com");
 	String mailSeb = Mail.Mock.getLastMessageReceivedBy("seb@example.com");
 
 	assertNotNull(mailFoo);
 	assertTrue(c1.sent);
 
 	assertNull(mailBar);
 
 	assertNull(mailSeb);
     }
 
     @Test
     public void onlySendSpecificAmount() throws Exception {
 	User sender = new User();
 	sender.email = "test@test.com";
 	sender.language = "en";
 	sender.save();
 
 	Play.configuration.put("mail.arrivalNotification.size", "10");
 
 	Date now = new Date();
 
 	for (int n = 0; n < 20; n++) {
 	    Note c = new Note();
 	    c.message = "m1";
 	    c.sendDate = now;
 	    c.setReceiverEmails(new String[] { "foo@example.com" });
 	    c.sender = sender;
 	    c.save();
 	}
 
 	ArrivalNotificationJob job = new ArrivalNotificationJob();
 
 	job.doJob();
 	assertEquals(10, Note.find("sent = ?", true).fetch().size());
 
 	job.doJob();
 	assertEquals(20, Note.find("sent = ?", true).fetch().size());
     }
 
     @Test
     public void sendMultiple() throws Exception {
 	User sender = new User();
 	sender.email = "test@test.com";
 	sender.language = "en";
 	sender.save();
 
 	Play.configuration.put("mail.arrivalNotification.size", "10");
 
 	Date now = new Date();
 	Note c1 = new Note();
 	c1.message = "m1";
 	c1.sendDate = now;
 	c1.setReceiverEmails(new String[] { "foo@example.com",
 		"bar@example.com" });
 	c1.sender = sender;
 	c1.save();
 
 	ArrivalNotificationJob job = new ArrivalNotificationJob();
 	job.doJob();
 	assertNotNull(Mail.Mock.getLastMessageReceivedBy("foo@example.com"));
 	assertNotNull(Mail.Mock.getLastMessageReceivedBy("bar@example.com"));
     }
 
     @Test
     public void facebookNotification() {
 	FacebookAccount account = new FacebookAccount();
 	account.userId = 100002031059460l;
 	account.accessToken = "198949360120440|26826aeebae5022a148af2bd-100002031059460|GG3yiQBct06TE25AXrnIakWRh4E";
 	account.save();
 	User user = new User();
 	user.email = "ycujdcg_huiwitz\u0040tfbnw.net";
 	user.facebook = account;
 	user.save();
 	Note note = new Note();
 	note.sender = user;
 	note.message = "this is just a test";
 	note.sendDate = new Date();
 	note.friend = 100002036699756l;
 	note.save();
 
 	ArrivalNotificationJob job = new ArrivalNotificationJob();
 	job.doJob();
 
 	note.refresh();
 	assertTrue(note.sent);
     }
 }
