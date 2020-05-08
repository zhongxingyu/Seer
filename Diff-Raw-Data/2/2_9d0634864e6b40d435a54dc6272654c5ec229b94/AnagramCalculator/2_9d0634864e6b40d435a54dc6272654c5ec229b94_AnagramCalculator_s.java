 package se.atoulou.facebook.hackercup.alphabetsoup;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multiset;
 import com.google.common.collect.Multisets;
 import com.google.common.collect.TreeMultiset;
 import com.google.common.io.CharStreams;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 /**
  * To run, 'mvn clean compile' followed by 'mvn exec:java' should do the trick.
  * 
  * NOTE: I'm totally having fun with more infrastructure than needed.
  * 
  * @author toulouse
  */
 public class AnagramCalculator {
 
     // BOILERPLATE
 
     // Initialize injector
     private static final Module module = new AlphabetInjectModule();
     private static final Injector injector = Guice.createInjector(new Module[] { module });
 
     // Initialize logging
     private static final Logger logger = LoggerFactory.getLogger(AnagramCalculator.class);
 
     public static void main(String[] args) {
         AnagramCalculator calculator = injector.getInstance(AnagramCalculator.class);
         calculator.run();
     }
 
     public static Injector getInjector() {
         return injector;
     }
 
     // SLIGHTLY MORE INTERESTING BOILERPLATE
 
     // Some app-specific configuration
     private static final String INPUT_FILE_PATH = "/se/atoulou/facebook/hackercup/alphabetsoup/input.txt";
 
     protected final List<String> inputLines;
     protected final PrintWriter writer;
 
     @Inject
     public AnagramCalculator(@StandardOutOutput OutputStream outputStream) {
         writer = new PrintWriter(outputStream);
 
         final InputStream stream = getClass().getResourceAsStream(INPUT_FILE_PATH);
         String inputString;
         try {
             inputString = CharStreams.toString(new InputStreamReader(stream));
             logger.trace("Input String:" + inputString);
         } catch (IOException e) {
             logger.error("Error loading input file into string", e);
             throw new RuntimeException(e);
         }
 
         String[] rawLines = inputString.split("\\r?\\n"); // Untested, alternatively, "[\\r\\n]+" to skip empty lines
         int expectedInputLength = Integer.parseInt(rawLines[0]);
         this.inputLines = Lists.newArrayListWithExpectedSize(expectedInputLength);
 
         // Not using String line : lines because I treat line #1 differently
         for (int i = 1; i < rawLines.length; i++) {
             // What if the input ends with an empty line?
             String line = rawLines[i];
             if (!line.isEmpty()) {
                 inputLines.add(line);
             }
 
             if (i > expectedInputLength) {
                 RuntimeException e = new IllegalArgumentException("The number of input lines read doesn't match the expected number given in the data file!");
                 logger.error("Bad input.", e);
                 throw e;
             }
         }
 
         checkConstraints(inputLines);
     }
 
     // INPUT VERIFICATION
 
     protected void checkConstraints(List<String> inputLines) {
        boolean size = inputLines.size() > 1 && inputLines.size() < 20;
         boolean uppercase = Iterables.all(inputLines, new Predicate<String>() {
 
             @Override
             public boolean apply(String input) {
                 return input.matches("^[A-Z ]+$");
             }
         });
 
         if (assertionsEnabled()) {
             assert size;
             assert uppercase;
         } else {
             if (!size) {
                 RuntimeException e = new IllegalArgumentException("The number of input lines violates the specification's constraints!");
                 logger.error("Bad input.", e);
                 throw e;
             }
             if (!uppercase) {
                 RuntimeException e = new IllegalArgumentException("There are invalid characters in the input text!");
                 logger.error("Bad input.", e);
                 throw e;
             }
         }
     }
 
     private boolean assertionsEnabled() {
         boolean assertionsAreEnabled = false;
         try {
             assert assertionsAreEnabled;
         } catch (AssertionError e) {
             assertionsAreEnabled = true;
         }
 
         return assertionsAreEnabled;
     }
 
     // THE GOOD STUFF
 
     protected void run() {
         int i = 1;
         for (String inputLine : inputLines) {
             int answer = calculateAnagram(inputLine);
             writer.println("Case #" + i + ": " + answer);
             writer.flush();
             i++;
         }
     }
 
     private int calculateAnagram(String inputLine) {
         // Tree multiset used for prettiness so the debug output alphabetizes the log.
         final Multiset<Character> charactersMultiset = TreeMultiset.create(Lists.charactersOf(inputLine));
         final Multiset<Character> hackerCupMultiset = TreeMultiset.create(Lists.charactersOf("HACKERCUP"));
 
         int occurrences = 0;
         // Guava is sweet and so cheaty-face for this problem
         while (Multisets.intersection(charactersMultiset, hackerCupMultiset).containsAll(hackerCupMultiset)) {
             Multisets.removeOccurrences(charactersMultiset, hackerCupMultiset);
             occurrences++;
         }
 
         logger.trace("\"{}\"({}): {}", new String[] { inputLine, Integer.toString(occurrences), charactersMultiset.toString() });
 
         return occurrences;
     }
 }
