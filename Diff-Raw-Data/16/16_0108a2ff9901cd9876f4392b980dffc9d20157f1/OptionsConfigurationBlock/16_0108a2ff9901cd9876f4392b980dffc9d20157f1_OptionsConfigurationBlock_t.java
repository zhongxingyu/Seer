 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Cameron Bateman/Oracle - adapted for use in JSF validation tooling
  *******************************************************************************/
 package org.eclipse.jst.jsf.ui.internal.validation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jst.jsf.core.internal.IJSFPreferenceModel;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.ui.internal.JSFUiPlugin;
 import org.eclipse.jst.jsf.validation.internal.ELValidationPreferences.Severity;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 import org.eclipse.ui.preferences.IWorkingCopyManager;
 import org.eclipse.ui.preferences.WorkingCopyManager;
 import org.osgi.service.prefs.BackingStoreException;
 
 /**
  * Abstract options configuration block providing a general implementation for setting up
  * an options configuration page.
  * 
  * @since 2.1
  */
 abstract class OptionsConfigurationBlock 
 {
     /**
      * The preference model to be used
      */
     protected final IJSFPreferenceModel     _prefs;
     
     static final class Key 
     {
         private String fQualifier;
         private String fKey;
         
         Key(String qualifier, String key) {
             fQualifier= qualifier;
             fKey= key;
         }
         
         /**
          * @return the key name
          */
         public String getName() {
             return fKey;
         }
         
 //        private IEclipsePreferences getNode(IScopeContext context, IWorkingCopyManager manager) {
 //            IEclipsePreferences node= context.getNode(fQualifier);
 //            if (manager != null) {
 //                return manager.getWorkingCopy(node);
 //            }
 //            return node;
 //            
 //        }
         
         /**
          * @param prefModel
          * @param context
          * @param manager
          * @return the value stored for the key
          */
         public Object getStoredValue(IJSFPreferenceModel prefModel, IScopeContext context, IWorkingCopyManager manager)
         {
             return prefModel.getStoredValueByKey(context, fKey);
         }
         
         /**
          * @param prefModel
          * @param context
          * @param manager
          * @return the stored value in prefModel under context for
          * this key
          */
         public Object getCurValue(IJSFPreferenceModel prefModel, IScopeContext context, IWorkingCopyManager manager) {
             //return getNode(context, manager).get(fKey, null);
             return prefModel.getValueByKey(context, fKey);
         }
         
         /**
          * @param prefModel
          * @param lookupOrder
          * @param ignoreTopScope
          * @param manager
          * @return the stored value in the prefModelunder context
          * using the list of lookupOrder for precedence of scopes
          * in which to look.  Return first found based on order in lookupOrder
          */
         public Object getCurValue(IJSFPreferenceModel prefModel, IScopeContext[] lookupOrder, boolean ignoreTopScope, IWorkingCopyManager manager) {
             for (int i= ignoreTopScope ? 1 : 0; i < lookupOrder.length; i++) {
                 Object value= getCurValue(prefModel, lookupOrder[i], manager);
                 if (value != null) {
                     return value;
                 }
             }
             return null;
         }
         
         /**
          * Set the stored value
          * @param prefModel 
          * @param context
          * @param value
          * @param manager
          * @return the old value or null if none
          */
         public Object setCurValue(IJSFPreferenceModel prefModel, IScopeContext context, Object value, IWorkingCopyManager manager) {
             return prefModel.setValueByKey(context, fKey, value);
 //            if (value != null) {
 //                getNode(context, manager).put(fKey, value);
 //            } else {
 //                getNode(context, manager).remove(fKey);
 //            }
         }
             
         /* (non-Javadoc)
          * @see java.lang.Object#toString()
          */
         public String toString() {
             return fQualifier + '/' + fKey;
         }
 
         /**
          * @return the plugin qualifier
          */
         public String getQualifier() {
             return fQualifier;
         }
 
     }
     
 
     static class ControlData {
         private Key fKey;
         private String[] fValues;
         
         ControlData(Key key, String[] values) {
             fKey= key;
             fValues= values;
         }
         
         Key getKey() {
             return fKey;
         }
         
         String getValue(boolean selection) {
             int index= selection ? 0 : 1;
             return fValues[index];
         }
         
         String getValue(int index) {
             return fValues[index];
         }       
         
         int getSelection(String value) {
             if (value != null) {
                 for (int i= 0; i < fValues.length; i++) {
                     if (value.equals(fValues[i])) {
                         return i;
                     }
                 }
             }
             return fValues.length -1; // assume the last option is the least severe
         }
     }
     
     private static final String REBUILD_COUNT_KEY= "preferences_build_requested"; //$NON-NLS-1$
     
     private static final String SETTINGS_EXPANDED= "expanded"; //$NON-NLS-1$
 
     private final ArrayList fCheckBoxes;
     private final ArrayList fComboBoxes;
     private final ArrayList fTextBoxes;
     private final HashMap fLabels;
     private final ArrayList fExpandedComposites;
     
     private SelectionListener fSelectionListener;
     private ModifyListener fTextModifyListener;
 
     // TODO: protected IStatusChangeListener fContext;
     private final IProject fProject; // project or null
     private final Key[] fAllKeys;
     
     private IScopeContext[] fLookupOrder;
     
     private Shell fShell;
 
     private final IWorkingCopyManager fManager;
     private IWorkbenchPreferenceContainer fContainer;
 
     private Map fDisabledProjectSettings; // null when project specific settings are turned off
     
     private int fRebuildCount; /// used to prevent multiple dialogs that ask for a rebuild
     
     OptionsConfigurationBlock(/*IStatusChangeListener context,*/IJSFPreferenceModel prefs, IProject project, Key[] allKeys, IWorkbenchPreferenceContainer container) {
         //fContext= context;
         fProject= project;
         fAllKeys= allKeys;
         fContainer= container;
         _prefs = prefs;
         
         if (container == null) {
             fManager= new WorkingCopyManager();
         } else {
             fManager= container.getWorkingCopyManager();
         }
         
         if (fProject != null) {
             fLookupOrder= new IScopeContext[] {
                 new ProjectScope(fProject),
                 new InstanceScope(),
                 new DefaultScope()
             };
         } else {
             fLookupOrder= new IScopeContext[] {
                 new InstanceScope(),
                 new DefaultScope()
             };
         }
         
         testIfOptionsComplete(allKeys);
         if (fProject == null || hasProjectSpecificOptions(fProject)) {
             fDisabledProjectSettings= null;
         } else {
             fDisabledProjectSettings= new IdentityHashMap();
             for (int i= 0; i < allKeys.length; i++) {
                 Key curr= allKeys[i];
                 fDisabledProjectSettings.put(curr, curr.getCurValue(_prefs, fLookupOrder, false, fManager));
             }
         }
         
         fCheckBoxes= new ArrayList();
         fComboBoxes= new ArrayList();
         fTextBoxes= new ArrayList(2);
         fLabels= new HashMap();
         fExpandedComposites= new ArrayList();
         
         fRebuildCount= getRebuildCount();
     }   
     
     /**
      * @return the preference container
      */
     protected final IWorkbenchPreferenceContainer getPreferenceContainer() {
         return fContainer;
     }
     
     /**
      * @param plugin
      * @param key
      * @return construct a new Key based on the on the plugin id and
      * preference key
      */
     protected static Key getKey(String plugin, String key) {
         return new Key(plugin, key);
     }
     
     /**
      * @param key
      * @return construct a new Key for a JSF core plugin preference
      */
     protected final static Key getJSFCoreKey(String key) {
         return getKey(JSFCorePlugin.PLUGIN_ID, key);
     }
     
     private void testIfOptionsComplete(Key[] allKeys) {
         for (int i= 0; i < allKeys.length; i++) {
             if (allKeys[i].getCurValue(_prefs, fLookupOrder, false, fManager) == null) {
                 JSFUiPlugin.log(IStatus.ERROR, "preference option missing: " + allKeys[i] + " (" + this.getClass().getName() +')');  //$NON-NLS-1$//$NON-NLS-2$
             }
         }
     }
     
     private int getRebuildCount() {
         return fManager.getWorkingCopy(new DefaultScope().getNode(JavaUI.ID_PLUGIN)).getInt(REBUILD_COUNT_KEY, 0);
     }
     
     private void incrementRebuildCount() {
         fRebuildCount++;
         fManager.getWorkingCopy(new DefaultScope().getNode(JavaUI.ID_PLUGIN)).putInt(REBUILD_COUNT_KEY, fRebuildCount);
     }
     
 //    public void selectOption(String key, String qualifier) {
 //        for (int i= 0; i < fAllKeys.length; i++) {
 //            Key curr= fAllKeys[i];
 //            if (curr.getName().equals(key) && curr.getQualifier().equals(qualifier)) {
 //                selectOption(curr);
 //            }
 //        }
 //    }
 //    
 //    public void selectOption(Key key) {
 //        Control control= findControl(key);
 //        if (control != null) {
 //            if (!fExpandedComposites.isEmpty()) {
 //                ExpandableComposite expandable= getParentExpandableComposite(control);
 //                if (expandable != null) {
 //                    for (int i= 0; i < fExpandedComposites.size(); i++) {
 //                        ExpandableComposite curr= (ExpandableComposite) fExpandedComposites.get(i);
 //                        curr.setExpanded(curr == expandable);
 //                    }
 //                    expandedStateChanged(expandable);
 //                }
 //            }
 //            control.setFocus();
 //        }
 //    }
     
     
     /**
      * @param project
      * @return true if there are project specific overrides in the 
      * preferences for 'project'
      */
     public final boolean hasProjectSpecificOptions(IProject project) {
         if (project != null) {
             IScopeContext projectContext= new ProjectScope(project);
             Key[] allKeys= fAllKeys;
             for (int i= 0; i < allKeys.length; i++) {
                 if (allKeys[i].getCurValue(_prefs, projectContext, fManager) != null) {
                     return true;
                 }
             }
         }
         return false;
     }   
             
     /**
      * @return the shell hosting the UI
      */
     protected Shell getShell() {
         return fShell;
     }
     
     /**
      * Set the shell hosting the UI.
      * @param shell
      */
     protected void setShell(Shell shell) {
         fShell= shell;
     }   
     
     /**
      * @param parent
      * @return the parent of the UI control to be created
      */
     protected abstract Control createContents(Composite parent);
     
 //    protected Button addCheckBox(Composite parent, String label, Key key, String[] values, int indent) {
 //        ControlData data= new ControlData(key, values);
 //        
 //        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 //        gd.horizontalSpan= 3;
 //        gd.horizontalIndent= indent;
 //        
 //        Button checkBox= new Button(parent, SWT.CHECK);
 //        checkBox.setFont(JFaceResources.getDialogFont());
 //        checkBox.setText(label);
 //        checkBox.setData(data);
 //        checkBox.setLayoutData(gd);
 //        checkBox.addSelectionListener(getSelectionListener());
 //        
 //        makeScrollableCompositeAware(checkBox);
 //        
 //        String currValue= getValue(key);
 //        checkBox.setSelection(data.getSelection(currValue) == 0);
 //        
 //        fCheckBoxes.add(checkBox);
 //        
 //        return checkBox;
 //    }
     
     /**
      * @param parent
      * @param label
      * @param key
      * @param values
      * @param indent
      * @param widthHint
      * @param listener
      * @return a check box styled button with a related link
      */
     protected Button addCheckBoxWithLink(Composite parent, String label, Key key, String[] values, int indent, int widthHint, SelectionListener listener) {
         ControlData data= new ControlData(key, values);
         
         GridData gd= new GridData(GridData.FILL, GridData.FILL, true, false);
         gd.horizontalSpan= 3;
         gd.horizontalIndent= indent;
         
         Composite composite= new Composite(parent, SWT.NONE);
         GridLayout layout= new GridLayout();
         layout.marginHeight= 0;
         layout.marginWidth= 0;
         layout.numColumns= 2;
         composite.setLayout(layout);
         composite.setLayoutData(gd);
         
         Button checkBox= new Button(composite, SWT.CHECK);
         checkBox.setFont(JFaceResources.getDialogFont());
         checkBox.setData(data);
         checkBox.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false));
         checkBox.addSelectionListener(getSelectionListener());
         
         gd= new GridData(GridData.FILL, GridData.CENTER, true, false);
         gd.widthHint= widthHint;
         
         Link link= new Link(composite, SWT.NONE);
         link.setText(label);
         link.setLayoutData(gd);
         if (listener != null) {
             link.addSelectionListener(listener);
         }
         
         makeScrollableCompositeAware(link);
         makeScrollableCompositeAware(checkBox);
         
         String currValue= getValue(key);
         checkBox.setSelection(data.getSelection(currValue) == 0);
         
         fCheckBoxes.add(checkBox);
         
         return checkBox;
     }
     
     /**
      * @param parent
      * @param label
      * @param key
      * @param values
      * @param valueLabels
      * @param indent
      * @return a Combo box added to parent with the label and key
      */
     protected Combo addComboBox(Composite parent, String label, Key key, String[] values, String[] valueLabels, int indent) {
         GridData gd= new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1);
         gd.horizontalIndent= indent;
                 
         Label labelControl= new Label(parent, SWT.LEFT);
         labelControl.setFont(JFaceResources.getDialogFont());
         
        labelControl.setText(label);
         labelControl.setLayoutData(gd);
                 
         Combo comboBox= newComboControl(parent, key, values, valueLabels);
         comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 
         fLabels.put(comboBox, labelControl);
         
         return comboBox;
     }
     
     Combo addInversedComboBox(Composite parent, String label, Key key, String[] values, String[] valueLabels, int indent) {
         GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
         gd.horizontalIndent= indent;
         gd.horizontalSpan= 3;
         
         Composite composite= new Composite(parent, SWT.NONE);
         GridLayout layout= new GridLayout();
         layout.marginHeight= 0;
         layout.marginWidth= 0;
         layout.numColumns= 2;
         composite.setLayout(layout);
         composite.setLayoutData(gd);
         
         Combo comboBox= newComboControl(composite, key, values, valueLabels);
         comboBox.setFont(JFaceResources.getDialogFont());
         comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
         
         Label labelControl= new Label(composite, SWT.LEFT | SWT.WRAP);
         labelControl.setText(label);
         labelControl.setLayoutData(new GridData());
         
         fLabels.put(comboBox, labelControl);
         return comboBox;
     }
     
     Combo newComboControl(Composite composite, Key key, String[] values, String[] valueLabels) {
         ControlData data= new ControlData(key, values);
         
         Combo comboBox= new Combo(composite, SWT.READ_ONLY);
         comboBox.setItems(valueLabels);
         comboBox.setData(data);
         comboBox.addSelectionListener(getSelectionListener());
         comboBox.setFont(JFaceResources.getDialogFont());
             
         makeScrollableCompositeAware(comboBox);
         
         String currValue= getValue(key);    
         comboBox.select(data.getSelection(currValue));
         
         fComboBoxes.add(comboBox);
         return comboBox;
     }
 
     Text addTextField(Composite parent, String label, Key key, int indent, int widthHint) {   
         Label labelControl= new Label(parent, SWT.WRAP);
         labelControl.setText(label);
         labelControl.setFont(JFaceResources.getDialogFont());
         labelControl.setLayoutData(new GridData());
                 
         Text textBox= new Text(parent, SWT.BORDER | SWT.SINGLE);
         textBox.setData(key);
         textBox.setLayoutData(new GridData());
         
         makeScrollableCompositeAware(textBox);
         
         fLabels.put(textBox, labelControl);
         
         String currValue= getValue(key);    
         if (currValue != null) {
             textBox.setText(currValue);
         }
         textBox.addModifyListener(getTextModifyListener());
 
         GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
         if (widthHint != 0) {
             data.widthHint= widthHint;
         }
         data.horizontalIndent= indent;
         data.horizontalSpan= 2;
         textBox.setLayoutData(data);
 
         fTextBoxes.add(textBox);
         return textBox;
     }
     
     ScrolledPageContent getParentScrolledComposite(Control control) {
         Control parent= control.getParent();
         while (!(parent instanceof ScrolledPageContent) && parent != null) {
             parent= parent.getParent();
         }
         if (parent instanceof ScrolledPageContent) {
             return (ScrolledPageContent) parent;
         }
         return null;
     }
     
     ExpandableComposite getParentExpandableComposite(Control control) {
         Control parent= control.getParent();
         while (!(parent instanceof ExpandableComposite) && parent != null) {
             parent= parent.getParent();
         }
         if (parent instanceof ExpandableComposite) {
             return (ExpandableComposite) parent;
         }
         return null;
     }
     
     private void makeScrollableCompositeAware(Control control) {
         ScrolledPageContent parentScrolledComposite= getParentScrolledComposite(control);
         if (parentScrolledComposite != null) {
             parentScrolledComposite.adaptChild(control);
         }
     }
     
     ExpandableComposite createStyleSection(Composite parent, String label, int nColumns) {
         ExpandableComposite excomposite= new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
         excomposite.setText(label);
         excomposite.setExpanded(false);
         excomposite.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
         excomposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, nColumns, 1));
         excomposite.addExpansionListener(new ExpansionAdapter() {
             public void expansionStateChanged(ExpansionEvent e) {
                 expandedStateChanged((ExpandableComposite) e.getSource());
             }
         });
         fExpandedComposites.add(excomposite);
         makeScrollableCompositeAware(excomposite);
         return excomposite;
     }
     
     final void expandedStateChanged(ExpandableComposite expandable) {
         ScrolledPageContent parentScrolledComposite= getParentScrolledComposite(expandable);
         if (parentScrolledComposite != null) {
             parentScrolledComposite.reflow(true);
         }
     }
     
     void restoreSectionExpansionStates(IDialogSettings settings) {
         for (int i= 0; i < fExpandedComposites.size(); i++) {
             ExpandableComposite excomposite= (ExpandableComposite) fExpandedComposites.get(i);
             if (settings == null) {
                 excomposite.setExpanded(i == 0); // only expand the first node by default
             } else {
                 excomposite.setExpanded(settings.getBoolean(SETTINGS_EXPANDED + String.valueOf(i)));
             }
         }
     }
     
     void storeSectionExpansionStates(IDialogSettings settings) {
         for (int i= 0; i < fExpandedComposites.size(); i++) {
             ExpandableComposite curr= (ExpandableComposite) fExpandedComposites.get(i);
             settings.put(SETTINGS_EXPANDED + String.valueOf(i), curr.isExpanded());
         }
     }
     
     SelectionListener getSelectionListener() {
         if (fSelectionListener == null) {
             fSelectionListener= new SelectionListener() {
                 public void widgetDefaultSelected(SelectionEvent e) {/*do nothing*/}
     
                 public void widgetSelected(SelectionEvent e) {
                     controlChanged(e.widget);
                 }
             };
         }
         return fSelectionListener;
     }
     
     ModifyListener getTextModifyListener() {
         if (fTextModifyListener == null) {
             fTextModifyListener= new ModifyListener() {
                 public void modifyText(ModifyEvent e) {
                     textChanged((Text) e.widget);
                 }
             };
         }
         return fTextModifyListener;
     }       
     
     void controlChanged(Widget widget) {
         ControlData data= (ControlData) widget.getData();
         String newValue= null;
         if (widget instanceof Button) {
             newValue= data.getValue(((Button)widget).getSelection());           
         } else if (widget instanceof Combo) {
             newValue= data.getValue(((Combo)widget).getSelectionIndex());
         } else {
             return;
         }
         String oldValue= setValue(data.getKey(), newValue);
         validateSettings(data.getKey(), oldValue, newValue);
     }
     
     void textChanged(Text textControl) {
         Key key= (Key) textControl.getData();
         String number= textControl.getText();
         String oldValue= setValue(key, number);
         validateSettings(key, oldValue, number);
     }   
 
     boolean checkValue(Key key, String value) {
         return value.equals(getValue(key));
     }
     
     String getValue(Key key) {
         if (fDisabledProjectSettings != null) {
             return (String) fDisabledProjectSettings.get(key);
         }
         return key.getCurValue(_prefs, fLookupOrder, false, fManager).toString();
     }
     
     
     boolean getBooleanValue(Key key) {
         return Boolean.valueOf(getValue(key)).booleanValue();
     }
     
     String setValue(Key key, String value) {
         if (fDisabledProjectSettings != null) {
             return (String) fDisabledProjectSettings.put(key, value);
         }
         Object newValue =  key.setCurValue(_prefs, fLookupOrder[0], Severity.valueOfString(value), fManager);
         return newValue != null ? newValue.toString() : "";
     }
     
     String setValue(Key key, boolean value) {
         return setValue(key, String.valueOf(value));
     }
 
     /**
      * Returns the value as actually stored in the preference store.
      * @param key
      * @return the value as actually stored in the preference store.
      */
     Object getStoredValue(Key key) {
         return key.getCurValue(_prefs, fLookupOrder, false, fManager);
     }
     
     /**
      * Update fields and validate.
      * @param changedKey Key that changed, or null, if all changed.
      * @param oldValue 
      * @param newValue 
      */ 
     protected abstract void validateSettings(Key changedKey, String oldValue, String newValue);
     
     
     String[] getTokens(String text, String separator) {
         StringTokenizer tok= new StringTokenizer(text, separator); 
         int nTokens= tok.countTokens();
         String[] res= new String[nTokens];
         for (int i= 0; i < res.length; i++) {
             res[i]= tok.nextToken().trim();
         }
         return res;
     }
 
     private boolean getChanges(IScopeContext currContext, List changedSettings) {
         boolean needsBuild= false;
         for (int i= 0; i < fAllKeys.length; i++) {
             Key key= fAllKeys[i];
             Object oldVal= key.getStoredValue(_prefs, currContext, null);
             Object val= key.getCurValue(_prefs, currContext, fManager);
             if (val == null) {
                 if (oldVal != null) {
                     changedSettings.add(key);
                     needsBuild |= !oldVal.equals(key.getCurValue(_prefs, fLookupOrder, true, fManager));
                 }
             } else if (!val.equals(oldVal)) {
                 changedSettings.add(key);
                 needsBuild |= oldVal != null || !val.equals(key.getCurValue(_prefs, fLookupOrder, true, fManager));
             }
         }
         return needsBuild;
     }
     
     void useProjectSpecificSettings(boolean enable) {
         boolean hasProjectSpecificOption= fDisabledProjectSettings == null;
         if (enable != hasProjectSpecificOption && fProject != null) {
             if (enable) {
                 for (int i= 0; i < fAllKeys.length; i++) {
                     Key curr= fAllKeys[i];
                     String val= (String) fDisabledProjectSettings.get(curr);
                     curr.setCurValue(_prefs, fLookupOrder[0], Severity.valueOfString(val), fManager);
                 }
                 fDisabledProjectSettings= null;
                 updateControls();
                 validateSettings(null, null, null);
             } else {
                 fDisabledProjectSettings= new IdentityHashMap();
                 for (int i= 0; i < fAllKeys.length; i++) {
                     Key curr= fAllKeys[i];
                     Object oldSetting= curr.getCurValue(_prefs, fLookupOrder, false, fManager);
                     fDisabledProjectSettings.put(curr, oldSetting);
                     curr.setCurValue(_prefs, fLookupOrder[0], null, fManager); // clear project settings
                 }
             }
         }
     }
     
     boolean areSettingsEnabled() {
         return fDisabledProjectSettings == null || fProject == null;
     }
     
     
     boolean performOk() {
         return processChanges(fContainer);
     }
     
 //    public boolean performApply() {
 //        return processChanges(null); // apply directly
 //    }
     
     boolean processChanges(IWorkbenchPreferenceContainer container) {
         IScopeContext currContext= fLookupOrder[0];
         
         List /* <Key>*/ changedOptions= new ArrayList();
         boolean needsBuild= getChanges(currContext, changedOptions);
         if (changedOptions.isEmpty()) {
             return true;
         }
         if (needsBuild) {
             int count= getRebuildCount();
             if (count > fRebuildCount) {
                 needsBuild= false; // build already requested
                 fRebuildCount= count;
             }
         }
 
         boolean doBuild= false;
         
         final String  showBuildWarningKey = JSFCorePlugin.PLUGIN_ID + "." + "buildwarning_dont_show_again";
         final IPreferenceStore prefStore = JSFCorePlugin.getDefault().getPreferenceStore();
         final boolean showDialog = !MessageDialogWithToggle.ALWAYS.equals(prefStore.getString(showBuildWarningKey));
         
         if (needsBuild && showDialog) {
             String[] strings= getFullBuildDialogStrings(fProject == null);
             if (strings != null) {
                 MessageDialogWithToggle.openInformation
                 	(getShell(), strings[0], strings[1], 
                 			PreferencesMessages.ProblemSeveritiesConfigurationBlock_buildwarning_dont_show_again
                 			, false, prefStore, showBuildWarningKey);
 //                int res= dialog.open();
 //                if (res == 0) {
 //                    doBuild= true;
 //                } else if (res != 1) {
 //                    return false; // cancel pressed
 //                }
             }
         }
         if (container != null) {
             // no need to apply the changes to the original store: will be done by the page container
             if (doBuild) { // post build
                 incrementRebuildCount();
                 // TODO: container.registerUpdateJob(CoreUtility.getBuildJob(fProject));
             }
         } else {
             // apply changes right away
             try {
                 fManager.applyChanges();
             } catch (BackingStoreException e) {
                 JSFUiPlugin.log(IStatus.ERROR, "Error applying changes", e);
                 return false;
             }
             if (doBuild) {
                 //CoreUtility.getBuildJob(fProject).schedule();
             }
             
         }
         return true;
     }
     
     abstract String[] getFullBuildDialogStrings(boolean workspaceSettings);
             
     
 //    public void performDefaults() {
 //        for (int i= 0; i < fAllKeys.length; i++) {
 //            Key curr= fAllKeys[i];
 //            String defValue= curr.getStoredValue(fLookupOrder, true, fManager);
 //            setValue(curr, defValue);
 //        }
 //        
 //        settingsUpdated();
 //        updateControls();
 //        validateSettings(null, null, null);
 //    }
 
     /**
      * @since 3.1
      */
     void performRevert() {
         for (int i= 0; i < fAllKeys.length; i++) {
             Key curr= fAllKeys[i];
             String origValue= curr.getCurValue(_prefs, fLookupOrder, false, null).toString();
             setValue(curr, origValue);
         }
 
         updateControls();
         validateSettings(null, null, null);
     }
     
     void dispose() {
         // do nothing; sub-class should override
     }
     
     void updateControls() {
         // update the UI
         for (int i= fCheckBoxes.size() - 1; i >= 0; i--) {
             updateCheckBox((Button) fCheckBoxes.get(i));
         }
         for (int i= fComboBoxes.size() - 1; i >= 0; i--) {
             updateCombo((Combo) fComboBoxes.get(i));
         }
         for (int i= fTextBoxes.size() - 1; i >= 0; i--) {
             updateText((Text) fTextBoxes.get(i));
         }
     }
     
     void updateCombo(Combo curr) {
         ControlData data= (ControlData) curr.getData();
         
         String currValue= getValue(data.getKey());  
         curr.select(data.getSelection(currValue));                  
     }
     
     void updateCheckBox(Button curr) {
         ControlData data= (ControlData) curr.getData();
         
         String currValue= getValue(data.getKey());  
         curr.setSelection(data.getSelection(currValue) == 0);                       
     }
     
     void updateText(Text curr) {
         Key key= (Key) curr.getData();
         
         String currValue= getValue(key);
         if (currValue != null) {
             curr.setText(currValue);
         }
     }
     
     Button getCheckBox(Key key) {
         for (int i= fCheckBoxes.size() - 1; i >= 0; i--) {
             Button curr= (Button) fCheckBoxes.get(i);
             ControlData data= (ControlData) curr.getData();
             if (key.equals(data.getKey())) {
                 return curr;
             }
         }
         return null;        
     }
     
     Combo getComboBox(Key key) {
         for (int i= fComboBoxes.size() - 1; i >= 0; i--) {
             Combo curr= (Combo) fComboBoxes.get(i);
             ControlData data= (ControlData) curr.getData();
             if (key.equals(data.getKey())) {
                 return curr;
             }
         }
         return null;        
     }
     
     Text getTextControl(Key key) {
         for (int i= fTextBoxes.size() - 1; i >= 0; i--) {
             Text curr= (Text) fTextBoxes.get(i);
             ControlData data= (ControlData) curr.getData();
             if (key.equals(data.getKey())) {
                 return curr;
             }
         }
         return null;        
     }
     
     Control findControl(Key key) {
         Combo comboBox= getComboBox(key);
         if (comboBox != null) {
             return comboBox;
         }
         Button checkBox= getCheckBox(key);
         if (checkBox != null) {
             return checkBox;
         }
         Text text= getTextControl(key);
         if (text != null) {
             return text;
         }
         return null;
     }
     
     void setComboEnabled(Key key, boolean enabled) {
         Combo combo= getComboBox(key);
         Label label= (Label) fLabels.get(combo);
         combo.setEnabled(enabled);
         label.setEnabled(enabled);
     }
 }
