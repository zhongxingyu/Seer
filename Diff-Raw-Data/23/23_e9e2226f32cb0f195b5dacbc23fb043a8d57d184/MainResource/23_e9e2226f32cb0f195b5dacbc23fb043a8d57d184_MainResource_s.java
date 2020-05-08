 package org.bloodtorrent.resources;
 
 import com.yammer.dropwizard.hibernate.UnitOfWork;
 import lombok.Setter;
 import org.bloodtorrent.dto.CatchPhrase;
 import org.bloodtorrent.dto.SuccessStory;
 import org.bloodtorrent.dto.User;
 import org.bloodtorrent.view.MainView;
 import org.eclipse.jetty.server.SessionManager;
 
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.CookieParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 20
  * Time: 오후 12:06
  * To change this template use File | Settings | File Templates.
  */
 @Path("/")
 @Produces(MediaType.TEXT_HTML)
 public class MainResource {
 
     private final SessionManager sessionManager;
 
     @Setter
     private SuccessStoryResource successStoryResource;
 
     @Setter
     private CatchPhraseResource catchPhraseResource;
 
     private User user;
 
     public MainResource(SessionManager httpSessionManager) {
         sessionManager = httpSessionManager;
     }
 
     @GET
     @UnitOfWork
     public MainView forwardMainPage(@CookieParam("JSESSIONID") String sessionID) {
         HttpSession session = getSession(sessionID);
         List<SuccessStory> successStories = getSuccessStories();
         CatchPhrase catchPhrase = getCatchPhrase();
         if(session != null && session.getAttribute("user") !=  null){
             user = (User) session.getAttribute("user");
            if(successStories.isEmpty())
                 return new MainView(user, catchPhrase);
            else
                 return new MainView(user, successStories);
         }
 
        if(successStories.isEmpty())
             return new MainView(catchPhrase);
        else
             return new MainView(successStories);
     }
 
     private CatchPhrase getCatchPhrase() {
         return catchPhraseResource.get();
     }
 
     private List<SuccessStory> getSuccessStories() {
         return successStoryResource.getSuccessStoriesBriefly();
     }
 
     public HttpSession getSession(String sessionID) {
         try{
             return sessionManager.getHttpSession(sessionID);
         }catch(Exception e){
             return null;
         }
     }
 }
