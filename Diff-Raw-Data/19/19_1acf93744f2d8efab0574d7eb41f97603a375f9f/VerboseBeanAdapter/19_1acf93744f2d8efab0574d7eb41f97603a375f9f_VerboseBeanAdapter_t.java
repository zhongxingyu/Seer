 package org.xendan.logmonitor.idea;
 
 import com.jgoodies.binding.beans.BeanAdapter;
 import com.jgoodies.binding.value.AbstractValueModel;
 import com.jgoodies.binding.value.ValueModel;
 
import java.util.ArrayList;
import java.util.List;

 /**
  * User: id967161
  * Date: 17/09/13
  */
 public class VerboseBeanAdapter<T>  {
     private final BeanAdapter<T> adapter;
    private List<VerboseModel> models = new ArrayList<VerboseModel>();
 
     public VerboseBeanAdapter(T bean) {
         adapter = new BeanAdapter<T>(bean);
     }
 
     public T getBean() {
         return adapter.getBean();
     }
 
     public ValueModel getPropertyModel(String property) {
        VerboseModel model = new VerboseModel(adapter.getValueModel(property));
        models.add(model);
        return model;
     }
 
     public void setBean(T newBean) {
         adapter.setBean(newBean);
        for (VerboseModel model : models) {
            model.fireChanged(model.getValue(), null);
        }
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
            fireChanged(newValue, oldValue);
        }

        private void fireChanged(Object newValue, Object oldValue) {
             firePropertyChange("value", oldValue, newValue);
         }
     }
 }
