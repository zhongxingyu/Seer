 package org.munta.gui;
 
 import javax.swing.JList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.munta.model.Entity;
 import org.munta.model.Attribute;
 
 public class EntityDetailsViewModel
         extends AbstractCollectionViewModel<Attribute>
         implements ListSelectionListener {
 
     private EntityViewModel entityViewModel;
 
     public EntityDetailsViewModel(EntityViewModel entityViewModel) {
         this.entityViewModel = entityViewModel;
     }
 
     @Override
     public Object getElementAt(int i) {
         return getModelObjectAt(i);
     }
 
     @Override
     public void valueChanged(ListSelectionEvent lse) {
         if (lse.getValueIsAdjusting()) {
             return;
         }
 
         if (!JList.class.isAssignableFrom(lse.getSource().getClass())) {
             return;
         }
         JList jList = (JList) lse.getSource();
         int index = jList.getSelectedIndex();
 
         this.elementRemoved(null);
         runUpdate();
        
        if(index == -1)
            return;
 
         Entity e = entityViewModel.getModelObjectAt(index);
         
         for (Object o : e.getAttributes()) {
             this.elementAdded(o);
         }
 
         runUpdate();
     }
 }
