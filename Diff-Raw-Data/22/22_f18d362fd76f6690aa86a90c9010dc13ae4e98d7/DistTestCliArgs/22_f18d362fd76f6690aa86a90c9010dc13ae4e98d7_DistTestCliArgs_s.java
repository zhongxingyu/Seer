 package pt.com.broker.performance.distributed;
 
 import org.caudexorigo.cli.Option;
 
 /**
  * Auxiliary interface that maps command line arguments.
  * 
  */
 
 public interface DistTestCliArgs
 {
 	/**
 	 * 
 	 * @return Host name.
 	 */
 	@Option(shortName = "h", defaultValue = "localhost")
 	String getHost();
 
 	/**
 	 * 
 	 * @return Host port.
 	 */
 	@Option(shortName = "p", defaultValue = "3323")
 	int getPort();
 
 	/**
 	 * 
 	 * @return Host UDP port.
 	 */
 	@Option(shortName = "u", defaultValue = "3323")
 	int getUdpPort();
 
 	/**
 	 * 
 	 * @return Destination name (e.g. "/test").
 	 */
 	@Option(shortName = "n", defaultValue = "/test")
 	String getDestination();
 
 	/**
 	 * 
 	 * @return Destination Type (TOPIC, QUEUE, VIRTUAL_QUEUE).
 	 */
 	@Option(shortName = "d", defaultValue = "TOPIC")
 	String getDestinationType();
 
 
 	/**
 	 * 
 	 * @return Message length. Useful for generate random messages with known size.
 	 */
 	@Option(shortName = "l", defaultValue = "-1")
 	int getMessageLength();
 	
 	/**
 	 * 
 	 * @return Number of Messages.
 	 */
 	@Option(shortName = "m", defaultValue = "-1")
 	int getNumberOfMessages();
 	
 	/**
 	 * 
 	 * @return Number of consumers.
 	 */
 	@Option(shortName = "c", defaultValue = "-1")
 	int getNumberOfConsumers();
 	
 	/**
 	 * 
 	 * @return Actor name (consumer1, producer3)
 	 */
 	@Option(shortName = "a", defaultValue = "")
 	String getActorName();
 
 	/**
 	 * 
 	 * @return Machine name (machine1)
 	 */
 	@Option(shortName = "M", defaultValue = "")
 	String getMachineName();
 }
