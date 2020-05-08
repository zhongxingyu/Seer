 package jnome.test;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import chameleon.core.namespacepart.NamespacePart;
 import chameleon.core.type.Type;
 import chameleon.support.test.ExpressionTest;
 import chameleon.test.CompositeTest;
 import chameleon.test.VerificationTest;
 import chameleon.test.provider.BasicDescendantProvider;
 import chameleon.test.provider.ElementProvider;
 
 public abstract class JavaTest extends CompositeTest {
 	
 	/**
 	 * Set the log levels of Log4j. By default, nothing is changed.
 	 */
 	@Before
 	public void setLogLevels() {
		
 	}
 	
 	/**
 	 * Test getType expressions in the namespaces provided
 	 * by the namespace provider.
 	 */
 	@Test
 	public void testExpressions() throws Exception {
 		new ExpressionTest(modelProvider(), typeProvider()).testExpressionTypes();
 	}
 
 	public ElementProvider<Type> typeProvider() {
 		return new BasicDescendantProvider<Type>(namespaceProvider(), Type.class);
 	}
 
 	/**
 	 * Test the verification by invoking verify() for all namespace parts, and checking if the result is valid.
 	 */
 	@Test @Override
 	public void testVerification() throws Exception {
 	}
 
 }
