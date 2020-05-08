 package org.eclipse.uomo.examples.units.console;
 
 import org.eclipse.uomo.units.SI;
 import org.eclipse.uomo.units.USCustomary;
 import org.eclipse.uomo.units.impl.quantity.AreaAmount;
 import org.eclipse.uomo.units.impl.quantity.LengthAmount;
import org.eclipse.uomo.units.impl.quantity.MassAmount;
 import org.unitsofmeasurement.unit.Unit;
 import org.unitsofmeasurement.unit.UnitConverter;
 import org.unitsofmeasurement.quantity.Area;
 import org.unitsofmeasurement.quantity.Length;
 
 public class HelloUnits {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		LengthAmount length = new LengthAmount(10, SI.METRE);
 //		LengthAmount length = new LengthAmount(10, SI.KILOGRAM); // this won't work ;-)
 		
 		System.out.println(length);
 		Unit<Length> lenUnit =  length.getQuantityUnit();
 		System.out.println(lenUnit);
 		
 		System.out.println(length.doubleValue(USCustomary.FOOT)); 
 //		System.out.println(length.doubleValue(USCustomary.POUND)); // this won't work either.
 		UnitConverter footConverter = lenUnit.getConverterTo(USCustomary.INCH);
 		System.out.print(footConverter.convert(length.getNumber().doubleValue()));
 		System.out.println(" " + USCustomary.FOOT);
 		
 		@SuppressWarnings("unchecked")
 		AreaAmount area = new AreaAmount(length.getNumber().doubleValue() * length.getNumber().doubleValue(), 
 				(Unit<Area>) length.getQuantityUnit().multiply(SI.METRE));
 		System.out.println(area);
		
		MassAmount mass = new MassAmount(1000, SI.GRAM);
		MassAmount mass2 = new MassAmount(1, SI.KILOGRAM);
		System.out.println(mass.equals(mass2));
 	}
 }
