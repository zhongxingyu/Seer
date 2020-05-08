 /**
  * Copyright (c) 2013, Werner Keil and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.examples.units.types;
 
 import static org.eclipse.uomo.units.SI.METRE;
 import static org.eclipse.uomo.units.SI.Prefix.MILLI;
 import static org.eclipse.uomo.units.impl.system.USCustomary.FOOT;
 
 import org.eclipse.uomo.units.AbstractSystemOfUnits;
 import org.unitsofmeasurement.quantity.Length;
 import org.unitsofmeasurement.unit.SystemOfUnits;
 import org.unitsofmeasurement.unit.Unit;
 
import com.ibm.icu.math.BigDecimal;

 /**
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
 * @version 0.0.1
  * @see <a
  *      href="http://en.wikipedia.org/wiki/German_obsolete_units_of_measurement">Wikipedia: German Obsolete Units of Measurement</a>
  * @deprecated
  */
 public class GermanObsolete extends AbstractSystemOfUnits {
 
 	/**
	 * Distance between elbow and fingertip. In the North, often 2 feet,
 	 * In Prussia 17 / 8 feet, in the South variable, often 2½ feet.
 	 * The smallest known German Elle is 402.8 mm, the longest 811 mm.
 	 * */
 	public static final  Unit<Length> ELL_NORTH = FOOT.multiply(2);
 	
 	/**
 	 * Distance between elbow and fingertip. In the North, often 2 feet,
	 * In Prussia 17 / 8 feet, in the South variable, often 2½ feet.
 	 * The smallest known German Elle is 402.8 mm, the longest 811 mm.
 	 * */
 	public static final  Unit<Length> ELL_SOUTH = FOOT.multiply(2.5d);
 	
 	/** The Fuß or German foot varied widely from place to place in the German-speaking world, and also with time. 
 	 * In some places, more than one type of Fuß was in use.
 	 * @see <a href="http://en.wikipedia.org/wiki/German_obsolete_units_of_measurement#Fu.C3.9F_.28foot.29">Wikipedia: German Obsolete - Fuß</a>
 	 * */
 	public static final  Unit<Length> FOOT_ZURICH = MILLI(METRE).multiply(301);
 	
 	/** The Fuß or German foot varied widely from place to place in the German-speaking world, and also with time. 
 	 * In some places, more than one type of Fuß was in use.
 	 * @see <a href="http://en.wikipedia.org/wiki/German_obsolete_units_of_measurement#Fu.C3.9F_.28foot.29">Wikipedia: German Obsolete - Fuß</a>
 	 * */
 	public static final  Unit<Length> FOOT_ZUG = FOOT_ZURICH;
 	
 	/**
 	 * Fuß, Lausanne, Canton of Waadt<p> 
 	 * The Fuß or German foot varied widely from place to place in the German-speaking world, and also with time. 
 	 * In some places, more than one type of Fuß was in use.
 	 * @see <a href="http://en.wikipedia.org/wiki/German_obsolete_units_of_measurement#Fu.C3.9F_.28foot.29">Wikipedia: German Obsolete - Fuß</a>
 	 * */
 	public static final  Unit<Length> FOOT_LAUSANNE = MILLI(METRE).multiply(293);
 	
 	/**
 	 * Steinfuß, Zug, Canton of<p> 
 	 * The Fuß or German foot varied widely from place to place in the German-speaking world, and also with time. 
 	 * In some places, more than one type of Fuß was in use.
 	 * @see <a href="http://en.wikipedia.org/wiki/German_obsolete_units_of_measurement#Fu.C3.9F_.28foot.29">Wikipedia: German Obsolete - Fuß</a>
 	 * */
 	public static final  Unit<Length> STONE_FOOT = MILLI(METRE).multiply(268);
 	
 	@Override
 	public String getName() {
 		return GermanObsolete.class.getSimpleName();
 	}
 
     /**
      * Default constructor (prevents this class from being instantiated).
      */
     private GermanObsolete() {
     }
 	
 	/**
 	 * The singleton instance of {@code Seismic}.
 	 */
 	private static final GermanObsolete INSTANCE = new GermanObsolete();
 
 	/**
 	 * Returns the singleton instance of this class.
 	 * 
 	 * @return the Seismic system instance.
 	 */
 	public static final SystemOfUnits getInstance() {
 		return INSTANCE;
 	}	
 }
