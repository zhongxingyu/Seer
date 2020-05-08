 package org.hackermongo.gameofkrowns.application.service.impl;
 
 import java.util.List;
 
 import javax.ejb.Schedule;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.hackermongo.gameofkrowns.access.domain.Game;
 import org.hackermongo.gameofkrowns.application.service.GameofKrownsManagerTimerV1;
import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Stateless
 public class GameofKrownsManagerTimerBeanV1 implements GameofKrownsManagerTimerV1 {
 
 	private Logger log = LoggerFactory.getLogger(GameofKrownsManagerTimerBeanV1.class);
 	
 	@PersistenceContext(unitName = "gameofkrownsPersistenceUnit")
     private EntityManager entityManager;
 	
 	/**
 	 *  Check for timed out games once every hour and log them.
 	 */
 	@Schedule(hour="*/1", persistent=false)
 	public void handleTimeouts() {
 		log.info("Checking for timed out games....");
 		Query timeoutQuery = entityManager.createNamedQuery("findAllTimedOutEvents");
		timeoutQuery.setParameter("currentdate", new DateTime().toDate());
 		@SuppressWarnings("unchecked")
 		List<Game> timedOutGames = (List<Game>) timeoutQuery.getResultList();
 		for(Game timedOutGame : timedOutGames) {
 			log.warn("Game with gameId "+timedOutGame.getGameId()+" has timed out.");
 		}
 	}
 
 }
