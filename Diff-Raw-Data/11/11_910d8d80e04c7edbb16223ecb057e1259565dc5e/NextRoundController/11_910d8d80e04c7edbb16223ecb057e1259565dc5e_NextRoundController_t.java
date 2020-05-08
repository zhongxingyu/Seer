 package controllers;
 
 import helpers.PlayerInfo;
 import helpers.PlayerPool;
 import helpers.RoundPairings;
 import helpers.Tournament;
 
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
  * Date: 4/5/12
  * Time: 1:29 PM
  */
 public class NextRoundController extends HttpServlet
 {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
       Tournament tournament = Tournament.getTournament();
       PlayerPool playerPool = tournament.getPlayerPool();
       RoundPairings roundPairings = new RoundPairings();
 
       String[] players = request.getParameterValues("player");
       String[] opponents = request.getParameterValues("opponent");
       String[] playerWins = request.getParameterValues("wins");
       String[] playerLosses = request.getParameterValues("losses");
       int round = tournament.getRound();
      int maxWins = tournament.getMaxWins();
       int bestOf = tournament.getBestOf();
 
       boolean hasErrors = validateValues(playerWins, playerLosses, maxWins, bestOf);
 
       if(!hasErrors){
          for (int i = 0; i < playerWins.length; i++) {
             String playerName = players[i];
             String opponentName = opponents[i];
             int playerWon = Integer.valueOf(playerWins[i]);
             int playerLost = Integer.valueOf(playerLosses[i]);
 
             playerPool.setPlayerOutcome(playerName, opponentName, round, playerWon, playerLost);
             playerPool.setPlayerOutcome(opponentName, playerName, round, playerLost, playerWon);
          }
          tournament.nextRound();
 
          String title = "Round " + tournament.getPrevRound() + " Standings";
          if(tournament.getRound() > tournament.getMaxRound()) {
             title = "Final Results";
          }
          request.setAttribute("title", title);
          request.setAttribute("droppable", tournament.getPlayerPool().hasDroppable());
          request.setAttribute("results", tournament.getCurrentRankings());
 
          getServletContext().getRequestDispatcher("/pages/results.jsp").forward(request, response);
       }
       else
       {
          request.setAttribute("title", "Round " + tournament.getRound());
          request.setAttribute("message", "Please enter the wins of each player and opponent.");
          request.setAttribute("error", "The values for wins for each player has to be a number, cannot be blank, cannot be less than 0 and should sum to " + tournament.getBestOf() + " or less.");
 
          if (tournament.isNextRound())
          {
             roundPairings.setRoundPairings();
             tournament.incPrevRound();
          }
 
          ArrayList<PlayerInfo> listOfPairs = new ArrayList<PlayerInfo>();
          SortedMap<String, PlayerInfo> clonedMapOfPlayers = playerPool.cloneMapOfPlayers();
          while (clonedMapOfPlayers.size() != 0)
          {
             PlayerInfo player = clonedMapOfPlayers.get(clonedMapOfPlayers.firstKey());
             listOfPairs.add(player);
             clonedMapOfPlayers.remove(player.getName());
             clonedMapOfPlayers.remove(player.getOpponent());
          }
          request.setAttribute("listOfPairs", listOfPairs);
          request.setAttribute("maxWin", Math.ceil(tournament.getBestOf() / 2));
          request.setAttribute("bestOf", tournament.getBestOf());
 
          getServletContext().getRequestDispatcher("/pages/show.jsp").forward(request, response);
       }
    }
 
    private boolean validateValues(String[] playerWins, String[] playerLosses, int maxWins, int bestOf)
    {
       boolean hasErrors = false;
       for (int i = 0; i < playerWins.length; i++)
       {
          if (playerWins[i].matches("[0-maxWins]") && (playerLosses[i].matches("[0-maxWins]")))
          {
             int wins = Integer.valueOf(playerWins[i]);
             int losses = Integer.valueOf(playerLosses[i]);
             if ((wins + losses > bestOf))
             {
                hasErrors = true;
             }
          }
          else
          {
             hasErrors = true;
          }
       }
       return hasErrors;
    }
 }
