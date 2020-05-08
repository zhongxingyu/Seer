 package au.net.netstorm.boost.gunge.type;
 
 import au.net.netstorm.boost.edge.java.lang.EdgeClass;
 import au.net.netstorm.boost.sniper.core.LifecycleTestCase;
 import au.net.netstorm.boost.sniper.marker.HasFixtures;
 import au.net.netstorm.boost.sniper.marker.InjectableTest;
 
import java.lang.reflect.Field;

// FIX 2328 Can be tested with the AtomTestChecker.  Much simpler.
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
