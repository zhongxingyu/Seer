 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Room;
 import models.Thread;
 import models.User;
 import play.data.validation.Required;
 import play.mvc.Controller;
 import play.mvc.With;
 import utils.Pagination;
 
 @With(Dbtit.class)
 public class Rooms extends Controller {
 	
 	@LoggedIn
 	public static void list() {
 		User user = Dbtit.connectedUser();
 		List<Room> subscriptions;
 		Pagination pagination = new Pagination(params, user.subscriptions.size());
 		
    	if (pagination.getTo() > 0) { // J’aime pas JPA
     		subscriptions = user.subscriptions.subList(pagination.getFrom(), pagination.getTo() - pagination.getFrom());
     	} else {
     		subscriptions = new ArrayList<Room>();
     	}
 		
 		render(subscriptions, pagination);
 	}
 	
 	/**
 	 * Show the debates of the room
 	 * @param hash
 	 */
 	public static void show(String hash) {
 		Room room = Room.find("byHash", hash).first();
 		notFoundIfNull(room);
 		
 		if (!room.isPublic) {
 			User user = Dbtit.connectedUser();
 			if (user == null || !user.hasSubscribed(room)) {
 				forbidden();
 			}
 		}
 		
		Pagination pagination = new Pagination(params, room.threads.size(), 12);
		List<Thread> threads = Thread.find("room = ? ORDER BY lastActivity DESC", room).fetch(pagination.getCurrentPage(), pagination.getPageSize());
 		
 		render(room, threads, pagination);
 	}
 	
 	@LoggedIn
 	public static void newRoom() {
 		render();
 	}
 	
 	/**
 	 * Creates a new debate room
 	 * @param name
 	 * @param isPublic
 	 */
 	@LoggedIn
 	public static void create(@Required String name, @Required boolean isPublic) {
 		if (validation.hasErrors()) {
 			render("@newRoom", name, isPublic);
 		}
 		
 		Room room = Room.create(Dbtit.connectedUser(), name, isPublic);
 		show(room.hash);
 	}
 	
 	@LoggedIn
 	public static void subscribe(String hash, String url) {
 		Room room = Room.find("byHash", hash).first();
 		notFoundIfNull(room);
 		User user = Dbtit.connectedUser();
 		
 		user.subscribe(room);
 		
 		redirect(url);
 	}
 	
 	@LoggedIn
 	public static void unsubscribe(String hash, String url) {
 		Room room = Room.find("byHash", hash).first();
 		notFoundIfNull(room);
 		User user = Dbtit.connectedUser();
 		
 		user.unsubscribe(room);
 		
 		redirect(url);
 	}
 }
