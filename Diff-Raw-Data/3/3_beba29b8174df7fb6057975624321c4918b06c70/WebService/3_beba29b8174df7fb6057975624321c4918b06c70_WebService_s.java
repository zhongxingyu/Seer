 package controllers;
 
 import play.Logger;
 import play.Play;
 import play.data.validation.*;
 import play.libs.F.IndexedEvent;
 import play.libs.F.Promise;
 import play.libs.IO;
 import play.libs.WS;
 import play.libs.XML;
 import play.libs.XPath;
 import play.mvc.*;
 import play.mvc.Http.Header;
 import play.mvc.Http.StatusCode;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.SecureRandom;
 
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import models.BoyerMoore;
 import models.EventTopic;
 import models.ModelManager;
 import models.SupportedTopicsXML;
 import models.User;
 
 import org.event_processing.events.types.FacebookStatusFeedEvent;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.ontoware.rdf2go.RDF2Go;
 import org.ontoware.rdf2go.exception.ModelRuntimeException;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.Syntax;
 import org.ontoware.rdf2go.model.node.impl.URIImpl;
 import org.opensaml.saml1.core.impl.RequestAbstractTypeMarshaller;
 import org.petalslink.dsb.notification.client.http.HTTPNotificationConsumerClient;
 import org.petalslink.dsb.notification.client.http.HTTPNotificationProducerClient;
 import org.petalslink.dsb.notification.client.http.HTTPNotificationProducerRPClient;
 import org.petalslink.dsb.notification.commons.NotificationException;
 import org.petalslink.dsb.notification.commons.NotificationHelper;
 import org.w3c.dom.Document;
 
 import com.ebmwebsourcing.easycommons.xml.XMLHelper;
 import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscribeResponse;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.refinedabstraction.RefinedWsnbFactory;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.topics.datatypes.api.WstopConstants;
 import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationProducer;
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationProducerRP;
 import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
 import com.ebmwebsourcing.wsstar.wsrfbf.services.faults.AbsWSStarFault;
 import com.google.gson.reflect.TypeToken;
 import com.hp.hpl.jena.graph.Triple;
 
