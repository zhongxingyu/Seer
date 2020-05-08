 package yuku.infinitepassgen.lib.v3;
 
 import android.util.Log;
 
 import yuku.androidcrypto.DigestType;
 import yuku.androidcrypto.Digester;
 
 public class PwgenV3 {
 	public static final String TAG = PwgenV3.class.getSimpleName();
 
 	public static final int KAR_UPPER = 0x08;
 	public static final int KAR_LOWER = 0x04;
 	public static final int KAR_NUMBER= 0x02;
 	public static final int KAR_SYMBOL= 0x01;
 
	public static String calculate(String M, String K, PwgenV3Options options) {
		return calculate(M, K, options, 64);
	}
	
	private static String calculate(String M, String K, PwgenV3Options options, int expand) {
 		StringBuilder S = new StringBuilder();
 		if (options == null || options.getFlags() == 0) {
 			// no characters selected
 			return null;
 		}
 
 		int retlen = expand*16;
 		byte[] G = new byte[retlen];
 
 		for (int i = 0; i < expand; i++) {
 			S.append(M).append("pemisah").append(K);
 			byte[] m = md5(S); 
 			for (int j = 0; j < 16; j++) {
 				G[i*16+j] = m[j];
 			}
 		}
 	
 		int x = options.getMax() - options.getMin() + 1;
 		x = (G[retlen-1] & 0xff) % x;
 		int len = options.getMin() + x;
 	
 		boolean[] allowed = new boolean[256];
 	
 		if (options.getUpper()) {
 			for (int i = 'A'; i <= 'Z'; i++) {
 				allowed[i] = true;
 			}
 		}
 	
 		if (options.getLower()) {
 			for (int i = 'a'; i <= 'z'; i++) {
 				allowed[i] = true;
 			}
 		}
 	
 		if (options.getNumber()) {
 			for (int i = '0'; i <= '9'; i++) {
 				allowed[i] = true;
 			}
 		}
 	
 		if (options.getSymbol()) {
 			allowed['.'] = true;
 			allowed[','] = true;
 			allowed['?'] = true;
 			allowed['!'] = true;
 			allowed['_'] = true;
 			allowed['/'] = true;
 			allowed['@'] = true;
 			allowed['#'] = true;
 			allowed['$'] = true;
 			allowed['%'] = true;
 			allowed['&'] = true;
 			allowed['*'] = true;
 		}
 	
 		StringBuilder result = new StringBuilder();
 	
 		for (int i = 0; i < retlen; i++) {
 			if (allowed[G[i] & 0xff]) {
 				result.append((char) (G[i] & 0xff));
 				if (result.length() >= len) {
 					break;
 				}
 			}
 		}
 	
 		if (result.length() < len) {
 			Log.w(TAG, "expand " + expand + " not enough, doubling");
 			return calculate(M, K, options, expand*2);
 		} else {
 			return result.toString();
 		}
 	}
 
 	private static byte[] md5(CharSequence s) {
 		return Digester.digest(DigestType.MD5, s.toString());
 	};
 }
