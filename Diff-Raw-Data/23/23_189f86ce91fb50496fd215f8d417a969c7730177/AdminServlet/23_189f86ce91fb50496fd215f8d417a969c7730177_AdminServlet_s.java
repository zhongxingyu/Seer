 package fr.cpcgifts;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.sf.jsr107cache.Cache;
 import net.sf.jsr107cache.CacheException;
 import net.sf.jsr107cache.CacheManager;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 import fr.cpcgifts.model.Comment;
 import fr.cpcgifts.model.CpcUser;
 import fr.cpcgifts.model.Giveaway;
 import fr.cpcgifts.persistance.PMF;
 
 @SuppressWarnings("serial")
 public class AdminServlet extends HttpServlet {
 	
 	private static final Logger log = Logger.getLogger(AdminServlet.class.getName());
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		HttpSession session = req.getSession();
 		CpcUser cpcuser = (CpcUser) session.getAttribute("cpcuser");
 		if(cpcuser == null)
			resp.sendRedirect(userService.createLoginURL("/"));
 		cpcuser = pm.getObjectById(CpcUser.class, cpcuser.getKey());
 
 		if (user != null && cpcuser != null && userService.isUserAdmin()) {
 
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> params = req.getParameterMap();
 			
 			String reqType = params.get("reqtype")[0];
 			
 			Giveaway ga = null;
 			Key gaKey = null;
 			if (params.containsKey("gaid")) {
 				String gaID = params.get("gaid")[0];
 				gaKey = KeyFactory.createKey(Giveaway.class.getSimpleName(),Long.parseLong(gaID.trim()));
 				try {
 					ga = pm.getObjectById(Giveaway.class, gaKey);
 				} catch(Exception e) {}
 			}
 			
 			CpcUser userToUpdate = null;
 			Key userToUpdateKey = null;
 			if (params.containsKey("userid")) {
 				String userID = params.get("userid")[0];
 				userToUpdateKey = KeyFactory.createKey(CpcUser.class.getSimpleName(),Long.parseLong(userID.trim()));
 				try {
 					userToUpdate = pm.getObjectById(CpcUser.class, userToUpdateKey);
 				} catch(Exception e) {}
 			}
 			
 			Key commentToUpdateKey = null;
 			if (params.containsKey("commentid")) {
 				String commentID = params.get("commentid")[0];
 				commentToUpdateKey = KeyFactory.createKey(Comment.class.getSimpleName(),Long.parseLong(commentID.trim()));
 			}
 			
 			if(reqType.equals("reroll")) { //reroll
 				String winnerToRerollId = params.get("winnerToReroll")[0];
 				
 				ga.reroll(KeyFactory.createKey(CpcUser.class.getSimpleName(), Long.parseLong(winnerToRerollId)));
 				log.info("[ADMIN] " + cpcuser + " rerolled the giveaway " + ga + "." + "\n"
 						+ "New winners are : " + Arrays.deepToString(ga.getWinners().toArray()) + "."
 						);
 			} else if("closeGa".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " closed the giveaway " + ga + ".");
 				ga.setTitle(ga.getTitle() + " (Fermé par " + cpcuser.getCpcNickname() + ")");
 				ga.setOpen(false);
 			} else if("openGa".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " reopened the giveaway " + ga + ".");
 				ga.setOpen(true);
 			} else if("addWinner".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " added " + userToUpdate + " to winner list of " + ga + ".");
 				if(userToUpdate != null)
 					userToUpdate.addWon(gaKey);
 				if(ga != null)
 					ga.addWinner(userToUpdateKey);
 			} else if ("removeWinner".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " removed " + userToUpdate + " from winner list of " + ga + ".");
 				if(userToUpdate != null)
 					userToUpdate.removeWon(gaKey);
 				if(ga != null)
 					ga.removeWinner(userToUpdateKey);
 			} else if("addEntry".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " added " + userToUpdate + " in entries of " + ga + ".");
 				if(userToUpdate != null)
 					userToUpdate.addEntry(gaKey);
 				if(ga != null)
 					ga.addEntrant(userToUpdateKey);
 			} else if("removeEntry".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " removed " + userToUpdate + " from entries of " + ga + ".");
 				if(userToUpdate != null)
 					userToUpdate.removeEntry(gaKey);
 				if(ga != null)
 					ga.removeEntrant(userToUpdateKey);
 			} else if("removeComment".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " removed " + commentToUpdateKey + " from comments of " + ga + ".");
 				ga.removeComment(commentToUpdateKey);
 			} else if("banuser".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " banned " + userToUpdate + ".");
 				userToUpdate.setBanned(true);
 			} else if("unbanuser".equals(reqType)) {
 				log.info("[ADMIN] " + cpcuser + " unbanned " + userToUpdate + ".");
 				userToUpdate.setBanned(false);
 			}
 			
 			if(ga != null) {
 				resp.sendRedirect("/giveaway?gaID=" + ga.getKey().getId());
 			} else if(userToUpdate != null) {
 				resp.sendRedirect("/user?userID=" + userToUpdate.getKey().getId());
 			}
 			
 			try {
 	            Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
 	            
 	            if(ga != null) {
 	            	cache.remove(ga.getKey());
 	            }
 	            if(userToUpdate != null) {
 	            	cache.remove(userToUpdate.getKey());
 	            }
 	            
 				
 	        } catch (CacheException e) {
 	        	//rien
 	        }
 			
 		} else {
 
 			resp.sendRedirect("/");
 		}
 		
 		pm.close();
 	}
 }
