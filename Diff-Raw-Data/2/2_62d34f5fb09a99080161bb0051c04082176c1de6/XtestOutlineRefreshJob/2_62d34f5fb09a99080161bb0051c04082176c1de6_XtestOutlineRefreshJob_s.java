 package org.xtest.ui.outline;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
 import org.eclipse.xtext.ui.editor.outline.impl.OutlinePage;
 import org.eclipse.xtext.ui.editor.outline.impl.OutlineRefreshJob;
 import org.eclipse.xtext.ui.editor.outline.impl.OutlineTreeState;
 
 /**
  * Custom outline refresh job to grey-out the outline view while tests are running and automatically
  * expand failed tests.
  * 
  * @author Michael Barry
  */
 public class XtestOutlineRefreshJob extends OutlineRefreshJob {
     private OutlinePage outlinePage;
 
     /**
      * Executes a task in the UI thread to enable or disable the outline view
      * 
      * @param enable
      *            True to enable, false to disable
      */
     public void setControlEnabled(final boolean enable) {
        Display.getDefault().syncExec(new Runnable() {
             @Override
             public void run() {
                 outlinePage.getTreeViewer().getControl().setEnabled(enable);
             }
         });
     }
 
     @Override
     public void setOutlinePage(OutlinePage outlinePage) {
         this.outlinePage = outlinePage;
         super.setOutlinePage(outlinePage);
     }
 
     @Override
     protected IOutlineNode refreshOutlineModel(IProgressMonitor monitor,
             OutlineTreeState formerState, OutlineTreeState newState) {
         IOutlineNode refreshOutlineModel = super
                 .refreshOutlineModel(monitor, formerState, newState);
         if (!monitor.isCanceled()) {
             setControlEnabled(true);
         }
         return refreshOutlineModel;
     }
 
     @Override
     protected void restoreChildrenSelectionAndExpansion(IOutlineNode parent, Resource resource,
             OutlineTreeState formerState, OutlineTreeState newState) {
         super.restoreChildrenSelectionAndExpansion(parent, resource, formerState, newState);
         for (IOutlineNode child : parent.getChildren()) {
             if (child instanceof XTestEObjectNode && ((XTestEObjectNode) child).getFailed()) {
                 // Show failed nodes
                 newState.getExpandedNodes().add(child);
             } else {
                 // Attempt to hide passed nodes?
                 // newState.getExpandedNodes().remove(child);
             }
         }
     }
 
     @Override
     protected IStatus run(IProgressMonitor monitor) {
         IStatus status = org.eclipse.core.runtime.Status.OK_STATUS;
         if (outlinePage != null) {
             status = super.run(monitor);
         }
         return status;
     }
 }
