 package de.uniluebeck.itm.tr.util.domobserver;
 
 import com.google.inject.Provider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.namespace.QName;
 import javax.xml.xpath.*;
 import java.util.List;
 
 import static com.google.common.base.Throwables.propagate;
 import static com.google.common.collect.Lists.newArrayList;
 
 class DOMObserverImpl implements DOMObserver {
 
 	private static final Logger log = LoggerFactory.getLogger(DOMObserverImpl.class);
 
 	private final DOMObserverListenerManager listenerManager;
 
 	private Node currentDOM;
 
 	private final Provider<Node> nextDOMProvider;
 
 	DOMObserverImpl(final DOMObserverListenerManager listenerManager,
 					final Provider<Node> nextDOMProvider) {
 
 		this.listenerManager = listenerManager;
 		this.nextDOMProvider = nextDOMProvider;
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
 
 		if (listenerManager.getListeners().isEmpty()) {
 			return;
 		}
 
 		try {
 			updateCurrentDOM();
 		} catch (Exception e) {
 			notifyDOMLoadFailure(e);
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
			scopedChanges = getScopedChangesInternal(listenerManager.getLastDOM(listener), xPathExpression, qName);
			listenerManager.updateLastDOM(listener, currentDOM);
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
 	public DOMTuple getScopedChanges(final Node oldDOM, final String xPathExpression, final QName qName)
 			throws XPathExpressionException {
 
 		if (!changesOccurred(oldDOM)) {
 			return null;
 		}
 
 		return getScopedChangesInternal(oldDOM, xPathExpression, qName);
 	}
 
 	@Override
 	public DOMTuple updateCurrentDOM() {
 		Node newDOM;
 		try {
 			newDOM = nextDOMProvider.get();
 		} catch (Exception e) {
 			log.warn("Unable to load the next DOM Node. Maybe the source used by the provider ({}) is corrupted?",
 					nextDOMProvider
 			);
 			throw propagate(e);
 		}
 		Node oldDOM = currentDOM;
 		currentDOM = newDOM;
 		return new DOMTuple(oldDOM, currentDOM);
 	}
 
 	private DOMTuple getScopedChangesInternal(final Node oldDOM, final String xPathExpression, final QName qName)
 			throws XPathExpressionException {
 
 		Object oldScopedObject = oldDOM == null ? null : getScopedObject(oldDOM, xPathExpression, qName);
 		Object currentScopedObject = currentDOM == null ? null : getScopedObject(currentDOM, xPathExpression, qName);
 
 		// both objects null -->no change
 		if (null == oldScopedObject && null == currentScopedObject) {
 			return null;
 		}
 
 		// both not null --> check for qName
 		if (null != oldScopedObject && null != currentScopedObject) {
 			// //NODE --> check via isNodeEqual
 			if (XPathConstants.NODE.equals(qName)) {
 				if (((Node) oldScopedObject).isEqualNode(currentDOM)) {
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
 
 	private boolean changesOccurred(final Node oldDOM) {
 
 		boolean sameInstance = oldDOM == currentDOM;
 		boolean oldIsNullCurrentIsNot = oldDOM == null && currentDOM != null;
 		boolean oldIsNonNullCurrentIs = oldDOM != null && currentDOM == null;
 		boolean nodeTreesEqual = oldDOM != null && currentDOM != null && oldDOM.isEqualNode(currentDOM);
 
 		return !sameInstance && (oldIsNullCurrentIsNot || oldIsNonNullCurrentIs || !nodeTreesEqual);
 	}
 }
