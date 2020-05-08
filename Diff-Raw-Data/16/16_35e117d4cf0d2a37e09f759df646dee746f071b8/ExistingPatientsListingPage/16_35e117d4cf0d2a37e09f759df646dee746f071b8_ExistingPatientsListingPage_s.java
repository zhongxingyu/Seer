 package com.solidstategroup.radar.web.pages;
 
 import com.solidstategroup.radar.model.Centre;
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.model.DiagnosisCode;
 import com.solidstategroup.radar.model.user.ProfessionalUser;
 import com.solidstategroup.radar.model.user.User;
 import com.solidstategroup.radar.service.DemographicsManager;
 import com.solidstategroup.radar.service.DiagnosisManager;
 import com.solidstategroup.radar.web.RadarApplication;
 import com.solidstategroup.radar.web.RadarSecuredSession;
 import com.solidstategroup.radar.web.dataproviders.DemographicsDataProvider;
 import com.solidstategroup.radar.web.models.RadarModelFactory;
 import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.datetime.markup.html.basic.DateLabel;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 @AuthorizeInstantiation({User.ROLE_PROFESSIONAL, User.ROLE_SUPER_USER})
 public class ExistingPatientsListingPage extends BasePage {
 
     @SpringBean
     private DemographicsManager demographicsManager;
     @SpringBean
     private DiagnosisManager diagnosisManager;
 
 
     public ExistingPatientsListingPage() {
 

         Centre centre = null;
         AuthenticatedWebSession session = RadarSecuredSession.get();
         if (session.isSignedIn()) {
             if (session.getRoles().hasRole(User.ROLE_SUPER_USER)) {
                 // super user can see all patients - centre might be already null but setting to null again for clarity
                 centre = null;
             } else if (session.getRoles().hasRole(User.ROLE_PROFESSIONAL)) {
                 //get centre from the logged in professional user
                 ProfessionalUser professionalUser = (ProfessionalUser) RadarSecuredSession.get().getUser();
                 centre = professionalUser.getCentre();
             }
         }
 
         DemographicsDataProvider demographicsDataProvider = new DemographicsDataProvider(demographicsManager, centre);
 
         // List existing patients
         add(new DataView<Demographics>("patients", demographicsDataProvider) {
             @Override
             protected void populateItem(Item<Demographics> item) {
                 // Populate fields
                 item.add(new BookmarkablePageLink<PatientPage>("edit", PatientPage.class, PatientPage.getParameters(item.getModelObject())));
                 item.add(new Label("surname"), new Label("forename"));
                item.add(DateLabel.forDatePattern("dateOfBirth", RadarApplication.DATE_PATTERN));
                 item.add(new Label("id"));
 
                 IModel<DiagnosisCode> diagnosisCodeModel = RadarModelFactory.getDiagnosisCodeModel(new Model<Long>(
                         item.getModelObject().getId()), diagnosisManager);
 
                 DiagnosisCode diagnosisCode = diagnosisCodeModel.getObject();
 
                 item.add(new Label("diagnosis", diagnosisCode != null ? diagnosisCode.getAbbreviation() : null));
                 item.add(new Label("nhsNumber"));
                 item.add(new Label("hospitalNumber"));
                item.add(DateLabel.forDatePattern("dateRegistered", RadarApplication.DATE_PATTERN));
                 item.add(new Label("status.abbreviation"));
             }
         });
     }
 }
