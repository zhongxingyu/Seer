 package se.altrusoft.alfplay.behaviour;
   
 import java.util.Map;
 
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.repository.NodeService;
 import org.alfresco.service.cmr.repository.StoreRef;
 import org.springframework.extensions.webscripts.Cache;
 import org.springframework.extensions.webscripts.DeclarativeWebScript;
 import org.springframework.extensions.webscripts.Status;
 import org.springframework.extensions.webscripts.WebScriptException;
 import org.springframework.extensions.webscripts.WebScriptRequest;
 
 
 public class NodeObservChildCreationPost extends DeclarativeWebScript {
 	
 	protected static final StoreRef SPACES_STORE = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
 	//new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
 	
 	protected NodeService nodeService;
 	protected EventTriggerService eventTriggerService;
 	
 	public void setNodeService(NodeService nodeService) {
 		this.nodeService = nodeService;
 	}
 	
 	public void setEventTriggerService(EventTriggerService eventTriggerService) {
 		this.eventTriggerService = eventTriggerService;
 	}
 	
 	@Override
 	protected Map<String, Object> executeImpl(WebScriptRequest req,
 				Status status, Cache cache) {		
 
 		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
 		String nodeId = templateArgs.get("nodeId");
 		if (nodeId == null) throw new WebScriptException("No nodeId provided");
 		
 		NodeRef nodeRef = new NodeRef(SPACES_STORE, nodeId);
 		if (! this.nodeService.exists(nodeRef)) 
 			throw new WebScriptException("No such node: " + nodeId);
 		
 		this.eventTriggerService.addNotificaitonOnCreateChild(nodeRef);
 		
 		status.setCode(303);
		status.setLocation(req.getServiceContextPath()+"/alfplay/node/"+nodeId);
 		status.setRedirect(true);
 			
 		return null;
 	}
 }
