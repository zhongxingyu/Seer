 package com.erman.football.server.service;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.erman.football.client.service.LoginService;
 import com.erman.football.client.service.PlayerException;
 import com.erman.football.server.data.Player;
 import com.erman.football.server.data.Player_JDO_DB;
 import com.erman.football.shared.ClientPlayer;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 @SuppressWarnings("serial")
 public class LoginServiceImpl extends RemoteServiceServlet implements LoginService{
 
 	public ClientPlayer login(ClientPlayer player) throws PlayerException {
 		ClientPlayer result = null;
 		HttpServletRequest request = this.getThreadLocalRequest();
 		if(player==null){
 			//Check if already logged in
 			String playerId = (String)request.getSession().getAttribute("player");
 			if(playerId == null){
 				return result;
 			}else{
 				if(playerId.equals("-1")){
 					result = new ClientPlayer();
 					result.setAdmin(true);
 					result.setEmail("admin@main");
 					result.setKey(0);
 					result.setName("admin");
 					return result;
 				}
 				Player playerDO = Player_JDO_DB.getUserbyId(Long.parseLong(playerId));
 				if(playerDO==null){
 					return result;
 				}else{
 					return playerDO.convert();
 				}
 			}
 		}
 		if(player.getFacebookId()!=0){
 			//facebook user. Add and return if not in db, return user if in db
 			Player playerDO = Player_JDO_DB.getFacebookPlayer(player.getFacebookId());
 			if(playerDO==null){
 				try {
 					result = Player_JDO_DB.addUser(player);
 					request.getSession().setAttribute("player",String.valueOf(result.getKey()));
 				} catch (PlayerException e) {
 					//Should not be here for facebook user
 				}
 				
 				return result;
 			}else{
 				//known facebook user
 				request.getSession().setAttribute("player",String.valueOf(playerDO.getKey()));
 				return playerDO.convert();
 			}
 
 		}
 
 		// a non facebook player supplied
 		if(player.getEmail().equals("admin")){
 			result = new ClientPlayer();
 			result.setAdmin(true);
 			result.setEmail("admin@main");
 			result.setKey(-1);
 			result.setName("admin");
 			request.getSession().setAttribute("player",String.valueOf(result.getKey()));
 			return result;
 		}
 		//Not admin not facebook user. Check if user is known
 		Player playerDO = Player_JDO_DB.getUser(player.getEmail());
 		if(playerDO==null){
 			//maybe it is a join. check name is present
 			if(player.getName()!=null && !player.getName().equals("") && !player.getName().equals("name")){
 				//it is a join. add user
 				return Player_JDO_DB.addUser(player);
 			}
 			// unknown user with no name provided. return null (not logged in)
 			return result;
 		}else{
 			//known user if username supplied it is a join
 			if(player.getName()!=null && !player.getName().equals("") && !player.getName().equals("name")){
 				//it is a join. throw exception
 				throw new PlayerException("Email kullaniliyor");
 			}
 			request.getSession().setAttribute("player",String.valueOf(playerDO.getKey()));
 			return playerDO.convert();
 		}
 
 
 	}
 
 	public ClientPlayer logout() {
 		HttpServletRequest request = this.getThreadLocalRequest();
 		String playerId = (String)request.getSession().getAttribute("player");
 		if(playerId == null){
 			return null;
 		}
 		request.getSession().setAttribute("player",null);
 		Player tmpPlayer =  Player_JDO_DB.getUserbyId(Long.parseLong(playerId));
 		if(tmpPlayer!=null){
 			return tmpPlayer.convert();
 		}
 		return null;
 	}
 
 }
