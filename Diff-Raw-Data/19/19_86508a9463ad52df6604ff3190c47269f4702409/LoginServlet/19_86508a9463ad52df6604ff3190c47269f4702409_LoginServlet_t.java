 package com.soup.login;
 
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
 import com.j256.ormlite.logger.Logger;
 import com.j256.ormlite.logger.LoggerFactory;
 import com.soup.util.BCrypt;
 import com.soup.util.DatabaseHelper;
 
 
 /**
  * Servlet implementation class LoginServlet
  */
 @WebServlet("/login")
 public class LoginServlet extends HttpServlet
 {
 	private static final long serialVersionUID = 1L;
 	
 	private static final Pattern usernamePattern = Pattern.compile("^[a-z0-9_\\-]{3,15}$");
 	private static final Pattern passwdPattern = Pattern.compile("^[a-z0-9_\\-!@$*&%#]{6,16}$");
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public LoginServlet()
 	{
 		super();
 		DatabaseHelper.initDBConnection();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException
 	{
 		handleRequest(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException
 	{
 		handleRequest(request, response);
 	}
 	
 	@Override
 	public void destroy()
 	{
 		DatabaseHelper.killDBConnection();
 		super.destroy();
 	}
 
 	private void handleRequest(HttpServletRequest request, HttpServletResponse response)
 	{
 		HttpSession session = request.getSession();
 		
 		String reqType = request.getParameter("REQ_TYPE");
 		String username = request.getParameter("U");
 		String pw = request.getParameter("P");
 		String email = request.getParameter("EMAIL");
 		
 		if (LoginRequestType.LOGOUT.getName().equals(reqType))
 		{
 			System.out.println("loggin out");
 			session.removeAttribute("user");
 			session.invalidate();
 			response.addCookie(new Cookie("user", null));
 			try
 			{
 				response.sendRedirect("/soup");
 			} catch (IOException e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		if(StringUtils.isBlank(username) || !isValidUsername(username) || StringUtils.isBlank(pw) || !isValidPassword(pw))
 		{
 			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
 		}
 		
 		JdbcPooledConnectionSource cs = DatabaseHelper.getDBConnection();
 		if (LoginRequestType.LOGIN.getName().equals(reqType))
 		{
 			// do login action
 			User user = UserHelper.getUserFromDBByValue(cs, User.USERNAME_COLUMN_NAME, username);
 			try
 			{
 				if(UserHelper.verifyUser(user, cs, username, pw))
 				{
 					session.setAttribute("User", user);
 					response.addCookie(new Cookie("user", user.getUser_name()));
 				}
 				else
 				{
 					// user information incorrect
 					response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
 				}
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		else if (LoginRequestType.REGISTER.getName().equals(reqType))
 		{
 			// do register action
 			try
 			{
 				User userFromDB = UserHelper.getUserFromDBByValue(cs, User.USERNAME_COLUMN_NAME, username);
 				
 				if(userFromDB != null)
 				{
 //					user name already exists
 					response.setStatus(HttpServletResponse.SC_CONFLICT);
 				}
 				else
 				{
 					//create new user
 					Dao<User, Integer> userDao = DaoManager.createDao(cs, User.class);
 					User user = new User();
 					user.setUser_name(username);
 					user.setPassword(BCrypt.hashpw(pw, BCrypt.gensalt()));
 					user.setEmail_address(email);
 					userDao.create(user);
 					System.out.println("new user created");
 					session.setAttribute("User", user);
 				}
 			} 
 			catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			response.setIntHeader("foo", 400);
 		}
 	}
 	
 	private boolean isValidUsername(String username)
 	{
 		if (usernamePattern.matcher(username).matches())
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	private boolean isValidPassword(String passwd)
 	{
 		if (passwdPattern.matcher(passwd).matches())
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 }
