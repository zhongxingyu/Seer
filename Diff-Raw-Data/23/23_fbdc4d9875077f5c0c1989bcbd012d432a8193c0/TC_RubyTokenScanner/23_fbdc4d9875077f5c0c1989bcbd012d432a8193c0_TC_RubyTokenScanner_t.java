 package org.rubypeople.rdt.internal.ui.text.ruby;
 
 import junit.framework.TestCase;
 
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.rules.IToken;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.text.IRubyColorConstants;
 import org.rubypeople.rdt.internal.ui.text.RubyColorManager;
 
 public class TC_RubyTokenScanner extends TestCase {
 
 	private RubyTokenScanner fScanner;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		RubyColorManager colorManager = new RubyColorManager(false);
 		fScanner = new RubyTokenScanner(colorManager, RubyPlugin.getDefault().getPreferenceStore());
 	}
 
 	private void setUpScanner(String code) {
 		setUpScanner(code, 0, code.length());
 	}
 	
 	private void setUpScanner(String code, int offset, int length) {
 		Document doc = new Document(code);
 		fScanner.setRange(doc, offset, length);		
 	}
 
 	private void assertToken(String color, int offset, int length) {
 		IToken token = fScanner.nextToken();
 		assertEquals("Offsets don't match", offset, fScanner.getTokenOffset());
 		assertEquals("Lengths don't match", length, fScanner.getTokenLength());	
 		assertEquals("Colors don't match", fScanner.getToken(color), token); // call getToken so we bypass the scanner's overriding in doGetToken
 	}
 	
 	public void testSimpleClassDefinition() {
 		String code = "class Chris\nend\n";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 0, 5);
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 5, 6);
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 11, 1);
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 12, 3);
 	}
 	
 	public void testMultipleCommentsInARow() {
 		String code = "# comment one\n#comment two\nclass Chris\nend\n";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT, 0, 26);
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 26, 6); // '\nclass'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 32, 6); // ' Chris'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 38, 1); // '\n'
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 39, 3); // 'end'		
 	}
 	
 	public void testCommentAfterEnd() {
 		String code = "class Chris\nend # comment\n";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 0, 5); // 'class'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 5, 6); // ' Chris'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 11, 1); // '\n'
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 12, 3); // 'end'
 		assertToken(IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT, 16, 9); // '# comment'
 	}
 	
 	public void testCommentAfterEndWhileEditing() {
 		String code = "=begin\r\n" +
 "c\r\n" +
 "=end\r\n" +
 "#hmm\r\n" +
 "#comment here why is ths\r\n" +
 "class Chris\r\n" +
 "  def thing\r\n" +
 "  end  #ocmm \r\n" +
 "end";
 		setUpScanner(code, 75, 14);
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 75, 5); // '  end'
 		assertToken(IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT, 82, 5); // '#ocmm'
 	}
 
 	public void testCommentAtEndOfLineWithStringAtBeginning() {
 		String code = "hash = {\n" +
 				"  \"string\" => { # comment\n" +
 				"    123\n" +
 				"  }\n" +
 				"}";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 4); // 'hash'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 4, 2);  // ' ='
		assertToken(IRubyColorConstants.RUBY_DEFAULT, 6, 2);  // ' {'	
 		
		assertToken(IRubyColorConstants.RUBY_STRING, 8, 4);  // whitespace
 		assertToken(IRubyColorConstants.RUBY_STRING, 12, 6);
 		assertToken(IRubyColorConstants.RUBY_STRING, 18, 1);
 		
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 19, 3);
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 22, 2);
 		
 		assertToken(IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT, 25, 9);
 	}
 	
 	public void testLinesWithJustSpaceBeforeComment() {
 		String code = "  \n" +
 				"  # comment\n" +
 				"  def method\n" +
 				"    \n" +
 				"  end";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT, 5, 9); // '# comment'
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 14, 6);  // '\n  def'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 20, 7);  // ' method'	
 	}
 	
 	public void testSymbolAtEndOfLine() {
 		String code = "  helper_method :logged_in?\n" +
 				"  def method\n" +
 				"    \n" +
 				"  end";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 15);  // '  helper_method'
 		assertToken(IRubyColorConstants.RUBY_SYMBOL, 15, 2);  // ' :'
 		assertToken(IRubyColorConstants.RUBY_SYMBOL, 17, 10);  // 'logged_in?'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 27, 1);  // '\n'
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 28, 5);  // '  def'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 33, 7);  // ' method'	
 	}	
 	
 	public void testSymbolInsideBrackets() {
 		String code = "test[:begin]";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 4);  // 'test'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 4, 1);  // '['
 		assertToken(IRubyColorConstants.RUBY_SYMBOL, 5, 1);  // ' :'
 		assertToken(IRubyColorConstants.RUBY_SYMBOL, 6, 5);  // 'begin'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 11, 1);  // ']'	
 	}
 	
 	public void testCommentsWithAlotOfPrecedingSpaces() {
 		String code = "                # We \n" +
 				"                # caller-requested until.\n" +
 				"return self\n";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT, 16, 47);  //
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 63, 7);  // 'return'
 		assertToken(IRubyColorConstants.RUBY_KEYWORD, 70, 5);  // ' self'
 	}
 	
 	public void testSymbolInsideParentheses() {
 		String code = "Object.const_defined?(:RedCloth)";
 		setUpScanner(code);
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 0, 6);  // 'Object'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 6, 1);  // '.'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 7, 14);  // 'const_define?'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 21, 1);  // '('
 		assertToken(IRubyColorConstants.RUBY_SYMBOL, 22, 1);  // ':'
 		assertToken(IRubyColorConstants.RUBY_SYMBOL, 23, 8);  // 'RedCloth'
 		assertToken(IRubyColorConstants.RUBY_DEFAULT, 31, 1);  // ')'
 	}
 	
	public void testAliasWithTwoSymbols() {
		String code = "alias :tsort_each_child :each_key";
		setUpScanner(code);
		assertToken(IRubyColorConstants.RUBY_KEYWORD, 0, 5);  // 'alias'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 5, 2);  // ' :'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 7, 16);  // 'tsort_each_child'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 23, 2);  // ' :'
		assertToken(IRubyColorConstants.RUBY_SYMBOL, 25, 8);  // 'each_key'
	}
 }
