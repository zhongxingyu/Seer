 /**
  * Copyright 2010 The University of Nottingham
  * 
  * This file is part of lobbyservice.
  *
  *  lobbyservice is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  lobbyservice is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with lobbyservice.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package uk.ac.horizon.ug.lobby.user;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.persistence.EntityManager;
 import javax.servlet.http.*;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.appengine.api.datastore.KeyFactory;
 
 import uk.ac.horizon.ug.lobby.Constants;
 import uk.ac.horizon.ug.lobby.RequestException;
 import uk.ac.horizon.ug.lobby.model.Account;
 import uk.ac.horizon.ug.lobby.model.EMF;
 import uk.ac.horizon.ug.lobby.model.GameInstance;
 import uk.ac.horizon.ug.lobby.model.GameServer;
 import uk.ac.horizon.ug.lobby.model.GameServerStatus;
 import uk.ac.horizon.ug.lobby.model.GameTemplate;
 import uk.ac.horizon.ug.lobby.protocol.JSONUtils;
 
 /** 
  * Get all Accounts (admin view).
  * 
  * @author cmg
  *
  */
 @SuppressWarnings("serial")
 public class AddGameInstanceServlet extends HttpServlet implements Constants {
 	static Logger logger = Logger.getLogger(AddGameInstanceServlet.class.getName());
 	
 	@Override
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
         GameInstance gi = null;
 		try {
 			BufferedReader r = req.getReader();
 			String line = r.readLine();
 			// why does this seem to read {} ??
 			//JSONObject json = new JSONObject(req.getReader());
 			JSONObject json = new JSONObject(line);
 			gi = JSONUtils.parseGameInstance(json);
 		}
 		catch (JSONException je) {
 			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, je.toString());
 			return;
 		}
         
 		Account account = null;
 		try {
 			account = AccountUtils.getAccount(req);
 		}catch (RequestException re) {
 			resp.sendError(re.getErrorCode(), re.getMessage());
 			return;
 		}
 		EntityManager em = EMF.get().createEntityManager();
 		GameServer gs = null;
 		GameTemplate gt = null;
 		try {
 			// not sure when to enforce this...
 			if (gi.getGameServerId()!=null) {
 				gs = em.find(GameServer.class, gi.getGameServerId());
 				if (gs==null) {
 					resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Add GameInstance GameServer '"+gi.getGameServerId()+"' unknown");
 					return;
 				}
 				if (!KeyFactory.keyToString(gs.getOwnerId()).equals(KeyFactory.keyToString(account.getKey()))) {
 					resp.sendError(HttpServletResponse.SC_FORBIDDEN,"Add GameInstance GameServer '"+gi.getGameServerId()+"' not owned by "+account.getNickname());
 					return;				
 				}
 			}
 			if (gi.getGameTemplateId()==null) {
 				resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Add GameInstance must have gameTemplateId");
 				return;
 			}
 			gt = em.find(GameTemplate.class, GameTemplate.idToKey(gi.getGameTemplateId()));
 			if (gt==null) {
 				resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Add GameInstance GameTemplate '"+gi.getGameTemplateId()+"' unknown");
 				return;
 			}
 			if (!KeyFactory.keyToString(gt.getOwnerId()).equals(KeyFactory.keyToString(account.getKey()))) {
 				resp.sendError(HttpServletResponse.SC_FORBIDDEN,"Add GameInstance GameTemplate '"+gi.getGameTemplateId()+"' not owned by "+account.getNickname());
 				return;				
 			}
 
 			// cache state
			if (gi.getNumSlotsAllocated()<=0)
 				gi.setFull(true);
 			
 			em.persist(gi);
 			logger.info("Creating GameInstance "+gi+" for Account "+account.getUserId()+" ("+account.getNickname()+")");
 		}
 		finally {
 			em.close();
 		}
 
 		JSONUtils.sendGameInstance(resp, gi, gt, gs);
 	}
 }
