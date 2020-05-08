 import java.util.ArrayList;
 import java.util.List;
 
 public class MainTest {
 
 	public static Float fl(int i) {
 		return new Float(i);
 	}
 	public static Float fl(double d) {
 		return new Float(d);
 	}
 
 	public String loadFile(String addr) {
 		return this.getClass().getResource(addr).getPath();
 	}
 
 	public static void main(String[] args) {
 		
 		float zap = 0;
 		int count = 100;
 		float epsilon = 0;
 		int z0 = -1;
 		
 		if (args.length < 1)
			System.out.println("Usage : java MainTest file [zap count epsilon z]");
 		else {
 			
 			String file = args[0];
 			if (args.length > 1)
 				zap = Float.parseFloat(args[1]);
 			if (args.length > 2)
 				count = Integer.parseInt(args[2]);
 			if (args.length > 3)
 				epsilon = Float.parseFloat(args[3]);
 			if (args.length > 4)
 				z0 = Integer.parseInt(args[4]);
 			
 			
 			(new Test(file, true)).pagerank(zap, count, epsilon, z0);
 			
 		}
 	}
 }
