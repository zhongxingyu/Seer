 package io.scan;
 
 import java.io.*;
 import java.util.LinkedList;
 
 import base.SourceLocation;
 import io.QuietCloser;
 
 /**
  * @author Anton Chepurov
  */
 public class LexemeComposer {
 
 	private char lastReadChar;
 	private BufferedReader bReader;
 	private boolean toUpperCase = true;
 	private final VHDLLinesTracker vhdlLinesTracker;
 
 	private static final char END_OF_FILE = 65535;
 	public static final String DEFAULT_COMMENT = "--";
 
 	public LexemeComposer(String sourceString, boolean toUpperCase) {
 		this(sourceString);
 		this.toUpperCase = toUpperCase;
 	}
 
 	public LexemeComposer(File sourceFile, boolean toUpperCase) throws FileNotFoundException {
 		this(sourceFile);
 		this.toUpperCase = toUpperCase;
 	}
 
 	/**
 	 * Constructor with <code>toUpperCase == true</code> by default
 	 *
 	 * @param sourceString the source of lexemes
 	 */
 	public LexemeComposer(String sourceString) {
 		this(new StringReader(sourceString), null);
 	}
 
 	/**
 	 * Constructor with <code>toUpperCase == true</code> by default
 	 *
 	 * @param sourceFile the source of lexemes
 	 * @throws FileNotFoundException see {@link java.io.FileReader#FileReader(java.io.File)}
 	 */
 	public LexemeComposer(File sourceFile) throws FileNotFoundException {
 		this(new FileReader(sourceFile), sourceFile);
 	}
 
 	/**
 	 * Constructor with <code>toUpperCase == true</code> by default
 	 *
 	 * @param stream the source of lexemes
 	 */
 	public LexemeComposer(InputStream stream) {
 		this(new InputStreamReader(stream), null);
 	}
 
 	private LexemeComposer(Reader reader, File sourceFile) {
 		bReader = new BufferedReader(reader);
 		vhdlLinesTracker = new VHDLLinesTracker(sourceFile);
 	}
 
 
 	/**
 	 * Reads next lexeme out of sourceString/sourceFile.
 	 * The set of all possible lexemes is defined by {@link LexemeType}.
 	 *
 	 * @return next read lexeme in UpperCase or null if EOF is reached
 	 * @throws java.io.IOException if an I/O error occurs
 	 * @throws java.lang.Exception {@link LexemeType#diagnoseType(char)}
 	 */
 	public Lexeme nextLexeme() throws Exception {
 		StringBuilder newLexemeValue = new StringBuilder();
 		LexemeType newLexemeType = null;
 
 		/* Start reading a new character (without checking the lastReadChar)
 		* either if it is the first scan or the lastReadChar was a whitespace */
 		boolean ignoreLastReadChar = Character.isWhitespace(lastReadChar) || lastReadChar == 0;
 		while (true) {
 			/* Read next char */
 			if (ignoreLastReadChar) {
 				lastReadChar = (char) bReader.read();
 				/* Append EVERY NEW READ char to VHDLLinesTracker */
 				vhdlLinesTracker.append(lastReadChar);
 			}
 			ignoreLastReadChar = true;
 			/* Check EOF */
 			if (lastReadChar == END_OF_FILE) {
 				return newLexemeValue.length() == 0 ? null : new Lexeme(applyCase(newLexemeValue), newLexemeType);
 			}
 
 			/* Skip whitespace-s that precede the lexeme */
 			if (newLexemeValue.length() == 0 && Character.isWhitespace(lastReadChar)) continue;
 			/* Define the TYPE of the lexeme. Do it only once, right at the beginning. */
 			if (newLexemeType == null) {
 				newLexemeType = LexemeType.diagnoseType(lastReadChar);
 			}
 			/* Append the character */
 			newLexemeValue.append(lastReadChar);
 			/* Check COMMENTS */
 			if (newLexemeType == LexemeType.OP_SUBTR && newLexemeValue.toString().equals(DEFAULT_COMMENT)) {
 				bReader.readLine();
				vhdlLinesTracker.newLine();
 				newLexemeType = null;
 				newLexemeValue = new StringBuilder();
 				continue;
 			}
 			/* Check if LexemeType accepts appended character  */
 			if (!newLexemeType.accepts(newLexemeValue.toString())) {
 				newLexemeValue.deleteCharAt(newLexemeValue.length() - 1);
 				break;
 			}
 		}
 
 		return new Lexeme(applyCase(newLexemeValue), newLexemeType);
 	}
 
 	private String applyCase(StringBuilder line) {
 		return toUpperCase ? line.toString().toUpperCase() : line.toString();
 	}
 
 	public void close() {
 		QuietCloser.closeQuietly(bReader);
 	}
 
 	public SourceLocation getCurrentSource() {
 		return vhdlLinesTracker.getCurrentSource();
 	}
 
 	public void purgeCurrentLines() {
 		vhdlLinesTracker.purgeCurrentLines();
 	}
 
 	public int getCurrentLineCount() {
 		return vhdlLinesTracker.getCurrentLineCount();
 	}
 
 	private class VHDLLinesTracker {
 		private static final char NEW_LINE_UNIX = '\n';
 		private static final char NEW_LINE_MAC = '\r';
 		private static final char END_OF_FILE = 65535;
 
 		/**
 		 * Currently processed VHDL lines.
 		 * Not a single line, but a list of lines, since a single
 		 * {@link io.scan.VHDLToken} can span across multiple lines.
 		 */
 		private LinkedList<Integer> currentLines = new LinkedList<Integer>();
 
 		/* Fields for the line currently under processing */
 		private char lastChar;
 		private StringBuilder line;
 		private int currentLineCount;
 		private final File sourceFile;
 
 		private VHDLLinesTracker(File sourceFile) {
 			this.sourceFile = sourceFile;
 			lastChar = 0;
 			currentLineCount = 1;
 			line = new StringBuilder();
 		}
 
 		public SourceLocation getCurrentSource() {
 			/* Add any non-empty lines being currently under processing */
 			addLineToCurrentLines();
 			return currentLines.isEmpty() ? null : new SourceLocation(sourceFile, currentLines);
 		}
 
 		private void addLineToCurrentLines() {
 			String trimmedLine = line.toString().trim();
 			if (trimmedLine.length() > 0) {
 				/* Add all lines but comments to currentLines */
 				if (!trimmedLine.startsWith(DEFAULT_COMMENT)) {
 					/* Add to currentLines */
 					currentLines.add(currentLineCount);
 				}
 				/* Init new empty line */
 				line = new StringBuilder();
 			}
 		}
 
 		public void purgeCurrentLines() {
 			currentLines = new LinkedList<Integer>();
 			line = new StringBuilder();
 		}
 
		public void newLine() {
			addLineToCurrentLines();
			currentLineCount++;
		}

 		public void append(char newChar) {
 			/* Count lines */
 			if (isNewLine(newChar)) {
				newLine();
 			}
 			/* Append the character */
 			if (newChar != END_OF_FILE) {
 				line.append(newChar);
 			}
 			/* Save last character */
 			lastChar = newChar;
 		}
 
 		int getCurrentLineCount() {
 			return currentLineCount;
 		}
 
 		/**
 		 * New lines: {@link #NEW_LINE_MAC}, {@link #NEW_LINE_UNIX} or {@link #NEW_LINE_MAC} + {@link #NEW_LINE_UNIX}.<br>
 		 * Thus, new line is detected if either<br>
 		 * 1) <code>newChar</code> is {@link #NEW_LINE_MAC} or<br>
 		 * 2) <code>newChar</code> is {@link #NEW_LINE_UNIX} and the lastChar is not {@link #NEW_LINE_MAC}.<br><br>
 		 * The last condition allows avoiding counting the new line twice for {@link #NEW_LINE_MAC} + {@link #NEW_LINE_UNIX}.
 		 *
 		 * @param newChar newly appended character
 		 * @return <code>true</code> if the <code>newChar</code> denotes a new line
 		 */
 		private boolean isNewLine(char newChar) {
 			return newChar == NEW_LINE_MAC || (newChar == NEW_LINE_UNIX && lastChar != NEW_LINE_MAC);
 		}
 
 	}
 }
