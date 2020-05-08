 package tedx.mpk.data;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.content.Context;
 
 import tedx.mpk.Station;
 
 public class CSVDatabase {
 
 	private List<Stop> stopList = new ArrayList<Stop>();
 	private Context context;
 
 	public Station findNearest(Point point) {
 		double min = Double.MAX_VALUE;
 		Station current = null;
 		for (Stop stop : stopList)
 			if (min > point.distance(stop.getPoint())) {
 				min = point.distance(stop.getPoint());
 				current = stop;
 			}
 		return current;
 	}
 
 	private void loadStops() throws IOException {
 		CSVLoader loader = new CSVLoader("stops.csv");
 		List<String> line;
 		while((line=loader.nextRow()) != null)
 		{
 			stopList.add(new Stop(line.get(1),
 						convertCoordFromString(line.get(4)),
 						convertCoordFromString(line.get(5))));
 		}
 	}
 
	static long convertCoordFromString(String t) {
		return (long) (Double.valueOf(t) * 1000 * 1000);
 	}
 	
 	class CSVLoader {
 		private BufferedReader reader;
 		
 		public CSVLoader(String filename) throws IOException {
 			reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
 						
 		}
 		
 		public List<String> nextRow() throws IOException
 		{
 			String line = reader.readLine();
 			if(line != null)
 				return Arrays.asList(line.split(","));
 			return null;
 		}
 	}
 }
