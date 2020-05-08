 package lv.k2611a.service.game;
 
 import java.util.List;
 
 import lv.k2611a.domain.Map;
 import lv.k2611a.domain.Player;
 import lv.k2611a.network.resp.UpdateMap;
 import lv.k2611a.util.Point;
 
 public interface GameService {
     UpdateMap getFullMapUpdate();
 
     long getTickCount();
 
     void setTickCount(long tickCount);
 
     boolean isOwner(int buildingId, int playerId);
 
     boolean playerExist(int playerId);
 
     void registerChangedTile(Point point);
 
     Integer getFreePlayer();
 
     void freePlayer(Integer playerId);
 
     void start(Map map);
 
     List<Player> getPlayers();

    boolean hasLost(int playerId);
 }
