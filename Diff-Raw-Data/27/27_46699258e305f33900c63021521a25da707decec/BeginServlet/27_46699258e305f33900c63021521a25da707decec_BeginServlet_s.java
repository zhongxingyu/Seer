 package com.raibaz.lupus;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.raibaz.lupus.dao.LupusDAO;
 import com.raibaz.lupus.facebook.FacebookAPI;
 import com.raibaz.lupus.game.Game;
 import com.raibaz.lupus.game.Player;
 
 @SuppressWarnings("serial")
 public class BeginServlet extends HttpServlet {
 	
 	private static final Logger log = Logger.getLogger("BeginServlet");
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		process(req, resp);
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		process(req, resp); 
 	}
 	
 	private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		LupusDAO dao = new LupusDAO();
 		
 		if(req.getParameter("signed_request") != null) {
 			log.info("signed request = " + req.getParameter("signed_request"));
 			String fbId = FacebookAPI.getUserIdFromSignedRequest(req.getParameter("signed_request"));
 			if(fbId == null) {				
 				resp.sendRedirect("/index.jsp?need_authentication=true");
 				return;
 			}
 			Player p = dao.getPlayer(fbId);
 			if(p == null) {		
 				FacebookAPI fb = new FacebookAPI(FacebookAPI.getAccessTokenFromSignedRequest(req.getParameter("signed_request")));
 				p = fb.getPlayerProfile();
 				if(p == null || p.getFbId() == null) {
 					resp.sendRedirect("/index.jsp?need_authentication=true");
 				}
 				dao.ofy().put(p);
 			}
 			
 			Game ownedGame = dao.getOwnedGame(p);
 			if(ownedGame != null) {			
 				String newToken = ChannelServiceFactory.getChannelService().createChannel(p.getFbId() + "-waiting");
 				
				resp.sendRedirect("/waiting_game.jsp?game_id=" + ownedGame.getId() + "&channel_token=" + newToken);
 			} else {
 				resp.sendRedirect("/index.jsp?player_name=" + p.getName() + "&player_avatar=" + p.getPictureUrl() + "&player_id=" + p.getFbId());
 			}
 		} else {
 			log.info("Request from fb without signed_request!");
 			if(req.getParameter("player_id") != null && req.getParameter("player_name") != null) {
 				String fbId = req.getParameter("player_id");
 				Player p = dao.getPlayer(fbId);
 				if(p == null) {
 					p = new Player();
 					p.setFbId(fbId);
 					p.setName(req.getParameter("player_name"));
 					p.setPictureUrl(req.getParameter("player_avatar"));
 					dao.ofy().put(p);
 				}
 				resp.sendRedirect("/index.jsp?player_name=" + p.getName() + "&player_avatar=" + p.getPictureUrl() + "&player_id=" + p.getFbId());				
 			} else {
 				resp.sendRedirect("/index.jsp");
 			}
 		}				
 	}
 }
