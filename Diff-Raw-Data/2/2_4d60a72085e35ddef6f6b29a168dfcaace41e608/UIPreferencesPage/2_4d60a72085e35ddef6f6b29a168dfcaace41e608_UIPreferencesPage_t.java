 package de.objectcode.time4u.client.ui.preferences;
 
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IntegerFieldEditor;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 import de.objectcode.time4u.client.ui.UIPlugin;
 
 public class UIPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
 {
   public void init(final IWorkbench workbench)
   {
     setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
   }
 
   @Override
   protected void createFieldEditors()
   {
     addField(new BooleanFieldEditor(PreferenceConstants.UI_SHOW_TRAY_ICON, "Show &Tray icon", getFieldEditorParent()));
 
     final IntegerFieldEditor taskHistory = new IntegerFieldEditor(PreferenceConstants.UI_TASK_HISTORY_SIZE,
        "&Task history size", getFieldEditorParent());
     taskHistory.setValidRange(0, 30);
     addField(taskHistory);
   }
 
 }
