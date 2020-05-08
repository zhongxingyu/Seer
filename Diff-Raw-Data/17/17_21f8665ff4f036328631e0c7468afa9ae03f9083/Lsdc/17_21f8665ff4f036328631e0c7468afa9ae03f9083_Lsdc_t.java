 package at.ac.tuwien.lsdc;
 
import java.io.IOException;
 import java.util.List;
 
 import at.ac.tuwien.lsdc.generator.Request;
 import at.ac.tuwien.lsdc.generator.RequestGenerator;
 import at.ac.tuwien.lsdc.mape.Monitor;
 import at.ac.tuwien.lsdc.mape.SchedulingAgent;
 import at.ac.tuwien.lsdc.resources.PhysicalMachine;
 
 public class Lsdc {
	public static void main(String[] args) throws NumberFormatException, IOException {
 		// add a request to request queue
 		List<Request> requests = RequestGenerator.getInstance().generateRequests();
 		RequestGenerator.getInstance().setRequests(requests);
 		
 		// add new physical machines to our simulation
 		Integer maxPms = Configuration.getInstance().getMaxPms();
 		for (int i = 0; i < maxPms; i++) {
 			Monitor.getInstance().addPm(new PhysicalMachine());
 		}
 		
 		// start mape-loop and simulation
 		SchedulingAgent.getInstance().start();
 	}
 }
