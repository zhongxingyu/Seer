 package gx.realtime;
 
 import com.google.api.client.auth.oauth2.Credential;
 import gx.browserchannel.BrowserChannel;
 import gx.browserchannel.util.ConnectionFactory;
 import gx.browserchannel.util.URLWithQuery;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class Document extends EventTarget {
 	
 	//interfaces
 	public interface SuccessFunction{
 		public void execute(String json);
 	}
 	public interface FailureFunction{
 		public void execute(BaseModelEvent e);
 	}
 	
 	//attributes
 	private Model model;
 	private BrowserChannel channel;
 	private List<Collaborator> collaborators;
 	private Map<EventType, Set<EventHandler>> eventHandlers;
 
     private Credential credential;
     private Session session;
     private boolean isClosed = false;
     private RealtimeMessageHandler messageHandler;
 	
 	//functions
 	
 	protected Document(Credential credential, Session session) {
         this.credential = credential;
         this.session = session;
         this.messageHandler = new RealtimeMessageHandler(this);
         this.collaborators = new ArrayList<Collaborator>();
         this.eventHandlers = new HashMap<EventType, Set<EventHandler>>();
        this.model = new Model(this);
 
         // Set up browser channel
 		this.channel = new BrowserChannel(session.getRevision());
         this.channel.addMessageHandler(messageHandler);
         this.channel.addExtraParameter("id", session.getModelId());
         this.channel.addExtraParameter("access_token", credential.getAccessToken());
         this.channel.addExtraParameter("sid", session.getSessionId());
         
         addPrivateEventHandlers();
         this.channel.connect(RealtimeLoader.getChannelUrl());
     }
     
     private void addPrivateEventHandlers() {
         addEventListener(EventType.COLLABORATOR_JOINED, (CollaboratorJoinedEvent e) -> {
             collaborators.add(e.getCollaborator());
         });
         
         addEventListener(EventType.COLLABORATOR_LEFT, (CollaboratorLeftEvent e) -> {
             collaborators.remove(e.getCollaborator());
         });
 
         addEventListener(EventType.DOCUMENT_SAVE_STATE_CHANGED, (DocumentSaveStateChangedEvent e) -> {
             //...
         });
     }
     
     /**
      * Closes the document and disconnects from the server. After this function is called, event listeners will no longer fire and attempts to access the document, model, or model objects will throw a {@link gx.realtime.DocumentClosedError}. Calling this function after the document has been closed will have no effect.
      */
     public void close() {
         if(isClosed)
             return;
 
         isClosed = true;
     	eventHandlers = new HashMap<>();
         channel.prepareClose();
 
         // Set up parameters
         Map<String, String> parameters = new HashMap<>();
         parameters.put("id", session.getModelId());
         parameters.put("access_token", credential.getAccessToken());
         parameters.put("sid", session.getSessionId());
 
         try {
             // Create connection
             URLWithQuery urlq = new URLWithQuery(new URL(RealtimeLoader.getChannelUrl() + "/leave"), parameters);
             ConnectionFactory.createJsonReader(urlq).close();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         // This is already done by the request above
         // channel.disconnect();
 
         // This is already done by the request above
         // channel.disconnect();
     }
 
     /**
      * Exports the document to a JSON format.
      * @param successFn - A function that the exported JSON will be passed to when it is available.
      * @param failureFn - A function that will be called if the export fails.
      */
     public void exportDocument(SuccessFunction successFn, FailureFunction failureFn) {
         // TODO: implement
     }
 
     /**
      * Gets an array of collaborators active in this session. Each collaborator is a jsMap with these fields: sessionId, userId, displayName, color, isMe, isAnonymous.
      * @return
      */
     public List<Collaborator> getCollaborators() {
         return collaborators;
     }
 
     /**
      * Set the model of this document. This method should not be used directly,
      * the model is set automatically during the document load process.
      * @param model
      */
     protected void setModel(Model model) {
         this.model = model;
     }
     
     /**
      * Gets the collaborative model associated with this document.
      * @return
      */
     public Model getModel() {
         return model;
     }
 
     public <T extends Event> void addEventListener(EventType type, EventHandler<T> handler) {
     	Set<EventHandler> handlers = eventHandlers.get(type);
     	if(handlers == null){
     		handlers = new HashSet<EventHandler>();
     		eventHandlers.put(type, handlers);
     	}
     	handlers.add(handler);
     }
 
     public <T extends Event> void removeEventListener(EventType type, EventHandler<T> handler) {
     	Set<EventHandler> handlers = eventHandlers.get(type);
     	if(handlers != null){
     		handlers.remove(handler);
     	}
     }
 
     protected void fireEvent(EventType type, Event event) {
         // Delegate model events to the model
         if (event instanceof BaseModelEvent) {
             //TODO: add some callback?
             getModel().fireEvent((BaseModelEvent)event, null);
         } else {
             Set<EventHandler> handlers = eventHandlers.get(type);
             for (EventHandler handler : handlers){
                 handler.handleEvent(event);
             }
         }
     }
 
     public Session getSession() {
         return session;
     }
 
     public void setSession(Session session) {
         this.session = session;
     }
 }
