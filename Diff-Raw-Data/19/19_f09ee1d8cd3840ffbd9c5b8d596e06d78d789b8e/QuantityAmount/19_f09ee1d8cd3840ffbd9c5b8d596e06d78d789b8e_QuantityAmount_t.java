 /**
 * Copyright (c) 2005, 2013, Werner Keil and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.units;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 
import org.unitsofmeasurement.quantity.Dimensionless;
 import org.unitsofmeasurement.quantity.Quantity;
 import org.unitsofmeasurement.unit.IncommensurableException;
 import org.unitsofmeasurement.unit.UnconvertibleException;
 import org.unitsofmeasurement.unit.Unit;
 import org.unitsofmeasurement.unit.UnitConverter;
 
 import com.ibm.icu.util.Measure;
 import com.ibm.icu.util.MeasureUnit;
 
 /**
  * An amount of quantity, consisting of a Number and a Unit. QuantityAmount
  * objects are immutable.
  * 
  * @see java.lang.Number
  * @see MeasureUnit
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @param <Q>
  *            The type of the quantity.
 * @version 1.3.4 ($Revision: 212 $), $Date: 2011-09-12 01:20:44 +0200 (Mo, 12
  *          Sep 2011) $ XXX rename to Amount, AbstractAmount or MeasureAmount?
  *          FIXME Bug 338334 overwrite equals()
  */
 public abstract class QuantityAmount<Q extends Quantity<Q>> extends Measure
 		implements IMeasure<Q> {
 
	/**
	 * Holds a dimensionless measure of one (exact).
	 */
	@SuppressWarnings("unchecked")
	public static final Quantity<Dimensionless> ONE =
			QuantityFactory.getInstance(QuantityAmount.class).create(
					BigDecimal.ONE, AbstractUnit.ONE);
	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.icu.util.Measure#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
 			return false;
 		if (obj == this)
 			return true;
 		if (this.getClass() == obj.getClass()) {
 			return super.equals(obj);
 		} else {
 			if (obj instanceof Measure) {
 				Measure m = (Measure) obj;
 				if (m.getNumber().getClass() == this.getNumber().getClass()
 						&& m.getUnit().getClass() == this.unit().getClass()) {
 					return super.equals(obj);
 				} else {
 					// if (this.getQuantityUnit() instanceof AbstractUnit<?>) {
 					// if
 					// }
 					return super.equals(obj);
 				}
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * Indicates if this measure is exact.
 	 */
 	private boolean isExact;
 
 	/**
 	 * Holds the exact value (when exact) stated in this measure unit.
 	 */
 	// private long _exactValue;
 
 	/**
 	 * Holds the minimum value stated in this measure unit. For inexact
 	 * measures: _minimum < _maximum
 	 */
 	// private double _minimum;
 
 	/**
 	 * Holds the maximum value stated in this measure unit. For inexact
 	 * measures: _maximum > _minimum
 	 */
 	// private double _maximum;
 
 	protected QuantityAmount(Number number, MeasureUnit unit) {
 		super(number, unit);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.uomo.units.IMeasure#doubleValue(org.unitsofmeasurement.unit
 	 * .Unit)
 	 */
 	@Override
 	public double doubleValue(Unit<Q> unit) {
 		Unit<Q> myUnit = unit();
 		try {
 			UnitConverter converter = unit.getConverterTo(myUnit);
 			return converter.convert(getNumber().doubleValue());
 		} catch (UnconvertibleException e) {
 			throw e;
 		} // catch (IncommensurableException e) {
 		// throw new IllegalArgumentException(e.getMessage());
 		// }
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.uomo.units.IMeasure#longValue(org.unitsofmeasurement.unit
 	 * .Unit)
 	 */
 	@Override
 	public long longValue(Unit<Q> unit) {
 		Unit<Q> myUnit = unit();
 		try {
 			UnitConverter converter = unit.getConverterToAny(myUnit);
 			return (converter.convert(
 					BigDecimal.valueOf(getNumber().longValue()),
 					MathContext.DECIMAL128)).longValue();
 		} catch (UnconvertibleException e) {
 			throw e;
 		} catch (IncommensurableException e) {
 			throw new IllegalArgumentException(e.getMessage());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.unitsofmeasurement.quantity.Quantity#unit()
 	 */
 	@Override
 	public Unit<Q> unit() {
 		return internalUnit();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.unitsofmeasurement.quantity.Quantity#value()
 	 */
 	@Override
 	public Number value() {
 		return getNumber();
 	}
 
 	/**
 	 * Indicates if this measure amount is exact. An exact amount is guarantee
 	 * exact only when stated in this measure unit (e.g.
 	 * <code>this.longValue()</code>); stating the amount in any other unit may
 	 * introduce conversion errors.
 	 * 
 	 * @return <code>true</code> if this measure is exact; <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean isExact() {
 		return isExact;
 	}
 
 	// ////////////////////
 	// Factory Creation //
 	// ////////////////////
 	
 //	private static <Q extends Quantity<Q>> QuantityAmount<Q> create(Number value, Unit<Q> unit) {
 //		
 //	}
 	
 
 //	@SuppressWarnings("unchecked")
 //	protected static <Q extends Quantity<Q>> QuantityAmount<Q> newInstance(
 //			Number value, Unit<?> unit) {
 //		QuantityAmount<Q> measure = FACTORY.create(value, unit);
 //
 //		measure._unit = (Unit<Q>) unit;
 //
 //		return measure;
 //	}
 
 	/**
 	 * Get the unit (convenience to avoid cast).
 	 * 
 	 * @provisional This API might change or be removed in a future release.
 	 */
 	@SuppressWarnings("unchecked")
 	private final AbstractUnit<Q> internalUnit() {
 		return (AbstractUnit<Q>) super.getUnit();
 	}
 }
