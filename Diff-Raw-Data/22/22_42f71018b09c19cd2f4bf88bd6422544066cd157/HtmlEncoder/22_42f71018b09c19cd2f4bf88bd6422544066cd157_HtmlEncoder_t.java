 package org.nate.encoder.html;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.nate.Encoder;
 import org.nate.TransformResult;
 
 public class HtmlEncoder implements Encoder {
 
 	private static final String TYPE = "HTML";
 
 	public boolean isNullEncoder() {
 		return false;
 	}
 
 	public String type() {
 		return TYPE;
 	}
 
 	public Object encode(String source) {
 		return new HtmlFragment(source);
 	}
 
 	@SuppressWarnings("unchecked")
 	public TransformResult transformWith(Object template, Object data) {
 		assertType("data", data, Map.class);
 		final HtmlFragment fragment = (HtmlFragment) template;
 		processMapEntries((Map) data, fragment);
 		return new TransformResult() {
 			public String toHtml() {
 				return fragment.toHtml();
 			}
 		};
 	}
 
 	@SuppressWarnings("unchecked")
 	private void processMapEntries(Map map, HtmlFragment fragment) {
 		Set<Map.Entry> entrySet = map.entrySet();
 		for (Map.Entry entry : entrySet) {
 			Object key = entry.getKey();
 			Object value = entry.getValue();
 			assertType("key", key, String.class);
 			applySelector((String) key, value, fragment);
 		}
 	}
 
 	private void applySelector(String selector, Object value, HtmlFragment fragment) {
 		if (value == null) {
 			return;
 		}
 		if (fragment.hasAttribute(selector)) {
 			applySelectorAsAttributeSelector(selector, value, fragment);
 		} else {
 			applySelectorAsCssSelector(selector, value, fragment);
 		}
 	}
 
 	private void applySelectorAsAttributeSelector(String attributeName, Object value, HtmlFragment fragment) {
 		fragment.setAttribute(attributeName, value);
 	}
 
 	private void applySelectorAsCssSelector(String selector, Object value, HtmlFragment fragment) {
 		Set<HtmlFragment> matchingNodes = fragment.selectNodes(selector);
 		for (HtmlFragment matchingNode : matchingNodes) {
 			injectValueIntoFragment(value, matchingNode);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void injectValueIntoFragment(Object value, HtmlFragment fragment) {
		if (value instanceof Iterable) {
 			injectValuesIntoFragment((Iterable) value, fragment);
 		} else if (value instanceof Map) {
 			processMapEntries((Map) value, fragment);
 		} else {
			fragment.setTextContent(value.toString());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void injectValuesIntoFragment(Iterable values, HtmlFragment fragment) {
 		HtmlFragment parentNode = fragment.getParentNode();
 		parentNode.removeChild(fragment);
 		for (Object value : values) {
 			HtmlFragment newFragment = fragment.cloneFragment(true);
 			injectValueIntoFragment(value, newFragment);
 			parentNode.appendChild(newFragment);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void assertType(String description, Object object, Class expectedClass) {
 		String actualClassName = object == null ? null : object.getClass().getName();
 		if (object == null || !(expectedClass.isAssignableFrom(object.getClass()))) {
 			throw new IllegalArgumentException("Expected " + description + " to be a " + expectedClass + ", but got "
 					+ actualClassName + ", with value: " + object);
 		}
 	}
 
 }
