 package spectacular.data.model;
 
 
 import groovy.lang.Closure;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class FixtureInventory {
 
     private static Log LOGGER = LogFactory.getLog(FixtureInventory.class);
     private Map<String, Closure> fixtures;
     private Map<Pattern, String> fixturePatterns;
 
     public FixtureInventory() {
         this.fixtures = new HashMap<String, Closure>();
         this.fixturePatterns = new HashMap<Pattern, String>();
     }
 
 
     public void setFixtures(Map<String, Closure> fixtures) {
         this.fixturePatterns.clear();
         for(String key : fixtures.keySet()) {
             addFixture(key, fixtures.get(key));
         }
     }
 
     public void addFixture(String matchText, Closure fixture) {
         this.fixtures.put(matchText, fixture);
         createPattern(matchText);
     }
 
     public ExecutionInstance findExecutionInstanceForText(String text) {
 
         Closure closure = null;
         ExecutionInstance executionInstance = null;
 
         for(Pattern pattern : this.fixturePatterns.keySet()) {
 
             Matcher matcher = pattern.matcher(text);
             if(matcher.matches()) {
 
                 List<String> regexMatches = new LinkedList<String>();
                 closure = this.fixtures.get(this.fixturePatterns.get(pattern));
                 for(int i = 1 ; i <= matcher.groupCount() ; i++) {
                     String matchText = matcher.group(i);
                    LOGGER.info("Text to replace:  " + matchText);
                     regexMatches.add(matchText);
                 }
 
                 executionInstance = new ExecutionInstance(closure, regexMatches);
 
             }
 
 
         }
 
         return(executionInstance);
 
     }
 
     protected void setFixturePatterns(Map<Pattern, String> fixturePatterns) {
         this.fixturePatterns = fixturePatterns;
     }
 
     protected void addFixturePattern(String matchText, Pattern pattern) {
         this.fixturePatterns.put(pattern, matchText);
     }
 
     protected void createPattern(String text) {
         Pattern pattern = Pattern.compile(text);
         addFixturePattern(text, pattern);
     }
 }
