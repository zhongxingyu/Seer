 package com.delcyon.capo.xml.dom;
 
 import java.lang.reflect.Modifier;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Level;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.controller.ControlElement;
 import com.delcyon.capo.controller.elements.ResourceControlElement;
 import com.delcyon.capo.resourcemanager.ContentFormatType;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
 import com.delcyon.capo.resourcemanager.ResourceURI;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.Action;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.DefaultParameters;
 import com.delcyon.capo.resourcemanager.types.ContentMetaData;
 import com.delcyon.capo.resourcemanager.types.SimpleContentMetaData;
 import com.delcyon.capo.util.CloneControl;
 import com.delcyon.capo.util.ControlledClone;
 import com.delcyon.capo.util.ToStringControl;
 import com.delcyon.capo.util.CloneControl.Clone;
 import com.delcyon.capo.util.ToStringControl.Control;
 import com.delcyon.capo.xml.cdom.CAttr;
 import com.delcyon.capo.xml.cdom.CDOMEvent;
 import com.delcyon.capo.xml.cdom.CDOMEventListener;
 import com.delcyon.capo.xml.cdom.CElement;
 import com.delcyon.capo.xml.cdom.CNamedNodeMap;
 import com.delcyon.capo.xml.cdom.CNode;
 import com.delcyon.capo.xml.cdom.CNodeList;
 import com.delcyon.capo.xml.cdom.VariableContainer;
 
 @CloneControl(filter=CloneControl.Clone.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
 @ToStringControl(control=Control.exclude,modifiers=Modifier.STATIC+Modifier.FINAL)
 public class ResourceElement extends CElement implements ControlledClone,ResourceNode, CDOMEventListener
 {
 
    
     
     @CloneControl(filter=Clone.exclude)
     @ToStringControl(control=Control.exclude)
     private ResourceDocument ownerResourceDocument;
     
     private ResourceDescriptor resourceDescriptor;    
     
     private List<ContentMetaData> childResourceContentMetaData;
     private ContentMetaData contentMetaData;
    
 	private boolean dynamic = true;
 	private ResourceURI resourceURI;
 	private CElement content;
 	
 	
 	@SuppressWarnings("unused")
 	private ResourceElement(){} //serialization only
 	
 	public ResourceElement(ResourceDocument ownerResourceDocument, String localName, CElement content,ContentMetaData contentMetaData)
 	{
 		super(CapoApplication.RESOURCE_NAMESPACE_URI,"resource",localName);		
 		this.ownerResourceDocument = ownerResourceDocument;				
 		setContent(content);
 		this.dynamic = false;
 		setContentMetatData(contentMetaData);		
 	}
     
 	/**
 	 * This will pull the localName from the resourceDescriptor to use as it's localName
 	 * @param ownerResourceDocument
 	 * @param parentNode
 	 * @param resourceDescriptor
 	 * @throws Exception
 	 */
     public ResourceElement(ResourceDocument ownerResourceDocument,ResourceNode parentNode,ResourceDescriptor resourceDescriptor) throws Exception
     {
     	super(CapoApplication.RESOURCE_NAMESPACE_URI,"resource",resourceDescriptor.getLocalName());	
     	this.ownerResourceDocument = ownerResourceDocument;
         this.resourceDescriptor = resourceDescriptor;        
         this.dynamic = true;
         setContentMetatData(resourceDescriptor.getResourceMetaData(null));
     }
 
     public void setContentMetatData(ContentMetaData contentMetaData)
     {
     	this.contentMetaData = contentMetaData;
     	this.resourceURI = contentMetaData.getResourceURI();
 
     	setResourceAttribute("uri", contentMetaData.getResourceURI().getResourceURIString());
     	setResourceAttribute("new",false+"");
     	setResourceAttribute("modified",false+"");
     	List<String> supportedAttributeList = contentMetaData.getSupportedAttributes();
     	for (String attributeName : supportedAttributeList)
     	{
     		if (contentMetaData.getValue(attributeName) != null)
     		{                
     			ResourceAttr resourceAttr = new ResourceAttr(this,attributeName, contentMetaData.getValue(attributeName));
     			setResourceAttributeNode(resourceAttr);
     		}
     	}
 
     }
     
     @Override
     public void cascadeDOMEvent(CDOMEvent cdomEvent)
     {
     	if (content == null || ownerResourceDocument.isFullDocument() == true)
     	{
     		super.cascadeDOMEvent(cdomEvent);
     	}
     	else if (cdomEvent != null)
     	{
     		processEvent(cdomEvent);
     		if(getParentNode() != null && getParentNode() instanceof CNode)
     		{
     			((CNode) getParentNode()).cascadeDOMEvent(cdomEvent);
     		}
     	}
     }
     
     @Override
     public Vector<CDOMEventListener> getCDOMEventListeners()
     {
     	if(content == null || ownerResourceDocument.isFullDocument() == true)
     	{
     		return super.getCDOMEventListeners();
     	}
     	else
     	{
     		return content.getCDOMEventListeners();
     	}
     }
     
     @Override
     public boolean hasEventListeners()
     {
     	if(content == null || ownerResourceDocument.isFullDocument() == true)
     	{
     		return super.hasEventListeners();
     	}
     	else
     	{
     		return content.hasEventListeners();
     	}
     	
     }
     
    
     
     public boolean isContainter()
     {
     	if(contentMetaData != null)
     	{
     		return contentMetaData.isContainer();
     	}
     	return false;
     }
     
     @Override
     public void processEvent(CDOMEvent cdomEvent)
     {
     	System.out.println(cdomEvent);
     	if(cdomEvent.getSourceNode().equals(this))
     	{
     		cdomEvent.setHandled(true);
     	}
     	setResourceAttribute("modified",true+"");    	
     }
     
     public void update(VariableContainer variableContainer, ControlElement callingControlElement, ResourceParameter... resourceParameters) throws Exception
     {
     	if( (isNew() || isModified()) && isContainter() == false)
     	{
     		if(resourceDescriptor == null)
     		{
     			resourceDescriptor = getResourceControlElement().getParentGroup().getResourceDescriptor( callingControlElement, resourceURI.getResourceURIString());
     			resourceDescriptor.open(variableContainer,resourceParameters);
     		}
     		if(content == null && isModified())
     		{
     		    if (resourceDescriptor.getResourceMetaData(null).exists() == true)
                 {
                     resourceDescriptor.performAction(null, Action.DELETE);              
                 }                
     		}
     		else
     		{
     		    DefaultParameters updateStyle = DefaultParameters.MODIFY_REPLACE;
     		    if(isNew())
     		    {
     		        updateStyle = DefaultParameters.NEW;
     		    }
     		    ResourceParameterBuilder parameterBuilder = new ResourceParameterBuilder();
     		    parameterBuilder.addAll(resourceParameters);
     		    parameterBuilder.addParameter(updateStyle, "true");
     		    resourceDescriptor.writeXML(variableContainer, content,parameterBuilder.getParameters());
     		}
     		
         	setContentMetatData(resourceDescriptor.getResourceMetaData(null));
     	}
     	setResourceAttribute("new",false+"");
 		setResourceAttribute("modified",false+"");
     	CNodeList children = (CNodeList) getChildNodes();
     	for (Node node : children)
 		{
 			if(node instanceof ResourceElement)
 			{
 				((ResourceElement) node).update(variableContainer,callingControlElement,resourceParameters);
 			}
 		}
     }
     
     public boolean isNew()
     {
     	return getResourceAttribute("new").equals("true");
     }
     
     public void setNew(boolean isNew)
     {
     	setResourceAttribute("new",isNew+"");
     }
     
     public boolean isModified()
     {
     	return getResourceAttribute("modified").equals("true");
     }
     
     public void setModified(boolean modified)
     {
     	setResourceAttribute("modified",modified+"");
     }
     
     public void setResourceDescriptor(ResourceDescriptor resourceDescriptor)
 	{
 		this.resourceDescriptor = resourceDescriptor;
 	}
     
 //    public ResourceElement(ResourceNode parentNode, ResourceControlElement resourceControlElement) throws Exception
 //    {
 //        this.parentNode = parentNode;
 //        this.resourceControlElement = resourceControlElement;
 //        this.recursive = false;
 //        namespaceURI = parentNode.getNamespaceURI();
 //        prefix = parentNode.getPrefix();
 //        NamedNodeMap attributeNamedNodeMap = resourceControlElement.getControlElementDeclaration().getAttributes();
 //        for(int index = 0; index < attributeNamedNodeMap.getLength(); index++)
 //        {
 //            Attr attr = (Attr) attributeNamedNodeMap.item(index);
 //            ResourceAttr resourceAttr = new ResourceAttr(this,attr.getNodeName(), attr.getNodeValue());            
 //            attributeList.add(resourceAttr);
 //        }
 //        
 //        if(attributeList.getNamedItem("name") != null)
 //        {
 //            localName = attributeList.getNamedItem("name").getNodeValue();
 //        }
 //        else
 //        {
 //            localName = resourceControlElement.getControlElementDeclaration().getLocalName();
 //        }
 //        
 //        //if we have a URI, then we can go ahead a load a resourceDescriptor for this element 
 //        if(attributeList.getNamedItem("uri") != null)
 //        {
 //            this.resourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(resourceControlElement, attributeList.getNamedItem("uri").getNodeValue());
 //            
 //            this.resourceControlElement.setResourceDescriptor(resourceDescriptor);
 //        }
 //        else if(attributeList.getNamedItem("path") != null) //check for a path attribute, and ask our parent to load us
 //        {
 //            this.resourceDescriptor = parentNode.getResourceDescriptor().getChildResourceDescriptor(resourceControlElement, attributeList.getNamedItem("path").getNodeValue());
 //        }
 //        else
 //        {
 //            //we don't have anything, throw an exception for now?
 //            throw new Exception("Must have a uri or a path attribute");
 //        }
 //        
 //        //sometimes we may have variables that need to be filled out laster, so we can't init or open this yet.  
 //        if(resourceControlElement.getControlElementDeclaration().getAttribute("dynamic").equalsIgnoreCase("true") == false)
 //        {
 //        	resourceDescriptor.init(this,resourceControlElement.getParentGroup(), LifeCycle.EXPLICIT,true,ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
 //        	resourceDescriptor.open(resourceControlElement.getParentGroup(), ResourceParameterBuilder.getResourceParameters(resourceControlElement.getControlElementDeclaration()));
 //        }
 //        
 //        NodeList childResourceElementDeclarationNodeList =  XPath.selectNSNodes(resourceControlElement.getControlElementDeclaration(), prefix+":child", prefix+"="+namespaceURI);
 //        for(int index = 0; index < childResourceElementDeclarationNodeList.getLength(); index++)
 //        {
 //            ResourceControlElement childResourceControlElement = new ResourceControlElement();
 //            //XXX This is a hack! we are setting the parent group to null, so that it won't process any of the attributes that might have vars.
 //            childResourceControlElement.init((Element) childResourceElementDeclarationNodeList.item(index), resourceControlElement, null, resourceControlElement.getControllerClientRequestProcessor());
 //            //XXX then we set it back here, so the we still have the full var stack. This would all be fine until we change the init method in the AbstractControl class. 
 //            childResourceControlElement.setParentGroup(resourceControlElement.getParentGroup());
 //            nodeList.add(new ResourceElement(this, childResourceControlElement));
 //        }
 //    }
     
     public void setContent(CElement content)
 	{
     	
     	this.content = content;
     	if(content != null)
     	{
     		this.content.addCDOMEventListener(this);        		
     		content.setParent(this);
     	}
 	}
     
     public Element getContent()
 	{
 		return content;
 	}
     
     
     @Override
     public ResourceDescriptor getResourceDescriptor()
     {
     	return new ResourceElementResourceDescriptor(this);
     }
 
    @Override
    public ResourceDescriptor getProxyedResourceDescriptor()
    {
 	   return this.resourceDescriptor;
    }
    
    @Override
    public ResourceControlElement getResourceControlElement()
    {
        return this.ownerResourceDocument.getResourceControlElement();
    }
 
     
     @Override
     public String getNodeName()
     {
         if(isContainter() == false && content != null && ownerResourceDocument.isFullDocument() == false)
         {
             return content.getNodeName();
         }
         else
         {
             return super.getNodeName();
         }
     }
 
    
 
    
 
 
     @Override
     public NodeList getChildNodes()
     {    	
         //depending on how we're created, we either know the list, or we want to figure it out.
         if (dynamic == false)
         {
         	if(isContainter() == true)
         	{
         		return super.getChildNodes();
         	}
         	else //were a leaf, and we should return our content, and we should pretend that our content's root element is us, and we don't have any children
         	{
         		CNodeList tempNodeList = new CNodeList();
         		tempNodeList.addAll(content.getChildNodes());
 //        		if(ownerResourceDocument.isFullDocument())
 //        		{
         			tempNodeList.addAll(super.getChildNodes());
         		//}
 		        return tempNodeList;
         	}
         
         }
         
         if (childResourceContentMetaData == null)
         {
             childResourceContentMetaData = contentMetaData.getContainedResources();
             for (ContentMetaData childContentMetaData : childResourceContentMetaData)
             {
                 
                 try
                 {
                     appendChild(new ResourceElement(ownerResourceDocument,this,CapoApplication.getDataManager().getResourceDescriptor(null, childContentMetaData.getResourceURI().getBaseURI())));
                 }
                 catch (Exception e)
                 {
                    CapoApplication.logger.log(Level.WARNING,"couldn't load resource: "+childContentMetaData.getResourceURI(),e);
                 }
             }
             if (contentMetaData.isContainer() == false)
            {                
                 if (contentMetaData.getContentFormatType() == ContentFormatType.XML)
                 {
                     try
                     {
                         resourceDescriptor.open(null);
                         Element xmlElement = resourceDescriptor.readXML(null);
                         resourceDescriptor.close(null);
                         appendChild(xmlElement);
                     }
                     catch (Exception e)
                     {
                         CapoApplication.logger.log(Level.WARNING,"couldn't load resource data: "+contentMetaData.getResourceURI(),e);
                     }
                 }
                 else 
                 {
                    //XXX this is incomplete!!!
                 }
             }
         }
         return super.getChildNodes();
     }
 
     @Override
     public Node getFirstChild()
     {
     	NodeList childNodes = getChildNodes();
     	if (childNodes.getLength() > 0)
     	{
     		return childNodes.item(0);
     	}
     	else
     	{
     		return null;
     	}
         
     }
 
    
 
     @Override
     public Node getNextSibling()
     {
     
        NodeList siblingList = getParentNode().getChildNodes();
        int myPosition = 0;
        for(int index = 0; index < siblingList.getLength(); index++)
        {
     	   Node siblingNode = siblingList.item(index);
     	   if (siblingNode.isSameNode(this))
     	   {
     		   myPosition = index;
     		   break;
     	   }
        }
        if (myPosition+1 < siblingList.getLength())
        {
     	   return siblingList.item(myPosition+1);
        }
        else
        {
     	   return null;
        }
     }
 
     @Override
     public NamedNodeMap getAttributes()
     {
     	if(ownerResourceDocument.isFullDocument())
     	{
     		return super.getAttributes();
     	}
         else if(isContainter() == true )
         {        	
         	return  new CNamedNodeMap();
         }
         else if (content != null)
         {
         	return content.getAttributes();
         }
         else
         {
         	return  new CNamedNodeMap();
         }
         
     }
 
     
     public ResourceDocument getOwnerResourceDocument()
 	{
 		return ownerResourceDocument;
 	}
     
     
 
     @Override
     public boolean hasChildNodes()
     {
         return getChildNodes().getLength() > 0;
     }
 
  
     
     public void postClone(Object parentObject, Object clonedObject)
     {
         ResourceElement clonedResourceElement = (ResourceElement) clonedObject;
         //we treat these differently, because we don't want them to recurse
         clonedResourceElement.ownerResourceDocument = ownerResourceDocument;
        
         super.postClone(parentObject, clonedObject);
         if (clonedResourceElement.content != null)
         {
         	clonedResourceElement.content.addCDOMEventListener(clonedResourceElement);
         }
     }
 
 
 
     @Override
     public String getNamespaceURI()
     {        
         if(isContainter() == false && content != null && ownerResourceDocument.isFullDocument() == false)
         {
             return content.getNamespaceURI();
         }
         else
         {
             return super.getNamespaceURI();
         }
     }
 
    
 
     @Override
     public String getLocalName()
     {
         if(isContainter() == false  && content != null && ownerResourceDocument.isFullDocument() == false)
         {
             return content.getLocalName();
         }
         else
         {
             return super.getLocalName();
         }
     }
 
     @Override
     public Node appendChild(Node newChild) throws DOMException
     {
         if(newChild instanceof ResourceElement == false)
         {
             SimpleContentMetaData metaData = new SimpleContentMetaData(contentMetaData.getResourceURI());
             ResourceElement childResourceElement = new ResourceElement(getOwnerResourceDocument(), newChild.getLocalName(), (CElement) newChild, metaData);
             childResourceElement.setNew(true);
             childResourceElement.setModified(true);
             return super.appendChild(childResourceElement);
         }
         return super.appendChild(newChild);
     }
 
     @Override
     public Node removeChild(Node oldChild) throws DOMException
     {
         if(oldChild instanceof ResourceElement == false)
         {
             content = null;
             setModified(true);
             return oldChild;
         }
         return super.removeChild(oldChild);
     }
 
     @Override
     public String getAttribute(String name)
     {
     	if (hasAttribute(name))
     	{
     		return getAttributes().getNamedItem(name).getNodeValue();
     	}
     	else
     	{
     		return "";
     	}
     }
 
 
    
 
     public Element export(boolean contentOnly) throws Exception
     {    	
         return ResourceDocument.export(this,contentOnly).getDocumentElement();
     }
 
     /**
      * Will always set a ResourceElements real attributes, as opposed to it's content's
      * @param name
      * @param value
      */
 	public void setResourceAttribute(String name, String value)
 	{
 		
 		attributeList.setNamedItemNS(new CAttr(this, null, name, value));
 	}
 
 	public String getResourceAttribute(String name)
 	{
 		if (attributeList.getNamedItem(name) == null)
         {
             return "";
         }
         else
         {
             return attributeList.getNamedItem(name).getNodeValue();
         }
 	}
 
 	public CAttr setResourceAttributeNode(CAttr attr)
 	{
 		 attributeList.setNamedItem(attr);	        
 		 return attr;
 	}
 
 	
 
     
 }
