 package org.muis.core.style;
 
 import prisms.util.ArrayUtils;
 
 /**
  * A utility class used by {@link SimpleConditionalStyle}
  *
  * @param <E> The type of expression supported by the style set using this class
  * @param <V> The type of the attribute that this holder holds values for
  */
 public class StyleValueHolder<E extends StyleExpression<E>, V>
 	implements Cloneable {
 	private StyleExpressionValue<E, V> [] theValues;
 
 	private boolean isSorted;
 
 	/** @param value The initial expression value to hold */
 	protected StyleValueHolder(StyleExpressionValue<E, V> value) {
 		theValues = new StyleExpressionValue[] {value};
 	}
 
 	/** @return The number of expression values in this holder */
 	public int size() {
 		return theValues.length;
 	}
 
 	/** @param value The expression value to add to this holder */
 	public void add(StyleExpressionValue<E, V> value) {
 		isSorted = false;
 		theValues = ArrayUtils.add(theValues, value);
 	}
 
 	/**
 	 * Removes the value for the given expression from this holder
	 *
 	 * @param exp The expression to remove the value for
 	 * @return Whether this operation might have caused an attribute value to be different for some condition
 	 */
 	public boolean remove(E exp) {
 		StyleExpressionValue<E, V> [] values = theValues;
 		boolean fireEvent = true;
 		for(int i = 0; i < values.length; i++) {
 			if(ArrayUtils.equals(exp, values[i].getExpression())) {
 				theValues = ArrayUtils.remove(theValues, values[i]);
 				return fireEvent;
 			} else if(values[i].getExpression() == null || (exp != null && values[i].getExpression().getWhenTrue(exp) > 0))
 				fireEvent = false;
 		}
 		return false;
 	}
 
 	/** @return This holder's expression values, sorted from most general to most specific condition */
 	public StyleExpressionValue<E, V> [] sort() {
 		if(isSorted)
 			return theValues;
 		StyleExpressionValue<E, V> [] values = theValues;
 		java.util.Arrays.sort(values, new java.util.Comparator<StyleExpressionValue<E, V>>() {
 			@Override
 			public int compare(StyleExpressionValue<E, V> o1, StyleExpressionValue<E, V> o2) {
 				E exp1 = o1.getExpression();
 				E exp2 = o2.getExpression();
 				if(exp1 == null)
 					return exp2 == null ? 0 : 1;
 				if(exp2 == null)
 					return -1;
 				return exp2.getPriority() - exp1.getPriority();
 			}
 		});
		if(values != theValues) {
 			theValues = values;
 			isSorted = true;
 		}
 		return values;
 	}
 
 	void set(StyleExpressionValue<E, V> sev) {
 		boolean set = false;
 		boolean found = true;
 		while(found && !set) {
 			found = false;
 			StyleExpressionValue<E, V> [] values = theValues;
 			for(int i = 0; i < values.length; i++) {
 				if(ArrayUtils.equals(sev.getExpression(), values[i].getExpression())) {
 					found = true;
 					if(theValues[i] == values[i]) {
 						theValues[i] = sev;
 						set = true;
 					}
 					break;
 				}
 			}
 		}
 		if(!found)
 			add(sev);
 	}
 
 	@Override
 	protected StyleValueHolder<E, V> clone() {
 		StyleValueHolder<E, V> ret;
 		try {
 			ret = (StyleValueHolder<E, V>) super.clone();
 		} catch(CloneNotSupportedException e) {
 			throw new IllegalStateException(e);
 		}
 		ret.theValues = theValues.clone();
 		return ret;
 	}
 }
