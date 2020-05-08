 package org.rulemaker.parser;
 
 import static org.junit.Assert.*;
 
import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 import org.rulemaker.model.Term;
 
 public class ConditionParserTest extends BaseParserTest {
 	
 	@Test
 	public void shouldRecognizeAValidSingleTermCondition() throws Exception {
 		RulesParserParser parser = this.buildParser("(documentType=11)");
 		parser.condition();
 		List<Term> termsList = parser.getCurrentConditionTermList();
 		assertEquals("One term expected", 1, termsList.size());
		Term obtainedTerm = termsList.get(0);
		Term expectedTerm = new Term("documentType", Term.TermType.NUMBER, "11");
		assertEquals(expectedTerm, obtainedTerm);
	}
	
	@Test
	public void shouldRecognizeMultipleTermsConditions() throws Exception {
		Term [] expectedTermsArray = {new Term("documentType", Term.TermType.NUMBER, "11"), 
				                      new Term("targetType", Term.TermType.IDENTIFIER, "X"),
				                      new Term("value", Term.TermType.STRING, "John Doe")};
		RulesParserParser parser = this.buildParser("(documentType=11, targetType=X, value='John Doe')");
		parser.condition();
		List<Term> termsList = parser.getCurrentConditionTermList();
		List<Term> expectedTermList = new ArrayList<Term>();
		expectedTermList.addAll(Arrays.asList(expectedTermsArray));
		//assertEquals(expectedTermList.get(0), termsList.get(0));
		//assertEquals(expectedTermList.get(1), termsList.get(1));
		//assertEquals(expectedTermList.get(2), termsList.get(2));
		assertEquals(expectedTermList, termsList);
 	}
 
 }
