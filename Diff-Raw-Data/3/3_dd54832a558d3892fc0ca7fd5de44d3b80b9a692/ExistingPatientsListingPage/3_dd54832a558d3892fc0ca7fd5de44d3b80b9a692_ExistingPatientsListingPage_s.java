 package org.patientview.radar.web.pages.patient;
 
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.datetime.markup.html.basic.DateLabel;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.patientview.model.Patient;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.model.user.DemographicsUserDetail;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.service.DemographicsManager;
 import org.patientview.radar.service.DiagnosisManager;
 import org.patientview.radar.web.RadarApplication;
 import org.patientview.radar.web.RadarSecuredSession;
 import org.patientview.radar.web.dataproviders.DemographicsDataProvider;
 import org.patientview.radar.web.pages.BasePage;
 import org.patientview.radar.web.pages.patient.alport.AlportPatientPage;
 import org.patientview.radar.web.pages.patient.hnf1b.HNF1BPatientPage;
 import org.patientview.radar.web.pages.patient.srns.SrnsPatientPage;
 
 import java.util.Date;
 
 @AuthorizeInstantiation({User.ROLE_PROFESSIONAL, User.ROLE_SUPER_USER})
 public class ExistingPatientsListingPage extends BasePage {
 
     @SpringBean
     private DemographicsManager demographicsManager;
 
     @SpringBean
     private DiagnosisManager diagnosisManager;
 
     public ExistingPatientsListingPage() {
 
         DemographicsDataProvider demographicsDataProvider = new DemographicsDataProvider(demographicsManager,
                 RadarSecuredSession.get().getUser());
 
         // List existing patients
         add(new DataView<Patient>("patients", demographicsDataProvider) {
             @Override
             protected void populateItem(Item<Patient> item) {
                 // Populate fields
                 Patient patient = item.getModelObject();
 
                 // TODO: this is terrible as we need to check disease groups to know where to send it - well done abul
                 // TODO: need to implement a patient base page with the constructors needed and then have an enum map
                 // TODO: that maps disease ids to the page they need to go to so we dont need all these ifs
                 if (patient.getDiseaseGroup() != null && (patient.getDiseaseGroup().getId().equals(
                         DiseaseGroup.SRNS_DISEASE_GROUP_ID) || patient.getDiseaseGroup().getId().
                         equals(DiseaseGroup.MPGN_DISEASEGROUP_ID))) {
                     item.add(new BookmarkablePageLink<SrnsPatientPage>("edit", SrnsPatientPage.class,
                             SrnsPatientPage.getParameters(patient)));
                 } else if (patient.getDiseaseGroup() != null && patient.getDiseaseGroup().getId().equals(
                         DiseaseGroup.ALPORT_DISEASEGROUP_ID)) {
                     item.add(new BookmarkablePageLink<AlportPatientPage>("edit", AlportPatientPage.class,
                             AlportPatientPage.getPageParameters(patient)));
                 } else if (patient.getDiseaseGroup() != null && patient.getDiseaseGroup().getId().equals(
                         DiseaseGroup.HNF1B_DISEASEGROUP_ID)) {
                     item.add(new BookmarkablePageLink<AlportPatientPage>("edit", HNF1BPatientPage.class,
                             HNF1BPatientPage.getPageParameters(patient)));
                 } else {
                     item.add(new BookmarkablePageLink<GenericPatientPage>("edit", GenericPatientPage.class,
                             GenericPatientPage.getPageParameters(patient)));
                 }
 
                 item.add(new Label("surname"), new Label("forename"));
                 item.add(DateLabel.forDatePattern("dob", RadarApplication.DATE_PATTERN2));
                 item.add(new Label("id"));
 
                 String diseaseGroup = "";
                 if (patient.getDiseaseGroup() != null) {
                     diseaseGroup = patient.getDiseaseGroup().getId();
                 }
 
                 item.add(new Label("diagnosis", diseaseGroup));
 
                 item.add(new Label("nhsNumber", patient.getNhsno()));
                 item.add(new Label("hospitalnumber"));
                 item.add(DateLabel.forDatePattern("dateReg", RadarApplication.DATE_PATTERN2));
                item.add(new Label("status.abbreviation"));
                 item.add(new Label("renalUnit.name"));
 
                 item.add(new Label("rrtModalityEunm"));
                 DemographicsUserDetail demographicsUserDetail =
                         demographicsManager.getDemographicsUserDetail(patient.getNhsno(), patient.getUnitcode());
 
                 item.add(new Label("lastverificationdate",
                         formatDate(demographicsUserDetail.getLastverificationdate())));
                 item.add(new Label("email", patient.getEmailAddress()));
 
                 item.add(new Label("lastlogon", formatDate(demographicsUserDetail.getLastlogon())));
                 item.add(new Label("accountlocked", "" + (demographicsUserDetail.isAccountlocked() ? "Yes" : "No")));
             }
 
             private String formatDate(Date date){
                 return date == null? "" : DateFormatUtils.format(date, RadarApplication.DATE_PATTERN2);
             }
         });
     }
 }
