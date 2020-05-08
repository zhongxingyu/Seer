 package csci1902.lab4;
 
 public class BusStopEvent implements Event {
 	Bus bus;
 	public BusStopEvent(Bus b){
 		bus = b;
 	}
 	
 	@Override
 	public void run() {
 		
 		bus.current = bus.getNextStop();
 		bus.incNextStop(); //Plan next stop
 		
 		int stopTime = 0; //Keeps track of the time spent loading and unloading
 		
 		//Unload current passengers set for this stop
 		Q1 tempQ = new Q1();
 		while(bus.riders.length() > 0)
 		{
 			Rider currentRider = (Rider)bus.riders.remove();
 			if(currentRider.exitStop == bus.current.getStopNumber())
 			{
 				stopTime += Settings.DEBOARDTIME;
 				Stats.totalRideTime += Simulator.agenda.getCurrentTime() - currentRider.boardTime; 
 			}	
 			else
 				tempQ.add(currentRider); //Add the rider back into the new bus queue
 		}
 		
 		bus.riders = tempQ; // replace the bus queue
 		
 		//Load the waiting riders from the stop
 		while(bus.riders.length() < 60 && bus.current.riders.length() > 0)
 		{
 			Rider r = (Rider)bus.current.riders.remove();
			Stats.totalWaitTime += Simulator.agenda.getCurrentTime() - r.boardTime;
 			bus.riders.add(r);
 			stopTime += Settings.BOARDTIME;
 		}
 		
 		Simulator.agenda.add(new BusStopEvent(bus), Settings.TRAVELTIME); 
 		
 		//There is no need to implement the ability to skip a stop. System naturally handles it
 	}
 
 }
