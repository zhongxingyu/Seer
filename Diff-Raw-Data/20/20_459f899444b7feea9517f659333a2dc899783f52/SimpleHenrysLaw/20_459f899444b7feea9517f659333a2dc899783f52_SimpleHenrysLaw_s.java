 package blh.core.formulas.carbonation;
 
 import blh.core.formulas.Formula;
 import blh.core.uncategorized.FullContext;
 import blh.core.units.CO2Volumes;
 import blh.core.units.pressure.Bar;
 import blh.core.units.pressure.PSI;
 import blh.core.units.temperature.Celsius;
 import blh.core.units.temperature.Fahrenheit;
 
 /**
  * Created by Erik Larkö at 5/28/13 7:03 AM
  */
 public class SimpleHenrysLaw implements Formula<PSI> {
 
     @Override
     public PSI calc(FullContext context) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public PSI calc(CO2Volumes volumes, Fahrenheit temperature) {
        double d = volumes.value() + temperature.value() + 12.4;
         d = d/4.85;
         d -= 14.7;
 
         return new PSI(d);
     }
 
     public Bar calc(CO2Volumes volumes, Celsius temperature) {
        double d = volumes.value() +  1.8 * temperature.value() +34.4;
        d = d/4.85;
        d -= 1;
 
        return new Bar(d);
     }
 }
