 package org.ow2.chameleon.rose.pubsubhubbub.publisher.internal;
 
 import static org.osgi.framework.FrameworkUtil.createFilter;
 import static org.osgi.service.log.LogService.LOG_INFO;
 import static org.osgi.service.log.LogService.LOG_WARNING;
 import static org.ow2.chameleon.rose.RoseMachine.ENDPOINT_LISTENER_INTEREST;
 import static org.ow2.chameleon.rose.RoseMachine.EndpointListerInterrest.LOCAL;
 import static org.ow2.chameleon.rose.constants.RoseRSSConstants.FEED_AUTHOR;
 import static org.ow2.chameleon.rose.constants.RoseRSSConstants.FEED_TITLE_NEW;
 import static org.ow2.chameleon.rose.constants.RoseRSSConstants.FEED_TITLE_REMOVE;
 import static org.ow2.chameleon.rose.constants.RoseRSSConstants.RSS_EVENT_TOPIC;
 import static org.ow2.chameleon.rose.pubsubhubbub.publisher.Publisher.COMPONENT_NAME;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.apache.felix.ipojo.Factory;
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Property;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.EndpointListener;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 import org.ow2.chameleon.json.JSONService;
 import org.ow2.chameleon.rose.RoseMachine;
import org.ow2.chameleon.rose.constants.RoseRSSConstants;
 import org.ow2.chameleon.rose.pubsubhubbub.publisher.Publisher;
 import org.ow2.chameleon.rose.util.DefaultLogService;
 import org.ow2.chameleon.syndication.FeedEntry;
 import org.ow2.chameleon.syndication.FeedReader;
 import org.ow2.chameleon.syndication.FeedWriter;
 
 /**
  * Tracking and publish RSS feed for local endpoints, send events to webcosole
  * plugin.
  * 
  * @author Bartek
  * 
  */
 @Component(name = COMPONENT_NAME)
 public class EndpointTrackerRSS implements Publisher, EndpointListener {
 
 	private static final String SERVLET_FACTORY_FILTER = "(&("
 			+ Constants.OBJECTCLASS
 			+ "=org.apache.felix.ipojo.Factory)(factory.name=org.ow2.chameleon.syndication.rome.servlet))";
 	private static final String WRITER_SERVICE_CLASS = "org.ow2.chameleon.syndication.FeedWriter";
 	private static final String WRITER_FILER_PROPERTY = "org.ow2.chameleon.syndication.feed.url";
 
 	@Property(mandatory = true, name = INSTANCE_PROPERTY_RSS_URL)
 	private String rssUrl;
 
 	@Property(name = INSTANCE_PROPERTY_HUB_URL)
 	private String hubUrl;
 
 	@Requires(optional = true, defaultimplementation = DefaultLogService.class)
 	private LogService logger;
 
 	@Requires
 	private JSONService json;
 
 	@Requires(optional = true)
 	private EventAdmin eventAdmin;
 
 	@Requires
 	private RoseMachine rose;
 
 	private FeedWriter writer;
 	private final BundleContext context;
 	private ServiceRegistration endpointListener;
 	private ServiceTracker factoryTracker;
 	private ServiceTracker feedWriterTracker;
 	private Dictionary<String, Object> instanceDictionary;
 	private Map<String, Object> eventProperties;
 	private Event event;
 	private HubPublisher hubPublisher;
 
 	public EndpointTrackerRSS(final BundleContext pContext) {
 		super();
 		this.context = pContext;
 	}
 
 	@Override
 	@Validate
 	public final void start() throws IOException {
 
 		// tracking an FeedWriter and Feed servlet factory
 		startTracking();
 
 		// Configure an event properties
 		eventProperties = new HashMap<String, Object>();
 		eventProperties.put(FeedReader.ENTRY_AUTHOR_KEY, FEED_AUTHOR);
 		eventProperties.put(FeedReader.ENTRY_URL_KEY, rssUrl);
 
 		hubPublisher = new HubPublisher(hubUrl, rssUrl, context, rose, logger);
 
 		final Dictionary<String, Object> props = new Hashtable<String, Object>();
 		props.put(ENDPOINT_LISTENER_INTEREST, LOCAL);
 		// Register an EndpointListener
 		endpointListener = context.registerService(
 				EndpointListener.class.getName(), this, props);
 		logger.log(LOG_INFO, "EndpointTrackerRSS sucessfully started");
 
 	}
 
 	@Override
 	@Invalidate
 	public final void stop() {
 		endpointListener.unregister();
 		if (factoryTracker != null) {
 			factoryTracker.close();
 		}
 
 		if (feedWriterTracker != null) {
 			feedWriterTracker.close();
 		}
 		if (hubPublisher != null) {
 			hubPublisher.unregister();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.service.remoteserviceadmin.EndpointListener#endpointAdded(org
 	 * .osgi.service.remoteserviceadmin.EndpointDescription, java.lang.String)
 	 */
 	public final void endpointAdded(final EndpointDescription endp,
 			final String filter) {
 		if (writer == null) {
 			logger.log(LOG_WARNING,
 					"Rss feed not published, Rss writer not found");
 			return;
 		}
 		final FeedEntry feed = writer.createFeedEntry();
 		feed.title(FEED_TITLE_NEW);
 		feed.content(json.toJSON(endp.getProperties()));
 		feed.url(rssUrl);
 		try {
 			// publish a feed
 			writer.addEntry(feed);
 			// sending an event
 			sendEndpointEvent(FEED_TITLE_NEW, json.toJSON(endp.getProperties()));
 
 			hubPublisher.update();
 		} catch (IOException e) {
 			logger.log(LOG_WARNING, "Error in updateing a feed", e);
 		} catch (Exception e) {
 			logger.log(LOG_WARNING, "Error in sending a feed", e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.service.remoteserviceadmin.EndpointListener#endpointRemoved(
 	 * org.osgi.service.remoteserviceadmin.EndpointDescription,
 	 * java.lang.String)
 	 */
 	public final void endpointRemoved(final EndpointDescription endp,
 			final String arg1) {
 		if (writer == null) {
 			return;
 		}
 		final FeedEntry feed = writer.createFeedEntry();
 		feed.title(FEED_TITLE_REMOVE);
 		feed.content(json.toJSON(endp.getProperties()));
 		try {
 			// publish a feed
 			writer.addEntry(feed);
 			// sending an event
 			sendEndpointEvent(FEED_TITLE_REMOVE,
 					json.toJSON(endp.getProperties()));
 
 			hubPublisher.update();
 		} catch (IOException e) {
 			logger.log(LOG_WARNING, "Error in updateing a feed", e);
 		} catch (Exception e) {
 			logger.log(LOG_WARNING, "Error in sending a feed", e);
 		}
 
 	}
 
 	/**
 	 * Sends an event to {@link RoseRSSConstants.RSS_EVENT_TOPIC}.
 	 * 
 	 * @param title
 	 *            event title
 	 * @param content
 	 *            event content
 	 */
 	private void sendEndpointEvent(final String title, final String content) {
 		// check if eventAdmin service is available
 		if (eventAdmin != null) {
 			// prepare event properties
 			eventProperties.put(FeedReader.ENTRY_TITLE_KEY, title);
 			eventProperties.put(FeedReader.ENTRY_CONTENT_KEY, content);
 			eventProperties.put(EventConstants.TIMESTAMP,
 					System.currentTimeMillis());
 
 			// create and send a event
 			event = new Event(RSS_EVENT_TOPIC, eventProperties);
 			eventAdmin.sendEvent(event);
 		}
 	}
 
 	/**
 	 * Run trackers for Feed writer and Feed writer factories.
 	 */
 	private void startTracking() {
 		try {
 			new FeedWriterTracker();
 			new FactoryTracker();
 		} catch (InvalidSyntaxException e) {
 			logger.log(LogService.LOG_ERROR, "Tracker not stared", e);
 		}
 	}
 
 	/**
 	 * Tracker for writer factory.
 	 * 
 	 * @author Bartek
 	 * 
 	 */
 	private class FactoryTracker implements ServiceTrackerCustomizer {
 
 		/**
 		 * Set instance properties and run a tracker.
 		 * 
 		 * @throws InvalidSyntaxException
 		 *             exception
 		 */
 		public FactoryTracker() throws InvalidSyntaxException {
 
 			instanceDictionary = new Hashtable<String, Object>();
 			instanceDictionary.put(FeedReader.FEED_TITLE_PROPERTY, "RoseRss");
 			instanceDictionary.put(
 					"org.ow2.chameleon.syndication.feed.servlet.alias", rssUrl);
 
 			factoryTracker = new ServiceTracker(context,
 					createFilter(SERVLET_FACTORY_FILTER), this);
 			factoryTracker.open();
 		}
 
 		public Object addingService(final ServiceReference reference) {
 			final Factory factory = (Factory) context.getService(reference);
 			try {
 				if (writer == null) {
					factory.createComponentInstance(instanceDictionary);
 				}
 			} catch (Exception e) {
 				logger.log(LogService.LOG_ERROR,
 						"Error in adding a feed writer", e);
 			}
			return factory;
 		}
 
 		public void modifiedService(final ServiceReference reference,
 				final Object service) {
 			return;
 		}
 
 		public void removedService(final ServiceReference reference,
 				final Object service) {
 			writer = null;
 		}
 	}
 
 	/**
 	 * Tracker for Feed writer.
 	 * 
 	 * @author Bartek
 	 * 
 	 */
 	private class FeedWriterTracker implements ServiceTrackerCustomizer {
 
 		/**
 		 * Set a filter properties and run feed reader tracker.
 		 * 
 		 * @throws InvalidSyntaxException
 		 *             exception
 		 */
 		public FeedWriterTracker() throws InvalidSyntaxException {
 
 			String writerFilter = ("(&(" + Constants.OBJECTCLASS + "="
 					+ WRITER_SERVICE_CLASS + ")(" + WRITER_FILER_PROPERTY
 					+ "=http:*" + rssUrl + "))");
 			feedWriterTracker = new ServiceTracker(context,
 					createFilter(writerFilter), this);
 			feedWriterTracker.open();
 
 		}
 
 		public Object addingService(final ServiceReference reference) {
 			writer = (FeedWriter) context.getService(reference);
 			return writer;
 		}
 
 		public void modifiedService(final ServiceReference reference,
 				final Object service) {
 			writer = (FeedWriter) context.getService(reference);
 		}
 
 		public void removedService(final ServiceReference reference,
 				final Object service) {
 			context.ungetService(reference);
 			writer = null;
 		}
 
 	}
 
 }
