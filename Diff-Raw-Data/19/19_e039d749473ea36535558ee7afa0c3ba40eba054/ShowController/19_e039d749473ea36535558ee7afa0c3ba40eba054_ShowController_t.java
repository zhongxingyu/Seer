 package magic.tournament.generator.controllers;
 
 import magic.tournament.generator.helpers.PlayerInfo;
 import magic.tournament.generator.helpers.PlayerPool;
 import magic.tournament.generator.helpers.RoundPairings;
 import magic.tournament.generator.helpers.Tournament;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.SortedMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * {NAME}
  * Author: Phillip Boksz
  * Date: 4/3/12
  * Time: 2:43 PM
  */
 public class ShowController extends HttpServlet
 {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
       Tournament tournament = Tournament.getTournament();
       PlayerPool playerPool =  tournament.getPlayerPool();
       RoundPairings roundPairings =  new RoundPairings();
 
       request.setAttribute("title", "Round " + tournament.getRound());
       request.setAttribute("message", "Please enter the wins of each player and opponent");
 
       if(tournament.isNextRound()){
          roundPairings.setRoundPairings();
          tournament.incPrevRound();
       }
 
       ArrayList<PlayerInfo> listOfPairs = new ArrayList<PlayerInfo>();
       SortedMap<String, PlayerInfo> clonedMapOfPlayers = playerPool.cloneMapOfPlayers();
       while(clonedMapOfPlayers.size() != 0) {
          PlayerInfo player = clonedMapOfPlayers.get(clonedMapOfPlayers.firstKey());
          listOfPairs.add(player);
          clonedMapOfPlayers.remove(player.getName());
          clonedMapOfPlayers.remove(player.getOpponent());
       }
       request.setAttribute("listOfPairs", listOfPairs);
      request.setAttribute("maxWins", tournament.getMaxWins());
       request.setAttribute("bestOf", tournament.getBestOf());
       
       getServletContext().getRequestDispatcher("/pages/show.jsp").forward(request, response);
    }
 }
