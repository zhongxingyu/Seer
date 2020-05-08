 
 package axirassa.services.pinger;
 
 import java.io.IOException;
 
 import org.hornetq.api.core.HornetQException;
 import org.hornetq.api.core.client.ClientConsumer;
 import org.hornetq.api.core.client.ClientMessage;
 import org.hornetq.api.core.client.ClientProducer;
 import org.hornetq.api.core.client.ClientSession;
 
 import axirassa.config.Messaging;
 import axirassa.model.HttpStatisticsEntity;
 import axirassa.model.PingerEntity;
 import axirassa.services.Service;
 import axirassa.services.exceptions.AxirassaServiceException;
 import axirassa.services.exceptions.InvalidMessageClassException;
 import axirassa.util.AutoSerializingObject;
 
 public class PingerService implements Service {
 
 	private final ClientSession messagingSession;
 	private final HTTPPinger pinger;
 
 
 	public PingerService(ClientSession messagingSession) {
 		this.messagingSession = messagingSession;
 
 		pinger = new HTTPPinger();
 	}
 
 
 	@Override
 	public void execute() throws HornetQException, IOException, ClassNotFoundException, InterruptedException,
 	        AxirassaServiceException {
 		// we have to start before reading messages
 		messagingSession.start();
 
		ClientProducer responseProducer = messagingSession.createProducer(Messaging.PINGER_RESPONSE_QUEUE);
		ClientConsumer requestConsumer = messagingSession.createConsumer(Messaging.PINGER_REQUEST_QUEUE);

		try {
 			while (true) {
				ClientMessage message = requestConsumer.receive();
				message.acknowledge();
				messagingSession.commit();
 
 				byte[] buffer = new byte[message.getBodyBuffer().readableBytes()];
 				message.getBodyBuffer().readBytes(buffer);
 
 				Object rawobject = AutoSerializingObject.fromBytes(buffer);
 				if (rawobject instanceof PingerEntity) {
 					PingerEntity request = (PingerEntity) rawobject;
 
 					System.out.println("Pinging: " + request.getUrl());
 					HttpStatisticsEntity statistic = pinger.ping(request);
 
 					sendResponseMessage(responseProducer, statistic);
 				} else
 					throw new InvalidMessageClassException(PingerEntity.class, rawobject);
 			}
		} finally {
 			responseProducer.close();
 			requestConsumer.close();
 			messagingSession.stop();
 		}
 	}
 
 
 	private void sendResponseMessage(ClientProducer responseProducer, HttpStatisticsEntity statistic)
 	        throws IOException, HornetQException {
 		ClientMessage message = messagingSession.createMessage(false);
 		message.getBodyBuffer().writeBytes(statistic.toBytes());
 		responseProducer.send(message);
 	}
 }
