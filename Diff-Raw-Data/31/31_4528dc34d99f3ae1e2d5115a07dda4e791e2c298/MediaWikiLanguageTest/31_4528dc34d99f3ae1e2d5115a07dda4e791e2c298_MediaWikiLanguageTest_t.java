 /*******************************************************************************
  * Copyright (c) 2007, 2012 David Green and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     David Green - initial API and implementation
  *     Jeremie Bresson - Bug 381506, 381912
  *******************************************************************************/
 package org.eclipse.mylyn.wikitext.mediawiki.core;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import junit.framework.TestCase;
 
 import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
 import org.eclipse.mylyn.wikitext.core.parser.builder.DocBookDocumentBuilder;
 import org.eclipse.mylyn.wikitext.core.parser.builder.RecordingDocumentBuilder;
 import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;
 import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineParser;
 import org.eclipse.mylyn.wikitext.tests.TestUtil;
 
 /**
  * @author David Green
  */
 public class MediaWikiLanguageTest extends TestCase {
 
 	private MarkupParser parser;
 
 	private MediaWikiLanguage markupLanguage;
 
 	private Locale locale;
 
 	@Override
 	public void setUp() {
 		locale = Locale.getDefault();
 		Locale.setDefault(Locale.ENGLISH);
 
 		markupLanguage = new MediaWikiLanguage();
 		parser = new MarkupParser(markupLanguage);
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		super.tearDown();
 
 		Locale.setDefault(locale);
 	}
 
 	public void testIsDetectingRawHyperlinks() {
 		assertTrue(parser.getMarkupLanguage().isDetectingRawHyperlinks());
 	}
 
 	public void testParagraph() {
 		String html = parser.parseToHtml("first para<br/>\nfirst para line2\n\nsecond para\n\nthird para");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile(
 				"<body><p>first para<br/>\\s*first para line2</p><p>second para</p><p>third para</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testNowiki() {
 		String html = parser.parseToHtml("'''<nowiki>no <!-- markup here</nowiki>'''");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p><b>no &lt;!-- markup here</b></p></body>").matcher(html).find());
 	}
 
 	public void testNoWiki_325022() {
 		// bug 325022: nowiki is not correctly detected
 		String html = parser.parseToHtml("//<nowiki>[</nowiki>username<nowiki>[</nowiki>:password]@]host<nowiki>[</nowiki>:port]");
 		//[username[:password]@]host[:port]
 		TestUtil.println(html);
 		assertTrue(html.contains("<p>//[username[:password]@]host[:port]</p>"));
 	}
 
 	public void testBoldItalic() {
 		String html = parser.parseToHtml("normal '''''bold italic text''''' normal");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p>normal <b><i>bold italic text</i></b> normal</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testBold() {
 		String html = parser.parseToHtml("normal '''bold text''' normal");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p>normal <b>bold text</b> normal</p></body>").matcher(html).find());
 	}
 
 	public void testBoldImmediatelyFollowingTag() {
 		String html = parser.parseToHtml("normal<br>'''bold text''' normal");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p>normal<br/><b>bold text</b> normal</p></body>").matcher(html).find());
 	}
 
 	public void testBold_single_character_bug369921() {
 		String html = parser.parseToHtml("'''aa''' bb '''cc'''");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p><b>aa</b> bb <b>cc</b></p></body>").matcher(html).find());
 
 		html = parser.parseToHtml("'''a''' b '''c'''");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p><b>a</b> b <b>c</b></p></body>").matcher(html).find());
 	}
 
 	public void testBold_adjacentText_bug369921() {
 		String html = parser.parseToHtml("'''aa'''bb");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p><b>aa</b>bb</p></body>").matcher(html).find());
 	}
 
 	public void testItalic() {
 		String html = parser.parseToHtml("normal ''italic text'' normal");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p>normal <i>italic text</i> normal</p></body>").matcher(html).find());
 	}
 
 	public void testHeadings() {
 		for (int x = 1; x <= 6; ++x) {
 			String delimiter = repeat(x, "=");
 			String[] headingMarkupSamples = new String[] { delimiter + "heading text" + delimiter,
 					delimiter + "heading text" + delimiter + "  ", delimiter + "heading text" + delimiter + " \t " };
 			for (String headingMarkup : headingMarkupSamples) {
 				String html = parser.parseToHtml(headingMarkup
 						+ "\nfirst para<br/>\nfirst para line2\n\nsecond para\n\nthird para");
 				TestUtil.println(html);
 				assertTrue(Pattern.compile(
 						"<body><h"
 								+ x
 								+ " id=\"[^\"]+\">heading text</h"
 								+ x
 								+ "><p>first para<br/>\\s*first para line2</p><p>second para</p><p>third para</p></body>",
 						Pattern.MULTILINE)
 						.matcher(html)
 						.find());
 			}
 		}
 	}
 
 	public void testHeadingWithStyles_bug355713() {
 		String html = parser.parseToHtml("== '''bold''' ''italic'' <u>underlined</u> <s>strikethrough</s> ==");
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<h2.*?><b>bold</b> <i>italic</i> <u>underlined</u> <s>strikethrough</s></h2>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testHeadingsWithPara() {
 		String html = parser.parseToHtml("\n== H1 ==\n\npa\n\n=== H3 ===\n\nabc");
 		TestUtil.println(html);
 		assertTrue(html.contains("<body><h2 id=\"H1\">H1</h2><p>pa</p><h3 id=\"H3\">H3</h3><p>abc</p></body>"));
 	}
 
 	// FIXME: can paragraphs be interrupted by headings?
 	//	public void testHeadingsWithPara2() {
 	//		String html = parser.parseToHtml("== H1 ==\npa\n=== H3 ===\n\nabc");
 	//		TestUtil.println(html);
 	//		assertTrue(html.contains("<body><h2>H1</h2><p>pa</p><h3>H3</h3><p>abc</p></body>"));
 	//	}
 
 	private String repeat(int i, String string) {
 		StringBuilder buf = new StringBuilder(string.length() * i);
 		for (int x = 0; x < i; ++x) {
 			buf.append(string);
 		}
 		return buf.toString();
 	}
 
 	public void testHorizontalRule() {
 		String html = parser.parseToHtml("an hr ---- foo");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("hr <hr/> foo"));
 	}
 
 	public void testHorizontalRule2() {
 		String html = parser.parseToHtml("Mediawiki should render:\n----\nAs a \"horizontal rule\".");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile("render\\:\\s*<hr/>\\s*As a").matcher(html).find());
 	}
 
 	public void testListUnordered() throws IOException {
 		String html = parser.parseToHtml("* a list\n* with two lines");
 
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<ul>"));
 		assertTrue(html.contains("<li>a list</li>"));
 		assertTrue(html.contains("<li>with two lines</li>"));
 		assertTrue(html.contains("</ul>"));
 	}
 
 	public void testListOrdered() throws IOException {
 		String html = parser.parseToHtml("# a list\n# with two lines");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<ol>"));
 		assertTrue(html.contains("<li>a list</li>"));
 		assertTrue(html.contains("<li>with two lines</li>"));
 		assertTrue(html.contains("</ol>"));
 	}
 
 	public void testListOrderedWithContinuation() throws IOException {
 		String html = parser.parseToHtml("# a list\n" + "## a nested item\n" + "### another nested item\n"
 				+ "#: continued\n" + "# another item");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><ol><li>a list<ol><li>a nested item<ol><li>another nested item</li></ol>continued</li></ol></li><li>another item</li></ol></body>"));
 
 		// TODO: continuations on first level?
 	}
 
 	public void testListOrderedWithContinuationToDocBook() throws IOException {
 		StringWriter out = new StringWriter();
 		parser.setBuilder(new DocBookDocumentBuilder(out));
 
 		parser.parse("# a list\n" + "## a nested item\n" + "### another nested item\n" + "#: continued\n"
 				+ "# another item");
 
 		String docbook = out.toString();
 
 		TestUtil.println("DocBook: \n" + docbook);
 
 		// should look like this:
 		//
 		//		<orderedlist>
 		//			<listitem>
 		//				<para>a list</para>
 		//				<orderedlist>
 		//					<listitem>
 		//						<para>a nested item</para>
 		//						<orderedlist>
 		//							<listitem>
 		//								<para>another nested item</para>
 		//							</listitem>
 		//						</orderedlist>
 		//						<para>continued</para>
 		//					</listitem>
 		//				</orderedlist>
 		//			</listitem>
 		//			<listitem>
 		//				<para>another item</para>
 		//			</listitem>
 		//		</orderedlist>
 
 		assertTrue(docbook.contains("<orderedlist><listitem><para>a list</para><orderedlist><listitem><para>a nested item</para><orderedlist><listitem><para>another nested item</para></listitem></orderedlist><para>continued</para></listitem></orderedlist></listitem><listitem><para>another item</para></listitem></orderedlist>"));
 
 	}
 
 	public void testListNested() throws IOException {
 		String html = parser.parseToHtml("# a list\n## nested\n## nested2\n# level1\n\npara");
 
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<ol>"));
 		assertTrue(html.contains("<li>a list"));
 		assertTrue(html.contains("<li>nested"));
 		assertTrue(html.contains("</ol>"));
 	}
 
 	public void testListMixed() throws IOException {
 		// test for bug# 47
 		String html = parser.parseToHtml("# first\n* second");
 
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<ol><li>first</li></ol><ul><li>second</li></ul>"));
 	}
 
 	public void testListNestedMixed() throws IOException {
 		String html = parser.parseToHtml("# a list\n#* nested\n#* nested2\n# level1\n\npara");
 
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<ol><li>a list<ul><li>nested</li><li>nested2</li></ul></li><li>level1</li></ol>"));
 	}
 
 	public void testDefinitionList() {
 		String html = parser.parseToHtml(";Definition\n:item1\n:item2\na para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><dl><dt>Definition</dt><dd>item1</dd><dd>item2</dd></dl><p>a para</p></body>"));
 	}
 
 	public void testDefinitionList2() {
 		String html = parser.parseToHtml(";Definition : item1\n:item2\na para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><dl><dt>Definition</dt><dd>item1</dd><dd>item2</dd></dl><p>a para</p></body>"));
 	}
 
 	public void testDefinitionList3() {
 		String html = parser.parseToHtml(";Definition [http://www.foobar.com Foo Bar] : Foo Bar test 123\n;Definition 2 [http://www.foobarbaz.com Foo Bar Baz] : another definition\na para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("</head><body><dl><dt>Definition <a href=\"http://www.foobar.com\">Foo Bar</a></dt><dd>Foo Bar test 123</dd><dt>Definition 2 <a href=\"http://www.foobarbaz.com\">Foo Bar Baz</a></dt><dd>another definition</dd></dl><p>a para</p></body>"));
 	}
 
 	public void testIndented() {
 		String html = parser.parseToHtml("::Indented\na para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><dl><dd><dl><dd>Indented</dd></dl></dd></dl><p>a para</p></body>"));
 	}
 
 	public void testPreformatted() {
 		String html = parser.parseToHtml("normal para\n preformatted\n more pre\nnormal para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile(
 				"<body><p>normal para</p><pre>preformatted\\s+more pre\\s+</pre><p>normal para</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testPreformattedWithTag() {
 		String html = parser.parseToHtml("normal para\n<pre style=\"overflow:scroll\" class=\"TEST\">preformatted\n more pre\n</pre>normal para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile(
 				"<body><p>normal para</p><pre class=\"TEST\" style=\"overflow:scroll\">preformatted\\s+more pre\\s+</pre><p>normal para</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testPreformattedWithTagStartEndOnSameLine() {
 		String html = parser.parseToHtml("normal para\n<pre>preformatted</pre>normal para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile("<body><p>normal para</p><pre>preformatted\\s+</pre><p>normal para</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testPreformattedWithTagStartEndOnSameLine3() {
 		String html = parser.parseToHtml("normal para\n<pre>preformatted</pre>\nnormal para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile("<body><p>normal para</p><pre>preformatted\\s+</pre><p>normal para</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testPreformattedWithTagStartEndOnSameLine2() {
 		//see also BUG 381506 for the usage of tags:
 		String html = parser.parseToHtml("example:\n\n<pre><a href=\"show_bug.cgi\\?id\\=(.+?)\">.+?<span class=\"summary\">(.+?)</span></pre>\n\nIf");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile(
 				"<body><p>example:</p><pre>"
 						+ Pattern.quote("&lt;a href=\"show_bug.cgi\\?id\\=(.+?)\"&gt;.+?&lt;span class=\"summary\"&gt;(.+?)&lt;/span&gt;")
 						+ "\\s+</pre><p>If</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testPreformattedSource_bug349724() {
 		String html = parser.parseToHtml("normal para\n<source lang=\"javascript\">preformatted\n more pre\n</source>normal para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile(
 				"<p>normal para</p><pre class=\"source-javascript\">preformatted\\s+more pre\\s+</pre><p>normal para</p>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testPreformattedWithTagAndMarkup() {
 		//BUG 381506:
 		String html = parser.parseToHtml("example:\n\n<pre>a block\nWith '''Bold text''' or ''Italic text'' style\nIs not converted</pre>\n\nIf");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile(
 				"<body><p>example:</p><pre>"
 						+ "a block\\s+With '''Bold text''' or ''Italic text'' style\\s+Is not converted"
 						+ "\\s+</pre><p>If</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testPreformattedWithMarkup() {
 		//BUG 381506:
 		String html = parser.parseToHtml("normal para\n preformatted\n with '''Bold text''' or ''Italic text'' style\n more pre\nnormal para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile(
 				"<body><p>normal para</p><pre>preformatted\\s+with <b>Bold text</b> or <i>Italic text</i> style\\s+more pre\\s+</pre><p>normal para</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testPreformattedWithFont() {
 		//BUG 381506:
 		String html = parser.parseToHtml("normal para\n preformatted\n with <font color=\"red\">some red color</font>\n more pre\nnormal para");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile(
 				"<body><p>normal para</p><pre>preformatted\\s+with <font color=\"red\">some red color</font>\\s+more pre\\s+</pre><p>normal para</p></body>")
 				.matcher(html)
 				.find());
 	}
 
 	public void testHtmlTags() {
 		String html = parser.parseToHtml("normal para <b id=\"foo\">test heading</b>");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<p>normal para <b id=\"foo\">test heading</b></p>"));
 	}
 
 	public void testHtmlComment() {
 		String html = parser.parseToHtml("normal para <!-- test comment --> normal *foo*");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>normal para  normal *foo*</p></body>"));
 	}
 
 	public void testHtmlCodeWithNestedFormatting() {
 		// bug 325023
 		String html = parser.parseToHtml("<code>NonItalic=''Italic''</code>");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<p><code>NonItalic=<i>Italic</i></code></p>"));
 	}
 
 	public void testLinkInternalPageReference() {
 		String html = parser.parseToHtml("a [[Main Page]] reference to the Main Page");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <a href=\"/wiki/Main_Page\" title=\"Main Page\">Main Page</a> reference to the Main Page</p></body>"));
 	}
 
 	public void testLinkInternalPageAnchorReference() {
 		String html = parser.parseToHtml("a [[#Some link|alt text]] reference to an internal anchor");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <a href=\"#Some_link\">alt text</a> reference to an internal anchor</p></body>"));
 	}
 
 	public void testLinkInternalPageReferenceWithAltText() {
 		String html = parser.parseToHtml("a [[Main Page|alternative text]] reference to the Main Page");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <a href=\"/wiki/Main_Page\" title=\"Main Page\">alternative text</a> reference to the Main Page</p></body>"));
 	}
 
 	public void testLinkInternalPageReferenceWithAltText2() {
 		String html = parser.parseToHtml("[[Orion/Server_API/Preference API| Preference API]]");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<p><a href=\"/wiki/Orion/Server_API/Preference_API\" title=\"Orion/Server_API/Preference API\">Preference API</a></p>"));
 	}
 
 	public void testLinkInternalPageReferenceWithAltTextInTables() {
 		String html = parser.parseToHtml("{|\n" //
 				+ "| [[Orion/Server_API/Preference API| Preference API]]\n" //
 				+ "|}");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<td><a href=\"/wiki/Orion/Server_API/Preference_API\" title=\"Orion/Server_API/Preference API\">Preference API</a></td>"));
 	}
 
 	public void testLinkInternalCategoryReference() {
 		String html = parser.parseToHtml("a [[:Category:Help]] reference to the Main Page");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <a href=\"Category:Help\" title=\"Category:Help\">Category:Help</a> reference to the Main Page</p></body>"));
 	}
 
 	public void testHyperlinkImplied() {
 		String html = parser.parseToHtml("a http://example.com hyperlink");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <a href=\"http://example.com\">http://example.com</a> hyperlink</p></body>"));
 	}
 
 	public void testHyperlinkInternal() {
 		String html = parser.parseToHtml("Also see the [[Mylyn_FAQ#Installation_Troubleshooting | Installation FAQ]].");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<p>Also see the <a href=\"/wiki/Mylyn_FAQ#Installation_Troubleshooting\" title=\"Mylyn_FAQ#Installation_Troubleshooting\">Installation FAQ</a>.</p>"));
 	}
 
 	public void testHyperlinkQualifiedInternal() {
 		markupLanguage.setInternalLinkPattern("http://wiki.eclipse.org/Mylyn/{0}");
 		String html = parser.parseToHtml("Also see the [[Mylyn/FAQ#Installation_Troubleshooting | Installation FAQ]].");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<p>Also see the <a href=\"http://wiki.eclipse.org/Mylyn/FAQ#Installation_Troubleshooting\" title=\"Mylyn/FAQ#Installation_Troubleshooting\">Installation FAQ</a>.</p>"));
 	}
 
 	public void testHyperlinkInternalPiped() {
 		String html = parser.parseToHtml("[[MoDisco/QueryManager|create a query set]]");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<a href=\"/wiki/MoDisco/QueryManager\" title=\"MoDisco/QueryManager\">create a query set</a>"));
 	}
 
 	public void testHyperlinkInternalWithSpaces() {
 		markupLanguage.setInternalLinkPattern("http://wiki.eclipse.org/{0}");
 		String html = parser.parseToHtml("Also see the [[Mylyn/User Guide]].");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<p>Also see the <a href=\"http://wiki.eclipse.org/Mylyn/User_Guide\" title=\"Mylyn/User Guide\">Mylyn/User Guide</a>.</p>"));
 	}
 
 	public void testHyperlinkExternal() {
 		String html = parser.parseToHtml("a [http://example.com] hyperlink");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <a href=\"http://example.com\">http://example.com</a> hyperlink</p></body>"));
 	}
 
 	public void testHyperlinkExternalWithAltText() {
 		String html = parser.parseToHtml("a [http://example.com|Example] hyperlink");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <a href=\"http://example.com\">Example</a> hyperlink</p></body>"));
 	}
 
 	public void testHyperlinkExternalWithAltText2() {
 		String html = parser.parseToHtml("a [http://example.com Example Title] hyperlink");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <a href=\"http://example.com\">Example Title</a> hyperlink</p></body>"));
 	}
 
 	public void testImage() {
 		String html = parser.parseToHtml("a [[Image:foo.png]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <img border=\"0\" src=\"foo.png\"/> image</p></body>"));
 	}
 
 	public void testImageWithAltText() {
 		String html = parser.parseToHtml("a [[Image:foo.png|Example]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <img title=\"Example\" alt=\"Example\" border=\"0\" src=\"foo.png\"/> image</p></body>"));
 	}
 
 	public void testImageWithAltText2() {
 		String html = parser.parseToHtml("a [[Image:foo.png|Alt Text|Caption]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<img alt=\"Alt Text\" title=\"Caption\" border=\"0\" src=\"foo.png\"/>"));
 	}
 
 	public void testImageWithAltTextAndOptions() {
 		String html = parser.parseToHtml("a [[Image:foo.png|100px|center|Example]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <img width=\"100\" align=\"middle\" title=\"Example\" alt=\"Example\" border=\"0\" src=\"foo.png\"/> image</p></body>"));
 	}
 
 	public void testImageWithAltTextAndHeightWidth() {
 		String html = parser.parseToHtml("a [[Image:foo.png|100x220px]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <img height=\"220\" width=\"100\" border=\"0\" src=\"foo.png\"/> image</p></body>"));
 	}
 
 	public void testImageWithAltTextAndWidth() {
 		String html = parser.parseToHtml("a [[Image:foo.png|100px]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <img width=\"100\" border=\"0\" src=\"foo.png\"/> image</p></body>"));
 	}
 
 	public void testImageWithLinkInCaption() {
 		// example from http://en.wikipedia.org/wiki/International_Floorball_Federation
 		String html = parser.parseToHtml("[[Image:IFF Logo.JPG|left|the logo|Official logo of the [[International Floorball Federation]], floorball's governing body.]]");
 
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<img align=\"left\" alt=\"the logo\" title=\"Official logo of the [[International Floorball Federation]], floorball's governing body.\" border=\"0\" src=\"IFF_Logo.JPG\"/>"));
 	}
 
 	public void testImageWithLinkInCaptionThumbnail() {
 		// example from http://en.wikipedia.org/wiki/International_Floorball_Federation
 		String html = parser.parseToHtml("[[Image:IFF Logo.JPG|thumb|left|the logo|Official logo of the [[International Floorball Federation]], floorball's governing body.]]");
 
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<div class=\"thumb left\"><div class=\"thumbinner\"><a href=\"IFF_Logo.JPG\" class=\"image\"><img class=\"thumbimage\" align=\"left\" alt=\"the logo\" border=\"0\" src=\"IFF_Logo.JPG\"/></a><div class=\"thumbcaption\">Official logo of the <a href=\"/wiki/International_Floorball_Federation\" title=\"International Floorball Federation\">International Floorball Federation</a>, floorball's governing body.</div></div></div>"));
 	}
 
 	public void testImageWithTitle() {
 		String html = parser.parseToHtml("text text text text text text\n[[Image:Westminstpalace.jpg|150px|alt=A large clock tower and other buildings line a great river.|The Palace of Westminster]]");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<img width=\"150\" alt=\"A large clock tower and other buildings line a great river.\" title=\"The Palace of Westminster\" border=\"0\" src=\"Westminstpalace.jpg\"/>"));
 	}
 
 	public void testImageSimple() {
 		String html = parser.parseToHtml("[[Image:ImportFedoraGit.png]]");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<img border=\"0\" src=\"ImportFedoraGit.png\"/>"));
 	}
 
 	public void testImageWithLeadingWhitespace() {
 		String html = parser.parseToHtml("[[Image: SomeImage.png]]");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<img border=\"0\" src=\"SomeImage.png\"/>"));
 	}
 
 	public void testImageFile() {
 		String html = parser.parseToHtml("a [[File:foo.png]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <img border=\"0\" src=\"foo.png\"/> image</p></body>"));
 	}
 
 	public void testImageFile_Negative() {
 		String html = parser.parseToHtml("a [[FilImage:foo.png]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertFalse(html.contains("<img"));
 	}
 
 	public void testImage_Lower() {
 		String html = parser.parseToHtml("a [[image:foo.png]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <img border=\"0\" src=\"foo.png\"/> image</p></body>"));
 	}
 
 	public void testImageFile_Lower() {
 		String html = parser.parseToHtml("a [[file:foo.png]] image");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>a <img border=\"0\" src=\"foo.png\"/> image</p></body>"));
 	}
 
 	public void testTable() {
 		String html = parser.parseToHtml("{|\n" + "|Orange\n" + "|Apple\n" + "|-\n" + "|Bread\n" + "|Pie\n" + "|-\n"
 				+ "|Butter\n" + "|Ice cream \n" + "|}");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><table><tr><td>Orange</td><td>Apple</td></tr><tr><td>Bread</td><td>Pie</td></tr><tr><td>Butter</td><td>Ice cream </td></tr></table></body>"));
 	}
 
 	public void testTable2() {
 		String html = parser.parseToHtml("{|\n" + "|  Orange    ||   Apple   ||   more\n" + "|-\n"
 				+ "|   Bread    ||   Pie     ||   more\n" + "|-\n" + "|   Butter   || Ice cream ||  and more\n"
 				+ "|}\n");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><table><tr><td>Orange</td><td>Apple</td><td>more</td></tr><tr><td>Bread</td><td>Pie</td><td>more</td></tr><tr><td>Butter</td><td>Ice cream</td><td>and more</td></tr></table></body>"));
 	}
 
 	public void testTableHeadings() {
 		String html = parser.parseToHtml("{|\n" + "!  Fruit    !!   Quantity   !!  Price\n" + "|-\n"
 				+ "|   Apple    ||   lb     ||   0.99\n" + "|}\n");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><table><tr><th>Fruit</th><th>Quantity</th><th>Price</th></tr><tr><td>Apple</td><td>lb</td><td>0.99</td></tr></table></body>"));
 	}
 
 	public void testTableHeadingsMixed() {
 		String html = parser.parseToHtml("{|\n! headerCell || normalCell\n|-\n| normalCell2 !! headerCell2\n|}");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><table><tr><th>headerCell</th><td>normalCell</td></tr><tr><td>normalCell2</td><th>headerCell2</th></tr></table></body>"));
 	}
 
 	public void testTableLexicalOffsets() {
 		final RecordingDocumentBuilder builder = new RecordingDocumentBuilder();
 		parser.setBuilder(builder);
 		final String content = "{|\n" + "|  Orange    ||   Apple   ||   more\n" + "|-\n"
 				+ "|   Bread    ||   Pie     ||   more\n" + "|-\n" + "|   Butter   || Ice cream ||  and more\n"
 				+ "|}\n";
 		TestUtil.println(content);
 		parser.parse(content);
 		TestUtil.println("Events: \n" + builder);
 
 		for (RecordingDocumentBuilder.Event event : builder.getEvents()) {
 			if (event.text != null) {
 				int start = event.locator.getDocumentOffset();
 				int end = event.locator.getLineSegmentEndOffset() + event.locator.getLineDocumentOffset();
 				assertEquals(event.text.length(), end - start);
 				assertTrue(end >= start);
 				assertEquals(content.substring(start, end), event.text);
 			}
 		}
 
 	}
 
 	public void testTableIncomplete() {
 		final RecordingDocumentBuilder builder = new RecordingDocumentBuilder();
 		parser.setBuilder(builder);
 		final String content = "{|\n" + "|  Orange    ||   Apple   ||   more\n" + "|-\n"
 				+ "|   Bread    ||   Pie     ||   more\n" + "|-\n" + "|   Butter   || Ice cream ||  and more\n"
 				+ "| \n";
 		TestUtil.println(content);
 		parser.parse(content);
 		TestUtil.println("Events: \n" + builder);
 
 		for (RecordingDocumentBuilder.Event event : builder.getEvents()) {
 			if (event.text != null) {
 				int start = event.locator.getDocumentOffset();
 				int end = event.locator.getLineSegmentEndOffset() + event.locator.getLineDocumentOffset();
 				assertEquals(event.text.length(), end - start);
 				assertTrue(end >= start);
 				assertEquals(content.substring(start, end), event.text);
 			}
 		}
 
 	}
 
 	public void testTableIncomplete2() {
 		final RecordingDocumentBuilder builder = new RecordingDocumentBuilder();
 		parser.setBuilder(builder);
 		final String content = "{|\n" + "| foo |\n" + "|}";
 		TestUtil.println(content);
 		parser.parse(content);
 		TestUtil.println("Events: \n" + builder);
 
 		for (RecordingDocumentBuilder.Event event : builder.getEvents()) {
 			if (event.text != null) {
 				int start = event.locator.getDocumentOffset();
 				int end = event.locator.getLineSegmentEndOffset() + event.locator.getLineDocumentOffset();
 				assertEquals(event.text.length(), end - start);
 				assertTrue(end >= start);
 				assertEquals(content.substring(start, end), event.text);
 			}
 		}
 	}
 
 	public void testTableWithSyntax() {
 		final RecordingDocumentBuilder builder = new RecordingDocumentBuilder();
 		parser.setBuilder(builder);
 		String content = "{|\n" + "| <nowiki>'''''bold italic'''''</nowiki> || '''''bold italic''''' ||\n" + "|}";
 		TestUtil.println(content);
 		parser.parse(content);
 		TestUtil.println("Events: \n" + builder);
 
 		for (RecordingDocumentBuilder.Event event : builder.getEvents()) {
 			if (event.text != null) {
 				int start = event.locator.getDocumentOffset();
 				int end = event.locator.getLineSegmentEndOffset() + event.locator.getLineDocumentOffset();
 
 				assertTrue(end >= start);
 
 			}
 		}
 	}
 
 	public void testTableOptions() {
 		String html = parser.parseToHtml("{| border=\"1\"\n" + "|- style=\"font-style:italic;color:green;\"\n"
 				+ "| colspan=\"2\" | Orange || valign=\"top\" | Apple\n" + "|}");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<table border=\"1\"><tr style=\"font-style:italic;color:green;\">"));
 		assertTrue(html.contains("<td colspan=\"2\">Orange</td>"));
 		assertTrue(html.contains("<td valign=\"top\">Apple</td>"));
 	}
 
 	public void testTableOptions_CssClass() {
 		String html = parser.parseToHtml("{|class=\"foo\"\n|Some text\n|}");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<table class=\"foo\"><tr><td>Some text</td></tr></table>"));
 	}
 
 	public void testTableWithParagraphs() {
 		//BUG 381912:
 		StringBuilder sb = new StringBuilder();
 		sb.append("{|border=\"1\"\n");
 		sb.append("|\n");
 		sb.append("A paragraph with '''Bold text''' in a cell.\n");
 		sb.append("|\n");
 		sb.append("A cell ''containing'' more...\n");
 		sb.append("\n");
 		sb.append("Than one paragraph.\n");
 		sb.append("|}\n");
 
 		String html = parser.parseToHtml(sb.toString());
 		TestUtil.println("HTML: \n" + html);
 		Pattern pattern = Pattern.compile("<table border=\"1\">\\s*<tr>\\s*<td>\\s*<p>\\s*A paragraph with \\s*<b>\\s*Bold text\\s*</b>\\s* in a cell.\\s*</p>\\s*</td>\\s*<td>\\s*<p>\\s*A cell \\s*<i>\\s*containing\\s*</i>\\s* more...\\s*</p>\\s*<p>\\s*Than one paragraph.\\s*</p>\\s*</td>\\s*</tr>\\s*</table>");
 		assertContainsPattern(html, pattern);
 	}
 
 	public void testTableWithLongerText() {
 		//BUG 381912:
 		//See: http://www.mediawiki.org/wiki/Help:Tables "longer text and more complex wiki syntax inside table cells".
 		StringBuilder sb = new StringBuilder();
 		sb.append("{|border=\"1\"\n");
 		sb.append("|Sxto mesto kusoks ti sam, \n");
 		sb.append("Da skandalis studentis bezopasostif tut, \n");
 		sb.append("dost takai vcxera na mne\n");
 		sb.append("Mai na zxen problem zembulbas, \n");
 		sb.append("dost vozduh dusxijm kai te. \n");
 		sb.append("\n");
 		sb.append("Oliv slozxju informacias bi bez\n");
 		sb.append("om gde detes komnat,\n");
 		sb.append("To divaj neskolk pridijt ili\n");
 		sb.append("Ktor zapalka bezopasostif es tot. \n");
 		sb.append("|\n");
 		sb.append("* Sxto mesto kusoks ti sam\n");
 		sb.append("* Vi edat zaspatit zapomnitlubovijm sol\n");
 		sb.append("* dost takai vcxera na mne\n");
 		sb.append("|}\n");
 
 		String html = parser.parseToHtml(sb.toString());
 		TestUtil.println("HTML: \n" + html);
 		Pattern pattern = Pattern.compile("<table border=\"1\">\\s*<tr>\\s*<td>\\s*Sxto mesto kusoks ti sam,\\s*<p>\\s*Da skandalis studentis bezopasostif tut,\\s+dost takai vcxera na mne\\s+Mai na zxen problem zembulbas,\\s+dost vozduh dusxijm kai te.\\s*</p>\\s*<p>\\s*Oliv slozxju informacias bi bez\\s*om gde detes komnat,\\s*To divaj neskolk pridijt ili\\s*Ktor zapalka bezopasostif es tot.\\s*</p>\\s*</td>\\s*<td>\\s*<ul>\\s*<li>\\s*Sxto mesto kusoks ti sam\\s*</li>\\s*<li>\\s*Vi edat zaspatit zapomnitlubovijm sol\\s*</li>\\s*<li>\\s*dost takai vcxera na mne\\s*</li>\\s*</ul>\\s*</td>\\s*</tr>\\s*</table>");
 		assertContainsPattern(html, pattern);
 	}
 
 	public void testTableWithCodeInCellAndOptions() {
 		//BUG 381912:
 		StringBuilder sb = new StringBuilder();
 		sb.append("{|border=\"1\"\n");
 		sb.append("|\n");
 		sb.append("  some\n");
 		sb.append("|\n");
 		sb.append("  code\n");
 		sb.append("  multiline\n");
 		sb.append("|style=\"background-color:#FFFF00;\"|\n");
 		sb.append("  this is code in an highlighted cell\n");
 		sb.append("|}\n");
 
 		String html = parser.parseToHtml(sb.toString());
 		TestUtil.println("HTML: \n" + html);
 		Pattern pattern = Pattern.compile("<table border=\"1\">\\s*<tr>\\s*<td>\\s*<pre>\\s*some\n</pre>\\s*</td>\\s*<td>\\s*<pre>\\s*code\n multiline\n</pre>\\s*</td>\\s*<td style=\"background-color:#FFFF00;\">\\s*<pre>\\s*this is code in an highlighted cell\n</pre>\\s*</td>\\s*</tr>\\s*</table>");
 		assertContainsPattern(html, pattern);
 	}
 
 	public void testTableWithExplicitFirstRowAndRowSpan() {
 		//BUG 381912:
 		StringBuilder sb = new StringBuilder();
 		sb.append("{|border=\"1\"\n");
 		sb.append("|-\n");
 		sb.append("!colspan=\"6\"|XYZ uv\n");
 		sb.append("|-\n");
 		sb.append("|rowspan=\"2\"|X1 & X2\n");
 		sb.append("|y1\n");
 		sb.append("|y2\n");
 		sb.append("|y3\n");
 		sb.append("|colspan=\"2\"|Z9\n");
 		sb.append("|-\n");
 		sb.append("|z8\n");
 		sb.append("|colspan=\"2\"|T6\n");
 		sb.append("|u4\n");
 		sb.append("|U6\n");
 		sb.append("|}\n");
 
 		String html = parser.parseToHtml(sb.toString());
 		TestUtil.println("HTML: \n" + html);
 		Pattern pattern = Pattern.compile("<table border=\"1\">\\s*<tr>\\s*<th colspan=\"6\">\\s*XYZ uv\\s*</th>\\s*</tr>\\s*<tr>\\s*<td rowspan=\"2\">\\s*X1 &amp; X2\\s*</td>\\s*<td>\\s*y1\\s*</td>\\s*<td>\\s*y2\\s*</td>\\s*<td>\\s*y3\\s*</td>\\s*<td colspan=\"2\">\\s*Z9\\s*</td>\\s*</tr>\\s*<tr>\\s*<td>\\s*z8\\s*</td>\\s*<td colspan=\"2\">\\s*T6\\s*</td>\\s*<td>\\s*u4\\s*</td>\\s*<td>\\s*U6\\s*</td>\\s*</tr>\\s*</table>");
 		assertContainsPattern(html, pattern);
 	}
 
 	public void testEntityReference() {
 		String tests = "&Agrave; &Aacute; &Acirc; &Atilde; &Auml; &Aring; &AElig; &Ccedil; &Egrave; &Eacute; &Ecirc; &Euml; &Igrave; &Iacute; &Icirc; &Iuml; &Ntilde; &Ograve; &Oacute; &Ocirc; &Otilde; &Ouml; &Oslash; &Ugrave; &Uacute; &Ucirc; &Uuml; &szlig; &agrave; &aacute; &acirc; &atilde; &auml; &aring; &aelig; &ccedil; &egrave; &eacute; &ecirc; &euml; &igrave; &iacute; &icirc; &iuml; &ntilde; &ograve; &oacute; &ocirc; &oelig; &otilde; &ouml; &oslash; &ugrave; &uacute; &ucirc; &uuml; &yuml; &iquest; &iexcl; &sect; &para; &dagger; &Dagger; &bull; &ndash; &mdash; &lsaquo; &rsaquo; &laquo; &raquo; &lsquo; &rsquo; &ldquo; &rdquo; &trade; &copy; &reg; &cent; &euro; &yen; &pound; &curren; &#8304; &sup1; &sup2; &sup3; &#8308; &int; &sum; &prod; &radic; &minus; &plusmn; &infin; &asymp; &prop; &equiv; &ne; &le; &ge; &times; &middot; &divide; &part; &prime; &Prime; &nabla; &permil; &deg; &there4; &alefsym; &oslash; &isin; &notin; &cap; &cup; &sub; &sup; &sube; &supe; &not; &and; &or; &exist; &forall;  &rArr; &lArr; &dArr; &uArr; &hArr; &rarr; &darr; &uarr; &larr; &harr; &mdash; &ndash;";
 		final String[] allEntities = tests.split("\\s+");
 		assertTrue(allEntities.length > 100);
 		for (String testEntity : allEntities) {
 			// sanity check
 			assertTrue(testEntity.startsWith("&"));
 			assertTrue(testEntity.endsWith(";"));
 
 			String html = parser.parseToHtml(testEntity);
 			assertTrue(testEntity + " in " + html, html.contains(testEntity));
 			html = parser.parseToHtml(testEntity + " trailing text");
 			assertTrue(testEntity + " in " + html, html.contains(testEntity));
 			html = parser.parseToHtml(testEntity + "trailing text");
 			assertTrue(testEntity + " in " + html, html.contains(testEntity));
 			html = parser.parseToHtml("leading text " + testEntity);
 			assertTrue(testEntity + " in " + html, html.contains(testEntity));
 			html = parser.parseToHtml("leading text" + testEntity);
 			assertTrue(testEntity + " in " + html, html.contains(testEntity));
 		}
 	}
 
 	public void testTemplateEnDash() {
 		// note: spacing is very specific
 		String html = parser.parseToHtml("A{{ndash}}B");
 		TestUtil.println(html);
 		assertTrue(html.contains("<body><p>A&nbsp;&ndash; B</p></body>"));
 		html = parser.parseToHtml("A{{endash}}B");
 		TestUtil.println(html);
 		assertTrue(html.contains("<body><p>A&nbsp;&ndash; B</p></body>"));
 	}
 
 	public void testTemplateEmDash() {
 		// note: spacing is very specific
 		String html = parser.parseToHtml("A{{mdash}}B");
 		TestUtil.println(html);
 		assertTrue(html.contains("<body><p>A&nbsp;&mdash; B</p></body>"));
 		html = parser.parseToHtml("A{{emdash}}B");
 		TestUtil.println(html);
 		assertTrue(html.contains("<body><p>A&nbsp;&mdash; B</p></body>"));
 	}
 
 	public void testTemplateCurrentMonth() {
 		String html = parser.parseToHtml("{{CURRENTMONTH}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>[01]\\d</p>"));
 	}
 
 	public void testTemplateCurrentMonthName() {
 		String html = parser.parseToHtml("{{CURRENTMONTHNAME}}");
 		TestUtil.println(html);
 		assertContainsPattern(
 				html,
 				Pattern.compile("<p>(January|February|March|April|May|June|July|August|September|October|November|December)</p>"));
 	}
 
 	public void testTemplateCurrentMonthNameAbbrev() {
 		String html = parser.parseToHtml("{{CURRENTMONTHABBREV}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)</p>"));
 	}
 
 	public void testTemplateCurrentDay() {
 		String html = parser.parseToHtml("{{CURRENTDAY}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>[0123]\\d</p>"));
 	}
 
 	public void testTemplateCurrentDOW() {
 		String html = parser.parseToHtml("{{CURRENTDOW}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>\\d</p>"));
 	}
 
 	public void testTemplateCurrentDayName() {
 		String html = parser.parseToHtml("{{CURRENTDAYNAME}}");
 		TestUtil.println(html);
 		assertContainsPattern(html,
 				Pattern.compile("<p>(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)</p>"));
 	}
 
 	public void testTemplateCurrentTime() {
 		String html = parser.parseToHtml("{{CURRENTTIME}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>[012]\\d:[0-5]\\d</p>"));
 	}
 
 	public void testTemplateCurrentHour() {
 		String html = parser.parseToHtml("{{CURRENTHOUR}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>[012]\\d</p>"));
 	}
 
 	public void testTemplateCurrentWeek() {
 		String html = parser.parseToHtml("{{CURRENTWEEK}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>[0-5]\\d</p>"));
 	}
 
 	public void testTemplateUnmatched() {
 		String html = parser.parseToHtml("a{{ABogusTemplateName}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>a</p>"));
 
 		html = parser.parseToHtml("a{{#foo}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>a</p>"));
 	}
 
 	public void testTemplateCurrentTimestamp() {
 		String html = parser.parseToHtml("{{CURRENTTIMESTAMP}}");
 		TestUtil.println(html);
 		assertContainsPattern(html, Pattern.compile("<p>\\d{14}</p>"));
 	}
 
 	private void assertContainsPattern(String html, Pattern pattern) {
 		if (!pattern.matcher(html).find()) {
 			fail("Expected " + pattern + " but got " + html);
 		}
 	}
 
 	public void testDefinitionListIndenting() {
 		String markup = ": one\n: two\n\n: three\nfour\n:five";
 		String html = parser.parseToHtml(markup);
 		TestUtil.println(html);
 		assertTrue(html.contains("<body><dl><dd>one</dd><dd>two</dd></dl><dl><dd>three</dd></dl><p>four</p><dl><dd>five</dd></dl></body>"));
 	}
 
 	public void testParagraphBreaksOnPreformatted() {
 		String markup = "a normal para\n preformatted\n p\nnormal\n";
 		String html = parser.parseToHtml(markup);
 		TestUtil.println(html);
 		assertTrue(Pattern.compile("<body><p>a normal para</p><pre>preformatted\\s+p\\s+</pre><p>normal</p></body>",
 				Pattern.MULTILINE)
 				.matcher(html)
 				.find());
 	}
 
 	public void testParagraphBreaksOnHeading() {
 		String markup = "a normal para\n= h1 =\nnormal\n";
 		String html = parser.parseToHtml(markup);
 		TestUtil.println(html);
 		assertTrue(html.contains("<body><p>a normal para</p><h1 id=\"h1\">h1</h1><p>normal</p></body>"));
 	}
 
 	public void testComputeOutline() throws IOException {
 		OutlineParser outlineParser = new OutlineParser();
 		outlineParser.setMarkupLanguage(new MediaWikiLanguage());
 
 		OutlineItem outline = outlineParser.parse(readFully("sample.mediawiki"));
 
 		Set<String> topLevelLabels = new LinkedHashSet<String>();
 		Set<String> topLevelIds = new LinkedHashSet<String>();
 		List<OutlineItem> children = outline.getChildren();
 		for (OutlineItem item : children) {
 			topLevelLabels.add(item.getLabel());
 			topLevelIds.add(item.getId());
 		}
 		assertEquals(children.size(), topLevelIds.size());
 		assertEquals(children.size(), topLevelLabels.size());
 		assertTrue("Top-level labels: " + topLevelLabels, topLevelLabels.contains("Task-Focused UI"));
 	}
 
 	public void testCloneTemplateExcludes() {
 		markupLanguage.setTemplateExcludes("*foo");
 		MediaWikiLanguage copy = (MediaWikiLanguage) markupLanguage.clone();
 		assertEquals(markupLanguage.getTemplateExcludes(), copy.getTemplateExcludes());
 	}
 
 	public void testTemplateExcludes() {
 		// bug 367525
 		markupLanguage.setTemplateExcludes("one, two, four_five");
 		markupLanguage.setTemplates(Arrays.asList(new Template("one", "1"), new Template("two", "2"), new Template(
 				"three", "3"), new Template("four_five", "45")));
 		String html = parser.parseToHtml("a{{one}} and {{two}} and {{three}} and {{four_five}}");
 
 		TestUtil.println("HTML: \n" + html);
 
 		assertTrue(html.contains("<p>a and  and 3 and </p>"));
 	}
 
	public void testTemplateExcludesComplexNames() {
		//Bug 367525
		markupLanguage.setTemplateExcludes("#eclipseproject:technology.linux-distros");
		markupLanguage.setTemplates(Arrays.asList(new Template("#eclipseproject:technology.linux-distros",
				"! Not excluded - !")));
		String html = parser.parseToHtml("foo {{#eclipseproject:technology.linux-distros}} bar");

		TestUtil.println("HTML: \n" + html);

		assertTrue(html.contains("<p>foo  bar</p>"));
	}

	public void testTemplateExcludesRegEx() {
		//Bug 367525
		markupLanguage.setTemplateExcludes("*eclipseproject*, Linux_Tools");
		markupLanguage.setTemplates(Arrays.asList(new Template("Linux_Tools", "!Not excluded - Linux_Tools!"),
				new Template("#eclipseproject:technology.linux-distros", "!Not excluded - eclipseproject!")));
		String html = parser.parseToHtml("foo {{#eclipseproject:technology.linux-distros}} bar {{Linux_Tools}} baz");

		TestUtil.println("HTML: \n" + html);

		assertTrue(html.contains("<p>foo  bar  baz</p>"));
	}

 	public void testTableOfContents() throws IOException {
 		String html = parser.parseToHtml("= Table Of Contents =\n\n__TOC__\n\n= Top Header =\n\nsome text\n\n== Subhead ==\n\n== Subhead2 ==\n\n= Top Header 2 =\n\n== Subhead 3 ==\n\n=== Subhead 4 ===");
 
 		TestUtil.println("HTML: \n" + html);
 
 		assertTrue(html.contains("<a href=\"#Subhead2\">"));
 		assertTrue(html.contains("<h2 id=\"Subhead2\">"));
 		assertTrue(html.contains("href=\"#Subhead_4\""));
 		assertTrue(html.contains("<h3 id=\"Subhead_4\">"));
 	}
 
 	public void testTableOfContents_WithTextFollowingTOC() throws IOException {
 		String html = parser.parseToHtml("= Table Of Contents =\n\nfoo\n__TOC__ bar\n\n= Top Header =\n\nsome text\n\n== Subhead ==\n\n== Subhead2 ==\n\n= Top Header 2 =\n\n== Subhead 3 ==\n\n=== Subhead 4 ===");
 
 		TestUtil.println("HTML: \n" + html);
 
 		assertTrue(html.contains("<a href=\"#Subhead2\">"));
 		assertTrue(html.contains("<h2 id=\"Subhead2\">"));
 		assertTrue(html.contains("href=\"#Subhead_4\""));
 		assertTrue(html.contains("<h3 id=\"Subhead_4\">"));
 	}
 
 	public void testComment_SingleLine() throws IOException {
 		String html = parser.parseToHtml("<!-- comment -->");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body></body>"));
 	}
 
 	public void testComment_SingleLine_TrailingText() throws IOException {
 		String html = parser.parseToHtml("<!-- comment --> not a comment");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p> not a comment</p></body>"));
 	}
 
 	public void testComment_SingleLine_LeadingText() throws IOException {
 		String html = parser.parseToHtml("not a comment <!-- comment -->");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>not a comment </p></body>"));
 	}
 
 	public void testComment_SingleLine_LeadingTrailingText() throws IOException {
 		String html = parser.parseToHtml("not a comment <!-- comment --> more text");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>not a comment  more text</p></body>"));
 	}
 
 	public void testComment_MultiLine() throws IOException {
 		String html = parser.parseToHtml("<!-- comment\nwith\nMultiple lines of text -->\n");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body></body>"));
 	}
 
 	public void testComment_MultiLine_Multiple() throws IOException {
 		String html = parser.parseToHtml("<!-- comment\nwith\nMultiple lines of text -->\n<!-- another comment -->");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body></body>"));
 	}
 
 	public void testComment_MultiLine_Multiple2() throws IOException {
 		String html = parser.parseToHtml("<!-- comment\nwith\nMultiple lines of text -->abc<!-- another\ncomment -->");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>abc</p></body>"));
 	}
 
 	public void testComment_MultiLine_TrailingText() throws IOException {
 		String html = parser.parseToHtml("<!-- comment\nwith\nMultiple lines of text --> not a comment");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p> not a comment</p></body>"));
 	}
 
 	public void testComment_MultiLine_LeadingText() throws IOException {
 		String html = parser.parseToHtml("not a comment <!-- comment\nwith\nMultiple lines of text -->");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<body><p>not a comment </p></body>"));
 	}
 
 	public void testComment_MultiLine_LeadingTrailingText() throws IOException {
 		String html = parser.parseToHtml("not a comment <!-- comment\nwith\nMultiple lines of text --> more text");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(Pattern.compile("<body><p>not a comment\\s+more text</p></body>").matcher(html).find());
 	}
 
 	public void testImageFilenameCaseInsensitivity() {
 		String html = parser.parseToHtml("[[Image:foo.gif]]");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<img border=\"0\" src=\"foo.gif\"/>"));
 
 		Set<String> imageNames = new HashSet<String>();
 		imageNames.add("Foo.gif");
 		markupLanguage.setImageNames(imageNames);
 
 		html = parser.parseToHtml("[[Image:foo.gif]]");
 		TestUtil.println("HTML: \n" + html);
 		assertTrue(html.contains("<img border=\"0\" src=\"Foo.gif\"/>"));
 	}
 
 	public void testHeadingWithHtmlTags() {
 		String html = parser.parseToHtml("= <span style=\"font-family:monospace\">Heading Text</span> =\n\n text");
 
 		TestUtil.println("HTML: \n" + html);
 
 		assertTrue(html.contains("<h1 id=\"Heading_Text\"><span style=\"font-family:monospace\">Heading Text</span></h1>"));
 	}
 
 	private String readFully(String resource) throws IOException {
 		Reader reader = new InputStreamReader(MediaWikiLanguageTest.class.getResourceAsStream(resource));
 		StringWriter writer = new StringWriter();
 		try {
 			int i;
 			while ((i = reader.read()) != -1) {
 				writer.write(i);
 			}
 		} finally {
 			reader.close();
 		}
 		return writer.toString();
 	}
 }
