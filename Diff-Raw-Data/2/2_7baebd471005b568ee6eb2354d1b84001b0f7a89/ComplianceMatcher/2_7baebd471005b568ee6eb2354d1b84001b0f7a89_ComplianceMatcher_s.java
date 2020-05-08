 package de.codescape.bitvunit.hamcrest;
 
 import de.codescape.bitvunit.rule.Rule;
 import de.codescape.bitvunit.ruleset.BasicRuleSet;
 import de.codescape.bitvunit.ruleset.RuleSet;
 import org.hamcrest.Description;
 import org.hamcrest.Factory;
 import org.hamcrest.Matcher;
 import org.hamcrest.TypeSafeMatcher;
 
 import static de.codescape.bitvunit.util.HtmlPageUtil.toHtmlPage;
 
 /**
  * Hamcrest matcher to be used to run accessibility checks against a single {@link Rule} or a {@link RuleSet}.
  * <p/>
  * <b>Usage examples:</b>
  * <pre><code>
  * assertThat(supportedType, is(compliantTo(ruleSet)));
  * assertThat(supportedType, is(compliantTo(rule)));
  * </code></pre>
  * <p/>
  * Have a look at {@link de.codescape.bitvunit.util.HtmlPageUtil} methods for supported types.
  *
  * @param <T> one of the supported types
  * @author Stefan Glase
  * @see de.codescape.bitvunit.util.HtmlPageUtil
  * @since 0.4
  */
 public class ComplianceMatcher<T> extends TypeSafeMatcher<T> {
 
     private RuleSet ruleSet;
 
     /**
      * Creates a new {@link ComplianceMatcher} against the provided {@link RuleSet}.
      *
     * @param ruleSet
      */
     public ComplianceMatcher(RuleSet ruleSet) {
         this.ruleSet = ruleSet;
     }
 
     @Override
     protected boolean matchesSafely(T item) {
         return !ruleSet.applyTo(toHtmlPage(item)).hasViolations();
     }
 
     @Override
     public void describeTo(Description description) {
         description.appendText("compliant to ").appendText(ruleSet.toString());
     }
 
     /**
      * Returns a {@link ComplianceMatcher} that checks one of the supported types (see JavaDoc at class level) against
      * the given {@link RuleSet}.
      *
      * @param ruleSet {@link RuleSet} that should be used
      * @param <T>     supported types are contained in JavaDoc at class level
      * @return {@link ComplianceMatcher} to check against the {@link RuleSet}
      */
     @Factory
     public static <T> Matcher<T> compliantTo(RuleSet ruleSet) {
         return new ComplianceMatcher<T>(ruleSet);
     }
 
     /**
      * Returns a {@link ComplianceMatcher} that checks one of the supported types (see JavaDoc at class level) against
      * the given {@link Rule}.
      *
      * @param rule {@link Rule} that should be used
      * @param <T>  supported types are contained in JavaDoc at class level
      * @return {@link ComplianceMatcher} to check against the {@link Rule}
      */
     @Factory
     public static <T> Matcher<T> compliantTo(Rule rule) {
         return compliantTo(new BasicRuleSet(rule));
     }
 
 }
