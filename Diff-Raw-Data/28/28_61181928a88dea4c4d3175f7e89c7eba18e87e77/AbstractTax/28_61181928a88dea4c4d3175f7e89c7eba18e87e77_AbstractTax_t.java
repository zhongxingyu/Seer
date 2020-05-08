 package org.uva.training.tax;
 
import java.math.BigDecimal;
import java.math.RoundingMode;

 /**
  * This abstract class contains only common method(s) to compute a tax value.<br/>
  * 
  * <pre>
  * --------------------------------
  * | Taxe calcule | Taxe impute |
  * --------------------------------
  * |          0.99 |         1.00 |
  * --------------------------------
  * |          1.00 |         1.00 |
  * --------------------------------
  * |          1.01 |         1.05 |
  * --------------------------------
  * |          1.02 |         1.05 |
  * --------------------------------
  * </pre>
  * 
  * <br/>
  * The around method must follows this rules.
  * 
  * @author uvachon
  */
 public abstract class AbstractTax implements Tax {
 
    /**
    * Takes a tax and around it following rules.
     * 
     * @param value the tax value to compute.
     * @return the rounded value.
     */
    protected float round(float value) {
      BigDecimal result = new BigDecimal(Math.ceil(value * 20) / 20);
      result.setScale(2, RoundingMode.HALF_UP);
      return result.floatValue();
    }
 }
