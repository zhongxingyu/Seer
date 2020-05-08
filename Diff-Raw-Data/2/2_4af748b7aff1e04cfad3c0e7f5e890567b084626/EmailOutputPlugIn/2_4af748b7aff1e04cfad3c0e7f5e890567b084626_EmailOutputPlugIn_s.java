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
 import org.clapper.curn.MainConfigItemPlugIn;
 import org.clapper.curn.OutputHandler;
 import org.clapper.curn.PreFeedOutputPlugIn;
 import org.clapper.curn.PostOutputPlugIn;
 import org.clapper.curn.Version;
 import org.clapper.curn.parser.RSSItem;
 import org.clapper.curn.parser.RSSChannel;
 
 import org.clapper.util.classutil.ClassUtil;
 import org.clapper.util.config.ConfigurationException;
 import org.clapper.util.logging.Logger;
 import org.clapper.util.mail.EmailMessage;
 import org.clapper.util.mail.EmailTransport;
 import org.clapper.util.mail.SMTPEmailTransport;
 import org.clapper.util.mail.EmailAddress;
 import org.clapper.util.mail.EmailException;
 import org.clapper.util.misc.MIMETypeUtil;
 import org.clapper.util.text.TextUtil;
 
 import java.io.File;
 
 import java.text.DecimalFormat;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 /**
  * The <tt>EmailOutputPlugIn</tt> handles emailing the output from a
  * <i>curn</i> run, if one or more email addresses are specified in the
  * configuration file. It intercepts the following main (<tt>[curn]</tt>)
  * section configuration parameters:
  *
  * <table border="1">
  *   <tr valign="top">
  *     <th align="left">Parameter</th>
  *     <th align="left">Meaning</th>
  *     <th align="left">Default</th>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>MailOutputTo</tt></td>
  *     <td>One or more comma- or blank-separated email addresses to receive
  *         an email containing the output.</td>
  *     <td>None</td>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>MailFrom</tt></td>
  *     <td>The email address to use as the sender of the message.</td>
  *     <td>The user running curn, and the current machine.</td>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>MailSubject</tt></td>
  *     <td>The subject to use for email messages.</td>
  *     <td>"RSS Feeds"</td>
  *   </tr>
  *   <tr valign="top">
  *     <td><tt>MailIndividualArticles</tt></td>
  *     <td>If set to <tt>true</tt> (or <tt>yes</tt> or <tt>1</tt>),
  *         <i>curn</i> will send each RSS article individually--i.e.,
  *         one article per email. Otherwise, it sends all the articles from
  *         all feeds in a single email.</td>
  *     <td><tt>false</tt> (i.e., send one email with all articles)</td>
  *   </tr>
  * </table>
  *
  * @version <tt>$Revision$</tt>
  */
 public class EmailOutputPlugIn
     implements MainConfigItemPlugIn,
                PreFeedOutputPlugIn,
                PostOutputPlugIn
 {
     /*----------------------------------------------------------------------*\
                              Private Constants
     \*----------------------------------------------------------------------*/
 
     private static final String VAR_EMAIL_OUTPUT_TO       = "MailOutputTo";
     private static final String VAR_SMTP_HOST             = "SMTPHost";
     private static final String DEF_SMTP_HOST             = "localhost";
     private static final String VAR_EMAIL_SENDER          = "MailSender";
     private static final String VAR_EMAIL_SUBJECT         = "MailSubject";
     private static final String DEF_EMAIL_SUBJECT         = "RSS Feeds";
     private static final String VAR_MAIL_INDIVIDUAL_ITEMS = "MailIndividualArticles";
 
     /*----------------------------------------------------------------------*\
                             Private Data Items
     \*----------------------------------------------------------------------*/
 
     /**
      * Collection of email addresses
      */
     private Collection<EmailAddress> emailAddresses = null;
 
     /**
      * SMTP host to use
      */
     private String smtpHost = DEF_SMTP_HOST;
 
     /**
      * Email sender address
      */
     private EmailAddress emailSender = null;
 
     /**
      * Email subject
      */
     private String emailSubject = DEF_EMAIL_SUBJECT;
 
     /**
      * For log messages
      */
     private static final Logger log = new Logger (EmailOutputPlugIn.class);
 
     /**
      * If set, mail individual items, one at a time. If not set, mail
      * all items at the end, in one email.
      */
     private boolean mailIndividualItems = false;
 
     /*----------------------------------------------------------------------*\
                                 Constructor
     \*----------------------------------------------------------------------*/
 
     /**
      * Default constructor (required).
      */
     public EmailOutputPlugIn()
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
         return "Email Output";
     }
 
     /**
      * Get the sort key for this plug-in.
      *
      * @return the sort key string.
      */
     public String getPlugInSortKey()
     {
         return ClassUtil.getShortClassName(getClass().getName());
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
      * configuration item in the main [curn] configuration section. All
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
      * @param config       the {@link CurnConfig} object
      *
      * @throws CurnException on error
      *
      * @see CurnConfig
      */
     public void runMainConfigItemPlugIn(String     sectionName,
                                         String     paramName,
                                         CurnConfig config)
         throws CurnException
     {
         try
         {
             if (paramName.equals(VAR_SMTP_HOST))
             {
                 smtpHost = config.getConfigurationValue(sectionName,
                                                         paramName);
             }
 
             else if (paramName.equals(VAR_EMAIL_SENDER))
             {
                 if (emailSender != null)
                 {
                     throw new CurnException
                         (Constants.BUNDLE_NAME,
                          "EmailOutputPlugIn.senderAlreadyDefined",
                          "Section [{0}], configuration item \"{1}\": Email " +
                          "sender has already been defined.",
                          new Object[] {sectionName, paramName});
                 }
 
                 String sender = config.getConfigurationValue(sectionName,
                                                              paramName);
                 try
                 {
                     emailSender = new EmailAddress(sender);
 
                 }
 
                 catch (EmailException ex)
                 {
                     throw new CurnException
                         (Constants.BUNDLE_NAME,
                          "EmailOutputPlugIn.badEmailAddress",
                          "Section [{0}], configuration item \"{1}\": " +
                          "\"{2}\" is an invalid email address",
                          new Object[] {sectionName, paramName, sender},
                          ex);
                 }
             }
 
             else if (paramName.equals(VAR_EMAIL_SUBJECT))
             {
                 emailSubject = config.getConfigurationValue(sectionName,
                                                             paramName);
             }
 
             else if (paramName.equals(VAR_EMAIL_OUTPUT_TO))
             {
                 String addrList = config.getConfigurationValue(sectionName,
                                                                paramName);
                 String[] addrs = TextUtil.split(addrList, ",");
 
                 if ((addrs == null) || (addrs.length == 0))
                 {
                     throw new CurnException
                         (Constants.BUNDLE_NAME,
                          "EmailOutputPlugIn.missingEmailAddresses",
                          "Missing email address(es) in {0} section " +
                          "configuration item \"{1}\"",
                          new Object[] {sectionName, paramName});
                 }
 
                 // Might as well validate them here.
 
                 emailAddresses = new ArrayList<EmailAddress>();
                 for (String addr : addrs)
                 {
                     try
                     {
                         addr = addr.trim();
                         emailAddresses.add(new EmailAddress(addr));
                     }
 
                     catch (EmailException ex)
                     {
                         emailAddresses = null;
                         throw new CurnException
                             (Constants.BUNDLE_NAME,
                              "EmailOutputPlugIn.badEmailAddress",
                              "Section [{0}], configuration item \"{1}\": " +
                              "\"{2}\" is an invalid email address",
                              new Object[] {sectionName, paramName, addr},
                              ex);
                     }
                 }
             }
 
             else if (paramName.equals(VAR_MAIL_INDIVIDUAL_ITEMS))
             {
                 mailIndividualItems = config.getRequiredBooleanValue(sectionName,
                                                                      paramName);
             }
         }
 
         catch (ConfigurationException ex)
         {
             throw new CurnException (ex);
         }
     }
 
     /**
      * Called immediately before a parsed feed is passed to an output
      * handler. This method cannot affect the feed's processing. (The time
      * to stop the processing of a feed is in one of the other, preceding
      * phases.) This method will be called multiple times for each feed if
      * there are multiple output handlers.
      *
      * @param feedInfo      the {@link FeedInfo} object for the feed that
      *                      has been downloaded and parsed.
      * @param channel       the parsed channel data. The plug-in is free
      *                      to edit this data; it's receiving a copy
      *                      that's specific to the output handler.
      * @param outputHandler the {@link OutputHandler} that is about to be
      *                      called. This object is read-only.
      *
      * @throws CurnException on error
      *
      * @see RSSChannel
      * @see FeedInfo
      */
     public void runPreFeedOutputPlugIn(FeedInfo      feedInfo,
                                        RSSChannel    channel,
                                        OutputHandler outputHandler)
         throws CurnException
     {
         if (mailIndividualItems &
             (emailAddresses != null) &
             (emailAddresses.size() > 0))
         {
             // Use the output handler to generate the output. Break the channel
             // into multiple channels with one item each.
 
             RSSChannel newChannel = channel.makeCopy();
             for (RSSItem item : channel.getItems())
             {
                 newChannel.setItems(Collections.singletonList(item));
                outputHandler.displayChannel(channel, feedInfo);
                 outputHandler.flush();
                 emailOutput(Collections.singletonList(outputHandler),
                             emailAddresses);
 
                 // Be sure to reinitialize the output handler, so it can be
                 // used fresh again.
 
                 try
                 {
                     outputHandler.reInit();
                 }
 
                 catch (ConfigurationException ex)
                 {
                     throw new CurnException(ex);
                 }
             }
         }
     }
 
     /**
      * Called after <i>curn</i> has flushed <i>all</i> output handlers. A
      * post-output plug-in is a useful place to consolidate the output from
      * all output handlers. For instance, such a plug-in might pack all the
      * output into a zip file, or email it.
      *
      * @param outputHandlers a <tt>Collection</tt> of the
      *                       {@link OutputHandler} objects (useful for
      *                       obtaining the output files, for instance).
      *
      * @throws CurnException on error
      *
      * @see OutputHandler
      */
     public void runPostOutputPlugIn(Collection<OutputHandler> outputHandlers)
         throws CurnException
     {
         if ((! mailIndividualItems) &
             (emailAddresses != null) &
             (emailAddresses.size() > 0))
         {
             log.debug("There are email addresses.");
             emailOutput(outputHandlers, emailAddresses);
         }
     }
 
     /*----------------------------------------------------------------------*\
                               Private Methods
     \*----------------------------------------------------------------------*/
 
     private void emailOutput(Collection<OutputHandler> outputHandlers,
                              Collection<EmailAddress>  emailAddresses)
         throws CurnException
     {
         try
         {
             OutputHandler firstHandlerWithOutput = null;
             int           totalAttachments = 0;
 
             // First, figure out whether we have any attachments or not.
 
             for (OutputHandler handler : outputHandlers)
             {
                 if (handler.hasGeneratedOutput())
                 {
                     totalAttachments++;
                     if (firstHandlerWithOutput == null)
                         firstHandlerWithOutput = handler;
                 }
             }
 
             if (totalAttachments == 0)
             {
                 // None of the handlers produced any output.
 
                 log.debug("None of the output handlers " +
                           "produced any emailable output.");
             }
 
             else
             {
                 // Create an SMTP transport and a new email message.
 
                 EmailTransport transport = new SMTPEmailTransport(smtpHost);
                 EmailMessage   message = new EmailMessage();
 
                 log.debug ("SMTP host = " + smtpHost);
 
                 // Fill 'er up.
 
                 for (EmailAddress emailAddress : emailAddresses)
                 {
                     try
                     {
                         log.debug("Email recipient = " + emailAddress);
                         message.addTo(emailAddress);
                     }
 
                     catch (EmailException ex)
                     {
                         throw new CurnException(ex);
                     }
                 }
 
                 message.addHeader("X-Mailer",
                                   Version.getInstance().getFullVersion());
                 message.setSubject(emailSubject);
 
                 if (emailSender != null)
                     message.setSender(emailSender);
 
                 if (log.isDebugEnabled())
                     log.debug("Email sender = " + message.getSender());
 
                 // Add the output. If there's only one attachment, and its
                 // output is text, then there's no need for attachments.
                 // Just set it as the text part, and set the appropriate
                 // Content-type: header. Otherwise, make a
                 // multipart-alternative message with separate attachments
                 // for each output.
 
                 DecimalFormat fmt  = new DecimalFormat("##000");
                 StringBuffer  name = new StringBuffer();
                 String        ext;
                 String        contentType;
                 File          file;
 
                 if (totalAttachments == 1)
                 {
                     OutputHandler handler = firstHandlerWithOutput;
                     contentType = handler.getContentType();
                     ext = MIMETypeUtil.fileExtensionForMIMEType(contentType);
                     file = handler.getGeneratedOutput();
                     message.setMultipartSubtype(EmailMessage.MULTIPART_MIXED);
 
                     name.append(fmt.format(1));
                     name.append('.');
                     name.append(ext);
 
                     if (contentType.startsWith("text/"))
                         message.setText(file, name.toString(), contentType);
                     else
                         message.addAttachment(file,
                                               name.toString(),
                                               contentType);
                 }
 
                 else
                 {
                     message.setMultipartSubtype
                                           (EmailMessage.MULTIPART_ALTERNATIVE);
 
                     int i = 1;
                     for (OutputHandler handler : outputHandlers)
                     {
                         contentType = handler.getContentType();
                         ext = MIMETypeUtil.fileExtensionForMIMEType
                                                                 (contentType);
                         file = handler.getGeneratedOutput();
                         if (file != null)
                         {
                             name.setLength(0);
                             name.append(fmt.format(i));
                             name.append('.');
                             name.append(ext);
                             i++;
                             message.addAttachment(file,
                                                   name.toString(),
                                                   contentType);
                         }
                     }
                 }
 
                 log.debug("Sending message.");
                 transport.send(message);
                 message.clear();
             }
         }
 
         catch (EmailException ex)
         {
             throw new CurnException (ex);
         }
     }
 }
