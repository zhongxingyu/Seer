 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * DescriptionPane.java
  *
  * Created on 20. Oktober 2006, 11:06
  */
 package Sirius.navigator.ui;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.resource.ResourceManager;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.status.DefaultStatusChangeSupport;
 import Sirius.navigator.ui.status.Status;
 import Sirius.navigator.ui.status.StatusChangeListener;
 import Sirius.navigator.ui.status.StatusChangeSupport;
 
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 
 import org.openide.util.WeakListeners;
 
 import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.print.PageFormat;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.SwingWorker;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.cids.editors.CidsObjectEditorFactory;
 
 import de.cismet.cids.navigator.utils.MetaTreeNodeStore;
 
 import de.cismet.cids.tools.metaobjectrenderer.CidsObjectRendererFactory;
 import de.cismet.cids.tools.metaobjectrenderer.ScrollableFlowPanel;
 import de.cismet.cids.tools.metaobjectrenderer.SelfDisposingPanel;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.collections.MultiMap;
 import de.cismet.tools.collections.TypeSafeCollections;
 
 import de.cismet.tools.gui.ComponentWrapper;
 import de.cismet.tools.gui.WrappedComponent;
 import de.cismet.tools.gui.breadcrumb.DefaultBreadCrumbModel;
 import de.cismet.tools.gui.breadcrumb.LinkStyleBreadCrumbGui;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @version  $Revision$, $Date$
  */
 public class DescriptionPane extends JPanel implements StatusChangeSupport {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final ResourceManager resource = ResourceManager.getManager();
 //    private final transient Map<Component, PropertyChangeListener> strongReferencesOnWeakListenerMap = TypeSafeCollections.newHashMap();
 
     //~ Instance fields --------------------------------------------------------
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private final DefaultStatusChangeSupport statusChangeSupport;
     private final JPanel panRenderer = new JPanel();
     private final JComponent wrappedWaitingPanel;
     private SwingWorker worker = null;
     private GridBagConstraints gridBagConstraints;
     private String welcomePage;
     private String blankPage;
     private boolean showsWaitScreen = false;
     private DefaultBreadCrumbModel breadCrumbModel = new DefaultBreadCrumbModel();
     private LinkStyleBreadCrumbGui breadCrumbGui;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.xhtmlrenderer.simple.FSScrollPane fSScrollPane1;
     private javax.swing.JButton jButton1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JLabel lblRendererCreationWaitingLabel;
     private javax.swing.JPanel panBreadCrump;
     private javax.swing.JPanel panObjects;
     private javax.swing.JScrollPane scpRenderer;
     private org.xhtmlrenderer.simple.XHTMLPanel xHTMLPanel1;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates new form DescriptionPane.
      */
     public DescriptionPane() {
 //        timerAction = new TimerAction();
 //        cadenceTimer = new Timer(300, timerAction);
 //        cadenceTimer.setRepeats(false);
 
         initComponents();
 
         xHTMLPanel1.getSharedContext().setUserAgentCallback(new WebAccessManagerUserAgent());
 
         showHTML();
         breadCrumbGui = new LinkStyleBreadCrumbGui(breadCrumbModel);
         panBreadCrump.add(breadCrumbGui, BorderLayout.CENTER);
         this.statusChangeSupport = new DefaultStatusChangeSupport(this);
         BufferedReader reader = null;
         try {
             StringBuffer buffer = new StringBuffer();
             String string = null;
             reader = new BufferedReader(new InputStreamReader(
                         resource.getNavigatorResourceAsStream("doc/welcome.html"))); // NOI18N
 
             while ((string = reader.readLine()) != null) {
                 buffer.append(string);
             }
 
             this.welcomePage = buffer.toString();
 
             buffer = new StringBuffer();
             string = null;
             reader = new BufferedReader(new InputStreamReader(
                         resource.getNavigatorResourceAsStream("doc/blank.xhtml"),
                         "UTF-8")); // NOI18N
 
             while ((string = reader.readLine()) != null) {
                 buffer.append(string);
             }
 
             this.blankPage = buffer.toString();
         } catch (IOException ioexp) {
             if (log.isDebugEnabled()) {
                 log.debug(ioexp, ioexp);
             }
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException ex) {
                     log.warn(ex, ex);
                 }
             }
         }
 
         scpRenderer.setViewportView(panRenderer);
         panRenderer.setLayout(new GridBagLayout());
         final ComponentWrapper cw = CidsObjectEditorFactory.getInstance().getComponentWrapper();
         if (cw != null) {
             wrappedWaitingPanel = (JComponent)cw.wrapComponent(lblRendererCreationWaitingLabel);
         } else {
             wrappedWaitingPanel = null;
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         lblRendererCreationWaitingLabel = new javax.swing.JLabel();
         jButton1 = new javax.swing.JButton();
         panObjects = new javax.swing.JPanel();
         scpRenderer = new javax.swing.JScrollPane();
         jPanel1 = new javax.swing.JPanel();
         panBreadCrump = new javax.swing.JPanel();
         fSScrollPane1 = new org.xhtmlrenderer.simple.FSScrollPane();
         xHTMLPanel1 = new org.xhtmlrenderer.simple.XHTMLPanel();
 
         lblRendererCreationWaitingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblRendererCreationWaitingLabel.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/Sirius/navigator/resource/img/load.png"))); // NOI18N
 
         jButton1.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
         jButton1.setText(org.openide.util.NbBundle.getMessage(DescriptionPane.class, "DescriptionPane.JButton1")); // NOI18N
 
         setLayout(new java.awt.CardLayout());
 
         panObjects.setLayout(new java.awt.BorderLayout());
 
         scpRenderer.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
         scpRenderer.setViewportView(jPanel1);
 
         panObjects.add(scpRenderer, java.awt.BorderLayout.CENTER);
 
         panBreadCrump.setLayout(new java.awt.BorderLayout());
         panObjects.add(panBreadCrump, java.awt.BorderLayout.PAGE_START);
 
         add(panObjects, "objects");
 
         fSScrollPane1.setViewportView(xHTMLPanel1);
 
         add(fSScrollPane1, "html");
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      */
     private void showHTML() {
         final Runnable htmlRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     ((CardLayout)getLayout()).show(DescriptionPane.this, "html"); // NOI18N
                 }
             };
         if (EventQueue.isDispatchThread()) {
             htmlRunnable.run();
         } else {
             EventQueue.invokeLater(htmlRunnable);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void showObjects() {
         final Runnable showObjRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     ((CardLayout)getLayout()).show(DescriptionPane.this, "objects"); // NOI18N
                 }
             };
         if (EventQueue.isDispatchThread()) {
             showObjRunnable.run();
         } else {
             EventQueue.invokeLater(showObjRunnable);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void clear() {
         final Runnable clearRunnable = new Runnable() {
 
                 @Override
                 public void run() {
                     // release the strong references on the listeners, so that the weak listeners can be GCed.
                     // xHTMLPanel1.setDocument((Document) null);
                     xHTMLPanel1.setDocumentFromString(blankPage, "", new XhtmlNamespaceHandler());
                     removeAndDisposeAllRendererFromPanel();
                     repaint();
                 }
             };
 
         if (!EventQueue.isDispatchThread()) {
             EventQueue.invokeLater(clearRunnable);
         } else {
             clearRunnable.run();
         }
     }
 
     @Override
     public void addStatusChangeListener(final StatusChangeListener listener) {
         this.statusChangeSupport.addStatusChangeListener(listener);
     }
 
     @Override
     public void removeStatusChangeListener(final StatusChangeListener listener) {
         this.statusChangeSupport.removeStatusChangeListener(listener);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  page  DOCUMENT ME!
      */
     public void setPage(final String page) {
         try {
             if (log.isInfoEnabled()) {
                log.info("setPage:" + page); // NOI18N
             }
             if ((page == null) || (page.trim().length() <= 0)) {
                // TODO: Following call raises a NPE in Flying Saucer
                // xHTMLPanel1.setDocument((Document) null);
                 xHTMLPanel1.setDocumentFromString(blankPage, "", new XhtmlNamespaceHandler());
             } else {
                 xHTMLPanel1.setDocument(page);
             }
         } catch (Exception e) {
            log.info("Error in setPage()", e); // NOI18N
 
             statusChangeSupport.fireStatusChange(
                 org.openide.util.NbBundle.getMessage(
                     DescriptionPane.class,
                     "DescriptionPane.setPage(String).status.error"), // NOI18N
                 Status.MESSAGE_POSITION_3,
                 Status.ICON_DEACTIVATED,
                 Status.ICON_ACTIVATED);
         }
     }
     /**
      * Multiple Objects.
      *
      * @param  objects  DOCUMENT ME!
      */
     public void setNodesDescriptions(final List<?> objects) {
 //        breadCrumbModel.clear();
         if (objects.size() == 1) {
             setNodeDescription(objects.get(0));
         } else {
             if (worker != null) {
                 worker.cancel(true);
                 worker = null;
             }
             showObjects();
             clear();
             worker = new SwingWorker<SelfDisposingPanel, SelfDisposingPanel>() {
 
                     final List<JComponent> all = TypeSafeCollections.newArrayList();
 //                final Map<JComponent, PropertyChangeListener> localListenerMap = TypeSafeCollections.newHashMap();
 
                     @Override
                     protected SelfDisposingPanel doInBackground() throws Exception {
 //                    Vector filteredObjects = new Vector(objects);
                         final MultiMap objectsByClass = new MultiMap();
                         for (final Object object : objects) {
                             if ((object != null) && !((DefaultMetaTreeNode)object).isWaitNode()
                                         && !((DefaultMetaTreeNode)object).isRootNode()
                                         && !((DefaultMetaTreeNode)object).isPureNode()
                                         && ((DefaultMetaTreeNode)object).isObjectNode()) {
                                 try {
                                     final ObjectTreeNode n = (ObjectTreeNode)object;
                                     objectsByClass.put(n.getMetaClass(), n);
                                 } catch (Throwable t) {
                                     log.warn("Fehler beim Vorbereiten der Darstellung der Objekte", t); // NOI18N
                                 }
                             }
                         }
 //                    int y = 0;
                         final Iterator it = objectsByClass.keySet().iterator();
 
                         // splMain.setDividerLocation(1.0d);
                         while (it.hasNext() && !isCancelled()) {
                             // JSeparator sep=new JSeparator(JSeparator.HORIZONTAL);
                             final Object key = it.next();
                             final List l = (List)objectsByClass.get(key);
 
                             final List<MetaObject> v = TypeSafeCollections.newArrayList();
                             for (final Object o : l) {
                                 v.add(((ObjectTreeNode)o).getMetaObject());
                             }
                             final MetaClass mc = ((MetaObject)v.toArray()[0]).getMetaClass();
 
                             // Hier wird schon der Aggregationsrenderer gebaut, weil Einzelrenderer angezeigt werden
                             // fall getAggregationrenderer null lifert (keiner da, oder Fehler)
                             JComponent aggrRendererTester = null;
 
                             if (l.size() > 1) {
                                 // aggrRendererTester =
                                 // MetaObjectrendererFactory.getInstance().getAggregationRenderer(v, mc.getName() + " ("
                                 // + v.size() + ")");
                                 aggrRendererTester = CidsObjectRendererFactory.getInstance()
                                             .getAggregationRenderer(v, mc.getName() + " (" + v.size() + ")"); // NOI18N
                             }
                             if (aggrRendererTester == null) {
                                 log.warn("AggregationRenderer was null. Will use SingleRenderer");            // NOI18N
                                 for (final Object object : l) {
                                     final ObjectTreeNode otn = (ObjectTreeNode)object;
                                     // final JComponent comp =
                                     // MetaObjectrendererFactory.getInstance().getSingleRenderer(otn.getMetaObject(),
                                     // otn.getMetaClass().getName() + ": " + otn);
                                     final SelfDisposingPanel comp = encapsulateInSelfDisposingPanel(
                                             CidsObjectRendererFactory.getInstance().getSingleRenderer(
                                                 otn.getMetaObject(),
                                                 otn.getMetaClass().getName()
                                                         + ": "
                                                         + otn));
                                     final CidsBean bean = otn.getMetaObject().getBean();
                                     final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
 
                                             @Override
                                             public void propertyChange(final PropertyChangeEvent evt) {
                                                 comp.repaint();
                                             }
                                         };
                                     bean.addPropertyChangeListener(WeakListeners.propertyChange(
                                             propertyChangeListener,
                                             bean));
                                     comp.setStrongListenerReference(propertyChangeListener);
                                     publish(comp);
                                 }
                             } else {
                                 final SelfDisposingPanel comp = encapsulateInSelfDisposingPanel(aggrRendererTester);
                                 publish(comp);
                             }
                         }
                         return null;
                     }
 
                     @Override
                     protected void done() {
 //                    if (!isCancelled()) {
 //                        //access only in edt!
 //                        strongReferencesOnWeakListenerMap.clear();
 //                        strongReferencesOnWeakListenerMap.putAll(localListenerMap);
 //                    } else {
 //                        for (JComponent comp : all) {
 //                            if (comp instanceof DisposableCidsBeanStore) {
 //                                ((DisposableCidsBeanStore) comp).dispose();
 //                            }
 //                        }
 //                    }
                         all.clear();
                         if (worker == this) {
                             worker = null;
                         }
                     }
 
                     @Override
                     protected void process(final List<SelfDisposingPanel> chunks) {
                         int y = all.size();
                         for (final SelfDisposingPanel comp : chunks) {
                             try {
                                 final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                                 gridBagConstraints.gridx = 0;
                                 gridBagConstraints.gridy = y;
                                 gridBagConstraints.weightx = 1;
                                 gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                                 gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                                 panRenderer.add(comp, gridBagConstraints);
 
                                 comp.startChecking();
 
                                 panRenderer.revalidate();
                                 panRenderer.repaint();
 
                                 y++;
                             } catch (Throwable t) {
                                 log.error("Error while rendering MetaObjectrenderer", t); // NOI18N
                             }
                         }
                         all.addAll(chunks);
                     }
                 };
             if (worker != null) {
                 CismetThreadPool.execute(worker);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   renderer  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private SelfDisposingPanel encapsulateInSelfDisposingPanel(final JComponent renderer) {
         JComponent originalComponent = renderer;
         if (renderer instanceof WrappedComponent) {
             originalComponent = ((WrappedComponent)renderer).getOriginalComponent();
         }
         final SelfDisposingPanel sdp = new SelfDisposingPanel(originalComponent);
         sdp.setLayout(new BorderLayout());
         sdp.add(renderer, BorderLayout.CENTER);
         return sdp;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void showWaitScreen() {
         if (!showsWaitScreen) {
             showsWaitScreen = true;
             final Runnable run = new Runnable() {
 
                     @Override
                     public void run() {
                         removeAndDisposeAllRendererFromPanel();
                         final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
                         gridBagConstraints.gridx = 0;
                         gridBagConstraints.gridy = 0;
                         gridBagConstraints.weightx = 1;
                         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                         if (!(panRenderer.getLayout() instanceof GridBagLayout)) {
                             panRenderer.setLayout(new GridBagLayout());
                         }
                         if (wrappedWaitingPanel != null) {
                             panRenderer.add(wrappedWaitingPanel, gridBagConstraints);
                         }
                         repaint();
                     }
                 };
             if (EventQueue.isDispatchThread()) {
                 run.run();
             } else {
                 EventQueue.invokeLater(run);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  to             DOCUMENT ME!
      * @param  optionalTitle  DOCUMENT ME!
      */
     public void gotoMetaObject(final MetaObject to, final String optionalTitle) {
         breadCrumbModel.appendCrumb(new CidsMetaObjectBreadCrumb(to) {
 
                 @Override
                 public void crumbActionPerformed(final ActionEvent e) {
                     startSingleRendererWorker(to, optionalTitle);
                 }
             });
         startSingleRendererWorker(to, optionalTitle);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  mc             DOCUMENT ME!
      * @param  toObjectId     DOCUMENT ME!
      * @param  optionalTitle  DOCUMENT ME!
      */
     public void gotoMetaObject(final MetaClass mc, final int toObjectId, final String optionalTitle) {
         showWaitScreen();
         new SwingWorker<MetaObject, Void>() {
 
                 @Override
                 protected MetaObject doInBackground() throws Exception {
                     return SessionManager.getProxy().getMetaObject(toObjectId, mc.getId(), mc.getDomain());
                 }
 
                 @Override
                 protected void done() {
                     try {
                         final MetaObject result = get();
                         gotoMetaObject(result, optionalTitle);
                     } catch (Exception e) {
                         log.error("Exception in Background Thread", e); // NOI18N
                     }
                 }
             }.execute();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  node  DOCUMENT ME!
      */
     private void startSingleRendererWorker(final DefaultMetaTreeNode node) {
         final MetaObject o = ((ObjectTreeNode)node).getMetaObject();
 
         startSingleRendererWorker(o, node, node.toString());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  o      DOCUMENT ME!
      * @param  title  DOCUMENT ME!
      */
     private void startSingleRendererWorker(final MetaObject o, final String title) {
         startSingleRendererWorker(o, null, title);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  o      DOCUMENT ME!
      * @param  node   DOCUMENT ME!
      * @param  title  DOCUMENT ME!
      */
     private void startSingleRendererWorker(final MetaObject o, final DefaultMetaTreeNode node, final String title) {
         worker = new javax.swing.SwingWorker<SelfDisposingPanel, Void>() {
 
                 @Override
                 protected SelfDisposingPanel doInBackground() throws Exception {
                     // final JComponent comp = MetaObjectrendererFactory.getInstance().getSingleRenderer(o,
                     // n.toString());
                     final JComponent jComp = CidsObjectRendererFactory.getInstance().getSingleRenderer(o, title);
                     if ((jComp instanceof MetaTreeNodeStore) && (node != null)) {
                         ((MetaTreeNodeStore)jComp).setMetaTreeNode(node);
                     } else if ((jComp instanceof WrappedComponent)
                                 && (((WrappedComponent)jComp).getOriginalComponent() instanceof MetaTreeNodeStore)
                                 && (node != null)) {
                         final JComponent originalComponent = ((WrappedComponent)jComp).getOriginalComponent();
                         ((MetaTreeNodeStore)originalComponent).setMetaTreeNode(node);
                     }
                     final CidsBean bean = o.getBean();
                     log.fatal(bean.toJSONString());
                     final PropertyChangeListener localListener = new PropertyChangeListener() {
 
                             @Override
                             public void propertyChange(final PropertyChangeEvent evt) {
                                 jComp.repaint();
                             }
                         };
 //                strongReferencesOnWeakListenerMap.put(jComp, propertyChangeListener);
                     bean.addPropertyChangeListener(WeakListeners.propertyChange(localListener, bean));
                     final SelfDisposingPanel sdp = encapsulateInSelfDisposingPanel(jComp);
                     sdp.setStrongListenerReference(localListener);
                     return sdp;
                 }
 
                 @Override
                 protected void done() {
                     try {
                         final SelfDisposingPanel sdp = get();
                         if (!isCancelled()) {
                             showsWaitScreen = false;
                             // splMain.setDividerLocation(finalWidthRatio);
                             removeAndDisposeAllRendererFromPanel();
                             gridBagConstraints = new java.awt.GridBagConstraints();
                             gridBagConstraints.gridx = 0;
                             gridBagConstraints.gridy = 0;
                             gridBagConstraints.weightx = 1;
                             gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                             gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
 
                             if (sdp instanceof RequestsFullSizeComponent) {
                                 log.info("Renderer is FullSize Component!"); // NOI18N
                                 panRenderer.setLayout(new BorderLayout());
                                 panRenderer.add(sdp, BorderLayout.CENTER);
                             } else {
                                 panRenderer.setLayout(new GridBagLayout());
                                 panRenderer.add(sdp, gridBagConstraints);    // log.fatal("Comp added");
                             }
                             sdp.startChecking();
                             panRenderer.revalidate();
                             revalidate();
                             repaint();
                         } else {
                             if (log.isDebugEnabled()) {
                                 log.debug("Worker canceled!");               // NOI18N
                             }
 //                        if (comp instanceof DisposableCidsBeanStore) {
 //                            ((DisposableCidsBeanStore) comp).dispose();
 //                        }
                         }
                     } catch (InterruptedException iex) {
                         if (log.isDebugEnabled()) {
                             log.debug("Worker canceled!");                   // NOI18N
                         }
                     } catch (Exception e) {
                         log.error("Error during Renderer creation", e);      // NOI18N
                     }
                     if (worker == this) {
                         worker = null;
                     }
                 }
             };
         CismetThreadPool.execute(worker);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  n  DOCUMENT ME!
      */
     private void performSetNode(final DefaultMetaTreeNode n) {
         final String descriptionURL = n.getDescription();
         // besorge MO zum parametrisieren der URL
         if (n.isObjectNode()) {
             final MetaObject o = ((ObjectTreeNode)n).getMetaObject();
             breadCrumbModel.startWithNewCrumb(new CidsMetaObjectBreadCrumb(o) {
 
                     @Override
                     public void crumbActionPerformed(final ActionEvent e) {
                         startSingleRendererWorker(o, n.toString());
                     }
                 });
 
             startSingleRendererWorker(n);
         } else {
             if (n.isClassNode()) {
 //                try {
 //                    c = ((ClassTreeNode) n).getMetaClass();
 //                } catch (Throwable t) {
 //                    log.error(t);
 //                }
             } else if (n.isPureNode()) {
                 showHTML();
                 // splMain.setDividerLocation(0d);
             }
             showsWaitScreen = false;
         }
 
 //            try {
 //                descriptionURL = URLParameterizer.parameterizeURL(descriptionURL, c, o, Sirius.navigator.connection.SessionManager.getSession().getUser());
 //            } catch (Throwable t) {
 //                log.info("keine Parametrisierung m\u00F6glich url wie unparametrisiert verwendet", t);
 //            }
 
         if (log.isDebugEnabled()) {
             log.debug("loading description from url '" + descriptionURL + "'"); // NOI18N
         }
 
         this.setPage(descriptionURL);
     }
     /**
      * Single Object.
      *
      * @param  object  DOCUMENT ME!
      */
     public void setNodeDescription(final Object object) {
         if ((object != null) && !((DefaultMetaTreeNode)object).isWaitNode()
                     && !((DefaultMetaTreeNode)object).isRootNode()) {
             final DefaultMetaTreeNode n = (DefaultMetaTreeNode)object;
             if ((worker != null) && !worker.isDone()) {
                 worker.cancel(true);
                 worker = null;
             } else {
                 showObjects();
                 showWaitScreen();
             }
             performSetNode(n);
         } else {
             // if(logger.isDebugEnabled())logger.debug("no description url available");
             statusChangeSupport.fireStatusChange(
                 org.openide.util.NbBundle.getMessage(
                     DescriptionPane.class,
                     "DescriptionPane.setNodeDescription(Object).status.nodescription"), // NOI18N
                 Status.MESSAGE_POSITION_3,
                 Status.ICON_DEACTIVATED,
                 Status.ICON_DEACTIVATED);
 
             // this.setText("<html><body><h3>" + ResourceManager.getManager().getString("descriptionpane.welcome") +
             // "</h3></body></html>");
             xHTMLPanel1.setDocumentFromString(welcomePage, "", new XhtmlNamespaceHandler());
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void removeAndDisposeAllRendererFromPanel() {
 //        Component[] allComponents = panRenderer.getComponents();
 //        for (Component comp : allComponents) {
 //            if (comp instanceof WrappedComponent) {
 //                comp = ((WrappedComponent) comp).getOriginalComponent();
 //            }
 //            if (comp instanceof DisposableCidsBeanStore) {
 //                ((DisposableCidsBeanStore) comp).dispose();
 //            }
 //        }
 //        strongReferencesOnWeakListenerMap.clear();
         panRenderer.removeAll();
     }
 
     @Override
     public void paintComponent(final Graphics g) {
         final Graphics2D g2d = (Graphics2D)g;
 //        Paint p = g2d.getPaint();
         final GradientPaint gp = new GradientPaint(0, 0, getBackground(), getWidth(), getHeight(), Color.WHITE, false);
         g2d.setPaint(gp);
         g2d.fillRect(0, 0, getWidth(), getHeight());
         // super.paintComponent(g2d);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void prepareValueChanged() {
         if (worker != null) {
             worker.cancel(true);
         }
         showWaitScreen();
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class PrintableJPanel extends ScrollableFlowPanel implements Printable {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex)
                 throws PrinterException {
             if (pageIndex > 0) {
                 return (NO_SUCH_PAGE);
             } else {
                 final Graphics2D g2d = (Graphics2D)graphics;
                 g2d.scale(0.75, 0.75);
                 g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                 // Turn off double buffering
                 paint(g2d);
                 // Turn double buffering back on
                 return (PAGE_EXISTS);
             }
         }
     }
 //    final class TimerAction extends AbstractAction {
 //
 //        public TimerAction() {
 //        }
 //        private DefaultMetaTreeNode node;
 //
 //        @Override
 //        public void actionPerformed(ActionEvent e) {
 //            performSetNode(node);
 //        }
 //
 //        /**
 //         * @return the object
 //         */
 //        public DefaultMetaTreeNode getNode() {
 //            return node;
 //        }
 //
 //        /**
 //         * @param object the object to set
 //         */
 //        public void setNode(DefaultMetaTreeNode object) {
 //            this.node = object;
 //        }
 //    }
 }
