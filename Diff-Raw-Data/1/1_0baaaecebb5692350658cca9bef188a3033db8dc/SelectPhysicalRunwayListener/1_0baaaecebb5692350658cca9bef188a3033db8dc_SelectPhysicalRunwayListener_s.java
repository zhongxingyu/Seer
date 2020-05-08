 package Controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import Model.Airport;
 import Model.AirportObserver;
 import Model.PhysicalRunway;
 
 public class SelectPhysicalRunwayListener implements ActionListener{
 	//I am not sure how to select a runway. 
 	//Right now I have put to do it using a runway object, 
 	//but it could be done in a different way if that is more convenient
 	
 	Airport airport;
 	PhysicalRunway physicalRunway;
 	List<AirportObserver> airportObservers;
 	
 	public SelectPhysicalRunwayListener(Airport airport, PhysicalRunway physicalRunway, List<AirportObserver> airportObservers){
 		this.airport = airport;
 		this.physicalRunway  = physicalRunway;
 		this.airportObservers = airportObservers;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		airport.setCurrentPhysicalRunway(physicalRunway);
 		notifyAirportObservers();
 	}
 
 
 	void notifyAirportObservers() {
 		for(AirportObserver ao: airportObservers){
 			ao.updateAirport(airport);
 		}
 	}
 	
 }
