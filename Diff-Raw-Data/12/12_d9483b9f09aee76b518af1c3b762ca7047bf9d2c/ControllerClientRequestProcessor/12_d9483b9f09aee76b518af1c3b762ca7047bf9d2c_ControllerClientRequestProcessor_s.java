 package com.delcyon.capo.controller.server;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.ContextThread;
 import com.delcyon.capo.CapoApplication.Location;
 import com.delcyon.capo.Configuration.PREFERENCE;
 import com.delcyon.capo.annotations.DefaultDocumentProvider;
 import com.delcyon.capo.annotations.DirectoyProvider;
 import com.delcyon.capo.controller.elements.GroupElement;
 import com.delcyon.capo.exceptions.MissingAttributeException;
 import com.delcyon.capo.preferences.Preference;
 import com.delcyon.capo.preferences.PreferenceInfo;
 import com.delcyon.capo.preferences.PreferenceInfoHelper;
 import com.delcyon.capo.preferences.PreferenceProvider;
 import com.delcyon.capo.protocol.server.ClientRequest;
 import com.delcyon.capo.protocol.server.ClientRequestProcessor;
 import com.delcyon.capo.protocol.server.ClientRequestProcessorProvider;
 import com.delcyon.capo.protocol.server.ClientRequestXMLProcessor;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
 import com.delcyon.capo.resourcemanager.types.FileResourceType;
 import com.delcyon.capo.server.CapoServer;
 import com.delcyon.capo.xml.XPath;
 
 @PreferenceProvider(preferences=ControllerClientRequestProcessor.Preferences.class)
 @DirectoyProvider(preferenceName="CONTROLLER_DIR",preferences=PREFERENCE.class,location=Location.SERVER)
 @DefaultDocumentProvider(directoryPreferenceName="CONTROLLER_DIR",preferences=ControllerClientRequestProcessor.Preferences.class,name="default.xml,update.xml,identity.xml",location=Location.SERVER)
 @ClientRequestProcessorProvider(name="ControllerRequest")
 //@XMLProcessorProvider(documentElementNames={"ControllerRequest"},namespaceURIs={"http://www.delcyon.com/capo-client","http://www.delcyon.com/capo-server"})
 public class ControllerClientRequestProcessor implements ClientRequestProcessor
 {
 
 	
 	public enum Preferences implements Preference
 	{
 		@PreferenceInfo(arguments={"file"}, defaultValue="default.xml", description="default controller file, relative to controller dir", longOption="DEFAULT_CONTROLLER_FILE", option="DEFAULT_CONTROLLER_FILE")
 		DEFAULT_CONTROLLER_FILE,		
 		@PreferenceInfo(arguments={"file"}, defaultValue="identity.xml", description="main identity script file, relative to controller dir", longOption="DEFAULT_IDENTITY_FILE", option="DEFAULT_IDENTITY_FILE")
 		DEFAULT_IDENTITY_FILE,
 		@PreferenceInfo(arguments={"file"}, defaultValue="update.xml", description="main update script file, relative to controller dir", longOption="DEFAULT_UPDATE_FILE", option="DEFAULT_UPDATE_FILE")
 		DEFAULT_UPDATE_FILE;
 		@Override
 		public String[] getArguments()
 		{
 			return PreferenceInfoHelper.getInfo(this).arguments();
 		}
 
 		@Override
 		public String getDefaultValue()
 		{
 			return PreferenceInfoHelper.getInfo(this).defaultValue();
 		}
 
 		@Override
 		public String getDescription()
 		{
 			return PreferenceInfoHelper.getInfo(this).description();
 		}
 
 		@Override
 		public String getLongOption()
 		{
 			return PreferenceInfoHelper.getInfo(this).longOption();
 		}
 
 		@Override
 		public String getOption()
 		{		
 			return PreferenceInfoHelper.getInfo(this).option();
 		}
 		
 		@Override
 		public Location getLocation() 
 		{
 			return PreferenceInfoHelper.getInfo(this).location();
 		}
 	}
 	
 	public static final String REQUEST_TYPE_ATTRIBUTE = "type";
 	private static final String VALUE_ATTRIBUTE = "value";
 	private static final String NAME_ATTRIBUTE = "name";
 	private static final String MAIN_GROUP = "mainGroup";
 	
 		
 	private transient Document clientControlerDocument;
 	private HashMap<String, String> requestHashMap = new HashMap<String, String>();		
 	private String mainGroupName = "default";
 	private transient ClientRequestXMLProcessor clientRequestXMLProcessor;
 	private String sessionID;
     private HashMap<String, String> sessionHashMap = null;
     private String documentURI = null;
 
 	
 	
 	@Override
 	public void init(ClientRequestXMLProcessor clientRequestXMLProcessor,String sessionID,HashMap<String, String> sessionHashMap,String requestName) throws Exception
 	{
 		this.clientRequestXMLProcessor = clientRequestXMLProcessor;
 		this.clientControlerDocument = loadClientControlDocument(requestName,sessionHashMap.get("clientID"));
 		if (this.clientControlerDocument != null)
 		{
 			this.documentURI = this.clientControlerDocument.getDocumentURI();
 		}
 		this.sessionID = sessionID;
 		this.sessionHashMap = sessionHashMap;
 	}
 	
 	public void init(ClientRequestXMLProcessor clientRequestXMLProcessor,String sessionID,Document clientControlerDocument,HashMap<String, String> sessionHashMap) throws Exception
 	{
 		this.clientRequestXMLProcessor = clientRequestXMLProcessor;
 		this.clientControlerDocument = clientControlerDocument;
 		this.sessionID = sessionID;
 		this.sessionHashMap = sessionHashMap;
 	}
 	
 	public ClientRequestXMLProcessor getClientRequestXMLProcessor()
 	{
 		return clientRequestXMLProcessor;
 	}
 	
 	@Override
 	public void process(ClientRequest clientRequest) throws Exception
 	{	
 	    if (clientControlerDocument == null)
 	    {
 	        return; 
 	    }
 		ClientControllerRequest clientControllerRequest = new ClientControllerRequest(clientRequest);
 		processClientControllerRequest(clientControllerRequest);		
 	}
 	
 	
 	private void processClientControllerRequest(ClientControllerRequest clientControllerRequest) throws Exception
 	{
 	    String clientID = null;
 	    String requestType = null;
 		try
 		{
 			
 		    //Figure out the request type
 		    clientID = sessionHashMap.get("clientID");
 		    
 		    Element documentElement = clientControllerRequest.getControllerRequestDocument().getDocumentElement();
 		    
 		    if (documentElement.hasAttribute("type"))
 		    {
 		        requestType = documentElement.getAttribute("type");
 		    }
 		    
 		    loadClientRequestVariables(clientControllerRequest);
 		    
 
 		    loadIdentityVariables(clientID);
 		   
 			
 			//set main group name
 			if (clientControlerDocument.getDocumentElement().getAttribute(MAIN_GROUP) != null)
 			{
 				mainGroupName = clientControlerDocument.getDocumentElement().getAttribute(MAIN_GROUP);
 			}
 						
 			//find a main group
 			((ContextThread)Thread.currentThread()).setContext(clientControlerDocument);
 			CapoServer.logger.log(Level.FINE, "main group = "+mainGroupName);
 			Element mainGroupElement = (Element) XPath.selectSingleNode(clientControlerDocument, "server:group('"+mainGroupName+"','"+clientControlerDocument.getDocumentElement().getPrefix()+"')");
 			if (mainGroupElement == null)
 			{
 			    mainGroupElement = clientControlerDocument.getDocumentElement();
 			}
 			
 			//copy client request as first child to document element
 			Element mainGroupDocumentElement = mainGroupElement.getOwnerDocument().getDocumentElement(); 
 			mainGroupDocumentElement.insertBefore(mainGroupElement.getOwnerDocument().importNode(clientControllerRequest.getControllerRequestDocument().getDocumentElement(),true),mainGroupDocumentElement.getFirstChild());
             
 			
 			//create a new group element out whatever we have discovered
 			GroupElement groupElement = new GroupElement();
 			((ContextThread)Thread.currentThread()).setContext(groupElement);
 			groupElement.init(mainGroupElement, null, null, this);
 			CapoApplication.logger.log(Level.INFO,"Running request '"+requestType+"' for client:"+clientID);
 
 			//run this request
 			groupElement.processServerSideElement();
 			((ContextThread)Thread.currentThread()).setContext(null);
 
 		}
 		catch (ControllerProcessingException controllerProcessingException)
 		{
 			String message = "error running "+requestType+" ("+documentURI+") request for client: "+clientID+"\n"+controllerProcessingException.getMessage();
 			CapoServer.logger.log(Level.SEVERE, message);
 			
 			if (CapoApplication.getApplication().getExceptionList() != null)
 			{
 				CapoApplication.getApplication().getExceptionList().add(new Exception(message));
 			}
 		}
 		catch (MissingAttributeException missingAttributeException)
 		{
 			String message = "error running "+requestType+" ("+documentURI+") request for client: "+clientID+"\n"+missingAttributeException.getMessage();
 			CapoServer.logger.log(Level.SEVERE, message);
 			
 			if (CapoApplication.getApplication().getExceptionList() != null)
 			{
 				CapoApplication.getApplication().getExceptionList().add(new Exception(message));
 			}
 			
 		}
 		catch (Exception exception)
 		{
 			String message = "error running "+requestType+" ("+documentURI+") request for client: "+clientID;
 			CapoServer.logger.log(Level.SEVERE, message, exception);
 			
 			if (CapoApplication.getApplication().getExceptionList() != null)
 			{
 				CapoApplication.getApplication().getExceptionList().add(new Exception(message, exception));
 			}
 		}	
 	}
 	
 	
 	
 	private void loadIdentityVariables(String clientID) throws Exception
     {
 	    //make sure we are authenticated
 	    if (clientID == null || clientID.isEmpty())
 	    {
 	        return;
 	    }
         ///check and see if this is an identity push, identity information gets saved for later use
         String identityControlName = CapoApplication.getConfiguration().getValue(Preferences.DEFAULT_IDENTITY_FILE);
 
         //make sure we have a document
         ResourceDescriptor clientResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null, "clients:"+clientID+"/"+identityControlName);
         if (clientResourceDescriptor.getContentMetaData(null).exists() == false)
         {
             clientResourceDescriptor.performAction(null, Action.CREATE);
             CapoApplication.logger.log(Level.INFO,"Creating new identity document for "+clientID);
             XPath.dumpNode(CapoApplication.getDefaultDocument("ids.xml"), clientResourceDescriptor.getOutputStream(null));
         }
         
         Element identityDocumentElement = CapoApplication.getDocumentBuilder().parse(clientResourceDescriptor.getInputStream(null)).getDocumentElement();
         NodeList idElementNodeList = XPath.selectNodes(identityDocumentElement, "//server:id");
         for(int index = 0; index < idElementNodeList.getLength(); index++)
         {
             Element ideElement = (Element) idElementNodeList.item(index);
             sessionHashMap.put(ideElement.getAttribute("name"), ideElement.getAttribute("value"));
         }
         
     }
 
     private void loadClientRequestVariables(ClientControllerRequest clientControllerRequest)
     {
 	    //load clientRequestVariables
         NodeList clientRequestChildren = clientControllerRequest.getControllerRequestDocument().getDocumentElement().getChildNodes();
         for (int currentNode = 0; currentNode < clientRequestChildren.getLength();currentNode++)
         {
             Node node = clientRequestChildren.item(currentNode);
             if (node.getNodeType() != Node.ELEMENT_NODE)
             {
                 continue;
             }
             Element childElement = (Element) node;
             String nameAttribute = childElement.getAttribute(NAME_ATTRIBUTE);
             
             String valueAttributeValue = childElement.getAttribute(VALUE_ATTRIBUTE);
             if (valueAttributeValue != null && valueAttributeValue.isEmpty() == true)
             {
                 valueAttributeValue = null;                 
             }
             if (nameAttribute != null && valueAttributeValue == null && childElement.getTextContent() != null && childElement.getTextContent().isEmpty() == false)
             {                   
                 valueAttributeValue = childElement.getTextContent();
                 CapoServer.logger.log(Level.FINE, "seting client var: '"+nameAttribute+"' = '"+valueAttributeValue+"'");
             }
             else if (nameAttribute != null && valueAttributeValue != null && valueAttributeValue.trim().isEmpty() == false)
             {               
                 requestHashMap.put(nameAttribute, valueAttributeValue);
                 CapoServer.logger.log(Level.FINE, "seting client var: '"+nameAttribute+"' = '"+valueAttributeValue+"'");
             }
         }
         
     }
 
     public void writeResponse(ControllerResponse controllerResponse) throws Exception
 	{		
 		clientRequestXMLProcessor.writeResponse(controllerResponse);				
 	}
 
 	
 	
 	//SS - Group
 	public HashMap<String, String> getRequestHashMap()
 	{
 		return requestHashMap;
 	}
 
 	@Override
 	public Document readNextDocument() throws Exception
 	{		
 	    return XPath.unwrapDocument(clientRequestXMLProcessor.readNextDocument());
 	}
 	
 	
 	@Override
 	public String getSessionId()
 	{
 		return sessionID;
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param clientElement
 	 * @return first child of root element of the clients reply, imported into the original clientElement's document
 	 * @throws Exception
 	 */
 	//SS
 	public Element sendServerSideClientElement(Element clientElement) throws Exception
 	{
 		ControllerResponse controllerResponse = createResponse();
 		controllerResponse.setControlElement(clientElement);
 		
 		writeResponse(controllerResponse);
 		
 
 		//send the the response, and wait for the returned RequestDocument
 		Document replyDocument = readNextDocument();		
 		//import the returned root element into the original clientElements document
 		NodeList nodeList = replyDocument.getDocumentElement().getElementsByTagName("*");
 		if (nodeList.getLength() > 0)
 		{
 			return (Element) clientElement.getOwnerDocument().importNode(nodeList.item(0), true);
 		}
 		else
 		{
 			return null;
 		}
 		
 	}
 
 	
 
 	public ControllerResponse createResponse() throws Exception
 	{
 		ControllerResponse controllerResponse = new ControllerResponse();
 		controllerResponse.setSessionID(getSessionId());
 		return controllerResponse;
 	}
 
 	
 	private Document loadClientControlDocument(String controlerName,String clientID) throws Exception
 	{
 		
 		Document localCapoDocument = null;
 		if (controlerName == null || controlerName.isEmpty())
 		{
 			controlerName = CapoApplication.getConfiguration().getValue(Preferences.DEFAULT_CONTROLLER_FILE);
 		}
 		
 		localCapoDocument = CapoApplication.getDataManager().findDocument(controlerName, clientID, PREFERENCE.CONTROLLER_DIR);
 		if (localCapoDocument == null)
 		{
 		    CapoApplication.logger.log(Level.WARNING,"Couldn't find a controller named "+controlerName);
 		    return null;
 		}
 					
 		//load includes
 		
 		NodeList includeObjectList = XPath.selectNodes(localCapoDocument, "//server:include");
 		while(includeObjectList != null && includeObjectList.getLength() != 0)
 		{
 			for(int currentNode = 0; currentNode < includeObjectList.getLength();currentNode++)		
 			{
 				Element includeElement = ((Element)includeObjectList.item(currentNode));
 				String src = includeElement.getAttribute("src");
 				if (src == null)
 				{
 					includeElement.getParentNode().removeChild(includeElement);
 				}				
 				else
 				{
 					
 					ResourceDescriptor includeResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(null,src);
 					includeResourceDescriptor.addResourceParameters(null, new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.CONTROLLER_DIR));
 					if (includeResourceDescriptor.getContentMetaData(null).exists() == true)
 					{
 					    Document importDocument = CapoApplication.getDocumentBuilder().parse(includeResourceDescriptor.getInputStream(null));					
 					    includeElement.getParentNode().replaceChild(includeElement.getOwnerDocument().importNode(importDocument.getDocumentElement(), true), includeElement);
 					}
 					else
 					{
 					    CapoApplication.logger.log(Level.WARNING,"Couldn't include "+src+" into "+controlerName);
 					    includeElement.getParentNode().removeChild(includeElement);
 					}
 				}
 			}
 			includeObjectList = XPath.selectNodes(localCapoDocument, "//server:include");
 		}
 		
 		
 		
 		return localCapoDocument;
 	}
 
     public HashMap<String, String> getSessionHashMap()
     {
         return this.sessionHashMap ;
     }
 
 	
 	
 }
