 package ie.cit.clouddev.services;
 
 
 import ie.cit.clouddev.domain.Player;
 import ie.cit.clouddev.domain.TPlayers;
 import ie.cit.clouddev.domain.dao.PlayersRepository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.transaction.annotation.Transactional;
 @Transactional
 public class TplayersServiceImpl implements TplayersService {
 	
 	private PlayersRepository repo;
 
 	public TplayersServiceImpl(PlayersRepository repo) {
 		this.repo = repo;
 	}
 
 	public List<Player> getAllPlayers() {
 		return repo.getAllP();
 	}
 	
 	
 	
 	public Player newplayer(String name) {
 		Player player = new Player();
 		player.setName(name);
 		repo.add(player);
 		return player;
 	}
 	
 	public void fit(String playerId) {
 		Player player = repo.findPlayerId(playerId);
 		if ( player !=null) {
 				player.setFittoplay(false);
 				repo.update(player);
 		}
 	}
 	
 	
 	public void notfit(String playerId) {
 		Player player = repo.findPlayerId(playerId);
 		if ( player !=null) {
 				player.setFittoplay(true);
 				repo.update(player);
 		}
 	}
 	/*
 	 * 
 	
 
 	
 
 	
 	
 	
 	 * public TplayerServiceImpl() {
 		Player player = new Player();
 		player.setName("Aine Mitchell");
 		player.setDob("01-Jan-2001");
 		player.setContactno("071 9183477");
 		players.add(player);
 	}*/
 
 	
 
 	
 	
 	
 	
 	
 
 	public Player get(String playerId) {
 		
 			return repo.findPlayerId(playerId);
 		}
 	
 	@Override
 	public void delete(String PlayerId) {
 		repo.delete(PlayerId);
 	}
 	
 	
 
 	
 }
