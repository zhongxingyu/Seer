 package ca.wasabistudio.chat.rs;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.servlet.ServletConfig;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 
 import org.springframework.transaction.annotation.Transactional;
 
 import ca.wasabistudio.chat.connector.Connector;
 import ca.wasabistudio.chat.entity.Client;
 import ca.wasabistudio.chat.support.AlreadyJoinedException;
 import ca.wasabistudio.chat.support.RequestErrorException;
 import ca.wasabistudio.chat.support.Session;
 import ca.wasabistudio.chat.support.SessionException;
 
 @Path("/client")
 public class ClientResource {
 
    @Context
    private ServletConfig config;

    @Context
    private HttpServletRequest request;

     private EntityManager em;
     private Connector connector;
     private Session session;
 
     @PersistenceContext
     public void setEntityManager(EntityManager em) {
         this.em = em;
     }
 
     public void setConnector(Connector connector) {
         this.connector = connector;
     }
 
     public void setSession(Session session) {
         this.session = session;
     }
 
     @GET
     @Path("/session")
     @Produces("application/json")
     public String getSession(@Context HttpServletRequest request) {
         return request.getSession().getId();
     }
 
     @POST
     @Path("/enter/{sessionId}")
     @Produces("text/plain")
     @Transactional
    public String enter(@PathParam("sessionId") String sessionId) {
         if ((sessionId == null) || "".equals(sessionId)) {
             String message = "Session id cannot be empty.";
             throw new RequestErrorException(message);
         }
 
         String username = sessionId;
        if (isProductionMode()) {
            if (!connector.validateSession(sessionId, getIp())) {
                 throw new SessionException("Unable to enter.");
             }
 
             username = connector.getUsername(sessionId);
         }
 
         if (em.find(Client.class, username) != null) {
             throw new AlreadyJoinedException();
         }
 
         Client client = new Client(username);
         em.persist(client);
         session.setClient(client);
         return username;
     }
 
    private String getIp() {
         return request.getRemoteAddr();
     }
 
    private boolean isProductionMode() {
         if (config == null) {
             return false;
         }
         String mode = config.getServletContext().getInitParameter("mode");
         return "PRODUCTION".equals(mode);
     }
 
 }
