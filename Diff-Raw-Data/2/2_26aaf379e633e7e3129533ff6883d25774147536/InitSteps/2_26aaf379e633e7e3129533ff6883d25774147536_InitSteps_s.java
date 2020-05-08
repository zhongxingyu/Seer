 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.cli.test.functional;
 
 import static org.geogit.cli.test.functional.GlobalState.currentDirectory;
 import static org.geogit.cli.test.functional.GlobalState.geogit;
 import static org.geogit.cli.test.functional.GlobalState.geogitCLI;
 import static org.geogit.cli.test.functional.GlobalState.homeDirectory;
 import static org.geogit.cli.test.functional.GlobalState.stdOut;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.geogit.api.GeoGIT;
 import org.geogit.api.GlobalInjectorBuilder;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.plumbing.RefParse;
 import org.geogit.api.plumbing.UpdateRef;
 
 import com.google.common.base.Optional;
 import com.google.inject.Injector;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
 
 /**
  *
  */
 public class InitSteps extends AbstractGeogitFunctionalTest {
 
     @cucumber.annotation.After
     public void after() {
         if (GlobalState.geogit != null) {
             GlobalState.geogit.close();
         }
         deleteDirectories();
     }
 
     @Given("^I am in an empty directory$")
     public void I_am_in_an_empty_directory() throws Throwable {
         setUpDirectories();
         assertEquals(0, currentDirectory.list().length);
         setupGeogit();
     }
 
     @When("^I run the command \"([^\"]*)\"$")
     public void I_run_the_command_X(String commandSpec) throws Throwable {
         String[] args = commandSpec.split(" ");
         runCommand(args);
     }
 
     @Then("^it should answer \"([^\"]*)\"$")
     public void it_should_answer_exactly(String expected) throws Throwable {
         expected = expected.replace("${currentdir}", currentDirectory.getAbsolutePath())
                 .toLowerCase();
         String actual = stdOut.toString().replaceAll("\n", "").trim().toLowerCase();
         assertEquals(expected, actual);
     }
 
     @Then("^the response should contain \"([^\"]*)\"$")
     public void the_response_should_contain(String expected) throws Throwable {
         String actual = stdOut.toString().replaceAll("\n", "");
         assertTrue(actual, actual.contains(expected));
     }
 
     @Then("^the response should not contain \"([^\"]*)\"$")
     public void the_response_should_not_contain(String expected) throws Throwable {
         String actual = stdOut.toString().replaceAll("\n", "");
         assertFalse(actual, actual.contains(expected));
     }
 
     @Then("^the response should contain ([^\"]*) lines$")
     public void the_response_should_contain_x_lines(int lines) throws Throwable {
         String[] lineStrings = stdOut.toString().split("\n");
         assertEquals(lines, lineStrings.length);
     }
 
     @Then("^the response should start with \"([^\"]*)\"$")
     public void the_response_should_start_with(String expected) throws Throwable {
         String actual = stdOut.toString().replaceAll("\n", "");
         assertTrue(actual, actual.startsWith(expected));
     }
 
     @Then("^the repository directory shall exist$")
     public void the_repository_directory_shall_exist() throws Throwable {
         List<String> output = runAndParseCommand("rev-parse", "--resolve-geogit-dir");
         assertEquals(output.toString(), 1, output.size());
         String location = output.get(0);
         assertNotNull(location);
         if (location.startsWith("Error:")) {
             fail(location);
         }
         File repoDir = new File(new URI(location));
         assertTrue("Repository directory not found: " + repoDir.getAbsolutePath(), repoDir.exists());
     }
 
     @Given("^I have a remote ref called \"([^\"]*)\"$")
     public void i_have_a_remote_ref_called(String expected) throws Throwable {
         String ref = "refs/remotes/origin/" + expected;
         geogit.command(UpdateRef.class).setName(ref).setNewValue(ObjectId.NULL).call();
         Optional<Ref> refValue = geogit.command(RefParse.class).setName(ref).call();
         assertTrue(refValue.isPresent());
         assertEquals(refValue.get().getObjectId(), ObjectId.NULL);
     }
 
     @Given("^I have an unconfigured repository$")
     public void I_have_an_unconfigured_repository() throws Throwable {
         setUpDirectories();
         setupGeogit();
 
         List<String> output = runAndParseCommand("init");
         assertEquals(output.toString(), 1, output.size());
         assertNotNull(output.get(0));
         assertTrue(output.get(0), output.get(0).startsWith("Initialized"));
     }
 
     @Given("^there is a remote repository$")
     public void there_is_a_remote_repository() throws Throwable {
         I_am_in_an_empty_directory();
         GeoGIT oldGeogit = geogit;
         Injector oldInjector = geogitCLI.getGeogitInjector();
         geogitCLI.setGeogitInjector(GlobalInjectorBuilder.builder.get());
         List<String> output = runAndParseCommand("init", "remoterepo");
         assertEquals(output.toString(), 1, output.size());
         assertNotNull(output.get(0));
         assertTrue(output.get(0), output.get(0).startsWith("Initialized"));
         geogit = geogitCLI.getGeogit();
         insertAndAdd(points1);
         runCommand(("commit -m Commit1").split(" "));
         runCommand(("branch -c branch1").split(" "));
         insertAndAdd(points2);
         runCommand(("commit -m Commit2").split(" "));
         insertAndAdd(points3);
         runCommand(("commit -m Commit3").split(" "));
         runCommand(("checkout master").split(" "));
         insertAndAdd(lines1);
         runCommand(("commit -m Commit4").split(" "));
         geogit.close();
         geogit = oldGeogit;
         geogitCLI.setGeogit(oldGeogit);
         geogitCLI.setGeogitInjector(oldInjector);
     }
 
     @Given("^I have a repository$")
     public void I_have_a_repository() throws Throwable {
         I_have_an_unconfigured_repository();
         runCommand("config", "--global", "user.name", "John Doe");
         runCommand("config", "--global", "user.email", "JohnDoe@example.com");
     }
 
     @Given("^I have a repository with a remote$")
     public void I_have_a_repository_with_a_remote() throws Throwable {
         there_is_a_remote_repository();
 
         List<String> output = runAndParseCommand("init", "localrepo");
         assertEquals(output.toString(), 1, output.size());
         assertNotNull(output.get(0));
         assertTrue(output.get(0), output.get(0).startsWith("Initialized"));
 
         runCommand("config", "--global", "user.name", "John Doe");
         runCommand("config", "--global", "user.email", "JohnDoe@example.com");
 
         runCommand("remote", "add", "origin", currentDirectory + "/remoterepo");
     }
 
     private void setUpDirectories() throws IOException {
         homeDirectory = new File("target", "fakeHomeDir");
         FileUtils.deleteDirectory(homeDirectory);
         assertFalse(homeDirectory.exists());
         assertTrue(homeDirectory.mkdirs());
 
         currentDirectory = new File("target", "testrepo");
         FileUtils.deleteDirectory(currentDirectory);
         assertFalse(currentDirectory.exists());
         assertTrue(currentDirectory.mkdirs());
     }
 
     private void deleteDirectories() {
         try {
             FileUtils.deleteDirectory(homeDirectory);
             assertFalse(homeDirectory.exists());
 
             FileUtils.deleteDirectory(currentDirectory);
             assertFalse(currentDirectory.exists());
         } catch (IOException e) {
 
         }
     }
 
     @Given("^I have 6 unstaged features$")
     public void I_have_6_unstaged_features() throws Throwable {
         insertFeatures();
     }
 
     @Given("^I stage 6 features$")
     public void I_stage_6_features() throws Throwable {
         insertAndAddFeatures();
     }
 
     @Given("^I have several commits")
     public void I_have_several_commits() throws Throwable {
         insertAndAdd(points1);
         insertAndAdd(points2);
         runCommand(("commit -m Commit1").split(" "));
         insertAndAdd(points3);
         insertAndAdd(lines1);
         runCommand(("commit -m Commit2").split(" "));
         insertAndAdd(lines2);
         insertAndAdd(lines3);
         runCommand(("commit -m Commit3").split(" "));
         insertAndAdd(points1_modified);
         runCommand(("commit -m Commit4").split(" "));
 
     }
 
     @Given("^I have several branches")
     public void I_have_several_branches() throws Throwable {
         insertAndAdd(points1);
         runCommand(("commit -m Commit1").split(" "));
         runCommand(("branch -c branch1").split(" "));
         insertAndAdd(points2);
         runCommand(("commit -m Commit2").split(" "));
         insertAndAdd(points3);
         runCommand(("commit -m Commit3").split(" "));
         runCommand(("branch -c branch2").split(" "));
         insertAndAdd(lines1);
         runCommand(("commit -m Commit4").split(" "));
         runCommand(("checkout master").split(" "));
         insertAndAdd(lines2);
         runCommand(("commit -m Commit5").split(" "));
     }
 
     @Given("I modify and add a feature")
     public void I_modify_and_add_a_feature() throws Throwable {
         insertAndAdd(points1_modified);
     }
 
     @Given("I modify a feature")
     public void I_modify_a_feature() throws Throwable {
         insert(points1_modified);
     }
 
     @Then("^if I change to the respository subdirectory \"([^\"]*)\"$")
     public void if_I_change_to_the_respository_subdirectory(String subdirSpec) throws Throwable {
         String[] subdirs = subdirSpec.split("/");
         File dir = currentDirectory;
         for (String subdir : subdirs) {
             dir = new File(dir, subdir);
         }
         assertTrue(dir.exists());
         currentDirectory = dir;
     }
 
     @Given("^I am inside a repository subdirectory \"([^\"]*)\"$")
     public void I_am_inside_a_repository_subdirectory(String subdirSpec) throws Throwable {
         String[] subdirs = subdirSpec.split("/");
         File dir = currentDirectory;
         for (String subdir : subdirs) {
             dir = new File(dir, subdir);
         }
         assertTrue(dir.mkdirs());
         currentDirectory = dir;
     }
 }
