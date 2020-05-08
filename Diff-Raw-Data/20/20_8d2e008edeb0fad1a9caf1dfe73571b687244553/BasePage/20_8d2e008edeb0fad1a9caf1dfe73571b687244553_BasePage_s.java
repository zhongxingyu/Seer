 package com.solidstategroup.radar.web.pages;
 
 import com.solidstategroup.radar.model.user.User;
 import com.solidstategroup.radar.web.RadarSecuredSession;
 import com.solidstategroup.radar.web.behaviours.RadarStyleBehaviour;
 import com.solidstategroup.radar.web.pages.content.ConsentFormsPage;
 import com.solidstategroup.radar.web.pages.content.DiseaseIndexPage;
 import com.solidstategroup.radar.web.pages.content.MpgnPage;
 import com.solidstategroup.radar.web.pages.content.SrnsPage;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 
 public abstract class BasePage extends WebPage {
 
     public BasePage() {
 
         boolean userLoggedIn = (isProfessionalOrSuperUserLoggedIn() || isPatientUserLoggedIn());
 
        // Attach styles
        add(new RadarStyleBehaviour());

         add(new Label("title", getTitle()));
 
         // Generic links
         add(new BookmarkablePageLink<HomePage>("homePageLink", HomePage.class));
         add(new BookmarkablePageLink<DiseaseIndexPage>("diseaseIndexPageLink", DiseaseIndexPage.class));
 
         AjaxLink logoutLink = new AjaxLink<HomePage>("logoutLink") {
             @Override
             public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                 RadarSecuredSession.get().invalidate();
                 setResponsePage(HomePage.class);
             }
         };
         logoutLink.setVisible(userLoggedIn);
         add(logoutLink);
 
         // Enter new patient - only visible when a professional is logged in
         BookmarkablePageLink enterNewPatientPageLink = new BookmarkablePageLink<PatientPage>("enterNewPatientPageLink",
                 PatientPage.class);
         enterNewPatientPageLink.setVisible(isProfessionalOrSuperUserLoggedIn());
         add(enterNewPatientPageLink);
 
         // Container for existing patients links, only visible when a professional is logged in
         MarkupContainer existingPatientsContainer = new WebMarkupContainer("existingPatientsContainer");
         existingPatientsContainer.setVisible(isProfessionalOrSuperUserLoggedIn());
         existingPatientsContainer.add(
                 new BookmarkablePageLink<ExistingPatientsListingPage>("patientsListingPageLink", ExistingPatientsListingPage.class),
                 new BookmarkablePageLink<RecruitmentPage>("recruitmentPageLink", RecruitmentPage.class)
         );
         add(existingPatientsContainer);
 
         // Container for clinicians links
         MarkupContainer cliniciansContainer = new WebMarkupContainer("cliniciansContainer");
         cliniciansContainer.setVisible(isProfessionalOrSuperUserLoggedIn());
         cliniciansContainer.add(
                 new BookmarkablePageLink<MpgnPage>("mpgnPageLink", MpgnPage.class),
                 new BookmarkablePageLink<SrnsPage>("srnsPageLink", SrnsPage.class),
                 new BookmarkablePageLink<ConsentFormsPage>("consentFormsPageLink", ConsentFormsPage.class)
         );
         add(cliniciansContainer);
 
         // only want to show if on patient login page
         BookmarkablePageLink patientRegistrationLink = new BookmarkablePageLink<PatientRegistrationPage>(
                 "patientRegistrationPageLink", PatientRegistrationPage.class);
         patientRegistrationLink.setVisible(getPageClass() == PatientsLoginPage.class);
         add(patientRegistrationLink);
 
         // only want to show on professional login page or homepage
         BookmarkablePageLink professionalRegistrationPageLink = new BookmarkablePageLink<ProfessionalRegistrationPage>(
                 "professionalRegistrationPageLink", ProfessionalRegistrationPage.class);
         professionalRegistrationPageLink.setVisible(getPageClass() == ProfessionalsLoginPage.class
                 || getPageClass() == HomePage.class);
         add(professionalRegistrationPageLink);
 
         BookmarkablePageLink professionalsPageLink = new BookmarkablePageLink<ProfessionalsPage>(
                 "professionalsPageLink", ProfessionalsPage.class);
         professionalsPageLink.setVisible(!userLoggedIn);
         add(professionalsPageLink);
 
         BookmarkablePageLink patientsPageLink = new BookmarkablePageLink<PatientPageReadOnly>("patientsPageLink",
                 PatientPageReadOnly.class);
         patientsPageLink.setVisible(!userLoggedIn);
         add(patientsPageLink);
     }
 
     public String getTitle() {
         return "RADAR - National Renal Rare Disease Registry";
     }
 
     protected boolean isProfessionalOrSuperUserLoggedIn() {
         RadarSecuredSession session = RadarSecuredSession.get();
         return session.isSignedIn() ? session.getRoles().hasRole(User.ROLE_PROFESSIONAL) ||
                 session.getRoles().hasRole(User.ROLE_SUPER_USER) : false;
     }
 
     protected boolean isPatientUserLoggedIn() {
         RadarSecuredSession session = RadarSecuredSession.get();
         return session.isSignedIn() ? session.getRoles().hasRole(User.ROLE_PATIENT) : false;
     }
 
 }
