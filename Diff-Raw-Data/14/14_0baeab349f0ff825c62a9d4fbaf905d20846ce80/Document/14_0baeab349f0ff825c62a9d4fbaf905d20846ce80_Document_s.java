 package gx.realtime;
 
 import gx.browserchannel.BrowserChannel;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
public class Document extends EventTarget{
 	
 	//interfaces
 	public interface SuccessFunction{
 		public void execute(String json);
 	}
 	public interface FailureFunction{
 		public void execute(BaseModelEvent e);
 	}
 	public interface EventHandler{
 		public void execute(BaseModelEvent e);
 	}
 	
 	//attributes
 	private Model model;
 	private BrowserChannel channel;
 	private List<Collaborator> collaborators;
 	private Map<EventType, Set<EventHandler>> eventHandlers;
 	
 	//functions
 	
	protected Document(BrowserChannel channel){
 		this.channel = channel;
 		collaborators = new ArrayList<Collaborator>();
 		eventHandlers = new HashMap<EventType, Set<EventHandler>>();
 	}
 
     /**
      * Closes the document and disconnects from the server. After this function is called, event listeners will no longer fire and attempts to access the document, model, or model objects will throw a {@link gapi.drive.realtime.DocumentClosedError}. Calling this function after the document has been closed will have no effect.
      */
     public void close() {
     	channel.disconnect();
     	eventHandlers = new HashMap<EventType, Set<EventHandler>>();
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
 
    public void addEventListener(EventType type, EventHandler handler){
     	Set<EventHandler> handlers = eventHandlers.get(type);
     	if(handlers == null){
     		handlers = new HashSet<EventHandler>();
     		eventHandlers.put(type, handlers);
     	}
     	handlers.add(handler);
     }
 
    public void removeEventListener(EventType type, EventHandler handler){
     	Set<EventHandler> handlers = eventHandlers.get(type);
     	if(handlers != null){
     		handlers.remove(handler);
     	}
     }
     
    protected void fireEvent(EventType type, BaseModelEvent event){
     	Set<EventHandler> handlers = eventHandlers.get(type);
     	for(EventHandler handler : handlers){
     		handler.execute(event);
     	}
     }
 }
