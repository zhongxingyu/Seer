 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.util;
 
 import java.net.URI;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * A set of utility methods and constants for the HTTP protocol.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public interface HttpUtil {
 
     /**
      * The HTTP methods as defined in RFC1945 (HTTP/1.0), RFC2616
      * (HTTP/1.1), and RFC4918 (WebDAV).
      */
     public static interface METHOD {
 
         /** <tt>OPTIONS</tt> (HTTP/1.1 - RFC 2616) */
         public static final String OPTIONS = "OPTIONS";
 
         /** <tt>GET</tt> (HTTP/1.1 - RFC 2616) */
         public static final String GET = "GET";
 
         /** <tt>HEAD</tt> (HTTP/1.1 - RFC 2616) */
         public static final String HEAD = "HEAD";
 
         /** <tt>POST</tt> (HTTP/1.1 - RFC 2616) */
         public static final String POST = "POST";
 
         /** <tt>PUT</tt> (HTTP/1.1 - RFC 2616) */
         public static final String PUT = "PUT";
 
         /** <tt>DELETE</tt> (HTTP/1.1 - RFC 2616) */
         public static final String DELETE = "DELETE";
 
         /** <tt>TRACE</tt> (HTTP/1.1 - RFC 2616) */
         public static final String TRACE = "TRACE";
 
         /** <tt>CONNECT</tt> (HTTP/1.1 - RFC 2616) */
         public static final String CONNECT = "CONNECT";
 
         /** <tt>PROPFIND</tt> (WebDAV - RFC 4918) */
         public static final String PROPFIND = "PROPFIND";
 
         /** <tt>PROPPATCH</tt> (WebDAV - RFC 4918) */
         public static final String PROPPATCH = "PROPPATCH";
 
         /** <tt>MKCOL</tt> (WebDAV - RFC 4918) */
         public static final String MKCOL = "MKCOL";
 
         /** <tt>COPY</tt> (WebDAV - RFC 4918) */
         public static final String COPY = "COPY";
 
         /** <tt>MOVE</tt> (WebDAV - RFC 4918) */
         public static final String MOVE = "MOVE";
 
         /** <tt>LOCK</tt> (WebDAV - RFC 4918) */
         public static final String LOCK = "LOCK";
 
         /** <tt>UNLOCK</tt> (WebDAV - RFC 4918) */
         public static final String UNLOCK = "UNLOCK";
     }
 
 
     /**
      * The HTTP status codes as defined in RFC1945 (HTTP/1.0), RFC2616
      * (HTTP/1.1), and RFC4918 (WebDAV).
      */
     public static abstract class STATUS {
 
         // 1xx Informational
 
         /** <tt>100 Continue</tt> (HTTP/1.1 - RFC 2616) */
         public static final int CONTINUE = 100;
         /** <tt>101 Switching Protocols</tt> (HTTP/1.1 - RFC 2616)*/
         public static final int SWITCHING_PROTOCOLS = 101;
 
         // 2xx Successful
 
         /** <tt>200 OK</tt> (HTTP/1.0 - RFC 1945) */
         public static final int OK = 200;
         /** <tt>201 Created</tt> (HTTP/1.0 - RFC 1945) */
         public static final int CREATED = 201;
         /** <tt>202 Accepted</tt> (HTTP/1.0 - RFC 1945) */
         public static final int ACCEPTED = 202;
         /** <tt>203 Non Authoritative Information</tt> (HTTP/1.1 - RFC 2616) */
         public static final int NON_AUTHORITATIVE_INFORMATION = 203;
         /** <tt>204 No Content</tt> (HTTP/1.0 - RFC 1945) */
         public static final int NO_CONTENT = 204;
         /** <tt>205 Reset Content</tt> (HTTP/1.1 - RFC 2616) */
         public static final int RESET_CONTENT = 205;
         /** <tt>206 Partial Content</tt> (HTTP/1.1 - RFC 2616) */
         public static final int PARTIAL_CONTENT = 206;
         /** <tt>207 Multi-Status</tt> (WebDAV - RFC 4918) */
         public static final int MULTI_STATUS = 207;
 
         // 3xx Redirection
 
        /** <tt>300 Multiple Choices</tt> (HTTP/1.1 - RFC 2616) */
         public static final int MULTIPLE_CHOICES = 300;
         /** <tt>301 Moved Permanently</tt> (HTTP/1.0 - RFC 1945) */
         public static final int MOVED_PERMANENTLY = 301;
         /** <tt>302 Found</tt> (HTTP/1.1 - RFC 2616) */
         public static final int FOUND = 302;
         /** <tt>303 See Other</tt> (HTTP/1.1 - RFC 2616) */
         public static final int SEE_OTHER = 303;
         /** <tt>304 Not Modified</tt> (HTTP/1.0 - RFC 1945) */
         public static final int NOT_MODIFIED = 304;
         /** <tt>305 Use Proxy</tt> (HTTP/1.1 - RFC 2616) */
         public static final int USE_PROXY = 305;
         /** <tt>307 Temporary Redirect</tt> (HTTP/1.1 - RFC 2616) */
         public static final int TEMPORARY_REDIRECT = 307;
 
         // 4xx Client Error
 
         /** <tt>400 Bad Request</tt> (HTTP/1.1 - RFC 2616) */
         public static final int BAD_REQUEST = 400;
         /** <tt>401 Unauthorized</tt> (HTTP/1.0 - RFC 1945) */
         public static final int UNAUTHORIZED = 401;
         /** <tt>402 Payment Required</tt> (HTTP/1.1 - RFC 2616) */
         public static final int PAYMENT_REQUIRED = 402;
         /** <tt>403 Forbidden</tt> (HTTP/1.0 - RFC 1945) */
         public static final int FORBIDDEN = 403;
         /** <tt>404 Not Found</tt> (HTTP/1.0 - RFC 1945) */
         public static final int NOT_FOUND = 404;
         /** <tt>405 Method Not Allowed</tt> (HTTP/1.1 - RFC 2616) */
         public static final int METHOD_NOT_ALLOWED = 405;
         /** <tt>406 Not Acceptable</tt> (HTTP/1.1 - RFC 2616) */
         public static final int NOT_ACCEPTABLE = 406;
         /** <tt>407 Proxy Authentication Required</tt> (HTTP/1.1 - RFC 2616)*/
         public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
         /** <tt>408 Request Timeout</tt> (HTTP/1.1 - RFC 2616) */
         public static final int REQUEST_TIMEOUT = 408;
         /** <tt>409 Conflict</tt> (HTTP/1.1 - RFC 2616) */
         public static final int CONFLICT = 409;
         /** <tt>410 Gone</tt> (HTTP/1.1 - RFC 2616) */
         public static final int GONE = 410;
         /** <tt>411 Length Required</tt> (HTTP/1.1 - RFC 2616) */
         public static final int LENGTH_REQUIRED = 411;
         /** <tt>412 Precondition Failed</tt> (HTTP/1.1 - RFC 2616) */
         public static final int PRECONDITION_FAILED = 412;
         /** <tt>413 Request Entity Too Large</tt> (HTTP/1.1 - RFC 2616) */
         public static final int REQUEST_ENTITY_TOO_LARGE = 413;
         /** <tt>414 Request-URI Too Long</tt> (HTTP/1.1 - RFC 2616) */
         public static final int REQUEST_URI_TOO_LONG = 414;
         /** <tt>415 Unsupported Media Type</tt> (HTTP/1.1 - RFC 2616) */
         public static final int UNSUPPORTED_MEDIA_TYPE = 415;
         /** <tt>416 Requested Range Not Satisfiable</tt> (HTTP/1.1 - RFC 2616) */
         public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
         /** <tt>417 Expectation Failed</tt> (HTTP/1.1 - RFC 2616) */
         public static final int EXPECTATION_FAILED = 417;
         /** <tt>422 Unprocessable Entity</tt> (WebDAV - RFC 4918) */
         public static final int UNPROCESSABLE_ENTITY = 422;
         /** <tt>423 Locked</tt> (WebDAV - RFC 4918) */
         public static final int LOCKED = 423;
         /** <tt>424 Failed Dependency</tt> (WebDAV - RFC 4918) */
         public static final int FAILED_DEPENDENCY = 424;
 
         // 5xx Server Error
 
         /** <tt>500 Server Error</tt> (HTTP/1.0 - RFC 1945) */
         public static final int INTERNAL_SERVER_ERROR = 500;
         /** <tt>501 Not Implemented</tt> (HTTP/1.0 - RFC 1945) */
         public static final int NOT_IMPLEMENTED = 501;
         /** <tt>502 Bad Gateway</tt> (HTTP/1.0 - RFC 1945) */
         public static final int BAD_GATEWAY = 502;
         /** <tt>503 Service Unavailable</tt> (HTTP/1.0 - RFC 1945) */
         public static final int SERVICE_UNAVAILABLE = 503;
         /** <tt>504 Gateway Timeout</tt> (HTTP/1.1 - RFC 2616) */
         public static final int GATEWAY_TIMEOUT = 504;
         /** <tt>505 HTTP Version Not Supported</tt> (HTTP/1.1 - RFC 2616) */
         public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
         /** <tt>507 Insufficient Storage</tt> (WebDAV - RFC 4918) */
         public static final int INSUFFICIENT_STORAGE = 507;
 
         /**
          * Returns the HTTP status text corresponding to a status code.
          *
          * @param status         the HTTP status code
          *
          * @return the corresponding HTTP status text, or
          *         "Unknown" if not recognized
          */
         public static String asText(int status) {
             switch (status) {
             case STATUS.CONTINUE:
                 return "Continue";
             case STATUS.SWITCHING_PROTOCOLS:
                 return "Switching Protocols";
             case STATUS.OK:
                 return "OK";
             case STATUS.CREATED:
                 return "Created";
             case STATUS.ACCEPTED:
                 return "Accepted";
             case STATUS.NON_AUTHORITATIVE_INFORMATION:
                 return "Non-Authoritative Information";
             case STATUS.NO_CONTENT:
                 return "No Content";
             case STATUS.RESET_CONTENT:
                 return "Reset Content";
             case STATUS.PARTIAL_CONTENT:
                 return "Partial Content";
             case STATUS.MULTI_STATUS:
                 return "Multi-Status";
             case STATUS.MULTIPLE_CHOICES:
                 return "Multiple Choices";
             case STATUS.MOVED_PERMANENTLY:
                 return "Moved Permanently";
             case STATUS.FOUND:
                 return "Found";
             case STATUS.SEE_OTHER:
                 return "See Other";
             case STATUS.NOT_MODIFIED:
                 return "Not Modified";
             case STATUS.USE_PROXY:
                 return "Use Proxy";
             case STATUS.TEMPORARY_REDIRECT:
                 return "Temporary Redirect";
             case STATUS.BAD_REQUEST:
                 return "Bad Request";
             case STATUS.UNAUTHORIZED:
                 return "Unauthorized";
             case STATUS.PAYMENT_REQUIRED:
                 return "Payment Required";
             case STATUS.FORBIDDEN:
                 return "Forbidden";
             case STATUS.NOT_FOUND:
                 return "Not Found";
             case STATUS.METHOD_NOT_ALLOWED:
                 return "Method Not Allowed";
             case STATUS.NOT_ACCEPTABLE:
                 return "Not Acceptable";
             case STATUS.PROXY_AUTHENTICATION_REQUIRED:
                 return "Proxy Authentication Required";
             case STATUS.REQUEST_TIMEOUT:
                 return "Request Timeout";
             case STATUS.CONFLICT:
                 return "Conflict";
             case STATUS.GONE:
                 return "Gone";
             case STATUS.LENGTH_REQUIRED:
                 return "Length Required";
             case STATUS.PRECONDITION_FAILED:
                 return "Precondition Failed";
             case STATUS.REQUEST_ENTITY_TOO_LARGE:
                 return "Request Entity Too Large";
             case STATUS.REQUEST_URI_TOO_LONG:
                 return "Request-URI Too Long";
             case STATUS.UNSUPPORTED_MEDIA_TYPE:
                 return "Unsupported Media Type";
             case STATUS.REQUESTED_RANGE_NOT_SATISFIABLE:
                 return "Requested Range Not Satisfiable";
             case STATUS.EXPECTATION_FAILED:
                 return "Expectation Failed";
             case STATUS.UNPROCESSABLE_ENTITY:
                 return "Unprocessable Entity";
             case STATUS.LOCKED:
                 return "Locked";
             case STATUS.FAILED_DEPENDENCY:
                 return "Failed Dependency";
             case STATUS.INTERNAL_SERVER_ERROR:
                 return "Internal Server Error";
             case STATUS.NOT_IMPLEMENTED:
                 return "Not Implemented";
             case STATUS.BAD_GATEWAY:
                 return "Bad Gateway";
             case STATUS.SERVICE_UNAVAILABLE:
                 return "Service Unavailable";
             case STATUS.GATEWAY_TIMEOUT:
                 return "Gateway Timeout";
             case STATUS.HTTP_VERSION_NOT_SUPPORTED:
                 return "HTTP Version Not Supported";
             case STATUS.INSUFFICIENT_STORAGE:
                 return "Insufficient Storage";
             default:
                 return "Unknown";
             }
         }
     }
 
 
     /**
      * A number of standard HTTP headers as defined in RFC1945
      * (HTTP/1.0), RFC2616 (HTTP/1.1), and RFC4918 (WebDAV).
      */
     public static interface HEADER {
 
         /** RFC 2616 (HTTP/1.1) Section 14.1 */
         public static final String ACCEPT = "Accept";
 
         /** RFC 2616 (HTTP/1.1) Section 14.2 */
         public static final String ACCEPT_CHARSET = "Accept-Charset";
 
         /** RFC 2616 (HTTP/1.1) Section 14.3 */
         public static final String ACCEPT_ENCODING = "Accept-Encoding";
 
         /** RFC 2616 (HTTP/1.1) Section 14.4 */
         public static final String ACCEPT_LANGUAGE = "Accept-Language";
 
         /** RFC 2616 (HTTP/1.1) Section 14.5 */
         public static final String ACCEPT_RANGES = "Accept-Ranges";
 
         /** RFC 2616 (HTTP/1.1) Section 14.6 */
         public static final String AGE = "Age";
 
         /** RFC 1945 (HTTP/1.0) Section 10.1, RFC 2616 (HTTP/1.1) Section 14.7 */
         public static final String ALLOW = "Allow";
 
         /** RFC 1945 (HTTP/1.0) Section 10.2, RFC 2616 (HTTP/1.1) Section 14.8 */
         public static final String AUTHORIZATION = "Authorization";
 
         /** RFC 2616 (HTTP/1.1) Section 14.9 */
         public static final String CACHE_CONTROL = "Cache-Control";
 
         /** RFC 2616 (HTTP/1.1) Section 14.10 */
         public static final String CONNECTION = "Connection";
 
         /** RFC 1945 (HTTP/1.0) Section 10.3, RFC 2616 (HTTP/1.1) Section 14.11 */
         public static final String CONTENT_ENCODING = "Content-Encoding";
 
         /** RFC 2616 (HTTP/1.1) Section 14.12 */
         public static final String CONTENT_LANGUAGE = "Content-Language";
 
         /** RFC 1945 (HTTP/1.0) Section 10.4, RFC 2616 (HTTP/1.1) Section 14.13 */
         public static final String CONTENT_LENGTH = "Content-Length";
 
         /** RFC 2616 (HTTP/1.1) Section 14.14 */
         public static final String CONTENT_LOCATION = "Content-Location";
 
         /** RFC 2616 (HTTP/1.1) Section 14.15 */
         public static final String CONTENT_MD5 = "Content-MD5";
 
         /** RFC 2616 (HTTP/1.1) Section 14.16 */
         public static final String CONTENT_RANGE = "Content-Range";
 
         /** RFC 1945 (HTTP/1.0) Section 10.5, RFC 2616 (HTTP/1.1) Section 14.17 */
         public static final String CONTENT_TYPE = "Content-Type";
 
         /** RFC 1945 (HTTP/1.0) Section 10.6, RFC 2616 (HTTP/1.1) Section 14.18 */
         public static final String DATE = "Date";
 
         /** RFC 4918 (WebDAV) Section 10.1 */
         public static final String DAV = "DAV";
 
         /** RFC 4918 (WebDAV) Section 10.2 */
         public static final String DEPTH = "Depth";
 
         /** RFC 4918 (WebDAV) Section 10.3 */
         public static final String DESTINATION = "Destination";
 
         /** RFC 2616 (HTTP/1.1) Section 14.19 */
         public static final String ETAG = "ETag";
 
         /** RFC 2616 (HTTP/1.1) Section 14.20 */
         public static final String EXPECT = "Expect";
 
         /** RFC 1945 (HTTP/1.0) Section 10.7, RFC 2616 (HTTP/1.1) Section 14.21 */
         public static final String EXPIRES = "Expires";
 
         /** RFC 1945 (HTTP/1.0) Section 10.8, RFC 2616 (HTTP/1.1) Section 14.22 */
         public static final String FROM = "From";
 
         /** RFC 2616 (HTTP/1.1) Section 14.23 */
         public static final String HOST = "Host";
 
         /** RFC 4918 (WebDAV) Section 10.4 */
         public static final String IF = "If";
 
         /** RFC 2616 (HTTP/1.1) Section 14.24 */
         public static final String IF_MATCH = "If-Match";
 
         /** RFC 1945 (HTTP/1.0) Section 10.9, RFC 2616 (HTTP/1.1) Section 14.25 */
         public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
 
         /** RFC 2616 (HTTP/1.1) Section 14.26 */
         public static final String IF_NONE_MATCH = "If-None-Match";
 
         /** RFC 2616 (HTTP/1.1) Section 14.27 */
         public static final String IF_RANGE = "If-Range";
 
         /** RFC 2616 (HTTP/1.1) Section 14.28 */
         public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
 
         /** RFC 1945 (HTTP/1.0) Section 10.10, RFC 2616 (HTTP/1.1) Section 14.29 */
         public static final String LAST_MODIFIED = "Last-Modified";
 
         /** RFC 1945 (HTTP/1.0) Section 10.11, RFC 2616 (HTTP/1.1) Section 14.30 */
         public static final String LOCATION = "Location";
 
         /** RFC 4918 (WebDAV) Section 10.5 */
         public static final String LOCK_TOKEN = "Lock-Token";
 
         /** RFC 2616 (HTTP/1.1) Section 14.31 */
         public static final String MAX_FORWARDS = "Max-Forwards";
 
         /** RFC 4918 (WebDAV) Section 10.6 */
         public static final String OVERWRITE = "Overwrite";
 
         /** RFC 1945 (HTTP/1.0) Section 10.12, RFC 2616 (HTTP/1.1) Section 14.32 */
         public static final String PRAGMA = "Pragma";
 
         /** RFC 2616 (HTTP/1.1) Section 14.33 */
         public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
 
         /** RFC 2616 (HTTP/1.1) Section 14.34 */
         public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
 
         /** RFC 2616 (HTTP/1.1) Section 14.35 */
         public static final String RANGE = "Range";
 
         /** RFC 1945 (HTTP/1.0) Section 10.13, RFC 2616 (HTTP/1.1) Section 14.36 */
         public static final String REFERER = "Referer";
 
         /** RFC 2616 (HTTP/1.1) Section 14.37 */
         public static final String RETRY_AFTER = "Retry-After";
 
         /** RFC 1945 (HTTP/1.0) Section 10.14, RFC 2616 (HTTP/1.1) Section 14.38 */
         public static final String SERVER = "Server";
 
         /** RFC 2616 (HTTP/1.1) Section 14.39 */
         public static final String TE = "TE";
 
         /** RFC 4918 (WebDAV) Section 10.7 */
         public static final String TIMEOUT = "Timeout";
 
         /** RFC 2616 (HTTP/1.1) Section 14.40 */
         public static final String TRAILER = "Trailer";
 
         /** RFC 2616 (HTTP/1.1) Section 14.41 */
         public static final String TRANSFER_ENCODING = "Transfer-Encoding";
 
         /** RFC 2616 (HTTP/1.1) Section 14.42 */
         public static final String UPGRADE = "Upgrade";
 
         /** RFC 1945 (HTTP/1.0) Section 10.15, RFC 2616 (HTTP/1.1) Section 14.43 */
         public static final String USER_AGENT = "User-Agent";
 
         /** RFC 2616 (HTTP/1.1) Section 14.44 */
         public static final String VARY = "Vary";
 
         /** RFC 2616 (HTTP/1.1) Section 14.45 */
         public static final String VIA = "Via";
 
         /** RFC 2616 (HTTP/1.1) Section 14.46 */
         public static final String WARNING = "Warning";
 
         /** RFC 1945 (HTTP/1.0) Section 10.16, RFC 2616 (HTTP/1.1) Section 14.47 */
         public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
     }
 
 
     /**
      * Some static utility methods for HTTP.
      */
     public abstract class Helper {
 
         /**
          * Encodes a URL with proper URL encoding.
          *
          * @param href           the URL to encode
          *
          * @return the encoded URL
          */
         public static String encodeUrl(String href) {
             try {
                 if (href.contains(":")) {
                     String scheme = StringUtils.substringBefore(href, ":");
                     String ssp = StringUtils.substringAfter(href, ":");
                     return new URI(scheme, ssp, null).toASCIIString();
                 } else {
                     return new URI(null, href, null).toASCIIString();
                 }
             } catch (Exception e) {
                 return href;
             }
         }
 
         /**
          * Decodes a URL from the URL encoding.
          *
          * @param href           the URL to decode
          *
          * @return the decoded URL
          */
         public static String decodeUrl(String href) {
             StringBuilder  buffer = new StringBuilder();
             URI            uri;
 
             try {
                 uri = new URI(href);
                 if (uri.getScheme() != null) {
                     buffer.append(uri.getScheme());
                     buffer.append("://");
                     buffer.append(uri.getAuthority());
                 }
                 if (uri.getPath() != null) {
                     buffer.append(uri.getPath());
                 }
                 if (uri.getQuery() != null) {
                     buffer.append("?");
                     buffer.append(uri.getQuery());
                 }
                 if (uri.getFragment() != null) {
                     buffer.append("#");
                     buffer.append(uri.getFragment());
                 }
                 return buffer.toString();
             } catch (Exception e) {
                 return href;
             }
         }
     }
 }
