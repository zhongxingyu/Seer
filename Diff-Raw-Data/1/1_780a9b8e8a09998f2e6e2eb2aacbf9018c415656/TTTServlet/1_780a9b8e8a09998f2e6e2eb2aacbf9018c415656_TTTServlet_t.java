 //BEGIN FILE TTTServlet.java
 package controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.Collections;
 
 import model.*;
 
 /**
  * Servlet implementation class TTTServlet
  * @author Mustache Cash Stash
  * @version 1.0
  */
 @WebServlet(description = "Servlet for handling tic-tac-toe interactions", urlPatterns = { "/TTTServlet" })
 //BEGIN CLASS TTTServlet
 public class TTTServlet extends HttpServlet 
 {
 	public static final long TIMEOUT_TIME = 60000; //In milliseconds
 	private static final long serialVersionUID = 1L;
 	private HashMap<Integer, Game> currentGames;
 	private HashMap<User, HashSet<User>> gameRequests; //Key is player game is requested AGAINST, NOT by
 	private HashMap<User, HashSet<Integer>> openGames;
 	private Set<User> users;
 	private Random rng;
 	private OnlineUserTracker onlineUsers;
        
     /**
      * Starts the HTTPServlet
      * @see HttpServlet#HttpServlet()
      */
 	//BEGIN CONSTRUCTOR public TTTServlet() 
     public TTTServlet() 
     {
     	currentGames = new HashMap<Integer, Game>();
     	gameRequests = new HashMap<User, HashSet<User>>();
     	users = Collections.synchronizedSet(new HashSet<User>());
     	openGames = new HashMap<User, HashSet<Integer>>();
     	rng = new Random();
     	onlineUsers = new OnlineUserTracker(TIMEOUT_TIME);        
     }
     //END METHOD public TTTServlet() 
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
     @Override
     //BEGIN METHOD protected void doGet(HttpServletRequest request, HttpServletResponse response)
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
 	{
 		PrintWriter writer = response.getWriter();
 		String gameNumString = request.getParameter("GameNumber");
 		String userString = request.getParameter("User");
 		if(gameNumString != null)
 		{
 			int gameNum = Integer.parseInt(gameNumString);
 			Game game = this.currentGames.get(gameNum);
 			if(game != null)
 			{
 				if(game.hasAPlayer(User.parseUser(userString)))
 				{
 					StringBuffer sb = new StringBuffer();
 					sb.append("<TTTGame num=\""+gameNum+"\">\n");
 					sb.append(boardToXML(game.getBoard()));
 					sb.append("\n</TTTGame>");
 					writer.println(sb.toString());
 				}
 				else
 				{
 					response.sendError(403, "You shouldn't be looking at other people's games. Cheater.");
 				}
 			}
 			
 			else
 			{
 				response.sendError(404, "lol, that's not a game.");
 			}
 		}
 		else if(userString != null)
 		{
 			StringBuffer sb = new StringBuffer();
 			User user = User.parseUser(userString);
 			sb.append("<User userName=\""+userString+"\">");
 			if(this.gameRequests.get(user) != null)
 			{
 				for(User u:this.gameRequests.get(user))
 				{
 					sb.append("<requestingUser>"+u+"</requestingUser>\n");
 				}
 			}
 			if(this.currentGames.get(user) != null)
 			{
 				for(Integer i:this.openGames.get(user))
 				{
 					sb.append("<openGame>"+i+"</openGame>\n");
 				}
 			}
 			sb.append("</User>");
 			writer.println(sb);
 		}
 		else
 		{
 			StringBuffer sb = new StringBuffer();
 			sb.append("<Server timeout=\"");
 			sb.append(TIMEOUT_TIME);
 			sb.append("\">\n");
 			sb.append(onlineUsers.getOnlineUsersInXML());
 			sb.append("</Server>");
 			writer.print(sb.toString());
 		}
 	}
 	//END METHOD protected void doGet(HttpServletRequest request, HttpServletResponse response)
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 * Handles requests for moves, resignation and new game requests.
 	 * Requests for moves and resignation require the following:
 	 *   an int "GameNumber" assigned by the server
 	 *   a User "User" who is making the request. Should be in string representation "characterName.XXX" where XXX is the code number.
 	 *   Which piece is being controlled, "Side". Either "X" or "O".
 	 * Additionally, a move requires the following fields:
 	 *   an int "xPosition", representing the index (0-indexed) of the column to play in.
 	 *   an int "yPosition", representing the index (0-indexed) of the row to play in.
 	 * 	 x and y position follow the following schematic:
 	 *     [0,0][0,1][0,2]
 	 *     [1,0][1,1][1,2]
 	 *     [2,0][2,1][2,2]
 	 * Alternatively, to resign, the parameter "Resign" should be sent with the value "true"
 	 * 
 	 * Sending a request with "RequestedUserName" by itself will create a user with that name and return a string representation of your User.
 	 * 
 	 * Request a game against an opponent by including parameter "RequestedOpponent".
 	 * This parameter's value should be a string representation of another user.
 	 * 
 	 * Keep a user alive by sending the user's string representation in "UserToKeepAlive"
 	 */
     @Override
 	//BEGIN METHOD protected void doPost(HttpServletRequest request, HttpServletResponse response)
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
 	{
 		String gameNumString = request.getParameter("GameNumber");
 		String userString = request.getParameter("User");
 		String side = request.getParameter("Side");
 		String resigning = request.getParameter("Resign");
 		String newUserName = request.getParameter("RequestedUserName");
 		String opponentRequest = request.getParameter("RequestedOpponent");
 		String liveRequest = request.getParameter("UserToKeepAlive");
 		
 		if (resigning == null) 
 		{
 			resigning = "false";
 		}
 		if(liveRequest != null)
 		{
 			User userToKeepAlive = User.parseUser(liveRequest);
 			//if(users.contains(userToKeepAlive))
 			//{
 				onlineUsers.keepUserAlive(userToKeepAlive);
 				response.getWriter().print("Success");
 			//}
 		}
 		else if(gameNumString != null)
 		{
 			int gameNum = Integer.parseInt(gameNumString);
 			Game game = this.currentGames.get(gameNum);
 			
 			if(game != null)
 			{
 				boolean success = false;
 				PlaceValue piece = PlaceValue.parsePlaceValue(side);
 				if(resigning.equals("true")) 
 				{
 					switch(piece)
 					{
 					case X:
 						if(game.hasXPlayer(User.parseUser(userString)))
 						{
 							success = game.resign(piece);
 						}
 						break;
 					case O:
 						if(game.hasOPlayer(User.parseUser(userString)))
 						{
 							success = game.resign(piece);
 						}
 						break;
 					default:
 						success = false;
 						break;
 					}
 				}
 				else 
 				{
 					int xPos = Integer.parseInt(request.getParameter("xPosition"));
 					int yPos = Integer.parseInt(request.getParameter("yPosition"));
 					switch(piece){
 						case X:
 							if(game.hasXPlayer(User.parseUser(userString)))
 							{
 								success = game.requestMove(xPos, yPos, piece);
 							}
 							break;
 						case O:
 							if(game.hasOPlayer(User.parseUser(userString)))
 							{
 								success = game.requestMove(xPos, yPos, piece);
 							}
 							break;
 						default:
 							success = false;
 							break;
 					}
 				}
 				
 				if(success)
 				{
 					response.getWriter().print("Success");
 				}
 				else
 				{
 					response.getWriter().print("Failure");
 				}
 			}
 		}
 		else if(newUserName != null)
 		{
 			int i;
 			User newUser;
 			do
 			{
 				i = rng.nextInt(1000);
 				newUser = new User(newUserName, i);
 			}
 			while(users.contains(newUser));
 			
 			if(users.add(newUser))
 			{
 				if(users.contains(newUser))	response.getWriter().print(newUser);
 			}
 			else{response.getWriter().print("FFUU");}
 		}
 		else if(opponentRequest != null)
 		{
 			User requestingUser = User.parseUser(userString);
 			User challengedUser = User.parseUser(opponentRequest);
 			HashSet<User> requests = gameRequests.get(requestingUser);
 			
 			if(requests == null)
 			{
 				gameRequests.put(requestingUser, new HashSet<User>());
				requests = gameRequests.get(requestingUser);
 			}
 			if(requests.contains(challengedUser))
 			{
 				int i;
 				if(rng.nextBoolean()){
 					do{
 						i = rng.nextInt(8191)+1;
 					}while(!currentGames.containsKey(i));
 					currentGames.put(i, new Game(requestingUser, challengedUser));
 				}
 				else
 				{
 					do
 					{
 						i = rng.nextInt(8191)+1;
 					}
 					while(!currentGames.containsKey(i));
 					currentGames.put(i, new Game(challengedUser, requestingUser));
 				}
 				requests.remove(challengedUser);
 				if(openGames.get(requestingUser)== null)
 				{
 					openGames.put(requestingUser, new HashSet<Integer>());
 					openGames.get(requestingUser).add(i);
 				}
 				else
 				{
 					openGames.get(challengedUser).add(i);
 				}
 				if(openGames.get(challengedUser)== null)
 				{
 					openGames.put(challengedUser, new HashSet<Integer>());
 					openGames.get(challengedUser).add(i);
 				}
 				else
 				{
 					openGames.get(challengedUser).add(i);
 				}
 				response.getWriter().print(i);
 			}
 			else
 			{
 				HashSet<User> challengedUserRequests = gameRequests.get(challengedUser);
 				
 				if(challengedUserRequests == null)
 				{
 					gameRequests.put(challengedUser, new HashSet<User>());
 					gameRequests.get(challengedUser).add(requestingUser);
 				}
 				else
 				{
 					gameRequests.get(challengedUser).add(requestingUser);
 				}
 				
 				response.getWriter().print("Success");
 			}
 		}
 	}
 	//END METHOD protected void doPost(HttpServletRequest request, HttpServletResponse response)
 	
 	/**
 	 * Converts a GameBoard into an XML String
 	 * @param board the GameBoard to convert
 	 * @return The XML String of this GamebBoard
 	 */
 	//BEGIN METHOD private static String boardToXML(GameboardImp board)
 	private static String boardToXML(GameboardImp board)
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append("  <gameState>\n");
 		sb.append("    <gameResult>"+board.getResult().getRepr()+"</gameResult>\n");
 		sb.append("    <whoseTurn>");
 		if(board.xsTurn()){
 			sb.append("X");
 		}else if(board.osTurn()){
 			sb.append("O");
 		}else{
 			sb.append("Nobody");
 		}
 		sb.append("    </whoseTurn>\n");
 		sb.append("    <gameBoard>\n");
 		for(int i = 0; i < 3; i++){
 			sb.append("      <row id=\"row"+i+">");
 			for(int j = 0; j < 3; j++){
 				sb.append(board.getBoard()[i][j].getRepr());
 			}
 			sb.append("</row>\n");
 		}
 		sb.append("    </gameBoard>\n");
 		sb.append("  </gameState>");
 		return sb.toString();
 	}
 	//END METHOD private static String boardToXML(GameboardImp board)
 
 }
 //END CLASS TTTServlet
 //END FILE TTTServlet.java
