 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.ui.tests.indenting;
 
 import org.eclipse.dltk.ruby.internal.ui.RubyPreferenceConstants;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyAutoEditStrategy;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyPartitionScanner;
 import org.eclipse.dltk.ruby.internal.ui.text.RubyPartitions;
 import org.eclipse.dltk.ruby.ui.tests.RubyUITests;
 import org.eclipse.dltk.ui.CodeFormatterConstants;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceStore;
 import org.eclipse.jface.text.DocCmd;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.rules.FastPartitioner;
 
 
 public class RubyAutoIndentStrategyTest extends RubyUITests {
 	/*
 	 * Tests with _ in the beginning relies on features, not
 	 * presented now, and may be will be implemented in future
 	 */
 	
 	public RubyAutoIndentStrategyTest(String name) {
 		super(name);
 	}
 
 	private static final String FOUR = "    ";
 	
 	private static final String EIGHT = FOUR + FOUR;
 	
 	private static final String AUTOINDENT = FOUR;
 	
 	private static final String DUMMY_POSTFIX = "#comment";
 
     private RubyAutoEditStrategy strategy;
 	private String doc;
 	private DocCmd docCmd;
 	private String expected;
 	IPreferenceStore fStore;
 	
 	/**
 	 * Installs a partitioner with <code>document</code>.
 	 * 
 	 * @param document
 	 *            the document
 	 */
 	private void installStuff(Document document) {
 		String[] types = new String[] { RubyPartitions.RUBY_STRING,
 				RubyPartitions.RUBY_COMMENT, IDocument.DEFAULT_CONTENT_TYPE };
 		FastPartitioner partitioner = new FastPartitioner(
 				new RubyPartitionScanner(), types);
 		partitioner.connect(document);
 		document.setDocumentPartitioner(RubyPartitions.RUBY_PARTITIONING,
 				partitioner);
 	}
 
     protected void setUp() throws Exception {
        super.setUp();
     	fStore = new PreferenceStore();
     	RubyPreferenceConstants.initializeDefaultValues(fStore);
     	fStore.setValue(CodeFormatterConstants.FORMATTER_TAB_CHAR, CodeFormatterConstants.SPACE);
 		String fPartitioning = RubyPartitions.RUBY_PARTITIONING;
     	strategy = new RubyAutoEditStrategy(fStore, fPartitioning);
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     
     public void __testTab() { System.out.println("Tab\n");
 
         String str = 
             "        args = [ '-1', '-2',\n"+ 
             "                ";
         DocCmd docCmd = new DocCmd(str.length(), 0, "\t");
         strategy.customizeDocumentCommand(new Document(str), docCmd);
         assertEquals("    ", docCmd.text);
     }
     
     public void _testSpaces() { System.out.println("Spaces\n");
 
         DocCmd docCmd = new DocCmd(0, 0, "\t");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("    ", docCmd.text);
         
         docCmd = new DocCmd(0, 0, "\t\t");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("        ", docCmd.text);
         
         docCmd = new DocCmd(0, 0, "\tabc");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("    abc", docCmd.text);
         
         docCmd = new DocCmd(0, 0, "\tabc\t");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("    abc    ", docCmd.text);
         
         docCmd = new DocCmd(0, 0, " ");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals(" ", docCmd.text);
     }
     
     public void doTestInsertion(String prefix, String postfix, String inserted, String expected) {
 	    Document doc = new Document(prefix + postfix);
 	    DocCmd docCmd = new DocCmd(prefix.length(), 0, inserted);
 	    strategy.customizeDocumentCommand(doc, docCmd);
 	    assertEquals(expected, docCmd.text);
     }
     
     public void doTestLineReindent(String prefix, String line, String appended, String postfix, String expected) throws Exception {
     	Document doc = new Document(prefix + line + postfix);
     	DocCmd cmd = new DocCmd(prefix.length() + line.length(), 0, appended);
     	strategy.customizeDocumentCommand(doc, cmd);
     	doc.replace(cmd.offset, cmd.length, cmd.text);
     	assertEquals(expected, doc.get());
     }
     
     public void doTestNewLineIndent(String prefix, String postfix, String indent) {
     	doTestInsertion(prefix, postfix, "\n", "\n" + indent);
     }
     
     public void testSimpleNewLine() {
     	String stat1 = "def foo";
     	String stat2 = "puts 'Ruby is cool'";
     	doTestNewLineIndent(stat1 + "\n" + FOUR + stat2, DUMMY_POSTFIX, FOUR);
     }
     
     public void testIndentedNewLineAfterDef() {
     	String stat1 = "def foo";
     	doTestNewLineIndent(stat1, DUMMY_POSTFIX, AUTOINDENT);
     }
     
     private void doTestBraceDeindent(String opening, String closing) throws Exception {
     	String s1 = "\tputs " + opening + "\n";
     	String s2 = "\t        2+2\n";
     	doTestLineReindent(s1 + s2, "\t        ", closing, "", 
     			s1 + s2 + "\t     " + closing);
     }
     
     public void testIndentingOfClosingParenToOpeningOne() throws Exception {
     	doTestBraceDeindent("(", ")");
     }
     
     public void testIndentingOfClosingSquareBraceToOpeningOne() throws Exception {
     	doTestBraceDeindent("[", "]");
     }
     
     public void testIndentingOfClosingCurlyBrace() throws Exception {
     	String s1 = "\t puts {\n";
     	String s2 = "\t\t\t     2+2\n";
     	doTestLineReindent(s1 + s2, "\t        ", "}", "", 
     			s1 + s2 + "\t }");
     }
     
     public void testDeindentingOfRdocBegin() throws Exception {
     	String s1 = "\t puts {\n";
     	doTestLineReindent(s1, "\t\t\t=begi", "n", "", s1 + "=begin");
     }
     
     public void testDeindentingOfRdocEnd() throws Exception {
     	String s1 = "\t puts {\n";
     	doTestLineReindent(s1, "\t\t\t=en", "d", "", s1 + "=end");
     }
     
     public void testDeindentOnEnd() throws Exception {
     	String s1 = "\tdef foo\n";
 		String s2 = "\t\tputs\n";
 		doTestLineReindent(s1 + s2, "\t\ten", "d", "", 
 				s1 + s2 + "\tend");
     }
     
     public void __testNewLineAfterReturn() { System.out.println("NewLineAfterReturn\n");
 
     	String str = "dfdsfd" +
     			"def m1(self):\n" +
     			"    return 'foo'\n" +
     			"#ffo";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-"#ffo".length(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n", docCmd.text); 
     	
     }
     
     public void __testIgnoreComment() { System.out.println("IgnoreComment\n");
 
     	String str = "" +
     	"titleEnd = ('[#')" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n", docCmd.text); 
     	
     }
     
     public void __testIgnoreComment2() { System.out.println("IgnoreComment2\n");
 
     	String str = "" +
     	"titleEnd = ('''\n" +
     	"            [#''')" + //should wrap to the start
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n", docCmd.text); 
     	
     }
     
     public void _testNewLineAfterOpeningParWithOtherContents() { System.out.println("NewLineAfterOpeningParWithOtherContents\n");
 
     	String str = "" +
     	"def m1(  self,";
     	//        |<-- should indent here in this case, and not on the parenthesis
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n         ", docCmd.text); 
     }
     
     public void _testNewLineAfterReturn2() { System.out.println("NewLineAfterReturn2\n");
 
         String str = "" +
         "def m1(self):\n" +
         "    return ('foo',";
                      
                          
         final Document doc = new Document(str);
         DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
         strategy.customizeDocumentCommand(doc, docCmd);
         assertEquals("\n            ", docCmd.text); 
         
     }
     
     
     public void __testMaintainIndent() { System.out.println("MaintainIndent\n");
 
         String str = "" +
         "def moo():\n" +
         "    if not 1:\n" +
         "        print 'foo'\n" +
         "    print 'bla'"+
         "";
         
         
         final Document doc = new Document(str);
         DocCmd docCmd = new DocCmd(doc.getLength()-"print 'bla'".length(), 0, "\n");
         strategy.customizeDocumentCommand(doc, docCmd);
         assertEquals("\n    ", docCmd.text); 
         
     }
     
     public void _testMaintainIndent2() { System.out.println("MaintainIndent2\n");
 
     	String str = "" +
     	"def moo():\n" +
     	"    if not 1:\n" +
     	"        print 'foo'\n" +
     	"    print 'bla'"+
     	"";
     	
     	
     	final Document doc = new Document(str);
     	int offset = doc.getLength()-"  print 'bla'".length();
 		DocCmd docCmd = new DocCmd(offset, 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n  ", docCmd.text); 
     	assertEquals(offset+2, docCmd.caretOffset); 
     	
     }
     
     
     public void __testDontChangeCursorOffset() { System.out.println("DontChangeCursorOffset\n");
 
     	String str = "" +
     	"def moo():\n" +
     	"    if not 1:\n" +
     	"        print    'foo'" +
     	"";
     	
     			
     	final Document doc = new Document(str);
     	int offset = doc.getLength()-"    'foo'".length();
     	DocCmd docCmd = new DocCmd(offset, 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n        ", docCmd.text); 
     	assertEquals(-1, docCmd.caretOffset); //don't change it 
     	
     }
     
     
     public void __testTabIndentToLevel() { System.out.println("TabIndentToLevel\n");
 
     	String str = "" +
     	"properties.create( \n" +
     	"                  a,\n" +
     	"        \n" +
     	"\n" + //cursor is here
     	"                  b,\n" +
     	")" +
     	"";
     	
     	
     	final Document doc = new Document(str);
     	int offset = doc.getLength()-"\n                  b,\n)".length();
     	DocCmd docCmd = new DocCmd(offset, 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("                  ", docCmd.text); 
     	
     }
     
     
     
     public void _testTabIndentToLevel2() { System.out.println("TabIndentToLevel2\n");
 
     	String str = "" +
     	"class ContaminantFont( Barrier, ModelBase ):\n" +
     	"    '''\n" +
     	"    This class contains information to edit a contaminant.\n" +
     	"    '''\n" +
     	"    properties.create( \n" +
     	"                          \n" +
     	"                          #defines where is the source (in the water or in the soil)\n" +
     	"                          sourceLocation = SOURCE_LOCATION_WATER,\n" +
     	"                          \n" +
     	"" + //we're here (indent to the first level)
     	"";
     	
     	
     	final Document doc = new Document(str);
     	int offset = doc.getLength();
     	DocCmd docCmd = new DocCmd(offset, 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("    ", docCmd.text); 
     	
     }
     
     
     
     public void __testTabIndentToLevel3() { System.out.println("TabIndentToLevel3\n");
 
     	String str = "" +
     	"class ContaminantFont( Barrier, ModelBase ):\n" +
     	"    '''\n" +
     	"    This class contains information to edit a contaminant.\n" +
     	"    '''\n" +
     	"    properties.create( \n" +
     	"                          \n" +
     	"                          #defines where is the source (in the water or in the soil)\n" +
     	"                          sourceLocation = SOURCE_LOCATION_WATER,\n" +
     	"                          \n" +
     	"    " + //now that we're already in the first level, indent to the current level
     	"";
     	
     	
     	final Document doc = new Document(str);
     	int offset = doc.getLength();
     	DocCmd docCmd = new DocCmd(offset, 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("                          ", docCmd.text);
     	assertEquals(offset - 4, docCmd.offset);
     	assertEquals(4, docCmd.length);    	
     }
     
     
     public void __testNoAutoIndentClosingPar() { System.out.println("NoAutoIndentClosingPar\n");
 
     	String str = "" +
     	"newTuple = (\n" +
     	"              what(),\n" + //the next line should be indented to this one, and not to the start of the indent
     	"            )\n" +
     	"";
     	
     	
     	final Document doc = new Document(str);
     	String s = 
     		"\n"+
     		"            )\n";
     	DocCmd docCmd = new DocCmd(doc.getLength()-s.length(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n              ", docCmd.text); 
     	
     }
     
     public void __testNoAutoIndentClosingPar2() { System.out.println("NoAutoIndentClosingPar2\n");
 
     	String str = "" +
     	"newTuple = (\n" +
     	"              what(),\n" + 
     	"\n" + //pressing tab in the start of this line will bring us to the 'what()' level.
     	"            )\n" +
     	"";
     	
     	
     	final Document doc = new Document(str);
     	String s = 
     		"\n"+
     		"            )\n";
     	DocCmd docCmd = new DocCmd(doc.getLength()-s.length(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("              ", docCmd.text); 
     	
     }
     
     public void __testNewLineAfterLineWithComment() { System.out.println("NewLineAfterLineWithComment\n");
 
     	String str = "" +
     	"string1 = '01234546789[#]'" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n", docCmd.text); 
     	
     }
     
     public void __testNewLine10() { System.out.println("NewLine10\n");
 
     	String str = "" +
     	"def M1(a):\n" +
     	"    doFoo(a,b(),\n" +
     	"          '',b)" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n    ", docCmd.text); 
     	
     }
     
     public void __testNewLine11() { System.out.println("NewLine11\n");
 
     	String str = "" +
     	"def fun():\n" +
     	"    if True:\n" +
     	"        passif False: 'foo'" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-"if False: 'foo'".length(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n    ", docCmd.text); 
     	
     }
     
     
     public void __testNewLine12() { System.out.println("NewLine12\n");
 
     	String str = "" +
     	"if False:print 'done'" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-"print 'done'".length(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n    ", docCmd.text); 
     }
     
 
     public void __testNewLine3() { System.out.println("NewLine3\n");
 
     	String str = "for a in b:    " +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-4, 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n    ", docCmd.text); 
     	
     	String expected = "for a in b:    ";
     	assertEquals(expected, doc.get());
     }
     
     public void __testNewLine6() { System.out.println("NewLine6\n");
 
     	String str = "" +
     			"for v in w:\n" +
     			"    pass\n" + //dedent on pass
     			"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n", docCmd.text); 
     }
     
     public void __testNewLine6a() { System.out.println("NewLine6a\n");
 
     	String str = "" +
     	"def getSpilledComps( *dummy ):\n" +
     	"    return [self.component4]" + //dedent here
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n", docCmd.text); 
     }
     
     public void __testNewLine7() { System.out.println("NewLine7\n");
 
         String str = "" +
         "class C:\n" +
         "    a = 30\n" +
         "print C.a\n" +
         "\n" +
         "";
         final Document doc = new Document(str);
         DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
         strategy.customizeDocumentCommand(doc, docCmd);
         assertEquals("\n", docCmd.text); 
     }
     
     public void __testNewLine8() { System.out.println("NewLine8\n");
 
         String str = "" +
         "class C:\n" +
         "    pass\n" +
         "    a = 30\n" +
         "    " +
         "";
         final Document doc = new Document(str);
         DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
         strategy.customizeDocumentCommand(doc, docCmd);
         assertEquals("\n    ", docCmd.text); 
     }
     
     public void __testIndent() { System.out.println("Indent\n");
 
     	String str = "" +
     	"while False:\n" +
     	"    if foo:" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-"if foo:".length(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n    ", docCmd.text); 
     }
     
     public void __testIndentAfterRet() { System.out.println("IndentAfterRet\n");
 
     	String str = "" +
     	"class Foo:\n" +
     	"    def m1():\n" +
     	"        for a in b:\n" +
     	"            if a = 20:\n" +
     	"                print 'foo'\n" +
     	"        return 30\n" +
     	"    " +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n    ", docCmd.text); 
     }
     
     public void __testIndentAfterRet2() { System.out.println("IndentAfterRet2\n");
 
     	String str = "" +
     	"class Foo:\n" +
     	"    def m1():\n" +
     	"        for a in b:\n" +
     	"            if a = 20:\n" +
     	"                print 'foo'\n" +
     	"        return 30\n" +
     	"    \n" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("    ", docCmd.text); 
     }
     
     public void __testNewLine9() { System.out.println("NewLine9\n");
 
         String str = "" +
         "class C:\n" +
         "    try:" +
         "";
         final Document doc = new Document(str);
         DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
         strategy.customizeDocumentCommand(doc, docCmd);
         assertEquals("\n        ", docCmd.text); 
     }
     
     public void __testNewLine4() { System.out.println("NewLine4\n");
 
     	String str = "" +
     			"def a():\n" +
     			"    print a" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-"    print a".length(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	String expected = "" +
     	"def a():\n" +
     	"    print a" +
     	"";
     	assertEquals(expected, doc.get()); 
     	assertEquals("\n", docCmd.text); 
     	
     }
     
     public void _testNewLine5() { System.out.println("NewLine5\n");
 
     	String str = "" +
     	"def a():\n" +
     	"    " +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-"    ".length(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	String expected = "" +
     	"def a():\n" +
     	"    " +
     	"";
     	assertEquals(expected, doc.get()); 
     	assertEquals("\n", docCmd.text); 
     }
     
     public void __testNewLine() { System.out.println("NewLine\n");
 
     	String str = "createintervention() #create " +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n", docCmd.text); 
     	
     }
     
     public void __testNewLine2() { System.out.println("NewLine2\n");
 
     	String str = "err)" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("\n", docCmd.text); 
     	
     }
     
     
     public void __testTabInComment() { System.out.println("TabInComment\n");
 
     	String str = "#comment" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("    ", docCmd.text); // a single tab should go to the correct indent
     	
     }
     
     public void __testIndentingWithTab() { System.out.println("IndentingWithTab\n");
 
     	String str = "class C:\n" +
     			     "    def m1(self):\n" +
     			     "";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\t");
 		strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("        ", docCmd.text); // a single tab should go to the correct indent
     }
     
     public void __testIndentingWithTab2() { System.out.println("IndentingWithTab2\n");
 
     	String str = "" +
     			"class C:\n" +
     			"    pass\n" +
     			"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("    ", docCmd.text); // a single tab should go to the correct indent
     }
     
     public void __testIndentingWithTab3() { System.out.println("IndentingWithTab3\n");
 
     	String str = "" +
     	"class C:\n" +
     	"    def m1(self):            \n" +
     	"        print 1\n" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("        ", docCmd.text); // a single tab should go to the correct indent
     }
     
 //    public void __testWithoutSmartIndent() { System.out.println("WithoutSmartIndent\n");
 //    	final TestIndentPrefs prefs = new TestIndentPrefs(true, 4);
 //    	prefs.smartIndentAfterPar = false;
 //
 //    	String str = "" +
 //    	"class C:\n" +
 //    	"    def m1(self):" +
 //    	"";
 //    	final Document doc = new Document(str);
 //    	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\n");
 //    	strategy.customizeDocumentCommand(doc, docCmd);
 //    	assertEquals("\n        ", docCmd.text); // a single tab should go to the correct indent
 //    }
     
     public void __testIndentingWithTab4() { System.out.println("IndentingWithTab4\n");
 
     	String str = "" +
     	"class C:\n" +
     	"    def m1(self):            \n" +
     	"        print 'a'\n" +
     	"        " + //now, a 'regular' tab should happen
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("    ", docCmd.text); // a single tab should go to the correct indent
     }
     
     public void __testIndentingWithTab5() { System.out.println("IndentingWithTab5\n");
 
     	String str = "" +
     	"class C:\n" +
     	"    def m1(self):            \n" +
     	"        print 'a'\n" +
     	"       " + //now, only 1 space is missing to the correct indent
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	//assertEquals(" ", docCmd.text); // a single tab should go to the correct indent
     	assertEquals ("        ", docCmd.text);
     	assertEquals ("       ".length(), docCmd.length);    	
     	assertEquals (doc.getLength() - "       ".length(), docCmd.offset);
     }
     
     public void __testIndentingWithTab6() { System.out.println("IndentingWithTab6\n");
 
     	String str = "" +
     	"class C:\n" +
     	"    def m1(self):            \n" +
     	"print 'a'" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-"print 'a'".length(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("        ", docCmd.text); // a single tab should go to the correct indent
     }
     
     public void __testIndentingWithTab7() { System.out.println("IndentingWithTab7\n");
 
     	String str = "" +
     	"class C:\n" +
     	"    def m1(self):            \n" +
     	"  print 'a'" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-"  print 'a'".length(), 0, "\t");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("        ", docCmd.text); // a single tab should go to the correct indent
     	assertEquals(2, docCmd.length); // the spaces after the indent should be removed
     }
     
     public void _testTabs() { System.out.println("Tabs\n");
 
         DocCmd docCmd = new DocCmd(0, 0, "\t");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("\t", docCmd.text);
         
         docCmd = new DocCmd(0, 0, "\t\t");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("\t\t", docCmd.text);
         
         docCmd = new DocCmd(0, 0, "\tabc");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("\tabc", docCmd.text);
         
         docCmd = new DocCmd(0, 0, "\tabc\t");
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("\tabc\t", docCmd.text);
         
         docCmd = new DocCmd(0, 0, "    abc"); //paste
         strategy.customizeDocumentCommand(new Document(""), docCmd);
         assertEquals("\tabc", docCmd.text);
     }
 
     public void __testCommentsIndent() { System.out.println("CommentsIndent\n");
 
 
         doc = "class c: #some comment";
         docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "\n" +
                    "    ";
         assertEquals(expected, docCmd.text);
     }
     
     public void _testCommentsIndent2() { System.out.println("CommentsIndent2\n");
 
         //__test not indent more
         doc = "    # comment:";
         docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "\n" +
         "    ";
         assertEquals(expected, docCmd.text);
         
         //test indent more
         doc = "    if False:";
         docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "\n" +
         "        ";
         assertEquals(expected, docCmd.text);
     }
     
     public void __testIndentLevel3() { System.out.println("IndentLevel3\n");
 
     	
     	String doc = "" +
 		"a = (1, \n" +
 		"  2,"; //should keep this indent, and not go to the opening bracket indent.
     	DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
     	strategy.customizeDocumentCommand(new Document(doc), docCmd);
     	String expected = "\n  ";
     	assertEquals(expected, docCmd.text);
     }
     
     public void __testIndentLevel() { System.out.println("IndentLevel\n");
 
         
         String doc = "" +
                 "def m1(): #some comment\n" +
                 "    print foo(a,\n" +
                 "              b)";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n    ";
         assertEquals(expected, docCmd.text);
     }
     
     public void __testIndentLevel2() { System.out.println("IndentLevel2\n");
 
         
         String doc = "" +
         "def m1(): #some comment\n" +
         "    def metfoo(a,\n" +
         "               b):";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n        ";
         assertEquals(expected, docCmd.text);
     }
     
     public void __testDedent() { System.out.println("Dedent\n");
 
 
         String doc = "def m1(): #some comment\n" +
                      "    return 10";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n";
         assertEquals(expected, docCmd.text);
         
         //test ending with
         doc = "def m1(): #some comment\n" +
               "    return";
         docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "\n";
         assertEquals(expected, docCmd.text);
         
         //test not dedenting
         doc = "def m1(): #some comment\n" +
         "    returnIs10 = 10";
         docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "\n"+
         "    ";
         assertEquals(expected, docCmd.text);
         
     }
     
     public void __testIndentSpaces() { System.out.println("IndentSpaces\n");
         //test after class xxx:\n
 
         String doc = "class c:";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n" +
         		          "    ";
         assertEquals(expected, docCmd.text);
         
         //test regular
         doc = "    a = 2";
         docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "\n" +
                    "    ";
         assertEquals(expected, docCmd.text);
 
         /*
         //test after [ a,\n
         doc = "m = [a,";
         docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "\n" +
                    "     ";
         assertEquals(expected, docCmd.text);
         */
     }        
     
     public void _testAfterClosePar1() { System.out.println("AfterClosePar1\n");
 
         String doc = "m = [a,";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n" +
         "     ";
         assertEquals(expected, docCmd.text);
         
     }
     
 //    public void __testAfterCloseParOnlyIndent() { System.out.println("AfterCloseParOnlyIndent\n");
 //    	final TestIndentPrefs prefs = new TestIndentPrefs(true, 4);
 //
 //		prefs.indentToParLevel = false;
 //    	String doc = "m = [a,";
 //    	DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
 //    	strategy.customizeDocumentCommand(new Document(doc), docCmd);
 //    	String expected = "\n" +
 //    	"    ";
 //    	assertEquals(expected, docCmd.text);
 //    	
 //    }
     
 //    public void __testAfterCloseParOnlyIndent2() { System.out.println("AfterCloseParOnlyIndent2\n");
 //    	final TestIndentPrefs prefs = new TestIndentPrefs(true, 4);
 //
 //    	prefs.indentToParLevel = false;
 //    	String doc = "" +
 //    			"class A:\n" +
 //    			"    def m1(a,";
 //    	DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
 //    	strategy.customizeDocumentCommand(new Document(doc), docCmd);
 //    	String expected = "\n" +
 //    	"        ";
 //    	assertEquals(expected, docCmd.text);
 //    	
 //    }
     
     public void __testAfterClosePar2() { System.out.println("AfterClosePar2\n");
 
         String doc = "m = [a,\n" +
                      "     b,";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n" +
         "     ";
         assertEquals(expected, docCmd.text);
         
     }
     public void _testAfterClosePar() { System.out.println("AfterClosePar\n");
 
         String doc = "m = [a, (#comment";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n" +
         "         ";
         assertEquals(expected, docCmd.text);
         
 //        doc = "m = [a, otherCall(), ]";
 //        docCmd = new DocCmd(doc.length()-1, 0, "\n"); //right before the last ']'
 //        strategy.customizeDocumentCommand(new Document(doc), docCmd);
 //        expected = "\n" +
 //        "      ";
 //        assertEquals(expected, docCmd.text);
     }
     
     public void _testIndent2() { System.out.println("Indent2\n");
 
         String doc = "m = [a, otherCall(), ";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
 //        strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n" +
         "      ";
 //        assertEquals(expected, docCmd.text);
 //
 //        doc = "m = [a, otherCall(), ]";
 //        docCmd = new DocCmd(doc.length()-1, 0, "\n"); //right before the last ']'
 //        strategy.customizeDocumentCommand(new Document(doc), docCmd);
 //        expected = "\n" +
 //        "      ";
 //        assertEquals(expected, docCmd.text);
         
         doc = "def m2(self):\n"+
               "    m1(a, b(), )";
         docCmd = new DocCmd(doc.length()-1, 0, "\n"); //right before the last ')'
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "\n" +
               "       ";
         assertEquals(expected, docCmd.text);
         
     }
     
     public void _testIndent3() { System.out.println("Indent3\n");
         
 
         String doc = ""+
         "properties.create(a = newClass(),"; 
         DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "\n"+
         "                  ";
         assertEquals(expected, docCmd.text);
         
     }
     public void __testIndent3a() { System.out.println("Indent3a\n");
 
     	String doc = ""+
 		"properties.create(a = newClass(),\n" +
 		"                  b = newClass(),"; //don't indent after the '(' in this line, but to the default one
     	DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
     	strategy.customizeDocumentCommand(new Document(doc), docCmd);
     	String expected = "\n"+
     	"                  ";
     	assertEquals(expected, docCmd.text);
     }
     
     public void _testIndent4() { System.out.println("Indent4\n"); //even if it does not end with ',' we should indent in parenthesis
 
     	String doc = ""+
     	"properties.create(a = newClass(),\n" +
     	"                  b = newClass("; //don't indent after the '(' in this line, but to the default one
     	DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
     	strategy.customizeDocumentCommand(new Document(doc), docCmd);
     	String expected = "\n"+
     	"                               ";
     	assertEquals(expected, docCmd.text);
     }
     
     public void __testDedent5() { System.out.println("Dedent5\n"); 
 
     	String doc = ""+
     	"properties.create(a = newClass(),\n" +
     	"                  b = newClass(\n" +
     	"                               )"; //go to the last indentation
     	DocCmd docCmd = new DocCmd(doc.length(), 0, "\n");
     	strategy.customizeDocumentCommand(new Document(doc), docCmd);
     	String expected = "\n"+
     	"                  ";
     	assertEquals(expected, docCmd.text);
     }
     
 //    public void __testNoSmartIndent() { System.out.println("NoSmartIndent\n");
 //    	
 //    	TestIndentPrefs prefs = new TestIndentPrefs(false, 4, true);
 //    	prefs.smartIndentAfterPar = false;
 //
 //
 //		String doc = null;
 //        DocCmd docCmd = null;
 //        String expected = null;
 //
 //	    //test after [ a,\n
 //        doc = "m = [a,";
 //        docCmd = new DocCmd(doc.length(), 0, "\n");
 //        strategy.customizeDocumentCommand(new Document(doc), docCmd);
 //        expected = "\n";
 //        assertEquals(expected, docCmd.text);
 //
 //        //test after \t[ a,\n
 //        doc = "\tm = [a,";
 //        docCmd = new DocCmd(doc.length(), 0, "\n");
 //        strategy.customizeDocumentCommand(new Document(doc), docCmd);
 //        expected = "\n" +
 //                   "\t";
 //        assertEquals(expected, docCmd.text);
 //
 //        //test after \t[ a,\n
 //        doc = "\tm = [a,  ";
 //        docCmd = new DocCmd(doc.length(), 0, "\n");
 //        strategy.customizeDocumentCommand(new Document(doc), docCmd);
 //        expected = "\n" +
 //                   "\t";
 //        assertEquals(expected, docCmd.text);
 //
 //    }
 
 
     public void __testAutoClose() { System.out.println("AutoClose\n");
 
         String doc = "class c(object): ";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "[");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "[]";
         assertEquals(expected, docCmd.text);
 
     }
 
 //    public void __testAutoSelf() { System.out.println("AutoSelf\n");
 //    	TestIndentPrefs testIndentPrefs = new TestIndentPrefs(false, 4, true);
 //    	testIndentPrefs.autoAddSelf = false;
 //
 //    	String doc = null;
 //    	DocCmd docCmd = null;
 //    	String expected = null;
 //    	
 //    	doc = "class c:\n" +
 //    	"    def met";
 //    	docCmd = new DocCmd(doc.length(), 0, "(");
 //    	strategy.customizeDocumentCommand(new Document(doc), docCmd);
 //    	expected = "():";
 //    	assertEquals(expected, docCmd.text);
 //    	
 //    }
     
     /**
      * Tests automatically adding/replacing brackets, colons, and parentheses.
      * @see PyAutoIndentStrategy
      */
     public void _testAutoPar() { System.out.println("AutoPar\n");
 
         String doc = "class c";
         DocCmd docCmd = new DocCmd(doc.length(), 0, "(");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = "():";
         assertEquals(expected, docCmd.text);
         
         doc = "class c:\n" +
     		  "    def met";
         docCmd = new DocCmd(doc.length(), 0, "(");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "(self):";
         assertEquals(expected, docCmd.text);
         
         //same as above, but with tabs
         doc = "class c:\n" +
         "\tdef met";
         docCmd = new DocCmd(doc.length(), 0, "(");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "(self):";
         assertEquals(expected, docCmd.text);
         
         doc = "class c(object): #";
         docCmd = new DocCmd(doc.length(), 0, "(");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "("; //in comment
         assertEquals(expected, docCmd.text);
         
         doc = "def a";
         docCmd = new DocCmd(doc.length(), 0, "(");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "():";
         assertEquals(expected, docCmd.text);
         
         doc = "a";
         docCmd = new DocCmd(doc.length(), 0, "(");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "()";
         assertEquals(expected, docCmd.text);
         
         doc = "a()";
         docCmd = new DocCmd(doc.length()-1, 0, "(");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = "(";
         assertEquals(expected, docCmd.text);
 		
 		// test very simple ':' detection
 		doc = "def something():";
 		docCmd = new DocCmd(doc.length() - 1, 0, ":");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "";
 		assertEquals(expected, docCmd.text);
 		assertEquals(15, docCmd.offset);
 
 		// test inputting ':' when you already have a ':', like at the end of a function declaraction
 		doc = "class c:\n" +
 				"    def __init__(self):";
 		docCmd = new DocCmd(doc.length() - 1, 0, ":");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "";
 		assertEquals(expected, docCmd.text);
 		assertEquals(32, docCmd.caretOffset);
 		
 		// test inputting ':' at the end of a document
 		doc = "class c:\n" +
 				"    def __init__(self)";
 		docCmd = new DocCmd(doc.length(), 0, ":");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = ":";
 		assertEquals(expected, docCmd.text);
 		assertEquals(31, docCmd.offset);
 		
 		// test same as above, but with a comment
 		doc = "class c:\n" +
 				"    def __init__(self): # comment";
 		docCmd = new DocCmd(doc.length() - 11, 0, ":");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "";
 		assertEquals(expected, docCmd.text);
 		assertEquals(32, docCmd.caretOffset);
 		
 		// test inputting ')' at the end of a document
 		doc = "class c:\n" +
 				"    def __init__(self)";
 		docCmd = new DocCmd(doc.length(), 0, ")");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = ")";
 		assertEquals(expected, docCmd.text);
 		assertEquals(0, docCmd.caretOffset);
 		
 		// test inputting ')' at the end of a document when it should replace a ')'
 		doc = "class c:\n" +
 				"    def __init__(self)";
 		docCmd = new DocCmd(doc.length() - 1, 0, ")");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "";
 		assertEquals(expected, docCmd.text);
 		assertEquals(31, docCmd.caretOffset);
 		
 		// test inputting ')' in the middle of the document
 		doc = "def __init__(self):\n" + 
 			  "   pass";
 		docCmd = new DocCmd(17, 0, ")");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "";
 		assertEquals(expected, docCmd.text);
 		assertEquals(18, docCmd.caretOffset);
 		
 		// check very simple braces insertion
 		doc = "()";
 		docCmd = new DocCmd(1, 0, ")");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "";
 		assertEquals(expected, docCmd.text);
 		assertEquals(2, docCmd.caretOffset);
 		
 		// check simple braces insertion not at end of document
 		doc = "() ";
 		docCmd = new DocCmd(1, 0, ")");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "";
 		assertEquals(expected, docCmd.text);
 		assertEquals(2, docCmd.caretOffset);
 		
 		// check insertion that should happen even being just before a ')'
 		doc = "(() ";
 		docCmd = new DocCmd(2, 0, ")");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = ")";
 		assertEquals(expected, docCmd.text);
 		assertEquals(0, docCmd.caretOffset);
 		
 		// check same stuff for brackets
 		// check simple braces insertion not at end of document
 		doc = "[] ";
 		docCmd = new DocCmd(1, 0, "]");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "";
 		assertEquals(expected, docCmd.text);
 		assertEquals(2, docCmd.caretOffset);
 		
 		// two different kinds of braces next to each other
 		doc = "([)";
 		docCmd = new DocCmd(2, 0, "]");
 		strategy.customizeDocumentCommand(new Document(doc), docCmd);
 		expected = "]";
 		assertEquals(expected, docCmd.text);
 		assertEquals(0, docCmd.caretOffset);
     }
     
     public void __testParens() { System.out.println("Parens\n");
 
     	String str = "isShown() #suite()" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-") #suite()".length(), 0, ")");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("", docCmd.text); 
     	assertEquals(9, docCmd.caretOffset);
     	
     }
     
     public void __testParens2() { System.out.println("Parens2\n");
 
     	String str = "isShown() #suite()'" +
     	"";
     	final Document doc = new Document(str);
     	DocCmd docCmd = new DocCmd(doc.getLength()-") #suite()'".length(), 0, ")");
     	strategy.customizeDocumentCommand(doc, docCmd);
     	assertEquals("", docCmd.text); 
     	assertEquals(9, docCmd.caretOffset);
     	
     }
 
     
     public void __testElse() { System.out.println("Else\n");
         //first part of test - simple case
 
         String strDoc = "if foo:\n" +
                      "    print a\n" +
                      "    else";
         int initialOffset = strDoc.length();
         DocCmd docCmd = new DocCmd(initialOffset, 0, ":");
         Document doc = new Document(strDoc);
         strategy.customizeDocumentCommand(doc, docCmd);
         String expected = ":";
         assertEquals(docCmd.offset, initialOffset-4);
         assertEquals(expected, docCmd.text);
         assertEquals(
                 "if foo:\n" +
                 "    print a\n" +
                 "else",
                 doc.get());
         
         //second part of test - should not dedent
 
         strDoc = 
         "if foo:\n" +
         "    if somethingElse:" +
         "        print a\n" +
         "    else";
         initialOffset = strDoc.length();
         docCmd = new DocCmd(initialOffset, 0, ":");
         doc = new Document(strDoc);
         strategy.customizeDocumentCommand(doc, docCmd);
         expected = ":";
         assertEquals(expected, docCmd.text);
         assertEquals(docCmd.offset, initialOffset);
         assertEquals(
                 "if foo:\n" +
                 "    if somethingElse:" +
                 "        print a\n" +
                 "    else",
                 doc.get());
         
     }
     
     public void _testElif() { System.out.println("Elif\n");
     	//first part of test - simple case
 
     	String strDoc = "if foo:\n" +
     	"    print a\n" +
     	"    elif";
     	int initialOffset = strDoc.length();
     	DocCmd docCmd = new DocCmd(initialOffset, 0, " ");
     	Document doc = new Document(strDoc);
     	strategy.customizeDocumentCommand(doc, docCmd);
     	String expected = " ";
     	assertEquals(docCmd.offset, initialOffset-4);
     	assertEquals(expected, docCmd.text);
     	assertEquals(
     			"if foo:\n" +
     			"    print a\n" +
     			"elif",
     			doc.get());
     	
     	//second part of test - should not dedent
 
     	strDoc = 
     		"if foo:\n" +
     		"    if somethingElse:" +
     		"        print a\n" +
     		"    elif";
     	initialOffset = strDoc.length();
     	docCmd = new DocCmd(initialOffset, 0, " ");
     	doc = new Document(strDoc);
     	strategy.customizeDocumentCommand(doc, docCmd);
     	expected = " ";
     	assertEquals(expected, docCmd.text);
     	assertEquals(docCmd.offset, initialOffset);
     	assertEquals(
     			"if foo:\n" +
     			"    if somethingElse:" +
     			"        print a\n" +
     			"    elif",
     			doc.get());
     	
     }
     
     
     public void __testElseInFor() { System.out.println("ElseInFor\n");
         //first part of test - simple case
 
         String strDoc = 
         "for i in []:\n" +
         "    msg=\"success at %s\" % i\n" +
         "    else" +
         "";
         int initialOffset = strDoc.length();
         DocCmd docCmd = new DocCmd(initialOffset, 0, ":");
         Document doc = new Document(strDoc);
         strategy.customizeDocumentCommand(doc, docCmd);
         String expected = ":";
         assertEquals(docCmd.offset, initialOffset-4);
         assertEquals(expected, docCmd.text);
         assertEquals(
                 "for i in []:\n" +
                 "    msg=\"success at %s\" % i\n" +
                 "else" +
                 "",
                 doc.get());
     }
     
     public void __testElseInTry() { System.out.println("ElseInTry\n");
         //first part of test - simple case
 
         String strDoc = 
             "try:\n" +
             "    print a\n" +
             "except:\n" +
             "    pass\n" +
             "    else";
         int initialOffset = strDoc.length();
         DocCmd docCmd = new DocCmd(initialOffset, 0, ":");
         Document doc = new Document(strDoc);
         strategy.customizeDocumentCommand(doc, docCmd);
         String expected = ":";
         assertEquals(docCmd.offset, initialOffset-4);
         assertEquals(expected, docCmd.text);
         assertEquals(
                 "try:\n" +
                 "    print a\n" +
                 "except:\n" +
                 "    pass\n" +
                 "else",
                 doc.get());
     }
     
     public void _testElifWithPar() { System.out.println("ElifWithPar\n");
     	//first part of test - simple case
 
     	String strDoc = "if foo:\n" +
     	"    print a\n" +
     	"    elif";
     	int initialOffset = strDoc.length();
     	DocCmd docCmd = new DocCmd(initialOffset, 0, "(");
     	Document doc = new Document(strDoc);
     	strategy.customizeDocumentCommand(doc, docCmd);
     	String expected = "()";
     	assertEquals(docCmd.offset, initialOffset-4);
     	assertEquals(expected, docCmd.text);
     	assertEquals(
     			"if foo:\n" +
     			"    print a\n" +
     			"elif",
     			doc.get());
     	
     	//second part of test - should not dedent
 
     	strDoc = 
     		"if foo:\n" +
     		"    if somethingElse:" +
     		"        print a\n" +
     		"    elif";
     	initialOffset = strDoc.length();
     	docCmd = new DocCmd(initialOffset, 0, "(");
     	doc = new Document(strDoc);
     	strategy.customizeDocumentCommand(doc, docCmd);
     	expected = "()";
     	assertEquals(expected, docCmd.text);
     	assertEquals(docCmd.offset, initialOffset);
     	assertEquals(
     			"if foo:\n" +
     			"    if somethingElse:" +
     			"        print a\n" +
     			"    elif",
     			doc.get());
     	
     }
     
     public void _testAutoImportStr() { System.out.println("AutoImportStr\n");
 
         String doc = "from xxx";
         DocCmd docCmd = new DocCmd(doc.length(), 0, " ");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         String expected = " import ";
         assertEquals(expected, docCmd.text);
         
         doc = "from xxx import";
         docCmd = new DocCmd(doc.length(), 0, " ");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = " ";
         assertEquals(expected, docCmd.text);
         
         doc = "no from xxx";
         docCmd = new DocCmd(doc.length(), 0, " ");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = " ";
         assertEquals(expected, docCmd.text);
         
         doc = "From xxx";
         docCmd = new DocCmd(doc.length(), 0, " ");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = " ";
         assertEquals(expected, docCmd.text);
         
         doc = "from this space";
         docCmd = new DocCmd(doc.length(), 0, " ");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = " ";
         assertEquals(expected, docCmd.text);
         
         doc = "from";
         docCmd = new DocCmd(doc.length(), 0, " ");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = " ";
         assertEquals(expected, docCmd.text);
 
         doc = "from xxx import yyy";
         docCmd = new DocCmd(8, 0, " ");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = " ";
         assertEquals(expected, docCmd.text);
         
         doc = "from xxx #import yyy";
         docCmd = new DocCmd(8, 0, " ");
         strategy.customizeDocumentCommand(new Document(doc), docCmd);
         expected = " import ";
         assertEquals(expected, docCmd.text);
         
     }
 
     public void testBug186514() throws Exception {
     	String prefix = "class A\n\tdef b\n\tend\n\t";
 		String postfix = "\nend";
 		String inserted = "def test1\nend\n|";
 		String expected = "\tdef test1\n\tend\n\t|";
 		doTestInsertion(prefix, postfix, inserted, expected);
     }    
     
 
 }
