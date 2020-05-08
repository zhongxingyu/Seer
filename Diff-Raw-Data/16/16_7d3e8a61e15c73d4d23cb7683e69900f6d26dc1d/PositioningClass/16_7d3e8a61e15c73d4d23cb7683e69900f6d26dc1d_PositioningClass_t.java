 package de.unidue.wiwi.tdr.kn3.rasp_home;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import android.content.Context;
 import android.net.wifi.ScanResult;
 
 public class PositioningClass implements Serializable {
 
 	private static final long serialVersionUID = -6557936159576437331L;
 
 	private Map<String, List<Position>> locations;
 	private Map<String, Position> meanLocations;
 
 	public PositioningClass() {
 		locations = new HashMap<String, List<Position>>();
 		meanLocations = new HashMap<String, Position>();
 	}
 
 	public void updateLocations(String[] updateLocations) {
 		for (String location : updateLocations) {
 			if (!locations.containsKey(location)) {
 				locations.put(location, new ArrayList<Position>());
 			}
 		}
 		for (String location : locations.keySet()) {
 			boolean contains = false;
 			for (String updateLocation : updateLocations) {
 				if (location.equals(updateLocation)) {
 					contains = true;
 					break;
 				}
 			}
 			if (!contains) {
 				locations.remove(location);
 			}
 		}
 	}
 
 	public boolean addPositionScanResults(String location, List<ScanResult> results) {
		if (locations.containsKey(location) && results.size() >= 3) {
 			locations.get(location).add(new Position(results));
 			if (meanLocations.containsKey(location)) {
 				meanLocations.get(location).updatePosition(results);
 			} else {
 				meanLocations.put(location, new Position(results));
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public Set<String> getLocations() {
 		return locations.keySet();
 	}
 
 	public int getPositionsCount(String location) {
 		if (locations.containsKey(location)) {
 			return locations.get(location).size();
 		} else {
 			return 0;
 		}
 	}
 
 	public boolean removePositions(String location) {
 		if (locations.containsKey(location)) {
 			locations.get(location).clear();
 			if (meanLocations.containsKey(location)) {
 				meanLocations.remove(location);
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public String getBestLocation(Position comparePosition) {
 		String location = "";
 		double distance = 0, bestDistance = Double.MAX_VALUE;
 
 		for (Entry<String, Position> meanPosition : meanLocations.entrySet()) {
 			distance = meanPosition.getValue().getDistance(comparePosition);
			if (distance < bestDistance) {
 				location = meanPosition.getKey();
 				bestDistance = distance;
 			}
 		}
 
 		return location;
 	}
 
 	public static PositioningClass loadPositions(Context context, String fileName) {
 		PositioningClass positions = new PositioningClass();
 		if (context.getFileStreamPath(fileName).exists()) {
 			try {
 				FileInputStream fis = context.openFileInput(fileName);
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				positions = (PositioningClass) ois.readObject();
 				ois.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return positions;
 	}
 
 	public static void savePositions(Context context, String fileName, PositioningClass positions) {
 		try {
 			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(positions);
 			oos.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static class Position implements Serializable {
 
 		private static final long serialVersionUID = 5710190099743727158L;
 
 		private Map<String, Integer> stations;
 
 		public Position(List<ScanResult> results) {
 			stations = new HashMap<String, Integer>();
 			for (ScanResult result : results) {
 				stations.put(result.BSSID, result.level);
 			}
 		}
 
 		public double getDistance(Position position) {
 			double distance = 0;
 			for (Entry<String, Integer> station : stations.entrySet()) {
 				if (position.stations.get(station.getKey()) != null) {
 					distance += Math.pow(station.getValue() - position.stations.get(station.getKey()), 2);
 				}
 			}
			return Math.sqrt(distance);
 		}
 
 		public void updatePosition(List<ScanResult> results) {
 			for (ScanResult result : results) {
 				if (stations.containsKey(result.BSSID)) {
 					stations.put(result.BSSID, (stations.get(result.BSSID) + result.level) / 2);
 				} else {
 					stations.put(result.BSSID, result.level);
 				}
 			}
 		}
 
 	}
 
 }
