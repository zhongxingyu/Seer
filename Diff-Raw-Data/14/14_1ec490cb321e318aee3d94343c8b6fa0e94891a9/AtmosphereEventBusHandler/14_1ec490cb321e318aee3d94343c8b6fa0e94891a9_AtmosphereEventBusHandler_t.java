 package ch.cern.atlas.apvs.eventbus.server;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.atmosphere.gwt.server.AtmosphereGwtHandler;
 import org.atmosphere.gwt.server.GwtAtmosphereResource;
 
 /**
  * @author Mark Donszelmann
  */
 public class AtmosphereEventBusHandler extends AtmosphereGwtHandler {
 
     @Override
     public void init(ServletConfig servletConfig) throws ServletException {
         super.init(servletConfig);
         System.out.println("AtmosphereEventBusHandler started...");
         Logger.getLogger("").setLevel(Level.INFO);
         Logger.getLogger("gwtcomettest").setLevel(Level.ALL);
         Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
         logger.trace("Updated logging levels");
     }
 
     @Override
     public int doComet(GwtAtmosphereResource resource) throws ServletException, IOException {
     	System.out.println("AtmosphereEventBusHandler.doComet()...");
     	System.out.println(resource.getAtmosphereResource().getAtmosphereResourceEvent());
         resource.getBroadcaster().setID("GWT_COMET");
         HttpSession session = resource.getAtmosphereResource().getRequest().getSession(false);
         if (session != null) {
             logger.debug("Got session with id: " + session.getId());
             logger.debug("Time attribute: " + session.getAttribute("time"));
         } else {
             logger.warn("No session");
         }
         if (logger.isDebugEnabled()) {
             logger.debug("Url: " + resource.getAtmosphereResource().getRequest().getRequestURL()
                     + "?" + resource.getAtmosphereResource().getRequest().getQueryString());
         }
         return NO_TIMEOUT;
     }
    
    public void doPost(HttpServletRequest postRequest, HttpServletResponse postResponse, List<Serializable> messages, GwtAtmosphereResource resource) {
    	super.doPost(postRequest, postResponse, messages, resource);
     	System.out.println("Post...");
     	for (Iterator<Serializable> i = messages.iterator(); i.hasNext(); ) {
     		System.out.println("-- "+i.next().getClass());
     	}
     }
     @Override
     public void broadcast(List<Serializable> messages, GwtAtmosphereResource resource) {
     	super.broadcast(messages, resource);
     	System.out.println("bCast...");
     	for (Iterator<Serializable> i = messages.iterator(); i.hasNext(); ) {
     		System.out.println("-- "+i.next().getClass());
     	}
     }
     
     @Override
     public void broadcast(Serializable message, GwtAtmosphereResource resource) {
     	super.broadcast(message, resource);
     	System.out.println("bCast..."+message.getClass());
     }
        
     @Override
     public void cometTerminated(GwtAtmosphereResource cometResponse, boolean serverInitiated) {
         super.cometTerminated(cometResponse, serverInitiated);
         logger.debug("Comet disconnected");
     }
 
 }