import fr.inria.eventcloud.api.Collection;
 import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
 import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;
 import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslatorImpl;
 
 /**
  * The WebService controller is in charge of SOAP connection with the DSB.
  * 
  * @author Alexandre Bourdin
  * 
  */
 public class WebService extends Controller {
 	// DSB adress to subscribe to
 	public static String dsbSubscribe = "http://94.23.221.97:8084/petals/services/NotificationProducerPortService";
 	// public static String dsbNotify =
 	// "http://94.23.221.97:8084/petals/services/NotificationConsumerPortService";
 	public static String dsbNotify = "http://www.postbin.org/y83a5d";
 
 	static {
 		Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(), new WsrfrModelFactoryImpl(),
 				new WsrfrlModelFactoryImpl(), new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
 				new WsnbModelFactoryImpl());
 	}
 
 	/**
 	 * SOAP endpoint to receive WS-Notifications from the DSB.
 	 * 
 	 * @param topicId
 	 *            : necessary to have a unique endpoint for each topic.
 	 */
 	public static void soapNotifEndPoint(String topicId) {
 		WsNotificationTranslator translator = new WsNotificationTranslatorImpl();
 		URI eventId = generateRandomUri();
 		Event event = translator.translateWsNotifNotificationToEvent(request.body,
 				inputStreamFrom("public/xml/xsd-01.xml"), eventId);
 
 		Collection<Triple> triples = event.getTriples();
 		String title = "Error";
 		String content = "Error";
 		for (Triple t : triples) {
 			String predicate = t.getPredicate().toString();
 			if (BoyerMoore.match("Topic", predicate).size() > 0) {
 				title = "Topic: " + t.getObject().toString();
 			}
 			if (BoyerMoore.match("emissionDate", predicate).size() > 0) {
 				content = "emissionDate: " + t.getObject().toString();
 			}
 		}
 		ModelManager.get().getTopicById(topicId).multicast(new models.Event(title, content));
 	}
 
 	/**
 	 * Sends a request to the DSB to get the list of supported topics
 	 */
 	@Util
 	public static ArrayList<EventTopic> getSupportedTopics() {
 		INotificationProducerRP resourceClient = new HTTPNotificationProducerRPClient(dsbSubscribe);
 		try {
 			QName qname = WstopConstants.TOPIC_SET_QNAME;
 			com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.GetResourcePropertyResponse response = resourceClient
 					.getResourceProperty(qname);
 			Document dom = Wsnb4ServUtils.getWsrfrpWriter().writeGetResourcePropertyResponseAsDOM(response);
 
 			ArrayList<EventTopic> topics = new ArrayList<EventTopic>();
 			String domString = XMLHelper.createStringFromDOMDocument(dom);
 
 			SAXBuilder sxb = new SAXBuilder();
 			org.jdom.Document xml = new org.jdom.Document();
 			org.jdom.Element root = null;
 			try {
 				xml = sxb.build(new StringReader(domString));
 				root = xml.getRootElement();
 			} catch (Exception e) {
 				Logger.error("jDom : Error while parsing XML document");
 				e.printStackTrace();
 			}
 			SupportedTopicsXML.parseXMLTree(topics, root, "");
 			return topics;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Subscription action Forwars the subscription to the DSB
 	 * 
 	 * @param et
 	 */
 	@Util
 	public static int subscribe(EventTopic et) {
 		String ep = "http://demo.play-project.eu/webservice/soapnotifendpoint/" + et.getId();
 		QName topic = new QName(et.uri, et.name, et.namespace);
 		Subscribe subscribe;
 		try {
 			subscribe = NotificationHelper.createSubscribe(ep, topic);
 			INotificationProducer producerClient = new HTTPNotificationProducerClient(dsbSubscribe);
 			try {
 				SubscribeResponse response = producerClient.subscribe(subscribe);
 
 				// System.out.println("Got a response from the DSB");
 				// Document dom =
 				// Wsnb4ServUtils.getWsnbWriter().writeSubscribeResponseAsDOM(response);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} catch (NotificationException e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/**
 	 * Notify action triggered by buttons on the web interface Generates an
 	 * event and sends it to the DSB on the specified topic
 	 * 
 	 * @param name
 	 * @param status
 	 * @param location
 	 * @param topic
 	 */
 	public static void notif(String name, String status, String location, String topic) {
 		Model model = RDF2Go.getModelFactory().createModel(new URIImpl("http://www.inria.fr"));
 		model.open();
 		model.setNamespace("", "http://events.event-processing.org/types/");
 		model.setNamespace("e", "http://events.event-processing.org/ids/");
 		model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
 		model.setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
 		model.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
 		model.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
 		FacebookStatusFeedEvent e2 = new FacebookStatusFeedEvent(model,
 				"http://events.event-processing.org/ids/e2#event", true);
 		e2.setName(name);
 		e2.setStatus(status);
 		e2.setLocation(location);
 		e2.setEndTime(javax.xml.bind.DatatypeConverter.parseDateTime("2011-08-24T14:42:01.011"));
 		String modelString = model.serialize(Syntax.RdfXml);
 
 		String producerAddress = "http://localhost:9998/foo/Producer";
 		String endpointAddress = "http://localhost:9998/foo/Endpoint";
 		String uuid = UUID.randomUUID().toString();
 
 		QName topicUsed = new QName("http://dsb.petalslink.org/notification", "Sample", "dsbn");
 		String dialect = WstopConstants.CONCRETE_TOPIC_EXPRESSION_DIALECT_URI.toString();
 		try {
 			Document notifPayload = XMLHelper.createDocumentFromString(modelString);
 			Notify notify;
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			dbf.setNamespaceAware(true);
 			notify = NotificationHelper.createNotification(producerAddress, endpointAddress, uuid, topicUsed,
 					dialect, notifPayload);
 			Document dom = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
 			XMLHelper.writeDocument(dom, System.out);
 			INotificationConsumer consumerClient = new HTTPNotificationConsumerClient(dsbNotify);
 			consumerClient.notify(notify);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void notifTest() {
 		String producerAddress = "http://localhost:9998/foo/Producer";
 		String endpointAddress = "http://localhost:9998/foo/Endpoint";
 		String uuid = UUID.randomUUID().toString();
 
 		QName topicUsed = new QName("http://dsb.petalslink.org/notification", "NuclearUC", "tns");
 		String dialect = WstopConstants.CONCRETE_TOPIC_EXPRESSION_DIALECT_URI.toString();
 		// TODO initialize notifPayload otherwise
 		Document notifPayload = null;
 		Notify notify;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		dbf.setNamespaceAware(true);
 		try {
 			notify = NotificationHelper.createNotification(producerAddress, endpointAddress, uuid, topicUsed,
 					dialect, notifPayload);
 			Document dom = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
 			XMLHelper.writeDocument(dom, System.out);
 			INotificationConsumer consumerClient = new HTTPNotificationConsumerClient(dsbNotify);
 			consumerClient.notify(notify);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Puts the content of a file on an InputStream
 	 * 
 	 * @param file
 	 * @return
 	 */
 	@Util
 	private static InputStream inputStreamFrom(String file) {
 		InputStream is = null;
 
 		if (file != null) {
 			try {
 				is = new FileInputStream(Play.getFile(file));
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return is;
 	}
 
 	@Util
 	private static URI generateRandomUri() {
 		String legalChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
 
 		StringBuilder result = new StringBuilder("http://www.inria.fr/");
 		SecureRandom random = new SecureRandom();
 
 		for (int i = 0; i < 20; i++) {
 			result.append(random.nextInt(legalChars.length()));
 		}
 
 		try {
 			return new URI(result.toString());
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
