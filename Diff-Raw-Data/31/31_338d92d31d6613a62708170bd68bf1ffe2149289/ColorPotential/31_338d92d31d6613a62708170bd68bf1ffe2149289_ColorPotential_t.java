 package org.blh.core.units.color;
 
 import org.blh.core.units.DoubleUnit;
 import org.blh.core.units.weight.Lbs;
 
 /**
  *
  * @author thinner
  */
 public class ColorPotential extends DoubleUnit {
 
	private static double getPotential(Lovibond color, Lbs amount) {
		return color.value() * amount.value();
	}
 
     public ColorPotential() {
         super(0d);
     }
 
     public ColorPotential(Lovibond color, Lbs amount) {
        super(getPotential(color, amount));
     }
 
	private ColorPotential(double value) {
		super(value);
	}

    /**
	 * Because units are immutable we must return a new object here.
	 *
	 * @param color
	 * @param amount
	 * @return
	 */
	public ColorPotential add(Lovibond color, Lbs amount) {
		return new ColorPotential(this.value() + getPotential(color, amount));
     }
 }
