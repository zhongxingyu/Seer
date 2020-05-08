 package org.eclipse.dltk.ruby.formatter.internal;
 
 import org.eclipse.dltk.ruby.formatter.lexer.ILexerReader;
 import org.eclipse.dltk.ruby.formatter.lexer.StringLexerReader;
 import org.eclipse.dltk.ui.formatter.FormatterSyntaxProblemException;
 import org.jruby.common.NullWarnings;
 import org.jruby.lexer.yacc.LexerSource;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.jruby.parser.DefaultRubyParser;
 import org.jruby.parser.RubyParserConfiguration;
 import org.jruby.parser.RubyParserPool;
 import org.jruby.parser.RubyParserResult;
 
 /**
  * Serves as a simple facade for all the parsing magic.
  */
 public class RubyParser {
 
 	public static RubyParserResult parse(String content)
 			throws FormatterSyntaxProblemException {
 		return parse(new StringLexerReader(content));
 	}
 
 	public static RubyParserResult parse(ILexerReader content)
 			throws FormatterSyntaxProblemException {
 		final RubyParserConfiguration configuration = new RubyParserConfiguration();
 		final RubyParserPool parserPool = RubyParserPool.getInstance();
 		final DefaultRubyParser parser = parserPool.borrowParser();
 		try {
 			parser.setWarnings(new NullWarnings());
 			final LexerSource source = LexerSource.getSource(FILENAME, content);
 			final RubyParserResult result = parser.parse(configuration, source);
 			return result;
 		} catch (SyntaxException e) {
			throw new FormatterSyntaxProblemException(e);
 		} finally {
 			parserPool.returnParser(parser);
 		}
 	}
 
 	private static final String FILENAME = "";
 
 }
