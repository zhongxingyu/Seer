 package edu.wm.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import Exceptions.NoPlayerFoundException;
 import Exceptions.NoPlayersException;
 
 import werewolf.dao.IPlayerDAO;
 import werewolf.dao.PostgresPlayerDAO;
 
 import edu.wm.something.domain.GPSLocation;
 import edu.wm.something.domain.Player;
 
 public class GameService {
 
 		//@Autowired private MongoPlayerDAO playerDao;
 		//@Autowired private MongoUserDAO userDao;
 		@Autowired private PlayerService playerService;
 		//@Autowired private PostgresPlayerDAO postgresPlayerDao;
 		private static PostgresPlayerDAO postgresPlayerDao = new PostgresPlayerDAO();
 		
 		static Logger logger = Logger.getLogger(GameService.class.getName());
 
 		
 		public void updatePosition(Player player, GPSLocation location){
 			player.setLat(location.getLat());
 			player.setLng(location.getLng());
 		}
 		
 		public List<Player> getAllAlive() throws NoPlayersException {
 			logger.info("In gameSerice.getAllAlive()");
 			return PlayerService.getAllAlive();
 
 		}
 		
 		public List<Player> getAllWerewolves() throws NoPlayersException {
 			List<Player> playerList = getAllAlive();
 			List<Player> werewolfList = (List<Player>) new ArrayList<Player>();
 			int length = playerList.size();
 			for (int i=0;i<length;i++){
 				Player player = playerList.get(i);
 				if (player.isWereWolf()){
 					werewolfList.add(player);
 				}
 			}
 			return werewolfList;
 		}
 		
 		
 		public List<Player> getAllNear(Player p) {
 			return playerService.getAllNear(p);
 		}
 		
 		public List<Player> Move(Player p, long lat,long lng) throws NoPlayersException {
 			p.setLat(lat);
 			p.setLng(lng);
 			return getAllAlive();
 		}
 		
 		public void Update(Player p) throws NoPlayerFoundException {
 			postgresPlayerDao.updatePlayer(p);
 		}
 		
 		public List<Player> Kill(Player p) throws NoPlayerFoundException, NoPlayersException {
 			playerService.deletePlayer(p);
 			return PlayerService.getAllAlive();
 		}
 		
 		public List<Player> CreatePlayer(Player p) throws NoPlayersException {
 			playerService.addplayer(p);
 			return PlayerService.getAllAlive();
 		}
 
 		public Player getPlayerByID(int ownerId) throws NoPlayerFoundException {
 			//return playerService.getPlayerFromDbByID(ownerId);
 			return postgresPlayerDao.getPlayerById(ownerId);
 		}
 
 		public Player getPicByID(int ownerId) throws NoPlayerFoundException {
 			return postgresPlayerDao.getPlayerPic(ownerId);
 		}
 
 		public Player playerInfoRequest(int ownerId) throws NoPlayerFoundException {
 			return postgresPlayerDao.getPlayerInfo(ownerId);
 		}
 
 		public void voteOnPlayer(Player p) throws NoPlayerFoundException {
 			postgresPlayerDao.voteOnPlayer(p);
 		}
 		
 		public Boolean canKill(Player killer, Player victim){
 			if ((killer.isWereWolf())&&(killer.isNear(victim))){
 				return true;
 			}
 			else{
 				return false;
 			}
 		}
 		
 		@Scheduled(fixedDelay=5000)
 		public void checkGameOperation(){
 			//check if all players have checked in recently
 			//logger.info("checking game operation...");
 		}
 		
 		public void restartGame(){
 			
 			//playerDao.dropAllPlayers();
 			
 			//List<MyUser> users = userDao.getAllUsers();
 			List<Player> players = new ArrayList<>();
 			
 			//for (MyUser u : users){
 				//Player p = ne PLayer();
 				//p.setId(u.getId);
 				//and more
 			//}
 		}
 		
 }
