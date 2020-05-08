 package org.patientview.radar.web.panels;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
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
 import org.apache.wicket.model.LoadableDetachableModel;
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
 import org.patientview.radar.service.UserManager;
 import org.patientview.radar.service.UtilityManager;
 import org.patientview.radar.service.generic.DiseaseGroupManager;
 import org.patientview.radar.web.RadarApplication;
 import org.patientview.radar.web.RadarSecuredSession;
 import org.patientview.radar.web.components.CentreDropDown;
 import org.patientview.radar.web.components.ClinicianDropDown;
 import org.patientview.radar.web.components.RadarComponentFactory;
 import org.patientview.radar.web.components.RadarRequiredCheckBox;
 import org.patientview.radar.web.components.RadarRequiredDateTextField;
 import org.patientview.radar.web.components.RadarRequiredDropdownChoice;
 import org.patientview.radar.web.components.RadarRequiredTextField;
 import org.patientview.radar.web.components.RadarTextFieldWithValidation;
 import org.patientview.radar.web.models.RadarModelFactory;
 import org.patientview.radar.web.pages.patient.srns.SrnsPatientPage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
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
     private UserManager userManager;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(DemographicsPanel.class);
 
 
     public DemographicsPanel(String id, final Patient patient) {
 
         super(id);
 
         List<Component> nonEditableComponents = new ArrayList<Component>();
 
         setOutputMarkupId(true);
         setOutputMarkupPlaceholderTag(true);
 
         final User user = RadarSecuredSession.get().getUser();
 
         // Set up model - if given radar number loadable detachable getting demographics by radar number
         final CompoundPropertyModel<Patient> model = new CompoundPropertyModel<Patient>(
                 new LoadableDetachableModel<Patient>() {
                     @Override
                     public Patient load() {
 
                         return patient;
                     }
                 });
 
         // Set up form
         final Form<Patient> form = new Form<Patient>("form", model) {
             @Override
             protected void onSubmit() {
 
                 Patient patient = getModelObject();
                 patient.setRadarConsentConfirmedByUserId(user.getUserId());
 
                 try {
 
                     userManager.savePatientUser(patient);
 
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
                     Diagnosis diagnosis = diagnosisManager.getDiagnosisByRadarNumber(patient.getId());
                     if (diagnosis == null) {
                         Diagnosis diagnosisNew = new Diagnosis();
                         diagnosisNew.setRadarNumber(patient.getId());
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
                 return patient.isEditableDemographics();
             }
         });
 
         TextField<Long> radarNumberField = new TextField<Long>("radarNo", new Model<Long>(patient.getId()));
         radarNumberField.setEnabled(false);
         form.add(radarNumberField);
 
         DateTextField dateRegistered = DateTextField.forDatePattern("dateReg", RadarApplication.DATE_PATTERN);
 
         form.add(dateRegistered);
 
         RadarRequiredDropdownChoice diagnosis =
                 new RadarRequiredDropdownChoice("diagnosis", RadarModelFactory.getDiagnosisCodeModel(
                         new Model<Long>(patient.getId()),
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
 
         String diseaseGroup = patient.getDiseaseGroup().getId();
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
         Label nameLabel = new Label("nameLabel", "Name") {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patient.getForename());
             }
         };
         nameLabel.setOutputMarkupId(true);
         nameLabel.setOutputMarkupPlaceholderTag(true);
         form.add(nameLabel);
 
         TextField forenameForHeader = new TextField("forenameForHeader", RadarModelFactory.getFirstNameModel(
                 new Model<Long>(patient.getId()), demographicsManager)) {
             @Override
             public boolean isVisible() {
                 return StringUtils.isNotBlank(patient.getForename());
             }
         };
         forenameForHeader.setOutputMarkupId(true);
         forenameForHeader.setOutputMarkupPlaceholderTag(true);
         componentsToUpdateList.add(forenameForHeader);
 
 
         // surname
         TextField surnameForHeader = new TextField("surnameForHeader", RadarModelFactory.getSurnameModel(
                 new Model<Long>(patient.getId()), demographicsManager)) {
             @Override
             public boolean isVisible() {
 
                 return StringUtils.isNotBlank(patient.getSurname());
             }
         };
         surnameForHeader.setOutputMarkupId(true);
         surnameForHeader.setOutputMarkupPlaceholderTag(true);
         componentsToUpdateList.add(surnameForHeader);
 
         // date of birth
         Label dobLabel = new Label("dobLabel", "DoB") {
             @Override
             public boolean isVisible() {
                 return patient.getDob() != null;
             }
         };
         dobLabel.setOutputMarkupId(true);
         dobLabel.setOutputMarkupPlaceholderTag(true);
         form.add(dobLabel);
 
         TextField dateOfBirthForHeader = new org.apache.wicket.extensions.markup.html.form.DateTextField(
                 "dateOfBirthForHeader", RadarModelFactory.getDobModel(new Model<Long>(patient.getId()),
                 demographicsManager), RadarApplication.DATE_PATTERN) {
             @Override
             public boolean isVisible() {
                 return patient.getDob() != null;
             }
         };
         dateOfBirthForHeader.setOutputMarkupId(true);
         dateOfBirthForHeader.setOutputMarkupPlaceholderTag(true);
         componentsToUpdateList.add(dateOfBirthForHeader);
 
         form.add(diagnosis, surnameForHeader, forenameForHeader, dateOfBirthForHeader);
 
         // Sex
         RadarRequiredDropdownChoice sex =
                 new RadarRequiredDropdownChoice("sexModel", demographicsManager.getSexes(),
                         new ChoiceRenderer<Sex>("type", "id"), form, componentsToUpdateList);
 
         // Ethnicity
         DropDownChoice<Ethnicity> ethnicity = new DropDownChoice<Ethnicity>("ethnicity", utilityManager.
                 getEthnicities(), new ChoiceRenderer<Ethnicity>("name", "id"));
         form.add(sex, ethnicity);
         nonEditableComponents.add(sex);
 
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
         final IModel<String> centreNumber = new Model<String>();
         Centre renalUnitSelected = form.getModelObject().getRenalUnit();
         centreNumber.setObject(renalUnitSelected != null ? renalUnitSelected.getUnitCode() : null);
 
         final ClinicianDropDown clinician = new ClinicianDropDown("clinician", centreNumber);
         form.add(clinician);
 
         DropDownChoice<Centre> renalUnit;
 
 
         // if its a super user then the drop down will let them change renal units
         // if its a normal user they can only add to their own renal unit
         if (user.getSecurityRole().equals(User.ROLE_SUPER_USER)) {
             renalUnit = new CentreDropDown("renalUnit", model.getObject().getNhsno());
 
             renalUnit.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                 @Override
                 protected void onUpdate(AjaxRequestTarget target) {
                     Patient patient = model.getObject();
                     if (patient != null) {
                         centreNumber.setObject(patient.getRenalUnit() != null ?
                                 patient.getRenalUnit().getUnitCode() :
                                 null);
                     }
 
                     clinician.clearInput();
                     target.add(clinician);
                 }
             });
         } else {
             List<Centre> centres = new ArrayList<Centre>();
 
             for (String unitCode : userManager.getUnitCodes(RadarSecuredSession.get().getUser())) {
                 Centre centre = new Centre();
                 centre.setUnitCode(unitCode);
                 centre.setName(unitCode);
                 centres.add(centre);
             }
 
             renalUnit = new CentreDropDown("renalUnit", centres);
         }
 
         form.add(renalUnit);
        //nonEditableComponents.add(renalUnit);
 
         RadarRequiredCheckBox consent = new RadarRequiredCheckBox("consent", form, componentsToUpdateList);
         form.add(consent);
 
         form.add(new ExternalLink("consentFormsLink", "http://www.rarerenal.org/join/criteria-and-consent/"));
 
         Label tickConsentUser = new Label("radarConsentConfirmedByUserId",
                 model.getObject() != null ? utilityManager.getUserName(
                         model.getObject().getRadarConsentConfirmedByUserId()) : "") {
             @Override
             public boolean isVisible() {
                 return true;
             }
         };
         tickConsentUser.setOutputMarkupId(true);
         tickConsentUser.setOutputMarkupPlaceholderTag(true);
         form.add(tickConsentUser);
 
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
                 return patient == null ? "Add this patient" : "Update";
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
                 return patient == null ? "Add this patient" : "Update";
             }
         }));
 
         form.add(ajaxSubmitLinkBottom);
 
         if (!model.getObject().isEditableDemographics()) {
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
