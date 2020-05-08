 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004-2005 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms are permitted provided
   that: (1) source distributions retain this entire copyright notice and
   comment; and (2) modifications made to the software are prominently
   mentioned, and a copy of the original software (or a pointer to its
   location) are included. The name of the author may not be used to endorse
   or promote products derived from this software without specific prior
   written permission.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
   Effectively, this means you can do what you want with the software except
   remove this notice or take advantage of the author's name. If you modify
   the software and redistribute your modified version, you must indicate that
   your version is a modification of the original, and you must provide either
   a pointer to or a copy of the original.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.output;
 
 import org.clapper.curn.ConfigFile;
 import org.clapper.curn.ConfiguredOutputHandler;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.OutputHandler;
 import org.clapper.curn.util.Util;
 import org.clapper.curn.parser.RSSChannel;
 import org.clapper.curn.parser.RSSItem;
 
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.config.NoSuchSectionException;
 import org.clapper.util.io.FileUtil;
 import org.clapper.util.io.IOExceptionExt;
 import org.clapper.util.io.RollingFileWriter;
 import org.clapper.util.logging.Logger;
 import org.clapper.util.text.HTMLUtil;
 import org.clapper.util.text.Unicode;
 
 import java.io.IOException;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * <p><tt>FileOutputHandler</tt> is an abstract base class for
  * <tt>OutputHandler</tt> subclasses that write RSS feed summaries to a
  * file. It consolidates common logic and configuration handling for such
  * classes, providing both consistent implementation and configuration.
  * It handles two additional output handler-specific configuration items:</p>
  *
  * <ul>
  *   <li><tt>SaveAs</tt> takes a file name argument and specifies a file
  *       where the handler should save its output permanently. It's useful
  *       if the user wants to keep a copy of the output the handler generates,
  *       in addition to having the output reported by <i>curn</i>.
  *   <li><tt>SaveOnly</tt> instructs the handler to save the output in the
  *       <tt>SaveAs</tt> file, but not report the output to <i>curn</i>.
  *       From <i>curn</i>'s perspective, the handler generates no output
  *       at all.
  * </ul>
  *
  * @see OutputHandler
  * @see org.clapper.curn.Curn
  * @see org.clapper.curn.parser.RSSChannel
  *
  * @version <tt>$Revision$</tt>
  */
 public abstract class FileOutputHandler implements OutputHandler
 {
     /*----------------------------------------------------------------------*\
                              Public Constants
     \*----------------------------------------------------------------------*/
 
     /**
      * Configuration variable: encoding
      */
     public static final String CFG_ENCODING = "Encoding";
 
     /**
      * Whether or not to show curn information
      */
     public static final String CFG_SHOW_CURN_INFO = "ShowCurnInfo";
 
     /**
      * Where to save the output, if any
      */
     public static final String CFG_SAVE_AS = "SaveAs";
 
     /**
      * Whether we're ONLY saving output
      */
     public static final String CFG_SAVE_ONLY = "SaveOnly";
 
     /**
      * Number of backups of saved files to keep.
      */
     public static final String CFG_SAVED_BACKUPS = "SavedBackups";
 
     /*----------------------------------------------------------------------*\
                              Public Constants
     \*----------------------------------------------------------------------*/
 
     /**
      * Default encoding value
      */
     private static final String DEFAULT_CHARSET_ENCODING = "utf-8";
 
     /*----------------------------------------------------------------------*\
                            Private Instance Data
     \*----------------------------------------------------------------------*/
 
     private File        outputFile     = null;
     private ConfigFile  config         = null;
     private boolean     saveOnly       = false;
     private String      name           = null;
     private boolean     showToolInfo   = true;
     private int         savedBackups   = 0;
     private String      encoding       = null;
 
     /**
      * For logging
      */
     private Logger log = null;
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Construct a new <tt>FileOutputHandler</tt>
      */
     public FileOutputHandler()
     {
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Initializes the output handler for another set of RSS channels.
      *
      * @param config     the parsed <i>curn</i> configuration data
      * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
      *                   containing this object; the wrapper has some useful
      *                   metadata, such as the object's configuration section
      *                   name and extra variables.
      *
      * @throws ConfigurationException  configuration error
      * @throws CurnException           some other initialization error
      */
     public final void init (ConfigFile              config,
                             ConfiguredOutputHandler cfgHandler)
         throws ConfigurationException,
                CurnException
     {
         String saveAs      = null;
         String sectionName = null;
 
         this.config = config;
         sectionName = cfgHandler.getSectionName();
         this.name   = sectionName;
 
         log = new Logger (FileOutputHandler.class.getName()
                         + "["
                         + name
                         + "]");
         try
         {
             if (sectionName != null)
             {
                 saveAs = config.getOptionalStringValue (sectionName,
                                                         CFG_SAVE_AS,
                                                         null);
                 savedBackups = config.getOptionalCardinalValue
                                               (sectionName,
                                                CFG_SAVED_BACKUPS,
                                                0);
                 saveOnly = config.getOptionalBooleanValue (sectionName,
                                                            CFG_SAVE_ONLY,
                                                            false);
 
                 showToolInfo = config.getOptionalBooleanValue
                                                (sectionName,
                                                 CFG_SHOW_CURN_INFO,
                                                 true);
                 encoding = config.getOptionalStringValue
                                                (sectionName,
                                                 CFG_ENCODING,
                                                 DEFAULT_CHARSET_ENCODING);
 
                 // saveOnly cannot be set unless saveAs is non-null. The
                 // ConfigFile class is supposed to trap for this, so an
                 // assertion is fine here.
 
                 assert ((! saveOnly) || (saveAs != null));
             }
         }
 
         catch (NoSuchSectionException ex)
         {
             throw new ConfigurationException (ex);
         }
 
         if (saveAs != null)
             outputFile = new File (saveAs);
 
         else
         {
             try
             {
                 outputFile = File.createTempFile ("curn", null);
                 outputFile.deleteOnExit();
             }
 
             catch (IOException ex)
             {
                 throw new CurnException (Util.BUNDLE_NAME,
                                          "FileOutputHandler.cantMakeTempFile",
                                          "Cannot create temporary file",
                                          ex);
             }
         }
 
         log.debug ("Calling "
                  + this.getClass().getName()
                 + ".initOutputHandler()");
 
         initOutputHandler (config, cfgHandler);
     }
 
     /**
      * Perform any subclass-specific initialization. Subclasses must
      * override this method.
      *
      * @param config     the parsed <i>curn</i> configuration data
      * @param cfgHandler the <tt>ConfiguredOutputHandler</tt> wrapper
      *                   containing this object; the wrapper has some useful
      *                   metadata, such as the object's configuration section
      *                   name and extra variables.
      *
      * @throws ConfigurationException  configuration error
      * @throws CurnException           some other initialization error
      */
     public abstract void initOutputHandler (ConfigFile              config,
                                             ConfiguredOutputHandler cfgHandler)
         throws ConfigurationException,
                CurnException;
 
     /**
      * Display the list of <tt>RSSItem</tt> news items to whatever output
      * is defined for the underlying class. Output is written to the
      * <tt>PrintWriter</tt> that was passed to the {@link #init init()}
      * method.
      *
      * @param channel  The channel containing the items to emit. The method
      *                 should emit all the items in the channel; the caller
      *                 is responsible for clearing out any items that should
      *                 not be seen.
      * @param feedInfo Information about the feed, from the configuration
      *
      * @throws CurnException  unable to write output
      */
     public abstract void displayChannel (RSSChannel  channel,
                                          FeedInfo    feedInfo)
         throws CurnException;
 
     /**
      * Flush any buffered-up output.
      *
      * @throws CurnException  unable to write output
      */
     public abstract void flush() throws CurnException;
     
     /**
      * Get the content (i.e., MIME) type for output produced by this output
      * handler.
      *
      * @return the content type
      */
     public abstract String getContentType();
 
     /**
      * Get the <tt>File</tt> that represents the output produced by the
      * handler, if applicable. (Use of a <tt>File</tt>, rather than an
      * <tt>InputStream</tt>, is more efficient when mailing the output,
      * since the email API ultimately wants files and will create
      * temporary files for <tt>InputStream</tt>s.)
      *
      * @return the output file, or null if no suitable output was produced
      *
      * @throws CurnException an error occurred
      */
     public final File getGeneratedOutput()
         throws CurnException
     {
         return hasGeneratedOutput() ? outputFile : null;
     }
 
     /**
      * Determine whether this handler has produced any actual output (i.e.,
      * whether {@link #getGeneratedOutput()} will return a non-null
      * <tt>File</tt> if called).
      *
      * @return <tt>true</tt> if the handler has produced output,
      *         <tt>false</tt> if not
      *
      * @see #getGeneratedOutput
      * @see #getContentType
      */
     public final boolean hasGeneratedOutput()
     {
         boolean hasOutput = false;
 
         if ((! saveOnly) && (outputFile != null))
         {
             long len = outputFile.length();
             log.debug ("outputFile=" + outputFile.getPath() + ", size=" + len);
 
             hasOutput = (len > 0);
         }
 
         log.debug ("hasGeneratedOutput? " + hasOutput);
         return hasOutput;
     }
 
     /*----------------------------------------------------------------------*\
                              Protected Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Get the output file.
      *
      * @return the output file, or none if not created yet
      */
     protected final File getOutputFile()
     {
         return outputFile;
     }
 
     /**
      * Open the output file, returning a <tt>PrintWriter</tt>. Handles
      * whether or not to roll the saved file, etc.
      *
      * @return the <tt>PrintWriter</tt>
      *
      * @throws CurnException unable to open file
      */
     protected PrintWriter openOutputFile()
         throws CurnException
     {
         PrintWriter w = null;
 
         try
         {
             log.debug ("Opening output file \"" + outputFile + "\"");
 
             // For the output handler output file, the index marker between
             // the file name and the extension, rather than at the end of
             // the file (since the extension is likely to matter).
 
             w = Util.openOutputFile (outputFile,
                                      encoding,
                                      Util.IndexMarker.BEFORE_EXTENSION,
                                      savedBackups);
         }
 
         catch (IOExceptionExt ex)
         {
             throw new CurnException (ex);
         }
 
         return w;
     }
 
     /**
      * Determine whether the handler is saving output only, or also reporting
      * output to <i>curn</i>.
      *
      * @return <tt>true</tt> if saving output only, <tt>false</tt> if also
      *         reporting output to <i>curn</i>
      */
     protected final boolean savingOutputOnly()
     {
         return saveOnly;
     }
 
     /**
      * Get the configured encoding.
      *
      * @return the encoding, or null if not configured
      *
      * @see #setEncoding
      */
     protected final String getEncoding()
     {
         return encoding;
     }
 
     /**
      * Override the encoding specified by the {@link #CFG_ENCODING}
      * configuration parameter. To have any effect, this method must be
      * called before {@link #openOutputFile}
      *
      * @param newEncoding  the new encoding, or null to use the default
      *
      * @see #getEncoding
      */
     protected final void setEncoding (String newEncoding)
     {
         this.encoding = newEncoding;
     }
 
     /**
      * Determine whether or not to display curn tool-related information in
      * the generated output. Subclasses are not required to display
      * tool-related information in the generated output, but if they do,
      * they are strongly encouraged to do so conditionally, based on the
      * value of this configuration item.
      *
      * @return <tt>true</tt> if tool-related information is to be displayed
      *         (assuming the output handler supports it), or <tt>false</tt>
      *         if tool-related information should be suppressed.
      */
     protected final boolean displayToolInfo()
     {
         return this.showToolInfo;
     }
 
     /**
      * Convert certain Unicode characters in a string to plain text
      * sequences. Also strips embedded HTML tags from the string. Useful
      * primarily for handlers that produce plain text.
      *
      * @param s  the string to convert
      *
      * @return the possibly converted string
      */
     protected String convert (String s)
     {
         StringBuffer buf = new StringBuffer();
         char[]       ch;
 
         if (s == null)
             return "";
 
         s = HTMLUtil.textFromHTML (s);
         ch = s.toCharArray();
 
         buf.setLength (0);
         for (int i = 0; i < ch.length; i++)
         {
             switch (ch[i])
             {
                 case Unicode.LEFT_SINGLE_QUOTE:
                 case Unicode.RIGHT_SINGLE_QUOTE:
                     buf.append ('\'');
                     break;
 
                 case Unicode.LEFT_DOUBLE_QUOTE:
                 case Unicode.RIGHT_DOUBLE_QUOTE:
                     buf.append ('"');
                     break;
 
                 case Unicode.EM_DASH:
                     buf.append ("--");
                     break;
 
                 case Unicode.EN_DASH:
                     buf.append ('-');
                     break;
 
                 case Unicode.TRADEMARK:
                     buf.append ("[TM]");
                     break;
 
                 default:
                     buf.append (ch[i]);
                     break;
             }
         }
 
         return buf.toString();
     }
 
     /**
      * Convert various fields in a channel and its subitems by invoking the
      * {@link #convert} method on them. Intended primarily for output handlers
      * that produce plain text. Produces a copy of the channel, so that the
      * original channel isn't modified (since it might be used by subsequent
      * handlers that don't want the data to be converted).
      *
      * @param channel  the channel
      *
      * @return a copy of the channel, with possibly converted data.
      *
      * @throws CurnException on error
      */
     protected RSSChannel convertChannelText (RSSChannel channel)
         throws CurnException
     {
         RSSChannel channelCopy = channel.makeCopy();
 
         Collection<RSSItem> items = channelCopy.getItems();
         if ((items != null) && (items.size() > 0))
         {
             for (Iterator it = items.iterator(); it.hasNext(); )
             {
                 RSSItem item = (RSSItem) it.next();
                 item.setTitle (convert (item.getTitle()));
 
                 Collection<String> authors = item.getAuthors();
                 if ((authors != null) && (authors.size() > 0))
                 {
                     Collection<String> cvtAuthors = new ArrayList<String>();
 
                     for (String author : authors)
                         cvtAuthors.add (convert (author));
 
                     item.setAuthors (cvtAuthors);
                 }
 
                 String s = item.getSummary();
                 if (s != null)
                     item.setSummary (convert (s));
             }
         }
 
         return channelCopy;
     }
 }
