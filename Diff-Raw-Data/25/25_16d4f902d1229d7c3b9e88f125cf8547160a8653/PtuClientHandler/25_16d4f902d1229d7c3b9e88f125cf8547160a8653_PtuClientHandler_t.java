 package ch.cern.atlas.apvs.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.domain.APVSException;
 import ch.cern.atlas.apvs.domain.Error;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.Message;
 import ch.cern.atlas.apvs.domain.Report;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.ptu.server.PtuJsonReader;
 import ch.cern.atlas.apvs.ptu.server.PtuReconnectHandler;
 import ch.cern.atlas.apvs.ptu.shared.EventChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 public class PtuClientHandler extends PtuReconnectHandler {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private final RemoteEventBus eventBus;
 
 	private List<Measurement> measurementChanged = new ArrayList<Measurement>();
 
 	public PtuClientHandler(ClientBootstrap bootstrap,
 			final RemoteEventBus eventBus) {
 		super(bootstrap);
 		this.eventBus = eventBus;
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
 		// Print out the line received from the server.
 		String line = (String) event.getMessage();
 		line = line.replaceAll("\u0000", "");
 		line = line.replaceAll("\u0010", "");
 		line = line.replaceAll("\u0013", "");
 		// log.info("'"+line+"'");
 		// log.info(len+" "+line.length());
 
 		List<Message> list;
 		try {
 			list = PtuJsonReader.jsonToJava(line);
 			for (Iterator<Message> i = list.iterator(); i.hasNext();) {
 				Message message = i.next();
 				try {
 					if (message instanceof Measurement) {
 						handleMessage((Measurement) message);
 					} else if (message instanceof Report) {
 						handleMessage((Report) message);
 					} else if (message instanceof Event) {
 						handleMessage((Event) message);
 					} else if (message instanceof Error) {
 						handleMessage((Error) message);
 					} else {
 						log.warn("Error: unknown Message Type: "
 								+ message.getType());
 					}
 				} catch (APVSException e) {
 					log.warn("Could not add measurement", e);
 				}
 			}
 		} catch (IOException ioe) {
 			// TODO Auto-generated catch block
 			ioe.printStackTrace();
 		}
 
 	}
 
 	private void handleMessage(Measurement message) throws APVSException {
 
 		// Scale down to microSievert
 		if (message.getUnit().equals("mSv")) {
 			message = new Measurement(message.getPtuId(), message.getName(), message.getValue().doubleValue()*1000, message.getSamplingRate(), "&micro;Sv", message.getDate());
 		}
		if (message.getUnit().equals("mSv/h")) {
			message = new Measurement(message.getPtuId(), message.getName(), message.getValue().doubleValue()*1000, message.getSamplingRate(), "&micro;Sv/h", message.getDate());
		}
 		
 		measurementChanged.add(message);
 
 		sendEvents();
 	}
 
 	private void handleMessage(Event message) {
 		String ptuId = message.getPtuId();
 		String sensor = message.getName();
 
 		log.info("EVENT " + message);
 
 		eventBus.fireEvent(new EventChangedEvent(new Event(ptuId, sensor,
 				message.getEventType(), message.getValue(), message
 						.getTheshold(), message.getDate())));
 	}
 
 	private void handleMessage(Report report) {
 		log.warn(report.getType() + " NOT YET IMPLEMENTED, see #23 and #112");
 	}
 
 	private void handleMessage(Error error) {
 		log.warn(error.getType() + " NOT YET IMPLEMENTED, see #114");
 	}
 
 	private synchronized void sendEvents() {
 
 		for (Iterator<Measurement> i = measurementChanged.iterator(); i
 				.hasNext();) {
 			Measurement m = i.next();
 			eventBus.fireEvent(new MeasurementChangedEvent(m));
 		}
 
 		measurementChanged.clear();
 	}
 }
