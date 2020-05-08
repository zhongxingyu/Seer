 package hbase;
 
 import static java.lang.Integer.signum;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static util.HSerializer.compare;
 import static util.HSerializer.Order.ASCENDING;
 import static util.HSerializer.Order.DESCENDING;
 
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import util.HSerializer;
 
 public abstract class TestHSerializable<T extends Comparable<T>> {
 
   protected abstract T create();
   protected abstract HSerializer<T> ascendingSerializer();
   protected abstract HSerializer<T> descendingSerializer();
 
   protected void testSerialization(T val, HSerializer<T> serde) {
     byte[] bytes = serde.toBytes(val);
     T p = serde.fromBytes(bytes);
     assertEquals(
       "round-trip byte[] serialization should be equal.",
       0, HSerializer.compare(serde, val, p));
     Arrays.fill(bytes, (byte) 0);
     ByteBuffer buf = ByteBuffer.allocate(bytes.length);
     serde.putBytes(buf, val);
     buf.flip();
     buf.get(bytes, 0, bytes.length);
     p = serde.fromBytes(bytes);
     assertEquals("round-trip ByteBuffer serialization should be equal",
       0, HSerializer.compare(serde, val, p));
   }
 
   protected void testSort(T val1, T val2, HSerializer<T> serde) {
    int direction = serde.order() == ASCENDING ? 1 : -1;
     byte[] bytes1 = serde.toBytes(val1);
     byte[] bytes2 = serde.toBytes(val2);
 
     assertEquals(
       String.format("%s sort order broken for <%s>, <%s>", serde.order(), val1, val2),
      signum(compare(serde, val1, val2)),
      signum(direction * compare(bytes1, bytes2)));
    assertEquals(
      String.format("%s sort order broken for <%s>, <%s>", serde.order(), val2, val1),
      signum(compare(serde, val2, val1)),
      signum(direction * compare(bytes2, bytes1)));
   }
 
   @Test
   public void testHSerializable() {
     T o1 = create(), o2 = create();
     HSerializer<T> asc = ascendingSerializer();
     HSerializer<T> dsc = descendingSerializer();
 
     testSerialization(o1, asc);
     testSerialization(o1, dsc);
     testSort(o1, o2, asc);
     testSort(o1, o2, dsc);
   }
 
   @Test
   public void testNullAwareCompareAsc() {
     assertTrue(0 == compare(ASCENDING, (Long) null, null));
     assertTrue(0 > compare(ASCENDING, null, 1L));
     assertTrue(0 < compare(ASCENDING, 1L, null));
     assertTrue(0 > compare(ASCENDING, 1L, 2L));
   }
 
   @Test
   public void testNullAwareCompareDsc() {
     assertTrue(0 == compare(DESCENDING, (Long) null, null));
     assertTrue(0 < compare(DESCENDING, null, 1L));
     assertTrue(0 > compare(DESCENDING, 1L, null));
     assertTrue(0 < compare(DESCENDING, 1L, 2L));
   }
 }
