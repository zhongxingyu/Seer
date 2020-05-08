 package com.nearinfinity.hbaseclient;
 
 import com.nearinfinity.hbaseclient.Util;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.nio.ByteBuffer;
 import java.util.*;
 
 public class UtilTest {
     @Test
     public void testNextColumn() throws Exception {
         byte[] column = ByteBuffer.allocate(8 * 3)
                 .putLong(1)
                 .putLong(2)
                 .putLong(3)
                 .array();
         byte[] expected = ByteBuffer.allocate(8 * 3)
                 .putLong(1)
                 .putLong(2)
                 .putLong(4)
                 .array();
         byte[] result = Util.incrementColumn(column, 8 * 2);
         Assert.assertArrayEquals(expected, result);
     }
 
    @Test(expected = IllegalStateException.class)
     public void testMergeByteArray() throws Exception {
         final byte[] first = ByteBuffer.allocate(8 * 3)
                 .putLong(1)
                 .putLong(2)
                 .putLong(3)
                 .array();
         final byte[] second = ByteBuffer.allocate(8 * 3)
                 .putLong(1)
                 .putLong(2)
                 .putLong(4)
                 .array();
         List<byte[]> pieces = new LinkedList<byte[]>() {
             {
                 add(first);
                 add(second);
             }
         };
         Util.mergeByteArrays(pieces, first.length);
     }
 
     @Test
     public void testSerDe() throws Exception {
         Map<String, byte[]> map = new HashMap<String, byte[]>();
         byte[] b = new byte[]{2};
         map.put("Test", b);
         byte[] mapBytes = Util.serializeMap(map);
         Map<String, byte[]> result = Util.deserializeMap(mapBytes);
         Assert.assertArrayEquals(b, result.get("Test"));
     }
 
     @Test
     public void utilCorrectlySerializesLists() {
         List<List<String>> s = new LinkedList<List<String>>();
         s.add(new LinkedList<String>() {{
             add("Test");
         }});
         byte[] bytes = Util.serializeList(s);
         List<List<String>> result = Util.deserializeList(bytes);
         Assert.assertEquals("Test", result.get(0).get(0));
     }
 }
