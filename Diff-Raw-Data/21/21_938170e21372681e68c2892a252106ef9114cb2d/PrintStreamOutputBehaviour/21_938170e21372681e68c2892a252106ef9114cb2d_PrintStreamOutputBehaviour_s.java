 package org.jbehave.core.reporters;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.is;
 import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
 import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.Properties;
 
 import org.apache.commons.io.IOUtils;
 import org.jbehave.core.i18n.LocalizedKeywords;
 import org.jbehave.core.io.CodeLocations;
 import org.jbehave.core.io.StoryLocation;
 import org.jbehave.core.io.StoryPathResolver;
 import org.jbehave.core.io.UnderscoredCamelCaseResolver;
 import org.jbehave.core.junit.JUnitStory;
 import org.jbehave.core.model.Description;
 import org.jbehave.core.model.ExamplesTable;
 import org.jbehave.core.model.Narrative;
 import org.jbehave.core.model.OutcomesTable;
 import org.jbehave.core.model.Scenario;
 import org.jbehave.core.model.Story;
 import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
 import org.jbehave.core.reporters.FilePrintStreamFactory.FileConfiguration;
 import org.jbehave.core.reporters.FreemarkerViewGenerator.ViewGenerationFailedForTemplate;
import org.junit.Assert;
 import org.junit.Test;
 
 public class PrintStreamOutputBehaviour {
 
     @Test
     public void shouldReportEventsToTxtOutput() {
         // Given
         OutputStream out = new ByteArrayOutputStream();
         StoryReporter reporter = new TxtOutput(new PrintStream(out));
 
         // When
         narrateAnInterestingStory(reporter);
 
         // Then
         String expected = "An interesting story\n"
                 + "(/path/to/story)\n"
                 + "Narrative:\n"
                 + "In order to renovate my house\n"
                 + "As a customer\n"
                 + "I want to get a loan\n"
                 + "Scenario: I ask for a loan\n"
                 + "GivenStories: [/given/story1,/given/story2]\n"
                 + "Given I have a balance of $50\n"
                 + "!-- A comment\n"
                 + "When I request $20\n"
                 + "When I ask Liz for a loan of $100\n"
                 + "Then I should have a balance of $30 (PENDING)\n"
                 + "Then I should have $20 (NOT PERFORMED)\n"
                 + "Then I don't return loan (FAILED)\n"
                 + "(org.jbehave.core.model.OutcomesTable$OutcomesFailed)\n" 
                 + "|Description|Value|Matcher|Verified|\n"
                 + "|I don't return all|100|<50.0>|false|\n"                
                 + "Examples:\n"
                 + "Given money <money>\n"
                 + "Then I give it to <to>\n"
                 + "\n"
                 + "|money|to|\n" + "|$30|Mauro|\n"
                 + "|$50|Paul|\n" + "\n\n" // Examples table
                 + "\nExample: {to=Mauro, money=$30}\n"
                 + "\nExample: {to=Paul, money=$50}\n"
                 + "\n" // end of examples
                 + "\n\n"; // end of scenario and story
         assertThatOutputIs(out, expected);
     }
 
     @Test
     public void shouldReportEventsToHtmlOutput() {
         // Given
         final OutputStream out = new ByteArrayOutputStream();
         PrintStreamFactory factory = new PrintStreamFactory() {
 
             public PrintStream createPrintStream() {
                 return new PrintStream(out);
             }
         };
         StoryReporter reporter = new HtmlOutput(factory.createPrintStream());
 
         // When
         narrateAnInterestingStory(reporter);
 
         // Then
         String expected = "<div class=\"story\">\n<h1>An interesting story</h1>\n"
                 + "<div class=\"path\">/path/to/story</div>\n"
                 + "<div class=\"narrative\"><h2>Narrative:</h2>\n"
                 + "<div class=\"element inOrderTo\"><span class=\"keyword inOrderTo\">In order to</span> renovate my house</div>\n"
                 + "<div class=\"element asA\"><span class=\"keyword asA\">As a</span> customer</div>\n"
                 + "<div class=\"element iWantTo\"><span class=\"keyword iWantTo\">I want to</span> get a loan</div>\n"
                 + "</div>\n"
                 + "<div class=\"scenario\">\n<h2>Scenario: I ask for a loan</h2>\n"
                 + "<div class=\"givenStories\">GivenStories: [/given/story1,/given/story2]</div>\n"
                 + "<div class=\"step successful\">Given I have a balance of $50</div>\n"
                 + "<div class=\"step ignorable\">!-- A comment</div>\n"
                 + "<div class=\"step successful\">When I request $20</div>\n"
                 + "<div class=\"step successful\">When I ask Liz for a loan of $100</div>\n"
                 + "<div class=\"step pending\">Then I should have a balance of $30 <span class=\"keyword pending\">(PENDING)</span></div>\n"
                 + "<div class=\"step notPerformed\">Then I should have $20 <span class=\"keyword notPerformed\">(NOT PERFORMED)</span></div>\n"
                 + "<div class=\"step failed\">Then I don't return loan <span class=\"keyword failed\">(FAILED)</span><br/><span class=\"message failed\">org.jbehave.core.model.OutcomesTable$OutcomesFailed</span></div>\n"
                 + "<div class=\"outcomes\"><table>\n"
                 + "<thead>\n"
                 + "<tr>\n"
                 + "<th>Description</th><th>Value</th><th>Matcher</th><th>Verified</th></tr>\n"
                 + "</thead>\n"
                 + "<tbody>\n"
                 + "<tr class=\"notVerified\">\n"
                 + "<td>I don't return all</td><td>100.0</td><td>&lt;50.0&gt;</td><td>false</td></tr>\n"
                 + "</tbody>\n"
                 + "</table></div>\n"                
                 + "<div class=\"examples\">\n" + "<h3>Examples:</h3>\n"
                 + "<div class=\"step\">Given money &lt;money&gt;</div>\n"
                 + "<div class=\"step\">Then I give it to &lt;to&gt;</div>\n"
                 + "<table>\n" + "<thead>\n"
                 + "<tr>\n<th>money</th><th>to</th></tr>\n" + "</thead>\n" + "<tbody>\n"
                 + "<tr>\n<td>$30</td><td>Mauro</td></tr>\n" + "<tr>\n<td>$50</td><td>Paul</td></tr>\n" + "</tbody>\n"
                 + "</table>\n" + "\n<h3 class=\"example\">Example: {to=Mauro, money=$30}</h3>\n"
                 + "\n<h3 class=\"example\">Example: {to=Paul, money=$50}</h3>\n" + "</div>\n" + // end
                 // of
                 // examples
                 "</div>\n</div>\n"; // end of scenario and story
         assertThatOutputIs(out, expected);
     }
 
     @Test
     public void shouldReportEventsToHtmlOutputUsingCustomPatterns() {
         // Given
         final OutputStream out = new ByteArrayOutputStream();
         PrintStreamFactory factory = new PrintStreamFactory() {
 
             public PrintStream createPrintStream() {
                 return new PrintStream(out);
             }
         };
         Properties patterns = new Properties();
         patterns.setProperty("afterStory", "</div><!-- after story -->\n");
         patterns.setProperty("afterScenario", "</div><!-- after scenario -->\n");
         patterns.setProperty("afterExamples", "</div><!-- after examples -->\n");
         StoryReporter reporter = new HtmlOutput(factory.createPrintStream(), patterns);
 
         // When
         narrateAnInterestingStory(reporter);
 
         // Then
         String expected = "<div class=\"story\">\n<h1>An interesting story</h1>\n"
                 + "<div class=\"path\">/path/to/story</div>\n"
                 + "<div class=\"narrative\"><h2>Narrative:</h2>\n"
                 + "<div class=\"element inOrderTo\"><span class=\"keyword inOrderTo\">In order to</span> renovate my house</div>\n"
                 + "<div class=\"element asA\"><span class=\"keyword asA\">As a</span> customer</div>\n"
                 + "<div class=\"element iWantTo\"><span class=\"keyword iWantTo\">I want to</span> get a loan</div>\n"
                 + "</div>\n"
                 + "<div class=\"scenario\">\n<h2>Scenario: I ask for a loan</h2>\n"
                 + "<div class=\"givenStories\">GivenStories: [/given/story1,/given/story2]</div>\n"
                 + "<div class=\"step successful\">Given I have a balance of $50</div>\n"
                 + "<div class=\"step ignorable\">!-- A comment</div>\n"
                 + "<div class=\"step successful\">When I request $20</div>\n"
                 + "<div class=\"step successful\">When I ask Liz for a loan of $100</div>\n"
                 + "<div class=\"step pending\">Then I should have a balance of $30 <span class=\"keyword pending\">(PENDING)</span></div>\n"
                 + "<div class=\"step notPerformed\">Then I should have $20 <span class=\"keyword notPerformed\">(NOT PERFORMED)</span></div>\n"
                 + "<div class=\"step failed\">Then I don't return loan <span class=\"keyword failed\">(FAILED)</span><br/><span class=\"message failed\">org.jbehave.core.model.OutcomesTable$OutcomesFailed</span></div>\n"
                 + "<div class=\"outcomes\"><table>\n"
                 + "<thead>\n"
                 + "<tr>\n"
                 + "<th>Description</th><th>Value</th><th>Matcher</th><th>Verified</th></tr>\n"
                 + "</thead>\n"
                 + "<tbody>\n"
                 + "<tr class=\"notVerified\">\n"
                 + "<td>I don't return all</td><td>100.0</td><td>&lt;50.0&gt;</td><td>false</td></tr>\n"
                 + "</tbody>\n"
                 + "</table></div>\n"                
                 + "<div class=\"examples\">\n" + "<h3>Examples:</h3>\n"
                 + "<div class=\"step\">Given money &lt;money&gt;</div>\n"
                 + "<div class=\"step\">Then I give it to &lt;to&gt;</div>\n"
                 + "<table>\n" + "<thead>\n"
                 + "<tr>\n<th>money</th><th>to</th></tr>\n" + "</thead>\n" + "<tbody>\n"
                 + "<tr>\n<td>$30</td><td>Mauro</td></tr>\n" + "<tr>\n<td>$50</td><td>Paul</td></tr>\n" + "</tbody>\n"
                 + "</table>\n" + "\n<h3 class=\"example\">Example: {to=Mauro, money=$30}</h3>\n"
                 + "\n<h3 class=\"example\">Example: {to=Paul, money=$50}</h3>\n" + "</div><!-- after examples -->\n"
                 + "</div><!-- after scenario -->\n" + "</div><!-- after story -->\n";
         assertThatOutputIs(out, expected);
     }
 
     @Test
     public void shouldReportEventsToXmlOutput() {
         // Given
         final OutputStream out = new ByteArrayOutputStream();
         PrintStreamFactory factory = new PrintStreamFactory() {
 
             public PrintStream createPrintStream() {
                 return new PrintStream(out);
             }
         };
         StoryReporter reporter = new XmlOutput(factory.createPrintStream());
 
         // When
         narrateAnInterestingStory(reporter);
 
 
         // Then
         String expected = "<story path=\"/path/to/story\" title=\"An interesting story\">\n"
                 + "<narrative keyword=\"Narrative:\">\n"
                 + "  <inOrderTo keyword=\"In order to\">renovate my house</inOrderTo>\n"
                 + "  <asA keyword=\"As a\">customer</asA>\n"
                 + "  <iWantTo keyword=\"I want to\">get a loan</iWantTo>\n"
                 + "</narrative>\n"
                 + "<scenario keyword=\"Scenario:\" title=\"I ask for a loan\">\n"
                 + "<givenStories keyword=\"GivenStories:\"paths=\"[/given/story1,/given/story2]\"</givenStories>\n"
                 + "<step outcome=\"successful\">Given I have a balance of $50</step>\n"
                 + "<step outcome=\"ignorable\">!-- A comment</step>\n"
                 + "<step outcome=\"successful\">When I request $20</step>\n"
                 + "<step outcome=\"successful\">When I ask Liz for a loan of $100</step>\n"
                 + "<step outcome=\"pending\" keyword=\"PENDING\">Then I should have a balance of $30</step>\n"
                 + "<step outcome=\"notPerformed\" keyword=\"NOT PERFORMED\">Then I should have $20</step>\n"
                 + "<step outcome=\"failed\" keyword=\"FAILED\">Then I don&apos;t return loan<failure>org.jbehave.core.model.OutcomesTable$OutcomesFailed</failure></step>\n"
                 + "<outcomes>\n"
                 + "<fields><field>Description</field><field>Value</field><field>Matcher</field><field>Verified</field></fields>\n"
                 + "<outcome><value>I don&apos;t return all</value><value>100.0</value><value>&lt;50.0&gt;</value><value>false</value></outcome>\n"
                 + "</outcomes>\n"
                 + "<examples keyword=\"Examples:\">\n"
                 + "<step>Given money &lt;money&gt;</step>\n"
                 + "<step>Then I give it to &lt;to&gt;</step>\n"
                 + "<parameters>\n"
                 + "<names><name>money</name><name>to</name></names>\n"
                 + "<values><value>$30</value><value>Mauro</value></values>\n"
                 + "<values><value>$50</value><value>Paul</value></values>\n" + "</parameters>\n"
                 + "\n<example keyword=\"Example:\">{to=Mauro, money=$30}</example>\n"
                 + "\n<example keyword=\"Example:\">{to=Paul, money=$50}</example>\n" + "</examples>\n"
                 + "</scenario>\n" + "</story>\n";
         assertThatOutputIs(out, expected);
     }
 
     private void narrateAnInterestingStory(StoryReporter reporter) {
         Story story = new Story(new Description("An interesting story"),
                 new Narrative("renovate my house", "customer", "get a loan"), "/path/to/story", new ArrayList<Scenario>());
         boolean givenStory = false;
         reporter.beforeStory(story, givenStory);
         String title = "I ask for a loan";
         reporter.beforeScenario(title);
         reporter.givenStories(asList("/given/story1,/given/story2"));
         reporter.successful("Given I have a balance of $50");
         reporter.ignorable("!-- A comment");
         reporter.successful("When I request $20");
         reporter.successful("When I ask Liz for a loan of $100");
         reporter.pending("Then I should have a balance of $30");
         reporter.notPerformed("Then I should have $20");
         OutcomesTable outcomesTable = new OutcomesTable();
         outcomesTable.addOutcome("I don't return all", 100.0, equalTo(50.));
         try {
         	outcomesTable.verify();
         } catch ( OutcomesFailed e ){
         	reporter.failedOutcomes("Then I don't return loan", e.outcomesTable());
         }
         ExamplesTable table = new ExamplesTable("|money|to|\n|$30|Mauro|\n|$50|Paul|\n");
         reporter.beforeExamples(asList("Given money <money>", "Then I give it to <to>"), table);
         reporter.example(table.getRow(0));
         reporter.example(table.getRow(1));
         reporter.afterExamples();
         reporter.afterScenario();
         reporter.afterStory(givenStory);
     }
 
     private void assertThatOutputIs(OutputStream out, String expected) {
        Assert.assertEquals(expected, out.toString());
    	assertThat(dos2unix(out.toString()), equalTo(expected));
     }
 
     private String dos2unix(String string) {
         return string.replace("\r\n", "\n");
     }
 
     @Test
     public void shouldReportFailureTraceWhenToldToDoSo() {
         // Given
         IllegalAccessException exception = new IllegalAccessException("Leave my money alone!");
         OutputStream stackTrace = new ByteArrayOutputStream();
         exception.printStackTrace(new PrintStream(stackTrace));
         OutputStream out = new ByteArrayOutputStream();
         StoryReporter reporter = new TxtOutput(new PrintStream(out), new Properties(),
                 new LocalizedKeywords(), true);
 
         // When
         reporter.beforeScenario("A title");
         reporter.successful("Given I have a balance of $50");
         reporter.successful("When I request $20");
         reporter.failed("When I ask Liz for a loan of $100", exception);
         reporter.pending("Then I should have a balance of $30");
         reporter.notPerformed("Then I should have $20");
         reporter.afterScenario();
 
         // Then
         String expected = "Scenario: A title\n" 
         		+ "Given I have a balance of $50\n" 
         		+ "When I request $20\n"
                 + "When I ask Liz for a loan of $100 (FAILED)\n"
                 + "(java.lang.IllegalAccessException: Leave my money alone!)\n"
                 + "Then I should have a balance of $30 (PENDING)\n"
                 + "Then I should have $20 (NOT PERFORMED)\n" 
                 + "\n" + dos2unix(stackTrace.toString()) + "\n";
         assertThatOutputIs(out, expected);
 
         // Given
         out = new ByteArrayOutputStream();
         reporter = new TxtOutput(new PrintStream(out));
 
         // When
         reporter.beforeScenario("A title");
         reporter.successful("Given I have a balance of $50");
         reporter.successful("When I request $20");
         reporter.failed("When I ask Liz for a loan of $100", exception);
         reporter.pending("Then I should have a balance of $30");
         reporter.notPerformed("Then I should have $20");
         reporter.afterScenario();
 
         // Then
         assertThat(out.toString().contains(stackTrace.toString()), is(false));
     }
 
     @Test
     public void shouldReportEventsToTxtOutputWithCustomPatterns() {
         // Given
         IllegalAccessException exception = new IllegalAccessException("Leave my money alone!");
         OutputStream out = new ByteArrayOutputStream();
         Properties patterns = new Properties();
         patterns.setProperty("pending", "{0} - {1} - need to implement me\n");
         patterns.setProperty("failed", "{0} <<< {1}\n");
         patterns.setProperty("notPerformed", "{0} : {1} (because of previous pending)\n");
         StoryReporter reporter = new TxtOutput(new PrintStream(out), patterns, new LocalizedKeywords(),
                 true);
 
         // When
         reporter.successful("Given I have a balance of $50");
         reporter.successful("When I request $20");
         reporter.failed("When I ask Liz for a loan of $100", exception);
         reporter.pending("Then I should have a balance of $30");
         reporter.notPerformed("Then I should have $20");
 
         // Then
         String expected = "Given I have a balance of $50\n" + "When I request $20\n"
                 + "When I ask Liz for a loan of $100 <<< FAILED\n"
                 + "Then I should have a balance of $30 - PENDING - need to implement me\n"
                 + "Then I should have $20 : NOT PERFORMED (because of previous pending)\n";
 
         assertThatOutputIs(out, expected);
 
     }
 
     @Test
     public void shouldReportEventsToPrintStreamInItalian() {
         // Given
         IllegalAccessException exception = new IllegalAccessException("Lasciate in pace i miei soldi!");
         OutputStream out = new ByteArrayOutputStream();
         LocalizedKeywords keywords = new LocalizedKeywords(Locale.ITALIAN);
         StoryReporter reporter = new TxtOutput(new PrintStream(out), new Properties(), keywords,
                 true);
 
         // When
         reporter.successful("Dato che ho un saldo di $50");
         reporter.successful("Quando richiedo $20");
         reporter.failed("Quando chiedo a Liz un prestito di $100", exception);
         reporter.pending("Allora dovrei avere un saldo di $30");
         reporter.notPerformed("Allora dovrei avere $20");
 
         // Then
         String expected = "Dato che ho un saldo di $50\n" 
         		+ "Quando richiedo $20\n"
                 + "Quando chiedo a Liz un prestito di $100 (FALLITO)\n"
                 + "(java.lang.IllegalAccessException: Lasciate in pace i miei soldi!)\n"
                 + "Allora dovrei avere un saldo di $30 (IN SOSPESO)\n"
                 + "Allora dovrei avere $20 (NON ESEGUITO)\n";
 
         assertThatOutputIs(out, expected);
 
     }
 
     @Test
     public void shouldCreateAndWriteToFilePrintStreamForStoryClass() throws IOException {
 
         // Given
         String storyPath = storyPath(MyStory.class);
         FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(CodeLocations.codeLocationFromClass(this.getClass()), storyPath));
         File file = factory.getOutputFile();
         file.delete();
         assertThat(!file.exists(), is(true));
 
         // When
         PrintStream printStream = factory.createPrintStream();
         printStream.print("Hello World");
 
         // Then
         assertThat(file.exists(),  is(true));
         assertThat(IOUtils.toString(new FileReader(file)), equalTo("Hello World"));
     }
 
     @Test
     public void shouldReportEventsToFilePrintStreamsAndGenerateView() throws IOException {
         final String storyPath = storyPath(MyStory.class);
         File outputDirectory = new File("target/output");
         StoryReporter reporter = new StoryReporterBuilder().withOutputDirectory(outputDirectory.getName())
         		.withFormats(HTML, TXT)
                 .build(storyPath);
 
         // When
         narrateAnInterestingStory(reporter);
         ViewGenerator viewGenerator = new FreemarkerViewGenerator();
         viewGenerator.generateView(outputDirectory, asList("html", "txt"), FreemarkerViewGenerator.defaultResources());
 
         // Then
         ensureFileExists(new File(outputDirectory, "view/index.html"));
     }
 
     @Test
     public void shouldBuildPrintStreamReportersAndOverrideDefaultForAGivenFormat() throws IOException {
         final String storyPath = storyPath(MyStory.class);
         final FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(CodeLocations.codeLocationFromClass(this.getClass()), storyPath));
         StoryReporter reporter = new StoryReporterBuilder() {
             public StoryReporter reporterFor(String storyPath, Format format) {
                 switch (format) {
                     case TXT:
                         factory.useConfiguration(new FileConfiguration("text"));
                         return new TxtOutput(factory.createPrintStream(), new Properties(), new LocalizedKeywords(), true);
                     default:
                         return super.reporterFor(storyPath, format);
                 }
             }
         }.withFormats(TXT).build(storyPath);
 
         // When
         narrateAnInterestingStory(reporter);
 
         // Then
         File outputFile = factory.getOutputFile();
         ensureFileExists(outputFile);
     }
 
     private void ensureFileExists(File renderedOutput) throws IOException, FileNotFoundException {
         assertThat(renderedOutput.exists(),  is(true));
         assertThat(IOUtils.toString(new FileReader(renderedOutput)).length(), greaterThan(0));
     }
 
     @Test(expected = ViewGenerationFailedForTemplate.class)
     public void shouldFailRenderingOutputWithInexistentTemplates() throws IOException {
         // Given
         Properties templates = new Properties();
         templates.setProperty("index", "target/inexistent");
         ViewGenerator viewGenerator = new FreemarkerViewGenerator();
         // When
         File outputDirectory = new File("target");
         viewGenerator.generateView(outputDirectory, asList("html"), templates);
         // Then ... fail as expected
     }
 
     private String storyPath(Class<MyStory> storyClass) {
         StoryPathResolver resolver = new UnderscoredCamelCaseResolver(".story");
         return resolver.resolve(storyClass);
     }
 
     private abstract class MyStory extends JUnitStory {
 
     }
 }
