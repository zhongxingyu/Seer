 package org.oobium.build.esp.parser.internal.parsers;
 
 import static org.oobium.build.esp.parser.internal.parsers.Scanner.EOE;
 import static org.oobium.build.esp.parser.internal.parsers.Scanner.EOL;
 
 import org.oobium.build.esp.dom.EspDom.DocType;
 import org.oobium.build.esp.dom.EspPart.Type;
 import org.oobium.build.esp.dom.elements.StyleElement;
 import org.oobium.build.esp.dom.parts.StylePart;
 import org.oobium.build.esp.dom.parts.style.Declaration;
 import org.oobium.build.esp.dom.parts.style.Property;
 import org.oobium.build.esp.dom.parts.style.Ruleset;
 import org.oobium.build.esp.dom.parts.style.Selector;
 import org.oobium.build.esp.dom.parts.style.SelectorGroup;
 import org.oobium.build.esp.parser.exceptions.EspEndException;
 import org.oobium.build.esp.parser.exceptions.EspException;
 
 public class StyleBuilder extends MarkupBuilder {
 
 	public StyleBuilder(Scanner scanner) {
 		super(scanner);
 	}
 
 	private void parseChild(Declaration declaration) throws EspEndException {
 		switch(scanner.scanForAny(';', '{', '(', ':', '}', EOL)) {
 		case '{':
 			parseRuleset(declaration);
 			break;
 		case ';': case '(': case ':':
 			parseProperty(declaration);
 			break;
		case EOL: case EOE:
 			if(scanner.hasDeclaration()) {
 				parseRuleset(declaration);
 			} else {
 				parseProperty(declaration);
 			}
 		}
 	}
 	
 	private void parseDeclaration(Ruleset rule) throws EspEndException {
 		Declaration declaration = scanner.push(new Declaration());
 		rule.setDeclaration(declaration);
 
 		try {
 			if(scanner.isChar('{')) {
 				scanner.setContainmentToCloser();
 				scanner.next();
 			} else {
 				scanner.setContainmentToEOE();
 			}
 			while(true) {
 				if(scanner.isChar(';')) {
 					scanner.next();
 				}
 				scanner.forward();
 				if(scanner.isChar('&',':')) {
 					parseRuleset(declaration);
 				}
 				else {
 					parseChild(declaration);
 				}
 			}
 		} catch(EspEndException e) {
 			if(scanner.isChar('}')) {
 				scanner.handleContainmentEnd();
 				scanner.move(1);
 				scanner.pop(declaration);
 				scanner.forward();
 			} else {
 				scanner.pop(declaration, e.getOffset());
 			}
 		}
 	}
 	
 	private void parseParametricRuleset(StylePart style) throws EspEndException {
 		Ruleset rule = scanner.push(new Ruleset());
 		style.addRuleset(rule);
 		scanner.setContainmentToEOL();
 		try {
 			if(scanner.isChar('&')) {
 				rule.setMerged(true);
 				scanner.next();
 			}
 
 			parseSelectorGroup(rule);
 			scanner.find('(');
 			
 			parseMethodSigArgs(rule);
 			scanner.find('{');
 			
 		} catch(EspEndException e) {
 			scanner.handleContainmentEnd();
 		}
 		scanner.popTo(rule);
 		
 		scanner.setContainmentToEOE();
 		try {
 			parseDeclaration(rule);
 		} catch(EspException e) {
 			scanner.pop(rule, e.getOffset());
 		}
 	}
 
 	private void parseProperty(Declaration declaration) throws EspEndException {
 		Property property = scanner.push(new Property());
 		declaration.addProperty(property);
 		
 		scanner.setContainmentToEOL();
 		try {
 			scanner.forward();
 			property.setName(scanner.push(Type.StylePropertyName));
 			scanner.findEndOfStylePropertyName();
 			scanner.pop(property.getName());
 
 			scanner.forward();
 			if(scanner.isChar('(')) {
 				parseArgsAndEntries(property);
 			}
 			
 			scanner.forward();
 			if(scanner.isChar(':')) {
 				scanner.next().forward();
 				property.setValue(scanner.push(Type.StylePropertyValue));
 				try {
 					scanner.find(';');
 				} catch(EspEndException e) {
 					scanner.pop(property.getValue());
 					property.getValue().setEnd(scanner.getTrimmedEndFrom(e.getOffset()));
 					throw e;
 				}
 				scanner.pop(property.getValue(), scanner.getTrimmedEnd());
 			}
 			scanner.pop(property);
 		} catch(EspEndException e) {
 			scanner.pop(property, e.getOffset());
 		}
 	}
 	
 	private void parseRuleset(Declaration declaration) throws EspEndException {
 		Ruleset rule = scanner.push(new Ruleset());
 		declaration.getParent().addNestedRule(rule);
 		parseRuleset(rule);
 	}
 	
 	private void parseRuleset(Ruleset rule) throws EspEndException {
 		scanner.setContainmentToEOL();
 		try {
 			if(scanner.isChar('&')) {
 				rule.setMerged(true);
 				scanner.next();
 			}
 			parseSelectorGroup(rule);
 			scanner.find('{');
 		} catch(EspEndException e) {
 			scanner.handleContainmentEnd();
 		}
 		
 		scanner.setContainmentToEOE();
 		try {
 			parseDeclaration(rule);
 			scanner.pop(rule);
 		} catch(EspException e) {
 			scanner.pop(rule, e.getOffset());
 		}
 	}
 	
 	private void parseRuleset(StylePart style) throws EspEndException {
 		Ruleset rule = scanner.push(new Ruleset());
 		style.addRuleset(rule);
 		parseRuleset(rule);
 	}
 
 	private void parseSelector(SelectorGroup group) throws EspEndException {
 		Selector selector = scanner.push(new Selector());
 		group.addSelector(selector);
 		scanner.setContainmentToEOL();
 		try {
 			scanner.findEndOfStyleSelector();
 			scanner.pop(selector);
 		} catch(EspEndException e) {
 			scanner.pop(selector, e.getOffset());
 		}
 	}
 	
 	private void parseSelectorGroup(Ruleset rule) throws EspEndException {
 		SelectorGroup group = scanner.push(new SelectorGroup());
 		rule.setSelectorGroup(group);
 		scanner.setContainmentToEOL();
 		try {
 			scanner.forward();
 			do { parseSelector(group); }
 			while(scanner.isChar(',') && scanner.next().forward().hasNext());
 			scanner.pop(group);
 		} catch(EspEndException e) {
 			scanner.pop(group, e.getOffset());
 		}
 	}
 	
 	public void parseStyleElement() throws EspEndException {
 		push(new StyleElement(), new BuildRunner<StyleElement>() {
 			public void parse(StyleElement element) throws EspEndException {
 				if(element.getDom().is(DocType.ESP)) {
 					scanner.setContainmentToEOL();
 					try {
 						parseTag(element);
 						parseJavaType(element);
 						parseArgsAndEntries(element);
 					} catch(EspEndException e) {
 						scanner.handleContainmentEnd();
 						scanner.popTo(element);
 					}
 				}
 				parseStylePart(element);
 			}
 		});
 	}
 
 	private void parseStylePart(StyleElement element) throws EspEndException {
 		StylePart style = new StylePart();
 		element.setAsset(scanner.push(style));
 
 		scanner.setContainmentToEOE();
 		while(scanner.hasNext()) {
 			try {
 				scanner.forward();
 				if(scanner.isNextParametricRuleset()) {
 					parseParametricRuleset(style);
 				} else {
 					parseRuleset(style);
 				}
 				scanner.popTo(style);
 			} catch(EspEndException e) {
 				if(element.getDom().is(DocType.ESP)) {
 					throw e;
 				} else {
 					scanner.handleContainmentEnd();
 					// keep going!
 				}
 			}
 		}
 	}
 	
 }
