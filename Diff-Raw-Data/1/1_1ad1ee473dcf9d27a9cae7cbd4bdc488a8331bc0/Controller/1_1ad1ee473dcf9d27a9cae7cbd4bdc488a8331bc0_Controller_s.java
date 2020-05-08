 package edu.unsw.comp9321.logic;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.tomcat.util.http.fileupload.DefaultFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.FileItem;
 
 import edu.unsw.comp9321.beans.SessionBean;
 import edu.unsw.comp9321.common.ServiceLocatorException;
 import edu.unsw.comp9321.exception.EmptyResultException;
 import edu.unsw.comp9321.jdbc.ActorDAO;
 import edu.unsw.comp9321.jdbc.ActorDTO;
 import edu.unsw.comp9321.jdbc.CastDAO;
 import edu.unsw.comp9321.jdbc.CinemaDAO;
 import edu.unsw.comp9321.jdbc.CommentDTO;
 import edu.unsw.comp9321.jdbc.DerbyDAOImpl;
 import edu.unsw.comp9321.jdbc.MovieDAO;
 import edu.unsw.comp9321.jdbc.MovieDTO;
 import edu.unsw.comp9321.jdbc.MySQLDAOImpl;
 import edu.unsw.comp9321.jdbc.CharacterDTO;
 import edu.unsw.comp9321.jdbc.UserDAO;
 import edu.unsw.comp9321.jdbc.UserDTO;
 import edu.unsw.comp9321.mail.MailSender;
 import edu.unsw.comp9321.mail.MailSenderTest;
 
 /**
  * Servlet implementation class Controller
  */
 public class Controller extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	static Logger logger = Logger.getLogger(Controller.class.getName());
 	private CastDAO cast;
 	private MovieDAO movies;
 	private ActorDAO actors;
 	private UserDAO users;
 	private UserDTO currentUser;
 	private CinemaDAO cinemas;
 	private SessionBean sessionBean;
 	
 	DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        
 	// upload stuff
 	private static final String UPLOAD_DIRECTORY = "images";
 	private static final int THRESHOLD_SIZE = 1024 * 1024 * 3;    // 3MB
 	private static final int MAX_FILE_SIZE = 1024 * 1024 * 40;    // 40MB
 	private static final int REQUEST_SIZE = 1024 * 1024 * 50;    // 50MB
 	
     /**
      * @throws ServletException 
      * @see HttpServlet#HttpServlet()
      */
     public Controller() throws ServletException {
     	// TODO Auto-generated constructor stub
         super();
         try {
         	
         	movies = new MovieDAO();
         	users = new UserDAO();
         	actors = new ActorDAO();
 			cast = new DerbyDAOImpl();
 			cinemas = new CinemaDAO();
 		} catch (ServiceLocatorException e) {
 			logger.severe("Trouble connecting to database "+e.getStackTrace());
 			throw new ServletException();
 		} catch (SQLException e) {
 			logger.severe("Trouble connecting to database "+e.getStackTrace());
 			throw new ServletException();
 		}
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request,response);
 	}
 
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String forwardPage = "";
 		sessionBean = (SessionBean) request.getSession().getAttribute("sessionBean");
 		request.setAttribute("genreList", movies.getGenres());
 		request.setAttribute("actorList", actors.getAll());
 		
 		if(request.getParameter("action").equals("nowShowing")){
 			
 			List<MovieDTO> resSet = movies.findNowShowing();
 			request.setAttribute("movieDeets",  resSet);
 			forwardPage = "nowShowing.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 			dispatcher.forward(request, response);
 		} else if(request.getParameter("action").equals("comingSoon")){
 			
 			List<MovieDTO> resSet = movies.findComingSoon();
 			request.setAttribute("movieDeets",  resSet);
 			forwardPage = "comingSoon.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 			dispatcher.forward(request, response);
 		} else if(request.getParameter("action").equals("signup")){
 			String username = request.getParameter("username");
 			String password = request.getParameter("password");
 			String confirmPassword = request.getParameter("confirmPassword");
 			String email = request.getParameter("email");
 			String confirmEmail = request.getParameter("confirmEmail");
 			
 			//Check is the data that the user has entered is valid
 			String errorMessage = "";
 			boolean error = false;
 			if(!email.equals(confirmEmail)){
 				errorMessage = "Emails don't match";
 				error = true;
 			}
 			if(!password.equals(confirmPassword)){
 				errorMessage = "Passwords don't match";
 				error = true;
 			}
 			if(username == "" && password == "" && email == ""){
 				errorMessage = "Not all values completed";
 				error = true;
 			}
 			if(users.checkUsername(username)){
 				errorMessage = "Username already in use, please choose a different username";
 				error = true;
 			}
 			
 			if(!error){
 				System.out.println("i made it");
 				//store data to database
 				// default to 1, presumably, admins added by direct insert
 				users.addUserDetails(username, 1, password, confirmPassword, email, confirmEmail); 
 				//send email
 				//TODO: what does 'confirming' actually do? nothing?
 				MailSenderTest ms = new MailSenderTest();
 				ms.sendConfirmationEmail(username, email);
 				//return to check email page
 				forwardPage = "confirmSignup.jsp";
 				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 				dispatcher.forward(request, response);
 			} else {
 				//return to check email page
 				request.setAttribute("message",  errorMessage);
 				forwardPage = "signupError.jsp";
 				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 				dispatcher.forward(request, response);
 			}
 		} else if(request.getParameter("action").equals("confirmSignup")){
 			
 			UserDTO user = users.getUserDetails(request.getParameter("username"));
 			ArrayList<ActorDTO> actorList = actors.getAll();
 			request.setAttribute("user",  user);
 			request.setAttribute("actorList",  actorList);
 			forwardPage = "editProfile.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 			dispatcher.forward(request, response);
 		} else if(request.getParameter("action").equals("editProfile")){
 			
 			List<String> errorMessages = new ArrayList<String>();
 			//Get data from form
 			String username = sessionBean.getUser().getUsername();
 			String password;
 			if (request.getParameter("password") != null) {
 				if (!request.getParameter("password").equals(request.getParameter("confirmPassword"))) {
 					errorMessages.add("Passwords dont match.");
 					password = sessionBean.getUser().getPassword();
 				} else {
 					password = request.getParameter("password");
 				}
 			} else {
 				password = sessionBean.getUser().getPassword();
 			}
 			String firstName;
 			if (request.getParameter("firstName") != null) {
 				firstName = request.getParameter("firstName");
 			} else {
 				if (sessionBean.getUser().getFirstName() != null) {
 					firstName = sessionBean.getUser().getFirstName();
 				} else {
 					firstName = null;
 				}
 			}
 			
 			String lastName;
 			if ( request.getParameter("lastName") != null) {
 				lastName = request.getParameter("lastName");
 			} else {
 				if (sessionBean.getUser().getLastName() != null) {
 					lastName = sessionBean.getUser().getLastName();
 				} else {
 					lastName = null;
 				}
 			}
 
 			String email;
 			if ( request.getParameter("email") != null) {
 				email = request.getParameter("email");
 			} else {
 				if (sessionBean.getUser().getEmailAddress() != null) {
 					email = sessionBean.getUser().getEmailAddress();
 				} else {
 					email = null;
 				}
 			}
 			
 			String nickName;
 			if ( request.getParameter("nickName") != null) {
 				nickName = request.getParameter("nickName");
 			} else {
 				if (sessionBean.getUser().getNickName() != null) {
 					nickName = sessionBean.getUser().getNickName();
 				} else {
 					nickName = null;
 				}
 			}
 			
 			int yearOfBirth;
 			if ( Integer.parseInt(request.getParameter("yearOfBirth")) != 0) {
 				yearOfBirth = Integer.parseInt(request.getParameter("yearOfBirth"));
 			} else {
 				if (sessionBean.getUser().getYearOfBirth() != 0) {
 					yearOfBirth = sessionBean.getUser().getYearOfBirth();
 				} else {
 					yearOfBirth = 0;
 				}
 			}
 
 			//send the data to the database
 			users.updateUserDetails(username, password, email, firstName, lastName, nickName, yearOfBirth);
 			//send user to confirmation page
 			forwardPage = "editProfileConfirm.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 			dispatcher.forward(request, response);
 		} else if(request.getParameter("action").equals("viewProfile")){
 			//Get data from form
 			//send the data to the database
 			//send user to confirmation page
 			forwardPage = "myProfile.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 			dispatcher.forward(request, response);
 		} else if (request.getParameter("action").equals("addCinema")){
 			String name = request.getParameter("name");
 			String location = request.getParameter("location");
 			int capacity = Integer.parseInt(request.getParameter("capacity"));
 			String[] amenities = request.getParameterValues("amenities");
 			
 			cinemas.addCinema(name, location, capacity, amenities);
 			
 			request.setAttribute("adminResponse", new String("Cinema Added!"));
 			forwardPage = "admin.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 			dispatcher.forward(request, response);
 		} else if (request.getParameter("action").equals("addMovie")){
 			
 			String title = request.getParameter("title");
 			String actors = request.getParameter("actors");
 			String genres = request.getParameter("genres");
 			String director = request.getParameter("director");
 			String synopsis = request.getParameter("synopsis");
 			String ageRating = request.getParameter("agerating");
 			Date releaseDate = null;
 			try {
 				releaseDate = fmt.parse(request.getParameter("releasedate"));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			
 			
 //			file upload stuff:
 //			http://stackoverflow.com/questions/2422468/how-to-upload-files-to-server-using-jsp-servlet
 			
 			request.setAttribute("adminResponse", new String("Movie Added!"));
 			forwardPage = "admin.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 			dispatcher.forward(request, response);
 		} else if (request.getParameter("action").equals("login")) {
 			UserDTO attempedLogin = users.getUserDetails(request.getParameter("username"));
 			if (!request.getParameter("password").equals(attempedLogin.getPassword())) {
 				request.setAttribute("failedLogin", new String("Incorrect Password! Please try again"));
 				forwardPage = request.getParameter("source");
 				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 				dispatcher.forward(request, response);
 			} else {
 				sessionBean.setUserType(attempedLogin.getUserType());
 				sessionBean.setUser(attempedLogin);
 				request.getSession().setAttribute("sessionBean", sessionBean);
 				forwardPage = request.getParameter("source");
 				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 				dispatcher.forward(request, response);
 			}
 		} else if (request.getParameter("action").equals("logout")) {
 			forwardPage = "index.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
 			request.getSession().invalidate();
 			dispatcher.forward(request, response);
 		} else if (request.getParameter("action").equals("search")) {
 			
 			if (request.getParameter("state") != null) {
 				
 				// if needed : .equals("results")
 				// collect input
 				String yearRange = request.getParameter("yearrange");
 				String title = request.getParameter("title");
 				String actor = request.getParameter("actor");
 				String genre = request.getParameter("genre");
 				//build results object
 				List<MovieDTO> results = movies.searchOn(yearRange, title, actor, genre);
 				//send results as attribute
 				request.setAttribute("resultData", results);
 			}
 			// otherwise just serve the page as per normal
 			
 			
 			forwardPage = "advancedSearch.jsp";
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
			request.getSession().invalidate();
 			dispatcher.forward(request, response);
 		} 
 	}
 	
 	private String handlePostcomment(HttpServletRequest request, HttpServletResponse response){
 		String forwardPage = "";
 		String character = (String) request.getParameter("character");
 		logger.info("Comment on character: "+character);
 		try{
 			CharacterDTO mchar = cast.findChar(character);
 			String commentString = request.getParameter("comments");
 			CommentDTO comment = new CommentDTO(mchar.getId(), mchar.getName(), "SKV", new Date(), commentString);
 			cast.storeComment(comment);
 			request.setAttribute("comments", cast.getComments(character));
 			forwardPage = "success.jsp";
 		}catch(Exception e){
 			e.printStackTrace();
 			forwardPage = "error.jsp";
 		}
 		return forwardPage;
 	}
 
 }
