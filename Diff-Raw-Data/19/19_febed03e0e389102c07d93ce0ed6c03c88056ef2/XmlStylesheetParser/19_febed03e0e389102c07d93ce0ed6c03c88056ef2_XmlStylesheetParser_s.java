 package org.muis.core.parser;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdom2.Element;
 import org.muis.core.MuisClassView;
 import org.muis.core.MuisDocument;
 import org.muis.core.MuisElement;
 import org.muis.core.MuisException;
 import org.muis.core.style.StyleAttribute;
 import org.muis.core.style.StyleDomain;
 import org.muis.core.style.sheet.AnimatedStyleSheet;
 import org.muis.core.style.sheet.ParsedStyleSheet;
 import org.muis.core.style.sheet.StateGroupTypeExpression;
 import org.muis.core.style.stateful.StateExpression;
 
 import prisms.arch.PrismsConfig;
 import prisms.lang.*;
 
 /** Parses instances of {@link ParsedStyleSheet} from XML documents */
 public class XmlStylesheetParser {
 	public static class ParsedStyleAssign extends ParsedItem {
 		private String theNamespace;
 
 		private String theDomain;
 
 		private String theAttrName;
 
 		private ParsedItem theValue;
 
 		@Override
 		public void setup(PrismsParser parser, ParsedItem parent, ParseMatch match) throws ParseException {
 			super.setup(parser, parent, match);
 			theNamespace = getStored("namespace") == null ? null : getStored("namespace").text;
 			theDomain = getStored("domain").text;
 			theAttrName = getStored("attr").text;
 			theValue = parser.parseStructures(this, getStored("value"))[0];
 		}
 
 		public String getNamespace() {
 			return theNamespace;
 		}
 
 		public String getDomain() {
 			return theDomain;
 		}
 
 		public String getAttribute() {
 			return theAttrName;
 		}
 
 		public ParsedItem getValue() {
 			return theValue;
 		}
 
 		@Override
 		public ParsedItem [] getDependents() {
 			return new ParsedItem[] {theValue};
 		}
 
 		@Override
 		public void replace(ParsedItem dependent, ParsedItem toReplace) throws IllegalArgumentException {
 			if(theValue == dependent)
 				theValue = toReplace;
 			throw new IllegalArgumentException("No such dependent " + dependent);
 		}
 
 		@Override
 		public EvaluationResult evaluate(EvaluationEnvironment env, boolean asType, boolean withValues) throws EvaluationException {
 			return null;
 		}
 	}
 
 	public static class ParsedStyleDomainAssign extends ParsedItem {
 		private String theNamespace;
 
 		private String theDomain;
 
 		private String [] theAttrNames;
 
 		private ParsedItem [] theValues;
 
 		@Override
 		public void setup(PrismsParser parser, ParsedItem parent, ParseMatch match) throws ParseException {
 			super.setup(parser, parent, match);
 			theNamespace = getStored("namespace") == null ? null : getStored("namespace").text;
 			theDomain = getStored("domain").text;
 			ArrayList<String> attrNames = new ArrayList<>();
 			ArrayList<ParseMatch> values = new ArrayList<>();
 			for(ParseMatch child : match.getParsed()) {
 				if(child.getParsed() == null)
 					continue;
 				for(ParseMatch grandChild : child.getParsed()) {
 					if("attr".equals(grandChild.config.get("storeAs")))
 						attrNames.add(grandChild.text);
 					if("value".equals(grandChild.config.get("storeAs")))
 						values.add(grandChild);
 				}
 			}
 			theAttrNames = attrNames.toArray(new String[attrNames.size()]);
 			theValues = parser.parseStructures(this, values.toArray(new ParseMatch[values.size()]));
 		}
 
 		public String getNamespace() {
 			return theNamespace;
 		}
 
 		public String getDomain() {
 			return theDomain;
 		}
 
 		public String [] getAttributes() {
 			return theAttrNames;
 		}
 
 		public ParsedItem [] getValues() {
 			return theValues;
 		}
 
 		@Override
 		public ParsedItem [] getDependents() {
 			return theValues;
 		}
 
 		@Override
 		public void replace(ParsedItem dependent, ParsedItem toReplace) throws IllegalArgumentException {
 			for(int i = 0; i < theValues.length; i++)
 				if(theValues[i] == dependent) {
 					theValues[i] = toReplace;
 					return;
 				}
 			throw new IllegalArgumentException("No such dependent " + dependent);
 		}
 
 		@Override
 		public EvaluationResult evaluate(EvaluationEnvironment env, boolean asType, boolean withValues) throws EvaluationException {
 			return null;
 		}
 	}
 
 	private static class ExpressionContext {
 		List<Class<? extends MuisElement>> theTypes;
 
 		List<String> theGroups;
 
 		StateExpression theState;
 
 		ExpressionContext() {
 			theTypes = new ArrayList<>();
 			theGroups = new ArrayList<>();
 		}
 
 		boolean isEmpty() {
 			return theTypes.isEmpty() && theGroups.isEmpty() && theState == null;
 		}
 	}
 
 	private static class ExpressionContextStack implements Iterable<StateGroupTypeExpression<?>> {
 		private List<ExpressionContext> theStack;
 
 		ExpressionContextStack() {
 			theStack = new ArrayList<>();
 		}
 
 		void push() {
 			theStack.add(new ExpressionContext());
 		}
 
 		void pop() {
 			theStack.remove(theStack.size() - 1);
 		}
 
 		ExpressionContext top() {
 			return theStack.get(theStack.size() - 1);
 		}
 
 		void addType(Class<? extends MuisElement> type) throws MuisParseException {
 			for(int i = theStack.size() - 1; i >= 0; i--) {
 				if(!theStack.get(i).theTypes.isEmpty()) {
 					boolean isSubType = false;
 					for(Class<?> preType : theStack.get(i).theTypes) {
 						if(preType.equals(type))
 							throw new MuisParseException("Type " + type.getSimpleName() + " is already in this category");
 						if(preType.isAssignableFrom(type)) {
 							isSubType = true;
 							break;
 						}
 					}
 					if(!isSubType) {
 						throw new MuisParseException("Type " + type.getSimpleName() + " is not a sub type of a type in a super-category");
 					}
 				}
 			}
 			top().theTypes.add(type);
 		}
 
 		void addGroup(String groupName) throws MuisParseException {
 			for(int i = 0; i < theStack.size() - 1; i++)
 				if(!theStack.get(i).theGroups.isEmpty())
 					throw new MuisParseException(
 						"Groups are not hierarchical--a category with a group cannot contain a category with a group");
 			top().theGroups.add(groupName);
 		}
 
 		void setState(StateExpression state) {
 			top().theState = state;
 		}
 
 		@Override
 		public java.util.Iterator<StateGroupTypeExpression<?>> iterator() {
 			if(top().isEmpty()) {
 				return new java.util.Iterator<StateGroupTypeExpression<?>>() {
 					@Override
 					public boolean hasNext() {
 						return false;
 					}
 
 					@Override
 					public StateGroupTypeExpression<?> next() {
 						return null;
 					}
 
 					@Override
 					public void remove() {
 					}
 				};
 			}
 			return new java.util.Iterator<StateGroupTypeExpression<?>>() {
 				private List<Class<? extends MuisElement>> theIterableTypes;
 
 				private List<String> theIterableGroups;
 
 				private StateExpression theOverallState;
 
 				private int theTypeIdx;
 
 				private int theGroupIdx;
 
 				private boolean hasCalledNext;
 
 				{
 					theIterableTypes = new ArrayList<>();
 					theIterableGroups = new ArrayList<>();
 					for(int i = theStack.size() - 1; i >= 0; i--) {
 						if(theIterableTypes.isEmpty())
 							theIterableTypes.addAll(theStack.get(i).theTypes);
 						if(theIterableGroups.isEmpty())
 							theIterableGroups.addAll(theStack.get(i).theGroups);
 					}
 					for(ExpressionContext ctx : theStack) {
 						if(ctx.theState != null) {
 							if(theOverallState == null)
 								theOverallState = ctx.theState;
 							else
 								theOverallState = theOverallState.and(ctx.theState);
 						}
 					}
 				}
 
 				@Override
 				public boolean hasNext() {
 					return !hasCalledNext || theTypeIdx < top().theTypes.size() || theGroupIdx < top().theGroups.size();
 				}
 
 				@Override
 				public StateGroupTypeExpression<?> next() {
 					StateGroupTypeExpression<?> ret;
 					if(theGroupIdx < theIterableGroups.size()) {
 						if(theTypeIdx < theIterableTypes.size()) {
 							ret = new StateGroupTypeExpression<>(theOverallState, theIterableGroups.get(theGroupIdx),
 								theIterableTypes.get(theTypeIdx++));
 						} else {
 							ret = new StateGroupTypeExpression<>(theOverallState, theIterableGroups.get(theGroupIdx), null);
 						}
 						if(theTypeIdx >= theIterableTypes.size()) {
 							theGroupIdx++;
 							theTypeIdx = 0;
 						}
 					} else if(theIterableGroups.isEmpty()) {
 						if(theTypeIdx < theIterableTypes.size()) {
 							ret = new StateGroupTypeExpression<>(theOverallState, null, theIterableTypes.get(theTypeIdx++));
 						} else if(theIterableTypes.isEmpty()) {
 							ret = new StateGroupTypeExpression<>(theOverallState, null, null);
 						} else
 							throw new java.util.NoSuchElementException();
 					} else
 						throw new java.util.NoSuchElementException();
 					hasCalledNext = true;
 					return ret;
 				}
 
 				@Override
 				public void remove() {
 					throw new UnsupportedOperationException();
 				}
 			};
 		}
 	}
 
 	private prisms.lang.PrismsParser theExpressionParser;
 
 	private prisms.lang.EvaluationEnvironment theEnv;
 
 	public XmlStylesheetParser() {
 		theExpressionParser = new PrismsParser();
 		try {
 			theExpressionParser.configure(getStyleSheetDefConfig());
 		} catch(java.io.IOException | MuisParseException e) {
 			throw new IllegalStateException("Could not configure style sheet expression parser", e);
 		}
 		theEnv = new DefaultEvaluationEnvironment();
 	}
 
 	private static PrismsConfig getStyleSheetDefConfig() throws IOException, MuisParseException {
 		return getConfig(getStyleSheetDefXml());
 	}
 
 	private static PrismsConfig getConfig(Element element) {
 		return new PrismsConfig.DefaultPrismsConfig(element.getName(), element.getTextNormalize(), getSubConfigs(element));
 	}
 
 	private static PrismsConfig [] getSubConfigs(Element element) {
 		ArrayList<PrismsConfig> ret = new ArrayList<>();
 		for(org.jdom2.Attribute att : element.getAttributes()) {
 			ret.add(new PrismsConfig.DefaultPrismsConfig(att.getName(), att.getValue(), new PrismsConfig[0]));
 		}
 		for(Element el : element.getChildren()) {
 			ret.add(getConfig(el));
 		}
 		return ret.toArray(new PrismsConfig[ret.size()]);
 	}
 
 	private static Element getStyleSheetDefXml() throws IOException, MuisParseException {
 		try {
 			return new org.jdom2.input.SAXBuilder().build(
 				new java.io.InputStreamReader(DefaultStyleSheetParser.class.getResourceAsStream("MSS Expression Grammar.xml")))
 				.getRootElement();
 		} catch(org.jdom2.JDOMException e) {
 			throw new MuisParseException("Could not parse MSS Expession Grammar.xml", e);
 		}
 	}
 
 	/**
 	 * Parses a style sheet from an XML document
 	 *
 	 * @param location The location to get the document from
 	 * @param doc The MUIS document which refers (directly or indirectly) to the style sheet
 	 * @return The style sheet parsed from the XML resource
 	 * @throws java.io.IOException If an error occurs reading the resource
 	 * @throws MuisParseException If an error occurs parsing the XML or interpreting it as a style sheet
 	 */
 	public ParsedStyleSheet parse(java.net.URL location, MuisDocument doc) throws java.io.IOException, MuisParseException {
 		Element rootEl;
 		try {
 			rootEl = new org.jdom2.input.SAXBuilder().build(new java.io.InputStreamReader(location.openStream())).getRootElement();
 		} catch(org.jdom2.JDOMException e) {
 			throw new MuisParseException("Could not parse style sheet XML for " + location, e);
 		}
 		if(!rootEl.getName().equals("style-sheet"))
 			throw new MuisParseException("style-sheet element expected for root of " + location);
 		ParsedStyleSheet ret = parse(rootEl, doc, doc.getClassView());
 		ret.setLocation(location);
 		return ret;
 	}
 
 	/**
 	 * @param element The XML element to parse as a style sheet
 	 * @param doc The document that the style sheet is to be parsed for
 	 * @param classView The class view required to access toolkits required by the style sheet
 	 * @return The style sheet represented by the XML
 	 */
 	public ParsedStyleSheet parse(Element element, MuisDocument doc, MuisClassView classView) throws MuisParseException {
 		ParsedStyleSheet ret = new ParsedStyleSheet();
 
 		// Parse animation
 		animLoop: for(Element animEl : element.getChildren("animate")) {
 			String varName = animEl.getAttributeValue("variable");
 			if(varName == null) {
 				doc.msg().error("No \"variable\" attribute found on animate element of style sheet", "element", animEl);
 				continue animLoop;
 			}
 			double initValue;
 			try {
 				initValue = Double.parseDouble(animEl.getAttributeValue("init"));
 			} catch(NullPointerException e) {
 				doc.msg().error("No \"init\" attribute found on animate element of style sheet", "element", animEl);
 				continue animLoop;
 			} catch(NumberFormatException e) {
 				doc.msg().error("\"init\" attribute \"" + animEl.getAttributeValue("init") + "\" on animate element is not a valid number",
 					"element", animEl);
 				continue animLoop;
 			}
 			List<Element> advances = animEl.getChildren("advance");
 			AnimatedStyleSheet.AnimationSegment[] segments = new AnimatedStyleSheet.AnimationSegment[advances.size()];
 			for(int i = 0; i < segments.length; i++) {
 				double endValue;
 				try {
 					endValue = Double.parseDouble(advances.get(i).getAttributeValue("to"));
 				} catch(NullPointerException e) {
 					doc.msg().error("No \"to\" attribute found on advance element in animation of style sheet", "element", animEl);
 					continue animLoop;
 				} catch(NumberFormatException e) {
 					doc.msg().error(
 						"\"to\" attribute \"" + advances.get(i).getAttributeValue("to") + "\" on advance element is not a valid number",
 						"element", animEl);
 					continue animLoop;
 				}
 				long duration;
 				try {
 					duration = Math.round(Double.parseDouble(advances.get(i).getAttributeValue("duration")) * 1000);
 				} catch(NullPointerException e) {
 					doc.msg()
 						.error("No \"duration\" attribute found on advance element inside animation of style sheet", "element", animEl);
 					continue animLoop;
 				} catch(NumberFormatException e) {
 					doc.msg().error(
 						"\"duration\" attribute \"" + advances.get(i).getAttributeValue("duration")
 							+ "\" on advance element is not a valid number", "element", animEl);
 					continue animLoop;
 				}
 				if(duration < 0) {
 					doc.msg().error(
 						"\"duration\" attribute \"" + advances.get(i).getAttributeValue("duration")
 							+ "\" on advance element may not be negative", "element", animEl);
 					continue animLoop;
 				}
 				segments[i] = new AnimatedStyleSheet.AnimationSegment(endValue, duration);
 			}
 			ret.addVariable(new AnimatedStyleSheet.AnimatedVariable(varName, initValue, segments, "false".equals(animEl
 				.getAttributeValue("repeat"))));
 		}
 
 		parseStyleAssignments(ret, element, null, doc, classView);
 		ExpressionContextStack stack = new ExpressionContextStack();
 		for(Element category : element.getChildren("category")) {
 			try {
 				parseCategory(ret, category, stack, doc, classView);
 			} catch(MuisParseException e) {
 				doc.msg().error("Could not parse style sheet category", e, "element", category);
 			}
 		}
 		return ret;
 	}
 
 	protected void parseStyleAssignments(ParsedStyleSheet ret, Element element, StateGroupTypeExpression<?> expr, MuisDocument doc,
 		MuisClassView classView) {
 		for(org.jdom2.Content content : element.getContent()) {
 			if(content instanceof org.jdom2.Text) {
 				String value = ((org.jdom2.Text) content).getText();
 				if(value.trim().length() == 0)
 					continue;
 				ParsedItem [] items;
 				try {
 					items = theExpressionParser.parseStructures(new ParseStructRoot(value), theExpressionParser.parseMatches(value));
 				} catch(ParseException e) {
 					doc.msg().error("Could not parse style expressions: " + value.trim(), e);
 					continue;
 				}
 				for(ParsedItem item : items) {
 					if(item instanceof ParsedStyleAssign) {
 						try {
 							apply(ret, expr, (ParsedStyleAssign) item, doc, classView);
 						} catch(MuisException e) {
 							doc.msg().error("Could not apply given style expression", e, "assignment", item);
 						}
 					} else if(item instanceof ParsedStyleDomainAssign) {
 						try {
 							apply(ret, expr, (ParsedStyleDomainAssign) item, doc, classView);
 						} catch(MuisException e) {
 							doc.msg().error("Could not apply given style expression", e, "assignment", item);
 						}
 					} else {
 						doc.msg().error("Style assignment expected, not " + item.getClass().getSimpleName());
 						continue;
 					}
 				}
 			}
 		}
 	}
 
 	protected void parseCategory(ParsedStyleSheet ret, Element category, ExpressionContextStack stack, MuisDocument doc,
 		MuisClassView classView) throws MuisParseException {
 		stack.push();
 		String groupAttr = category.getAttributeValue("group");
 		if(groupAttr != null)
 			for(String groupName : groupAttr.split(","))
 				stack.addGroup(groupName);
 		String typeAttr = category.getAttributeValue("type");
 		if(typeAttr != null) {
 			for(String typeName : typeAttr.split(","))
 				try {
 					stack.addType(classView.loadMappedClass(typeName, MuisElement.class));
 				} catch(MuisException e) {
 					throw new MuisParseException("Could not load element type " + typeName, e);
 				}
 		}
 		String stateAttr = category.getAttributeValue("state");
 		if(stateAttr != null) {
 			try {
 				stack.setState(parseState(stateAttr));
 			} catch(MuisParseException e) {
 				throw new MuisParseException("Could not parse state expression " + stateAttr, e);
 			}
 		}
 		for(StateGroupTypeExpression<?> expr : stack)
 			parseStyleAssignments(ret, category, expr, doc, classView);
 		for(Element subCategory : category.getChildren("category")) {
 			try {
 				parseCategory(ret, subCategory, stack, doc, classView);
 			} catch(MuisParseException e) {
 				doc.msg().error("Could not parse style sheet category", e, "element", subCategory);
 			}
 		}
 		stack.pop();
 	}
 
 	private StateExpression parseState(String state) throws MuisParseException {
 		StringBuilder sb = new StringBuilder(state);
 		StateExpression ret = parseNextState(sb);
 		while(sb.length() > 0) {
 			char ch = sb.charAt(0);
 			sb.deleteCharAt(0);
 			if(Character.isWhitespace(ch))
 				continue;
 			else if(ch == '&')
 				ret = ret.and(parseNextState(sb));
 			else if(ch == '|')
 				ret = ret.or(parseNextState(sb));
 			ret = parseNextState(sb);
 
 		}
 		if(sb.length() > 0)
 			throw new MuisParseException("State expression " + state + " could not be parsed");
 		return ret;
 	}
 
 	private StateExpression parseNextState(StringBuilder state) throws MuisParseException {
 		StringBuilder currentState = new StringBuilder();
 		while(true) {
 			char ch = state.length() > 0 ? state.charAt(0) : '\'';
 			if(ch == '-') {
 				if(currentState.length() > 0)
 					throw new MuisParseException("Unexpected: '" + ch + "'");
 				state.deleteCharAt(0);
 				return parseNextState(state).not();
 			} else if(ch == '(') {
 				if(currentState.length() > 0)
 					throw new MuisParseException("Unexpected: '" + ch + "'");
 				state.deleteCharAt(0);
 				return parseParenthetic(state);
 			} else if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '-') {
 				state.deleteCharAt(0);
 				currentState.append(ch);
 			} else if(currentState.length() > 0) {
 				int todo;// TODO Find priority!
				return new StateExpression.Simple(new org.muis.core.mgr.MuisState(currentState.toString(), 0));
 			} else {
 				throw new MuisParseException("Unexpected: '" + ch + "'");
 			}
 		}
 	}
 
 	private StateExpression parseParenthetic(StringBuilder state) throws MuisParseException {
 		int stack = 1;
 		int i;
 		for(i = 0; i < state.length() && stack > 0; i++) {
 			if(state.charAt(i) == '(')
 				stack++;
 			else if(state.charAt(i) == ')')
 				stack--;
 		}
 		if(stack > 0)
 			throw new MuisParseException("Unclosed '('");
 		String sub = state.substring(0, i);
 		state.deleteCharAt(i + 1);
 		return parseState(sub);
 	}
 
 	void apply(ParsedStyleSheet style, StateGroupTypeExpression<?> expr, ParsedStyleAssign assign, MuisDocument doc, MuisClassView classView)
 		throws MuisException {
 		StyleDomain domain = org.muis.core.style.StyleParsingUtils.getStyleDomain(assign.getNamespace(), assign.getDomain(), classView);
 		for(StyleAttribute<?> attr : domain) {
 			if(attr.getName().equals(assign.getAttribute())) {
 				// TODO Check validity of the value for the attribute. Maybe this should be done in the style sheet, though
 				style.setAnimatedValue(attr, expr, replaceIdentifiersAndStrings(assign.getValue(), style, attr, classView));
 				return;
 			}
 		}
 		doc.msg().error(
 			"No attribute " + assign.getAttribute() + " in domain " + (assign.getNamespace() == null ? "" : assign.getNamespace() + ":")
 				+ assign.getDomain());
 	}
 
 	void apply(ParsedStyleSheet style, StateGroupTypeExpression<?> expr, ParsedStyleDomainAssign assign, MuisDocument doc,
 		MuisClassView classView) throws MuisException {
 		StyleDomain domain = org.muis.core.style.StyleParsingUtils.getStyleDomain(assign.getNamespace(), assign.getDomain(), classView);
 		for(int i = 0; i < assign.getAttributes().length; i++) {
 			for(StyleAttribute<?> attr : domain) {
 				if(attr.getName().equals(assign.getAttributes()[i])) {
 					// TODO Check validity of the value for the attribute. Maybe this should be done in the style sheet, though
 					try {
 						style.setAnimatedValue(attr, expr, replaceIdentifiersAndStrings(assign.getValues()[i], style, attr, classView));
 					} catch(MuisException e) {
 						doc.msg().error("Could not interpret value " + assign.getValues()[i] + " for attribute " + attr, e, "attribute",
 							attr, "value", assign.getValues()[i]);
 					}
 					return;
 				}
 			}
 			doc.msg().error(
 				"No attribute " + assign.getAttributes()[i] + " in domain "
 					+ (assign.getNamespace() == null ? "" : assign.getNamespace() + ":") + assign.getDomain());
 		}
 	}
 
 	ParsedItem replaceIdentifiersAndStrings(ParsedItem value, ParsedStyleSheet style, StyleAttribute<?> attr, MuisClassView classView)
 		throws MuisException {
 		if(value instanceof prisms.lang.types.ParsedIdentifier) {
 			String text = ((prisms.lang.types.ParsedIdentifier) value).getName();
 			for(AnimatedStyleSheet.AnimatedVariable var : style) {
 				if(var.getName().equals(text))
 					return value;
 			}
 			return new org.muis.core.style.sheet.ConstantItem(attr.getType().getType(), attr.getType().parse(classView, text));
 		} else if(value instanceof prisms.lang.types.ParsedString) {
 			String text = ((prisms.lang.types.ParsedString) value).getValue();
 			return new org.muis.core.style.sheet.ConstantItem(attr.getType().getType(), attr.getType().parse(classView, text));
 		} else {
 			for(ParsedItem depend : value.getDependents()) {
 				ParsedItem replace = replaceIdentifiersAndStrings(value, style, attr, classView);
 				if(replace != depend)
 					value.replace(depend, replace);
 			}
 			return value;
 		}
 	}
 }
