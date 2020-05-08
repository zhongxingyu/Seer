 package nz.ac.victoria.ecs.kpsmart.state.manipulation;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 
 import javax.persistence.EntityManager;
 
 import nz.ac.victoria.ecs.kpsmart.GuiceServletContextListner;
 import nz.ac.victoria.ecs.kpsmart.InjectOnCall;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Location;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.Route;
 import nz.ac.victoria.ecs.kpsmart.state.entities.state.TransportMeans;
 
 import org.hibernate.Session;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.google.inject.Inject;
 
 public class HibernateStateManipulatorDBTest {
 	@Inject
 	private Session session;
 	
 	@Before
 	public void setUp() throws Exception {
 		GuiceServletContextListner.init();
 	}
 
 	@Test @Ignore
 	public void testGetMailDelivery() {
 		
 	}
 
 	@Test @Ignore
 	public void testSaveMailDelivery() {
 		fail("Not yet implemented");
 	}
 
 	@Test @Ignore
 	public void testGetAllLocations() {
 		Location l = new Location();
 		l.setInternational(true);
 		l.setLatitude(-1);
 		l.setLongitude(1);
 		l.setName("fubar");
 		
 		assertEquals(Arrays.asList(l), new HibernateImpl().getAllLocations());
 	}
 	
 	@Test @InjectOnCall
 	public void testGetAllRoutes() throws Exception {
 		HibernateImpl sm = new HibernateImpl();
 		
		Route r1 = Route.newInstance();
 		r1.setCarrier(sm.getCarrier(1));
 		r1.setCarrierVolumeUnitCost((float) 10.9);
 		r1.setCarrierWeightUnitCost((float)10.9);
 		r1.setDisabled(false);
 		r1.setDuration(10);
 		r1.setEndPoint(sm.getLocationForName("Rome"));
 		r1.setFrequency(10);
 		r1.setStartPoint(sm.getLocationForName("Wellington"));
 		r1.setStartingTime(new SimpleDateFormat("y-m-d h:m:s").parse("2012-03-27 11:18:18"));
 		r1.setTransportMeans(TransportMeans.Air);
 		
		Route r2 = Route.newInstance();
 		r2.setCarrier(sm.getCarrier(1));
 		r2.setCarrierVolumeUnitCost((float) 8.0);
 		r2.setCarrierWeightUnitCost((float)8.9);
 		r2.setDisabled(false);
 		r2.setDuration(1);
 		r2.setEndPoint(sm.getLocationForName("Auckland"));
 		r2.setFrequency(6);
 		r2.setStartPoint(sm.getLocationForName("Wellington"));
 		r2.setStartingTime(new SimpleDateFormat("y-m-d h:m:s").parse("2012-03-27 11:45:18"));
 		r2.setTransportMeans(TransportMeans.Air);
 		
 		assertEquals(Arrays.asList(r1, r2), sm.getAllLocations());
 	}
 }
