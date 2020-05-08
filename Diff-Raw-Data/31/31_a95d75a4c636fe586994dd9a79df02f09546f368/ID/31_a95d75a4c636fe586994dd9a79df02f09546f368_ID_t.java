public class ID
 {
 	public static long nextVal = 0;
	public static long val = 0;
 	
 	public ID()
 	{
 		val = nextVal;
 		nextVal++;
 	}
 }
