 package edu.tum.lua.junit.operator;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import edu.tum.lua.operator.Operator;
 import edu.tum.lua.operator.OperatorRegistry;
 import edu.tum.lua.operator.arithmetic.AddOperator;
 
 public class OperatorRegistryTest {
 
 	@Test
 	public void test() {
 		try {
 			Operator op = OperatorRegistry.registry[20];
 			fail();
 		} catch (ArrayIndexOutOfBoundsException e) {
 		}
 		assertTrue(OperatorRegistry.registry[0] instanceof AddOperator);
 	}
 }
