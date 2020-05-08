 package controllers;
 
 import java.util.Date;
 import java.util.List;
 
 import models.User;
 import play.db.jpa.JPA;
 
 public class Contacts extends Main {
 	public static void index() {
		List<User> myContacts = getUser().contacts;
 
 		List<User> otherUsers = User.findAll();
 		for (User isContact : myContacts) {
 			otherUsers.remove(isContact);
 		}
 		otherUsers.remove(getUser());
 
 		renderArgs.put("contacts", myContacts);
 		renderArgs.put("users", otherUsers);
 		renderArgs.put("currentDate", new Date());
 
 		render();
 	}
 
 	public static void add(Long contactId) {
 		User contact = (User) JPA.em()
 				.createQuery("SELECT u FROM User u WHERE u.id = :id")
 				.setParameter("id", contactId).getSingleResult();
 		User user = getUser();
 
 		user.contacts.add(contact);
 		user.save();
 
 		index();
 	}
 
 	public static void remove(Long contactId) {
 		User contact = (User) JPA.em()
 				.createQuery("SELECT u FROM User u WHERE u.id = :id")
 				.setParameter("id", contactId).getSingleResult();
 		User user = getUser();
 
 		user.contacts.remove(contact);
 		user.save();
 
 		index();
 	}
 }
