 package com.versionone.taskview.views.actions;
 
import javax.xml.bind.ValidationException;

 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 
 import com.versionone.common.sdk.ApiDataLayer;
 import com.versionone.common.sdk.DataLayerException;
 import com.versionone.common.sdk.ValidatorException;
 import com.versionone.taskview.Activator;
 import com.versionone.taskview.views.ErrorMessageDialog;
 import com.versionone.taskview.views.TaskView;
 
 class SaveAction extends Action {
 
     private static final ApiDataLayer DATA = ApiDataLayer.getInstance();
 
     private final TaskView workitemView;
     private final TreeViewer treeViewer;
 
     public SaveAction(TaskView workItemView) {
         this.workitemView = workItemView;
         this.treeViewer = workItemView.getViewer();
 
         setText("Save");
         setToolTipText("Save");
         setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.SAVE_IMAGE_ID));
     }
 
     @Override
     public void run() {
         workitemView.getActionsManager().enableAction(false, false);
         if (treeViewer.isCellEditorActive()) {
             treeViewer.getTree().getShell().traverse(SWT.TRAVERSE_TAB_NEXT);
         }
 
         try {
             DATA.commitChanges();
             Activator.connect();
         } catch (ValidatorException e) {
             Activator.logWarning(e.getMessage());
             ErrorMessageDialog errorMessage = new ErrorMessageDialog(treeViewer.getTree().getShell(), e.getMessage());
             errorMessage.setBlockOnOpen(true);
             errorMessage.open();             
         } catch(DataLayerException e) {            
             Activator.logError(e);
             workitemView.showMessage("Error saving task. Check Error log for more information.");
         } catch (Exception e) {
             Activator.logError(e);
             workitemView.showMessage("Error saving task. Check Error log for more information.");
         }
 
         if (workitemView.loadDataToTable())
             workitemView.getActionsManager().enableAction(true, true);
     }
 }
