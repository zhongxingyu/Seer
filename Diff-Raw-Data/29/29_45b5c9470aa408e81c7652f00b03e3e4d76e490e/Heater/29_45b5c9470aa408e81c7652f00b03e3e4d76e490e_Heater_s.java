 package edu.ucsb.cs56.S13.lab04.nguyenm;
 
 /** A class to represent a Heater and it's properties */
 public class Heater {
 	/** represents the name of the brand of this heater */
 	private String brandName;
 	
 	/** represents the temperature this heater is set to heat 
 	  * the room by before stopping (measured in celcius) */
 	private int temperature;
 
 	/** Creates and initializes a Heater object to "Dyson" at 
 	temperature 30 */
	Heater() {
 		brandName = "Dyson";
 		temperature = 30;
 	}
 	
 	/** Creates and initializes a Heater object to the brandName 
 	  * and temperature given
 	  * @param brandName the brand name of the heater
 	  * @param temperature the temperature the heater is set to 
 	  * 		heat the room by before turning off (measured 
 	  * 		in degrees Celcius).
 	  */
	Heater(String brandName, int temperature) {
 		this.brandName = brandName;
 		this.temperature = temperature;
 	}
 
 	/** Sets the brand name of this heater 
 	  * @param brandName the new brand name for this heater 
 	  */
	void setBrandName(String brandName) {
 		this.brandName = brandName;
 	}
 	
 	/** Retrieves the brand name of this heater 
	  * @returns the brand name of this heater
 	  */
	String getBrandName() {
 		return brandName;
 	}
 
 	/** Sets the temperature of this heater to heat up to before 
 	  * turning off its heating mechanism
 	  * @param temperature the temperature to heat the room up to 
 	  * 		before turning off as measured in degrees Celcius
 	  */
	void setTemperature(int temperature) {
 		this.temperature = temperature;
 	}
 
 	/** Retrieves the temperature of this heater to heat up to before 
 	  * turning off its heating function
	  * @returns the temperature to heat the roo up to before turning 
 			off as measured in degrees Celcius
 	  */
	int getTemperature() {
 		return temperature;
 	}
 
 	@Override
 	/** Converts this object into a human readable value as described here: </br>
 	  * <code>[brandName=..., temperature=...]</code></br>
	  * @returns a human readable string representing the object's value
 	  */
 	public String toString() {
 		return String.format("[brandName=%s, temperature=%s]",
 				brandName, temperature);
 	}
 
 	/** Compares to objects to see if they have the same brand name and 
 	  * the same temperature
	  * @returns true if they have the same value, false otherwise
 	  */
 	public boolean equals(Object object) {
 		if (object instanceof Heater) {
 			Heater heater = (Heater) object;
 			return this.brandName.equals(heater.brandName) && this.temperature == heater.temperature;
 		}
 
 		else return super.equals(object);
 	}
 
 	public static void main(String[] args) {
 		System.out.println(new Heater());
 	}
 }
