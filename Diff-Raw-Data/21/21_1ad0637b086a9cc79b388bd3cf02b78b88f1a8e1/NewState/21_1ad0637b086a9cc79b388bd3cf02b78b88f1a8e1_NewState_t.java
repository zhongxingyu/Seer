 package de.htwg.seapal.aview.tui.states.mark;
 
 import java.util.UUID;
 
 import de.htwg.seapal.aview.tui.StateContext;
 import de.htwg.seapal.aview.tui.TuiState;
 import de.htwg.seapal.aview.tui.activity.MarkActivity;
 import de.htwg.seapal.controller.IMarkController;
 
 public class NewState implements TuiState {
 
 	@Override
 	public String buildString(StateContext context) {
 		return "q - quit \n\n Enter MarkName";
 	}
 
 	@Override
 	public boolean process(StateContext context, String input) {
 		IMarkController controller = ((MarkActivity) context).getController();
 		if (input.equals("q")) {
 			context.setState(new StartState());
 			return false;
 		}
 
 		// test getRandomLocation
 		double minLat = -90.00;
 		double maxLat = 90.00;
 		double latitude = minLat
 				+ (double) (Math.random() * ((maxLat - minLat) + 1));
 		latitude = latitude * 10000;
 		latitude = Math.round(latitude);
 		latitude = latitude / 10000;
 
 		double minLon = 0.00;
 		double maxLon = 180.00;
 		double longitude = minLon
 				+ (double) (Math.random() * ((maxLon - minLon) + 1));
 		longitude = longitude * 10000;
 		longitude = Math.round(longitude);
 		longitude = longitude / 10000;
 
 
		UUID mark = controller.newMark(latitude, longitude);
 		controller.setName(mark, input);
 		context.setState(new ShowState(mark));
 		return true;
 	}
 
 }
