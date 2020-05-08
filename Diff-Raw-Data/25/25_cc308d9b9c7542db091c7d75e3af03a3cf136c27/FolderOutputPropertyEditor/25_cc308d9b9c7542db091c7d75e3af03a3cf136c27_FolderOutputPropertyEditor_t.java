 package org.iplantc.de.apps.integration.client.view.propertyEditors;
 
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.ArgumentEditorConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.SplittableToStringConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.style.AppTemplateWizardAppearance;
 import org.iplantc.de.apps.widgets.client.view.editors.widgets.CheckBoxAdapter;
 import org.iplantc.de.client.models.apps.integration.Argument;
 import org.iplantc.de.client.models.apps.integration.FileInfoType;
 import org.iplantc.de.client.services.AppMetadataServiceFacade;
import org.iplantc.de.commons.client.validators.CmdLineArgCharacterValidator;
import org.iplantc.de.commons.client.validators.DiskResourceNameValidator;
import org.iplantc.de.resources.client.messages.I18N;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsContextualHelpMessages;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsPropertyPanelLabels;
 import org.iplantc.de.resources.client.uiapps.widgets.argumentTypes.FolderOutputLabels;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.LeafValueEditor;
 import com.google.gwt.editor.client.SimpleBeanEditorDriver;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.google.web.bindery.autobean.shared.Splittable;
 
 import com.sencha.gxt.widget.core.client.form.ComboBox;
 import com.sencha.gxt.widget.core.client.form.FieldLabel;
 import com.sencha.gxt.widget.core.client.form.TextField;
 
 public class FolderOutputPropertyEditor extends AbstractArgumentPropertyEditor {
 
     interface EditorDriver extends SimpleBeanEditorDriver<Argument, FolderOutputPropertyEditor> {}
     interface FolderOutputPropertyEditorUiBinder extends UiBinder<Widget, FolderOutputPropertyEditor> {}
 
     private static FolderOutputPropertyEditorUiBinder uiBinder = GWT.create(FolderOutputPropertyEditorUiBinder.class);
 
     @UiField(provided = true)
     AppsWidgetsPropertyPanelLabels appLabels;
 
     @UiField
     @Path("name")
     TextField argumentOption;
 
     @UiField(provided = true)
     ArgumentEditorConverter<String> defaultValueEditor;
     @UiField
     @Path("visible")
     CheckBoxAdapter doNotDisplay;
 
     @Ignore
     @UiField(provided = true)
     @Path("dataObject.fileInfoType")
     ComboBox<FileInfoType> fileInfoTypeComboBox;
 
     @UiField(provided = true)
     FolderOutputLabels folderOutputLabels;
 
     @UiField
     @Path("dataObject.implicit")
     CheckBoxAdapter isImplicit;
     @UiField
     TextField label;
     @UiField
     CheckBoxAdapter omitIfBlank, requiredEditor;
     @UiField
     @Path("description")
     TextField toolTipEditor;
 
     @UiField
     FieldLabel toolTipLabel, argumentOptionLabel;
 
     private final EditorDriver editorDriver = GWT.create(EditorDriver.class);
 
     @Inject
     public FolderOutputPropertyEditor(AppTemplateWizardAppearance appearance, AppsWidgetsPropertyPanelLabels appLabels, AppsWidgetsContextualHelpMessages help,
             AppMetadataServiceFacade appMetadataService) {
         super(appearance);
         this.appLabels = appLabels;
         this.folderOutputLabels = appLabels;
 
         TextField textField = new TextField();
        textField.addValidator(new DiskResourceNameValidator());
         defaultValueEditor = new ArgumentEditorConverter<String>(textField, new SplittableToStringConverter());
         fileInfoTypeComboBox = createFileInfoTypeComboBox(appMetadataService);
 
         initWidget(uiBinder.createAndBindUi(this));

        argumentOption.addValidator(new CmdLineArgCharacterValidator(I18N.V_CONSTANTS
                .restrictedCmdLineChars()));

         toolTipLabel.setHTML(appearance.createContextualHelpLabel(appLabels.toolTipText(), help.toolTip()));
         argumentOptionLabel.setHTML(appearance.createContextualHelpLabel(appLabels.argumentOption(), help.argumentOption()));
         doNotDisplay.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appLabels.doNotDisplay()).toSafeHtml());
 
         requiredEditor.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appLabels.isRequired()).toSafeHtml());
 
         omitIfBlank.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;")
                 .append(appearance.createContextualHelpLabelNoFloat(appLabels.excludeWhenEmpty(), help.fileOutputExcludeArgument()))
                 .toSafeHtml());
         isImplicit.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appearance.createContextualHelpLabelNoFloat(appLabels.doNotPass(), help.doNotPass()))
                 .toSafeHtml());
 
 
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
     protected LeafValueEditor<Splittable> getDefaultValueEditor() {
         return defaultValueEditor;
     }
 
     @Override
     @Ignore
     protected ComboBox<FileInfoType> getFileInfoTypeComboBox() {
         return fileInfoTypeComboBox;
     }
 
     @Override
     protected void initLabelOnlyEditMode(boolean isLabelOnlyEditMode) {
         defaultValueEditor.setEnabled(!isLabelOnlyEditMode);
         doNotDisplay.setEnabled(!isLabelOnlyEditMode);
         fileInfoTypeComboBox.setEnabled(!isLabelOnlyEditMode);
         isImplicit.setEnabled(!isLabelOnlyEditMode);
         argumentOption.setEnabled(!isLabelOnlyEditMode);
         omitIfBlank.setEnabled(!isLabelOnlyEditMode);
         requiredEditor.setEnabled(!isLabelOnlyEditMode);

        if (isLabelOnlyEditMode) {
            defaultValueEditor.getValidators().clear();
            doNotDisplay.getValidators().clear();
            fileInfoTypeComboBox.getValidators().clear();
            isImplicit.getValidators().clear();
            argumentOption.getValidators().clear();
            omitIfBlank.getValidators().clear();
            requiredEditor.getValidators().clear();
        }
     }
 
     @UiHandler("defaultValueEditor")
     void onDefaultValueChange(ValueChangeEvent<Splittable> event) {
         // Forward defaultValue onto value.
         model.setValue(event.getValue());
     }
 
}
