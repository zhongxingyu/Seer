 package org.pinion.core.test;
 
 import com.google.common.io.CharStreams;
 import org.apache.commons.vfs2.FileSystemException;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.mockito.Mockito;
 import org.pinion.core.asset.Asset;
 import org.pinion.core.preprocessor.DirectiveProcessor;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.mockito.Mockito.when;
 
 @RunWith(JUnit4.class)
 public class DirectiveProcessorTest {
 
   DirectiveProcessor dp;
   Asset asset;
   List<String> dependencies;
 
   @Before
   public void init() {
     //Mock an asset with the needed fields
     asset = Mockito.mock(Asset.class);
     asset.fsPath = getClass().getResource("/directives").getPath();
     dependencies = new ArrayList<>();
     when(asset.getDependencyPaths()).thenReturn(dependencies);
     dp = new DirectiveProcessor();
   }
 
   private String testData(String name) {
     try {
       return CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/directives/" + name)));
     } catch (IOException e) {
       throw new Error(e);
     }
   }
 
   @Test
   public void doubleSlashTest() {
     try {
       dp.run(testData("double_slash"), asset);
     } catch (FileSystemException e) {
       fail();
     }
     assertThat(asset.getDependencyPaths(), hasItem("a"));
     assertThat(asset.getDependencyPaths(), hasItem("b"));
     assertThat(asset.getDependencyPaths(), hasItem("c"));
   }
 
   @Test
   public void slashStarTest() {
     try {
       dp.run(testData("slash_star"), asset);
     } catch (FileSystemException e) {
       fail();
     }
     assertThat(asset.getDependencyPaths(), hasItem("a"));
     assertThat(asset.getDependencyPaths(), hasItem("b"));
     assertThat(asset.getDependencyPaths(), hasItem("c"));
   }
 
   @Test
   public void slashStarSingleTest() {
     try {
       dp.run(testData("slash_star_single"), asset);
     } catch (FileSystemException e) {
       fail();
     }
     assertThat(asset.getDependencyPaths(), hasItem("a"));
 
   }
 
   @Test
   public void commentsWithoutDirectives() {
     try {
       dp.run(testData("comment_without_directives"), asset);
     } catch (FileSystemException e) {
       fail();
     }
     assertTrue(dependencies.isEmpty());
 
   }
 
   @Test
   public void directivesInCommentsAfterTheHeaderAreNotProcessed() {
     try {
       dp.run(testData("directives_after_header"), asset);
     } catch (FileSystemException e) {
       fail();
     }
     assertThat(asset.getDependencyPaths(), hasItem("a"));
     assertThat(asset.getDependencyPaths(), hasItem("b"));
     assertThat(asset.getDependencyPaths(), hasItem("c"));
     assertThat(asset.getDependencyPaths(), hasItem("d"));
     assertThat(asset.getDependencyPaths(), not(hasItem("e")));
 
   }
 
   @Test
    public void headerMustOccurAtTheStartOfTheFile() {
     try {
       String input = testData("code_before_comment");
       String output = dp.run(input, asset);
       assertEquals(input, output);
     } catch (FileSystemException e) {
       fail();
     }
     assertTrue(dependencies.isEmpty());
 
   }
 
   @Test
   public void noHeader() {
     try {
       String input = testData("no_header");
       String output = dp.run(input, asset);
       assertEquals(input, output);
     } catch (FileSystemException e) {
       fail();
     }
     assertTrue(dependencies.isEmpty());
 
   }
 
  @Test
  public void resolvesLogicalPath() {
    try {
      String input = testData("nested_path");
      String output = dp.run(input, asset);
      assertEquals(input, output);
    } catch (FileSystemException e) {
      fail();
    }
    assertThat(asset.getDependencyPaths(), hasItem("js/a"));
    assertThat(asset.getDependencyPaths(), hasItem("app/js/b"));
    assertThat(asset.getDependencyPaths(), hasItem("c"));
  }

 }
