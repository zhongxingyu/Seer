 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.formatter;
 
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.formatter.nodes.FormatterContext;
 import org.eclipse.dltk.formatter.nodes.FormatterDocument;
 import org.eclipse.dltk.formatter.nodes.FormatterWriter;
 import org.eclipse.dltk.formatter.nodes.IFormatterContainerNode;
 import org.eclipse.dltk.ruby.formatter.internal.DumpContentException;
 import org.eclipse.dltk.ruby.formatter.internal.Messages;
 import org.eclipse.dltk.ruby.formatter.internal.RubyFormatterNodeBuilder;
 import org.eclipse.dltk.ruby.formatter.internal.RubyFormatterPlugin;
 import org.eclipse.dltk.ruby.formatter.internal.RubyParser;
 import org.eclipse.dltk.ui.CodeFormatterConstants;
 import org.eclipse.dltk.ui.formatter.AbstractScriptFormatter;
import org.eclipse.dltk.ui.formatter.FormatterException;
import org.eclipse.dltk.ui.formatter.FormatterSyntaxProblemException;
import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.ReplaceEdit;
 import org.eclipse.text.edits.TextEdit;
 import org.jruby.parser.RubyParserResult;
 
 public class RubyFormatter extends AbstractScriptFormatter {
 
 	private static final String[] INDENTING = {
 			RubyFormatterConstants.INDENT_CLASS,
 			RubyFormatterConstants.INDENT_MODULE,
 			RubyFormatterConstants.INDENT_METHOD,
 			RubyFormatterConstants.INDENT_BLOCKS,
 			RubyFormatterConstants.INDENT_IF,
 			RubyFormatterConstants.INDENT_CASE,
 			RubyFormatterConstants.INDENT_WHEN };
 
 	private static final String[] BLANK_LINES = {
 			RubyFormatterConstants.LINES_FILE_AFTER_REQUIRE,
 			RubyFormatterConstants.LINES_FILE_BETWEEN_MODULE,
 			RubyFormatterConstants.LINES_FILE_BETWEEN_CLASS,
 			RubyFormatterConstants.LINES_FILE_BETWEEN_METHOD,
 			RubyFormatterConstants.LINES_BEFORE_FIRST,
 			RubyFormatterConstants.LINES_BEFORE_MODULE,
 			RubyFormatterConstants.LINES_BEFORE_CLASS,
 			RubyFormatterConstants.LINES_BEFORE_METHOD };
 
 	public static Map createTestingPreferences() {
 		final Map result = new HashMap();
 		for (int i = 0; i < INDENTING.length; ++i) {
 			result.put(INDENTING[i], Boolean.TRUE);
 		}
 		for (int i = 0; i < BLANK_LINES.length; ++i) {
 			result.put(BLANK_LINES[i], new Integer(-1));
 		}
 		result.put(RubyFormatterConstants.FORMATTER_TAB_CHAR,
 				CodeFormatterConstants.TAB);
 		result.put(RubyFormatterConstants.FORMATTER_INDENTATION_SIZE,
 				new Integer(1));
 		result.put(RubyFormatterConstants.LINES_PRESERVE, new Integer(
 				Integer.MAX_VALUE));
 		return result;
 	}
 
 	private final String lineDelimiter;
 
 	public RubyFormatter(String lineDelimiter, Map preferences) {
 		super(preferences);
 		this.lineDelimiter = lineDelimiter;
 	}
 
	public TextEdit format(String source, int offset, int length, int indent)
			throws FormatterException {
 		final String input = source.substring(offset, offset + length);
 		final RubyParserResult result = RubyParser.parse(input);
		if (result == null) {
			throw new FormatterSyntaxProblemException();
		}
		final String output = format(input, result);
		if (output != null) {
			if (!input.equals(output)) {
				if (!isValidation()
						|| equalsIgnoreBlanks(new StringReader(input),
								new StringReader(output))) {
 					return new ReplaceEdit(offset, length, output);
 				} else {
 					RubyFormatterPlugin.log(new Status(IStatus.ERROR,
 							RubyFormatterPlugin.PLUGIN_ID, IStatus.OK,
 							Messages.RubyFormatter_contentCorrupted,
 							new DumpContentException(input)));
 				}
			} else {
				return new MultiTextEdit(); // NOP
 			}
 		}
 		return null;
 	}
 
	protected boolean isValidation() {
		return true;
	}

 	/**
 	 * @param input
 	 * @param result
 	 * @return
 	 */
 	private String format(String input, RubyParserResult result) {
 		final RubyFormatterNodeBuilder builder = new RubyFormatterNodeBuilder();
 		FormatterDocument document = new FormatterDocument(input);
 		for (int i = 0; i < INDENTING.length; ++i) {
 			document.setBoolean(INDENTING[i], getBoolean(INDENTING[i]));
 		}
 		for (int i = 0; i < BLANK_LINES.length; ++i) {
 			document.setInt(BLANK_LINES[i], getInt(BLANK_LINES[i]));
 		}
 		IFormatterContainerNode root = builder.build(result, document);
 		FormatterContext context = new FormatterContext();
 		FormatterWriter writer = new FormatterWriter(lineDelimiter,
 				createIndentGenerator());
 		writer.setLinesPreserve(getInt(RubyFormatterConstants.LINES_PRESERVE));
 		try {
 			root.accept(context, writer);
 			writer.flush(context);
 			return writer.getOutput();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private boolean equalsIgnoreBlanks(Reader inputReader, Reader outputReader) {
 		LineNumberReader input = new LineNumberReader(inputReader);
 		LineNumberReader output = new LineNumberReader(outputReader);
 		for (;;) {
 			final String inputLine = readLine(input);
 			final String outputLine = readLine(output);
 			if (inputLine == null) {
 				if (outputLine == null) {
 					return true;
 				} else {
 					return false;
 				}
 			} else if (outputLine == null) {
 				return false;
 			} else if (!inputLine.equals(outputLine)) {
 				return false;
 			}
 		}
 	}
 
 	private String readLine(LineNumberReader reader) {
 		String line;
 		do {
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				// should not happen
 				return null;
 			}
 			if (line == null) {
 				return line;
 			}
 			line = line.trim();
 		} while (line.length() == 0);
 		return line;
 	}
 
 }
