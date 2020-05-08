 package de.uniluebeck.itm.tr.util.domobserver;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import de.uniluebeck.itm.tr.util.ListenerManager;
 import org.w3c.dom.Node;
 
 import javax.xml.namespace.QName;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPathExpression;
 
 public class DOMObserverImpl implements DOMObserver {
 
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
 
 			try {
 				listener.onDOMChanged(getLastScopedChangesInternal(xPathExpression, qName));
 			} catch (XPathExpressionException e) {
 				throw new RuntimeException(e);
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
			currentNode = null;
		}
 	}
 
 	private DOMTuple getLastScopedChangesInternal(final String xPathExpression, final QName qName)
 			throws XPathExpressionException {
 
 		Object oldScopedObject = oldNode == null ? null : getScopedObject(oldNode, xPathExpression, qName);
 		Object currentScopedObject =
 				currentNode == null ? null : getScopedObject(currentNode, xPathExpression, qName);
 
 		return new DOMTuple(oldScopedObject, currentScopedObject);
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
 		boolean nodeTreesEqual = oldNode != null && currentNode != null && !oldNode.isEqualNode(currentNode);
 
 		return oldIsNullCurrentIsNot || !sameInstance || nodeTreesEqual;
 	}
 }
