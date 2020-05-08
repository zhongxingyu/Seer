 package cucumber.todo.stepdefinition;
 
 import cucumber.api.java.After;
 import cucumber.api.java.Before;
 import cucumber.api.java.en.Given;
 import cucumber.api.java.en.Then;
 import cucumber.api.java.en.When;
 import cucumber.todo.pageobject.ToDoPage;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import static cucumber.todo.hook.JettyRunner.getBaseUrl;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 public class ToDoSteps {
     private ToDoPage page;
 
     @Before
     public void setUpScenario() throws Exception {
         page = new ToDoPage(new FirefoxDriver());
     }
 
     @After
     public void tearDownScenario() {
         page.close();
     }
 
     @Given("^I am on the To-Do page$")
     public void I_am_on_the_To_Do_page() throws Throwable {
         page.open(getBaseUrl());
     }
 
     @When("^I fill in new todo with \"([^\"]*)\"$")
     public void I_fill_in_new_todo_with(String label) throws Throwable {
         page.fillInNewItem(label);
     }
 
     @When("^I press Enter$")
     public void I_press_enter() throws Throwable {
         page.pressReturnOnCurrentItem();
     }
 
     @Then("^I should see \"([^\"]*)\" item$")
     public void I_should_see_item(String label) throws Throwable {
         WebElement item = page.findItem(label);
 
         assertTrue(item.isDisplayed());
     }
 
     @Then("^I should see \"([^\"]*)\"$")
     public void I_should_see(String text) throws Throwable {
         WebElement item = page.findByText(text);
 
         assertTrue(item.isDisplayed());
     }
 
     @Given("^I have created \"([^\"]*)\" item$")
     public void I_have_created_item(String label) throws Throwable {
         I_am_on_the_To_Do_page();
         I_fill_in_new_todo_with(label);
         I_press_enter();
     }
 
     @When("^I mark \"([^\"]*)\" item as done$")
     public void I_mark_item_as_done(String label) throws Throwable {
         WebElement checkbox = page.findAndClickOnItemCheckbox(label);
         WebElement item = page.findItem(label);
 
         assertThat(checkbox.getAttribute("checked"), is("true"));
         assertThat(item.getAttribute("className"), is("done"));
     }
 
     @Then("^I should see completed \"([^\"]*)\" item$")
     public void I_should_see_completed_item(String label) throws Throwable {
         WebElement item = page.findItem(label);
 
         assertTrue(item.isDisplayed());
         assertThat(item.getAttribute("className"), is("done"));
     }
 
     @When("^I double click on \"([^\"]*)\" item$")
     public void I_double_click_on_item(String label) throws Throwable {
         page.performDoubleClickOnItem(label);
     }
 
     @When("^I replace \"([^\"]*)\" item label with \"([^\"]*)\"$")
     public void I_replace_item_label_with(String oldLabel, String newLabel) throws Throwable {
         page.findAndEditItemField(oldLabel, newLabel);
     }
 
     @Then("^I should not see \"([^\"]*)\" item$")
     public void I_should_not_see_item(String label) throws Throwable {
        try {
            page.findItem(label);
            fail("to-do item with <" + label + "> is present.");
        } catch (NoSuchElementException e) {
            // Do nothing
        }
     }
 
     @Given("^I have completed \"([^\"]*)\" item$")
     public void I_have_completed_item(String label) throws Throwable {
         I_have_created_item(label);
         I_mark_item_as_done(label);
     }
 
     @When("^I click on \"([^\"]*)\"$")
     public void I_click_on(String label) throws Throwable {
         page.clickOnLink(label);
     }
 
 }
