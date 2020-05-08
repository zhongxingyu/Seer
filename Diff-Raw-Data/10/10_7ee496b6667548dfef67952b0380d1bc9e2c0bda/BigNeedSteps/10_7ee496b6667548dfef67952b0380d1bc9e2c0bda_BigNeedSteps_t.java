 package org.sukrupa.cucumber.steps;
 
 import cuke4duke.annotation.Before;
 import cuke4duke.annotation.I18n.EN.*;
 import cuke4duke.annotation.Pending;
 import net.sf.sahi.client.Browser;
 import org.sukrupa.cucumber.context.Login;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
import static org.sukrupa.cucumber.CucumberFacade.getConfigProperty;
 import static org.sukrupa.cucumber.SahiFacade.browser;
 
public class BigNeedSteps extends Login {
     private static final String TOP_LEVEL_DIV = "page";
 
     @Given("^I am on the Big Needs page$")
     public void navigateTo() {
        browser().navigateTo(getConfigProperty("homepage"));
        browser().link("Big Needs").click();
     }
 
 
 }
