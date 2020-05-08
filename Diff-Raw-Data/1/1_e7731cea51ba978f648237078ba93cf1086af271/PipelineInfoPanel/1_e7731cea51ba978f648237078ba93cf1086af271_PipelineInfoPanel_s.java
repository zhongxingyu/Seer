 package org.iplantc.core.client.pipelines.views.panels;
 
 import org.iplantc.core.client.pipelines.I18N;
 import org.iplantc.core.jsonutil.JsonUtil;
 
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FieldEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 
 /**
  * 
  * A panel that provides a form to collect basic information about a workflow
  * 
  * @author sriram
  * 
  */
 public class PipelineInfoPanel extends PipelineStep {
 
     private static final String AUTO_GEN = "auto-gen"; //$NON-NLS-1$
     private FormPanel panel;
     private TextField<String> txtPipelineName;
     private TextArea txtPipelineDesc;
 
     private FormData formData;
 
     public PipelineInfoPanel(String title) {
         super(title);
         initForm();
         compose();
     }
 
     private void initForm() {
         panel = new FormPanel();
         panel.setHeaderVisible(false);
         formData = new FormData("-20"); //$NON-NLS-1$
         panel.setLabelAlign(LabelAlign.TOP);
         panel.setBodyBorder(false);
         initPipelineNameField();
         initPipelineDescField();
     }
 
     private void compose() {
         panel.add(txtPipelineName, formData);
         panel.add(txtPipelineDesc, formData);
         add(panel);
     }
 
     private void initPipelineNameField() {
         txtPipelineName = new TextField<String>();
         txtPipelineName.setAutoValidate(true);
         txtPipelineName.setFieldLabel(I18N.DISPLAY.pipelineName());
         txtPipelineName.setAllowBlank(false);
         addNameValidationListeners();
     }
 
     private void initPipelineDescField() {
         txtPipelineDesc = new TextArea();
         txtPipelineDesc.setAutoValidate(true);
         txtPipelineDesc.setFieldLabel(I18N.DISPLAY.pipelineDescription());
         txtPipelineDesc.setHeight(100);
         txtPipelineDesc.setAllowBlank(false);
         addDescValidationListeners();
     }
 
     private void addNameValidationListeners() {
         txtPipelineName.addListener(Events.Valid, new FieldValidationEvent());
         txtPipelineName.addListener(Events.Invalid, new FieldValidationEvent());
     }
 
     private void addDescValidationListeners() {
         txtPipelineDesc.addListener(Events.Valid, new FieldValidationEvent());
         txtPipelineDesc.addListener(Events.Invalid, new FieldValidationEvent());
     }
 
     @Override
     public boolean isValid() {
         // remove validation listeners before testing for valid form to prevent infinite loop
         txtPipelineName.removeAllListeners();
         txtPipelineDesc.removeAllListeners();
         boolean valid = panel.isValid();
         addNameValidationListeners();
         addDescValidationListeners();
         return valid;
     }
 
     @Override
     public JSONValue toJson() {
         JSONObject obj = new JSONObject();
         obj.put("id", new JSONString(AUTO_GEN)); //$NON-NLS-1$
         obj.put("analysis_name", //$NON-NLS-1$
                 new JSONString(
                         JsonUtil.formatString(txtPipelineName.getValue() != null ? txtPipelineName
                                 .getValue() : ""))); //$NON-NLS-1$
         obj.put("description", //$NON-NLS-1$
                 new JSONString(
                         JsonUtil.formatString(txtPipelineDesc.getValue() != null ? txtPipelineDesc
                                 .getValue() : ""))); //$NON-NLS-1$
         return obj;
     }
 
     private class FieldValidationEvent implements Listener<FieldEvent> {
 
         @Override
         public void handleEvent(FieldEvent be) {
             firePipelineStepValidationEvent(isValid());
         }
 
     }
 
     @Override
     protected void setData(JSONObject obj) {
         if (obj != null) {
             txtPipelineName.setValue(JsonUtil.getString(obj, "analysis_name"));
             txtPipelineDesc.setValue(JsonUtil.getString(obj, "description"));
 
         }
     }
 
 }
