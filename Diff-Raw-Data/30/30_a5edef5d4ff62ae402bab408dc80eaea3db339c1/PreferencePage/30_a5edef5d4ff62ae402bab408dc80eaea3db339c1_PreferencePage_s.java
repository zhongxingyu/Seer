 /**
 * Copyright (c) 2012 Tobias Boehm.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Tobias Boehm - initial API and implementation.
  */
 
 package org.eclipse.recommenders.internal.codesearch.rcp;
 
 import java.io.File;
 
 import javax.swing.plaf.basic.BasicOptionPaneUI.ButtonActionListener;
 
 import org.apache.lucene.store.Directory;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.ColorFieldEditor;
 import org.eclipse.jface.preference.DirectoryFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.IntegerFieldEditor;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.CodeIndexer;
 import org.eclipse.recommenders.injection.InjectionService;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.dnd.SwtUtil;
 
 import com.google.inject.Inject;
 
 public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
     
     public static final String P_KEEP_IN_SYNC = "recommenders.codesearch.index.keep_in_sync";
     public static final String P_MAX_HITS = "recommenders.codesearch.search.no_of_hits";
     public static final String P_HIGHLIGHT_SUMMARY = "recommenders.codesearch.index.highlight_in_summary";
     public static final String P_HIGHLIGHT_COLOR = "recommenders.codesearch.index.highlight_color";
     public static final String P_USE_CUSTOM_LOC = "recommenders.codesearch.index.use_custom_location";
     public static final String P_CUSTOM_LOC = "recommenders.codesearch.index.custom_location";
     private DirectoryFieldEditor dir;
     private Composite dirParent;
     private ColorFieldEditor colorF;
    
     
     @Override
     protected void createFieldEditors() {
         
         addField(new BooleanFieldEditor(P_KEEP_IN_SYNC, "Keep index in sync with workspace.", getFieldEditorParent()));
         
         addField(new BooleanFieldEditor(P_HIGHLIGHT_SUMMARY, "Highlight matching terms in summary.", getFieldEditorParent()));
         
         final String[] s = CodesearchIndexPlugin.getDefault().getPreferenceStore().getString(PreferencePage.P_HIGHLIGHT_COLOR).split(",");
         colorF = new ColorFieldEditor( P_HIGHLIGHT_COLOR, "Highlight color",  getFieldEditorParent());
         colorF.getColorSelector().setColorValue(new RGB(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2])));
         addField(colorF);
         
         addField(new BooleanFieldEditor(P_USE_CUSTOM_LOC, "Use a Custom location for index.", getFieldEditorParent()));
         dirParent= getFieldEditorParent();
         dir = new DirectoryFieldEditor(P_CUSTOM_LOC, "Directory to save index", dirParent);
         
         if(!CodesearchIndexPlugin.getDefault().getPreferenceStore().getBoolean(P_USE_CUSTOM_LOC)){
             dir.setEnabled(false, dirParent);
         }           
         addField(dir);
         
         final Composite parent = getFieldEditorParent();
         addField(new IntegerFieldEditor(P_MAX_HITS, "Max no of Hits to receive.", parent));
         final Button clear = new Button(parent, SWT.WRAP|SWT.CENTER);
         clear.setText("clear Index");
         clear.addSelectionListener( new SelectionListener() {            
             @Override
             public void widgetSelected(SelectionEvent e) {
                 File folder = CodesearchIndexModule.findOrCreateIndexFolder();
                 boolean confirmed = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
                         "Warning - deleting the whole search index", 
                         "You are about to delete the whole search index for code search. This cannot be rolled back."
                         + "Are you sure you want to delete following folder?\n"
                         + folder.getAbsolutePath()
                         + "\n\nPress OK to delete or press Cancel to return.");
                 if(confirmed){                    
                     deleteFolder(folder);
                 }
 
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                          
             }
         });
         final Label label = new Label(parent, SWT.WRAP | SWT.BORDER);
         final GridData data = GridDataFactory.fillDefaults().grab(true, true).span(2, 1).hint(100, 100).create();
         data.horizontalSpan = 2;
         data.widthHint = 200;
         data.grabExcessHorizontalSpace = true;
         label.setLayoutData(data);
 
         label.setText("Note that building the initial index is a long running operation. "
                 + "Its duration depends on workspace size and number project dependencies and "
                 + "may take up to 30 minutes.\n\nThis operation can be canceled at any time; "
                 + "Indexing operation starts from the last sucessfully indexed project.");
     }
 
     @Override
     public void init(final IWorkbench workbench) {
         setPreferenceStore(CodesearchIndexPlugin.getDefault().getPreferenceStore());
     }
 
     public static boolean isActive() {
         final CodesearchIndexPlugin plugin = CodesearchIndexPlugin.getDefault();
         final IPreferenceStore store = plugin.getPreferenceStore();
         final boolean res = store.getBoolean(P_KEEP_IN_SYNC);
         return res;
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent event) {       
         super.propertyChange(event);
         
         if(event.getSource() instanceof BooleanFieldEditor && 
                 ((BooleanFieldEditor) event.getSource()).getPreferenceName().equals(P_KEEP_IN_SYNC)){
             
             if((Boolean) event.getNewValue()){
 
               boolean confirmed = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
                                     "Warning - Long running operation", 
                                     "Note that building the initial index is a long running operation. "
                                     + "Press OK to start indexing now.\nOtherwise it will be started on next restart."); 
               IndexUpdateService service = InjectionService.getInstance().getInjector()
                       .getInstance(IndexUpdateService.class);
               
               if(confirmed){
                   service.reindexWorkspace();
               }
             }
         }
         else  if(event.getSource() instanceof BooleanFieldEditor && 
                 ((BooleanFieldEditor) event.getSource()).getPreferenceName().equals(P_USE_CUSTOM_LOC)){
             if((Boolean) event.getNewValue()){
                 dir.setEnabled(true, dirParent);
                 IPreferenceStore store = CodesearchIndexPlugin.getDefault().getPreferenceStore();
                 store.setValue(P_USE_CUSTOM_LOC, ((BooleanFieldEditor) event.getSource()).getBooleanValue());
                 Display.getDefault().asyncExec(new Runnable() {
                     public void run() {
                         
                         IWorkbench wb = PlatformUI.getWorkbench();
                         boolean confirmed = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
                                 "Info - Eclipse need a restart to affect changes", 
                                 "Note that index captures all the information relatively to workspace root except for 3rd party libraries.\n"
                                 + "Since libraries are shared across workspaces and they reside inside eclipse installation, abosulte paths are"
                                 + "used when capturing examples from external libraries.\n"
                                 + "This may cause problems when the index is shared across multiple installations.\n"
                                 +  "Press OK to restart now. Otherwise restart manually later.\n"
                                 );
                         if(confirmed){
                             
                             wb.restart();
                         }
                       
                     }
                 });
             }
             if(!(Boolean) event.getNewValue()){
               dir.setEnabled(false, dirParent);
               IPreferenceStore store = CodesearchIndexPlugin.getDefault().getPreferenceStore();
               store.setValue(P_USE_CUSTOM_LOC, ((BooleanFieldEditor) event.getSource()).getBooleanValue());
               Display.getDefault().asyncExec(new Runnable() {
                   public void run() {
                       
                       //store.setV
                       IWorkbench wb = PlatformUI.getWorkbench();
                       boolean confirmed = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
                               "Warning - Eclipse need a restart to affect changes", 
                               "Press OK to restart now. Otherwise restart manually later.\n"
                              
                               ); 
                       if(confirmed){
                           
                           wb.restart();
                       }
                   }
               });
             }
         }
         else if(event.getSource() instanceof DirectoryFieldEditor){
             IPreferenceStore store = CodesearchIndexPlugin.getDefault().getPreferenceStore();
             store.setValue(P_CUSTOM_LOC, ((DirectoryFieldEditor) event.getSource()).getStringValue());
             Display.getDefault().asyncExec(new Runnable() {
                 public void run() {
                     
                     //store.setV
                     IWorkbench wb = PlatformUI.getWorkbench();
                     boolean confirmed = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), 
                             "Warning - Eclipse need a restart to affect changes", 
                             "Press OK to restart now. Otherwise restart manually later.\n"
                            
                             ); 
                     if(confirmed){
                         
                         wb.restart();
                     }
                 }
             });
         }   
     }
     private static void deleteFolder(File folder) {
         File[] files = folder.listFiles();
         if(files!=null) { 
             for(File f: files) {
                 if(f.isDirectory()) {
                     deleteFolder(f);
                 } else {
                     f.delete();
                 }
             }
         }
         //folder.delete();
     }
     
     
 }
