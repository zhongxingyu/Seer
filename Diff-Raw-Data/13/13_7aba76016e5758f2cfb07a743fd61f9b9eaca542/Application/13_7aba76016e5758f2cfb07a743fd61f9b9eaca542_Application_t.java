 package com.capgemini.contacts.application;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.ws.rs.ApplicationPath;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.capgemini.contacts.services.ContactService;
 
 @ApplicationPath("rest")
 public class Application extends javax.ws.rs.core.Application{
 	Logger logger = LoggerFactory.getLogger(Application.class);
 
 	@Override
 	public Set<Object> getSingletons() {
 		ContactService contactService = new ContactService();
 		Set<Object> singletons = new HashSet<Object>();
 		singletons.add(contactService);
		logger.info("ContactsService is ready at work.");
 		return singletons;
 	}
 
 }
