 package org.eclipse.recommenders.internal.codesearch.rcp;
 
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.recommenders.codesearch.rcp.index.apidoc.LocalExamplesProvider;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 public class SearchTypesPreferences extends FieldEditorPreferencePage implements
         IWorkbenchPreferencePage {
 
     @Override
     public void init(IWorkbench workbench) {
         setPreferenceStore(CodesearchIndexPlugin.getDefault().getPreferenceStore());
     }
 
     @Override
     protected void createFieldEditors() {
       
         addField(new BooleanFieldEditor(LocalExamplesProvider.CHECKED_EXCEPTION_SEARCH, "Trigger search for checked exceptions. ", getFieldEditorParent()));
         addField(new BooleanFieldEditor(LocalExamplesProvider.CLASS_FIELD_SEARCH, "Trigger search for instance variable(field) types. ", getFieldEditorParent()));
         addField(new BooleanFieldEditor(LocalExamplesProvider.EXTENDED_TYPE_SEARCH, "Trigger search for extended superclass types. ", getFieldEditorParent()));
         addField(new BooleanFieldEditor(LocalExamplesProvider.IMPLEENTED_TYPE_SEARCH, "Trigger search for implemented superinterfaces types. ", getFieldEditorParent()));
         addField(new BooleanFieldEditor(LocalExamplesProvider.METHOD_INVOCATION_SEARCH, "Trigger search for method invocations. ", getFieldEditorParent()));
         addField(new BooleanFieldEditor(LocalExamplesProvider.METHOD_PARAMETER_SEARCH, "Trigger search for method parameter types. ", getFieldEditorParent()));
         addField(new BooleanFieldEditor(LocalExamplesProvider.RETURN_TYPE_SEARCH, "Trigger search for method return types. ", getFieldEditorParent()));
         addField(new BooleanFieldEditor(LocalExamplesProvider.USED_ANNOTATION_SEARCH, "Trigger search for used annotation types. ", getFieldEditorParent()));
         addField(new BooleanFieldEditor(LocalExamplesProvider.VAR_USAGE_SEARCH, "Trigger search for variable usage examples. ", getFieldEditorParent()));
 
     }
 
 }
