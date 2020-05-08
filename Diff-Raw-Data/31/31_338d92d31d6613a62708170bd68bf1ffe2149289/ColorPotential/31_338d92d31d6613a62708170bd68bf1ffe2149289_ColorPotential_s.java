 package org.blh.core.units.color;
 
 import org.blh.core.units.DoubleUnit;
 import org.blh.core.units.weight.Lbs;
 
 /**
  *
  * @author thinner
  */
 public class ColorPotential extends DoubleUnit {
 
	private final Lovibond color;
	private final Lbs amount;
 
     public ColorPotential() {
         super(0d);
		this.color = new Lovibond(0);
		this.amount = new Lbs(0);
     }
 
     public ColorPotential(Lovibond color, Lbs amount) {
        super(color.value() * amount.value());
		this.color = color;
		this.amount = amount;
     }
 
    public ColorPotential add(Lovibond color, Lbs amount) {
		Lovibond newColor = new Lovibond(this.color.value() + color.value());
		Lbs newAmount = new Lbs(this.amount.value() + amount.value());
        return new ColorPotential(newColor, newAmount);
     }
 }
