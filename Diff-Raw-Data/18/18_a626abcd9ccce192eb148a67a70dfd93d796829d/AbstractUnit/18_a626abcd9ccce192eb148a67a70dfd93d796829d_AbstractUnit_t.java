 /**
 * Copyright (c) 2005, 2011, Werner Keil, JScience and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Jean-Marie Dautelle, Steve Emmerson  
  *    - initial API and implementation
  */
 package org.eclipse.uomo.units;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.text.ParsePosition;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.uomo.core.IName;
 import org.eclipse.uomo.core.ISymbol;
 import org.eclipse.uomo.units.impl.AlternateUnit;
 import org.eclipse.uomo.units.impl.BaseUnit;
 import org.eclipse.uomo.units.impl.DimensionImpl;
 import org.eclipse.uomo.units.impl.ProductUnit;
 import org.eclipse.uomo.units.impl.TransformedUnit;
 import org.eclipse.uomo.units.impl.converter.AddConverter;
 import org.eclipse.uomo.units.impl.converter.LogConverter;
 import org.eclipse.uomo.units.impl.converter.MultiplyConverter;
 import org.eclipse.uomo.units.impl.converter.RationalConverter;
 import org.eclipse.uomo.units.impl.format.LocalUnitFormatImpl;
 import org.unitsofmeasurement.quantity.Dimensionless;
 import org.unitsofmeasurement.quantity.Quantity;
 import org.unitsofmeasurement.unit.Dimension;
 import org.unitsofmeasurement.unit.IncommensurableException;
 import org.unitsofmeasurement.unit.UnconvertibleException;
 import org.unitsofmeasurement.unit.Unit;
 import org.unitsofmeasurement.unit.UnitConverter;
 import org.unitsofmeasurement.unit.UnitFormat;
 
 import com.ibm.icu.util.MeasureUnit;
 
 /**
  * <p>
  * This class represents a determinate
  * {@linkplain javax.measure.quantity.Quantity quantity} (as of length, time,
  * heat, or value) adopted as a standard of measurement.
  * </p>
  * 
  * <p>
  * It is helpful to think of instances of this class as recording the history by
  * which they are created. Thus, for example, the string "g/kg" (which is a
  * dimensionless unit) would result from invoking the method toString() on a
  * unit that was created by dividing a gram unit by a kilogram unit. Yet, "kg"
  * divided by "kg" returns {@link #ONE} and not "kg/kg" due to automatic unit
  * factorization.
  * </p>
  * 
  * <p>
  * This class supports the multiplication of offsets units. The result is
  * usually a unit not convertible to its {@linkplain #toMetric metric unit}.
  * Such units may appear in derivative quantities. For example Celsius per meter
  * is an unit of gradient, which is common in atmospheric and oceanographic
  * research.
  * </p>
  * 
  * <p>
  * Units raised at non-integral powers are not supported. For example,
  * <code>LITRE.root(2)</code> raises an <code>ArithmeticException</code>; but
  * <code>LITRE.toMetric().root(2)</code> returns <code>METRE</code>.
  * </p>
  * 
  * <p>
  * Instances of this class and sub-classes are immutable.
  * </p>
  * 
  * @param <Q>
  *            The type of the quantity measured by this unit.
  * 
  * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
  * @author <a href="mailto:steve@unidata.ucar.edu">Steve Emmerson</a>
  * @author <a href="mailto:desruisseaux@users.sourceforge.net">Martin
  *         Desruisseaux</a>
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * 
  * @version 1.7 ($Revision: 312 $), $Date: 2011-03-07 00:50:44 +0430 $
  * @see <a href="http://en.wikipedia.org/wiki/Units_of_measurement"> Wikipedia:
  *      Units of measurement</a>
  */
 public abstract class AbstractUnit<Q extends Quantity<Q>> extends MeasureUnit
 		implements Unit<Q>, IName, ISymbol {
 
 	/**
 	 * For cross-version compatibility.
 	 */
	@SuppressWarnings("unused")
 	private static final long serialVersionUID = -2107517222666572443L;
 
 	/**
 	 * Holds the name.
 	 */
 	private String name;
 
 	/**
 	 * Holds the dimensionless unit <code>ONE</code>.
 	 */
 	public static final Unit<Dimensionless> ONE = new ProductUnit<Dimensionless>();
 
 	/**
 	 * Holds the unique symbols collection (base unit or alternate units).
 	 */
 	protected static final HashMap<String, Unit<?>> SYMBOL_TO_UNIT = new HashMap<String, Unit<?>>();
 
 	/**
 	 * Default constructor.
 	 */
 	protected AbstractUnit() {
 		this(null);
 	}
 
 	/**
 	 * Named constructor.
 	 */
 	@SuppressWarnings("deprecation")
 	protected AbstractUnit(String name) {
 		this.name = name;
 	}
 
 	void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the symbol (if any) of this unit. The default implementation
 	 * returns <code>null</code> (no symbol).
 	 * 
 	 * @return this unit symbol or <code>null</code> if this unit has not
 	 *         specific symbol associated with (e.g. product of units).
 	 */
 	public String getSymbol() {
 		return null;
 	}
 
 	/**
 	 * Returns the simple units and their exponent whose product is this unit or
 	 * <code>null</code> if this unit is a simple unit (not a product of
 	 * existing units).
 	 * 
 	 * @return the simple units and their exponent making up this unit.
 	 */
 	public Map<Unit<?>, Integer> getProductUnits() {
 		return null;
 	}
 
 	// ////////////////////////////////////////////////////
 	// Contract methods (for sub-classes to implement). //
 	// ////////////////////////////////////////////////////
 	/**
 	 * Returns the unscaled metric unit from which this unit is derived.
 	 * <p>
 	 * Metric units are either {@link BaseUnit base units}, {@link #alternate
 	 * alternate units} or product of rational powers of metric units.
 	 * </p>
 	 * <p>
 	 * Because the metric unit is unique by quantity type, it can be be used to
 	 * identify the quantity given the unit. For example:[code] static boolean
 	 * isAngularVelocity(Unit<?> unit) { return
 	 * unit.toMetric().equals(RADIAN.divide(SECOND)); }
 	 * assert(REVOLUTION.divide(MINUTE).isAngularVelocity()); // Returns true.
 	 * [/code]
 	 * 
 	 * @return the metric unit this unit is derived from or <code>this</code> if
 	 *         this unit {@link #isMetric() is a metric} unit.
 	 * @see #isMetric()
 	 */
 	protected abstract Unit<Q> toMetric();
 
 	/**
 	 * Returns the converter to the metric unit.
 	 * 
 	 * <p>
 	 * <i> Note: Having the same metric unit is not sufficient to ensure that a
 	 * converter exists between the two units (e.g. Â°C/m and K/m).</i>
 	 * </p>
 	 * 
 	 * @return the unit converter from this unit to its metric unit.
 	 */
 	public abstract UnitConverter getConverterToMetric();
 
 	/**
 	 * Returns the hash code for this unit.
 	 * 
 	 * @return this unit hashcode value.
 	 */
 	@Override
 	public abstract int hashCode();
 
 	/**
 	 * Indicates if the specified unit can be considered equals to the one
 	 * specified.
 	 * 
 	 * @param that
 	 *            the object to compare to.
 	 * @return <code>true</code> if this unit is considered equal to that unit;
 	 *         <code>false</code> otherwise.
 	 */
 	@Override
 	public abstract boolean equals(Object that);
 
 	/**
 	 * Indicates if this unit is an unscaled metric unit. Metric units are
 	 * either {@link BaseUnit base units}, {@link #alternate alternate units} or
 	 * product of rational powers of metric units. Because metric units are
 	 * unscaled units, {@link SI#METRE METRE} is a metric units; but
 	 * <code>KILO(METRE)</code> is not.
 	 * 
 	 * @return <code>this.toMetric().equals(this)</code>
 	 * @see #toMetric()
 	 */
 	public boolean isUnscaledMetric() {
 		return toMetric().equals(this);
 	}
 
 	/**
 	 * Indicates if this unit is compatible with the unit specified. Units don't
 	 * need to be equals to be compatible. For example:[code] RADIAN.equals(ONE)
 	 * == false RADIAN.isCompatible(ONE) == true [/code]
 	 * 
 	 * @param that
 	 *            the other unit.
 	 * @return <code>this.getDimension().equals(that.getDimension())</code>
 	 * @see #getDimension()
 	 */
 	public final boolean isCompatible(Unit<?> that) {
 		return (this == that) || this.toMetric().equals(that.getSystemUnit())
 				|| (!"".equals(this.getDimension().toString()) && this //$NON-NLS-1$
 						.getDimension().equals(that.getDimension()));
 	}
 
 	/**
 	 * Casts this unit to a parameterized unit of specified nature or throw a
 	 * <code>ClassCastException</code> if the dimension of the specified
 	 * quantity and this unit's dimension do not match. For example:[code]
 	 * Unit<Velocity> C =
 	 * METRE.times(299792458).divide(SECOND).asType(Velocity.class); [/code]
 	 * 
 	 * @param <T>
 	 *            The type of the quantity measured by the unit.
 	 * @param type
 	 *            the quantity class identifying the nature of the unit.
 	 * @return this unit parameterized with the specified type.
 	 * @throws ClassCastException
 	 *             if the dimension of this unit is different from the specified
 	 *             quantity dimension.
 	 * @throws UnsupportedOperationException
 	 *             if the specified type is not recognized.
 	 */
 	@SuppressWarnings("unchecked")
 	public final <T extends Quantity<T>> Unit<T> asType(Class<T> type)
 			throws ClassCastException {
 		Unit<T> metricUnit = QuantityFactory.getInstance(type).getMetricUnit();
 		if ((metricUnit == null) || metricUnit.isCompatible(this))
 			return (Unit<T>) this;
 		throw new ClassCastException("The unit: " + this //$NON-NLS-1$
 				+ " is not of parameterized type " + type); //$NON-NLS-1$
 		// TODO use NLS
 	}
 
 	/**
 	 * Returns the dimension of this unit (depends upon the current dimension
 	 * {@linkplain DimensionImpl.Model model}).
 	 * 
 	 * @return the dimension of this unit for the current model.
 	 */
 	public Dimension getDimension() {
 		// All possible metric units (BaseUnit, AlternateUnit and ProductUnit)
 		// overrides this method.
 		return this.toMetric().getDimension();
 	}
 
 	/**
 	 * Returns the intrinsic dimensional transform of this unit (depends upon
 	 * the current {@linkplain DimensionImpl.Model model} for {@link BaseUnit}
 	 * instance). Metric units should override this method.
 	 * 
 	 * @return the intrinsic transformation of this unit relatively to its
 	 *         dimension.
 	 * @throws UnsupportedOperationException
 	 *             if this unit is a metric unit and it does not override this
 	 *             method.
 	 */
 	public UnitConverter getDimensionalTransform() {
 		// All possible metric units (BaseUnit, AlternateUnit and ProductUnit)
 		// overrides this method.
 		return this.getConverterToMetric().concatenate(
 				((AbstractUnit<Q>) this.toMetric()).getDimensionalTransform());
 	}
 
 	/**
 	 * Returns a converter of numeric values from this unit to another unit of
 	 * same type (convenience method not raising checked exception).
 	 * 
 	 * @param that
 	 *            the unit of same type to which to convert the numeric values.
 	 * @return the converter from this unit to <code>that</code> unit.
 	 * @throws UnconvertibleException
 	 *             if the converter cannot be constructed.
 	 */
 	public UnitConverter getConverterTo(Unit<Q> that)
 			throws UnconvertibleException {
 		return searchConverterTo(that);
 	}
 
 	/**
 	 * Returns a converter form this unit to the specified unit of type unknown.
 	 * This method can be used when the dimension of the specified unit is
 	 * unknown at compile-time or when the {@linkplain DimensionImpl.Model
 	 * dimensional model} allows for conversion between units of different type.
 	 * To convert to a unit having the same parameterized type,
 	 * {@link #getConverterTo(AbstractUnit)} is preferred (no checked exception
 	 * raised).
 	 * 
 	 * @param that
 	 *            the unit to which to convert the numeric values.
 	 * @return the converter from this unit to <code>that</code> unit.
 	 * @throws ConversionException
 	 *             if the units are not compatible (e.g.
 	 *             <code>!this.isCompatible(that)</code>).
 	 * @throws UnsupportedOperationException
 	 *             if the converter cannot be constructed.
 	 */
 	public UnitConverter getConverterToAny(Unit<?> that)
 			throws IncommensurableException, UnconvertibleException {
 		return ((this == that) || this.equals(that)) ? AbstractConverter.IDENTITY
 				: searchConverterTo(that);
 	}
 
 	@SuppressWarnings("unchecked")
 	private UnitConverter searchConverterTo(Unit<?> that)
 			throws UnconvertibleException {
 		// First we have find a common dimension to convert to.
 
 		// Try the SI unit.
 		Unit<Q> thisSI = this.toMetric();
 		Unit<?> thatSI = that.getSystemUnit();
 		if (thisSI.equals(thatSI))
 			return ((AbstractUnit<?>) that).getConverterToMetric().inverse()
 					.concatenate(this.getConverterToMetric());
 
 		// Use dimensional unit.
 		if (!thisSI.getDimension().equals(thatSI.getDimension()))
 			throw new UnconvertibleException(this + " is not compatible with " //$NON-NLS-1$
 					+ that);
 		UnitConverter thisTransform = ((AbstractUnit<?>) thisSI)
 				.getDimensionalTransform().concatenate(
 						this.getConverterToMetric());
 		@SuppressWarnings("rawtypes")
 		UnitConverter thatTransform = ((AbstractUnit<Dimensionless>) thatSI)
 				.getDimensionalTransform().concatenate(
 						((AbstractUnit) that).getConverterToMetric());
 		return thatTransform.inverse().concatenate(thisTransform);
 	}
 
 	/**
 	 * Returns a metric unit equivalent to this unscaled metric unit but used in
 	 * expressions to distinguish between quantities of a different nature but
 	 * of the same dimensions.
 	 * 
 	 * <p>
 	 * Examples of alternate units:[code] Unit<Angle> RADIAN =
 	 * ONE.alternate("rad"); Unit<Force> NEWTON =
 	 * METRE.times(KILOGRAM).divide(SECOND.pow(2)).alternate("N");
 	 * Unit<Pressure> PASCAL = NEWTON.divide(METRE.pow(2)).alternate("Pa");
 	 * [/code]
 	 * </p>
 	 * 
 	 * @param <Q>
 	 *            the type of the quantity measured by the new alternate unit.
 	 * 
 	 * @param symbol
 	 *            the new symbol for the alternate unit.
 	 * @return the alternate unit.
 	 * @throws UnsupportedOperationException
 	 *             if this unit is not an unscaled metric unit.
 	 * @throws IllegalArgumentException
 	 *             if the specified symbol is already associated to a different
 	 *             unit.
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public final Unit<Q> alternate(String symbol) {
 		return new AlternateUnit(symbol, this);
 	}
 
 	/**
 	 * Returns the unit derived from this unit using the specified converter.
 	 * The converter does not need to be linear. For example:[code]
 	 * Unit<Dimensionless> DECIBEL = Unit.ONE.transform( new
 	 * LogConverter(10).inverse().concatenate( new RationalConverter(1, 10)));
 	 * [/code]
 	 * 
 	 * @param operation
 	 *            the converter from the transformed unit to this unit.
 	 * @return the unit after the specified transformation.
 	 */
 	@SuppressWarnings("unchecked")
 	public final Unit<Q> transform(UnitConverter operation) {
 		if (this instanceof TransformedUnit) {
 			Unit<Q> tf = this;
 			Unit<?> parent = (Unit<?>) ((TransformedUnit<?>) tf)
 					.getParentUnit();
 			UnitConverter toParent = ((TransformedUnit<?>) tf).toParentUnit();
 			if (toParent == null)
 				return (Unit<Q>) parent;
 			UnitConverter toParentConcat = toParent.concatenate(operation);
 			if (toParentConcat == AbstractConverter.IDENTITY)
 				return (Unit<Q>) parent;
 			return new TransformedUnit<Q>((Unit<Q>) parent,
 					(AbstractConverter) toParentConcat);
 		}
 		if (operation == AbstractConverter.IDENTITY)
 			return this;
 		return new TransformedUnit<Q>(this, (AbstractConverter) operation);
 	}
 
 	/**
 	 * Returns the result of adding an offset to this unit. The returned unit is
 	 * convertible with all units that are convertible with this unit.
 	 * 
 	 * @param offset
 	 *            the offset added (expressed in this unit, e.g.
 	 *            <code>CELSIUS = KELVIN.add(273.15)</code>).
 	 * @return <code>this.transform(new AddConverter(offset))</code>
 	 */
 	public final Unit<Q> add(double offset) {
 		if (offset == 0)
 			return this;
 		return transform(new AddConverter(offset));
 	}
 
 	/**
 	 * Returns the result of multiplying this unit by an exact factor.
 	 * 
 	 * @param factor
 	 *            the exact scale factor (e.g.
 	 *            <code>KILOMETRE = METRE.multiply(1000)</code>).
 	 * @return <code>this.transform(new RationalConverter(factor, 1))</code>
 	 */
 	final Unit<Q> multiply(long factor) {
 		if (factor == 1)
 			return this;
 		return transform(new RationalConverter(BigInteger.valueOf(factor),
 				BigInteger.ONE));
 	}
 
 	/**
 	 * Returns the result of multiplying this unit by a an approximate factor.
 	 * 
 	 * @param factor
 	 *            the approximate factor (e.g.
 	 *            <code>ELECTRON_MASS = KILOGRAM.multiply(9.10938188e-31)</code>
 	 *            ).
 	 * @return <code>this.transform(new MultiplyConverter(factor))</code>
 	 */
 	public final Unit<Q> multiply(double factor) {
 		if (factor == 1)
 			return this;
 		return transform(new MultiplyConverter(factor));
 	}
 
 	/**
 	 * Returns the product of this unit with the one specified.
 	 * 
 	 * @param that
 	 *            the unit multiplicand.
 	 * @return <code>this * that</code>
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public final Unit<?> multiply(Unit<?> that) {
 		if (this.equals(ONE))
 			return that;
 		if (that.equals(ONE))
 			return this;
 		if (this.isRationalFactor())
 			return that.transform(this.getConverterTo((Unit) ONE));
 		if (((AbstractUnit) that).isRationalFactor())
 			return this.transform(that.getConverterTo((Unit) ONE));
 		return ProductUnit.getProductInstance(this, (AbstractUnit<?>) that);
 	}
 
 	private boolean isRationalFactor() {
 		if (!(this instanceof TransformedUnit<?>))
 			return false;
 		TransformedUnit<Q> tu = (TransformedUnit<Q>) this;
 		return tu.getParentUnit().equals(ONE)
 				&& (tu.getConverterTo(tu.getSystemUnit()) instanceof RationalConverter);
 	}
 
 	/**
 	 * Returns the inverse of this unit.
 	 * 
 	 * @return <code>1 / this</code>
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public final Unit<?> inverse() {
 		if (this.equals(ONE))
 			return this;
 		if (this.isRationalFactor())
 			return this.transform(this.getConverterTo((Unit) ONE).inverse());
 		return ProductUnit.getQuotientInstance((AbstractUnit<?>) ONE, this);
 	}
 
 	/**
 	 * Returns the result of dividing this unit by an exact divisor.
 	 * 
 	 * @param divisor
 	 *            the exact divisor. (e.g.
 	 *            <code>QUART = GALLON_LIQUID_US.divide(4)</code>).
 	 * @return <code>this.transform(new RationalConverter(1 , divisor))</code>
 	 */
 	public final Unit<Q> divide(long divisor) {
 		if (divisor == 1)
 			return this;
 		return transform(new RationalConverter(BigInteger.ONE,
 				BigInteger.valueOf(divisor)));
 	}
 
 	/**
 	 * Returns the result of dividing this unit by an approximate divisor.
 	 * 
 	 * @param divisor
 	 *            the approximate divisor.
 	 * @return <code>this.transform(new MultiplyConverter(1.0 / divisor))</code>
 	 */
 	public final Unit<Q> divide(double divisor) {
 		if (divisor == 1)
 			return this;
 		return transform(new MultiplyConverter(1.0 / divisor));
 	}
 	
 	/**
 	 * Returns the logarithmic result of this unit.
 	 * 
 	 * @param base
 	 *            the logarithmic base.
 	 * @return <code>this.transform(new MultiplyConverter(1.0 / divisor))</code>
 	 */	
 	public final Unit<Q> log(double base) {
 		return transform(new LogConverter(base));
 	}
 
 	/**
 	 * Returns the quotient of this unit with the one specified.
 	 * 
 	 * @param that
 	 *            the unit divisor.
 	 * @return <code>this / that</code>
 	 */
 	public final Unit<?> divide(Unit<?> that) {
 		return (AbstractUnit<?>) this.multiply(that.inverse());
 	}
 
 	/**
 	 * Returns a unit equals to the given root of this unit.
 	 * 
 	 * @param n
 	 *            the root's order.
 	 * @return the result of taking the given root of this unit.
 	 * @throws ArithmeticException
 	 *             if <code>n == 0</code> or if this operation would result in
 	 *             an unit with a fractional exponent.
 	 */
 	public final Unit<?> root(int n) {
 		if (n > 0)
 			return ProductUnit.getRootInstance(this, n);
 		else if (n == 0)
 			throw new ArithmeticException("Root's order of zero"); //$NON-NLS-1$
 		else
 			// n < 0
 			return ONE.divide(this.root(-n));
 	}
 
 	/**
 	 * Returns a unit equals to this unit raised to an exponent.
 	 * 
 	 * @param n
 	 *            the exponent.
 	 * @return the result of raising this unit to the exponent.
 	 */
 	public final Unit<?> pow(int n) {
 		if (n > 0)
 			return this.multiply(this.pow(n - 1));
 		else if (n == 0)
 			return ONE;
 		else
 			// n < 0
 			return ONE.divide(this.pow(-n));
 	}
 
 	/**
 	 * Returns a unit instance that is defined from the specified character
 	 * sequence (text) using the {@linkplain AbstractFormat#getInstance default}
 	 * unit format (<a href="http://unitsofmeasure.org/">UCUM</a> based). This
 	 * method is capable of parsing any units representations produced by
 	 * {@link #toString()}. Locale-sensitive unit formatting and parsing are
 	 * handled by the {@link AbstractFormat} class and its subclasses.
 	 * 
 	 * <p>
 	 * This method can be used to parse dimensionless units.[code]
 	 * Unit<Dimensionless> PERCENT =
 	 * Unit.valueOf("100").inverse().asType(Dimensionless.class); [/code]
 	 * 
 	 * @param charSequence
 	 *            the character sequence to parse.
 	 * @return <code>UnitFormat.getInstance().parse(csq, new ParsePosition(0))</code>
 	 * @throws IllegalArgumentException
 	 *             if the specified character sequence cannot be correctly
 	 *             parsed (e.g. not UCUM compliant).
 	 */
 	public static Unit<?> valueOf(CharSequence charSequence) {
 		return LocalUnitFormatImpl.getInstance().parse(charSequence,
 				new ParsePosition(0));
 	}
 
 	// ////////////////////
 	// GENERAL CONTRACT //
 	// ////////////////////
 	/**
 	 * Returns the international <code>String</code> representation of this unit
 	 * (<a href="http://unitsofmeasure.org/">UCUM</a> based). The string
 	 * produced for a given unit is always the same; it is not affected by the
 	 * locale. This means that it can be used as a canonical string
 	 * representation for exchanging units, or as a key for a Hashtable, etc.
 	 * Locale-sensitive unit formatting and parsing is handled by
 	 * {@link AbstractFormat} class and its subclasses.
 	 * 
 	 * @return <code>UnitFormat.getInstance().format(this)</code>
 	 */
 	@Override
 	public String toString() {
 		try {
 			UnitFormat format = LocalUnitFormatImpl.getInstance();
 			return String.valueOf(format
 					.format(this, new StringBuilder()));
 		} catch (IOException e) {
 			// TODO should this happen?
 			return ""; //$NON-NLS-1$
 		}
 	}
 }
