 package edu.umich.eecs.data;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 
 import edu.umich.eecs.dto.Cell;
 import edu.umich.eecs.dto.GpsPosition;
 import edu.umich.eecs.service.CellService;
 import edu.umich.eecs.service.MDCCellSpanService;
 import edu.umich.eecs.util.Tic;
 
 
 /**
  * This program reads the MDC GSM files and tries to find GPS location data associated with
  * each tower.
  * @author Pedro
  *
  */
 public class CellGpsDataImporter {
 	
 	public int matchingToleranceInSeconds = 40;
 	
 	/**
 	 * This class represents a single GPS log.
 	 * @author Pedro
 	 *
 	 */
 	public static class GpsTimestampedData implements Comparable<GpsTimestampedData> {
 		public double latitude, longitude;
 		public int time; // seconds since unix epoch
 		// Reads a GPS location line from the file
 		public GpsTimestampedData(Scanner scanner) {
 			// Format:
 			// userid	time	tz	gps_time	altitude	longitude	latitude	speed	heading	horizontal_accuracy	horizontal_dop	vertical_accuracy	vertical_dop	speed_accuracy	heading_accuracy	time_since_gps_boot
 			scanner.next();
 			time = scanner.nextInt();
 			scanner.next(); // tz
 			scanner.next(); // gps_time
 			scanner.next(); // altitude
 			longitude = scanner.nextDouble();
 			latitude = scanner.nextDouble();
 			scanner.nextLine(); // TODO
 		}
 		@Override
 		public int compareTo(GpsTimestampedData arg0) {
 			return Integer.compare(this.time, arg0.time);
 		}
 	}
 	
 	public CellGpsDataImporter(int matchingToleranceInSeconds) {
 		super();
 		this.matchingToleranceInSeconds = matchingToleranceInSeconds;
 	}
 	
 	public int getMatchingToleranceInSeconds() {
 		return matchingToleranceInSeconds;
 	}
 
 	public void setMatchingToleranceInSeconds(int matchingToleranceInSeconds) {
 		this.matchingToleranceInSeconds = matchingToleranceInSeconds;
 	}
 
 	/**
 	 * Goes through the GSM file and tries to find matching GPS logs in the GPS file. Matching
 	 * means that the GSM and GPS logs are close to each other. This is a parameter controlled by
 	 * the maximumToleranceInSeconds variable. We return a map of Cells and all matching GPS logs.
 	 * @param gsmFile
 	 * @param gpsFile
 	 * @param cellLocationMap
 	 * @return
 	 * @throws FileNotFoundException
 	 */
 	public HashMap<Cell, List<GpsTimestampedData>> readGpsGsmFilePair(
 			File gsmFile, File gpsFile,
 			HashMap<Cell, List<GpsTimestampedData>> cellLocationMap)
 			throws FileNotFoundException {
 		//
 		// The basic algorithm works like this:
 		//	- Read the whole GPS file and put it in memory.
 		//  - Sort the list of GPS logs so we can do binary search.
 		//  - For every entry in the GSM log, try to find a GPS log that's approximately at the same time.
 		//    - If we find it, add it to the list of GPS positions associated with that tower.
 		//
 		List<GpsTimestampedData> gpsData = readGpsFile(new Scanner(gpsFile));
 		Collections.sort(gpsData); // we sort it because we do a binary search
 		Scanner gsmScanner = new Scanner(gsmFile);
 		gsmScanner.nextLine(); // skip header
 		while(gsmScanner.hasNext()) {
 			//
 			// Read the line and try to find a corresponding GPS location.
 			//
 			// Format: userid	time	tz	country_code	network_code	cell_id	area_code	signal	signaldbm
 			gsmScanner.nextInt(); // userId;
 			int time = gsmScanner.nextInt();
 			gsmScanner.next(); // timezone is not used
 			int countryID = gsmScanner.nextInt();
 			int networkID = gsmScanner.nextInt();
 			int cellID = gsmScanner.nextInt();
 			int areaID = gsmScanner.nextInt();
 			gsmScanner.nextLine();
 			
 			GpsTimestampedData associatedGps = findMatchingGpsData(time, gpsData, matchingToleranceInSeconds);
 			if(associatedGps == null) {
 				continue;
 			}
 			
 			Cell cell = new Cell(countryID, networkID, areaID, cellID);
 			if(!cellLocationMap.containsKey(cell)) {
 				cellLocationMap.put(cell, new ArrayList<GpsTimestampedData>());
 			}
 			cellLocationMap.get(cell).add(associatedGps);
 		}
 		return cellLocationMap;
 	}
 	
 	/**
 	 * Given a map of cell towers and all their GPS sightings, we compute our GPS position estimate by
 	 * averaging the latitudes and longitudes. We also calculate an approximation of the standard deviation.
 	 * @param cellGpsData
 	 * @return
 	 */
 	public HashMap<Cell, GpsPosition> computeCellGpsPosition(HashMap<Cell, List<GpsTimestampedData>> cellGpsData) {
 		HashMap<Cell, GpsPosition> cellPosition = new HashMap<>(cellGpsData.size());
 		for (Cell cell : cellGpsData.keySet()) {
 			double latitudeSum = 0;
 			double longitudeSum = 0;
 			List<GpsTimestampedData> gpsDataList = cellGpsData.get(cell);
 			for(GpsTimestampedData gpsData : gpsDataList) {
 				latitudeSum += gpsData.latitude;
 				longitudeSum += gpsData.longitude;
 			}
 			double latitudeMean = latitudeSum / gpsDataList.size();
 			double longitudeMean = longitudeSum / gpsDataList.size();
 			
 			double latitudeVarianceSum = 0;
 			double longitudeVarianceSum = 0;
 			for(GpsTimestampedData gpsData : gpsDataList) {
 				latitudeVarianceSum += Math.pow(gpsData.latitude - latitudeMean, 2);
 				longitudeVarianceSum +=  Math.pow(gpsData.longitude - longitudeMean, 2);;
 			}
 			
 			double latitudeVariance = latitudeVarianceSum / gpsDataList.size();
 			double longitudeVariance = longitudeVarianceSum / gpsDataList.size();
 			
 			//
 			// Our standard deviation is a little fishy -- it doesn't make sense to think
 			// of a single standard deviation of a two-dimensional value. We just average
 			// the longitude and latitude variances and take the sqrt of that.
 			//
 			
 			double positionStdDev = Math.sqrt((latitudeVariance + longitudeVariance) / 2);
 			
 			GpsPosition gpsPosition = new GpsPosition(latitudeMean, longitudeMean, positionStdDev);
 			gpsPosition.setCountSightings(gpsDataList.size());
 			cellPosition.put(cell, gpsPosition);
 		}
 		return cellPosition;
 	}
 
 	/**
 	 * Tries to find a GPS log whose timespace is within toleranceInSeconds seconds of the needleTime.
 	 * We do a binary search so this is O(log n), and probably pretty close to Omega(log n) since it's
 	 * quite rare to find a GPS timestamp that matches exactly.
 	 * needleTime exactly.
 	 * @param needleTime
 	 * @param gpsData
 	 * @param toleranceInSeconds
 	 * @return
 	 */
 	public GpsTimestampedData findMatchingGpsData(int needleTime, List<GpsTimestampedData> gpsData, int toleranceInSeconds) {
 		//
 		// We do a binary search keyed on the timestamp. gpsData therefore needs to be sorted.
 		//
 		int lowIx = 0;
 		int highIx = gpsData.size() - 1;
 		while(lowIx <= highIx) {
 			int midIx = lowIx + (highIx - lowIx) / 2;
 			if(needleTime < gpsData.get(midIx).time) {
 				highIx = midIx - 1;
 			} else if(needleTime > gpsData.get(midIx).time) {
 				lowIx = midIx + 1;
 			} else {
 				// BOOM! exact match
 				return gpsData.get(midIx);
 			}
 		}
 		//
 		// We didn't find an exact match, but are we close enough? First, let's
 		// see which of the two pointers is closest to needleTime. Then we see if that's
 		// close enough (i.e. within the admitted tolerance).
 		//
 		// Our pointers might be out of bounds so the next couple of ugly lines handle that.
 		//
 		GpsTimestampedData low = lowIx < gpsData.size() && lowIx >= 0 ? gpsData.get(lowIx) : null;
 		GpsTimestampedData high = highIx < gpsData.size() && highIx >= 0 ? gpsData.get(highIx) :  null;
 		GpsTimestampedData closest = null;
 		if(low != null && high != null) {
 			closest = Math.abs(low.time - needleTime) < Math.abs(high.time - needleTime)? low : high;
 		} else if(high != null) {
 			closest = high;
 		} else if(low != null) {
 			closest = low;
 		}
 		if(closest != null && isWithinRange(needleTime, closest, toleranceInSeconds)) {
 			return closest;
 		} else {
 			return null;
 		}
 	}
 	
 	private boolean isWithinRange(int needleTime,
 			GpsTimestampedData gpsTimestampedData, int toleranceInSeconds) {
 		return Math.abs(needleTime - gpsTimestampedData.time) <= toleranceInSeconds; 
 	}
 
 	/**
 	 * Reads the whole GPS log file and put it in memory.
 	 * @param gpsScanner
 	 * @return
 	 */
 	public List<GpsTimestampedData> readGpsFile(Scanner gpsScanner) {
 		List<GpsTimestampedData> data = new ArrayList<>();
 		gpsScanner.nextLine(); // skip first line
 		while(gpsScanner.hasNext()) {
 			GpsTimestampedData currentLine = new GpsTimestampedData(gpsScanner);
 			data.add(currentLine);
 		}
 		return data;
 	}
 	
 	public static void main(String[] args) throws FileNotFoundException {
 		Tic clock = new Tic(); clock.setVerbose(true);
 		CellService cellSvc = new CellService();
 		//
 		// Replace this with the path to where the personid/gsm.csv files are.
 		//
 		
 		String pathToCsvFiles = "C:\\Users\\Pedro\\Desktop\\589 project data\\mdc2012-373-taskopen\\mdc2012-373-taskopen\\";
 		//
 		// Every person has a directory with her own ID. Inside that directory are files called
 		// "gsm.csv" and "gps.csv" with the data we want.
 		//
 		File dir = new File(pathToCsvFiles);
 		int count = 0;
 		String[] contents = dir.list();
 		//
 		// This specifies a maximum 40 second tolerance between GPS and GSM timestamps for a position
 		// association to be made.
 		//
 		CellGpsDataImporter importer = new CellGpsDataImporter(40);
 		HashMap<Cell, List<GpsTimestampedData>> cellGpsDataMap = new HashMap<>();
 		for (String personDir : contents) {
 			importer.readGpsGsmFilePair(new File(pathToCsvFiles + personDir + "\\gsm.csv"),
 					new File(pathToCsvFiles + personDir + "\\gps.csv"),
 					cellGpsDataMap);
 
 			System.out.println(++count + "/" + contents.length + " CSV files analyzed DB");
			break;
 		}
 
 		HashMap<Cell, GpsPosition> cellGpsPosition = importer.computeCellGpsPosition(cellGpsDataMap);
 		Collection<Cell> cells = cellGpsPosition.keySet();
 		for(Cell cell : cells) {
 			cell.setGpsPosition(cellGpsPosition.get(cell));
 		}
 		try {
 			cellSvc.saveCells(cells);
 		} finally {
 			cellSvc.tearDown();
 		}
 		clock.toc();
 	}
 }
