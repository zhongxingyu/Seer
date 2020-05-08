 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004-2006 Brian M. Clapper. All rights reserved.
 
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
 
 package org.clapper.curn;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import java.text.DecimalFormat;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Iterator;
 
 import org.clapper.curn.parser.RSSParserFactory;
 import org.clapper.curn.parser.RSSParser;
 import org.clapper.curn.parser.RSSParserException;
 import org.clapper.curn.parser.RSSChannel;
 
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.io.FileUtil;
 import org.clapper.util.logging.Logger;
 
 import org.clapper.util.mail.EmailMessage;
 import org.clapper.util.mail.EmailTransport;
 import org.clapper.util.mail.SMTPEmailTransport;
 import org.clapper.util.mail.EmailAddress;
 import org.clapper.util.mail.EmailException;
 import org.clapper.util.misc.MIMETypeUtil;
 
 /**
  * <p><i>curn</i>: Customizable Utilitarian RSS Notifier.</p>
  *
  * <p><i>curn</i> is an RSS reader. It scans a configured set of URLs, each
  * one representing an RSS feed, and summarizes the results in an
  * easy-to-read text format. <i>curn</i> keeps track of URLs it's seen
  * before, using an on-disk cache; when using the cache, it will suppress
  * displaying URLs it has already reported (though that behavior can be
  * disabled). <i>curn</i> can be extended to use any RSS parser; its
  * built-in RSS parser, the
  * {@link org.clapper.curn.parser.minirss.MiniRSSParser MiniRSSParser}
  * class, can handle files in
  * {@link <a href="http://www.atomenabled.org/developers/">Atom</a>}
  * format (0.3) and RSS formats
  * {@link <a target="_top" href="http://backend.userland.com/rss091">0.91</a>},
  * 0.92,
  * {@link <a target="_top" href="http://web.resource.org/rss/1.0/">1.0</a>} and
  * {@link <a target="_top" href="http://blogs.law.harvard.edu/tech/rss">2.0</a>}.</p>
  *
  * <p>The <tt>Curn</tt> class represents the API entry point into the
  * <i>curn</i> processing. Any program can call a <tt>Curn</tt> object's
  * {@link #processRSSFeeds processRSSFeeds()} method to invoke a <i>curn</i>
  * run. In practice, most people use the existing <tt>Tool</tt> command-line
  * program.</p>
  *
  * @version <tt>$Revision$</tt>
  */
 public class Curn
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     private CurnConfig config = null;
     private boolean useCache = true;
     private FeedCache cache = null;
     private Date currentTime = new Date();
     private MetaPlugIn metaPlugIn = null;
 
     private Collection<ConfiguredOutputHandler> outputHandlers =
         new ArrayList<ConfiguredOutputHandler>();
 
     /**
      * For log messages
      */
     private static Logger log = new Logger (Curn.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Instantiate a new <tt>Curn</tt> object and loads its plugins.
      *
      * @throws CurnException on error
      */
     public Curn()
         throws CurnException
     {
         metaPlugIn = MetaPlugIn.getMetaPlugIn();
     }
 
     /*----------------------------------------------------------------------*\
                               Public Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Run <i>curn</i> against a configuration file.
      *     
      * @param configPath      path to the configuration data
      * @param emailAddresses  a collection of (string) email addresses to
      *                        receive the output, or null (or empty collection)
      *                        for none.
      * @param useCache        whether or not to use the cache
      *
      * @throws CurnException on error
      */
     public void run (String             configPath,
                      Collection<String> emailAddresses,
                      boolean            useCache)
         throws CurnException
     {
         metaPlugIn.runStartupPlugIn();
 
         try
         {
             this.config = loadConfig (configPath);
             processRSSFeeds (emailAddresses, useCache);
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
 
         catch (RSSParserException ex)
         {
             throw new CurnException (ex);
         }
 
         finally
         {
             metaPlugIn.runShutdownPlugIn();
         }
     }
 
     /**
      * Set the cache's notion of the current time. This method will change
      * the time used when reading and pruning the cache from the current time
      * to the specified time. This method must be called before
      * <tt>processRSSFeeds()</tt>.
      *
      * @param newTime  the time to pretend is the current time
      */
     public void setCurrentTime (Date newTime)
     {
         this.currentTime = newTime;
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     /**
      * Read the RSS feeds specified in a parsed configuration, writing them
      * to the output handler(s) specified in the configuration.
      *
      * @param emailAddresses  a collection of (string) email addresses to
      *                        receive the output, or null (or empty collection)
      *                        for none.
      * @param useCache        whether or not to use the cache
      *
      * @throws IOException             unable to open or read a required file
      * @throws ConfigurationException  error in configuration file
      * @throws RSSParserException      error parsing XML feed(s)
      * @throws CurnException           any other error
      */
     private void processRSSFeeds (Collection<String> emailAddresses,
                                   boolean            useCache)
         throws ConfigurationException,
                RSSParserException,
                CurnException
     {
         String                   parserClassName;
         Map<FeedInfo,RSSChannel> channels;
         boolean                  parsingEnabled = true;
         File                     cacheFile = config.getCacheFile();
 
         loadOutputHandlers (config);
 
         if (useCache && (cacheFile != null))
         {
             cache = new FeedCache (config);
             cache.setCurrentTime (currentTime);
             cache.loadCache();
             metaPlugIn.runCacheLoadedPlugIn (cache);
         }
 
         if (config.isDownloadOnly())
         {
             // No output handlers. No need to instantiate a parser.
 
             log.debug ("Config is download-only. Skipping XML parse phase.");
             parsingEnabled = false;
         }
 
         Collection<FeedInfo> feeds = config.getFeeds();
         if (feeds.size() == 0)
         {
             throw new ConfigurationException (Constants.BUNDLE_NAME,
                                               "Curn.noConfiguredFeeds",
                                               "No configured RSS feed URLs.");
         }
 
         if ((config.getMaxThreads() == 1) || (feeds.size() == 1))
             channels = doSingleThreadedFeedDownload (parsingEnabled,
                                                      cache,
                                                      config);
         else
             channels = doMultithreadedFeedDownload (parsingEnabled,
                                                     cache,
                                                     config);
 
         log.debug ("After downloading, total (parsed) channels = "
                  + channels.size());
 
         if (channels.size() > 0)
         {
             // Note: If we're not emailing the output, then dump the output
             // from the first handler to the screen.
 
             displayChannels (channels, emailAddresses.size() == 0);
         }
 
         // If there are email addresses, then mail the output.
 
         if ((emailAddresses != null) && (emailAddresses.size() > 0))
             emailOutput (outputHandlers, emailAddresses);
 
         log.debug ("cacheFile="
                  + ((cacheFile == null) ? "null" : cacheFile.getPath())
                  + ", mustUpdateCache="
                  + config.mustUpdateCache());
 
         if ((cache != null) && config.mustUpdateCache())
         {
             int totalCacheBackups = config.totalCacheBackups();
             metaPlugIn.runPreCacheSavePlugIn (cache);
             cache.saveCache (totalCacheBackups);
         }
     }
 
     private CurnConfig loadConfig (String configPath)
         throws CurnException
     {
         try
         {
             config = new CurnConfig (configPath);
             MetaPlugIn.getMetaPlugIn().runPostConfigurationPlugIn (config);
             return config;
         }
 
         catch (FileNotFoundException ex)
         {
             throw new CurnException (Constants.BUNDLE_NAME,
                                      "Curn.cantFindConfig",
                                      "Cannot find configuration file \"{0}\"",
                                      new Object[] {configPath},
                                      ex);
         }
 
         catch (IOException ex)
         {
             throw new CurnException (Constants.BUNDLE_NAME,
                                      "Curn.cantReadConfig",
                                      "I/O error reading configuration file "
                                    + "\"{0}\"",
                                      new Object[] {configPath},
                                      ex);
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
     }
 
     private void loadOutputHandlers (CurnConfig configuration)
         throws ConfigurationException,
                CurnException
     {
         if (configuration.totalOutputHandlers() > 0)
         {
             for (ConfiguredOutputHandler cfgHandler : configuration.getOutputHandlers())
             {
                 // Ensure that the output handler can be instantiated.
 
                String className = cfgHandler.getClassName();

                 log.debug ("Instantiating output handler \""
                          + cfgHandler.getName()
                          + "\", of type "
                         + className);
                OutputHandler handler = cfgHandler.getOutputHandler();

                log.debug ("Initializing output handler \""
                         + cfgHandler.getName()
                         + "\", of type "
                         + className);

                handler.init (config, cfgHandler);
 
                 // Save it.
 
                 outputHandlers.add (cfgHandler);
             }
         }
     }
 
     /**
      * Download the configured feeds sequentially. This method is called
      * when the configured number of concurrent download threads is 1.
      *
      * @param parsingEnabled <tt>true</tt> if parsing is to be done,
      *                       <tt>false</tt> otherwise
      * @param feedCache      the loaded cache of feed data; may be modified
      * @param configuration  the parsed configuration
      *
      * @return a <tt>Map</tt> of <tt>RSSChannel</tt> objects, indexed
      *         by <tt>FeedInfo</tt>
      *
      * @throws RSSParserException error parsing feeds
      * @throws CurnException      some other error
      */
     private Map<FeedInfo,RSSChannel>
     doSingleThreadedFeedDownload (boolean    parsingEnabled,
                                   FeedCache  feedCache,
                                   CurnConfig configuration)
         throws RSSParserException,
                CurnException
     {
         // Instantiate a single FeedDownloadThread object, but call it
         // within this thread, instead of spawning another thread.
 
         final Map<FeedInfo,RSSChannel> channels =
             new LinkedHashMap<FeedInfo,RSSChannel>();
 
         FeedDownloadThread  downloadThread;
 
         log.info ("Doing single-threaded download of feeds.");
 
         downloadThread =
             new FeedDownloadThread
                 ("main",
                  getRSSParser (configuration),
                  feedCache,
                  configuration,
                  null,
                  new FeedDownloadDoneHandler()
                  {
                      public void feedFinished (FeedInfo  feedInfo,
                                                RSSChannel channel)
                      {
                          channels.put (feedInfo, channel);
                      }
                  });
 
         for (Iterator it = configuration.getFeeds().iterator(); it.hasNext(); )
         {
             FeedInfo feedInfo = (FeedInfo) it.next();
 
             if (! feedShouldBeProcessed (feedInfo))
             {
                 // Log messages already emitted.
 
                 continue;
             }
 
             downloadThread.processFeed (feedInfo);
 
             // Don't abort if an exception occurred. It might just be a
             // parse error for this feed. Don't want to abort the whole
             // run if one feed has problems.
             /*
             if (downloadThread.errorOccurred())
                 throw downloadThread.getException();
             */
 
             RSSChannel channel = downloadThread.getParsedChannelData();
             if (channel != null)
                 channels.put (feedInfo, channel);
         }
 
         return channels;
     }
 
     /**
      * Download the configured feeds using multiple simultaneous threads.
      * This method is called when the configured number of concurrent
      * download threads is greater than 1.
      *
      * @param parsingEnabled <tt>true</tt> if parsing is to be done,
      *                       <tt>false</tt> otherwise
      * @param feedCache      the loaded cache of feed data; may be modified
      * @param configuration  the parsed configuration
      *
      * @return a <tt>Map</tt> of <tt>RSSChannel</tt> objects, indexed
      *         by <tt>FeedInfo</tt>
      *
      * @throws RSSParserException error parsing feeds
      * @throws CurnException      some other error
      */
     private Map<FeedInfo,RSSChannel>
     doMultithreadedFeedDownload (boolean    parsingEnabled,
                                  FeedCache  feedCache,
                                  CurnConfig configuration)
         throws RSSParserException,
                CurnException
     {
         int maxThreads = configuration.getMaxThreads();
         Collection<FeedInfo> feeds = configuration.getFeeds();
         int totalFeeds = feeds.size();
         final Map<FeedInfo,RSSChannel> channels =
             new LinkedHashMap<FeedInfo,RSSChannel>();
         List<FeedDownloadThread> threads = new ArrayList<FeedDownloadThread>();
         List<FeedInfo> feedQueue  = new LinkedList<FeedInfo>();
         Iterator it;
         FeedDownloadThread thread;
 
         if (maxThreads > totalFeeds)
             maxThreads = totalFeeds;
 
         log.info ("Doing multithreaded download of feeds, using "
                 + maxThreads
                 + " threads.");
 
         // Fill the feed queue and make it a synchronized list.
 
         for (it = feeds.iterator(); it.hasNext(); )
         {
             FeedInfo feedInfo = (FeedInfo) it.next();
 
             if (! feedShouldBeProcessed (feedInfo))
             {
                 // Log messages already emitted.
 
                 continue;
             }
 
             feedQueue.add (feedInfo);
         }
 
         if (feedQueue.size() == 0)
         {
             throw new CurnException (Constants.BUNDLE_NAME,
                                      "Curn.allFeedsDisabled",
                                      "All configured RSS feeds are disabled.");
         }
 
         feedQueue = Collections.synchronizedList (feedQueue);
 
         // Create the thread objects. They'll pull feeds off the queue
         // themselves.
 
         for (int i = 0; i < maxThreads; i++)
         {
             RSSParser parser = null;
 
             if (parsingEnabled)
                 parser = getRSSParser (configuration);
 
             thread = new FeedDownloadThread
                          (String.valueOf (i),
                           parser,
                           feedCache,
                           configuration,
                           feedQueue,
                           new FeedDownloadDoneHandler()
                           {
                               public void feedFinished (FeedInfo  feedInfo,
                                                         RSSChannel channel)
                               {
                                   channels.put (feedInfo, channel);
                               }
                           });
             thread.start();
             threads.add (thread);
         }
 
         log.debug ("Main thread priority is "
                  + Thread.currentThread().getPriority());
 
         log.debug ("All feeds have been parceled out to threads. Waiting for "
                  + "threads to complete.");
 
         for (it = threads.iterator(); it.hasNext(); )
         {
             thread = (FeedDownloadThread) it.next();
             String threadName = thread.getName();
 
             log.info ("Waiting for thread " + threadName);
 
             try
             {
                 thread.join();
             }
 
             catch (InterruptedException ex)
             {
                 log.debug ("Interrupted during thread join: " + ex.toString());
             }
 
             log.info ("Joined thread " + threadName);
         }
 
         return channels;
     }
 
     /**
      * Get a new instance of an RSS parser.
      *
      * @param configuration the parsed configuration
      *
      * @return the RSSParser
      *
      * @throws RSSParserException error instantiating parser
      */
     private RSSParser getRSSParser (CurnConfig configuration)
         throws RSSParserException
     {
         String parserClassName = configuration.getRSSParserClassName();
         log.info ("Getting parser \"" + parserClassName + "\"");
         return RSSParserFactory.getRSSParser (parserClassName);
     }
 
     /**
      * Determine whether a feed should be processed. If this method
      * determines that a feed should not be processed, it emits appropriate
      * log messages.
      *
      * @param feedInfo  the feed information
      *
      * @return <tt>true</tt> if the feed should be processed,
      *         <tt>false</tt> if it should be skipped
      *
      * @throws CurnException on error
      */
     private boolean feedShouldBeProcessed (FeedInfo feedInfo)
         throws CurnException
     {
         boolean process = true;
 
         if (! feedInfo.feedIsEnabled())
         {
             log.info ("Skipping disabled feed: "
                     + feedInfo.getURL().toString());
             process = false;
         }
 
         return process;
     }
 
     private void displayChannels (Map<FeedInfo,RSSChannel> channels,
                                   boolean                  showFirstOutput)
         throws CurnException,
                ConfigurationException
     {
         ConfiguredOutputHandler firstOutput = null;
         OutputHandler           handler;
 
         // Dump the output to each output handler
 
         for (ConfiguredOutputHandler cfgHandler : outputHandlers)
         {
             log.info ("Initializing output handler \""
                     + cfgHandler.getName()
                     + "\", of type "
                     + cfgHandler.getClassName());
 
             handler = cfgHandler.getOutputHandler();
 
             for (FeedInfo fi : channels.keySet())
             {
                 RSSChannel channel = channels.get (fi);
                 metaPlugIn.runPreFeedOutputPlugIn (fi,
                                                    channel.makeCopy(),
                                                    handler);
                 handler.displayChannel (channel, fi);
                 metaPlugIn.runPostFeedOutputPlugIn (fi, handler);
             }
 
             handler.flush();
             ReadOnlyOutputHandler ro = new ReadOnlyOutputHandler (handler);
             if (metaPlugIn.runPostOutputHandlerFlushPlugIn (ro))
             {
                 // Okay to consider this one.
 
                 if ((firstOutput == null) && (handler.hasGeneratedOutput()))
                     firstOutput = cfgHandler;
             }
 
             else
             {
                 cfgHandler.disable();
             }
         }
 
 
         if (showFirstOutput)
         {
             if (firstOutput == null)
                 log.info ("None of the output handlers produced output.");
 
             else
             {
                 handler = firstOutput.getOutputHandler();
                 File output = handler.getGeneratedOutput();
 
                 try
                 {
                     FileUtil.copyStream (new FileInputStream (output),
                                          System.out);
                 }
 
                 catch (IOException ex)
                 {
                     throw new CurnException (Constants.BUNDLE_NAME,
                                              "Curn.outputCopyFailed",
                                              "Failed to copy output from "
                                            + "handler \"{0}\" to standard "
                                            + "output.",
                                              new Object[]
                                              {
                                                  firstOutput.getClassName()
                                              },
                                              ex);
 
                 }
             }
         }
     }
 
     private void
     emailOutput (Collection<ConfiguredOutputHandler> outputHandlers,
                  Collection<String>                  emailAddresses)
         throws CurnException
     {
         try
         {
             log.debug ("There are email addresses.");
 
             Iterator                 it;
             OutputHandler            firstHandlerWithOutput = null;
             OutputHandler            handler;
             ConfiguredOutputHandler  handlerWrapper;
             int                      totalAttachments = 0;
 
             // First, figure out whether we have any attachments or not.
 
             for (ConfiguredOutputHandler cfgHandler : outputHandlers)
             {
                 handler = cfgHandler.getOutputHandler();
 
                 if (handler.hasGeneratedOutput())
                 {
                     totalAttachments++;
                     if (firstHandlerWithOutput == null)
                     {
                         log.debug ("First handler with output="
                                  + cfgHandler.getName());
                         firstHandlerWithOutput = handler;
                     }
                 }
             }
 
             if (totalAttachments == 0)
             {
                 // None of the handlers produced any output.
 
                 System.err.println ("Warning: None of the output handlers "
                                   + "produced any emailable output.");
             }
 
             else
             {
                 // Create an SMTP transport and a new email message.
 
                 String smtpHost = config.getSMTPHost();
                 String sender = config.getEmailSender();
                 EmailTransport transport = new SMTPEmailTransport (smtpHost);
                 EmailMessage message = new EmailMessage();
 
                 log.debug ("SMTP host = " + smtpHost);
 
                 // Fill 'er up.
 
                 for (String emailAddress : emailAddresses)
                 {
                     try
                     {
                         message.addTo (new EmailAddress (emailAddress));
                     }
 
                     catch (EmailException ex)
                     {
                         throw new CurnException ("\""
                                                + emailAddress
                                                + "\" is invalid",
                                                  ex);
                     }
                 }
 
                 message.addHeader ("X-Mailer", Version.getFullVersion());
                 message.setSubject (config.getEmailSubject());
 
                 if (sender != null)
                     message.setSender (sender);
 
                 if (log.isDebugEnabled())
                     log.debug ("Email sender = " + message.getSender());
 
                 // Add the output. If there's only one attachment, and its
                 // output is text, then there's no need for attachments.
                 // Just set it as the text part, and set the appropriate
                 // Content-type: header. Otherwise, make a
                 // multipart-alternative message with separate attachments
                 // for each output.
 
                 DecimalFormat fmt  = new DecimalFormat ("##000");
                 StringBuffer  name = new StringBuffer();
                 String        ext;
                 String        contentType;
                 File          file;
 
                 if (totalAttachments == 1)
                 {
                     handler = firstHandlerWithOutput;
                     contentType = handler.getContentType();
                     ext = MIMETypeUtil.fileExtensionForMIMEType (contentType);
                     file = handler.getGeneratedOutput();
                     message.setMultipartSubtype (EmailMessage.MULTIPART_MIXED);
 
                     name.append (fmt.format (1));
                     name.append ('.');
                     name.append (ext);
 
                     if (contentType.startsWith ("text/"))
                         message.setText (file, name.toString(), contentType);
                     else
                         message.addAttachment (file,
                                                name.toString(),
                                                contentType);
                 }
 
                 else
                 {
                     message.setMultipartSubtype
                                           (EmailMessage.MULTIPART_ALTERNATIVE);
 
                     int i = 1;
                     for (ConfiguredOutputHandler cfgHandler : outputHandlers)
                     {
                         handler = cfgHandler.getOutputHandler();
 
                         contentType = handler.getContentType();
                         ext = MIMETypeUtil.fileExtensionForMIMEType
                                                                 (contentType);
                         file = handler.getGeneratedOutput();
                         if (file != null)
                         {
                             name.setLength (0);
                             name.append (fmt.format (i));
                             name.append ('.');
                             name.append (ext);
                             i++;
                             message.addAttachment (file,
                                                    name.toString(),
                                                    contentType);
                         }
                     }
                 }
 
                 log.debug ("Sending message.");
                 transport.send (message);
                 message.clear();
             }
         }
 
         catch (EmailException ex)
         {
             throw new CurnException (ex);
         }
     }
 }
