 //plasmaCrawlWorker.java 
 //------------------------
 //part of YaCy
 //(C) by Michael Peter Christen; mc@yacy.net
 //first published on http://www.anomic.de
 //Frankfurt, Germany, 2006
 //
 // $LastChangedDate: 2006-08-12 16:28:14 +0200 (Sa, 12 Aug 2006) $
 // $LastChangedRevision: 2397 $
 // $LastChangedBy: theli $
 //
 //This program is free software; you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation; either version 2 of the License, or
 //(at your option) any later version.
 //
 //This program is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with this program; if not, write to the Free Software
 //Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 package de.anomic.crawler.retrieval;
 
 import java.io.IOException;
 import java.util.Date;
 
 import de.anomic.crawler.Latency;
 import de.anomic.data.Blacklist;
 import de.anomic.document.Parser;
 import de.anomic.http.client.Client;
 import de.anomic.http.metadata.HeaderFramework;
 import de.anomic.http.metadata.RequestHeader;
 import de.anomic.http.metadata.ResponseContainer;
 import de.anomic.search.Switchboard;
 import de.anomic.yacy.yacyURL;
 import de.anomic.yacy.logging.Log;
 
 public final class HTTPLoader {
 
     private static final String DEFAULT_ENCODING = "gzip,deflate";
     private static final String DEFAULT_LANGUAGE = "en-us,en;q=0.5";
     private static final String DEFAULT_CHARSET = "ISO-8859-1,utf-8;q=0.7,*;q=0.7";
     private static final long   DEFAULT_MAXFILESIZE = 1024 * 1024 * 10;
     public  static final int    DEFAULT_CRAWLING_RETRY_COUNT = 5;
     public  static final String crawlerUserAgent = "yacybot (" + Client.getSystemOST() +") http://yacy.net/bot.html";
     public  static final String yacyUserAgent = "yacy (" + Client.getSystemOST() +") yacy.net";
     
     /**
      * The socket timeout that should be used
      */
     private final int socketTimeout;
     
     /**
      * The maximum allowed file size
      */
     //private long maxFileSize = -1;
     
     //private String acceptEncoding;
     //private String acceptLanguage;
     //private String acceptCharset;
     private final Switchboard sb;
     private final Log log;
     
     public HTTPLoader(final Switchboard sb, final Log theLog) {
         this.sb = sb;
         this.log = theLog;
         
         // refreshing timeout value
         this.socketTimeout = (int) sb.getConfigLong("crawler.clientTimeout", 10000);
     }  
    
     public Response load(final Request entry, final boolean acceptOnlyParseable) throws IOException {
         long start = System.currentTimeMillis();
         Response doc = load(entry, acceptOnlyParseable, DEFAULT_CRAWLING_RETRY_COUNT);
         Latency.update(entry.url().hash().substring(6), entry.url().getHost(), System.currentTimeMillis() - start);
         return doc;
     }
     
     private Response load(final Request request, boolean acceptOnlyParseable, final int retryCount) throws IOException {
 
         if (retryCount < 0) {
             sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, "redirection counter exceeded").store();
             throw new IOException("Redirection counter exceeded for URL " + request.url().toString() + ". Processing aborted.");
         }
         
         final String host = request.url().getHost();
        if (host == null || host.length() < 2) throw new IOException("host is not well-formed: '" + host + "'");
         final String path = request.url().getFile();
         int port = request.url().getPort();
         final boolean ssl = request.url().getProtocol().equals("https");
         if (port < 0) port = (ssl) ? 443 : 80;
         
         // if not the right file type then reject file
         if (acceptOnlyParseable) {
             String supportError = Parser.supportsExtension(request.url());
             if (supportError != null) {
                 sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, supportError);
                 throw new IOException("REJECTED WRONG EXTENSION TYPE: " + supportError);
             }
         }
         
         // check if url is in blacklist
         final String hostlow = host.toLowerCase();
         if (Switchboard.urlBlacklist.isListed(Blacklist.BLACKLIST_CRAWLER, hostlow, path)) {
             sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, "url in blacklist").store();
             throw new IOException("CRAWLER Rejecting URL '" + request.url().toString() + "'. URL is in blacklist.");
         }
         
         // take a file from the net
         Response response = null;
         final long maxFileSize = sb.getConfigLong("crawler.http.maxFileSize", DEFAULT_MAXFILESIZE);
 
         // create a request header
         final RequestHeader requestHeader = new RequestHeader();
         requestHeader.put(HeaderFramework.USER_AGENT, crawlerUserAgent);
         yacyURL refererURL = null;
         if (request.referrerhash() != null) refererURL = sb.getURL(request.referrerhash());
         if (refererURL != null) requestHeader.put(RequestHeader.REFERER, refererURL.toNormalform(true, true));
         requestHeader.put(HeaderFramework.ACCEPT_LANGUAGE, sb.getConfig("crawler.http.acceptLanguage", DEFAULT_LANGUAGE));
         requestHeader.put(HeaderFramework.ACCEPT_CHARSET, sb.getConfig("crawler.http.acceptCharset", DEFAULT_CHARSET));
         requestHeader.put(HeaderFramework.ACCEPT_ENCODING, sb.getConfig("crawler.http.acceptEncoding", DEFAULT_ENCODING));
 
         // HTTP-Client
         final Client client = new Client(socketTimeout, requestHeader);
         
         ResponseContainer res = null;
         try {
             // send request
             res = client.GET(request.url().toString(), maxFileSize);
             // FIXME: 30*-handling (bottom) is never reached
             // we always get the final content because httpClient.followRedirects = true
 
             if (res.getStatusCode() == 200 || res.getStatusCode() == 203) {
                 // the transfer is ok
                 
                 if (acceptOnlyParseable) {
                 	// if the response has not the right file type then reject file
                     String supportError = Parser.supports(request.url(), res.getResponseHeader().mime());
                     if (supportError != null) {
                     	sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, supportError);
                     	throw new IOException("REJECTED WRONG MIME TYPE: " + supportError);
                     }
                 }
                 
                 // we write the new cache entry to file system directly
                 res.setAccountingName("CRAWLER");
                 final byte[] responseBody = res.getData();
                 long contentLength = responseBody.length;
 
                 // check length again in case it was not possible to get the length before loading
                 if (maxFileSize > 0 && contentLength > maxFileSize) {
                 	sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, "file size limit exceeded");                    
                 	throw new IOException("REJECTED URL " + request.url() + " because file size '" + contentLength + "' exceeds max filesize limit of " + maxFileSize + " bytes. (GET)");
                 }
 
                 // create a new cache entry
                 response = new Response(
                         request,
                         requestHeader,
                         res.getResponseHeader(), 
                         res.getStatusLine(),
                         sb.crawler.profilesActiveCrawls.getEntry(request.profileHandle()),
                         responseBody
                 );
 
                 return response;
             } else if (res.getStatusLine().startsWith("30")) {
                 if (res.getResponseHeader().containsKey(HeaderFramework.LOCATION)) {
                     // getting redirection URL
                     String redirectionUrlString = res.getResponseHeader().get(HeaderFramework.LOCATION);
                     redirectionUrlString = redirectionUrlString.trim();
 
                     if (redirectionUrlString.length() == 0) {
                         sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, "redirection header empy");
                         throw new IOException("CRAWLER Redirection of URL=" + request.url().toString() + " aborted. Location header is empty.");
                     }
                     
                     // normalizing URL
                     final yacyURL redirectionUrl = yacyURL.newURL(request.url(), redirectionUrlString);
 
                     // restart crawling with new url
                     this.log.logInfo("CRAWLER Redirection detected ('" + res.getStatusLine() + "') for URL " + request.url().toString());
                     this.log.logInfo("CRAWLER ..Redirecting request to: " + redirectionUrl);
 
                     // if we are already doing a shutdown we don't need to retry crawling
                     if (Thread.currentThread().isInterrupted()) {
                         sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, "server shutdown");
                         throw new IOException("CRAWLER Retry of URL=" + request.url().toString() + " aborted because of server shutdown.");
                     }
 
                     // generating url hash
                     final String urlhash = redirectionUrl.hash();
                     
                     // check if the url was already indexed
                     final String dbname = sb.urlExists(urlhash);
                     if (dbname != null) {
                         sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, "redirection to double content");
                         throw new IOException("CRAWLER Redirection of URL=" + request.url().toString() + " ignored. The url appears already in db " + dbname);
                     }
                     
                     // retry crawling with new url
                     request.redirectURL(redirectionUrl);
                     return load(request, acceptOnlyParseable, retryCount - 1);
                 }
             } else {
                 // if the response has not the right response type then reject file
                 sb.crawlQueues.errorURL.newEntry(request, sb.peers.mySeed().hash, new Date(), 1, "wrong http status code " + res.getStatusCode() +  ")");
                 throw new IOException("REJECTED WRONG STATUS TYPE '" + res.getStatusLine() + "' for URL " + request.url().toString());
             }
         } finally {
             if(res != null) {
                 // release connection
                 res.closeStream();
             }
         }
         return response;
     }
     
 }
