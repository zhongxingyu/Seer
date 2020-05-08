 package org.iplantc.de.apps.integration.client.view.propertyEditors;
 
 import org.iplantc.de.apps.integration.client.view.propertyEditors.widgets.ArgumentValidatorEditor;
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.ArgumentEditorConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.SplittableToStringConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.style.AppTemplateWizardAppearance;
 import org.iplantc.de.apps.widgets.client.view.editors.widgets.CheckBoxAdapter;
 import org.iplantc.de.client.models.apps.integration.Argument;
 import org.iplantc.de.client.models.apps.integration.ArgumentValidator;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsContextualHelpMessages;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsPropertyPanelLabels;
 import org.iplantc.de.resources.client.uiapps.widgets.argumentTypes.TextInputLabels;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.LeafValueEditor;
 import com.google.gwt.editor.client.SimpleBeanEditorDriver;
 import com.google.gwt.editor.client.impl.Refresher;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.google.web.bindery.autobean.shared.Splittable;
 
 import com.sencha.gxt.widget.core.client.form.FieldLabel;
 import com.sencha.gxt.widget.core.client.form.TextField;
 
 import java.util.List;
 
 public class TextInputPropertyEditor extends AbstractArgumentPropertyEditor {
 
     interface EditorDriver extends SimpleBeanEditorDriver<Argument, TextInputPropertyEditor> {}
     interface TextInputPropertyEditorUiBinder extends UiBinder<Widget, TextInputPropertyEditor> {}
 
     private static TextInputPropertyEditorUiBinder uiBinder = GWT.create(TextInputPropertyEditorUiBinder.class);
 
     @UiField(provided = true)
     AppsWidgetsPropertyPanelLabels appLabels;
 
     @UiField
     @Path("name")
     TextField argumentOptionEditor;
     @UiField(provided = true)
     ArgumentEditorConverter<String> defaultValueEditor;
 
 
     @UiField
     FieldLabel defaultValueLabel, argumentOptionLabel, toolTipLabel;
     @UiField
     @Path("visible")
     CheckBoxAdapter doNotDisplay;
 
     @UiField
     TextField label;
 
     @UiField
     CheckBoxAdapter omitIfBlank, requiredEditor;
     @UiField(provided = true)
     TextInputLabels textInputLabels;
 
     @UiField
     @Path("description")
     TextField toolTipEditor;
     @Path("")
     @UiField(provided = true)
     ArgumentValidatorEditor validatorsEditor;
 
     private final EditorDriver editorDriver = GWT.create(EditorDriver.class);
 
     @Inject
     public TextInputPropertyEditor(AppTemplateWizardAppearance appearance, AppsWidgetsPropertyPanelLabels appLabels, AppsWidgetsContextualHelpMessages help, ArgumentValidatorEditor validatorsEditor) {
         super(appearance);
         this.appLabels = appLabels;
         this.textInputLabels = appLabels;
         this.validatorsEditor = validatorsEditor;
 
         TextField textField = new TextField();
         textField.setEmptyText(textInputLabels.textInputWidgetEmptyEditText());
         defaultValueEditor = new ArgumentEditorConverter<String>(textField, new SplittableToStringConverter());
 
         initWidget(uiBinder.createAndBindUi(this));
 
         defaultValueLabel.setHTML(appearance.createContextualHelpLabel(appLabels.textInputDefaultLabel(), help.textInputDefaultText()));
 
         toolTipLabel.setHTML(appearance.createContextualHelpLabel(appLabels.toolTipText(), help.toolTip()));
         argumentOptionLabel.setHTML(appearance.createContextualHelpLabel(appLabels.argumentOption(), help.argumentOption()));
         doNotDisplay.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appLabels.doNotDisplay()).toSafeHtml());
 
         requiredEditor.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appLabels.isRequired()).toSafeHtml());
 
 
         omitIfBlank.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;")
                 .append(appearance.createContextualHelpLabelNoFloat(appLabels.excludeWhenEmpty(), help.textInputExcludeArgument()))
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
         doNotDisplay.setEnabled(!isLabelOnlyEditMode);
         omitIfBlank.setEnabled(!isLabelOnlyEditMode);
         requiredEditor.setEnabled(!isLabelOnlyEditMode);
     }
 
     @UiHandler("defaultValueEditor")
     void onDefaultValueChange(ValueChangeEvent<Splittable> event) {
         // Forward defaultValue onto value.
         model.setValue(event.getValue());
     }
 
     @UiHandler("validatorsEditor")
     void onValidatorListChanged(@SuppressWarnings("unused") ValueChangeEvent<List<ArgumentValidator>> event) {
         // FIXME CORE-4806 Refactor ArgumentValidatorEditor s.t. it integrates with InitializeTwoWayBinding
         editorDriver.flush();
         this.getBoundEditorDelegate().accept(new Refresher());
     }
 
}
