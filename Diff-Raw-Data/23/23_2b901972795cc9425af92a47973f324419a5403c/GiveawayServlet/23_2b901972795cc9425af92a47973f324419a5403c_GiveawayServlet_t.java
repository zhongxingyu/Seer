 package fr.cpcgifts;
 
 import java.io.IOException;
 import java.util.Calendar;
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
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 import fr.cpcgifts.model.CpcUser;
 import fr.cpcgifts.model.Giveaway;
 import fr.cpcgifts.persistance.PMF;
 
 @SuppressWarnings("serial")
 public class GiveawayServlet extends HttpServlet {
 	
 	private static final Logger log = Logger.getLogger(GiveawayServlet.class.getName());
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		HttpSession session = req.getSession();
 		CpcUser cpcuser = (CpcUser) session.getAttribute("cpcuser");
		if(cpcuser == null)
			resp.sendRedirect(userService.createLoginURL("/"));
 		cpcuser = pm.getObjectById(CpcUser.class, cpcuser.getKey());
 
 		if (user != null && cpcuser != null) {
 
 			@SuppressWarnings("unchecked")
 			Map<String, String[]> params = req.getParameterMap();
 			
 			String imgUrl = params.get("imgUrl")[0];
 			String gameName = params.get("gameName")[0];
 			String gameDescription = params.get("gameDescription")[0];
 			String endDateStr = params.get("endDateParsed")[0];
 			String[] endDateSplittedStr = endDateStr.split("@");
 			String[] splittedDate = endDateSplittedStr[0].split("-");
 			String[] splittedTime = endDateSplittedStr[1].split("-");
 			int nbCopies = 1;
 			try {
 				nbCopies = Integer.parseInt(params.get("nbCopies")[0]);
 			} catch (NumberFormatException e) {
 				nbCopies = 1;
 			}
 			if(nbCopies < 1) {
 				nbCopies = 1;
 			}
 			
 			Calendar c = Calendar.getInstance();
 			c.set(Calendar.YEAR, Integer.parseInt(splittedDate[0]));
 			c.set(Calendar.MONTH, Integer.parseInt(splittedDate[1])-1);
 			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(splittedDate[2]));
 			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splittedTime[0]));
 			c.set(Calendar.MINUTE, Integer.parseInt(splittedTime[1]));
 			c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + (Integer.parseInt(endDateSplittedStr[2])/10));
 			
 			
 			Giveaway ga = new Giveaway(cpcuser.getKey(), gameName, gameDescription, imgUrl, c.getTime(), nbCopies);
 			
 			log.info("Created ga : " + ga.getTitle() + " by " + cpcuser.getCpcNickname());
 			
 			try {
 				pm.makePersistent(ga);
 				
 				cpcuser.addGiveaway(ga.getKey());				
 				pm.makePersistent(cpcuser);
 				
 			} finally {
 				pm.close();
 			}
 			
 			resp.sendRedirect("/giveaway?gaID=" + ga.getKey().getId());
 			
 			try {
 	            Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
 	            
 	            cache.remove(cpcuser.getKey());
 				
 	        } catch (CacheException e) {
 	        	//rien
 	        }
 			
 		} else {
 
 			resp.sendRedirect("/");
 			return;
 		}
 	}
 }
