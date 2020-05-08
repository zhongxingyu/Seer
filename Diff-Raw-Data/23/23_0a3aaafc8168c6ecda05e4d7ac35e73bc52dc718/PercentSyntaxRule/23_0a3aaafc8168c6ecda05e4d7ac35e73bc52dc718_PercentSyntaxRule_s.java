 package org.rubypeople.rdt.internal.ui.text;
 
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.Token;
 
 public class PercentSyntaxRule implements IRule {
 
 	public IToken evaluate(ICharacterScanner scanner) {
 		int c = scanner.read();
 		if (((char) c) == '%') {
 			c = scanner.read(); // get next char
 			switch ((char) c) {
 			case 'r': // regular expression
 				return detectToken(scanner, IRubyPartitions.RUBY_REGEX);
 			case 'x': // commands
 				return detectToken(scanner, IRubyPartitions.RUBY_COMMAND);
 			case 'q':
 			case 'Q': // strings
 				return detectToken(scanner, IRubyPartitions.RUBY_STRING);
 			case 'w':
 			case 'W': // Array of strings
 				scanner.unread();
 				scanner.unread();
				// FIXME Mark teh individual elements as strings
 				return Token.UNDEFINED;
 			default: // special case of string (no letter following percent)
 				scanner.unread();
 				return detectToken(scanner, IRubyPartitions.RUBY_STRING);
 			}
 		}
 		scanner.unread();
 		return Token.UNDEFINED;
 	}
 
 	private IToken detectToken(ICharacterScanner scanner, String tokenType) {
 		int c = scanner.read(); // get delimeter
 		int lastChar = c;
 		char endChar = getMatchingBracket((char) c);
 		// Read until EOF, End character or EOL
 		while (true) {
 			c = scanner.read();
 			// FIXME This is a big hack. Expression substitution actually needs to be returned as normal ruby code (and repartitioned)
 			if ((char) c == '#') { // Try to handle expression substitution
				char d = (char) scanner.read();
				if (d == '{') {
 					// read until '}'
 					do {
						d = (char)scanner.read();
					} while (d != '}');
 					continue;
				} else if (d == '@' || d == '$') {
 					// read until whitespace
 					do {
						d = (char) scanner.read();
					} while (d != ' ');
 					continue;
 				} else {// not expression subst.
 					scanner.unread();
 				}
 			}
 
 			// TODO Stop at EOL,char[][] originalDelimiters=
 			// scanner.getLegalLineDelimiters();
 			if (c == ICharacterScanner.EOF)
 				break; // we're done
 			if ((char) c == endChar && ((char) lastChar != '\\'))
 				break; // we found matching bracket
 			lastChar = c;
 		}
 		return new Token(tokenType);
 	}
 
 	/**
 	 * Returns whether the next characters to be read by the character scanner
 	 * are an exact match with the given sequence. No escape characters are
 	 * allowed within the sequence. If specified the sequence is considered to
 	 * be found when reading the EOF character.
 	 * 
 	 * @param scanner
 	 *            the character scanner to be used
 	 * @param sequence
 	 *            the sequence to be detected
 	 * @param eofAllowed
 	 *            indicated whether EOF terminates the pattern
 	 * @return <code>true</code> if the given sequence has been detected
 	 */
 	protected boolean sequenceDetected(ICharacterScanner scanner,
 			char[] sequence, boolean eofAllowed) {
 		for (int i = 1; i < sequence.length; i++) {
 			int c = scanner.read();
 			if (c == ICharacterScanner.EOF && eofAllowed) {
 				return true;
 			} else if (c != sequence[i]) {
 				// Non-matching character detected, rewind the scanner back to
 				// the start.
 				// Do not unread the first character.
 				scanner.unread();
 				for (int j = i - 1; j > 0; j--)
 					scanner.unread();
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	private char getMatchingBracket(char c) {
 		switch (c) {
 		case '(':
 			return ')';
 		case '{':
 			return '}';
 		case '[':
 			return ']';
 		case '<':
 			return '>';
 		default:
 			return c;
 		}
 	}
 
 }
