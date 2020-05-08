 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a BSD-style license:
 
   Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
 
   1.  Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.
 
   2.  The end-user documentation included with the redistribution, if any,
       must include the following acknowlegement:
 
         "This product includes software developed by Brian M. Clapper
         (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
         copyright (c) 2004-2007 Brian M. Clapper."
 
       Alternately, this acknowlegement may appear in the software itself,
       if wherever such third-party acknowlegements normally appear.
 
   3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
       nor any of the names of the project contributors may be used to
       endorse or promote products derived from this software without prior
       written permission. For written permission, please contact
       bmc@clapper.org.
 
   4.  Products derived from this software may not be called "clapper.org
       Java Utility Library", nor may "clapper.org" appear in their names
       without prior written permission of Brian M. Clapper.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
   NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn.plugins;
 
 import org.clapper.curn.Constants;
 import org.clapper.curn.CurnConfig;
 import org.clapper.curn.CurnException;
 import org.clapper.curn.FeedInfo;
 import org.clapper.curn.FeedConfigItemPlugIn;
 import org.clapper.curn.PostConfigPlugIn;
 import org.clapper.curn.PreFeedDownloadPlugIn;
 import org.clapper.curn.PostFeedDownloadPlugIn;
 
 import org.clapper.util.classutil.ClassUtil;
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.io.FileUtil;
 import org.clapper.util.logging.Logger;
 
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.Writer;
 
 import java.net.URLConnection;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import org.clapper.curn.CurnUtil;
 import org.clapper.util.cmdline.CommandLineUsageException;
 import org.clapper.util.cmdline.ParameterHandler;
 import org.clapper.util.cmdline.ParameterParser;
 import org.clapper.util.cmdline.UsageInfo;
 import org.clapper.util.io.IOExceptionExt;
 import org.clapper.util.text.TextUtil;
 
 /**
  * The <tt>RawFeedSaveAsPlugIn</tt> handles saving a feed to a known location.
  * It intercepts the following per-feed configuration parameters:
  *
  * <table border="1">
  *   <tr valign="top">
  *     <td><tt>SaveAs</tt></td>
  *     <td>Path to file where raw XML should be saved.</td>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>SaveOnly</tt></td>
  *     <td>If set to "true", this parameter indicates that raw XML should be
  *         saved, but not parsed. This parameter can only be specified if
  *         <tt>SaveAs</tt> is also specified.</td>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>SaveAsEncoding</tt></td>
  *     <td>The character set encoding to use when saving the file. Default:
  *     "utf-8"</td>
  *   </tr>
  * </table>
  *
  * @version <tt>$Revision$</tt>
  */
 public class RawFeedSaveAsPlugIn
     implements FeedConfigItemPlugIn,
                PostConfigPlugIn,
                PreFeedDownloadPlugIn,
                PostFeedDownloadPlugIn
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String VAR_SAVE_FEED_AS      = "SaveAs";
     private static final String VAR_SAVE_ONLY         = "SaveOnly";
     private static final String VAR_SAVE_AS_ENCODING  = "SaveAsEncoding";
 
     /*----------------------------------------------------------------------*\
                               Private Classes
     \*----------------------------------------------------------------------*/
 
     /**
      * Feed save info
      */
     class FeedSaveInfo
     {
         String  sectionName;
         File    saveAsFile;
         int     backups = 0;
         boolean saveOnly;
         String  saveAsEncoding = "utf-8";
 
         FeedSaveInfo()
         {
             // Nothing to do
         }
     }
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * Feed save data, by feed
      */
     private Map<FeedInfo,FeedSaveInfo> perFeedSaveAsMap =
         new HashMap<FeedInfo,FeedSaveInfo>();
 
     /**
      * Saved reference to the configuration
      */
     private CurnConfig config = null;
 
     /**
      * For log messages
      */
     private static final Logger log = new Logger (RawFeedSaveAsPlugIn.class);
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Default constructor (required).
      */
     public RawFeedSaveAsPlugIn()
     {
         // Nothing to do
     }
 
     /*----------------------------------------------------------------------*\
                Public Methods Required by *PlugIn Interfaces
     \*----------------------------------------------------------------------*/
 
     /**
      * Get a displayable name for the plug-in.
      *
      * @return the name
      */
     public String getPlugInName()
     {
         return "Save As";
     }
 
     /**
      * Get the sort key for this plug-in.
      *
      * @return the sort key string.
      */
     public String getPlugInSortKey()
     {
         return ClassUtil.getShortClassName (getClass().getName());
     }
 
     /**
      * Initialize the plug-in. This method is called before any of the
      * plug-in methods are called.
      *
      * @throws CurnException on error
      */
     public void initPlugIn()
         throws CurnException
     {
     }
 
     /**
      * Called immediately after <i>curn</i> has read and processed a
      * configuration item in a "feed" configuration section. All
      * configuration items are passed, one by one, to each loaded plug-in.
      * If a plug-in class is not interested in a particular configuration
      * item, this method should simply return without doing anything. Note
      * that some configuration items may simply be variable assignment;
      * there's no real way to distinguish a variable assignment from a
      * blessed configuration item.
      *
      * @param sectionName  the name of the configuration section where
      *                     the item was found
      * @param paramName    the name of the parameter
      * @param config       the active configuration
      * @param feedInfo     partially complete <tt>FeedInfo</tt> object
      *                     for the feed. The URL is guaranteed to be
      *                     present, but no other fields are.
      *
      * @return <tt>true</tt> to continue processing the feed,
      *         <tt>false</tt> to skip it
      *
      * @throws CurnException on error
      *
      * @see CurnConfig
      * @see FeedInfo
      * @see FeedInfo#getURL
      */
     public boolean runFeedConfigItemPlugIn (String     sectionName,
                                             String     paramName,
                                             CurnConfig config,
                                             FeedInfo   feedInfo)
         throws CurnException
     {
         try
         {
             if (paramName.equals (VAR_SAVE_FEED_AS))
             {
                 handleSaveAsConfigParam(sectionName,
                                         paramName,
                                         config,
                                         feedInfo);
             }
 
             else if (paramName.equals (VAR_SAVE_ONLY))
             {
                 FeedSaveInfo saveInfo = getOrMakeFeedSaveInfo (feedInfo);
                 saveInfo.saveOnly =
                     config.getOptionalBooleanValue (sectionName,
                                                     paramName,
                                                     false);
                 saveInfo.sectionName = sectionName;
                 log.debug ("[" + sectionName + "]: SaveOnly=" +
                            saveInfo.saveOnly);
             }
 
             else if (paramName.equals (VAR_SAVE_AS_ENCODING))
             {
                 String msg =
                     config.getDeprecatedParamMessage(paramName,
                                                      VAR_SAVE_FEED_AS);
                 CurnUtil.getErrorOut().println(msg);
                 log.warn(msg);
 
                 FeedSaveInfo saveInfo = getOrMakeFeedSaveInfo (feedInfo);
                 saveInfo.saveAsEncoding =
                     config.getConfigurationValue (sectionName, paramName);
                 saveInfo.sectionName = sectionName;
                 log.debug ("[" + sectionName + "]: SaveAsEncoding=" +
                            saveInfo.saveAsEncoding);
             }
 
             return true;
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
     }
 
     /**
      * Called after the entire configuration has been read and parsed, but
      * before any feeds are processed. Intercepting this event is useful
      * for plug-ins that want to adjust the configuration. For instance,
      * the <i>curn</i> command-line wrapper intercepts this plug-in event
      * so it can adjust the configuration to account for command line
      * options.
      *
      * @param config  the parsed {@link CurnConfig} object
      *
      * @throws CurnException on error
      *
      * @see CurnConfig
      */
     public void runPostConfigPlugIn (CurnConfig config)
         throws CurnException
     {
         this.config = config;
 
         for (FeedInfo feedInfo : perFeedSaveAsMap.keySet())
         {
             FeedSaveInfo saveInfo = perFeedSaveAsMap.get (feedInfo);
 
             if (saveInfo.saveOnly && (saveInfo.saveAsFile == null))
             {
                 throw new CurnException
                     (Constants.BUNDLE_NAME,
                      "CurnConfig.saveOnlyButNoSaveAs",
                      "Configuration section \"{0}\": " +
                      "\"[1}\" may only be specified if \"{2}\" is set.",
                      new Object[]
                      {
                          saveInfo.sectionName,
                          VAR_SAVE_ONLY,
                          VAR_SAVE_FEED_AS
                      });
             }
         }
     }
 
     /**
      * <p>Called just before a feed is downloaded. This method can return
      * <tt>false</tt> to signal <i>curn</i> that the feed should be
      * skipped. The plug-in method can also set values on the
      * <tt>URLConnection</tt> used to download the plug-in, via
      * <tt>URL.setRequestProperty()</tt>. (Note that <i>all</i> URLs, even
      * <tt>file:</tt> URLs, are passed into this method. Setting a request
      * property on the <tt>URLConnection</tt> object for a <tt>file:</tt>
      * URL will have no effect--though it isn't specifically harmful.)</p>
      *
      * <p>Possible uses for a pre-feed download plug-in include:</p>
      *
      * <ul>
      *   <li>filtering on feed URL to prevent downloading non-matching feeds
      *   <li>changing the default User-Agent value
      *   <li>setting a non-standard HTTP header field
      * </ul>
      *
      * @param feedInfo  the {@link FeedInfo} object for the feed to be
      *                  downloaded
      * @param urlConn   the <tt>java.net.URLConnection</tt> object that will
      *                  be used to download the feed's XML.
      *
      * @return <tt>true</tt> if <i>curn</i> should continue to process the
      *         feed, <tt>false</tt> to skip the feed
      *
      * @throws CurnException on error
      *
      * @see FeedInfo
      */
     public boolean runPreFeedDownloadPlugIn (FeedInfo      feedInfo,
                                              URLConnection urlConn)
         throws CurnException
     {
         boolean processFeed = true;
 
         // If this is a download-only configuration, and there's no
         // save-as file, then we can skip this feed.
 
         if (config.isDownloadOnly())
         {
             FeedSaveInfo saveInfo = perFeedSaveAsMap.get (feedInfo);
 
             if ((saveInfo == null) || (saveInfo.saveAsFile == null))
             {
                 log.debug ("Feed " +
                            feedInfo.getURL().toString() +
                            " has no SaveAs file, and this is a " +
                            " download-only run. Skipping feed.");
                 processFeed = false;
             }
         }
 
         return processFeed;
     }
 
     /**
      * Called immediately after a feed is downloaded. This method can
      * return <tt>false</tt> to signal <i>curn</i> that the feed should be
      * skipped. For instance, a plug-in that filters on the unparsed XML
      * feed content could use this method to weed out non-matching feeds
      * before they are downloaded.
      *
      * @param feedInfo      the {@link FeedInfo} object for the feed that
      *                      has been downloaded
      * @param feedDataFile  the file containing the downloaded, unparsed feed
      *                      XML. <b><i>curn</i> may delete this file after all
      *                      plug-ins are notified!</b>
      * @param encoding      the encoding used to store the data in the file,
      *                      or null for the default
      *
      * @return <tt>true</tt> if <i>curn</i> should continue to process the
      *         feed, <tt>false</tt> to skip the feed. A return value of
      *         <tt>false</tt> aborts all further processing on the feed.
      *         In particular, <i>curn</i> will not pass the feed along to
      *         other plug-ins that have yet to be notified of this event.
      *
      * @throws CurnException on error
      *
      * @see FeedInfo
      */
     public boolean runPostFeedDownloadPlugIn (FeedInfo feedInfo,
                                               File     feedDataFile,
                                               String   encoding)
         throws CurnException
     {
         boolean keepGoing = true;
         FeedSaveInfo saveInfo = perFeedSaveAsMap.get (feedInfo);
 
         if ((saveInfo != null) && (saveInfo.saveAsFile != null))
         {
             try
             {
                 String s = ((encoding == null) ? "default" : encoding);
                 log.debug ("Copying temporary file \"" +
                            feedDataFile.getPath() +
                            "\" (encoding " +
                            s +
                            ") to \"" +
                            saveInfo.saveAsFile.getPath() +
                            "\" (encoding " +
                            saveInfo.saveAsEncoding +
                            ")");
 
                 Writer out =
                     CurnUtil.openOutputFile(saveInfo.saveAsFile,
                                             saveInfo.saveAsEncoding,
                                             CurnUtil.IndexMarker.BEFORE_EXTENSION,
                                             saveInfo.backups);
 
                Reader in;
                if (encoding == null)
                {
                    in = new FileReader(feedDataFile);
                }
                else
                {
                    in = new InputStreamReader
                             (new FileInputStream(feedDataFile), encoding);
                }
                 FileUtil.copyReader(in, out);
                 out.close();
                 in.close();
             }
 
             catch (IOExceptionExt ex)
             {
                 throw new CurnException ("Can't copy \"" +
                                          feedDataFile.getPath() +
                                          "\" to \"" +
                                          saveInfo.saveAsFile.getPath() +
                                          "\": ",
                                          ex);
             }
 
             catch (IOException ex)
             {
                 throw new CurnException ("Can't copy \"" +
                                          feedDataFile.getPath() +
                                          "\" to \"" +
                                          saveInfo.saveAsFile.getPath() +
                                          "\": ",
                                          ex);
             }
 
             keepGoing = ! saveInfo.saveOnly;
         }
 
         return keepGoing;
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     private FeedSaveInfo getOrMakeFeedSaveInfo (FeedInfo feedInfo)
     {
         FeedSaveInfo saveInfo = perFeedSaveAsMap.get (feedInfo);
         if (saveInfo == null)
         {
             saveInfo = new FeedSaveInfo();
             perFeedSaveAsMap.put (feedInfo, saveInfo);
         }
 
         return saveInfo;
     }
 
     private void handleSaveAsConfigParam(final String     sectionName,
                                          final String     paramName,
                                          final CurnConfig config,
                                          final FeedInfo   feedInfo)
         throws CurnException,
                ConfigurationException
     {
         final FeedSaveInfo saveInfo = getOrMakeFeedSaveInfo(feedInfo);
 
         // Parse the value as a command line.
 
         UsageInfo usageInfo = new UsageInfo();
         usageInfo.addOption('b', "backups", "<n>",
                             "Number of backups to keep");
         usageInfo.addOption('e', "encoding", "<encoding>",
                             "Desired output encoding");
         usageInfo.addParameter("<path>", "Path to RSS output file", true);
 
         // Inner class for handling command-line syntax of the value.
 
         class ConfigParameterHandler implements ParameterHandler
         {
             private String rawValue;
 
             ConfigParameterHandler(String rawValue)
             {
                 this.rawValue = rawValue;
             }
 
             public void parseOption(char             shortOption,
                                     String           longOption,
                                     Iterator<String> it)
                 throws CommandLineUsageException,
                        NoSuchElementException
             {
                 String value;
                 switch (shortOption)
                 {
                     case 'b':
                         value = it.next();
                         try
                         {
                             saveInfo.backups = Integer.parseInt(value);
                         }
 
                         catch (NumberFormatException ex)
                         {
                             throw new CommandLineUsageException
                                 ("Section [" + sectionName +
                                  "], parameter \"" + paramName + "\": " +
                                  "Unexpected non-numeric value \"" + value +
                                  "\" for \"" +
                                   UsageInfo.SHORT_OPTION_PREFIX + shortOption +
                                   "\" option.");
                         }
                         break;
 
                     case 'e':
                         saveInfo.saveAsEncoding = it.next();
                         break;
 
                     default:
                         throw new CommandLineUsageException
                             ("Section [" + sectionName +
                              "], parameter \"" + paramName + "\": " +
                              "Unknown option \"" +
                              UsageInfo.SHORT_OPTION_PREFIX + shortOption +
                             "\" in value \"" + rawValue + "\"");
                 }
             }
 
             public void parsePostOptionParameters(Iterator<String> it)
                 throws CommandLineUsageException,
                        NoSuchElementException
             {
                 saveInfo.saveAsFile = CurnUtil.mapConfiguredPathName(it.next());
             }
         };
 
         // Parse the parameters.
 
         ParameterParser paramParser = new ParameterParser(usageInfo);
         String rawValue = config.getConfigurationValue(sectionName, paramName);
         try
         {
             String[] valueTokens = config.getConfigurationTokens(sectionName,
                                                                  paramName);
             if (log.isDebugEnabled())
             {
                 log.debug("[" + sectionName + "]: SaveAsRSS: value=\"" +
                           rawValue + "\", tokens=" +
                           TextUtil.join(valueTokens, '|'));
             }
 
             ConfigParameterHandler handler = new ConfigParameterHandler(rawValue);
             log.debug("Parsing value \"" + rawValue + "\"");
             paramParser.parse(valueTokens, handler);
             log.debug("Section [" + sectionName + "], parameter \"" +
                       paramName + "\": backups=" + saveInfo.backups +
                       ", encoding=" + saveInfo.saveAsEncoding +
                       ", path=" + saveInfo.saveAsFile.getPath());
         }
 
         catch (CommandLineUsageException ex)
         {
             throw new CurnException("Section [" + sectionName +
                                     "], parameter \"" + paramName +
                                     "\": Error parsing value \"" + rawValue +
                                     "\"",
                                     ex);
         }
     }
 }
