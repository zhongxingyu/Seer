 package org.iplantc.core.tito.client.panels;
 
 import org.iplantc.core.client.widgets.BoundedTextField;
 import org.iplantc.core.client.widgets.validator.IPlantValidator;
 import org.iplantc.core.metadata.client.property.Property;
 import org.iplantc.core.metadata.client.validation.MetaDataValidator;
 import org.iplantc.core.tito.client.I18N;
 import org.iplantc.core.tito.client.events.CommandLineArgumentChangeEvent;
 import org.iplantc.core.tito.client.events.JSONMetaDataObjectChangedEvent;
 import org.iplantc.core.uicommons.client.events.EventBus;
 
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.KeyListener;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.Label;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 
 public abstract class PropertyTypeEditorPanel extends VerticalPanel {
     public static final String DEFAULT_STRING = ""; //$NON-NLS-1$
 
     private static final String ID_FLD_CMD_L_OPTN = "idFldCmdLOptn"; //$NON-NLS-1$
     private static final String ID_PROP_LBL = "idPropLbl"; //$NON-NLS-1$
     private static final String ID_DISP_GUI_CBX = "idDispGuiCbx"; //$NON-NLS-1$
     private static final String ID_OPTN_FLAG_CBX = "idOptnFlagCbx"; //$NON-NLS-1$
     private static final String ID_REQ_CBX = "idReqCbx"; //$NON-NLS-1$
     private static final String ID_TOOL_TIP = "idToolTip"; //$NON-NLS-1$
 
     protected final Property property;
 
     protected VerticalPanel pnlWidgets;
     protected CheckBox cbxDisplayInGui;
     protected CheckBox cbxOptionFlag;
     protected CheckBox cbxRequired;
 
     private TextFieldContainer pnlCommandLineOption;
 
     protected PropertyTypeEditorPanel(Property property) {
         this.property = property;
 
         init();
         buildFields();
         initFieldValues();
         addFields();
     }
 
     protected void buildFields() {
         buildCommandLineOptionPanel();
     }
 
     protected void addFields() {
         add(pnlCommandLineOption);
     }
 
     /**
      * set the form field values from the property
      */
     protected void initFieldValues() {
         TextField<String> field = pnlCommandLineOption.getField();
 
         initTextField(field, property.getName());
         field.focus();
     }
 
     protected void init() {
         setSize(450, 450);
         setLayout(new FitLayout());
     }
 
     private void buildCommandLineOptionPanel() {
         String caption = I18N.DISPLAY.flag();
 
         TextField<String> field = buildTextField(ID_FLD_CMD_L_OPTN, 255, 128, new FlagEditKeyUpCommand());
         IPlantValidator.setRegexRestrictedCmdLineChars(field, caption);
 
         pnlCommandLineOption = buildTextFieldContainer(caption, field);
     }
 
     protected void buildWidgetsPanel() {
         pnlWidgets = new VerticalPanel();
         pnlWidgets.setLayout(new FitLayout());
     }
 
     protected void buildGuiEnabledCheckbox() {
         cbxDisplayInGui = buildCheckBox(ID_DISP_GUI_CBX, I18N.DISPLAY.displayInGUI(),
                 new Listener<BaseEvent>() {
                     @Override
                     public void handleEvent(final BaseEvent be) {
                         updatePropertyVisible(cbxDisplayInGui.getValue());
                     }
                 });
     }
 
     protected void buildOptionalFlagCheckbox() {
         cbxOptionFlag = buildCheckBox(ID_OPTN_FLAG_CBX, I18N.DISPLAY.passFlag(),
                 new Listener<BaseEvent>() {
                     @Override
                     public void handleEvent(final BaseEvent be) {
                         updatePropertyOmitIfBlank(cbxOptionFlag.getValue());
                     }
                 });
     }
 
     protected void buildRequiredCheckBox() {
         cbxRequired = buildCheckBox(ID_REQ_CBX, I18N.DISPLAY.userInputRequired(),
                 new Listener<BaseEvent>() {
                     @Override
                     public void handleEvent(final BaseEvent be) {
                         updatePropertyRequired(cbxRequired.getValue());
                     }
                 });
     }
 
     protected void initRequiredCheckBox() {
         MetaDataValidator validator = property.getValidator();
 
         if (validator != null) {
             cbxRequired.setValue(validator.isRequired());
         }
     }
 
     protected void updatePropertyName(String value) {
         property.setName(value);
     }
 
     protected void updatePropertyValue(String value) {
         property.setValue(value);
     }
 
     protected void updatePropertyLabel(String value) {
         property.setLabel(value);
     }
 
     protected void updatePropertyDescription(String value) {
         property.setDescription(value);
     }
 
     protected void updatePropertyOmitIfBlank(boolean value) {
         property.setOmit_if_blank(value);
     }
 
     protected void updatePropertyVisible(boolean isVisible) {
         property.setVisible(isVisible);
         setGuiWidgetsEnabled(isVisible);
     }
 
     private void setGuiWidgetsEnabled(boolean enabled) {
         if (cbxRequired != null && !enabled) {
             cbxRequired.setValue(false);
         }
 
         if (pnlWidgets != null) {
             pnlWidgets.setEnabled(enabled);
         }
     }
 
     protected void updatePropertyRequired(boolean value) {
         // add rule to validator
         MetaDataValidator validator = property.getValidator();
 
         if (validator != null) {
             validator.setRequired(value);
         } else {
             // safety check - this should always be true if our validator is null.
             if (value) {
                 validator = new MetaDataValidator();
                 validator.setRequired(value);
                 property.setValidator(validator);
             }
         }
 
         updateOptionFlag(value);
     }
 
     private void updateOptionFlag(boolean requiredFlag) {
         if (cbxOptionFlag != null) {
             cbxOptionFlag.setEnabled(!requiredFlag && cbxRequired.isEnabled());
 
            if (requiredFlag) {
                 cbxOptionFlag.setValue(false);
             }
         }
     }
 
     protected void initTextField(TextField<String> field, String value) {
         if (field != null && value != null && !value.isEmpty()) {
             field.setValue(value);
         }
     }
 
     protected TextField<String> buildTextField(String id, int width, int maxLength,
             final KeyUpCommand cmdKeyUp) {
         final TextField<String> ret = new BoundedTextField<String>();
 
         ret.setId(id);
         ret.setMaxLength(maxLength);
         ret.setWidth(width);
         ret.setSelectOnFocus(true);
         ret.setAutoValidate(true);
 
         if (cmdKeyUp != null) {
             ret.addKeyListener(new KeyListener() {
                 @Override
                 public void componentKeyUp(ComponentEvent event) {
                     cmdKeyUp.execute(ret.getValue());
                 }
             });
         }
 
         return ret;
     }
 
     protected TextFieldContainer buildTextFieldContainer(final String caption, TextField<String> field) {
         Label label = new Label(caption + ":"); //$NON-NLS-1$
         return new TextFieldContainer(label, field);
     }
 
     protected CheckBox buildCheckBox(String id, String label, Listener<BaseEvent> changeListener) {
         CheckBox ret = new CheckBox();
 
         ret.setId(id);
         ret.setBoxLabel(label);
 
         if (changeListener != null) {
             ret.addListener(Events.Change, changeListener);
         }
 
         return ret;
     }
 
     protected TextFieldContainer buildLabelFieldContainer() {
         return buildTextFieldContainer(I18N.DISPLAY.label(),
                 buildTextField(ID_PROP_LBL, 255, 255, new LabelEditKeyUpCommand()));
     }
 
     protected TextFieldContainer buildToolTipFieldContainer() {
         return buildTextFieldContainer(
                 I18N.DISPLAY.toolTipText(),
                 buildTextField(ID_TOOL_TIP, 255, 480, new DescriptionEditKeyUpCommand()));
     }
 
     private void fireCommandLineArgumentChangeEvent() {
         EventBus.getInstance().fireEvent(new CommandLineArgumentChangeEvent(property));
     }
 
     private void firePropertyChangedEvent() {
         EventBus.getInstance().fireEvent(new JSONMetaDataObjectChangedEvent(property));
     }
 
     protected interface KeyUpCommand {
         void handleNullInput();
 
         void execute(String value);
     }
 
     private class FlagEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             if (value == null) {
                 handleNullInput();
             } else {
                 updatePropertyName(value);
             }
 
             fireCommandLineArgumentChangeEvent();
         }
 
         @Override
         public void handleNullInput() {
             updatePropertyName(DEFAULT_STRING);
         }
     }
 
     protected class ValueEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             if (value == null) {
                 handleNullInput();
             } else {
                 updatePropertyValue(value);
             }
 
             fireCommandLineArgumentChangeEvent();
         }
 
         @Override
         public void handleNullInput() {
             updatePropertyValue(DEFAULT_STRING);
         }
     }
 
     protected class LabelEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             if (value == null) {
                 handleNullInput();
             } else {
                 updatePropertyLabel(value);
             }
 
             fireCommandLineArgumentChangeEvent();
             firePropertyChangedEvent();
         }
 
         @Override
         public void handleNullInput() {
             updatePropertyLabel(DEFAULT_STRING);
         }
     }
 
     private class DescriptionEditKeyUpCommand implements KeyUpCommand {
         @Override
         public void execute(String value) {
             if (value == null) {
                 handleNullInput();
             } else {
                 updatePropertyDescription(value);
             }
         }
 
         @Override
         public void handleNullInput() {
             updatePropertyDescription(DEFAULT_STRING);
         }
     }
 
     /**
      * A simple subclass of LayoutContainer that holds a label and a text field, and provides access to
      * the field.
      * 
      * @author hariolf
      * 
      */
     protected static class TextFieldContainer extends LayoutContainer {
         private final TextField<String> field;
 
         protected TextFieldContainer(Label label, TextField<String> field) {
             this.field = field;
 
             setLayout(new FitLayout());
             add(label);
             add(this.field);
         }
 
         protected TextField<String> getField() {
             return field;
         }
     }
 
 }
