 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import capstone.player.Bot;
 import capstone.server.util.BotManager;
 import capstone.server.util.GameManager;
 import java.io.IOException;
 import java.net.URL;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Max, jesse, Ryan
  *
  * Create a new game for a player to join
  */
 public class GameCreate extends HttpServlet {
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
             IOException {
         response.setContentType("text");
         response.setCharacterEncoding("UTF-8");
 
         if (request.getParameter("type").equals("public")) {
             String playerName = request.getParameter("player");
             String gameID = GameManager.getGameID(playerName);                              //Public Games
             GameManager.joinGame(request.getSession(), gameID);
             response.getWriter().write("joined");
         }
         else if (request.getParameter("type").equals("private")) {
             String playerName = request.getParameter("player");
             String gameID = GameManager.getGameID(playerName);                              //Private Games
             GameManager.joinGame(request.getSession(), gameID);
             response.getWriter().write("joined");
         }
         else if (request.getParameter("type").equals("newprivate")) {
             GameManager.newGame(request.getSession());
             response.getWriter().write("joined");
             GameManager.addPrivateName(request.getSession());
         }
         else if(request.getParameter("type").equals("any")) {
            if (GameManager.openGames.isEmpty()) {
                 GameManager.newGame(request.getSession());
                response.getWriter().write("created");
            } else {
                GameManager.joinAnyGame(request.getSession());
                 response.getWriter().write("joined");
            }
             response.getWriter().close();
         } else if(request.getParameter("type").equals("bot")) {
             GameManager.newGame(request.getSession());
             Bot bot = BotManager.getBot(request.getParameter("botID"), this.getServletContext().getRealPath("."));
             GameManager.playerBotJoin(request.getSession(), bot);
         } else {
 
             GameManager.newGame(request.getSession());
 
             if (request.getParameter("type").equals("solo")) {
                 //For now, only default bot
                 GameManager.BotJoin(request.getSession());
             }
 
 
         }
     }
 
     @Override
     public String getServletInfo() {
         return "Create a new game";
     }
 }
