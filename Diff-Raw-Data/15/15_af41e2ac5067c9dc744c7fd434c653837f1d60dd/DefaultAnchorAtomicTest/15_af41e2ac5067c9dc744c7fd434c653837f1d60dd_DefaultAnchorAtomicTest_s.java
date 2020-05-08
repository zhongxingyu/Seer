 package au.net.netstorm.boost.gunge.type;
 
import java.lang.reflect.Field;

 import au.net.netstorm.boost.edge.java.lang.EdgeClass;
 import au.net.netstorm.boost.sniper.core.LifecycleTestCase;
 import au.net.netstorm.boost.sniper.marker.HasFixtures;
 import au.net.netstorm.boost.sniper.marker.InjectableTest;
 
 public class DefaultAnchorAtomicTest extends LifecycleTestCase implements HasFixtures, InjectableTest {
     private Anchor subject;
     private Field anchor;
     EdgeClass classer;
 
     public void setUpFixtures() {
         anchor = classer.getDeclaredField(Dude.class, "tricks");
         subject = new DefaultAnchor(anchor);
     }
 
     public void testFieldAccessor() {
         assertSame(anchor, subject.getField());
     }
 }
