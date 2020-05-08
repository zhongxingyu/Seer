 /**
  * 
  */
 package jbookreader.formatengine.style.impl;
 
 import jbookreader.style.IStyleRule;
 import jbookreader.style.StyleAttribute;
 
 abstract class BasicStyleValueComputer implements IStyleValueComputer {
 
 	public Object compute(StyleAttribute attribute, IStyleRule rule, StyleStackState oldState) {
 		switch (rule.getValueType()) {
 		case INHERIT:
			return oldState.getDefaultValue(attribute);
 		case STRING_ARRAY:
 			return handleStringArray(attribute, rule, oldState);
 		case VALUE:
 			return handleEnumValue(attribute, rule, oldState);
 		case INTEGER:
 			return handleInteger(attribute, rule, oldState);
 		case DIMENSION:
 			return handleDimension(attribute, rule, oldState);
 		default:
 			throw new InternalError("Undandled rule value type: " + rule.getValueType());
 		}
 	}
 	
 	protected Object handleDimension(StyleAttribute attribute, IStyleRule rule, StyleStackState oldState) {
 		throw new IllegalArgumentException(
 				"Bad " + attribute + " rule: " + rule);
 	}
 
 	protected Object handleStringArray(StyleAttribute attribute, IStyleRule rule, StyleStackState oldState) {
 		throw new IllegalArgumentException(
 				"Bad " + attribute + " rule: " + rule);
 	}
 	
 	protected Object handleEnumValue(StyleAttribute attribute, IStyleRule rule, StyleStackState oldState) {
 		throw new IllegalArgumentException(
 				"Bad " + attribute + " rule: " + rule);
 	}
 	
 	protected Object handleInteger(StyleAttribute attribute, IStyleRule rule, StyleStackState oldState) {
 		throw new IllegalArgumentException(
 				"Bad " + attribute + " rule: " + rule);
 	}
}
