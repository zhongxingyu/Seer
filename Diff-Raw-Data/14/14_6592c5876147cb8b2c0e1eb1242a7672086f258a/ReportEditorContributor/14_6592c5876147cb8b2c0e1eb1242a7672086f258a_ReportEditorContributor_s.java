 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.splash.editors;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
 
 /**
  * Class that contributes to toolbar
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class ReportEditorContributor extends MultiPageEditorActionBarContributor {
 
     private AddTextAction addTextAction;
     private AddImageAction addImageAction;
     private ReportGUIEditor part;
 
     @Override
     public void setActivePage(IEditorPart activeEditor) {
         if (activeEditor instanceof ReportGUIEditor) {
             part = (ReportGUIEditor)activeEditor;
             addTextAction.setEditor(part);
             addImageAction.setEditor(part);
         }
     }
 
     @Override
     public void setActiveEditor(IEditorPart part) {
         if (part instanceof ReportGUIEditor)
             part = (ReportGUIEditor)part;
     }
 
     @Override
     public void contributeToToolBar(IToolBarManager toolBarManager) {
         toolBarManager.add(new Separator());
         addTextAction = new AddTextAction();
         addImageAction = new AddImageAction();
         toolBarManager.add(addTextAction);
         toolBarManager.add(addImageAction);
 
     }
 
     /**
      * Class for an action that adds text to a report
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     public class AddTextAction extends Action {
         private ReportGUIEditor editor;
 
         /**
          * Constructor
          * 
          * @param text
          * @param image
          */
         public AddTextAction() {
             // TODO image
            super("AddText", PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
         }
 
         public void setEditor(ReportGUIEditor editor) {
             this.editor = editor;
 
         }
 
         @Override
         public void run() {
             editor.addNewText();
         }
 
     }
 
     /**
      * Class for an action that adds image to a report
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     public class AddImageAction extends Action {
         private ReportGUIEditor editor;
 
         /**
          * Constructor
          * 
          * @param text
          * @param image
          */
         public AddImageAction() {
            super("Add image", PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
         }
 
         /**
          * Setter for editor field
          * 
          * @param editor
          */
         public void setEditor(ReportGUIEditor editor) {
             this.editor = editor;
 
         }
 
         @Override
         public void run() {
             editor.addNewImage();
         }
 
     }
 }
