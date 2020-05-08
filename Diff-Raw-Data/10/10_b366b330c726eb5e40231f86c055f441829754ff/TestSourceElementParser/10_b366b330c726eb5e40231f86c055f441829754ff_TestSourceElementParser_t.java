 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.tests.model;
 
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 
 public class TestSourceElementParser implements ISourceElementParser {
 
 	private static final String PARSEME_HEADER = "# parseme!\n";
 	private ISourceElementRequestor requestor;
 
 	public TestSourceElementParser() {
 
 	}
 
 // TestSourceElementParser(ISorceElementRequestor requestor) {
 // this.requestor = requestor;
 // }
 
	public void parseSourceModule(char[] contents,
 			ISourceModuleInfo astCashe, char[] filename) {
 		String file = new String(contents);
 		if (file.startsWith(PARSEME_HEADER)) {
 			parsePseudo(file);
			return;
 		}
 		requestor.enterModule();
 		ISourceElementRequestor.TypeInfo ti = new ISourceElementRequestor.TypeInfo();
 		ti.name = "Class1";
 		requestor.enterType(ti);
 		ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 		mi.name = "Method1";
 		requestor.enterMethod(mi);
 		requestor.exitMethod(10);
 		requestor.exitType(10);
 		mi.name = "Procedure1";
 		requestor.enterMethod(mi);
 		requestor.exitMethod(11);
 		requestor.exitModule(20);
		return;
 	}
 
 	/**
 	 * Parses pseudo-model-code File should have \n as the line endings
 	 * 
 	 * @param file
 	 */
 	private void parsePseudo(String file) {
 		if (requestor == null)
 			return;
 		requestor.enterModule();
 		String[] lines = file.split("\n");
 		int currentLineOffset = 0;
 		for (int i = 0; i < lines.length; i++) {
 			String line = lines[i];
 			String[] split = line.split("\\s+");
 			if (split.length > 0) {
 				String cmd = split[0];
 				String arg = (split.length > 1)?split[1]:null;
 				if (cmd.equals("enterType")) {
 					ISourceElementRequestor.TypeInfo ti = new ISourceElementRequestor.TypeInfo();
 					ti.name = arg;
 					ti.declarationStart = currentLineOffset;
 					ti.nameSourceStart = currentLineOffset;
 					ti.nameSourceEnd = currentLineOffset + line.length();
 					requestor.enterType(ti);
 				} else if (cmd.equals("enterMethod")) {
 					ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 					mi.name = arg;
 					mi.declarationStart = currentLineOffset;
 					mi.nameSourceStart = currentLineOffset;
 					mi.nameSourceEnd = currentLineOffset + line.length();
 					requestor.enterMethod(mi);
 				} else if (cmd.equals("exitType")) {
 					requestor.exitType(currentLineOffset + line.length());
 				} else if (cmd.equals("exitMethod")) {
 					requestor.exitMethod(currentLineOffset + line.length());
 				}
 			}
 			currentLineOffset += line.length() + 1;
 		}
 		requestor.exitModule(file.length());
 	}
 
 	public void setRequestor(ISourceElementRequestor requestor) {
 		this.requestor = requestor;
 	}
 
 	public void setReporter(IProblemReporter reporter) {
 	}
 }
