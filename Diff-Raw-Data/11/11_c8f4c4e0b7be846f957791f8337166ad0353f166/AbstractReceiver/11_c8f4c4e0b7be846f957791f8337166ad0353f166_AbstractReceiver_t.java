 package eu.play_project.play_eventadapter;
 
 import static eu.play_project.play_commons.constants.Event.WSN_MSG_DEFAULT_SYNTAX;
 import static eu.play_project.play_commons.constants.Event.WSN_MSG_ELEMENT;
 import static eu.play_project.play_commons.constants.Event.WSN_MSG_GRAPH_ATTRIBUTE;
 import static eu.play_project.play_commons.constants.Event.WSN_MSG_NS;
 import static eu.play_project.play_commons.constants.Event.WSN_MSG_SYNTAX_ATTRIBUTE;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.namespace.QName;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.event_processing.events.types.Event;
 import org.ontoware.rdf2go.exception.ModelRuntimeException;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.ModelSet;
 import org.ontoware.rdf2go.model.Syntax;
 import org.ontoware.rdfreactor.runtime.ReactorResult;
 import org.petalslink.dsb.notification.client.http.simple.HTTPProducerClient;
 import org.petalslink.dsb.notification.client.http.simple.HTTPProducerRPClient;
 import org.petalslink.dsb.notification.client.http.simple.HTTPSubscriptionManagerClient;
 import org.petalslink.dsb.notification.commons.NotificationException;
 import org.w3c.dom.Node;
 
 import com.ebmwebsourcing.easycommons.xml.XMLHelper;
 import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.NotificationMessageHolderType;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
 
 import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 
 /**
  * A an abstract event consumer which can subscribe to PLAY RDF events and deal
  * with the necessary un-marshalling and parsing.
  * 
  * @author stuehmer
  * 
  */
 public abstract class AbstractReceiver {
 
 	static {
 		Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
 				new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
 				new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
 				new WsnbModelFactoryImpl());
 	}
 
 	private String dsbSubscribe = Constants.getProperties().getProperty(
 			"dsb.subscribe.endpoint");
 	private String dsbUnsubscribe = Constants.getProperties().getProperty(
 			"dsb.unsubscribe.endpoint");
 	private final Logger logger = Logger.getAnonymousLogger();
 	private final Map<String, QName> subscriptions = Collections.synchronizedMap(new HashMap<String, QName>());
 
 	/**
 	 * Subscribe to a topic at the endpoint in
 	 * {@link AbstractReceiver#dsbSubscribe}. The callback will be used by the
 	 * DSB to send the subscriptions.
 	 * 
 	 * @param topic
 	 * @param notificationsEndPoint
 	 *            callback SOAP URI to receive notifications
 	 * @throws NotificationException
 	 */
 	public String subscribe(QName topic, String notificationsEndPoint) throws NotificationException {
 		HTTPProducerClient client = new HTTPProducerClient(dsbSubscribe);
 
 		try {
 			String subscriptionId = client.subscribe(topic,
 					notificationsEndPoint);
 			subscriptions.put(subscriptionId, topic);
 			return subscriptionId;
 		} catch (NotificationException e) {
 			logger.log(Level.WARNING, "Problem while subcribing to topic '"
 					+ topic + "' at DSB endpoint '" + dsbSubscribe
 					+ "' with callback endpoint '" + notificationsEndPoint
 					+ "'", e);
 			throw e;
 		}
 
 	}
 
 	/**
 	 * Unsubscribe from a specific subscription.
 	 * 
 	 * @param subscriptionId
 	 * @throws NotificationException
 	 */
 	public void unsubscribe(String subscriptionId)
 			throws NotificationException {
 		HTTPSubscriptionManagerClient subscriptionManagerClient = new HTTPSubscriptionManagerClient(
 				dsbUnsubscribe);
 		try {
 			subscriptionManagerClient.unsubscribe(subscriptionId);
 			subscriptions.remove(subscriptionId);
 
 		} catch (NotificationException e) {
 			logger.log(Level.WARNING,
 					"Problem while unsubcribing from subscription '"
 							+ subscriptionId + "' at DSB endpoint '"
 							+ dsbSubscribe + "'", e);
 			throw e;
 		}
 
 	}
 
 	/**
 	 * Unsubscribe from all previous subscriptions. Don't fail if something goes wrong.
 	 */
 	public void unsubscribeAll() {
 		int failCount = 0;
		// Make a copy of the collection because it will be modified in #unsubscribe()
		Set<String> removal = new HashSet<String>(subscriptions.keySet());
		for (String subscriptionID : removal) {
 			try {
				unsubscribe(subscriptionID);
 			} catch (NotificationException e) {
 				failCount++;
 			}
 		}
 		if (failCount > 0) {
 			logger.log(Level.WARNING,
 					"Problem while unsubcribing from all subscriptions: "
 							+ failCount
 							+ " unsubscriptions failed at DSB endpoint '"
 							+ dsbSubscribe + "'");
 		} else {
 			logger.log(Level.INFO,
 					"Successfully unsubcribed from all subscriptions at DSB endpoint '"
 							+ dsbSubscribe + "'");
 		}
 	}
 
 	/**
 	 * Retreive the current list of topics from the endpoint in
 	 * {@link AbstractReceiver#dsbSubscribe}.
 	 * 
 	 * @return the current list of topics
 	 */
 	public List<QName> getTopics() {
 		HTTPProducerRPClient rpclient = new HTTPProducerRPClient(dsbSubscribe);
 		List<QName> topics;
 		try {
 			topics = rpclient.getTopics();
 			logger.info(topics.toString());
 		} catch (NotificationException e) {
 			logger.log(Level.WARNING,
 					"Problem while retreiving the available topics from DSB endpoint '"
 							+ dsbSubscribe + "'", e);
 		} finally {
 			topics = new ArrayList<QName>();
 		}
 		return topics;
 	}
 
 	/**
 	 * Retrieve RDF from the contents of an XML message.
 	 * 
 	 * @param stringNotify
 	 * @return
 	 * @throws NoRdfEventException if there was no RDF to parse in the input
 	 */
 	public Model parseRdf(String stringNotify) throws NoRdfEventException {
 		try {
 			return parseRdf(XMLHelper.createDocumentFromString(stringNotify));
 		} catch (Exception e) {
 			throw new NoRdfEventException("Exception while reading RDF event from XML message.", e);
 		}
 	}
 	
 	/**
 	 * Retrieve RDF from the contents of an XML message.
 	 * 
 	 * @param notify
 	 * @return
 	 * @throws NoRdfEventException if there was no RDF to parse in the input
 	 */
 	public Model parseRdf(Notify notify) throws NoRdfEventException {
 		for (NotificationMessageHolderType holder : notify.getNotificationMessage()) {
 			// we support only one event message per notify envelope, return immediately:
 			return parseRdf(holder.getMessage().getAny());
 		}
 		// If we reach this point past the loop, fail:
 		throw new NoRdfEventException("An event was receieved without a <wsnt:Message> element.");
 	}
 		
 	/**
 	 * Retrieve RDF from the contents of an XML message.
 	 * 
 	 * @param xmlNotify
 	 * @return
 	 * @throws NoRdfEventException if there was no RDF to parse in the input
 	 */
 	public Model parseRdf(Node xmlNotify) throws NoRdfEventException {
 
 		// The ModelSet to hold the RDF data:
 		ModelSet rdf = EventHelpers.createEmptyModelSet();
 		// The RDF syntax (serialization format):
 		String syntax;
 		
 		//Evaluate XPath against Document itself
 		XPath xPath = XPathFactory.newInstance().newXPath();
 		xPath.setNamespaceContext(nc);
 		Node playMsgElement = null;
 		
 		try {
 			// Select the first [1] WSN_MSG_ELEMENT in document order:
 			playMsgElement = (Node)xPath.evaluate("(//" + WSN_MSG_ELEMENT.getPrefix() + ":" + WSN_MSG_ELEMENT.getLocalPart() + ")[1]",
 					xmlNotify, XPathConstants.NODE);
 		} catch (XPathExpressionException e) {
 			throw new NoRdfEventException("An event was receieved with no or wrong content element: " + WSN_MSG_ELEMENT + ". " + e.getMessage());
 		}
 
 		String playMsgContent = (playMsgElement != null && playMsgElement.getTextContent() != null) ? playMsgElement.getTextContent() : "";
 		if (playMsgContent.isEmpty()) {
 			throw new NoRdfEventException("An event was receieved with no or empty content element: " + WSN_MSG_ELEMENT);
 		}
 		Reader r = new StringReader(playMsgContent);
 
 		
 		/*
 		 * Find the RDF syntax
 		 */
 		Node syntaxAttribute = playMsgElement.getAttributes().getNamedItemNS(WSN_MSG_NS, WSN_MSG_SYNTAX_ATTRIBUTE);
 		if (syntaxAttribute != null && !syntaxAttribute.getTextContent().isEmpty()) {
 			syntax = syntaxAttribute.getTextContent();
 		}
 		else {
 			syntax = WSN_MSG_DEFAULT_SYNTAX;
 		}
 		logger.fine("Parsing an incoming event with syntax '" + syntax + "'");
 		
 		try {
 			rdf.readFrom(r, Syntax.forMimeType(syntax));
 		} catch (ModelRuntimeException e) {
 			throw new NoRdfEventException("An exception occured while parsing RDF of an incoming event.", e);
 		} catch (IOException e) {
 			throw new NoRdfEventException("An exception occured while parsing RDF of an incoming event.", e);
 		}
 		
 		/*
 		 * A hack to select the event graph in the rare case when more than one
 		 * graph were returned:
 		 */
 		Model model = rdf.getDefaultModel();
 		long max = model.size();
 		Iterator<Model> it = rdf.getModels();
 		// For now, select the largest model
 		while (it.hasNext()) {
 			Model temp = it.next();
 			long tempSize = temp.size();
 			if (tempSize > max) {
 				max = tempSize;
 				model = temp;
 			}
 		}
 		
 		if (max == 0) {
 			throw new NoRdfEventException("The RDF event had no attributes, or other features (zero quads).");
 		}
 
 		// If there is no RDF context try to get it from the XML message
 		if (model.getContextURI() == null) {
 			Node graphAttribute = playMsgElement.getAttributes().getNamedItem(WSN_MSG_GRAPH_ATTRIBUTE);
 			if (graphAttribute != null && !graphAttribute.getTextContent().isEmpty()) {
 				Model temp = EventHelpers.createEmptyModel(graphAttribute.getTextContent());
 				temp.addModel(model);
 				model = temp;
 			}
 
 		}
 		return EventHelpers.addNamespaces(model);
 	}
 	
 	/**
 	 * Retrieve an Event from the contents of an XML message.
 	 * 
 	 * @param xmlNotify
 	 * @param type the event class which is expected as a return type
 	 * @return an event of the given type, if one was found. {@code null} otherwise
 	 * @throws NoRdfEventException if there was no RDF to parse in the input
 	 */
 	public <EventType extends Event> EventType getEvent(String stringNotify, Class<EventType> type) throws NoRdfEventException {
 		try {
 			return this.getEvent(XMLHelper.createDocumentFromString(stringNotify), type);
 		} catch (Exception e) {
 			throw new NoRdfEventException("Exception while reading RDF event from XML message.", e);
 		}
 	}
 
 	/**
 	 * Retrieve an Event from the contents of an XML message.
 	 * 
 	 * @param xmlNotify
 	 * @param type the event class which is expected as a return type
 	 * @return an event of the given type, if one was found. {@code null} otherwise
 	 * @throws NoRdfEventException if there was no RDF to parse in the input
 	 */
 	public <EventType extends Event> EventType getEvent(Node xmlNotify, Class<EventType> type) throws NoRdfEventException {
 		Model model = this.parseRdf(xmlNotify);
 		EventType event = null;
 		
 		Method m;
 		try {
 			m = type.getMethod("getAllInstances_as", Model.class);
 			@SuppressWarnings("unchecked")
 			ReactorResult<EventType> result = (ReactorResult<EventType>) m.invoke(null, model);
 			Iterator<EventType> it = result.asClosableIterator();
 			if (it.hasNext()) {
 				event = it.next();
 			}
 		} catch (Exception e) {
 			throw new NoRdfEventException("Exception while instanciating event from RDF.", e);
 		}
 		
 		return event;
 	}
 
 	/**
 	 * Get the current subscribe endpoint.
 	 */
 	public String getDsbSubscribe() {
 		return this.dsbSubscribe;
 	}
 	
 	/**
 	 * Overwrite the subscribe endpoint.
 	 * 
 	 * @param dsbSubscribe
 	 */
 	public void setDsbSubscribe(String dsbSubscribe) {
 		this.dsbSubscribe = dsbSubscribe;
 	}
 	
 	/**
 	 * Get the current <b>un</b>subscribe endpoint.
 	 */
 	public String getDsbUnsubscribe() {
 		return this.dsbUnsubscribe;
 	}
 	
 	/**
 	 * Overwrite the <b>un</b>subscribe endpoint.
 	 * 
 	 * @param dsbUnsubscribe
 	 */
 	public void setDsbUnsubscribe(String dsbUnSubscribe) {
 		this.dsbUnsubscribe = dsbUnSubscribe;
 	}
 	
 	private final NamespaceContext nc = new NamespaceContext() {
 		@Override
 		public String getNamespaceURI(String prefix) {
 			if (prefix == null)
 				throw new NullPointerException("Null prefix");
 			else if (WSN_MSG_ELEMENT.getPrefix().equals(prefix))
 				return WSN_MSG_ELEMENT.getNamespaceURI();
 			else if ("xml".equals(prefix))
 				return XMLConstants.XML_NS_URI;
 			return XMLConstants.NULL_NS_URI;
 		}
 
 		// This method isn't necessary for XPath processing.
 		@Override
 		public String getPrefix(String uri) {
 			throw new UnsupportedOperationException();
 		}
 
 		// This method isn't necessary for XPath processing either.
 		@SuppressWarnings("rawtypes")
 		@Override
 		public Iterator getPrefixes(String uri) {
 			throw new UnsupportedOperationException();
 		}
 	};
 
 }
