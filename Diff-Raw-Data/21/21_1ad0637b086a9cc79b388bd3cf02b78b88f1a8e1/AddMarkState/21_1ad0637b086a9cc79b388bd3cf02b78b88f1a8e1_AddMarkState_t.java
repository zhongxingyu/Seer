 package de.htwg.seapal.aview.tui.states.route;
 
 import java.util.List;
 import java.util.UUID;
 
 import android.widget.Toast;
 import de.htwg.seapal.aview.tui.StateContext;
 import de.htwg.seapal.aview.tui.TuiState;
 import de.htwg.seapal.aview.tui.activity.RouteActivity;
 import de.htwg.seapal.controller.IMarkController;
 import de.htwg.seapal.controller.IRouteController;
 
 public class AddMarkState implements TuiState {
 
 	private UUID route;
 	private IMarkController markController;
 	private List<UUID> marks;
 
 	public AddMarkState(UUID route, IMarkController markController) {
 		this.route = route;
 		this.markController = markController;
 	}
 
 	@Override
 	public String buildString(StateContext context) {
 		marks = markController.getMarks();
 		StringBuilder sb = new StringBuilder();
 		sb.append("q - quit\n");
 		sb.append("c - create a Routepoint\n");
 		sb.append("<x> - add Mark to Route\n");
 		sb.append("----------------------------\n");
 		int i = 1;
 		for (UUID uuid : marks) {
 			sb.append(i++).append(")\t").append(markController.getName(uuid))
 					.append("\n");
 		}
 
 		return sb.toString();
 	}
 
 	@Override
 	public boolean process(StateContext context, String input) {
 		RouteActivity activity = (RouteActivity) context;
 		IRouteController controller = activity.getController();
 		switch (input.charAt(0)) {
 		case 'q':
 			context.setState(new ShowState(route));
 			break;
 		case 'c':
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
 
 
			UUID newRouteMark = markController.newRouteMark(latitude, longitude);
 			markController.setName(newRouteMark, "Point");
 			controller.addMark(route, newRouteMark);
 			context.setState(new ShowMarksState(route));
 			break;
 		default:
 			Integer number;
 			try {
 				number = Integer.valueOf(input) - 1;
 			} catch (NumberFormatException e) {
 				Toast.makeText(activity, "Unkown Option", Toast.LENGTH_SHORT)
 						.show();
 				return false;
 			}
 
 			if (number < marks.size() && number >= 0)
 				controller.addMark(route, marks.get(number));
 			else
 				Toast.makeText(activity, "Unkown Option", Toast.LENGTH_SHORT)
 						.show();
 			context.setState(new ShowMarksState(route));
 		}
 		return true;
 	}
 
 }
