 package org.emaginalabs.demo.jaxws.cxf.client.spring;
 
 import java.util.List;
 
 import org.emaginalabs.demo.jaxws.cxf.model.Player;
 import org.emaginalabs.demo.jaxws.cxf.ws.ITeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 
 
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);
 
 
     public static void main(String[] args) throws Exception
     {
         //Initializes Spring Container
         ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("/spring/applicationContext.xml");
         ITeamService teamServiceClient = (ITeamService) applicationContext.getBean("teamServiceClient");
 
         //test
         logger.info("getTeam");
         List<Player> team = teamServiceClient.getTeam();
         for (Player player : team)
         {
             logger.info("       " + player.getNumber() + " : " + player.getName() + " (" + player.getAge() +")");
         }
 
         logger.info("\n updatePlayerByNumber");
        teamServiceClient.updatePlayerByNumber(1, new Player(1, "Anders Lindegaard", 28));
 
         logger.info("\n deletePlayer");
         teamServiceClient.deletePlayer(6);
 
         logger.info("\n getPlayers");
         team = teamServiceClient.getPlayers(1,3,6);
         for (Player player : team)
         {
             logger.info("       " + player.getNumber() + " : " + player.getName() + " (" + player.getAge() +")");
         }
     }
 
 }
