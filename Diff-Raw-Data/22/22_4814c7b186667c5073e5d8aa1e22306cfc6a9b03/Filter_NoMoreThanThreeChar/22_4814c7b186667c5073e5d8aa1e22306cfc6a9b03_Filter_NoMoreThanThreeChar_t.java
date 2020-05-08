 package cs151.hw2.ex4_08;
 
 /**
  * Filter obj to filter strings with length less than or equal to 3
  */
 public class Filter_NoMoreThanThreeChar implements Filter{
 	
	/**
 	 * Filter the String
 	 * @return true if string length is less than or equal to 3, otherwise false
 	 */
 	public boolean accept(String x) {
 		if (x.length() <= 3) return true;
 		else return false;
 	}
 }
