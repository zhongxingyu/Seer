 package at.fhv.smartdevices.singleNodeDHWH.tests;
 
 import java.util.Calendar;
 import org.junit.Before;
 import org.junit.Test;
 
 import at.fhv.smartdevices.datamining.MinkowskiMetric;
 import at.fhv.smartdevices.scheduling.Clock;
 import at.fhv.smartdevices.scheduling.DataAquisition;
 import at.fhv.smartdevices.scheduling.SchedulableSwitch;
 import at.fhv.smartdevices.scheduling.tests.TestHelper;
 import at.fhv.smartdevices.singleNodeDHWH.KNNBasedOptimization;
 import at.fhv.smartdevices.singleNodeDHWH.SimulatedDHWHController;
 
 public class KNNTests {
 
 	Calendar _currentTime = Calendar.getInstance();
 	private SimulatedDHWHController _controller;
 	private Clock _clock;
 	private DataAquisition _dm;
 	private SchedulableSwitch _switch;
 
 	@Before
 	public void before() {
 		_currentTime = Calendar.getInstance();
 		_currentTime.set(Calendar.HOUR, 1);
 		_currentTime.set(Calendar.AM_PM, Calendar.AM);
 		_currentTime.set(Calendar.MINUTE, 0);
 		_currentTime.set(Calendar.SECOND, 0);
 		_controller = new SimulatedDHWHController();
 		_clock = new Clock(_controller);
 		TestHelper.ClearDataManagerSerialization(_clock, _controller);
 		_dm = new DataAquisition(_controller, _clock, false);
 		_switch = new SchedulableSwitch(_controller, _clock, false);
 	}
 
 	@Test
 	public void testKNN() {
 		/*
 		 * SerializableTreeMap<Long, Boolean> switchMap = new
 		 * SerializableTreeMap<Long, Boolean>(); SerializableTreeMap<Long,
 		 * Float> temp = new SerializableTreeMap<Long, Float>();
 		 * SerializableTreeMap<Long, Float> demands = new
 		 * SerializableTreeMap<Long, Float>(); TreeMap<Long, Integer> costs =
 		 * new TreeMap<Long, Integer>(); float temp0 = 40.0f;
 		 * 
 		 * Random random = new Random(); Boolean u = false; Long i = 0L; for (i
 		 * = 0L; i < 100000; i++) { temp.put(i * deltat, temp0); float demand =
 		 * random.nextFloat() > 0.5 ? random.nextFloat() * 10 : 0; demands.put(i
 		 * * deltat, demand); byte uByte = 0; if (temp0 < 60) { uByte = 1; }
 		 * double[][] temp1 = SingleNodeDHWHThermalModel.simulateDHWH(uByte,
 		 * temp0, demand, deltat); switchMap.put(i * deltat, temp1[0][0] > 0);
 		 * temp0 = (float) temp1[1][1]; costs.put(i * deltat,
 		 * random.nextInt(10)); } for (Long j = i + 1; j < i + 100L; j++) {
 		 * costs.put(j * deltat, random.nextInt(10)); }
 		 */
 
		KNNBasedOptimization knn = new KNNBasedOptimization(_dm, _switch, "0", new MinkowskiMetric(2), 60*60*1000, 10, 10, (short) 1);
 
		//_controller.setHysteresisMode(40, 70);		
 		
 		for (int i = 0; i < 24*7; i++) {
			_currentTime.add(Calendar.HOUR, (int) 1);
			_clock.waitFor((long)1000*60*60);
			System.out.println(_clock.getDate());
 			_dm.run();
 		}		
 		_controller.setHysteresisMode(5, 90);
 		
 		knn.run();
 	}
 
 }
