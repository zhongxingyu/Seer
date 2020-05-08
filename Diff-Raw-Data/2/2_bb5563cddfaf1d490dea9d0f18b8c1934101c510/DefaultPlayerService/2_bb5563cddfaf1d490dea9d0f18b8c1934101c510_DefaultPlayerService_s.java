 package au.com.pingmate.service;
 
 import au.com.pingmate.dao.PlayerDao;
 import au.com.pingmate.domain.PingPongPlayer;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 @Service
 public class DefaultPlayerService implements PlayerService {
    private static final int INITIAL_RANKING = 1200;
 
     @Autowired
     private PlayerDao playerDao;
 
     @Transactional
     public PingPongPlayer findPlayer(int id) {
         return playerDao.find(id);
     }
 
     @Transactional
     public void addPlayer(PingPongPlayer player) {
         player.setRanking(INITIAL_RANKING);
         player.setResigned(false);
         playerDao.save(player);
     }
 
     @Transactional
     public List<PingPongPlayer> listActivePlayers() {
         //todo: filter active only players here
         return playerDao.listPlayers();
     }
 }
