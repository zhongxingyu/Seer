 package org.tedxuba.social.events.twitter;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.twitter.hbc.ClientBuilder;
 import com.twitter.hbc.core.Client;
 import com.twitter.hbc.core.HttpHosts;
 import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
 import com.twitter.hbc.core.processor.StringDelimitedProcessor;
 import com.twitter.hbc.httpclient.auth.Authentication;
 import com.twitter.hbc.httpclient.auth.OAuth1;
 
 public class TwitterTermsListener extends TwitterEventListener {
 	private final Logger logger = LoggerFactory
 			.getLogger(TwitterTermsListener.class);
 
 	private String twitterTerms;
 
 	private static final String TERM_EVENT = "term";
 
 	public TwitterTermsListener(String twitterTerms) {
 		this.twitterTerms = twitterTerms;
 	}
 
 	@Override
 	public void start() {
 		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
 
 		StatusesFilterEndpoint filterEndpoint = new StatusesFilterEndpoint();
 		List<String> terms = Lists.newArrayList(this.twitterTerms);
 		filterEndpoint.trackTerms(terms);
 
 		Authentication hosebirdAuth = new OAuth1("aeIBIUf1vVu4PWNvxlJVYg",
 				"BRoMmIAozI0Hi95D344c1tk6JdtoDnkiKtS7weyw",
 				"1915420819-XXj2cwXtUoSnA1E0ZQvSHg55BQYfZ2b8vBh3nUY",
 				"WE0tt69igf45tXeT5peyiCQTPj03cv5TJFpqKHq3w");
 
 		ClientBuilder builder = new ClientBuilder().name("Hosebird-Client-01")
 				.hosts(HttpHosts.STREAM_HOST).authentication(hosebirdAuth)
 				.endpoint(filterEndpoint)
 				.processor(new StringDelimitedProcessor(msgQueue));
 
 		Client hosebirdClient = builder.build();
 		hosebirdClient.connect();
 
 		while (!hosebirdClient.isDone()) {
 			try {
 				String msg;
 				msg = msgQueue.take();
				logger.debug(msg);
				setCountDiff(TERM_EVENT, 1);
				this.setChanged();
				this.notifyObservers();
 			} catch (Exception e) {
 				logger.error(e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public List<String> getEventNames() {
 		return Arrays.asList(new String[] { TERM_EVENT });
 	}
 }
