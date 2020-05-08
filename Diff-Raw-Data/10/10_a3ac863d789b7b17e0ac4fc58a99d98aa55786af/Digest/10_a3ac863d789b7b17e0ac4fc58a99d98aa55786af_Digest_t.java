 package com.crypticbit.javelin.store;
 
 import java.io.Serializable;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.util.Arrays;
 import java.util.Random;
 
 import com.google.common.io.BaseEncoding;
 
 /**
  * Subclases must implement Comparable
  * 
  * @author leo
  */
 public class Digest implements Identity, Serializable {
 
     private byte[] digest;
 
     /** Random digest */
     public Digest() {
 	digest = createRandomData(64);
     }
 
     public Digest(byte[] digestAsByte) {
 	this.digest = digestAsByte;
     }
 
     public Digest(MessageDigest messageDigest) {
 	digest = messageDigest.digest();
     }
 
     public Digest(String string) {
 	this.digest = BaseEncoding.base32Hex().decode(string);
     }
 
     @Override
     public int compareTo(Identity o) {
 	if (o instanceof Digest) {
 	    return ByteBuffer.wrap(digest).compareTo(ByteBuffer.wrap(((Digest) o).digest));
 	}
 	else {
 	    return this.getClass().getName().compareTo(o.getClass().getName());
 	}
     }
 
     @Override
     public boolean equals(Object compare) {
 	if (compare instanceof Digest) {
 	    return Arrays.equals(digest, ((Digest) compare).digest);
 	}
 	else {
 	    return false;
 	}
     }
 
     @Override
     public byte[] getDigestAsByte() {
 	return digest;
     }
 
     @Override
     public String getDigestAsString() {
 	return BaseEncoding.base32Hex().encode(getDigestAsByte());
     }
 
     @Override
     public int hashCode() {
	return ByteBuffer.wrap(digest).hashCode();
     }
 
     @Override
     public String toString() {
 	return getDigestAsString().substring(0, 6);
     }
 
     private static byte[] createRandomData(int length) {
 	byte[] b = new byte[length];
 	new Random().nextBytes(b);
 	return b;
     }
 
 }
