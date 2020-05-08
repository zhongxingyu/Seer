 package controllers;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import models.Contact;
 import models.Link;
 import play.Logger;
 import play.mvc.Controller;
 
 public class Cron extends Controller {
 
 	/**
 	 * Envoi de l'email
 	 */
 	public static void send() {
 
 		List<Contact> list = Contact.all().fetch();
 		Iterator<Contact> iter = list.iterator();
 		while (iter.hasNext()) {
 			String body = "";
 			Contact contact = iter.next();
 			List<Link> listLink = Link.getByNotModified(contact);
 			Iterator<Link> iterLink = listLink.iterator();
 			while (iterLink.hasNext()) {
 				Link link = iterLink.next();
				if (link.url.toUpperCase().indexOf("blank") > -1) {
 					Logger.debug("send " + contact.firstName + " " + link.url);
 					// body = body +"\n"+ "<a href=\""+ link.url + "\">" +
 					// link.url
 					// + "</a>";
 					body = body + "\n" + link.url + "\n";
 				}
 
 				link.isModified = true;
 				link.save();
 			}
 
 			if (body.length() > 0) {
 				body = body
 						+ "\nEnjoy this links.\n\nRegards,\n	Link Me First by Malys";
 
 				sendEmail(body, contact.emailAddress);
 			}
 
 		}
 	}
 
 	/**
 	 * Nettoyage
 	 */
 	public static void clean() {
 
 		List<Link> listLink = Link.getModified();
 		Iterator<Link> iterLink = listLink.iterator();
 		while (iterLink.hasNext()) {
 			Link link = iterLink.next();
 			link.delete();
 		}
 
 	}
 
 	private static void sendEmail(String body, String email) {
 		try {
 			Properties props = new Properties();
 			Session session = Session.getDefaultInstance(props, null);
 			Message msg = new MimeMessage(session);
 			msg.setContent(body, "text/html; charset=ISO-8859-1");
 
 			msg.setFrom(new InternetAddress("domino0028@gmail.com"));
 
 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
 					email, "LinkMeFirst:"));
 			msg.setSubject("Enlacitos");
 			msg.setText(body);
 			Transport.send(msg);
 			Logger.info("Envoi: " + body);
 
 		} catch (AddressException e) {
 			Logger.error(e.getMessage());
 		} catch (MessagingException e) {
 			Logger.error(e.getMessage());
 		} catch (UnsupportedEncodingException e) {
 			Logger.error(e.getMessage());
 		}
 
 	}
 
 }
