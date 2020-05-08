 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 
 package org.webmacro.resource;
 
 import java.lang.*;
 import java.net.*;
 import java.io.*;
 import org.webmacro.util.*;
 import org.webmacro.*;
 
 /**
   * This is the canonical provider for mapping URLs to Handlers. The
   * reactor will request that the "handler" provider return a Handler
   * object when supplied with a URL.
   * <p>
   * You could implement your own version of this class to return 
   * handlers based on whatever criteria you wanted.
   */
 
 final public class UrlProvider extends CachingProvider
 {
 
    static final public long AVG_TIMEOUT = 10000;
    static final public long MAX_TIMEOUT = 60000;
    static final public long MIN_TIMEOUT = 5000;
 
    Broker _broker;
 
    /**
      * We serve up "url" type resources
      */
    final public String getType() { return "url"; }
 
 
    public void init(Broker b, Settings config) throws InitException
    {
       super.init(b,config);
       _broker = b;
    }
 
    /**
      * Load a URL from the specified name and return it. The expire
      * time for the SoftReference will be set to the expire time
      * for the loaded URL.
      * <p>
      * Http expires information will be obeyed between the MIN_TIMEOUT
      * and MAX_TIMEOUT bounds. URLs which do not specify a timeout, 
      * and files from the filesystem, will be cached for AVG_TIMEOUT
      * milliseconds.
      */
    final public Object load(String name, CacheElement ce) 
    throws ResourceException {
 
       try {
          URL u;
          if (name.indexOf(":") < 3) {
             u = new URL("file",null,-1,name); 
          } else {
             u = new URL(name);
          }
 
          URLConnection uc;
          InputStream is;
          try {
            uc = u.openConnection();
            is = uc.getInputStream();
          }
          catch (IOException e) {
            if (name.indexOf(":") < 3) {
              uc = _broker.getResource(name).openConnection();
              is = uc.getInputStream();
            }
            else 
              throw e;
          }
 
          String encoding = uc.getContentEncoding();
          if (encoding == null) {
             encoding = "UTF-8";
          }
          Reader in = new InputStreamReader(
                            new BufferedInputStream(is), encoding);
 
          int length = uc.getContentLength();
          if (length == -1) {
             length = 1024;
          }
 
          long now = System.currentTimeMillis();
          long timeout = uc.getExpiration();
          if (timeout != 0) {
             timeout -= now;
          } else {
             timeout = AVG_TIMEOUT; 
          }
          if (timeout > MAX_TIMEOUT) timeout = MAX_TIMEOUT;   
          else if (timeout < MIN_TIMEOUT) timeout = MIN_TIMEOUT;
 
          char buf[] = new char[1024];
          StringWriter sw = new StringWriter(length);
          int num;
          while ( (num = in.read(buf)) != -1 ) {
             sw.write(buf, 0, num);
          }
          in.close();
          return sw.toString();
       } catch (Exception e) {
          throw new ResourceException(this + " unable to load " + name, e);
       }
    }
 	
 
    /**
     * Utility routine to get the last modification date for a URL.  
     * The standard implementation of .getLastModified() doesn't work
     * for file or jar URLs, so we try to be a bit more clever (running
     * the risk that we'll shoot ourselves in the foot, of course.)
     * Also used by BrokerTemplateProvider; maybe this should go somewhere
     * else? 
     */
    public static long getUrlLastModified(URL u) {
       String protocol = u.getProtocol();
       if (protocol.equals("file")) {
          // We're going to use the File mechanism instead
          File f = new File(u.getFile());
          return f.lastModified();
       }
       else if (protocol.equals("jar")) {
          // We'll extract the jar source and recurse
          String source = u.getFile();
          int lastIndex = source.lastIndexOf("!");
          if (lastIndex > 0) 
            source = source.substring(lastIndex);
          try {
             return getUrlLastModified(new URL(source));
          } 
          catch (MalformedURLException e) {
             return 0;
          }
       }
       else {
          // Ask the URL, maybe it knows
          try {
             URLConnection uc = u.openConnection();
             uc.connect();
             return uc.getLastModified();
          }
          catch (IOException e) { 
             return 0;
          }
       }
    }
 
    /**
     * Utility routine to get the input stream associated with a URL.  
     * It special-cases "file" URLs because this way is faster.  If it
     * doesn't work on some platforms (I could imagine some Windows JVM
     * breaking) then we can back off to the standard implementation.
     * Also used by BrokerTemplateProvider; maybe this should go somewhere
     * else? 
     */
   public static InputStream getUrlInputStream(URL u) throws IOException {
     if (u.getProtocol().equals("file")) {
        InputStream is = new FileInputStream(new File(u.getFile()));
        return is;
     }
     else
        return u.openStream();
   }
   
 }
 
 
