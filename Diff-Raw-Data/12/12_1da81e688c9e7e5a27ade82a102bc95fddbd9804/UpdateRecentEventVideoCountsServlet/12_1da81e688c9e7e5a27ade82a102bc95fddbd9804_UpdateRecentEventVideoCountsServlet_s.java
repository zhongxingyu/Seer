 package com.bfi;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.bfi.jdo.Event;
 import com.bfi.jdo.PMF;
 import com.google.gdata.data.youtube.VideoEntry;
 
 @SuppressWarnings("serial")
 public class UpdateRecentEventVideoCountsServlet extends HttpServlet {
 	PersistenceManager pm = null;
 	Collection<Event> updateCollection = new ArrayList<Event>();
 
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		try {
			
 			pm = PMF.get().getPersistenceManager();
 			Query q = pm.newQuery(Event.class);
 			q.setOrdering("date desc");
 			@SuppressWarnings("unchecked")
			Collection<Event> events = (Collection<Event>) q.execute();
 			updateCollection = new ArrayList<Event>();
 			for(Event e: events){
 				YouTubeUtils ytu = new YouTubeUtils();
 				List<VideoEntry> cleanedList = ytu.getVideos(e);
 				e.setVideoCount(String.valueOf(cleanedList.size()));
 				updateCollection.add(e);
 				if(updateCollection.size() == 10){
 					pm.makePersistentAll(updateCollection);
 					updateCollection = new ArrayList<Event>();
 					break;
 				}
 			}
 		} finally {
 			pm.close();
 		}
 		resp.getWriter().println("Successfully updated "+String.valueOf(updateCollection.size())+" event video counts");
 	}
 }
