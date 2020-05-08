 /**
  * Copyright (c) 2005, 2011, Werner Keil, JScience and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.units;
 
 import org.unitsofmeasurement.quantity.Quantity;
 import org.unitsofmeasurement.unit.Unit;
 
 /**
  * <p> This interface represents the IMeasure, countable, or comparable 
  *     property or aspect of a thing.</p>
  *     
  * <p> Implementing instances are typically the result of a measurement:[code]
  *         IMeasure<Mass> weight = BaseAmount.valueOf(180.0, POUND);
  *     [/code]
  *     They can also be created from custom classes:[code]
  *     class Delay implements IMeasure<Duration> {
  *          private long nanoSeconds; // Implicit internal unit.
  *          public double doubleValue(Unit<Velocity> unit) { ... }
  *          public long longValue(Unit<Velocity> unit) { ... }
  *     }
  *     Thread.wait(new Delay(24, HOUR)); // Assuming Thread.wait(IMeasure<Duration>) method.
  *     [/code]</p>
  *     
  * <p> Although IMeasure instances are for the most part scalar quantities; 
  *     more complex implementations (e.g. vectors, data set) are allowed as 
  *     long as an agregate magnitude can be determined. For example:[code]
  *     class Velocity3D implements IMeasure<Velocity> {
  *          private double x, y, z; // Meter per seconds.
  *          public double doubleValue(Unit<Velocity> unit) { ... } // Returns vector norm.
  *          ... 
  *     }
  *     class Sensors<Q extends Quantity> extends QuantityAmount<double[], Q> {
  *          public doubleValue(Unit<Q> unit) { ... } // Returns median value. 
  *          ...
  *     } [/code]</p>
  * 
  * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @version 5.3, $Date: 2011-04-05 03:03:44 +0430 $
  * @param <R>
  */
 public interface IMeasure<Q extends Quantity<Q>> extends Quantity<Q> {
 	
     /**
      * Returns the numeric value of this measurement.
      * 
      * @return the numeric value.
      */
     Number getValue();
     
     /**
      * Returns the estimated value of this measurement stated 
      * in the specified unit as a <code>double</code>.
      * 
      * @param unit the unit in which the measurement value is stated.
      * @return the numeric value after conversion to type <code>double</code>.
      */
     double doubleValue(Unit<Q> unit);
 
     /**
      * Returns the estimated value of this measurement stated in the specified 
      * unit as a <code>long</code>.
      * 
      * @param unit the unit in which the measurement value is stated.
      * @return the numeric value after conversion to type <code>long</code>.
      * @throws ArithmeticException if this quantity cannot be represented 
      *         as a <code>long</code> number in the specified unit.
      */
     long longValue(Unit<Q> unit) throws ArithmeticException;
     
     /**
     * Get the unit (convenience to avoid cast).
      * @draft UOMo 0.6
      * @provisional This API might change or be removed in a future release.
      */
     Unit<Q> getQuantityUnit();
     
     /**
      * Returns the sum of this amount with the one specified.
      *
      * @param  that the amount to be added.
      * @return <code>this + that</code>.
      */
     IMeasure<Q> add(IMeasure<Q> that);
     
     /**
      * Returns the difference between this amount and the one specified.
      *
      * @param  that the number to be subtracted.
      * @return <code>this - that</code>.
      */
     IMeasure<Q> substract(IMeasure<Q> that);
     
     /**
      * Returns the product of this amount with the one specified.
      *
      * @param  that the number multiplier.
      * @return <code>this · that</code>.
      */
     IMeasure<?> multiply(IMeasure<?> that);
     
     /**
      * Returns this amount divided by the one specified.
      *
      * @param  that the amount divisor.
      * @return <code>this / that</code>.
      */
     IMeasure<?> divide(IMeasure<?> that);
     
     /**
      * Returns this measurement converted into another unit.
      * 
      * @param unit
      * @return the converted result.
      */
     IMeasure<Q> to(Unit<Q> unit);
 }
