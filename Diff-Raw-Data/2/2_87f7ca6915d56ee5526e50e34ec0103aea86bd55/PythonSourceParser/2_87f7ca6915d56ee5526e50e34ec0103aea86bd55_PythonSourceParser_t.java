 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.internal.core.parser;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.Token;
 import org.antlr.runtime.TokenStream;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.parser.AbstractSourceParser;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.python.internal.core.parsers.DLTKPythonErrorReporter;
 import org.eclipse.dltk.python.internal.core.parsers.DLTKTokenConverter;
 import org.eclipse.dltk.python.internal.core.parsers.PythonTokenStream;
 import org.eclipse.dltk.python.internal.core.parsers.python_v3Lexer;
 import org.eclipse.dltk.python.internal.core.parsers.python_v3Parser;
 import org.eclipse.dltk.python.parser.ast.PythonModuleDeclaration;
 
 public class PythonSourceParser extends AbstractSourceParser {
 	private TokenStream fTokenStream;
 	private IProblemReporter problemReporter = null;
 
 	public PythonSourceParser(/* IProblemReporter reporter */) {
 // this.problemReporter = reporter;
 	}
 
 	public static class MyLexer extends python_v3Lexer {
 		public MyLexer(CharStream lexer) {
 			super(lexer);
 		}
 
 		public Token nextToken() {
 			startPos = getCharPositionInLine();
 			return super.nextToken();
 		}
 	}
 
 	/**
 	 * Parses selected context to module declaration using python parser.
 	 * 
 	 */
 	public ModuleDeclaration parse(char[] fileName, char[] content0, IProblemReporter reporter) {// throws
 		this.problemReporter = reporter;
 		
 		PythonModuleDeclaration moduleDeclaration = new PythonModuleDeclaration(
 				content0.length, true);
 
 		CharStream st = new ANTLRStringStream(new String(content0));
 		python_v3Lexer pythonLexer = new MyLexer(st);
 
 		CommonTokenStream tokens = new CommonTokenStream(pythonLexer);
 		tokens.discardOffChannelTokens(true);
 		PythonTokenStream indentedSource = new PythonTokenStream(tokens);
 		tokens = new CommonTokenStream(indentedSource);
 		this.fTokenStream = tokens;
 
 		python_v3Parser pythonParser = new python_v3Parser(this.fTokenStream);
 		pythonParser.decl = moduleDeclaration;
 		pythonParser.length = content0.length;
 		pythonParser.converter = new DLTKTokenConverter(content0);
 		pythonParser.reporter = new DLTKPythonErrorReporter(pythonParser.converter, problemReporter, pythonParser);
 
 		try {
 			pythonParser.file_input();
 		} catch (Throwable e) {
 			if (DLTKCore.DEBUG_PARSER) {
 				e.printStackTrace();
 			}
 		}
 		moduleDeclaration.rebuild();
 
 		return moduleDeclaration;
 	}
 }
