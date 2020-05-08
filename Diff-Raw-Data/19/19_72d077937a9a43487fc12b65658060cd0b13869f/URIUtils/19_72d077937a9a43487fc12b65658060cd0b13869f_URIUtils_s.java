 /*****************************************************************************
  *                The Virtual Light Company Copyright (c) 1999
  *                               Java Source
  *
  * This code is licensed under the GNU Library GPL. Please read license.txt
  * for the full details. A copy of the LGPL may be found at
  *
  * http://www.gnu.org/copyleft/lgpl.html
  *
  * Project:    URI Class libs
  *
  * Version History
  * Date        TR/IWOR  Version  Programmer
  * ----------  -------  -------  ------------------------------------------
  *
  ****************************************************************************/
 
 package org.ietf.uri;
 
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.IOException;
 import java.util.HashMap;
 import java.net.FileNameMap;
 import java.net.MalformedURLException;
 
 /**
  * Utilities for parsing, building and hacking URIs.
  * <P>
  *
  * For details on URIs see the IETF working group:
  * <A HREF="http://www.ietf.org/html.charters/urn-charter.html">URN</A>
  * <P>
  *
  * This softare is released under the
  * <A HREF="http://www.gnu.org/copyleft/lgpl.html">GNU LGPL</A>
  * <P>
  *
  * DISCLAIMER:<BR>
  * This software is the under development, incomplete, and is
  * known to contain bugs. This software is made available for
  * review purposes only. Do not rely on this software for
  * production-quality applications or for mission-critical
  * applications.
  * <P>
  *
  * Portions of the APIs for some new features have not
  * been finalized and APIs may change. Some features are
  * not fully implemented in this release. Use at your own risk.
  * <P>
  *
  * @author  Justin Couch
  * @version 0.7 (27 August 1999)
  */
 public final class URIUtils
   implements URIConstants
 {
 
   /** Hashmap to hold all of the ports to protocol mapping */
   private static HashMap protocol_ports;
 
   /**
    * Static initialiser that is used to create the protocol/port lookup
    * table.
    */
   static
   {
     protocol_ports = new HashMap(30);
 
     protocol_ports.put(HTTP_SCHEME, new Integer(HTTP_PORT));
     protocol_ports.put(SHTTP_SCHEME, new Integer(SHTTP_PORT));
     protocol_ports.put(HTTPS_SCHEME, new Integer(HTTPS_PORT));
     protocol_ports.put(HTTP_NG_SCHEME, new Integer(HTTP_NG_PORT));
     protocol_ports.put(COFFEE_SCHEME, new Integer(COFFEE_PORT));
     protocol_ports.put(FTP_SCHEME, new Integer(FTP_PORT));
     protocol_ports.put(TELNET_SCHEME, new Integer(TELNET_PORT));
     protocol_ports.put(NNTP_SCHEME, new Integer(NNTP_PORT));
     protocol_ports.put(NEWS_SCHEME, new Integer(NEWS_PORT));
     protocol_ports.put(SNEWS_SCHEME, new Integer(SNEWS_PORT));
     protocol_ports.put(HNEWS_SCHEME, new Integer(HNEWS_PORT));
     protocol_ports.put(SMTP_SCHEME, new Integer(SMTP_PORT));
     protocol_ports.put(GOPHER_SCHEME, new Integer(GOPHER_PORT));
     protocol_ports.put(WAIS_SCHEME, new Integer(WAIS_PORT));
     protocol_ports.put(WHOIS_SCHEME, new Integer(WHOIS_PORT));
     protocol_ports.put(WHOIS_PLUS_SCHEME, new Integer(WHOIS_PLUS_PORT));
     protocol_ports.put(RWHOIS_SCHEME, new Integer(RWHOIS_PORT));
     protocol_ports.put(IMAP_SCHEME, new Integer(IMAP_PORT));
     protocol_ports.put(POP_SCHEME, new Integer(POP_PORT));
     protocol_ports.put(PROSPERO_SCHEME, new Integer(PROSPERO_PORT));
     protocol_ports.put(IRC_SCHEME, new Integer(IRC_PORT));
     protocol_ports.put(LDAP_SCHEME, new Integer(LDAP_PORT));
     protocol_ports.put(Z3950R_SCHEME, new Integer(Z3950R_PORT));
     protocol_ports.put(Z3950S_SCHEME, new Integer(Z3950S_PORT));
     protocol_ports.put(VEMMI_SCHEME, new Integer(VEMMI_PORT));
     protocol_ports.put(VIDEOTEX_SCHEME, new Integer(VIDEOTEX_PORT));
     protocol_ports.put(NFS_SCHEME, new Integer(NFS_PORT));
   }
 
   /**
    * Construct a new URI from the string. If the string starts with "urn:" a
    * URN object is constructed, otherwise a URL is constructed. If the
    * syntax is invalid, and exception will be generated.
    *
    * @param uri The String representing the URI to be constructed
    * @return An object representing the string
    * @throws MalformedURLException Could not treat the string as a URL
    * @throws MalformedURNException Could not treat the string as a URN
    */
   public static URI createURI(String uri)
     throws MalformedURLException, MalformedURNException
   {
     URI ret_val = null;
 
     if(uri.startsWith("urn:"))
       ret_val = new URN(uri);
     else
       ret_val = new URL(uri);
 
     return ret_val;
   }
 
   /**
    * Get the scheme from the url string that we've been given. The scheme is
    * determined by the regex <CODE>(([^:/?#]+):)?</CODE>. The scheme may be
    * one of the predefined types that are defined as constants for this class.
    *
    * @param uri The uri as a string
    * @return A string representing the scheme, or null if it can't be found
    */
   public static String getScheme(String uri)
   {
     // we do this the hard way using char arrays rather than using a
     // regex package to reduce core dependencies
     char[] uri_chars = uri.toCharArray();
 
     return getScheme(uri_chars);
   }
 
   /**
    * Character array based version of the getScheme method.
    *
    * @param uri The URI to parse
    * @return The string representing the scheme.
    */
   private static String getScheme(char[] uri)
   {
     int size = uri.length;
     String scheme = null;
     int index = getSchemeCharLength(uri);
 
     if((index > 0) && (index < size)  &&  (uri[index] == ':'))
       scheme = new String(uri, 0, index).toLowerCase();
 
     return scheme;
   }
 
   /**
    * Variant on the getScheme method that tells how long the string is
    * in terms of characters from the start of the array
    *
    * @param uri The characters of the URI
    * @return The number of chars in the length of the scheme part
    */
   private static int getSchemeCharLength(char[] uri)
   {
     int index = 0;
     int size = uri.length;
     String scheme = null;
 
     // quick case insensitive compare to see if the URI starts with either
     // "URL" or "URI"
     if ((uri[3] == ':')  &&
         (uri[0] == 'u'  ||  uri[0] == 'U')  &&
         (uri[1] == 'r'  ||  uri[1] == 'R')  &&
         (uri[2] == 'i'  ||  uri[2] == 'I'  ||
          uri[2] == 'l'  ||  uri[2] == 'L'))
       index = 4;
 
     while((index < size)  &&
           (uri[index] != ':')  &&
           (uri[index] != '/')  &&
           (uri[index] != '?')  &&
           (uri[index] != '#'))
         index++;
 
     return index;
   }
 
   /**
    * Return the default port used by a given protocol.
    *
    * @param protocol the protocol
    * @return the port number, or 0 if unknown
    */
   public static int getDefaultPort(String protocol)
   {
     String prot = protocol.trim().toLowerCase();
 
     int ret_val = 0;
     Integer val = (Integer)protocol_ports.get(prot);
 
     if(val != null)
       ret_val = val.intValue();
 
     return ret_val;
   }
 
   /**
    * Parse the string as a URL and extract the host name from it. If
    * the protocol does not support host names, then it will return
    * <CODE>null</CODE>. The string returned contains both the host name
    * and port from the URL in two strings. The first string from the
    * return value is the host name and the second is the port number as
    * a string.
    * <P>
    * If the URL does not explicitly set the port number then this will attempt
    * to fetch the port number by calling getDefaultPort() and returning a
    * string representation of that.
    *
    * @param url The string as a url
    * @return The hostname or null if not defined for the URL type
    * @exception MalformedURLException Invalid URL passed
    */
   public static String[] getHostAndPortFromUrl(String url)
     throws MalformedURLException
   {
     // first fetch the scheme. If we know that the scheme is one that can
     // support hostnames, we'll keep looking otherwise return straight away.
     int size = url.length();
     char[] url_chars = url.toCharArray();
 
     // now, from the scheme onwards look for a host name
     String scheme = getScheme(url_chars);
 
     // if the scheme has a default port number, then it must also have a
     // host available as part of the URL. If it doesn't exist in the hashmap
     // then assume it doesn't and barf.
     if(protocol_ports.get(scheme) == null)
       return null;
 
     // now parse the URL. Must rememeber that a URL can also contain password
     // and username info _before_ the hostname/port combo. Also, we need to
     // strip the scheme and leading ':' and possibly "//" characters too.
     // Start at the sceheme and count until we hit something that is
     // alphanumeric, then start looking at the hostname stuff.
     //
     // Start the index at the int length of the scheme string. Since the String
     // is terminated by a ':' then this should give us already one cahr offset
     // that is needed. All we do is wander through until we reach something
     // that is not '/' to start stripping the authority part.
     int start_pos, end_pos;
     int start_hostname, end_hostname;
     int start_port, end_port;
     int index = scheme.length() + 1;
     String hostname = null;
     String port = null;
     boolean has_userinfo = false;
 
     for( ; (index < size) && (url_chars[index] == '/') ; index++)
       ;
 
     // now look for anything not ? / or # and
     start_pos = index;
 
     // first find the end of the authority part.
     while((index < size) &&
           (url_chars[index] != '/') &&
           (url_chars[index] != '?') &&
           (url_chars[index] != '#'))
     {
       index++;
     }
 
     end_pos = index;
     start_hostname = start_pos;
     end_hostname = end_pos;
 
     // now lets start again with just the authority. Search for @ and :
     for(index = start_pos ;
         (index < end_pos) && (url_chars[index] != '@') ;
         index++)
       ;
 
     if(index != end_pos)
     {
       start_hostname = index + 1;
       has_userinfo = true;
     }
 
     // From this endpoint, now look for a semi colon
     index = has_userinfo ? index + 1 : start_pos;
 
     for( ;
         (index < end_pos) && (url_chars[index] != ':') ;
         index++)
       ;
 
     if((index <= end_pos) || (url_chars[index] == ':'))
       end_hostname = index - 1;
     else
       end_hostname = index;
 
     start_port = index + 1;
 
     if((end_pos == url_chars.length) ||
        (url_chars[end_pos] == '/') ||
        (url_chars[end_pos] == '?') ||
        (url_chars[end_pos] == '#'))
       end_port = end_pos - 1;
     else
       end_port = end_pos;
 
 
     hostname = new String(url_chars, start_hostname, end_hostname - start_hostname + 1);
 
     if(!(start_port >= end_pos))
     {
       port = new String(url_chars, start_port, end_port - start_port + 1);
     }
     else
       port = Integer.toString(getDefaultPort(scheme));
 
     String[] ret_val = { hostname, port};
 
     return ret_val;
   }
 
   /**
    * Strip a URL so that all we have is everything that comprises the path
    * and later parts of the URL. We remove the scheme and authority parts
    * and return the rest
    *
    * @param url The original chars
    * @return index into the array of the remaining end of the authority.
    */
   private static int getEndOfAuthorityChar(char[] url)
   {
     int i;
     int size = url.length;
     int index = getSchemeCharLength(url) + 1; // +1 for the ':' delimiter
 
     // for generic URLs, we get <scheme>://<authority> but this may not always
     // happen. For example, on unix we can get "file:/usr/..." with only a
     // single '/' so we want to strip one or two consecutive, but definitely
     // not 3 because that third one is the delimiter on a Win32
     // "file:///c:/..." based URL and helps identify the start of the
     // authority.
 
     boolean has_authority = false;
 
     if((index != url.length) && (url[index] == '/') && (url[index + 1] == '/'))
     {
       has_authority = true;
       index += 2;
     }
 
     if(has_authority)
     {
       // now just trundle along until we hit '/', '?' or '#'.
       while((index < size) &&
             (url[index] != '/') &&
             (url[index] != '?') &&
             (url[index] != '#'))
         index++;
     }
 
     return index;
   }
 
   /**
    * Fetch the path component of the URI. The path does not include any query
    * parts (started with a '?') or references (start with '#');
    * <P>
    * For URLs we start by stripping the scheme and authority part from the
    * representation and work from that baseline.
    * <P>
    * For URNs we start with the Namespace Specific String and extract the
    * path from that.
    *
    * @param uri The URI to fetch the path from
    * @return The path component or null if none specified.
    */
   public static String getPath(URI uri)
   {
     String ret_val = null;
 
     if(uri instanceof URN)
     {
       char[] raw_path = ((URN)uri).getNSS().toCharArray();
 
       ret_val = getPath(raw_path);
     }
     else
     {
       try
       {
         ret_val = getPathFromUrlString(uri.toExternalForm());
       }
       catch(MalformedURLException mue)
       {
         // this should never ever happen!
       }
     }
 
     return ret_val;
   }
 
   /**
    * Fetch the path component of the URI. The path does not include any query
    * parts (started with a '?') or references (start with '#');
    * <P>
    * Assumes that the string represents a full path minus the first character
    * - which must be a '/'. For example, if you started with a URL of
    * <CODE>http://www.ietf.org?image=yes</CODE> and passed the character string
    * of <CODE>image=yes</CODE> that is what would be returned as the path by
    * this method.
    *
    * @param uri The URI to deal with
    * @return The path component of the URI
    */
   private static String getPath(char[] uri)
   {
     int size = uri.length;
     int index = 0;
     int path_start, path_end;
 
     // now look for the first occurance of ? or # and terminate there
     while((index < size) &&
           (uri[index] != '?') &&
           (uri[index] != '#'))
       index++;
 
     String ret_val = new String(uri, 0, index);
 
     return ret_val;
   }
 
   /**
    * Fetch the path component of the URI. The path does not include any query
    * parts (started with a '?') or references (start with '#');
    * <P>
    * For a URL, we separate everything following the host/port part. For a urn,
    * we take the namespace specific string and start striping from that point.
    *
    * @param uri The URI to deal with
    * @return The path component of the URI
    * @exception MalformedURLException Invalid URL passed
    */
   public static String getPathFromUrlString(String url)
     throws MalformedURLException
   {
     char[] raw_path = url.toCharArray();
 
     int start_pos = getEndOfAuthorityChar(raw_path);
 
     int length = raw_path.length - start_pos;
     char[] real_path = new char[length];
 
     System.arraycopy(raw_path, start_pos, real_path, 0, length);
 
     return getPath(real_path);
   }
 
   /**
    * Get the query part from a URI. The query starts from the first '?'
    * encountered and terminates at either the end of the string or at the
    * first instance of '?'. If there is no query part to this URI then
    * null is returned. It does not return the leading '?'.
    *
    * @param uri The URI to strip the information from or null if none
    */
   public static String getQuery(URI uri)
   {
     String ret_val = null;
 
     if(uri instanceof URN)
     {
       char[] raw_path = ((URN)uri).getNSS().toCharArray();
 
       ret_val = getQuery(raw_path);
     }
     else
     {
       try
       {
         ret_val = getQueryFromUrlString(uri.toExternalForm());
       }
       catch(MalformedURLException mue)
       {
         // this should never ever happen!
       }
     }
 
     return ret_val;
   }
 
   /**
    * Get the query part from a URI. The query starts from the first '?'
    * encountered and terminates at either the end of the string or at the
    * first instance of '?'. If there is no query part to this URI then
    * null is returned. It does not return the leading '?'.
    *
    * @param uri The URI to strip the information from or null if none
    */
   private static String getQuery(char[] path)
   {
     int index;
     int size;
     int start_pos = 0;
 
     // from the start of the path, keep working until we hit a '?' or '#'
     index = start_pos;
     size = path.length;
 
     while((index < size) &&
           (path[index] != '?') &&
           (path[index] != '#'))
       index++;
 
     // if we've hit the end of the string or a # then obviously we didn't get
     // a query so just return null right now.
     if((index == size) || (path[index] == '#'))
       return null;
 
     start_pos = index + 1;
 
     while((index < size) && (path[index] != '#'))
       index++;
 
     String ret_val = new String(path, start_pos, index - start_pos);
 
     return ret_val;
   }
 
   /**
    * Get the query portion from a URL String.
    *
    * @exception MalformedURLException The string was stuffed.
    */
   public static String getQueryFromUrlString(String url)
     throws MalformedURLException
   {
     char[] raw_path = url.toCharArray();
 
     int start_pos = getEndOfAuthorityChar(raw_path);
 
     int length = raw_path.length - start_pos;
     char[] real_path = new char[length];
 
     System.arraycopy(raw_path, start_pos, real_path, 0, length);
 
     return getQuery(real_path);
   }
 
   /**
    * Strip the path, query and reference parts from the file string. It assumes
    * that the file path starts the string (there are no other parts of the
    * url/urn before that.
    * <P>
    * The file is stripped so that value 0 is the path portion (everything up
    * to the first '?'. Index 1 is the query (everything between '?' and '#'
    * and index 3 is everything after '#'. If any of these parts are not
    * specified, then that index is <CODE>null</CODE>
    *
    * @param file The string representing the file portion of a URL or the
    *   NSS part of a URN.
    * @return An array, length 3, of the stripped parts.
    */
   public static String[] stripFile(String file)
   {
     String[] ret_val = new String[3];
 
     int end_path = file.indexOf('?');
 
     if(end_path == -1)
     {
      ret_val[0] = file;
      ret_val[1] = null;
      ret_val[2] = null;
     }
     else
     {
       ret_val[0] = file.substring(0, end_path);
 
       int end_query = file.indexOf('#', end_path + 1);
 
       if(end_query == -1)
       {
         ret_val[1] = file.substring(end_path + 1);
         ret_val[2] = null;
       }
       else
       {
         ret_val[1] = file.substring(end_path + 1, end_query);
         ret_val[2] = file.substring(end_query + 1);
       }
     }
 
     return ret_val;
   }
 
 /********
   public static void main(String[] args)
   {
     String [] test_urls = {
       "http://www.vlc.com.au/something",
       "http://www.vlc.com.au/something?query=another+thisthing",
       "http://www.vlc.com.au/something?query=another+thisthing",
       "http://www.vlc.com.au/something?query=another+thisthing#ref",
       "http://www.vlc.com.au?query",
       "http://www.vlc.com.au?query=another+thisthing",
       "http://www.vlc.com.au?query=another+thisthing#ref",
       "http://www.vlc.com.au?query#ref",
       "http://www.vlc.com.au#ref",
       "http://www.vlc.com.au/",
       "http://www.vlc.com.au",
       "http://www.vlc.com.au:8080/",
       "http://www.vlc.com.au:8080",
       "http://justin@www.vlc.com.au:8080/",
       "http://justin@www.vlc.com.au:8080",
       "http://justin:password@www.vlc.com.au:8080/",
       "http://justin:password@www.vlc.com.au:8080",
       "http://justin:password@www.vlc.com.au/",
       "http://justin:password@www.vlc.com.au",
       "http://justin@www.vlc.com.au",
       "file:///c|/something/blah.txt",
       "file:/c|/something/blah.txt"
     };
 
     String[] test_urns = {
       "urn:vrml:eai:/test"
     };
 
     URN urn;
     URL url;
 
     // basic test to check scheme and port numbers
     System.out.println("Get scheme " + getScheme(test_urls[0]));
     System.out.println("Get port " + getDefaultPort(getScheme(test_urls[0])));
 
     // more advanced test to extract host and port from a variety of URLs
     System.out.println("\nHost and Port Test for URLs");
 
     for(int i = 0; i < test_urls.length; i++)
     {
       try
       {
         String[] test_res = getHostAndPortFromUrl(test_urls[i]);
 
         if(test_res == null)
           System.out.println("The URL does not contain a host");
         else
         {
           System.out.println("host: " + test_res[0] +
                              " on port " + test_res[1]);
           System.out.println();
         }
       }
       catch(MalformedURLException mue)
       {
       }
     }
 
     // Find the path component from a URL
     System.out.println("\nPath Component Test for URLs");
     for(int i = 0; i < test_urls.length; i++)
     {
       try
       {
         System.out.println("testing URL " + test_urls[i]);
         System.out.println("  Direct call " + getPathFromUrlString(test_urls[i]));
 
         url = new URL(test_urls[i]);
         System.out.println("  Path using URL is " + getPath(url));
         System.out.println("  java.net version: " +
                            new java.net.URL(test_urls[i]).getFile());
       }
       catch(MalformedURLException mue)
       {
         System.out.println("Dropped url " + i + " " + test_urls[i]);
       }
     }
 
     // find the path component of a URN
     System.out.println("\nPath Component Test for URNs");
 
     for(int i = 0; i < test_urns.length; i++)
     {
       try
       {
         urn = new URN(test_urns[i]);
         System.out.println("Path is " + getPath(urn));
       }
       catch(MalformedURNException mue)
       {
         System.out.println("Dropped urn " + i + " " + test_urns[i]);
       }
     }
 
     // Get everything in a URL from the path to the end of the string.
     System.out.println("\nFull item test for URLs");
     for(int i = 0; i < test_urls.length; i++)
     {
       char[] url_chars = test_urls[i].toCharArray();
       int pos = getEndOfAuthorityChar(url_chars);
       String url_str = new String(url_chars, pos, url_chars.length - pos);
       System.out.println("Full Path is " + url_str);
 
       try
       {
         System.out.println("Query is " + getQueryFromUrlString(test_urls[i]));
         System.out.println();
       }
       catch(MalformedURLException mue)
       {
         System.out.println("Dropped url " + i + " " + test_urls[i]);
       }
     }
 
   }
 */
 }
