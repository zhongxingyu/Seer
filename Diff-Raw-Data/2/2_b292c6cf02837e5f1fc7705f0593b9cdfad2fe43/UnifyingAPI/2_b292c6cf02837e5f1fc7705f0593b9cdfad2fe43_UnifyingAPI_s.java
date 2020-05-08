 public class UnifyingAPI
 {
	private native String convert_raw_to_unified(String data, int offset, int length,
 			String type, String desc, String sensor);
 			
 	private native String convert_unified_to_raw (String data);
 	
 	static
 	{
 		System.loadLibrary("CJavaInterface");
 	}
 }
