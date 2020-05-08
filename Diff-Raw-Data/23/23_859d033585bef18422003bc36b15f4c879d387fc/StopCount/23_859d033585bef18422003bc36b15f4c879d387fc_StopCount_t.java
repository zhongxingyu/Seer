 package infoprivacy.simulator.processors;
 
 import infoprivacy.simulator.Processor;
 import infoprivacy.simulator.Reporter;
 
 import java.util.Date;
 
 public class StopCount implements Processor
 {
 	boolean lastWasStop = false;
 	int stopCount = 0;
	double lastSpeed = 10;
 	
 	@Override
 	public void process(Date time, double speedMPH) {
 		
		if(speedMPH < 0)
 		{
			Reporter.getInstance().logValue("Number of Stops", stopCount);
 		}
 		
		if(lastSpeed != 0 && speedMPH == 0)
 		{
 			stopCount++;
 		}
 		
		lastSpeed = speedMPH;		
 	}
 
 	@Override
 	public String getName() {
 		return "Stop Count";
 	}
 
 }
