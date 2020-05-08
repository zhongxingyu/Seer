 package dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor;
 
 import java.util.Scanner;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.channels.Channel;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.exceptions.NodeInitializationException;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.nodes.AbstractNode;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor.components.SensorComponent;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor.components.TransceiverComponent;
 
 /**
  * Sensor Node for Sensor Network
  * 
  */
 public class Sensor extends AbstractNode {
 	
 	private TransceiverComponent transceiverComponent;
 	
 	private SensorComponent sensorComponent;
 
 	public Sensor(String id, int period, int threshold, int leftPortNumber,
 			int rightPortNumber, Channel[] leftChannels,
 			Channel[] rightChannels, int ackTimeout) {
 		
 		super(id);
 
 		this.transceiverComponent = new TransceiverComponent(id, leftPortNumber,
 				rightPortNumber, leftChannels, rightChannels,
 				ackTimeout);
 
 		this.sensorComponent = new SensorComponent(id, this.transceiverComponent,
 				period, threshold);
 	}
 
 	public TransceiverComponent getTransceiverComponent() {
 		return transceiverComponent;
 	}
 
 	public SensorComponent getSensorComponent() {
 		return sensorComponent;
 	}
 
 	public static void main(String[] args) {
 
 		if (args.length != 1) {
 			System.out
 					.println("Please provide only one parameter - a suitable property file");
 			return;
 		}
 
 		Sensor sensor = null;
 
 		try {
 			sensor = SensorUtility.getSensorInstance(args[0]);
 		} catch (NodeInitializationException e) {
 			System.out.println(e.getMessage());
 			return;
 		}
 		
 	    Scanner in = new Scanner(System.in);
 
 		in.next();
 		in.close();
 
 		System.out.println("Done");
 
		sensor.transceiverComponent.close();
		sensor.sensorComponent.interrupt();
 	}
 
 }
