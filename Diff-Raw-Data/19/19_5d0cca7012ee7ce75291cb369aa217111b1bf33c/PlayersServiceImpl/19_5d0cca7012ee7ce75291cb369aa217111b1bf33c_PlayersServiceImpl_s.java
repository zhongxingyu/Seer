 package cz.krajcovic.playersengine.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import cz.krajcovic.playersengine.base.Player;
 import cz.krajcovic.playersengine.client.PlayersService;
 
 public class PlayersServiceImpl extends RemoteServiceServlet implements
 		PlayersService {
 
 	/**
 	 * 
 	 */
	private static final long serialVersionUID = -5767380066228906245L;

 	ArrayList<Player> playersList = new ArrayList<Player>();
 
 	@Override
 	public void add(Player player) {
 		playersList.add(player);
 
 	}
 
 	@Override
 	public void update(int playerId, Player player) {
 
 		Player old = getById(playerId);
 		if (old != null) {
 			old.update(player);
 		}
 	}
 
 	@Override
 	public boolean remove(int playerId) {
 
 		Player old = getById(playerId);
 		if (old != null) {
 			return playersList.remove(old);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean remove(Player player) {
 
 		return playersList.remove(player);
 
 	}
 
 	@Override
 	public Player getById(int index) {
 		for (Player player : playersList) {
 			if (player.getId() == index)
 				return player;
 		}
 		return null;
 	}
 
 	@Override
 	public List<Player> getAll() {
 		return playersList;
 	}
 
 }
