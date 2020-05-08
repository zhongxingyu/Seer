 public class ID
 {
 	public static long nextVal = 0;
 	public long val = 0;
 	
 	public ID()
 	{
 		val = nextVal;
 		nextVal++;
 	}
 	
 	public String toString(int numberOfPlaces)
 	{
		int numberOfZeros = numberOfPlaces-1;
		if(val != 0)
			numberOfZeros = numberOfPlaces - ((int)Math.log10(val)+1);
		
 		String zeros = "";
 		for(int i = 0; i < numberOfZeros; i++)
 			zeros += "0";
		
 		return zeros+val;
 	}
 	
 	public String toString()
 	{
 		return toString(3);
 	}
 }
