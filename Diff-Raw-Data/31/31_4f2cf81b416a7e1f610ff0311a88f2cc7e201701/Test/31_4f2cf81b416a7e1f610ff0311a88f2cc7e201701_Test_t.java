 package zone.test;
 
import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
import zone.BlockIDWritable;
 import zone.Star;
 
 public class Test {
 	private static double theta = 0.1;
 	public static void main (String[] args) {
		int splitSize;	// the number of records
		Star stars[];
 		try {
			File file = new File (args[0]);
			splitSize = (int) (file.length() / Star.storeSize);
 			FileInputStream in = new FileInputStream(args[0]);
			stars = new Star[splitSize];
 			byte[] bytes = new byte[Star.storeSize];
 			for (int i = 0; i < splitSize; i++) {
 				int size = in.read(bytes);
 				if (size < bytes.length) {
 					break;
 				}
 				stars[i] = new Star();
 				stars[i].set(bytes);
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}

 		int num = 0;
 		for (int i = 0; i < splitSize; i++) {
			for (int j = i + 1; j < splitSize; j++) {
 				if (stars[i].x * stars[j].x + stars[i].y * stars[j].y + stars[i].z * stars[j].z > Math.cos(Math.toRadians(theta))) {
					BlockIDWritable loc = new BlockIDWritable(stars[i].ra, stars[i].dec);
					System.out.println(loc + "\t(" + stars[i] + "," + stars[j] + ")");
					System.out.println(loc + "\t(" + stars[j] + "," + stars[i] + ")");
 					num += 2;
 				}
 			}
 		}
 		System.out.println("num: " + num);
 	}
 }
