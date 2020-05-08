 package ch.zhaw.jroute.model;
 
 import gov.nasa.worldwind.geom.Angle;
 import gov.nasa.worldwind.geom.Position;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.zhaw.jroute.model.businessObjects.Waypoint;
 import ch.zhaw.jroute.model.interfaces.IWaypointBuilder;
 import ch.zhaw.jroute.model.util.WaypointStatusEnum;
 
 public class WaypointBuilderTest {
 
 	private IWaypointBuilder builder;
 	private WaypointTestObserver observer;
 	private double testLat = 55.55;
 	private double testLon = 66.66;
 
 	@Before
 	public void setUp() throws Exception {
 		builder = new WaypointBuilder();
 		observer = new WaypointTestObserver();
 
 		builder.registerObserver(observer);
 	}
 
 	@Test
 	public void createWaypointFromPositionTest() {
 
 		Angle lat = Angle.fromDegreesLatitude(testLat);
 		Angle lon = Angle.fromDegreesLatitude(testLon);
 
 		builder.createWaypointFromPosition(new Position(lat, lon, 0));
 
 		assert (observer.isNotified());
 
 		assert (!builder.getWaypointList().isEmpty());
 
		assert (observer.getNotifierObj().getLat() == testLat);
		assert (observer.getNotifierObj().getLon() == testLon);

 		observer.reset();
 	}
 
 	@Test
 	public void setStartWaypointTest() {
 		builder.setStartWaypoint(WaypointStatusEnum.start, new Waypoint());
 
 		assert (observer.isStart());
 
 		assert (builder.getStartWaypoint() != null);
 
 		observer.reset();
 	}
 
 	@Test
 	public void setEndWaypoint() {
 		builder.setEndWaypoint(WaypointStatusEnum.end, new Waypoint());
 
 		assert (observer.isEnd());
 
		assert (builder.getEndWaypoint() == null);

 		observer.reset();
 	}
 
 	private class WaypointTestObserver implements Observer {
 
 		private boolean notified = false;
 		private boolean start = false;
 		private boolean end = false;
 
 		private Waypoint notifierObj;
 
 		@Override
 		public void update(Observable arg0, Object arg1) {
 			notified = true;
 
 			notifierObj = (Waypoint) arg1;
 
 			switch (notifierObj.getStatus()) {
 			case undefined:
 				break;
 			case start:
 				start = true;
 				break;
 			case end:
 				end = true;
 				break;
 			default:
 				break;
 			}
 
 		}
 
 		public boolean isNotified() {
 			return notified;
 		}
 
 		public void reset() {
 			this.notified = false;
 			this.start = false;
 			this.end = false;
 			notifierObj = null;
 		}
 
 		public boolean isStart() {
 			return start;
 		}
 
 		public boolean isEnd() {
 			return end;
 		}
 
		public Waypoint getNotifierObj() {
			return notifierObj;
		}

 	}
 
 }
