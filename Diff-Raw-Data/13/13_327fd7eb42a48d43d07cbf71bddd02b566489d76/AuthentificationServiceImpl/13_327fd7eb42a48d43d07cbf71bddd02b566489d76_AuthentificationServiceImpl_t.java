 package org.daum.javase.webportal.server;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import org.daum.common.genmodel.impl.AgentImpl;
 import org.daum.javase.webportal.dto.AdapteurAgent;
 import org.daum.javase.webportal.shared.Agent;
 import org.daum.javase.webportal.client.AuthentificationService;
 import org.daum.library.ormH.persistence.PersistenceSession;
 import org.daum.library.ormH.persistence.PersistenceSessionFactoryImpl;
 import org.daum.library.ormH.utils.PersistenceException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pdespagn
  * Date: 7/13/12
  * Time: 4:10 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class AuthentificationServiceImpl extends RemoteServiceServlet implements AuthentificationService {
     private PersistenceSessionFactoryImpl factory;
     private Logger logger = LoggerFactory.getLogger(this.getClass());
 
 
     public AuthentificationServiceImpl(PersistenceSessionFactoryImpl factory){
         this.factory = factory;
     }
 
     @Override
     public Agent createAgent(String nom, String prenom, String matricule, String password) {
         Agent agent = new Agent(nom,prenom,matricule,password);
         AdapteurAgent adapteurAgent = new AdapteurAgent(factory);
         agent = adapteurAgent.saveAgent(agent);
         return agent;
     }
 
     @Override
    public boolean authenticateAgent(String matricule, String password)
    {
             List<Agent> listeAgents = getAllAgent();
             for (Agent a : listeAgents) {
                 if (a.getMatricule().equals(matricule) && a.getPassword().equals(password)) {
                     return true;
                 }
             }
         return false;
     }
 
     private Agent getAgentAlreadyFromSession() {
         Agent agent = null;
         HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
         HttpSession session = httpServletRequest.getSession();
         Object userObj = session.getAttribute("agent");
         if (userObj != null && userObj instanceof Agent) {
             agent = (Agent) userObj;
         }
         return agent;
     }
 
 
 
 
     private void storeAgentInSession(Agent agent) {
         HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
         HttpSession session = httpServletRequest.getSession();
         session.setAttribute("agent", agent);
     }
 
     @Override
     public void logout() {
         deleteAgentFromSession();
     }
 
     private void deleteAgentFromSession() {
         HttpServletRequest httpServletRequest = this.getThreadLocalRequest();
         HttpSession session = httpServletRequest.getSession();
         session.removeAttribute("agent");
     }
 
     @Override
     public Agent loginFromSessionServer() {
         Agent agent = getAgentAlreadyFromSession();
         return agent;
     }
 
     @Override
     public List<Agent> getAllAgent() {
         AdapteurAgent adapteurAgent = new AdapteurAgent(factory);
         return adapteurAgent.getAllAgentFromSitac();
     }
 
 
 }
