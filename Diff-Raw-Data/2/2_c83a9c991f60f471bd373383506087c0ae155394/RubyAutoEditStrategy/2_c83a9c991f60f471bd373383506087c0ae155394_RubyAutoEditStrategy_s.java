 package org.eclipse.dltk.ruby.internal.ui.text;
 
 import java.util.regex.Pattern;
 
 import org.eclipse.dltk.ui.text.util.AutoEditUtils;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.DocumentCommand;
 import org.eclipse.jface.text.DocumentRewriteSession;
 import org.eclipse.jface.text.DocumentRewriteSessionType;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.rules.FastPartitioner;
 
 public class RubyAutoEditStrategy extends DefaultIndentLineAutoEditStrategy {
 
 	private String fPartitioning;
 
 	private RubyPreferenceInterpreter prefs;
 
 	private RubyIndenter indenter;
 
 	public RubyAutoEditStrategy(IPreferenceStore store, String part) {
 		fPartitioning = part;
 		prefs = new RubyPreferenceInterpreter(store);
 		indenter = new RubyIndenter(part, prefs);
 	}
 
 	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
 		if (c.doit == false)
 			return;
 		if (AutoEditUtils.isNewLineInsertionCommand(d, c))
 			if (prefs.isSmartMode())
 				handleSmartNewLine(d, c);
 			else
 				super.customizeDocumentCommand(d, c);
 		else if (c.text != null && c.text.length() > 1 && prefs.isSmartPaste())
 			handleSmartPaste(d, c); // no smart backspace for paste
 		else if (AutoEditUtils.isSingleCharactedInsertionOrReplaceCommand(c))
 			handeSingleCharacterTyped(d, c);
 	}
 
 	/**
 	 * Installs a partitioner with <code>document</code>.
 	 * 
 	 * @param document
 	 *            the document
 	 */
 	private static void installStuff(Document document) {
 		String[] types = new String[] { RubyPartitions.RUBY_STRING, RubyPartitions.RUBY_COMMENT,
 				IDocument.DEFAULT_CONTENT_TYPE };
 		FastPartitioner partitioner = new FastPartitioner(new RubyPartitionScanner(), types);
 		partitioner.connect(document);
 		document.setDocumentPartitioner(RubyPartitions.RUBY_PARTITIONING, partitioner);
 	}
 
 	/**
 	 * Removes partitioner with <code>document</code>.
 	 * 
 	 * @param document
 	 *            the document
 	 */
 	private static void removeStuff(Document document) {
 		document.setDocumentPartitioner(RubyPartitions.RUBY_PARTITIONING, null);
 	}
 
 	/**
 	 * If we have pressed ":" for example, than we need to reindent line. This
 	 * function changes document and sets correct indent for current line.
 	 * 
 	 * @param d
 	 * @param c
 	 */
 	private void reindent(IDocument d, DocumentCommand c) {
 		try {
 			if (AutoEditUtils.getRegionType(d, fPartitioning, c.offset) != IDocument.DEFAULT_CONTENT_TYPE)
 				return;
 			int line = d.getLineOfOffset(c.offset);
 			String newIndent = indenter.calculateChangedLineIndent(d, line, false, c.offset, c);
 			if (newIndent == null)
 				return;
 			String curIndent = AutoEditUtils.getLineIndent(d, line);
 			if (AutoEditUtils.getIndentVisualLength(prefs, curIndent) < AutoEditUtils
 					.getIndentVisualLength(prefs, newIndent))
 				return;
 			d.replace(d.getLineOffset(line), curIndent.length(), newIndent);
 			c.offset += (newIndent.length() - curIndent.length());
 		} catch (BadLocationException e) {
 		}
 	}
 
 	/**
 	 * Processes command in work with brackets, strings, etc
 	 * 
 	 * @param d
 	 * @param c
 	 */
 	private void autoClose(IDocument d, DocumentCommand c) {
 		if (c.offset == -1)
 			return;
 		try {
 			if (d.getChar(c.offset - 1) == '\\')
 				return;
 		} catch (BadLocationException e1) {
 		}
 		if ('\"' == c.text.charAt(0) && !prefs.closeStrings())
 			return;
 		if ('\'' == c.text.charAt(0) && !prefs.closeStrings())
 			return;
 		if (!prefs.closeBrackets()
 				&& ('[' == c.text.charAt(0) || '(' == c.text.charAt(0) || '{' == c.text.charAt(0)))
 			return;
 		try {
 
 			switch (c.text.charAt(0)) {
 			case '\"':
 			case '\'':
 				// if we close existing quote, do nothing
 				if ('\"' == c.text.charAt(0) && c.offset > 0 && "\"".equals(d.get(c.offset - 1, 1)))
 					return;
 
 				if ('\'' == c.text.charAt(0) && c.offset > 0 && "\'".equals(d.get(c.offset - 1, 1)))
 					return;
 
 				if (c.offset != d.getLength() && c.text.charAt(0) == d.get(c.offset, 1).charAt(0))
 					c.text = "";
 				else {
 					c.text += c.text;
 					c.length = 0;
 				}
 
 				c.shiftsCaret = false;
 				c.caretOffset = c.offset + 1;
 				break;
 			case '(':
 			case '{':
 			case '[':
 				// check partition
 				if (AutoEditUtils.getRegionType(d, fPartitioning, c.offset) != IDocument.DEFAULT_CONTENT_TYPE)
 					return;
 				if (c.offset != d.getLength() && c.text.charAt(0) == d.get(c.offset, 1).charAt(0))
 					return;
 
 				try { // in class closing
 					String regex = "^\\s*class\\s+.*";
 					String regex2 = ".*\\(.*\\).*";
 					int start = d.getLineOffset(d.getLineOfOffset(c.offset));
 					String curLine = d.get(start, c.offset - start);
 					if (Pattern.matches(regex, curLine) && !Pattern.matches(regex2, curLine)) {
						c.text = "():";
 						c.shiftsCaret = false;
 						c.caretOffset = c.offset + 1;
 						return;
 					}
 				} catch (BadLocationException e) {
 				}
 
 				// add closing peer
 				c.text = c.text + AutoEditUtils.getBracePair(c.text.charAt(0));
 				c.length = 0;
 
 				c.shiftsCaret = false;
 				c.caretOffset = c.offset + 1;
 				break;
 			case '}':
 			case ']':
 			case ')':
 				// check partition
 				if (AutoEditUtils.getRegionType(d, fPartitioning, c.offset) != IDocument.DEFAULT_CONTENT_TYPE)
 					return;
 				if (!prefs.closeBrackets())
 					return;
 				// if we already have bracket we should jump over it
 				if (c.offset != d.getLength() && c.text.charAt(0) == d.get(c.offset, 1).charAt(0)) {
 					c.text = "";
 					c.shiftsCaret = false;
 					c.caretOffset = c.offset + 1;
 					return;
 				}
 				break;
 			}
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Does the Right Thing when the users presses the Tab key.
 	 */
 	private boolean handleSmartTabulation(IDocument d, DocumentCommand c) {
 		if (c.offset == -1 || d.getLength() == 0)
 			return false;
 		try {
 			int tabOffset = c.offset;
 			int lineIndex = d.getLineOfOffset(tabOffset);
 			int lineStart = d.getLineOffset(lineIndex);
 			int lineLength = d.getLineLength(lineIndex);
 			int lineEnd = lineStart + lineLength;
 			int lineHome = AutoEditUtils.findEndOfWhiteSpace(d, lineStart, lineEnd);
 
 			if (tabOffset >= lineHome && lineHome < lineEnd) {
 				// tab pressed in the middle of the line
 				if (!prefs.getTabAlwaysIndents())
 					return false;
 
 				// add indent to the start of line
 				c.offset = lineHome;
 				c.length = tabOffset - lineHome;
 				c.text = prefs.getIndent() + d.get(lineHome, tabOffset - lineHome);
 				return true;
 			}
 
 			String line = d.get(lineStart, lineLength);
 			String resultIndent = indenter.forciblyCalculateLineIndent(d, lineIndex, lineStart,
 					line, -1);
 			int resultIndentSize = AutoEditUtils.getIndentVisualLength(prefs, resultIndent);
 
 			if (lineHome == lineEnd) {
 				int visualColumn = AutoEditUtils.calculateVisualLength(prefs, d, lineStart,
 						lineLength, lineStart, tabOffset);
 				if (visualColumn >= resultIndentSize)
 					return false;
 			} else if (tabOffset < lineHome) {
 				int currentIndentSize = AutoEditUtils.calculateVisualLength(prefs, d, lineStart,
 						lineLength, lineStart, lineHome);
 				if (currentIndentSize == resultIndentSize) {
 					// just move the cursor, but don't change the doc
 					c.offset = lineHome;
 					c.length = 0;
 					c.text = null;
 					return true;
 				}
 			}
 
 			c.offset = lineStart;
 			c.length = lineHome - lineStart;
 			c.text = resultIndent;
 			return true;
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private void handleSmartPaste(IDocument d, DocumentCommand c) {
 		final boolean whitespaceCollapsingAtTheStartOfFirstPastedLineEnabled = true;
 		try {
 			int pasteOffset = c.offset;
 			final int firstLineIndex = d.getLineOfOffset(pasteOffset);
 			final int firstLineStart = d.getLineOffset(firstLineIndex);
 			final int firstLineLength = d.getLineOffset(firstLineIndex);
 			final int firstLineHome = AutoEditUtils.findEndOfWhiteSpace(d, firstLineStart, firstLineStart + firstLineLength);
 			
 			Document temp = new Document(d.get(0, pasteOffset) + c.text);
 			DocumentRewriteSession session = temp
 					.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
 			installStuff(temp);
 			
 			final int numberOfPastedLines = temp.getNumberOfLines() - firstLineIndex;
 			boolean insertionPointInsideIndent = pasteOffset <= firstLineHome;
 			if (insertionPointInsideIndent && (numberOfPastedLines > 1)) {
 				// pretend we're inserting to the very beginning
 				temp.replace(firstLineStart, pasteOffset - firstLineStart, "");
 				pasteOffset = c.offset = firstLineStart;
 			}
 			
 			final int firstTargetLineLength = d.getLineLength(firstLineIndex);
 			final int firstTargetLineEnd = firstLineStart + firstTargetLineLength;
 			final int firstTargetLineHome = AutoEditUtils.findEndOfWhiteSpace(d, firstLineStart,
 					firstTargetLineEnd);
 
 			final boolean reindentFirstInsertedLine = (pasteOffset < firstTargetLineHome);
 			int firstSourceIndentedLineIndex;
 			final int targetLineIndexToCalculateCommonIndentationFor;
 			if (reindentFirstInsertedLine) {
 				targetLineIndexToCalculateCommonIndentationFor = firstLineIndex;
 				firstSourceIndentedLineIndex = firstLineIndex;
 			} else {
 				if (firstLineIndex + 1 < d.getNumberOfLines())
 					targetLineIndexToCalculateCommonIndentationFor = firstLineIndex + 1;
 				else
 					targetLineIndexToCalculateCommonIndentationFor = firstLineIndex;
 				firstSourceIndentedLineIndex = firstLineIndex + 1;
 			}
 
 			// collapse the indentation of the first line of pasted data (if
 			// several lines are being pasted)
 			if (whitespaceCollapsingAtTheStartOfFirstPastedLineEnabled)
 				if (!reindentFirstInsertedLine && numberOfPastedLines > 1)
 					collapseIndentationOfFirstPastedLine(temp, pasteOffset, firstLineIndex, firstLineStart);
 
 			// determine the amount of common indentation to remove
 			int sourceIndentSizeToRemove = determineAmountOfIndentationToRemove(temp,
 					firstSourceIndentedLineIndex);
 
 			// determine the indentation that should be applied to all (possibly
 			// except the first) pasted lines
 			final int indentSizeToApplyToPastedText = determineIndentationToApplyToPastedText(d,
 					targetLineIndexToCalculateCommonIndentationFor);
 			
 			// traverse through all the lines and fixup the indentation
 			fixupIndentation(temp, firstSourceIndentedLineIndex, numberOfPastedLines,
 					-sourceIndentSizeToRemove + indentSizeToApplyToPastedText);
 
 			temp.stopRewriteSession(session);
 			removeStuff(temp);
 			c.text = temp.get(pasteOffset, temp.getLength() - pasteOffset);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void collapseIndentationOfFirstPastedLine(Document temp, final int pasteOffset,
 			final int firstLineIndex, final int firstLineStart)
 			throws BadLocationException {
 		int firstSourceLineLength = temp.getLineLength(firstLineIndex);
 		int firstSourceLineEnd = firstLineStart + firstSourceLineLength;
 		String x = temp.get(firstLineStart, firstSourceLineLength);
 		int firstSourceLineInsertionHome = AutoEditUtils.findEndOfWhiteSpace(temp, pasteOffset,
 				firstSourceLineEnd);
 		if (AutoEditUtils.atEndOfLine(temp, firstSourceLineInsertionHome, firstSourceLineEnd))
 			// only whitespace was pasted, remove it all
 			temp.replace(pasteOffset, firstSourceLineInsertionHome - pasteOffset, "");
 		else if (firstSourceLineInsertionHome > pasteOffset)
 			// replace any kind of whitespace with a single space
 			temp.replace(pasteOffset, firstSourceLineInsertionHome - pasteOffset, " ");
 	}
 
 	private int determineAmountOfIndentationToRemove(Document temp,
 			final int firstSourceIndentedLineIndex) throws BadLocationException {
 		int sourceIndentSizeToRemove = Integer.MAX_VALUE;
 		for (int lineIndex = firstSourceIndentedLineIndex; lineIndex < temp.getNumberOfLines(); lineIndex++) {
 			int start = temp.getLineOffset(lineIndex);
 			int length = temp.getLineLength(lineIndex);
 			// TODO: check for start of comment and other first-column things
 			int home = AutoEditUtils.findEndOfWhiteSpace(temp, start, start + length);
 			if (AutoEditUtils.atEndOfLine(temp, home, start + length))
 				continue;
 			int indent = AutoEditUtils.calculateVisualLength(prefs, temp, start, length, start,
 					home);
 			sourceIndentSizeToRemove = Math.min(sourceIndentSizeToRemove, indent);
 		}
 		return sourceIndentSizeToRemove;
 	}
 
 	private int determineIndentationToApplyToPastedText(IDocument document,
 			final int targetLineIndexToCalculateCommonIndentationFor) throws BadLocationException {
 		int lineOffset = document.getLineOffset(targetLineIndexToCalculateCommonIndentationFor);
 		// note: we pretend that the line we're inserting text before is empty
 		String indent = indenter.forciblyCalculateLineIndent(document,
 				targetLineIndexToCalculateCommonIndentationFor, lineOffset, "", -1);
 		return AutoEditUtils.getIndentVisualLength(prefs, indent);
 	}
 
 	private void fixupIndentation(Document temp, final int firstSourceIndentedLineIndex,
 			final int numberOfPastedLines, int indentSizeDelta) throws BadLocationException {
 		for (int lineIndex = firstSourceIndentedLineIndex; lineIndex < temp.getNumberOfLines(); lineIndex++) {
 			final int lineStart = temp.getLineOffset(lineIndex);
 			final int lineLength = temp.getLineLength(lineIndex);
 			final int end = lineStart + lineLength;
 			final int home = AutoEditUtils.findEndOfWhiteSpace(temp, lineStart, end);
 			// TODO: check for start of comment and other first-column things
 
 			final int existingLineIndentSize = AutoEditUtils.calculateVisualLength(prefs, temp,
 					lineStart, lineLength, lineStart, home);
 			final int correctLineIndentSize;
 			if (lineIndex == temp.getNumberOfLines() - 1 &&
 					AutoEditUtils.atEndOfLine(temp, home, end) && numberOfPastedLines > 1)
 				// remove all whitespace on the last inserted line if it
 				// consists of whitespace only
 				correctLineIndentSize = 0;
 			else
 				correctLineIndentSize = Math.max(existingLineIndentSize + indentSizeDelta, 0);
 			if (existingLineIndentSize != correctLineIndentSize) {
 				String indent = prefs.getIndentByVirtualSize(correctLineIndentSize);
 				temp.replace(lineStart, home - lineStart, indent);
 				System.out.println();
 			}
 		}
 	}
 	
 	private void replaceIndentation(Document temp, final int lineIndex, final int correctLineIndentSize) throws BadLocationException {
 		final int lineStart = temp.getLineOffset(lineIndex);
 		final int lineLength = temp.getLineLength(lineIndex);
 		final int end = lineStart + lineLength;
 		final int home = AutoEditUtils.findEndOfWhiteSpace(temp, lineStart, end);
 		final int existingLineIndentSize = AutoEditUtils.calculateVisualLength(prefs, temp,
 				lineStart, lineLength, lineStart, home);
 		if (existingLineIndentSize != correctLineIndentSize) {
 			String indent = prefs.getIndentByVirtualSize(correctLineIndentSize);
 			temp.replace(lineStart, home - lineStart, indent);
 		}
 	}
 
 	private void handleSmartNewLine(IDocument document, DocumentCommand c) {
 		try {
 			int lineIndex = document.getLineOfOffset(c.offset);
 			int lineStart = document.getLineOffset(lineIndex);
 			int lineLength = document.getLineLength(lineIndex);
 			String line = document.get(c.offset, lineStart + lineLength - c.offset);
 			String indent = indenter.forciblyCalculateLineIndent(document, lineIndex + 1,
 					lineStart, line, c.offset);
 			c.text = c.text + indent;
 		} catch (BadLocationException e) {
 			super.customizeDocumentCommand(document, c);
 		}
 	}
 
 	private void handeSingleCharacterTyped(IDocument d, DocumentCommand c) {
 		char newChar = c.text.charAt(0);
 		switch (newChar) {
 		case 'n':
 		case 'd':
 		case 'e':
 		case 'f':
 			reindent(d, c);
 			break;
 		case '}':
 		case ']':
 		case ')':
 			reindent(d, c);
 			autoClose(d, c);
 			break;
 		case '\"':
 		case '\'':
 		case '(':
 		case '{':
 		case '[':
 			autoClose(d, c);
 			break;
 		case '\t':
 			boolean jumped = false;
 			if (prefs.isSmartTab()) {
 				jumped = handleSmartTabulation(d, c);
 			}
 			if (!jumped) {
 				c.text = prefs.getIndent();
 			}
 			break;
 		}
 	}
 
 }
