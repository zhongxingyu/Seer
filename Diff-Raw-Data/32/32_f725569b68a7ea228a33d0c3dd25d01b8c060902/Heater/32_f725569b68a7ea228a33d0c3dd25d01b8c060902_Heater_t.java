 package edu.ucsb.cs56.S13.lab04.nguyenm;
 
 public class Heater {
 
 	private String brandName;
 	private int temperature;
 
 	/** Creates and initializes a Heater object to "Dyson" at 
 	temperature 30 */
 	Heater() {
		brandName = "Dyson";
		temperature = 30;
 	}
 	
 	Heater(String brandName, int temperature) {
		this.brandName = brandName;
		this.temperature = temperature;
 	}
 
 	void setBrandName(String brandName) {
		this.brandName = brandName;
 	}
 	
 	String getBrandName() {
		return brandName;
 	}
 
 	void setTemperature(int temperature) {
		this.temperature = temperature;
 	}
 
 	int getTemperature() {
		return temperature;
 	}
 
 	@Override
 	public String toString() {
		return String.format("[brandName=%s, temperature=%s]",
				brandName, temperature);
 	}
 
 	public boolean equals(Object object) {
		if (object instanceof Heater) {
			Heater heater = (Heater) object;
			return this.brandName.equals(heater.brandName) && this.temperature == heater.temperature;
		}

		else return super.equals(object);
 	}
 }
