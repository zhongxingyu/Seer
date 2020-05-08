 package com.marbol.marbol;
 
 import java.util.HashMap;
 
 public class ImperialConverter implements MarbolUnitConverter {
 
 	private static final double km2tomi2 = 0.386102;
 	private static final double mpstomph = 2.23694;
 	private static final double mtoft = 3.28084;
 	private static final double ftpermi = 5280;
 	
 	@Override
 	public double[] convert(Adventure adv) {
 		
 		double[] retval = new double[4];
 		retval[0] = adv.getArea(Adventure.STANDARD_RADIUS) * km2tomi2;
 		retval[1] = adv.getAverageSpeed() * mpstomph;
 		retval[2] = adv.getElevationDiff() * mtoft;
 		retval[3] = (adv.getDistanceInMeters() * mtoft) / 5280;
 		return retval;
 	}
 
 	@Override
 	public HashMap<String, String> getUnit() {
 		
 		HashMap<String, String> units = new HashMap<String, String>();
 		units.put("area_unit", "mi\u00b2");
 		units.put("distance_unit", "mi");
 		units.put("elevation_unit", "ft");
 		units.put("speed_unit", "ft/s");
 		
		return units;
 	}
 	
 }
