 package cz.muni.fi.pv243.security;
 
 import java.util.logging.Logger;
 
 import javax.inject.Inject;
 
 import org.jboss.annotation.ejb.Service;
 import org.jboss.seam.security.Identity;
 import org.jboss.seam.security.annotations.Secures;
 
 import cz.muni.fi.pv243.model.*;
 
 @Service
 public class Authorization {
 
//	@Inject
//	static Logger log;
 
 	@Secures
 	@Admin
 	public static boolean isAdmin(Identity identity) {
//		log.finest("Authorization: isAdmin");
 		if (!identity.isLoggedIn()) {
 			return false;
 		}
 		return "admin".equals(((User) identity.getUser()).getRole());
 	}
 
 	@Secures
 	@BasicPermission
 	public static boolean isCustomer(Identity identity) {
//		log.finest("Authorization: isCustomer");
 		if (!identity.isLoggedIn()) {
 			return false;
 		}	
 		return "customer".equals(((User) identity.getUser()).getRole());
 	}
 
 }
