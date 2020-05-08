 package com.gokaconsulting.notifyweb.service;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.mail.Address;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Part;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import org.jsoup.Jsoup;
 
 import com.gokaconsulting.notifyweb.dao.PMF;
 import com.gokaconsulting.notifyweb.gateway.AlertGateway;
 import com.gokaconsulting.notifyweb.model.Notification;
 import com.gokaconsulting.notifyweb.model.User;
 import com.gokaconsulting.notifyweb.web.MailHandlerServlet;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 public class MailService {
 
 	public MailService() {
 
 	}
 
 	private static final String legalString = "The information in this Internet Email is confidential and may be legally privileged. It is intended solely for the addressee. Access to this Email by anyone else is unauthorized. If you are not the intended recipient, any disclosure, copying, distribution or any action taken or omitted to be taken in reliance on it, is prohibited and may be unlawful. When addressed to our clients any opinions or advice contained in this Email are subject to the terms and conditions expressed in any applicable governing The Home Depot terms of business or client engagement letter. The Home Depot disclaims all responsibility and liability for the accuracy and content of this attachment and for any damages or losses arising from any inaccuracies, errors, viruses, e.g., worms, trojan horses, etc., or other items of a destructive nature, which may be contained in this attachment and shall not be liable for direct, indirect, consequential or special damage...";
 
 	// TODO: check environment if (SystemProperty.environment.value() ==
 	// SystemProperty.Environment.Value.Production)
 	private int unRead = 0;
 	private boolean textIsHtml = false;
 	private String userEmailAddress = "not set";
 	private final Logger logger = Logger.getLogger(MailHandlerServlet.class
 			.getName());
 
 	public void processEmail(MimeMessage message) throws MessagingException,
 			IOException {
 
 		Notification n = null;
 		AlertGateway ag = new AlertGateway();
 
 		try {
 			n = parseMessage(message);
 		} catch (UserDeleteException e) {
 			deleteUser(e.getUser());
 			return;
 		} catch (DoNotPersistEmailException e) {
 			ag.sendAlert(e.getNotification(), unRead);
 			return;
 		} catch (DeleteAllException e) {
 			deleteAllMessages(e.getUser());
 			return;
 		} catch (UserDoesNotExistException e) {
 			logger.warning("Email received for non-existant user: "
 					+ userEmailAddress);
 			return;
 		}
 
 		logger.info("Persisting message");
 		persistNotification(n);
 		incrementUnRead(userEmailAddress);
 		logger.info("Sending alert for " + n.getUserEmail() + " " + unRead);
 		ag.sendAlert(n, unRead);
 		return;
 	}
 
 	public List<Notification> getNotifications(String theUserEmail) {
 
 		String user = theUserEmail;
 
 		logger.info("Returning all emails for: " + user);
 
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Query q = pm.newQuery(Notification.class);
 		q.setFilter("userEmail == user");
 		q.setOrdering("sentDate desc");
 		q.declareParameters("String user");
 
 		@SuppressWarnings("unchecked")
 		List<Notification> results = (List<Notification>) q.execute(user);
 		logger.info("Count of emails found for user: " + user + " is: "
 				+ results.size());
 		if (!results.isEmpty()) {
 			for (Notification n : results) {
 				logger.info("Notification ID is: " + n.getId()
 						+ " subject is: " + n.getSubject());
 				if (n.getId() == null) {
 					// TODO: Figure out why this is happening
 					logger.severe("Task id for task: " + n.getKey().getId()
 							+ " was 0");
 					n.setId(n.getKey().getId());
 				}
 			}
 		}
 		pm.close();
 		return results;
 	}
 
 	public void deleteNotification(String id) {
 
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		if (id != null && Integer.parseInt(id) > 0) {
 			logger.info("Delete requested for notification: " + id);
 			Key k = KeyFactory.createKey(Notification.class.getSimpleName(),
 					Long.valueOf(id));
 			Notification n = pm.getObjectById(Notification.class, k);
 
 			pm.deletePersistent(n);
 			logger.info("Delete succesful for Notification: " + id);
 
 			pm.close();
 
 		} else {
 			logger.log(Level.WARNING,
 					"Delete request without valid notification parameter");
 		}
 	}
 
 	private String getText(Part p) throws MessagingException, IOException {

 		if (p.isMimeType("text/*")) {
 			String s = null;
 			try {
 				s = (String) p.getContent();
 			} catch (Exception e) {
 				logger.log(Level.WARNING, "Error processing message body", e);
 			}
 			textIsHtml = p.isMimeType("text/html");
 			return s;
 		}
 
 		else if (p.isMimeType("multipart/alternative")) {
 			// prefer html text over plain text
 			Multipart mp = (Multipart) p.getContent();
 			String text = null;
 			for (int i = 0; i < mp.getCount(); i++) {
 				Part bp = mp.getBodyPart(i);
 				if (bp.isMimeType("text/plain")) {
 					if (text == null)
 						text = getText(bp);
 					continue;
 				} else if (bp.isMimeType("text/html")) {
 					String s = getText(bp);
 					if (s != null)
 						return s;
 				} else {
 					return getText(bp);
 				}
 			}
 			return text;
 		} else if (p.isMimeType("multipart/*")) {
 			Multipart mp = (Multipart) p.getContent();
 			for (int i = 0; i < mp.getCount(); i++) {
 				String s = getText(mp.getBodyPart(i));
 				if (s != null)
 					return s;
 			}
 		}
 		return null;
 	}
 
 	private boolean userExists(String user) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			// TODO: Add memcache for this
 			User tempUser = pm.getObjectById(User.class, user);
 			if (tempUser != null) {
 				unRead = tempUser.getUnRead();
 				return true;
 			}
 		} catch (Exception e) {
 			logger.log(Level.WARNING,
 					"Error retrieving user for received mail", e);
 		} finally {
 			logger.info("Closing pm in userExists");
 			pm.close();
 		}
 		return false;
 	}
 
 	private void incrementUnRead(String user) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			// TODO: Add memcache for this
 			User tempUser = pm.getObjectById(User.class, user);
 			if (tempUser != null) {
 				tempUser.setUnRead(tempUser.getUnRead() + 1);
 				unRead = tempUser.getUnRead();
 			}
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "Error incrementing unread count", e);
 		} finally {
 			pm.close();
 		}
 	}
 
 	private void deleteUser(String userEmail) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		logger.info("Delete requested for user: " + userEmail);
 		User u = pm.getObjectById(User.class, userEmail);
 		pm.deletePersistent(u);
 		logger.info("Delete succesful for user: " + userEmail);
 		pm.close();
 	}
 
 	private void persistNotification(Notification n) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		try {
 			logger.info("Saving message - ");
 			pm.makePersistent(n);
 
 			n.setId(n.getKey().getId());
 		} catch (Exception e) {
 
 		} finally {
 			pm.close();
 		}
 
 	}
 
 	private Notification parseMessage(MimeMessage message)
 			throws MessagingException, IOException, UserDeleteException,
 			DoNotPersistEmailException, UserDoesNotExistException,
 			DeleteAllException {
 
 		String subject = message.getSubject();
 		subject = subject.replaceFirst("FW:", "");
 		String userEmail = " ";
 		Date sentDate = message.getSentDate();
 		String messageBody = "";
 
 		boolean isDirectForward = false;
 
 		Address[] toAddArray = message.getAllRecipients();
 		String toAddress = null;
 		if (toAddArray.length > 0) {
 			toAddress = toAddArray[0].toString().toLowerCase();
 		}
 
 		logger.info("Before conversion toAddress: " + toAddress);
 
 		// Boolean highImportance;
 		//TODO Change is gmail logic to check for anything not sent to appspot
 		
 		int start = toAddress.indexOf('@');
 		try {
 			if (!toAddress.contains("@notifyweb.appspotmail.com")) {
 				isDirectForward = true;
 				logger.info("Setting direct forward to true");
 			}
 			else
 			{
 				toAddress = toAddress.substring(0, start).trim().toLowerCase();
 			}
 		} catch (StringIndexOutOfBoundsException e) {
 			logger.log(Level.WARNING, "Issue processing toAddress"
 					+ toAddress, e);
 		}
 
 		Address[] from = message.getFrom();
 		String fromAddress = null;
 		if (from.length > 0) {
 			userEmail = from[0].toString().toLowerCase();
 			if (userEmail.contains("<")) {
 				logger.info("Useremail contains: <, is currently: " + userEmail);
 				Pattern pattern = Pattern.compile("<(.*?)>");
 				Matcher matcher = pattern.matcher(userEmail);
 				if (matcher.find()) {
 					userEmail = matcher.group(1);
 					logger.info("Useremail updated to be: " + userEmail);
 				}
 			}
 		}
 
 		messageBody = getText(message);
 	
 
 		if (textIsHtml) {
 			if (messageBody != null) {
 				logger.info("Message before jsoup: " + messageBody);
 
 				messageBody = Jsoup.parse(
 						messageBody.replaceAll("(?i)<br[^>]*>", "br2nl")
 								.replaceAll("\n", "br2nl")).text();
 				messageBody = messageBody.replaceAll("br2nl ", "\n")
 						.replaceAll("br2nl", "\n").trim();
 
 				logger.info("Message after jsoup: " + messageBody);
 			} else {
 				logger.info("Html with null message body");
 			}
 		}
 		
 		if (messageBody != null) {
 			messageBody = messageBody.replaceAll(legalString, " ");
 			messageBody = messageBody.replace(
 					"________________________________", " ");
 
 			String delims = "[\\s\\n]+";
 			String[] tokens = messageBody.split(delims);
 			for (int i = 0; i < tokens.length; i++) {
 				// logger.info("Token: " + i + " is: " + tokens[i]);
 				if (tokens[i].equalsIgnoreCase("From:")) {
 					logger.info("From address found: " + tokens[i + 1]);
 					fromAddress = tokens[i + 1];
 					String atSymbol = "@";
 					if (!fromAddress.contains(atSymbol)) {
 						logger.info("Adding to the from address"
 								+ tokens[i + 2]);
 						fromAddress += " " + tokens[i + 2];
 					}
 					break;
 				} else if (tokens[i].contains("From:")) {
 					logger.info("Found from in token: " + tokens[i]);
 					fromAddress = tokens[i + 1];
 					String atSymbol = "@";
 					if (!fromAddress.contains(atSymbol)) {
 						fromAddress += " " + tokens[i + 2];
 					}
 					break;
 				}
 			}
 		}
 		
 		else {
			logger.warning("Trying altnerate parse method on null message");
 			final Object content = message.getContent();
 			final StringBuffer sb = new StringBuffer();
 			
             if (content instanceof String) {
                 messageBody = (String) content;
             } else if (content instanceof MimeMultipart) {
 				final MimeMultipart mmp = (MimeMultipart) content;
 				for (int i = 0; i < mmp.getCount(); i++) {
 					final Part p = mmp.getBodyPart(i);
 					final Object bp = p.getContent();
 					if (bp instanceof String)
 						sb.append((String) bp);
 				}
             }
             else
             {
             	logger.warning("Message is of type: " + content.getClass().getName());
             }
             
             messageBody = sb.toString();
             logger.info("Message after alternate parsing is: " + messageBody);
 		}
 		
 		if (isDirectForward) {
 			logger.info("Webmail address");
 			fromAddress = userEmail;
 
 			userEmail = toAddress;
 			
 			Pattern pattern = Pattern.compile("<(.*?)>");
 			Matcher matcher = pattern.matcher(userEmail);
 			if (matcher.find()) {
 				userEmail = matcher.group(1);
 			}
 			logger.info("User email updated to be: " + userEmail);
 		}
 
 		if (fromAddress != null) {
 			fromAddress = fromAddress.replaceAll("<", "");
 			fromAddress = fromAddress.replaceAll(">To:", "");
 			fromAddress = fromAddress.trim();
 		} else {
 			fromAddress = userEmail;
 		}
 		
 		logger.info("Receieved message from " + userEmail + " subject "
 				+ subject + " sent to: " + toAddress);
 		
 		logger.info("Preparing to create notification");
 		Notification n = new Notification(fromAddress, userEmail, sentDate,
 				messageBody, subject);
 		userEmailAddress = userEmail;
 
 		if (userExists(userEmail)) {
 			if (toAddress.contentEquals("reset")) {
 				logger.info("Delete requested for user: " + fromAddress);
 				throw new UserDeleteException(userEmail);
 			} else if (toAddress.contentEquals("deleteall")) {
 				logger.info("Delete all requested for user: " + fromAddress);
 				throw new DeleteAllException(userEmail);
 			} else if (toAddress.contentEquals("donotsave")) {
 				logger.info("Message received, but not persisted from: "
 						+ fromAddress);
 				throw new DoNotPersistEmailException(n);
 			}
 		} else {
 			throw new UserDoesNotExistException(userEmail);
 		}
 		return n;
 	}
 
 	private void deleteAllMessages(String user) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		Query q = pm.newQuery(Notification.class);
 		q.setFilter("userEmail == user");
 		q.setOrdering("sentDate desc");
 		q.declareParameters("String user");
 
 		@SuppressWarnings("unchecked")
 		List<Notification> results = (List<Notification>) q.execute(user);
 		logger.info("Count of emails found for user: " + user + " is: "
 				+ results.size());
 		if (!results.isEmpty()) {
 			for (Notification n : results) {
 				pm.deletePersistent(n);
 			}
 		}
 		pm.close();
 	}
 }
