 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.test.parser.jflex;
 
 /**
  *
  */
 public class HtmlPreTagTest extends JFlexParserTest {
 
 	/**
 	 *
 	 */
 	public HtmlPreTagTest(String name) {
 		super(name);
 	}
 
 	/**
 	 *
 	 */
 	public void testMalformed() {
 		String input = "";
 		String output = "";
 		input = "<pre><u>test</u></pre><u>test</u></pre>";
 		output = "<pre>&lt;u&gt;test&lt;/u&gt;</pre><p><u>test</u>&lt;/pre&gt;\n</p>";
 		assertEquals(output, this.parse(input));
 	}
 
 	/**
 	 *
 	 */
 	public void testStandard() {
 		String input = "";
 		String output = "";
 		input = "<pre>test</pre>";
 		output = "<pre>test</pre>\n";
 		assertEquals(output, this.parse(input));
 		input = "<pre><u>test</u></pre>";
 		output = "<pre>&lt;u&gt;test&lt;/u&gt;</pre>\n";
 		assertEquals(output, this.parse(input));
 	}
 
 	/**
 	 *
 	 */
 	public void testWikiSyntax() {
 		String input = "";
 		String output = "";
 		input = "<pre><nowiki>test</nowiki></pre>";
 		output = "<pre>test</pre>\n";
 		assertEquals(output, this.parse(input));
 		input = "<pre>'''bold'''</pre>";
		output = input+"\n";
 		assertEquals(output, this.parse(input));
 	}
 }
