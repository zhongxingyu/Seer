 package com.open.rotile.service.persist;
 
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyFactory;
 import com.googlecode.objectify.ObjectifyService;
import com.open.rotile.model.Project;
 import com.open.rotile.model.Vote;
 
 public class OfyService {
 	static {
 		factory().register(Vote.class);
		factory().register(Project.class);
 	}
 
 	public static Objectify ofy() {
 		return ObjectifyService.ofy();
 	}
 
 	public static ObjectifyFactory factory() {
 		return ObjectifyService.factory();
 	}
 }
