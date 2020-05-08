 package de.cosmocode.lucene.fragments;
 
 import org.junit.Test;
 
 import de.cosmocode.lucene.LuceneQuery;
 import de.cosmocode.lucene.QueryModifier;
 
 /**
  * <p> Tests the addArgument-method
  * {@link LuceneQuery#addArgument(String, QueryModifier)}.
  * 
  * @author Oliver Lorenz
  */
 public class AddFieldStringModFragment extends AbstractQueryModifierFragment {
     
     /**
      * Tests {@link LuceneQuery#addField(String, String, QueryModifier)}
      * with a null String and a dummy QueryModifier.
      */
     @Test(expected = IllegalStateException.class)
     public void stringNull() {
         final LuceneQuery unit = unit().addField(FIELD1, (String) null, QueryModifier.DEFAULT);
         unit.getQuery();
     }
     
     /**
      * Tests {@link LuceneQuery#addField(String, String, QueryModifier)}
      * with an empty String ("") and a dummy QueryModifier.
      */
     @Test(expected = IllegalStateException.class)
     public void stringEmpty() {
         final LuceneQuery unit = unit().addField(FIELD1, "", QueryModifier.DEFAULT);
         unit.getQuery();
     }
     
     /**
      * Tests {@link LuceneQuery#addField(String, String, QueryModifier)}
      * with a blank String ("   ") and a dummy QueryModifier.
      */
     @Test(expected = IllegalStateException.class)
     public void stringBlank() {
         final LuceneQuery unit = unit().addField(FIELD1, "   ", QueryModifier.DEFAULT);
         unit.getQuery();
     }
     
     /**
      * Tests {@link LuceneQuery#addField(String, String, QueryModifier)}
      * with a valid String and the QueryModifier null.
      * Expects a NullPointerException.
      */
     @Test(expected = NullPointerException.class)
     public void modifierNull() {
         unit().addField(FIELD1, "test", null);
     }
     
 
     @Override
     protected void applyNormal(LuceneQuery query, QueryModifier mod) {
         query.addField(FIELD1, ARG3, mod);
     }
 
     @Override
     protected String expectedNormalConjunct() {
         return FIELD1 + ":" + ARG3;
     }
 
     @Override
     protected String expectedNormalDisjunct() {
         return FIELD1 + ":" + ARG3;
     }
     
 
     @Override
     protected void applyFuzzy(LuceneQuery query, QueryModifier mod) {
         query.addField(FIELD1, FUZZY1, mod);
     }
 
     @Override
     protected String expectedFuzzyConjunct() {
         return FIELD1 + ":" + FUZZY1 + "~0.7";
     }
 
     @Override
     protected String expectedFuzzyDisjunct() {
         return FIELD1 + ":" + FUZZY1 + "~0.7";
     }
     
 
     @Override
     protected void applySplit(LuceneQuery query, QueryModifier mod) {
         query.addField(FIELD1, ARG1 + "   " + ARG3, mod);
     }
 
     @Override
     protected String expectedSplitConjunct() {
         return FIELD1 + ":(" + ARG1 + " AND " + ARG3 + ")";
     }
 
     @Override
     protected String expectedSplitDisjunct() {
         return FIELD1 + ":(" + ARG1 + " OR " + ARG3 + ")";
     }
     
 
     @Override
     protected void applyWildcarded(LuceneQuery query, QueryModifier mod) {
         query.addField(FIELD1, WILDCARD1, mod);
     }
 
     @Override
     protected String expectedWildcardedConjunct() {
         return FIELD1 + ":(" + WILDCARD1 + " " + WILDCARD1 + "*)";
     }
 
     @Override
     protected String expectedWildcardedDisjunct() {
         return FIELD1 + ":(" + WILDCARD1 + " " + WILDCARD1 + "*)";
     }
     
 
     @Override
     protected void applyFuzzySplit(LuceneQuery query, QueryModifier mod) {
         query.addField(FIELD1, FUZZY1 + "   " + FUZZY2, mod);
     }
 
     @Override
     protected String expectedFuzzySplitConjunct() {
         return FIELD1 + ":(" + "(" + FUZZY1 + "~0.7) AND (" + FUZZY2 + "~0.7))";
     }
 
     @Override
     protected String expectedFuzzySplitDisjunct() {
         return FIELD1 + ":(" + "(" + FUZZY1 + "~0.7) OR (" + FUZZY2 + "~0.7))";
     }
     
 
     @Override
     protected void applyWildcardedFuzzy(LuceneQuery query, QueryModifier mod) {
         query.addField(FIELD1, FUZZY1, mod);
     }
 
     @Override
     protected String expectedWildcardedFuzzyConjunct() {
         return FIELD1 + ":(" + FUZZY1 + " " + FUZZY1 + "* " + FUZZY1 + "~0.7)";
     }
 
     @Override
     protected String expectedWildcardedFuzzyDisjunct() {
         return FIELD1 + ":(" + FUZZY1 + " " + FUZZY1 + "* " + FUZZY1 + "~0.7)";
     }
     
 
     @Override
     protected void applyWildcardedSplit(LuceneQuery query, QueryModifier mod) {
         query.addField(FIELD1, WILDCARD1 + "   " + WILDCARD2, mod);
     }
 
     @Override
     protected String expectedWildcardedSplitConjunct() {
         return 
             FIELD1 + ":(" + 
                "(" + WILDCARD1 + "* " + WILDCARD1 + ")" +
                "AND (" + WILDCARD2 + "* " + WILDCARD2 + ")" +
             ")";
     }
 
     @Override
     protected String expectedWildcardedSplitDisjunct() {
         return 
             FIELD1 + ":(" + 
                "(" + WILDCARD1 + "* " + WILDCARD1 + ") " +
                "OR (" + WILDCARD2 + "* " + WILDCARD2 + ")" +
             ")";
     }
     
 
     @Override
     protected void applyWildcardedFuzzySplit(LuceneQuery query, QueryModifier mod) {
         query.addField(FIELD1, FUZZY1 + "   " + WILDCARD2, mod);
     }
 
     @Override
     protected String expectedWildcardedFuzzySplitConjunct() {
         return
             FIELD1 + ":(" + 
                 "(" + FUZZY1 + "\\ \\ \\ " + WILDCARD2 + " " +
                     FUZZY1 + "\\ \\ \\ " + WILDCARD2 + "* " +
                     FUZZY1 + "\\ \\ \\ " + WILDCARD2 + "~0.7"
                 + ") OR (" +
                     "(" + FUZZY1 + " " + FUZZY1 + "* " + FUZZY1 + "~0.7) " +
                     "AND (" + WILDCARD2 + " " + WILDCARD2 + "* " + WILDCARD2 + "~0.7)" +
                 ")^0.5"
             + ")";
     }
 
     @Override
     protected String expectedWildcardedFuzzySplitDisjunct() {
         return
             FIELD1 + ":(" + 
                 "(" + FUZZY1 + "\\ \\ \\ " + WILDCARD2 + " " +
                     FUZZY1 + "\\ \\ \\ " + WILDCARD2 + "* " +
                     FUZZY1 + "\\ \\ \\ " + WILDCARD2 + "~0.7"
                 + ") OR (" +
                     "(" + FUZZY1 + " " + FUZZY1 + "* " + FUZZY1 + "~0.7) " +
                     "OR (" + WILDCARD2 + " " + WILDCARD2 + "* " + WILDCARD2 + "~0.7)" +
                 ")^0.5"
             + ")";
     }
 
 }
