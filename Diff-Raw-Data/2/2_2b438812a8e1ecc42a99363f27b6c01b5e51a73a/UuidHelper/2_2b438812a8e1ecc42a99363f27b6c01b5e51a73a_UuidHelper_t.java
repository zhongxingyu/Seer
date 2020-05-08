 package org.sunnycode.hash.file2;
 
 import java.nio.ByteBuffer;
 import java.util.UUID;
 
 public class UuidHelper {
     public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buf = ByteBuffer.allocate(16);
 
         buf.putLong(uuid.getMostSignificantBits());
         buf.putLong(uuid.getLeastSignificantBits());
 
         return buf.array();
     }
 }
