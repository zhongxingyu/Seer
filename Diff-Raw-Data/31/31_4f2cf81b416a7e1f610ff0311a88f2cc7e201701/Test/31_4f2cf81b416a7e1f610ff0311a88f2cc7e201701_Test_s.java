 package zone.test;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import zone.Star;
 
 public class Test {
	private static int splitSize = 1000;	// the number of records
 	private static double theta = 0.1;
 	public static void main (String[] args) {
		Star stars[] = new Star[splitSize];
 		try {
 			FileInputStream in = new FileInputStream(args[0]);
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
			for (int j = 0; j < splitSize; j++) {
				if (i == j)
					continue;
				if (stars[i].objID > stars[j].objID)
					continue;
				
 				if (stars[i].x * stars[j].x + stars[i].y * stars[j].y + stars[i].z * stars[j].z > Math.cos(Math.toRadians(theta))) {
					System.out.println("(66,9)\t(" + stars[i] + "," + stars[j] + ")");
					System.out.println("(66,9)\t(" + stars[j] + "," + stars[i] + ")");
 					num += 2;
 				}
 			}
 		}
 		System.out.println("num: " + num);
 	}
 }
