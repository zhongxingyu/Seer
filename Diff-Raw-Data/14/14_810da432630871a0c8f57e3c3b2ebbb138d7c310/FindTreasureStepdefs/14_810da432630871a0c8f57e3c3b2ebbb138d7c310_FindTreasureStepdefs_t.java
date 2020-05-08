 package ua.com.mcgray.bdd.tools;
 
import cucumber.api.java.After;
 import cucumber.api.java.en.Given;
 import cucumber.api.java.en.Then;
 import cucumber.api.java.en.When;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import ua.com.mcgray.bdd.tools.pages.GitHubProfilePage;
 import ua.com.mcgray.bdd.tools.pages.GitHubRepositoryPage;
 import ua.com.mcgray.bdd.tools.pages.GitHubSearchPage;
 import ua.com.mcgray.bdd.tools.pages.GitHubSearchResultPage;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author orezchykov
  * @since 28.02.13
  */
 public class FindTreasureStepdefs {
 
     private WebDriver webDriver = new ChromeDriver();
 
     private GitHubSearchPage gitHubSearchPage = new GitHubSearchPage(webDriver);
     private GitHubSearchResultPage gitHubSearchResultPage = new GitHubSearchResultPage(webDriver);
     private GitHubProfilePage gitHubProfilePage = new GitHubProfilePage(webDriver);
     private GitHubRepositoryPage gitHubRepositoryPage = new GitHubRepositoryPage(webDriver);
 
 
 
     @Given("^the user is on the GitHub search page$")
     public void the_user_is_on_the_github_search_page() {
         gitHubSearchPage.open();
     }
 
     @When("^the user looks up for (.+)$")
     public void the_user_looks_up_for(String name) {
         gitHubSearchPage.search_for(name);
     }
 
     @Then("^the one should see the (.+) in test results$")
     public void the_one_should_see_the_in_test_results(String name) {
         assertThat(gitHubSearchResultPage.get_search_result_list(), containsString(name));
     }
 
     @Given("^GitHub profile page$")
     public void github_profile_page() throws InterruptedException {
         gitHubSearchResultPage.proceed_to_profile();
     }
 
     @When("^the user inspects (.+) profile page$")
     public void the_user_inspects__profile_page(String name) {
         gitHubProfilePage.click_repo_tab();
     }
 
     @Then("^(.+) exists as a repository$")
     public void exists_as_a_repository(String repoName) {
         assertThat(gitHubProfilePage.get_repositories(), containsString(repoName));
     }
 
     @Given("^cucumber-jvm-html-elements project$")
      public void cucumber_jvm_html_elements_project() {
         gitHubProfilePage.proceed_to_repo();
     }
 
     @When("^browsing to test in (.+)$")
     public void  browsing_to_test_in(String projectName) throws InterruptedException {
         gitHubRepositoryPage.browse_to_project_test(projectName);
     }
 
     @Then("^(.+) is found in the test code$")
     public void is_found_in_the_test_code(String text) {
         assertThat(gitHubRepositoryPage.get_file_content(), containsString(text));
     }
 
    @After
    public void tearDown() {
        webDriver.close();
    }

     //That is a hidden treasure
     // that the test should find
 }
