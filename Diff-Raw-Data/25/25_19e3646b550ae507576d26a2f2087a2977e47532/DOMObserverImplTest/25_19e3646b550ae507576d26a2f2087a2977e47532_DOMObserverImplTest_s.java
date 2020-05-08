 package de.uniluebeck.itm.tr.util.domobserver;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Provider;
 import com.google.inject.TypeLiteral;
 import de.uniluebeck.itm.tr.util.ListenerManager;
 import de.uniluebeck.itm.tr.util.ListenerManagerImpl;
 import de.uniluebeck.itm.tr.util.Logging;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 
 @RunWith(MockitoJUnitRunner.class)
 public class DOMObserverImplTest {
 
 	static {
 		Logging.setLoggingDefaults();
 	}
 
 	private static final String CONFIG_1 = "de/uniluebeck/itm/tr/util/domobserver/tr.iwsn-testbed.xml";
 
 	private static final String CONFIG_2 = "de/uniluebeck/itm/tr/util/domobserver/tr.iwsn-testbed2.xml";
 
 	private static final String XPATH_EXPRESSION_ROOT_NODE = "/*";
 
 	private static final String XPATH_EXPRESSION_APPLICATION_NODES = "//application";
 
 	private DOMObserver domObserver;
 
 	@Mock
 	private Provider<Node> nodeProviderMock;
 
 	@Mock
 	private DOMObserverListener listenerMock;
 
 	@Before
 	public void setUp() throws Exception {
 		domObserver = Guice.createInjector(new AbstractModule() {
 			@Override
 			protected void configure() {
 				bind(new TypeLiteral<ListenerManager<DOMObserverListener>>() {
 				}
 				).to(new TypeLiteral<ListenerManagerImpl<DOMObserverListener>>() {
 				}
 				);
 				bind(Node.class).toProvider(nodeProviderMock);
 				bind(DOMObserver.class).to(DOMObserverImpl.class);
 			}
 		}
 		).getInstance(DOMObserver.class);
 	}
 
 	@Test
 	public void testThatNoChangeIsDetectedWhenOldAndNewAreBothNull() throws Exception {
 
 		// setup
 		when(nodeProviderMock.get()).thenReturn(null);
 
 		// act
 		domObserver.updateCurrentDOM();
 
 		// validate
 		assertNull(domObserver.getScopedChanges(XPATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE));
 	}
 
 	@Test
 	public void testThatChangeIsDetectedWhenOldIsNullAndNewIsNotNull() throws Exception {
 
 		Node node = createDOM(CONFIG_1);
 		when(nodeProviderMock.get()).thenReturn(node);
 
 		domObserver.updateCurrentDOM();
 
 		DOMTuple lastScopedChanges = domObserver.getScopedChanges(XPATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
 		assertNull(lastScopedChanges.getFirst());
 		assertTrue(node.isEqualNode((Node) lastScopedChanges.getSecond()));
 	}
 
 	private Node createDOM(final String fileName) throws Exception {
 
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		Document document = dBuilder.parse(DOMObserverImplTest.class.getClassLoader().getResourceAsStream(fileName));
 		return document.getDocumentElement();
 	}
 
 	@Test
 	public void testThatChangeIsDetectedWhenOldIsNotNullButNewIsNull() throws Exception {
 		Node node1 = createDOM(CONFIG_1);
 		when(nodeProviderMock.get()).thenReturn(node1).thenReturn(null);
 		domObserver.updateCurrentDOM(); //new node is now node1
 		domObserver.updateCurrentDOM(); //new node is now null old node is node1
 
 		DOMTuple lastScopedChanges = domObserver.getScopedChanges(XPATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
 		assertNotNull(lastScopedChanges);
 		assertNotNull(lastScopedChanges.getFirst());
 		assertNull(lastScopedChanges.getSecond());
 
 
 	}
 
 	@Test
 	public void testThatNoChangeIsDetectedWhenBotNotNullAndBothAreEqualUnscoped() throws Exception {
 		Node node1 = createDOM(CONFIG_1);
 		Node node2 = createDOM(CONFIG_1); // same config for both nodes
 		when(nodeProviderMock.get()).thenReturn(node1).thenReturn(node2);
 		domObserver.updateCurrentDOM(); //new node is now node1
 		domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
 		DOMTuple lastScopedChanges = domObserver.getScopedChanges(XPATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
 		assertNull(lastScopedChanges);
 
 	}
 
 	@Test
 	public void testThatChangeIsDetectedWhenBothNotNullAndChangeOccurredUnscoped() throws Exception {
 		Node node1 = createDOM(CONFIG_1);
 		Node node2 = createDOM(CONFIG_2); //now the configs differ
 		when(nodeProviderMock.get()).thenReturn(node1).thenReturn(node2);
 		domObserver.updateCurrentDOM(); //new node is now node1
 		domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
 		DOMTuple lastScopedChanges = domObserver.getScopedChanges(XPATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE);
 		assertNotNull(lastScopedChanges);
 		assertNotNull(lastScopedChanges.getFirst());
 		assertFalse(((Node) lastScopedChanges.getFirst()).isEqualNode((Node) lastScopedChanges.getSecond()));
 	}
 
 	@Test
 	public void testThatNoChangeIsDetectedWhenBotNotNullAndBothAreEqualScoped() throws Exception {
 
 		// twice the same document but different instances
 		when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1)).thenReturn(createDOM(CONFIG_1));
 
 		domObserver.updateCurrentDOM(); //new node is now node1
 		domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
 
 		DOMTuple scopedChanges = domObserver.getScopedChanges(
 				XPATH_EXPRESSION_APPLICATION_NODES,
 				XPathConstants.NODESET
 		);
 		assertNull(scopedChanges);
 	}
 
 	@Test
 	public void testThatNoListenersAreNotifiedWhenBotNotNullAndBothAreEqualScoped() throws Exception {
 
 		when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1)).thenReturn(createDOM(CONFIG_1));
 		when(listenerMock.getXPathExpression()).thenReturn(XPATH_EXPRESSION_ROOT_NODE);
 		when(listenerMock.getQName()).thenReturn(XPathConstants.NODE);
 
 		domObserver.updateCurrentDOM(); //new node is now node1
 		domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
 
 		verify(listenerMock, never()).onDOMChanged(Matchers.<DOMTuple>any());
 	}
 
 	@Test
 	public void testThatChangeIsDetectedWhenBothNotNullAndChangeOccurredScoped() throws Exception {
 		Node node1 = createDOM(CONFIG_1);
 		Node node2 = createDOM(CONFIG_2); //now the configs differ
 		when(nodeProviderMock.get()).thenReturn(node1).thenReturn(node2);
 		domObserver.updateCurrentDOM(); //new node is now node1
 		domObserver.updateCurrentDOM(); //new node is now node2 old node is node1
 		DOMTuple lastScopedChanges =
 				domObserver.getScopedChanges(XPATH_EXPRESSION_APPLICATION_NODES, XPathConstants.NODESET);
 		assertNotNull(lastScopedChanges);
 		assertNotNull(lastScopedChanges.getFirst());
 		assertNotNull(lastScopedChanges.getSecond());
 	}
 
 	@Test
 	public void testThatExceptionIsThrownIfXmIsInvalid() throws Exception {
 		Node node1 = createDOM(CONFIG_1);
 		when(nodeProviderMock.get()).thenReturn(node1).thenThrow(new RuntimeException("test xml is invalid"));
 		domObserver.updateCurrentDOM(); // new node is now node1
 		try {
 			domObserver.updateCurrentDOM();
 			fail("An exception should have been thrown");
 		} catch (Exception expected) {
 		}
 	}
 
 	@Test
 	public void testThatNoChangesAreDetectedIfProviderDeliversSameInstanceAgain() throws Exception {
 		Node config1DOM = createDOM(CONFIG_1);
 		when(nodeProviderMock.get()).thenReturn(config1DOM);
 		domObserver.updateCurrentDOM();
 		domObserver.updateCurrentDOM();
 		assertNull(domObserver.getScopedChanges(XPATH_EXPRESSION_ROOT_NODE, XPathConstants.NODE));
 	}
 
 	@Test
 	public void testThatNoListenersAreCalledIfProviderDeliversSameInstanceAgain() throws Exception {
 		Node config1DOM = createDOM(CONFIG_1);
 		when(nodeProviderMock.get()).thenReturn(config1DOM);
 		DOMObserverListener listenerMock = mock(DOMObserverListener.class);
 		when(listenerMock.getXPathExpression()).thenReturn(XPATH_EXPRESSION_ROOT_NODE);
 		when(listenerMock.getQName()).thenReturn(XPathConstants.NODE);
 		domObserver.addListener(listenerMock);
 		domObserver.run();
 		verify(listenerMock, times(1)).onDOMChanged(Matchers.<DOMTuple>any());
 		reset(listenerMock);
 		domObserver.run();
 		verify(listenerMock, never()).onDOMChanged(Matchers.<DOMTuple>any());
 	}
 
 	@Test(expected = Exception.class)
 	public void testThatExceptionIsThrownIfProviderThrowsException() throws Exception {
 
 		when(nodeProviderMock.get()).thenThrow(new RuntimeException("Test"));
 
 		domObserver.updateCurrentDOM();
 	}
 
 	@Test
 	public void testThatListenersAreNotifiedIfProviderThrowsException() throws Exception {
 
 		when(nodeProviderMock.get()).thenThrow(new RuntimeException("Test"));
 		when(listenerMock.getXPathExpression()).thenReturn(XPATH_EXPRESSION_ROOT_NODE);
 		when(listenerMock.getQName()).thenReturn(XPathConstants.NODE);
 		domObserver.addListener(listenerMock);
 
 		domObserver.run();
 
 		verify(listenerMock).onDOMLoadFailure(Matchers.<Throwable>any());
 	}
 
 	@Test(expected = XPathExpressionException.class)
 	public void testThatExceptionIsThrownIfXPathExpressionIsInvalid() throws Exception {
 
 		when(nodeProviderMock.get()).thenReturn(createDOM(CONFIG_1));
 
 		domObserver.updateCurrentDOM();
 		domObserver.getScopedChanges("hello, world", XPathConstants.NODE);
 	}
 
 	@Test
 	public void testThatListenersAreNotifiedIfXPathExpressionIsInvalid() throws Exception {
 
 		Node config1DOM = createDOM(CONFIG_1);
 		when(nodeProviderMock.get()).thenReturn(config1DOM);
 
 		when(listenerMock.getXPathExpression()).thenReturn("hello, world");
 		when(listenerMock.getQName()).thenReturn(XPathConstants.NODE);
 		domObserver.addListener(listenerMock);
 
 		domObserver.run();
 
 		verify(listenerMock).onXPathEvaluationFailure(Matchers.<XPathExpressionException>any());
 	}
 }
