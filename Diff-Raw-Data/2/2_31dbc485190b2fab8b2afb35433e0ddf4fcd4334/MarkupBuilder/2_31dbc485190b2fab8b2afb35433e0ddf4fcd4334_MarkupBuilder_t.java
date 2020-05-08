 package org.oobium.build.esp.parser.internal.parsers;
 
 import static org.oobium.build.esp.parser.internal.parsers.Scanner.EOL;
 
 import org.oobium.build.esp.dom.EspPart;
 import org.oobium.build.esp.dom.EspPart.Type;
 import org.oobium.build.esp.dom.common.MethodPart;
 import org.oobium.build.esp.dom.elements.MarkupComment;
 import org.oobium.build.esp.dom.elements.MarkupElement;
 import org.oobium.build.esp.dom.parts.MethodArg;
 import org.oobium.build.esp.parser.exceptions.EspEndException;
 
 public class MarkupBuilder extends Builder {
 
 	public MarkupBuilder(Scanner scanner) {
 		super(scanner);
 	}
 
 	public MarkupComment parseMarkupComment() throws EspEndException {
 		return push(new MarkupComment(), new BuildRunner<MarkupComment>() {
 			public void parse(MarkupComment element) throws EspEndException {
 				element.setTag(scanner.push(Type.MarkupTag));
 				scanner.setContainmentToEOL();
 				try {
 					scanner.move(3);
 					scanner.pop(element.getTag());
 					parseArgsAndEntries(element);
 					parseInnerText(element);
 				} catch(EspEndException e) {
 					scanner.handleContainmentEnd();
 					scanner.popTo(element);
 				}
 				scanner.setContainmentToEOE();
 				scanner.parseChildren();
 			}
 		});
 	}
 	
 	public MarkupElement parseMarkupElement() throws EspEndException {
 		return push(new MarkupElement(), new BuildRunner<MarkupElement>() {
 			public void parse(MarkupElement element) throws EspEndException {
 				scanner.setContainmentToEOE();
 				try {
 					parseTag(element);
 					parseJavaType(element);
 					parseId(element);
 					parseClasses(element);
 					parseStyles(element); // Styles (before arguments)
 					parseArgsAndEntries(element);
 					parseStyles(element); // Styles (after arguments)
 					parseInnerText(element);
 				} catch(EspEndException e) {
 					scanner.handleContainmentEnd();
 					scanner.popTo(element);
 				}
 				scanner.parseChildren();
 			}
 		});
 	}
 	
 	protected void parseTag(MarkupElement element) throws EspEndException {
 		element.setTag(scanner.push(Type.MarkupTag));
 		scanner.setContainmentToEOL();
 		scanner.findEndOfWord();
 		scanner.pop(element.getTag());
 	}
 	
 	protected void parseArgsAndEntries(MethodPart method) throws EspEndException {
 		if(scanner.isChar('(')) {
 			method.initArgs();
 			EspPart args = scanner.push(Type.MethodArgs);
 			scanner.setContainmentToEOL();
 			scanner.setContainmentToCloser();
 			try {
 				while(true) {
 					MethodArg arg = null;
 					try {
 						scanner.next();
 						arg = scanner.push(new MethodArg());
 						
 						if(scanner.isNextEntry()) {
 							scanner.forward();
 							arg.setName(scanner.push(Type.VarName));
 							scanner.findEndOfMarkupAttr();
 							if(scanner.isChar('(')) {
 								scanner.pop(arg.getName());
 								arg.setCondition(scanner.push(Type.JavaContainer));
 								try {
 									 scanner.findCloser();
 								} catch(EspEndException e) {
 									scanner.handleContainmentEnd();
 									scanner.popTo(arg.getCondition());
 									scanner.next();
 									scanner.pop(arg.getCondition());
 								}
 								scanner.find(':');
 							} else {
 								scanner.find(':');
 								scanner.pop(arg.getName());
 							}
 							scanner.move(1);
 						}
 						
 						scanner.forward();
 						arg.setValue(scanner.push(Type.JavaContainer));
 						scanner.findAny(',');
 					} finally {
 						if(arg != null) {
 							method.addArg(arg);
 							scanner.pop(arg);
 						}
 					}
 				}
 			} catch(EspEndException e) {
 				if(scanner.isChar(')')) {
 					scanner.handleContainmentEnd();
 					scanner.next();
 					scanner.pop(args);
 				} else {
 					scanner.pop(args, e.getOffset());
 				}
 			}
 		}
 	}
 
 	protected void parseClasses(MarkupElement element) throws EspEndException {
 		if(scanner.isChar('.')) { // Class(es)
 			scanner.setContainmentToEOL();
 			do {
 				EspPart part = scanner.push(Type.MarkupClass);
 				element.addClass(part);
				scanner.next().findEndOfMarkupAttr();
 				scanner.pop(part);
 			}
 			while(scanner.isChar('.'));
 		}
 	}
 
 	protected void parseId(MarkupElement element) throws EspEndException {
 		if(scanner.isChar('#')) { // ID
 			scanner.setContainmentToEOL();
 			element.setId(scanner.push(Type.MarkupId));
 			scanner.findEndOfMarkupId();
 			scanner.pop(element.getId());
 		}
 	}
 	
 	protected void parseInnerText(MarkupElement element) throws EspEndException {
 		if(scanner.isChar(' ')) { // inner HTML
 			scanner.skip();
 			if(scanner.isChar(EOL)) {
 				return;
 			}
 			element.setInnerText(scanner.push(Type.InnerTextPart));
 			scanner.setContainmentToEOL();
 			try {
 				scanner.findEndOfContainment();
 			} catch(EspEndException e) {
 				scanner.pop(element.getInnerText(), e.getOffset());
 			}
 		}
 	}
 
 	protected void parseJavaType(MarkupElement element) throws EspEndException {
 		if(scanner.isChar('<')) { // Type
 			element.setJavaType(scanner.push(Type.JavaContainer));
 			scanner.setContainmentToEOL();
 			try {
 				scanner.findCloser();
 			} catch(EspEndException e) {
 				if(scanner.isChar('>')) {
 					scanner.handleContainmentEnd();
 					scanner.popTo(element.getJavaType());
 					scanner.next();
 					scanner.pop(element.getJavaType(), scanner.getOffset());
 				} else {
 					scanner.pop(element.getJavaType(), e.getOffset());
 				}
 			}
 		}
 	}
 
 	protected void parseStyles(MarkupElement element) throws EspEndException {
 		if(scanner.isChar('|')) { // Styles (after arguments)
 			scanner.skip();
 			if(scanner.isChar(EOL)) {
 				return;
 			}
 			EspPart part = scanner.push(Type.StylePart);
 			scanner.setContainmentToEOL();
 			if(scanner.isCharSequence('h','i','d','e') && scanner.move(4).isChar('(',' ')) {
 				element.setHidden(true);
 			}
 			scanner.findEndOfWord();
 			scanner.pop(part);
 		}
 	}
 
 }
