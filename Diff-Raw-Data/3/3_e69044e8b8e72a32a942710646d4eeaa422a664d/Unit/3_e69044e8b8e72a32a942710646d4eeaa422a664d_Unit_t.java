 package org.gvsig.units;
 
 import java.util.ArrayList;
 
 public enum Unit {
 	KILOMETERS("Kilometros", "Km", 1000), METERS("Metros", "m", 1), CENTIMETERS(
 			"Centimetros", "cm", 0.01), MILIMETERS("Milimetros", "mm", 0.001), MILES(
 			"Millas", "mi", 1609.344), YARDS("Yardas", "Ya", 0.9144), FEET(
			"Pies", "ft", 0.3048), INCHES("Pulgadas", "inche", 0.0254), DEGREES(
			"Grados", "º", 1 / 8.983152841195214E-6);
 
 	public String name;
 	public String symbol;
 	private double toMeter;
 	private double toSquareMeter;
 
 	Unit(String name, String symbol, double toMeter) {
 		this.name = name;
 		this.symbol = symbol;
 		this.toMeter = toMeter;
 		this.toSquareMeter = Math.pow(toMeter, 2);
 	}
 
 	public double toMeter() {
 		return toMeter;
 	}
 
 	public double toSquareMeter() {
 		return toSquareMeter;
 	}
 
 	public static String[] getDistanceNames() {
 		ArrayList<String> ret = new ArrayList<String>();
 		for (Unit unit : Unit.values()) {
 			ret.add(unit.name);
 		}
 
 		return ret.toArray(new String[0]);
 	}
 
 	public static String[] getDistanceSymbols() {
 		ArrayList<String> ret = new ArrayList<String>();
 		for (Unit unit : Unit.values()) {
 			ret.add(unit.symbol);
 		}
 
 		return ret.toArray(new String[0]);
 	}
 
 	/**
 	 * @param name
 	 * @return
 	 * @throws IllegalArgumentException
 	 *             If there is no unit with the specified name
 	 */
 	public static Unit fromName(String name) throws IllegalArgumentException {
 		for (Unit unit : values()) {
 			if (unit.name.equals(name)) {
 				return unit;
 			}
 		}
 
 		throw new IllegalArgumentException("No such unit: " + name);
 	}
 
 	public String getSquareSuffix() {
 		/*
 		 * All units are linear
 		 */
 		return String.valueOf((char) 178);
 	}
 }
