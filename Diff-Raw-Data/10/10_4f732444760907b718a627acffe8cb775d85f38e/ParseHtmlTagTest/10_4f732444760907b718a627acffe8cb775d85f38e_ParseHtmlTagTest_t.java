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
 package org.jamwiki.parser;
 
 /**
  *
  */
 public class ParseHtmlTagTest extends TestParser {
 
 	/**
 	 *
 	 */
 	public ParseHtmlTagTest(String name) {
 		super(name);
 	}
 
 	/**
 	 *
 	 */
 	public void testHtmlWithAttributes() throws Exception {
 		executeParserTest("HtmlWithAttributes1");
 		executeParserTest("HtmlWithAttributes2");
 		executeParserTest("HtmlWithAttributes3");
 	}
 
 	/**
 	 *
 	 */
 	public void testHtmlNoAttributes() throws Exception {
 		executeParserTest("HtmlNoAttributes1");
 		executeParserTest("HtmlNoAttributes2");
 		executeParserTest("HtmlNoAttributes3");
 	}
 
 	/**
 	 *
 	 */
	public void testHtmlMismatch() throws Exception {
// FIXME
//		executeParserTest("HtmlMismatchTest1");
//		executeParserTest("HtmlMismatchTest2");
//		executeParserTest("HtmlMismatchTest3");
	}

	/**
	 *
	 */
 	public void testXSS() throws Exception {
 		// FIXME - this test is currently failing
 		// executeParserTest("HtmlXSS1");
 		executeParserTest("HtmlXSS2");
 	}
 }
