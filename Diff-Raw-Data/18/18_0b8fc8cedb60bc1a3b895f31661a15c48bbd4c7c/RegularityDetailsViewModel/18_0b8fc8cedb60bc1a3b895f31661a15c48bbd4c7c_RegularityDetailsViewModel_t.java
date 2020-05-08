 package org.munta.gui;
 
 import javax.swing.JList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import org.munta.model.Regularity;
 
 public class RegularityDetailsViewModel
         extends AbstractCollectionViewModel<Regularity>
         implements ListSelectionListener {
 
     private RegularityViewModel regularityViewModel;
 
     public RegularityDetailsViewModel(RegularityViewModel regularityViewModel) {
         this.regularityViewModel = regularityViewModel;
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
            
         Regularity r = regularityViewModel.getModelObjectAt(index).getValue();
         
         this.elementAdded("Target attribute:");
         this.elementAdded(r.getTarget());
         this.elementAdded(" ");
         
         this.elementAdded("Conditions:");
         for (Object o : r.getConditions()) {
             this.elementAdded(o);
         }
         this.elementAdded(" ");
         
         this.elementAdded("Context:");
         for (Object o : r.getContext()) {
             this.elementAdded(o);
         }
 
         runUpdate();
     }
 }
