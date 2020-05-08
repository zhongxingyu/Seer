 package dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor.components;
 
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.transceiver.AbstractTransceiver;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.logging.LoggingUtility;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.MessageType;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.Packet;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.PacketType;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor.SensorUtility;
 
 public class SensorComponent extends Thread {
 
 	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	private int period;
 	
 	private int threshold;
 	
 	private String nodeId;
 
 	private AbstractTransceiver transceiver;
 
 	public SensorComponent(String nodeId, AbstractTransceiver relatedTransceiver, int period,
 			int threshold) {
 		this.nodeId = nodeId;
 		this.transceiver = relatedTransceiver;
 
 		this.period = period;
 		this.threshold = threshold;
 		
 		logger.debug("Sensor created");
 
 		this.start();
 	}
 
 	public synchronized void setPeriod(int p) {
 		this.period = p;
 	}
 
 	public synchronized void setThreshold(int t) {
 		this.threshold = t;
 	}
 	
 	public int getPeriod() {
 		return period;
 	}
 
 	public int getThreshold() {
 		return threshold;
 	}
 
 	public AbstractTransceiver getTransceiver() {
 		return transceiver;
 	}
 	
 	private int getTemperature() {
 		Random rng = new Random();
 
 		int temperature = (int) (SensorUtility.MEASUREMENT_MEAN + SensorUtility.MEASUREMENT_STD
 				* (2 * rng.nextDouble() - 1));
 
 		return temperature;
 	}
 
 	public void run() {
 
 		int measurement;
 		Packet outPacket;
 
 		while (true) {
 
 			synchronized (this) {
 				try {
 					this.wait(period);
 				} catch (InterruptedException e) {
 					return;
 				}
 			}
 
 			measurement = getTemperature();
 
 			synchronized (this) {
 				if (measurement > threshold) {
 					outPacket = new Packet(nodeId, PacketType.ALM,
 							Integer.toString(measurement));
 				} else {
 					outPacket = new Packet(nodeId, PacketType.DAT,
 							Integer.toString(measurement));
 				}
 			}			
 			
 			logger.debug(outPacket + " created");
 			
 			LoggingUtility.logMessage(transceiver.getId(), transceiver.getId(), MessageType.GEN, outPacket.getType(), outPacket.getValue());
 			
 			transceiver.handlePacket(outPacket);
 		}
 
 	}
 }
