 package org.iplantc.de.apps.integration.client.view.propertyEditors;
 
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.ClearComboBoxSelectionKeyDownHandler;
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.ArgumentEditorConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.SplittableToReferenceGenomeConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.style.AppTemplateWizardAppearance;
 import org.iplantc.de.apps.widgets.client.view.editors.widgets.CheckBoxAdapter;
 import org.iplantc.de.client.models.apps.integration.Argument;
 import org.iplantc.de.client.models.apps.integration.ReferenceGenome;
 import org.iplantc.de.client.services.AppMetadataServiceFacade;
import org.iplantc.de.commons.client.validators.CmdLineArgCharacterValidator;
 import org.iplantc.de.resources.client.messages.I18N;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsContextualHelpMessages;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsPropertyPanelLabels;
 import org.iplantc.de.resources.client.uiapps.widgets.argumentTypes.ReferenceSelectorLabels;
 
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
 
 public class ReferenceAnnotationPropertyEditor extends AbstractArgumentPropertyEditor {
 
     interface EditorDriver extends SimpleBeanEditorDriver<Argument, ReferenceAnnotationPropertyEditor> {}
     interface ReferenceAnnotationPropertyEditorUiBinder extends UiBinder<Widget, ReferenceAnnotationPropertyEditor> {}
 
     private static ReferenceAnnotationPropertyEditorUiBinder uiBinder = GWT.create(ReferenceAnnotationPropertyEditorUiBinder.class);
 
     @UiField(provided = true)
     AppsWidgetsPropertyPanelLabels appLabels;
 
     @UiField
     @Path("name")
     TextField argumentOptionEditor;
 
     @UiField(provided = true)
     ArgumentEditorConverter<ReferenceGenome> defaultValueEditor;
 
     @UiField
     TextField label;
 
     @UiField
     CheckBoxAdapter omitIfBlank, requiredEditor;
 
     @UiField(provided = true)
     ReferenceSelectorLabels referenceAnnotationSelectorLabels;
 
     @UiField
     @Path("description")
     TextField toolTipEditor;
 
     @UiField
     FieldLabel toolTipLabel, argumentOptionLabel, selectionItemDefaultValueLabel;
 
     private final EditorDriver editorDriver = GWT.create(EditorDriver.class);
 
     @Inject
     public ReferenceAnnotationPropertyEditor(AppTemplateWizardAppearance appearance, AppsWidgetsPropertyPanelLabels appLabels, AppsWidgetsContextualHelpMessages help,
             AppMetadataServiceFacade appMetadataService) {
         super(appearance);
         this.appLabels = appLabels;
         this.referenceAnnotationSelectorLabels = appLabels;
         ComboBox<ReferenceGenome> comboBox = createReferenceGenomeStore(appMetadataService);
         comboBox.setEmptyText(I18N.APPS_MESSAGES.emptyListSelectionText());
         comboBox.setMinChars(1);
         ClearComboBoxSelectionKeyDownHandler handler = new ClearComboBoxSelectionKeyDownHandler(comboBox);
         comboBox.addKeyDownHandler(handler);
         defaultValueEditor = new ArgumentEditorConverter<ReferenceGenome>(comboBox, new SplittableToReferenceGenomeConverter());
 
         initWidget(uiBinder.createAndBindUi(this));
 
        argumentOptionEditor.addValidator(new CmdLineArgCharacterValidator(I18N.V_CONSTANTS
                .restrictedCmdLineChars()));

         selectionItemDefaultValueLabel.setHTML(appearance.createContextualHelpLabel(appLabels.singleSelectionDefaultValue(), help.singleSelectDefaultItem()));
 
         toolTipLabel.setHTML(appearance.createContextualHelpLabel(appLabels.toolTipText(), help.toolTip()));
         argumentOptionLabel.setHTML(appearance.createContextualHelpLabel(appLabels.argumentOption(), help.argumentOption()));
 
         requiredEditor.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appLabels.isRequired()).toSafeHtml());
 
 
         omitIfBlank.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;")
                 .append(appearance.createContextualHelpLabelNoFloat(appLabels.excludeWhenEmpty(), help.excludeReference()))
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
     protected void initLabelOnlyEditMode(boolean isLabelOnlyEditMode) {
         argumentOptionEditor.setEnabled(!isLabelOnlyEditMode);
         defaultValueEditor.setEnabled(!isLabelOnlyEditMode);
         omitIfBlank.setEnabled(!isLabelOnlyEditMode);
         requiredEditor.setEnabled(!isLabelOnlyEditMode);

        if (isLabelOnlyEditMode) {
            argumentOptionEditor.getValidators().clear();
            defaultValueEditor.getValidators().clear();
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
