 package pt.com.broker.client.sample;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.caudexorigo.cli.CliFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.client.BrokerClient;
 import pt.com.broker.client.CliArgs;
 import pt.com.broker.client.messaging.BrokerMessage;
 
 public class QueueSyncConsumer
 {
	private static final Logger log = LoggerFactory.getLogger(QueueSyncConsumer.class);
 	private final AtomicInteger counter = new AtomicInteger(0);
 
 	private String host;
 	private int port;
 	private String dname;
 
 	public static void main(String[] args) throws Throwable
 	{
 		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);
 
 		QueueSyncConsumer qsconsumer = new QueueSyncConsumer();
 
 		qsconsumer.host = cargs.getHost();
 		qsconsumer.port = cargs.getPort();
 		qsconsumer.dname = cargs.getDestination();
		
 		BrokerClient bk = new BrokerClient(qsconsumer.host, qsconsumer.port, "tcp://mycompany.com/mysniffer");
 
 		qsconsumer.receiveLoop(bk);
 	}
 
 	private void receiveLoop(BrokerClient bk) throws Throwable
 	{
 		while (true)
 		{
 			BrokerMessage m = bk.poll(dname);
 			log.info(String.format("%s -> Received Message: %s", counter.incrementAndGet(), m.textPayload));
 			bk.acknowledge(m);
 		}
 	}
 }
