 package br.com.boltframework.scan;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import br.com.boltframework.annotation.Controller;
 import br.com.boltframework.core.ControllerMapping;
import br.com.boltframework.scan.classes.PersonController;
 import br.com.boltframework.scan.classes.User;
 import br.com.boltframework.scan.classes.UserController;
 
 public class ClassScannerTest {
 
 	private ClassScanner classScanner = ClassScanner.createInstance().withDefaultDirectoryClasses();
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testIsControllerWithValidClass() {
 		Class clazz = UserController.class;
 		boolean annotated = classScanner.isAnnotatedWithController(clazz);
 		assertTrue(annotated);
 	}
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testIsControllerWithoutValidClass() {
 		Class clazz = User.class;
 		boolean annotated = classScanner.isAnnotatedWithController(clazz);
 		assertFalse(annotated);
 	}
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testObtainControllerAnnotationByClassWithValidClass() {
 		Class clazz = UserController.class;
 		Controller controller = classScanner.obtainControllerAnnotationByClass(clazz);
 		assertNotNull(controller);
 		assertEquals("user", controller.mappedBy());
 	}
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testObtainControllerAnnotationByClassWithoutValidClass() {
 		Class clazz = User.class;
 		Controller controller = classScanner.obtainControllerAnnotationByClass(clazz);
 		assertNull(controller);
 	}
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testGetActionsByClassIfIsNotNull() {
 		Class clazz = UserController.class;
 		List<ControllerMapping> mappings = classScanner.getActionsByClass(clazz, "user");
 		assertNotNull(mappings);
 	}
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testGetActionsByClassIfHasValidSize() {
 		Class clazz = UserController.class;
 		List<ControllerMapping> mappings = classScanner.getActionsByClass(clazz, "user");
 		assertEquals(7, mappings.size());
 	}
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testGetActionsByClassIfHasValidMethodForRunBeforeAllActions() {
 		Class clazz = UserController.class;
 		List<ControllerMapping> mappings = classScanner.getActionsByClass(clazz, "user");
 		for (ControllerMapping mapping : mappings) {
 			assertNotNull(mapping.getRunBeforeAction());
 		}
 	}
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testGetActionsByClassIfHasValidMethodForRunBeforeSomeActions() {
		Class clazz = PersonController.class;
 		List<ControllerMapping> mappings = classScanner.getActionsByClass(clazz, "user");
 		for (ControllerMapping mapping : mappings) {
 			String action = mapping.getActionName();
 			if ("save".equals(action) || "update".equals(action) || "delete".equals(action)) {
 				assertNotNull(mapping.getRunBeforeAction());
 			}
 			else {
 				assertNull(mapping.getRunBeforeAction());
 			}
 		}
 	}
 
 }
