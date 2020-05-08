 package org.xendan.logmonitor.idea;
 
 import com.jgoodies.binding.beans.BeanAdapter;
 import com.jgoodies.binding.value.AbstractValueModel;
 import com.jgoodies.binding.value.ValueModel;
 
 /**
  * User: id967161
  * Date: 17/09/13
  */
 public class VerboseBeanAdapter<T>  {
     private final BeanAdapter<T> adapter;
 
     public VerboseBeanAdapter(T bean) {
         adapter = new BeanAdapter<T>(bean);
     }
 
     public T getBean() {
         return adapter.getBean();
     }
 
     public ValueModel getPropertyModel(String property) {
        return new VerboseModel(adapter.getValueModel(property));
     }
 
     public void setBean(T newBean) {
         adapter.setBean(newBean);
     }
 
     private class VerboseModel extends AbstractValueModel {
         private final ValueModel valueModel;
 
         public VerboseModel(ValueModel valueModel) {
             this.valueModel = valueModel;
         }
 
         @Override
         public Object getValue() {
             return valueModel.getValue();
         }
 
         @Override
         public void setValue(Object newValue) {
             Object oldValue = getValue();
             valueModel.setValue(newValue);
             firePropertyChange("value", oldValue, newValue);
         }
     }
 }
