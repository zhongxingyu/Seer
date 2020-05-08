 package acceptance;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
 
 public class CucumberStepDefs {
	
 	@Given("^the list for module (.+):$")
 	public void the_list_below(String moduleName,
 			List<ScenarioQuestion> questions) throws Throwable {
		assertEquals(3,questions.size());//should be 2!
//		assertEquals("Age",questions.get(0).getQuestion());
 	}
 
 	@When("^foo$")
 	public void foo() throws Throwable {
 	    
 	}
	
	@Then("^I should see the following list:$")
	public void I_should_see_the_following_list(List<ScenarioAnswer> scenarioAudit) throws Throwable {
	
 	}
	
 
 }
