 package http;
 
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 
 import static org.junit.Assert.*;
 
 /**
 * This is an abstract class that is used to test the common behaviour between the different types of property access
 * methods on an object.
  *
  * @author Karl Bennett
  */
 public abstract class AbstractMessagePropertyTest<M, P> extends AbstractPropertyProducer<P> {
 
     /**
      * A wrapper for the {@code Message} object that will be tested. It is used to decouple the method call within the
      * test from the method that is being tested. So that different methods can be called within the same test.
      *
      * @param <M> the type of message object that will be tested.
      * @param <P> the type of property that will be tested.
      */
     protected interface MessageExecutor<M, P> {
 
         public abstract M newMessage();
 
        public abstract Collection<P> getProperties(M message);

         public abstract Collection<P> getProperties(M message, String name);
 
         public abstract void addProperty(M message, P property);
 
         public abstract void addProperties(M message, Collection<P> properties);
 
         public abstract void setProperties(M message, Collection<P> properties);
 
         public abstract P removeProperty(M message, P property);
 
         public abstract Collection<P> removeProperties(M message, Collection<P> properties);
     }
 
 
     private PropertyExecutor<P> propertyExecutor;
     private MessageExecutor<M, P> messageExecutor;
     private P propertyOne;
     private P propertyTwo;
     private P propertyThree;
     private Collection<P> properties;
 
 
     /**
      * Create a new {@code AbstractMessagePropertyTest} with a property and message executor that will be used within
      * all the tests.
      *
      * @param propertyExecutor the property executor that will be used to create and inspect properties.
      * @param messageExecutor  the message executor that will be used to create and test a message object.
      */
     protected AbstractMessagePropertyTest(PropertyExecutor<P> propertyExecutor, MessageExecutor<M, P> messageExecutor) {
         super(propertyExecutor);
 
         this.propertyExecutor = propertyExecutor;
         this.messageExecutor = messageExecutor;
     }
 
 
     @Override
     protected void exposeProperties(P propertyOne, P propertyTwo, P propertyThree, Collection<P> properties) {
 
         this.propertyOne = propertyOne;
         this.propertyTwo = propertyTwo;
         this.propertyThree = propertyThree;
         this.properties = properties;
     }
 
 
     @Test
     public void testGetPropertiesWhenNoPropertiesHaveBeenAdded() throws Exception {
 
         M message = messageExecutor.newMessage();
 
         Collection<P> properties = messageExecutor.getProperties(message);
 
         assertNotNull("a collection of properties should be returned.", properties);
         assertEquals("the collection of properties should be empty.", 0, properties.size());
     }
 
     @Test
     public void testSetProperties() throws Exception {
 
         M message = messageExecutor.newMessage();
 
         messageExecutor.addProperty(message, propertyExecutor.newProperty("someName", "someValue"));
 
         messageExecutor.setProperties(message, properties);
 
         Collection<P> properties = messageExecutor.getProperties(message);
 
         assertEquals("the number of message properties is correct", 3, properties.size());
         assertTrue("the first property has been set correctly", properties.contains(propertyOne));
         assertTrue("the second property has been set correctly", properties.contains(propertyTwo));
         assertTrue("the third property has been set correctly", properties.contains(propertyThree));
     }
 
     @Test
     public void testSetEmptyProperties() throws Exception {
 
         setNoPropertiesTest(Collections.<P>emptySet());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testSetNullProperties() throws Exception {
 
         setNoPropertiesTest(null);
     }
 
     @Test
     public void testGetProperty() throws Exception {
 
         M message = messageExecutor.newMessage();
         messageExecutor.setProperties(message, properties);
 
         assertEquals("property one is retrieved correctly.", new HashSet<P>(Arrays.asList(propertyOne)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyOne)));
         assertEquals("property two is retrieved correctly.", new HashSet<P>(Arrays.asList(propertyTwo)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyTwo)));
         assertEquals("property three is retrieved correctly.", new HashSet<P>(Arrays.asList(propertyThree)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyThree)));
     }
 
     @Test
     public void testGetPropertyThatDoesNotExist() throws Exception {
 
         M message = messageExecutor.newMessage();
 
         assertNull("retrieving a properties when no properties exist should return an empty collection.",
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyOne)));
 
         messageExecutor.setProperties(message, properties);
 
         assertNull("retrieving a property that does not exist should return an empty collection.",
                 messageExecutor.getProperties(message, "not here"));
     }
 
     @Test
     public void testAddProperty() throws Exception {
 
         M message = messageExecutor.newMessage();
 
         assertEquals("no properties should exist", 0, messageExecutor.getProperties(message).size());
 
         messageExecutor.addProperty(message,
                 propertyExecutor.newProperty(propertyExecutor.getName(propertyOne),
                         propertyExecutor.getValue(propertyOne)));
 
         assertEquals("one property should exist", 1, messageExecutor.getProperties(message).size());
         assertEquals("property one should have been added", new HashSet<P>(Arrays.asList(propertyOne)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyOne)));
 
         messageExecutor.addProperty(message,
                 propertyExecutor.newProperty(propertyExecutor.getName(propertyTwo),
                         propertyExecutor.getValue(propertyTwo)));
 
         assertEquals("two properties should exist", 2, messageExecutor.getProperties(message).size());
         assertEquals("property two should have been added", new HashSet<P>(Arrays.asList(propertyTwo)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyTwo)));
 
         messageExecutor.addProperty(message,
                 propertyExecutor.newProperty(propertyExecutor.getName(propertyThree),
                         propertyExecutor.getValue(propertyThree)));
 
         assertEquals("three properties should exist", 3, messageExecutor.getProperties(message).size());
         assertEquals("property three should have been added", new HashSet<P>(Arrays.asList(propertyThree)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyThree)));
     }
 
     @Test
     public void testAddPropertyWithEmptyValue() throws Exception {
 
         addPropertyWithBlankValueTest("");
     }
 
     @Test
     public void testAddPropertyWithNullValue() throws Exception {
 
         addPropertyWithBlankValueTest(null);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testAddPropertyWithEmptyName() throws Exception {
 
         propertyExecutor.newProperty("", propertyExecutor.getValue(propertyOne));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testAddPropertyWithNullName() throws Exception {
 
         propertyExecutor.newProperty(null, propertyExecutor.getValue(propertyOne));
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testAddPropertyWithEmptyValues() throws Exception {
 
         propertyExecutor.newProperty("", "");
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testAddPropertyWithNullValues() throws Exception {
 
         propertyExecutor.newProperty(null, null);
     }
 
     @Test
     public void testAddProperties() throws Exception {
 
         M message = messageExecutor.newMessage();
 
         assertEquals("no properties should exist", 0, messageExecutor.getProperties(message).size());
 
         messageExecutor.addProperties(message, properties);
 
         assertEquals("three properties should exist", 3, messageExecutor.getProperties(message).size());
         assertEquals("property one should have been added", new HashSet<P>(Arrays.asList(propertyOne)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyOne)));
         assertEquals("property two should have been added", new HashSet<P>(Arrays.asList(propertyTwo)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyTwo)));
         assertEquals("property three should have been added", new HashSet<P>(Arrays.asList(propertyThree)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(propertyThree)));
     }
 
     @Test
     public void testRemoveProperty() throws Exception {
 
         M message = messageExecutor.newMessage();
 
         Collection<P> removeProperties = new HashSet<P>();
 
         removeProperties.add(propertyExecutor.newProperty(
                 propertyExecutor.getName(propertyOne), propertyExecutor.getValue(propertyOne)));
         removeProperties.add(propertyExecutor.newProperty(
                 propertyExecutor.getName(propertyTwo), propertyExecutor.getValue(propertyTwo)));
         removeProperties.add(propertyExecutor.newProperty(
                 propertyExecutor.getName(propertyThree), propertyExecutor.getValue(propertyThree)));
 
         messageExecutor.setProperties(message, removeProperties);
 
         assertEquals("property one is removed correctly.", propertyOne,
                 messageExecutor.removeProperty(message, propertyOne));
         assertFalse("property one is no longer in the message.",
                 messageExecutor.getProperties(message).contains(propertyOne));
         assertNull("property one should not be able to be removed again.",
                 messageExecutor.removeProperty(message, propertyOne));
 
         assertEquals("property two is removed correctly.", propertyTwo,
                 messageExecutor.removeProperty(message, propertyTwo));
         assertFalse("property two is no longer in the message.",
                 messageExecutor.getProperties(message).contains(propertyTwo));
         assertNull("property two should not be able to be removed again.",
                 messageExecutor.removeProperty(message, propertyTwo));
 
         assertEquals("property three is removed correctly.", propertyThree,
                 messageExecutor.removeProperty(message, propertyThree));
         assertFalse("property three is no longer in the message.",
                 messageExecutor.getProperties(message).contains(propertyThree));
         assertNull("property three should not be able to be removed again.",
                 messageExecutor.removeProperty(message, propertyThree));
     }
 
     @Test
     public void testRemoveProperties() throws Exception {
 
         M message = messageExecutor.newMessage();
         Collection<P> removeProperties = new HashSet<P>();
 
         removeProperties.add(propertyExecutor.newProperty(
                 propertyExecutor.getName(propertyOne), propertyExecutor.getValue(propertyOne)));
         removeProperties.add(propertyExecutor.newProperty(
                 propertyExecutor.getName(propertyTwo), propertyExecutor.getValue(propertyTwo)));
         removeProperties.add(propertyExecutor.newProperty(
                 propertyExecutor.getName(propertyThree), propertyExecutor.getValue(propertyThree)));
 
         messageExecutor.setProperties(message, removeProperties);
 
         Collection<P> removedProperties = new HashSet<P>(Arrays.asList(propertyTwo, propertyThree));
 
         assertEquals("properties removed correctly.", removedProperties,
                 messageExecutor.removeProperties(message, removedProperties));
         assertFalse("properties no longer in the message.",
                 messageExecutor.getProperties(message).containsAll(removedProperties));
         assertEquals("properties should not be able to be removed again.", Collections.emptySet(),
                 messageExecutor.removeProperties(message, removedProperties));
         assertTrue("property one is still in the message.",
                 messageExecutor.getProperties(message).contains(propertyOne));
     }
 
 
     private void setNoPropertiesTest(Collection<P> empty) {
 
         M message = messageExecutor.newMessage();
         messageExecutor.setProperties(message, empty);
 
         Collection<P> properties = messageExecutor.getProperties(message);
 
         assertNotNull("a collection of properties should be returned.", properties);
         assertEquals("the number of properties should be zero.", 0, properties.size());
     }
 
     private void addPropertyWithBlankValueTest(Object blank) {
 
         M message = messageExecutor.newMessage();
 
         P property = propertyExecutor.newProperty(propertyExecutor.getName(propertyOne), blank);
 
         assertEquals("no properties should exist", 0, messageExecutor.getProperties(message).size());
 
         messageExecutor.addProperty(message, property);
 
         assertEquals("one property should exist", 1, messageExecutor.getProperties(message).size());
         assertEquals("property one should have an empty value", new HashSet<P>(Arrays.asList(property)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(property)));
 
         messageExecutor.addProperty(message, property);
 
         assertEquals("one property should exist", 1, messageExecutor.getProperties(message).size());
         assertEquals("property one should have an empty value", new HashSet<P>(Arrays.asList(property)),
                 messageExecutor.getProperties(message, propertyExecutor.getName(property)));
     }
 }
