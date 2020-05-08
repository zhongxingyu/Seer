 package acceptance;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
 
 public class CucumberStepDefs {
 	@Given("^the list for module (.+):$")
	//DataTable arg1
 	public void the_list_below(String moduleName,
 			List<ScenarioQuestion> questions) throws Throwable {
		assertEquals(2,questions.size());//should be 2!
		assertEquals("Age",questions.get(0).getQuestion());
 	}
 
 	@When("^foo$")
 	public void foo() throws Throwable {
 	    
 	}

	@Then("^bar$")
	public void bar() throws Throwable {
	    
 	}
 
 }
