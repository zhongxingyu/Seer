 package controllers;
 
 import models.Action;
 import models.Author;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class ActionController extends Controller {
 
 	public static void add(String name, Action parent) {
 		Author author = Author.find("byName", session.get("username")).first();
 		Action a = new Action(name, parent, author);
 		a.save();
 	}
 
 	public static void rename(Action action, String newName) {
 		action.name = newName;
 		action.save();
 		updateFullNames(action);
 	}
 
 	public static void delete(Action action) {
 		action.delete();
 	}
 
 	/**
 	 * Sets a new node being the action's parent node.
 	 * 
 	 * @param action
 	 *            - the action to be moved.
 	 * @param newParentId
 	 *            - the new parent node.
 	 */
 	public static void move(Action action, long newParentId) {
 		Action newParent = Action.findById(newParentId);
 		action.parent = newParent;
 		action.save();
 		updateFullNames(action);
 	}
 
 	/**
 	 * Updates the full names (names including the names of all parent nodes) of
 	 * the defined element and all children nodes (recursive).
 	 * 
 	 * @param a
 	 *            - The element to start with.
 	 */
 	public static void updateFullNames(Action a) {
 		a.updateFullName();
 		a.save();
		for (Action c : a.children)
			updateFullNames(c);
 	}

 }
