 package org.sukrupa.cucumber.context;
 
 import cuke4duke.annotation.Before;
 import org.sukrupa.cucumber.steps.BasicWebSteps;
 
 import static org.sukrupa.cucumber.SahiFacade.browser;
 
 public class OnCreateEventPage extends BasicWebSteps{
 
     @Before("@OnCreateEventPage")
     public void OnCreateEventPage(){
        browser().navigateTo(BasicWebSteps.HOME_PAGE + "events/create");
        // MAKE LINE BELOW WORK!!!!!
        //browser().link("Create an Event").click();
     }
     
 }
