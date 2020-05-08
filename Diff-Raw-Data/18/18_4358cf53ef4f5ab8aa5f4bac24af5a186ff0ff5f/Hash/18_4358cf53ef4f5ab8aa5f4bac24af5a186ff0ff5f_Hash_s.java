 package edu.uw.zookeeper.safari;
 
 import com.google.common.base.Function;
 import com.google.common.hash.HashCode;
 import com.google.common.hash.HashFunction;
 import com.google.common.hash.Hashing;
import com.google.common.primitives.Ints;
 
 import edu.uw.zookeeper.common.Pair;
 import edu.uw.zookeeper.common.Reference;
 
 public enum Hash implements Function<String, Hash.Hashed>, Reference<HashFunction> {
     MURMUR3_32(Hashing.murmur3_32());
     
     public static Hash default32() {
         return MURMUR3_32;
     }
     
     public class Hashed extends Pair<String, HashCode> {
         public Hashed(String first, HashCode second) {
             super(first, second);
         }
         
         public Hashed rehash() {
            int inputBytes = Ints.fromByteArray(first().getBytes());
            int h = second().asInt() ^ inputBytes;
            return new Hashed(first(), get().hashInt(h));
         }
         
         public Identifier asIdentifier() {
             return Identifier.valueOf(second.asInt());
         }
     }
     
     private final HashFunction hashFunction;
     
     private Hash(HashFunction hashFunction) {
         this.hashFunction = hashFunction;
     }
 
     public int bits() {
         return get().bits();
     }
     
     @Override
     public Hashed apply(String input) {
        return new Hashed(input, get().hashUnencodedChars(input));
     }
 
     @Override
     public HashFunction get() {
         return hashFunction;
     }
 }
