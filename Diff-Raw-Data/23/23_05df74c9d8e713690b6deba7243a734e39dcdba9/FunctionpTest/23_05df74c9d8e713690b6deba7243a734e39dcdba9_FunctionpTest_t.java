 package temmental2;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class FunctionpTest extends AbstractTestElement {
 
 	@Test
 	public void testFunctionCharAtTransform() throws TemplateException, NoSuchMethodException, SecurityException {
 		Functionp f = functionp(identifier("'charat", "-:l1:c2"), list(2), identifier("$text", "-:l1:c2"));
 		
 		Method mcharat = String.class.getDeclaredMethod("charAt", int.class);
 		
 		populateTransform("charat", mcharat);
 		
 		populateModel("text", "Something...");
 		
 		assertEquals('m', f.writeObject(transforms, model, null));
 	}
 	
 	
 	@Test
 	public void testFunctionCharAtMethod() throws TemplateException, NoSuchMethodException, SecurityException {
 		Functionp f = functionp(identifier("'charat", "-:l1:c2"), list(2), identifier("$text", "-:l1:c2"));
 		
 		Method mcharat = String.class.getDeclaredMethod("charAt", int.class);
 					
 		populateTransform("charat", mcharat);
 		
 		populateModel("text", "Something...");
 		
 		assertEquals('m', f.writeObject(transforms, model, null));
 	}
 	
 	@Test
 	public void testFunctionCharAtMethodInvalidInput() throws TemplateException, NoSuchMethodException, SecurityException {
 		Functionp f = functionp(identifier("'charat", "-:l1:c2"), list(2), identifier("$text", "-:l1:c2"));
 		
 		Method mcharat = String.class.getDeclaredMethod("charAt", int.class);
 					
 		populateTransform("charat", mcharat);
 		
 		populateModel("text", 1234);
 		
 		try {
 			f.writeObject(transforms, model, null);
 			fail("An exception must be raised");
 		} catch (TemplateException e) {
 			assertEquals("Unable to render '…:'charat' at position '-:l1:c2'. The function charat expects java.lang.String. It receives java.lang.Integer.", e.getMessage());
 		}
 		
 		
 	}
 	
 	@Test
 	public void testFunctionCharAtMethodWrongNumberOfParameter() throws TemplateException, NoSuchMethodException, SecurityException {
 		Functionp f = functionp(identifier("'charat", "-:l1:c2"), list(3, 3), identifier("$text", "-:l1:c2"));
 		
 		Method mcharat = String.class.getDeclaredMethod("charAt", int.class);
 					
 		populateTransform("charat", mcharat);
 		
 		populateModel("text", "Something...");
 		
 		try {
 			System.err.println(f.writeObject(transforms, model, null));
 			fail("An exception must be raised.");
 		} catch (TemplateException e) {
 			assertEquals("Unable to render '…:'charat' at position '-:l1:c2'. The function charat expects 1 init-parameter(s) but receives 2 init-parameter(s).", e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testFunctionCharAtMethodInvalidInitParameterType() throws TemplateException, NoSuchMethodException, SecurityException {
 		Functionp f = functionp(identifier("'charat", "-:l1:c2"), list("z"), identifier("$text", "-:l1:c2"));
 		
 		Method mcharat = String.class.getDeclaredMethod("charAt", int.class);
 					
 		populateTransform("charat", mcharat);
 		
 		populateModel("text", "Something...");
 		
 		try {
 			f.writeObject(transforms, model, null);
 			fail("An exception must be raised");
 		} catch (TemplateException e) {
 			assertEquals("Unable to render '…:'charat' at position '-:l1:c2'. The function charat expects int for parameter #1. It receives java.lang.String.", e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testFunctionSubstrMethod() throws TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		Functionp f = functionp(identifier("'substr", "-:l1:c2"), list(2, 5), identifier("$text", "-:l1:c2"));
 		
 		Method msubstr = String.class.getDeclaredMethod("substring", int.class, int.class);
 					
 		populateTransform("substr", msubstr);
 		
 		populateModel("text", "Something...");
 		
 		assertEquals("met", f.writeObject(transforms, model, null));
 	}
 	
 	@Test
 	public void testFunctionSubstrTransform() throws TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		Functionp f = functionp(identifier("'substr", "-:l1:c2"), list(2, 5), identifier("$text", "-:l1:c2"));
 		
 		Transform<Integer[], Transform<String, String>> tsubstr = new Transform<Integer[], Transform<String,String>>() {
 			public Transform<String,String> apply(final Integer[] values) throws TemplateException {
 				return new Transform<String, String>() {
 					public String apply(String value) throws TemplateException {
 						return value.substring(values[0], values[1]);
 					}
 				};
 			}
 		};
 		populateTransform("substr", tsubstr);
 		
 		populateModel("text", "Something...");
 		
 		assertEquals("met", f.writeObject(transforms, model, null));
 	}
 	
 	@Test
 	public void testFunctionSubstrTransformBadInitParameterType() throws TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		Functionp f = functionp(identifier("'substr", "-:l1:c2"), list("2", "5"), identifier("$text", "-:l1:c2"));
 		
 		Transform<Integer[], Transform<String, String>> tsubstr = new Transform<Integer[], Transform<String,String>>() {
 			public Transform<String,String> apply(final Integer[] values) throws TemplateException {
 				return new Transform<String, String>() {
 					public String apply(String value) throws TemplateException {
 						return value.substring(values[0], values[1]);
 					}
 				};
 			}
 		};
 		populateTransform("substr", tsubstr);
 		
 		populateModel("text", "Something...");
 		
 		try {
 			f.writeObject(transforms, model, null);
 			fail("An exception must be raised");
 		} catch (TemplateException e) {
 			assertEquals("Unable to render '…:'substr' at position '-:l1:c2'. The function substr expects java.lang.Integer[] for parameter #1. It receives java.lang.String[].", e.getMessage());
 		}
 		
 	}
 	
 	@Test
 	public void testFunctionSubstrTransformBadInputType() throws TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		Functionp f = functionp(identifier("'substr", "-:l1:c2"), list(2, 5), identifier("$text", "-:l1:c2"));
 		
 		Transform<Integer[], Transform<String, String>> tsubstr = new Transform<Integer[], Transform<String,String>>() {
 			public Transform<String,String> apply(final Integer[] values) throws TemplateException {
 				return new Transform<String, String>() {
 					public String apply(String value) throws TemplateException {
 						return value.substring(values[0], values[1]);
 					}
 				};
 			}
 		};
 		populateTransform("substr", tsubstr);
 		
 		populateModel("text", 123);
 		
 		try {
 			f.writeObject(transforms, model, null);
 			fail("An exception must be raised");
 		} catch (TemplateException e) {
 			assertEquals("Unable to render '…:'substr' at position '-:l1:c2'. The function substr expects java.lang.String. It receives java.lang.Integer.", e.getMessage());
 		}
 		
 	}
 	
 	@Test
 	public void testFunctionIsDigit() throws TemplateException, NoSuchMethodException, SecurityException {
 		Functionp f = functionp(identifier("'indexOf", "-:l1:c2"), list('m'), identifier("$text", "-:l1:c2"));
 		
 		Method indexOf = String.class.getDeclaredMethod("indexOf", int.class);
 		
 		populateTransform("indexOf", indexOf);
 		
 		populateModel("text", "Something...");
 		
 		assertEquals(2, f.writeObject(transforms, model, null));
 	}
 	
 }
