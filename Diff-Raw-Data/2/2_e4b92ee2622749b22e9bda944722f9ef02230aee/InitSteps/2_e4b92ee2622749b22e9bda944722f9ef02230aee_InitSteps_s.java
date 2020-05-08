 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
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
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.commons.io.FileUtils;
 import org.geogit.api.GeoGIT;
 import org.geogit.api.GlobalInjectorBuilder;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.plumbing.RefParse;
 import org.geogit.api.plumbing.UpdateRef;
 import org.geogit.api.plumbing.diff.AttributeDiff;
 import org.geogit.api.plumbing.diff.FeatureDiff;
 import org.geogit.api.plumbing.diff.GenericAttributeDiffImpl;
 import org.geogit.api.plumbing.diff.Patch;
 import org.geogit.api.plumbing.diff.PatchSerializer;
 import org.geogit.api.plumbing.merge.MergeConflictsException;
 import org.geogit.api.porcelain.BranchCreateOp;
 import org.geogit.api.porcelain.CheckoutOp;
 import org.geogit.api.porcelain.CommitOp;
 import org.geogit.api.porcelain.MergeOp;
 import org.opengis.feature.Feature;
 import org.opengis.feature.type.PropertyDescriptor;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Optional;
 import com.google.common.base.Suppliers;
 import com.google.common.collect.Maps;
 import com.google.common.io.Files;
 import com.google.inject.Injector;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
 
 /**
  *
  */
 public class InitSteps extends AbstractGeogitFunctionalTest {
 
     private static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
     @cucumber.annotation.After
     public void after() {
         if (GlobalState.geogitCLI != null) {
             GlobalState.geogitCLI.close();
         }
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
         for (int i = 0; i < args.length; i++) {
             args[i] = args[i].replace("${currentdir}", currentDirectory.getAbsolutePath());
         }
         runCommand(args);
     }
 
     @Then("^it should answer \"([^\"]*)\"$")
     public void it_should_answer_exactly(String expected) throws Throwable {
         expected = expected.replace("${currentdir}", currentDirectory.getAbsolutePath())
                 .toLowerCase().replaceAll("\\\\", "/");
         String actual = stdOut.toString().replaceAll(LINE_SEPARATOR, "").replaceAll("\\\\", "/")
                 .trim().toLowerCase();
         assertEquals(expected, actual);
     }
 
     @Then("^the response should contain \"([^\"]*)\"$")
     public void the_response_should_contain(String expected) throws Throwable {
         String actual = stdOut.toString().replaceAll(LINE_SEPARATOR, "").replaceAll("\\\\", "/");
         expected.replaceAll("\\\\", "/");
         assertTrue(actual, actual.contains(expected));
     }
 
     @Then("^the response should not contain \"([^\"]*)\"$")
     public void the_response_should_not_contain(String expected) throws Throwable {
         String actual = stdOut.toString().replaceAll(LINE_SEPARATOR, "").replaceAll("\\\\", "/");
         expected.replaceAll("\\\\", "/");
         assertFalse(actual, actual.contains(expected));
     }
 
     @Then("^the response should contain ([^\"]*) lines$")
     public void the_response_should_contain_x_lines(int lines) throws Throwable {
         String[] lineStrings = stdOut.toString().split(LINE_SEPARATOR);
         assertEquals(lines, lineStrings.length);
     }
 
     @Then("^the response should start with \"([^\"]*)\"$")
     public void the_response_should_start_with(String expected) throws Throwable {
         String actual = stdOut.toString().replaceAll(LINE_SEPARATOR, "");
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
 
     @Given("^I have a merge conflict state$")
     public void I_have_a_merge_conflict_state() throws Throwable {
         I_have_two_conflicting_branches();
         Ref branch = geogit.command(RefParse.class).setName("branch1").call().get();
         try {
             geogit.command(MergeOp.class).addCommit(Suppliers.ofInstance(branch.getObjectId()))
                     .call();
             fail();
         } catch (MergeConflictsException e) {
             e = e;
         }
     }
 
     @Given("^I have two conflicting branches$")
     public void I_have_two_conflicting_branches() throws Throwable {
         // Create the following revision graph
         // o
         // |
         // o - Points 1 added
         // |\
         // | o - TestBranch - Points 1 modified and points 2 added
         // |
         // o - master - HEAD - Points 1 modifiedB
         Feature points1ModifiedB = feature(pointsType, idP1, "StringProp1_3", new Integer(2000),
                 "POINT(1 1)");
         Feature points1Modified = feature(pointsType, idP1, "StringProp1_2", new Integer(1000),
                 "POINT(1 1)");
         insertAndAdd(points1);
         geogit.command(CommitOp.class).call();
         geogit.command(BranchCreateOp.class).setName("branch1").call();
         insertAndAdd(points1Modified);
         geogit.command(CommitOp.class).call();
         geogit.command(CheckoutOp.class).setSource("branch1").call();
         insertAndAdd(points1ModifiedB);
         insertAndAdd(points2);
         geogit.command(CommitOp.class).call();
 
         geogit.command(CheckoutOp.class).setSource("master").call();
     }
 
     @Given("^there is a remote repository$")
     public void there_is_a_remote_repository() throws Throwable {
         I_am_in_an_empty_directory();
         GeoGIT oldGeogit = geogit;
         Injector oldInjector = geogitCLI.getGeogitInjector();
         geogitCLI.setGeogitInjector(GlobalInjectorBuilder.builder.build());
         List<String> output = runAndParseCommand("init", "remoterepo");
         assertEquals(output.toString(), 1, output.size());
         assertNotNull(output.get(0));
         assertTrue(output.get(0), output.get(0).startsWith("Initialized"));
         geogit = geogitCLI.getGeogit();
         runCommand("config", "--global", "user.name", "John Doe");
         runCommand("config", "--global", "user.email", "JohnDoe@example.com");
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
         homeDirectory = new File("target", "fakeHomeDir" + new Random().nextInt());
         FileUtils.deleteDirectory(homeDirectory);
         assertFalse(homeDirectory.exists());
         assertTrue(homeDirectory.mkdirs());
 
         currentDirectory = new File("target", "testrepo" + new Random().nextInt());
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
 
     @Given("^I have staged \"([^\"]*)\"$")
     public void I_have_staged(String feature) throws Throwable {
         if (feature.equals("points1")) {
             insertAndAdd(points1);
         } else if (feature.equals("points2")) {
             insertAndAdd(points2);
         } else if (feature.equals("points3")) {
             insertAndAdd(points3);
         } else if (feature.equals("points1_modified")) {
             insertAndAdd(points1_modified);
         } else if (feature.equals("lines1")) {
             insertAndAdd(lines1);
         } else if (feature.equals("lines2")) {
             insertAndAdd(lines2);
         } else if (feature.equals("lines3")) {
             insertAndAdd(lines3);
         } else {
             throw new Exception("Unknown Feature");
         }
     }
 
     @Given("^I have 6 unstaged features$")
     public void I_have_6_unstaged_features() throws Throwable {
         insertFeatures();
     }
 
     @Given("^I have unstaged \"([^\"]*)\"$")
     public void I_have_unstaged(String feature) throws Throwable {
         if (feature.equals("points1")) {
             insert(points1);
         } else if (feature.equals("points2")) {
             insert(points2);
         } else if (feature.equals("points3")) {
             insert(points3);
         } else if (feature.equals("points1_modified")) {
             insert(points1_modified);
         } else if (feature.equals("lines1")) {
             insert(lines1);
         } else if (feature.equals("lines2")) {
             insert(lines2);
         } else if (feature.equals("lines3")) {
             insert(lines3);
         } else {
             throw new Exception("Unknown Feature");
         }
     }
 
     @Given("^I stage 6 features$")
     public void I_stage_6_features() throws Throwable {
         insertAndAddFeatures();
     }
 
     @Given("^I have several commits$")
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
 
     @Given("^I modify a feature type$")
     public void I_modify_a_feature_type() throws Throwable {
         deleteAndReplaceFeatureType();
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
 
     @Given("^I have a patch file$")
     public void I_have_a_patch_file() throws Throwable {
         Patch patch = new Patch();
         String path = NodeRef.appendChild(pointsName, points1.getIdentifier().getID());
         Map<PropertyDescriptor, AttributeDiff> map = Maps.newHashMap();
         Optional<?> oldValue = Optional.fromNullable(points1.getProperty("sp").getValue());
         GenericAttributeDiffImpl diff = new GenericAttributeDiffImpl(oldValue, Optional.of("new"));
         map.put(pointsType.getDescriptor("sp"), diff);
         FeatureDiff feaureDiff = new FeatureDiff(path, map, RevFeatureType.build(pointsType),
                 RevFeatureType.build(pointsType));
         patch.addModifiedFeature(feaureDiff);
         File file = new File(currentDirectory, "test.patch");
         BufferedWriter writer = Files.newWriter(file, Charsets.UTF_8);
         PatchSerializer.write(writer, patch);
         writer.flush();
         writer.close();
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
