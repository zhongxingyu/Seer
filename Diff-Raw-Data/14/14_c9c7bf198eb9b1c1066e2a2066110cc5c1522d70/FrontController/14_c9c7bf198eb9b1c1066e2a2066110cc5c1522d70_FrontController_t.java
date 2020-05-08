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
 import java.net.InetAddress;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Logger;
 
 import exceptions.MapperException;
 import exceptions.ParameterException;
 import exceptions.UnrecognizedUserException;
 import application.commands.CreateMessageCommand;
 import application.commands.CreateUserCommand;
 import application.commands.DeleteMessageCommand;
 import application.commands.DeleteUserCommand;
 import application.commands.DownvoteMessageCommand;
 import application.commands.FrontCommand;
 import application.commands.GetMessageIDsCommand;
 import application.commands.GetServerParametersCommand;
 import application.commands.ReadMessageCommand;
 import application.commands.ReadUserCommand;
 import application.commands.UnsupportedCommand;
 import application.commands.UpdateServerParametersCommand;
 import application.commands.UpdateUserCommand;
 import application.commands.UpvoteMessageCommand;
 import foundation.Database;
import foundation.finder.ServerListFinder;
 import foundation.tdg.ServerListTDG;
 
 
 public class FrontController extends HttpServlet {
 
 	private static final long serialVersionUID = 8691536805973858130L;
 	private Logger logger;
 	
 	// map that holds all the command to be executed depending on the passed command string in the request url
 	private HashMap<String, FrontCommand> commandMap = null;
 		
 	// Overridden to make sure that we have a database.
 	public void init() throws ServletException {
 		try {
 			if (!Database.isDatabaseCreated())
 				Database.createDatabase();
 			InetAddress addr = InetAddress.getLocalHost();
			String hostname = addr.getHostName();		
			ResultSet rs = ServerListFinder.find(hostname);		
			if(!rs.next()) {
				int port = 8080;
				ServerListTDG.insert(hostname, port);
			}						
 		} catch (Exception E) {
 			// TODO why was this commented out?
 			throw new ServletException(E);
 		} finally {
 			try {
 				Database.freeConnection();
 			} catch (SQLException e) {
 				logger = (Logger)LoggerFactory.getLogger("application");
 				logger.error("SQLException occurred when trying to free a database connection. {}", e);
 			}
 		}
 	}
 
 	/**
 	 * Constructor
 	 * Initialises the commands in the command map. It will be used to determine the right command to execute depending on the command parameter in the query string
 	 * @throws SQLException 
 	 */
 	public FrontController() throws SQLException {
 		
 		commandMap = new HashMap<String, FrontCommand>();
 		
 		// Command for creating a new message from an uploaded audio file
 		commandMap.put("POST.createmessage", new CreateMessageCommand());
 		
 		// Command for deleting a message
 		commandMap.put("DELETE.deletemessage", new DeleteMessageCommand());
 		
 		// Command for downloading messages
 		commandMap.put("GET.readmessage", new ReadMessageCommand());
 		
 		// Command for upvoting a message
 		commandMap.put("POST.upvote", new UpvoteMessageCommand());
 		
 		// Command for downvoting a message
 		commandMap.put("POST.downvote", new DownvoteMessageCommand());
 
 		// Command for getting message ids 
 		// This should be called before readmessage
 		commandMap.put("GET.getmessageids", new GetMessageIDsCommand());
 		
 		// Command for displaying server parameters
 		commandMap.put("GET.getserverparameters", new GetServerParametersCommand());
 		
 		// Command for updating server parameters
 		commandMap.put("POST.updateserverparameters", new UpdateServerParametersCommand());
 		
 		// Command for creating a new user
 		commandMap.put("POST.createuser", new CreateUserCommand());
 		
 		// Command for getting a user
 		commandMap.put("GET.getuser", new ReadUserCommand());
 		
 		// Command for updating a user
 		commandMap.put("POST.updateuser", new UpdateUserCommand());
 		
 		// Command for deleting a user
 		commandMap.put("DELETE.deleteuser", new DeleteUserCommand());
 		
 		ServerParameters params = ServerParameters.getUniqueInstance();
 	
 		// Frees the connection from the connection pool
 		Database.freeConnection();
 		
 		// Initialize the logger
 		logger = (Logger)LoggerFactory.getLogger("application");
 		logger.trace("Starting Application Server. FrontController started.");
 	}
 	
 	/**
 	 * This method returns the command that is mapped for the specified command string.
 	 * @param commandString
 	 * @return Returns the command mapped to the passed command string or an unknow command if there is no mapping
 	 */
 	private FrontCommand getCommand(String httpMethod, String commandString) {
 		FrontCommand command = commandMap.get(httpMethod + "." + commandString);
 		if (command == null)
 			command = new UnsupportedCommand(httpMethod, commandString);
 		return command;
 	}
 
 	
 	/**
 	 * This method handles all requests
 	 */
 	private void handleRequest(HttpServletRequest request, HttpServletResponse response, String httpMethod) throws ServletException {
 		Connection conn = null;
 		
 		logger.trace("handleRequest() starting.");
 		
 		try {
 			conn = Database.getConnection();
 			conn.setAutoCommit(false);
 			PreparedStatement ps = conn.prepareStatement("START TRANSACTION;");
 			ps.execute();
 			ps.close();
 		} catch (SQLException e1) {
 			logger.debug("SQL exception occured when starting transaction {}", e1);
 			e1.printStackTrace();
 		}
 		
 		// Get the command string from the request object (url ex.: www.webserver.com?command=downvote&messageid=12433512343524683542)
 		String commandString = request.getParameter("command");
 
 		// Get the command from the command string
 		FrontCommand command = getCommand(httpMethod, commandString);
 		
 		try {
 			try {				
 				logger.trace("Executing command {}", command);
 				
 				// Execute the command
 				command.execute(request, response);
 			} catch (SQLException e) {
 				logger.debug("The following SQLException occured", e);
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 			} catch (IOException e) {
 				logger.debug("The following IOException occured", e);
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 			} catch (ParameterException e) {
 				logger.debug("The following ParameterException occured", e);
 				response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
 				// TODO would the below be bad requests as well
 			} catch (MapperException e) {
 				logger.debug("The following MapperException occured", e);
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 			} catch (UnrecognizedUserException e) {
 				logger.debug("The following UnrecognizedUserException occured", e);
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
 			}
 		} catch (IOException e) {
 			throw new ServletException(e);
 		}
 		
 		try {
 			conn = Database.getConnection();
 			PreparedStatement ps = conn.prepareStatement("COMMIT;");
 			ps.execute();
 			ps.close();
 		} catch (SQLException e) {
 			logger.debug("The following SQLException occured", e);				
 			e.printStackTrace();
 		} finally {
 			try {
 				Database.freeConnection();
 			} catch (SQLException e) {
 				logger.debug("The following SQLException occured", e);				
 				e.printStackTrace();
 			}
 		}
 		
 		logger.trace("handleRequest() ending.");
 	}
 	
 	/*
 	 * Methods for handling different types of HTTP method calls.
 	 * They all call handleRequest() by passing in their HTTP method name.
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 		handleRequest(request, response, "GET");
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 		handleRequest(request, response, "POST");
 	}
 	
 	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 		handleRequest(request, response, "PUT");
 	}
 	
 	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 		handleRequest(request, response, "DELETE");
 	}
 }
