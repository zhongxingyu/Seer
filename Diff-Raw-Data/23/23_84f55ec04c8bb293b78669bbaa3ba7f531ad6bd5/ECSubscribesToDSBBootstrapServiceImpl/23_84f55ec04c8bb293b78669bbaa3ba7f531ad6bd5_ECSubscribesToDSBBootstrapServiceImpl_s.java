 /**
  *
  * Copyright (c) 2012, PetalsLink
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package eu.playproject.platform.service.bootstrap;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.xml.namespace.QName;
 
 import org.ow2.play.metadata.api.Data;
 import org.ow2.play.metadata.api.MetadataException;
 import org.ow2.play.metadata.api.Resource;
 import org.ow2.play.metadata.api.service.MetadataService;
 
 import eu.playproject.commons.utils.StreamHelper;
 import eu.playproject.governance.api.GovernanceExeption;
 import eu.playproject.governance.api.bean.Metadata;
 import eu.playproject.governance.api.bean.Topic;
 import eu.playproject.platform.service.bootstrap.api.BootstrapFault;
 import eu.playproject.platform.service.bootstrap.api.BootstrapService;
 import eu.playproject.platform.service.bootstrap.api.EventCloudClientFactory;
 import eu.playproject.platform.service.bootstrap.api.GovernanceClient;
 import eu.playproject.platform.service.bootstrap.api.LogService;
 import eu.playproject.platform.service.bootstrap.api.Subscription;
 import eu.playproject.platform.service.bootstrap.api.SubscriptionRegistry;
 import eu.playproject.platform.service.bootstrap.api.TopicManager;
 import fr.inria.eventcloud.webservices.api.EventCloudManagementWsApi;
 
 /**
  * The EventCloud subscribes to the DSB topics. Just for the simple topics and
  * not the complex ones.
  * 
  * @author chamerling
  * 
  */
 public class ECSubscribesToDSBBootstrapServiceImpl implements BootstrapService {
 
 	private static Logger logger = Logger
 			.getLogger(ECSubscribesToDSBBootstrapServiceImpl.class.getName());
 
 	private TopicManager topicManager;
 
 	private EventCloudClientFactory eventCloudClientFactory;
 
 	private GovernanceClient governanceClient;
 
 	private SubscriptionRegistry subscriptionRegistry;
 
 	private MetadataService metadataServiceClient;
 
 	/*
 	 * Provider is the DSB. subscriber is the EC.
 	 */
 	public List<Subscription> bootstrap(String providerEndpoint,
 			String subscriberEndpoint) throws BootstrapFault {
 		logger.info("Init all EC subscribes to DSB");
 
 		List<Subscription> result = new ArrayList<Subscription>();
 
 		List<Topic> topics = governanceClient.getTopics();
 
 		if (topics == null || topics.size() == 0) {
 			throw new BootstrapFault("Can not get any topic");
 		}
 
 		for (Topic topic : topics) {
 			logger.info("Create stuff for topic " + topic);
 
 			Subscription bean = createResources(topic, providerEndpoint,
 					subscriberEndpoint);
 			if (bean != null) {
 				result.add(bean);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Provider is the DSB, subscriber is the EC.
 	 * 
 	 * @param topic
 	 * @param providerEndpoint
 	 * @param subscriberEndpoint
 	 * @return
 	 */
 	private Subscription createResources(Topic topic, String providerEndpoint,
 			String subscriberEndpoint) {
 
 		LogService log = MemoryLogServiceImpl.get();
 
 		Subscription result = null;
 
 		EventCloudManagementWsApi client = eventCloudClientFactory
 				.getClient(subscriberEndpoint);
 
 		QName topicName = new QName(topic.getNs(), topic.getName(),
 				topic.getPrefix());
 
 		String stream = StreamHelper.getStreamName(topicName);
 		List<String> publishURLs = client.getPublishProxyEndpointUrls(stream);
 
 		logger.info("Got some URLs back from the EC : " + publishURLs);
 
 		String subscriber = (publishURLs != null && publishURLs.size() > 0) ? publishURLs
 				.get(0) : null;
 
 		if (subscriber == null) {
 			log.log("Can not find any valid endpoint from EC for stream %s",
 					stream);
 			logger.info("Can not get any valid EC endpoint");
 			return null;
 		}
 
 		logger.info("Let's use the EC endpoint at : " + subscriber);
 
 		// check if we already subscribed...
 		if (alreadySubscribed(topic, subscriber, providerEndpoint)) {
 			log.log("EC already subscribed to DSB for topic %s", topic);
 			logger.info(String.format(
 					"EC at %s already subscribed to topic %s", topic));
 			return result;
 		}
 
		if (needsToSubscribe(topic)) {
 			// send the subscribe to the event cloud on behalf of the DSB
 			try {
 				logger.info("Subscribe for topic " + topic);
 				result = topicManager.subscribe(providerEndpoint, topicName,
 						subscriber);
 				log.log("EC subscribed to DSB : " + result);
 			} catch (BootstrapFault e) {
 				e.printStackTrace();
 			}
 		} else {
 			log.log("Do not need to subscribe EC->DSB for stream %s", stream);
 			logger.info("No need to subscribe EC->DSB for the topic " + topic);
 		}
 
 		return result;
 	}
 
	protected boolean needsToSubscribe(Topic topic) {
		// only subscribe if this is not a topic the DSB needs to subscribe...

		List<Metadata> filter = new ArrayList<Metadata>();
		Metadata meta = new Metadata();
		meta.setName("dsbneedstosubscribe");
		meta.setValue("true");
		filter.add(meta);

		List<Topic> topics = null;
		try {
			topics = this.governanceClient.getTopicsWithMeta(filter);
		} catch (GovernanceExeption e) {
			e.printStackTrace();
			return false;
		}

		return topics != null && !topics.contains(topic);
	}

 	/**
 	 * Subscribes only if the dsbneedstosubscribe metadata is not present or set
 	 * to false.
 	 * 
 	 * @param stream
 	 * @return
 	 */
 	protected boolean needsToSubscribe(String stream) {
 		if (stream == null) {
 			return false;
 		}
 
 		Resource r = new Resource("stream", stream);
 		org.ow2.play.metadata.api.Metadata metadata = null;
 		try {
 			metadata = metadataServiceClient.getMetadataValue(r,
 					"http://www.play-project.eu/xml/ns/dsbneedstosubscribe");
 		} catch (MetadataException e) {
 			return false;
 		}
 		return metadata == null
 				|| metadata.getData().contains(new Data("literal", "false"));
 	}
 
 	/**
 	 * Event cloud is the subscriber, dsb is the provider.
 	 * 
 	 * @param topic
 	 * @param eventCloudEndpoint
 	 * @param dsbEndpoint
 	 * @return
 	 */
 	protected boolean alreadySubscribed(Topic topic, String eventCloudEndpoint,
 			String dsbEndpoint) {
 
 		List<Subscription> subscriptions = this.subscriptionRegistry
 				.getSubscriptions();
 
 		Iterator<Subscription> iter = subscriptions.iterator();
 		boolean found = false;
 		while (iter.hasNext() && !found) {
 			Subscription subscription = iter.next();
 			found = subscription.getTopic().equals(topic)
 					&& subscription.getSubscriber().equals(eventCloudEndpoint);
 		}
 
 		return found;
 	}
 
 	public void setTopicManager(TopicManager topicManager) {
 		this.topicManager = topicManager;
 	}
 
 	public void setEventCloudClientFactory(
 			EventCloudClientFactory eventCloudClientFactory) {
 		this.eventCloudClientFactory = eventCloudClientFactory;
 	}
 
 	public void setGovernanceClient(GovernanceClient governanceClient) {
 		this.governanceClient = governanceClient;
 	}
 
 	public void setSubscriptionRegistry(
 			SubscriptionRegistry subscriptionRegistry) {
 		this.subscriptionRegistry = subscriptionRegistry;
 	}
 
 	/**
 	 * @param metadataServiceClient
 	 *            the metadataServiceClient to set
 	 */
 	public void setMetadataServiceClient(MetadataService metadataServiceClient) {
 		this.metadataServiceClient = metadataServiceClient;
 	}
 
 }
