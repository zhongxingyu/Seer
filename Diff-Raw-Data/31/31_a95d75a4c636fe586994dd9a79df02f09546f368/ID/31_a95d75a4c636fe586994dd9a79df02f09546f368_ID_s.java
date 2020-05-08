public ID
 {
 	public static long nextVal = 0;
	public static final long val;
 	
 	public ID()
 	{
 		val = nextVal;
 		nextVal++;
 	}
 }
