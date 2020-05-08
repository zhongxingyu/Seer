 package com.solidstategroup.radar.web.pages;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 
 public abstract class BasePage extends WebPage {
 
     public BasePage() {
         add(new Label("title", getTitle()));
 
         BookmarkablePageLink[] naviationPages = {
                new BookmarkablePageLink("homePage", HomePage.class),
                new BookmarkablePageLink("proRegistrationPage", RegistrationProfessionalPage.class),
                new BookmarkablePageLink("professionalsPage", ProfessionalsPage.class),
                new BookmarkablePageLink("patientsPage", PatientPage.class),
                new BookmarkablePageLink("diseaseIndexPage", DiseaseIndexPage.class),

                // Todo: This is wrong for now
                new BookmarkablePageLink("demographicsPage", PatientPage.class),

                new BookmarkablePageLink("patientsListingPage", PatientsListingPage.class),
                new BookmarkablePageLink("recruitmentPage", RecruitmentPage.class),
                new BookmarkablePageLink("mpgnPage", MpgnPage.class),
                new BookmarkablePageLink("srnsPage", SrnsPage.class),
                new BookmarkablePageLink("consentFormsPage", ConsentFormsPage.class)
         };
 
         add(naviationPages);
     }
 
     public String getTitle() {
         return "RADAR - National Renal Rare Disease Registry";
     }
 
 }
