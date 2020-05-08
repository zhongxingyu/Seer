 package eu.play_project.dcep.distribution.tests.srbench.performance;
 
 import java.util.LinkedList;
 
 import com.hp.hpl.jena.graph.Node;
 
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.Quadruple;
 
 /**
  * Generate events and push them to the DistributedEtalisTestApi. The event rate
  * can be adapted. An instance of this class runs in his own thread. For this
  * reason the possible delay between events do not stops other threads.
  * 
  * @author sobermeier
  * 
  */
 public class EventProducerThread implements Runnable {
 	private Thread thisThread;
 	private DistributedEtalisTestApi[] testApi;
 	private MeasurementUnit meausrementUnit;
 	private int numberOfEvents;
 	private int delay;
 
 	/**
 	 * Generate an instance in his own thread and publish events to destinations.
 	 * 
 	 * @param testApi
 	 *            Destination for the events.
 	 * @param delay
 	 *            Delay between two events. Given in ms.
 	 */
 	public EventProducerThread(int numberOfEvents, int delay, DistributedEtalisTestApi... testApi) {
 		this.testApi = testApi;
 		this.numberOfEvents = numberOfEvents;
 		this.delay =  delay;
 		
 		meausrementUnit = new MeasurementUnit();
 		meausrementUnit.calcRateForNEvents(500);
 		
 		thisThread = new Thread(this);
 		thisThread.start();
 	}
 
 	@Override
 	public void run() {
 		int destination = 0; //Destinations are stored in testApi.
 		for (int i = 0; i < numberOfEvents; i++) {
 			
 			//Publis event
 			destination++;
 			testApi[destination%testApi.length].publish(createEvent("EventId" + Math.random()));
 			
 			//Some statistics
 			meausrementUnit.nexEvent();
 			
 			//Wait
 			delay();
 		}		
 	}
 
 	private void delay() {
 		try {
 			Thread.sleep(delay);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	public static CompoundEvent createEvent(String eventId) {
 
 		LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 
 		Quadruple q1 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://events.event-processing.org/types/stream"),
 				Node.createURI("http://streams.event-processing.org/ids/Srbench#stream"));
 
 		Quadruple q3 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e2"),
 				Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#WindSpeedObservation"));
 
 		Quadruple q4 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e2"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#observedProperty"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#_WindSpeed"));
 
 		Quadruple q5 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e2"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"));
 
 		Quadruple q6 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
				Node.createURI("6.0"));
 
 //		Quadruple q7 = new Quadruple(
 //				Node.createURI("http://events.event-processing.org/eventId/"
 //						+ eventId),
 //				Node.createURI("http://prefix.example.com/e1"),
 //				Node.createURI("http://events.event-processing.org/types/endTime"),
 //				Node.createURI(new SimpleDateFormat(
 //						eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601)
 //						.format(new Date())));
 		Quadruple q7 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://events.event-processing.org/types/endTime"),
 				Node.createURI(System.currentTimeMillis() + ""));
 
 		Quadruple q8 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e2"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#procedure"),
 				Node.createURI("http://sensor.example.com/S1"));
 
 		quads.add(q1);
 		// quads.add(q2);
 		quads.add(q3);
 		quads.add(q4);
 		quads.add(q5);
 		quads.add(q6);
 		quads.add(q7);
 		quads.add(q8);
 
 		return new CompoundEvent(quads);
 	}
 }
