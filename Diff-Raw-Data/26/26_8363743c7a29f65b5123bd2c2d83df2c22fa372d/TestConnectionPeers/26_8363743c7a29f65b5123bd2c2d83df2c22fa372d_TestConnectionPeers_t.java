 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Formatter;
 import java.util.TreeMap;
 
 import org.ardverk.coding.BencodingInputStream;
 
 public class TestConnectionPeers {
 	public static void main(String[] args) throws IOException {
 		// HOST = 192.168.0.11
 		// TRACKER = 95.215.62.5 port = http (80)
 		
 		BufferedInputStream is = new BufferedInputStream(new FileInputStream("data"));
 		File file = new File("data"); 
 		long size = file.length();
 		byte[] buffer = new byte[(int) size]; 
 		
 		is.read(buffer); 
 		System.out.println(getPos(buffer, 0, 4));
 		System.out.println(getPos(buffer, 4, 4));
 		System.out.println(getPos(buffer, 8, 4));
 		System.out.println(getPos(buffer, 12, 4));
 		System.out.println(getPos(buffer, 16, 4));
 		System.out.println(getPos(buffer, 16, 4));
 		
		for (int i = 20; i < size; i += 6) { 
			System.out.println(getIp(buffer, i));
			System.out.println(getPort(buffer, i + 4));
		}
//		BencodingInputStream input = new BencodingInputStream(
//				new FileInputStream("data1"));
//		TreeMap tree = (TreeMap) input.readMap();		
//		System.out.println(tree.keySet());
//		TreeMap a = (TreeMap) tree.get("a");
//		System.out.println(a.keySet());
//		System.out.println(a.get("id").toString());
//		
//		System.out.println(tree.get("q"));
 	}
 	
 	public static String getPos(byte[] a, int start, int size) { 
 		Formatter format = new Formatter();
 		
 		for (int i = start; i < start + size; i++) { 
 			format.format("%02x", a[i]);
 		}
 		return format.toString();
 	}
 	
 	public static String getIp(byte[] a, int start) { 
 		String result = "";
 		for (int i = start; i < start + 4; i++) { 
 			String hex = getPos(a, i, 1); 
 			int x = Integer.parseInt(hex, 16);
 			result += x  + ".";
 		}
 		return result;
 	}
 	
 	public static int getPort(byte[] a, int start) { 
 		String hex = getPos(a, start, 2); 
 		return Integer.parseInt(hex, 16);
 	}
 }
