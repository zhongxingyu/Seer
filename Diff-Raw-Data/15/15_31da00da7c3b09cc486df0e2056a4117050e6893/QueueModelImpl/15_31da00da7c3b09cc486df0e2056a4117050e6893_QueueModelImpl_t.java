 /*
  * Created On:  4-Jun-07 11:08:32 AM
  */
 package com.thinkparity.ophelia.model.queue;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.Executors;
 
 import com.thinkparity.codebase.model.queue.notification.NotificationException;
 import com.thinkparity.codebase.model.queue.notification.NotificationMonitor;
 import com.thinkparity.codebase.model.queue.notification.NotificationSession;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.util.xmpp.event.XMPPEvent;
 
 import com.thinkparity.ophelia.model.Model;
 import com.thinkparity.ophelia.model.queue.monitor.ProcessStep;
 import com.thinkparity.ophelia.model.queue.notification.NotificationReaderRunnable;
 import com.thinkparity.ophelia.model.util.ProcessAdapter;
 import com.thinkparity.ophelia.model.util.ProcessMonitor;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 import com.thinkparity.service.AuthToken;
 import com.thinkparity.service.QueueService;
 import com.thinkparity.service.client.ServiceFactory;
 
 /**
  * <b>Title:</b>thinkParity Ophelia Model Queue Model Implementation<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class QueueModelImpl extends Model implements QueueModel,
         InternalQueueModel {
 
     /** A headless monitor for process queue. */
     private static final ProcessMonitor HEADLESS_MONITOR;
 
     /** A synchronization lock used to process the event queue. */
     private static final Object QUEUE_LOCK;
 
     /** A workspace attribute key for the notification client. */
     private static final String WS_ATTRIBUTE_KEY_NOTIFICATION_CLIENT;
 
     static {
         HEADLESS_MONITOR = new ProcessAdapter() {};
         WS_ATTRIBUTE_KEY_NOTIFICATION_CLIENT = "QueueModelImpl#notificationClient";
         QUEUE_LOCK = new Object();
     }
 
     /** A queue web-service interface. */
     private QueueService queueService;
 
     /**
      * Create QueueModelImpl.
      *
      */
     public QueueModelImpl() {
         super();
     }
 
     /**
      * @see com.thinkparity.ophelia.model.queue.InternalQueueModel#process(com.thinkparity.ophelia.model.util.ProcessMonitor)
      *
      */
     public void process(final ProcessMonitor monitor) {
         try {
             final Integer size = queueService.readSize(getAuthToken());
             notifyDetermine(monitor, size);
             final EventHandler handler = new EventHandler(modelFactory);
             synchronized (QUEUE_LOCK) {
                 notifyStepBegin(monitor, ProcessStep.HANDLE_EVENT);
                 final List<XMPPEvent> events = queueService.readEvents(getAuthToken());
                 for (final XMPPEvent event : events) {
                     try {
                         handler.handleEvent(event);
                     } finally {
                         queueService.deleteEvent(getAuthToken(), event);
                     }
                 }
                 notifyStepEnd(monitor, ProcessStep.HANDLE_EVENT);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.queue.InternalQueueModel#readSize()
      *
      */
     public Integer readSize() {
         try {
             return queueService.readSize(getAuthToken());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.queue.InternalQueueModel#startNotificationClient()
      * 
      */
     public void startNotificationClient() {
         try {
             // stop the client if running
             if (isSetNotificationClient()) {
                 try {
                     removeNotificationClient().closeReader();
                 } catch (final Exception x) {
                     logger.logError(x, "An error occured closing notification client.");
                 }
             }
             // start the client
             final NotificationMonitor monitor = new NotificationMonitor() {
                 public void chunkReceived(final int chunkSize) {}
                 public void chunkSent(final int chunkSize) {}
                 public void headerReceived(final String header) {}
                 public void headerSent(final String header) {}
                 public void streamError(final NotificationException error) {
                     logger.logWarning("Notification client offline.");
                    try {
                        removeNotificationClient().closeReader();
                    } catch (final Exception x) {
                        logger.logError(x, "An error occured closing notification client.");
                    }
                 }
             };
             final NotificationSession session =
                 queueService.createNotificationSession(getAuthToken());
             final NotificationReaderRunnable notificationClient =
                 newNotificationClient(monitor, session);
             setNotificationClient(notificationClient);
             // THREAD - QueueModelImpl#startNotificationClient
             final Thread thread =
                 Executors.defaultThreadFactory().newThread(notificationClient);
             thread.setDaemon(true);
             thread.setName("TPS-OpheliaModel-NotificationClient");
             thread.start();
         } catch (final IOException iox) {
             throw panic(iox);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.Model#initializeModel(com.thinkparity.codebase.model.session.Environment, com.thinkparity.ophelia.model.workspace.Workspace)
      *
      */
     @Override
     protected void initializeModel(final Environment environment,
             final Workspace workspace) {
         final ServiceFactory serviceFactory = ServiceFactory.getInstance();
         queueService = serviceFactory.getQueueService();
     }
 
     /**
      * Obtain the session authentication token.
      * 
      * @return A session <code>AuthToken</code>.
      */
     private AuthToken getAuthToken() {
         return getSessionModel().getAuthToken();
     }
 
     /**
      * Get the notification client workspace attribute.
      * 
      * @return A notification client <code>NotificationReaderRunnable</code>.
      */
     private NotificationReaderRunnable getNotificationClient() {
         return (NotificationReaderRunnable) workspace.getAttribute(
                 WS_ATTRIBUTE_KEY_NOTIFICATION_CLIENT);
     }
 
     /**
      * Determine if the notification client is set.
      * 
      * @return True if the notification client is set.
      */
     private boolean isSetNotificationClient() {
         return workspace.isSetAttribute(
                 WS_ATTRIBUTE_KEY_NOTIFICATION_CLIENT).booleanValue();
     }
 
     /**
      * Create an instance of a notification client.
      * 
      * @param session
      *            A <code>NotificationSession</code>.
      * @return A <code>NotificationReaderRunnable</code>.
      */
     private NotificationReaderRunnable newNotificationClient(final NotificationMonitor monitor,
             final NotificationSession session) throws IOException {
         final NotificationReaderRunnable notificationClient =
             new NotificationReaderRunnable(monitor, session);
         notificationClient.addObserver(new Observer() {
             public void update(final Observable o, final Object arg) {
                 final boolean didNotify = ((Boolean) arg).booleanValue();
                 if (didNotify) {
                     getQueueModel().process(HEADLESS_MONITOR);
                 } else {
                     // TODO - QueueModelImpl#newNotificationClient - add more complete notification protocol
                     logger.logWarning("Illegal notification protocol.");
                 }
             }
         });
         return notificationClient;
     }
 
     /**
      * Remove the notification client workspace attribute.
      * 
      * @return The <code>NotificationReaderRunnable</code>.
      */
     private NotificationReaderRunnable removeNotificationClient() {
         final NotificationReaderRunnable notificationClient = getNotificationClient();
         if (workspace.isSetAttribute(WS_ATTRIBUTE_KEY_NOTIFICATION_CLIENT)) {
             workspace.removeAttribute(WS_ATTRIBUTE_KEY_NOTIFICATION_CLIENT);
         }
         return notificationClient;
     }
 
     /**
      * Set the notification client workspace attribute.
      * 
      * @param notificationClient
      *            A notification client <code>Thread</code>.
      */
     private void setNotificationClient(final NotificationReaderRunnable notificationClient) {
         workspace.setAttribute(WS_ATTRIBUTE_KEY_NOTIFICATION_CLIENT, notificationClient);
     }
 }
