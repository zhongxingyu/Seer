 package se.chalmers.datastrukt.lab1;
 
 /**
  * 
  * @author tomassellden and Anton Palmqvist group 36
  * 
  */
 public class Uppgift2 {
 	/**
 	 * check if the input sqr is higher than 1 and then the method call the
 	 * helpmethod help
 	 * 
 	 * @param sqr
 	 *            the value you want to calculate the squareroot from
 	 * @param eps
 	 *            the margin of error for the calculated square root
 	 * @return
 	 */
 	public static double binarySqrt(double sqr, double eps) {
 		if (sqr <= 1) {
 			throw new IllegalArgumentException(
 					"The number you want to square must be over =>1");
 		}
 		return help(sqr, eps, 1, sqr);
 
 	}
 
 	/**
 	 * Find the square root of given number sqr with a margin of error eps
 	 * 
 	 * @param sqr
 	 *            the value you want to calculate the squareroot from
 	 * @param eps
 	 *            the margin of error for the calculated square root
 	 * @param low
 	 *            the lowest value the square root can be
 	 * 
 	 * @param high
 	 *            the highest value the square root can be
 	 * @return double the square root of sqr with the margin of error eps
 	 */
 	//Remark: declared private since it is only called within the class
	private static double help(double sqr, double eps, double low, double high) {
 
 		if (Math.abs(high - low) < eps) {
 			return (high + low) / 2;
 		} else {
 			double tmpSqr = ((high - low) / 2) + low;
 			if (Math.pow(tmpSqr, 2) > sqr) {
 				high = tmpSqr;
 			} else {
 				low = tmpSqr;
 			}
 			return help(sqr, eps, low, high);
 		}
 	}
 
 	public static void main(String[] args) {
 		double a = 12354;
 		double eps = 10e-6;
 		// Test with Math.sqrt
		System.out.println("* Berknat vrde=" + binarySqrt(a, eps) + " # br vara nra " +
				"Math.sqrt-vrde=" + Math.sqrt(a)+ "\nmed en felmarginal p " + eps + "\n");
 		
 		//Test without Math.sqrt
 		double b = 100;
 		double bRot = binarySqrt(b, eps);
		System.out.println("* Berknat rotvrdevrde gnger sig sjlvt=" + bRot*bRot + " # " +
				"br vara nra ursprungsvrdet=" + b + "\nmed en felmarginal p " + eps + "\n");
 
 		double c = 37;
 		double cRot = binarySqrt(c, eps);
		System.out.println("* Berknat rotvrdevrde gnger sig sjlvt=" + cRot*cRot + " # " +
				"br vara nra ursprungsvrdet=" + c + "\nmed en felmarginal p " + eps + "\n");
 		
 		//Test of exception
 		//binarySqrt(-2, 1000);
		/*Exception "IllegalArgumentException: The number you want to square must be over =>1"
 			kastas p rtt stt. (Bortkommenterad fr att koden skall kras)*/
 	}
 }
