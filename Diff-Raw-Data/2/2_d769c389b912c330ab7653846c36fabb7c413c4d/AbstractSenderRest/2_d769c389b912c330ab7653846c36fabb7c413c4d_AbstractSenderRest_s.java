 package eu.play_project.play_eventadapter;
 
 import static eu.play_project.play_commons.constants.Event.WSN_MSG_DEFAULT_SYNTAX;
 
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.Form;
 import javax.ws.rs.core.MultivaluedHashMap;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.xml.namespace.QName;
 
 import org.event_processing.events.types.Event;
 import org.ontoware.rdf2go.model.Model;
 import org.ow2.play.governance.platform.user.api.rest.PublishService;
 import org.slf4j.LoggerFactory;
 
 import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 
 /**
  * A sender of PLAY events. It is configured to publish events at a fixed
  * RESTful HTTP endpoint under varying event topics (streams).
  * 
  * @author Roland Stühmer
  */
 public class AbstractSenderRest {
 	
 	/** Credentials for publishing events to PLAY Platform */
 	private final String PLAY_PLATFORM_APITOKEN = Constants.getProperties("play-eventadapter.properties").getProperty(
 			"play.platform.api.token");
 
 	private final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractSenderRest.class);
 	private String defaultTopic;
 	private Boolean online = true;
 	private final Client client;
 	private final WebTarget notifyTarget;
 	
 	/**
 	 * Construct a sender with a default topic to create messages and with a
 	 * given endpoint to publish messages.
 	 */
 	public AbstractSenderRest(String defaultTopic, String notifyEndpoint) {
 		this.defaultTopic = defaultTopic;
 		this.client = ClientBuilder.newClient();
 		this.notifyTarget = client.target(notifyEndpoint);
 		
 		if (PLAY_PLATFORM_APITOKEN.isEmpty()) {
 			logger.warn("API token from properties file is empty. You will probably not be authenticated to send events.");
 		}
 		else if (PLAY_PLATFORM_APITOKEN.startsWith("$")) {
			logger.warn("API token from properties file is an unexapanded '$variable'. You will probably not be authenticated to send events.");
 		}
 	}
 	
 	/**
 	 * Construct a sender with a default topic to create messages and with a
 	 * given endpoint to publish messages.
 	 */
 	public AbstractSenderRest(QName defaultTopic, String notifyEndpoint) {
 		this(defaultTopic.getNamespaceURI() + defaultTopic.getLocalPart(), notifyEndpoint);
 	}
 	
 	/**
 	 * Construct a sender with a default topic to create messages.
 	 * 
 	 * Messages will be sent to the endpoint defined in the PLAY properties in
 	 * {@link Constants#getProperties()}.
 	 */
 	public AbstractSenderRest(String defaultTopic) {
 		this(defaultTopic, Constants.getProperties().getProperty(
 				"play.platform.endpoint") + PublishService.PATH);
 	}
 
 	/**
 	 * Construct a sender with a default topic to create messages.
 	 * 
 	 * Messages will be sent to the endpoint defined in the PLAY properties
 	 * files.
 	 */
 	public AbstractSenderRest(QName defaultTopic) {
 		this(defaultTopic.getNamespaceURI() + defaultTopic.getLocalPart());
 	}
 	
 	/**
 	 * Send an {@linkplain Event} to the default Topic.
 	 */
 	public void notify(Event event) {
 		notify(event, this.defaultTopic);
 	}
 
 	/**
 	 * Send an {@linkplain Event} to a specific topic.
 	 */
 	public void notify(Event event, String topicUsed) {
 		notify(event.getModel(), topicUsed);
 	}
 
 	/**
 	 * Send a {@linkplain Model} to the default topic.
 	 */
 	public void notify(Model model) {
 		notify(model, this.defaultTopic);
 	}
 
 	/**
 	 * Send a {@linkplain Model} to a specific topic.
 	 */
 	public void notify(Model model, String topicUsed) {
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		EventHelpers.write(stream, model);
 		try {
 			notify(stream.toString("UTF-8"), topicUsed);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Send a {@linkplain String} payload to the default topic.
 	 * 
 	 * The payload must be formatted in the default RDF syntax from
 	 * {@linkplain eu.play_project.play_commons.constants.Event#WSN_MSG_DEFAULT_SYNTAX}
 	 * .
 	 */
 	public void notify(String notifPayload) {
 		notify(notifPayload, this.defaultTopic);
 	}
 
 	/**
 	 * Send a {@linkplain String} payload to a specific topic.
 	 * 
 	 * The payload must be formatted in the default RDF syntax from
 	 * {@linkplain eu.play_project.play_commons.constants.Event#WSN_MSG_DEFAULT_SYNTAX}
 	 * .
 	 */
 	public void notify(String notifPayload, String topicUsed) {
 		notify(notifPayload, topicUsed, WSN_MSG_DEFAULT_SYNTAX);
 	}
 	
 	/**
 	 * Send a {@linkplain String} payload to a specific topic.
 	 */
 	public void notify(String notifPayload, String topicUsed, String notifMediatype) {
 		
 		if (topicUsed.endsWith(Stream.STREAM_ID_SUFFIX)) {
 			logger.warn("Topic ends in suffix {}. You should use a topic name without the suffix.", Stream.STREAM_ID_SUFFIX);
 		}
 		
 		// See class org.ow2.play.governance.platform.user.api.rest.bean.Notification for available fields:
 		MultivaluedMap<String, String> data = new MultivaluedHashMap<String, String>();
 		data.add("resource", topicUsed + Stream.STREAM_ID_SUFFIX);
 		data.add("message", notifPayload);
 		data.add("messageMediatype", notifMediatype);
 		
 		Entity<Form> entity = Entity.form(data);
 		
 		if (online) {
 			Response response = notifyTarget.request()
 				  .header("Authorization", "Bearer " + PLAY_PLATFORM_APITOKEN)
 				  .buildPost(entity)
 				  .invoke();
 			
 			if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL){
 				logger.error(String.format("No event was notified because of response status %s %s, Topic: '%s', DSB: '%s'", response.getStatus(), response.getStatusInfo(), topicUsed, this.notifyTarget.getUri()));
 			}
 			else {
 				logger.debug("Response status: "+response.getStatus());
 			}
 			
 			response.close();
 		}
 	}
 
 	/**
 	 * Get the current notify endpoint.
 	 */
 	public String getNotifyEndpoint() {
 		return this.notifyTarget.getUri().toString();
 	}
 	
 	/**
 	 * Set the default topic to be used when no topic is specified with a
 	 * {@code notify} method.
 	 * 
 	 * @param defaultTopic
 	 */
 	public void setDefaultTopic(String defaultTopic) {
 		if (defaultTopic == null) {
 			throw new NullPointerException("defaultTopic may not be null");
 		}
 		this.defaultTopic = defaultTopic;
 	}
 
 	/**
 	 * For debugging purposes: do not actually send a notification.
 	 */
 	public void setNoNetworking(Boolean offline) {
 		this.online = !offline;
 	}
 	
 	@Override
 	public void finalize() {
 		client.close();
 	}
 }
