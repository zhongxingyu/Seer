 package cars;
 
 import static java.lang.Double.parseDouble;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class DbReader {
 	public static Db read(String filename, int count) {
 		Car[] db = new Car[count];
 		double[][] attrDb = new double[count][];
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(filename));
 
 			for (int i = 0; i < count; i++) {
 				String line = br.readLine();
 				String[] splitted = line.split(";");
 
 				int attrCount = splitted.length - 2;
 				double[] attr = new double[attrCount];
 
 				for (int j = 0; j < attrCount; j++) {
 					attr[j] = parseDouble(splitted[j + 2]);
 				}
 
 				Car car = new Car();
 				car.make = splitted[0];
 				car.model = splitted[1];
 				car.attr = attr;
 				db[i] = car;
 				attrDb[i] = attr;
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Db ret = new Db();
 		ret.db = db;
 		ret.attrDb = attrDb;
 		return ret;
 	}
 	
 	static class Db {
 		public Car[] db;
 		public double[][] attrDb;
 	}
 }
