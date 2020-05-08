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
 package org.paxle.crawler.ftp.impl;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Dictionary;
 
 import junit.framework.TestCase;
 
 import org.osgi.service.cm.ConfigurationException;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.core.io.temp.ITempDir;
 import org.paxle.core.io.temp.ITempFileManager;
 import org.paxle.crawler.CrawlerContext;
 import org.paxle.crawler.impl.CrawlerContextLocal;
 
 public class FtpCrawlerOnlineTest extends TestCase {
 	
 	private FtpCrawler crawler;
 	private ITempFileManager tempManager;
 	private ICrawlerDocument crawlerDoc;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		// create dummy temp-file manager
 		this.tempManager = new ITempFileManager() {
 			public File createTempFile() throws IOException {
 				File tmp = File.createTempFile("test", ".tmp");
 				tmp.deleteOnExit();
 				return tmp;
 			}
 			public void releaseTempFile(File arg0) throws FileNotFoundException, IOException {
 				arg0.delete();				
 			}
 			public void removeTempDirFor(String... arg0) { }
			public void setTempDirFor(ITempDir arg0, String... arg1) { }
			public boolean isKnown(File file) { return true; }			
 		};
 		
 		// init crawler-context
 		CrawlerContextLocal threadLocal = new CrawlerContextLocal(){{
 			this.supportedMimeTypes.add("text/html");
 			this.tempFileManager = tempManager;
 		}};
 		CrawlerContext.setThreadLocal(threadLocal);
 		
 		// create crawler
 		this.crawler = new FtpCrawler();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		if (this.tempManager != null && this.crawlerDoc != null && this.crawlerDoc.getContent() != null) {
 			this.tempManager.releaseTempFile(this.crawlerDoc.getContent());
 		}
 	}
 	
 	public void testReadDirectory() {
 		URI testUri = URI.create("ftp://ftp.debian.org/");
 		this.crawlerDoc = this.crawler.request(testUri);		
 		assertNotNull(crawlerDoc);		
 		assertEquals(testUri, crawlerDoc.getLocation());
 		assertEquals(ICrawlerDocument.Status.OK, crawlerDoc.getStatus());
 		assertEquals("text/html",crawlerDoc.getMimeType());
 		assertNotNull(crawlerDoc.getContent());
 		assertTrue(crawlerDoc.getContent().exists());
 		assertTrue(crawlerDoc.getContent().length() > 0);
 	}
 	
 	public void testReadDocument() {
 		URI testUri = URI.create("ftp://ftp.debian.org/debian/README");
 		this.crawlerDoc = this.crawler.request(testUri);
 		assertNotNull(crawlerDoc);		
 		assertEquals(testUri, crawlerDoc.getLocation());
 		assertEquals(ICrawlerDocument.Status.OK, crawlerDoc.getStatus());
 		assertNotNull(crawlerDoc.getContent());
 		assertTrue(crawlerDoc.getContent().exists());
 		assertTrue(crawlerDoc.getContent().length() > 0);
 	}
 	
 	public void _testReadDocumentMaxDownloadSizeLimit() throws ConfigurationException {
 		URI testUri = URI.create("ftp://ftp.debian.org/debian/README");
 
 		// change crawler settings
 		Dictionary<String, Object> props = this.crawler.getDefaults();
 		props.put(FtpCrawler.PROP_MAXDOWNLOAD_SIZE, new Integer(500));
 		this.crawler.updated(props);
 		
 		// download document
 		this.crawlerDoc = this.crawler.request(testUri);
 		assertNotNull(crawlerDoc);		
 		assertEquals(testUri, crawlerDoc.getLocation());
 		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, crawlerDoc.getStatus());
 		assertNull(crawlerDoc.getContent());
 	}
 }
