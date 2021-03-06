 /* Copyright 2010-2013 Norconex Inc.
  * 
  * This file is part of Norconex HTTP Collector.
  * 
  * Norconex HTTP Collector is free software: you can redistribute it and/or 
  * modify it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Norconex HTTP Collector is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of 
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Norconex HTTP Collector. If not, 
  * see <http://www.gnu.org/licenses/>.
  */
 package com.norconex.collector.http.crawler;
 
 import com.norconex.collector.http.doc.HttpDocument;
 import com.norconex.collector.http.doc.IHttpDocumentProcessor;
 import com.norconex.collector.http.fetch.IHttpDocumentFetcher;
 import com.norconex.collector.http.fetch.IHttpHeadersFetcher;
 import com.norconex.collector.http.filter.IHttpDocumentFilter;
 import com.norconex.collector.http.filter.IHttpHeadersFilter;
 import com.norconex.collector.http.filter.IURLFilter;
 import com.norconex.collector.http.robot.RobotsMeta;
 import com.norconex.collector.http.robot.RobotsTxt;
 import com.norconex.commons.lang.map.Properties;
 
 /**
  * <p>Allows implementers to react to any crawler-specific events.</p>
  * <p><b>CAUTION:</b> Implementors should not implement this interface directly.
  * They are strongly advised to subclass the
  * {@link HttpCrawlerEventAdapter} class instead for forward compatibility.</p>
  * <p>Keep in mind that if defined as part of crawler defaults, 
  * a single instance of this listener will be shared amongst crawlers
  * (unless overwritten).</p>
  * @author Pascal Essiembre
  * @see HttpCrawlerEventFirer
  */
 public interface IHttpCrawlerEventListener {
     
 	//TODO add two new methods for headers and document checksum 
 	//(i.e. modified vs not-modified.   Or rely on rejected flag?
 	//TODO add documentDeleted
	//TODO add urlProcessed with CrawlStatus to help with reporting
    //TODO refactor even handling by having 1 method only accepting an 
    //event interface (or superclass).  More scalable that way.
    
     void crawlerStarted(HttpCrawler crawler);
     void documentRobotsTxtRejected(HttpCrawler crawler,
             String url, IURLFilter filter, RobotsTxt robotsTxt);
     void documentRobotsMetaRejected(HttpCrawler crawler,
             String url, RobotsMeta robotsMeta);
     void documentURLRejected(
             HttpCrawler crawler, String url, IURLFilter filter);
     void documentHeadersFetched(HttpCrawler crawler,
             String url, IHttpHeadersFetcher headersFetcher, Properties headers);
     void documentHeadersRejected(HttpCrawler crawler,
             String url, IHttpHeadersFilter filter, Properties headers);
     void documentFetched(HttpCrawler crawler, 
             HttpDocument document, IHttpDocumentFetcher fetcher);
     void documentURLsExtracted(HttpCrawler crawler,HttpDocument document);
     void documentRejected(HttpCrawler crawler,
             HttpDocument document, IHttpDocumentFilter filter);
     void documentPreProcessed(HttpCrawler crawler,
             HttpDocument document, IHttpDocumentProcessor preProcessor);
     void documentImported(HttpCrawler crawler, HttpDocument document);
     void documentImportRejected(HttpCrawler crawler, HttpDocument document);
     void documentPostProcessed(HttpCrawler crawler,
             HttpDocument document, IHttpDocumentProcessor postProcessor);
     void documentCrawled(HttpCrawler crawler, HttpDocument document);
     void crawlerFinished(HttpCrawler crawler);
 }
