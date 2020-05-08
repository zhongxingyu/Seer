 package org.ow2.chameleon.rose.pubsubhubbub.constants;
 
 
 public class PubsubhubbubConstants {
 
 	public static final String RSS_EVENT_TOPIC = "org/ow2/chameleon/syndication";
 	public static final String FEED_TITLE_NEW = "Endpoint added";
 	public static final String FEED_TITLE_REMOVE = "Endpoint removed";
 	public static final String FEED_AUTHOR = "Rose RSS";
 	public static final String HTTP_POST_HEADER_TYPE = "application/x-www-form-urlencoded";
 	public static final String HTTP_POST_PARAMETER_HUB_MODE = "hub.mode";
 	public static final String HTTP_POST_PARAMETER_URL_CALLBACK = "hub.callback";
 	public static final String HTTP_POST_PARAMETER_RSS_TOPIC_URL = "hub.topic";
 	public static final String HTTP_POST_PARAMETER_ENDPOINT_FILTER = "hub.endp.filter";
 	public static final String HTTP_POST_UPDATE_SUBSTRIPCTION_OPTION = "hub.subscription";
 	public static final String HTTP_POST_UPDATE_CONTENT = "hub.content";
 	public static final String HUB_SUBSCRIPTION_UPDATE_ENDPOINT_ADDED = "endpoint.add";
 	public static final String HUB_SUBSCRIPTION_UPDATE_ENDPOINT_REMOVED = "endpoint.remove";
 	public static final String HUB_UPDATE_TOPIC_DELETE = "topic.remove";
 	public static final String DEFAULT_HTTP_PORT = "8080";
 
 	public enum HubMode {
 		publish, unpublish, update, subscribe, unsubscribe, getAllEndpoints;
 	}
 }
