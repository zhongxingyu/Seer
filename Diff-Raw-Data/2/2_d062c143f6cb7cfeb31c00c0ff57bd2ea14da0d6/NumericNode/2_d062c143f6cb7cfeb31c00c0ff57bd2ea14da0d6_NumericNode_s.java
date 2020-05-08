 package org.dawnsci.common.widgets.tree;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.measure.quantity.Quantity;
 import javax.measure.unit.NonSI;
 import javax.measure.unit.Unit;
 import javax.swing.tree.TreeNode;
 
 import org.jscience.physics.amount.Amount;
 import org.jscience.physics.amount.Constants;
 
 /**
  * This class may be used with TreeNodeContentProvider to create a Tree of editable
  * items.
  * 
  * The classes LabelNode, NumericNode and ObjectNode are generic and may be used 
  * elsewhere. They have not been moved somewhere generic yet because they create a 
  * dependency on jscience.
  * 
  * @author fcp94556
  *
  */
 @SuppressWarnings({"rawtypes", "unchecked"})
 public class NumericNode<E extends Quantity> extends LabelNode {
 			
 	private Amount     value;  // Intentionally not E
 	private Amount<E>  defaultValue;
 	private Amount<E>  lowerBound;
 	private Amount<E>  upperBound;
 	private Unit<E>    defaultUnit;
 	private double     increment;
 	private NumberFormat format;
 	
 	/**
 	 * allowedUnits does not have to be E intentionally
 	 * For instance angstrom and eV are compatible using jscience
 	 * which knows about the Planck constant relationship.
 	 */
 	private List<Unit> allowedUnits;
 
 	/**
 	 * Unit must not be null.
 	 * @param label
 	 * @param unit, nit null
 	 */
 	public NumericNode(String label, Unit<E> unit) {
 		this(label, null, unit);
 	}
 	
 	/**
 	 * Unit must not be null.
 	 * @param label
 	 * @param unit
 	 */
 	public NumericNode(String label, LabelNode parent, Unit<E> unit) {
 		super(label, parent);
 		this.defaultUnit = unit;
 		this.increment   = 0.1;
 		this.format  = new DecimalFormat("#0.####");
 	}
 
 
 	public boolean isNaN() {
 		return value==null&&defaultValue==null;
 	}
 
     /**
      * The double value in the current unit set.
      * @return
      */
 	public Amount<E> getValue() {
 		if (value!=null)        return value;
 		if (defaultValue!=null) return defaultValue;
 		return null;
 	}
 	
 	public double getValue(Unit requiredUnit) {
 		Amount<E> val=getValue();
 		if (isInAngstroms(val, requiredUnit)) {	
 			return Constants.ℎ.times(Constants.c).divide(val).doubleValue(requiredUnit);
 		} else {
 			if (val!=null) {
 				return val.doubleValue(requiredUnit);
 			}
 		}
 
 		return Double.NaN;
 	}
 	
 	public String getValue(boolean isFormat) {
 		if (isFormat) {
 			return format.format(getValue(getValue().getUnit()));
 		} else {
 			return String.valueOf(getValue(getValue().getUnit()));
 		}
 	}
 
 	public double getDoubleValue() {
 		final Amount<E> val = getValue();
 		return val!=null? val.doubleValue(val.getUnit()) : Double.NaN;
 	}
 	
 	public void setDoubleValue(double val) {
 		if (value!=null)        {
 			value = Amount.valueOf(val, value.getUnit());
 			fireAmountChanged(value);
 			return;
 		}
 		if (defaultValue!=null) {
 			value = Amount.valueOf(val, defaultValue.getUnit());
 			fireAmountChanged(value);
 			return;
 		}
 		value = Amount.valueOf(val, defaultUnit);
 		fireAmountChanged(value);
 		return;
 	}
 	
 	private Collection<AmountListener<E>> listeners;
 	
 	protected void fireAmountChanged(Amount<E> value, AmountListener<E>... ignored) {
 		if (listeners==null) return;
 
 		Collection<AmountListener<E>> informees;
 		if (ignored == null || ignored.length == 0) {
 			informees = listeners;
 		} else {
 			informees = new HashSet<AmountListener<E>>(listeners);
 			informees.removeAll(Arrays.asList(ignored));
 		}
 		if (informees.size() == 0)
 			return;
 
 		final AmountEvent<E> evt = new AmountEvent<E>(this, value);
 		for (AmountListener<E> l : informees) {
 			l.amountChanged(evt);
 		}
 	}
 	
 	public void addAmountListener(AmountListener<E> l) {
 		if (listeners==null) listeners = new HashSet<AmountListener<E>>(3);
 		listeners.add(l);
 	}
 	
 	public void removeAmountListener(AmountListener<E> l) {
 		if (listeners==null) return;
 		listeners.remove(l);
 	}
 	
 	private Collection<UnitListener> unitListeners;
 	
 	protected void fireUnitChanged(Unit<E> unit) {
 		if (unitListeners==null) return;
 		final UnitEvent<E> evt = new UnitEvent<E>(this, unit);
 		for (UnitListener l : unitListeners) {
 			l.unitChanged(evt);
 		}
 	}
 	
 	public void addUnitListener(UnitListener l) {
 		if (unitListeners==null) unitListeners = new HashSet<UnitListener>(3);
 		unitListeners.add(l);
 	}
 	
 	public void removeUnitListener(UnitListener l) {
 		if (listeners==null) return;
 		unitListeners.remove(l);
 	}
 
 	/**
 	 * May be null
 	 * @param val
 	 */
 	public void setValueQuietly(Amount<E> val) {
 		value = val;
 	}
 
 	/**
 	 * May be null
 	 * @param val
 	 */
 	public void setValue(Amount<E> val) {
 		setValue(val, (AmountListener<E>[]) null);
 	}
 
 	/**
 	 * May be null
 	 * @param val
 	 * @param ignored amount listeners
 	 */
 	public void setValue(Amount<E> val, AmountListener<E>... ignored) {
 		Amount oldValue = value;
 		setValueQuietly(val);
 		if (value != null) {
 			fireAmountChanged(val, ignored);
 			Unit oldUnit = oldValue != null ? oldValue.getUnit() : (defaultValue != null ? defaultValue.getUnit() : defaultUnit);
 			Unit unit = val.getUnit();
 			if (!oldUnit.equals(unit))
 				fireUnitChanged(unit);
 		}
 	}
 
 	/**
 	 * @param val
 	 * @param unit
 	 * @return unit used
 	 */
 	public void setValueQuietly(double val, Unit<E> unit) {
 		if (Double.isNaN(val)) {
 			value=null;
 			return;// The value is NaN, doing Amount.valueOf(...) would set to 0
 		}
 		if (unit==null) {
 			unit = value != null ? value.getUnit() : (defaultValue != null ? defaultValue.getUnit() : defaultUnit);
 		}
 		value = Amount.valueOf(val, unit);
 	}
 
 	/**
 	 * @param val
 	 * @param unit
 	 */
 	public void setValue(double val, Unit<E> unit) {
 		setValue(val, unit, (AmountListener<E>[]) null);
 	}
 
 	/**
 	 * @param val
 	 * @param unit
 	 * @param ignored amount listeners
 	 */
 	public void setValue(double val, Unit<E> unit, AmountListener<E>... ignored) {
 		Amount oldValue = value;
 		setValueQuietly(val, unit);
 		if (value == null)
 			return;
 
 		fireAmountChanged(value, ignored);
 		Unit oldUnit = oldValue != null ? oldValue.getUnit() : (defaultValue != null ? defaultValue.getUnit() : defaultUnit);
 		unit = value.getUnit();
 		if (!oldUnit.equals(unit))
 			fireUnitChanged(unit);
 	}
 
 	public boolean mergeValue(TreeNode node) throws Throwable {
 		
 		if (equals(node)) return false;
 		
 		Amount<E> newValue = null;
 		if (node instanceof ObjectNode) {
 			ObjectNode on = (ObjectNode)node;
 			newValue = parseValue(on.getValue());
 		}
 		if (node instanceof NumericNode) {
 			NumericNode nn = (NumericNode)node;
 			newValue = nn.getValue();
 		}
 		
 		if (newValue!=null) {
 			setValue(newValue);
 			return true;
 		}
 		return false;
 	}
 	
 	private Amount parseValue(Object val) throws Throwable {
 		try {
 			if (val instanceof Amount) return (Amount<E>)val;
 			
 			final double dbl = Double.parseDouble(val.toString());
 			return Amount.valueOf(dbl, getValue().getUnit());
 					
 		} catch (Throwable ne) {
 			try {
 				return (Amount)Amount.valueOf(val.toString()); //e.g. "100.0 mm"
 				
 			} catch (Throwable e) {
 				throw e;
 			}
 		}
 	}
 
 	public void setDefault(Amount<E> amount) {
 		this.defaultValue = amount;
 	}
 	/**
 	 * Sets the default value and sets the bounds to
 	 * the values 0 and 10000.
 	 * @param value
 	 */
 	public void setDefault(double value, Unit<E> unit) {
 		if (Double.isNaN(value)) return;// The value is NaN, doing Amount.valueOf(...) would set to 0
 		this.defaultValue = Amount.valueOf(value, unit);
 		this.lowerBound   = Amount.valueOf(Integer.MIN_VALUE, unit);
 		this.upperBound   = Amount.valueOf(Integer.MAX_VALUE, unit);
 	}
 
     /**
      * The double value in the current unit set.
      * @return
      */
 	public Amount<E> getDefaultValue() {
 		if (defaultValue!=null) return defaultValue;
 		return Amount.valueOf(Double.NaN, getUnit());
 	}
 	public String getDefaultValue(boolean isFormat) {
 		if (isFormat) {
 			return format.format(getDefaultValue().doubleValue(getDefaultValue().getUnit()));
 		} else {
 			return String.valueOf(getDefaultValue().doubleValue(getDefaultValue().getUnit()));
 		}
 	}
 	
 	public Unit<E> getUnit() {
 		if (value!=null)        return value.getUnit();
 		if (defaultValue!=null) return defaultValue.getUnit();
 		return defaultUnit;
 	}
 	
 	public int getUnitIndex() {
 		if (allowedUnits==null) return 0;
 		return allowedUnits.indexOf(getUnit());
 	}
 
 	public void setUnitIndex(int index) {
 		if (allowedUnits==null) return;
 		final Unit<E> to = allowedUnits.get(index);
 		if (value==null&&defaultValue!=null) {
 			value = defaultValue.copy();
 		}
 		if (value!=null) {
 			// BODGE for A and eV !
 			if (isInAngstroms(value, to)) {	
 				value = Constants.ℎ.times(Constants.c).divide(value).to(to); 
 			} else {
 			    value = value.to(to);
 			}
 			fireUnitChanged(to);
 		}
 	}
 
 	private boolean isInAngstroms(Amount val, Unit to) {
 		boolean isAngstrom = allowedUnits!=null && allowedUnits.contains(NonSI.ANGSTROM) && allowedUnits.contains(NonSI.ELECTRON_VOLT);
 	    if (!isAngstrom) return false;
 	    return !val.getUnit().isCompatible(to); // Only convert incompatible.
 	}
 
 	public void setUnit(Unit<E> unit) {
 		if (value!=null)        value        = Amount.valueOf(value.doubleValue(unit), unit);
 		if (defaultValue!=null) defaultValue = Amount.valueOf(defaultValue.doubleValue(unit), unit);
 		fireUnitChanged(unit);
 	}
 
 	public void reset() {
 		value = null;
 		fireAmountChanged(getValue());
 	}
 
 	public Amount<E> getLowerBound() {
 		return lowerBound;
 	}
 	public double getLowerBoundDouble() {
 		return lowerBound.doubleValue(lowerBound.getUnit());
 	}
 
 	public void setLowerBound(Amount<E> lowerBound) {
 		this.lowerBound = lowerBound;
 	}
 	
 	public void setLowerBound(double lb) {
 		this.lowerBound = Amount.valueOf(lb, getUnit());
 	}
 
 	public Amount<E> getUpperBound() {
 		return upperBound;
 	}
 	public double getUpperBoundDouble() {
 		return upperBound.doubleValue(upperBound.getUnit());
 	}
 
 	public void setUpperBound(Amount<E> upperBound) {
 		this.upperBound = upperBound;
 	}
 	
 	public void setUpperBound(double ub) {
 		this.upperBound = Amount.valueOf(ub, getUnit());
 	}
 
 	public double getIncrement() {
 		return increment;
 	}
 
 	public void setIncrement(double increment) {
 		this.increment = increment;
 	}
 
 	public NumberFormat getFormat() {
 		return format;
 	}
 
 	public void setFormat(NumberFormat format) {
 		this.format = format;
 	}
 	public void setFormat(String format) {
 		this.format = new DecimalFormat(format);
 	}
 
 	public List<Unit> getUnits() {
 		return allowedUnits;
 	}
 
 	public void setUnits(List<Unit> allowedUnits) {
 		this.allowedUnits = allowedUnits;
 	}
 	public void setUnits(Unit... allowedUnits) {
 		this.allowedUnits = Arrays.asList(allowedUnits);
 		
 		if (value!=null)        {
 			value = convertToNewSet(value, allowedUnits);
 			fireAmountChanged(value);
 		}
 		if (defaultValue!=null) defaultValue = convertToNewSet(defaultValue, allowedUnits);
 	}
 
 	private Amount<E> convertToNewSet(Amount<E> val, Unit[] au) {
 
 		for (Unit unit : au) {
 			// This unit is active and may have just 
 			if (val.getUnit().toString().equals(unit.toString())) {
 				Amount standard = val.to(val.getUnit().getStandardUnit());
 				return standard.to(unit);
 			}
 		}
 		return val;
 	}
 
 	public String[] getUnitsString() {
 		final String[] ret = new String[allowedUnits.size()];
 		for (int i = 0; i < ret.length; i++) ret[i] = allowedUnits.get(i).toString();
 		return ret;
 	}
 
 	/**
 	 * Gets the decimal places used to view the number
 	 * @return
 	 */
 	public int getDecimalPlaces() {
		return format.getMinimumFractionDigits();
 	}
 
 	public void dispose() {
 		super.dispose();
 		if (listeners!=null) listeners.clear();
 		listeners = null;
 		if (unitListeners!=null) unitListeners.clear();
 		unitListeners = null;
 		
 		if (allowedUnits!=null) {
 			try {
 				allowedUnits.clear();
 			} catch (Throwable ne) {
 				// they are allowed unmodifiable units.
 			}
 		}
 		allowedUnits = null;
 		
 		value        =null;
 		defaultValue =null;
 		lowerBound   =null;
 		upperBound   =null;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result
 				+ ((allowedUnits == null) ? 0 : allowedUnits.hashCode());
 		result = prime * result
 				+ ((defaultUnit == null) ? 0 : defaultUnit.hashCode());
 		result = prime * result
 				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
 		result = prime * result + ((format == null) ? 0 : format.hashCode());
 		long temp;
 		temp = Double.doubleToLongBits(increment);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result
 				+ ((lowerBound == null) ? 0 : lowerBound.hashCode());
 		result = prime * result
 				+ ((upperBound == null) ? 0 : upperBound.hashCode());
 		result = prime * result + ((value == null) ? 0 : value.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		NumericNode other = (NumericNode) obj;
 		if (allowedUnits == null) {
 			if (other.allowedUnits != null)
 				return false;
 		} else if (!allowedUnits.equals(other.allowedUnits))
 			return false;
 		if (defaultUnit == null) {
 			if (other.defaultUnit != null)
 				return false;
 		} else if (!defaultUnit.equals(other.defaultUnit))
 			return false;
 		if (defaultValue == null) {
 			if (other.defaultValue != null)
 				return false;
 		} else if (!defaultValue.equals(other.defaultValue))
 			return false;
 		if (format == null) {
 			if (other.format != null)
 				return false;
 		} else if (!format.equals(other.format))
 			return false;
 		if (Double.doubleToLongBits(increment) != Double
 				.doubleToLongBits(other.increment))
 			return false;
 		if (lowerBound == null) {
 			if (other.lowerBound != null)
 				return false;
 		} else if (!lowerBound.equals(other.lowerBound))
 			return false;
 		if (upperBound == null) {
 			if (other.upperBound != null)
 				return false;
 		} else if (!upperBound.equals(other.upperBound))
 			return false;
 		if (value == null) {
 			if (other.value != null)
 				return false;
 		} else if (!value.equals(other.value))
 			return false;
 		return true;
 	}
 
 }
