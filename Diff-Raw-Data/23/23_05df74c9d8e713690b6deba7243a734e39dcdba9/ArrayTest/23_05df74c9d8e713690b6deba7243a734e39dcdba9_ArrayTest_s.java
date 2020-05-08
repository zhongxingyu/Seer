 package temmental2;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 
 import org.junit.Test;
 
 public class ArrayTest  extends AbstractTestElement {
 
 	@Test
 	public void testArray() throws TemplateException, NoSuchMethodException, SecurityException {
 		
 		Transform<List<Integer>, Integer> add = new Transform<List<Integer>, Integer>() {
			@Override
 			public Integer apply(List<Integer> values) throws TemplateException {
 				int s = 0;
 				for (int i : values) {
 					s += i;
 				}
 				return s;
 			}
 		}; 
 		
 		Function f = function(identifier("$f", p(1, 12)),
 				array(p(7,7), identifier("$b1", p(1, 3)),identifier("$b2", p(1, 7))));
 		
 		populateTransform("somme", add);
 		
 		populateModel("f", "somme");
 		populateModel("b1", 5);
 		populateModel("b2", 8);
 		
 		assertEquals(13, f.writeObject(transforms, model, null));
 	}
 
 }
