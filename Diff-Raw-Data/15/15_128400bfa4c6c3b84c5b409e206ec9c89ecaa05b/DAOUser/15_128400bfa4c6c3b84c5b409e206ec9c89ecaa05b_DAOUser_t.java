 package tv.tipoff.infrastructure;
 
 import java.io.UnsupportedEncodingException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 
 import tv.tipoff.application.model.User;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 
 public class DAOUser {
 
 	PersistenceManager pm;
 
 	// TODO revoir les assert!
 
 	/**
 	 * . Create a new user in the database with the minimum
 	 * 
 	 * @param pseudo
 	 */
 	public void createUser(User user) {
 		Key key = KeyFactory.createKey(User.class.getSimpleName(), user.getPseudo());
 		user.setKey(key);
 
 		String password = getPassword(8);
 		user.setPassword(password);
 
 		pm = PMF.getPersistenceManager();
 		try {
 			pm.makePersistent(user);
 		} finally {
 			pm.close();
 		}
 
 		Properties props = new Properties();
 		Session session = Session.getDefaultInstance(props, null);
 		final String pseudoEmail = user.getEmail();
 
 		String msgBody = "<div style=\"text-align:center;border:1px solid #444;\">"
 				+ "	<img src=\"\" alt=\"logo\" />"
 				+ "	<p>Salut "
 				+ user.getPseudo()
 				+ "!</p>"
 				+ "	<p> Bienvenu sur le service Geommiti.</p>"
 				+ "	<p>Voici ton mot de passe pour te connecter :</p>"
 				+ "<p><b>" + password + "</b></p>" + "</div>";
 
 		try {
 			MimeMessage msg = new MimeMessage(session);
 			MimeBodyPart htmlPart = new MimeBodyPart();
 			msg.setContent(htmlPart, "text/html");
 			msg.setFrom(new InternetAddress("ferreira.vincent14@gmail.com",
 					"Geomiti auto-message"));
 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
 					pseudoEmail, user.getPseudo()));
 			msg.setSubject("Welcome to Geomiti");
 			msg.setText(msgBody);
 			Transport.send(msg);
 			System.out.println("Message send to " + pseudoEmail);
 		} catch (AddressException e) {
 			e.printStackTrace();
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * . Create a new user in the database with the minimum
 	 * 
 	 * @param pseudo
 	 */
 	public void updateUser(User user) {
 		pm = PMF.getPersistenceManager();
		try {
			pm.makePersistent(user);
		}
		finally {
			pm.close();
		}
 	}
 
 	/**
 	 * . Delete an existing user in the database
 	 * 
 	 * @param pseudo
 	 *            of the user
 	 */
 	// TODO:delete from all other users' friendship as well!
 	public void deleteUser(String pseudo) {
 		assert pseudo != null : "pseudo null";
 		assert pseudo.equals("") : "pseudo vide";
 
 		pm = PMF.getPersistenceManager();
 
 		Key k = KeyFactory.createKey(User.class.getSimpleName(), pseudo);
 		try {
 			User user = pm.getObjectById(User.class, k);
 			if (user != null) {
 				pm.deletePersistent(user);
 			}
 		} catch (Exception e) {
 			System.err.println(" !!! Trying an non-existing User!");
 		}
 		pm.close();
 
 	}
 
 	public User getUserProfile(String pseudo) {
 		pm = PMF.getPersistenceManager();
 
 		Key k = KeyFactory.createKey(User.class.getSimpleName(), pseudo);
 		try {
 			return pm.getObjectById(User.class, k);
 		} catch (Exception e) {
 			return null;
 		} finally {
 			pm.close();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public User getUserProfileByEmail(String email) {
 		pm = PMF.getPersistenceManager();
 
 		Query query = pm.newQuery(User.class);
 		query.setFilter("email == emailParam");
 		query.declareParameters("String emailParam");
 
 		return ((List<User>)query.execute(email)).get(0);
 	}
 
 	public String getUserProfileLight(String pseudo) {
 		assert pseudo != null : "pseudo null";
 		assert pseudo.equals("") : "pseudo vide";
 		String str = "";
 
 		pm = PMF.getPersistenceManager();
 
 		Key k = KeyFactory.createKey(User.class.getSimpleName(), pseudo);
 		try {
 			User user = pm.getObjectById(User.class, k);
 			if (user != null) {
 				str += user.toJSonLight();
 			}
 		} catch (Exception e) {
 			System.err.println(" !!! Trying an non-existing User!");
 		}
 		pm.close();
 		return str;
 	}
 
 	public void addFriendship(String pseudo1, String pseudo2) {
 		assert pseudo1 != null : "User1 from null";
 		assert pseudo1.equals("") : "User1 vide";
 		assert pseudo2 != null : "User2 to null";
 		assert pseudo2.equals("") : "User2 vide";
 
 		pm = PMF.getPersistenceManager();
 
 		Key k1 = KeyFactory.createKey(User.class.getSimpleName(), pseudo1);
 		Key k2 = KeyFactory.createKey(User.class.getSimpleName(), pseudo2);
 		try {
 			User user1 = pm.getObjectById(User.class, k1);
 			User user2 = pm.getObjectById(User.class, k2);
 			if (user1 != null && user2 != null) {
 				// le test d'existance est present dans la methode d'ajout
 				user1.addFriendship(pseudo2);
 				user2.addFriendship(pseudo1);
 			}
 		} catch (Exception e) {
 			System.err.println(" !!! Trying an non-existing User(s)!");
 		}
 		pm.close();
 	}
 
 	public void deleteFriendship(String pseudo1, String pseudo2) {
 		assert pseudo1 != null : "User1 from null";
 		assert pseudo1.equals("") : "User1 vide";
 		assert pseudo2 != null : "User2 to null";
 		assert pseudo2.equals("") : "User2 vide";
 
 		pm = PMF.getPersistenceManager();
 
 		Key k1 = KeyFactory.createKey(User.class.getSimpleName(), pseudo1);
 		Key k2 = KeyFactory.createKey(User.class.getSimpleName(), pseudo2);
 		try {
 			User user1 = pm.getObjectById(User.class, k1);
 			User user2 = pm.getObjectById(User.class, k2);
 			if (user1 != null && user2 != null) {
 				user1.deleteFriendship(pseudo2);
 				user2.deleteFriendship(pseudo1);
 			}
 		} catch (Exception e) {
 			System.err.println(" !!! Trying an non-existing User(s)!");
 		}
 		pm.close();
 	}
 
 	public boolean connect(String email, String pass) {
 		pm = PMF.getPersistenceManager();
 
 		final String queryString = "SELECT FROM com.geomiti.server.User WHERE email == \""
 			+ email + "\" && password == \"" + pass + "\"";
 		final Query query = pm.newQuery(queryString);
 
 		try {
 			@SuppressWarnings("rawtypes")
 			// TODO refactor
 			List results = (List) query.execute();
 			User user = (User) results.get(0);
 			if (user != null) {
 				if (user.getPassword().equals(pass)) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return false;
 	}
 
 	public String getUserFriends(String pseudo) {
 		assert pseudo != null : "pseudo null";
 		assert pseudo.equals("") : "pseudo vide";
 		int count, i = 1;
 		String str = "";
 
 		pm = PMF.getPersistenceManager();
 
 		Key k = KeyFactory.createKey(User.class.getSimpleName(), pseudo);
 		try {
 			User user = pm.getObjectById(User.class, k);
 			if (user != null) {
 				List<String> friends = user.getFriendsList();
 				count = friends.size();
 				str += "{\"friends\":{\"friend\":[";
 				for (String friend : friends) {
 					Key k2 = KeyFactory.createKey(User.class.getSimpleName(),
 							friend);
 					User contact = pm.getObjectById(User.class, k2);
 					if (count == 1) {
 						str += contact.toJSonLight() + ",";
 					} else if (count == i) {
 						str += contact.toJSonLight();
 					} else {
 						str += contact.toJSonLight() + ",";
 					}
 					i++;
 				}
 				str += "]}}";
 			}
 		} catch (Exception e) {
 			System.err.println(" !!! Trying an non-existing User!");
 		}
 		pm.close();
 		return str;
 	}
 
 	public void askFriendship(String pseudo1, String pseudo2) {
 		assert pseudo1 != null : "User1 from null";
 		assert pseudo1.equals("") : "User1 vide";
 		assert pseudo2 != null : "User2 to null";
 		assert pseudo2.equals("") : "User2 vide";
 		User user = null;
 
 		pm = PMF.getPersistenceManager();
 
 		Key k = KeyFactory.createKey(User.class.getSimpleName(), pseudo2);
 		try {
 			user = pm.getObjectById(User.class, k);
 		} catch (Exception e) {
 			System.err.println(" !!! Trying an non-existing User!");
 		}
 		pm.close();
 
 		Properties props = new Properties();
 		Session session = Session.getDefaultInstance(props, null);
 		String pseudo1Email = user.getEmail();
 		pseudo1Email = "damien.chaillou@gmail.com";
 
 		String msgBody = "<div style=\"text-align:center;border:1px solid #444;\">"
 				+ "	<img src=\"\" alt=\"logo\" />"
 				+ "	<p>Salut "
 				+ pseudo1
 				+ "!</p>"
 				+ "	<p>"
 				+ pseudo2
 				+ " t'as demand&eacute; de faire parti de ses contacts</p>"
 				+ "	<p>Pour confirmer le contact click ici:</p>"
 				+ "	<p>http://geomiti.appspot.com/?action=addfriendship&pseudo1="
 				+ pseudo1
 				+ "&pseudo2="
 				+ pseudo2
 				+ "</p>"
 				+ // TODO CHANGER EN HASHCODE
 				"	<p></p>"
 				+ "	<p>Pour refuser la demande click ici:</p>"
 				+ "	<p>http://geomiti.appspot.com/?action=addfriendship&pseudo1="
 				+ pseudo1 + "&pseudo2=" + pseudo2 + "</p>" + "</div>";
 
 		try {
 			Message msg = new MimeMessage(session);
 			MimeBodyPart htmlPart = new MimeBodyPart();
 			msg.setContent(htmlPart, "text/html");
 			msg.setFrom(new InternetAddress("damien.chaillou@gmail.com",
 					"Geomiti auto-message"));
 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
 					pseudo1Email, pseudo1));
 			msg.setSubject("Someone has asked you as contact on Geomiti");
 			msg.setText(msgBody);
 			Transport.send(msg);
 
 		} catch (AddressException e) {
 			e.printStackTrace();
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<User> getAllUsers() {
 		pm = PMF.getPersistenceManager();
 		Query query = pm.newQuery(User.class);
 		return (List<User>) query.execute();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<User> getUsers(String minDate, String maxDate) {
 		pm = PMF.getPersistenceManager();
 		Query query = pm.newQuery(User.class);
 		query.setFilter("birthdate <= maxDateParam");
 		query.setFilter("birthdate >= minDateParam");
 
 		DateFormat df = DateFormat.getDateInstance();
 		Date minDateParam = null;
 		Date maxDateParam = null;
 
 		try {
 			 minDateParam = df.parse(minDate);
 			 maxDateParam = df.parse(maxDate);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 
 		query.declareParameters("java.text.Date maxDateParam");
 		query.declareParameters("java.text.Date minDateParam");
 
 		return (List<User>) query.execute();
 	}
 
 	private String getPassword(int n) {
 		char[] pw = new char[n];
 		int c = 'A';
 		int r1 = 0;
 		for (int i = 0; i < n; i++) {
 			r1 = (int) (Math.random() * 3);
 			switch (r1) {
 			case 0:
 				c = '0' + (int) (Math.random() * 10);
 				break;
 			case 1:
 				c = 'a' + (int) (Math.random() * 26);
 				break;
 			case 2:
 				c = 'A' + (int) (Math.random() * 26);
 				break;
 			}
 			pw[i] = (char) c;
 		}
 		return new String(pw);
 	}
 
 }
