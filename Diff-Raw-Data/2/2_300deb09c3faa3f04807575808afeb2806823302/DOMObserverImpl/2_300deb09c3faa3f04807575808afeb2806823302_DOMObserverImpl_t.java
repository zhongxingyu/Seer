 package de.uniluebeck.itm.tr.util.domobserver;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import de.uniluebeck.itm.tr.util.ListenerManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.namespace.QName;
 import javax.xml.xpath.*;
 import java.util.ArrayList;
 
 public class DOMObserverImpl implements DOMObserver {
 
 	private static final Logger log = LoggerFactory.getLogger(DOMObserverImpl.class);
 
 	private ListenerManager<DOMObserverListener> listenerManager;
 
 	private Node oldNode;
 
 	private Node currentNode;
 
 	private Provider<Node> newNodeProvider;
 
 	@Inject
 	public DOMObserverImpl(final ListenerManager<DOMObserverListener> listenerManager,
 						   final Provider<Node> newNodeProvider) {
 
 		this.listenerManager = listenerManager;
 		this.newNodeProvider = newNodeProvider;
 	}
 
 	@Override
 	public void addListener(final DOMObserverListener listener) {
 		listenerManager.addListener(listener);
 	}
 
 	@Override
 	public void removeListener(final DOMObserverListener listener) {
 		listenerManager.removeListener(listener);
 	}
 
 	@Override
 	public void run() {
 
 		updateCurrentDOM();
 
 		if (!changesOccurred()) {
 			return;
 		}
 
 		for (DOMObserverListener listener : listenerManager.getListeners()) {
 
 			String xPathExpression = listener.getXPathExpression();
 			QName qName = listener.getQName();
 
 			DOMTuple lastScopedChangesInternal = null;
 
 			try {
 				lastScopedChangesInternal = getLastScopedChangesInternal(xPathExpression, qName);
 			} catch (XPathExpressionException e) {
 				throw new RuntimeException(e);
 			}
 
 			try {
 				listener.onDOMChanged(lastScopedChangesInternal);
 			} catch (Exception e) {
 				log.error("Exception occurred while calling {} listener: {}", listener, e);
 			}
 		}
 
 	}
 
 	@Override
 	public DOMTuple getLastScopedChanges(final String xPathExpression, final QName qName)
 			throws XPathExpressionException {
 
 		if (!changesOccurred()) {
 			return null;
 		}
 
 		return getLastScopedChangesInternal(xPathExpression, qName);
 	}
 
 	@Override
 	public void updateCurrentDOM() {
 		oldNode = currentNode;
 		try {
 			currentNode = newNodeProvider.get();
 		} catch (Exception e) {
 			//TODO: check if it is better to throw the error
			log.warn("Unable to load the next DOM Node. Maybe the source used by the provider ({}) is corrupted?", newNodeProvider);
 			currentNode = null;
 		}
 	}
 
 	private DOMTuple getLastScopedChangesInternal(final String xPathExpression, final QName qName)
 			throws XPathExpressionException {
 
 		Object oldScopedObject = oldNode == null ? null : getScopedObject(oldNode, xPathExpression, qName);
 		Object currentScopedObject = currentNode == null ? null : getScopedObject(currentNode, xPathExpression, qName);
 
 		// both objects null -->no change
 		if (null == oldScopedObject && null == currentScopedObject) {
 			return null;
 		}
 
 		// both not null --> check for qName
 		if (null != oldScopedObject && null != currentScopedObject) {
 			// //NODE --> check via isNodeEqual
 			if (XPathConstants.NODE.equals(qName)) {
 				if (((Node) oldScopedObject).isEqualNode((Node) currentNode)) {
 					return null;
 				}
 
 			}
 			if (XPathConstants.NODESET.equals(qName)) {
 				if (areNodeSetsEqual(oldScopedObject, currentScopedObject)) {
 					return null;
 				}
 			} else {
 				// XPathConstants.BOOLEAN, NUMBER, STRING --> rely on equals method
 				if (oldScopedObject.equals(currentScopedObject)) {
 					return null;
 				}
 			}
 
 		}
 		// either both not null and change detected or
 		// one object null the other not return change
 
 		return new DOMTuple(oldScopedObject, currentScopedObject);
 
 	}
 
 	/**
 	 * Checks equality of two node sets.
 	 * <p/>
 	 * Wraps the nodes so that we have a proper equals method and then use List.containsAll
 	 * for check of equality.
 	 *
 	 * @param oldScopedObject
 	 * @param currentScopedObject
 	 *
 	 * @return {@code null} if no change is detected else {@link DOMTuple}
 	 */
 	private boolean areNodeSetsEqual(Object oldScopedObject, Object currentScopedObject) {
 
 		ArrayList<WrappedNode> oldNodes = convertNodeListToHashSet((NodeList) oldScopedObject);
 		ArrayList<WrappedNode> currentNodes = convertNodeListToHashSet((NodeList) currentScopedObject);
 		if (oldNodes.size() == currentNodes.size()) {
 			return oldNodes.containsAll(currentNodes);
 		} else {
 			return false;
 		}
 
 	}
 
 	private ArrayList<WrappedNode> convertNodeListToHashSet(NodeList nodeList) {
 		ArrayList<WrappedNode> result = new ArrayList<WrappedNode>();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			result.add(new WrappedNode(nodeList.item(i)) {
 			}
 			);
 		}
 		return result;
 	}
 
 	class WrappedNode {
 
 		private Node node;
 
 		public Node getNode() {
 			return node;
 		}
 
 		public WrappedNode(Node node) {
 			this.node = node;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			return this.node.isEqualNode(((WrappedNode) obj).getNode());
 		}
 
 	}
 
 	private Object getScopedObject(final Node node, final String xPathExpression, final QName qName)
 			throws XPathExpressionException {
 
 		XPathFactory xPathFactory = XPathFactory.newInstance();
 		XPath xPath = xPathFactory.newXPath();
 		XPathExpression expression = xPath.compile(xPathExpression);
 
 		return expression.evaluate(node, qName);
 	}
 
 	private boolean changesOccurred() {
 
 		boolean sameInstance = oldNode == currentNode;
 		boolean oldIsNullCurrentIsNot = oldNode == null && currentNode != null;
 		boolean oldIsNonNullCurrentIs = oldNode != null && currentNode == null;
 		boolean nodeTreesEqual = oldNode != null && currentNode != null && oldNode.isEqualNode(currentNode);
 
 		return !sameInstance && (oldIsNullCurrentIsNot || oldIsNonNullCurrentIs || !nodeTreesEqual);
 	}
 }
