 package fit;
 
 //Copyright (c) 2002 Cunningham & Cunningham, Inc.
 //Released under the terms of the GNU General Public License version 2 or later.
 
 import junit.framework.TestCase;
 
 public class ParseTest extends TestCase {
 
 	public ParseTest(String name) {
 		super(name);
 	}
 
 	public void testParsing () throws Exception {
 		Parse p = new Parse("leader<Table foo=2>body</table>trailer", new String[] {"table"});
 		assertEquals("leader", p.leader);
 		assertEquals("<Table foo=2>", p.tag);
 		assertEquals("body", p.body);
 		assertEquals("trailer", p.trailer);
 	}
 
 	public void testRecursing () throws Exception {
 		Parse p = new Parse("leader<table><TR><Td>body</tD></TR></table>trailer");
 		assertEquals(null, p.body);
 		assertEquals(null, p.parts.body);
 		assertEquals("body", p.parts.parts.body);
 	}
 
 	public void testIterating () throws Exception {
 		Parse p = new Parse("leader<table><tr><td>one</td><td>two</td><td>three</td></tr></table>trailer");
 		assertEquals("one", p.parts.parts.body);
 		assertEquals("two", p.parts.parts.more.body);
 		assertEquals("three", p.parts.parts.more.more.body);
 	}
 
 	public void testIndexing () throws Exception {
 		Parse p = new Parse("leader<table><tr><td>one</td><td>two</td><td>three</td></tr><tr><td>four</td></tr></table>trailer");
 		assertEquals("one", p.at(0,0,0).body);
 		assertEquals("two", p.at(0,0,1).body);
 		assertEquals("three", p.at(0,0,2).body);
 		assertEquals("three", p.at(0,0,3).body);
 		assertEquals("three", p.at(0,0,4).body);
 		assertEquals("four", p.at(0,1,0).body);
 		assertEquals("four", p.at(0,1,1).body);
 		assertEquals("four", p.at(0,2,0).body);
 		assertEquals(1, p.size());
 		assertEquals(2, p.parts.size());
 		assertEquals(3, p.parts.parts.size());
 		assertEquals("one", p.leaf().body);
 		assertEquals("four", p.parts.last().leaf().body);
 	}
 
 	public void testParseException () {
 		try {
			Parse p = new Parse("leader<table><tr><th>one</th><th>two</th><th>three</th></tr><tr><td>four</td></tr></table>trailer");
 		} catch (java.text.ParseException e) {
 			assertEquals(17, e.getErrorOffset());
 			assertEquals("Can't find tag: td", e.getMessage());
 			return;
 		}
 		fail("exptected exception not thrown");
 	}
 
 	public void testText () throws Exception {
 		String tags[] ={"td"};
 		Parse p = new Parse("<td>a&lt;b</td>", tags);
 		assertEquals("a&lt;b", p.body);
 		assertEquals("a<b", p.text());
 		p = new Parse("<td>\ta&gt;b&nbsp;&amp;&nbsp;b>c &&&lt;</td>", tags);
 		assertEquals("a>b & b>c &&<", p.text());
 		p = new Parse("<td>\ta&gt;b&nbsp;&amp;&nbsp;b>c &&lt;</td>", tags);
 		assertEquals("a>b & b>c &<", p.text());
 		p = new Parse("<TD><P><FONT FACE=\"Arial\" SIZE=2>GroupTestFixture</FONT></TD>", tags);
 		assertEquals("GroupTestFixture",p.text());
 
 		assertEquals("", Parse.htmlToText("&nbsp;"));
 		assertEquals("a b", Parse.htmlToText("a <tag /> b"));
 		assertEquals("a", Parse.htmlToText("a &nbsp;"));
 		assertEquals("&nbsp;", Parse.htmlToText("&amp;nbsp;"));
 		assertEquals("1     2", Parse.htmlToText("1 &nbsp; &nbsp; 2"));
 		assertEquals("1     2", Parse.htmlToText("1 \u00a0\u00a0\u00a0\u00a02"));
 		assertEquals("a", Parse.htmlToText("  <tag />a"));
 		assertEquals("a\nb", Parse.htmlToText("a<br />b"));
 
 		assertEquals("ab", Parse.htmlToText("<font size=+1>a</font>b"));
 		assertEquals("ab", Parse.htmlToText("a<font size=+1>b</font>"));
 		assertEquals("a<b", Parse.htmlToText("a<b"));
 
 		assertEquals("a\nb\nc\nd", Parse.htmlToText("a<br>b<br/>c<  br   /   >d"));
 		assertEquals("a\nb", Parse.htmlToText("a</p><p>b"));
 		assertEquals("a\nb", Parse.htmlToText("a< / p >   <   p  >b"));
 		assertEquals("a\nb", Parse.htmlToText("a</p> <p yadda>b"));
 		assertEquals("a b", Parse.htmlToText("a</p> <pyadda>b"));
 	}
 
 	public void testUnescape () {
 		assertEquals("a<b", Parse.unescape("a&lt;b"));
 		assertEquals("a>b & b>c &&", Parse.unescape("a&gt;b&nbsp;&amp;&nbsp;b>c &&"));
 		assertEquals("&amp;&amp;", Parse.unescape("&amp;amp;&amp;amp;"));
 		assertEquals("a>b & b>c &&", Parse.unescape("a&gt;b&nbsp;&amp;&nbsp;b>c &&"));
 	}
 
 	public void testWhitespaceIsCondensed() {
 		assertEquals("a b", Parse.condenseWhitespace(" a  b  "));
 		assertEquals("a b", Parse.condenseWhitespace(" a  \n\tb  "));
 		assertEquals("", Parse.condenseWhitespace(" "));
 		assertEquals("", Parse.condenseWhitespace("  "));
 		assertEquals("", Parse.condenseWhitespace("   "));
 		assertEquals("", Parse.condenseWhitespace(new String(new char[]{(char) 160})));
 	}
 }
