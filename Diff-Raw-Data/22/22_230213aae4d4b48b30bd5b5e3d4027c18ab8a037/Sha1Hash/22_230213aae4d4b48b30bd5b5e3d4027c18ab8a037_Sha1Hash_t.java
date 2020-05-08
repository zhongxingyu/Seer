 package org.trolo.common;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableList;
 import com.google.common.primitives.Bytes;
 
 import javax.annotation.concurrent.Immutable;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Formatter;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 /**
  * @author Stanislav Kurilin
  */
 @Immutable
 public final class Sha1Hash {
     private final ImmutableList<Byte> value;
    public static final int BIT_IN_ONE_HASH = 160;
    public static final int BYTE_IN_ONE_HASH = BIT_IN_ONE_HASH / Byte.SIZE;
 
     private Sha1Hash(ImmutableList<Byte> value) {
         this.value = value;
     }
 
     public ImmutableList<Byte> hash() {
         return value;
     }
 
     public static Sha1Hash valueOf(List<Byte> hash) {
         final ImmutableList<Byte> safeValue = ImmutableList.copyOf(hash);
        checkArgument(safeValue.size() == BYTE_IN_ONE_HASH,
                "Each SHA-1 should consist from %s bytes, but %s presented", BYTE_IN_ONE_HASH, hash.size());
         return new Sha1Hash(safeValue);
     }
 
     public static Sha1Hash valueOf(byte[] hash) {
         return valueOf(Bytes.asList(hash));
     }
 
     public static Sha1Hash hash(String toHash) {
         return hash(toHash.getBytes(Charsets.UTF_8));
     }
 
     public static Sha1Hash hash(List<Byte> toHash) {
         return hash(Bytes.toArray(toHash));
     }
 
     public static Sha1Hash hash(byte[] toHash) {
         try {
             final MessageDigest md = MessageDigest.getInstance("SHA-1");
             return valueOf(md.digest(toHash));
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this.getClass().getSimpleName()).add("value", byteArray2Hex(value)).toString();
     }
 
     @Override
     public boolean equals(Object o) {
         return this == o || !(o == null || getClass() != o.getClass()) && value.equals(((Sha1Hash) o).value);
     }
 
     @Override
     public int hashCode() {
         return value.hashCode();
     }
 
     private static String byteArray2Hex(ImmutableList<Byte> hash) {
         Formatter formatter = new Formatter();
         for (byte b : hash) {
             formatter.format("%02x", b);
         }
         return formatter.toString();
     }
 
 
 }
