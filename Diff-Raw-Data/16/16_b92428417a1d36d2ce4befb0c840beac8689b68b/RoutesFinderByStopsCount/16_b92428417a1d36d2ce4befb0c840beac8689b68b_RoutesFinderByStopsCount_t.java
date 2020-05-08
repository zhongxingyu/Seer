 package son.graph;
 
 import java.util.List;
 
 import son.graph.algorithms.RoutesConstraint;
 import son.graph.algorithms.RoutesFinder;
 import son.graph.models.interfaces.Node;
 
 /**
  * {@link RoutesFinder} with the number of stops between begin node and end node
  * is exactly the same with stops count.
  * 
  * @author son
  */
 public class RoutesFinderByStopsCount extends RoutesFinderByConstraint {
 
 	public RoutesFinderByStopsCount(RoutesFinder routesFinder) {
 		super(routesFinder);
 	}
 
 	public List<List<Node>> findRoutes(String beginName, String endName, int stopsCount) {
		return findRoutes(beginName, endName, new Constraint(stopsCount));
 	}
 
	public static class Constraint implements RoutesConstraint {
		public Constraint(int stopsCount) {
 			this.stopsCount = stopsCount;
 		}
 
 		private final int stopsCount;
 
 		@Override
 		public boolean canStop(List<Node> currentRoute) {
 			return currentRoute.size() == stopsCount + 1;
 		}
 
 		@Override
 		public boolean canMoveNext(List<Node> currentRoute) {
 			return currentRoute.size() < stopsCount + 1;
 		}
 	}
 }
