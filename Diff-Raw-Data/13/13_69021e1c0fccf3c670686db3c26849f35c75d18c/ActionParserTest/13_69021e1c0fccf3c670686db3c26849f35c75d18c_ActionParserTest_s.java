 package org.rulemaker.parser;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 import org.rulemaker.model.Action;
 import org.rulemaker.model.Term;
 
 public class ActionParserTest extends BaseParserTest {
 	
 	@Test
 	public void shouldRecognizeEmptyActionToken() throws Exception {
 		RulesParserParser parser = this.buildParser("thisAction()");
 		parser.action();
 		List<Action> actionList = parser.getActionList();
 		Action expectedAction = new Action("thisAction", new ArrayList<Term>());
 		assertEquals(expectedAction, actionList.get(0));
 	}
 
 	@Test
 	public void shouldRecognizeSingleActionToken() throws Exception {
 		RulesParserParser parser = this.buildParser("create(value=11)");
 		parser.action();
 		List<Action> actionList = parser.getActionList();
 		Action expectedAction = new Action("create", Arrays.asList(new Term []{new Term("value", Term.TermType.NUMBER, "11")}));
 		assertEquals(expectedAction, actionList.get(0));
 	}
 	
 	public void shouldRecognizeMultipleActionTokens() throws Exception {
 		RulesParserParser parser = this.buildParser("create(value=11) update(level=X, name='John Doe')");
		parser.action();
		parser.action();
 		List<Action> actionList = parser.getActionList();
 		List<Action> expectedActions = Arrays.asList(new Action("create", 
 				                                                Arrays.asList(new Term[]{new Term("value", Term.TermType.NUMBER, "11")})),
 													 new Action("update", 
 															    Arrays.asList(new Term[]{new Term("level", Term.TermType.IDENTIFIER, "X"),
 															                  new Term("name", Term.TermType.STRING, "John Doe")})));
 		assertEquals(expectedActions, actionList);
 	}
 
 }
