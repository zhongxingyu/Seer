 package au.net.netstorm.boost.demo.nursery.autoedge;
 
import au.net.netstorm.boost.edge.java.lang.EdgeClass;
 import au.net.netstorm.boost.nursery.autoedge.AutoEdger;
 import au.net.netstorm.boost.sniper.core.LifecycleTestCase;
 import au.net.netstorm.boost.sniper.marker.HasFixtures;
 import au.net.netstorm.boost.sniper.marker.InjectableTest;
 import demo.edge.java.lang.Class;
 
 public class ClassDemoTest extends LifecycleTestCase implements HasFixtures, InjectableTest {
     private Class<String> subject;
     AutoEdger edger;
 
     public void setUpFixtures() {
         // FIX 2328 unchecked cast - handling generic classes in edge method should
         // FIX 2328 take same approach as Nu, see gunge.generics.TypeToken
         subject = edger.edge(Class.class, String.class);
     }
 
     public void testNewInstance() {
         String result = subject.newInstance();
         assertEquals("", result);
     }
 }
