 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.contrib.mailarchive.internal;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 import java.util.zip.GZIPOutputStream;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 import javax.mail.Flags;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Part;
 import javax.mail.Session;
 import javax.mail.internet.MimeBodyPart;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.xwiki.bridge.DocumentAccessBridge;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.manager.ComponentManager;
 import org.xwiki.component.phase.Initializable;
 import org.xwiki.component.phase.InitializationException;
 import org.xwiki.context.Execution;
 import org.xwiki.context.ExecutionContext;
 import org.xwiki.contrib.mail.ConnectionErrors;
 import org.xwiki.contrib.mail.IMailComponent;
 import org.xwiki.contrib.mail.MailContent;
 import org.xwiki.contrib.mail.MailItem;
 import org.xwiki.contrib.mail.Utils;
 import org.xwiki.contrib.mail.internal.JavamailMessageParser;
 import org.xwiki.contrib.mail.internal.MailAttachment;
 import org.xwiki.contrib.mailarchive.IMailArchive;
 import org.xwiki.contrib.mailarchive.IMailingList;
 import org.xwiki.contrib.mailarchive.IServer;
 import org.xwiki.contrib.mailarchive.IType;
 import org.xwiki.contrib.mailarchive.LoadingSession;
 import org.xwiki.contrib.mailarchive.internal.data.Factory;
 import org.xwiki.contrib.mailarchive.internal.data.MailArchiveConfiguration;
 import org.xwiki.contrib.mailarchive.internal.data.MailDescriptor;
 import org.xwiki.contrib.mailarchive.internal.data.TopicDescriptor;
 import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
 import org.xwiki.contrib.mailarchive.internal.persistence.IPersistence;
 import org.xwiki.contrib.mailarchive.internal.persistence.XWikiPersistence;
 import org.xwiki.contrib.mailarchive.internal.threads.MessagesThreader;
 import org.xwiki.contrib.mailarchive.internal.threads.ThreadableMessage;
 import org.xwiki.contrib.mailarchive.internal.timeline.TimeLine;
 import org.xwiki.contrib.mailarchive.internal.utils.MailUtils;
 import org.xwiki.contrib.mailarchive.internal.utils.TextUtils;
 import org.xwiki.environment.Environment;
 import org.xwiki.logging.LoggerManager;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 import org.xwiki.rendering.parser.StreamParser;
 import org.xwiki.rendering.renderer.PrintRendererFactory;
 import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
 import org.xwiki.rendering.renderer.printer.WikiPrinter;
 import org.xwiki.rendering.syntax.Syntax;
 
 import ch.qos.logback.classic.Level;
 
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.util.Util;
 
 /**
  * Implementation of a <tt>IMailArchive</tt> component.
  */
 @Component
 @Singleton
 public class DefaultMailArchive implements IMailArchive, Initializable
 {
 
     /**
      * Name of the space that contains end-user targeted pages.
      */
    public static final String SPACE_HOME = "MailArchive";
 
     /**
      * Name of the space that contains technical code.
      */
     public static final String SPACE_CODE = "MailArchiveCode";
 
     /**
      * Name of the space that contains configuration / preferences
      */
     public static final String SPACE_PREFS = "MailArchivePrefs";
 
     /**
      * Name of the space that contains created objects
      */
     public static String SPACE_ITEMS = "MailArchiveItems";
 
     /**
      * XWiki profile name of a non-existing user.
      */
     public static final String UNKNOWN_USER = "XWiki.UserDoesNotExist";
 
     public boolean isConfigured = false;
 
     // Components injected by the Component Manager
 
     /** Provides access to documents. */
     @Inject
     private DocumentAccessBridge dab;
 
     /** Provides access to the request context. */
     @Inject
     public Execution execution;
 
     /** Provides access to execution environment and from it to context and old core */
     @Inject
     private Environment environment;
 
     /**
      * Secure query manager that performs checks on rights depending on the query being executed.
      */
     // TODO : @Requirement("secure") ??
     @Inject
     private QueryManager queryManager;
 
     /** Provides access to log facility */
     @Inject
     Logger logger;
 
     @Inject
     LoggerManager loggerManager;
 
     /**
      * The component used to parse XHTML obtained after cleaning, when transformations are not executed.
      */
     @Inject
     @Named("html/4.01")
     private StreamParser htmlStreamParser;
 
     /**
      * The component manager. We need it because we have to access some components dynamically based on the input
      * syntax.
      */
     @Inject
     private ComponentManager componentManager;
 
     /** Provides access to low-level mail api component */
     @Inject
     private IMailComponent mailManager;
 
     // Other global objects
 
     /** The XWiki context */
     private XWikiContext context;
 
     // TODO remove dependency to old core
     /** The XWiki old core */
     private XWiki xwiki;
 
     /** Provides access to Mail archive configuration items */
     private ItemsManager store;
 
     /** Factory to convert raw conf to POJO */
     private Factory factory;
 
     /** Provides access to the Mail archive configuration */
     private IMailArchiveConfiguration config;
 
     /** Used to persist pages for mails and topics */
     private IPersistence persistence;
 
     /** Some utilities */
     public MailUtils mailutils;
 
     /** Already archived topics, loaded from database */
     private HashMap<String, TopicDescriptor> existingTopics;
 
     /** Already archived messages, loaded from database */
     private HashMap<String, MailDescriptor> existingMessages;
 
     /** Is the component initialized ? */
     private boolean isInitialized = false;
 
     /** Are we currently in a loading session ? */
     private boolean inProgress = false;
 
     private Level logLevel;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.component.phase.Initializable#initialize()
      */
     @Override
     public void initialize() throws InitializationException
     {
         try {
             logger.debug("initialize()");
             ExecutionContext context = execution.getContext();
             this.context = (XWikiContext) context.getProperty("xwikicontext");
             this.xwiki = this.context.getWiki();
             this.factory = new Factory(dab);
             this.store = new ItemsManager(queryManager, logger);
             this.persistence = new XWikiPersistence(this.context, this.xwiki, this.logger);
             logger.info("Mail archive initiliazed !");
             logger.debug("PERMANENT DATA DIR : " + this.environment.getPermanentDirectory());
         } catch (Throwable e) {
             logger.error("Could not initiliaze mailarchive ", e);
             e.printStackTrace();
         }
 
         this.isInitialized = true;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.IMailArchive#checkMails()
      */
     @Override
     public int queryServerInfo(String serverPrefsDoc)
     {
         // Retrieve connection properties from prefs
         IServer server = factory.createMailServer(serverPrefsDoc);
         if (server == null) {
             logger.warn("Could not retrieve server information from wiki page " + serverPrefsDoc);
             return ConnectionErrors.INVALID_PREFERENCES.getCode();
         }
 
         return queryServerInfo(server);
     }
 
     /**
      * @param server
      * @return
      */
     public int queryServerInfo(IServer server)
     {
         logger.info("Checking server " + server);
 
         int nbMessages =
             mailManager.check(server.getHost(), server.getPort(), server.getProtocol(), server.getFolder(),
                 server.getUser(), server.getPassword(), server.getAdditionalProperties(), true);
         logger.debug("check of server " + server + " returned " + nbMessages);
 
         // Persist connection state
 
         try {
             persistence.updateServerState(server.getWikiDoc(), nbMessages);
         } catch (Exception e) {
             logger.info("Failed to persist server connection state", e);
         }
 
         return nbMessages;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.IMailArchive#computeThreads(java.lang.String)
      */
     public ThreadableMessage computeThreads(String topicId)
     {
         MessagesThreader threads = new MessagesThreader(queryManager, logger);
 
         try {
             if (topicId == null) {
                 return threads.thread();
             } else {
                 return threads.thread(topicId);
             }
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.IMailArchive#loadMails(org.xwiki.contrib.mailarchive.LoadingSession)
      */
     @Override
     public int loadMails(LoadingSession session)
     {
         if (session.isDebugMode()) {
             enterDebugMode();
         }
         logger.info("Starting new MAIL loading session...");
         int nbMessages = 0;
         int currentMsg = 0;
         int nbSuccess = 0;
 
         if (inProgress) {
             logger.warn("Loading process already in progress ...");
             return -1;
         }
 
         inProgress = true;
         try {
             configure();
             loadItems();
 
             List<IServer> servers = null;
             if (!StringUtils.isBlank(session.getServerPrefsDoc())) {
                 IServer server = factory.createMailServer(session.getServerPrefsDoc());
                 if (server != null) {
                     servers = new ArrayList<IServer>();
                     servers.add(server);
                 }
             } else {
                 servers = config.getServers();
             }
 
             for (IServer server : servers) {
                 logger.info("[{}] Loading mails from server", server.getId());
 
                 List<Message> messages;
                 try {
                     messages = fetchMailsFromServer(server);
                 } catch (Exception e1) {
                     logger.info("[{}] Can't retrieve messages from server", server.getId());
                     messages = new ArrayList<Message>();
                 }
                 logger.info("[{}] Number of messages to treat : {}", new Object[] {server.getId(), messages.size()});
                 currentMsg = 0;
                 LoadingSessionResult result = null;
                 while ((currentMsg < session.getLimit() || session.getLimit() < 0) && currentMsg < messages.size()) {
                     logger.debug("[{}] Loading message {}/{}",
                         new Object[] {server.getId(), currentMsg, Math.min(messages.size(), session.getLimit())});
                     try {
                         Message message = messages.get(currentMsg);
                         try {
                             logger.debug("DEBUG MODE ? " + session.isDebugMode());
                             if (session.isDebugMode() || true) {
                                 try {
                                     final String id =
                                         JavamailMessageParser.extractSingleHeader(message, "Message-ID").replaceAll(
                                             "[^a-zA-Z0-9-_\\.]", "_");
                                     FileOutputStream fo = new FileOutputStream(id + ".eml");
                                     message.writeTo(fo);
                                     fo.flush();
                                     fo.close();
 
                                     logger.debug("Message dumped into " + id + ".eml");
                                 } catch (Throwable t) {
                                     // we catch Throwable because we don't want to cause problems in debug mode
                                     logger.debug("Could not dump message for debug", t);
                                 }
                             }
                             result = loadMail(message, !session.isSimulationMode(), false, null);
                         } catch (Exception me) {
                             if (me instanceof MessagingException || me instanceof IOException) {
                                 logger.debug("[" + server.getId() + "] Could not load email because of", me);
                                 logger.info("[{}] Could not load email, trying to load a clone", server.getId());
                                 // Retrieve the Session object established during server connection
                                 Session jsession =
                                     mailManager.getSession(server.getHost(), server.getPort(), server.getProtocol(),
                                         server.getFolder(), server.getUser());
                                 Message clone = mailManager.cloneEmail(message, jsession);
                                 message = clone;
                                 if (message != null) {
                                     result = loadMail(message, !session.isSimulationMode(), false, null);
                                 }
                             }
 
                         }
                         if (result != null && result.isSuccess()) {
                             nbSuccess++;
                             if (!session.isSimulationMode()) {
                                 message.setFlag(Flags.Flag.SEEN, true);
 
                                 if (config.isUseStore()) {
                                     try {
                                         mailManager.writeToStore(server.getFolder(), messages.get(currentMsg));
                                     } catch (MessagingException e) {
                                         logger.error("Can't copy mail to local store", e);
                                     }
                                 }
                             }
                         }
                     } catch (Exception e) {
                         logger.warn("Failed to load mail", e);
                     }
                     currentMsg++;
                 }
 
                 nbMessages += currentMsg;
             }
 
             try {
                 // Compute timeline feed
                 if (config.isManageTimeline() && nbMessages > 0) {
                     TimeLine timeline = new TimeLine(config, xwiki, context, queryManager, logger);
                     String timelineFeed = timeline.compute();
                     if (!StringUtils.isBlank(timelineFeed)) {
                         File timelineFeedLocation =
                             new File(environment.getPermanentDirectory(), "mailarchive/timeline");
                         FileWriter fw =
                             new FileWriter(timelineFeedLocation.getAbsolutePath() + "/TimeLineFeed.xml", false);
                         fw.write(timelineFeed);
                         fw.close();
                     }
                 }
             } catch (Exception e) {
                 logger.warn("Could not compute timeline data", e);
             }
 
         } catch (MailArchiveException e) {
             logger.warn("EXCEPTION ", e);
             return -1;
         } catch (InitializationException e) {
             logger.warn("EXCEPTION ", e);
             return -1;
         } finally {
             inProgress = false;
             this.existingMessages = null;
             this.existingTopics = null;
             if (session.isDebugMode()) {
                 quitDebugMode();
             }
         }
 
         inProgress = false;
         return nbSuccess;
 
     }
 
     public String computeTimeline() throws XWikiException, InitializationException, MailArchiveException
     {
         configure();
         TimeLine timeline = new TimeLine(config, xwiki, context, queryManager, logger);
         return timeline.compute();
     }
 
     /**
      * @throws InitializationException
      * @throws MailArchiveException
      */
     protected void configure() throws InitializationException, MailArchiveException
     {
         // Init
         if (!this.isInitialized) {
             initialize();
         }
 
         config =
             new MailArchiveConfiguration(SPACE_PREFS + ".GlobalParameters", this.context, this.queryManager,
                 this.logger, this.factory);
 
         if (config.getItemsSpaceName() != null && !"".equals(config.getItemsSpaceName())) {
             SPACE_ITEMS = config.getItemsSpaceName();
         }
         if (config.isUseStore()) {
             File maStoreLocation = new File(environment.getPermanentDirectory(), "mailarchive/storage");
             logger.info("Local Store Location: " + maStoreLocation.getAbsolutePath());
             logger.info("Local Store Provider: mstor");
             mailManager.setStore(maStoreLocation.getAbsolutePath(), "mstor");
         }
 
         mailutils = new MailUtils(xwiki, context, logger, queryManager);
         TextUtils.setLogger(this.logger);
 
         this.isConfigured = true;
     }
 
     protected void loadItems() throws MailArchiveException
     {
         existingTopics = store.loadStoredTopics();
         existingMessages = store.loadStoredMessages();
     }
 
     public IType getType(String name)
     {
         return config.getMailTypes().get(name);
     }
 
     /**
      * @param m
      */
     public void setMailSpecificParts(final MailItem m)
     {
         // Types
         List<IType> foundTypes = extractTypes(config.getMailTypes().values(), m);
         foundTypes.remove(getType(IType.TYPE_MAIL));
         if (foundTypes.size() > 0) {
             m.setType(foundTypes.get(0).getName());
         } else {
             m.setType(IType.TYPE_MAIL);
         }
 
         // User
         String userwiki = null;
         if (config.isMatchProfiles()) {
             userwiki = mailutils.parseUser(m.getFrom(), config.isMatchLdap());
         }
         if (StringUtils.isBlank(userwiki)) {
             userwiki = UNKNOWN_USER;
         }
         m.setWikiuser(userwiki);
 
         // Compatibility: crop ids
         if (config.isCropTopicIds() && m.getTopicId().length() >= 30) {
             m.setTopicId(m.getTopicId().substring(0, 29));
         }
     }
 
     /**
      * Find matching types for this mail.
      * 
      * @param m
      */
     protected List<IType> extractTypes(final Collection<IType> types, final MailItem m)
     {
         List<IType> result = new ArrayList<IType>();
 
         if (types == null || m == null) {
             throw new IllegalArgumentException("extractTypes: Types and mailitem can't be null");
         }
 
         // set IType
         for (IType type : types) {
             logger.info("Checking for type " + type);
             boolean matched = true;
             for (Entry<List<String>, String> entry : type.getPatterns().entrySet()) {
                 logger.info("  Checking for entry " + entry);
                 List<String> fields = entry.getKey();
                 String regexp = entry.getValue();
                 Pattern pattern = null;
                 try {
                     pattern = Pattern.compile(regexp);
                 } catch (PatternSyntaxException e) {
                     logger.warn("Invalid Pattern " + regexp + "can't be compiled, skipping this mail type");
                     break;
                 }
                 Matcher matcher = null;
                 boolean fieldMatch = false;
                 for (String field : fields) {
                     logger.info("  Checking field " + field);
                     String fieldValue = "";
                     if ("from".equals(field)) {
                         fieldValue = m.getFrom();
                     } else if ("to".equals(field)) {
                         fieldValue = m.getTo();
                     } else if ("cc".equals(field)) {
                         fieldValue = m.getCc();
                     } else if ("subject".equals(field)) {
                         fieldValue = m.getSubject();
                     }
                     matcher = pattern.matcher(fieldValue);
                     if (matcher != null) {
                         fieldMatch = matcher.find();
                     }
                     if (fieldMatch) {
                         logger.info("Field " + field + " value [" + fieldValue + "] matches pattern [" + regexp + "]");
                         break;
                     }
                 }
                 matched = matched && fieldMatch;
             }
             if (matched) {
                 logger.info("Matched type " + type.getDisplayName());
                 result.add(type);
                 matched = true;
             }
         }
         return result;
     }
 
     @Override
     public String parseUser(String internetAddress)
     {
         logger.debug("parseUser {}", internetAddress);
         try {
             configure();
         } catch (Exception e) {
             logger.warn("parseUser: failed to configure the Mail Archive", e);
             return null;
         }
         String user = mailutils.parseUser(internetAddress, config.isMatchLdap());
         logger.debug("parseUser return {}", user);
         return user;
     }
 
     /**
      * @param mail
      * @param confirm
      * @param isAttachedMail
      * @param parentMail
      * @return
      * @throws XWikiException
      * @throws ParseException
      * @throws IOException
      * @throws MessagingException
      */
     public LoadingSessionResult loadMail(Part mail, boolean confirm, boolean isAttachedMail, String parentMail)
         throws Exception, ParseException, MessagingException, IOException
     {
         MailItem m = null;
 
         m = mailManager.parseHeaders(mail);
         setMailSpecificParts(m);
         // Compatibility option with old version of the mail archive
         if (config.isCropTopicIds() && m.getTopicId().length() > 30) {
             m.setTopicId(m.getTopicId().substring(0, 29));
         }
         logger.warn("PARSED MAIL  " + m);
 
         return loadMail(m, confirm, isAttachedMail, parentMail);
     }
 
     /**
      * @param existingTopics
      * @param existingMessages
      * @param message
      * @param confirm
      * @param isAttachedMail
      * @param parentMail
      * @throws XWikiException
      * @throws ParseException
      */
     public LoadingSessionResult loadMail(MailItem m, boolean confirm, boolean isAttachedMail, String parentMail)
         throws Exception, ParseException
     {
         XWikiDocument msgDoc = null;
         XWikiDocument topicDoc = null;
         String topicDocName = null;
 
         logger.debug("Loading mail content into wiki objects");
 
         // set loading user for rights - loading user must have edit rights on IMailArchive and MailArchiveCode spaces
         context.setUser(config.getLoadingUser());
         logger.debug("Loading user " + config.getLoadingUser() + " set in context");
 
         SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ", m.getLocale());
 
         // Create a new topic if needed
         String existingTopicId = "";
         // we don't create new topics for attached emails
         if (!isAttachedMail) {
             existingTopicId = existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId());
             if (existingTopicId == null) {
                 logger.debug("  did not find existing topic, creating a new one");
                 if (existingTopics.containsKey(m.getTopicId())) {
                     logger.debug("  new topic but topicId already loaded, using messageId as new topicId");
                     m.setTopicId(m.getMessageId());
                 }
                 existingTopicId = m.getTopicId();
                 topicDocName = createTopicPage(m, dateFormatter, confirm);
 
                 // FIXME: to be removed when everything has been moved to IPersistence/XWikiPersistence
                 topicDoc = xwiki.getDocument(topicDocName, context);
 
                 logger.debug("  loaded new topic " + topicDoc);
             } else if (TextUtils.similarSubjects(m.getTopic(), existingTopics.get(existingTopicId).getSubject())) {
                 logger.debug("  topic already loaded " + m.getTopicId() + " : " + existingTopics.get(existingTopicId));
                 topicDoc = updateTopicPage(m, existingTopicId, dateFormatter, confirm);
             } else {
                 // We consider this was a topic hack : someone replied to an existing thread, but to start on another
                 // subject.
                 // In this case, we split, use messageId as a new topic Id, and set replyToId to empty string in order
                 // to treat this as a new topic to create.
                 // In order for this new thread to be correctly threaded, we search for existing topic with this new
                 // topicId,
                 // so now all new mails in this case will be attached to this new topic.
                 logger.debug("  found existing topic but subjects are too different, using new messageid as topicid ["
                     + m.getMessageId() + "]");
                 m.setTopicId(m.getMessageId());
                 m.setReplyToId("");
                 existingTopicId = existsTopic(m.getTopicId(), m.getTopic(), m.getReplyToId(), m.getMessageId());
                 logger.debug("  creating new topic");
                 topicDocName = createTopicPage(m, dateFormatter, confirm);
 
                 // FIXME: to be removed when everything has been moved to IPersistence/XWikiPersistence
                 topicDoc = xwiki.getDocument(topicDocName, context);
             }
         } // if not attached email
 
         // Create a new message if needed
         if (!existingMessages.containsKey(m.getMessageId())) {
             logger.debug("creating new message " + m.getMessageId() + " ...");
             /*
              * Note : use already existing topic id if any, instead of the one from the message, to keep an easy to
              * parse link between thread messages
              */
             if ("".equals(existingTopicId)) {
                 existingTopicId = m.getTopicId();
             }
             // Note : correction bug of messages linked to same topic but with different topicIds
             m.setTopicId(existingTopicId);
             try {
                 msgDoc = createMailPage(m, existingTopicId, isAttachedMail, parentMail, confirm);
             } catch (Exception e) {
                 logger.error("Could not create mail page for " + m.getMessageId(), e);
                 return new LoadingSessionResult(false, topicDoc != null ? topicDoc.getFullName() : null, null);
             }
 
             return new LoadingSessionResult(true, topicDoc != null ? topicDoc.getFullName() : null, msgDoc != null
                 ? msgDoc.getFullName() : null);
         } else {
             // message already loaded
             logger.debug("Mail already loaded - checking for updates ...");
 
             MailDescriptor msg = existingMessages.get(m.getMessageId());
             logger.debug("TopicId of existing message " + msg.getTopicId() + " and of topic " + existingTopicId
                 + " are different ?" + (!msg.getTopicId().equals(existingTopicId)));
             if (!msg.getTopicId().equals(existingTopicId)) {
                 msgDoc = xwiki.getDocument(existingMessages.get(m.getMessageId()).getFullName(), context);
                 BaseObject msgObj = msgDoc.getObject(SPACE_CODE + ".MailClass");
                 msgObj.set("topicid", existingTopicId, context);
                 if (confirm) {
                     logger.debug("saving message " + m.getSubject());
                     saveAsUser(msgDoc, null, config.getLoadingUser(), "Updated mail with existing topic id found");
                 }
             }
 
             return new LoadingSessionResult(true, topicDoc != null ? topicDoc.getFullName() : null, msgDoc != null
                 ? msgDoc.getFullName() : null);
         }
     }
 
     /**
      * createTopicPage Creates a wiki page for a Topic.
      * 
      * @throws XWikiException
      */
     protected String createTopicPage(MailItem m, SimpleDateFormat dateFormatter, boolean create) throws Exception
     {
         String pageName = "T" + m.getTopic().replaceAll(" ", "");
 
         // Materialize mailing-lists information and mail IType in Tags
         ArrayList<String> taglist = extractMailingListsTags(m);
         taglist.add(m.getType());
 
         String createdTopicName = persistence.createTopic(pageName, m, taglist, config.getLoadingUser(), create);
 
         // add the existing topic created to the map
         existingTopics.put(m.getTopicId(), new TopicDescriptor(createdTopicName, m.getTopic()));
 
         return createdTopicName;
     }
 
     /**
      * updateTopicPage Update topic against new mail taking part to existing topic.
      */
     /**
      * @param m
      * @param existingTopicId
      * @param dateFormatter
      * @param create
      * @return
      * @throws XWikiException
      * @throws ParseException
      */
     public XWikiDocument updateTopicPage(MailItem m, String existingTopicId, SimpleDateFormat dateFormatter,
         boolean create) throws XWikiException, ParseException
     {
         logger.debug("updateTopicPage(" + existingTopicId + ")");
 
         String newuser = mailutils.parseUser(m.getFrom(), config.isMatchLdap());
         if (newuser == null || "".equals(newuser)) {
             newuser = UNKNOWN_USER;
         }
         XWikiDocument topicDoc = xwiki.getDocument(existingTopics.get(existingTopicId).getFullName(), context);
         logger.debug("Existing topic " + topicDoc);
         BaseObject topicObj = topicDoc.getObject(SPACE_CODE + ".MailTopicClass");
         Date lastupdatedate = topicObj.getDateValue("lastupdatedate");
         Date startdate = topicObj.getDateValue("startdate");
         String originalAuthor = topicObj.getStringValue("author");
         if (lastupdatedate == null || "".equals(lastupdatedate)) {
             lastupdatedate = m.getDate();
         } // note : this should never occur
         if (startdate == null || "".equals(startdate)) {
             startdate = m.getDate();
         }
 
         boolean isMoreRecent = (m.getDate().getTime() > lastupdatedate.getTime());
         boolean isMoreAncient = (m.getDate().getTime() < startdate.getTime());
         logger.debug("mail date = " + m.getDate().getTime() + ", last update date = " + lastupdatedate.getTime()
             + ", is more recent = " + isMoreRecent + ", is more ancient = " + isMoreAncient + ", first in topic = "
             + m.isFirstInTopic());
 
         // If the first one, we add the startdate to existing topic
         if (m.isFirstInTopic() || isMoreRecent) {
             boolean dirty = false;
             logger.debug("Checking if existing topic has to be updated ...");
             String comment = "";
             // if (m.isFirstInTopic) {
             if ((!originalAuthor.equals(m.getFrom()) && isMoreAncient) || "".equals(originalAuthor)) {
                 logger.debug("     updating author from " + originalAuthor + " to " + m.getFrom());
                 topicObj.set("author", m.getFrom(), context);
                 comment += " Updated author ";
                 dirty = true;
             }
             logger.debug("     existing startdate " + topicObj.getDateValue("startdate"));
             if ((topicObj.getStringValue("startdate") == null || "".equals(topicObj.getStringValue("startdate")))
                 || isMoreAncient) {
                 logger.debug("     checked startdate not already added to topic");
                 topicObj.set("startdate", m.getDate(), context);
                 topicDoc.setCreationDate(m.getDate());
                 comment += " Updated start date ";
                 dirty = true;
             }
             // }
             if (isMoreRecent) {
                 logger.debug("     updating lastupdatedate from " + lastupdatedate + " to "
                     + dateFormatter.format(m.getDate()));
                 topicObj.set("lastupdatedate", m.getDate(), context);
                 topicDoc.setDate(m.getDate());
                 topicDoc.setContentUpdateDate(m.getDate());
 
                 comment += " Updated last update date ";
                 dirty = true;
             }
             topicDoc.setComment(comment);
 
             if (create && dirty) {
                 logger.debug("     Updated existing topic");
                 saveAsUser(topicDoc, newuser, config.getLoadingUser(), comment);
             }
             existingTopics.put(m.getTopicId(),
                 new TopicDescriptor(topicDoc.getFullName(), topicObj.getStringValue("subject")));
         } else {
             logger.debug("     Nothing to update in topic");
         }
 
         // return topicDoc
 
         return null;
     }
 
     /**
      * createMailPage Creates a wiki page for a Mail.
      * 
      * @throws XWikiException
      * @throws IOException
      * @throws MessagingException
      */
     protected XWikiDocument createMailPage(MailItem m, String existingTopicId, boolean isAttachedMail,
         String parentMail, boolean create) throws XWikiException, MessagingException, IOException
     {
         logger.debug("createMailPage(" + m + "," + existingTopicId + "," + isAttachedMail + "," + parentMail + ","
             + create + ")");
 
         XWikiDocument msgDoc;
         String content = "";
         String htmlcontent = "";
         String zippedhtmlcontent = "";
         List<Message> attachedMails = new ArrayList<Message>();
         List<String> attachedMailsPages = new ArrayList<String>();
         // a map to store attachment filename = contentId for replacements in HTML retrieved from mails
         HashMap<String, String> attachmentsMap = new HashMap<String, String>();
         ArrayList<MimeBodyPart> attbodyparts = new ArrayList<MimeBodyPart>();
 
         char prefix = 'M';
         if (isAttachedMail) {
             prefix = 'A';
         }
         String msgwikiname = xwiki.clearName(prefix + m.getTopic().replaceAll(" ", ""), context);
         if (msgwikiname.length() >= XWikiPersistence.MAX_PAGENAME_LENGTH) {
             msgwikiname = msgwikiname.substring(0, XWikiPersistence.MAX_PAGENAME_LENGTH);
         }
         String pagename = xwiki.getUniquePageName(SPACE_ITEMS, msgwikiname, context);
         msgDoc = xwiki.getDocument(SPACE_ITEMS + '.' + pagename, context);
         logger.debug("NEW MSG msgwikiname=" + msgwikiname + " pagename=" + pagename);
 
         Object bodypart = m.getBodypart();
         logger.debug("bodypart class " + bodypart.getClass());
         // addDebug("mail content type " + m.contentType)
         // Retrieve mail body(ies)
         logger.debug("Fetching mail content");
         MailContent mailContent = mailManager.parseContent(m.getOriginalMessage());
 
         logger.debug("Loading attached mails ...");
         attachedMails = mailContent.getAttachedMails();
         for (Message message : attachedMails) {
             try {
                 LoadingSessionResult result = loadMail(message, create, true, msgDoc.getFullName());
                 if (result.isSuccess()) {
                     attachedMailsPages.add(result.getCreatedMailDocumentName());
                 }
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
 
         content = mailContent.getText();
         htmlcontent = mailContent.getHtml();
 
         // Truncate body
         content = TextUtils.truncateStringForBytes(content, 65500, 65500);
 
         // Treat Html part
         zippedhtmlcontent = treatHtml(msgDoc, htmlcontent, mailContent.getWikiAttachments());
 
         // Treat lengths
         m.setMessageId(TextUtils.truncateForString(m.getMessageId()));
         m.setSubject(TextUtils.truncateForString(m.getSubject()));
         existingTopicId = TextUtils.truncateForString(existingTopicId);
         m.setTopicId(TextUtils.truncateForString(m.getTopicId()));
         m.setTopic(TextUtils.truncateForString(m.getTopic()));
         m.setReplyToId(TextUtils.truncateForLargeString(m.getReplyToId()));
         m.setRefs(TextUtils.truncateForLargeString(m.getRefs()));
         m.setFrom(TextUtils.truncateForLargeString(m.getFrom()));
         m.setTo(TextUtils.truncateForLargeString(m.getTo()));
         m.setCc(TextUtils.truncateForLargeString(m.getCc()));
 
         // Assign text body converted from html content if there is no pure-text content
         if (StringUtils.isBlank(content) && !StringUtils.isBlank(htmlcontent)) {
             String converted = null;
             try {
 
                 WikiPrinter printer = new DefaultWikiPrinter();
                 PrintRendererFactory printRendererFactory =
                     componentManager.lookup(PrintRendererFactory.class, Syntax.PLAIN_1_0.toIdString());
                 htmlStreamParser.parse(new StringReader(htmlcontent), printRendererFactory.createRenderer(printer));
 
                 converted = printer.toString();
 
             } catch (Throwable t) {
                 logger.warn("Conversion from HTML to plain text thrown exception", t);
                 converted = null;
             }
             if (converted != null && !"".equals(converted)) {
                 // replace content with value (remove excessive whitespace also)
                 content = converted.replaceAll("[\\s]{2,}", "\n");
                 logger.debug("Text body now contains converted html content");
             } else {
                 logger.debug("Conversion from HTML to Plain Text returned empty or null string");
             }
         }
 
         // Fill all new object's fields
         BaseObject msgObj = msgDoc.newObject(SPACE_CODE + ".MailClass", context);
         msgObj.set("messageid", m.getMessageId(), context);
         msgObj.set("messagesubject", m.getSubject(), context);
 
         msgObj.set("topicid", existingTopicId, context);
         msgObj.set("topicsubject", m.getTopic(), context);
         msgObj.set("inreplyto", m.getReplyToId(), context);
         msgObj.set("references", m.getRefs(), context);
         msgObj.set("date", m.getDate(), context);
         msgDoc.setCreationDate(m.getDate());
         msgDoc.setDate(m.getDate());
         msgDoc.setContentUpdateDate(m.getDate());
         msgObj.set("from", m.getFrom(), context);
         msgObj.set("to", m.getTo(), context);
         msgObj.set("cc", m.getCc(), context);
         msgObj.set("body", content, context);
         msgObj.set("bodyhtml", zippedhtmlcontent, context);
         msgObj.set("sensitivity", m.getSensitivity(), context);
         if (attachedMails.size() != 0) {
             msgObj.set("attachedMails", StringUtils.join(attachedMailsPages, ','), context);
         }
         if (!isAttachedMail) {
             msgObj.set("type", m.getType(), context);
         } else {
             msgObj.set("type", "Attached Mail", context);
         }
         if (parentMail != null) {
             msgDoc.setParent(parentMail);
         } else if (existingTopics.get(m.getTopicId()) != null) {
             msgDoc.setParent(existingTopics.get(m.getTopicId()).getFullName());
         }
         msgDoc.setTitle("Message " + m.getSubject());
         if (!isAttachedMail) {
             msgDoc.setComment("Created message");
         } else {
             msgDoc.setComment("Attached mail created");
         }
         ArrayList<String> taglist = extractMailingListsTags(m);
         if (taglist.size() > 0) {
             BaseObject tagobj = msgDoc.newObject("XWiki.TagClass", context);
             String tags = StringUtils.join(taglist.toArray(new String[] {}), ',');
             tagobj.set("tags", tags.replaceAll(" ", "_"), context);
         }
 
         if (create && !checkMsgIdExistence(m.getMessageId())) {
             logger.debug("saving message " + m.getSubject());
             saveAsUser(msgDoc, m.getWikiuser(), config.getLoadingUser(), "Created message from mailing-list");
         }
         existingMessages.put(m.getMessageId(),
             new MailDescriptor(m.getSubject(), existingTopicId, msgDoc.getFullName()));
         logger.debug("  mail loaded and saved :" + msgDoc.getFullName());
         logger.debug("adding attachments to document");
         addAttachmentsFromMail(msgDoc, attbodyparts, attachmentsMap);
 
         return msgDoc;
     }
 
     /*
      * Cleans up HTML content and treat it to replace cid tags with correct image urls (targeting attachments), then zip
      * it.
      */
     String treatHtml(XWikiDocument msgdoc, String htmlcontent, HashMap<String, MailAttachment> attachmentsMap)
         throws IOException
     {
         if (!StringUtils.isBlank(htmlcontent)) {
             logger.debug("Original HTML length " + htmlcontent.length());
 
             // Replace "&nbsp;" to avoid issue of "A circumflex" characters displayed (???)
             htmlcontent = htmlcontent.replaceAll("&Acirc;", " ");
 
             // Replace attachment URLs in HTML content for images to be shown
             for (Entry<String, MailAttachment> att : attachmentsMap.entrySet()) {
                 // remove starting "<" and finishing ">"
                 // FIXME : NPE
                 String pattern = att.getKey().substring(1, att.getKey().length() - 2);
                 pattern = "cid:" + pattern;
 
                 logger.debug("Testing for CID pattern " + Util.encodeURI(pattern, context) + " " + pattern);
                 // FIXME : should not be done here be sooner, so to remove dependency on msgdoc ...
                 String replacement = msgdoc.getAttachmentURL(att.getValue().getFilename(), context);
                 logger.debug("To be replaced by " + replacement);
                 htmlcontent = htmlcontent.replaceAll(pattern, replacement);
             }
 
             logger.debug("Zipping HTML part ...");
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream zos = new GZIPOutputStream(bos);
             byte[] bytes = htmlcontent.getBytes("UTF8");
             zos.write(bytes, 0, bytes.length);
             zos.finish();
             zos.close();
 
             byte[] compbytes = bos.toByteArray();
             htmlcontent = Utils.byte2hex(compbytes);
             bos.close();
 
             if (htmlcontent.length() > TextUtils.LONG_STRINGS_MAX_LENGTH) {
                 logger.debug("Failed to have HTML fit in target field, truncating");
                 htmlcontent = TextUtils.truncateForLargeString(htmlcontent);
             }
 
         } else {
             logger.debug("No HTML to treat");
         }
 
         logger.debug("Html Zipped length : " + htmlcontent.length());
         return htmlcontent;
     }
 
     // ****** Check existence of wiki object with same value as 'messageid', from database
     public boolean checkMsgIdExistence(String msgid)
     {
         boolean exists = false;
         String hql =
             "select count(*) from StringProperty as prop where prop.name='messageid' and prop.value='" + msgid + "')";
 
         try {
             List<Object> result = queryManager.createQuery(hql, Query.HQL).execute();
             logger.debug("CheckMsgIdExistence result " + result);
             exists = (Long) result.get(0) != 0;
         } catch (QueryException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         if (!exists) {
             logger.debug("Message with id " + msgid + " does not exist in database");
             return false;
         } else {
             logger.debug("Message with id " + msgid + " already loaded in database");
             return true;
         }
 
     }
 
     /*
      * Add map of attachments (bodyparts) to a document (doc1)
      */
     public int addAttachmentsFromMail(XWikiDocument doc1, ArrayList<MimeBodyPart> bodyparts,
         HashMap<String, String> attachmentsMap) throws MessagingException, IOException, XWikiException
     {
         int nb = 0;
         for (MimeBodyPart bodypart : bodyparts) {
             String fileName = bodypart.getFileName();
             String cid = bodypart.getContentID();
 
             try {
                 // replace by correct name if filename was renamed (multiple attachments with same name)
                 if (attachmentsMap.containsKey(cid)) {
                     fileName = attachmentsMap.get(cid);
                 }
                 logger.debug("Treating attachment: " + fileName + " with contentid " + cid);
                 if (fileName == null) {
                     fileName = "fichier.doc";
                 }
                 if (fileName.equals("oledata.mso") || fileName.endsWith(".wmz") || fileName.endsWith(".emz")) {
                     logger.debug("Not treating Microsoft crap !");
                 } else {
                     String disposition = bodypart.getDisposition();
                     String contentType = bodypart.getContentType().toLowerCase();
 
                     logger.debug("Treating attachment of type: " + contentType);
 
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     OutputStream out = new BufferedOutputStream(baos);
                     // We can't just use p.writeTo() here because it doesn't
                     // decode the attachment. Instead we copy the input stream
                     // onto the output stream which does automatically decode
                     // Base-64, quoted printable, and a variety of other formats.
                     InputStream ins = new BufferedInputStream(bodypart.getInputStream());
                     int b = ins.read();
                     while (b != -1) {
                         out.write(b);
                         b = ins.read();
                     }
                     out.flush();
                     out.close();
                     ins.close();
 
                     logger.debug("Treating attachment step 3: " + fileName);
 
                     byte[] data = baos.toByteArray();
                     logger.debug("Ready to attach attachment: " + fileName);
                     nb += addAttachmentFromMail(doc1, fileName, data);
                 } // end if
             } catch (Exception e) {
                 logger.warn("Attachment " + fileName + " could not be treated", e);
             }
         } // end for all attachments
         return nb;
     }
 
     /*
      * Add to document (doc1) an attached file (afilename) with its content (adata), and fills a map (adata) with
      * relation between contentId (cid) and (afilename)
      */
     public int addAttachmentFromMail(XWikiDocument doc, String afilename, byte[] adata) throws XWikiException
     {
         String filename = getAttachmentValidName(afilename);
         logger.debug("adding attachment: " + filename);
 
         XWikiAttachment attachment = new XWikiAttachment();
         doc.getAttachmentList().add(attachment);
         attachment.setContent(adata);
         attachment.setFilename(filename);
         // TODO: handle Author
         attachment.setAuthor(context.getUser());
         // Add the attachment to the document
         attachment.setDoc(doc);
         logger.debug("saving attachment: " + filename);
         doc.setComment("Added attachment " + filename);
         doc.saveAttachmentContent(attachment, context);
 
         return 1;
     }
 
     /*
      * Returns a valid name for an attachment from its original name
      */
     public String getAttachmentValidName(String afilename)
     {
         String fname = afilename;
         int i = fname.lastIndexOf("\\");
         if (i == -1) {
             i = fname.lastIndexOf("/");
         }
         String filename = fname.substring(i + 1);
         filename = filename.replaceAll("\\+", " ");
         return filename;
     }
 
     /**
      * @param m
      * @return
      */
     protected ArrayList<String> extractMailingListsTags(MailItem m)
     {
         ArrayList<String> taglist = new ArrayList<String>();
 
         for (IMailingList list : config.getMailingLists().values()) {
             if (m.getFrom().contains(list.getDisplayName()) || m.getTo().contains(list.getPattern())
                 || m.getCc().contains(list.getPattern())) {
                 // Add tag of this mailing-list to the list of tags
                 taglist.add(list.getTag());
             }
         }
 
         return taglist;
     }
 
     /**
      * @param doc
      * @param user
      * @param contentUser
      * @param comment
      * @throws XWikiException
      */
     protected void saveAsUser(final XWikiDocument doc, final String user, final String contentUser, final String comment)
         throws XWikiException
     {
         String luser = user;
         // If user is not provided we leave existing one
         if (luser == null) {
             if (xwiki.exists(doc.getFullName(), context)) {
                 luser = doc.getCreator();
             } else {
                 luser = UNKNOWN_USER;
             }
         }
         // We set creator only at document creation
         if (!xwiki.exists(doc.getFullName(), context)) {
             doc.setCreator(luser);
         }
         doc.setAuthor(luser);
         doc.setContentAuthor(contentUser);
         // avoid automatic set of update date to current date
         doc.setContentDirty(false);
         doc.setMetaDataDirty(false);
         xwiki.getXWiki(context).saveDocument(doc, comment, context);
     }
 
     /**
      * Returns the topicId of already existing topic for this topic id or subject. If no topic with this id or subject
      * is found, try to search for a message for wich msgid = replyid of new msg, then attach this new msg to the same
      * topic. If there is no existing topic, returns null. Search topic with same subject only if inreplyto is not
      * empty, meaning it's not supposed to be the first message of another topic.
      * 
      * @param topicId
      * @param topicSubject
      * @param inreplyto
      * @return
      */
     public String existsTopic(String topicId, String topicSubject, String inreplyto, String messageid)
     {
         String foundTopicId = null;
         String replyId = inreplyto;
         String previous = "";
         String previousSubject = topicSubject;
         boolean quit = false;
 
         // Search in existing messages for existing msg id = new reply id, and grab topic id
         // search replies until root message
         while (existingMessages.containsKey(replyId) && existingMessages.get(replyId) != null && !quit) {
             XWikiDocument msgDoc = null;
             try {
                 msgDoc = context.getWiki().getDocument(existingMessages.get(replyId).getFullName(), context);
             } catch (XWikiException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             if (msgDoc != null) {
                 BaseObject msgObj = msgDoc.getObject(SPACE_CODE + ".MailClass");
                 if (msgObj != null) {
                     logger
                         .debug("existsTopic : message " + replyId + " is a reply to " + existingMessages.get(replyId));
                     if (TextUtils.similarSubjects(previousSubject, msgObj.getStringValue("topicsubject"))) {
                         previous = replyId;
                         replyId = msgObj.getStringValue("inreplyto");
                         previousSubject = msgObj.getStringValue("topicSubject");
                     } else {
                         logger.debug("existsTopic : existing message subject is too different, exiting loop");
                         quit = true;
                     }
                 } else {
                     replyId = null;
                 }
             } else {
                 replyId = null;
             }
         }
         if (replyId != inreplyto && replyId != null) {
             logger
                 .debug("existsTopic : found existing message that current message is a reply to, to attach to same topic id");
             foundTopicId = existingMessages.get(previous).getTopicId();
             logger.debug("existsTopic : Found topic id " + foundTopicId);
         }
         // Search in existing topics with id
         if (foundTopicId == null) {
             if (!StringUtils.isBlank(topicId) && existingTopics.containsKey(topicId)) {
                 logger.debug("existsTopic : found topic id in loaded topics");
                 if (TextUtils.similarSubjects(topicSubject, existingTopics.get(topicId).getSubject())) {
                     foundTopicId = topicId;
                 } else {
                     logger.debug("... but subjects are too different");
                 }
             }
         }
         // Search with references
         if (foundTopicId == null) {
             String xwql =
                 "select distinct mail.topicid from Document doc, doc.object(" + SPACE_CODE
                     + ".MailClass) as mail where mail.references like '%" + messageid + "%'";
             try {
                 List<String> topicIds = queryManager.createQuery(xwql, Query.XWQL).execute();
                 // We're not supposed to find several topics related to messages having this id in references ...
                 if (topicIds.size() == 1) {
                     foundTopicId = topicIds.get(0);
                 }
                 if (topicIds.size() > 1) {
                     logger.warn("We should have found only one topicId instead of this list : " + topicIds
                         + ", using the first found");
                 }
             } catch (QueryException e) {
                 logger.warn("Issue while searching for references", e);
             }
         }
         // Search in existing topics with exactly same subject
         if (foundTopicId == null) {
             for (String currentTopicId : existingTopics.keySet()) {
                 TopicDescriptor currentTopic = existingTopics.get(currentTopicId);
                 if (currentTopic.getSubject().trim().equalsIgnoreCase(topicSubject.trim())) {
                     logger.debug("existsTopic : found subject in loaded topics");
                     if (!StringUtils.isBlank(inreplyto)) {
                         foundTopicId = currentTopicId;
                     } else {
                         logger.debug("existsTopic : found a topic but it's first message in topic");
                         // Note : desperate tentative to attach this message to an existing topic
                         // instead of creating a new one ... Sometimes replyId and refs can be
                         // empty even if this is a reply to something already loaded, in this
                         // case we just check if topicId was already loaded once, even if not
                         // the same topic ...
                         if (existingTopics.containsKey(topicId)) {
                             logger
                                 .debug("existsTopic : ... but we 'saw' this topicId before, so attach to found topicId "
                                     + currentTopicId + " with same subject");
                             foundTopicId = currentTopicId;
                         }
                     }
 
                 }
             }
         }
 
         return foundTopicId;
     }
 
     /**
      * @param server
      * @return
      * @throws MailArchiveException
      */
     public List<Message> fetchMailsFromServer(IServer server) throws MailArchiveException
     {
         assert (server != null);
 
         List<Message> messages = new ArrayList<Message>();
 
         if (queryServerInfo(server) >= 0) {
 
             try {
                 logger.info("Trying to retrieve mails from server " + server.toString());
                 messages =
                     mailManager.fetch(server.getHost(), server.getPort(), server.getProtocol(), server.getFolder(),
                         server.getUser(), server.getPassword(), server.getAdditionalProperties(), true);
             } catch (Exception e) {
                 throw new MailArchiveException("Could not connect to server " + server, e);
             }
         } else {
             throw new MailArchiveException("Connection to server checked as failed, not trying to load mails");
         }
 
         logger.info("Found " + messages.size() + " messages");
 
         return messages;
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @throws IOException
      * @throws XWikiException
      * @throws MailArchiveException
      * @throws InitializationException
      * @see org.xwiki.contrib.mailarchive.IMailArchive#getDecodedMailText(java.lang.String, boolean)
      */
     @SuppressWarnings("deprecation")
     @Override
     public String getDecodedMailText(String mailPage, boolean cut) throws IOException, XWikiException,
         InitializationException, MailArchiveException
     {
         if (!this.isConfigured) {
             configure();
         }
         if (!StringUtils.isBlank(mailPage)) {
             XWikiDocument htmldoc = xwiki.getDocument(mailPage, this.context);
             if (htmldoc != null) {
                 BaseObject htmlobj = htmldoc.getObject("MailArchiveCode.MailClass");
                 if (htmlobj != null) {
                     String ziphtml = htmlobj.getLargeStringValue("bodyhtml");
                     String body = htmlobj.getLargeStringValue("body");
 
                     return (mailutils.decodeMailContent(ziphtml, body, cut));
                 }
             }
         }
 
         return "";
 
     }
 
     // FIXME: is that absolutely needed ? triggering an dependency on log implementation is pretty bad since it make
     // impossible to switch it to something else. If that's really needed you could add this feature to
     // xwiki-commons-log module maybe.
     // FIXME: Starting with 4.2.M2 it is possible to manipulate log level from LoggerManager, so when migrating to this
     // version
     // following code could be adapted. Meanwhile it should either be removed (and the feature along with it), or
     // described in the MailArchive release notes that this debug mode is only supported for logback and does not user
     // LoggerManager. This is tracked down by XMAILARCH-13.
     public void enterDebugMode()
     {
         // Logs level
         ch.qos.logback.classic.Logger myLogger = (ch.qos.logback.classic.Logger) this.logger;
         this.logLevel = myLogger.getLevel();
         myLogger.setLevel(Level.DEBUG);
 
         logger.debug("DEBUG MODE ON");
     }
 
     public void quitDebugMode()
     {
         logger.debug("DEBUG MODE OFF");
         ch.qos.logback.classic.Logger myLogger = (ch.qos.logback.classic.Logger) this.logger;
         myLogger.setLevel(this.logLevel);
     }
 
 }
