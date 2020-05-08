 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.controller;
 
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Level;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.ContextThread;
 import com.delcyon.capo.controller.client.ClientSideControl;
 import com.delcyon.capo.controller.client.ServerControllerResponse;
 import com.delcyon.capo.controller.elements.GroupElement;
 import com.delcyon.capo.controller.server.ControllerClientRequestProcessor;
 import com.delcyon.capo.controller.server.ServerSideControl;
 import com.delcyon.capo.exceptions.MissingAttributeException;
 import com.delcyon.capo.modules.ModuleProvider;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.LifeCycle;
 import com.delcyon.capo.xml.XPath;
 
 /**
  * @author jeremiah
  *
  */
 @SuppressWarnings("unchecked")
 public abstract class AbstractControl implements ServerSideControl
 {
 
 	
 	
 	private static HashMap<String, Class> controlElementHashMap = null;
 	static
 	{
 		controlElementHashMap = new HashMap<String, Class>();
 		Set<String> controlElementProviderSet = CapoApplication.getAnnotationMap().get(ControlElementProvider.class.getCanonicalName());
 		for (String className : controlElementProviderSet)
 		{
 			try
 			{
 				Class controlElementProviderClass = Class.forName(className);
 				ControlElementProvider controlElementProvider = (ControlElementProvider) controlElementProviderClass.getAnnotation(ControlElementProvider.class);
 				controlElementHashMap.put(controlElementProvider.name(), controlElementProviderClass);
 				CapoApplication.logger.log(Level.CONFIG, "Loaded ControlElementProvider <"+controlElementProvider.name()+"/> from "+className);
 			} catch (ClassNotFoundException classNotFoundException)
 			{
 				CapoApplication.logger.log(Level.WARNING, "Error getting ControlElement providers",classNotFoundException);
 			}
 		}
 	}
 	
 	
 	
 	public static ControlElement getControlElementInstanceForLocalName(String localName) throws Exception
 	{
 		Class controlElementClass = controlElementHashMap.get(localName);
 		if (controlElementClass != null)
 		{
 			return (ControlElement) controlElementClass.newInstance();
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	private Group parentGroup;
 	private transient Element controlElementDeclaration;
 	private transient Element originalControlElementDeclaration;
 	private ControllerClientRequestProcessor controllerClientRequestProcessor;
 	private transient ControlElement parentControlElement;
 	
 	
 	
 	public void processChildren(NodeList children, Group parentGroup, ControlElement parentControl, ControllerClientRequestProcessor controllerClientRequestProcessor) throws Exception
 	{
 		processAbstractChildren(children, parentGroup, parentControl, controllerClientRequestProcessor);
 	}
 	
 	public void processChildren(NodeList children, Group parentGroup, ControlElement parentControl, ServerControllerResponse serverControllerResponse) throws Exception
 	{
 		processAbstractChildren(children, parentGroup, parentControl, serverControllerResponse);
 	}
 	
 	private void processAbstractChildren(NodeList children, Group parentGroup, ControlElement parentControl, Object clientRequestOrServerResponse) throws Exception
 	{
 		
 		Object originalContext = getContext();
 		
 		for (int currentNode = 0; currentNode < children.getLength(); currentNode++)
 		{
 
 			Node node = children.item(currentNode);
 			if (node.getNodeType() != Node.ELEMENT_NODE)
 			{
 				continue;
 			}
 
 			Element controlElementDeclaration = (Element) node;
 			
 			if (controlElementDeclaration.hasAttribute("DoNotProcess"))
 			{
 			    continue;
 			}
 			
			//skip ControllerRequest element as it's just here for xpath processing, but not control processing. 
			if (controlElementDeclaration.getLocalName().equals("ControllerRequest"))
			{
			    continue;
			}
			
 			ControlElement controlElement = getControlElementInstanceForLocalName(controlElementDeclaration.getLocalName());
 			if (controlElement == null)
 			{
 			    
 			    //see if we are a module reference
 			    Element moduleElement = ModuleProvider.getModuleElement(controlElementDeclaration.getLocalName());
 			    if (moduleElement != null)
 			    {
 
 
 			        if (moduleElement.hasAttribute("name") == false)
 			        {
 			            moduleElement.setAttribute("name", controlElementDeclaration.getLocalName());
 			        }
 
 			        CapoApplication.logger.log(Level.FINE, "found '"+controlElementDeclaration.getLocalName()+"' module.");
 			        controlElement = getControlElementInstanceForLocalName(moduleElement.getLocalName());
 			        if (controlElement != null)
 			        {
 			            NamedNodeMap attributeNodeMap = controlElementDeclaration.getAttributes();
 			            for(int index = 0; index < attributeNodeMap.getLength(); index++)
 			            {
 			                moduleElement.setAttribute(attributeNodeMap.item(index).getLocalName(), attributeNodeMap.item(index).getNodeValue());
 			            }
 			            NodeList childNodes = controlElementDeclaration.getChildNodes();
 			            boolean isFirstChild = true;
 			            Element moduleDataElement = moduleElement.getOwnerDocument().createElement("server:moduleData");
 			            moduleDataElement.setAttribute("DoNotProcess", "true");
 			            for(int index = 0; index < childNodes.getLength(); index++)
 			            {
 			                Node childNode = childNodes.item(index);
 			                if (childNode instanceof Element)
 			                {
 			                    if (isFirstChild)
 			                    {
 			                        moduleElement.appendChild(moduleDataElement);
 			                    }
 			                    moduleDataElement.appendChild(moduleElement.getOwnerDocument().importNode(childNode, true));
 			                }
 			            }
 			            
 			            controlElementDeclaration = moduleElement;  
 			            //XPath.dumpNode(controlElementDeclaration, System.err);    
 			        }
 
 			    }
 			}
 			if (controlElement != null)
 			{	
 				setContext(controlElement);
 				
 				try
 				{
 					String name = controlElementDeclaration.getAttribute("name");
 					if (name != null && name.isEmpty() == false)
 					{
 						CapoApplication.logger.log(Level.FINE, "Setting "+controlElementDeclaration.getLocalName()+"."+name+" to "+XPath.getXPath(controlElementDeclaration));
 						parentGroup.set(controlElementDeclaration.getLocalName()+"."+name, XPath.getXPath(controlElementDeclaration));
 					}
 					if (clientRequestOrServerResponse instanceof ControllerClientRequestProcessor)
 					{
 						((ServerSideControl) controlElement).init(controlElementDeclaration,this,parentGroup,(ControllerClientRequestProcessor) clientRequestOrServerResponse);
 						Enum[] missingAttributes = controlElement.getMissingAttributes();
 						if (missingAttributes.length > 0)
 						{
 							throw new MissingAttributeException(missingAttributes,controlElementDeclaration);		
 						}
 						((ServerSideControl) controlElement).processServerSideElement();
 						
 					}
 					else if (clientRequestOrServerResponse instanceof ServerControllerResponse)
 					{
 						((ClientSideControl) controlElement).init(controlElementDeclaration,this,parentGroup,(ServerControllerResponse) clientRequestOrServerResponse);
 						Enum[] missingAttributes = controlElement.getMissingAttributes();
 						if (missingAttributes.length > 0)
 						{
 							throw new MissingAttributeException(missingAttributes,controlElementDeclaration);	
 						}
 						((ClientSideControl) controlElement).processClientSideElement();
 					}
 					if (parentGroup != null)
 					{
 						parentGroup.closeResourceDescriptors(LifeCycle.REF);
 					}
 					controlElement.destroy();
 					setContext(originalContext);
 				}
 				catch (Exception exception)
 				{
 					CapoApplication.logger.log(Level.WARNING, "Couldn't process "+XPath.getXPath(node));
 					setContext(originalContext);
 					throw exception;
 				}
 			}
 			else
 			{
 			    
 			    CapoApplication.logger.log(Level.WARNING, "Don't know what to do with: "+XPath.getXPath(controlElementDeclaration));
 			    
 			}
 		}
 		if (this instanceof GroupElement && parentGroup != null)
 		{			
 			parentGroup.closeResourceDescriptors(LifeCycle.GROUP);			
 		}
 		//cleanup this element
 		destroy();
 	}
 	
 	@Override
 	public void init(Element controlElementDeclaration, ControlElement parentControlElement, Group parentGroup, ControllerClientRequestProcessor controllerClientRequestProcessor) throws Exception
 	{
 		this.parentGroup = parentGroup;		
 		this.originalControlElementDeclaration = controlElementDeclaration;
 		if (parentGroup != null)
 		{
 			this.controlElementDeclaration = (Element) parentGroup.replaceVarsInAttributeValues(controlElementDeclaration);
 		}
 		else
 		{
 			this.controlElementDeclaration = controlElementDeclaration;
 		}
 		this.controllerClientRequestProcessor = controllerClientRequestProcessor;
 		this.parentControlElement = parentControlElement;
 	}
 	
 	
 	
 	@Override
 	public Group getParentGroup()
 	{
 		return parentGroup;
 	}
 	
 	protected void setParentGroup(Group parentGroup)
 	{
 		this.parentGroup = parentGroup;
 	}
 	
 	@Override
 	public ControlElement getParentControlElement()
 	{
 		return this.parentControlElement;
 	}
 	
 	public void setParentControlElement(ControlElement parentControlElement)
 	{
 		this.parentControlElement = parentControlElement;
 	}
 	
 	@Override
 	public Element getOriginalControlElementDeclaration()
 	{
 		return this.originalControlElementDeclaration;
 	}
 	
 	public void setOriginalControlElementDeclaration(Element originalControlElementDeclaration)
 	{
 		this.originalControlElementDeclaration = originalControlElementDeclaration;
 	}
 	
 	@Override
 	public Element getControlElementDeclaration()
 	{
 		return this.controlElementDeclaration;
 	}
 	
 	public void setControlElementDeclaration(Element controlElementDeclaration)
 	{
 		this.controlElementDeclaration = controlElementDeclaration;
 	}
 	
 	
 	
 	@Override
 	public ControllerClientRequestProcessor getControllerClientRequestProcessor()
 	{
 		return this.controllerClientRequestProcessor;
 	}
 	
 	@Override
 	public void destroy() throws Exception
 	{		
 		
 		CapoApplication.logger.log(Level.FINE, "Destroying: "+getElementName());		
 	}
 	
 	public String getAttributeValue(Enum attributeEnum)
 	{	
 		if (getControlElementDeclaration() == null)
 		{
 			return "";
 		}
 		else
 		{
 			return getControlElementDeclaration().getAttribute(attributeEnum.toString());
 		}		
 	}
 	
 	public boolean getAttributeBooleanValue(Enum attributeEnum)
 	{	
 		if (getControlElementDeclaration() != null)
 		{
 			if (getControlElementDeclaration().hasAttribute(attributeEnum.toString()))
 			{
 				if (getAttributeValue(attributeEnum).equalsIgnoreCase("true"))
 				{
 					return true;
 				}				
 			}
 		}			
 		return false;
 	}
 	
 	
 	public boolean getBooleanValue(Enum name)
 	{
 		if (getValue(name) != null)
 		{			
 			if (getValue(name).equalsIgnoreCase("true"))
 			{
 				return true;
 			}							
 		}			
 		return false;
 	}
 	
 	public String getValue(Enum name)
 	{
 		if (getParentGroup() != null)
 		{
 			return getParentGroup().getVarValue(name.toString());
 		}
 		else
 		{
 			return "";
 		}
 	}
 	
 	@Override
 	public String getElementName()
 	{
 		return this.getClass().getAnnotation(ControlElementProvider.class).name();
 	}
 	
 	@Override
 	public Enum[] getMissingAttributes()
 	{
 		Vector<Enum> missingAttributeVector = new Vector<Enum>();
 		Enum[] requiredAttributes = getRequiredAttributes();
 		for (Enum requiredAttribute : requiredAttributes)
 		{
 			if (getControlElementDeclaration().hasAttribute(requiredAttribute.toString()) == false)
 			{
 				missingAttributeVector.add(requiredAttribute);
 				
 			}
 		}		
 		return missingAttributeVector.toArray(new Enum[]{});
 	}
 	
 	public void setContext(Object context)
 	{
 		if (Thread.currentThread() instanceof ContextThread)
 		{
 			ContextThread contextThread = (ContextThread) Thread.currentThread();
 			contextThread.setContext(context);
 			
 		}
 	}
 	
 	public Object getContext()
 	{
 		if (Thread.currentThread() instanceof ContextThread)
 		{
 			ContextThread contextThread = (ContextThread) Thread.currentThread();
 			return contextThread.getContext();
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	public Node getContextNode()
 	{
 		Object context = getContext();
 		Node contextNode = null;
 		if (context != null && context instanceof ControlElement)
 		{
 			contextNode = ((ControlElement)context).getControlElementDeclaration();
 		}
 		else if (context != null && context instanceof Node)
 		{
 			contextNode = (Node) context;
 		}
 		return contextNode;
 	}
 	
 	public ControlElement getContextControlElement()
 	{
 		Object context = getContext();
 		
 		if (context != null && context instanceof ControlElement)
 		{
 			return  ((ControlElement)context);
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	public String getControlElementMD5() throws Exception
 	{
 		return XPath.getElementMD5(getControlElementDeclaration());	
 	}
 	
 	
 	
 }
