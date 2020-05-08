 package eu.stratosphere.meteor.common;
 
 /**
  * 
  * A collection of global constants important for clients and server.
  *
  * @author André Greiner-Petter
  *
  */
 public final class SchedulerConfigConstants {	
 	/**
 	 * The host address of DOPAScheduler. It's "localhost" by default.
 	 */
 	public static final String SCHEDULER_HOST_ADDRESS = "localhost";
 	
 	/**
 	 * This port is the default port of rabbitMQ. (RabbitMQ already runs on the scheduler system)
 	 * If we want to change this we have to change the configuration of rabbitMQ. To do this and 
 	 * for more informations look at:
 	 * http://www.rabbitmq.com/configure.html#config-items
 	 */
 	public static final int SCHEDULER_PORT = 5672;
 	
 	/**
 	 * The message priority to force the scheduler to reconnect the client.
 	 */
 	public static final int SCHEDULER_RECONNECT_PRIORITY = 10;
 	
 	/**
 	 * The exchange name to handle requests from clients.
 	 */
 	public static final String REQUEST_EXCHANGE = "dopa.scheduler.exchange.request";
 	
 	/**
 	 * The type of exchange for requests
 	 */
 	public static final String REQUEST_EXCHANGE_TYPE = "topic";
 	
 	/**
 	 * Durable exchanges survive broker (DOPAScheduler) restarts whereas
 	 * transient exchanges do not (the have to be redeclared when broker
 	 * comes back online).
 	 */
 	public static final boolean REQUEST_EXCHANGE_DURABLE = true;
 	
 	/**
 	 * True: rabbitMQ delete a message from the queue automatically after the scheduler peeked for it.
 	 * False: rabbitMQ server holds the message while the scheduler doesn't acknowledged it manually.
 	 * 
 	 * If (for any reason) we need to set this flag to false, please checkout the information box 
 	 * "Note on message persistance" at www.rabbitmq.com/tutorials/tutorial-two-java.html
 	 */
 	public static final boolean REQUEST_AUTO_ACKNOWLEDGES = true;
 	
 	/**
 	 * The routing key mask for jobs.
 	 * 
 	 * Routing Key: setJob.<clientID>.<jobID>
 	 */
 	public static final String JOB_KEY_MASK = "setJob.*.#";
 	
 	/**
 	 * The routing key mask for requests.
 	 * 
 	 * Routing Key: requestStatus
 	 */
 	public static final String REQUEST_KEY_MASK = "request";
 	
 	/**
 	 * The routing key mask for hand shakes. Use this to register a client at
 	 * the DOPAScheduler service. After you registered each client got a individually
 	 * status queue for jobs.
 	 * 
 	 * Routing Key: 
 	 * 		register.login
 	 * or	register.logoff
 	 */
 	public static final String REGISTER_KEY_MASK = "register.*";
 	
 	/**
 	 * Content type of json in a string format
 	 */
 	public static final String JSON = "application/json";
 	
 	/**
 	 * The maximum size of blocks the scheduler would send back to the client (in bytes).
 	 * It just allows to be an integer ( 2^32 ~ 4GB ) cause these blocks are just byte[].
 	 * By default you can set it to 100 * 1024 * 1024 = 100 MB!
 	 */
 	public static final int MAX_BLOCK_SIZE = 100 * 1024 * 1024;
 	
 	/**
 	 * The default configuration to submitting a job.
 	 */
	public static String[] EXECUTER_CONFIG = new String[]{ "--configDir", "/dopa-vm/stratosphere/conf", "--updateTime", "1000", "--wait" };
 	
 	/**
 	 * Generate a key by given queueName. This method warrant consistency.
 	 * @param clientName given name of a queue
 	 * @return the unique key for this queue
 	 */
 	public static String getRoutingKey( String clientName ){
 		return clientName;
 	}
 }
