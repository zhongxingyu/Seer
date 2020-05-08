 package infoprivacy.simulator.processors;
 
 import infoprivacy.simulator.Processor;
 import infoprivacy.simulator.Reporter;
 
 import java.util.Date;
 
 public class StopCount implements Processor
 {
 	boolean lastWasStop = false;
 	int stopCount = 0;
 	
 	@Override
 	public void process(Date time, double speedMPH) {
		if(lastWasStop && speedMPH < .3 && speedMPH >= 0)
		{
			return;
		}
 		
		if(lastWasStop && speedMPH >= .3)
 		{
			lastWasStop = false;
 		}
 		
		if(speedMPH < .3 && speedMPH >= 0)
 		{
			lastWasStop = true;
 			stopCount++;
 		}
 		
		if(speedMPH < 0)
		{
			Reporter.getInstance().logValue("Number of Stops", stopCount);
		}
 	}
 
 	@Override
 	public String getName() {
 		return "Stop Count";
 	}
 
 }
