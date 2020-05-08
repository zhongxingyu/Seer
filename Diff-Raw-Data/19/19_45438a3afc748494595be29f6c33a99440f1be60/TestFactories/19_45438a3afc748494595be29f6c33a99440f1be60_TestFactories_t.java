 package org.apache.felix.ipojo.everest.ipojo.test;
 
 import org.apache.felix.ipojo.Factory;
 import org.apache.felix.ipojo.everest.filters.RelationFilters;
 import org.apache.felix.ipojo.everest.impl.DefaultRequest;
 import org.apache.felix.ipojo.everest.ipojo.test.b1.FooService;
 import org.apache.felix.ipojo.everest.services.*;
 import org.junit.Before;
 import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import static org.apache.felix.ipojo.everest.ipojo.test.ResourceAssert.assertThatResource;
 import static org.apache.felix.ipojo.everest.services.Action.*;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.junit.Assert.fail;
 import static org.osgi.framework.Constants.SERVICE_ID;
 
 /**
  * Test /ipojo/factory and sons
  */
@ExamReactorStrategy(PerMethod.class)
 public class TestFactories extends EverestIpojoTestCommon {
 
     /**
      * Read /ipojo/factory
      */
     @Test
     public void testReadFactories() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/factory");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         // Metadata should be empty
         assertThat(r.getMetadata()).isEmpty();
         // Resource should have relations to /ipojo/factory/Foo and /ipojo/factory/$BAR
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("factories[Foo]"),
                 RelationFilters.hasHref("/ipojo/factory/Foo")));
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("factories[" + BAR + "]"),
                 RelationFilters.hasHref("/ipojo/factory/" + BAR)));
     }
 
     /**
      * Read /ipojo/factory/Foo
      */
     @Test
     public void testReadFooFactories() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/factory/Foo");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         // Metadata should be empty
         assertThat(r.getMetadata()).isEmpty();
         // There should be 1 sub-resource
         assertThat(r.getResources()).hasSize(1);
         // Resource should have relations to /ipojo/factory/Foo/1.2.3.foo
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("factory[1.2.3.foo]"),
                 RelationFilters.hasHref("/ipojo/factory/Foo/1.2.3.foo")));
     }
 
     /**
      * Read /ipojo/factory/$BAR
      */
     @Test
     public void testReadBarFactories() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/factory/" + BAR);
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         // Metadata should be empty
         assertThat(r.getMetadata()).isEmpty();
         // There should be 2 sub-resources
         assertThat(r.getResources()).hasSize(2);
         // Resource should have relations to /ipojo/factory/$BAR/null and /ipojo/factory/$BAR/2.0.0
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/ipojo/factory/" + BAR + "/null")));
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/ipojo/factory/" + BAR + "/2.0.0")));
     }
 
     /**
      * Read /ipojo/factory/Foo/1.2.3.foo
      */
     @Test
     public void testReadFoo123Factory() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/factory/Foo/1.2.3.foo");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         ResourceMetadata m = r.getMetadata();
         // Check name, version , class name
         assertThat(m.get("name")).isEqualTo("Foo");
         assertThat(m.get("version")).isEqualTo("1.2.3.foo");
         assertThat(m.get("className")).isEqualTo("org.apache.felix.ipojo.everest.ipojo.test.b1.FooProviderImpl");
         // Check state
         assertThat(m.get("state")).isEqualTo("valid");
         // Check missing handler
         assertThat(m.get("missingHandlers", List.class)).isEmpty();
         //TODO Check more metadata, as soon as more metadata are provided...
         // Check adaptation
         assertThat(r.adaptTo(Factory.class)).isSameAs(getFactory("Foo", "1.2.3.foo"));
         // Check relation on declaring bundle
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasName("bundle"),
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/osgi/bundles/" + testBundle.getBundleId())));
         // Check relations on required handlers
         for (String handlerName : new String[]{"properties", "provides", "architecture"}) {
             assertThatResource(r).hasRelation(RelationFilters.and(
                     RelationFilters.hasAction(READ),
                     RelationFilters.hasName(String.format("requiredHandler[%s:%s]", IPOJO, handlerName)),
                     RelationFilters.hasHref("/ipojo/handler/" + IPOJO + "/" + handlerName)));
         }
         // Check relation on Factory service
         ServiceReference<Factory> ref = getFactoryReference("Foo", "1.2.3.foo");
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasName("service"),
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/osgi/services/" + ref.getProperty(SERVICE_ID))));
         // Check relation on declared instance
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasName("instance[DeclaredFoo123]"),
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/ipojo/instance/DeclaredFoo123")));
         //TODO Check more relations, as soon as more relations are provided...
     }
 
     /**
      * Read /ipojo/factory/$BAR/null
      */
     @Test
     public void testReadBarNullFactory() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/factory/" + BAR + "/null");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         ResourceMetadata m = r.getMetadata();
         // Check name, version , class name
         assertThat(m.get("name")).isEqualTo(BAR);
         assertThat(m.get("version")).isEqualTo(null);
         assertThat(m.get("className")).isEqualTo(BAR);
         // Check state
         assertThat(m.get("state")).isEqualTo("valid");
         // Check missing handler
         assertThat(m.get("missingHandlers", List.class)).isEmpty();
         //TODO Check more, as soon as more metadata are provided...
         // Check adaptation
         assertThat(r.adaptTo(Factory.class)).isSameAs(getFactory(BAR, null));
         // Check relation on declaring bundle
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasName("bundle"),
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/osgi/bundles/" + testBundle.getBundleId())));
         // Check relations on required handlers
         for (String handlerName : new String[]{"properties", "provides", "requires", "callback", "architecture"}) {
             assertThatResource(r).hasRelation(RelationFilters.and(
                     RelationFilters.hasAction(READ),
                     RelationFilters.hasName(String.format("requiredHandler[%s:%s]", IPOJO, handlerName)),
                     RelationFilters.hasHref("/ipojo/handler/" + IPOJO + "/" + handlerName)));
         }
         // Check relation on Factory service
         ServiceReference<Factory> ref = getFactoryReference(BAR, null);
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasName("service"),
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/osgi/services/" + ref.getProperty(SERVICE_ID))));
         // TODO Check relation on declared instance, but name is unknown
         //TODO Check more relations, as soon as more relations are provided...
     }
 
     /**
      * Read /ipojo/factory/$BAR/2.0.0
      */
     @Test
     public void testReadBar2Factory() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = read("/ipojo/factory/" + BAR + "/2.0.0");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         ResourceMetadata m = r.getMetadata();
         // Check name, version , class name
         assertThat(m.get("name")).isEqualTo(BAR);
         assertThat(m.get("version")).isEqualTo("2.0.0");
         assertThat(m.get("className")).isEqualTo(BAR_2);
         // Check state
         assertThat(m.get("state")).isEqualTo("valid");
         // Check missing handler
         assertThat(m.get("missingHandlers", List.class)).isEmpty();
         //TODO Check more, as soon as more metadata are provided...
         // Check adaptation
         assertThat(r.adaptTo(Factory.class)).isSameAs(getFactory(BAR, "2.0.0"));
         // Check relation on declaring bundle
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasName("bundle"),
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/osgi/bundles/" + testBundle2.getBundleId())));
         // Check relations on required handlers
         for (String handlerName : new String[]{"properties", "provides", "requires", "architecture"}) {
             assertThatResource(r).hasRelation(RelationFilters.and(
                     RelationFilters.hasAction(READ),
                     RelationFilters.hasName(String.format("requiredHandler[%s:%s]", IPOJO, handlerName)),
                     RelationFilters.hasHref("/ipojo/handler/" + IPOJO + "/" + handlerName)));
         }
         // Check relation on Factory service
         ServiceReference<Factory> ref = getFactoryReference(BAR, "2.0.0");
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasName("service"),
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasHref("/osgi/services/" + ref.getProperty(SERVICE_ID))));
         // TODO Check relation on declared instance, but name is unknown
         //TODO Check more relations, as soon as more relations are provided...
     }
 
     /**
      * Create /ipojo/factory/Foo/1.2.3.foo
      */
     @Test
     public void testCreateFoo123Factory() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = everest.process(new DefaultRequest(CREATE, Path.from("/ipojo/factory/Foo/1.2.3.foo"), null));
         // Check the path of the returned resource
         assertThat(r.getPath().toString()).startsWith("/ipojo/instance/Foo-");
         // Resource should be observable
         assertThat(r.isObservable()).isTrue();
         // Check name, factory
         ResourceMetadata m = r.getMetadata();
         String name = m.get("name", String.class);
         assertThat(name).startsWith("Foo-");
         assertThat(m.get("factory.name", String.class)).isEqualTo("Foo");
         assertThat(m.get("factory.version", String.class)).isEqualTo("1.2.3.foo");
         // Check default configuration has taken place.
         ServiceReference ref = ipojoHelper.getServiceReferenceByName(FooService.class.getName(), name);
         assertThat(ref).isNotNull();
         assertThat(ref.getProperty("fooCounter")).isEqualTo(0);
         FooService foo = (FooService) context.getService(ref);
         assertThat(foo.getFoo()).isEqualTo("0");
         // Check relation from instance to factory
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("factory"),
                 RelationFilters.hasHref("/ipojo/factory/Foo/1.2.3.foo")));
         // Check relation from factory to instance
         assertThatResource(read("/ipojo/factory/Foo/1.2.3.foo")).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("instance[" + name + "]"),
                 RelationFilters.hasHref(r)));
     }
 
     /**
      * Create /ipojo/factory/Foo/1.2.3.foo with parameters
      */
     @Test
     public void testCreateFoo123FactoryWithConfiguration() throws ResourceNotFoundException, IllegalActionOnResourceException {
         // Request creation on factory Foo resource, with configuration
         Map<String, Object> config = new LinkedHashMap<String, Object>();
         config.put("instance.name", "ConfiguredFoo");
         config.put("fooPrefix", "__configured");
         config.put("fooCounter", 666);
         Resource r = everest.process(new DefaultRequest(CREATE, Path.from("/ipojo/factory/Foo/1.2.3.foo"), config));
         assertThat(r).isNotNull();
         // Check name, factory
         ResourceMetadata m = r.getMetadata();
         assertThat(m.get("name", String.class)).isEqualTo("ConfiguredFoo");
         assertThat(m.get("factory.name", String.class)).isEqualTo("Foo");
         assertThat(m.get("factory.version", String.class)).isEqualTo("1.2.3.foo");
         // Check configuration has been taken into consideration.
         ServiceReference ref = ipojoHelper.getServiceReferenceByName(FooService.class.getName(), "ConfiguredFoo");
         assertThat(ref).isNotNull();
         assertThat(ref.getProperty("fooCounter")).isEqualTo(666);
         FooService foo = (FooService) context.getService(ref);
         assertThat(foo.getFoo()).isEqualTo("__configured666");
         // Check relation from instance to factory
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("factory"),
                 RelationFilters.hasHref("/ipojo/factory/Foo/1.2.3.foo")));
         // Check relation from factory to instance
         assertThatResource(read("/ipojo/factory/Foo/1.2.3.foo")).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("instance[ConfiguredFoo]"),
                 RelationFilters.hasHref(r)));
     }
 
     /**
      * Create /ipojo/factory/Foo/1.2.3.foo with bad parameters
      */
     @Test(expected = IllegalActionOnResourceException.class)
     public void testCreateFoo123FactoryWithBadConfiguration() throws ResourceNotFoundException, IllegalActionOnResourceException {
         // Request creation on factory Foo resource, with bad configuration
         Map<String, Object> badConfig = new LinkedHashMap<String, Object>();
         badConfig.put("instance.name", "BadConfiguredFoo");
         badConfig.put("fooPrefix", "__bad_configured");
         badConfig.put("fooCounter", "notAnInteger");
         everest.process(new DefaultRequest(CREATE, Path.from("/ipojo/factory/Foo/1.2.3.foo"), badConfig));
         // Failed!
     }
 
     /**
      * Create /ipojo/factory/$BAR/null
      */
     @Test
     public void testCreateBarNullFactory() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = everest.process(new DefaultRequest(CREATE, Path.from("/ipojo/factory/" + BAR + "/null"), null));
         // Check name, factory
         ResourceMetadata m = r.getMetadata();
         String name = m.get("name", String.class);
         assertThat(name).startsWith(BAR + "-");
         assertThat(m.get("factory.name", String.class)).isEqualTo(BAR);
         assertThat(m.get("factory.version", String.class)).isNull();
         // Check relation from instance to factory
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("factory"),
                 RelationFilters.hasHref("/ipojo/factory/" + BAR + "/null")));
         // Check relation from factory to instance
         assertThatResource(read("/ipojo/factory/" + BAR + "/null")).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("instance[" + name + "]"),
                 RelationFilters.hasHref(r)));
     }
 
     /**
      * Create /ipojo/factory/$BAR/2.0.0
      */
     @Test
     public void testCreateOnBar2Factory() throws ResourceNotFoundException, IllegalActionOnResourceException {
        // First create an instance with the null factory
        everest.process(new DefaultRequest(CREATE, Path.from("/ipojo/factory/" + BAR + "/null"), null));
        // Then craete an instance with factory version 2.0.0...
         Resource r = everest.process(new DefaultRequest(CREATE, Path.from("/ipojo/factory/" + BAR + "/2.0.0"), null));
         // Check name, factory
         ResourceMetadata m = r.getMetadata();
         String name = m.get("name", String.class);
         assertThat(name).startsWith(BAR + "-");
         assertThat(name).endsWith("-2.0.0");
         assertThat(m.get("factory.name", String.class)).isEqualTo(BAR);
         assertThat(m.get("factory.version", String.class)).isEqualTo("2.0.0");
         // Check relation from instance to factory
         assertThatResource(r).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("factory"),
                 RelationFilters.hasHref("/ipojo/factory/" + BAR + "/2.0.0")));
         // Check relation from factory to instance
         assertThatResource(read("/ipojo/factory/" + BAR + "/2.0.0")).hasRelation(RelationFilters.and(
                 RelationFilters.hasAction(READ),
                 RelationFilters.hasName("instance[" + name + "]"),
                 RelationFilters.hasHref(r)));
     }
 
     // ========================================================================
     // Destructive tests that MUST be executed at the very end of this suite!!!
 
     /**
      * Test that DELETE action on resource representing Bar factory 2.0.0 has the expected behavior.
      */
     @Test
     public void testDeleteBar2Factory() throws ResourceNotFoundException, IllegalActionOnResourceException, InvalidSyntaxException {
         // Check Factory refs before
         Collection<ServiceReference<Factory>> refs1 = context.getServiceReferences(Factory.class, "(factory.name=" + BAR + ")");
         assertThat(refs1).hasSize(2);
         // Delete factory Bar v2.0.0
         Resource r = everest.process(new DefaultRequest(DELETE, Path.from("/ipojo/factory/" + BAR + "/2.0.0"), null));
         // Check that the result represents the deleted factory
         assertThat(r).isNotNull();
         ResourceMetadata m = r.getMetadata();
         assertThat(m.get("name")).isEqualTo(BAR);
         assertThat(m.get("version")).isEqualTo("2.0.0");
         assertThat(m.get("className")).isEqualTo(BAR_2);
         // Check Factory refs after
         Collection<ServiceReference<Factory>> refs2 = context.getServiceReferences(Factory.class, "(factory.name=" + BAR + ")");
         assertThat(refs2).hasSize(1);
         // Check that accessing Bar version null still works
         assertThat(read("/ipojo/factory/" + BAR + "/null")).isNotNull();
         // Check that accessing the deleted resource (Bar version "2.0.0") fails miserably
         try {
             read("/ipojo/factory/" + BAR + "/2.0.0");
             fail("/ipojo/factory/" + BAR + "/2.0.0 should not exist anymore");
         } catch (ResourceNotFoundException e) {
             // Ok : that's normal!
         }
     }
 
 }
