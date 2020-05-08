 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui.text;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.dltk.ruby.internal.ui.text.syntax.RubyBlocks;
 import org.eclipse.dltk.ui.text.blocks.Balance;
 import org.eclipse.dltk.ui.text.blocks.BlocksConfiguration;
 import org.eclipse.dltk.ui.text.blocks.Instance;
 import org.eclipse.dltk.ui.text.blocks.Balance.Listener;
 import org.eclipse.dltk.ui.text.blocks.Instance.JoinResult;
 import org.eclipse.dltk.ui.text.util.AutoEditUtils;
 import org.eclipse.dltk.ui.text.util.IPartitionFilter;
 import org.eclipse.dltk.ui.text.util.IRangeFilter;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DocumentCommand;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextUtilities;
 
 public class RubyIndenter {
 	
 	private static final int maxCharsAway = 100;
 
 	private static final String CONTINUATION_DESIGNATOR = "\\";
 
 	private static final String[] CLOSING_BRACES = new String[] {"}", "]", ")"};
 	private static final String[] JAVADOC_NOINDENT_TAGS = new String[] {"=begin", "=end"};
 
 	
 	private static final IPartitionFilter DEFAULT_PARTITION_FILTER = new IPartitionFilter() {
 
 					public boolean allowPartition(String partition) {
 						return partition == IDocument.DEFAULT_CONTENT_TYPE;
 					}
 			
 		};
 	
 	/*
 	 * 1. Make these strategies work with any kind of block.
 	 * 
 	 * 2. Use blocks for paren matching.
 	 * 
 	 * 3. More strategies: force same column.
 	 * 
 	 * 4. Option: reindent inner area when closing word is reindented.
 	 */
 	
 	public interface IMatchingPairIndentingStrategy {
 	
 		public String getIndentForOpeningOne(IDocument doc, int matchOffset) throws BadLocationException;
 		
 	}
 	
 	private final IMatchingPairIndentingStrategy previousMinusOneIndenter = new IMatchingPairIndentingStrategy() {
 
 		public String getIndentForOpeningOne(IDocument d, int matchOffset) throws BadLocationException {
 			int lineIndex = d.getLineOfOffset(matchOffset);
 			int lineOffset = d.getLineOffset(lineIndex);
 			String indent = AutoEditUtils.getLineIndent(d, lineIndex);
 			int pos = matchOffset - lineOffset - indent.length();
 			return indent + AutoEditUtils.getNSpaces(pos);
 		}
 		
 	};
 	
 	private final IMatchingPairIndentingStrategy previousLineIndenter = new IMatchingPairIndentingStrategy() {
 		
 		public String getIndentForOpeningOne(IDocument d, int matchOffset) throws BadLocationException {
 			int lineIndex = d.getLineOfOffset(matchOffset);
 			return AutoEditUtils.getLineIndent(d, lineIndex);
 		}
 		
 	};
 	
 	private final IMatchingPairIndentingStrategy[] braceIndentingStrategies = new IMatchingPairIndentingStrategy[] {
 			previousLineIndenter, previousMinusOneIndenter, previousMinusOneIndenter
 	};
 
 	private final String fPartitioning;
 	private final RubyPreferenceInterpreter prefs;
 
 	public RubyIndenter(String partitioning, RubyPreferenceInterpreter prefs) {
 		this.fPartitioning = partitioning;
 		this.prefs = prefs;
 	}
 	
 	private Balance calculateBalance(IDocument document, int startOffset, int length, BlocksConfiguration blocks, Listener listener) throws BadLocationException {
 		return Balance.calculateBalance(document, startOffset, length, blocks, new IRangeFilter() {
 
 			public boolean allowRange(IDocument document, int start, int length) throws BadLocationException {
 				return AutoEditUtils.rangeIsInsideDefaultPartition(document, start, length, fPartitioning);
 			}
 			
 		}, listener);
 	}
 
 	private Balance calculateLineBalance(IDocument document, int lineIndex, Balance nextLineBalance, BlocksConfiguration blocks, Listener listener, int rightLimit) throws BadLocationException {
 		int lineStart = document.getLineOffset(lineIndex);
 		int lineLength = document.getLineLength(lineIndex);
 		if (rightLimit >= 0)
 			lineLength = rightLimit - lineStart;
 		Balance balance = calculateBalance(document, lineStart, lineLength, blocks, listener);
 		if (nextLineBalance != null)
 			balance.addAll(nextLineBalance, listener);
 		return balance;
 	}
 
 	private int getMasterLineIndex(IDocument document, int lineIndex, Balance nextLineBalance, int lookbehindLimit, boolean acceptNearest, BlocksConfiguration blocks, int prevLineRightLimit) throws BadLocationException {
 		if (lineIndex < 0)
 			return -1;
 		
 		lineIndex = AutoEditUtils.getLastNonEmptyLine(document, lineIndex, "#");
 		if (lineIndex < 0)
 			return -1;
 		
 		Balance balance = calculateLineBalance(document, lineIndex, nextLineBalance, blocks, null, prevLineRightLimit);
 		int result = -1;
 		
 		System.err.println();
 		if (acceptNearest) {
 			result = getSingleLineMasterIndex(document, lineIndex, balance);
 			if (result != -1)
 				return result;
 		}
 		if (lookbehindLimit > 0) {
 			result = getMasterLineIndex(document, lineIndex - 1, balance, lookbehindLimit - 1, true, blocks, -1);
 		}
 		if (!acceptNearest && result == -1) {
 			result = getSingleLineMasterIndex(document, lineIndex, balance);
 		}
 		return result;
 	}
 
 	private int getSingleLineMasterIndex(IDocument document, int lineIndex, Balance balance) throws BadLocationException {
 		String line = AutoEditUtils.getDocumentLine(document, lineIndex);
 		if (line.endsWith(CONTINUATION_DESIGNATOR) || balance.isAnythingOpen())
 			return lineIndex;
 		return -1;
 	}
 	
 	private int findMatchBackward(IDocument document, int startLineIndex, String matchText, final int matchOffset, int lookbehindLimit, BlocksConfiguration blocks, int rightLimit) throws BadLocationException {
 		if (startLineIndex < 0)
 			return -1;
 		Balance nextLineBalance = new Balance(blocks);
 		JoinResult result0 = nextLineBalance.process(document, matchText, matchOffset, null);
 		if (result0 == JoinResult.UNHANDLED)
 			return -1;
 		final int[] result = new int[] {-1};
 		Balance.Listener listener = new Balance.Listener() {
 
 			public void instanceMatched(Instance instance) {
 				int beginningOffset = instance.getBeginningOffset();
 				int middleOffset = instance.getMiddleOffset();
 				int endingOffset = instance.getEndingOffset();
 				if (endingOffset == matchOffset)
 					if (middleOffset >= 0)
 						result[0] = middleOffset;
 					else if (beginningOffset >= 0)
 						result[0] = beginningOffset;
 					else
 						;
 				else if (middleOffset == matchOffset)
 					if (beginningOffset >= 0)
 						result[0] = beginningOffset;
 			}
 			
 		};
 		int lastLineIndexToTest = Math.max(0, startLineIndex - lookbehindLimit + 1);
 		for (int lineIndex = startLineIndex; lineIndex >= lastLineIndexToTest; lineIndex--) {
 			nextLineBalance = calculateLineBalance(document, lineIndex, nextLineBalance, blocks, listener, rightLimit);
 			if (result[0] >= 0)
 				break;
 			rightLimit = -1; // reset after first processed line
 		}
 		return result[0];
 	}
 	
 	public String calculateLineIndent(IDocument document, String line, int lineIndex, int lineOffset, String typedText, int prevLineRightLimit) throws BadLocationException {
 		String result;
 		
 		result = calculateIndentByCurrentLineContent(document, line, lineIndex, lineOffset, prevLineRightLimit);
 		if (result != null)
 			return result;
 		
 		int masterLine = getMasterLineIndex(document, lineIndex - 1, null, 0, false, RubyBlocks.ALL_BLOCKS, prevLineRightLimit);
 		if (masterLine >= 0) {
 			String indent = AutoEditUtils.getLineIndent(document, masterLine);
 			return indent + prefs.getIndent();
 		}
 	
 		return null;
 	}
 
 	private String calculateIndentByCurrentLineContent(IDocument document, String line, int lineIndex, int lineOffset, int prevLineRightLimit) throws BadLocationException {
 		if (!AutoEditUtils.rangeIsInsideDefaultPartition(document, lineOffset, 1, fPartitioning))
 			return null;
 		
 		String lineIndent = AutoEditUtils.getLineIndent(line);
 		String unindentedLine = line.substring(lineIndent.length());
 		
 		int braceIndex = TextUtilities.startsWith(CLOSING_BRACES, unindentedLine);
 		if (braceIndex >= 0)
 			return handleClosingBrace(document, lineOffset, braceIndex);
 		
 		if (TextUtilities.startsWith(JAVADOC_NOINDENT_TAGS, unindentedLine) != -1)
 			return "";
 		
 		// extract the first word of the line (if any)
 		Pattern pattern = RubyBlocks.ALL_BLOCKS.getMiddleEndAlignedPattern();
 		Matcher matcher = pattern.matcher(unindentedLine);
 		if (matcher.find()) {			
 			int matchedOffset = findMatchBackward(document, lineIndex - 1,
 					matcher.group(), lineOffset, 1000, RubyBlocks.ALL_BLOCKS, prevLineRightLimit);
 			if (matchedOffset >= 0) {
 				int matchLineIndex = document.getLineOfOffset(matchedOffset);
 				return AutoEditUtils.getLineIndent(document, matchLineIndex);
 			}
 		}
 		
 		return null;
 	}
 
 	private String handleClosingBrace(IDocument document, int lineOffset, int braceIndex) throws BadLocationException {
 		char closing = CLOSING_BRACES[braceIndex].charAt(0);
 		char opening = AutoEditUtils.getBracePair(closing);
 		int matching = AutoEditUtils.findMatchingCharacter(document, lineOffset, false, fPartitioning,
 		opening, closing, DEFAULT_PARTITION_FILTER, maxCharsAway);
 		return braceIndentingStrategies[braceIndex].getIndentForOpeningOne(document, matching);
 	}
 
 	public String calculateChangedLineIndent(IDocument document, int lineIndex, boolean newLine, int insertionOffset, DocumentCommand command)
 			throws BadLocationException {
 		
 		String line;
 		int lineOffset;
 		String text = (command == null ? "" : command.text);
 		if (newLine) {
 			line = "";
 			if (lineIndex == document.getNumberOfLines())
 				lineOffset = document.getLength();
 			else
 				lineOffset = document.getLineOffset(lineIndex);  
 		} else {
 			lineOffset = document.getLineOffset(lineIndex);
 			int lineLength = document.getLineLength(lineIndex);
 			line = document.get(lineOffset, lineLength);
 			if (command != null && text != null)
 				if (command.offset >= lineOffset && command.offset <= lineOffset + lineLength) {
 					int relativeOffset = insertionOffset - lineOffset;
 					int relativeRemovedLength = Math.min(command.length, lineLength - relativeOffset);
 					line = line.substring(0, relativeOffset) +
 						text + line.substring(relativeOffset + relativeRemovedLength);
 				}
 		}
 		
 		return calculateLineIndent(document, line, lineIndex, lineOffset, text, -1);
 		
 	}
 
 	public String forciblyCalculateLineIndent(IDocument document, int lineIndex, int lineStart, String line, int prevLineRightLimit) throws BadLocationException {
 		String resultIndent = calculateLineIndent(document, line, lineIndex, lineStart, "", prevLineRightLimit);
 		if (resultIndent == null) {
 			int previousCodeLine = AutoEditUtils.getLastNonEmptyLine(document, lineIndex - 1, "#");
 			int indentAmount;
 			if (previousCodeLine < 0)
 				// the subject line is the first code line in the document
 				indentAmount = 0;
 			else
 				indentAmount = AutoEditUtils.getIndentVisualLength(prefs, document, previousCodeLine);
 			resultIndent = prefs.getIndentByVirtualSize(indentAmount);
 		}
 		return resultIndent;
 	}
 
 	public String forciblyCalculateLineIndent(IDocument document, final int lineIndex) throws BadLocationException {
 		int lineStart = document.getLineOffset(lineIndex);
 		int lineLength = document.getLineLength(lineIndex);
 		String line = document.get(lineStart, lineLength);
 		return forciblyCalculateLineIndent(document, lineIndex, lineStart, line, -1);
 	}
 	
 }
