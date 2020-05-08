 package org.lenition.singleton;
 
 import com.google.gson.Gson;
 import org.apache.commons.lang3.ArrayUtils;
 import org.lenition.domain.Factbook;
 import org.lenition.domain.Quiz;
 
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.math.BigDecimal;
import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * Singleton to hold factbook quiz data
  */
 public enum FactbookQuiz {
 
     INSTANCE;                           // must be first listed member in an enum
     private Factbook factbook;
     private Random random = new Random(System.currentTimeMillis());
     private String[] cd = {};           // category distribution
     private static final Logger log = Logger.getLogger(FactbookQuiz.class.getName());
     private final Map<String, Integer> weightedCategories = new HashMap<String, Integer>() {{
         put("area", 5);
         put("population", 5);
         put("gdpPerCapita", 5);
        put("healthExpenditure", 5);
        put("gini", 5);
     }};
     private static String[] EXCLUDED_IDS = {
         "um", // United States Pacific Island Wildlife Refuges
         "wf", // Wallis and Futuna
         "bq", // Navassa Island
         "at", // Ashmore and Cartier Islands
         "kt", // Christmas Island
         "ck", // Cocos (Keeling) Islands
         "cr", // Coral Sea Islands
         "ne", // Niue
         "nf", // Norfolk Island
         "cq", // Northern Mariana Islands
         "tl", // Tokelau
         "tb", // Saint Barthelemy
         "dx", // Dhekelia
         "jn", // Jan Mayen
         "je", // Jersey
         "ip", // Clipperton Island
         "sb", // Saint Pierre and Miquelon
         "io", // British Indian Ocean Territory
         "sh", // Saint Helena, Ascension, and Tristan da Cunha
         "bv", // Bouvet Island
         "fs", // French Southern and Antarctic Lands
         "hm", // Heard Island and McDonald Islands
         "nc", // New Caledonia
         "wq", // Wake Island
         "nr", // Nauru
         "vc", // Saint Vincent and the Grenadines
         "pf", // Paracel Islands
         "kr", // Kiribati
         "tn" // Tonga
     };
 
     FactbookQuiz() {
         // Read factbook data
         Reader reader = new InputStreamReader(FactbookQuiz.class.getClassLoader().getResourceAsStream("factbook-countries.json"));
         Factbook.FactbookContainer o = (new Gson()).fromJson(reader, Factbook.FactbookContainer.class);
         factbook = o.factbook;
 
         // Create probability array for categories
         List<String> cdList = new ArrayList<>();
         for(Map.Entry<String, Integer> weightedCategory : weightedCategories.entrySet()) {
             cdList.addAll(Collections.nCopies(weightedCategory.getValue(), weightedCategory.getKey()));
         }
         cd = cdList.toArray(new String[]{});
     }
 
     /**
      * Return random factbook questions
      * @param numberOfQuestions number of questions
      * @return random
      */
     public Quiz getQuestions(final int numberOfQuestions) {
         int index = 0;
 
         Quiz quiz = new Quiz();
         while(index < numberOfQuestions) {
             Quiz.Question q = new Quiz.Question();
             Factbook.Value[] values = null;
             Factbook.Country[] countries;
             q.category = getRandomCategory();
             do {
                 countries = this.getRandomCountries();
 
 //                System.out.println("Category: " + q.category);
 //                System.out.println("Countries: " + countries[0].name + ", " + countries[1].name);
 
                 switch (q.category) {
                     case "area":
                         values = ArrayUtils.toArray(countries[0].area, countries[1].area);
                         q.text = String.format("%s is bigger than %s.", countries[0].name, countries[1].name);
                         q.hint = String.format("%s: %s<br>%s: %s", countries[0].name, values[0].text, countries[1].name, values[1].text);
                         break;
                     case "population":
                         values = ArrayUtils.toArray(countries[0].population, countries[1].population);
                         q.text = String.format("%s has more people than %s.", countries[0].name, countries[1].name);
                         q.hint = String.format("%s: population growth rate of %s<br>%s: population growth rate of %s",
                                 countries[0].name, percentage(countries[0].populationGrowthRate.value),
                                 countries[1].name, percentage(countries[1].populationGrowthRate.value) );
                         break;
                     case "gdpPerCapita":
                         values = ArrayUtils.toArray(countries[0].gdpPerCapita, countries[1].gdpPerCapita);
                         q.text = String.format("%s has higher GDP (PPP) per capita than %s.", countries[0].name, countries[1].name);
                         q.hint = String.format("%s: total GDP is %s billion%s<br>%s: total GDP is %s billion%s",
                                 countries[0].name, currencyBillions(countries[0].gdp.value), countries[0].gdp.text != null ? " (" + countries[0].gdp.text + ")" : "",
                                 countries[1].name, currencyBillions(countries[1].gdp.value), countries[1].gdp.text != null ? " (" + countries[1].gdp.text + ")" : "");
                         break;
                     case "healthExpenditure":
                         values = ArrayUtils.toArray(countries[0].healthExpenditure, countries[1].healthExpenditure);
                         q.text = String.format("%s has higher health expenditure (%%GDP) than %s.", countries[0].name, countries[1].name);
                         q.hint = String.format("%s: death rate of %s<br>%s: death rate of %s",
                                 countries[0].name, percentage(countries[0].deathRate.value),
                                 countries[1].name, percentage(countries[1].deathRate.value) );
                         break;
                     case "gini":
                         values = ArrayUtils.toArray(countries[0].gini, countries[1].gini);
                         q.text = String.format("%s has a higher Gini index than %s.", countries[0].name, countries[1].name);
                         q.hint = "The Gini index is a measure of income<br>inequality. Higher values mean higher<br>inequality.";
                         break;
                     default:
                         log.info("No such feedback category.");
                         break;
                 }
             } while(values[0].value == null || values[1].value == null );
 
//            System.out.println("values[0].value == null ? " + String.valueOf(values[0].value == null));
//            System.out.println("values[1].value == null ? " + String.valueOf(values[1].value == null));
//
//            System.out.println("Category: " + q.category);
//            System.out.println("Countries: " + countries[0].name + ", " + countries[1].name);
//            System.out.println("Values: " + values[0].value + ", " + values[1].value);
//            System.out.println("Texts: " + values[0].text + ", " + values[1].text);


             q.fact = String.valueOf(values[0].value.compareTo(values[1].value) > 0);
            q.feedback = String.format(" {\"category\": \"%s\", \"values\": [{\"name\": \"%s\",\"value\": %d},{\"name\": \"%s\",\"value\": %d}]}",
                                        q.category, countries[0].name, values[0].value.intValue(), countries[1].name, values[1].value.intValue());
             q.options = getBooleanOptions();
 
             quiz.questions = ArrayUtils.add(quiz.questions, q);
             index++;
         }
 
         return quiz;
     }
 
     /**
      * Returns a randomly selected category based on their weighted probability
      * @return a random category
      */
     private String getRandomCategory() {
         return cd[random.nextInt(cd.length)];
     }
 
     /**
      * Returns a two different, randomly selected countries, neither of which is from the excluded list
      * @return two random countries
      */
     public Factbook.Country[] getRandomCountries() {
         Factbook.Country countryOne, countryTwo;
         do {
             countryOne = this.getRandomCountry();
             countryTwo = this.getRandomCountry();
         } while( countryOne == countryTwo);
         return ArrayUtils.toArray(countryOne, countryTwo);
     }
 
     /**
      * Returns a randomly selected country not in the excluded list
      * @return a random country
      */
     private Factbook.Country getRandomCountry() {
         Factbook.Country country;
         do {
             country = factbook.countries[random.nextInt(factbook.countries.length)];
         } while(ArrayUtils.contains(EXCLUDED_IDS, country.id));
         return country;
     }
 
     /**
      * Default true/false question options
      * @return default boolean options
      */
     private String[] getBooleanOptions() {
             return ArrayUtils.toArray("true", "false");
     }
 
     private static String currencyBillions(BigDecimal n) {
         if(n == null) {
             return "--.--";
         }
         BigDecimal billion = new BigDecimal(1000000000);
         return NumberFormat.getCurrencyInstance().format(n.divide(billion));
     }
 
     private static String percentage(BigDecimal n) {
         if(n == null) {
             return "NA";
         }
         n = n.setScale(2, BigDecimal.ROUND_DOWN);
         DecimalFormat df = new DecimalFormat();
         df.setMaximumFractionDigits(2);
         df.setMinimumFractionDigits(0);
         df.setGroupingUsed(false);
         return df.format(n);
     }
 

 }
