 package transformation.rules;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import petrinet.Petrinet;
 import transformation.Rule;
 import transformation.Transformation;
 import transformation.TransformationComponent;
import data.Rule2Data;
import exceptions.GeneralPetrinetException;
 
 public class Rule7Test {
 
 	/** petrinet to transform */
	private static Petrinet nPetrinet = Rule2Data.getnPetrinet();
 	/** rule to apply */
	private static Rule rule = Rule2Data.getRule();
 
 	private static String preBefore;
 	private static String postBefore;
 	private static Transformation transformation;
 	private static String preAfter;
 	private static String postAfter;
 
 	@Test
 	public void testApplyingRule() {
 		// Because the reference stays the same after transformation
 		// nPetrinet will always equals itself, no matter what happens in
 		// transformation
 		// so we check the toString() of its pre-matrix instead
 		preBefore = nPetrinet.getPre().matrixStringOnly();
 		postBefore = nPetrinet.getPost().matrixStringOnly();
 		
 		transformation = TransformationComponent.getTransformation().transform(
 				nPetrinet, rule);
 		assertNull("There should be no morphism found", transformation);
 		
 		preAfter = nPetrinet.getPre().matrixStringOnly();
 		postAfter = nPetrinet.getPost().matrixStringOnly();
 
 		// Did the petrinet stayed untouched?
 		assertEquals(preBefore, preAfter);
 		assertEquals(postBefore, postAfter);
 	}
 
 }
