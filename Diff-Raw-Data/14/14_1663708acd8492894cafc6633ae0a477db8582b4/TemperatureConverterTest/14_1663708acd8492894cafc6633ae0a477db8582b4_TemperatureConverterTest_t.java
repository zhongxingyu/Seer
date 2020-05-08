 package jp.gmom.sample.temperature.tests;
 
 import android.test.suitebuilder.annotation.SmallTest;
 import jp.gmom.sample.temperature.TemperatureConverter;
 import junit.framework.TestCase;
 
 public class TemperatureConverterTest extends TestCase {
 
     public static final double DELTA = 0.005;
 
     @SmallTest
     public void testFahrenheitToCelsius() {
         double converted =
                 TemperatureConverter.fahrenheitToCelsius(TemperatureConverter.FAHRENHEIT_FREEZING_POINT);
         assertEquals(TemperatureConverter.CELSIUS_FREEZING_POINT, converted, DELTA);
 
         converted = TemperatureConverter.fahrenheitToCelsius(-148.0);
         assertEquals(-100.0, converted, DELTA);
 
         converted = TemperatureConverter.fahrenheitToCelsius(TemperatureConverter.FAHRENHEIT_BOILING_POINT);
         assertEquals(TemperatureConverter.CELSIUS_BOILING_POINT, converted, DELTA);
     }
 
     @SmallTest
     public void testCelsiusToFahrenheit() {
         double converted = TemperatureConverter.celsiusToFahrenheit(TemperatureConverter.CELSIUS_FREEZING_POINT);
         assertEquals(TemperatureConverter.FAHRENHEIT_FREEZING_POINT, converted, DELTA);
 
         converted = TemperatureConverter.celsiusToFahrenheit(-100.0);
         assertEquals(-148.0, converted, DELTA);
 
         converted = TemperatureConverter.celsiusToFahrenheit(TemperatureConverter.CELSIUS_BOILING_POINT);
         assertEquals(TemperatureConverter.FAHRENHEIT_BOILING_POINT, converted, DELTA);
     }
 
 }
