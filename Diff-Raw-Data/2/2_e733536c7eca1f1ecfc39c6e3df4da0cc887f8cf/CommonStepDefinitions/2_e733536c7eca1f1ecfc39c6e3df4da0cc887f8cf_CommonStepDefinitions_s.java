 package com.openfeint.qa.ggp.step_definitions;
 
 import com.openfeint.qa.core.caze.step.definition.BasicStepDefinition;
 import com.openfeint.qa.core.command.Then;
 
 public class CommonStepDefinitions extends BasicStepDefinition {
     @Then("(.*)as ios automation(.*)")
     public void ignoreIosStep(){
         // do nothing
     }
     @Then("(.*)as server automation(.*)")
    public void ignoreAndroidStep(){
         // do nothing
     }
 }
