 package bzb.gwt.planner.client.panels;
 
 import bzb.gwt.planner.client.Planner;
 import bzb.gwt.planner.client.data.CTrip;
 
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 
 public class TripsPanel extends FlowPanel {
 
 	public TripsPanel () {
		if (Planner.getUser().getTrips() != null) {
			for (CTrip trip : Planner.getUser().getTrips()) {
				add(new HTML(trip.getName()));
			}
		} else {
			add (new HTML("No trips"));
 		}
 	}
 	
 }
