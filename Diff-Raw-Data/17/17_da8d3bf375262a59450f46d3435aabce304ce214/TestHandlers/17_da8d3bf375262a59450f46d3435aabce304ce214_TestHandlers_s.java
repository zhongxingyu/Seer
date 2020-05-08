 package org.ow2.chameleon.everest.ipojo.test;
 
 import org.apache.felix.ipojo.HandlerFactory;
 import org.ow2.chameleon.everest.filters.RelationFilters;
 import org.ow2.chameleon.everest.services.IllegalActionOnResourceException;
 import org.ow2.chameleon.everest.services.Resource;
 import org.ow2.chameleon.everest.services.ResourceMetadata;
 import org.ow2.chameleon.everest.services.ResourceNotFoundException;
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.ow2.chameleon.everest.ipojo.test.ResourceAssert.assertThatResource;
 import static org.ow2.chameleon.everest.services.Action.READ;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.osgi.framework.Constants.SERVICE_ID;
 
 /**
  * Test /ipojo/factory and sons
  */
 public class TestHandlers extends EverestIpojoTestCommon {
 
     /**
      * The names of all the iPOJO core handlers.
      */
     public static final String[] CORE_HANDLERS = {"properties", "provides", "controller", "callback", "architecture", "requires"};
 
     /**
      * Read /ipojo/handler
      */
     @Test
     public void testReadHandlers() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/handler");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         // Metadata should be empty
         assertThat(r.getMetadata()).isEmpty();
         // Resource should have relations to /ipojo/handler/$IPOJO and /ipojo/handler/foo.bar
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("handlers[" + IPOJO + "]"),
                 RelationFilters.hasHref("/ipojo/handler/" + IPOJO)));
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("handlers[foo.bar]"),
                 RelationFilters.hasHref("/ipojo/handler/foo.bar")));
     }
 
     /**
      * Read /ipojo/handler/$IPOJO
      */
     @Test
     public void testReadIpojoHandlers() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/handler/" + IPOJO);
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         // Metadata should be empty
         assertThat(r.getMetadata()).isEmpty();
         // Resource should have relations to /ipojo/handler/$IPOJO/$name
         for (String name : CORE_HANDLERS) {
             assertThatResource(r).hasRelation(RelationFilters.and(
                     RelationFilters.hasAction(READ),
                    RelationFilters.hasName("handler[" + name + "]"),
                     RelationFilters.hasHref("/ipojo/handler/" + IPOJO + "/" + name)));
         }
     }
 
     /**
      * Read /ipojo/handler/foo.bar
      */
     @Test
     public void testReadFooBarHandlers() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/handler/foo.bar");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         // Metadata should be empty
         assertThat(r.getMetadata()).isEmpty();
         // Resource should have relations to /ipojo/handler/foo.bar/qux
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                RelationFilters.hasName("handler[qux]"),
                 RelationFilters.hasHref("/ipojo/handler/foo.bar/qux")));
     }
 
     /**
      * Read /ipojo/handler/$IPOJO/$name
      */
     @Test
     public void testReadEachIpojoHandler() throws ResourceNotFoundException, IllegalActionOnResourceException {
         for (String name : CORE_HANDLERS) {
             Resource r = read("/ipojo/handler/" + IPOJO + "/" + name);
             // Resource should be observable
             assertThat(r.isObservable()).isTrue();
             // Check namespace/name, state, missing handlers
             ResourceMetadata m = r.getMetadata();
             assertThat(m.get("namespace")).isEqualTo(IPOJO);
             assertThat(m.get("name")).isEqualTo(name);
             assertThat(m.get("state")).isEqualTo("valid");
             assertThat(m.get("missingHandlers", List.class)).isEmpty();
             //TODO Check more metadata, as soon as more metadata are provided...
             // Check adaptation
             assertThat(r.adaptTo(HandlerFactory.class)).isSameAs(getHandlerFactory(IPOJO, name));
             // Resource should have relations to the iPOJO bundle
             assertThatResource(r).hasRelation(RelationFilters.and(
                     RelationFilters.hasAction(READ),
                     RelationFilters.hasName("bundle"),
                     RelationFilters.hasHref("/osgi/bundles/" + ipojoBundle.getBundleId())));
             // Check relation on HandlerFactory service
             assertThatResource(r).hasRelation(RelationFilters.and(
                     RelationFilters.hasName("service"),
                     RelationFilters.hasAction(READ),
                     RelationFilters.hasHref("/osgi/services/" + getHandlerFactoryReference(IPOJO, name).getProperty(SERVICE_ID))));
             //TODO Check more relations, as soon as more relations are provided...
         }
     }
 
     /**
      * Read /ipojo/handler/foo.bar/qux
      */
     @Test
     public void testReadFooBarQuxHandler() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/handler/foo.bar/qux");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         // Check namespace/name, state, missing handlers
         ResourceMetadata m = r.getMetadata();
         assertThat(m.get("namespace")).isEqualTo("foo.bar");
         assertThat(m.get("name")).isEqualTo("qux");
         assertThat(m.get("state")).isEqualTo("valid");
         assertThat(m.get("missingHandlers", List.class)).isEmpty();
         //TODO Check more metadata, as soon as more metadata are provided...
         // Resource should have relations to the test bundle
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("bundle"),
                 RelationFilters.hasHref("/osgi/bundles/" + testBundle.getBundleId())));
         // Check relation on HandlerFactory service
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasName("service"),
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/osgi/services/" + getHandlerFactoryReference("foo.bar", "qux").getProperty(SERVICE_ID))));
         // Resource should have relations to the required bundles
         for (String name : CORE_HANDLERS) {
             System.out.println("Checking handler: " + name);
             assertThatResource(r).hasRelation(RelationFilters.and(
                     RelationFilters.hasAction(READ),
                     RelationFilters.hasName("requiredHandler[" + IPOJO + ":" + name + "]"),
                     RelationFilters.hasHref("/ipojo/handler/" + IPOJO + "/" + name)));
         }
         //TODO Check more relations, as soon as more relations are provided...
     }
 
 }
