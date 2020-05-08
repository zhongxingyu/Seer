 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.reconnector;
 
 import Sirius.navigator.ui.ComponentRegistry;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.SwingWorker;
 
 import de.cismet.tools.gui.StaticSwingTools;
 
 /**
  * DOCUMENT ME!
  *
  * @author   jruiz
  * @version  $Revision$, $Date$
  */
 public abstract class Reconnector<S extends Object> {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Reconnector.class);
 
     //~ Enums ------------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static enum ReconnectorState {
 
         //~ Enum constants -----------------------------------------------------
 
         CONNECTING, CANCELED, COMPLETED, FAILED
     }
 
     //~ Instance fields --------------------------------------------------------
 
     private Class serviceClass;
     private ConnectorWorker connectorWorker;
     private JDialog reconnectorDialog;
     private JFrame dialogOwner;
     private S service;
     private boolean isReconnecting = true;
 
     private final ReconnectorListener dispatcher = new ListenerDispatcher();
     private List<ReconnectorListener> listeners = new LinkedList<ReconnectorListener>();
     private List<ReconnectorPanel> reconnectorPanels = new LinkedList<ReconnectorPanel>();
     // private List<ReconnectorWrapperPanel> wrapperPanels = new LinkedList<ReconnectorWrapperPanel>();
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new Reconnector object.
      *
      * @param  serviceClass  DOCUMENT ME!
      */
     protected Reconnector(final Class serviceClass) {
         this.serviceClass = serviceClass;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  ReconnectorException  DOCUMENT ME!
      */
     protected abstract S connectService() throws ReconnectorException;
 
     /**
      * DOCUMENT ME!
      *
      * @param   throwable  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Throwable  DOCUMENT ME!
      */
     protected abstract ReconnectorException getReconnectorException(final Throwable throwable) throws Throwable;
     /**
      * Abbrechen.
      */
     public void doAbbort() {
         reconnectorDialog.setVisible(false);
         isReconnecting = false;
     }
     /**
      * Verbindungsaufbau über Swingworker.
      */
     public void doReconnect() {
         // Worker schon vorhanden?
         if (connectorWorker != null) {
             // worker anhalten
             connectorWorker.cancel(false);
             connectorWorker = null;
         }
         if (isReconnecting) {
             // neuen Worker erzeugen und starten
             connectorWorker = new ConnectorWorker();
             connectorWorker.execute();
         }
     }
     /**
      * Verbindungsaufbau abbrechen.
      */
     public void doCancel() {
         if ((connectorWorker != null) && !connectorWorker.isDone()) {
             connectorWorker.cancel(true);
             connectorWorker = null;
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Verbindungsvorgang abgebrochen");
             }
         } else {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("nichts zum Abbrechen");
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  listener  DOCUMENT ME!
      */
     public void addListener(final ReconnectorListener listener) {
         listeners.add(listener);
     }
     /**
      * Service Proxy.
      *
      * @return  DOCUMENT ME!
      */
     public S getProxy() {
         return (S)Proxy.newProxyInstance(
                 serviceClass.getClassLoader(),
                 new Class[] { serviceClass },
                 new ReconnectorInvocationHandler());
     }
 
     /**
      * DOCUMENT ME!
      */
     void pack() {
         reconnectorDialog.pack();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  useDialog    DOCUMENT ME!
      * @param  parentFrame  DOCUMENT ME!
      */
     public void useDialog(final boolean useDialog, final JFrame parentFrame) {
         if (useDialog) {
             if (parentFrame != null) {
                 dialogOwner = parentFrame;
             } else {
                 dialogOwner = null;
             }
 
             reconnectorDialog = new JDialog(dialogOwner, "Verbindungsfehler", true);
             reconnectorDialog.setResizable(true);
             reconnectorDialog.setMinimumSize(new Dimension(400, 150));
             reconnectorDialog.setContentPane(createReconnectorPanel());
             reconnectorDialog.addWindowListener(new WindowAdapter() {
 
                     @Override
                     public void windowClosing(final WindowEvent we) {
                         doAbbort();
                     }
                 });
             reconnectorDialog.setAlwaysOnTop(true);
         } else {
             // TODO reconnectorPanel des Dialogs aus der Liste entfernen
             if (reconnectorDialog != null) {
                 final ReconnectorPanel panel = (ReconnectorPanel)reconnectorDialog.getContentPane();
                 reconnectorPanels.remove(panel);
                 listeners.remove(panel);
             }
             dialogOwner = null;
             reconnectorDialog = null;
         }
     }
     /**
      * public JPanel registerFrame(final JPanel panel) { final ReconnectorWrapperPanel reconnectorWrapperPanel = new
      * ReconnectorWrapperPanel(panel, createReconnectorPanel()); addListener(reconnectorWrapperPanel);
      * //wrapperPanels.add(reconnectorWrapperPanel); return reconnectorWrapperPanel; } Die Nutzung des services ist
      * Fehlgeschlagen. Je nach Modus (attended / unattend) wird die Verbindung entweder sofort wieder hergestellt, oder
      * es wird eine Fehlermeldung angezeigt und darauf gewartet, dass der User die Verbindung wieder anstößt.
      *
      * @param  exception  DOCUMENT ME!
      */
     private void serviceFailed(final ReconnectorException exception) {
         final Component component = exception.getComponent();
         if (isUnattended()) {
             doReconnect();
         } else {
             dispatcher.connectionFailed(new ReconnectorEvent(component));
         }
     }
     /**
      * mit GUI oder ohne?
      *
      * @return  DOCUMENT ME!
      */
     private boolean isUnattended() {
         return (reconnectorDialog == null /* && wrapperPanels.isEmpty()*/);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private ReconnectorPanel createReconnectorPanel() {
         final ReconnectorPanel reconnectorPanel = new ReconnectorPanel(this);
 
         reconnectorPanels.add(reconnectorPanel);
         addListener(reconnectorPanel);
         return reconnectorPanel;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void updateReconnectorDialogOwner() {
         if (dialogOwner == null) {
             if (ComponentRegistry.isRegistred()) {
                 useDialog(false, null);
                 useDialog(true, ComponentRegistry.getRegistry().getMainWindow());
             }
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class ReconnectorInvocationHandler implements InvocationHandler {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
             Throwable targetEx = null;
             while (isReconnecting) {
                 try {
                     // service wieder verbinden
                     if (service == null) {
                         service = connectService();
                     }
 
                     // Methodenaufruf durchreichen
                     return method.invoke(service, args);
 
                     // wieder verbinden fehlgeschlagen
                 } catch (final ReconnectorException ex) {
                     // Fehler anzeigen und neuverbindung anfragen
                     serviceFailed(ex);
                 } catch (final InvocationTargetException ex) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Exception while invocation", ex);
                     }
                     targetEx = ex.getTargetException();
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Wrapped Exception", targetEx);
                     }
                     serviceFailed(getReconnectorException(targetEx));
                 }
             }
             isReconnecting = true;
             throw targetEx;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class ConnectorWorker extends SwingWorker<S, Void> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected S doInBackground() throws Exception {
             dispatcher.connecting();
             // alten service verwerfen
             service = null;
             // Verbindungs-Prozess starten
             try {
                 return connectService();
             } catch (ReconnectorException exception) {
                 serviceFailed(exception);
                 throw new ExecutionException(exception);
             }
         }
 
         @Override
         protected void done() {
             // abgebrochen ?
             if (isCancelled()) {
                 dispatcher.connectionCanceled();
                 // abgeschlossen ?
             } else {
                 try {
                     // neuen service setzen
                     service = get();
                     // panels verstecken
                     dispatcher.connectionCompleted();
 
                     // Fehler beim Verbinden?
                 } catch (final Exception ex) {
                     // Neuverbindung in Gang setzen
                     if (ex instanceof ExecutionException) {
                     } else {
                         serviceFailed(new ReconnectorException(ex.getClass().getCanonicalName()));
                     }
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class ListenerDispatcher implements ReconnectorListener {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @param  state  DOCUMENT ME!
          * @param  evt    DOCUMENT ME!
          */
         private void updateAndNotifyAboutState(final ReconnectorState state, final ReconnectorEvent evt) {
             if (reconnectorDialog != null) {
                 final boolean isVisible = reconnectorDialog.isVisible();
 
                 if (!isVisible) {
                     updateReconnectorDialogOwner();
                 }
 
                 switch (state) {
                     case CONNECTING: {
                         for (final ReconnectorListener listener : listeners) {
                             listener.connecting();
                         }
                         break;
                     }
 
                     case FAILED: {
                         for (final ReconnectorListener listener : listeners) {
                             listener.connectionFailed(evt);
                         }
                         break;
                     }
                     case COMPLETED: {
                         for (final ReconnectorListener listener : listeners) {
                             listener.connectionCompleted();
                         }
                         break;
                     }
                     case CANCELED: {
                         for (final ReconnectorListener listener : listeners) {
                             listener.connectionCanceled();
                         }
                         break;
                     }
                 }
 
                 reconnectorDialog.pack();
 
                 if (!isVisible) {
                     StaticSwingTools.showDialog(reconnectorDialog);
                 }
             }
         }
 
         @Override
         public void connecting() {
             this.updateAndNotifyAboutState(ReconnectorState.CONNECTING, null);
         }
 
         @Override
         public void connectionFailed(final ReconnectorEvent event) {
             this.updateAndNotifyAboutState(ReconnectorState.FAILED, event);
         }
 
         @Override
         public void connectionCompleted() {
             this.updateAndNotifyAboutState(ReconnectorState.COMPLETED, null);
         }
 
         @Override
         public void connectionCanceled() {
             this.updateAndNotifyAboutState(ReconnectorState.CANCELED, null);
         }
     }
 }
