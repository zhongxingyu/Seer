 package edu.nus.tp.web.fetch;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import twitter4j.internal.org.json.JSONArray;
 import twitter4j.internal.org.json.JSONException;
 import twitter4j.internal.org.json.JSONObject;
 
 /**
  * Servlet implementation class FetchTweet
  */
 @WebServlet("/FetchTweet")
 public class FetchTweet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Default constructor.
 	 */
 	public FetchTweet() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		// Fetching tweets from twitter
 
 		String searchTopic = request.getParameter("topic");
 		if (!searchTopic.equals("null") && searchTopic != null) {
 			request.getSession().setAttribute("topic",searchTopic);
 			searchTopic = searchTopic.trim().replaceAll("( )+","%20");
 			
 			URL twitterURL = new URL("http://search.twitter.com/search.json?q="
 					+ searchTopic + "&rpp=100");
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					twitterURL.openStream()));
 			String inputLine;
 			try {
 				while ((inputLine = in.readLine()) != null) {
 					JSONObject streamObj;
 					streamObj = new JSONObject(inputLine);
 					JSONArray resultArray = (JSONArray) streamObj
 							.get("results");
 					List <String> tweetList = new ArrayList<String>();
 					
 					for (int i =0; i< resultArray.length(); i++){
 						JSONObject tweetObj = (JSONObject) resultArray.get(i);
 						String tweet = tweetObj.get("text").toString();
 						tweetList.add(tweet);
 					}
 					
					String action=request.getParameter("action");
 					if (!action.equals("null") && action != null && action.equals("evaluate") ) {
 						request.getSession().setAttribute("tweetData",tweetList);
 						request.getRequestDispatcher("./Evaluation").forward(request, response);
 					}
 					else
 					{
 					
 						request.getSession().setAttribute("tweetData",tweetList);
 						request.getRequestDispatcher("./Train.jsp").forward(request, response);
 					}
 				}
 				in.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				request.getRequestDispatcher("./error.jsp").forward(request, response);
 			}catch(Exception e)
 			{
 				e.printStackTrace();
 				request.getRequestDispatcher("./error.jsp").forward(request, response);
 			}
 		}
 
 	}
 
 }
