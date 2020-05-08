 /*
  * Author: Ignacio Rodriguez
  * This is based largely off the sample code found at https://github.com/yusuke/sign-in-with-twitter
  */
 
 package Controller;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 import Beans.User;
 
 public class TwitterCallbackController extends HttpServlet {
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
         Twitter twitter = (Twitter)request.getSession().getAttribute("twitter");
 		RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
 		HttpSession session = request.getSession();
 		String verifier = request.getParameter("oauth_verifier");
 		try{
 			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
 			setupUser(accessToken, session);
 			session.removeAttribute("requestToken");
 		}catch (TwitterException e) {
             throw new ServletException(e);
         }
        response.sendRedirect(request.getContextPath() + "/usr_home.html");
 	}
 
 	private void setupUser(AccessToken accessToken, HttpSession session) {
 		String userName = accessToken.getScreenName();
 		try {
 			User user = new User();
 			int userid = user.addUser(userName);
 			session.setAttribute("userid", userid);
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 }
