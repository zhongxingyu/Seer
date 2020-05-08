 package org.rulemaker.parser;
 
 import static org.junit.Assert.*;
 
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
		Term expectedTerm = termsList.get(0);
		assertEquals("documentType", expectedTerm.getIdentifier());
		assertEquals(Term.TermType.NUMBER, expectedTerm.getExpressionType());
		assertEquals("11", expectedTerm.getExpressionValue());
 	}
 
 }
