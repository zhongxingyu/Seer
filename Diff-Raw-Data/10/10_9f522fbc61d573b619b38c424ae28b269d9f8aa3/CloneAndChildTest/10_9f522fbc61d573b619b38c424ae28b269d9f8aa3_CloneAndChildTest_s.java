 package chameleon.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.Test;
 
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.namespace.Namespace;
 import chameleon.input.ParseException;
 import chameleon.test.provider.ElementProvider;
 import chameleon.test.provider.ModelProvider;
 
 /**
  * A test class for the clone and children methods of elements. It test all elements
  * in the namespaces of its namespace provider.
  * 
  * @author Marko van Dooren
  */
 public class CloneAndChildTest extends ModelTest {
 
 
 
 	/**
 	 * Create a new clone a child tester with the given model provider and namespace provider.
 	 * @throws IOException 
 	 * @throws ParseException 
 	 */
  /*@
    @ public behavior
    @
    @ pre provider != null;
    @ pre namespaceProvider != null;
    @
    @ post modelProvider() == provider;
    @ post namespaceProvider() == namespaceProvider;
    @*/
 	public CloneAndChildTest(ModelProvider provider, ElementProvider<Namespace> namespaceProvider) throws ParseException, IOException {
 		super(provider);
 		_namespaceProvider = namespaceProvider;
 	}
 	
 	private ElementProvider<Namespace> _namespaceProvider;
 
 	public ElementProvider<Namespace> namespaceProvider() {
 		return _namespaceProvider;
 	}
 	
 	@Test
 	public void testClone() throws LookupException {
 		for(Namespace namespace: namespaceProvider().elements(language())) {
 			assertTrue(namespace != null);
 		  for(Element element : namespace.descendants()) {
 		  	test(element);
 		  }
 		}
 	}
 
 	/**
 	 * Test the clone method of the given element.
 	 * 
 	 * The test fails if the clone method modifies the given element.
    * The test fails if the element has null as its one of its children.
    * The test fails if the clone does not have the same amount of children as
    * the given element.
    * The test fails if the element is derived. Derived element should never be reachable
    * from the model through the lexical navigation methods of Element.
    * The test fails if the clone is null.
    * The test fails if the clone has null as one of its children.  
 	 */
 	private void test(Element element) {
 		String msg = "element type:"+element.getClass().getName();
 		assertFalse(element.isDerived());
 		List<Element> children = element.children();
 		assertNotNull(msg,children);
		assertFalse(msg,children.contains(null));
 		Element clone = element.clone();
 		assertNotNull(msg,clone);
 		List<Element> clonedChildren = clone.children();
 		List<Element> newChildren = element.children();
 		assertNotNull(msg,clonedChildren);
 		assertFalse(msg,clonedChildren.contains(null));
 		assertEquals(msg,children.size(), newChildren.size());
 		assertEquals(msg,children, newChildren);
 		assertEquals(msg,children.size(), clonedChildren.size());
 	}
 	
 }
