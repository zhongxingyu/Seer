 package com.od.jtimeseries.ui.selector.shared;
 
 import com.od.jtimeseries.util.identifiable.Identifiable;
 import com.od.swing.action.ListSelectionActionModel;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 07/01/11
  * Time: 06:58
  * To change this template use File | Settings | File Templates.
  */
 public class IdentifiableListActionModel extends ListSelectionActionModel<Identifiable> {
 
     private Map<Class, List<? extends Identifiable>> nodesByClass = new HashMap<Class, List<? extends Identifiable>>();
 
     public void setSelected(List<Identifiable> series) {
        super.setSelected(series);
         nodesByClass.clear();
     }
 
     public void addSelected(Identifiable series) {
        super.addSelected(series);
         nodesByClass.clear();
     }
 
     public void removeSelected(Identifiable series) {
        super.removeSelected(series);
         nodesByClass.clear();
     }
 
     public void setSelected(Identifiable series){
        super.setSelected(series);
         nodesByClass.clear();
     }
 
     public void doClearActionModelState() {
        super.doClearActionModelState();
         nodesByClass.clear();
     }
 
     public boolean isSelectionLimitedToType(Class type) {
         return getSelected(type).size() == getSelected().size();
     }
 
     public <C extends Identifiable> java.util.List<C> getSelected(Class<C> clazz) {
         List<C> identifiables = (List<C>)nodesByClass.get(clazz);
         if ( identifiables == null ) {
             identifiables = new LinkedList<C>();
             List<Identifiable> l = getSelected();
             for ( Identifiable i : l) {
                 if ( clazz.isAssignableFrom(i.getClass())) {
                     identifiables.add((C)i);
                 }
             }
             nodesByClass.put(clazz, identifiables);
         }
         return identifiables;
     }
 
 }
