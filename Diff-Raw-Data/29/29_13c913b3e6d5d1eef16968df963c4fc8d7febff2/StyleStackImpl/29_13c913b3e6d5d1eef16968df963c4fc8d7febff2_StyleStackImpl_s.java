 package jbookreader.formatengine.style.impl;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 
 import jbookreader.book.INode;
 import jbookreader.book.IStylesheet;
 import jbookreader.formatengine.IStyleConfig;
 import jbookreader.formatengine.IStyleStack;
 import jbookreader.style.Alignment;
 import jbookreader.style.Display;
 import jbookreader.style.FontStyle;
 import jbookreader.style.IStyleRule;
 import jbookreader.style.StyleAttribute;
 
 import static jbookreader.style.StyleAttribute.*;
 
 public class StyleStackImpl implements IStyleStack {
 	private List<IStylesheet> stylesheets = new ArrayList<IStylesheet>();
 	private List<StyleStackState> stateStack = new ArrayList<StyleStackState>();
 	private StyleStackState currentState = new StyleStackState();
 	private IStyleConfig config;
 
 	public StyleStackImpl() {
 		stateStack.add(currentState);
 	}
 	
 	public void setConfig(final IStyleConfig config) {
 		this.config = config;
 	}
 	
 	IStyleConfig getConfig() {
 		return config;
 	}
 
 	public void addStylesheet(final IStylesheet stylesheet) {
 		stylesheets.add(stylesheet);
 	}
 
 	public void push(INode node) {
 		Map<StyleAttribute, IStyleRule> rules =
 			new EnumMap<StyleAttribute, IStyleRule>(
 					StyleAttribute.class);
 		
 		for (IStylesheet stylesheet: stylesheets) {
 			for (IStyleRule rule:
 					stylesheet.getApplicableRules(node)) {
 				StyleAttribute attrib = rule.getAttribute();
 				long weight = rule.getWeight();
 				if (!rules.containsKey(attrib) ||
 					rules.get(attrib).getWeight() <= weight) {
 					rules.put(attrib, rule);
 				}
 			}
 		}
 		
 //		System.out.println(node);
 //		for (Map.Entry<StyleAttribute, IStyleRule> rule : rules.entrySet()) {
 //			System.out.println(rule.getKey() + ": (" + rule.getValue().getValueType() + ") "+ rule.getValue().getValue(Object.class));
 //		}
 		
 		currentState = new StyleStackState(currentState, rules);
 		
 		stateStack.add(currentState);
 	}
 
 	public void pop() {
 		stateStack.remove(currentState);
 		if (stateStack.isEmpty()) {
 			currentState = new StyleStackState();
 			stateStack.add(currentState);
 			System.err.println("style stack became empty!");
 		} else {
 			currentState = stateStack.get(stateStack.size() - 1);
 		}
 	}
 	
 	public Display getDisplay() {
 		return currentState.getAttributeValue(DISPLAY);
 	}
 	
 	public int getWidth() {
		return currentState.getAttributeValue(WIDTH);
 	}
 
 	public Alignment getTextAlign() {
 		return currentState.getAttributeValue(TEXT_ALIGN);
 	}
 
 	public String[] getFontFamily() {
 		return currentState.getAttributeValue(FONT_FAMILY);
 	}
 
 	public int getFontSize() {
		return currentState.getAttributeValue(FONT_SIZE);
 	}
 
 	public int getFontWeight() {
		return currentState.getAttributeValue(FONT_WEIGHT); 
 	}
 
 	public FontStyle getFontStyle() {
 		return currentState.getAttributeValue(FONT_STYLE);
 	}
 
 }
