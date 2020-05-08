 /**
  * SOEN 490
  * Capstone 2011
  * Front Controller; acts as an entrypoint for the program.
  * Delegates to FrontCommands.
  * Team members: 	Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 package application;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import foundation.Database;
import foundation.ServletInformation;
 
 public class FrontController extends HttpServlet {
 
 	private static final long serialVersionUID = 8691536805973858130L;
 	
 	private static FrontController singleton = null;
 	private HashMap<String, FrontCommand> commandMap;
 	private UnknownCommand unknownCommand = null;
 	
 	public FrontController()
 	{
 		commandMap = new HashMap<String, FrontCommand>();
 		unknownCommand = new UnknownCommand();
 		commandMap.put("GetMessages", new GetMessagesCommand());
 		commandMap.put("PutMessage", new PutMessageCommand());
 		commandMap.put("UpvoteMessage", new UpvoteMessageCommand());
 		commandMap.put("DownvoteMessage", new DownvoteMessageCommand());
 	}
 	
 	public static FrontController getInstance()
 	{
 		if (singleton == null)
 			singleton = new FrontController();
 		return singleton;
 	}
 	
 	private FrontCommand getCommand(String commandString)
 	{
 		FrontCommand command = commandMap.get(commandString);
 		if (command == null)
 			command = unknownCommand;
 		return command;
 	}
 	
 	private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException
 	{
 		String commandString = request.getParameter("command");
 		FrontCommand command = getCommand(commandString);
 		try {
 			if (command.responsible(request, response)) {
 				try {
 					command.execute(request, response);
 				}
 				catch (SQLException e) {
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 				}
 				catch (IOException e) {
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 				}
 			}
 		}
 		catch (Exception e) {
 			throw new ServletException(e);
 		}
 	}
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 		handleRequest(request, response);
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 		handleRequest(request, response);
 	}
 	
 	// Overridden to make sure that we have a database.
 	public void init() throws ServletException {
 		try {
			ServletInformation.getInstance().setServletContext(getServletContext());
 			if (!Database.getInstance().isDatabaseCreated())
 				Database.getInstance().createDatabase();
 		}
 		catch (Exception E) {
 			throw new ServletException(E);
 		}
 	}
 }
