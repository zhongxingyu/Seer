 package org.jboss.pressgang.ccms.contentspec.client.commands;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static org.junit.matchers.JUnitMatchers.containsString;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyBoolean;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.internal.Console;
 import net.sf.ipsedixit.annotation.Arbitrary;
 import org.apache.commons.io.FileUtils;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.client.BaseUnitTest;
 import org.jboss.pressgang.ccms.contentspec.client.config.ClientConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.config.ContentSpecConfiguration;
 import org.jboss.pressgang.ccms.contentspec.client.utils.ClientUtilities;
 import org.jboss.pressgang.ccms.contentspec.processor.ContentSpecParser;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.utils.common.FileUtilities;
 import org.jboss.pressgang.ccms.utils.common.ZipUtilities;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.contrib.java.lang.system.ExpectedSystemExit;
 import org.junit.contrib.java.lang.system.internal.CheckExitCalled;
 import org.mockito.Mock;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.rule.PowerMockRule;
 
 @PrepareForTest({RESTProviderFactory.class, FileUtilities.class, ZipUtilities.class, ClientUtilities.class})
 public class AssembleCommandTest extends BaseUnitTest {
     private static final String BOOK_TITLE = "Test";
     private static final String DUMMY_BUILD_FILE_NAME = "Test.zip";
     private static final String DUMMY_PROJECT_BUILD_FILE_NAME = "Test-publican.zip";
 
     @Rule public PowerMockRule rule = new PowerMockRule();
     @Rule public final ExpectedSystemExit exit = ExpectedSystemExit.none();
 
     @Arbitrary Integer id;
     @Arbitrary String randomString;
     @Mock JCommander parser;
     @Mock ContentSpecConfiguration cspConfig;
     @Mock ClientConfiguration clientConfig;
     @Mock RESTProviderFactory providerFactory;
     @Mock ContentSpecProvider contentSpecProvider;
     @Mock ContentSpecWrapper contentSpecWrapper;
     @Mock UpdateableCollectionWrapper<CSNodeWrapper> contentSpecChildren;
     @Mock ContentSpec contentSpec;
 
     AssembleCommand command;
     File rootTestDirectory;
     File bookDir;
     File projectBookDir;
     File emptyFile;
     File projectEmptyFile;
 
     @Before
     public void setUp() throws IOException {
         bindStdOut();
         PowerMockito.mockStatic(RESTProviderFactory.class);
         when(RESTProviderFactory.create(anyString())).thenReturn(providerFactory);
         when(providerFactory.getProvider(ContentSpecProvider.class)).thenReturn(contentSpecProvider);
         command = new AssembleCommand(parser, cspConfig, clientConfig);
 
         // Only test the assemble command and not the build command content.
         command.setNoBuild(true);
 
         rootTestDirectory = FileUtils.toFile(ClassLoader.getSystemResource(""));
         when(cspConfig.getRootOutputDirectory()).thenReturn(rootTestDirectory.getAbsolutePath() + File.separator);
 
         // Make the book title directory
         bookDir = new File(rootTestDirectory, BOOK_TITLE);
         bookDir.mkdir();
 
         // Make a empty file in that directory
         emptyFile = new File(bookDir, DUMMY_BUILD_FILE_NAME);
         emptyFile.createNewFile();
 
         // Make the project book directory
         projectBookDir = new File(rootTestDirectory, BOOK_TITLE + File.separator + "assembly");
         projectBookDir.mkdirs();
 
         // Make a empty file in that directory
         projectEmptyFile = new File(projectBookDir, DUMMY_PROJECT_BUILD_FILE_NAME);
         projectEmptyFile.createNewFile();
     }
 
     @Test
     public void shouldFailWithNoIds() {
         // Given a command with no ids
         command.setIds(new ArrayList<String>());
         // and no csprocessor.cfg data
         given(cspConfig.getContentSpecId()).willReturn(null);
 
         // When it is processed
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(5));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("No ID was specified by the command line or a csprocessor.cfg file."));
     }
 
     @Test
     public void shouldFailWhenNoValidContentSpecs() {
         // Given a command with ids
         command.setIds(Arrays.asList(id.toString()));
         // and the provider will return a wrapper, that has no children
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(true);
 
         // When the command is processing
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("No valid version exists on the server, please fix any errors and try again."));
     }
 
     @Test
     public void shouldShutdownWhenBuildFileDoesntExist() {
         PowerMockito.mockStatic(ClientUtilities.class);
         PowerMockito.mockStatic(FileUtilities.class);
         // Given a command with ids
         command.setIds(Arrays.asList(rootTestDirectory.getAbsolutePath()));
         // and the content spec file will be found
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the content spec parses
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(contentSpec);
         // and the content spec returns a title and id
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpec.getId()).willReturn(id);
         // and the fix file path method returns something
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
         when(ClientUtilities.fixDirectoryPath(anyString())).thenCallRealMethod();
 
         // When processing the command
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("Unable to assemble the Content Specification because the \"" + DUMMY_BUILD_FILE_NAME +
                 "\" file couldn't be found."));
     }
 
     @Test
     public void shouldShutdownWhenUnableToUnzipBuild() {
         PowerMockito.mockStatic(ClientUtilities.class);
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.mockStatic(ZipUtilities.class);
         // Given a command with ids
         command.setIds(Arrays.asList(rootTestDirectory.getAbsolutePath()));
         // and the build file information has been set
         command.setOutputPath(rootTestDirectory.getAbsolutePath() + File.separator + BOOK_TITLE);
         // and the file will be found
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the content spec parses
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(contentSpec);
         // and the content spec returns a title and id
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpec.getId()).willReturn(id);
         // and the fix file path method returns something
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
         when(ClientUtilities.fixDirectoryPath(anyString())).thenCallRealMethod();
         // and the unzip fails
         when(ZipUtilities.unzipFileIntoDirectory(any(File.class), anyString())).thenReturn(false);
 
         // When processing the command
         try {
             command.process();
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("The content specification failed to be assembled."));
     }
 
     @Test
     public void shouldOnlyPrintSuccessWhenUnzipBuildAndNoPublicanBuild() {
         PowerMockito.mockStatic(ClientUtilities.class);
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.mockStatic(ZipUtilities.class);
         final String rootPath = rootTestDirectory.getAbsolutePath();
         // Given a command with ids
         command.setIds(Arrays.asList(rootPath));
         // and the build file information has been set
         command.setOutputPath(rootPath + File.separator + BOOK_TITLE);
         // and the file will be found
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the content spec parses
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(contentSpec);
         // and the content spec returns a title and id
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpec.getId()).willReturn(id);
         // and the fix file path method returns something
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
         when(ClientUtilities.fixDirectoryPath(anyString())).thenCallRealMethod();
         // and no publican build
         command.setNoPublicanBuild(true);
         // and the unzip succeeds
         when(ZipUtilities.unzipFileIntoDirectory(any(File.class), anyString())).thenReturn(true);
 
         // When processing the command
         command.process();
 
         // Then the command printed a success message and the runPublican method wasn't executed
         assertThat(getStdOutLogs(), containsString("Content Specification build unzipped to " + rootPath + File.separator + BOOK_TITLE));
     }
 
     @Test
     public void shouldPrintSuccessAndRunPublican() throws IOException {
         PowerMockito.mockStatic(ClientUtilities.class);
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.mockStatic(ZipUtilities.class);
         final String rootPath = rootTestDirectory.getAbsolutePath();
         // Given a command with ids
         command.setIds(Arrays.asList(rootPath));
         // and the build file information has been set
         command.setOutputPath(rootPath + File.separator + BOOK_TITLE);
         // and the content spec file will be found
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the content spec parses
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(contentSpec);
         // and the content spec returns a title and id
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpec.getId()).willReturn(id);
         // and the fix file path method returns something
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
         when(ClientUtilities.fixDirectoryPath(anyString())).thenCallRealMethod();
         // and the unzip succeeds
         when(ZipUtilities.unzipFileIntoDirectory(any(File.class), anyString())).thenReturn(true);
         // and the publican command will execute successfully
         when(ClientUtilities.runCommand(anyString(), any(File.class), any(Console.class), anyBoolean(), anyBoolean())).thenReturn(0);
 
         // When processing the command
         command.process();
 
         // Then the command printed a success message and the runPublican method wasn't executed
         assertThat(getStdOutLogs(), containsString("Content Specification build unzipped to " + rootPath + File.separator + BOOK_TITLE));
         assertThat(getStdOutLogs(),
                 containsString("Content Specification successfully assembled at " + rootPath + File.separator + BOOK_TITLE));
     }
 
     @Test
     public void shouldSuccessfullyRunFromCsprocessorCfg() throws IOException {
         PowerMockito.mockStatic(ClientUtilities.class);
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.mockStatic(ZipUtilities.class);
         final String rootPath = rootTestDirectory.getAbsolutePath();
         // Given a command with no ids and a csprocessor.cfg
         given(cspConfig.getContentSpecId()).willReturn(id);
         // and the content spec file will be found
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         given(contentSpecWrapper.getTitle()).willReturn(BOOK_TITLE);
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         when(ClientUtilities.getOutputRootDirectory(eq(cspConfig), eq(contentSpecWrapper))).thenReturn(bookDir.getAbsolutePath
                 () + File.separator);
         // and the fix file path method returns something
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
         when(ClientUtilities.fixDirectoryPath(anyString())).thenCallRealMethod();
         // and the unzip succeeds
         when(ZipUtilities.unzipFileIntoDirectory(any(File.class), anyString())).thenReturn(true);
         // and the publican command will execute successfully
         when(ClientUtilities.runCommand(anyString(), any(File.class), any(Console.class), anyBoolean(), anyBoolean())).thenReturn(0);
 
         // When processing the command
         command.process();
 
         // Then the command printed a success message and the runPublican method wasn't executed
         assertThat(getStdOutLogs(), containsString("Content Specification build unzipped to " + rootPath + File.separator + BOOK_TITLE));
         assertThat(getStdOutLogs(),
                 containsString("Content Specification successfully assembled at " + rootPath + File.separator + BOOK_TITLE));
     }
 
     @Test
     public void shouldShutdownWhenContentSpecNotFound() {
         // Given a command called with an an ID
         command.setIds(Arrays.asList(id.toString()));
         // And no matching content spec
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(null);
 
         // When finding files
         try {
             command.findBuildDirectoryAndFiles(contentSpecProvider, false);
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("No data was found for the specified ID!"));
     }
 
     @Test
     public void shouldShutdownWhenContentSpecFileNotFound() {
         // Given a command called with a file
         command.setIds(Arrays.asList(rootTestDirectory.getAbsolutePath()));
         // And no file
         PowerMockito.mockStatic(FileUtilities.class);
         when(FileUtilities.readFileContents(any(File.class))).thenReturn("");
 
         // When it finding files
         try {
             command.findBuildDirectoryAndFiles(contentSpecProvider, false);
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should be shutdown and an error message printed
         assertThat(getStdOutLogs(), containsString("The specified file was empty!"));
     }
 
     @Test
     public void shouldSetBuildFileLocationsWhenLoadingFromCsprocessorCfg() {
         // Given a command with no ids
         command.setIds(new ArrayList<String>());
         given(cspConfig.getContentSpecId()).willReturn(id);
         // and the provider will return a wrapper
         given(contentSpecProvider.getContentSpec(anyInt(), anyInt())).willReturn(contentSpecWrapper);
         given(contentSpecWrapper.getChildren()).willReturn(contentSpecChildren);
         given(contentSpecChildren.isEmpty()).willReturn(false);
         // and the wrapper returns a title and id
         given(contentSpecWrapper.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpecWrapper.getId()).willReturn(id);
 
         // When the command is finding the files
         command.findBuildDirectoryAndFiles(contentSpecProvider, true);
 
         // Then the command should have the build information set
         assertThat(command.getBuildFileDirectory(), containsString("assembly" + File.separator));
         assertThat(command.getOutputDirectory(), containsString("assembly" + File.separator + "publican" + File.separator));
         assertThat(command.getBuildFileName(), containsString(BOOK_TITLE + "-publican.zip"));
     }
 
     @Test
     public void shouldSetBuildFileLocationsWhenFileSpecifiedAndNoOutputPath() {
         PowerMockito.mockStatic(FileUtilities.class);
         PowerMockito.mockStatic(ClientUtilities.class);
         // Given a command with a file
         command.setIds(Arrays.asList(rootTestDirectory.getAbsolutePath()));
         // and the content spec file will be found
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the content spec parses
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(contentSpec);
         // and the content spec returns a title and id
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpec.getId()).willReturn(id);
         // and the fix file path method returns something
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
         when(ClientUtilities.fixDirectoryPath(anyString())).thenCallRealMethod();
         // and the content spec returns a title and id
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpec.getId()).willReturn(id);
 
         // When the command is finding the files
         command.findBuildDirectoryAndFiles(contentSpecProvider, false);
 
         // Then the command should have the build information set
         assertThat(command.getBuildFileDirectory(), is(""));
         assertThat(command.getOutputDirectory(), is(BOOK_TITLE));
         assertThat(command.getBuildFileName(), containsString(BOOK_TITLE + ".zip"));
     }
 
     @Test
     public void shouldSetBuildFileLocationsWhenFileSpecifiedAndOutputDirectory() {
         final String rootPath = rootTestDirectory.getAbsolutePath() + File.separator;
         // Given a command with ids
         command.setIds(Arrays.asList(rootPath));
         // and the file will be found
         PowerMockito.mockStatic(FileUtilities.class);
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the output path is a directory
         command.setOutputPath(rootPath);
         // and the content spec parses
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(contentSpec);
         // and the fix file path method returns something
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
         when(ClientUtilities.fixDirectoryPath(anyString())).thenCallRealMethod();
         // and the content spec returns a title and id
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpec.getId()).willReturn(id);
 
         // When the command is finding the files
         command.findBuildDirectoryAndFiles(contentSpecProvider, false);
 
         // Then the command should have the build information set
         assertThat(command.getBuildFileDirectory(), is(rootPath));
         assertThat(command.getOutputDirectory(), is(rootPath + BOOK_TITLE));
         assertThat(command.getBuildFileName(), containsString(BOOK_TITLE + ".zip"));
     }
 
     @Test
     public void shouldSetBuildFileLocationsWhenFileSpecifiedAndOutputFile() {
         final String rootPath = rootTestDirectory.getAbsolutePath() + File.separator;
         // Given a command with ids
         command.setIds(Arrays.asList(rootPath));
         // and the file will be found
         PowerMockito.mockStatic(FileUtilities.class);
         when(FileUtilities.readFileContents(any(File.class))).thenReturn(randomString);
         // and the output path is a directory
         command.setOutputPath(rootPath + DUMMY_BUILD_FILE_NAME);
         // and the content spec parses
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.parseContentSpecString(eq(providerFactory), any(ErrorLoggerManager.class), anyString(),
                 any(ContentSpecParser.ParsingMode.class), anyBoolean())).thenReturn(contentSpec);
         // and the fix file path method returns something
         when(ClientUtilities.fixFilePath(anyString())).thenCallRealMethod();
         // and the content spec returns a title and id
         given(contentSpec.getTitle()).willReturn(BOOK_TITLE);
         given(contentSpec.getId()).willReturn(id);
 
         // When the command is finding the files
         command.findBuildDirectoryAndFiles(contentSpecProvider, false);
 
         // Then the command should have the build information set
         assertThat(command.getBuildFileDirectory(), is(""));
         assertThat(command.getOutputDirectory(), is(rootPath + BOOK_TITLE));
         assertThat(command.getBuildFileName(), containsString(BOOK_TITLE + ".zip"));
     }
 
     @Test
     public void shouldShutdownWhenRunPublicanFails() throws IOException {
         int exitCode = 1;
         // Given publican build options
         clientConfig.setPublicanBuildOptions("--formats html-single --langs en-US");
         // and the publican command fails with an exit code other than 0
         PowerMockito.mockStatic(ClientUtilities.class);
         when(ClientUtilities.runCommand(anyString(), any(File.class), any(Console.class), anyBoolean(), anyBoolean())).thenReturn(exitCode);
 
         // When running publican
         try {
             command.runPublican(rootTestDirectory);
             // Then an error is printed and the program is shut down
             fail(SYSTEM_EXIT_ERROR);
         } catch (CheckExitCalled e) {
             assertThat(e.getStatus(), is(-1));
         }
 
         // Then the command should shutdown and an error be printed
         assertThat(getStdOutLogs(), containsString("Unable to assemble the Content Specification because an error " +
                 "occurred while running Publican. (exit code: " + exitCode + ")"));
     }
 
     @Test
     public void shouldRequireAnExternalConnection() {
         // Given an already initialised command
 
         // When invoking the method
         boolean result = command.requiresExternalConnection();
 
         // Then the answer should be true
         assertTrue(result);
     }
 
     @Test
     public void shouldNotLoadFromCsprocessorCfgWhenIdsSpecified() {
         // Given a command with ids specified
         command.setIds(Arrays.asList(id.toString()));
 
         // When invoking the method
         boolean result = command.loadFromCSProcessorCfg();
 
         // Then the result should be false
         assertFalse(result);
     }
 
     @Test
     public void shouldReturnRightCommandName() {
         // Given
         // When getting the commands name
         String commandName = command.getCommandName();
 
         // Then the name should be "assemble"
         assertThat(commandName, is("assemble"));
     }
 
     @After
     public void cleanUp() throws IOException {
         FileUtils.deleteDirectory(bookDir);
         FileUtils.deleteDirectory(projectBookDir);
     }
 }
