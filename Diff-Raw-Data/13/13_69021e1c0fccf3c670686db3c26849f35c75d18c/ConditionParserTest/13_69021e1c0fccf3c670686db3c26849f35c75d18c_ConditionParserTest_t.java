 package org.rulemaker.parser;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 import org.rulemaker.model.Condition;
 import org.rulemaker.model.Term;
 
 public class ConditionParserTest extends BaseParserTest {
 	
 	@Test
 	public void shouldRecognizeAnEmptyCondition() throws Exception{
 		RulesParserParser parser = this.buildParser("()");
 		parser.condition();
 		List<Term> termsList = parser.getCurrentTokenTermList();
 		assertEquals("No terms expected", 0, termsList.size());
 	}
 	
 	@Test
 	public void shouldRecognizeAValidSingleTermCondition() throws Exception {
 		RulesParserParser parser = this.buildParser("(documentType=11)");
 		parser.condition();
 		List<Term> termsList = parser.getCurrentTokenTermList();
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
 		List<Term> termsList = parser.getCurrentTokenTermList();
 		List<Term> expectedTermList = new ArrayList<Term>();
 		expectedTermList.addAll(Arrays.asList(expectedTermsArray));
 		assertEquals(expectedTermList, termsList);
 	}
 		
 	@Test
 	public void shouldRecognizeMultipleConditions() throws Exception {
 		Condition []expectedContitionsArray = {
 				new Condition(Arrays.asList(new Term []{new Term("documentType", Term.TermType.NUMBER, "11")})),
 				new Condition(Arrays.asList(new Term []{new Term("targetType", Term.TermType.IDENTIFIER, "X"),
 				                                        new Term("value", Term.TermType.STRING, "John Doe")}))
 		};
 		RulesParserParser parser = this.buildParser("(documentType=11) (targetType=X, value='John Doe')");
		parser.conditionList();
		//parser.condition();
 		List<Condition> conditionList = parser.getConditionList();
 		List<Condition> expectedConditionList = Arrays.asList(expectedContitionsArray);
 		assertEquals(expectedConditionList, conditionList);
 	}
 
 }
