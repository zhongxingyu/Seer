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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.mail.Flags;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 
 import org.slf4j.Logger;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.phase.Initializable;
 import org.xwiki.component.phase.InitializationException;
 import org.xwiki.contrib.mail.IMailComponent;
 import org.xwiki.contrib.mail.IMailReader;
 import org.xwiki.contrib.mailarchive.IMASource;
 import org.xwiki.contrib.mailarchive.IMailArchive;
 import org.xwiki.contrib.mailarchive.IMailArchiveConfiguration;
 import org.xwiki.contrib.mailarchive.IMailArchiveLoader;
 import org.xwiki.contrib.mailarchive.LoadingSession;
 import org.xwiki.contrib.mailarchive.internal.data.MailStore;
 import org.xwiki.contrib.mailarchive.internal.data.Server;
 import org.xwiki.contrib.mailarchive.internal.exceptions.MailArchiveException;
 import org.xwiki.logging.LogLevel;
 import org.xwiki.rendering.parser.StreamParser;
 
 /**
  * @version $Id$
  */
 @Component
 @Singleton
 public class DefaultMailLoader implements IMailArchiveLoader, Initializable
 {
     @Inject
     private IMailArchive mailArchive;
 
     /** Provides access to low-level mail api component */
     @Inject
     private IMailComponent mailManager;
 
     /** Provides access to the Mail archive configuration */
     @Inject
     private IMailArchiveConfiguration config;
 
     /** Aggregated component logger */
     @Inject
     private IAggregatedLoggerManager aggregatedLoggerManager;
 
     @Inject
     private Logger logger;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.component.phase.Initializable#initialize()
      */
     @Override
     public void initialize() throws InitializationException
     {
         aggregatedLoggerManager.addComponentLogger(IMailArchive.class);
         aggregatedLoggerManager.addComponentLogger(IMailComponent.class);
         aggregatedLoggerManager.addComponentLogger(StreamParser.class);
        try {
            this.config = mailArchive.getConfiguration();
        } catch (MailArchiveException e) {
            throw new InitializationException("Failed to initiliaze mail archive configuration", e);
        }
     }
 
     /**
      * @param session
      * @return
      */
     public int loadMails(final LoadingSession session)
     {
         return loadMails(session, null);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.IMailArchive#loadMails(org.xwiki.contrib.mailarchive.LoadingSession)
      */
     public int loadMails(final LoadingSession session, final LoadingJob job)
     {
         int nbSuccess = 0;
 
         if (session == null) {
             logger.warn("Invalid loading session (null) ...");
             return -1;
         } else {
             logger.info("Starting new MAIL loading session...");
         }
 
         if (mailArchive.isLocked()) {
             logger.warn("Loading process already in progress ...");
             return -1;
         }
 
         mailArchive.setLocked(true);
 
         logger.debug("Loading from session: " + session.toString());
 
         if (session.isDebugMode()) {
             enterDebugMode();
         }
 
         try {
             final List<IMASource> servers = mailArchive.getSourcesList(session);
 
             if (job != null) {
                 job.notifyPushLevelProgress(servers.size());
             }
 
             // Loop on all servers
             for (IMASource server : servers) {
                 IMailReader mailReader = getReader(server);
                 if (job != null) {
                     job.setCurrentSource("" + server.getType() + ':' + server.getId());
                 }
                 if (mailReader != null) {
                     nbSuccess += loadMails(mailReader, server.getFolder(), session, server.getId(), job);
                     mailReader.close();
                 }
                 if (job != null) {
                     job.notifyStepPropress();
                 }
 
             }
 
             if (job != null) {
                 job.notifyPopLevelProgress();
             }
 
             try {
                 // Compute timeline feed
                 if (config.isManageTimeline() && nbSuccess > 0) {
                     mailArchive.computeTimeline();
                 }
             } catch (Exception e) {
                 logger.warn("Could not compute timeline data", e);
             }
 
         } catch (Exception e) {
             logger.warn("EXCEPTION ", e);
             return -1;
         } finally {
             mailArchive.setLocked(false);
             if (session.isDebugMode()) {
                 quitDebugMode();
             }
         }
 
         return nbSuccess;
 
     }
 
     /**
      * @param mailReader
      * @param folder
      * @param session
      * @param serverId
      * @param job
      * @return
      */
     protected int loadMails(IMailReader mailReader, final String folder, final LoadingSession session,
         final String serverId, final LoadingJob job)
     {
 
         int currentMsg = 1;
         int nbSuccess = 0;
 
         List<Message> messages = null;
 
         if (mailReader != null) {
             try {
                 messages = mailReader.read(folder, !session.isLoadAll(), session.getLimit());
             } catch (MessagingException e) {
                 logger.warn("[{}] Can't read messages from server", serverId, e);
                 messages = new ArrayList<Message>();
             }
         }
         final int total = messages.size();
         int limit = session.getLimit();
         if (limit > 0) {
             if (limit > total) {
                 limit = total;
             }
         } else {
             limit = total;
         }
         logger.info("[{}] Number of messages to treat : {} / {}", new Object[] {serverId, limit, total});
         if (job != null) {
             job.notifyPushLevelProgress(limit);
         }
 
         MailLoadingResult result = null;
         while (currentMsg < limit) {
             Message message = messages.get(currentMsg - 1);
             logger.debug("[{}] Loading message {}/{}", new Object[] {serverId, currentMsg, total});
             try {
                 try {
                     if (session.isDebugMode()) {
                         mailArchive.dumpEmail(message);
                     }
                     if (job != null) {
                         job.setCurrentMail(message.getSubject());
                     }
                     result = mailArchive.loadMail(message, !session.isSimulationMode(), false, null);
                 } catch (Exception me) {
                     if (me instanceof MessagingException || me instanceof IOException) {
                         logger.debug("[" + serverId + "] Could not load email because of", me);
                         logger.info("[{}] Could not load email {}, trying to load a clone", currentMsg, serverId);
 
                         message = mailReader.cloneEmail(message);
                         if (message != null) {
                             result = mailArchive.loadMail(message, !session.isSimulationMode(), false, null);
                         }
                     } else {
                         logger.warn("Unexpected exception occurred while loading email", me);
                     }
 
                 }
 
                 if (job != null) {
                     job.notifyStepPropress();
                 }
 
                 if (result != null && result.isSuccess()) {
                     nbSuccess++;
                     if (!session.isSimulationMode()) {
                         message.setFlag(Flags.Flag.SEEN, true);
 
                         // Back it up in the built-in store
                         if (config.isUseStore()) {
                             mailArchive.saveToInternalStore(serverId, mailReader.getMailSource(), message);
                         }
                     }
                     if (job != null) {
                         job.incNbSuccess();
                     }
                 } else if (job != null) {
                     job.incNbFailure();
                 }
 
             } catch (Throwable e) {
                 logger.error("Failed to load mail", e);
             }
 
             currentMsg++;
         }
 
         if (job != null) {
             job.setCurrentMail("");
             job.notifyPopLevelProgress();
         }
 
         return nbSuccess;
     }
 
     protected IMailReader getReader(final IMASource source)
     {
         if (!source.isEnabled()) {
             logger.info("[{}] Server not enabled, skipping it", source.getId());
             return null;
         }
         logger.info("[{}] Loading mails from server", source.getId());
 
         IMailReader mailReader = null;
         try {
             if (source instanceof Server) {
                 Server s = (Server) source;
                 mailReader =
                     mailManager.getMailReader(s.getHostname(), s.getPort(), s.getProtocol(), s.getUsername(),
                         s.getPassword(), source.getAdditionalProperties());
 
             } else if (source instanceof MailStore) {
                 MailStore s = (MailStore) source;
                 mailReader = mailManager.getStoreManager(s.getFormat(), s.getLocation());
             }
         } catch (Exception e1) {
             logger.warn("[{}] Can't retrieve a mail reader", source.getId(), e1);
         }
 
         return mailReader;
     }
 
     public void enterDebugMode()
     {
         // Logs level
         logger.debug("enterDebugMode()");
         aggregatedLoggerManager.pushLogLevel(LogLevel.DEBUG);
         logger.debug("DEBUG MODE ON");
     }
 
     public void quitDebugMode()
     {
         logger.debug("DEBUG MODE OFF");
         aggregatedLoggerManager.popLogLevel();
         logger.debug("quitDebugMode()");
     }
 
 }
