 package chameleon.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Test;
 import org.rejuse.association.Association;
 import org.rejuse.predicate.SafePredicate;
 
 import chameleon.core.element.Element;
 import chameleon.core.element.ElementImpl;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.namespace.Namespace;
 import chameleon.core.namespace.RootNamespace;
 import chameleon.input.ParseException;
 import chameleon.test.provider.ElementProvider;
 import chameleon.test.provider.ModelProvider;
 
 /**
  * A test class for the the children methods of elements, based on reflection.
  * 
  * @author Nelis Boucke
  * @author Marko van Dooren
  */
 public class ChildrenTest extends ModelTest {
 
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
 	public ChildrenTest(ModelProvider provider, ElementProvider<Namespace> namespaceProvider) throws ParseException, IOException {
 		super(provider);
 		_namespaceProvider = namespaceProvider;
 	}
 	
 	private Map<Class<? extends Element>,List<String>> _excludedFieldNames = new HashMap<Class<? extends Element>,List<String>>();
 	
 	public void excludeFieldName(Class<? extends Element> type, String fieldName) {
 		List<String> list = _excludedFieldNames.get(type);
 		if(list == null) {
 			list = new ArrayList<String>();
 			_excludedFieldNames.put(type, list);
 		}
 		list.add(fieldName);
 	}
 	
 	public List<String> excludedFieldNames(Class<? extends Element> type) {
 		return _excludedFieldNames.get(type);
 	}
 	
 	private ElementProvider<Namespace> _namespaceProvider;
 
 	public ElementProvider<Namespace> namespaceProvider() {
 		return _namespaceProvider;
 	}
 	
 	@Test
 	public void testChildren() throws LookupException {
 		for(Namespace<?> namespace: namespaceProvider().elements(language())) {
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
 		
 		Set<Element> children = new HashSet<Element>();
 		children.addAll(element.children());
 		
 		assertNotNull(msg,children);
 		assertFalse(msg,children.contains(null));
 		
 		Set<Element> reflchildren = new HashSet<Element>();
 		
 		Class<? extends Element>  currentClass = element.getClass();
 		
 		ArrayList<Field> fields = getAllFieldsTillClass(currentClass, ElementImpl.class);
 		
 		for (Field field : fields) {
 			Object content = getFieldValue(field, element);
 			if(content != null) {
 				try{
 					Association<Element,Element> asso = (Association<Element,Element>) content;
 					List<Element> elms = asso.getOtherEnds();
 					for (Element child : elms) {
 						if(!child.getClass().equals(RootNamespace.class)){
 							reflchildren.add(child);
 						}
 					}
 					// debug code
 //					System.out.println("Class:"+currentClass+" Field: " + field.getName());
 					
 //					for (Element element2 : elms) {
 //						System.out.println(element2);
 //					}
 					// end debug code
 				}catch(ClassCastException exc){}
 			}
 		}
 		assertEquals(msg,reflchildren,children);
 		//assertTrue(msg,reflchildren.containsAll(implchildren) && implchildren.containsAll(reflchildren));
 	}
 
 	public ArrayList<Field> getAllFieldsTillClass(Class currentClass, Class till){
 		ArrayList<Field> arrayList = new ArrayList<Field>();
 		addAllFieldsTillClass(currentClass, till, arrayList);
 		return arrayList;
 	}
 
 	public void addAllFieldsTillClass(final Class currentClass, Class till, Collection<Field> accumulator){
 		List<Field> fieldList = new ArrayList<Field>(Arrays.asList(currentClass.getDeclaredFields()));
 		new SafePredicate<Field>() {
 
 			@Override
 			public boolean eval(Field field) {
 				final String fieldName = field.getName();
 				return new SafePredicate<String>() {
 
 					@Override
 					public boolean eval(String excludedField) {
 						return !excludedField.equals(fieldName);
 					}
 					
 				}.forall(_excludedFieldNames.get(currentClass));
 			}
 		}.filter(fieldList);
 		accumulator.addAll(fieldList);
     if(currentClass != till) {
   		Class superClass = currentClass.getSuperclass();
   		accumulator.addAll(getAllFieldsTillClass(superClass, till));
     }
 	}
 	
 	public Object getFieldValue(Field field, Object element){
 		field.setAccessible(true);
 		try {
 			field.setAccessible(true);
 			return field.get(element);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {	
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
 
