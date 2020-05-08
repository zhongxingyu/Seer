 package utils;
 
import org.apache.commons.lang.time.StopWatch;
 
 public class Utils {
 	public final static String ALPHABET = "ATGC";
 	public static int toInt(char c)
 	{
 		if (c == 'N')
 			return -1;
 		return ALPHABET.indexOf(c); 
 	}
 	public static int fromInt(int i)
 	{
 		if (i == -1)
 			return 'N';
 		return ALPHABET.charAt(i);
 	}
 	public static int[] toInt(String s)
 	{
 		int[] res = new int[s.length()];
 		for (int i = 0; i < s.length(); i++) {
 			res[i] = toInt(s.charAt(i));
 		}
 		return res;
 	}
 	public static String fromInt(int[] s)
 	{
 		StringBuilder res = new StringBuilder();
 		for (int i = 0; i < s.length; i++) {
 			res.append(fromInt(s[i]));
 		}
 		return res.toString();
 	}
 	public static int[] fwdCodes(int[] codes, int depth)
 	{
 		int[] res = new int[codes.length];
 		for (int i = 0; i < codes.length-depth+1; i++) {
 			int x = 0;
 			for (int j = 0; j < depth; j++)
 				if (codes[i+j] == -1) {
 					x = -1;
 					break;
 				} else {
 					x = x*4 + codes[i+j];
 				}
 			res[i] = x;
 		}
 		return res;
 	}
 	public static int[] backInvCodes(int[] codes, int depth)
 	{
 		int[] res = new int[codes.length];
 		for (int i = 0; i < codes.length-depth+1; i++) {
 			int x = 0;
 			for (int j = depth-1; j >= 0; j--)
 				if (codes[i+j] == -1) {
 					x = -1;
 					break;
 				} else {
 					x = x*4 + (codes[i+j]^1);
 				}
 			res[i] = x;
 		}
 		return res;
 	}
 	public static double getFAT(int[] codes, int s, int e) {
 		int k = 0, n = 0;
 		for (int i = s; i <= e; i++) {
 			if (codes[i] == -1) continue;
 			++n;
 			if (codes[i] < 2)
 				++k;
 		}
 		return k/(double)n;
 	}
 	public static double getFAT(int[] codes) {
 		return getFAT(codes,0,codes.length-1);
 	}
 	public static boolean isFAT(int[] codes, int s, int e) {
 		double fat = getFAT(codes,s,e);
 		return fat <= 0.35 || 0.65 <= fat;
 	}
 	public static boolean isFAT(int[] codes) {
 		return isFAT(codes,0,codes.length-1);
 	}
 	
	static StopWatch globalTime = new StopWatch();
	static {
		globalTime.start();
	}
 	public static void log(String message) {
		System.out.println(globalTime + " " + message);
 	}
 }
