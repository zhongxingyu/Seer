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
 
 import java.net.URI;
import java.util.Dictionary;
import java.util.Hashtable;
 
 import org.osgi.service.cm.ConfigurationException;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.crawler.impl.ACrawlerTest;
 
 public class FtpCrawlerOnlineTest extends ACrawlerTest {
 	
 	private FtpCrawler crawler;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		// create crawler
 		this.crawler = new FtpCrawler(){{
 			this.contextLocal = crawlerContextLocal;
 		}};
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
 	
 	public void testReadDocumentMaxDownloadSizeLimit() throws ConfigurationException {
 		URI testUri = URI.create("ftp://ftp.debian.org/debian/README");
 
 		// change crawler settings
		final Dictionary<String, Object> props = new Hashtable<String, Object>();
 		props.put(FtpCrawler.PROP_MAXDOWNLOAD_SIZE, Integer.valueOf(500));
 		this.crawler.activate(props);
 		
 		// download document
 		this.crawlerDoc = this.crawler.request(testUri);
 		assertNotNull(crawlerDoc);		
 		assertEquals(testUri, crawlerDoc.getLocation());
 		assertEquals(ICrawlerDocument.Status.UNKNOWN_FAILURE, crawlerDoc.getStatus());
 		assertNull(crawlerDoc.getContent());
 	}
 }
