 package org.sukrupa.cucumber.context;
 
 import cuke4duke.annotation.Before;
 import org.sukrupa.cucumber.steps.BasicWebSteps;
 
 import static org.sukrupa.cucumber.SahiFacade.browser;
 
 public class OnAddNewTalentPage extends BasicWebSteps {
 
    private final String homePage = "http://localhost:8080/students/";

     @Before("@OnAddNewTalentPage")
     public void addNewTalentPage(){
        browser().navigateTo(homePage);
        browser().link("Tools").click();
        browser().link("Add New Talent").click();
     }
 
 
 }
