 /**
  * 
  */
 package uk.co.craigwarren.gascalc;
 
 /**
  * Class which can perform the real gas calculations for Bar and Litres using the formulae
  * 
  * (P+(((n^2)a)/(v^2)))(V-nb) = nRT
  * 
  * where:
  * P is the pressure the g is the gas is stored under in Atmospheres (Bar)
  * V is the volume the gas occupies in Litres
  * n is the number of mols of gas
  * R is the idea gas constant used for Bars and Litres (0.0821)
  * T is the temperature the ambient pressure of the gas in degrees kelvin
  * a is the Van der Waals constant for the molecular elasticity of the gas in question
  * b is the Van der Waals constant for the molecular volume of the gas in question
  *
  * @author craig
  */
 public class RealGasLaw extends GasLaw {
 	
 	private final Gas gas;
 	private final VanDerWaalsConstantsCalculator calc;
 	private Double a;
 	private Double b;
 	
 	public RealGasLaw(Gas gas, VanDerWaalsConstantsCalculator calc){
 		this.gas = gas;
 		this.calc = calc;
 	}
 	
 	private double getA(){
 		if(null==a){
 			a = calc.getVdwElasticity(gas);
 		}
 		return a;
 	}
 	
 	private double getB(){
 		if(null==b){
 			b = calc.getVdwVolume(gas);
 		}
 		return b;
 	}
 
 	@Override
 	public float getPressureBar(float volumeLitres, float molsOfGas, float temperatureKelvin){
		return 0f;
 	}
 	
 	@Override
 	public float getVolumeLitres(float pressureBar, float molsOfGas, float temperatureKelvin){
 		return 0f;
 	}
 	
 	@Override
 	public float getMolsOfGas(float pressureBar, float volumeLitres, float temperatureKelvin){
 		return 0f;
 	}
 	
 	@Override
 	public float getTemperatureKelvin(float pressureBar, float volumeLitres, float molsOfGas){
 		return 0f;
 	}
 }
