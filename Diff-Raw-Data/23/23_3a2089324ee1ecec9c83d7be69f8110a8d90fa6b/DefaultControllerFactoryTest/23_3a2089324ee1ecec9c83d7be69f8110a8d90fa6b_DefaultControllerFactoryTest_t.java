 package vinna;
 
 import org.junit.Before;
 import org.junit.Test;
 import vinna.controllers.Empty;
 import vinna.controllers.sub.Empty2;
 
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 
 public class DefaultControllerFactoryTest {
 
     private ControllerFactory controllerFactory;
 
     @Before
     public void setUp() {
        this.controllerFactory = new DefaultControllerFactory("vinna", "controllers");
     }
 
     @Test
     public void testUsesClassWhenProvided() {
         Object controller = controllerFactory.create(null, DummyController.class);
         assertNotNull(controller);
         assertTrue(controller instanceof DummyController);
     }
 
     @Test
     public void testUsesClassWhenProvidedEventIfIdNotNull() {
         Object controller = controllerFactory.create("there is no such class", DummyController.class);
         assertNotNull(controller);
         assertTrue(controller instanceof DummyController);
     }
 
     @Test
     public void testUsesIdAsFullyQualifiedClassName() {
         Object controller = controllerFactory.create("vinna.DefaultControllerFactoryTest$DummyController", null);
         assertNotNull(controller);
         assertTrue(controller instanceof DummyController);
     }
 
     @Test
     public void testExandPartialId() {
         // expands to {basePackage}.controllers.{controller}
         Object controller = controllerFactory.create("Empty", null);
         assertNotNull(controller);
         assertTrue(controller instanceof Empty);
     }
 
     @Test
     public void testExandPartialIdHandlingCase() {
         Object controller = controllerFactory.create("empty", null);
         assertNotNull(controller);
         assertTrue(controller instanceof Empty);
     }
 
     @Test
     public void testExandPartialIdWithASubPackage() {
         // expands to {basePackage}.controllers.{controller}
         Object controller = controllerFactory.create("sub.Empty2", null);
         assertNotNull(controller);
         assertTrue(controller instanceof Empty2);
     }
 
     @Test
     public void testExandPartialIdWithASubPackageHandlingCase() {
         Object controller = controllerFactory.create("sub.empty2", null);
         assertNotNull(controller);
         assertTrue(controller instanceof Empty2);
     }
 
     public static final class DummyController {
 
     }
 }
