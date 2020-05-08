 package actor;
 
 import static org.junit.Assert.*;
 
 import java.util.Map;
 
import math.Vector3f;

 import org.junit.Test;
 
 public class ActorIdTest {
     @Test
     public final void testHashCode() {
         int frequency[] = new int[64];
         
         for (int i = 0; i < 1024; i++) {
             ActorId id = new ActorId(0);
             frequency[id.hashCode() % 64]++;
         }
         
         for (int f: frequency) {
             assertTrue(f > 1024 / 2 / frequency.length);
             assertTrue(f < 1024 * 2 / frequency.length);
         }
     }
 
     @Test
     public final void testEqualsActorId() {
         assertFalse(new ActorId(0).equals(new ActorId(0)));
         ActorId id = new ActorId(0);
         
         assertTrue(id.equals(id));
         assertTrue(new ActorId(0, 238).equals(new ActorId(0, 238)));
     }
 
     @Test
     public final void testToString() {
         ActorId id = new ActorId(123, 456);
        
         assertTrue(id.toString().contains("#123"));
         assertTrue(id.toString().contains(".456"));
     }
 
     @Test
     public final void testAsMapKey() {
         ActorId a = new ActorId(123, 345);
         ActorId b = new ActorId(123, 345);
     
         assertTrue(a.equals(b));
         assertEquals(a.hashCode(), b.hashCode());
         
         Map<ActorId, String> map = new java.util.HashMap<ActorId, String>();
         
         map.put(a, "test");
         assertEquals("test", map.get(a));
         assertEquals("test", map.get(b));
         assertEquals(1, map.size());
         
         map.put(b, "test2");
         assertEquals(1, map.size());
         assertEquals("test2", map.get(a));
         assertEquals("test2", map.get(b));
     }
 }
