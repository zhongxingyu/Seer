 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.breadcrumb;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class DefaultBreadCrumbModel implements BreadCrumbModel {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient Set<BreadCrumbModelListener> listeners;
     private final transient List<BreadCrumb> data;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DefaultBreadCrumbModel object.
      */
     public DefaultBreadCrumbModel() {
         listeners = new HashSet<BreadCrumbModelListener>();
         data = new ArrayList<BreadCrumb>();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void appendCrumb(final BreadCrumb bc) {
         appendCrumbSilently(bc);
         fireBreadCrumbAdded(new BreadCrumbEvent(this, bc));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  bc  DOCUMENT ME!
      */
     private void appendCrumbSilently(final BreadCrumb bc) {
         data.add(bc);
         bc.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     final BreadCrumbEvent bce = new BreadCrumbEvent(DefaultBreadCrumbModel.this, bc);
                     fireBreadCrumbActionPerformed(bce);
                 }
             });
     }
 
     @Override
     public List<BreadCrumb> getAllCrumbs() {
         return new ArrayList<BreadCrumb>(data);
     }
 
     @Override
     public BreadCrumb getCrumbAt(final int position) {
         return data.get(position);
     }
 
     @Override
     public int getPositionOf(final BreadCrumb bc) {
         return data.indexOf(bc);
     }
 
     @Override
     public int getSize() {
         return data.size();
     }
 
     @Override
     public BreadCrumb getLastCrumb() {
         return data.get(data.size() - 1);
     }
 
     @Override
     public BreadCrumb getFirstCrumb() {
         return data.get(0);
     }
 
     @Override
     public void clear() {
         data.clear();
        fireBreadCrumbModelChanged(new BreadCrumbEvent(this));
     }
 
     @Override
     public void removeTill(final BreadCrumb bc) {
         final int lastIndex = data.lastIndexOf(bc);
         if (lastIndex != -1) {
             for (int i = data.size() - 1; i > lastIndex; --i) {
                 data.remove(i);
             }
         }
         fireBreadCrumbModelChanged(new BreadCrumbEvent(this));
     }
 
     @Override
     public void startWithNewCrumb(final BreadCrumb bc) {
         data.clear();
         appendCrumbSilently(bc);
         fireBreadCrumbModelChanged(new BreadCrumbEvent(this));
     }
 
     @Override
     public void addBreadCrumbModelListener(final BreadCrumbModelListener bcListener) {
         synchronized (listeners) {
             listeners.add(bcListener);
         }
     }
 
     @Override
     public void removeBreadCrumbModelListener(final BreadCrumbModelListener bcListener) {
         synchronized (listeners) {
             listeners.remove(bcListener);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  bce  DOCUMENT ME!
      */
     public void fireBreadCrumbModelChanged(final BreadCrumbEvent bce) {
         final Iterator<BreadCrumbModelListener> it;
         synchronized (listeners) {
             it = new HashSet<BreadCrumbModelListener>(listeners).iterator();
         }
 
         while (it.hasNext()) {
             it.next().breadCrumbModelChanged(bce);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  bce  DOCUMENT ME!
      */
     public void fireBreadCrumbAdded(final BreadCrumbEvent bce) {
         final Iterator<BreadCrumbModelListener> it;
         synchronized (listeners) {
             it = new HashSet<BreadCrumbModelListener>(listeners).iterator();
         }
 
         while (it.hasNext()) {
             it.next().breadCrumbAdded(bce);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  bce  DOCUMENT ME!
      */
     public void fireBreadCrumbActionPerformed(final BreadCrumbEvent bce) {
         final Iterator<BreadCrumbModelListener> it;
         synchronized (listeners) {
             it = new HashSet<BreadCrumbModelListener>(listeners).iterator();
         }
 
         while (it.hasNext()) {
             it.next().breadCrumbActionPerformed(bce);
         }
     }
 }
