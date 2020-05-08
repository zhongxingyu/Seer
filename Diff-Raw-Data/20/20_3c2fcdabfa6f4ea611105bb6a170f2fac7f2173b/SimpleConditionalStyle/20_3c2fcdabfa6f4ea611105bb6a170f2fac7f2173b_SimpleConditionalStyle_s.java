 package org.muis.core.style;
 
 import java.util.Iterator;
 
 import prisms.util.ArrayUtils;
 
 /**
  * Implements ConditionalStyle without dependencies
  *
  * @param <S> The type of style set
  * @param <E> The type of expression supported by the style set
  */
 public abstract class SimpleConditionalStyle<S extends ConditionalStyle<S, E>, E extends StyleExpression<E>> implements
 	ConditionalStyle<S, E>, Cloneable {
 	private static final Object NULL = new Object();
 
 	private java.util.concurrent.ConcurrentHashMap<StyleAttribute<?>, StyleValueHolder<E, ?>> theAttributes;
 
 	private java.util.concurrent.ConcurrentLinkedQueue<StyleExpressionListener<S, E>> theListeners;
 
 	/** Creates a SimpleStatefulStyle */
 	protected SimpleConditionalStyle() {
 		theAttributes = new java.util.concurrent.ConcurrentHashMap<>();
 		theListeners = new java.util.concurrent.ConcurrentLinkedQueue<>();
 	}
 
 	@Override
 	public Iterable<StyleAttribute<?>> allLocal() {
 		return ArrayUtils.immutableIterable(theAttributes.keySet());
 	}
 
 	@Override
 	public <T> StyleExpressionValue<E, T> [] getLocalExpressions(StyleAttribute<T> attr) {
 		StyleValueHolder<E, T> holder = (StyleValueHolder<E, T>) theAttributes.get(attr);
 		return holder == null ? new StyleExpressionValue[0] : holder.sort();
 	}
 
 	@Override
 	public void addListener(StyleExpressionListener<S, E> listener) {
 		if(listener != null)
 			theListeners.add(listener);
 	}
 
 	@Override
 	public void removeListener(StyleExpressionListener<S, E> listener) {
 		theListeners.remove(listener);
 	}
 
 	/**
 	 * @param <T> The type of the attribute
 	 * @see MutableConditionalStyle#set(StyleAttribute, StyleExpression, Object) Implemented to make extensions of this class easily support
 	 *      {@link MutableConditionalStyle}
 	 * @param attr The attribute to set the value of
 	 * @param value The value to set for the attribute
 	 * @throws ClassCastException If the given value is not recognized by the attribute
 	 * @throws IllegalArgumentException If the given value is invalid for the given attribute
 	 */
 	protected <T> void set(StyleAttribute<T> attr, T value) throws ClassCastException, IllegalArgumentException {
 		set(attr, null, value);
 	}
 
 	/**
 	 * @see MutableConditionalStyle#set(StyleAttribute, StyleExpression, Object) Implemented to make extensions of this class easily support
 	 *      {@link MutableConditionalStyle}
 	 * @param <T> The type of the attribute
 	 * @param attr The attribute to set the value of
 	 * @param exp The state expression for the value to be active for
 	 * @param value The value to set for the attribute
 	 * @throws ClassCastException If the given value is not recognized by the attribute
 	 * @throws IllegalArgumentException If the given value is invalid for the given attribute
 	 */
 	protected <T> void set(StyleAttribute<T> attr, E exp, T value) throws ClassCastException, IllegalArgumentException {
 		if(value == null) {
 			clear(attr, exp);
 			return;
 		}
 		if(attr == null)
 			throw new NullPointerException("Cannot set the value of a null attribute");
 		value = castAndValidate(attr, value);
 		setValue(attr, exp, value);
 	}
 
 	/**
 	 * Ensures that the given value can be set for a particular attribute
 	 *
 	 * @param <T> The type of the attribute
 	 * @param attr The attribute to set a value for
 	 * @param value The value to be set for the attribute
 	 * @return The value to store for the attribute
 	 * @throws ClassCastException If the given value is not recognized by the attribute
 	 * @throws IllegalArgumentException If the given value is not valid for the attribute
 	 */
 	protected <T> T castAndValidate(StyleAttribute<T> attr, T value) throws ClassCastException, IllegalArgumentException {
 		T value2 = attr.getType().cast(value);
 		if(value2 == null)
 			throw new ClassCastException(value.getClass().getName() + " instance " + value + " cannot be set for attribute " + attr
 				+ " of type " + attr.getType());
 		if(attr.getValidator() != null)
 			try {
				attr.getValidator().assertValid(value);
 			} catch(org.muis.core.MuisException e) {
 				throw new IllegalArgumentException(e.getMessage());
 			}
 		return value2;
 	}
 
 	private <T> void setValue(StyleAttribute<T> attr, E exp, T value) {
 		if(value == null)
 			value = (T) NULL;
 		StyleExpressionValue<E, T> sev = new StyleExpressionValue<>(exp, value);
 		StyleValueHolder<E, T> holder = (StyleValueHolder<E, T>) theAttributes.get(attr);
 		if(holder == null) {
 			holder = new StyleValueHolder<>(sev);
 			theAttributes.put(attr, holder);
 		} else
 			holder.set(sev);
 		styleChanged(attr, exp, null);
 	}
 
 	/**
 	 * @see MutableConditionalStyle#clear(StyleAttribute, StyleExpression) Implemented to make extensions of this class easily support
 	 *      MutableConditionalStyle
 	 * @param attr The attribute to clear the value of
 	 */
 	protected void clear(StyleAttribute<?> attr) {
 		clear(attr, null);
 	}
 
 	/**
 	 * @see MutableConditionalStyle#clear(StyleAttribute, StyleExpression) Implemented to make extensions of this class easily support
 	 *      MutableConditionalStyle
 	 * @param attr The attribute to clear the value of
 	 * @param exp The state expression to clear the value for
 	 */
 	protected void clear(StyleAttribute<?> attr, E exp) {
 		StyleValueHolder<E, ?> holder = theAttributes.get(attr);
 		if(holder != null && holder.remove(exp))
 			styleChanged(attr, exp, null);
 		if(holder.size() == 0)
 			theAttributes.remove(attr);
 	}
 
 	/**
 	 * @param attr The attribute for which a style value has changed
 	 * @param expr The state expression for which a style value has changed
 	 * @param root The stateful style in which the style expression changed--may be this style or one of its dependencies or their
 	 *            dependencies, etc.
 	 */
 	protected void styleChanged(StyleAttribute<?> attr, E expr, S root) {
 		StyleExpressionEvent<S, E, ?> newEvent = new StyleExpressionEvent<S, E, Object>(root == null ? (S) this : root, (S) this,
 			(StyleAttribute<Object>) attr, expr);
 		for(StyleExpressionListener<S, E> listener : theListeners)
 			listener.eventOccurred(newEvent);
 	}
 
 	@Override
 	public SimpleConditionalStyle<S, E> clone() {
 		SimpleConditionalStyle<S, E> ret;
 		try {
 			ret = (SimpleConditionalStyle<S, E>) super.clone();
 		} catch(CloneNotSupportedException e) {
 			throw new IllegalStateException(e);
 		}
 		ret.theAttributes = new java.util.concurrent.ConcurrentHashMap<>();
 		for(java.util.Map.Entry<StyleAttribute<?>, StyleValueHolder<E, ?>> entry : theAttributes.entrySet()) {
 			ret.theAttributes.put(entry.getKey(), entry.getValue().clone());
 		}
 		ret.theListeners = new java.util.concurrent.ConcurrentLinkedQueue<>();
 		// Don't add the listeners--ret is a new style
 		return ret;
 	}
 
 	/** An iterator for style attributes in a style and its dependencies */
 	protected static class AttributeIterator implements java.util.Iterator<StyleAttribute<?>> {
 		private Iterator<StyleAttribute<?>> theLocalAttribs;
 
 		private final Iterator<StyleAttribute<?>> [] theDependencies;
 
 		private int childIndex;
 
 		private boolean calledNext;
 
 		/**
 		 * @param style The style for local attributes
 		 * @param dependencies The set of the style's dependencies
 		 */
 		public AttributeIterator(MuisStyle style, MuisStyle... dependencies) {
 			theLocalAttribs = style.localAttributes().iterator();
 			theDependencies = new java.util.Iterator[dependencies.length];
 			for(int d = 0; d < dependencies.length; d++)
 				theDependencies[d] = dependencies[d].iterator();
 			childIndex = -1;
 		}
 
 		@Override
 		public boolean hasNext() {
 			calledNext = false;
 			if(theLocalAttribs != null) {
 				if(theLocalAttribs.hasNext())
 					return true;
 				else
 					theLocalAttribs = null;
 			}
 			if(childIndex < 0)
 				childIndex = 0;
 			while(childIndex < theDependencies.length && !theDependencies[childIndex].hasNext())
 				childIndex++;
 			return childIndex < theDependencies.length;
 		}
 
 		@Override
 		public StyleAttribute<?> next() {
 			if((calledNext && !hasNext()) || childIndex >= theDependencies.length)
 				throw new java.util.NoSuchElementException();
 			calledNext = true;
 			if(theLocalAttribs != null)
 				return theLocalAttribs.next();
 			return theDependencies[childIndex].next();
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("Style iterators are immutable");
 		}
 	}
 }
