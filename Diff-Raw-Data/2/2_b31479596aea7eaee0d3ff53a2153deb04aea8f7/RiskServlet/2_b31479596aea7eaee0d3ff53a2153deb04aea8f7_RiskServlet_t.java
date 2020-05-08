 package edu.gatech.cs2340.risk.controller;
 
 import edu.gatech.cs2340.risk.model.Player;
 import edu.gatech.cs2340.risk.model.GameLogic;
 import edu.gatech.cs2340.risk.model.Planet;
 import edu.gatech.cs2340.risk.model.StarSystem;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.*;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @WebServlet(urlPatterns={
         "/playerSelection", // GET
 		"/game", //GAME
         "/create", // POST 
         "/update/*", // PUT
         "/delete/*" // DELETE
     })
 	
 	public class RiskServlet extends HttpServlet {
 
     ArrayList<Player> players = new ArrayList<Player>();
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         System.out.println("In doPost()");
         // Handle the hidden HTML form field that simulates
         // HTTP PUT and DELETE methods.
         String operation = (String) request.getParameter("operation");
         // If form didn't contain an operation field and
         // we're in doPost(), the operation is POST
         if (null == operation) operation = "POST";
         System.out.println("operation is " + operation);
         if (operation.equalsIgnoreCase("PUT")) {
             System.out.println("Delegating to doPut().");
             doPut(request, response);
         } else if (operation.equalsIgnoreCase("DELETE")) {
             System.out.println("Delegating to doDelete().");
             doDelete(request, response);
         } else if (operation.equalsIgnoreCase("RANDOM")) {
 			System.out.println("Delegating to doRandom().");
 			doRandom(request, response);
 		} else if (operation.equalsIgnoreCase("GAME")) {
 			System.out.println("Delegating to doGame().");
 			doGame(request, response);
         } else if (operation.equalsIgnoreCase("PLANET_INFO")) {
             System.out.println("Delegating to doPlanetStats");
             doPlanetStats(request, response);
 		} else {
             String name = request.getParameter("name");
             String color = request.getParameter("color");
             players.add(players.size(), new Player(name, color));
             request.setAttribute("players", players);
             RequestDispatcher dispatcher = 
                 getServletContext().getRequestDispatcher("/playerSelection.jsp");
             dispatcher.forward(request,response);
         }
     }
 
     /**
      * Called when HTTP method is GET 
      * (e.g., from an <a href="...">...</a> link).
      */
     protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
             throws IOException, ServletException {
         System.out.println("In doGet()");
         request.setAttribute("players", players);
         RequestDispatcher dispatcher = 
             getServletContext().getRequestDispatcher("/playerSelection.jsp");
         dispatcher.forward(request,response);
     }
 	
 	protected void doRandom(HttpServletRequest request,
                          HttpServletResponse response)
             throws IOException, ServletException {
         System.out.println("In doRandom()");
         request.setAttribute("players", players);
 		Collections.shuffle(players);
         RequestDispatcher dispatcher = 
             getServletContext().getRequestDispatcher("/playerSelection.jsp");
         dispatcher.forward(request,response);
     }
 	
 	protected void doGame(HttpServletRequest request,
                          HttpServletResponse response)
             throws IOException, ServletException {
         System.out.println("In doGame()");
 		GameLogic game = new GameLogic(players);
 		ArrayList<StarSystem> systems = game.getAllSystems();
 		request.setAttribute("players", players);
 		request.setAttribute("game", game);
 		request.setAttribute("systems", systems);
         RequestDispatcher dispatcher = 
            getServletContext().getRequestDispatcher("/map.jsp");
         dispatcher.forward(request,response);
     }
 
 
     protected void doPlanetStats(HttpServletRequest request,
                                 HttpServletResponse response)
             throws IOException, ServletException {
        
         RequestDispatcher dispatcher =
             getServletContext().getRequestDispatcher("/planetStats.jsp");
         dispatcher.forward(request, response);
     }
 	
     protected void doPut(HttpServletRequest request,
                          HttpServletResponse response)
             throws IOException, ServletException {
         System.out.println("In doPut()");
         String name = (String) request.getParameter("name");
         String color = (String)  request.getParameter("color");
         int id = getId(request);
         players.add(id, new Player(name, color));
         request.setAttribute("players", players);
         RequestDispatcher dispatcher = 
             getServletContext().getRequestDispatcher("/playerSelection.jsp");
         dispatcher.forward(request,response);
     }
 
     protected void doDelete(HttpServletRequest request,
                             HttpServletResponse response)
             throws IOException, ServletException {
         System.out.println("In doDelete()");
         int id = getId(request);
         players.remove(id);
         request.setAttribute("players", players);
         RequestDispatcher dispatcher = 
             getServletContext().getRequestDispatcher("/playerSelection.jsp");
         dispatcher.forward(request,response);
     }
 
     private int getId(HttpServletRequest request) {
         String uri = request.getPathInfo();
         // Strip off the leading slash, e.g. "/2" becomes "2"
         String idStr = uri.substring(1, uri.length()); 
         return Integer.parseInt(idStr);
     }    
 
 }
