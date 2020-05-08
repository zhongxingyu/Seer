 package little.nj.gui.binding.tests;
 
 import little.nj.core.tests.MockObjects.Ob;
 import little.nj.core.tests.MockObjects.ObGeneric;
 import little.nj.core.tests.MockObjects.ObGenericChangeNotify;
 import little.nj.gui.binding.*;
 import little.nj.gui.binding.GenericBindingImpl.Getter;
 import little.nj.gui.binding.GenericBindingImpl.Marshal;
 import little.nj.gui.binding.GenericBindingImpl.Setter;
 import little.nj.gui.binding.StdComponents.GetterImpl;
 import little.nj.gui.binding.StdComponents.IntToStringMarshal;
 import little.nj.gui.binding.StdComponents.SetterImpl;
 import little.nj.gui.binding.StdComponents.StringToIntMarshal;
 import little.nj.gui.binding.events.AbstractEventSource;
 import org.junit.Test;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Arrays;
 
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
 
@SuppressWarnings("deprecation")
 public class FluentBindingTest {
 
     private static Marshal<Integer, String> intToString = new IntToStringMarshal();
 
     private static Marshal<String, Integer> stringToInt = new StringToIntMarshal();
 
     @Test
     public void test_Prototype() {
         final Ob ob1 = new Ob(10);
         final ObGeneric<String> ob2 = new ObGeneric<String>("Hello, World");
 
         GenericBindingImpl<Integer, String> bind = new GenericBindingImpl<Integer, String>(
                 new Getter<Integer>() {
                     @Override
                     public Integer get() {
                         return ob1.getField();
                     } },
                 new Setter<String>() {
                     @Override
                     public void set(String value) {
                         ob2.setField(value);
                     } },
                 intToString
                 );
 
         bind.bind();
 
         assertEquals("10", ob2.getField());
     }
 
     @Test
     public void test_Fluent() {
         final Ob ob1 = new Ob(10);
         final ObGeneric<String> ob2 = new ObGeneric<String>("Hello, World");
 
         FluentBindingImpl<Integer, String> bind =
                 FluentBindingImpl.bind(int.class, String.class)
                     .from(new Getter<Integer>() {
 
                         @Override
                         public Integer get() {
                             return ob1.getField();
                         } })
                     .to(new Setter<String>() {
 
                         @Override
                         public void set(String value) {
                             ob2.setField(value);
                         } })
                     .via(intToString);
 
         bind.bind();
 
         assertEquals("10", ob2.getField());
     }
 
     @Test
     public void test_Fluent2() {
         Ob ob1 = new Ob(10);
         ObGeneric<String> ob2 = new ObGeneric<String>("Hello, World");
 
         FluentBindingFactory fac = new FluentBindingFactoryImpl();
 
         FluentBinding<Integer, String> bind =
                 fac.bind(ob1, int.class, "getField", ob2, String.class, "setField");
 
         bind.via(intToString);
 
         bind.bind();
 
         assertEquals("10", ob2.getField());
 
         FluentBinding<String, Integer> bind2 =
                 fac.bind(ob2, String.class, "getField", ob1, int.class, "setField");
 
         bind2.via(stringToInt);
 
         ob2.setField("20");
 
         bind2.bind();
 
         assertEquals(20, ob1.getField());
     }
 
     @Test
     public void test_Fluent3() {
         final Ob ob1 = new Ob(10);
         final ObGeneric<String> ob2 = new ObGeneric<String>("Hello, World");
 
         FluentBindingFactory fac = new FluentBindingFactoryImpl();
 
         try {
             @SuppressWarnings("unused")
             FluentBinding<Integer, String> bind = fac.bind(ob2, int.class, "getField", ob1, String.class, "setField");
             fail();
         } catch (ClassCastException e) {
 
         }
 
     }
 
     @Test
     public void test_Fluent4() {
 
         final Ob ob1 = new Ob(10);
         final ObGeneric<String> ob2 = new ObGeneric<String>("Hello, World");
 
         FluentBindingImpl<Integer, String> bind = new FluentBindingImpl<Integer, String>();
 
         bind.from(new GetterImpl<Integer>(ob1, "getField", int.class))
             .to(new SetterImpl<String>(ob2, "setField", String.class))
             .via(intToString);
 
         bind.bind();
 
         assertEquals("10", ob2.getField());
     }
 
     @Test
     public void test_Fluent_Bind_Event()
     {
         ObGenericChangeNotify<String> ob1 = new ObGenericChangeNotify<String>("Hello, World");
         Ob ob2 = new Ob(10);
 
 
         FluentBindingImpl<String, Integer> bind = new FluentBindingImpl<String, Integer>();
         bind.from(new GetterImpl<String>(ob1, "getField", String.class))
             .to(new SetterImpl<Integer>(ob2, "setField", int.class))
             .via(stringToInt)
             .when(new AbstractEventSource<ObGenericChangeNotify<String>>(ob1) {
 
                 @Override
                 protected void init(ObGenericChangeNotify<String> obj) {
                     obj.addPropertyChangeListener(new PropertyChangeListener() {
 
                         @Override
                         public void propertyChange(PropertyChangeEvent evt) {
                             fireBindingEvent(evt.getSource());
                         }
                     });
                 }
             });
 
         ob1.setField("35");
 
         assertEquals(35, ob2.getField());
 
     }
 
     @Test
     public void test_Bind_Primitive_Array() {
 
         class ObPrim {
             byte[] field;
             byte[] getField() { return field; }
             void setField(byte[] value) { field = value; }
         }
 
         final ObPrim ob1 = new ObPrim(),
                      ob2 = new ObPrim();
 
         final byte[] field1 = new byte[] { 0x0, 0x1, 0x2, 0x3 },
                      field2 = new byte[] { 0x3, 0x2, 0x1, 0x0 };
 
         ob1.setField(field1);
         ob2.setField(field2);
 
         FluentBinding<byte[], byte[]> bind = new FluentBindingImpl<byte[], byte[]>();
 
         bind.from(new Getter<byte[]>() {
             @Override
             public byte[] get() {
                 return ob1.getField();
             }
         }).to(new Setter<byte[]>() {
             @Override
             public void set(byte[] value) {
                 ob2.setField(value);
             }
         }).via(new Marshal<byte[], byte[]>() {
             @Override
             public byte[] marshal(byte[] value) {
                 return Arrays.copyOf(value, value.length);
             } });
 
         bind.bind();
 
         assertEquals(true, Arrays.equals(field1, ob2.getField()));
 
     }
 }
