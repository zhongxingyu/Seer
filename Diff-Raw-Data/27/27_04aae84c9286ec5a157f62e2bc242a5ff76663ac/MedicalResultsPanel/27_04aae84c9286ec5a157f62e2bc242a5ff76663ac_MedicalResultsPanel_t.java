 package com.solidstategroup.radar.web.panels.generic;
 
 
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.model.generic.MedicalResult;
 import com.solidstategroup.radar.service.generic.MedicalResultManager;
 import com.solidstategroup.radar.web.RadarApplication;
 import com.solidstategroup.radar.web.components.ComponentHelper;
 import com.solidstategroup.radar.web.components.RadarComponentFactory;
 import com.solidstategroup.radar.web.components.RadarDateTextField;
 import com.solidstategroup.radar.web.components.RadarTextFieldWithValidation;
 import com.solidstategroup.radar.web.panels.PatientDetailPanel;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.Radio;
 import org.apache.wicket.markup.html.form.RadioGroup;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MedicalResultsPanel extends Panel {
 
     public static final String TEST_RESULT_NULL_DATE_MESSAGE = "Test result must have a date";
     public static final String TEST_RESULT_AT_LEAST_ONE = "A test result must be entered";
    public static final String TEST_RESULT_BP = "BP Systolic and Diastolic must be entered";
 
     @SpringBean
     private MedicalResultManager medicalResultManager;
 
    public MedicalResultsPanel(String id, MedicalResult medicalResult, final Demographics demographics) {
         super(id);
 
         // general feedback for messages that are not to do with a certain component in the form
         final FeedbackPanel formFeedback = new FeedbackPanel("formFeedbackPanel");
         formFeedback.setOutputMarkupId(true);
         formFeedback.setOutputMarkupPlaceholderTag(true);
 
         // components to update on ajax refresh
         final List<Component> componentsToUpdateList = new ArrayList<Component>();
         IModel<MedicalResult> model = new Model<MedicalResult>(medicalResult);
 
         // create form and components
 
         Form<MedicalResult> form = new Form<MedicalResult>("form", new CompoundPropertyModel<MedicalResult>(model)) {
             @Override
             protected void onSubmit() {
                 MedicalResult medicalResult = getModelObject();
 
                 if (medicalResult.getBloodUrea() == null
                         && medicalResult.getSerumCreatanine() == null
                         && medicalResult.getWeight() == null
                         && medicalResult.getHeight() == null
                         && medicalResult.getBpSystolic() == null
                         && medicalResult.getAntihypertensiveDrugs() == null) {
                     error(TEST_RESULT_AT_LEAST_ONE);
                 }
 
                 // test result cannot have a null date
                 if (medicalResult.getBloodUrea() != null && medicalResult.getBloodUreaDate() == null) {
                     get("bloodUreaDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                 }
 
                 if (medicalResult.getSerumCreatanine() != null && medicalResult.getCreatanineDate() == null) {
                     get("creatanineDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                 }
 
                 if (medicalResult.getWeight() != null && medicalResult.getWeightDate() == null) {
                     get("weightDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                 }
 
                 if (medicalResult.getHeight() != null && medicalResult.getHeightDate() == null) {
                     get("heightDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                 }
 
                if (medicalResult.getBpSystolic() != null || medicalResult.getBpDiastolic() != null) {
                    // if one has been entered need to make sure the other one is
                    if (medicalResult.getBpSystolic() == null || medicalResult.getBpDiastolic() == null) {
                        get("bpDate").error(TEST_RESULT_BP);
                    }
 
                    if (medicalResult.getBpDate() == null) {
                        get("bpDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                    }
                 }
 
                 if (medicalResult.getAntihypertensiveDrugs() != null
                         && !medicalResult.getAntihypertensiveDrugs().equals(MedicalResult.YesNo.UNKNOWN)
                         && medicalResult.getAntihypertensiveDrugsDate() == null) {
                     get("antihypertensiveDrugsDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                 }
 
                 if (!hasError()) {
                    medicalResult.setRadarNo(demographics.getId());
                     medicalResultManager.save(medicalResult);
                 }
             }
         };
         add(form);
 
         // have to set the generic feedback panel to only pick up msgs for them form
         ComponentFeedbackMessageFilter filter = new ComponentFeedbackMessageFilter(form);
         formFeedback.setFilter(filter);
         form.add(formFeedback);
 
         PatientDetailPanel patientDetail = new PatientDetailPanel("patientDetail", demographics, "Medical Results");
         patientDetail.setOutputMarkupId(true);
         form.add(patientDetail);
         componentsToUpdateList.add(patientDetail);
 
         form.add(new RadarTextFieldWithValidation<Double>("bloodUrea", null, form, componentsToUpdateList));
         form.add(new RadarDateTextField("bloodUreaDate", form, componentsToUpdateList));
 
         form.add(new RadarTextFieldWithValidation<Double>("serumCreatanine", null, form, componentsToUpdateList));
         form.add(new RadarDateTextField("creatanineDate", form, componentsToUpdateList));
 
         form.add(new RadarTextFieldWithValidation<Double>("weight", null, form, componentsToUpdateList));
         form.add(new RadarDateTextField("weightDate", form, componentsToUpdateList));
 
         form.add(new RadarTextFieldWithValidation<Double>("height", null, form, componentsToUpdateList));
         form.add(new RadarDateTextField("heightDate", form, componentsToUpdateList));
 
         form.add(new RadarTextFieldWithValidation<Integer>("bpSystolic", null, form, componentsToUpdateList));
         form.add(new RadarTextFieldWithValidation<Integer>("bpDiastolic", null, form, componentsToUpdateList));
         form.add(new RadarDateTextField("bpDate", form, componentsToUpdateList));
 
         RadioGroup<MedicalResult.YesNo> antihypertensiveDrugs = new RadioGroup<MedicalResult.YesNo>(
                 "antihypertensiveDrugs");
         antihypertensiveDrugs.add(new Radio("yes", new Model(MedicalResult.YesNo.YES)));
         antihypertensiveDrugs.add(new Radio("no", new Model(MedicalResult.YesNo.NO)));
         antihypertensiveDrugs.add(new Radio("unknown", new Model(MedicalResult.YesNo.UNKNOWN)));
         form.add(antihypertensiveDrugs);
 
         form.add(new RadarDateTextField("antihypertensiveDrugsDate", form, componentsToUpdateList));
 
         final Label successMessage = RadarComponentFactory.getSuccessMessageLabel("successMessage", form,
                 componentsToUpdateList);
         Label errorMessage = RadarComponentFactory.getErrorMessageLabel("errorMessage", form, componentsToUpdateList);
 
         form.add(new AjaxSubmitLink("save") {
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 ComponentHelper.updateComponentsIfParentIsVisible(target, componentsToUpdateList);
                 target.appendJavaScript(RadarApplication.FORM_IS_DIRTY_FALSE_SCRIPT);
                 target.add(formFeedback);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 ComponentHelper.updateComponentsIfParentIsVisible(target, componentsToUpdateList);
                 target.add(formFeedback);
             }
         });
     }
 }
