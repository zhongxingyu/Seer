 package ru.altruix;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Hello world!
  * 
  */
 public class App {
     private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
 
     public void run() {
         LOGGER.info("Sample application");
 
         final Map<Integer, Double> deathRatesByAge =
                 readDeathRates("death-rates.csv");
 
         final List<Human> people = readPeople("population.csv");
 
         final DeathProbabilityCalculator calculator =
                 new DeathProbabilityCalculator();
 
         calculator.setDeathRatesByAge(deathRatesByAge);
         calculator.init();
 
         for (final Human curPerson : people) {
             final double experiment = Math.random();
 
             final double[] range =
                     calculator.getConditionalProbabilityRange(curPerson
                             .getAge());
 
             boolean death;
             if ((range[0] >= experiment) && (experiment <= range[1])) {
                 death = true;
             } else {
                 death = false;
             }
 
             LOGGER.debug("Age: {}, death: {}",
                     new Object[] { curPerson.getAge(), death });
         }
     }
 
     private List<Human> readPeople(final String aPath) {
         final List<Human> returnValue = new LinkedList<Human>();
         final InputStream inputStream =
                 Thread.currentThread()
                         .getContextClassLoader()
                         .getResourceAsStream(
                                 aPath);
 
         try {
             final List<String> lines = IOUtils.readLines(inputStream);
 
             lines.remove(0); // Remove header
 
             for (final String curLine : lines) {
                 final StringTokenizer tokenizer =
                         new StringTokenizer(curLine, ";");
 
                 final String idAsString = tokenizer.nextToken();
                 final String ageAsString = tokenizer.nextToken();
 
                 long id = parseLongQuietly(idAsString);
                 int age = parseIntQuietly(ageAsString);
 
                 if ((id > 0) && (age >= 0)) {
                     final Human human = new Human();
 
                     human.setAge(age);
                     human.setId(id);
 
                     returnValue.add(human);
                 }
             }
         } catch (final IOException exception) {
             LOGGER.error("", exception);
         } finally {
             IOUtils.closeQuietly(inputStream);
         }
         return returnValue;
     }
 
     private int parseIntQuietly(final String aAgeAsString) {
         int age = -1;
         try {
             age = Integer.parseInt(aAgeAsString);
         } catch (final NumberFormatException exception) {
             LOGGER.error("Cannot parse integer value '{}'", aAgeAsString);
         }
         return age;
     }
 
     private long parseLongQuietly(final String aIdAsString) {
         long id = -1;
         try {
             id = Long.parseLong(aIdAsString);
         } catch (final NumberFormatException exception) {
             LOGGER.error("Cannot parse long value '{}'", aIdAsString);
         }
         return id;
     }
 
     private Map<Integer, Double> readDeathRates(final String aPath) {
         final Map<Integer, Double> returnValue = new HashMap<Integer, Double>();
         final InputStream inputStream =
                 Thread.currentThread()
                         .getContextClassLoader()
                         .getResourceAsStream(
                                 aPath);
 
         try {
             final List<String> lines = IOUtils.readLines(inputStream);
 
             lines.remove(0);
 
             for (final String curLine : lines) {
                 final StringTokenizer tokenizer =
                         new StringTokenizer(curLine, ";");
 
                 tokenizer.nextToken(); // Ignore year
                 final String ageAsString = tokenizer.nextToken();
                 tokenizer.nextToken(); // Ignore female death rate
                 tokenizer.nextToken(); // Ignore male death rate
                 final String deathRateAsString = tokenizer.nextToken();
 
                 int age = parseIntQuietly(ageAsString);
                 double deathRate = parseDoubleQuietly(deathRateAsString);
 
                 if ((age >= 0) && (deathRate >= 0.)) {
                     returnValue.put(age, deathRate);
                 }
             }
         } catch (final IOException exception) {
             LOGGER.error("", exception);
         } finally {
             IOUtils.closeQuietly(inputStream);
         }
 
         return returnValue;
     }
 
     private double parseDoubleQuietly(final String aDoubleValueAsString) {
         double age = -1;
         try {
             age = Double.parseDouble(aDoubleValueAsString);
         } catch (final NumberFormatException exception) {
             LOGGER.error("Cannot parse integer value '{}'",
                     aDoubleValueAsString);
         }
         return age;
 
     }
 
     public static void main(final String[] aArgs) {
         final App app = new App();
 
         app.run();
     }
 }
