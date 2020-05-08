 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.util;
 
 public class NmeaUtil {
 	public static double nmeaDeg2Ndeg(double degrees) {
 		long deg = (long) degrees;
		double fra_part = degrees - (double) deg;
		return (((double) deg * 100.0) + (fra_part * 60.0));
 	}
 }
