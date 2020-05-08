 package no.ntnu.capgeminitest.data;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import no.ntnu.capgeminitest.event.PropertyChangeListener;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class PropertyTest {
 
     class FakeListener implements PropertyChangeListener<Integer> {
         Integer received = null;
 
         @Override
         public void propertyChanged(Property<Integer> property) {
             received = property.get();
         }
     }
 
     private FakeListener listener = new FakeListener();
     Property<Integer> property;
 
     @Before
     public void setUp() {
         property = new Property<Integer>(5);
     }
 
     @Test
     public void testSetGet() {
         assertThat(property.get(), equalTo(5));
         property.set(10);
         assertThat(property.get(), equalTo(10));
     }
 
     @Test
     public void testListener() {
         property.addListener(listener);
         property.set(7);
         assertThat(listener.received, equalTo(7));
     }
     
     @Test
     public void testOnChange() {
         Property<Integer> property_ = new Property<Integer>(20) {
             @Override protected void onChange(Integer newValue) {
                 assertThat(newValue, equalTo(25));
                 assertThat(listener.received, equalTo(null));
             }
         };
         
         property_.addListener(listener);
         property_.set(25);
         assertThat(listener.received, equalTo(25));
     }
 }
