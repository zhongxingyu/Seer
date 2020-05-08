 package redis;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPool;
 import redis.clients.jedis.JedisPoolConfig;
 import redis.clients.jedis.JedisPubSub;
 import static org.testng.Assert.*;
 
 /**
  * @author Matteo Moci ( matteo (dot) moci (at) gmail (dot) com ), Gabriele de
  *         Capoa, Daniele Morgantini
  */
 public class RedisPublisherIntegrationTestCase {
 
 	private JedisPool pool;
 	private Jedis jedis_publisher;
 	private Jedis jedis_subscriber;
 	private RedisStringPublisher redisPublisher;
 	private Subscriber subscriber;
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(RedisPublisherIntegrationTestCase.class);
 
 	@BeforeClass
 	public void setUp() {
 
 		this.pool = new JedisPool(new JedisPoolConfig(), "localhost");
 		this.jedis_publisher = this.pool.getResource();
 		this.jedis_subscriber = this.pool.getResource();
 		this.redisPublisher = new RedisStringPublisher(this.jedis_publisher);
 		this.subscriber = new Subscriber();
 
 		assertTrue(redisIsRunning());
 	}
 
 	private Boolean redisIsRunning() {
 		String pingResponse = this.jedis_publisher.ping();
 		LOGGER.info("Ping Redis: "+ pingResponse);
 		return (pingResponse.equals("PONG"));
 	}
 
 	@Test
 	public void shouldPublishAndConsumeOnRedis() {
 		final String channel = "test";
		
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
					LOGGER.info("Subscription to channel. This thread will be blocked");
 					jedis_subscriber.subscribe(subscriber, channel);
 					LOGGER.info("Subscription ended.");
 				} catch (Exception e) {
 					LOGGER.error("Subscribing failed.", e);
 				}
 			}
 		}).start();
 		
		this.redisPublisher.publish(channel, "1");
		this.redisPublisher.publish(channel, "2");
		this.redisPublisher.publish(channel, "3");
 		
		this.subscriber.unsubscribe();
	} 
 
 	@AfterClass
 	public void tearDown() {
 		this.pool.returnResource(this.jedis_publisher);
 		this.pool.returnResource(this.jedis_subscriber);
 		this.pool.destroy();
 	}
 
 }
 
 class Subscriber extends JedisPubSub {
 
	private final static Logger logger = LoggerFactory.getLogger(Subscriber.class);
 
 	@Override
 	public void onMessage(String channel, String message) {
 		logger.info("Message received. Channel: {}, Msg: {}", channel, message);
 	}
 
 	@Override
 	public void onPMessage(String pattern, String channel, String message) {
 
 	}
 
 	@Override
 	public void onSubscribe(String channel, int subscribedChannels) {
 
 	}
 
 	@Override
 	public void onUnsubscribe(String channel, int subscribedChannels) {
 
 	}
 
 	@Override
 	public void onPUnsubscribe(String pattern, int subscribedChannels) {
 
 	}
 
 	@Override
 	public void onPSubscribe(String pattern, int subscribedChannels) {
 
 	}
 }
