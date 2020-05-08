 package org.iplantc.de.apps.integration.client.view.propertyEditors;
 
 import org.iplantc.de.apps.integration.client.view.propertyEditors.widgets.ArgumentValidatorEditor;
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.ArgumentEditorConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.SplittableToIntegerConverter;
 import org.iplantc.de.apps.widgets.client.view.editors.style.AppTemplateWizardAppearance;
 import org.iplantc.de.apps.widgets.client.view.editors.widgets.CheckBoxAdapter;
 import org.iplantc.de.client.models.apps.integration.Argument;
 import org.iplantc.de.client.models.apps.integration.ArgumentValidator;
 import org.iplantc.de.commons.client.widgets.IPlantSideErrorHandler;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsContextualHelpMessages;
 import org.iplantc.de.resources.client.uiapps.widgets.AppsWidgetsPropertyPanelLabels;
 import org.iplantc.de.resources.client.uiapps.widgets.argumentTypes.IntegerInputLabels;
 
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
 import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
 import com.sencha.gxt.widget.core.client.form.SpinnerField;
 import com.sencha.gxt.widget.core.client.form.TextField;
 
 import java.util.List;
 
 
 public class IntegerInputPropertyEditor extends AbstractArgumentPropertyEditor {
 
     interface EditorDriver extends SimpleBeanEditorDriver<Argument, IntegerInputPropertyEditor> {}
     interface IntegerInputPropertyEditorUiBinder extends UiBinder<Widget, IntegerInputPropertyEditor> {}
 
     private static IntegerInputPropertyEditorUiBinder uiBinder = GWT.create(IntegerInputPropertyEditorUiBinder.class);
 
     @UiField(provided = true)
     AppsWidgetsPropertyPanelLabels appLabels;
 
     @UiField
     @Path("name")
     TextField argumentOption;
 
     @UiField(provided = true)
     ArgumentEditorConverter<Integer> defaultValueEditor;
 
     @UiField
     @Path("visible")
     CheckBoxAdapter doNotDisplay;
     @UiField(provided = true)
     IntegerInputLabels integerInputLabels;
     @UiField
     TextField label;
 
     @UiField
     CheckBoxAdapter omitIfBlank, requiredEditor;
     @UiField
     @Path("description")
     TextField toolTipEditor;
     @UiField
     FieldLabel toolTipLabel, argumentOptionLabel, defaultValueLabel;
 
     @Path("")
     @UiField(provided = true)
     ArgumentValidatorEditor validatorsEditor;
 
 
     private final EditorDriver editorDriver = GWT.create(EditorDriver.class);
 
     @Inject
     public IntegerInputPropertyEditor(AppTemplateWizardAppearance appearance, AppsWidgetsPropertyPanelLabels appLabels, AppsWidgetsContextualHelpMessages help, ArgumentValidatorEditor validatorsEditor) {
         super(appearance);
         this.appLabels = appLabels;
         this.integerInputLabels = appLabels;
         this.validatorsEditor = validatorsEditor;
 
         SpinnerField<Integer> spinnerField = new SpinnerField<Integer>(new NumberPropertyEditor.IntegerPropertyEditor());
         spinnerField.setEmptyText(integerInputLabels.integerInputWidgetEmptyEditText());
         spinnerField.setErrorSupport(new IPlantSideErrorHandler(spinnerField));
         spinnerField.setMinValue(Integer.MIN_VALUE);
         defaultValueEditor = new ArgumentEditorConverter<Integer>(spinnerField, new SplittableToIntegerConverter());
 
 
         initWidget(uiBinder.createAndBindUi(this));
 
         defaultValueLabel.setHTML(appearance.createContextualHelpLabel(integerInputLabels.integerInputDefaultLabel(), help.integerInputDefaultValue()));
 
         // TODO validation label cont help, update it
 
         toolTipLabel.setHTML(appearance.createContextualHelpLabel(appLabels.toolTipText(), help.toolTip()));
         argumentOptionLabel.setHTML(appearance.createContextualHelpLabel(appLabels.argumentOption(), help.argumentOption()));
         doNotDisplay.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appLabels.doNotDisplay()).toSafeHtml());
 
         requiredEditor.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;").append(appLabels.isRequired()).toSafeHtml());
 
         omitIfBlank.setHTML(new SafeHtmlBuilder().appendHtmlConstant("&nbsp;")
                 .append(appearance.createContextualHelpLabelNoFloat(appLabels.excludeWhenEmpty(), help.integerInputExcludeArgument())).toSafeHtml());
 
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
         argumentOption.setEnabled(!isLabelOnlyEditMode);
         defaultValueEditor.setEnabled(!isLabelOnlyEditMode);
         doNotDisplay.setEnabled(!isLabelOnlyEditMode);
         omitIfBlank.setEnabled(!isLabelOnlyEditMode);
         requiredEditor.setEnabled(!isLabelOnlyEditMode);
         validatorsEditor.setEnabled(!isLabelOnlyEditMode);
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
