 package acceptance;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TwoParameters extends AcceptanceTest {
 
 	
 	@BeforeClass
 	public static void setup() throws Exception {
 		acceptanceTest("like"                 + '\n' +
  		               "  involves"           + '\n' +
 		               "    c - Character"    + '\n' +
 		               "    d - Character"    + '\n' +
 		               "  then "              + '\n' +
		               "    " +
		               "c likes d");
                                          
 		model.setVariable("doc",    "Document.all.first");
 		model.setVariable("action", "doc.actions.first");
 		model.setVariable("p1",     "action.parameters.first");
 		model.setVariable("p2",     "action.parameters.last");
 		model.setVariable("effect", "action.effects.first");
 	}
 	
 	@Test
 	public void documentHasOneAction() {
 		model.assertEquals(1, "doc.actions.size()");
 	}
 	
 	@Test
 	public void actionIsNamedLike() {
 		model.assertEquals("like", "action.name");
 	}
 	
 	// Parameters
 	
 	@Test
 	public void actionHasTwoParameters() {
 		model.assertEquals(2, "action.parameters.size()");
 	}
 	
 	@Test
 	public void firstParameterIsC() {
 		model.assertEquals("c", "p1.name");
 	}
 	
 	@Test
 	public void firstParameterIsCharacter() {
 		model.assertTrue("CharacterParameter.isType(p1)");
 	}
 	
 	@Test
 	public void secondParameterIsD() {
 		model.assertEquals("d", "p2.name");
 	}
 	
 	@Test
 	public void secondParameterIsCharacter() {
 		model.assertTrue("CharacterParameter.isType(p2)");
 	}
 	
 	// Preconditions
 	
 	@Test
 	public void actionHasOnePreconditionSet() {
 		model.assertEquals(1, "action.preconditionSets.size()");
 	}
 	
 	@Test
 	public void preconditionSetHasNoPreconditions() {
 		model.assertEquals(0, "action.preconditionSets.first.preconditions.size()");
 	}
 	
 	// Effects
 	
 	@Test
 	public void actionHasOneEffect() {
 		model.assertEquals(1, "action.effects.size()");
 	}
 	
 	@Test
 	public void effectHasTypeWithNameLikes() {
 		model.assertEquals("likes", "effect.type.name");
 	}
 
 	@Test
 	public void effectHasTwoParameters() {
 		model.assertEquals(2, "effect.parameters.size()");
 	}
 	
 	@Test
 	public void firstEffectParameterIsC() {
 		model.assertEquals("c", "effect.parameters.first.name");
 	}
 	
 	@Test
 	public void secondEffectParameterIsD() {
 		model.assertEquals("d", "effect.parameters.last.name");
 	}
 }
