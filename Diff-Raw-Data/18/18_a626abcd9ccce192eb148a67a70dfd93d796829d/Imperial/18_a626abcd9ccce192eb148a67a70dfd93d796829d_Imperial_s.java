 /**
  * Copyright (c) 2005, 2011, Werner Keil, Ikayzo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Ikayzo and others - initial API and implementation
  */
 package org.eclipse.uomo.units.impl.system;
 
 
 import static org.eclipse.uomo.units.SI.*;
 import static org.eclipse.uomo.units.SI.Prefix.MICRO;
 import static org.eclipse.uomo.units.impl.system.NonSI.AVOIRDUPOIS_POUND_DIVIDEND;
 import static org.eclipse.uomo.units.impl.system.NonSI.AVOIRDUPOIS_POUND_DIVISOR;
 
 import org.eclipse.uomo.units.AbstractSystemOfUnits;
 import org.unitsofmeasurement.quantity.Area;
 import org.unitsofmeasurement.quantity.Length;
 import org.unitsofmeasurement.quantity.Mass;
 import org.unitsofmeasurement.quantity.Temperature;
 import org.unitsofmeasurement.quantity.Time;
 import org.unitsofmeasurement.quantity.Volume;
 import org.unitsofmeasurement.unit.SystemOfUnits;
 import org.unitsofmeasurement.unit.Unit;
 
 /**
  * <p>
  * This class contains units from the Imperial system.
  * </p>
  * <p>
  * 
  * @noextend This class is not intended to be extended by clients.
  * 
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @version 1.1 ($Revision: 332 $), $Date: 2011-09-11 14:52:11 +0200 $
  * @see <a
  *      href="http://en.wikipedia.org/wiki/http://en.wikipedia.org/wiki/Imperial_unit">Wikipedia:
  *      Imperial Units</a>
  */
 public final class Imperial extends AbstractSystemOfUnits  {
 
 	/**
 	 * Default constructor (prevents this class from being instantiated).
 	 */
 	private Imperial() {
 	}
 
 	/**
 	 * Returns the unique instance of this class.
 	 * 
 	 * @return the Imperial instance.
 	 */
 	public static SystemOfUnits getInstance() {
 		return INSTANCE;
 	}
 
 	private static final Imperial INSTANCE = new Imperial();
 
 	// //////////
 	// Length //
 	// //////////
 
 	/**
 	 * A unit of length equal to <code>0.0254 m</code> (standard name
 	 * <code>in</code>).
 	 */
 	public static final Unit<Length> INCH = USCustomary.INCH;
 	
 	// ////////
 	// Mass //
 	// ////////
 
 	/**
 	 * A unit of mass equal to <code>453.59237 grams</code> (avoirdupois pound,
 	 * standard name <code>lb</code>).
 	 */
 	static final Unit<Mass> POUND = addUnit(KILOGRAM.multiply(
 			AVOIRDUPOIS_POUND_DIVIDEND).divide(AVOIRDUPOIS_POUND_DIVISOR));
 
 	/**
 	 * A unit of mass equal to <code>1 / 16 {@link #POUND}</code> (standard name
 	 * <code>oz</code>).
 	 */
 	public static final Unit<Mass> OUNCE = addUnit(POUND.divide(16));
 	
 	/**
 	 * A unit of mass equal to <code>2240 {@link #POUND}</code> (long ton,
 	 * standard name <code>ton_uk</code>).
 	 */
 	public static final Unit<Mass> TON_UK = addUnit(POUND.multiply(2240));
 
 	/**
 	 * A unit of mass equal to <code>1000 kg</code> (metric ton, standard name
 	 * <code>t</code>).
 	 */
 	public static final Unit<Mass> METRIC_TON = addUnit(KILOGRAM.multiply(1000));
 
 	// ///////////////
 	// Temperature //
 	// ///////////////
 
 	/**
 	 * A unit of temperature equal to <code>5/9 °K</code> (standard name
 	 * <code>°R</code>).
 	 */
 	static final Unit<Temperature> RANKINE = addUnit(KELVIN.multiply(5)
 			.divide(9));
 
 	/**
 	 * A unit of temperature equal to degree Rankine minus
 	 * <code>459.67 °R</code> (standard name <code>°F</code>).
 	 * 
 	 * @see #RANKINE
 	 */
 	static final Unit<Temperature> FAHRENHEIT = addUnit(RANKINE
 			.add(459.67));
 
 	// /////////
 	// Angle //
 	// /////////
 
 	
 	// ////////////
 	// TimeUnit //
 	// ////////////
 	/**
 	 * A unit of time equal to <code>60 s</code> (standard name <code>min</code>
 	 * ).
 	 */
 	static final Unit<Time> MINUTE = addUnit(SECOND.multiply(60));
 
 	/**
 	 * A unit of duration equal to <code>60 {@link #MINUTE}</code> (standard
 	 * name <code>h</code>).
 	 */
 	static final Unit<Time> HOUR = addUnit(MINUTE.multiply(60));
 
 	// ////////////
 	// Velocity //
 	// ////////////
 
 
 	// ////////
 	// Area //
 	// ////////
 
 	/**
 	 * A unit of area (standard name <code>sft</code>
 	 * ).
 	 */
 	public static final Unit<Area> SQUARE_FOOT = USCustomary.SQUARE_FOOT;
 	
 	/**
 	 * One acre is 43,560 <code>square feet</code> (standard name <code>a</code>
 	 * ).
 	 */
 	public static final Unit<Area> ACRE = addUnit(SQUARE_FOOT.multiply(43560));
 
 	// ///////////////
 	// Data Amount //
 	// ///////////////
 	
 
 	// //////////
 	// Energy //
 	// //////////
 
 
 	// //////////
 	// Volume //
 	// //////////
 	/**
 	 * A unit of volume equal to one cubic decimeter (default label
 	 * <code>L</code>, also recognized <code>ÂµL, mL, cL, dL</code>).
 	 */
 	static final Unit<Volume> LITRE = addUnit(CUBIC_METRE.divide(1000));
 
 	/**
 	 * A unit of volume equal to one cubic inch (<code>in³</code>).
 	 */
 	static final Unit<Volume> CUBIC_INCH = addUnit(INCH.pow(3).asType(
 			Volume.class));
 
 	/**
 	 * A unit of volume equal to <code>4.546 09 {@link #LITRE}</code> (standard
 	 * name <code>gal_uk</code>).
 	 */
 	public static final Unit<Volume> GALLON_UK = addUnit(LITRE.multiply(454609)
 			.divide(100000));
 
 	/**
 	 * A unit of volume equal to one UK gallon, Liquid Unit.
 	 */
 	public static final Unit<Volume> GALLON_LIQUID = addUnit(CUBIC_INCH
 			.multiply(277.42));
 	
 	/**
 	 * A unit of volume equal to <code>1 / 160 {@link #GALLON_UK}</code>
 	 * (standard name <code>oz_fl_uk</code>).
 	 */
 	static final Unit<Volume> OUNCE_LIQUID_UK = addUnit(GALLON_UK
 			.divide(160));
 	
 	/**
 	 * A unit of volume equal to <code>1 / 160 {@link #GALLON_LIQUID}</code>
 	 * (standard name <code>oz_fl</code>).
 	 */
 	public static final Unit<Volume> OUNCE_LIQUID = OUNCE_LIQUID_UK;
 
 	/**
 	 * A unit of volume equal to <code>5 {@link #OUNCE_LIQUID}</code> (standard name
 	 * <code>gi</code>).
 	 */
 	public static final Unit<Volume> GILL = addUnit(OUNCE_LIQUID.multiply(5));
 
 	/**
 	 * A unit of volume equal to <code>20 {@link #OUNCE_LIQUID}</code> (standard name
 	 * <code>pt</code>).
 	 */
 	public static final Unit<Volume> PINT = addUnit(OUNCE_LIQUID.multiply(20));
 
 	/**
 	 * A unit of volume equal to <code>40 {@link #OUNCE_LIQUID}</code> (standard name
 	 * <code>qt</code>).
 	 */
 	public static final Unit<Volume> QUART = addUnit(OUNCE_LIQUID.multiply(40));
 	
 	/**
 	 * A unit of volume <code>~ 1 drop or 0.95 grain of water </code> (standard
 	 * name <code>min</code>).
 	 */
 	public static final Unit<Volume> MINIM = addUnit(MICRO(LITRE).multiply(
 			59.1938802d));
 
 	/**
 	 * A unit of volume equal to <code>20 {@link #MINIM}</code> (standard name
 	 * <code>fl scr</code>).
 	 */
 	public static final Unit<Volume> FLUID_SCRUPLE = addUnit(MINIM.multiply(60));
 
 	/**
 	 * A unit of volume equal to <code>3 {@link #FLUID_SCRUPLE}</code> (standard name
 	 * <code>fl drc</code>).
 	 */
 	public static final Unit<Volume> FLUID_DRACHM = addUnit(FLUID_SCRUPLE.multiply(3));
 
 
	@Override
 	public String getName() {
 		return getClass().getSimpleName();
 	}
 }
