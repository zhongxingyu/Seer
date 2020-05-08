 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project;
 
 import org.apache.log4j.Logger;
 
 import org.netbeans.api.progress.ProgressHandle;
 import org.netbeans.api.progress.ProgressHandleFactory;
 
 import org.openide.nodes.AbstractNode;
 import org.openide.nodes.Children;
 import org.openide.nodes.Node;
import org.openide.util.NbBundle;
 
 import java.awt.EventQueue;
 
 import java.io.IOException;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import de.cismet.cids.abf.utilities.nodes.LoadingNode;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 // can probably be replaced completely by ChildrenFactory, tbi
 public abstract class ProjectChildren extends Children.Keys {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(ProjectChildren.class);
 
     private static final transient ExecutorService EXECUTOR = Executors.newFixedThreadPool(8);
 
     //~ Instance fields --------------------------------------------------------
 
     protected final transient DomainserverProject project;
 
     private final transient Object lock;
 
     private transient Future<?> refreshFuture;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ProjectChildren object.
      *
      * @param  project  DOCUMENT ME!
      */
     public ProjectChildren(final DomainserverProject project) {
         super(true);
         this.project = project;
         this.lock = new Object();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     protected void addNotify() {
         final LoadingNode loadingNode = new LoadingNode();
         final Runnable r = new Runnable() {
 
                 @Override
                 public void run() {
                     if (LOG.isDebugEnabled()) {
                        LOG.debug("addNotify caller debug, initialized: " + isInitialized(), // NOI18N
                             new Throwable("trace: " + this)); // NOI18N
                     }
 
                     if (!isInitialized()) {
                         setKeys(new Object[] { loadingNode });
                     }
                 }
             };
 
         if (EventQueue.isDispatchThread()) {
             r.run();
         } else {
             EventQueue.invokeLater(r);
         }
 
         refreshFuture = EXECUTOR.submit(new Runnable() {
 
                     private final transient ProgressHandle handle = ProgressHandleFactory.createHandle(
                            NbBundle.getMessage(
                                ProjectChildren.class,
                                "ProjectChildren.addNotify.refreshFuture.handle.message", // NOI18N
                                ProjectChildren.this.getNode().getDisplayName()));
 
                     @Override
                     public void run() {
                         try {
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         handle.start();
                                         handle.switchToIndeterminate();
                                     }
                                 });
 
                             threadedNotify();
                         } catch (final Exception e) {
                             LOG.error("could not generate keys", e); // NOI18N
                             setKeysEDT(e);
                         } finally {
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         handle.finish();
                                     }
                                 });
 
                             loadingNode.dispose();
                             synchronized (lock) {
                                 refreshFuture = null;
                             }
                         }
                     }
                 });
     }
 
     @Override
     protected Node[] createNodes(final Object o) {
         final Node[] created;
 
         if (o instanceof Exception) {
             final AbstractNode node = new AbstractNode(LEAF);
             node.setName(((Exception)o).getLocalizedMessage());
 
             created = new Node[] { node };
         } else if (o instanceof LoadingNode) {
             created = new Node[] { (Node)o };
         } else {
             created = createUserNodes(o);
         }
 
         return created;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   o  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected abstract Node[] createUserNodes(final Object o);
 
     /**
      * DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     protected abstract void threadedNotify() throws IOException;
 
     /**
      * DOCUMENT ME!
      *
      * @param  keys  DOCUMENT ME!
      */
     protected void setKeysEDT(final Collection keys) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         setKeys(keys);
                    } catch (final Exception e) {
                        LOG.error("cannot set keys: " + keys + " | " + this, e); // NOI18N
                     }
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  keys  DOCUMENT ME!
      */
     protected void setKeysEDT(final Object... keys) {
         setKeysEDT(Arrays.asList(keys));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Future<?> refreshByNotify() {
         synchronized (lock) {
             if (refreshFuture == null) {
                 addNotify();
             }
         }
 
         return refreshFuture;
 
         // we do not refresh the already present nodes anymore since we don't want all sub-objects to query the db for
         // changes. it is decided that the user has to use the ABF to do any db changes or he is responsible for data
         // consistency to himself.
     }
 }
