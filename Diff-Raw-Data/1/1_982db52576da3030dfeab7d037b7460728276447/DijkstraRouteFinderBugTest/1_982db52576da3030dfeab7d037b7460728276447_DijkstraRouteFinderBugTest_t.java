 package nz.ac.victoria.ecs.kpsmart.routefinder;
 
 import static org.junit.Assert.assertNotNull;
 import nz.ac.victoria.ecs.kpsmart.Data;
 import nz.ac.victoria.ecs.kpsmart.GuiceServletContextListner;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.RouteUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Priority;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Route;
 import nz.ac.victoria.ecs.kpsmart.entities.state.TransportMeans;
 import nz.ac.victoria.ecs.kpsmart.integration.EntityManager;
 import nz.ac.victoria.ecs.kpsmart.integration.HibernateModule;
 import nz.ac.victoria.ecs.kpsmart.logging.Log;
 import nz.ac.victoria.ecs.kpsmart.logging.impl.HibernateLogger;
 import nz.ac.victoria.ecs.kpsmart.reporting.impl.DefaultReport;
 import nz.ac.victoria.ecs.kpsmart.state.State;
 import nz.ac.victoria.ecs.kpsmart.state.impl.HibernateState;
 import nz.ac.victoria.ecs.kpsmart.util.HibernateDataTest;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class DijkstraRouteFinderBugTest extends HibernateDataTest {
 	private DijkstraRouteFinder route;
 	
 	@Before
 	@Override
 	public void setUp() throws Exception {
 		GuiceServletContextListner.createNewInjector(
 				new HibernateState.Module(), 
 				new HibernateLogger.Module(),
 				new DefaultReport.Module(),
 				new EntityManager.Module(),
				new DijkstraRouteFinder.Module(),
 				new HibernateModule("hibernate.memory.properties"));
 		
 		this.state = (HibernateState) GuiceServletContextListner.getInjector().getInstance(State.class);
 		this.logger = (HibernateLogger) GuiceServletContextListner.getInjector().getInstance(Log.class);
 		this.entity = GuiceServletContextListner.getInjector().getInstance(EntityManager.class);
 		this.route = new DijkstraRouteFinder();
 	}
 	
 	@Test
 	public void testLeavingNewZealandBySeaAndThenByAirFindsCorectRoute() {
 		new Data().createData();
 		this.entity.performEvent(new RouteUpdateEvent(new Route(
 				TransportMeans.Sea, 
 				this.state.getLocationForName("Wellington"), 
 				this.state.getLocationForName("Auckland"), 
 				this.state.getAllCarriers().iterator().next())));
 		this.entity.performEvent(new RouteUpdateEvent(new Route(
 				TransportMeans.Air, 
 				this.state.getLocationForName("Auckland"), 
 				this.state.getLocationForName("Sofia"), 
 				this.state.getAllCarriers().iterator().next())));
 		
 		assertNotNull(this.route.calculateRoute(
 				Priority.International_Air, 
 				this.state.getLocationForName("Wellington"), 
 				this.state.getLocationForName("Sofia"), 
 				1, 1));
 	}
 }
