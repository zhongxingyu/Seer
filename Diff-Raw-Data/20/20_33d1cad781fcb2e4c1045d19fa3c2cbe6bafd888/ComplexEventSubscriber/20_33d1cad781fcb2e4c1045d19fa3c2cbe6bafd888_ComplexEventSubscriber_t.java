 package eu.play_project.dcep.distribution.tests.srbench.performance;
 
 import java.io.Serializable;
 
 import eu.play_project.dcep.distributedetalis.api.SimplePublishApi;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.Quadruple;
 
 public class ComplexEventSubscriber implements SimplePublishApi, Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 100L;
 	MeasurementUnit mu; 
 	
 	public ComplexEventSubscriber(){}
 	
 	@Override
 	public void publish(CompoundEvent event) {
 		if(mu==null){
 			mu = new MeasurementUnit();
 			mu.calcRateForNEvents(300);
 		}
 		//mu.nexEvent();
 		printTimeSpentInSystem(event);
 	}
 	int eventCounter = 0;
 	long eventTime;
 	public void printTimeSpentInSystem(CompoundEvent event) {
 		if ((eventCounter++ % 500) == 0) {
 			for (Quadruple quadruple : event) {
 				// Use endTime
 				if (quadruple.getPredicate().toString().equals("http://events.event-processing.org/types/sedTime")) {
					eventTime = Long.parseLong(quadruple.getObject().toString().substring(1, quadruple.getObject().toString().length()-1));
 				}
 
 				long time = System.currentTimeMillis();
 				// Print time spend in system.
 				System.out.println(time + "\t"
 						+ (System.currentTimeMillis() - eventTime));
 			}
 		}
 	}
 }
 
