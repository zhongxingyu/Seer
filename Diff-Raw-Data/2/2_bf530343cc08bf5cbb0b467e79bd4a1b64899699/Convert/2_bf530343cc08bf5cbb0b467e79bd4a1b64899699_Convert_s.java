 /**
  * Conversions for each unit, to and from Meters
  */
 package edu.usc.danielcantwell.converter;
 
 /**
  * @author Daniel
  *
  */
 public class Convert {
 	
 	/***********************************
 	 * Distance * (Meter as main unit) *
 	 ***********************************/
 	
 	// Kilometer
 	public static float kiloToM(float num) {
 		return (float) (num / .001);
 	}
 	public static float mToKilo(float num) {
 		return (float) (num * .001);
 	}
 	
 	// Centimeter
 	public static float centiToM(float num) {
 		return (float) (num / 100.0);
 	}
 	public static float mToCenti(float num) {
 		return (float) (num * 100.0);
 	}
 	
 	// Mile
 	public static float mileToM(float num) {
 		return (float) (num * 1609.344);
 	}
 	public static float mToMile(float num) {
 		return (float) (num / 1609.344);
 	}
 	
 	// Yard
 	public static float yardToM(float num) {
 		return (float) (num * 0.9144);
 	}
 	public static float mToYard(float num) {
 		return (float) (num / 0.9144);
 	}
 	
 	// Foot
 	public static float footToM(float num) {
 		return (float) (num * 0.3048);
 	}
 	public static float mToFoot(float num) {
 		return (float) (num / 0.3048);
 	}
 	
 	// Inch
 	public static float inchToM(float num) {
 		return (float) (num * 0.0254);
 	}
 	public static float mToInch(float num) {
 		return (float) (num / 0.0254);
 	}
 	
 	// Sea League
 	public static float leagueToM(float num) {
 		return (float) (num / .000179985601);
 	}
 	public static float mToLeague(float num) {
 		return (float) (num * .000179985601);
 	}
 	
 	// Fathom
 	public static float fathomToM(float num) {
 		return (float) (num / .546806649);
 	}
 	public static float mToFathom(float num) {
 		return (float) (num * .546806649);
 	}
 	
 	/***************************************
 	 * Mass / Weight * (Gram as main unit) *
 	 ***************************************/
 	
 	// Kilogram
 	public static float kiloToG(float num) {
 		return (float) (num * 1000.0);
 	}
 	public static float gToKilo(float num) {
 		return (float) (num / 1000.0);
 	}
 	
 	// Ounce
 	public static float ounceToG(float num) {
 		return (float) (num / 0.0352739619);
 	}
 	public static float gToOunce(float num) {
 		return (float) (num * 0.0352739619);
 	}
 	
 	// Pound
 	public static float poundToG(float num) {
 		return (float) (num /  0.00220462262);
 	}
 	public static float gToPound(float num) {
 		return (float) (num *  0.00220462262);
 	}
 	
 	// Ton
 	public static float tonToG(float num) {
 		return (float) (num / .0000011023);
 	} public static float gToTon(float num) {
 		return (float) (num * .0000011023);
 	}
 	
 	// Metric Ton
 	public static float mTonToG(float num) {
 		return (float) (num * 1000000.0);
 	}
 	public static float gToMTon(float num) {
 		return (float) (num / 1000000.0);
 	}
 	
 	/***************************************
 	 * Volume * (Liter as main unit) *
 	 ***************************************/
 	
 	// Milliliter
 	public static float milliToL(float num) {
 		return (float) (num / 1000.0);
 	}
 	public static float lToMilli(float num) {
 		return (float) (num * 1000.0);
 	}
 	
 	// Fluid Ounces
 	public static float fluidOuncesToL(float num) {
 		return (float) (num / 33.81402266);
 	}
 	public static float lToFluidOunces(float num) {
 		return (float) (num * 33.81402266);
 	}
 	
 	// Pint
 	public static float pintToL(float num) {
 		return (float) (num / 2.11337642);
 	}
 	public static float lToPint(float num) {
 		return (float) (num * 2.11337642);
 	}
 	
 	// Quart
 	public static float quartToL(float num) {
 		return (float) (num / 1.05668821);
 	}
 	public static float lToQuart(float num) {
 		return (float) (num * 1.05668821);
 	}
 	
 	// Gallon
 	public static float gallonToL(float num) {
 		return (float) (num / 0.26417205);
 	}
 	public static float lToGallon(float num) {
 		return (float) (num * 0.26417205);
 	}
 	
 	// Teaspoon
 	public static float teaToL(float num) {
 		return (float) (num / 202.884136);
 	}
 	public static float lToTea(float num) {
 		return (float) (num * 202.884136);
 	}
 	
 	// Tablespoon
 	public static float tableToL(float num) {
 		return (float) (num / 67.6280454);
 	}
 	public static float lToTable(float num) {
 		return (float) (num * 67.6280454);
 	}
 	
 	// Cup
 	public static float cupToL(float num) {
 		return (float) (num / 4.22675284);
 	}
 	public static float lToCup(float num) {
 		return (float) (num * 4.22675284);
 	}
 	
 	
 	/***************
 	 * Temperature *
 	 ***************/
 	
 	// Fahrenheit
 	public static float fToC(float num) {
 		return (float) ((num - 32.0) * 5.0 / 9.0);
 	}
 	public static float fToK(float num) {
 		return (float) ((num + 459.67) * 5.0 / 9.0);
 	}
 	
 	// Celcius
 	public static float cToF(float num) {
 		return (float) ((num * 9.0 / 5.0) + 32.0);
 	}
 	public static float cToK(float num) {
		return (float) + 273.15;
 	}
 	
 	// Kelvin
 	public static float kToF(float num) {
 		return (float) ((num * 9.0 / 5.0) - 459.67);
 	}
 	public static float kToC(float num) {
 		return (float) (num - 273.15);
 	}
 }
