 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 
 package org.eclipse.dltk.javascript.formatter;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.ProblemCollector;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.formatter.AbstractScriptFormatter;
 import org.eclipse.dltk.formatter.FormatterDocument;
 import org.eclipse.dltk.formatter.FormatterIndentDetector;
 import org.eclipse.dltk.formatter.IFormatterContainerNode;
 import org.eclipse.dltk.formatter.IFormatterContext;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.formatter.internal.FormatterNodeBuilder;
 import org.eclipse.dltk.javascript.formatter.internal.JavaScriptFormatterContext;
 import org.eclipse.dltk.javascript.formatter.internal.JavaScriptFormatterWriter;
 import org.eclipse.dltk.javascript.formatter.internal.JavascriptFormatterNodeRewriter;
 import org.eclipse.dltk.javascript.parser.JSProblem;
 import org.eclipse.dltk.javascript.parser.JavaScriptParser;
 import org.eclipse.dltk.ui.formatter.FormatterException;
 import org.eclipse.dltk.ui.formatter.FormatterSyntaxProblemException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.ReplaceEdit;
 import org.eclipse.text.edits.TextEdit;
 
 public class JavaScriptFormatter extends AbstractScriptFormatter {
 
 	private final String lineDelimiter;
 
 	public JavaScriptFormatter(String lineDelimiter,
 			Map<String, ? extends Object> preferences) {
 		super(preferences);
 		this.lineDelimiter = lineDelimiter;
 	}
 
 	public TextEdit format(String source, int offset, int length,
 			int indentationLevel) throws FormatterException {
 
 		String input = source.substring(offset, offset + length);
 
 		String formatted = format(input, indentationLevel);
 
 		if (!input.equals(formatted)) {
 			return new ReplaceEdit(offset, length, formatted);
 		} else {
 			return new MultiTextEdit(); // NOP
 		}
 	}
 
 	private static class ParserProblemReporter extends ProblemCollector {
 
 		@Override
 		public String toString() {
 			if (problems == null)
 				return "No problems";
 
 			StringBuffer buffer = new StringBuffer();
 			for (int i = 0; i < problems.size(); i++) {
 				buffer.append(problems.toString());
 				buffer.append("\n");
 			}
 
 			return buffer.toString();
 		}
 	}
 
 	private int detectIndentationLevel(String input, int offset) {
 		ParserProblemReporter reporter = new ParserProblemReporter();
 
 		Script ast = new JavaScriptParser().parse(null, input.toCharArray(),
 				reporter);
 
 		if (ast == null) {
 			if (DLTKCore.DEBUG)
 				System.out.println(reporter.toString());
 
 			return 0;
 		}
 
 		final FormatterDocument fDocument = createDocument(input);
 		final FormatterNodeBuilder builder = new FormatterNodeBuilder(fDocument);
 		IFormatterContainerNode root = builder.build(ast);
 		IFormatterContext context = new JavaScriptFormatterContext(0);
 
 		new JavascriptFormatterNodeRewriter(ast).rewrite(root);
 
 		FormatterIndentDetector detector = new FormatterIndentDetector(offset);
 		try {
 			root.accept(context, detector);
 			return detector.getLevel();
 		} catch (Exception e) {
 			// ignore all
 		}
 		return 0;
 	}
 
 	@Override
 	public int detectIndentationLevel(IDocument document, int offset) {
 		return detectIndentationLevel(document.get(), offset);
 	}
 
 	public String format(String source, int indentationLevel)
 			throws FormatterException {
 
 		ParserProblemReporter reporter = new ParserProblemReporter();
 
 		Script root = new JavaScriptParser().parse(null, source.toCharArray(),
 				reporter);
 
		if (root == null) {
 			final List<IProblem> errors = reporter.getErrors();
 			if (!errors.isEmpty()) {
 				if (errors.size() == 1 && errors.get(0) instanceof JSProblem) {
 					final JSProblem problem = (JSProblem) errors.get(0);
 					throw new FormatterSyntaxProblemException(problem
 							.getMessage(), problem.getCause());
 				}
 				throw new FormatterSyntaxProblemException(errors.toString());
 			} else {
 				throw new FormatterSyntaxProblemException("Syntax error");
 			}
 		}
 
 		return format(source, root, indentationLevel);
 	}
 
 	private String format(String source, Script ast, int indentationLevel)
 			throws FormatterException {
 
 		final FormatterDocument document = createDocument(source);
 		final FormatterNodeBuilder builder = new FormatterNodeBuilder(document);
 
 		IFormatterContainerNode root = builder.build(ast);
 
 		new JavascriptFormatterNodeRewriter(ast).rewrite(root);
 
 		IFormatterContext context = new JavaScriptFormatterContext(
 				indentationLevel);
 		JavaScriptFormatterWriter writer = new JavaScriptFormatterWriter(
 				document, lineDelimiter, createIndentGenerator());
 
 		writer
 				.setWrapLength(getInt(JavaScriptFormatterConstants.WRAP_COMMENTS_LENGTH));
 		writer.setLinesPreserve(1);// FIXME
 		writer.setPreserveSpaces(false);
 		writer
 				.setKeepLines(getBoolean(JavaScriptFormatterConstants.KEEP_LINES));
 
 		try {
 			root.accept(context, writer);
 			writer.flush(context);
 			return writer.getOutput();
 		} catch (Exception e) {
 			throw new FormatterException(e);
 		}
 	}
 
 	private FormatterDocument createDocument(String input) {
 		FormatterDocument document = new FormatterDocument(input);
 
 		// initialize preferences
 		String[] options = JavaScriptFormatterConstants.getNames();
 		for (int i = 0; i < options.length; i++) {
 			String name = options[i];
 			if (JavaScriptFormatterConstants.isBoolean(name)) {
 				document.setBoolean(name, getBoolean(name));
 			} else if (JavaScriptFormatterConstants.isInteger(name)) {
 				document.setInt(name, getInt(name));
 			} else if (JavaScriptFormatterConstants.isString(name)) {
 				document.setString(name, getString(name));
 			}
 		}
 
 		return document;
 	}
 }
