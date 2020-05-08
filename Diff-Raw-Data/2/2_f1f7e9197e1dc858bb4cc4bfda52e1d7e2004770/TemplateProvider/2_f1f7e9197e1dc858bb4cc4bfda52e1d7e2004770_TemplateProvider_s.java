 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.resource;
 import  org.webmacro.*;
 import  org.webmacro.engine.FileTemplate;
 import  org.webmacro.broker.*;
 import  org.webmacro.util.*;
 import  java.util.*;
 import  java.io.*;
 
 /**
   * This is the reference implementation for the "template" ResourceProvider.
   * Create a TemplateProvider with a directory name and it will search for 
   * and return templates from that directory when requested to do so. 
   * <p>
   * The "template" type will automatically be used by Handler when it 
   * attempts to resolve a template name, via the ResourceBroker. You could
   * install a different TemplateHandler if you wanted to load your templates
   * from a different location (out of a database, over the network, etc.)
   * <p>
   * It supports only the requestResource method, and does not support the 
   * creation or deletion of templates. 
   * <p>
   * @see ResourceProvider
   * @see Handler
   */
 final public class TemplateProvider implements ResourceProvider
 {
 
    // INITIALIZATION
 
    private static String pathSeparator_ = ";";
    private String templateDirectory_[] = null;
    private static ResourceBroker _broker = null;
 
    static {
       try {
          pathSeparator_ = System.getProperty("path.separator");
      } catch (Exception e) {
          // do nothing
       }
    }
 
    private int cacheDuration = 10 * 60 * 1000; // default 10min
 
    /**
      * For use by other classes when referring to the single type served
      * by this provider.
      */
    final public static String TYPE = "template";
 
    /**
      * Create a new TemplateProvider that uses the specified directory
      * as the source for Template objects that it will return
      * @exception ResourceInitException provider failed to initialize
      */
    public void init(final ResourceBroker broker)
       throws ResourceInitException
    {
 
       _broker = broker;
 
       try {
 
          try {
             String cacheStr = (String) 
                  broker.getValue(Config.TYPE, Config.TEMPLATE_CACHE);
             cacheDuration = Integer.valueOf(cacheStr).intValue();
          } catch (Exception ee) {
             // use default
          }
 
          String templatePath = 
             (String) broker.getValue(Config.TYPE,Config.TEMPLATE_DIR);
          StringTokenizer st = new StringTokenizer(templatePath, pathSeparator_);
          if (_debug) {
             _log.debug("template path = " + templatePath);
          }
          templateDirectory_ = new String[ st.countTokens() ];
          int i;
          for (i=0; i < templateDirectory_.length; i++) 
          {
             String dir = st.nextToken(); 
             if (_debug) {
                _log.debug("template dir = " + dir);
             }
             templateDirectory_[i] = dir;
          }
 
 
       } catch(Exception e) {
          _log.exception(e);
          throw new ResourceInitException("Could not initialize");
       }
    }
 
    /**
      * Enable/disable debugging statements
      */
    static private final boolean _debug = false && Log.debug;
 
    /**
      * Where we write our log messages 
      */
    static public final Log _log = 
       new Log(TYPE,"Template storage resource");
 
 
    // RESOURCE PROVIDER API
 
 
    /**
      * This implementation only supports the "template" type
      */
    final private static String _types[] = { TYPE };
 
    /**
      * Supports the "template" type
      */
    final public String[] getTypes() {
       return _types;
    }
 
    /**
      * Let cache expire after 10 minutes
      */
    final public int resourceExpireTime() {
       return cacheDuration;
    }
 
    /**
      * Allow a worker thread to process this class concurrently
      */
    final public int resourceThreads() {
       return 1; // some concurrency is valuable, allow 1 worker thread
    }
 
    /**
      * Grab a template based on its name, setting the request event to 
      * contain it if we found it.
      * @exception ResourceNotFoundException resource not found (authoritative)
      * @exception InterruptedException work being done is no longer wanted
      * @param request has type "template" and name equal to the template sought
      */
    final public void resourceRequest(RequestResourceEvent request)
       throws NotFoundException, InterruptedException
    {
       Template t = get(request.getName());
       if (t == null) {
          return; // maybe someone else has it
       }
       try {
          request.set(t); // this makes it available and means we handled it
       } catch (Exception e) {
          return; // do nothing
       }
    }
 
    /**
      * Unsupported. Does nothing.
      */
    final public void resourceCreate(CreateResourceEvent evt) {
       // operation unsupported (doing nothing means that)
    }
 
    /**
      * Unsupported. Does nothing.
      */
    final public boolean resourceSave(ResourceEvent save) {
       // operation unsupported (doing nothing means that)
       return false;
    }
 
    /**
      * Unsupported. Does nothing.
      * @return false
      */
    final public boolean resourceDelete(ResourceEvent evt) {
       return false; // operation unsupported
    }
 
    /**
      * We don't really have anything to do on shutdown
      */
    final public void destroy()
    {
       // do nothing
    }
 
 
    // IMPLEMENTATION
 
    /**
      * Find the specified template in the directory managed by this 
      * template store. Any path specified in the filename is relative
      * to the directory managed by the template store. 
      * <p>
      * @param fileName relative to the current directory fo the store
      * @returns a template matching that name, or null if one cannot be found
      */
    final public Template get(String fileName) {
       for (int i=0; i < templateDirectory_.length; i++) {
          Template t;
          String dir = templateDirectory_[i];
          if (_debug) {
             _log.debug("TemplateProvider: searching directory " + dir);
          }
          File tFile  = new File(dir,fileName);
          if (tFile.canRead()) {
             try {
                if (_debug) {
                   _log.debug("TemplateProvider: loading " + tFile);
                }
                t = new FileTemplate(_broker,tFile);
                t.parse();
                return t;
             } catch (Exception e) {
                _log.exception(e);
                _log.warning("TemplateProvider: Could not load template: " 
                      + tFile);
             } 
             if (_debug) {
                _log.debug("TemplateProvider: " + fileName + " not found.");
             }
          }
       }
       return null;
    }
 
    /**
      * Print out the name of this TemplateProvider, including its directory
      */
    final public String toString() {
       return "TemplateProvider(" + getPath() + ")";
    }
 
    /**
      * Get the search path used by this template provider 
      */
    final public String getPath() {
       StringBuffer sb = new StringBuffer(200);
       for (int i=0; i < templateDirectory_.length; i++) {
          if (i != 0)  {
             sb.append(pathSeparator_);
          }
          sb.append(templateDirectory_[i]);
       }
       return sb.toString();
    }
 
 }
 
 
 final class TemplateFilter implements java.io.FilenameFilter
 {
    public final boolean accept(File dir, String name) {
       return name.endsWith(".wm");
    }
 }
 
 
