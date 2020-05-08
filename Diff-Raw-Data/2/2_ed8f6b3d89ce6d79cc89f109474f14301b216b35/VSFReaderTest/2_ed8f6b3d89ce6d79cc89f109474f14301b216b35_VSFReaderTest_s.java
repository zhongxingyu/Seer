 package tests;
 
 import java.io.*;
 import java.text.ParseException;
 import java.util.Calendar;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import common.*;
 import common.Vessel.VesselType;
 
 import simulator.SimulatorConfiguration;
 
 public class VSFReaderTest {
 
 	@Test
 	public void testValid() throws IOException, ParseException {
 		Calendar reftime = Calendar.getInstance();
 		SimulatorConfiguration sc = getConfig("assets/tests.valid.vsf", reftime);
 		assertEquals("001", sc.getVersion());
 		assertEquals(0, sc.getStartDelay());
		assertEquals(2, sc.getTimeInterval());
 		assertEquals(500, sc.getTotalTime());
 		assertEquals(5000, sc.getRadarRange());
 		assertEquals(3, sc.getVessels().size());
 		assertEquals("001", sc.getVessels().get(0).getId());
 		assertEquals("002", sc.getVessels().get(1).getId());
 		assertEquals("003", sc.getVessels().get(2).getId());
 		assertEquals(VesselType.SWIMMER, sc.getVessels().get(0).getType());
 		assertEquals(VesselType.FISHING_BOAT, sc.getVessels().get(1).getType());
 		assertEquals(VesselType.SPEED_BOAT, sc.getVessels().get(2).getType());
 		assertEquals(new Coord(4990, 0), sc.getVessels().get(0).getCoord(reftime));
 		assertEquals(new Coord(-3590, -2500), sc.getVessels().get(1).getCoord(reftime));
 		assertEquals(new Coord(0, 100), sc.getVessels().get(2).getCoord(reftime));
 		assertEquals(new Course(0.1, 0.1), sc.getVessels().get(0).getCourse(reftime));
 		assertEquals(new Course(5, 5), sc.getVessels().get(1).getCourse(reftime));
 		assertEquals(new Course(6, 0), sc.getVessels().get(2).getCourse(reftime));
 	}
 	
 	@Test
 	public void testInvalid() throws IOException, ParseException {
 		Calendar reftime = Calendar.getInstance();
 		boolean success = false;
 		try {
 			getConfig("assets/tests.invalid.vsf", reftime);
 		}
 		catch (ParseException e) {
 			success = true;
 		}
 		assertTrue(success);
 	}
 	
 	@Test
 	public void testIncorrectData() throws IOException, ParseException {
 		Calendar reftime = Calendar.getInstance();
 		boolean success = false;
 		try {
 			getConfig("assets/tests.incorrect.vsf", reftime);
 		}
 		catch (NumberFormatException e) {
 			success = true;
 		}
 		catch (ParseException e) {
 			success = true;
 		}
 		assertTrue(success);
 	}
 	
 	private SimulatorConfiguration getConfig(String path, Calendar reftime) throws IOException, ParseException {
 		BufferedReader bf = new BufferedReader(new FileReader(path));
 		SimulatorConfiguration sc = SimulatorConfiguration.parseVSF(bf, reftime);
 		bf.close();
 		return sc;
 	}
 
 }
