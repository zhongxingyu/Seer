 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.radar.web.panels;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.PatternValidator;
 import org.patientview.model.Centre;
 import org.patientview.model.Ethnicity;
 import org.patientview.model.Patient;
 import org.patientview.model.Sex;
 import org.patientview.model.Status;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.exception.RegisterException;
 import org.patientview.radar.model.Diagnosis;
 import org.patientview.radar.model.DiagnosisCode;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.service.ClinicalDataManager;
 import org.patientview.radar.service.DemographicsManager;
 import org.patientview.radar.service.DiagnosisManager;
 import org.patientview.radar.service.LabDataManager;
 import org.patientview.radar.service.TherapyManager;
 import org.patientview.radar.service.UnitManager;
 import org.patientview.radar.service.UserManager;
 import org.patientview.radar.service.UtilityManager;
 import org.patientview.radar.service.generic.DiseaseGroupManager;
 import org.patientview.radar.web.RadarApplication;
 import org.patientview.radar.web.RadarSecuredSession;
 import org.patientview.radar.web.components.ClinicianDropDown;
 import org.patientview.radar.web.components.PatientCentreDropDown;
 import org.patientview.radar.web.components.RadarComponentFactory;
 import org.patientview.radar.web.components.RadarRequiredCheckBox;
 import org.patientview.radar.web.components.RadarRequiredDateTextField;
 import org.patientview.radar.web.components.RadarRequiredDropdownChoice;
 import org.patientview.radar.web.components.RadarRequiredTextField;
 import org.patientview.radar.web.components.RadarTextFieldWithValidation;
 import org.patientview.radar.web.models.RadarModelFactory;
 import org.patientview.radar.web.pages.patient.srns.PatientCallBack;
 import org.patientview.radar.web.pages.patient.srns.SrnsPatientPage;
 import org.patientview.util.CommonUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class DemographicsPanel extends Panel {
 
     @SpringBean
     private DemographicsManager demographicsManager;
 
     @SpringBean
     private DiseaseGroupManager diseaseGroupManager;
 
     @SpringBean
     private DiagnosisManager diagnosisManager;
 
     @SpringBean
     private ClinicalDataManager clinicalDataManager;
 
     @SpringBean
     private LabDataManager labDataManager;
 
     @SpringBean
     private TherapyManager therapyManager;
 
     @SpringBean
     private UtilityManager utilityManager;
 
     @SpringBean
     private UnitManager unitManager;
 
     @SpringBean
     private UserManager userManager;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(DemographicsPanel.class);
 
 
     public DemographicsPanel(String id, final IModel<Patient> patientModel, final PatientCallBack patientCallBack) {
 
         super(id);
 
         List<Component> nonEditableComponents = new ArrayList<Component>();
 
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         final User user = RadarSecuredSession.get().getUser();
 
         // Set up model - if given radar number loadable detachable getting demographics by radar number
         final CompoundPropertyModel<Patient> model = new CompoundPropertyModel<Patient>(patientModel.getObject());
         final IModel<Date> registrationHeaderModel = new Model<Date>();
 
         if (patientModel.getObject().getDateReg() != null) {
             registrationHeaderModel.setObject(patientModel.getObject().getDateReg());
         }   else {
             registrationHeaderModel.setObject(new Date());
         }
 
         final IModel<Long> radarHeaderModel = new Model<Long>(patientModel.getObject().getRadarNo());
         final IModel<String> forenameHeaderModel = new Model<String>(patientModel.getObject().getForename());
         final IModel<String> surnameHeaderModel = new Model<String>(patientModel.getObject().getSurname());
         final IModel<Date> dobHeaderModel = new Model<Date>(patientModel.getObject().getDob());
 
         // Set up form
         final Form<Patient> form = new Form<Patient>("form", model) {
             @Override
             protected void onSubmit() {
 
                 Patient patient = getModelObject();
                 patient.setRadarConsentConfirmedByUserId(user.getUserId());
 
                 try {
 
                     patientModel.setObject(patient);
                     patientModel.getObject().setDateReg(new Date());
 
                     userManager.addPatientUserOrUpdatePatient(patient);
 
 
                     patientCallBack.updateModel(patient.getRadarNo());
                     // Update the header with the saved record
 
                     forenameHeaderModel.setObject(patientModel.getObject().getForename());
                     surnameHeaderModel.setObject(patientModel.getObject().getSurname());
                     dobHeaderModel.setObject(patientModel.getObject().getDob());
                     registrationHeaderModel.setObject(patientModel.getObject().getDateReg());
                     radarHeaderModel.setObject(patient.getRadarNo());
 
 
                 } catch (RegisterException re) {
                     LOGGER.error("Registration Exception {} ", re.getMessage());
                     error("Could not register patient" + re.getMessage());
                 } catch (Exception e) {
                     String message = "Error registering new patient to accompany this demographic";
                     LOGGER.error("{}, message {}", message, e.getMessage());
                     error(message);
                 }
 
                 // create new diagnosis if it doesnt exist becuase diagnosis code is set in demographics tab
                 if (patient.hasValidId()) {
                     Diagnosis diagnosis = diagnosisManager.getDiagnosisByRadarNumber(patient.getRadarNo());
                     if (diagnosis == null) {
                         Diagnosis diagnosisNew = new Diagnosis();
                         diagnosisNew.setRadarNumber(patient.getRadarNo());
                         DiagnosisCode diagnosisCode = (DiagnosisCode) ((DropDownChoice) get("diagnosis"))
                                 .getModelObject();
                         diagnosisNew.setDiagnosisCode(diagnosisCode);
                         diagnosisManager.saveDiagnosis(diagnosisNew);
                     }
                 }
 
             }
         };
 
         // More info
         Label nhsNumber = new Label("nhsno");
         WebMarkupContainer nhsNumberContainer = new WebMarkupContainer("nhsNumberContainer");
         nhsNumberContainer.add(nhsNumber);
 
         Label chiNumber = new Label("chiNumber");
         WebMarkupContainer chiNumberContainer = new WebMarkupContainer("chiNumberContainer") {
             @Override
             public boolean isVisible() {
                 return false;
             }
         };
         chiNumberContainer.add(chiNumber);
 
         form.add(nhsNumberContainer, chiNumberContainer);
 
         form.setOutputMarkupId(true);
         form.setOutputMarkupPlaceholderTag(true);
 
         add(form);
 
 
         final List<Component> componentsToUpdateList = new ArrayList<Component>();
 
         form.add(new Label("addNewPatientLabel", "Add a New Patient") {
             @Override
             public boolean isVisible() {
                 return patientModel.getObject().hasValidId();
             }
         });
 
         final TextField radarNumberField = new TextField("radarNo", radarHeaderModel);
         radarNumberField.setEnabled(false);
         radarNumberField.setOutputMarkupId(true);
         radarNumberField.setOutputMarkupPlaceholderTag(true);
 
         form.add(radarNumberField);
         componentsToUpdateList.add(radarNumberField);
 
         final TextField dateRegistered = new org.apache.wicket.extensions.markup.html.form.DateTextField("dateReg",
                 registrationHeaderModel, CommonUtils.UK_DATE_FORMAT);
 
         dateRegistered.setOutputMarkupId(true);
         dateRegistered.setOutputMarkupPlaceholderTag(true);
 
         form.add(dateRegistered);
         componentsToUpdateList.add(dateRegistered);
 
         RadarRequiredDropdownChoice diagnosis =
                 new RadarRequiredDropdownChoice("diagnosis", RadarModelFactory.getDiagnosisCodeModel(
                         new Model<Long>(patientModel.getObject().getRadarNo()),
                         diagnosisManager),
                         diagnosisManager.getDiagnosisCodes(),
                         new ChoiceRenderer("abbreviation", "id"), form, componentsToUpdateList) {
                     @Override
                     public boolean isEnabled() {
                         RadarSecuredSession securedSession = RadarSecuredSession.get();
                         if (securedSession.getRoles().hasRole(User.ROLE_PATIENT)) {
                             return false;
                         }
                         return getModelObject() == null;
                     }
                 };
 
         String diseaseGroup = patientModel.getObject().getDiseaseGroup().getId();
         DiagnosisCode diagnosisCode = new DiagnosisCode();
 
         // WARNING - This doesn't make sense, you cannot equate a disease group with a diagnosis code,
         // but works because these groups do not have specific diagnosis
         if (diseaseGroup.equals(DiseaseGroup.SRNS_DISEASE_GROUP_ID)) {
             diagnosisCode.setId(DiagnosisCode.SRNS_ID);
         } else if (diseaseGroup.equals(DiseaseGroup.MPGN_DISEASEGROUP_ID)) {
             diagnosisCode.setId(DiagnosisCode.MPGN_ID);
         }
 
         diagnosis.setModel(new Model(diagnosisCode));
 
         /**
          * Basic fields
          */
         RadarRequiredTextField surname = new RadarRequiredTextField("surname", form, componentsToUpdateList);
         RadarRequiredTextField forename = new RadarRequiredTextField("forename", form, componentsToUpdateList);
         RadarRequiredDateTextField dateOfBirth = new RadarRequiredDateTextField("dob", form,
                 componentsToUpdateList);
 
         dateOfBirth.setRequired(true);
 
         form.add(diagnosis, surname, forename, dateOfBirth);
         nonEditableComponents.add(surname);
         nonEditableComponents.add(forename);
         nonEditableComponents.add(dateOfBirth);
         /**
          *  Add basic fields for header too... apparently we can't render same component twice in wicket!..
          *
          *  As we cant set demographicsModelObject as final outside isVisible() implementations there's a bunch of
          *      code duplication
          */
 
         // forename
 
         final Label nameLabel = new Label("nameLabel", "Name") {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patientModel.getObject().getForename());
             }
         };
         nameLabel.setOutputMarkupId(true);
         nameLabel.setOutputMarkupPlaceholderTag(true);
         form.add(nameLabel);
 
         final TextField forenameForHeader = new TextField("forenameForHeader", forenameHeaderModel) {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patientModel.getObject().getForename());
             }
         };
         forenameForHeader.setOutputMarkupId(true);
         forenameForHeader.setOutputMarkupPlaceholderTag(true);
         componentsToUpdateList.add(forenameForHeader);
         componentsToUpdateList.add(nameLabel);
 
         // surname
         final TextField surnameForHeader = new TextField("surnameForHeader", surnameHeaderModel) {
             @Override
             public boolean isVisible() {
 
                 return StringUtils.isNotBlank(patientModel.getObject().getSurname());
             }
         };
         surnameForHeader.setOutputMarkupId(true);
         surnameForHeader.setOutputMarkupPlaceholderTag(true);
         componentsToUpdateList.add(surnameForHeader);
 
         // date of birth
         final Label dobLabel = new Label("dobLabel", "DoB") {
             @Override
             public boolean isVisible() {
                 return patientModel.getObject().getDob() != null;
             }
         };
         dobLabel.setOutputMarkupId(true);
         dobLabel.setOutputMarkupPlaceholderTag(true);
         form.add(dobLabel);
 
         final TextField dateOfBirthForHeader = new org.apache.wicket.extensions.markup.html.form.DateTextField(
                 "dateOfBirthForHeader", dobHeaderModel, CommonUtils.UK_DATE_FORMAT) {
             @Override
             public boolean isVisible() {
                 return patientModel.getObject().getDob() != null;
             }
         };
         dateOfBirthForHeader.setOutputMarkupId(true);
         dateOfBirthForHeader.setOutputMarkupPlaceholderTag(true);
         componentsToUpdateList.add(dateOfBirthForHeader);
         componentsToUpdateList.add(dobLabel);
 
         form.add(diagnosis, surnameForHeader, forenameForHeader, dateOfBirthForHeader);
 
         // Sex
         RadarRequiredDropdownChoice sex =
                 new RadarRequiredDropdownChoice("sexModel", demographicsManager.getSexes(),
                         new ChoiceRenderer<Sex>("type", "id"), form, componentsToUpdateList);
 
         // Ethnicity
         DropDownChoice<Ethnicity> ethnicity = new DropDownChoice<Ethnicity>("ethnicity", utilityManager.
                 getEthnicities(), new ChoiceRenderer<Ethnicity>("name", "id"));
         form.add(sex, ethnicity);
 
         // Address fields
         TextField address1 = new TextField("address1");
         TextField address2 = new TextField("address2");
         TextField address3 = new TextField("address3");
         TextField address4 = new TextField("address4");
         RadarTextFieldWithValidation postcode = new RadarTextFieldWithValidation("postcode",
                 new PatternValidator("[a-zA-Z]{1,2}[0-9][0-9A-Za-z]{0,1} {0,1}[0-9][A-Za-z]{2}$"), form,
                 componentsToUpdateList);
 
         form.add(address1, address2, address3, address4, postcode);
         nonEditableComponents.add(address1);
         nonEditableComponents.add(address2);
         nonEditableComponents.add(address3);
         nonEditableComponents.add(address4);
         nonEditableComponents.add(postcode);
 
         // Archive fields
         TextField surnameAlias = new TextField("surnameAlias");
         TextField previousPostcode = new TextField("postcodeOld");
         form.add(surnameAlias, previousPostcode);
 
         // More info
         RadarRequiredTextField hospitalNumber =
                 new RadarRequiredTextField("hospitalnumber", form, componentsToUpdateList);
         TextField renalRegistryNumber = new TextField("rrNo");
         TextField ukTransplantNumber = new TextField("uktNo");
 
         form.add(hospitalNumber, renalRegistryNumber, ukTransplantNumber);
         nonEditableComponents.add(hospitalNumber);
         // Status, consultants and centres drop down boxes
         form.add(new DropDownChoice<Status>("statusModel", demographicsManager.getStatuses(),
                 new ChoiceRenderer<Status>("abbreviation", "id")));
 
         // Consultant and renal unit
         Label sourceUnitCodeLabel = new Label("sourceUnitCodeLabel", "Linked to") {
             @Override
             public boolean isVisible() {
                 return model.getObject().isLinked();
 
             }
         };
 
         String sourceUnitNameLabelValue = model.getObject().getPatientLinkUnitCode() != null
                 ? utilityManager.getCentre(model.getObject().getPatientLinkUnitCode()).getName() : "";
 
         Label sourceUnitCode = new Label("sourceUnitCode", sourceUnitNameLabelValue)
                  {
             @Override
             public boolean isVisible() {
                 return model.getObject().isLinked();
 
             }
         };
         form.add(sourceUnitCodeLabel, sourceUnitCode);
 
         final ClinicianDropDown clinician = new ClinicianDropDown("clinician", user, form.getModelObject());
         form.add(clinician);
 
         DropDownChoice<Centre> renalUnit = new PatientCentreDropDown("renalUnit", user, form.getModelObject());
 
         renalUnit.add(new AjaxFormComponentUpdatingBehavior("onchange") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 Patient patient = model.getObject();
                 if (patient != null) {
                     clinician.updateCentre(patient.getRenalUnit() != null ?
                             patient.getRenalUnit().getUnitCode() :
                             null);
                 }
 
                 clinician.clearInput();
                 target.add(clinician);
             }
         });
 
 
         form.add(renalUnit);
 
         final IModel<String> consentUserModel = new Model<String>(utilityManager.getUserName(
                 patientModel.getObject().getRadarConsentConfirmedByUserId()));
 
         form.add(new ExternalLink("consentFormsLink", "http://www.rarerenal.org/join/criteria-and-consent/"));
 
         final Label tickConsentUser = new Label("radarConsentConfirmedByUserId",
                 consentUserModel) {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotEmpty(consentUserModel.getObject());
             }
         };
         tickConsentUser.setOutputMarkupId(true);
         tickConsentUser.setOutputMarkupPlaceholderTag(true);
         form.add(tickConsentUser);
 
         final RadarRequiredCheckBox consent = new RadarRequiredCheckBox("consent", form, componentsToUpdateList);
 
         consent.add(new AjaxFormComponentUpdatingBehavior("onclick") {
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
 
                 target.add(tickConsentUser);
 
                 if (consent.getModel().getObject().equals(Boolean.TRUE)) {
                     consentUserModel.setObject(RadarSecuredSession.get().getUser().getName());
                     tickConsentUser.setVisible(true);
 
                 } else {
                     tickConsentUser.setVisible(false);
                 }
             }
         });
 
         form.add(consent);
 
         final Label successMessageTop = RadarComponentFactory.getSuccessMessageLabel("successMessageTop", form,
                 componentsToUpdateList);
         final Label successMessageBottom = RadarComponentFactory.getSuccessMessageLabel("successMessageBottom", form,
                 componentsToUpdateList);
 
         Label errorMessage = RadarComponentFactory.getErrorMessageLabel("errorMessage", form, componentsToUpdateList);
 
         AjaxSubmitLink ajaxSubmitLinkTop = new AjaxSubmitLink("saveTop") {
 
             @Override
             protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 ajaxRequestTarget.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
                 successMessageTop.setVisible(true);
                 successMessageBottom.setVisible(true);
                 ajaxRequestTarget.add(successMessageTop);
                 ajaxRequestTarget.add(successMessageBottom);
                 ajaxRequestTarget.appendJavaScript(RadarApplication.FORM_IS_DIRTY_FALSE_SCRIPT);
             }
 
             @Override
             protected void onError(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 ajaxRequestTarget.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
                 successMessageTop.setVisible(false);
                 successMessageBottom.setVisible(false);
                 ajaxRequestTarget.add(successMessageTop);
                 ajaxRequestTarget.add(successMessageBottom);
             }
         };
 
         ajaxSubmitLinkTop.add(new AttributeModifier("value", new AbstractReadOnlyModel() {
             @Override
             public Object getObject() {
                 return patientModel.getObject() == null ? "Add this patient" : "Update";
             }
         }));
 
         form.add(ajaxSubmitLinkTop);
 
         AjaxSubmitLink ajaxSubmitLinkBottom = new AjaxSubmitLink("saveBottom") {
 
             @Override
             protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
 
                 ajaxRequestTarget.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
                 successMessageTop.setVisible(true);
                 successMessageBottom.setVisible(true);
                 ajaxRequestTarget.add(successMessageTop);
                 ajaxRequestTarget.add(successMessageBottom);
                 ajaxRequestTarget.appendJavaScript(RadarApplication.FORM_IS_DIRTY_FALSE_SCRIPT);
             }
 
             @Override
             protected void onError(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 ajaxRequestTarget.add(componentsToUpdateList.toArray(new Component[componentsToUpdateList.size()]));
                 successMessageTop.setVisible(false);
                 successMessageBottom.setVisible(false);
                 ajaxRequestTarget.add(successMessageTop);
                 ajaxRequestTarget.add(successMessageBottom);
             }
         };
 
         ajaxSubmitLinkBottom.add(new AttributeModifier("value", new AbstractReadOnlyModel() {
             @Override
             public Object getObject() {
                 return patientModel.getObject() == null ? "Add this patient" : "Update";
             }
         }));
 
         form.add(ajaxSubmitLinkBottom);
 
         if (model.getObject().isLinked()) {
             for (Component component : nonEditableComponents) {
                 component.setEnabled(false);
             }
         }
     }
 
     @Override
     public boolean isVisible() {
         return ((SrnsPatientPage) getPage()).getCurrentTab().equals(SrnsPatientPage.CurrentTab.DEMOGRAPHICS);
     }
 }
