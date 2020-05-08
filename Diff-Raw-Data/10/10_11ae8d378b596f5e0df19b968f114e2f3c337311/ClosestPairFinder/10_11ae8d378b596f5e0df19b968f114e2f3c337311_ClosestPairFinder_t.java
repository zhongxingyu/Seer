 package closestpair;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 public class ClosestPairFinder {
 	private static double minimum;
 
 	/** @param args */
 	public static void main(String[] args) {
 		List<Point> list = parse(args[0]);
 		Finder finder = new Finder(list);
 		minimum = finder.solve();
		System.out.println(minimum);
 	}
 
 	private static List<Point> parse(String file) {
 		Scanner scanner = null;
 		try {
 			scanner = new Scanner(new File(file));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
		
		if (file.endsWith(".tsp")) {
			while (!scanner.nextLine().equals("NODE_COORD_SECTION")) {}
		}
		
 		List<Point> list = new ArrayList<Point>();
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine().trim();
			if (!line.equals("") && !line.equals("EOF")) {
 				String pointData[] = line.split("\\s+");
 				list.add(new Point(pointData[0], Double.valueOf(pointData[1]), Double.valueOf(pointData[2])));
 			}
 		}
 		return list;
 	}
 
 	public static double lastMinimum() {
 		return minimum;
 	}
 
 }
