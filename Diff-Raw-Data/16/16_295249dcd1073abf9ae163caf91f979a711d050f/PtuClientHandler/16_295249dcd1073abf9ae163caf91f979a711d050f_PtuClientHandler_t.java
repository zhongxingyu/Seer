 package ch.cern.atlas.apvs.server;
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.buffer.ByteBuf;
 import io.netty.buffer.ByteBufOutputStream;
 import io.netty.buffer.Unpooled;
 import io.netty.channel.ChannelHandler.Sharable;
 import io.netty.channel.ChannelHandlerContext;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 import ch.cern.atlas.apvs.db.Database;
 import ch.cern.atlas.apvs.db.Scale;
 import ch.cern.atlas.apvs.db.SensorMap;
 import ch.cern.atlas.apvs.domain.APVSException;
 import ch.cern.atlas.apvs.domain.Device;
 import ch.cern.atlas.apvs.domain.Error;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.GeneralConfiguration;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.Message;
 import ch.cern.atlas.apvs.domain.Order;
 import ch.cern.atlas.apvs.domain.Packet;
 import ch.cern.atlas.apvs.domain.Report;
 import ch.cern.atlas.apvs.domain.Ternary;
 import ch.cern.atlas.apvs.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.event.ConnectionStatusChangedRemoteEvent.ConnectionType;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 import ch.cern.atlas.apvs.ptu.server.PtuJsonWriter;
 import ch.cern.atlas.apvs.ptu.server.PtuReconnectHandler;
 import ch.cern.atlas.apvs.ptu.shared.EventChangedEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 
 // FIXME not really sure...but was working in netty 3.5 without this... so was shared...
 @Sharable
 public class PtuClientHandler extends PtuReconnectHandler {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	private final RemoteEventBus eventBus;
 
 	private List<Measurement> measurementChanged = new ArrayList<Measurement>();
 
 	private Ternary dosimeterOk = Ternary.Unknown;
 	private String dosimeterCause = "Dosimeter not yet read by PTU";
 
 	private PtuSettings settings;
 
 	private Database database;
 	private SensorMap sensorMap;
 	private Map<String, Device> deviceMap;
 
 	public PtuClientHandler(Bootstrap bootstrap, final RemoteEventBus eventBus)
 			throws SerializationException {
 		super(bootstrap);
 		this.eventBus = eventBus;
 
 		database = Database.getInstance(eventBus, true);
 
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				String type = event.getRequestedClassName();
 
 				if (type.equals(ConnectionStatusChangedRemoteEvent.class
 						.getName())) {
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.daq, isConnected(), getCause());
 					ConnectionStatusChangedRemoteEvent.fire(eventBus,
 							ConnectionType.dosimeter,
 							isConnected() ? dosimeterOk : Ternary.False,
 							dosimeterCause);
 				}
 			}
 		});
 
 		PtuSettingsChangedRemoteEvent.subscribe(eventBus,
 				new PtuSettingsChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedRemoteEvent event) {
 						settings = event.getPtuSettings();
 					}
 				});
 	}
 
 	@Override
 	public void channelActive(ChannelHandlerContext ctx) throws Exception {
 		ConnectionStatusChangedRemoteEvent.fire(eventBus, ConnectionType.daq,
 				true, "");
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.dosimeter, dosimeterOk, dosimeterCause);
 		super.channelActive(ctx);
 
 		sensorMap = database.getSensorMap();
 
 		deviceMap = database.getDeviceMap();
 	}
 
 	@Override
 	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
 		ConnectionStatusChangedRemoteEvent.fire(eventBus, ConnectionType.daq,
 				false, getCause());
 		ConnectionStatusChangedRemoteEvent.fire(eventBus,
 				ConnectionType.dosimeter, dosimeterOk, dosimeterCause);
 		super.channelInactive(ctx);
 	}
 
 	public void sendOrder(Order order) {
 		try {
 			System.out.println(PtuJsonWriter.objectToJson(order));
 
 			ByteBuf buffer = Unpooled.buffer(8192);
 			OutputStream os = new ByteBufOutputStream(buffer);
 			PtuJsonWriter writer = new PtuJsonWriter(os);
 			writer.write(0x10);
 			writer.write(order);
 			writer.write(0x13);
 			System.out.println("Sending...");
 
 			ByteBufOutputStream cos = (ByteBufOutputStream) os;
 			getChannel().write(cos.buffer()).awaitUninterruptibly();
 			System.out.println(PtuJsonWriter.objectToJson(order));
 			writer.close();
 			System.out.println("Done...");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private final static boolean DEBUG = true;
 
 	@Override
 	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
 		// TODO Auto-generated method stub
 		super.channelReadComplete(ctx);
 		System.err.println("COMPLETE");
 	}
 
 	@Override
 	protected void channelRead0(ChannelHandlerContext ctx, Packet packet)
 			throws Exception {
 		System.err.println("READ " + packet);
 
 		Device device = deviceMap.get(packet.getSender());
 		List<Message> list = packet.getMessages();
 		if (device == null) {
 			log.warn("Messages (" + list.size() + ") from unknown device: "
 					+ packet.getSender());
 			return;
 		}
 
 		if (DEBUG) {
 			log.info("# of mesg: " + list.size());
 		}
 		for (Iterator<Message> i = list.iterator(); i.hasNext();) {
 			Message message = i.next();
 			if (DEBUG) {
 				log.info(message.toString());
 			}
 
 			try {
 				if (message instanceof Measurement) {
 					handleMessage((Measurement) message);
 				} else if (message instanceof Report) {
 					handleMessage((Report) message);
 				} else if (message instanceof Event) {
 					handleMessage((Event) message);
 				} else if (message instanceof Error) {
 					handleMessage((Error) message);
 				} else if (message instanceof GeneralConfiguration) {
 					handleMessage((GeneralConfiguration) message);
 				} else {
 					log.warn("Error: unknown Message Type: "
 							+ message.getType());
 				}
 			} catch (APVSException e) {
 				log.warn("Could not add measurement", e);
 			} catch (SerializationException e) {
 				log.error("Could not serialize event", e);
 			}
 		}
 	}
 
 	private final static long SECOND = 1000;
 	private final static long MINUTE = 60 * SECOND;
 
 	private void handleMessage(Measurement message) throws APVSException,
 			SerializationException {
 		// Quick fix for #371
 		Date now = new Date();
 		if (message.getDate().getTime() < (now.getTime() - 5 * MINUTE)) {
 			log.warn("UPDATE IGNORED, too old " + message.getDate() + " " + now
 					+ " " + message);
 			return;
 		}
 
 		Device ptu = message.getDevice();
 		System.err.println(ptu);
 		String sensor = message.getSensor();
 		System.err.println(sensor);
 
 		if (!sensorMap.isEnabled(ptu, sensor)) {
 			// log.warn("UPDATE IGNORED, disabled measurement " + ptuId + " " +
 			// sensor);
 			return;
 		}
 
 		String unit = message.getUnit();
 		Double value = message.getValue();
 		Double low = message.getLowLimit();
 		Double high = message.getHighLimit();
 
 		// Scale down to microSievert
 		value = Scale.getValue(value, unit);
 		low = Scale.getLowLimit(low, unit);
 		high = Scale.getHighLimit(high, unit);
 		unit = Scale.getUnit(sensor, unit);
 
 		message = new Measurement(message.getDevice(), sensor, value, low,
				high, unit, message.getSamplingRate(), "OneShoot", message.getDate());
 
 		System.err.println("Modified message: " + message);
 
 		measurementChanged.add(message);
 
 		sendEvents();
 	}
 
 	private void handleMessage(Event message) throws SerializationException {
 		Device device = message.getDevice();
 		String sensor = message.getSensor();
 
 		// log.info("EVENT " + message);
 
 		eventBus.fireEvent(new EventChangedEvent(new Event(device, sensor,
 				message.getEventType(), message.getValue(), message
 						.getThreshold(), message.getUnit(), message.getDate())));
 
 		if (message.getEventType().equals("DosConnectionStatus_OFF")) {
 			dosimeterOk = Ternary.False;
 			dosimeterCause = "Dosimeter not connected to PTU";
 			ConnectionStatusChangedRemoteEvent.fire(eventBus,
 					ConnectionType.dosimeter, dosimeterOk, dosimeterCause);
 		} else if (message.getEventType().equals("DosConnectionStatus_ON")) {
 			dosimeterOk = Ternary.True;
 			dosimeterCause = "";
 			ConnectionStatusChangedRemoteEvent.fire(eventBus,
 					ConnectionType.dosimeter, dosimeterOk, dosimeterCause);
 		}
 	}
 
 	private void handleMessage(GeneralConfiguration message)
 			throws SerializationException {
 		String ptuId = message.getDevice().getName();
 		String dosimeterId = message.getDosimeterId();
 
 		if (settings != null) {
 			settings.setDosimeterSerialNumber(ptuId, dosimeterId);
 
 			eventBus.fireEvent(new PtuSettingsChangedRemoteEvent(settings));
 		}
 	}
 
 	private void handleMessage(Report report) {
 		log.warn(report.getType() + " NOT YET IMPLEMENTED, see #23 and #112");
 	}
 
 	private void handleMessage(Error error) {
 		log.warn(error.getType() + " NOT YET IMPLEMENTED, see #114");
 	}
 
 	private synchronized void sendEvents() throws SerializationException {
 
 		for (Iterator<Measurement> i = measurementChanged.iterator(); i
 				.hasNext();) {
 			Measurement m = i.next();
 			eventBus.fireEvent(new MeasurementChangedEvent(m));
 		}
 
 		measurementChanged.clear();
 	}
 }
