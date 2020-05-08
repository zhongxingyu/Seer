 package org.cwi.waebric.parser;
 
 import java.util.List;
 
 import org.cwi.waebric.WaebricKeyword;
 import org.cwi.waebric.WaebricSymbol;
 import org.cwi.waebric.parser.ast.basic.IdCon;
 import org.cwi.waebric.parser.ast.functions.FunctionDef;
 import org.cwi.waebric.parser.ast.module.Import;
 import org.cwi.waebric.parser.ast.module.Module;
 import org.cwi.waebric.parser.ast.module.ModuleId;
 import org.cwi.waebric.parser.ast.module.Modules;
 import org.cwi.waebric.parser.ast.site.Site;
 import org.cwi.waebric.parser.exception.ParserException;
 import org.cwi.waebric.scanner.token.Token;
 import org.cwi.waebric.scanner.token.TokenIterator;
 import org.cwi.waebric.scanner.token.TokenSort;
 
 /**
  * Module parser
  * 
  * @author Jeroen van Schagen
  * @date 20-05-2009
  */
 public class ModuleParser extends AbstractParser {
 
 	private final SiteParser siteParser;
 	private final FunctionParser functionParser;
 	
 	public ModuleParser(TokenIterator tokens, List<ParserException> exceptions) {
 		super(tokens, exceptions);
 		
 		// Initialise sub parsers
 		siteParser = new SiteParser(tokens, exceptions);
 		functionParser = new FunctionParser(tokens, exceptions);
 	}
 	
 	public void visit(Modules modules) {
 		while(tokens.hasNext()) {
 			current = tokens.next();
 			if(WaebricParser.isKeyword(current, WaebricKeyword.MODULE)) {
 				Module module = new Module();
 				visit(module);
 				modules.add(module);
 			} else {
 				exceptions.add(new ParserException(
 						current.toString() + " is placed outside module."));
 			}
 		}
 	}
 	
 	public void visit(Module module) {
 		// Module identifier
 		ModuleId identifier = new ModuleId();
 		visit(identifier);
 		module.setIdentifier(identifier);
 		
 		// Module elements
 		while(tokens.hasNext()) {
 			if(WaebricParser.isKeyword(tokens.peek(1), WaebricKeyword.MODULE)) { 
 				break; // Break back to modules visit, so no exceptions are thrown
 			}
 			
 			// Delegate to element visitors
 			current = tokens.next();
 			if(current.getLexeme() == WaebricKeyword.IMPORT) {
 				Import imprt = new Import();
 				visit(imprt);
 				module.addElement(imprt);
 			} else if(current.getLexeme() == WaebricKeyword.SITE) {
 				Site site = new Site();
 				visit(site);
 				module.addElement(site);
 			} else if(current.getLexeme() == WaebricKeyword.DEF) {
 				FunctionDef def = new FunctionDef();
 				visit(def);
 				module.addElement(def);
 			} else {
 				exceptions.add(new ParserException(current.toString() + " is not a valid module keyword, " +
 						"expected \"import\", \"site\" or \"def\"."));
 			}
 		}	
 	}
 	
 	public void visit(ModuleId moduleId) {
		if(! tokens.hasNext()) {
 			exceptions.add(new ParserException(current.toString() + " has no module identifier."));
 			return;
 		}
 		
 		current = tokens.next();
 		if(current.getSort() == TokenSort.IDENTIFIER) {
 			String[] identifiers = current.getLexeme().toString().split("\\" + WaebricSymbol.PERIOD);
 			for(String identifier: identifiers) {
 				moduleId.addIdentifierElement(new IdCon(identifier));
 			}
 		} else {
 			exceptions.add(new ParserException(current.toString() + " is not a module identifier," +
 					"use words separated by periods."));
 			return;
 		}
 	}
 	
 	public void visit(Import imprt) {
 		if(! tokens.hasNext()) {
 			exceptions.add(new ParserException(current.toString() + " has no module identifier."));
 			return;
 		}
 		
 		Token peek = tokens.peek(1);
 		if(peek.getSort() == TokenSort.IDENTIFIER) {
 			ModuleId identifier = new ModuleId();
 			visit(identifier);
 			imprt.setIdentifier(identifier);
 		} else {
 			exceptions.add(new ParserException(peek.toString() + " is not a module identifier."));
 			return;
 		}
 	}
 	
 	/**
 	 * @see org.cwi.waebric.parser.SiteParser
 	 * @param site
 	 */
 	public void visit(Site site) {
 		siteParser.visit(site);
 	}
 	
 	/**
 	 * org.cwi.waebric.parser.FunctionParser
 	 * @param def
 	 */
 	public void visit(FunctionDef def) {
 		functionParser.visit(def);
 	}
 
 }
