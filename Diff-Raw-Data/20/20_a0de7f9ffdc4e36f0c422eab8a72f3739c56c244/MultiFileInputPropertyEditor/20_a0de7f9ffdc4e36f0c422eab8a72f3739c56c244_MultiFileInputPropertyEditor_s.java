 package org.iplantc.de.apps.integration.client.view.propertyEditors;
 
 import org.iplantc.de.apps.widgets.client.view.editors.style.AppTemplateWizardAppearance;
 import org.iplantc.de.apps.widgets.client.view.editors.widgets.CheckBoxAdapter;
 import org.iplantc.de.client.models.apps.integration.Argument;
 import org.iplantc.de.client.models.apps.integration.FileInfoType;
 import org.iplantc.de.client.services.AppMetadataServiceFacade;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsContextualHelpMessages;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsPropertyPanelLabels;
 import org.iplantc.de.resources.client.uiapps.widgets.argumentTypes.MultiFileInputLabels;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.SimpleBeanEditorDriver;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 
 import com.sencha.gxt.widget.core.client.form.ComboBox;
 import com.sencha.gxt.widget.core.client.form.FieldLabel;
 import com.sencha.gxt.widget.core.client.form.TextField;
 
 public class MultiFileInputPropertyEditor extends AbstractArgumentPropertyEditor {
 
     interface EditorDriver extends SimpleBeanEditorDriver<Argument, MultiFileInputPropertyEditor> {}
     interface MultiFileInputPropertyEditorUiBinder extends UiBinder<Widget, MultiFileInputPropertyEditor> {}
 
     private static MultiFileInputPropertyEditorUiBinder uiBinder = GWT.create(MultiFileInputPropertyEditorUiBinder.class);
 
     @UiField(provided = true)
     AppsWidgetsPropertyPanelLabels appLabels;
 
     @UiField
     @Path("name")
     TextField argumentOption;
 
     @UiField
     FieldLabel argumentOptionLabel, toolTipLabel, fileInfoTypeLabel;
     @Ignore
     @UiField(provided = true)
     @Path("dataObject.fileInfoType")
     ComboBox<FileInfoType> fileInfoTypeComboBox;
 
     @UiField
     TextField label;
 
     @UiField(provided = true)
     MultiFileInputLabels multiFileInputLabels;
     @UiField
     CheckBoxAdapter requiredEditor, omitIfBlank;
 
     @UiField
     @Path("description")
     TextField toolTipEditor;
 
     private final EditorDriver editorDriver = GWT.create(EditorDriver.class);
 
     @Inject
     public MultiFileInputPropertyEditor(AppTemplateWizardAppearance appearance, AppsWidgetsPropertyPanelLabels appLabels, AppsWidgetsContextualHelpMessages help,
             AppMetadataServiceFacade appMetadataService) {
         super(appearance);
         this.appLabels = appLabels;
         this.multiFileInputLabels = appLabels;
         this.fileInfoTypeComboBox = createFileInfoTypeComboBox(appMetadataService);
         initWidget(uiBinder.createAndBindUi(this));
 
         omitIfBlank.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appearance.createContextualHelpLabelNoFloat(appLabels.excludeWhenEmpty(), help.folderInputExcludeArgument())).toSafeHtml());
         argumentOptionLabel.setHTML(appearance.createContextualHelpLabel(appLabels.argumentOption(), help.argumentOption()));
         toolTipLabel.setHTML(appearance.createContextualHelpLabel(appLabels.toolTipText(), help.toolTip()));
         requiredEditor.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appLabels.isRequired()).toSafeHtml());
 
         editorDriver.initialize(this);
         editorDriver.accept(new InitializeTwoWayBinding(this));
     }
 
     @Override
     public void edit(Argument argument) {
         super.edit(argument);
         editorDriver.edit(argument);
     }
 
     @Override
     public com.google.gwt.editor.client.EditorDriver<Argument> getEditorDriver() {
         return editorDriver;
     }
 
     @Override
     @Ignore
     protected ComboBox<FileInfoType> getFileInfoTypeComboBox() {
         return fileInfoTypeComboBox;
     }
 
     @Override
     protected void initLabelOnlyEditMode(boolean isLabelOnlyEditMode) {
         argumentOption.setEnabled(!isLabelOnlyEditMode);
         fileInfoTypeComboBox.setEnabled(!isLabelOnlyEditMode);
         requiredEditor.setEnabled(!isLabelOnlyEditMode);
         omitIfBlank.setEnabled(!isLabelOnlyEditMode);
     }
 
}
