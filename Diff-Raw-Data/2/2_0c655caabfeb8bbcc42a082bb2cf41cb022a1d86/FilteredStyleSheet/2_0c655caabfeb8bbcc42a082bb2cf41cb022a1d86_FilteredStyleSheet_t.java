 package org.muis.core.style.sheet;
 
 import org.muis.core.MuisElement;
 import org.muis.core.style.StyleAttribute;
 import org.muis.core.style.StyleExpressionEvent;
 import org.muis.core.style.StyleExpressionListener;
 import org.muis.core.style.StyleExpressionValue;
 import org.muis.core.style.stateful.StateExpression;
 import org.muis.core.style.stateful.StatefulStyle;
 
 import prisms.util.ArrayUtils;
 
 /**
  * A stateful style that gets all its style information from a {@link StyleSheet}, filtered by a group name and an element type
  *
  * @param <E> The element type that this style filters by
  */
 public class FilteredStyleSheet<E extends MuisElement> implements StatefulStyle {
 	private final StyleSheet theStyleSheet;
 
 	private final String theGroupName;
 
 	private final Class<E> theType;
 
 	private final java.util.concurrent.ConcurrentLinkedQueue<StyleExpressionListener<StatefulStyle, StateExpression>> theListeners;
 
 	/**
 	 * @param styleSheet The style sheet to get the style information from
 	 * @param groupName The group name to filter by
 	 * @param type The element type to filter by
 	 */
 	public FilteredStyleSheet(StyleSheet styleSheet, String groupName, Class<E> type) {
 		theStyleSheet = styleSheet;
 		theGroupName = groupName;
 		if(type == null)
 			type = (Class<E>) MuisElement.class;
 		theType = type;
 		styleSheet.addListener(new StyleExpressionListener<StyleSheet, StateGroupTypeExpression<?>>() {
 			@Override
 			public void eventOccurred(StyleExpressionEvent<StyleSheet, StateGroupTypeExpression<?>, ?> evt) {
 				if(matchesFilter(evt.getExpression()))
 					styleChanged(evt.getAttribute(), evt.getExpression().getState());
 			}
 		});
 		theListeners = new java.util.concurrent.ConcurrentLinkedQueue<>();
 	}
 
 	/** @return The style sheet that this style gets its style information from */
 	public StyleSheet getStyleSheet() {
 		return theStyleSheet;
 	}
 
 	/** @return The name of the group that this style filters by */
 	public String getGroupName() {
 		return theGroupName;
 	}
 
 	/** @return The element type that this style filters by */
 	public Class<E> getType() {
 		return theType;
 	}
 
 	/**
 	 * @param expr The expression to check
 	 * @return Whether a {@link StateGroupTypeExpression} with the given group name and type matches this filter such that its attribute
 	 *         value will be exposed from this style's {@link StatefulStyle} methods
 	 */
 	public boolean matchesFilter(StateGroupTypeExpression<?> expr) {
 		return ArrayUtils.equals(expr.getGroupName(), theGroupName) && expr.getType() == theType;
 	}
 
 	@Override
 	public StatefulStyle [] getConditionalDependencies() {
 		return new StatefulStyle[0];
 	}
 
 	@Override
 	public Iterable<StyleAttribute<?>> allLocal() {
 		return new Iterable<StyleAttribute<?>>() {
 			@Override
 			public java.util.Iterator<StyleAttribute<?>> iterator() {
 				return ArrayUtils.conditionalIterator(theStyleSheet.allAttrs().iterator(),
 					new ArrayUtils.Accepter<StyleAttribute<?>, StyleAttribute<?>>() {
 						@Override
 						public StyleAttribute<?> accept(StyleAttribute<?> value) {
 							for(StyleExpressionValue<StateGroupTypeExpression<?>, ?> exp : theStyleSheet.getExpressions(value))
 								if(matchesFilter(exp.getExpression()))
 									return value;
 							return null;
 						}
 					}, false);
 			}
 		};
 	}
 
 	@Override
 	public Iterable<StyleAttribute<?>> allAttrs() {
 		return allLocal();
 	}
 
 	@Override
 	public <T> StyleExpressionValue<StateExpression, T> [] getLocalExpressions(StyleAttribute<T> attr) {
		StyleExpressionValue<StateGroupTypeExpression<?>, T> [] exprs = theStyleSheet.getLocalExpressions(attr);
 		java.util.ArrayList<StyleExpressionValue<StateExpression, T>> ret = new java.util.ArrayList<>();
 		for(StyleExpressionValue<StateGroupTypeExpression<?>, T> exp : exprs)
 			if(matchesFilter(exp.getExpression()))
 				ret.add(new StyleExpressionValue<StateExpression, T>(exp.getExpression().getState(), exp.getValue()));
 		return ret.toArray(new StyleExpressionValue[ret.size()]);
 	}
 
 	@Override
 	public <T> StyleExpressionValue<StateExpression, T> [] getExpressions(StyleAttribute<T> attr) {
 		return getLocalExpressions(attr);
 	}
 
 	@Override
 	public void addListener(StyleExpressionListener<StatefulStyle, StateExpression> listener) {
 		if(listener != null)
 			theListeners.add(listener);
 	}
 
 	@Override
 	public void removeListener(StyleExpressionListener<StatefulStyle, StateExpression> listener) {
 		theListeners.remove(listener);
 	}
 
 	void styleChanged(StyleAttribute<?> attr, StateExpression exp) {
 		StyleExpressionEvent<StatefulStyle, StateExpression, ?> evt = new StyleExpressionEvent<StatefulStyle, StateExpression, Object>(
 			this, this, (StyleAttribute<Object>) attr, exp);
 		for(StyleExpressionListener<StatefulStyle, StateExpression> listener : theListeners)
 			listener.eventOccurred(evt);
 	}
 }
