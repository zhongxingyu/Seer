 package controllers;
 
 import helpers.PlayerPool;
 import helpers.Tournament;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * {NAME}
  * Author: Phillip Boksz
  * Date: 4/5/12
  * Time: 2:16 PM
  */
 public class DropController extends HttpServlet
 {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
       Tournament tournament = Tournament.getTournament();
       PlayerPool playerPool = tournament.getPlayerPool();
 
      String dropped = request.getParameter("dropped");
      String getsBye = request.getParameter("getsbye");
       
       if(!playerPool.dropPlayer(tournament.getRound(), dropped, getsBye)){
         request.setAttribute("error", dropped + " cannot be dropped because there would not be enough players left to adequately pair everyone in the remaining rounds.");
       }
 
       getServletContext().getRequestDispatcher("/pages/results.jsp").forward(request, response);
    }
 }
