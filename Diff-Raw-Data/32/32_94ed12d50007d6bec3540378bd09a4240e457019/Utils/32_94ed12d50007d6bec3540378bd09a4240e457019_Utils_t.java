 /*******************************************************************************
  * Copyright (c) 2011 consiliens (consiliens@gmail.com).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  ******************************************************************************/
 package com.github.consiliens.harv.util;
 
 import static com.github.consiliens.harv.util.Invoke.invoke;
 import static com.github.consiliens.harv.util.Invoke.invokeStatic;
 
 import java.io.File;
 import java.net.HttpCookie;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.RequestLine;
 import org.apache.http.StatusLine;
 
 import com.subgraph.vega.api.model.IModel;
 import com.subgraph.vega.api.model.IWorkspace;
 import com.subgraph.vega.api.model.IWorkspaceEntry;
 import com.subgraph.vega.api.model.requests.IRequestLogRecord;
 import com.subgraph.vega.internal.model.Model;
 import com.subgraph.vega.internal.model.WorkspaceEntry;
 
 import edu.umass.cs.benchlab.har.HarCache;
 import edu.umass.cs.benchlab.har.HarContent;
 import edu.umass.cs.benchlab.har.HarCookie;
 import edu.umass.cs.benchlab.har.HarCookies;
 import edu.umass.cs.benchlab.har.HarEntry;
 import edu.umass.cs.benchlab.har.HarEntryTimings;
 import edu.umass.cs.benchlab.har.HarHeader;
 import edu.umass.cs.benchlab.har.HarHeaders;
 import edu.umass.cs.benchlab.har.HarPostData;
 import edu.umass.cs.benchlab.har.HarQueryString;
 import edu.umass.cs.benchlab.har.HarRequest;
 import edu.umass.cs.benchlab.har.HarResponse;
 import edu.umass.cs.benchlab.har.ISO8601DateFormatter;
 
 public class Utils {
 
     public static void p(Object o) {
         if (o == null) {
             System.out.println("null");
             return;
         }
 
         System.out.println(o.toString());
     }
 
     /** Create a fake object until Vega adds support for timings. **/
     public static HarEntryTimings getFakeHarEntryTimings() {
         final long blocked = 0L;
         final long dns = 0L;
         final long connect = 0L;
         final long send = 0L;
         final long wait = 0L;
         final long receive = 0L;
         final long ssl = 0L;
         final String comment = "";
 
         return new HarEntryTimings(blocked, dns, connect, send, wait, receive, ssl, comment);
     }
 
     public static void convertRecordsToHAR(final List<IRequestLogRecord> recordsList, final HarManager har) {
         for (final IRequestLogRecord record : recordsList) {
             Date startedDateTime = null;
             try {
                 startedDateTime = ISO8601DateFormatter.parseDate(ISO8601DateFormatter.format(new Date(record
                         .getTimestamp())));
             } catch (ParseException e) {
                 e.printStackTrace();
             }
 
             final long time = record.getRequestMilliseconds();
             final HttpRequest httpRequest = record.getRequest();
             final RequestLine line = httpRequest.getRequestLine();
             final String method = line.getMethod();
             final String url = line.getUri();
             final String httpVersion = line.getProtocolVersion().toString();
             final HarCookies requestCookies = new HarCookies();
             final HarHeaders requestHeaders = new HarHeaders();
             final HarQueryString queryString = new HarQueryString();
             final HarPostData postData = null;
             final long headersSize = -1;
             final long bodySize = -1;
             final String comment = null;
 
             for (Header header : httpRequest.getAllHeaders()) {
                 final String headerName = header.getName();
                 final String headerValue = header.getValue();
 
                 requestHeaders.addHeader(new HarHeader(headerName, headerValue));
                 
                 if (headerValue != null && headerName.equalsIgnoreCase("Cookie")) {
                     List<HttpCookie> parsedCookies = HttpCookie.parse(headerValue);
                     for (HttpCookie aCookie : parsedCookies) {
                         // expires = max age
                         // does HttpCookie support the "httpOnly" field?  hard coded to false for now.
                         final boolean httpOnly = false;
                         requestCookies.addCookie(new HarCookie(aCookie.getName(), aCookie.getValue(), aCookie.getPath(), aCookie.getDomain(),
                                 new Date(aCookie.getMaxAge()), httpOnly, aCookie.getSecure(), comment
                                 ));
                     }
                 }
             }
 
             String connection = String.valueOf(record.getRequestId());
 
             final String pageRef = null;
             final HarCache cache = null;
             final HarEntryTimings timings = getFakeHarEntryTimings();
             final String serverIPAddress = null;
 
             final HarRequest request = new HarRequest(method, url, httpVersion, requestCookies, requestHeaders, queryString,
                     postData, headersSize, bodySize, comment);
 
             // Process response.
             final HttpResponse httpResponse = record.getResponse();
             final StatusLine responseStatus = httpResponse.getStatusLine();
             final int status = responseStatus.getStatusCode();
             final String statusText = responseStatus.getReasonPhrase();
             final String responseHttpVersion = httpResponse.getProtocolVersion().toString();
 
             final HarCookies responseCookies = new HarCookies();
             final HarHeaders responseHeaders = new HarHeaders();
 
             for (Header header : httpResponse.getAllHeaders()) {
                 final String headerName = header.getName();
                 final String headerValue = header.getValue();
 
                 responseHeaders.addHeader(new HarHeader(headerName, headerValue));
                 
                 if (headerValue != null && headerName.equalsIgnoreCase("Cookie")) {
                     List<HttpCookie> parsedCookies = HttpCookie.parse(headerValue);
                     for (HttpCookie aCookie : parsedCookies) {
                         // expires = max age
                         // does HttpCookie support the "httpOnly" field?  hard coded to false for now.
                         final boolean httpOnly = false;
                         responseCookies.addCookie(new HarCookie(aCookie.getName(), aCookie.getValue(), aCookie.getPath(), aCookie.getDomain(),
                                 new Date(aCookie.getMaxAge()), httpOnly, aCookie.getSecure(), comment
                                 ));
                     }
                 }
             }
 
             long size = 0;
             String mimeType = "";
             String encoding = "";
 
             final HttpEntity contentEntity = httpResponse.getEntity();
             if (contentEntity != null) {
                 size = contentEntity.getContentLength();
 
                 final Header contentType = contentEntity.getContentType();
                 if (contentType != null) {
                     final String contentTypeValue = contentType.getValue();
                     mimeType = contentTypeValue == null ? "" : contentTypeValue;
                 }
 
                 final Header contentEnconding = contentEntity.getContentEncoding();
                 if (contentEnconding != null) {
                     final String contentEncondingValue = contentEnconding.getValue();
                     encoding = contentEncondingValue == null ? "" : contentEncondingValue;
                 }
             }
             final long compression = 0;
             final String text = null;
 
             final HarContent content = new HarContent(size, compression, mimeType, text, encoding, comment);
             final String redirectURL = "";
 
            final HarResponse response = new HarResponse(status, statusText, responseHttpVersion, responseCookies, responseHeaders, content,
                     redirectURL, headersSize, bodySize, comment);
 
             // Har entry is now complete.
             har.addEntry(new HarEntry(pageRef, startedDateTime, time, request, response, cache, timings,
                     serverIPAddress, connection, comment));
         }
     }
 
     public static IWorkspace openWorkspaceByNumber(final String wsNumber) {
         final File ws = getDefaultWorkspace(wsNumber);
         p("Workspace: " + ws);
 
         final IWorkspaceEntry entry = (IWorkspaceEntry) invokeStatic(WorkspaceEntry.class, "createFromPath", ws);
 
         final IModel model = new Model();
         invoke(model, "openWorkspaceEntry", entry);
 
         final IWorkspace openWorkspace = model.getCurrentWorkspace();
 
         if (openWorkspace == null) {
             p("open workspace failed");
             return null;
         }
 
         return openWorkspace;
     }
 
     public static File getDefaultWorkspace(final String wsNumber) {
         final File ws = new File(System.getProperty("user.home"), ".vega" + File.separator + "workspaces"
                 + File.separator + wsNumber);
         return ws;
     }
 }
