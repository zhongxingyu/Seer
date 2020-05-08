 package com.chaman.dao;
 
 import com.chaman.model.EventLocationCapable;
 import com.chaman.model.Following;
 import com.chaman.model.User;
 import com.chaman.model.Vote;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.util.DAOBase;
 
 public class Dao extends DAOBase {
 
 	static {
 		try {
 			
 			ObjectifyService.register(Following.class);
 			ObjectifyService.register(EventLocationCapable.class);
 			ObjectifyService.register(User.class);
 			ObjectifyService.register(Vote.class);
 		} catch (Exception ex) {
 			
 			System.out.println(ex.toString());
 		}
     }
 
 	public Dao() {
 		
 		super();
		// TODO find the best way to register the classes
		try {
			
			ObjectifyService.register(Following.class);
			ObjectifyService.register(EventLocationCapable.class);
			ObjectifyService.register(User.class);
			ObjectifyService.register(Vote.class);
		} catch (Exception ex) {
				
			System.out.println(ex.toString());
		}
 	}
 	
     /** Your DAO can have your own useful methods */
 //    public MyThing getOrCreateMyThing(long id)
 //    {
 //        MyThing found = ofy().find(clazz, id);
 //        if (found == null)
 //            return new MyThing(id);
 //        else
 //            return found;
 //    }
     
 }
