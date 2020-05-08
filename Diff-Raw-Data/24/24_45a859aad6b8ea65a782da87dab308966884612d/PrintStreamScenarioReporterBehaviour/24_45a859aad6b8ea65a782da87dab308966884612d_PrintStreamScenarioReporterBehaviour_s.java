 package org.jbehave.scenario.reporters;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.jbehave.Ensure.ensureThat;
 import static org.junit.Assert.assertEquals;
 
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
 import org.jbehave.scenario.Scenario;
 import org.jbehave.scenario.definition.Blurb;
 import org.jbehave.scenario.definition.ExamplesTable;
 import org.jbehave.scenario.definition.ScenarioDefinition;
 import org.jbehave.scenario.definition.StoryDefinition;
 import org.jbehave.scenario.i18n.I18nKeyWords;
 import org.jbehave.scenario.parser.ScenarioNameResolver;
 import org.jbehave.scenario.parser.UnderscoredCamelCaseResolver;
 import org.jbehave.scenario.reporters.FilePrintStreamFactory.FileConfiguration;
 import org.jbehave.scenario.reporters.FreemarkerReportRenderer.RenderingFailedException;
 import org.junit.Test;
 
 public class PrintStreamScenarioReporterBehaviour {
 
     @Test
     public void shouldReportEventsToPrintStream() {
         // Given
         OutputStream out = new ByteArrayOutputStream();
         ScenarioReporter reporter = new PrintStreamScenarioReporter(new PrintStream(out));
 
         // When
         narrateAnInterestingStory(reporter);
 
         // Then
         String expected = "An interesting story\n" + "(/path/to/story)\n" + "Scenario: I ask for a loan\n"
                 + "GivenScenarios: [/given/scenario1,/given/scenario2]\n" + "Given I have a balance of $50\n"
                 + "When I request $20\n" + "When I ask Liz for a loan of $100\n"
                 + "Then I should have a balance of $30 (PENDING)\n" + "Then I should have $20 (NOT PERFORMED)\n"
                 + "Then I don't return loan (FAILED)\n" + "Examples:\n\n" + "|money|to|\n" + "|$30|Mauro|\n"
                 + "|$50|Paul|\n" + "\n\n" + // Examples table
                 "\nExample: {to=Mauro, money=$30}\n" + "\nExample: {to=Paul, money=$50}\n" + "\n" + // end
                 // of
                 // examples
                 "\n\n"; // end of scenario and story
         ensureThatOutputIs(out, expected);
     }
 
     @Test
     public void shouldReportEventsToHtmlPrintStream() {
         // Given
         final OutputStream out = new ByteArrayOutputStream();
         PrintStreamFactory factory = new PrintStreamFactory() {
 
             public PrintStream getPrintStream() {
                 return new PrintStream(out);
             }
         };
         ScenarioReporter reporter = new HtmlPrintStreamScenarioReporter(factory.getPrintStream());
 
         // When
         narrateAnInterestingStory(reporter);
 
         // Then
         String expected = "<div class=\"story\">\n<h1>An interesting story</h1>\n"
                 + "<div class=\"path\">/path/to/story</div>\n"
                 + "<div class=\"scenario\">\n<h2>Scenario: I ask for a loan</h2>\n"
                 + "<div class=\"givenScenarios\">GivenScenarios: [/given/scenario1,/given/scenario2]</div>\n"
                 + "<div class=\"step successful\">Given I have a balance of $50</div>\n"
                 + "<div class=\"step successful\">When I request $20</div>\n"
                 + "<div class=\"step successful\">When I ask Liz for a loan of $100</div>\n"
                + "<div class=\"step pending\">Then I should have a balance of $30<span class=\"keyword pending\">(PENDING)</span></div>\n"
                + "<div class=\"step notPerformed\">Then I should have $20<span class=\"keyword notPerformed\">(NOT PERFORMED)</span></div>\n"
                + "<div class=\"step failed\">Then I don't return loan<span class=\"keyword failed\">(FAILED)</span></div>\n"
                 + "<div class=\"examples\">\n" + "<h3>Examples:</h3>\n" + "<table>\n" + "<thead>\n"
                 + "<tr>\n<th>money</th><th>to</th></tr>\n" + "</thead>\n" + "<tbody>\n"
                 + "<tr>\n<td>$30</td><td>Mauro</td></tr>\n" + "<tr>\n<td>$50</td><td>Paul</td></tr>\n" + "</tbody>\n"
                 + "</table>\n" + "\n<h3 class=\"example\">Example: {to=Mauro, money=$30}</h3>\n"
                 + "\n<h3 class=\"example\">Example: {to=Paul, money=$50}</h3>\n" + "</div>\n" + // end
                 // of
                 // examples
                 "</div>\n</div>\n"; // end of scenario and story
         ensureThatOutputIs(out, expected);
     }
 
     @Test
     public void shouldReportEventsToHtmlPrintStreamUsingCustomOutputPatterns() {
         // Given
         final OutputStream out = new ByteArrayOutputStream();
         PrintStreamFactory factory = new PrintStreamFactory() {
 
             public PrintStream getPrintStream() {
                 return new PrintStream(out);
             }
         };
         Properties patterns = new Properties();
         patterns.setProperty("afterStory", "</div><!-- after story -->\n");
         patterns.setProperty("afterScenario", "</div><!-- after scenario -->\n");
         patterns.setProperty("afterExamples", "</div><!-- after examples -->\n");
         ScenarioReporter reporter = new HtmlPrintStreamScenarioReporter(factory.getPrintStream(), patterns);
 
         // When
         narrateAnInterestingStory(reporter);
 
         // Then
         String expected = "<div class=\"story\">\n<h1>An interesting story</h1>\n"
                 + "<div class=\"path\">/path/to/story</div>\n"
                 + "<div class=\"scenario\">\n<h2>Scenario: I ask for a loan</h2>\n"
                 + "<div class=\"givenScenarios\">GivenScenarios: [/given/scenario1,/given/scenario2]</div>\n"
                 + "<div class=\"step successful\">Given I have a balance of $50</div>\n"
                 + "<div class=\"step successful\">When I request $20</div>\n"
                 + "<div class=\"step successful\">When I ask Liz for a loan of $100</div>\n"
                + "<div class=\"step pending\">Then I should have a balance of $30<span class=\"keyword pending\">(PENDING)</span></div>\n"
                + "<div class=\"step notPerformed\">Then I should have $20<span class=\"keyword notPerformed\">(NOT PERFORMED)</span></div>\n"
                + "<div class=\"step failed\">Then I don't return loan<span class=\"keyword failed\">(FAILED)</span></div>\n"
                 + "<div class=\"examples\">\n" + "<h3>Examples:</h3>\n" + "<table>\n" + "<thead>\n"
                 + "<tr>\n<th>money</th><th>to</th></tr>\n" + "</thead>\n" + "<tbody>\n"
                 + "<tr>\n<td>$30</td><td>Mauro</td></tr>\n" + "<tr>\n<td>$50</td><td>Paul</td></tr>\n" + "</tbody>\n"
                 + "</table>\n" + "\n<h3 class=\"example\">Example: {to=Mauro, money=$30}</h3>\n"
                 + "\n<h3 class=\"example\">Example: {to=Paul, money=$50}</h3>\n" + "</div><!-- after examples -->\n"
                 + "</div><!-- after scenario -->\n" + "</div><!-- after story -->\n";
         ensureThatOutputIs(out, expected);
     }
 
     private void narrateAnInterestingStory(ScenarioReporter reporter) {
         StoryDefinition story = new StoryDefinition(new Blurb("An interesting story"),
                 new ArrayList<ScenarioDefinition>(), "/path/to/story");
         boolean embeddedStory = true;
         reporter.beforeStory(story, embeddedStory);
         String title = "I ask for a loan";
         reporter.beforeScenario(title);
         reporter.givenScenarios(asList("/given/scenario1,/given/scenario2"));
         reporter.successful("Given I have a balance of $50");
         reporter.successful("When I request $20");
         reporter.successful("When I ask Liz for a loan of $100");
         reporter.pending("Then I should have a balance of $30");
         reporter.notPerformed("Then I should have $20");
         reporter.failed("Then I don't return loan", new Exception("Naughty me!"));
         ExamplesTable table = new ExamplesTable("|money|to|\n|$30|Mauro|\n|$50|Paul|\n");
         reporter.beforeExamples(table);
         reporter.example(table.getRow(0));
         reporter.example(table.getRow(1));
         reporter.afterExamples();
         reporter.afterScenario();
         reporter.afterStory(embeddedStory);
     }
 
     private void ensureThatOutputIs(OutputStream out, String expected) {
         // JUnit assertion allows easier comparison of strings in IDE
         assertEquals(expected, dos2unix(out.toString()));
         // ensureThat(out.toString(), equalTo(expected));
     }
 
     private String dos2unix(String string) {
         return string.replace("\r\n", "\n");
     }
 
     @Test
     public void shouldReportThrowablesWhenToldToDoSo() {
         // Given
         IllegalAccessException exception = new IllegalAccessException("Leave my money alone!");
         OutputStream stackTrace = new ByteArrayOutputStream();
         exception.printStackTrace(new PrintStream(stackTrace));
         OutputStream out = new ByteArrayOutputStream();
         ScenarioReporter reporter = new PrintStreamScenarioReporter(new PrintStream(out), new Properties(),
                 new I18nKeyWords(), true);
 
         // When
         reporter.beforeScenario("A title");
         reporter.successful("Given I have a balance of $50");
         reporter.successful("When I request $20");
         reporter.failed("When I ask Liz for a loan of $100", exception);
         reporter.pending("Then I should have a balance of $30");
         reporter.notPerformed("Then I should have $20");
         reporter.afterScenario();
 
         // Then
         String expected = "Scenario: A title\n" + "Given I have a balance of $50\n" + "When I request $20\n"
                 + "When I ask Liz for a loan of $100 (FAILED)\n" + "Then I should have a balance of $30 (PENDING)\n"
                 + "Then I should have $20 (NOT PERFORMED)\n" + "\n" + dos2unix(stackTrace.toString()) + "\n";
         ensureThatOutputIs(out, expected);
 
         // Given
         out = new ByteArrayOutputStream();
         reporter = new PrintStreamScenarioReporter(new PrintStream(out));
 
         // When
         reporter.beforeScenario("A title");
         reporter.successful("Given I have a balance of $50");
         reporter.successful("When I request $20");
         reporter.failed("When I ask Liz for a loan of $100", exception);
         reporter.pending("Then I should have a balance of $30");
         reporter.notPerformed("Then I should have $20");
         reporter.afterScenario();
 
         // Then
         ensureThat(!out.toString().contains(stackTrace.toString()));
     }
 
     @Test
     public void shouldReportEventsToPrintStreamWithCustomPatterns() {
         // Given
         IllegalAccessException exception = new IllegalAccessException("Leave my money alone!");
         OutputStream out = new ByteArrayOutputStream();
         Properties patterns = new Properties();
         patterns.setProperty("pending", "{0} - {1} - need to implement me\n");
         patterns.setProperty("failed", "{0} <<< {1}\n");
         patterns.setProperty("notPerformed", "{0} : {1} (because of previous pending)\n");
         ScenarioReporter reporter = new PrintStreamScenarioReporter(new PrintStream(out), patterns, new I18nKeyWords(),
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
 
         ensureThatOutputIs(out, expected);
 
     }
 
     @Test
     public void shouldReportEventsToPrintStreamInItalian() {
         // Given
         IllegalAccessException exception = new IllegalAccessException("Lasciate in pace i miei soldi!");
         OutputStream out = new ByteArrayOutputStream();
         I18nKeyWords keywords = new I18nKeyWords(Locale.ITALIAN);
         ScenarioReporter reporter = new PrintStreamScenarioReporter(new PrintStream(out), new Properties(), keywords,
                 true);
 
         // When
         reporter.successful("Dato che ho un saldo di $50");
         reporter.successful("Quando richiedo $20");
         reporter.failed("Quando chiedo a Liz un prestito di $100", exception);
         reporter.pending("Allora dovrei avere un saldo di $30");
         reporter.notPerformed("Allora dovrei avere $20");
 
         // Then
         String expected = "Dato che ho un saldo di $50\n" + "Quando richiedo $20\n"
                 + "Quando chiedo a Liz un prestito di $100 (FALLITO)\n"
                 + "Allora dovrei avere un saldo di $30 (PENDENTE)\n" + "Allora dovrei avere $20 (NON ESEGUITO)\n";
 
         ensureThatOutputIs(out, expected);
 
     }
 
     @Test
     public void shouldCreateAndWriteToFilePrintStreamForScenarioClass() throws IOException {
         UnderscoredCamelCaseResolver converter = new UnderscoredCamelCaseResolver(".scenario");
 
         // Given
         Class<MyScenario> scenarioClass = MyScenario.class;
         File file = fileFor(scenarioClass, converter);
         file.delete();
         ensureThat(!file.exists());
 
         // When
         FilePrintStreamFactory factory = new FilePrintStreamFactory(scenarioClass, converter);
         PrintStream printStream = factory.getPrintStream();
         printStream.print("Hello World");
 
         // Then
         ensureThat(file.exists());
         ensureThat(IOUtils.toString(new FileReader(file)), equalTo("Hello World"));
     }
 
     private File fileFor(Class<MyScenario> scenarioClass, UnderscoredCamelCaseResolver converter) {
         FileConfiguration configuration = new FileConfiguration();
         File outputDirectory = FilePrintStreamFactory.outputDirectory(scenarioClass, configuration);
         String fileName = FilePrintStreamFactory.fileName(scenarioClass, converter, configuration);
         return new File(outputDirectory, fileName);
     }
 
     @Test
     public void shouldReportEventsToFilePrintStreamsAndRenderAggregatedIndex() throws IOException {
         ScenarioNameResolver nameResolver = new UnderscoredCamelCaseResolver();
         Class<MyScenario> scenarioClass = MyScenario.class;
         ScenarioReporter htmlReporter = new HtmlPrintStreamScenarioReporter(new FilePrintStreamFactory(
                 scenarioClass, nameResolver, new FileConfiguration("html")).getPrintStream());
         ScenarioReporter statsReporter = new StatisticsScenarioReporter(new FilePrintStreamFactory(
                 scenarioClass, nameResolver, new FileConfiguration("stats")).getPrintStream());
         ScenarioReporter txtReporter = new PrintStreamScenarioReporter(new FilePrintStreamFactory(
                 scenarioClass, nameResolver, new FileConfiguration("txt")).getPrintStream());
         ScenarioReporter reporter = new DelegatingScenarioReporter(htmlReporter, statsReporter, txtReporter);
 
         // When
         narrateAnInterestingStory(reporter);
         File outputDirectory = new FilePrintStreamFactory(scenarioClass, new UnderscoredCamelCaseResolver())
                 .getOutputDirectory();
         ReportRenderer renderer = new FreemarkerReportRenderer();
         renderer.render(outputDirectory, asList("html", "stats", "txt"));
 
         // Then
         ensureFileExists(new File(outputDirectory, "rendered/index.html"));
     }
 
     private void ensureFileExists(File renderedOutput) throws IOException, FileNotFoundException {
         ensureThat(renderedOutput.exists());
         ensureThat(IOUtils.toString(new FileReader(renderedOutput)).length() > 0);
     }
 
     @Test(expected = RenderingFailedException.class)
     public void shouldFailRenderingOutputWithInexistentTemplates() throws IOException {
         // Given
         Properties templates = new Properties();
         templates.setProperty("index", "target/inexistent");
         ReportRenderer renderer = new FreemarkerReportRenderer(templates);
         // When
         File outputDirectory = new File("target");
         renderer.render(outputDirectory, asList("html"));
         // Then ... fail as expected
     }
 
     private static class MyScenario extends Scenario {
 
     }
 }
