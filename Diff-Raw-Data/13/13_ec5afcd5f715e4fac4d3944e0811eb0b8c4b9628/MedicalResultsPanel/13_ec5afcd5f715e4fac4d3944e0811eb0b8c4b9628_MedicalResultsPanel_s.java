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
     public static final String MUST_BE_BETWEEN_1_AND_100 = "Value must be between 1 - 100";
     public static final String MUST_BE_BETWEEN_10_AND_2800 = "Value must be between 10 - 2800";
     public static final String MUST_BE_BETWEEN_1_AND_250 = "Value must be between 1 - 250";
     public static final String DIASTOLIC_MUST_BE_LESS_THAN_OR_EQUAL_TO_SYSTOLIC =
             "Diastolic value must be less than or equal to systolic";
     public static final String MUST_BE_BETWEEN_0_AND_15000 = "Value must be between 0 - 15000";
     public static final String MUST_BE_BETWEEN_1_AND_3000 = "Value must be between 1 - 3000";
     public static final String FORMAT_MUST_BE_NNN_DOT_NN = "Format must be nnn.nn";
     public static final String FORMAT_MUST_BE_NNN_DOT_N = "Format must be nnn.n";
 
     @SpringBean
     private MedicalResultManager medicalResultManager;
 
     public MedicalResultsPanel(String id, final Demographics demographics) {
         super(id);
 
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         MedicalResult medicalResult = null;
 
         if (demographics.hasValidId()) {
             medicalResult = medicalResultManager.getMedicalResult(demographics.getId(),
                     demographics.getDiseaseGroup().getId());
         }
 
         if (medicalResult == null) {
             medicalResult = new MedicalResult();
             medicalResult.setRadarNo(demographics.getId());
             medicalResult.setDiseaseGroup(demographics.getDiseaseGroup());
             medicalResult.setNhsNo(demographics.getNhsNumber());
         }
 
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
 
                     if (medicalResult.getBloodUrea() < 1 || medicalResult.getBloodUrea() > 100) {
                         get("bloodUreaDate").error(". " + MUST_BE_BETWEEN_1_AND_100);
                     }
                 }
 
                if (medicalResult.getSerumCreatanine() != null && medicalResult.getCreatanineDate() == null) {
                    get("creatanineDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
 
                     if (medicalResult.getSerumCreatanine() < 10 || medicalResult.getSerumCreatanine() > 2800) {
                         get("serumCreatanine").error(MUST_BE_BETWEEN_10_AND_2800);
                     }
                 }
 
                 if (medicalResult.getWeight() != null) {
                     if (medicalResult.getWeightDate() == null) {
                         get("weightDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                     }
 
                     // format needs to be NNN.NN or NN.NN
                     int weightStringLength = medicalResult.getWeight().toString().length();
                     int indexOfDot = medicalResult.getWeight().toString().indexOf(".");
 
                     if ((weightStringLength != 4 && weightStringLength != 5 && weightStringLength != 6) ||
                             (weightStringLength == 6 && indexOfDot != 3) ||
                             (weightStringLength == 5 && (indexOfDot != 2 && indexOfDot != 3)) ||
                             (weightStringLength == 4 && indexOfDot != 2)) {
                         get("weight").error(FORMAT_MUST_BE_NNN_DOT_NN);
                     }
                 }
 
                 if (medicalResult.getHeight() != null) {
                     if (medicalResult.getHeightDate() == null) {
                         get("heightDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                     }
 
                     // format needs to be NNN.N or NN.N
                     int heightStringLength = medicalResult.getHeight().toString().length();
                     int indexOfDot = medicalResult.getHeight().toString().indexOf(".");
 
                     if ((heightStringLength != 4 && heightStringLength != 5) ||
                             (heightStringLength == 5 && indexOfDot != 3) ||
                             (heightStringLength == 4 && indexOfDot != 2)) {
                         get("height").error(FORMAT_MUST_BE_NNN_DOT_N);
                     }
                 }
 
                 if (medicalResult.getBpSystolic() != null || medicalResult.getBpDiastolic() != null) {
                     // if one has been entered need to make sure the other one is
                     if (medicalResult.getBpSystolic() == null || medicalResult.getBpDiastolic() == null) {
                         get("bpDate").error(TEST_RESULT_BP);
                     }
 
                     if (medicalResult.getBpDate() == null) {
                         get("bpDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                     }
 
                     if (medicalResult.getBpSystolic() != null &&
                             (medicalResult.getBpSystolic() < 1 || medicalResult.getBpSystolic() > 250)) {
                         get("bpSystolic").error(MUST_BE_BETWEEN_1_AND_250);
                     }
 
                     if (medicalResult.getBpDiastolic() != null &&
                             (medicalResult.getBpDiastolic() < 1 || medicalResult.getBpDiastolic() > 250)) {
                         get("bpDiastolic").error(MUST_BE_BETWEEN_1_AND_250);
                     }
 
                     if (medicalResult.getBpSystolic() != null && medicalResult.getBpDiastolic() != null &&
                             medicalResult.getBpDiastolic() > medicalResult.getBpSystolic()) {
                         get("bpDiastolic").error(DIASTOLIC_MUST_BE_LESS_THAN_OR_EQUAL_TO_SYSTOLIC);
                     }
                 }
 
                 if (medicalResult.getAntihypertensiveDrugs() != null
                         && !medicalResult.getAntihypertensiveDrugs().equals(MedicalResult.YesNo.UNKNOWN)
                         && medicalResult.getAntihypertensiveDrugsDate() == null) {
                     get("antihypertensiveDrugsDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                 }
 
                 if (medicalResult.getPcr() != null) {
                     if (medicalResult.getPcr() < 0 || medicalResult.getPcr() > 15000) {
                         get("pcr").error(MUST_BE_BETWEEN_0_AND_15000);
                     }
 
                     if (medicalResult.getPcrDate() == null) {
                         get("pcrDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                     }
                 }
 
                 if (medicalResult.getAcr() != null) {
                     if (medicalResult.getAcr() < 1 || medicalResult.getAcr() > 3000) {
                         get("acr").error(MUST_BE_BETWEEN_1_AND_3000);
                     }
 
                     if (medicalResult.getAcrDate() == null) {
                         get("acrDate").error(TEST_RESULT_NULL_DATE_MESSAGE);
                     }
                 }
 
                 if (!hasError()) {
                     medicalResult.setRadarNo(demographics.getId());
                     medicalResult.setNhsNo(demographics.getNhsNumber());
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
 
         form.add(new RadarTextFieldWithValidation<Integer>("pcr", null, form, componentsToUpdateList));
         form.add(new RadarDateTextField("pcrDate", form, componentsToUpdateList));
 
         form.add(new RadarTextFieldWithValidation<Integer>("acr", null, form, componentsToUpdateList));
         form.add(new RadarDateTextField("acrDate", form, componentsToUpdateList));
 
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
