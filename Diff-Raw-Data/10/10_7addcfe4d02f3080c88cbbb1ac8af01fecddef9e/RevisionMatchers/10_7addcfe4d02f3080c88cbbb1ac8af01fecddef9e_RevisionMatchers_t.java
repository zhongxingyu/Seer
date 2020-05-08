 package uk.org.sappho.codeheatmap.ui.web.server.handlers.utils;
 
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.endsWith;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
 
 import org.hamcrest.Matcher;
 
 import uk.org.sappho.code.change.management.data.RevisionData;
 import uk.org.sappho.codeheatmap.ui.web.server.handlers.AugmentedRevisionData;
 
 public class RevisionMatchers {
 
     public static Matcher<String> issueKeyPrefix(String issueKeyPrefix) {
         return having(on(AugmentedRevisionData.class).getIssueKey(), not(startsWith(issueKeyPrefix)));
     }
 
     public static Matcher<RevisionData> isntAMerge() {
         return having(on(RevisionData.class).isMerge(), is(false));
     }
 
     public static Matcher<String> isProductionJava() {
         return allOf(endsWith(".java"), not(containsString("src/test/java")));
     }
 
 }
