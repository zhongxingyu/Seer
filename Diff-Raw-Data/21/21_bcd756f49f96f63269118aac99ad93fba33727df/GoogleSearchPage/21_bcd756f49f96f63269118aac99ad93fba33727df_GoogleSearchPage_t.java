 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cuke4duke.annotation.I18n.EN.When;
 
 public class GoogleSearchPage {
 
 	private WebDriver driver;
 
 	@FindBy(name = "q")
 	private WebElement searchField;
 
 	@FindBy(name = "btnG")
 	private WebElement searchButton;
 
 	@FindBy(id = "ires")
 	private WebElement divResult;
 
 	public GoogleSearchPage(WebDriver driver) {
 		this.driver = driver;
 	}
 
 	@Given("I am on the Google search page")
 	public void visit() {
 		driver.get("http://google.com/");
 	}
 
	@When("I search for \"([^\"]*)\"$")
 	public void search(String query) {
 		searchField.sendKeys(query);
 		searchButton.click();
 	}
 
	@Then("I should see$")
 	public void shouldSee(String results) {
 		assertThat(divResult.getText(), containsString(results));
 	}
 
 }
