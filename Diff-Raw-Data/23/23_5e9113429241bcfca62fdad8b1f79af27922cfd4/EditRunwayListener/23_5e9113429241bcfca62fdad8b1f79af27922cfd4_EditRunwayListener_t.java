 package Controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import javax.swing.JList;
 
 import Model.Airport;
 import Model.AirportObserver;
 import View.EditRunwayDialog;
 
 public class EditRunwayListener implements ActionListener, AirportObserver{
 
 	Airport airport;
 	List<AirportObserver> airportObservers;
 
 	public EditRunwayListener(Airport airport, List<AirportObserver> airportObservers ){
 		this.airport = airport;
 		this.airportObservers = airportObservers;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
		
		System.out.println(airport.getPhysicalRunways().size());
		
 		if(airport.getPhysicalRunways().size() == 0){
 			@SuppressWarnings("unused")
 			EditRunwayDialog erd = new EditRunwayDialog(airport, new JList(), true, airportObservers);
 			notifyAirportObservers();
 		}else{
 			@SuppressWarnings("unused")
 			EditRunwayDialog erd = new EditRunwayDialog(airport, new JList(), false, airportObservers);
 			notifyAirportObservers();
 		}
 	}
 
 	@Override
 	public void updateAirport(Airport airport) {
 		this.airport = airport;
 	}
 
 	void notifyAirportObservers(){
 		for(AirportObserver ao: airportObservers){
 			ao.updateAirport(airport);
 		}
 	}
 }
