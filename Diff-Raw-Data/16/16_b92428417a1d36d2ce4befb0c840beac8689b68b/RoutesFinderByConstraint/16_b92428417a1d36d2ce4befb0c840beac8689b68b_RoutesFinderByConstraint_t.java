 package son.graph;
 
 import java.util.List;
 
 import son.graph.algorithms.RoutesFinder;
 import son.graph.algorithms.RoutesConstraint;
 import son.graph.models.interfaces.Node;
 
 /**
  * {@link RoutesFinderByConstraint} couple a {@link RoutesFinder} algorithm with
  * a {@link RoutesConstraint}. It allows use different graph algorithm with a
  * same constraint.
  * 
  * @author son
  * 
  */
 public abstract class RoutesFinderByConstraint extends RoutesFinder {
 	public RoutesFinderByConstraint(RoutesFinder routesFinder) {
 		super(routesFinder.getGraph());
 		this.routesFinder = routesFinder;
 	}
 
 	private final RoutesFinder routesFinder;
 
 	@Override
 	public List<List<Node>> findRoutes(String beginName, String endName,
			RoutesConstraint routeConstraint) {
		return routesFinder.findRoutes(beginName, endName, routeConstraint);
 	}
 }
