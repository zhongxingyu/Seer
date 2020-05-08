 package com.sample.facebook.simple;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.sample.facebook.simple.model.Friend;
 import com.sample.facebook.simple.model.NamePairScore;
 
 
 @SuppressWarnings("serial")
 public class SimpleFacebookSampleServlet extends HttpServlet {
 
 
 	private FriendManager friendManager = new FriendManager();
 	private FacebookManager facebookManager = new FacebookManager();
 
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		HttpSession session = req.getSession(true);
 
 		resp.setContentType("text/html");
 
 		String code = req.getParameter("code");
 
 		if(code ==null){
 			//redirect..
 			facebookManager.loginRedirect(resp, session);
 
 		}else{
			
 			//has code.. use it to load the list of friends
 			String incomingState = req.getParameter("state");
 			String state = (String)session.getValue("state");
 
 			resp.getWriter().println("incoming state :" + incomingState +"<br/>");
 			resp.getWriter().println("session state :" + state +"<br/>");
 
 			
 			//check for NPE ...
 			if(state.trim().equals(incomingState.trim())){
 
 				//Top list of friends
 
 				try {
 					String accessToken = (String) session.getValue("access_token");
 
 					if(accessToken == null){
 						accessToken = facebookManager.getAccessToken(code);
 						session.putValue("access_token", accessToken);
 					}
 
 					/**
 					 * TOP 10 Friends by friend count
 					 */
 					List<Friend> friends = friendManager.getTop10Friends(accessToken);
 
 					resp.getWriter().println("<p>Top 10 Friends</p>");
 					resp.getWriter().println("<ul>");
 					if(friends !=null){
 						for(Friend friend:friends){
 							resp.getWriter().println("<li>"+friend.getName()+", count: "+friend.getFriendCount()+"</li>");
 
 						}
 					}
 					resp.getWriter().println("</ul>");
 
 
 					friends = friendManager.getAllFriendLocation(accessToken);
 					
 					/**
 					 * Closest Friends
 					 */
 					List<Friend> closetFriends = friendManager.getToFriendsByDistance(friends, 1);
 					
 					resp.getWriter().println("<p>Top 10 Closest Friends, that we know location</p>");
 					resp.getWriter().println("<ul>");
 					if(closetFriends !=null){
 						for(Friend friend:closetFriends){
 							resp.getWriter().println("<li>"+friend.getName()+", count: "+friend.getDistanceFromMe()+"m </li>");
 						}
 					}
 					resp.getWriter().println("</ul>");
 
 					/**
 					 *  Farthest Friends
 					 */
 					List<Friend> farthestFriends = friendManager.getToFriendsByDistance(friends, -1);
 					
 					resp.getWriter().println("<p>Top 10 Farthest Friends, that we know location</p>");
 					resp.getWriter().println("<ul>");
 					if(farthestFriends !=null){
 						for(Friend friend:farthestFriends){
 							resp.getWriter().println("<li>"+friend.getName()+", count: "+friend.getDistanceFromMe()+"m </li>");
 						}
 					}
 					resp.getWriter().println("</ul>");
 
 					/**
 					 * Friends' character overlap count
 					 * 
 					 */
 					List<NamePairScore> overlapPairs = friendManager.commonCharacterFriends(friends);
 					
 					resp.getWriter().println("<p>Top 10 common character pairs of friends' names</p>");
 					resp.getWriter().println("<ul>");
 					if(farthestFriends !=null){
 						for(NamePairScore pair:overlapPairs){
 							resp.getWriter().println("<li>"+pair.getFirst()+" | "+pair.getSecond()+"  = "+ pair.getOverlapCount() +"</li>");
 						}
 					}
 					resp.getWriter().println("</ul>");
 					
 					
 				} catch (Exception e) {
 					resp.getWriter().println("BOOM");
 
 					//TODO: handle error
 					e.printStackTrace(resp.getWriter());
 				}
 
 			}else{
 				resp.setStatus(400);
 				resp.getWriter().println("bad state");			
 			}
 
 		}
 
 
 	}
 
 
 
 
 
 }
