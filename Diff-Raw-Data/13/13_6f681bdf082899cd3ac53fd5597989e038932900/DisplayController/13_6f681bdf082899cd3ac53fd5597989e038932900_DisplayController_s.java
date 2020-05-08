 package com.blogspot.symfonyworld.lyricsbase;
 
 import java.util.HashMap;
 import org.springframework.web.servlet.mvc.Controller;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.catalina.connector.Request;
 
 /**
  *
  * @author Tomasz Ducin <tomasz.ducin@gmail.com>
  */
 public class DisplayController implements Controller {
 
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
         HashMap model = new HashMap();
         model.put("jukebox", facade);
         model.put("song", song);
         return new ModelAndView("jsp/display.jsp", model);
     }
 }
