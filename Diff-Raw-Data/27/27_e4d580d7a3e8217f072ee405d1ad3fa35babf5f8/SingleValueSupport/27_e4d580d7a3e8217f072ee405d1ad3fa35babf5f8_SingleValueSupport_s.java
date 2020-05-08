 package com.versionone.taskview.views.editors;
 
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 
import com.versionone.common.sdk.ApiDataLayer;
 import com.versionone.common.sdk.ValueId;
 import com.versionone.common.sdk.Workitem;
 import com.versionone.taskview.Activator;
 import com.versionone.taskview.views.properties.WorkitemPropertySource;
 
 public class SingleValueSupport extends EditingSupport {
 
     private static final String ERROR_VALUE = "*** Error ***";
     private String propertyName;
    private final ApiDataLayer dataLayer;
     private ValueId currentValue;
     private final ISelectionProvider selectionProvider;
     
     
     public SingleValueSupport(String propertyName, TreeViewer viewer, ISelectionProvider selectionProvider) {
         super(viewer);
         this.propertyName = propertyName;
         this.selectionProvider = selectionProvider;
        dataLayer = ApiDataLayer.getInstance();
     }
     
     @Override
     protected boolean canEdit(Object element) {
         return true;
     }
 
     @Override
     protected CellEditor getCellEditor(Object element) {
         Workitem workitem = ((Workitem) element);
         return new SingleValueEditor(((TreeViewer)getViewer()).getTree(), workitem.getTypePrefix(), propertyName);
     }
 
     @Override
     protected Object getValue(Object element) {
         try {            
             ValueId value = (ValueId)((Workitem) element).getProperty(propertyName);
             currentValue = value;
             return currentValue;
         } catch (Exception e) {
             Activator.logError(e);
             return ERROR_VALUE;
         }
     }
 
     @Override
     protected void setValue(Object element, Object value) {
        Workitem workitem = ((Workitem) element);
        ValueId newValue = (ValueId) value;
        if (dataLayer.getListPropertyValues(workitem.getTypePrefix(), propertyName).contains(newValue) &&
                !currentValue.equals(newValue) && newValue != null) {
            workitem.setProperty(propertyName, newValue);
        }
         
         getViewer().update(element, null);
         selectionProvider.setSelection(new StructuredSelection(new WorkitemPropertySource(workitem, getViewer())));
     }

}
