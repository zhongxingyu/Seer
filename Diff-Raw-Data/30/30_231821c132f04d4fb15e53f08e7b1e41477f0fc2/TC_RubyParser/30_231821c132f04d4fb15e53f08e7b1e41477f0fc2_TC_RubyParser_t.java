 /*
  * Author: C.Williams
  *
  *  Copyright (c) 2004 RubyPeople. 
  *
  *  This file is part of the Ruby Development Tools (RDT) plugin for eclipse.
  *  You can get copy of the GPL along with further information about RubyPeople 
  *  and third party software bundled with RDT in the file 
  *  org.rubypeople.rdt.core_0.4.0/RDT.license or otherwise at 
  *  http://www.rubypeople.org/RDT.license.
  *
  *  RDT is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  RDT is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with RDT; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.rubypeople.rdt.internal.core.parser;
 
 import junit.framework.TestCase;
import junit.framework.TestSuite;
 
 import org.rubypeople.rdt.internal.core.parser.ast.RubyBegin;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyCase;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyClass;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyClassVariable;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyDo;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyElement;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyFor;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyGlobal;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyIf;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyInstanceVariable;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyMethod;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyModule;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyRequires;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyScript;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyUnless;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyUntil;
 import org.rubypeople.rdt.internal.core.parser.ast.RubyWhile;
 
 /**
  * @author Chris
  * 
  * To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Generation - Code and Comments
  */
 public class TC_RubyParser extends TestCase {
	
	public TC_RubyParser(String arg0) {
		super(arg0);
	}
	
	/*
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TC_RubyParser("testRecognizesGlobalProcessNumber"));
	    return suite ;
	}
	*/
	
 	public void testRecognizesSingleRequires() throws Exception {
 		RubyScript script = RubyParser.parse("require \"tk\"\n");
 		RubyRequires requires = new RubyRequires("tk", 0, 9);
 		assertEquals(requires, script.getRequires("tk"));
 		assertEquals(new Position(0, 9), script.getRequires("tk").getStart());
 		assertEquals(new Position(0, 11), script.getRequires("tk").getEnd());
 		assertTrue(script.contains(requires));
 		assertFalse(script.contains(new RubyRequires("fake", 0, -987)));
 		assertEquals(1, script.getElementCount());
 	}
 
 	public void testRecognizesRequiresInSingleQuotes() throws Exception {
 		RubyScript script = RubyParser.parse("require 'tk'\n");
 		RubyRequires requires = new RubyRequires("tk", 0, 9);
 		assertEquals(requires, script.getRequires("tk"));
 		assertEquals(new Position(0, 9), script.getRequires("tk").getStart());
 		assertEquals(new Position(0, 11), script.getRequires("tk").getEnd());
 		assertTrue(script.contains(requires));
 		assertFalse(script.contains(new RubyRequires("fake", 0, -987)));
 		assertEquals(1, script.getElementCount());
 	}
 
 	public void testRecognizesTwoRequires() throws Exception {
 		RubyScript script = RubyParser.parse("require \"tk\"\nrequire \"irb\"\n");
 		assertTrue(script.contains(new RubyRequires("tk", 0, 9)));
 		assertTrue(script.contains(new RubyRequires("irb", 1, 9)));
 		assertFalse(script.contains(new RubyRequires("fake", 2, 9)));
 		assertEquals(2, script.getElementCount());
 	}
 
 	public void testDuplicateRequiresAddParseException() throws Exception {
 		RubyScript script = RubyParser.parse("require \"tk\"\nrequire \"tk\"\n");
 		assertTrue(script.hasParseErrors());
 		assertEquals(1, script.getErrorCount());
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyRequires("tk", 0, 9)));
 	}
 
 	public void testRecognizesClass() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\nend\n");
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(1, script.getElementCount());
 		assertFalse(script.contains(new RubyClass("Bob", 0, 45345)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(1, 0), script.getClass("Bob").getEnd());
 	}
 
 	public void testRecognizesModule() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		assertFalse(script.contains(new RubyModule("Bob", 0, 365632)));
 		assertEquals(new Position(0, 7), script.getModule("Bob").getStart());
 		assertEquals(new Position(1, 0), script.getModule("Bob").getEnd());
 	}
 
 	public void testRecognizesScriptLevelMethod() throws Exception {
 		RubyScript script = RubyParser.parse("def bob\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyMethod("bob", 0, 4)));
 		assertFalse(script.contains(new RubyMethod("bob", 0, 9453)));
 		assertEquals(new Position(0, 4), script.getMethod("bob").getStart());
 		assertEquals(new Position(1, 0), script.getMethod("bob").getEnd());
 	}
 
 	public void testRecognizesOneLineMethod() throws Exception {
 		RubyScript script = RubyParser.parse("def bob doSomething() end\n");
 		assertEquals(1, script.getElementCount());
 		assertNotNull(script.getMethod("bob"));
 		assertTrue(script.contains(new RubyMethod("bob", 0, 4)));
 		assertFalse(script.contains(new RubyMethod("bob", 0, 4453)));
 		assertEquals(new Position(0, 4), script.getMethod("bob").getStart());
 		assertEquals(new Position(0, 22), script.getMethod("bob").getEnd());
 	}
 
 	public void testRecognizesMethodWithParameters() throws Exception {
 		RubyScript script = RubyParser.parse("def bob(name) doSomething() end\n");
 		assertEquals(1, script.getElementCount());
 		assertNotNull(script.getMethod("bob"));
 		assertTrue(script.contains(new RubyMethod("bob", 0, 4)));
 		assertFalse(script.contains(new RubyMethod("bob", 0, 475)));
 		assertEquals(new Position(0, 4), script.getMethod("bob").getStart());
 		assertEquals(new Position(0, 28), script.getMethod("bob").getEnd());
 	}
 
 	public void testRecognizesClassInstanceMethod() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef bob\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(3, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("bob", 1, 4)));
 		assertNotNull(bob.getElement("bob"));
 		assertEquals(new Position(1, 4), bob.getElement("bob").getStart());
 		assertEquals(new Position(2, 0), bob.getElement("bob").getEnd());
 	}
 
 	public void testRecognizesInstanceVariable() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\n@name = \"myName\"\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(2, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyInstanceVariable("@name", 1, 0)));
 		assertNotNull(bob.getElement("@name"));
 		assertEquals(new Position(1, 0), bob.getElement("@name").getStart());
 		assertEquals(new Position(1, 4), bob.getElement("@name").getEnd());
 	}
 
 	public void testRecognizesClassVariable() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\n@@name = \"myName\"\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(2, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyClassVariable("@@name", 1, 0)));
 		assertNotNull(bob.getElement("@@name"));
 		assertEquals(new Position(1, 0), bob.getElement("@@name").getStart());
 		assertEquals(new Position(1, 5), bob.getElement("@@name").getEnd());
 	}
 
 	public void testRecognizesGlobal() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\n$name = \"myName\"\nend\n");
 		assertEquals(2, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(2, 0), script.getClass("Bob").getEnd());
 		assertTrue(script.contains(new RubyGlobal("$name", 1, 0)));
 		assertNotNull(script.getElement("$name"));
 		assertEquals(new Position(1, 0), script.getElement("$name").getStart());
 		assertEquals(new Position(1, 4), script.getElement("$name").getEnd());
 	}
	
	public void testRecognizesGlobalProcessNumber() throws Exception {
		RubyScript script = RubyParser.parse("@processNumber = $$");
		assertEquals(2, script.getElementCount());
		assertNotNull(script.getElement("$$"));
	}	
 
 	public void testRecognizesIfBlock() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nif true\nputs \"Hi!\"\nelse\nputs \"Hello!\"\nend\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(8, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("initialize", 1, 4)));
 		assertEquals(new Position(1, 4), bob.getElement("initialize").getStart());
 		assertEquals(new Position(7, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(1, method.getElementCount());
 		assertTrue(method.contains(new RubyIf(2, 0)));
 	}
 
 	public void testIgnoresIfModifier() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nname = \"Nancy\" if true\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(4, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("initialize", 1, 4)));
 		assertEquals(new Position(1, 4), bob.getElement("initialize").getStart());
 		assertEquals(new Position(3, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(0, method.getElementCount());
 	}
 
 	public void testRecognizesUnlessBlock() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nunless true\nputs \"Hi!\"\nelse\nputs \"Hello!\"\nend\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(8, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("initialize", 1, 4)));
 		assertEquals(new Position(1, 4), bob.getElement("initialize").getStart());
 		assertEquals(new Position(7, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(1, method.getElementCount());
 		assertTrue(method.contains(new RubyUnless(2, 0)));
 	}
 
 	public void testIgnoresUnlessModifier() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nname = 34 unless true\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(4, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("initialize", 1, 4)));
 		assertEquals(new Position(1, 4), bob.getElement("initialize").getStart());
 		assertEquals(new Position(3, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(0, method.getElementCount());
 	}
 
 	public void testRecognizesCaseBlock() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\ncase var\nwhile 34\nputs \"Hi!\"\nelse\nputs \"Hello!\"\nend\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(9, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("initialize", 1, 4)));
 		assertEquals(new Position(1, 4), bob.getElement("initialize").getStart());
 		assertEquals(new Position(8, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(1, method.getElementCount());
 		assertTrue(method.contains(new RubyCase(2, 0)));
 	}
 
 	public void testRecognizesUntilBlock() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nuntil var == 3\nvar += 1\nputs \"Hi!\"\nend\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(0, 6), script.getClass("Bob").getStart());
 		assertEquals(new Position(7, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("initialize", 1, 4)));
 		assertEquals(new Position(1, 4), bob.getElement("initialize").getStart());
 		assertEquals(new Position(6, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(1, method.getElementCount());
 		assertTrue(method.contains(new RubyUntil(2, 0)));
 	}
 
 	public void testIgnoresUntilModifier() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nputs \"Hi!\" until var++ == 3\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(4, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("initialize", 1, 4)));
 		assertEquals(new Position(1, 4), bob.getElement("initialize").getStart());
 		assertEquals(new Position(3, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(0, method.getElementCount());
 	}
 
 	public void testComplainsAboutLowercaseClassName() throws Exception {
 		RubyScript script = RubyParser.parse("class bob\ndef initialize\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertEquals(1, script.getErrorCount());
 		assertTrue(script.contains(new RubyClass("bob", 0, 6)));
 		assertEquals(new Position(3, 0), script.getClass("bob").getEnd());
 		RubyClass bob = script.getClass("bob");
 		assertEquals(1, bob.getElementCount());
 		assertTrue(bob.contains(new RubyMethod("initialize", 1, 4)));
 		assertNotNull(bob.getElement("initialize"));
 		assertEquals(new Position(1, 4), bob.getElement("initialize").getStart());
 		assertEquals(new Position(2, 0), bob.getElement("initialize").getEnd());
 	}
 
 	public void testComplainsAboutLowercaseModuleName() throws Exception {
 		RubyScript script = RubyParser.parse("module bob\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertEquals(1, script.getErrorCount());
 		assertTrue(script.contains(new RubyModule("bob", 0, 7)));
 		assertEquals(new Position(1, 0), script.getModule("bob").getEnd());
 	}
 
 	public void testIgnoresAfterComment() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\n# @count = 34\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		assertEquals(new Position(2, 0), script.getModule("Bob").getEnd());
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(0, bob.getElementCount());
 		assertNull(bob.getElement("@count"));
 	}
 
 	public void testPoundSymbolIgnoredInsideString() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\nputs \"# text = #{@count}\"\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		assertEquals(new Position(2, 0), script.getModule("Bob").getEnd());
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertNotNull(bob.getElement("@count"));
 		assertEquals(new Position(1, 17), bob.getElement("@count").getStart());
 	}
 
 	public void testRecognizesWhileBlock() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nwhile var == 3\nvar += 1\nputs \"Hi!\"\nend\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(7, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertEquals(new Position(6, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(1, method.getElementCount());
 		assertTrue(method.contains(new RubyWhile(2, 0)));
 	}
 
 	public void testIgnoresWhileModifier() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nvar += 1 while var == 3\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(4, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertEquals(new Position(3, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(0, method.getElementCount());
 	}
 
 	public void testRecognizesForBlock() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nfor var in [1, 2, 3]\nvar += 1\nputs \"Hi!\"\nend\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(7, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(new Position(6, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(1, method.getElementCount());
 		assertTrue(method.contains(new RubyFor(2, 0)));
 	}
 
 	public void testIgnoresForInsideString() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nputs \"for var in [1, 2, 3]\"\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(4, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(new Position(3, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(0, method.getElementCount());
 	}
 
 	public void testRecognizesBeginBlock() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\nbegin\nvar += 1\nend\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(6, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(new Position(5, 0), bob.getElement("initialize").getEnd());
 		RubyMethod method = (RubyMethod) bob.getElement("initialize");
 		assertEquals(1, method.getElementCount());
 		assertTrue(method.contains(new RubyBegin(2, 0)));
 	}
 
 	public void testInstanceVariablesBubblesUpToClass() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize\n@var = 1\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyClass("Bob", 0, 6)));
 		assertEquals(new Position(4, 0), script.getClass("Bob").getEnd());
 		RubyClass bob = script.getClass("Bob");
 		assertEquals(2, bob.getElementCount());
 		assertNotNull(bob.getElement("@var"));
 	}
 
 	public void testInstanceVariablesBubblesUpToModule() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\ndef initialize\n@var = 1\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		assertEquals(new Position(4, 0), script.getModule("Bob").getEnd());
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(2, bob.getElementCount());
 		assertNotNull(bob.getElement("@var"));
 	}
 
 	public void testMethodDefaultsToPublic() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\ndef myMethod\nputs \"blah\"\nend\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		assertEquals(new Position(4, 0), script.getModule("Bob").getEnd());
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertNotNull(bob.getElement("myMethod"));
 		RubyMethod method = (RubyMethod) bob.getElement("myMethod");
 		assertEquals(RubyElement.PUBLIC, method.getAccess());
 	}
 
 	public void testInstanceVariableDefaultsToPrivate() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\n@var = 1\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertNotNull(bob.getElement("@var"));
 		RubyInstanceVariable method = (RubyInstanceVariable) bob.getElement("@var");
 		assertEquals(RubyElement.PRIVATE, method.getAccess());
 	}
 
 	public void testPrivateModifierOnOneMethod() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\ndef myMethod\nputs \"blah\"\nend\nprivate :myMethod\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertNotNull(bob.getElement("myMethod"));
 		RubyMethod method = (RubyMethod) bob.getElement("myMethod");
 		assertEquals(RubyElement.PRIVATE, method.getAccess());
 	}
 
 	public void testPrivateModifierOnMultipleMethods() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\ndef myMethod\nputs \"blah\"\nend\ndef hisMethod\nvar = 3\nend\nprivate :myMethod, :hisMethod\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(2, bob.getElementCount());
 		assertNotNull(bob.getElement("myMethod"));
 		RubyMethod method = (RubyMethod) bob.getElement("myMethod");
 		assertEquals(RubyElement.PRIVATE, method.getAccess());
 		assertNotNull(bob.getElement("hisMethod"));
 		RubyMethod hisMethod = (RubyMethod) bob.getElement("hisMethod");
 		assertEquals(RubyElement.PRIVATE, hisMethod.getAccess());
 	}
 
 	public void testAttributeReaderOnOneVariable() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\n@var = 1\nattr_reader :var\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertNotNull(bob.getElement("@var"));
 		RubyInstanceVariable var = (RubyInstanceVariable) bob.getElement("@var");
 		assertEquals(RubyElement.READ, var.getAccess());
 	}
 
 	public void testAttributeWriterOnOneVariable() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\n@var = 1\nattr_writer :var\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertNotNull(bob.getElement("@var"));
 		RubyInstanceVariable var = (RubyInstanceVariable) bob.getElement("@var");
 		assertEquals(RubyElement.WRITE, var.getAccess());
 	}
 
 	public void testAttributeAccessorOnOneVariable() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\n@var = 1\nattr_accessor :var\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertNotNull(bob.getElement("@var"));
 		RubyInstanceVariable var = (RubyInstanceVariable) bob.getElement("@var");
 		assertEquals(RubyElement.PUBLIC, var.getAccess());
 	}
 
 	public void testIgnoreMultiLineDocs() throws Exception {
 		RubyScript script = RubyParser.parse("=begin\n@docVar = 'hey'\n=end\nmodule Bob\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 3, 7)));
 	}
 
 	public void testFalsePositiveEnds() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\nreal.__send__(op, x)\n@docVar = 'blah'\nend\n");
 		assertEquals(1, script.getElementCount());
 		RubyModule module = script.getModule("Bob");
 		assertEquals(1, module.getElementCount());
 	}
 
 	public void testRecognizesDo() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\nstring.tr('+', ' ').gsub(/((?:%[0-9a-fA-F]{2})+)/n) do\n[var.delete('%')].pack('H*')\nend\n@var = 1\nend\n");
 		assertEquals(1, script.getElementCount());
 		RubyModule module = script.getModule("Bob");
 		assertEquals(2, module.getElementCount());
 		assertTrue(module.contains(new RubyDo(1, 52)));
 	}
 
 	public void testIgnoresNonSubstitutedInstanceVariableInRegex() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(/=\\?SHIFT_JIS\\?B\\?([!->@-~]+)\\?=/i) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedInstanceVariableInRegex() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(/#{@var}/i) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(2, rubyClass.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedInstanceVariableInDoubleQuotes() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nputs \"@var\"\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedInstanceVariableInDoubleQuotes() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nputs \"#{@var}\"\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(2, rubyClass.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedClassVariableInRegex() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(/=\\?SHIFT_JIS\\?B\\?([!->@@-~]+)\\?=/i) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedClassVariableInRegex() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(/#{@@var}/) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(2, rubyClass.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedClassVariableInDoubleQuotes() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nputs \"@@var\"\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedClassVariableInDoubleQuotes() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nputs \"#{@@var}\"\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(2, rubyClass.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedGlobalInRegex() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(/$var/i) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedGlobalInRegex() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(/#{$var}/) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(2, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedGlobalInDoubleQuotes() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nputs \"$var\"\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedGlobalInDoubleQuotes() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nputs \"#{$var}\"\nend\nend");
 		assertEquals(2, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testDoBlockAsObject() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef CGI::escape(string)\nstring.gsub(/([^ a-zA-Z0-9_.-]+)/n) do\n'%' + 1.unpack('H2' * 1.size).join('%').upcase\nend.tr(' ', '+')\nwhile true\nputs 'blah'\nend\nend\nend");
 		// expect a class
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		// expect a method
 		assertEquals(1, rubyClass.getElementCount());
 		RubyMethod method = (RubyMethod) rubyClass.getElement("CGI::escape");
 		// expect a do block and begin block
 		assertEquals(2, method.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedClassVariableInPercentRRegex() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(%r|@@var|) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedClassVariableInPercentRRegex() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(%r|#{@@var}|) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(2, rubyClass.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedClassVariableInPercentQString() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(%q,@@var,) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedClassVariableInPercentQString() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(%q:#{@@var}:) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(2, rubyClass.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedClassVariableInPercentCapitalQString() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(%Q{@@var}) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testClassVariable() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(@@var) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(2, rubyClass.getElementCount());
 	}
 
 	public void testIgnoresNonSubstitutedClassVariableInPercentCapitalQStringWithEscapedEndChar() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(%Q(blah\\)blah@@var)) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(1, rubyClass.getElementCount());
 	}
 
 	public void testRecognizesSubstitutedClassVariableInPercentCapitalQStringWithEscapedEndChar() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef decode_b(str)\nstr.gsub!(%Q(blah\\)blah#{@@var})) {\ndecode64(1)\n}\nend\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertEquals(2, rubyClass.getElementCount());
 	}
 
 	public void testIgnoreRequireInString() throws Exception {
 		RubyScript script = RubyParser.parse("eval \"require \\\"irb/ws-for-case-2\\\"\", TOPLEVEL_BINDING, __FILE__, __LINE__");
 		assertEquals(0, script.getElementCount());
 	}
 
 	public void testIgnoreRequireNameNotInQuotes() throws Exception {
 		RubyScript script = RubyParser.parse("require ARGV[0].gsub(/.+::/, '')");
 		assertEquals(0, script.getElementCount());
 	}
 
 	public void testCreatesInstanceVarIfUnknownSymbolInAttrModifier() throws Exception {
 		RubyScript script = RubyParser.parse("module Bob\nattr_reader :var\nend\n");
 		assertEquals(1, script.getElementCount());
 		assertTrue(script.contains(new RubyModule("Bob", 0, 7)));
 		RubyModule bob = script.getModule("Bob");
 		assertEquals(1, bob.getElementCount());
 		assertNotNull(bob.getElement("@var"));
 		RubyInstanceVariable var = (RubyInstanceVariable) bob.getElement("@var");
 		assertEquals(RubyElement.READ, var.getAccess());
 	}
 
 	public void testFalsePositiveClassDeclaration() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef initialize(parser_class = nil)\n@parser = (parser_class ? parser_class : $XMLParser)\nend\nend");
 		assertEquals(2, script.getElementCount());
 	}
 
 	public void testRecognizesDoWithNamedLocalVariables() throws Exception {
 		RubyScript script = RubyParser.parse("class Bob\ndef xml2obj(port)\nparser.parse(port) do |type, name, data|\nend\nend\n@var\nend");
 		assertEquals(1, script.getElementCount());
 		RubyClass rubyClass = script.getClass("Bob");
 		assertNotNull(rubyClass.getElement("@var"));
 		assertEquals(2, rubyClass.getElementCount());
 	}
 	
 	public void testRecognizesMultipleGlobalsOnSameLine() throws Exception {
 		RubyScript script = RubyParser.parse("alias $globalOne $globalTwo");
 		assertEquals(2, script.getElementCount());
 	}
 
 }
