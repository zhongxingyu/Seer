 package specs.steps;
 
 import static org.hamcrest.Matchers.isIn;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 
 import specs.IterationResultExample;
 
 import com.bigvisible.kanbansimulator.IterationResult;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
 import cucumber.runtime.PendingException;
 
 public class SimpleSimulationSteps extends StepDefinitionForSimulatorSpecification {
 	
 	// TODO: find the right home for this method.  It is being used in multiple features.
 	@When("^the simulator completes a run$")
 	public void the_simulator_completes_a_run() {
 		getStimulator().run(getResultsOutput());
 		if (getResultsOutput() != null) {
 			try {
 				getResultsOutput().close();
 			} catch (IOException e) {
 				throw new RuntimeException(
 						"While attempting to flush and close \""
 								+ getResultsFile().getAbsolutePath() + "\"", e);
 			}
 		}
 	}
 
 	@Given("^the BA capacity is (\\d+) stories per iteration$")
 	public void the_BA_capacity_is_stories_per_iteration(
 			int businessAnalystCapacity) {
 		getStimulator().setBusinessAnalystCapacity(businessAnalystCapacity);
 	}
 
 	@Given("^the Dev capacity is (\\d+) stories per iteration$")
 	public void the_Dev_capacity_is_stories_per_iteration(
 			int developmentCapacity) {
 		getStimulator().setDevelopmentCapacity(developmentCapacity);
 	}
 
 	@Given("^the Web Dev capacity is (\\d+) stories per iteration$")
 	public void the_Web_Dev_capacity_is_stories_per_iteration(
 			int webDevelopmentCapacity) {
 		getStimulator().setWebDevelopmentCapacity(webDevelopmentCapacity);
 	}
 
 	@Given("^the QA capacity is (\\d+) stories per iteration$")
 	public void the_QA_capacity_is_stories_per_iteration(
 			int qualityAssuranceCapacity) {
 		getStimulator().setQualityAssuranceCapacity(qualityAssuranceCapacity);
 	}
 
 	@Given("^the backlog starts with (\\d+) stories$")
 	public void the_backlog_starts_with_stories(int numberOfStories) {
 		getStimulator().addStories(numberOfStories);
 	}
 
 	@Given("^the batch size is (\\d+) stories$")
 	public void the_batch_size_is_stories(int batchSize) {
 		getStimulator().setBatchSize(batchSize);
 	}
 
 	@Given("^the desired output is comma-separated values$")
 	public void the_desired_output_is_comma_separated_values() {
 		setResultsFile(new File("resultsOutput.csv"));
 
 		try {
 			setResultsOutput(new FileOutputStream(getResultsFile()));
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(
 					"Trying to open file \""
 							+ getResultsFile().getAbsolutePath()
 							+ "\" for writing.  Does this process have permissions to write in this directory?",
 					e);
 		}
 	}
 
 	@Then("^the simulator will have generated the following results:$")
 	public void the_simulator_will_have_generated_the_following_results(
 			List<IterationResultExample> results) {
 		List<IterationResultExample> actualResults = IterationResultExample
 				.asExample(getStimulator().results());
 
 		for (IterationResultExample iterationResultExample : results) {
			assertThat("... while looking for an example in the list of actual results ... (opposite of what you might assume)",
 					iterationResultExample, isIn(actualResults));
 		}
 	}
 
 	@Then("^the simulator generates a .csv file$")
 	public void the_simulator_generates_a_csv_file() {
 		assertTrue(getResultsFile().exists());
 	}
 
 	@Then("^the .csv file includes the following results:$")
 	public void the_csv_file_includes_the_following_results(
 			List<IterationResultExample> results) {
 		InputStream inputStream;
 		try {
 			inputStream = new FileInputStream(getResultsFile());
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException("Trying to open \""
 					+ getResultsFile().getAbsolutePath() + "\" for reading.", e);
 		}
 
 		List<IterationResult> resultsFromCSV = IterationResult
 				.parseCSV(inputStream);
 		List<IterationResultExample> actualContents = IterationResultExample
 				.asExample(resultsFromCSV);
 
 		for (IterationResultExample iterationResultExample : results) {
 			assertThat("(looking for example in the list of actual results)",
 					iterationResultExample, isIn(actualContents));
 		}
 	}
 }
