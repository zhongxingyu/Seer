 package nu.danielsundberg.goodstuff.application.service.impl;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import nu.danielsundberg.goodstuff.access.entity.Game;
 import nu.danielsundberg.goodstuff.access.entity.Player;
 import nu.danielsundberg.goodstuff.application.service.GameControllService;
 
 @Stateless
 public class GameControllServiceBean implements GameControllService {
 
	@PersistenceContext(unitName = "games")
     private EntityManager entityManager;
     
 	public void setEntityManager(final EntityManager entityManager) {
             this.entityManager = entityManager;
     }
 
 	@Override
 	public Set<Game> getGamesForPlayer(String playerId) {
 		Query playerQuery = entityManager.createNamedQuery("player.findByPlayerId");
 		playerQuery.setParameter("playerId", playerId);
 		Player player = (Player) playerQuery.getSingleResult();
 		
 		Query gameQuery = entityManager.createNamedQuery("game.findByPlayer");
 		gameQuery.setParameter("player", player);
 		@SuppressWarnings("unchecked")
 		Set<Game> games = new HashSet<Game>((List<Game>) gameQuery.getResultList());
 		return games;
 	}
 	
 }
