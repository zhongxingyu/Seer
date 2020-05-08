 package controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import daoimpl.MySQLGameDAO;
 import daoimpl.MySQLUsersGamesDAO;
 import daointerfaces.DALException;
 import daointerfaces.GameIDAO;
 import daointerfaces.UsersGamesIDAO;
 import dto.BrugerDTO;
 import dto.GameDTO;
 import dto.UsersDTO;
 import dto.UsersGamesDTO;
 import funktionalitet.IMyGamesLogic;
 import funktionalitet.IUserLogic;
 import funktionalitet.MyGamesLogic;
 import funktionalitet.UserLogic;
 
 public class ControlMyGames extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	private IMyGamesLogic myGames = null;
 	private UsersDTO user = null;
 	private IUserLogic u = null;
 	private GameIDAO games = null;
 	
 	
 	public ControlMyGames() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ServletContext application = request.getSession().getServletContext();
 		HttpSession session = request.getSession();
 
 		myGames = (MyGamesLogic) application.getAttribute("myGames");
 		if (myGames == null) {
 			try {
 				myGames = new MyGamesLogic();
 				application.setAttribute("myGames", myGames);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 		u = (UserLogic) application.getAttribute("userLogic");
 		if (u == null) {
 			try {
 				u = new UserLogic();
 				application.setAttribute("userLogic", u);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 		games = (MySQLGameDAO) session.getAttribute("games");
 		if (games == null) {
 				games = new MySQLGameDAO();
 				application.setAttribute("games", games);
 		}
 		user = (UsersDTO) session.getAttribute("user");
 		if (user == null) {
 			try {
 				String email = request.getUserPrincipal().getName();
 				user = u.getUser(email);
 				session.setAttribute("user", user);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 
 		// Getting the action parameter.
 		String action = null;
 		action = request.getParameter("action");
 		// Action: Log outs the current user.
 		// Redirects to index.jsp.
 		if ("List".equals(action)) { 
 			try {
 				List<GameDTO> gameList = new ArrayList<GameDTO>(myGames.getMyGames(user.getEmail()));
 				List<String> ga = new ArrayList<String>();
 				for(GameDTO game: gameList)
 					ga.add(game.getGname());
 				Collections.sort(ga); 
 				request.setAttribute("gameList", ga);
 				request.getRequestDispatcher("../WEB-INF/myGames/myGames.jsp").forward(request, response); // Sends the request to the actual operator list jsp file.
 			} catch (DALException e) {
				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 
 			}
 		}else if ("Add".equals(action)) { 
 			try {
 				List<GameDTO> gameList = new ArrayList<GameDTO>(games.getList());
 				request.setAttribute("gameList", gameList);
 				request.getRequestDispatcher("../WEB-INF/myGames/addGame.jsp").forward(request, response); // Sends the request to the actual operator list jsp file.
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 
 			}
 		}else if ("gameToAdd".equals(action)) { 
 			try {
 				String gameToAdd = (String) request.getParameter("gameToAdd");
 				int g = Integer.parseInt(gameToAdd);
 				GameDTO game = games.get(g);
 				UsersGamesDTO userGame = new UsersGamesDTO(user.getEmail(), game.getGid());
 				myGames.addGame(userGame);
 				String message = game.getGname()+" was succesfully added to your list!";
 				request.setAttribute("message", message);
 				request.getRequestDispatcher("index.jsp?action=Add").forward(request, response); // Sends the request to the actual operator list jsp file.
 			} catch (DALException e) {
 				String errorMessage = e.getMessage();
 				if (errorMessage.contains("PRIMARY")) { errorMessage = "You have already added the game to your account!"; } // Shows a special error message if the opr_id is already in use.
 				request.setAttribute("error", errorMessage);
 				request.getRequestDispatcher("index.jsp?action=Add").forward(request, response);
 			}
		}else {request.getRequestDispatcher("../WEB-INF/index").forward(request, response);}
 	}
 }
