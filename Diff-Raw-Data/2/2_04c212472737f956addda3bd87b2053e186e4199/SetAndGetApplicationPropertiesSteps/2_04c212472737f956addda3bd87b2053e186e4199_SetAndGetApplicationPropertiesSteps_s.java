 package com.us.fountainhead.gifnoc;
 
 import com.us.fountainhead.gifnoc.server.entity.*;
 import com.us.fountainhead.gifnoc.server.service.PropertyService;
 import com.us.fountainhead.gifnoc.server.validate.ValidationException;
 import org.jbehave.core.annotations.*;
 import org.jbehave.core.model.ExamplesTable;
 import org.jbehave.core.steps.Parameters;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 /**
  *  Makes the steps executable
  */
 @Component
 public class SetAndGetApplicationPropertiesSteps {
 
     @Autowired
     PropertyService propertyService;
 
     @Autowired
     TearDown tearDown;
 
     private List<String> actualValues;
 
     @BeforeStory
     public void setUp() {
         actualValues = new ArrayList<String>();
     }
 
     /**
      * Sets up initial conditions for the test
      *
      * @param appName - name of the application
      * @param environments - list of environments for the application
      * @param propertyNames - list of property names for the application
      * @param propertyValues - list of property values
      * @throws ValidationException
      */
    @Given("an application named: $appName with environments: $environments that has properties named: $propertyNames with property values: propertyValues")
     public void setupProperties(String appName, ExamplesTable environments, ExamplesTable propertyNames, ExamplesTable propertyValues) throws ValidationException {
         String envName, propertyName, propertyValue;
 
         Application application = new Application();
         application.setName(appName);
 
         for(Parameters row : environments.getRowsAsParameters()) {
             envName = row.valueAs("environment", String.class);
             Environment environment = new Environment();
             environment.setName(envName);
             application.addEnvironment(environment);
         }
 
         for(Parameters row : propertyNames.getRowsAsParameters()) {
             propertyName = row.valueAs("property", String.class);
             Property property = new Property();
             property.setName(propertyName);
             application.addProperty(property);
         }
 
         propertyService.createApplication(application);
 
         for(Parameters row : propertyValues.getRowsAsParameters()) {
             envName = row.valueAs("environment", String.class);
             propertyName = row.valueAs("property name", String.class);
             propertyValue = row.valueAs("property value", String.class);
 
             propertyService.setPropertyValue(appName, envName, propertyName, propertyValue);
         }
     }
 
     /**
      * Get the value of valid properties
      * @param propertyNames - Names of properties to get
      * @throws ValidationException
      */
     @When("I request the values of the following properties: $propertyNames")
     public void getValidPropertyValues(ExamplesTable propertyNames) throws ValidationException {
         String appName, envName, propertyName, propertyValue;
 
         for(Parameters row : propertyNames.getRowsAsParameters()) {
             appName = row.valueAs("application", String.class);
             envName = row.valueAs("environment", String.class);
             propertyName = row.valueAs("property name", String.class);
             propertyValue = propertyService.getPropertyValue(appName, envName, propertyName);
             actualValues.add(propertyValue);
         }
     }
 
     /**
      * Undefined property case
      *
      * @param propertyNames - Names of properties to get
      */
     @When("I request the values of the following undefined properties: $propertyNames")
     public void getInvalidPropertyNames(ExamplesTable propertyNames) {
         String appName, envName, propertyName, actualValue;
 
         for(Parameters row : propertyNames.getRowsAsParameters()) {
             appName = row.valueAs("application", String.class);
             envName = row.valueAs("environment", String.class);
             propertyName = row.valueAs("property name", String.class);
             try {
                 actualValue = propertyService.getPropertyValue(appName, envName, propertyName);
             } catch (ValidationException e) {
                 actualValue = e.getErrors().get(0).getText();
             }
 
             actualValues.add(actualValue);
         }
     }
 
     /**
      *
      * @param propertyValues - expected property values
      * @throws ValidationException
      */
     @Then("the following property values are returned: $propertyValues")
     public void validatePropertyValues(ExamplesTable propertyValues) throws ValidationException {
         String expectedPropertyValue;
         int i = 0;
         for(Parameters row : propertyValues.getRowsAsParameters()) {
             expectedPropertyValue = row.valueAs("property value", String.class);
             assertEquals(expectedPropertyValue, actualValues.get(i));
             i++;
         }
     }
 
     /**
      * Check for expected error messages
      *
      * @param errorMessages - expected error messages
      */
     @Then("the following error messages are received: $errorMessages")
     public void validateErrorMessages(ExamplesTable errorMessages) {
         String expectedErrorMessage;
         int i = 0;
         for(Parameters row : errorMessages.getRowsAsParameters()) {
             expectedErrorMessage = row.valueAs("error message", String.class);
             assertEquals(expectedErrorMessage, actualValues.get(i));
             i++;
         }
     }
 
     @AfterStory
     public void tearDown() throws ValidationException {
         tearDown.execute();
     }
 }
