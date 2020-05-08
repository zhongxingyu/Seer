 package no.ntnu.capgeminitest.binding.android.propertyprovider;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import no.ntnu.capgeminitest.data.Property;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import android.view.View;

 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 @RunWith(RobolectricTestRunner.class)
 public class PropertyProviderStackTest {
     
     public class FakePropertyProvider implements PropertyProvider {
         public String bindingName;
         public Property<?> property;
         
         public FakePropertyProvider(String bindingName, Property<?> property) {
             this.bindingName = bindingName;
             this.property = property;
         }
         
         @Override
        public Property<?> getBoundProperty(View unused, String bindingName) {
             if (bindingName.equals(this.bindingName)) {
                 return property;
             } else {
                 return null;
             }
         }
     }
     
     private PropertyProviderStack stack = new PropertyProviderStack();
     private FakePropertyProvider pp1 = new FakePropertyProvider(
             "bindingName1", new Property<String>("property1"));
     private FakePropertyProvider pp2 = new FakePropertyProvider(
             "bindingName1", new Property<String>("property2"));
     private FakePropertyProvider pp3 = new FakePropertyProvider(
             "bindingName2", new Property<String>("property3"));
     
     @Test
     public void testSingleProvider() {
         stack.addProvider(pp1);
        assertThat((String)stack.getBoundProperty(null, "bindingName1").get(), equalTo("property1"));
     }
     
     @Test
     public void testProviderOverride1() {
         stack.addProvider(pp1);
         stack.addProvider(pp2);
        assertThat((String)stack.getBoundProperty(null, "bindingName1").get(), equalTo("property1"));
     }
     
     @Test
     public void testProviderOverride2() {
         stack.addProvider(pp2);
         stack.addProvider(pp1);
        assertThat((String)stack.getBoundProperty(null, "bindingName1").get(), equalTo("property2"));
     }
     
     @Test
     public void testMultipleBindingNames() {
         stack.addProvider(pp1);
         stack.addProvider(pp2);
         stack.addProvider(pp3);
        assertThat((String)stack.getBoundProperty(null, "bindingName1").get(), equalTo("property1"));
        assertThat((String)stack.getBoundProperty(null, "bindingName2").get(), equalTo("property3"));
     }
 }
