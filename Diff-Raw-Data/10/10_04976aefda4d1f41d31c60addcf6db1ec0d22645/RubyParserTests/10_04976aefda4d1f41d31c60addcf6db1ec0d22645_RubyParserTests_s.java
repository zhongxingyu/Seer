 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.core.tests.parser;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.eclipse.dltk.ast.declarations.ISourceParser;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.tests.model.AbstractModelTests;
 import org.eclipse.dltk.ruby.core.RubyNature;
 import org.eclipse.dltk.ruby.core.tests.Activator;
 import org.eclipse.dltk.utils.CorePrinter;
 
 public class RubyParserTests extends AbstractModelTests {
 	public RubyParserTests(String name) {
 		super(Activator.PLUGIN_ID, name);
 	}
 
 	public static Suite suite() {
 		return new Suite(RubyParserTests.class);
 	}
 
 	public void processScript(String name) throws Exception {
 		InputStream input = null;
 		try {
 			input = Activator.getDefault().openResource(name);
 
 			InputStreamReader reader = new InputStreamReader(input);
 			BufferedReader br = new BufferedReader(reader);
 			StringBuffer buffer = new StringBuffer();
 			while (br.ready()) {
 				String l = br.readLine();
 				if (l != null) {
 					buffer.append(l);
 					buffer.append('\n');
 				}
 			}
 			ISourceParser parser = DLTKLanguageManager
 					.getSourceParser(RubyNature.NATURE_ID);
 			ModuleDeclaration module = parser.parse(name.toCharArray(), buffer
 					.toString().toCharArray(), null);
 			assertNotNull(module);
 			assertFalse(module.isEmpty());
 		} finally {
 			if (input != null) {
 				input.close();
 			}
 		}
 	}
 
 	public void testJRubyParser001() throws Exception {
 		processScript("/workspace/parse/test_call.rb");
 	}
 // public void testJRubyParser002() throws Exception {
 // processScript("/workspace/parse/test_iterator.rb");
 // }
 }
