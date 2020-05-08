 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.parser.html.impl;
 
 import java.io.File;
 import java.net.URI;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.LinkInfo;
 import org.paxle.core.doc.LinkInfo.Status;
 import org.paxle.parser.impl.AParserTest;
 
 public class HtmlParserTest extends AParserTest {
 	
 	private HtmlParser parser;
 	
 	private static final String[] TEST_CASES = {
 		"svgopen.org_index.html",
 		"javascript_test.html",
 		"baseHrefTest.html",
 		"draft-ietf-webdav-rfc2518bis-12-from-11.diff.html",
 		"imdb_biographies_o.html",		// XXX: produced an stack-overflow error with htmlparser 2006
 		"javascript_tcom.html",
 		"pc-welt_archiv02_knowhow.html",
 		"pc-welt_archiv07_knowhow.html",
 //		"imdb_biographies_s.html",		// XXX: you need to set Xmx to 128m to run this
 //		"perltoc-search.cpan.org.html",	// XXX: you need to set Xmx to 128m to run this
 	};
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		// creating the parser
 		this.parser = new HtmlParser();
 		this.parser.activate(null);
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		this.parser.deactivate(null);
 		super.tearDown();
 	}
 	
 	public void testHtmlBaseHref() throws Exception {
 		final File testResource = new File("src/test/resources/", "baseHrefTest.html");
 		final IParserDocument pdoc = parser.parse(URI.create("http://www.example.org/baseHrefTest.html"), null, testResource);
 		assertNotNull(pdoc);
 		final Iterator<URI> it = pdoc.getLinks().keySet().iterator();
 		assertTrue(it.hasNext());
 		assertEquals(URI.create("http://www.example.net/test/blubb"), it.next());
 	}
 	
 	/** does not work as expected yet */
 	public void testHtmlParser() throws Exception {
 		final File testResources = new File("src/test/resources/");
 		for (final String testCase : TEST_CASES) {
 			final IParserDocument pdoc = parser.parse(new URI("http://www.example.org/" + testCase), null, new File(testResources, testCase));
 			assertNotNull(pdoc);
 			assertNotNull(pdoc.getMimeType());
 		}
 	}
 	
 	public void testHtmlParserThreaded() throws Exception {
 		final Thread[] threads = new Thread[TEST_CASES.length];
 		final File testResources = new File("src/test/resources/");
 		
 		for (int i=0; i<TEST_CASES.length; i++) {
 			final String testCase = TEST_CASES[i];
 			threads[i] = new Thread() {
 				@Override
 				public void run() {
 					super.setName("test-" + testCase);
 					try {
 						// System.out.println("started");
 						final IParserDocument pdoc = parser.parse(new URI("http://www.example.org/" + testCase), null, new File(testResources, testCase));
 						assertNotNull(pdoc);
 						/*
 						System.out.println(testCase);
 						System.out.println(pdoc.getLinks().size());
 						System.out.println(pdoc.getTextFile().length());
 						System.out.println();*/
 					} catch (Exception e) { e.printStackTrace(); }
 				}
 			};
 			threads[i].start();
 		}
 		
 		for (int i=0; i<threads.length; i++)
 			threads[i].join();
 	}
 	
 	public void testIndexRestrictions() throws Exception {
 		final File testResource = new File("src/test/resources/restricted.html");
 		final IParserDocument pdoc = parser.parse(new URI("http://www.example.org/restricted.html"), null, testResource);
 		assertNotNull(pdoc);
 		
 		assertEquals(1, pdoc.getLinks().size());
 		final Map.Entry<URI,LinkInfo> link = pdoc.getLinks().entrySet().iterator().next();
 		assertTrue(link.getValue().hasStatus(Status.FILTERED));
 		assertEquals(new URI("http://www.example.org/test.html"), link.getKey());
 		assertEquals("Test-Link", link.getValue().getTitle());
 		
 		assertEquals("Restricted Test Page", pdoc.getTitle());
 		
 		final int flags = (IParserDocument.FLAG_NOFOLLOW | IParserDocument.FLAG_NOINDEX);
 		assertEquals(flags, pdoc.getFlags() & flags);
 	}
 	
 	private static final String[][] REPL_CASES = {
 		{ "Il y a une &eacute;cole", "Il y a une \u00E9cole" },
 		{ "Da &amp; dort passierte &quot;etwas&quot;.", "Da & dort passierte \"etwas\"." }
 	};
 	
 	public void testHtmlReplace() throws Exception {
 		for (int i=0; i<REPL_CASES.length; i++) {
 			final String repl = HtmlTools.deReplaceHTML(REPL_CASES[i][0]);
 			final String exp = REPL_CASES[i][1];
 			assertNotNull(repl);
 			assertEquals(exp, repl);
 		}
 	}
 	
 	public void testParseWindows1256Html() throws Exception {
 		final File testResources = new File("src/test/resources/maktoobblog.com.html");
 
 		final IParserDocument pdoc = parser.parse(new URI("http://maktoobblog.com.html/"), null, testResources);
 		assertNotNull(pdoc);
 		assertEquals("\u0645\u0646\u0627\u0647\u0644 \u0627\u0644\u062a\u0631\u0628\u064a\u0629", pdoc.getTitle());
 		
 		LinkInfo lInfo = pdoc.getLinks().get(URI.create("http://www.maktoobblog.com/nextBlog.php"));
 		assertNotNull(lInfo);
 		assertEquals("\u0627\u0644\u0645\u062f\u0648\u0651\u0646\u0629 \u0627\u0644\u062a\u0627\u0644\u064a\u0629", lInfo.getTitle());
 	}
 }
