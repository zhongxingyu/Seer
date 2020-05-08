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
  * @author Dirk Hering
  */
 package de.hdm.cefx.concurrency;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Vector;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.hdm.cefx.client.CEFXClient;
 import de.hdm.cefx.client.net.CEFXSession;
 import de.hdm.cefx.concurrency.operations.ExecutionContext;
 import de.hdm.cefx.concurrency.operations.Operation;
 import de.hdm.cefx.concurrency.operations.OperationExecutor;
 import de.hdm.cefx.concurrency.operations.StateVector;
 import de.hdm.cefx.concurrency.operations.UpdateOperationImpl;
 import de.hdm.cefx.concurrency.operations.UpdateOperationsProcessor;
 import de.hdm.cefx.exceptions.NodeNotFoundException;
 import de.hdm.cefx.exceptions.OperationExecutionException;
 import de.hdm.cefx.util.CEFXUtil;
 
 /**
  * The AbstractConcurrencyControllerImpl class implements all methods of the
  * ConcurrencyController interface except the method
  * <code>setOperationExecutor(OperationExecutor impl)</code>. This method is
  * implemented by its subclass, the OrderingConcurrencyControllerImpl class. All
  * methods of the ExecutionContext interface are implemented completely by the
  * AbstractConcurrencyControllerImpl class.
  *
  * @author Ansgar Gerlicher
  * @author Michael Voigt
  * @author Dirk Hering
  *
  */
 public abstract class AbstractConcurrencyControllerImpl implements ConcurrencyController, ExecutionContext, Runnable {
 
 	// static fields
 	static protected final int RESOLVABLE = 0;
 	static protected final int UNRESOLVABLE = 1;
 	static protected final int NOCONFLICT = 2;
 	static protected final Logger log = Logger.getLogger("ConcurrencyControllerImpl");
 
 	private final ReentrantLock lock = new ReentrantLock();
 
 	protected Document doc;
 	private Map<String, Node> nodeMap;
 	protected HashMap<Operation, Node> deletedNodesMap;
 	private StateVector stateVector;
 	private LinkedBlockingQueue<Operation> operationQueue;
 	private Thread thread;
 	private boolean isRunning;
 
 	//TODO Debuginfos =>executedOperationList löschen
 	private List<Operation> executedOperationList;
 
 	// protected fields - visible to subclass
 
 	protected OperationExecutor context;
 
 	protected int operationcount;
 
 	protected Stack<Operation> historyBuffer;
 
 	protected Hashtable<Integer, StateVector> lastStateVectors;
 	protected CEFXSession                     session;
 	boolean                                   collaborationReady = false;
 	protected Stack<Operation>                lateJoinBuffer;
 	protected final ReentrantLock             lateJoinBufferLock = new ReentrantLock();
 
 	/*
 	 * Begin ConcurrencyController interface methods
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.ConcurrencyController#executeLocalOperation(de.hdm.cefx.concurrency.operations.Operation)
 	 */
 	public synchronized boolean executeLocalOperation(final Operation operation) {
 		updateLastStateVectors(operation);
 
 		boolean operationExecutedSuccessfully = false;
 		// check if operation is supported
 		if (!isOperationSupported(operation)) {
 			context.notifyOfNotSupportedOperation(operation);
 			return operationExecutedSuccessfully;
 		}
 
 		try {
 			System.out.println("Current local state: " + stateVector);
 			operationExecutedSuccessfully = operation.execute(this);
 			if (operation.getType()==Operation.UPDATE) {
 				UpdateOperationImpl uo=(UpdateOperationImpl)operation;
 				String text=uo.getTargetText(this);
 				if (text!=null) {
 					text=UpdateOperationsProcessor.process(null, uo, text);
 					uo.setTargetText(this, text);
 				}
 			}
 		} catch (OperationExecutionException e) {
 			e.printStackTrace();
 		}
 		if (operationExecutedSuccessfully) {
 
 			// first update the local state vector
 			updateStateVector(operation.getClientId());
 			// now update the state Vector of the operation
 			operation.setStateVector((StateVector) getStateVector().clone());
 			// add operation to history buffer
 			historyBuffer.add(operation);
 			// increase operation count for debugging
 			operationcount++;
 			// propagate the operation to the other sites
			propagateOperation(operation);

 		} else {
 			// Error occured, debug output.
 			log.log(Level.SEVERE, "Operation: " + operation + " could not be executed. SV: " + getStateVector());
 		}
 		System.out.println("Current local state: " + stateVector);
 		return operationExecutedSuccessfully;
 	}
 
 	private int getMinState(int id,Vector<Integer> ids) {
 		int ii;
 		int min=0;
 		boolean firstVal=true;
 		for (ii=0; ii<ids.size(); ii++) {
 			if (lastStateVectors.containsKey(ids.get(ii))) {
 				StateVector sv=lastStateVectors.get(ids.get(ii));
 				if (sv.containsKey(id)) {
 					int state=sv.getState(id);
 					if (firstVal) {
 						min=state;
 						firstVal=false;
 					} else {
 						if (state<min) {
 							min=state;
 						}
 					}
 				} else {
 					return 0;
 				}
 			} else {
 				return 0;
 			}
 		}
 
 		return min;
 	}
 
 	protected void cleanHistoryBuffer() {
 		if (session==null) return;
 
 		StateVector m=new StateVector();
 
 		HashMap<String, CEFXClient> clients=session.getClientMap();
 		Set<String> set=clients.keySet();
 		Vector<Integer> ids=new Vector<Integer>();
 
 		Iterator<String> it = set.iterator();
 		while ( it.hasNext() == true ) {
 			String key=it.next();
 			ids.add(new Integer(clients.get(key).getID()));
 		}
 
 		int ii;
 		for (ii=0; ii<ids.size(); ii++) {
 			int id=ids.elementAt(ii);
 			int min=getMinState(id,ids);
 			m.put(id, min);
 		}
 		m.put(0, 0);
 
 		int hbsize=historyBuffer.size();
 		int count=0;
 		while ((count<hbsize) && (!UpdateOperationsProcessor.isTransformationNeeded(historyBuffer.elementAt(count).getStateVector(),m))) {
 			count=count+1;
 		}
 
 		for (ii=0; ii<count; ii++) {
 			Operation o=historyBuffer.elementAt(0);
 			if (deletedNodesMap.containsKey(o)) {
 				deletedNodesMap.remove(o);
 			}
 			historyBuffer.remove(0);
 		}
 
 	}
 
 	protected void updateLastStateVectors(Operation o) {
 		if (o==null) return;
 		lastStateVectors.put(new Integer(o.getClientId()), o.getStateVector());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.ConcurrencyController#executeRemoteOperation(de.hdm.cefx.concurrency.operations.Operation)
 	 */
 	public boolean executeRemoteOperation(Operation o) {
 		updateLastStateVectors(o);
 
 		lateJoinBufferLock.lock();
 
 		if (isCollaborationReady()) {
 			boolean executedSuccessfully = false;
 			// here we have to check first, if the operation is ready to execute
 			if (isCausallyReady(o)) {
 
 				// Log.out(Log.ALL, this, ".remoteOperation() " + o);
 				System.out.println("Initialise executeRemoteOperation()" + this.getName() + " id=" 
 						+ getClientId() + ":" + o + " localstate: " + stateVector);
 				executedSuccessfully = prepareExecution(o);
 				if (!executedSuccessfully) {
 
 					// could not execute operation at this time put in pending
 					// operations list
 					// pendingOperations.add(o);
 					operationQueue.offer(o);
 					log.log(Level.SEVERE, this.getName() + " id=" + getClientId() + " could not execute operation: " + o + " from client: " + o.getClientName());
 				}
 				cleanHistoryBuffer();
 				//log.info(System.currentTimeMillis() + ": Finished executeRemoteOperation()" + this.getName() + " id=" + getClientId() + ":" + o + " localstate: " + stateVector);
 				return executedSuccessfully;
 			} else {
 				log.log(Level.SEVERE, this.getName() + " id=" + getClientId() + " not causally ready! History Buffer: " + 
 						historyBuffer + " \n " + " Cannot execute remote operation: " + o + " from: "
 						+ o.getClientName() + " - not causally ready: localState: " + stateVector + 
 						" operationState: " + o.getStateVector());
 
 				// FIXME OperationImpl was not ready to execute put OperationImpl in Stack for pending operations
 				// pendingOperations.add(o);
 
 				operationQueue.offer(o);
 				return true;
 			}
 		} else {
 			lateJoinBuffer.add(o);
 		}
 		lateJoinBufferLock.unlock();
 
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.ConcurrencyController#getStateVector()
 	 */
 	public final StateVector getStateVector() {
 		return stateVector;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.ConcurrencyController#getHistoryBuffer()
 	 */
 	public final Collection<Operation> getHistoryBuffer() {
 		return historyBuffer;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.ConcurrencyController#setDocument(org.w3c.dom.Document)
 	 */
 	public void setDocument(Document localDoc) {
 		Node root = localDoc.getDocumentElement();
 		doc = localDoc;
 		putNodesInHash(root);
 	}
 
 	/*
 	 * End of ConcurrencyController interface methods
 	 */
 
 	/*
 	 * Begin ExecutionContext interface methods
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.ExecutionContext#getNodeForId(java.lang.String)
 	 */
 	public synchronized Node getNodeForId(String rnodeID) throws NodeNotFoundException {
 
 		Node n = nodeMap.get(rnodeID);
 
 		if (n == null) {
 			throw new NodeNotFoundException("Node " + rnodeID + " not found in document of: " + context.getName());
 		}
 		return n;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.ExecutionContext#getNodeId(org.w3c.dom.Node)
 	 */
 	public String getNodeId(Node node) {
 		String nodeId = null;
 		NamedNodeMap nnm = node.getAttributes();
 		if (nnm != null) {
 			Attr a=(Attr)nnm.getNamedItemNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID);
 			if (a==null) {
 				String name = CEFXUtil.CEFXUID;
 				a=(Attr)nnm.getNamedItem(name);
 			}
 			if (a!=null) {
 				nodeId = a.getNodeValue();
 			}
 		}
 		return nodeId;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.ExecutionContext#getNodeFromDeletedNodeMap(de.hdm.cefx.concurrency.operations.Operation)
 	 */
 	public Node getNodeFromDeletedNodeMap(Operation operation) {
 		return deletedNodesMap.get(operation);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.ExecutionContext#refreshNodeMap()
 	 */
 	public void refreshNodeMap() {
 		nodeMap.clear();
 		putNodesInHash(doc.getDocumentElement());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.ExecutionContext#addNodeToDeletedNodeMap(de.hdm.cefx.concurrency.operations.Operation,
 	 *      org.w3c.dom.Node)
 	 */
 	public void addNodeToDeletedNodeMap(Operation operation, Node targetNode) {
 		deletedNodesMap.put(operation, targetNode);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.ExecutionContext#getLocalDoc()
 	 */
 	public Document getLocalDoc() {
 		return doc;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.ExecutionContext#existsNode(java.lang.String)
 	 */
 	public boolean existsNode(String rnodeID) {
 		Node n = (Node) nodeMap.get(rnodeID);
 		if (n == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see de.hdm.cefx.concurrency.operations.ExecutionContext#isChildOfNode(java.lang.String,
 	 *      java.lang.String)
 	 */
 	public boolean isChildOfNode(String childNode, String parentNode) {
 		Node parent = null;
 		try {
 			parent = this.getNodeForId(parentNode);
 		} catch (NodeNotFoundException e) {
 			// if node is not found, the node has been deleted
 			e.printStackTrace();
 		}
 		return isChildOfNode(childNode, parent);
 	}
 
 	/*
 	 * End of ExecutionContext interface methods
 	 */
 
 	/*
 	 * Begin abstract methods declaration These methods need to be implemented
 	 * by a subclass
 	 */
 
 	/**
 	 * A remote operation has to be processed before it can be executed. This
 	 * processing is done in the prepareExecution(Operation o) method.
 	 *
 	 * @param o
 	 *            the operation that is to be processed.
 	 * @return true if the operation could be successfully processed.
 	 */
 	protected abstract boolean prepareExecution(Operation o);
 
 	/*
 	 * End abstract methods declaration
 	 */
 
 	/*
 	 * Begin methods that can be overidden by a subclass
 	 */
 	/**
 	 * the OperationExecutor Initialises the Concurrency Controller and starts
 	 * the CC thread.
 	 *
 	 * @param client
 	 *            a reference to the OperationExecutor.
 	 */
 	protected void init(OperationExecutor client) {
 
 		/*
 		File logfile = new File("client" + client.getIdentifier() + ".log");
 		FileOutputStream fout = null;
 		try {
 			fout = new FileOutputStream(logfile);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		log.addHandler(new StreamHandler(fout, new SimpleFormatter()));
 		 */
 		executedOperationList = new ArrayList<Operation>();
 		context = client;
 		operationcount = 0;
 
 		// Operations that were executed
 		lateJoinBuffer = new Stack<Operation>();
 		historyBuffer = new Stack<Operation>();
 		nodeMap = Collections.synchronizedMap(new HashMap<String, Node>());
 		deletedNodesMap = new HashMap<Operation, Node>();
 		stateVector = new StateVector();
 
 		lastStateVectors=new Hashtable<Integer, StateVector>();
 
 		operationQueue = new LinkedBlockingQueue<Operation>(10000);
 		thread = new Thread(this);
 		thread.setPriority(Thread.MIN_PRIORITY);
 
 		thread.start();
 
 	}
 
 	/**
 	 * Undoes all operations in the history buffer starting at the end up to the
 	 * given position.
 	 *
 	 * @param pos
 	 *            the position of the last totally preceeding operation.
 	 */
 	protected void undoOperationsInHBTo(int pos) {
 		for (int i = historyBuffer.size() - 1; i >= pos; i--) {
 			Operation undoOp = historyBuffer.get(i);
 			undoAndUpdate(undoOp);
 		}
 	}
 
 	/**
 	 * Undoes an operation, updates the state vector and removes the operation
 	 * from the history buffer.
 	 *
 	 * @param undoOp
 	 *            the operation to be undone
 	 * @return true if the undo of the operation succeeded.
 	 */
 	protected boolean undoAndUpdate(Operation undoOp) {
 		if (undoOp.undo(this)) {
 			decreaseStateVector(undoOp.getClientId());
 			historyBuffer.remove(undoOp);
 			operationcount--;
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Copies the tail of the history buffer of operations into the given list.
 	 * All operations from the end of the HB up to the given position are
 	 * copied.
 	 *
 	 * @param pos
 	 *            the position up to where the HB is to be copied.
 	 * @param list
 	 *            the list to copy the operation to.
 	 * @return the list of copied operations from the tail of the HB.
 	 */
 	protected List<Operation> copyHBTail(int pos, ArrayList<Operation> list) {
 		List<Operation> orderedOps;
 		if (list == null) {
 			orderedOps = new ArrayList<Operation>();
 		} else {
 			orderedOps = list;
 		}
 		for (int i = pos; i < historyBuffer.size(); i++) {
 			Operation op = historyBuffer.get(i);
 			orderedOps.add(op);
 
 		}
 		return orderedOps;
 	}
 
 	/**
 	 * Executes an operation, updates the state vector and adds the operation to
 	 * the history buffer.
 	 *
 	 * @param operation
 	 *            the operation to execute.
 	 * @return true if the execution was successful.
 	 */
 	protected boolean executeAndUpdateState(Operation operation) {
 
 		if (executeNow(operation)) {
 			updateStateVector(operation.getClientId());
 			historyBuffer.add(operation);
 			operationcount++;
 			System.out.println("Executed operation! " + this.getName() + 
 					" id=" + getClientId() + ":" + operation + " localstate: " + stateVector);
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * Decreases the operation count in the state vector for the given client
 	 * id.
 	 *
 	 * @param clientId
 	 *            the id of the client for whose operation count is to be
 	 *            decreased.
 	 */
 	protected void decreaseStateVector(int clientId) {
 		lock.lock(); // block until condition holds
 		try {
 			int c = stateVector.getState(clientId);
 			c--;
 			stateVector.setState(clientId, c);
 		} finally {
 			lock.unlock();
 		}
 
 	}
 
 	/**
 	 * Executes an operation.
 	 *
 	 * @param o
 	 *            the operation to be executed.
 	 * @return true if the operation was successfully executed.
 	 */
 	protected boolean executeNow(Operation o) {
 		boolean executedSuccessfully = false;
 		try {
 			System.out.println("Current local state: " + stateVector);
 			executedSuccessfully = o.execute(this);
 			System.out.println("Current local state: " + stateVector);
 		} catch (OperationExecutionException e) {
 			executedSuccessfully = false;
 
 			// Operation could not be executed. Context wrong or something else
 			// happened
 			System.err.println(e.getStackTrace() + " operation: " + o);
 			e.printStackTrace();
 			return true;
 			// operationQueue.offer(o);
 			// OperationImpl will be executed again, when ready
 		}
 		return executedSuccessfully;
 	}
 
 	/**
 	 * Tries to find the parent of a node with a certain id in the document.
 	 *
 	 * @param modifiedNodeId -
 	 *            the id of the node whose parent we want to find.
 	 * @return id of the parent node or null, if no parent found or the node was
 	 *         not found.
 	 */
 	protected String getParentId(String modifiedNodeId) {
 		String parentId = null;
 		try {
 			Node node = getNodeForId(modifiedNodeId);
 			parentId = getNodeId(node);
 
 		} catch (NodeNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		return parentId;
 	}
 
 	/**
 	 * Searches for the last totally preceeding operation in the history buffer.
 	 *
 	 * @param o
 	 *            the new remote operation. Which position in the history buffer
 	 *            has it?
 	 * @return the position of the last totally preceeding operation in the
 	 *         history buffer.
 	 */
 	protected int findLastPreceedingOperaton(Operation o) {
 		int count = 0;
 		for (Operation hbop : historyBuffer) {
 			if (isTotallyPreceeding(hbop, o)) {
 				count++;
 			} else
 				return count;
 		}
 		return count;
 	}
 
 	/**
 	 * Checks the state vectors of the given operations in order to check if the
 	 * local operation is totally preceeding the remote operation.
 	 *
 	 * @param localOp
 	 *            the operation that is checked for total proceedingness.
 	 * @param remoteOperation
 	 *            the new remote operation.
 	 * @return true if the sum of the local operation's state vector is lower
 	 *         than the sum of the remote operation's SV.
 	 */
 	protected boolean isTotallyPreceeding(Operation localOp, Operation remoteOperation) {
 		return !UpdateOperationsProcessor.isTransformationNeeded(localOp.getStateVector(), remoteOperation.getStateVector());
 	}
 
 	/**
 	 * Increases the state vector for the given client id
 	 *
 	 * @param i
 	 *            the id of the client that executed an operation.
 	 */
 	protected void updateStateVector(int i) {
 		int c = stateVector.getState(i);
 		c++;
 		stateVector.setState(i, c);
 	}
 
 	/**
 	 * Propagates the given operation to the other clients in the session using
 	 * the NetworkController.
 	 *
 	 * @param operation
 	 */
 	protected void propagateOperation(final Operation operation) {
 		// before the operations can be propagated, they need to be copied
 		final Operation propOperation = operation.cloneOperation();
 		context.getNetworkController().propagateOperation(propOperation);
 	}
 
 	/**
 	 * Checks if the given operation is supported by this ConcurrencyController
 	 * implementation.
 	 *
 	 * @param o
 	 *            the operation to be checked.
 	 * @return true if the operation is supported.
 	 */
 	protected boolean isOperationSupported(Operation o) {
 		switch (o.getType()) {
 		case Operation.INSERT:
 			// is insert operation possible in the current context?
 			return checkInsertAllowed(o);
 		case Operation.UPDATE:
 			return checkModifyAllowed(o);
 		case Operation.DELETE:
 			return checkDeleteAllowed(o);
 		default:
 			break;
 		}
 		return true;
 	}
 
 	/**
 	 * Allows to retrieve the name of the client.
 	 *
 	 * @return the client's name.
 	 */
 	protected String getName() {
 		return context.getName();
 
 	}
 
 	/**
 	 * Allows to retrieve the id of the client.
 	 *
 	 * @return the client's id.
 	 */
 	protected int getClientId() {
 		return context.getIdentifier();
 	}
 
 	/*
 	 * End of methods that can be overidden by a subclass
 	 */
 
 	/*
 	 * Begin internal methods
 	 */
 	/**
 	 * Checks if the node with the given id is a child of the given parent node.
 	 *
 	 * @param childId
 	 *            the child's id.
 	 * @param parentNode
 	 *            the parent node.
 	 * @return true if the node with the given id is a child of the given parent
 	 *         node.
 	 */
 	private boolean isChildOfNode(String childId, Node parentNode) {
 		if (parentNode == null) {
 			System.out.println(this + "ConcurrencyControllerImpl.isChildOfNode() parent null");
 			log.log(Level.WARNING, "isChildOfNode(): parentNode is " + parentNode);
 			return false;
 		}
 
 		NodeList nl = parentNode.getChildNodes();
 		System.out.println(this + "ConcurrencyControllerImpl.isChildOfNode() Children: " + nl.getLength() + " node: " + parentNode.getNodeName());
 		boolean found = false;
 		for (int i = 0; i < nl.getLength(); i++) {
 			found = isMemberOf(childId, nl.item(i));
 		}
 		return found;
 
 	}
 
 	/**
 	 * Checks if the node with the given id is somewhere contained in the
 	 * subtree of the given root node.
 	 *
 	 * @param childId
 	 *            the id of the node looking for.
 	 * @param node
 	 *            the node whose subtree is to be searched.
 	 * @return true if the node with the given id is contained in the subtree of
 	 *         the given node.
 	 */
 	private boolean isMemberOf(String childId, Node node) {
 		boolean found = false;
 
 		if (node.getNodeType() == Node.ELEMENT_NODE) {
 
 			String nodeid = getNodeId(node);
 			System.out.println("ConcurrencyControllerImpl.isMemberOf() childId:" + childId + " " + node.getNodeName() + " nodeid " + nodeid);
 			if (childId.equals(nodeid)) {
 				return true;
 			} else {
 				NodeList nl = node.getChildNodes();
 				for (int i = 0; i < nl.getLength(); i++) {
 					found = isMemberOf(childId, nl.item(i));
 					if (found)
 						return true;
 				}
 			}
 		}
 		return found;
 	}
 
 	/**
 	 * Checks if the given operation is causally ready for execution.
 	 *
 	 * @param o
 	 *            the operation to be checked for causally readyness.
 	 * @return true if the operation is causally ready.
 	 */
 	private boolean isCausallyReady(Operation o) {
 		StateVector operationState = o.getStateVector();
 		/*
 		 * first test if the number of locally executed operations is n-1 of the
 		 * operations executed from the remote client this means that this
 		 * operation is causally ready for the client who initiated it.
 		 */
 		int localState = stateVector.getState(o.getClientId());
 		int remoteState = operationState.getState(o.getClientId());
 		if ((localState + 1) == remoteState) {
 			/*
 			 * now test if any other operation has been executed remotely that
 			 * this client was not aware of. In that case the client must wait
 			 * for the operations who are still missing to arrive and cannot
 			 * execute this new remote operation
 			 */
 			Iterator keyiter = stateVector.keySet().iterator();
 			while (keyiter.hasNext()) {
 				Integer key = (Integer) keyiter.next();
 				if (key.intValue() != o.getClientId()) {
 					int lstate = stateVector.getState(key.intValue());
 					int rstate = operationState.getState(key.intValue());
 					if (lstate >= rstate) {
 						// ok
 					} else {
 						/*
 						 * there is a remote operation still missing so this
 						 * operation is not causally ready
 						 */
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Builds the node map of the document containing all node's UUIDs and the
 	 * corresponding reference to the node object.
 	 *
 	 * @param root
 	 *            the root node of the document.
 	 */
 	private void putNodesInHash(Node root) {
 		if (root != null) {
 			if (root.getNodeType() == Document.ELEMENT_NODE) {
 				NodeList nl = root.getChildNodes();
 				for (int i = 0; i < nl.getLength(); i++) {
 					putNodesInHash(nl.item(i));
 				}
 				String uid = getNodeId(root);
 				if (uid != null) {
 					nodeMap.put(uid, root);
 				}
 			}
 		} else {
 			// Warning: debug output.
 			log.log(Level.WARNING, "node probably was deleted: " + root);
 		}
 	}
 
 	/**
 	 * Checks if the given delete operation is allowed to be executed.
 	 *
 	 * @param o
 	 *            the delete operation.
 	 * @return true if the delete operation's target node is contained within
 	 *         the document and thus the operation is allowed to be executed.
 	 */
 	private boolean checkDeleteAllowed(Operation o) {
 		String deleteTarget = o.getTargetId();
 		try {
 			// try to find delete target in nodemap
 			Node deleteNode = getNodeForId(deleteTarget);
 			if (deleteNode != null) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (NodeNotFoundException e) {
 			// Warning, debug output
 			log.log(Level.WARNING, " parent not found of delete target of operation: " + o);
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the target of the given update operation exists within the
 	 * document and thus the operation is allowed to be executed.
 	 *
 	 * @param o
 	 *            the operation that modies a node.
 	 * @return true if the operation can be executed.
 	 */
 	private boolean checkModifyAllowed(Operation o) {
 		String modifyTarget = o.getTargetId();
 		try {
 			// try to find modifytarget in nodemap
 			Node modifyNode = getNodeForId(modifyTarget);
 			if (modifyNode != null) {
 				return true;
 
 			} else {
 				return false;
 			}
 		} catch (NodeNotFoundException e) {
 			// Warning, debug output
 			log.log(Level.WARNING, "parent not found of modify target of operation: " + o);
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the given insert operation can be executed.
 	 *
 	 * @param o
 	 *            the insert operation to be executed.
 	 * @return true if the parent of the inserttarget exists within the document
 	 *         and thus the operation is allowed to be executed.
 	 */
 	private boolean checkInsertAllowed(Operation o) {
 		String insertTarget = o.getTargetId();
 		try {
 			// try to find parent of insert in nodemap
 			Node parent = getNodeForId(insertTarget);
 			if (parent != null) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (NodeNotFoundException e) {
 			// Warning, debug output
 			log.log(Level.WARNING, "parent not found of insert operation target: " + o);
 		}
 		return false;
 	}
 
 	/*
 	 * End of internal methods
 	 */
 
 	/*
 	 * Begin Runnable interface methods
 	 */
 	/* (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 		isRunning = true;
 
 		check: while (isRunning) {
 			Thread.yield();
 
 			Operation o;
 
 			o = operationQueue.poll();
 			if (o == null) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				Thread.yield();
 				continue check;
 
 			}
 			if (o.getClientId() == context.getIdentifier()) {
 				// if (isOperationSupported(o)) {
 				if (!executeLocalOperation(o)) {
 					// Error, debug output.
 					log.log(Level.SEVERE, "Could not execute local operation " + o);
 					// Exit the application
 					System.exit(0);
 				}
 				// } else {
 				// // Operation is not supported, so notify the client
 				// context.notifyOfNotSupportedOperation(o);
 				// }
 			} else {
 				if (!executeRemoteOperation(o)) {
 					// Error, debug output.
 					log.log(Level.SEVERE, "Could not execute remote operation" + o);
 					// Exit the application
 					System.exit(0);
 				}
 			}
 			// list of all executed operations for debugging purpose only
 			executedOperationList.add(o);
 
 		}
 
 		// Info output when exiting
 		log.info("------------------------------------------------------------------------------------------------------\n" + this.getName() + " id=" + getClientId() + " executed operations: \n"
 				+ executedOperationList.size() + "\n" + "------------------------------------------------------------------------------------------------------\n");
 		while (!operationQueue.isEmpty()) {
 			Operation o;
 
 			o = operationQueue.poll();
 			if (o == null)
 				break;
 			if (o.getClientId() == context.getIdentifier()) {
 				log.info(this.getName() + " id=" + getClientId() + "run: local after stop operation");
 				executeLocalOperation(o);
 			} else {
 				if (!executeRemoteOperation(o)) {
 					// Error, debug output.
 					log.log(Level.SEVERE, "Could not execute remote operation after stop" + o);
 				}
 			}
 			executedOperationList.add(o);
 		}
 		// Info output when exiting
 		log.info(this.getName() + " id=" + getClientId() + "run: queue: " + operationQueue);
 		// store the current document locally
 		//storeDocumentLocally();
 
 		synchronized (this) {
 			this.notify();
 		}
 		// Info output when exiting
 		log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n" + this.getName() + " id=" + getClientId() + this.getName() + " id="
 				+ getClientId() + " run: executedOperations: \n" + executedOperationList.size() + " : executed in order:  " + executedOperationList + "\n" + "historybuffer: " + historyBuffer + "\n"
 				+ "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
 		// Last info output shutdown complete
 		log.info(this.getName() + " id=" + getClientId() + this.getName() + " id=" + getClientId() + "run finished");
 
 	}
 
 	/*
 	 * End Runnable interface methods
 	 */
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return "Concurrency Controller of " + context.getName() + " id: " + context.getIdentifier();
 	}
 
 	public void setSession(CEFXSession session) {
 		this.session = session;
 	}
 
 	public HashMap<Operation, Node> getDeletedNodesMap() {
 		return deletedNodesMap;
 	}
 
 	public boolean isCollaborationReady() {
 		return collaborationReady;
 	}
 
 	public void setCollaborationReady(boolean collaborationReady) {
 		this.collaborationReady = collaborationReady;
 	}
 
 	public void reset() {
 		lateJoinBuffer.clear();
 		historyBuffer.clear();
 		deletedNodesMap.clear();
 	}
 
 	public void setStateVector(StateVector sv) {
 		stateVector=sv;
 	}
 }
