 package org.powertac.tourney.listeners;
 
 import org.powertac.tourney.services.Rest;
 
 import javax.faces.FacesException;
 import javax.faces.event.PhaseEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PhaseListener;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 
 
 public class RestoreViewPhaseListener implements PhaseListener {
 
   // Intercepts REST calls (get requests) and passes them to the Rest service
   // for parsing and returns the proper response
  public synchronized void beforePhase (PhaseEvent pe) {
     HttpServletRequest request = (HttpServletRequest) pe.getFacesContext().
         getExternalContext().getRequest();
     Map<String, String[]> params = getParams(request);
 
     if (params.size() != 0) {
       Rest rest = new Rest();
       String url = request.getRequestURL().toString();
 
       if (request.getMethod().equals("PUT") && url.contains("serverInterface.jsp")) {
         respond(pe, rest.handleServerInterfacePUT(params, request));
       }
       else if (url.contains("brokerLogin.jsp")) {
         respond(pe, rest.parseBrokerLogin(params));
       }
       else if (url.contains("visualizerLogin.jsp")) {
         respond(pe, rest.parseVisualizerLogin(request, params));
       }
       else if (url.contains("serverInterface.jsp")) {
         respond(pe, rest.parseServerInterface(params, request.getRemoteAddr()));
       }
       else if (url.contains("properties.jsp")) {
         respond(pe, rest.parseProperties(params));
       }
       else if (url.contains("pom.jsp")) {
         respond(pe, rest.parsePom(params));
       }
     }
   }
 
   private void respond(PhaseEvent pe, String responseString) {
     if (responseString.isEmpty()) {
       return;
     }
 
     HttpServletResponse response = (HttpServletResponse) pe.getFacesContext().
         getExternalContext().getResponse();
     response.setContentType("text/plain; charset=UTF-8");
 
     try {
       PrintWriter pw = response.getWriter();
       pw.print(responseString);
     }
     catch (IOException ex) {
       throw new FacesException(ex);
     }
 
     pe.getFacesContext().responseComplete();
   }
 
   // Which jsf phase to intercept, in this case the Restore View Phase
   public PhaseId getPhaseId () {
     return PhaseId.RESTORE_VIEW;
   }
 
   public void afterPhase (PhaseEvent arg0) {
 
   }
 
   private Map<String, String[]> getParams(HttpServletRequest request) {
     @SuppressWarnings("unchecked")
     Map<String, String[]> params = request.getParameterMap();
     return params;
   }
 }
