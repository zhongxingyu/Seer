 package org.emaginalabs.demo.jaxws.cxf.ws;
 
 import org.emaginalabs.demo.jaxws.cxf.model.Player;
 
 import java.util.List;
 
 import javax.jws.WebService;
 
 
 @WebService
 public interface ITeamService
 {
     List<Player> getTeam();
 
     List<Player> getPlayers(int... numbers);
 
     boolean updatePlayerByNumber(int number, Player player);
 
     boolean deletePlayer(int number);
 
 }
