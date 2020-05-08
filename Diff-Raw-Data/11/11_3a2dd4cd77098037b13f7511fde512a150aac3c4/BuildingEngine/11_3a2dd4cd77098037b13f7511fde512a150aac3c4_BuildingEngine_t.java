 package edificios;
 
 import gps.GPSEngine;
 import gps.GPSNode;
 
 import java.util.LinkedList;
 
 public class BuildingEngine extends GPSEngine {
 
 	private int currentMaxDepth = 1;
 	
	boolean adding = false;
	
 	@Override
 	public void addNode(GPSNode node) {
 		switch(strategy){
 		case BFS:
 			open.add(node);
 			break;
 		case DFS:
 			((LinkedList<GPSNode>) open).addFirst(node);
 			break;
 		case DFSI:			
 			if (open.isEmpty()) {
 				open.add(new GPSNode(problem.getInitState(), 0));
 				currentMaxDepth++;
				adding = true;
 			}
 			if (node.getDepth() <= currentMaxDepth) {
				if (adding) {
					((LinkedList<GPSNode>) open).addFirst(node);
				}
			} else {
				adding = false;
 			}
 			break;
 		default:
 			throw new IllegalStateException();
 		}
 	}
 
 }
