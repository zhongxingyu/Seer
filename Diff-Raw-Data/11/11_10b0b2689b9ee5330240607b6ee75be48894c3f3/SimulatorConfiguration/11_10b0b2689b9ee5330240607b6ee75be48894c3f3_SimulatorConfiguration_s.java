 package simulator;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import common.Coord;
 import common.Course;
 import common.Vessel;
 import common.Vessel.VesselType;
 public class SimulatorConfiguration {
 	private int _StartDelay;
 	private int _TimeInterval;
 	private int _TotalTime;
 	private int _RadarRange;
 	
 	List<Vessel> _Vessels;
 	
 	// Force users to create configuration from VSF file
 	private SimulatorConfiguration() {
 		_Vessels = new ArrayList<Vessel>();
 	}
 
 	public int getStartDelay() {
 		return _StartDelay;
 	}
 	public void setStartDelay(int _StartDelay) {
 		this._StartDelay = _StartDelay;
 	}
 	public int getTimeInterval() {
 		return _TimeInterval;
 	}
 	public void setTimeInterval(int _TimeInterval) {
 		this._TimeInterval = _TimeInterval;
 	}
 	public int getTotalTime() {
 		return _TotalTime;
 	}
 	public void setTotalTime(int _TotalTime) {
 		this._TotalTime = _TotalTime;
 	}
 	public int getRadarRange() {
 		return _RadarRange;
 	}
 	public void setRadarRange(int _RadarRange) {
 		this._RadarRange = _RadarRange;
 	}
 	
 	public List<Vessel> getVessels() {
 		return _Vessels;
 	}
 	public void addVessel(Vessel v) {
 		_Vessels.add(v);
 	}
 	
 	/*
 	 * Parse a VSF file to read configuration.
 	 * Should throw ParseException whenever it encounters an unexpected
 	 * format in the input file.
 	 */
 	public static SimulatorConfiguration parseVSF(BufferedReader in) 
 			throws IOException, ParseException {
 		SimulatorConfiguration sc = new SimulatorConfiguration();
 		String line;
 		String version;
 		while ((line = in.readLine()) != null) {
 			String[] a = line.split("\\s+");
 			if (a[0].trim().compareToIgnoreCase("VSF") == 0)
 				version = a[1];
 			else if (a[0].trim().compareToIgnoreCase("STARTTIME") == 0)
 				sc.setStartDelay((int) Double.parseDouble(a[1]));
 			else if (a[0].trim().compareToIgnoreCase("TIMESTEP") == 0) {
 				sc.setTimeInterval((int) Double.parseDouble(a[1]));
 			}
 			else if (a[0].trim().compareToIgnoreCase("TIME") == 0)
 				sc.setTotalTime((int) Double.parseDouble(a[1]));
 			else if (a[0].trim().compareToIgnoreCase("RANGE") == 0)
 				sc.setRadarRange(Integer.parseInt(a[1]));
 			else if (a[0].trim().compareToIgnoreCase("NEWT") == 0 && Double.parseDouble(a[7]) == 0) {
 				Vessel v = new Vessel(a[1],
 						VesselType.values()[Integer.parseInt(a[2])]);
 				v.update(new Coord(Integer.parseInt(a[3]), Integer.parseInt(a[4])),
 						new Course(Integer.parseInt(a[5]), Integer.parseInt(a[6])),
 						Calendar.getInstance());
 				sc.addVessel(v);
 			}
 /*
 			
 			else if (a[0].trim().compareToIgnoreCase("NEWT") == 0 && Double.parseDouble(a[7]) > 0) {
 				Vessel v = new Vessel(a[1],
 						VesselType.values()[Integer.parseInt(a[2])]);
 				v.update(new Coord(Integer.parseInt(a[3]), Integer.parseInt(a[4])),
 						new Course(Integer.parseInt(a[5]), Integer.parseInt(a[6])),
 						Calendar.getInstance());
 				sc.addVessel(v);
 			}
 			**/
 			
 		} // END OF WHILE LOOP
 		
		// XXX TODO by Pinsson; set properties of sc by reading file
 		return sc;
 	}
 }
