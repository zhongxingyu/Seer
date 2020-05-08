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
 import java.util.List;
 
 import static com.google.common.base.Throwables.propagate;
 import static com.google.common.collect.Lists.newArrayList;
 
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
 
 		try {
 			updateCurrentDOM();
 		} catch (Exception e) {
 			notifyDOMLoadFailure(e);
 		}
 
 		if (!changesOccurred()) {
 			return;
 		}
 
 		evaluateXPathExpressionsAndNotifyListeners();
 
 	}
 
 	private void evaluateXPathExpressionsAndNotifyListeners() {
 		for (DOMObserverListener listener : listenerManager.getListeners()) {
 			evaluateXPathExpressionAndNotify(listener);
 		}
 	}
 
 	private void evaluateXPathExpressionAndNotify(final DOMObserverListener listener) {
 
 		String xPathExpression = listener.getXPathExpression();
 		QName qName = listener.getQName();
 
 		DOMTuple scopedChanges;
 
 		try {
 			scopedChanges = getScopedChangesInternal(xPathExpression, qName);
 		} catch (XPathExpressionException e) {
 			notifyXPathEvaluationFailure(e);
 			return;
 		}
 
 		if (scopedChanges != null) {
 			notifyListener(listener, scopedChanges);
 		}
 	}
 
 	private void notifyListener(final DOMObserverListener listener, final DOMTuple scopedChangesInternal) {
 		try {
 			listener.onDOMChanged(scopedChangesInternal);
 		} catch (Exception e) {
 			log.warn("Exception occurred while calling {} listener: {}", listener, e);
 		}
 	}
 
 	private void notifyDOMLoadFailure(final Exception e) {
 		for (DOMObserverListener listener : listenerManager.getListeners()) {
 			listener.onDOMLoadFailure(e);
 		}
 	}
 
 	private void notifyXPathEvaluationFailure(final XPathExpressionException e) {
 		for (DOMObserverListener listener : listenerManager.getListeners()) {
 			listener.onXPathEvaluationFailure(e);
 		}
 	}
 
 	@Override
 	public DOMTuple getScopedChanges(final String xPathExpression, final QName qName)
 			throws XPathExpressionException {
 
 		if (!changesOccurred()) {
 			return null;
 		}
 
 		return getScopedChangesInternal(xPathExpression, qName);
 	}
 
 	@Override
 	public void updateCurrentDOM() {
 		Node newNode;
 		try {
 			newNode = newNodeProvider.get();
 		} catch (Exception e) {
 			log.warn("Unable to load the next DOM Node. Maybe the source used by the provider ({}) is corrupted?",
 					newNodeProvider
 			);
 			throw propagate(e);
 		}
 		oldNode = currentNode;
 		currentNode = newNode;
 	}
 
 	private DOMTuple getScopedChangesInternal(final String xPathExpression, final QName qName)
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
 				if (((Node) oldScopedObject).isEqualNode(currentNode)) {
 					return null;
 				}
 
 			}
 			if (XPathConstants.NODESET.equals(qName)) {
 				if (areNodeSetsEqual((NodeList) oldScopedObject, (NodeList) currentScopedObject)) {
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
 	 * @param set1
 	 * 		the first set
 	 * @param set2
 	 * 		the second set
 	 *
 	 * @return {@code null} if no change is detected else {@link DOMTuple}
 	 */
 	private boolean areNodeSetsEqual(NodeList set1, NodeList set2) {
 
 		List<WrappedNode> oldNodes = convertNodeListToHashSet(set1);
 		List<WrappedNode> currentNodes = convertNodeListToHashSet(set2);
 
 		return oldNodes.size() == currentNodes.size() && oldNodes.containsAll(currentNodes);
 
 	}
 
 	private List<WrappedNode> convertNodeListToHashSet(NodeList nodeList) {
 		List<WrappedNode> result = newArrayList();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			result.add(new WrappedNode(nodeList.item(i)));
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
 			return obj instanceof WrappedNode && this.node.isEqualNode(((WrappedNode) obj).getNode());
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
