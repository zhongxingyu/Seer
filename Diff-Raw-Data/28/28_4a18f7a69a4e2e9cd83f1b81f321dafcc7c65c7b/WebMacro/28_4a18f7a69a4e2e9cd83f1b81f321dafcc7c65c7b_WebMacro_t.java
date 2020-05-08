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
 
 
 package org.webmacro;
 
 import java.io.*;
 
 /**
  * WebMacro Manager Interface
  *
  * This interface provies root access into the WebMacro system. Use it to
  * obtain references to other WebMacro objects which you might need.
  * <p>
  * Create a new instance of this object in each thread that intends to
  * use WebMacro, and destroy() it when you're done. It probably maintains
  * a static reference count of the number of users of the broker,
  * and automatically shuts down the underlying broker when the last
  * instance is destroyed.
  */
 public interface WebMacro {
 
    /**
     * The current version of WebMacro.  The actual value is
     * inserted by ANT during the "compile" process
     */
    public static final String VERSION = "@VERSION@";
   public static final String BUILD_DATE = "@BUILD_DATE@";
 
    /**
     * Call this method when you are finished with WebMacro. If you don't
     * call this method, the Broker and all of WebMacro's caches may not
     * be properly shut down, potentially resulting in loss of data, and
     * wasted memory. This method is called in the finalizer, but it is
     * best to call it as soon as you know you are done with WebMacro.
     * <p> After a call to destroy() attempts to use this object may yield
     * unpredicatble results.
     */
    public void destroy();
 
    /**
     * This message returns false until you destroy() this object,
     * subsequently it returns true. Do not attempt to use this object
     * after it has been destroyed.
     */
    public boolean isDestroyed();
 
    /**
     * This object is used to access components that have been plugged
     * into WebMacro; it is shared between all instances of this class and
     * its subclasses. It is created when the first instance is initialized,
     * and deleted when the last instance is shut down. If you attempt to
     * access it after the last servlet has been shutdown, it will either
     * be in a shutdown state or else null.
     */
    public Broker getBroker();
 
 
    /**
     * Retrieve a FastWriter from WebMacro's internal pool of FastWriters.
     * A FastWriter is used when writing templates to an output stream.<p>
     *
     * If using a FastWriter directly, <b>always</b> make sure to <code>flush()</code>
     * and <code>close()</code> it when you're finished.  Closing it
     * automatically returns back to the pool for later reuse.
     *
     * @param out The output stream the FastWriter should write to.  Typically
     *            this will be your ServletOutputStream
     * @param enctype the Encoding type to use
     *
     * @throws java.io.UnsupportedEncodingException if the encoding type
     *         specified is not supported by your JVM.
     */
    public FastWriter getFastWriter(OutputStream out, String enctype)
          throws UnsupportedEncodingException;
 
    /**
     * Retrieve a template from the "template" provider. Equivalent to
     * getBroker().get(TemplateProvider.getType(),key)
     * @exception NotFoundException if the template was not found
     * @exception ResourceException if the template could not be loaded
     */
    public Template getTemplate(String key) throws ResourceException;
 
    /**
     * Retrieve the contents of a URL as a String. The only advantage of
     * using this instead of a regular URL object is that the result may
     * be cached for repeated use.
     */
    public String getURL(String url) throws ResourceException;
 
    /**
     * Retrieve configuration information from the "config" provider.
     * Equivalent to getBroker().get(Config.geType(),key)
     * @exception NotFoundException could not locate requested information
     */
    public String getConfig(String key) throws NotFoundException;
 
    /**
     * Create a new Context. You will likely call this method on every
     * request to create the Context you require for template execution.
     * Fill the Context up with the data you wish to display on the
     * template.
     */
    public Context getContext();
 
    /**
     * Get a log to write information to. The log messages will
     * be output to one or more pre-configured log files. The
     * type you specify will be printed in the log next to
     * any message you log. See the WebMacro.properties (or other
     * configuration) for information on how to set up and
     * control logging.
     */
    public Log getLog(String type, String description);
 
    /**
     * Get a log using the type as the description
     */
    public Log getLog(String type);
 
    /**
     * Create a new WebContext object. This returns a Context object
     * with special knowledge of servlets (request and response)
     * thereby enabling some extra functionality. If you are using
     * WebMacro under a servlet this is the preferred method,
     * otherwise you ought to use getContext().
     */
    public org.webmacro.servlet.WebContext
          getWebContext(javax.servlet.http.HttpServletRequest req,
                        javax.servlet.http.HttpServletResponse resp);
 
 
    /**
     * Convienence method for writing a template to an OutputStream.
     * This method takes care of all the typical work involved
     * in writing a template.<p>
     *
     * This method uses the <code>TemplateOutputEncoding</code> defined in
     * WebMacro.defaults, or your custom WebMacro.properties.
     *
     * @param templateName name of Template to write.  Must be accessible
     *                     via TemplatePath
     * @param out          where the output of the template should go
     * @param context      The Context (can be a WebContext too) used
     *                     during the template evaluation phase
     * @throws java.io.IOException if the template cannot be written to the
     *                             specified output stream
     * @throws ResourceException if the template name specified cannot be found
     * @throws PropertyException if a fatal error occured during the Template
     *                           evaluation phase
     */
    public void writeTemplate(String templateName, java.io.OutputStream out,
                              Context context)
          throws java.io.IOException, ResourceException, PropertyException;
 
    /**
     * Convienence method for writing a template to an OutputStream.
     * This method takes care of all the typical work involved
     * in writing a template.
     *
     * @param templateName name of Template to write.  Must be accessible
     *                     via TemplatePath
     * @param out          where the output of the template should go
     * @param encoding     character encoding to use when writing the template
     *                     if the encoding is <code>null</code>, the default
     *                     <code>TemplateOutputEncoding</code> is used
     * @param context      The Context (can be a WebContext too) used
     *                     during the template evaluation phase
     * @throws java.io.IOException if the template cannot be written to the
     *                             specified output stream
     * @throws ResourceException if the template name specified cannot be found
     * @throws PropertyException if a fatal error occured during the Template
     *                           evaluation phase
     */
    public void writeTemplate(String templateName, java.io.OutputStream out,
                              String encoding, Context context)
          throws java.io.IOException, ResourceException, PropertyException;
 
 }
 
