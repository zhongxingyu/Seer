 package cwc.temp.services;
 
 /*
  * Conversion implements the following relationships
  * 
  * [°C] = ([°F] - 32) × 5/9
  * [°F] = [°C] × 9/5 + 32
  */
 public class ConversionImpl implements Conversion {
 
     @Override
     public double fahrenheitToCelcius(double fahrenheitTemp) {
         return (fahrenheitTemp - 32.0) * (5.0/9.0);
     }
 
     @Override
     public double celciusToFahrenheit(double celciusTemp) {
         return (celciusTemp * (9.0/5.0)) + 32.0;
     }
 
     @Override
     public double celciusToKelvin(double celciusTemp) {
        throw new UnsupportedOperationException("Not supported yet.");
     }
     
 }
