 package demos;
 
 import vitro.model.*;
 import vitro.model.graph.*;
 import vitro.controller.*;
 import vitro.view.*;
 
 public class VacEye extends Host {
 	
 	public static void main(String[] args) {
 		new VacEye();
 	}
 
 	public VacEye() {
 		
 		VacWorld model                  = new VacWorld();
 		SequentialController controller = new SequentialController(model);
 		GraphView view                  = new GraphView(model, controller, 640, 480);
 
 		Node start = view.createNode(.5, .3, "start");
 		Node roomA = view.createNode(.3, .7, "room A");
 		Node roomB = view.createNode(.7, .7, "room B");
 
 		view.createEdge(start, roomA);
 		view.createEdge(roomA, roomB);
 
 		start.actors.add(model.createScrubby());
 		roomB.actors.add(model.createDirt());
 
 		show(view);
 	}
 }
