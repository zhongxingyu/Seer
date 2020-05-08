 package com.ingemark.requestage.plugin;
 
 import static com.ingemark.requestage.Util.event;
 import static com.ingemark.requestage.plugin.RequestAgePlugin.EVT_RUN_SCRIPT;
 import static com.ingemark.requestage.plugin.ui.RequestAgeView.requestAgeView;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.debug.ui.ILaunchShortcut;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.part.FileEditorInput;
 
 public class LaunchShortcut implements ILaunchShortcut
 {
   @Override public void launch(ISelection selection, String mode) {
     launch(((IFile)((StructuredSelection)selection).getFirstElement()));
   }
   @Override public void launch(IEditorPart editor, String mode) {
     launch(((FileEditorInput)editor.getEditorInput()).getFile());
   }
   void launch(IFile in) {
     requestAgeView.statsParent.notifyListeners(EVT_RUN_SCRIPT,
         event(in.getLocation().toOSString()));
   }
 }
