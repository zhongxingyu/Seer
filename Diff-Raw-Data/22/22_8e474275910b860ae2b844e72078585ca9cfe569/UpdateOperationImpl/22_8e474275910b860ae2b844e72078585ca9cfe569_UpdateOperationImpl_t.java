 /*******************************************************************************
  * Copyright (C) 2010 Ansgar Gerlicher
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 	http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Stuttgart, Hochschule der Medien: http://www.mi.hdm-stuttgart.de/mmb/
  * Collaborative Editing Framework or XML:
  * http://sourceforge.net/projects/cefx/
  * 
  * Dresden, University of Technology, Faculty of Computer Science
  * Computer Networks Group: http://www.rn.inf.tu-dresden.de
  * mobilis project: http://mobilisplatform.sourceforge.net
  ******************************************************************************/
 /**
  * This sourcecode is part of the Collaborative Editing Framework for XML (CEFX).
  * Copyright 2007 Ansgar Gerlicher.
  * @author Ansgar Gerlicher
  * @author Michael Voigt
  */
 package de.hdm.cefx.concurrency.operations;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.awareness.events.AwarenessEventTypes;
import de.hdm.cefx.awareness.events.EventPropagator;
 import de.hdm.cefx.exceptions.NodeNotFoundException;
 import de.hdm.cefx.util.DOM3Methods;
 
 /**
  * Implementing class of the UpdateOperation interface.
  *
  * @author Ansgar Gerlicher
  *
  */
 @SuppressWarnings("serial")
 public class UpdateOperationImpl implements UpdateOperation {
 	private final String targetNodeId;
 
 	private NodeModification orgNode;
 
 	private StateVector stateVector;
 
 	private int clientId;
 
 	private String clientName;
 
 	private int type;
 
 
 	private boolean undone = false;
 
 	private boolean discarded = false;
 
 	private final UpdateOperations disOperation;
 
 	private OperationID opID;
 
 	/**
 	 * Class constructor.
 	 *
 	 * @param trgtNodeId
 	 *            the UUID of the node to update.
 	 * @param sv
 	 *            the state vector of the operation.
 	 * @param name
 	 *            the name of the issueing client.
 	 * @param identifier
 	 *            the id of the issueing client.
 	 */
 	protected UpdateOperationImpl(UpdateOperations disOperation, final StateVector sv, final String name, int identifier) {
 		undone = false;
 		discarded = false;
 		targetNodeId = new String(disOperation.getNodePosition().getParentNodeId());
 		stateVector = (StateVector) sv.clone();
 		clientName = name;
 		clientId = identifier;
 		type = UPDATE;
 		disOperation.setParent(this);
 		this.disOperation=disOperation;
 		opID=new OperationID(this);
 	}
 
 	public String getUniqueNodePositionId() {
 		if (disOperation.getNodePosition()==null) {
 			return "";
 		}
 		String result="";
 		NodePosition p=disOperation.getNodePosition();
 		if (disOperation.getNodeType()==UpdateOperations.TEXT) {
 			if (p.getFixNodeId() == null || (p.getFixNodeId().equals("null")) || ("".equals(p.getFixNodeId()))) {
 				result=p.getParentNodeId();
 			} else {
 				result=p.getFixNodeId()+'_'+p.getRelativeInsertPosition();
 			}
 		} else {
 			result=p.getParentNodeId()+'_'+disOperation.getAttributName();
 		}
 		return result;
 	}
 
 	public UpdateOperations getDISOperation() {
 		return disOperation;
 	}
 
 	public String getTargetText(ExecutionContext context) {
 		if (disOperation==null) return null;
 		if (disOperation.getNodeType()==UpdateOperations.TEXT) {
 			return getTargetNodeText(context);
 		} else {
 			return getTargetAttribute(context);
 		}
 	}
 
 	private String getTargetAttribute(ExecutionContext context) {
 		String result="";
 		try {
 			Node node = context.getNodeForId(this.targetNodeId);
 			NamedNodeMap nnm = node.getAttributes();
 			Attr a=(Attr) nnm.getNamedItem(disOperation.getAttributName());
 			result=a.getValue();
 		} catch (NodeNotFoundException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	private String getTargetNodeText(ExecutionContext context) {
 		Text t=getTargetW3CTextNode(context,disOperation.getNodePosition());
 		if (t==null) {
 			return null;
 		} else {
 			return DOM3Methods.getTextContent(t);
 		}
 	}
 
 	private Text getTargetW3CTextNode(ExecutionContext context,NodePosition np) {
 		if (np==null) return null;
 		try {
 			Node node = context.getNodeForId(this.targetNodeId);
 			//node.normalize(); // TODO node normalize needed?
 			String fixNodeId=np.getFixNodeId();
 			if (fixNodeId == null || fixNodeId.equals("null") || ("".equals(fixNodeId))) {
 				if (!node.hasChildNodes()) {
 					return null;
 				} else {
 					NodeList nl = node.getChildNodes();
 					Node tmp=nl.item(0);
 					if (tmp.getNodeType()==Node.TEXT_NODE) {
 						return (Text)tmp;
 					} else {
 						return null;
 					}
 				}
 			} else {
 				Node fixNode=context.getNodeForId(fixNodeId);
 				if (fixNode==null) {
 					return null;
 				}
 				if (!fixNode.getParentNode().equals(node)) {
 					return null;
 				}
 
 				NodeList nl = node.getChildNodes();
 				int pos=-2;
 				int  ii=0;
 				while ((pos==-2) && (ii<nl.getLength())) {
 					if (nl.item(ii).equals(fixNode)) {
 						pos=ii;
 					}
 					ii=ii+1;
 				}
 
 				Node tmp=null;
 				if (np.getRelativeInsertPosition()==NodePosition.INSERT_BEFORE) {
 					pos=pos-1;
 				} else {
 					pos=pos+1;
 				}
 				if ((pos>=0) && (pos<nl.getLength())) {
 					tmp=nl.item(pos);
 				}
 				if ((tmp!=null) && (tmp.getNodeType()==Node.TEXT_NODE)) {
 					return (Text)tmp;
 				} else {
 					Text newText=context.getLocalDoc().createTextNode("");
 					if (np.getRelativeInsertPosition()==NodePosition.INSERT_BEFORE) {
 						node.insertBefore(newText, fixNode);
 					} else {
 						node.appendChild(newText);
 					}
 					return newText;
 				}
 
 			}
 		} catch (NodeNotFoundException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 	public void setTargetText(ExecutionContext context,String text) {
 		if (disOperation==null) return;
 		if (disOperation.getNodeType()==UpdateOperations.TEXT) {
 			setTargetNodeText(context,text);
 		} else {
 			setTargetAttribute(context,text);
 		}
 	}
 
 	private void setTargetAttribute(ExecutionContext context,String text) {
 		try {
 			Node node = context.getNodeForId(this.targetNodeId);
 			NamedNodeMap nnm = node.getAttributes();
 
 			Attr a=null;
 			a=(Attr)nnm.getNamedItem(disOperation.getAttributName());
 			if (a==null) {
 			  a=context.getLocalDoc().createAttribute(disOperation.getAttributName());
 			}
 			//a.setNodeValue(text);
 			a.setValue(text);
 			nnm.setNamedItem(a);
 		} catch (NodeNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void setTargetNodeText(ExecutionContext context, String text) {
 		Text t=getTargetW3CTextNode(context,disOperation.getNodePosition());
 		if (t!=null) {
 			// Android 1.5 does not implement setNodeValue() or setTextContent()!
 			DOM3Methods.setTextContent(t, text);
 		}
 	}
 	
 	/**
 	 * Checks whether the given <code>fixNode</code> has a sibling
 	 * (before or are after depending on value of <code>beforeAfter</code>)
 	 * of type {@link Text} Node. If <code>fixNode</code> is <code>null</code> it is checked
 	 * whether the parent's last child is a text node. If there is no text node at the
 	 * specified position a new text node is inserted at the desired position.
 	 * @param parent
 	 * @return the found or created {@link Text} node
 	 */
 	private Text findOrCreateTextNodeAtPosition(Element parent, Element fixNode, int beforeAfter, Document doc) {
 		// TODO: also add code to auto add a text node at the given position if desired
 		Text content = doc.createTextNode("");
 		if (fixNode == null){
 			Node last = parent.getLastChild();
 			if (last != null && last.getNodeType() == Node.TEXT_NODE) {
 				return (Text) last;
 			} else {
 				parent.appendChild(content);
 			}
 		}else if (beforeAfter == NodePosition.INSERT_BEFORE) {
 			Node prev = fixNode.getPreviousSibling();
 			if (prev != null && prev.getNodeType() == Node.TEXT_NODE){
 				return (Text) prev;
 			} else {
 				parent.insertBefore(content, fixNode);
 			}
 		} else if (beforeAfter == NodePosition.INSERT_AFTER){
 			Node next = fixNode.getNextSibling();
 			if (next != null && next.getNodeType() == Node.TEXT_NODE){
 				return (Text) next;
 			} else {
 				parent.insertBefore(content, fixNode.getNextSibling());
 			}
 		}
 		return content;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#execute(de.hdm.cefx.concurrency.operations.ExecutionContext)
 	 */
 	public synchronized boolean execute(ExecutionContext context) {
 		
 		if (getDISOperation() instanceof UpdateInsertOperation && getDISOperation().getNodeType() == UpdateOperations.TEXT) {
 			
 			// check if target node has child of type text at the specified position
 			UpdateInsertOperation dis = (UpdateInsertOperation) getDISOperation();
 			Element fixNode = null;
 			String fixNodeId = dis.getNodePosition().getFixNodeId();
 			if (fixNodeId != null && !fixNodeId.equals("null") && !fixNodeId.equals("")) {
 				try {
 					fixNode = (Element) context.getNodeForId(fixNodeId);
 				} catch (NodeNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			Element parentNode  = null;
 			String parentNodeId = dis.getNodePosition().getParentNodeId();
 			if (parentNodeId != null && !parentNodeId.equals("null") && !parentNodeId.equals("")) {
 				try {
 					parentNode = (Element) context.getNodeForId(parentNodeId);
 				} catch (NodeNotFoundException e){
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			findOrCreateTextNodeAtPosition(parentNode,
 					fixNode,
 					dis.getNodePosition().getRelativeInsertPosition(),
 					context.getLocalDoc());
 		}
 		
 		
 		context.refreshNodeMap();
 
 		if (discarded)
 			return true;
 		Node node = null;
 		try {
 			node = context.getNodeForId(this.targetNodeId);
 
 		} catch (NodeNotFoundException e) {
 
 			e.printStackTrace();
 			return false;
 		}
 		if (node != null) {
 			//node.normalize(); // TODO node normalize needed?
 			//store original node values
 			orgNode = new NodeModification(node);
 //			if (disOperation instanceof UpdateInsertOperation){
 //				UpdateInsertOperation upInsOp = (UpdateInsertOperation) disOperation;
 //				if (disOperation.getNodeType() == Node.TEXT_NODE){
 //					NodePosition np = upInsOp.getNodePosition();
 //					String textContent = node.getTextContent();
 //					TextUpdate up = new TextUpdate(np.parentNodeId, 
 //							context.getNodeId(node.getPreviousSibling()), 
 //							textContent.substring(0, upInsOp.getTextPos()) + upInsOp.getText() + textContent.substring(upInsOp.getTextPos(), textContent.length()), 
 //							context.getNodeId(node.getNextSibling()));
 //					List<TextUpdate> textUpdates = new ArrayList<TextUpdate>();
 //					textUpdates.add(up);
 //					modifyTextNodes(textUpdates, node, context);
 //				}
 //			}
 			context.refreshNodeMap();
 			undone = false;
 			return true;
 		} else {
 			// node probably was deleted
 			return false;
 		}
 	}
 
 	/**
 	 * Modifies the text nodes as specified in the NodeModification.
 	 *
 	 * @param texts
 	 *            the list of texts.
 	 * @param node
 	 *            the node to be modified.
 	 * @param context
 	 *            the context of execution.
 	 */
 	private void modifyTextNodes(List texts, Node node, ExecutionContext context) {
 		//node.normalize(); // TODO node normalize needed?
 		for (Object object : texts) {
 			TextUpdate tu = (TextUpdate) object;
 			NodePosition npos = tu.getNodePosition();
 			Text t=getTargetW3CTextNode(context,npos);
 			if (t!=null) {
 				// Android 1.5 does not implement setNodeValue() or setTextContent()!
 				DOM3Methods.setTextContent(t, tu.getTextValue());
 			}
 		}
 		//node.normalize(); // TODO node normalize needed?
 	}
 
 	/**
 	 * Modifies the attributes of the given node as specified in the
 	 * NodeModification.
 	 *
 	 * @param attributes
 	 *            the list of attributes.
 	 * @param node
 	 *            the node to be updated.
 	 * @param context
 	 *            the context of execution.
 	 */
 	private void modifiyAttributes(HashMap attributes, Node node, ExecutionContext context) {
 		Element ele = (Element) node;
 		NamedNodeMap nnm = ele.getAttributes();
 		Set keys = attributes.keySet();
 		for (Object object : keys) {
 			AttributeIdentifier aident = (AttributeIdentifier) object;
 			Attr a=null;
 			a=(Attr)nnm.getNamedItem(aident.getLocalName());
 			if (a==null) {
 			  a=context.getLocalDoc().createAttribute(aident.getLocalName());
 			}
 			//DOM3Methods.setTextContent(a, (String) attributes.get(object));
 			a.setValue((String) attributes.get(object));
			
			try {
				nnm.setNamedItemNS(a);
			} catch (DOMException e) {
				/* 
				 * TODO: dirty workaround for ditching the infamous DOMException.INUSE_ATTRIBUTE_ERR when undoing an operation.
				 * The problem here is, that a.ownerElement has to be null, so that the attribute can be attached to the node. Otherwise
				 * the somewhat dump Android ElementImpl implementation thinks, that the attribute is already attached to another element.
				 * Instead the Android ElementImpl implementation should check for (a.ownerElement != null && !a.ownerElement.equals(this))
				 * which should trigger the DOMException.
				 * So the solution would be either to use an own implementation of ElementImpl or set a.ownerElement to null (which not that easy,
				 * as there aren't any setters to achieve this.
				 */
				e.printStackTrace();
			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#getTargetId()
 	 */
 	public String getTargetId() {
 		return targetNodeId;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#undo(de.hdm.cefx.concurrency.operations.ExecutionContext)
 	 */
 	public boolean undo(ExecutionContext context) {
 
 		disOperation.undoTransformation();
 
 		if (discarded)
 			return true;
 		try {
 			boolean success = undoModifyNode(context);
 			// no exception thrown, node insertion seemed to have worked
 			// so insert node in node map
 			if (success) {
 				context.refreshNodeMap();
 				undone = true;
 				return true;
 			} else {
 				return false;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Undoes a node modification and establishes the state of the node before
 	 * the modification.
 	 *
 	 * @param context
 	 *            the context of execution.
 	 * @return true if the operation was successful.
 	 */
 	private boolean undoModifyNode(ExecutionContext context) {
 		context.refreshNodeMap();
 		Node node = null;
 		try {
 			node = context.getNodeForId(this.targetNodeId);
 		} catch (NodeNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 		if (node != null) {
 			if (orgNode == null) {
 				System.err.println("!!!!!!!!! ERROR OrgNode is not set?! UpdateOperationImpl.undoModifyNode()");
 				return false;
 			}
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				HashMap attributes = orgNode.getAttributes();
 				List texts = orgNode.getTextNodes();
 				modifiyAttributes(attributes, node, context);
 				modifyTextNodes(texts, node, context);
 			}
 			return true;
 		} else {
 			// node probably was deleted
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return "U(" + clientName + " " + this.getStateVector() + "," + hashCode() + " discarded=" + discarded + " undone=" + undone + " Trgt=" + targetNodeId + " )";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#cloneOperation()
 	 */
 	public Operation cloneOperation() {
 		return new UpdateOperationImpl(disOperation, stateVector, clientName, clientId);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#getStateVector()
 	 */
 	public final StateVector getStateVector() {
 		return stateVector;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#setStateVector(de.hdm.cefx.concurrency.operations.StateVector)
 	 */
 	public void setStateVector(final StateVector sv) {
 
 		stateVector = (StateVector) sv.clone();
 		System.out.println("UpdateOperationImpl.setStateVector() " + sv + " statevector: " + stateVector);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#getClientId()
 	 */
 	public int getClientId() {
 
 		return clientId;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#getClientName()
 	 */
 	public String getClientName() {
 		return clientName;
 	}
 
 	/**
 	 * Retrieves the UUID of the update modification target node.
 	 *
 	 * @return UUID of the target node.
 	 */
 	public String getTargetNodeId() {
 		return targetNodeId;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#getType()
 	 */
 	public int getType() {
 		return type;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#isDiscarded()
 	 */
 	public boolean isDiscarded() {
 		return discarded;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#setDiscarded(boolean)
 	 */
 	public void setDiscarded(boolean d) {
 		discarded = d;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.Operation#setClientId(int)
 	 */
 	public void setClientId(int id) {
 		this.clientId = id;
 
 	}
 
 	public OperationID getOperationID() {
 		return opID;
 	}
 
 	public void setOperationID(OperationID oid) {
 		opID=oid;
 	}
 }
