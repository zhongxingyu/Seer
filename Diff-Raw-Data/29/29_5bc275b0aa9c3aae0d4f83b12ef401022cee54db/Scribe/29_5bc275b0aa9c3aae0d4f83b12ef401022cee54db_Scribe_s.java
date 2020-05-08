 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package descent.internal.formatter;
 
 import java.util.Arrays;
 
 import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.ReplaceEdit;
 import org.eclipse.text.edits.TextEdit;
 
 import descent.core.dom.AST;
 import descent.core.dom.CompilationUnit;
 import descent.internal.compiler.parser.Lexer;
 import descent.internal.compiler.parser.ScannerHelper;
 import descent.internal.compiler.parser.TOK;
 import descent.internal.compiler.util.Util;
 
 /**
  * This class is responsible for dumping formatted source
  * @since 2.1
  */
 public class Scribe {	private static final int INITIAL_SIZE = 100;
 
 	private boolean checkLineWrapping;
 	/** one-based column */
 	public int column;
 	//private List comments;
 		
 	// Most specific alignment. 
 	public Alignment currentAlignment;
 	public TOK currentToken;
 	
 	// edits management
 	private OptimizedReplaceEdit[] edits;
 	public int editsIndex;
 	public CodeFormatterVisitor formatter;
 	public int indentationLevel;	
 	public int lastNumberOfNewLines;
 	public int line;
 	private String lineSeparator;
 	public Alignment memberAlignment;
 	public boolean needSpace = false;
 	public int pageWidth;
 	public boolean pendingSpace = false;
 	public Lexer lexer;
 	public int scannerEndPosition;
 	public int tabLength;	
 	public int indentationSize;	
 	private int textRegionEnd;
 	private int textRegionStart;
 	private DefaultCodeFormatterOptions.TabChar tabChar;
 	public int numberOfIndentations;
 	private boolean useTabsOnlyForLeadingIndents;
 	CompilationUnit unit;
 
     /** indent empty lines*/
     private final boolean indentEmptyLines;
     
     private void restartLexer(String source)
 	{
 		if(null == lexer)
			lexer = new Lexer(source, true, true, true, false, AST.D1);
 		else
 			lexer.reset(source.toCharArray(), 0, source.length(), true, true, false, false);
 	}
     
 	Scribe(CodeFormatterVisitor formatter, long sourceLevel, int offset, int length, CompilationUnit unit)
 	{
 		this.lexer = null;
 		this.formatter = formatter;
 		this.pageWidth = formatter.prefs.page_width;
 		this.tabLength = formatter.prefs.tab_size;
 		this.indentationLevel= 0; // initialize properly
 		this.numberOfIndentations = 0;
 		this.useTabsOnlyForLeadingIndents = formatter.prefs.use_tabs_only_for_leading_indentations;
         this.indentEmptyLines = formatter.prefs.indent_empty_lines;
 		this.tabChar = formatter.prefs.tab_char;
 		if (this.tabChar == DefaultCodeFormatterOptions.TabChar.MIXED) {
 			this.indentationSize = formatter.prefs.indentation_size;
 		} else {
 			this.indentationSize = this.tabLength;
 		}
 		this.lineSeparator = formatter.prefs.line_separator;
 		this.indentationLevel = formatter.prefs.initial_indentation_level * this.indentationSize;
 		this.textRegionStart = offset;
 		this.textRegionEnd = offset + length - 1;
 		if (unit != null) {
 			this.unit = unit;
 			//this.comments = unit.getCommentList();
 		}
 		reset();
 	}
 	
 	private final void addDeleteEdit(int start, int end) {
 		if (this.edits.length == this.editsIndex) {
 			// resize
 			resize();
 		}
 		// TODO There was a +1 here. Why? No idea. If stufff gets screwy later, come back here
 		addOptimizedReplaceEdit(start, end - start + 1, Util.EMPTY_STRING);
 	}
 
 	public final void addInsertEdit(int insertPosition, String insertedString) {
 		if (this.edits.length == this.editsIndex) {
 			// resize
 			resize();
 		}
 		addOptimizedReplaceEdit(insertPosition, 0, insertedString);
 	}
 
 	private final void addOptimizedReplaceEdit(int offset, int length, String replacement) {
 		if (this.editsIndex > 0) {
 			// try to merge last two edits
 			final OptimizedReplaceEdit previous = this.edits[this.editsIndex-1];
 			final int previousOffset = previous.offset;
 			final int previousLength = previous.length;
 			final int endOffsetOfPreviousEdit = previousOffset + previousLength;
 			final int replacementLength = replacement.length();
 			final String previousReplacement = previous.replacement;
 			final int previousReplacementLength = previousReplacement.length();
 			if (previousOffset == offset && previousLength == length && (replacementLength == 0 || previousReplacementLength == 0)) {
 				if (this.currentAlignment != null) {
 					final Location location = this.currentAlignment.location;
 					if (location.editsIndex == this.editsIndex) {
 						location.editsIndex--;
 						location.textEdit = previous;
 					}
 				}
 				this.editsIndex--;
 				return;
 			}
 			if (endOffsetOfPreviousEdit == offset) {
 				if (length != 0) {
 					if (replacementLength != 0) {
 						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength + length, previousReplacement + replacement);
 					} else if (previousLength + length == previousReplacementLength) {
 						// check the characters. If they are identical, we can get rid of the previous edit
 						boolean canBeRemoved = true;
 						loop: for (int i = previousOffset; i < previousOffset + previousReplacementLength; i++) {
 							if (lexer.input[i] != previousReplacement.charAt(i - previousOffset)) {
 								this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousReplacementLength, previousReplacement);
 								canBeRemoved = false;
 								break loop;
 							}
 						}
 						if (canBeRemoved) {
 							if (this.currentAlignment != null) {
 								final Location location = this.currentAlignment.location;
 								if (location.editsIndex == this.editsIndex) {
 									location.editsIndex--;
 									location.textEdit = previous;
 								}
 							}
 							this.editsIndex--;
 						}
 					} else {
 						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength + length, previousReplacement);
 					}
 				} else {
 					if (replacementLength != 0) {
 						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength, previousReplacement + replacement);
 					}
 				}
 			} else {
 				this.edits[this.editsIndex++] = new OptimizedReplaceEdit(offset, length, replacement);
 			}
 		} else {
 			this.edits[this.editsIndex++] = new OptimizedReplaceEdit(offset, length, replacement);
 		}
 	}
 	
 	public final void addReplaceEdit(int start, int end, String replacement) {
 		if (this.edits.length == this.editsIndex) {
 			// resize
 			resize();
 		}
 		addOptimizedReplaceEdit(start,  end - start + 1, replacement);
 	}
 
 	public void alignFragment(Alignment alignment, int fragmentIndex){
 		alignment.fragmentIndex = fragmentIndex;
 		alignment.checkColumn();
 		alignment.performFragmentEffect();
 	}
 	
 	public void consumeNextToken() {
 		printComment();
 		currentToken = lexer.nextToken();
 		addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 	}
 	
 	public Alignment createAlignment(String name, int mode, int count, int sourceRestart){
 		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
 	}
 
 	public Alignment createAlignment(String name, int mode, int count, int sourceRestart, boolean adjust){
 		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart, adjust);
 	}
 	
 	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart){
 		return createAlignment(name, mode, tieBreakRule, count, sourceRestart, this.formatter.prefs.continuation_indentation, false);
 	}
 
 	public Alignment createAlignment(String name, int mode, int count, int sourceRestart, int continuationIndent, boolean adjust){
 		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart, continuationIndent, adjust);
 	}
 
 	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart, int continuationIndent, boolean adjust){
 		Alignment alignment = new Alignment(name, mode, tieBreakRule, this, count, sourceRestart, continuationIndent);
 		// adjust break indentation
 		if (adjust && this.memberAlignment != null) {
 			Alignment current = this.memberAlignment;
 			while (current.enclosing != null) {
 				current = current.enclosing;
 			}
 			if ((current.mode & Alignment.M_MULTICOLUMN) != 0) {
 				final int indentSize = this.indentationSize;
 				switch(current.chunkKind) {
 					case Alignment.CHUNK_METHOD :
 					case Alignment.CHUNK_TYPE :
 						if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
 							alignment.breakIndentationLevel = this.indentationLevel + indentSize;
 						} else {
 							alignment.breakIndentationLevel = this.indentationLevel + continuationIndent * indentSize;
 						}
 						alignment.update();
 						break;
 					case Alignment.CHUNK_FIELD :
 						if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
 							alignment.breakIndentationLevel = current.originalIndentationLevel + indentSize;
 						} else {
 							alignment.breakIndentationLevel = current.originalIndentationLevel + continuationIndent * indentSize;
 						}
 						alignment.update();
 						break;
 				}
 			} else {
 				switch(current.mode & Alignment.SPLIT_MASK) {
 					case Alignment.M_COMPACT_SPLIT :
 					case Alignment.M_COMPACT_FIRST_BREAK_SPLIT :
 					case Alignment.M_NEXT_PER_LINE_SPLIT :
 					case Alignment.M_NEXT_SHIFTED_SPLIT :
 					case Alignment.M_ONE_PER_LINE_SPLIT :
 						final int indentSize = this.indentationSize;
 						switch(current.chunkKind) {
 							case Alignment.CHUNK_METHOD :
 							case Alignment.CHUNK_TYPE :
 								if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
 									alignment.breakIndentationLevel = this.indentationLevel + indentSize;
 								} else {
 									alignment.breakIndentationLevel = this.indentationLevel + continuationIndent * indentSize;
 								}
 								alignment.update();
 								break;
 							case Alignment.CHUNK_FIELD :
 								if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
 									alignment.breakIndentationLevel = current.originalIndentationLevel + indentSize;
 								} else {
 									alignment.breakIndentationLevel = current.originalIndentationLevel + continuationIndent * indentSize;
 								}
 								alignment.update();
 								break;
 						}
 						break;
 				}
 			}
 		}
 		return alignment; 
 	}
 
 	public Alignment createMemberAlignment(String name, int mode, int count, int sourceRestart) {
 		Alignment mAlignment = createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
 		mAlignment.breakIndentationLevel = this.indentationLevel;
 		return mAlignment;
 	}
 	
 	public void enterAlignment(Alignment alignment){
 		alignment.enclosing = this.currentAlignment;
 		this.currentAlignment = alignment;
 	}
 
 	public void enterMemberAlignment(Alignment alignment) {
 		alignment.enclosing = this.memberAlignment;
 		this.memberAlignment = alignment;
 	}
 
 	public void exitAlignment(Alignment alignment, boolean discardAlignment){
 		Alignment current = this.currentAlignment;
 		while (current != null){
 			if (current == alignment) break;
 			current = current.enclosing;
 		}
 		if (current == null) {
 			throw new AbortFormatting("could not find matching alignment: "+alignment); //$NON-NLS-1$
 		}
 		this.indentationLevel = alignment.location.outputIndentationLevel;
 		this.numberOfIndentations = alignment.location.numberOfIndentations;
 		if (discardAlignment){ 
 			this.currentAlignment = alignment.enclosing;
 		}
 	}
 	
 	public void exitMemberAlignment(Alignment alignment){
 		Alignment current = this.memberAlignment;
 		while (current != null){
 			if (current == alignment) break;
 			current = current.enclosing;
 		}
 		if (current == null) {
 			throw new AbortFormatting("could not find matching alignment: "+alignment); //$NON-NLS-1$
 		}
 		this.indentationLevel = current.location.outputIndentationLevel;
 		this.numberOfIndentations = current.location.numberOfIndentations;
 		this.memberAlignment = current.enclosing;
 	}
 	
 	public Alignment getAlignment(String name){
 		if (this.currentAlignment != null) {
 			return this.currentAlignment.getAlignment(name);
 		}
 		return null;
 	}
 	
 	/** 
 	 * Answer actual indentation level based on true column position
 	 * @return int
 	 */
 	public int getColumnIndentationLevel() {
 		return this.column - 1;
 	}	
 	
 	public String getEmptyLines(int linesNumber) {
 		StringBuffer buffer = new StringBuffer();
 		if (lastNumberOfNewLines == 0) {
 			linesNumber++; // add an extra line breaks
 			for (int i = 0; i < linesNumber; i++) {
                 if (indentEmptyLines) printIndentationIfNecessary(buffer);
 				buffer.append(this.lineSeparator);
 			}
 			lastNumberOfNewLines += linesNumber;
 			line += linesNumber;
 			column = 1;
 			needSpace = false;
 			this.pendingSpace = false;
 		} else if (lastNumberOfNewLines == 1) {
 			for (int i = 0; i < linesNumber; i++) {
                 if (indentEmptyLines) printIndentationIfNecessary(buffer);
 				buffer.append(this.lineSeparator);
 			}
 			lastNumberOfNewLines += linesNumber;
 			line += linesNumber;
 			column = 1;
 			needSpace = false;
 			this.pendingSpace = false;
 		} else {
 			if ((lastNumberOfNewLines - 1) >= linesNumber) {
 				// there is no need to add new lines
 				return Util.EMPTY_STRING;
 			}
 			final int realNewLineNumber = linesNumber - lastNumberOfNewLines + 1;
 			for (int i = 0; i < realNewLineNumber; i++) {
                 if (indentEmptyLines) printIndentationIfNecessary(buffer);
 				buffer.append(this.lineSeparator);
 			}
 			lastNumberOfNewLines += realNewLineNumber;
 			line += realNewLineNumber;
 			column = 1;
 			needSpace = false;
 			this.pendingSpace = false;
 		}
 		return String.valueOf(buffer);
 	}
 
 	public OptimizedReplaceEdit getLastEdit() {
 		if (this.editsIndex > 0) {
 			return this.edits[this.editsIndex - 1];
 		}
 		return null;
 	}
 	Alignment getMemberAlignment() {
 		return this.memberAlignment;
 	}
 	
 	public String getNewLine() {
 		if (lastNumberOfNewLines >= 1) {
 			column = 1; // ensure that the scribe is at the beginning of a new line
 			return Util.EMPTY_STRING;
 		}
 		line++;
 		lastNumberOfNewLines = 1;
 		column = 1;
 		needSpace = false;
 		this.pendingSpace = false;
 		return this.lineSeparator;
 	}
 
 	/** 
 	 * Answer next indentation level based on column estimated position
 	 * (if column is not indented, then use indentationLevel)
 	 */
 	public int getNextIndentationLevel(int someColumn) {
 		int indent = someColumn - 1;
 		if (indent == 0)
 			return this.indentationLevel;
 		if (this.tabChar == DefaultCodeFormatterOptions.TabChar.TAB) {
 			if (this.useTabsOnlyForLeadingIndents) {
 				return indent;
 			}
 			int rem = indent % this.indentationSize;
 			int addition = rem == 0 ? 0 : this.indentationSize - rem; // round to superior
 			return indent + addition;
 		} else {
 			return indent;
 		}
 	}
 
 	private String getPreserveEmptyLines(int count) {
 		if (count > 0) {
 			if (this.formatter.prefs.number_of_empty_lines_to_preserve != 0) {
 				int linesToPreserve = Math.min(count, this.formatter.prefs.number_of_empty_lines_to_preserve);
 				return this.getEmptyLines(linesToPreserve);
 			} else {
 				return getNewLine();
 			}
 		}
 		return Util.EMPTY_STRING;
 	}
 	
 	public TextEdit getRootEdit() {
 		MultiTextEdit edit = null;
 		int length = this.textRegionEnd - this.textRegionStart + 1;
 		if (this.textRegionStart <= 0) {
 			if (length <= 0) {
 				edit = new MultiTextEdit(0, 0);
 			} else {
 				edit = new MultiTextEdit(0, this.textRegionEnd + 1);
 			}
 		} else {
 			edit = new MultiTextEdit(this.textRegionStart, this.textRegionEnd - this.textRegionStart + 1);
 		}
 		for (int i= 0, max = this.editsIndex; i < max; i++) {
 			OptimizedReplaceEdit currentEdit = edits[i];
 			if (isValidEdit(currentEdit)) {
 				edit.addChild(new ReplaceEdit(currentEdit.offset, currentEdit.length, currentEdit.replacement));
 			}
 		}
 		this.edits = null;
 		return edit;
 	}
 	
 	public void handleLineTooLong() {
 		// search for closest breakable alignment, using tiebreak rules
 		// look for outermost breakable one
 		int relativeDepth = 0, outerMostDepth = -1;
 		Alignment targetAlignment = this.currentAlignment;
 		while (targetAlignment != null){
 			if (targetAlignment.tieBreakRule == Alignment.R_OUTERMOST && targetAlignment.couldBreak()){
 				outerMostDepth = relativeDepth;
 			}
 			targetAlignment = targetAlignment.enclosing;
 			relativeDepth++;
 		}
 		if (outerMostDepth >= 0) {
 			throw new AlignmentException(AlignmentException.LINE_TOO_LONG, outerMostDepth);
 		}
 		// look for innermost breakable one
 		relativeDepth = 0;
 		targetAlignment = this.currentAlignment;
 		while (targetAlignment != null){
 			if (targetAlignment.couldBreak()){
 				throw new AlignmentException(AlignmentException.LINE_TOO_LONG, relativeDepth);
 			}
 			targetAlignment = targetAlignment.enclosing;
 			relativeDepth++;
 		}
 		// did not find any breakable location - proceed
 	}
 
 	public void indent() {
 		this.indentationLevel += this.indentationSize;
 		this.numberOfIndentations++;
 	}	
 
 	/**
 	 * @param compilationUnitSource
 	 */
 	public void initializeScanner(String src) {
 		restartLexer(src);
 		scannerEndPosition = src.length();
 		lexer.reset(0, scannerEndPosition);
 		edits = new OptimizedReplaceEdit[INITIAL_SIZE];
 	}	
 	
 	private boolean isValidEdit(OptimizedReplaceEdit edit) {
 		final int editLength= edit.length;
 		final int editReplacementLength= edit.replacement.length();
 		final int editOffset= edit.offset;
 		if (editLength != 0) {
 			if (this.textRegionStart <= editOffset && (editOffset + editLength - 1) <= this.textRegionEnd) {
 				if (editReplacementLength != 0 && editLength == editReplacementLength) {
 					for (int i = editOffset, max = editOffset + editLength; i < max; i++) {
 						if (lexer.input[i] != edit.replacement.charAt(i - editOffset)) {
 							return true;
 						}
 					}
 					return false;
 				} else {
 					return true;
 				}
 			} else if (editOffset + editLength == this.textRegionStart) {
 				int i = editOffset;
 				for (int max = editOffset + editLength; i < max; i++) {
 					int replacementStringIndex = i - editOffset;
 					if (replacementStringIndex >= editReplacementLength || lexer.input[i] != edit.replacement.charAt(replacementStringIndex)) {
 						break;
 					}
 				}
 				if (i - editOffset != editReplacementLength && i != editOffset + editLength - 1) {
 					edit.offset = textRegionStart;
 					edit.length = 0;
 					edit.replacement = edit.replacement.substring(i - editOffset);
 					return true;
 				}
 			}
 		} else if (this.textRegionStart <= editOffset && editOffset <= this.textRegionEnd) {
 			return true;
 		} else if (editOffset == this.scannerEndPosition && editOffset == this.textRegionEnd + 1) {
 			return true;
 		}
 		return false;
 	}
 
 	private void preserveEmptyLines(int count, int insertPosition) {
 		if (count > 0) {
 			if (this.formatter.prefs.number_of_empty_lines_to_preserve != 0) {
 				int linesToPreserve = Math.min(count, this.formatter.prefs.number_of_empty_lines_to_preserve);
 				this.printEmptyLines(linesToPreserve, insertPosition);
 			} else {
 				printNewLine(insertPosition);
 			}
 		}
 	}
 
 	public void print(char[] s, boolean considerSpaceIfAny) {
 		if (checkLineWrapping && s.length + column > this.pageWidth) {
 			//handleLineTooLong();
 		}
 		this.lastNumberOfNewLines = 0;
 		printIndentationIfNecessary();
 		if (considerSpaceIfAny) {
 			this.space();
 		}
 		if (this.pendingSpace) {
 			this.addInsertEdit(lexer.token.ptr, " "); //$NON-NLS-1$
 		}
 		this.pendingSpace = false;	
 		this.needSpace = false;		
 		column += s.length;
 		needSpace = true;
 	}
 	
 	public void dontFormat(int startPosition, int length)
 	{
 		int endPosition = startPosition + length;
 		
 		lexer.reset(startPosition, endPosition - 1);
 		int currentCharacter;
 		boolean isNewLine = false;
 		int start = startPosition;
 		int nextCharacterStart = startPosition;
 		printIndentationIfNecessary();
 		if (this.pendingSpace)
 			this.addInsertEdit(startPosition, " "); //$NON-NLS-1$
 		this.needSpace = false;		
 		this.pendingSpace = false;		
 		int previousStart = startPosition;
 
 		while (nextCharacterStart <= endPosition && (currentCharacter = nextChar()) != -1) {
 			nextCharacterStart = lexer.p;
 
 			switch(currentCharacter) {
 				case '\r' :
 					start = previousStart;
 					isNewLine = true;
 					if (isNextChar('\n')) {
 						currentCharacter = '\n';
 						nextCharacterStart = lexer.p;
 					}
 					break;
 				case '\n' :
 					start = previousStart;
 					isNewLine = true;
 					break;
 				default:
 					if (isNewLine) {
 						if (ScannerHelper.isWhitespace((char) currentCharacter)) {
 							int previousStartPosition = lexer.p;
 							if (currentCharacter == '\r' || currentCharacter == '\n') {
 								nextCharacterStart = previousStartPosition;
 							}
 						}
 						this.column = 1;
 						this.line++;
 						addReplaceEdit(start, previousStart - 1, lineSeparator);
 					} else {
 						this.column += (nextCharacterStart - previousStart);
 					}
 					isNewLine = false;
 			}
 			previousStart = nextCharacterStart;
 			lexer.p = nextCharacterStart;
 		}
 		this.lastNumberOfNewLines = 0;
 		needSpace = false;
 		lexer.reset(endPosition, this.scannerEndPosition - 1);
 	}
 	
 	private void printBlockComment(char[] s, boolean isJavadoc) {
 		int currentTokenStartPosition = lexer.token.ptr;
 		int currentTokenEndPosition = currentTokenEndPosition() + 1;
 		
 		lexer.reset(currentTokenStartPosition, currentTokenEndPosition - 1);
 		int currentCharacter;
 		boolean isNewLine = false;
 		int start = currentTokenStartPosition;
 		int nextCharacterStart = currentTokenStartPosition;
 		printIndentationIfNecessary();
 		if (this.pendingSpace) {
 			this.addInsertEdit(currentTokenStartPosition, " "); //$NON-NLS-1$
 		}
 		this.needSpace = false;		
 		this.pendingSpace = false;		
 		int previousStart = currentTokenStartPosition;
 
 		while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter = nextChar()) != -1) {
 			nextCharacterStart = lexer.p;
 
 			switch(currentCharacter) {
 				case '\r' :
 					start = previousStart;
 					isNewLine = true;
 					if (isNextChar('\n')) {
 						currentCharacter = '\n';
 						nextCharacterStart = lexer.p;
 					}
 					break;
 				case '\n' :
 					start = previousStart;
 					isNewLine = true;
 					break;
 				default:
 					if (isNewLine) {
 						if (ScannerHelper.isWhitespace((char) currentCharacter)) {
 							int previousStartPosition = lexer.p;
 							while(currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n' && ScannerHelper.isWhitespace((char) currentCharacter)) {
 								previousStart = nextCharacterStart;
 								previousStartPosition = lexer.p;
 								currentCharacter = nextChar();
 								nextCharacterStart = lexer.p;
 							}
 							if (currentCharacter == '\r' || currentCharacter == '\n') {
 								nextCharacterStart = previousStartPosition;
 							}
 						}
 						this.column = 1;
 						this.line++;
 
 						StringBuffer buffer = new StringBuffer();
 						buffer.append(this.lineSeparator);
 						printIndentationIfNecessary(buffer);
 						buffer.append(' ');
 				
 						addReplaceEdit(start, previousStart - 1, String.valueOf(buffer));
 					} else {
 						this.column += (nextCharacterStart - previousStart);
 					}
 					isNewLine = false;
 			}
 			previousStart = nextCharacterStart;
 			lexer.p = nextCharacterStart;
 		}
 		this.lastNumberOfNewLines = 0;
 		needSpace = false;
 		lexer.reset(currentTokenEndPosition, this.scannerEndPosition - 1);
 		if (isJavadoc) {
 			printNewLine();
 		}
 	}
 	
 	public void printEndOfCompilationUnit() {
 			// if we have a space between two tokens we ensure it will be dumped in the formatted string
 			int currentTokenStartPosition = lexer.p;
 			boolean hasComment = false;
 			boolean hasLineComment = false;
 			boolean hasWhitespace = false;
 			int count = 0;
 			while (true) {
 				this.currentToken = lexer.nextToken();
 				switch(this.currentToken) {
 					case TOKwhitespace :
 						char[] whiteSpaces = lexer.token.getRawTokenSource();
 						count = 0;
 						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
 							switch(whiteSpaces[i]) {
 								case '\r' :
 									if ((i + 1) < max) {
 										if (whiteSpaces[i + 1] == '\n') {
 											i++;
 										}
 									}
 									count++;
 									break;
 								case '\n' :
 									count++;
 							}
 						}
 						if (count == 0) {
 							hasWhitespace = true;
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						} else if (hasComment) {
 							if (count == 1) {
 								this.printNewLine(lexer.token.ptr);
 							} else {
 								preserveEmptyLines(count - 1, lexer.token.ptr);
 							}
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						} else if (hasLineComment) {
 							this.preserveEmptyLines(count, lexer.token.ptr);
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						} else {
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						}
 						currentTokenStartPosition = lexer.p;						
 						break;
 					case TOKlinecomment:
 					case TOKdoclinecomment:
 						if (count >= 1) {
 							if (count > 1) {
 								preserveEmptyLines(count - 1, lexer.token.ptr);
 							} else if (count == 1) {
 								printNewLine(lexer.token.ptr);
 							}
 						} else if (hasWhitespace) {
 							space();
 						} 
 						hasWhitespace = false;
 						this.printCommentLine(lexer.token.getRawTokenSource());
 						currentTokenStartPosition = lexer.p;
 						hasLineComment = true;		
 						count = 0;
 						break;
 					case TOKblockcomment:
 					case TOKdocblockcomment:
 					case TOKpluscomment:
 					case TOKdocpluscomment:
 						if (count >= 1) {
 							if (count > 1) {
 								preserveEmptyLines(count - 1, lexer.token.ptr);
 							} else if (count == 1) {
 								printNewLine(lexer.token.ptr);
 							}
 						} else if (hasWhitespace) {
 							space();
 						} 
 						hasWhitespace = false;
 						this.printBlockComment(lexer.token.getRawTokenSource(), false);
 						currentTokenStartPosition = lexer.p;
 						hasLineComment = false;
 						hasComment = true;
 						count = 0;
 						break;
 					case TOKsemicolon:
 						char[] currentTokenSource = lexer.token.getRawTokenSource();
 						this.print(currentTokenSource, this.formatter.prefs.insert_space_before_semicolon);
 						break;
 					case TOKeof:
 						if (count >= 1 || this.formatter.prefs.insert_new_line_at_end_of_file_if_missing) {
 							this.printNewLine(this.scannerEndPosition);
 						}
 						return;
 					default :
 						// step back one token
 						lexer.reset(currentTokenStartPosition, this.scannerEndPosition - 1);
 						return;
 				}
 			}
 	}
 
 	public void printComment() {
 			// if we have a space between two tokens we ensure it will be dumped in the formatted string
 			int currentTokenStartPosition = lexer.p;
 			boolean hasComment = false;
 			boolean hasLineComment = false;
 			boolean hasWhitespace = false;
 			int count = 0;
 			while ((this.currentToken = this.lexer.nextToken()) != TOK.TOKeof) {
 				switch(this.currentToken) {
 					case TOKwhitespace :
 						char[] whiteSpaces = this.lexer.token.getRawTokenSource();
 						count = 0;
 						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
 							switch(whiteSpaces[i]) {
 								case '\r' :
 									if ((i + 1) < max) {
 										if (whiteSpaces[i + 1] == '\n') {
 											i++;
 										}
 									}
 									count++;
 									break;
 								case '\n' :
 									count++;
 							}
 						}
 						if (count == 0) {
 							hasWhitespace = true;
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						} else if (hasComment) {
 							if (count == 1) {
 								this.printNewLine(lexer.token.ptr);
 							} else {
 								preserveEmptyLines(count - 1, lexer.token.ptr);
 							}
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						} else if (hasLineComment) {
 							this.preserveEmptyLines(count, lexer.token.ptr);
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						} else if (count != 0 && this.formatter.prefs.number_of_empty_lines_to_preserve != 0) {
 							addReplaceEdit(lexer.token.ptr, currentTokenEndPosition(), this.getPreserveEmptyLines(count - 1));
 						} else {
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						}
 						currentTokenStartPosition = lexer.p;						
 						break;
 					case TOKlinecomment:
 					case TOKdoclinecomment:
 						if (count >= 1) {
 							if (count > 1) {
 								preserveEmptyLines(count - 1, lexer.token.ptr);
 							} else if (count == 1) {
 								printNewLine(lexer.token.ptr);
 							}
 						} else if (hasWhitespace) {
 							space();
 						} 
 						hasWhitespace = false;
 						this.printCommentLine(lexer.token.getRawTokenSource());
 						currentTokenStartPosition = lexer.p;
 						hasLineComment = true;		
 						count = 0;
 						break;
 					case TOKblockcomment:
 					case TOKdocblockcomment:
 					case TOKpluscomment:
 					case TOKdocpluscomment:
 						if (count >= 1) {
 							if (count > 1) {
 								preserveEmptyLines(count - 1, lexer.token.ptr);
 							} else if (count == 1) {
 								printNewLine(lexer.token.ptr);
 							}
 						} else if (hasWhitespace) {
 							space();
 						} 
 						hasWhitespace = false;
 						this.printBlockComment(lexer.token.getRawTokenSource(), false);
 						currentTokenStartPosition = lexer.p;
 						hasLineComment = false;
 						hasComment = true;
 						count = 0;
 						break;
 					default :
 						// step back one token
 						lexer.reset(currentTokenStartPosition, this.scannerEndPosition - 1);
 						return;
 				}
 			}
 	}
 	
 	private void printCommentLine(char[] s) {
 		int currentTokenStartPosition = lexer.token.ptr;
 		int currentTokenEndPosition = currentTokenEndPosition() + 1;
 		lexer.reset(currentTokenStartPosition, currentTokenEndPosition - 1);
 		int currentCharacter;
 		int start = currentTokenStartPosition;
 		int nextCharacterStart = currentTokenStartPosition;
 		printIndentationIfNecessary();
 		if (this.pendingSpace) {
 			this.addInsertEdit(currentTokenStartPosition, " "); //$NON-NLS-1$
 		}
 		this.needSpace = false;		
 		this.pendingSpace = false;		
 		int previousStart = currentTokenStartPosition;
 
 		loop: while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter = nextChar()) != -1) {
 			nextCharacterStart = lexer.p;
 
 			switch(currentCharacter) {
 				case '\r' :
 					start = previousStart;
 					break loop;
 				case '\n' :
 					start = previousStart;
 					break loop;
 			}
 			previousStart = nextCharacterStart;
 		}
 		if (start != currentTokenStartPosition) {
 			addReplaceEdit(start, currentTokenEndPosition - 1, lineSeparator);
 		}
 		line++; 
 		column = 1;
 		needSpace = false;
 		this.pendingSpace = false;
 		lastNumberOfNewLines = 1;
 		// realign to the proper value
 		if (this.currentAlignment != null) {
 			if (this.memberAlignment != null) {
 				// select the last alignment
 				if (this.currentAlignment.location.inputOffset > this.memberAlignment.location.inputOffset) {
 					if (this.currentAlignment.couldBreak() && this.currentAlignment.wasSplit) {
 						this.currentAlignment.performFragmentEffect();
 					}
 				} else {
 					this.indentationLevel = Math.max(this.indentationLevel, this.memberAlignment.breakIndentationLevel);
 				}
 			} else if (this.currentAlignment.couldBreak() && this.currentAlignment.wasSplit) {
 				this.currentAlignment.performFragmentEffect();
 			}
 		}
 		lexer.reset(currentTokenEndPosition, this.scannerEndPosition - 1);
 	}
 	public void printEmptyLines(int linesNumber) {
 		this.printEmptyLines(linesNumber, currentTokenEndPosition() + 1);
 	}
 
 	private void printEmptyLines(int linesNumber, int insertPosition) {
         final String buffer = getEmptyLines(linesNumber);
         if (Util.EMPTY_STRING == buffer) return;
         
 		addInsertEdit(insertPosition, buffer);
 	}
 
 	void printIndentationIfNecessary() {
 		StringBuffer buffer = new StringBuffer();
 		printIndentationIfNecessary(buffer);
 		if (buffer.length() > 0) {
 			addInsertEdit(lexer.token.ptr, buffer.toString());
 			this.pendingSpace = false;
 		}
 	}
 
 	private void printIndentationIfNecessary(StringBuffer buffer) {
 		switch(this.tabChar) {
 			case TAB :
 				boolean useTabsForLeadingIndents = this.useTabsOnlyForLeadingIndents;
 				int numberOfLeadingIndents = this.numberOfIndentations;
 				int indentationsAsTab = 0;
 				if (useTabsForLeadingIndents) {
 					while (this.column <= this.indentationLevel) {
 						if (indentationsAsTab < numberOfLeadingIndents) {
 							buffer.append('\t');
 							indentationsAsTab++;
 							this.lastNumberOfNewLines = 0;
 							int complement = this.tabLength - ((this.column - 1) % this.tabLength); // amount of space
 							this.column += complement;
 							this.needSpace = false;
 						} else {
 							buffer.append(' ');
 							this.column++;
 							this.needSpace = false;
 						}
 					}
 				} else {
 					while (this.column <= this.indentationLevel) {
 						buffer.append('\t');
 						this.lastNumberOfNewLines = 0;
 						int complement = this.tabLength - ((this.column - 1) % this.tabLength); // amount of space
 						this.column += complement;
 						this.needSpace = false;
 					}
 				}
 				break;
 			case SPACE :
 				while (this.column <= this.indentationLevel) {
 					buffer.append(' ');
 					this.column++;
 					this.needSpace = false;
 				}
 				break;
 			case MIXED :
 				useTabsForLeadingIndents = this.useTabsOnlyForLeadingIndents;
 				numberOfLeadingIndents = this.numberOfIndentations;
 				indentationsAsTab = 0;
 				if (useTabsForLeadingIndents) {
 					final int columnForLeadingIndents = numberOfLeadingIndents * this.indentationSize;
 					while (this.column <= this.indentationLevel) {
 						if (this.column <= columnForLeadingIndents) {
 							if ((this.column - 1 + this.tabLength) <= this.indentationLevel) {
 								buffer.append('\t');
 								this.column += this.tabLength;
 							} else if ((this.column - 1 + this.indentationSize) <= this.indentationLevel) {
 								// print one indentation
 								for (int i = 0, max = this.indentationSize; i < max; i++) {
 									buffer.append(' ');
 									this.column++;
 								}
 							} else {
 								buffer.append(' ');
 								this.column++;
 							}
 						} else {
 							for (int i = this.column, max = this.indentationLevel; i <= max; i++) {
 								buffer.append(' ');
 								this.column++;
 							}
 						}
 						this.needSpace = false;
 					}
 				} else {
 					while (this.column <= this.indentationLevel) {
 						if ((this.column - 1 + this.tabLength) <= this.indentationLevel) {
 							buffer.append('\t');
 							this.column += this.tabLength;
 						} else if ((this.column - 1 + this.indentationSize) <= this.indentationLevel) {
 							// print one indentation
 							for (int i = 0, max = this.indentationSize; i < max; i++) {
 								buffer.append(' ');
 								this.column++;
 							}
 						} else {
 							buffer.append(' ');
 							this.column++;
 						}
 						this.needSpace = false;
 					}
 				}
 				break;
 		}
 	}
 	
 	public void printNewLine() {
 		if (lastNumberOfNewLines >= 1) {
 			column = 1; // ensure that the scribe is at the beginning of a new line
 			return;
 		}
 		addInsertEdit(currentTokenEndPosition() + 1, this.lineSeparator);
 		line++;
 		lastNumberOfNewLines = 1;
 		column = 1;
 		needSpace = false;
 		this.pendingSpace = false;
 	}
 
 	public void printNewLine(int insertPosition) {
 		if (lastNumberOfNewLines >= 1) {
 			column = 1; // ensure that the scribe is at the beginning of a new line
 			return;
 		}
 		addInsertEdit(insertPosition, this.lineSeparator);
 		line++;
 		lastNumberOfNewLines = 1;
 		column = 1;
 		needSpace = false;
 		this.pendingSpace = false;
 	}
 	
 	public void printAnyToken()
 	{
 		printAnyToken(false);
 	}
 	
 	public void printAnyToken(boolean considerSpaceIfAny)
 	{
 		printComment();
 		char[] currentTokenSource = lexer.token.getRawTokenSource();
 		this.print(currentTokenSource, considerSpaceIfAny);
 	}
 	
 	public void printNextToken(TOK expectedTokenType){
 		printNextToken(expectedTokenType, false);
 	}
 
 	public void printNextToken(TOK expectedTokenType, boolean considerSpaceIfAny){
 		printComment();
 			this.currentToken = lexer.nextToken();
 			char[] currentTokenSource = lexer.token.getRawTokenSource();
 			if (expectedTokenType != this.currentToken) {
 				throw new AbortFormatting("unexpected token type, expecting:"+expectedTokenType+", actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
 			}
 			this.print(currentTokenSource, considerSpaceIfAny);
 	}
 
 	public void printNextToken(TOK[] expectedTokenTypes) {
 		printNextToken(expectedTokenTypes, false);
 	}
 
 	public void printNextToken(TOK[] expectedTokenTypes, boolean considerSpaceIfAny){
 		printComment();
 			this.currentToken = lexer.nextToken();
 			char[] currentTokenSource = lexer.token.getRawTokenSource();
 			if (Arrays.binarySearch(expectedTokenTypes, this.currentToken) < 0) {
 				StringBuffer expectations = new StringBuffer(5);
 				for (int i = 0; i < expectedTokenTypes.length; i++){
 					if (i > 0) {
 						expectations.append(',');
 					}
 					expectations.append(expectedTokenTypes[i]);
 				}				
 				throw new AbortFormatting("unexpected token type, expecting:["+expectations.toString()+"], actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
 			}
 			this.print(currentTokenSource, considerSpaceIfAny);
 	}
 	
 	private void printRule(StringBuffer stringBuffer) {
 		for (int i = 0; i < this.pageWidth; i++){
 			if ((i % this.tabLength) == 0) { 
 				stringBuffer.append('+');
 			} else {
 				stringBuffer.append('-');
 			}
 		}
 		stringBuffer.append(this.lineSeparator);
 		
 		for (int i = 0; i < (pageWidth / tabLength); i++) {
 			stringBuffer.append(i);
 			stringBuffer.append('\t');
 		}			
 	}
 
 	public void printTrailingComment() {
 			// if we have a space between two tokens we ensure it will be dumped in the formatted string
 			int currentTokenStartPosition = lexer.p;
 			boolean hasWhitespaces = false;
 			boolean hasComment = false;
 			boolean hasLineComment = false;
 			while ((this.currentToken = lexer.nextToken()) != TOK.TOKeof) {
 				switch(this.currentToken) {
 					case TOKwhitespace :
 						int count = 0;
 						char[] whiteSpaces = lexer.token.getRawTokenSource();
 						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
 							switch(whiteSpaces[i]) {
 								case '\r' :
 									if ((i + 1) < max) {
 										if (whiteSpaces[i + 1] == '\n') {
 											i++;
 										}
 									}
 									count++;
 									break;
 								case '\n' :
 									count++;
 							}
 						}
 						if (hasLineComment) {
 							if (count >= 1) {
 								currentTokenStartPosition = lexer.token.ptr;
 								this.preserveEmptyLines(count, currentTokenStartPosition);
 								addDeleteEdit(currentTokenStartPosition, currentTokenEndPosition());
 								lexer.reset(lexer.p, this.scannerEndPosition - 1);
 								return;
 							} else {
 								lexer.reset(currentTokenStartPosition, this.scannerEndPosition - 1);
 								return;
 							}
 						} else if (count >= 1) {
 							if (hasComment) {
 								this.printNewLine(lexer.token.ptr);
 							}
 							lexer.reset(currentTokenStartPosition, this.scannerEndPosition - 1);
 							return;
 						} else {
 							hasWhitespaces = true;
 							currentTokenStartPosition = lexer.p;						
 							addDeleteEdit(lexer.token.ptr, currentTokenEndPosition());
 						}
 						break;
 					case TOKlinecomment:
 					case TOKdoclinecomment:
 						if (hasWhitespaces) {
 							space();
 						}
 						this.printCommentLine(lexer.token.getRawTokenSource());
 						currentTokenStartPosition = lexer.p;
 						hasLineComment = true;
 						break;
 					case TOKblockcomment:
 					case TOKdocblockcomment:
 					case TOKpluscomment:
 					case TOKdocpluscomment:
 						if (hasWhitespaces) {
 							space();
 						}
 						this.printBlockComment(lexer.token.getRawTokenSource(), false);
 						currentTokenStartPosition = lexer.p;
 						hasComment = true;
 						break;
 					default :
 						// step back one token
 						lexer.reset(currentTokenStartPosition, this.scannerEndPosition - 1);
 						return;
 				}
 			}
 	}
 
 	void redoAlignment(AlignmentException e){
 		if (e.relativeDepth > 0) { // if exception targets a distinct context
 			e.relativeDepth--; // record fact that current context got traversed
 			this.currentAlignment = this.currentAlignment.enclosing; // pop currentLocation
 			throw e; // rethrow
 		} 
 		// reset scribe/scanner to restart at this given location
 		this.resetAt(this.currentAlignment.location);
 		lexer.reset(this.currentAlignment.location.inputOffset, lexer.end); //this.scanner.eofPosition);
 		// clean alignment chunkKind so it will think it is a new chunk again
 		this.currentAlignment.chunkKind = 0;
 	}
 
 	void redoMemberAlignment(AlignmentException e){
 		// reset scribe/scanner to restart at this given location
 		this.resetAt(this.memberAlignment.location);
 		lexer.reset(this.memberAlignment.location.inputOffset, lexer.end); //this.scanner.eofPosition);
 		// clean alignment chunkKind so it will think it is a new chunk again
 		this.memberAlignment.chunkKind = 0;
 	}
 
 	public void reset() {
 		this.checkLineWrapping = true;
 		this.line = 0;
 		this.column = 1;
 		this.editsIndex = 0;
 	}
 		
 	private void resetAt(Location location) {
 		this.line = location.outputLine;
 		this.column = location.outputColumn;
 		this.indentationLevel = location.outputIndentationLevel;
 		this.numberOfIndentations = location.numberOfIndentations;
 		this.lastNumberOfNewLines = location.lastNumberOfNewLines;
 		this.needSpace = location.needSpace;
 		this.pendingSpace = location.pendingSpace;
 		this.editsIndex = location.editsIndex;
 		if (this.editsIndex > 0) {
 			this.edits[this.editsIndex - 1] = location.textEdit;
 		}
 	}
 
 	private void resize() {
 		System.arraycopy(this.edits, 0, (this.edits = new OptimizedReplaceEdit[this.editsIndex * 2]), 0, this.editsIndex);
 	}
 
 	public void space() {
 		if (!this.needSpace) return;
 		this.lastNumberOfNewLines = 0;
 		this.pendingSpace = true;
 		this.column++;
 		this.needSpace = false;		
 	}
 
 	public String toString() {
 		StringBuffer stringBuffer = new StringBuffer();
 		stringBuffer
 			.append("(page width = " + this.pageWidth + ") - (tabChar = ");//$NON-NLS-1$//$NON-NLS-2$
 		switch(this.tabChar) {
 			case TAB :
 				 stringBuffer.append("TAB");//$NON-NLS-1$
 				 break;
 			case SPACE :
 				 stringBuffer.append("SPACE");//$NON-NLS-1$
 				 break;
 			default :
 				 stringBuffer.append("MIXED");//$NON-NLS-1$
 		}
 		stringBuffer
 			.append(") - (tabSize = " + this.tabLength + ")")//$NON-NLS-1$//$NON-NLS-2$
 			.append(this.lineSeparator)
 			.append("(line = " + this.line + ") - (column = " + this.column + ") - (identationLevel = " + this.indentationLevel + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
 			.append(this.lineSeparator)
 			.append("(needSpace = " + this.needSpace + ") - (lastNumberOfNewLines = " + this.lastNumberOfNewLines + ") - (checkLineWrapping = " + this.checkLineWrapping + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
 			.append(this.lineSeparator)
 			.append("==================================================================================")	//$NON-NLS-1$
 			.append(this.lineSeparator);
 		printRule(stringBuffer);
 		return stringBuffer.toString();
 	}
 	
 	public void unIndent() {
 		this.indentationLevel -= this.indentationSize;
 		this.numberOfIndentations--;
 	}
 	
 	private int nextChar()
 	{
 		try
 		{
 			return lexer.input[lexer.p++];
 		}
 		catch(IndexOutOfBoundsException e)
 		{
 			return -1;
 		}
 	}
 	
 	private boolean isNextChar(char test)
 	{
 		if(nextChar() == test)
 			return true;
 		lexer.p--;
 		return false;
 	}
 	
 	private int currentTokenEndPosition()
 	{
 		return lexer.p - 1;
 	}
 }
