 package net.thesocialos.server;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Calendar;
 import java.util.Properties;
 import java.util.Random;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.http.HttpServlet;
 
 import net.thesocialos.shared.ChannelApiEvents.ChApiChatUserChngState.STATETYPE;
 import net.thesocialos.shared.model.Account;
 import net.thesocialos.shared.model.Admin;
 import net.thesocialos.shared.model.Columns;
 import net.thesocialos.shared.model.Conversation;
 import net.thesocialos.shared.model.Facebook;
 import net.thesocialos.shared.model.FlickR;
 import net.thesocialos.shared.model.Google;
 import net.thesocialos.shared.model.Group;
 import net.thesocialos.shared.model.ImageUpload;
 import net.thesocialos.shared.model.Lines;
 import net.thesocialos.shared.model.Oauth1;
 import net.thesocialos.shared.model.Oauth2;
 import net.thesocialos.shared.model.Session;
 import net.thesocialos.shared.model.SharedHistory;
 import net.thesocialos.shared.model.SharedHistory.SHARETYPE;
 import net.thesocialos.shared.model.Twitter;
 import net.thesocialos.shared.model.User;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 
 @SuppressWarnings("serial")
 public class LoadOnStart extends HttpServlet {
 	
 	@Override
 	public void init() {
 		
 		// User
 		ObjectifyService.register(Session.class);
 		ObjectifyService.register(Account.class);
 		ObjectifyService.register(Admin.class);
 		ObjectifyService.register(Group.class);
 		ObjectifyService.register(Twitter.class);
 		ObjectifyService.register(User.class);
 		ObjectifyService.register(Columns.class);
 		
 		// Chat
 		ObjectifyService.register(Lines.class);
 		ObjectifyService.register(Conversation.class);
 		
 		// SocialOS
 		ObjectifyService.register(Oauth1.class);
 		ObjectifyService.register(Oauth2.class);
 		ObjectifyService.register(Facebook.class);
 		ObjectifyService.register(FlickR.class);
 		ObjectifyService.register(Google.class);
 		
 		ObjectifyService.register(ImageUpload.class);
 		ObjectifyService.register(SharedHistory.class);
 		
 		setAllUsertoOffline();
 		sendEmail();
 		// resetContacts();
 		// createCloudAccounts();
 		// createSharedExamples();
 		
 	}
 	
 	private void setAllUsertoOffline() {
 		Objectify ofy = ObjectifyService.begin();
 		Query<User> queryusers = ofy.query(User.class).filter("isConnected =", true);
 		for (User user : queryusers) {
 			user.isConnected = false;
 			user.chatState = STATETYPE.OFFLINE;
 			ofy.put(user);
 		}
 		
 	}
 	
 	private void resetContacts() {
 		Objectify ofy = ObjectifyService.begin();
 		Query<User> queryusers = ofy.query(User.class);
 		for (User user : queryusers) {
 			user.resetContacts();
 			user.resetPetionContacts();
 			ofy.put(user);
 		}
 		
 	}
 	
 	private void createCloudAccounts() {
 		Objectify ofy = ObjectifyService.begin();
 		User user = ofy.get(User.class, "unai@thesocialos.net");
 		User user1 = ofy.get(User.class, "virtual.solid.snake@gmail.com");
 		user.clearAccounts();
 		user1.clearAccounts();
 		Facebook facebook = new Facebook("facebook1");
 		FlickR flickR = new FlickR("flickr1");
 		Twitter twitter = new Twitter("twitter1");
 		Google google = new Google("google1");
 		user.addAccount(ofy.put(twitter));
 		user.addAccount(ofy.put(facebook));
 		user.addAccount(ofy.put(flickR));
 		user.addAccount(ofy.put(google));
 		ofy.put(user);
 		facebook = new Facebook("facebook2");
 		flickR = new FlickR("flickr2");
 		twitter = new Twitter("twitter2");
 		google = new Google("google2");
 		user1.addAccount(ofy.put(twitter));
 		user1.addAccount(ofy.put(facebook));
 		user1.addAccount(ofy.put(flickR));
 		user1.addAccount(ofy.put(google));
 		ofy.put(user1);
 		
 	}
 	
 	private void createSharedExamples() {
 		Objectify ofy = ObjectifyService.begin();
 		User user = ofy.get(User.class, "unai@thesocialos.net");
 		User user1 = ofy.get(User.class, "virtual.solid.snake@gmail.com");
 		
 		SharedHistory video = new SharedHistory(SHARETYPE.VIDEO,
 				"https://www.youtube.com/embed/EU3wnka0PVc?autoplay=1", "youtube video", Key.create(User.class,
 						user1.getEmail()), Calendar.getInstance().getTimeInMillis());
 		SharedHistory image = new SharedHistory(SHARETYPE.IMAGE,
 				"http://www.trackmania-carpark.com/images/skins/big/Razor%203%20640.jpg", "A modelated car",
 				Key.create(User.class, user1.getEmail()), Calendar.getInstance().getTimeInMillis());
 		
 		user.addShare(ofy.put(video));
 		user.addShare(ofy.put(image));
 		ofy.put(user);
 		
 	}
 	
 	private void sendEmail() {
 		String email = "virtual.solid.snake@gmail.com";
 		Properties props = new Properties();
 		javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, null);
 		
 		try {
 			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("admin@thesocialos.net", "SocialOS Administratrors"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("email", "Mr. User"));
 			Random random = new Random();
 			Integer password = (int) (random.nextFloat() * 1000000);
 			msg.setSubject("Your Example.com account has been activated");
 			String sendText = new String();
 			sendText = "<img  src='http://www.thesocialos.net/images/logo-big.png'/><table border='1'><tr><td>User:</td>"
 					+ "<td>"
 					+ email
 					+ "</td></tr><tr><td>Password:</td>"
 					+ "<td>"
 					+ password
 					+ "</td>"
 					+ "</tr>"
 					+ "</table>";
 			msg.setText(sendText);
 			Transport.send(msg);
 			
 		} catch (AddressException e) {
			// ...
 		} catch (MessagingException e) {
			// ...
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
