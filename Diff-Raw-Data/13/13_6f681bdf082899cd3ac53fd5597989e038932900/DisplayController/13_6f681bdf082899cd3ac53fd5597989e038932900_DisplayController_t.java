 package com.blogspot.symfonyworld.lyricsbase;
 
 import java.util.HashMap;
 import org.springframework.web.servlet.mvc.Controller;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.catalina.connector.Request;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  *
  * @author Tomasz Ducin <tomasz.ducin@gmail.com>
  */
 public class DisplayController implements Controller {
 
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
    
     private Jukebox facade;
     
     public Jukebox getFacade() {
         return facade;
     }
     
     public void setFacade(Jukebox jukebox) {
         facade = jukebox;
     }
     
     @Override
     public ModelAndView handleRequest(
             HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         Song song = facade.getSong(request.getParameter("title"));
        if (song == null)
            throw new ResourceNotFoundException("Song \"" + request.getParameter("title") + "\"not found.");
         HashMap model = new HashMap();
         model.put("jukebox", facade);
         model.put("song", song);
         return new ModelAndView("jsp/display.jsp", model);
     }
 }
