 package blh.core.formulas.color;
 
 import blh.core.formulas.Formula;
 import blh.core.uncategorized.FullContext;
 import blh.core.units.color.EBC;
 import blh.core.units.color.MaltColorUnit;
 
 /**
  * Daniels’ model differs from Mosher’s and suggests that homebrew is generally
  * darker than commercial beers. Like Mosher’s model, the formula has a minimum
  * color that is not reasonable. Consequently the formula should only be used
  * for beers with MCU greater than 11.
  *
  * @author thinner
  */
 public class Daniels implements Formula<EBC> {
 
     @Override
     public EBC calc(FullContext context) {
         MaltColorUnit mcu = context.maltColorUnit.value();
         
        return calc(mcu);
     }
 
    public EBC calc(MaltColorUnit mcu) {
        return new EBC(((0.2 * mcu.value()) + 8.4) * 1.97);
     }
 }
