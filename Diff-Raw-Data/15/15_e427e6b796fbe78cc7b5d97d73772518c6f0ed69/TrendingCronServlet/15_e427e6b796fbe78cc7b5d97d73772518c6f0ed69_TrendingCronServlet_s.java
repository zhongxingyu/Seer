 package com.google.cloud.demo;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreNeedIndexException;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.cloud.demo.model.Album;
 import com.google.cloud.demo.model.AlbumManager;
 import com.google.cloud.demo.model.LeaderboardEntry;
 import com.google.cloud.demo.model.LeaderboardManager;
 import com.google.cloud.demo.model.View;
 import com.google.cloud.demo.model.ViewManager;
 
 
 public class TrendingCronServlet extends HttpServlet {
   /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static final Logger logger =
 		      Logger.getLogger(PhotoServiceManager.class.getCanonicalName());
 
 @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp){
 	AppContext appContext = AppContext.getAppContext();
 	ConfigManager configManager = appContext.getConfigManager();
     AlbumManager albumManager = appContext.getAlbumManager();
     ViewManager viewManager = appContext.getViewManager();
     LeaderboardManager leaderboardManager = AppContext.getAppContext().getLeaderboardManager();
 
     LeaderboardEntry entryA = leaderboardManager.getLeaderboardEntry("EntryA");
     LeaderboardEntry entryB = leaderboardManager.getLeaderboardEntry("EntryB");
     LeaderboardEntry entryC = leaderboardManager.getLeaderboardEntry("EntryC");
    long entryA_albumId = entryA.getAlbumId();
    long entryB_albumId = entryB.getAlbumId();
    long entryC_albumId = entryC.getAlbumId();
     long entryA_views = entryA.getViewsNumber();
     long entryB_views = entryB.getViewsNumber();
     long entryC_views = entryC.getViewsNumber();
     String entryA_userId = entryA.getUserId();
     String entryB_userId = entryB.getUserId();
     String entryC_userId = entryC.getUserId();
     String Atitle=null;
     String Btitle=null;
     String Ctitle=null;
     
     boolean any_changes = false;
     
     Iterable<Album> albumIter = albumManager.getActiveAlbums();
   	try {
     	for (Album album : albumIter) {
    		if(album.getId().toString().compareTo(String.valueOf(entryA_albumId)) == 0 ) 
     			Atitle=album.getTitle();
    		if(album.getId().toString().compareTo(String.valueOf(entryB_albumId)) == 0 ) 
     			Btitle=album.getTitle();
    		if(album.getId().toString().compareTo(String.valueOf(entryC_albumId)) == 0 ) 
     			Ctitle=album.getTitle();
     		
      		Iterable<View> albumViews = viewManager.getAlbumViews(album.getId().toString());
     		long viewCount = 0;
     		for (View view : albumViews) {
     			viewCount++;
     		}
     		album.setViews(viewCount);
     		if(Long.valueOf(viewCount).compareTo(Long.valueOf(entryC_views)) > 0 ) {
         		if(Long.valueOf(viewCount).compareTo(Long.valueOf(entryB_views)) > 0 ) {
             		if(Long.valueOf(viewCount).compareTo(Long.valueOf(entryA_views)) > 0 ) {           		    
             		    entryC_albumId = entryB_albumId;
             		    entryB_albumId = entryA_albumId;
             		    entryA_albumId = album.getId();            		    
             		    entryC_views = entryB_views;
             		    entryB_views = entryA_views;
             		    entryA_views = viewCount; 
             		    entryC_userId = entryB_userId;
             		    entryB_userId = entryA_userId;
             		    entryA_userId =	album.getOwnerId();
             		    Ctitle=Btitle; Btitle=Atitle; Atitle=album.getTitle();
             		}
             		else {
             		    entryC_albumId = entryB_albumId;
                		    entryC_views = entryB_views;
                		    entryC_userId = entryB_userId;
                		    entryB_albumId = album.getId();
                		    entryB_views = viewCount;
             		    entryB_userId =	album.getOwnerId();
             		    Ctitle=Btitle; Btitle=album.getTitle();
            		}
         		}
         		else {
            		    entryC_albumId = album.getId();
            		    entryC_views = viewCount;        			
         		    entryC_userId =	album.getOwnerId();
         		    Ctitle=album.getTitle();
         		}
         		any_changes = true;
     		} 
         }
     	if(any_changes) {
 			leaderboardManager.deleteEntity(entryA);
 			entryA = leaderboardManager.newLeaderboardEntry("EntryA");
 			entryA.setAlbumId(entryA_albumId);
 			entryA.setViewsNumber(entryA_views);
 			entryA.setUserId(entryA_userId);
 			leaderboardManager.upsertEntity(entryA);
 			
 			leaderboardManager.deleteEntity(entryB);
 			entryB = leaderboardManager.newLeaderboardEntry("EntryB");
 			entryB.setAlbumId(entryB_albumId);
 			entryB.setViewsNumber(entryB_views);
 			entryB.setUserId(entryB_userId);
 			leaderboardManager.upsertEntity(entryB);
 			
 			leaderboardManager.deleteEntity(entryC);
 			entryC = leaderboardManager.newLeaderboardEntry("EntryC");
 			entryC.setAlbumId(entryC_albumId);
 			entryC.setViewsNumber(entryC_views);
 			entryC.setUserId(entryC_userId);
 			leaderboardManager.upsertEntity(entryC);           			
     	}
   	} catch (DatastoreNeedIndexException e) {
         try {
 			resp.sendRedirect(configManager.getErrorPageUrl(
 			  configManager.ERROR_CODE_DATASTORE_INDEX_NOT_READY));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
   	}
   	
   	// send email as report if it is required and enough time past since last report
   	
   	String want_report = null;
 	try {
 		want_report = appContext.getReportInterval();
 	} catch (EntityNotFoundException e1) {
 		// TODO Auto-generated catch block
 		e1.printStackTrace();
 	}
   	long now = System.currentTimeMillis();
   	long last_report = now;
 	try {
 		last_report = appContext.getPreviousReportTime();
 	} catch (EntityNotFoundException e1) {
 		// TODO Auto-generated catch block
 		//e1.printStackTrace();
 		last_report = now;
 		try {
 			appContext.setPreviousReportTime(last_report);
 		} catch (EntityNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
   	long delay = now - last_report;
   	if (want_report==null || want_report.equals("no_reports")) return;
   	
   	if (want_report.equals("five_min") || 
   	  ((want_report.equals("one_hour"))  && (delay > 60L * 60L * 1000L )) ||
   	  ((want_report.equals("every_day")) && (delay > 24L * 60L * 60L * 1000L)))
   	  { // send email report
   		  
   		Properties props = new Properties();
         Session session = Session.getDefaultInstance(props, null);
 
         String         msgBody = "nothing to report";
         if (Atitle !=null) msgBody=  Atitle.concat(" = ").concat(String.valueOf(entryA_views)).concat("\n");
         if (Btitle !=null) msgBody = msgBody.concat(Btitle).concat(" = ").concat(String.valueOf(entryB_views)).concat("\n");
         if (Ctitle !=null) msgBody = msgBody.concat(Ctitle).concat(" = ").concat(String.valueOf(entryC_views)).concat("\n");
 
         try {
             Message msg = new MimeMessage(session);
             try {
 				msg.setFrom(new InternetAddress("monica.farkash@gmail.com","Connexus"));
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             try {
 				msg.addRecipient(Message.RecipientType.TO,
 				                 new InternetAddress("kamram.ks+aptmini@gmail.com","TA"));
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             msg.setSubject("Connexus report");
             msg.setText(msgBody);
             Transport.send(msg);
             logger.info(" Sent trending report: " + msgBody);
             
             
         } catch (AddressException e) {
             // ...
         } catch (MessagingException e) {
             // ...
         }
         //set the time when the report was sent
   		try {
 			appContext.setPreviousReportTime(now);
 		} catch (EntityNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
   	  }
   }
 }
