 package de.sofd.viskit.controllers;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Collection;
 import java.util.IdentityHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import de.sofd.swing.BoundedListSelectionModel;
 import de.sofd.viskit.ui.imagelist.JImageListView;
 
 /**
  * Controller that maintains a list of references to {@link JImageListView}
  * objects and a boolean "enabled" flag. If the flag is set to true, the
  * selections of all the lists are kept synchronized.
  * <p>
  * If the {@link #isKeepRelativeSelectionIndices()} flag is also true, the
  * differences between the selection indices in different lists will be kept
  * constant (rather than all selection indices being set to the same value). For
  * this to work if the selections are moved to the beginning/end of one of the
  * lists, the controller must be able to constrain the selections of the lists
  * appropriately, such that, depending on the situation, some indices at the
  * beginning and/or end of the lists can no longer be selected. To make this
  * work, the selection models of the participating lists (
  * {@link JImageListView#setSelectionModel(ListSelectionModel)} must be set to
  * instances of {@link BoundedListSelectionModel}.
  * 
  * @author Sofd GmbH
  */
 public class ImageListViewSelectionSynchronizationController {
 
     private boolean enabled;
     public static final String PROP_ENABLED = "enabled";
     private boolean keepRelativeSelectionIndices;
     public static final String PROP_KEEPRELATIVESELECTIONINDICES = "keepRelativeSelectionIndices";
     private Map<JImageListView, Integer> listsAndSelectionIndices = new IdentityHashMap<JImageListView, Integer>();
 
     public ImageListViewSelectionSynchronizationController() {
     }
 
     public ImageListViewSelectionSynchronizationController(JImageListView... lists) {
         setLists(lists);
     }
 
     /**
      * The set of {@link JImageListView}s that this controller currently
      * synchronizes.
      * 
      * @return the value of lists
      */
     public JImageListView[] getLists() {
         return (JImageListView[]) listsAndSelectionIndices.keySet().toArray(new JImageListView[0]);
     }
     
     public boolean containsList(JImageListView l) {
         return listsAndSelectionIndices.containsKey(l);
     }
 
     public void addList(JImageListView l) {
         if (! listsAndSelectionIndices.containsKey(l)) {
             putSelectionIndex(l, l.getLeadSelectionIndex());
             l.addListSelectionListener(selectionHandler);
            updateSelectionBounds();
         }
     }
 
     public void removeList(JImageListView l) {
         if (null != listsAndSelectionIndices.remove(l)) {
             l.removeListSelectionListener(selectionHandler);
            updateSelectionBounds();
         }
     }
 
     private void putSelectionIndex(JImageListView l, int idx) {
         if (idx == -1) {
             listsAndSelectionIndices.put(l, null);
         } else {
             listsAndSelectionIndices.put(l, idx);
         }
     }
     
     /**
      * Set set of {@link JImageListView}s that this controller currently
      * synchronizes.
      * 
      * @param lists
      */
     public void setLists(JImageListView[] lists) {
         for (JImageListView l : listsAndSelectionIndices.keySet()) {
             removeList(l);
         }
         for (JImageListView l : lists) {
             addList(l);
         }
     }
 
     /**
      * Set set of {@link JImageListView}s that this controller currently
      * synchronizes.
      * 
      * @param lists
      */
     public void setLists(Collection<JImageListView> lists) {
         for (JImageListView l : listsAndSelectionIndices.keySet()) {
             removeList(l);
         }
         for (JImageListView l : lists) {
             addList(l);
         }
     }
     
     // TODO: deal with selection model changes on the lists?
     
     private void recordSelectionIndices() {
         for (JImageListView l : getLists()) {
             putSelectionIndex(l, l.getLeadSelectionIndex());
         }
     }
     
     private void dumpSelectionIndices() {
         for (JImageListView l : listsAndSelectionIndices.keySet()) {
             System.out.print("" + listsAndSelectionIndices.get(l) + " ");
         }
         System.out.println();
     }
 
     /**
      * Get the value of enabled
      *
      * @return the value of enabled
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * Set the value of enabled
      *
      * @param enabled new value of enabled
      */
     public void setEnabled(boolean enabled) {
         boolean oldEnabled = this.enabled;
         this.enabled = enabled;
         recordSelectionIndices();
         if (enabled && isKeepRelativeSelectionIndices()) {
             updateSelectionBounds();
         } else {
             clearSelectionBounds();
         }
         propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, enabled);
     }
 
     /**
      * Get the value of keepRelativeSelectionIndices
      *
      * @return the value of keepRelativeSelectionIndices
      */
     public boolean isKeepRelativeSelectionIndices() {
         return keepRelativeSelectionIndices;
     }
 
     /**
      * Set the value of keepRelativeSelectionIndices
      *
      * @param keepRelativeSelectionIndices new value of keepRelativeSelectionIndices
      */
     public void setKeepRelativeSelectionIndices(boolean keepRelativeSelectionIndices) {
         boolean oldKeepRelativeSelectionIndices = this.keepRelativeSelectionIndices;
         this.keepRelativeSelectionIndices = keepRelativeSelectionIndices;
         recordSelectionIndices();
         if (isEnabled() && keepRelativeSelectionIndices) {
             updateSelectionBounds();
         } else {
             clearSelectionBounds();
         }
         propertyChangeSupport.firePropertyChange(PROP_KEEPRELATIVESELECTIONINDICES, oldKeepRelativeSelectionIndices, keepRelativeSelectionIndices);
     }
 
 
     private ListSelectionListener selectionHandler = new ListSelectionListener() {
         private boolean inProgrammedSelectionChange = false;
         @Override
         public void valueChanged(ListSelectionEvent e) {
             if (!isEnabled()) {
                 return;
             }
             if (inProgrammedSelectionChange) {
                 return;
             }
             inProgrammedSelectionChange = true;
             try {
                 JImageListView sourceList = (JImageListView) e.getSource();
                 int selIndex = sourceList.getLeadSelectionIndex();
                 if (selIndex >= 0) {
                     if (keepRelativeSelectionIndices) {
                         Integer lastSourceSelIdx = listsAndSelectionIndices.get(sourceList);
                         if (lastSourceSelIdx == null) {
                             // nothing was selected in sourceList previously. Just remember the newly selected index
                             putSelectionIndex(sourceList, selIndex);
                             updateSelectionBounds();
                         } else {
                             putSelectionIndex(sourceList, selIndex);
                             int change = selIndex - lastSourceSelIdx;
                             for (JImageListView l : getLists()) {
                                 if (l != sourceList) {
                                     Integer si = listsAndSelectionIndices.get(l);
                                     if (si != null) {
                                         int newsi = si + change;
                                         listsAndSelectionIndices.put(l, newsi);  // newsi==-1 is a valid value here, so DON'T call putSelectionIndex
                                         if (newsi >= l.getLength()) {
                                             newsi = l.getLength() - 1;
                                         }
                                         if (newsi < 0) {
                                             newsi = 0;
                                         }
                                         l.getSelectionModel().setSelectionInterval(newsi, newsi);
                                     }
                                 }
                             }
                         }
                     } else {
                         putSelectionIndex(sourceList, selIndex);
                         for (JImageListView l : getLists()) {
                             if (l != sourceList) {
                                 l.getSelectionModel().setSelectionInterval(selIndex, selIndex);
                                 putSelectionIndex(l, selIndex);
                             }
                         }
                     }
                 } else {
                     putSelectionIndex(sourceList, -1);
                 }
             } finally {
                 inProgrammedSelectionChange = false;
                 dumpSelectionIndices();
             }
         }
     };
 
     /**
      * Constrain all the lists' selections such that the relative differences
      * between them stay the same no matter how the user shifts any of the
      * selections upwards/downwards.
      * <p>
      * Only works if the lists' selection models are instances of
      * {@link BoundedListSelectionModel}.
      */
     private void updateSelectionBounds() {
         if (listsAndSelectionIndices.isEmpty()) {
             return;
         }
         if (!(listsAndSelectionIndices.keySet().iterator().next().getSelectionModel() instanceof BoundedListSelectionModel)) {
             return;
         }
         int minSelIndex = Integer.MAX_VALUE;
         int minHeadroom = Integer.MAX_VALUE;
         for (Entry<JImageListView, Integer> e : listsAndSelectionIndices.entrySet()) {
             JImageListView list = e.getKey();
             Integer i = e.getValue();
             if (i == null) {
                 continue;
             }
             if (i < minSelIndex) {
                 minSelIndex = i;
             }
             int length = list.getLength();
             if (length - 1 - i < minHeadroom) {
                 minHeadroom = length - 1 - i;
             }
         }
         for (JImageListView l : listsAndSelectionIndices.keySet()) {
             ListSelectionModel sm = l.getSelectionModel();
             if (!(sm instanceof BoundedListSelectionModel)) {
                 continue;
             }
             BoundedListSelectionModel bsm = (BoundedListSelectionModel) sm;
             Integer idx = listsAndSelectionIndices.get(l);
             if (idx == null) {
                 bsm.disableBounds();
                 l.disableVisibilityLimits();
                 continue;
             }
             int lower = idx - minSelIndex;
             int upper = idx + minHeadroom;
             if (lower < 0 || lower > upper) {
                 System.err.println("shouldn't happen: trying to set selection bounds of list (length=" + l.getLength() +
                                    " to [" + lower + "," + upper + "]");
                 continue;
             }
             bsm.setLowerBound(lower);
             l.setLowerVisibilityLimit(lower);
             bsm.setUpperBound(upper);
             l.setUpperVisibilityLimit(upper);
         }
     }
     
     private void clearSelectionBounds() {
         if (listsAndSelectionIndices.isEmpty()) {
             return;
         }
         for (JImageListView l : listsAndSelectionIndices.keySet()) {
             ListSelectionModel sm = l.getSelectionModel();
             if (!(sm instanceof BoundedListSelectionModel)) {
                 continue;
             }
             BoundedListSelectionModel bsm = (BoundedListSelectionModel) sm;
             bsm.disableBounds();
             l.disableVisibilityLimits();
         }
     }
 
     private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 
     /**
      * Add PropertyChangeListener.
      *
      * @param listener
      */
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.addPropertyChangeListener(listener);
     }
 
     /**
      * Remove PropertyChangeListener.
      *
      * @param listener
      */
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         propertyChangeSupport.removePropertyChangeListener(listener);
     }
 
 }
