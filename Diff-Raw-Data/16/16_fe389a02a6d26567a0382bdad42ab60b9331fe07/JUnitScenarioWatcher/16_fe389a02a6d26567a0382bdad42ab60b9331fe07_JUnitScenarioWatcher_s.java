 package net.joala.bdd.watcher;
 
 import com.google.common.base.Strings;
 import org.junit.internal.AssumptionViolatedException;
 import org.junit.runner.Description;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nonnull;
 import java.util.Formatter;
 import java.util.regex.Pattern;
 
 import static java.lang.Math.max;
 
 /**
  * <p>
  * This watcher will log information on stories and scenarios executed. It assumes that
  * the story is the name of the test case while the scenario is the name of the test
  * method. While Story Names are expected to be written in camel-case scenarios must
  * contain the string "scenario" otherwise they won't be logged.
  * </p>
  * <p>
  * Add to your tests using:
  * </p>
  * <pre>{@code
  * &#64;Rule
  * public final TestWatcher testWatcher = new JUnitScenarioWatcher();
  * }
  * </pre>
  *
  * @since 6/2/12
  */
 public class JUnitScenarioWatcher extends JUnit411TestWatcher {
   private static final Logger LOG = LoggerFactory.getLogger(JUnitScenarioWatcher.class);
   private static final Pattern INSERT_SPACE_BEFORE_CAP_LETTERS_PATTERN = Pattern.compile("([A-Z][^A-Z0-9]*|[0-9]+)");
   private static final Pattern REMOVE_TEST_SUFFIX_PATTERN = Pattern.compile("I?Test$");
   private static final int MIN_DOTS = 3;
   private static final int MAX_TYPE_PADDING = 15;
 
   private String formatStory(final CharSequence rawStory) {
     final String withoutTest = REMOVE_TEST_SUFFIX_PATTERN.matcher(rawStory).replaceFirst("");
     return INSERT_SPACE_BEFORE_CAP_LETTERS_PATTERN.matcher(withoutTest).replaceAll(" $1").trim();
   }
 
   @Nonnull
   private String formatScenario(@Nonnull final String rawScenario) {
     return rawScenario.replace('_', ' ').replace("scenario", "").trim();
   }
 
   @Override
   protected void starting(@Nonnull final Description description) {
     if (isScenario(description)) {
      reportStart("Story", formatStory(description.getTestClass().getSimpleName()));
      reportStart("Scenario", formatScenario(description.getMethodName()));
     }
   }
 
   @Override
   protected void skipped(final AssumptionViolatedException e, final Description description) {
     if (isScenario(description)) {
      reportEnd("Scenario", formatScenario(description.getMethodName()), "skipped");
     }
   }
 
   @Override
   protected void failed(final Throwable e, @Nonnull final Description description) {
     if (isScenario(description)) {
      reportEnd("Scenario", formatScenario(description.getMethodName()), "failed");
     }
   }
 
   @Override
   protected void succeeded(@Nonnull final Description description) {
     if (isScenario(description)) {
      reportEnd("Scenario", formatScenario(description.getMethodName()), "success");
     }
   }
 
   private boolean isScenario(@Nonnull final Description description) {
     return description.getMethodName().contains("scenario");
   }
 
   protected void reportStart(final String type, final String message) {
     report(new Formatter().format("%1$S: %2$s %3$s", type, typePadding(type), message).toString());
   }
 
   protected void reportEnd(final String type, final String message, final String result) {
     report(new Formatter().format("%1$S: %2$s %3$s (%4$S)", type, typePadding(type), message, result).toString());
   }
 
   protected void report(final String message) {
     LOG.info(message);
   }
 
   private String typePadding(final CharSequence type) {
     return Strings.repeat(".", max(MIN_DOTS, MAX_TYPE_PADDING - type.length()));
   }
 }
