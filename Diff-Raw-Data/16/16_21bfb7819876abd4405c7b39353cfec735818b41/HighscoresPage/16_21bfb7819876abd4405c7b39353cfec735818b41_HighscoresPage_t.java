 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import ServerCom.RemoteTalker;
 import data.Player;
 import database.PersistenceManager;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Toshiba
  */
 public class HighscoresPage extends HttpServlet {
     
     /**
      * Gets all data from DB, which will be displayed on main screen (list of friends, monsters and notifications).
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     private void getDataFromDB(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession(false);
         if (session == null || session.getAttribute("user") == null) {
             // Redirects when user is not logged in
             response.sendRedirect("");
         } else {
             Player current = (Player) session.getAttribute("user");
             PersistenceManager pm = new PersistenceManager();
             // Check if any monster dies:
             pm.checkIfAnyMonsterDies();
             // Updates player informations
             current = pm.getPlayer(current.getUserID());
             session.setAttribute("user", current);
             // Saves all notifications to attribute
             request.setAttribute("notificationList", current.getNotifications());
             // Saves all friends to attribute
             request.setAttribute("friendList", current.getFriends());
             // Saves all friend requests to attribute
             request.setAttribute("requestList", pm.getFriendRequestList(current.getUserID()));
             // Saves all fight requests to attribute
             request.setAttribute("monsterRequestList", pm.getFightRequests(current.getUserID()));
             // Saves all monsters to attribute
             request.setAttribute("monsterList", pm.getMonsterList(current.getUserID()));
            request.getRequestDispatcher("/WEB-INF/highscores_page.jsp").forward(request, response);
         }
     }
 
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession(false);
         if(session == null || session.getAttribute("user") == null){
             // Redirects when user is not logged in
             response.sendRedirect("");
         }else{
             PersistenceManager pm = new PersistenceManager();
             Player current = (Player)session.getAttribute("user");
             RemoteTalker rt = new RemoteTalker();
             ArrayList<String> highscores = rt.getHighScores(current);
             request.setAttribute("highscores", highscores);
             this.getDataFromDB(request, response);
         }
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doGet(request, response);
     }
 
 }
