 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.parser;
 
 import java.io.CharArrayReader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.Declaration;
 import org.eclipse.dltk.ast.declarations.ISourceParser;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.ruby.core.RubyPlugin;
 import org.eclipse.dltk.ruby.internal.parsers.jruby.DLTKRubyParser;
 import org.eclipse.dltk.ruby.internal.parsers.jruby.RubyASTBuildVisitor;
 import org.jruby.ast.Node;
 import org.jruby.ast.visitor.NodeVisitor;
 
 public class JRubySourceParser implements IExecutableExtension, ISourceParser {
 
 	private static boolean silentState = true;
 
 	public static boolean isSilentState() {
 		return silentState;
 	}
 
 	/**
 	 * This option allows parser to suppress errors and exceptions and in result
 	 * generate possibly partially non-correct AST instead of failing with
 	 * exception. For running parser tests this option are being set to
 	 * <code>false</code>.
 	 */
 	public static void setSilentState(boolean s) {
 		silentState = s;
 	}
 
 	private final class ASTPositionsCorrector extends ASTVisitor {
 		public boolean visitGeneral (ASTNode node) throws Exception {
 			if (node.sourceStart() < 0 || node.sourceEnd() < 0)
 				return true;
 			int st = 0;
 			int en = 0;
 			int n_st = 0;
 			int n_en = 0;
 			for (Iterator iterator = fixPositions.iterator(); iterator
 					.hasNext();) {
 				Integer pos = (Integer) iterator.next();
 				int fixPos = pos.intValue();
 				// starts
 				if (node.sourceStart() > fixPos) {
 					st++;
 				}
 				if (node.sourceEnd() > fixPos) {
 					en++;
 				}
 				if (node instanceof Declaration) {
 					Declaration declaration = (Declaration) node;
 					if (declaration.getNameStart() > fixPos) {
 						n_st++;
 					}
 					if (declaration.getNameEnd() > fixPos) {
 						n_en++;
 					}
 				}
 			}
 
 			node.setStart(node.sourceStart() - st * magicLength);
 			node.setEnd(node.sourceEnd() - en * magicLength);
 			if (node instanceof Declaration) {
 				Declaration declaration = (Declaration) node;
 				declaration.setNameStart(declaration.getNameStart() - n_st
 						* magicLength);
 				declaration.setNameEnd(declaration.getNameEnd() - n_en
 						* magicLength);
 			}
 //			if (st == 0 && en == 0 && n_st == 0 && n_en == 0)
 //				return false;
 
 			return true;
 		}
 	}
 
 	private static final boolean TRACE_AST_JRUBY = Boolean.valueOf(
 			Platform.getDebugOption("org.eclipse.dltk.core/traceAST/jruby"))
 			.booleanValue();
 
 	private static final boolean TRACE_AST_DLTK = Boolean.valueOf(
 			Platform.getDebugOption("org.eclipse.dltk.core/traceAST/dltk"))
 			.booleanValue();
 
 	private static final Pattern DOT_FIXER = Pattern.compile("\\.(?=\\s|$)");
 	private static final Pattern DOLLAR_FIXER = Pattern.compile("\\$(?=\\s|$)");
 	private static final Pattern AT_FIXER = Pattern.compile("@(?=\\s|$)");
 	private static final Pattern COLON_FIXER = Pattern.compile("::(?=\\s|$)");
 	private static final Pattern INST_BRACK_FIXER = Pattern.compile("@(])");
 	private static final Pattern GLOB_BRACK_FIXER = Pattern.compile("\\$(])");
 	private IProblemReporter problemReporter;
 	private static final String missingName  = "_missing_method_name_";
 	private static final String missingName2 = "NoConstant___________";
 	private static final int magicLength = missingName.length(); // missingName.len should == missingName2.len
 
 	private final List fixPositions = new ArrayList();
 
 	private String fixBrokenThings(Pattern pattern, String content, String replacement, int delta) {
 		Matcher matcher = pattern.matcher(content);
 		StringBuffer result = new StringBuffer();
 		int regionStart = 0;
 		while (matcher.find(regionStart)) {
 			int offset = matcher.start();
 			if (offset > regionStart)
 				result.append(content.subSequence(regionStart, offset));
			fixPositions.add(result.length());
 			result.append(replacement);
 //			fixPositions.add(new Integer(offset + fixPositions.size() * magicLength));			
 			regionStart = offset + delta; //2
 		}
 		if (regionStart < content.length() - 1)
 			result.append(content.subSequence(regionStart, content.length()));
 		if (regionStart == 0)
 			return content; // nothing fixed
 		else
 			return result.toString();
 	}
 	
 	private String fixBrokenDots(String content) {
 		return fixBrokenThings(DOT_FIXER, content, "." + missingName, 1);
 	}
 	
 	private String fixBrokenColons(String content) {
 		return fixBrokenThings(COLON_FIXER, content, "::" + missingName2, 2);
 	}
 	
 	private String fixBrokenDollars(String content) {
 		return fixBrokenThings(DOLLAR_FIXER, content, "$" + missingName, 1);
 	}
 	
 	private String fixBrokenAts(String content) {
 		return fixBrokenThings(AT_FIXER, content, "@" + missingName, 1);
 	}
 	
 	private String fixBrokenInstbracks(String content) {
 		return fixBrokenThings(INST_BRACK_FIXER, content, "@" + missingName, 1);
 	}
 	
 	private String fixBrokenGlobbracks(String content) {
 		return fixBrokenThings(GLOB_BRACK_FIXER, content, "$" + missingName, 1);
 	}
 
 	private final boolean[] errorState = new boolean[1];
 
 	private class ProxyProblemReporter implements IProblemReporter {
 
 		private final IProblemReporter original;
 
 		public ProxyProblemReporter(IProblemReporter original) {
 			super();
 			this.original = original;
 		}
 
 		public IMarker reportProblem(IProblem problem) throws CoreException {
 			IMarker m = null;
 			if (original != null)
 				m = original.reportProblem(problem);
 			if (problem.isError()) {
 				errorState[0] = true;
 			}
 			return m;
 		}
 
 	}
 	
 	public JRubySourceParser() {
 		this.problemReporter = null;
 	}
 
 	/**
 	 * Should return visitor for creating ModuleDeclaration from JRuby's AST
 	 * @param module
 	 * @param content
 	 * @return
 	 */
 	protected NodeVisitor getASTBuilderVisitor(ModuleDeclaration module,
 			char[] content) {
 		return new RubyASTBuildVisitor(module, content);
 	}
 
 	public ModuleDeclaration parse(char[] fileName, char[] content, IProblemReporter reporter) {
 		this.problemReporter = reporter;
 		try {
 			DLTKRubyParser parser = new DLTKRubyParser();
 			ProxyProblemReporter proxyProblemReporter = new ProxyProblemReporter(
 					problemReporter);
 			errorState[0] = false;
 
 			long timeStart = System.currentTimeMillis();
 			Node node = parser.parse("", new CharArrayReader(content),
 					proxyProblemReporter);
 			fixPositions.clear();
 			if (!parser.isSuccess() || errorState[0]) {
 				String content2 = fixBrokenDots(new String( content ) );
 				content2 = fixBrokenColons(content2);
 				content2 = fixBrokenDollars(content2);
 				content2 = fixBrokenAts(content2);
 				content2 = fixBrokenInstbracks(content2);
 				content2 = fixBrokenGlobbracks(content2);
 
 				Node node2 = parser.parse("", new StringReader(content2), null);
 				if (node2 != null)
 					node = node2;
 				else
 					fixPositions.clear();
 				content = content2.toCharArray();
 			}
 
 			ModuleDeclaration module = new ModuleDeclaration(content.length);
 			NodeVisitor visitor = getASTBuilderVisitor(module, content);
 			if (node != null)
 				node.accept(visitor);
 
 			if (node != null) {
 				if (TRACE_AST_JRUBY || TRACE_AST_DLTK)
 					System.out.println("\n\nAST rebuilt\n");
 				if (TRACE_AST_JRUBY)
 					System.out.println("JRuby AST:\n" + node.toString());
 				if (TRACE_AST_DLTK)
 					System.out.println("DLTK AST:\n" + module.toString());
 			}
 
 			if (!fixPositions.isEmpty())
 				try {
 					module.traverse(new ASTPositionsCorrector());
 				} catch (Exception e) {
 					RubyPlugin.log(e);
 				}
 
 			long timeEnd = System.currentTimeMillis();
 			if (TRACE_AST_DLTK)
 				System.out.println("Parsing took " + (timeEnd - timeStart)
 						+ " ms");
 			return module;
 		} catch (Throwable t) {
 			if( DLTKCore.DEBUG ) {
 				t.printStackTrace();
 			}
 			if (isSilentState()) {
 				ModuleDeclaration mdl = new ModuleDeclaration(1);
 				return mdl;
 			}
 			throw new RuntimeException(t);
 		}
 	}
 
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 	}
 
 	public ModuleDeclaration parse(String source) {
 		return this.parse(null, source.toCharArray(), null);
 	}
 
 }
