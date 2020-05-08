 /**
  * 
  */
 package com.bluespot.parsing;
 
import static org.junit.Assert.*;

 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
import java.util.ArrayList;
 import java.util.Iterator;
import java.util.List;
 
 import org.junit.Test;
 
 /**
  * @author Aaron Faanes
  * 
  */
 public class ParsingTests {
 
 	@Test
 	public void simpleParsingCommands() throws Exception {
 
 	}
 
 	/**
 	 * This is a demonstration of how poorly parsing translates to code. You can
 	 * see how much work we need to do to do the simplest of tasks. This might
 	 * as well be written in assembler.
 	 */
 	@Test
 	public void getSomeNumbers() {
 		Reader reader = new StringReader("1234.46");
 		try {
 			while (true) {
 				int c = reader.read();
 				if (c == -1) {
 					break;
 				}
 				if (Character.isDigit(c) || c == '.') {
 					// Append to some number word
 				}
 			}
 		} catch (IOException e) {
 			throw new AssertionError(e);
 		}
 	}
 
 	private enum Tokens implements TokenFactory {
 		STRING {
 
 			@Override
 			public Token create(String content) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 		}
 	}
 
 	@Test
 	public void parserCanSplitStrings() throws IOException {
 		Parser parser = new Parser();
 		parser.split('\n').token(new TokenFactory<Token<?>>() {
 
 			@Override
 			public Token<?> create(String content) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 		});
 		Iterator<? extends Token<?>> iter = parser.feed(new FileReader("notime.txt"));
 		while (iter.hasNext()) {
			Token<?> token = iter.next();
 		}
 	}
 
 	/**
 	 * The parser should be able to capture different kinds of comments. There's
 	 * no inherent notion of lines, so multiline comments work identically to
 	 * line-limited comments.
 	 * <p>
 	 * The parser understands different forms of newlines, and also will accept
 	 * EOF as a newline character. This behavior is only specified for '\n';
 	 * other string sequences require their ending sequence to be valid.
 	 * <p>
 	 * Parsing rules have equal precedence amongst themselves. This means the
 	 * user doesn't need to take special care to handle cases where rules may be
 	 * intertwined.
 	 * <p>
 	 * Group rules recognize various types of delimiters, so users don't need to
 	 * specify their ending symbols.
 	 * 
 	 * @throws IOException
 	 * 
 	 * @see Parser#preparseList
 	 */
 	@Test
 	public void parserIgnoresCommentsAndCapturesStrings() throws IOException {
 		Parser parser = new Parser();
 		RuleSet rules = parser.ruleSet();
 		rules.group("#", "//").end('\n').drop();
 		rules.group("/*").end("*/").drop();
 		rules.group('"').token(Tokens.STRING);
 		Iterator<? extends Token<?>> iter = parser.feed(new FileReader("notime.txt"));
 		while (iter.hasNext()) {
			Token<?> token = iter.next();
 		}
 	}
 }
