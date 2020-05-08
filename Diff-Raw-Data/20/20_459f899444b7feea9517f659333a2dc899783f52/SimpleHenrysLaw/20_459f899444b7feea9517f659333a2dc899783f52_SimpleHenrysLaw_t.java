 package blh.core.formulas.carbonation;
 
 import blh.core.formulas.Formula;
 import blh.core.uncategorized.FullContext;
 import blh.core.units.CO2Volumes;
 import blh.core.units.pressure.Bar;
 import blh.core.units.pressure.PSI;
 import blh.core.units.temperature.Celsius;
 import blh.core.units.temperature.Fahrenheit;
 
 /**
 * From: https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&ved=0CCkQFjAA&url=http%3A%2F%2Fwww.iul-instruments.de%2Fpdf%2Fvitalsensors_2.pdf&ei=EqvIUdHiHcr24QSFkoCoBg&usg=AFQjCNHuDfxIMs31EoA_WvOCVycK0BD1Tg&bvm=bv.48293060,d.bGE&cad=rja
 *
 * psi = ((vols * (F + 12.4) / 4.85) - 14.7
 * F = (9/5) * C + 32
 * psi = ((vols * (9/5 * c + 32) + 12.4) / 4.85) - 14.7
 * bar = 14.503795 * psi = 14.503795 * (((vols * (9/5 * c + 32) + 12.4) / 4.85) - 14.7)
 *
  * Created by Erik Larkö at 5/28/13 7:03 AM
  */
 public class SimpleHenrysLaw implements Formula<PSI> {
 
     @Override
     public PSI calc(FullContext context) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public PSI calc(CO2Volumes volumes, Fahrenheit temperature) {
        double d = volumes.value() * (temperature.value() + 12.4);
         d = d/4.85;
         d -= 14.7;
 
         return new PSI(d);
     }
 
     public Bar calc(CO2Volumes volumes, Celsius temperature) {
        double d = (36 * temperature.value() + 888) / 97;
        d = volumes.value() * d;
        d = d - 14.7;
 
        return new PSI(d).asBar();
     }
 }
