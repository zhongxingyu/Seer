 package ch.cern.atlas.apvs.server;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferOutputStream;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.domain.APVSException;
 import ch.cern.atlas.apvs.domain.Error;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.Message;
 import ch.cern.atlas.apvs.domain.Order;
 import ch.cern.atlas.apvs.domain.Report;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 import ch.cern.atlas.apvs.ptu.server.PtuJsonReader;
 import ch.cern.atlas.apvs.ptu.server.PtuJsonWriter;
 import ch.cern.atlas.apvs.ptu.server.PtuReconnectHandler;
 import ch.cern.atlas.apvs.ptu.server.Scale;
 import ch.cern.atlas.apvs.ptu.shared.EventChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 public class PtuClientHandler extends PtuReconnectHandler {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private final RemoteEventBus eventBus;
 
 	private List<Measurement> measurementChanged = new ArrayList<Measurement>();
 
 	private boolean dosimeterOk;
 
 	public PtuClientHandler(ClientBootstrap bootstrap,
 			final RemoteEventBus eventBus) {
 		super(bootstrap);
 		this.eventBus = eventBus;
 
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				String type = event.getRequestedClassName();
 
 				if (type.equals(ConnectionStatusChangedRemoteEvent.class
 						.getName())) {
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.daq, isConnected());
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.dosimeter, isConnected()
 									&& dosimeterOk);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
 			throws Exception {
 		ConnectionStatusChangedRemoteEvent.fire(eventBus, ConnectionType.daq,
 				true);
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.dosimeter, dosimeterOk);
 		super.channelConnected(ctx, e);
 	}
 
 	@Override
 	public void channelDisconnected(ChannelHandlerContext ctx,
 			ChannelStateEvent e) throws Exception {
 		ConnectionStatusChangedRemoteEvent.fire(eventBus, ConnectionType.daq,
 				false);
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.dosimeter, dosimeterOk);
 		super.channelDisconnected(ctx, e);
 	}
 
 	public void sendOrder(Order order) {
 		try {
 			System.out.println(PtuJsonWriter.objectToJson(order));
 
 			ChannelBuffer buffer = ChannelBuffers.buffer(8192);
 			OutputStream os = new ChannelBufferOutputStream(buffer);
 			PtuJsonWriter writer = new PtuJsonWriter(os);
 			writer.write(0x10);
 			writer.write(order);
 			writer.write(0x13);
 			System.out.println("Sending...");
 
 			ChannelBufferOutputStream cos = (ChannelBufferOutputStream) os;
 			getChannel().write(cos.buffer()).awaitUninterruptibly();
 			System.out.println(PtuJsonWriter.objectToJson(order));
 			writer.close();
 			System.out.println("Done...");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private final static boolean DEBUG = false;
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
 		// Print out the line received from the server.
 		String line = (String) event.getMessage();
 
 		if (DEBUG) {
 			for (int i = 0; i < line.length(); i++) {
 				char c = line.charAt(i);
 				System.err.println(c + " " + Integer.toString(c));
 			}
 		}
 
 		line = line.replaceAll("\u0000", "");
 		line = line.replaceAll("\u0010", "");
 		line = line.replaceAll("\u0013", "");
 		if (DEBUG) {
 			log.info("'" + line + "'");
 			log.info("LineLength" + line.length());
 		}
 
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
		
	private final static long SECOND = 1000;
	private final static long MINUTE = 60 * SECOND;
 
 	private void handleMessage(Measurement message) throws APVSException {
 
		// Quick fix for #371
		Date now = new Date();
		if (message.getDate().getTime() < (now.getTime() - 5*MINUTE)) {
			log.warn("UPDATE IGNORED, too old "+message.getDate()+" "+now+" "+message);
			return;
		}
		
 		String unit = message.getUnit();
 		Number value = message.getValue();
 		Number low = message.getLowLimit();
 		Number high = message.getHighLimit();
 
 		// Scale down to microSievert
 		value = Scale.getValue(value, unit);
 		low = Scale.getLowLimit(low, unit);
 		high = Scale.getHighLimit(high, unit);
 		unit = Scale.getUnit(unit);
 
 		measurementChanged.add(new Measurement(message.getPtuId(), message
 				.getName(), value, low, high, unit, message.getSamplingRate(),
 				message.getDate()));
 
 		sendEvents();
 	}
 
 	private void handleMessage(Event message) {
 		String ptuId = message.getPtuId();
 		String sensor = message.getName();
 
 		log.info("EVENT " + message);
 
 		eventBus.fireEvent(new EventChangedEvent(new Event(ptuId, sensor,
 				message.getEventType(), message.getValue(), message
 						.getTheshold(), message.getUnit(), message.getDate())));
 
 		if (message.getEventType().equals("DosConnectionStatus_OFF")) {
 			dosimeterOk = false;
 			ConnectionStatusChangedRemoteEvent.fire(eventBus,
 					ConnectionType.dosimeter, dosimeterOk);
 		} else if (message.getEventType().equals("DosConnectionStatus_ON")) {
 			dosimeterOk = true;
 			ConnectionStatusChangedRemoteEvent.fire(eventBus,
 					ConnectionType.dosimeter, dosimeterOk);
 		}
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
