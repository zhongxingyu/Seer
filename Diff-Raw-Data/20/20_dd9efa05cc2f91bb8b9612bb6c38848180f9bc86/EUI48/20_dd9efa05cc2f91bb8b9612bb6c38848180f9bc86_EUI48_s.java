 /*-
  * Copyright 2012 Benedikt Meurer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.benediktmeurer.eui4j;
 
 import java.io.Serializable;
 
 /**
  * A class that represents an immutable 48-bit extended unique identifier (EUI-48), also referred to
  * as MAC-48 or simply MAC address.
  * 
  * @author Benedikt Meurer
  * @see EUI64
  */
 public final class EUI48 implements Comparable<EUI48>, Serializable {
     /** The serial version UID of this class. */
     private static final long serialVersionUID = 1L;
 
     /** The 32 most significant bits. */
     private final int mostSignificantBits;
 
     /** The 16 least significant bits. */
     private final short leastSignificantBits;
 
     /**
      * Constructs a new {@link EUI48} using the specified data. {@code mostSignificantBits} contains
      * the 32 most significant bits for the EUI-48, and {@code leastSignificantBits} contains the 16
      * least significant bits.
      * 
      * @param mostSignificantBits The 32 most significant bits.
      * @param leastSignificantBits The 16 least significant bits.
      * @see #getMostSignificantBits()
      * @see #getLeastSignificantBits()
      */
     public EUI48(int mostSignificantBits, short leastSignificantBits) {
         this.mostSignificantBits = mostSignificantBits;
         this.leastSignificantBits = leastSignificantBits;
     }
 
     /**
      * Constructs a new {@link EUI48} using the given {@code octets}.
      * 
      * @param octets The octet representation in transmission order.
      * @throws IllegalArgumentException If {@code octets} does not have exactly {@code 6} elements.
      * @throws NullPointerException If {@code octets} is {@code null}.
      * @see #getOctets()
      */
     public EUI48(byte[] octets) {
         if (octets.length != 6) {
             throw new IllegalArgumentException("octets is of illegal length " + octets.length);
         }
         this.mostSignificantBits = (octets[0] << 24)
                                    | (octets[1] << 16)
                                    | (octets[2] << 8)
                                    | octets[3];
         this.leastSignificantBits = (short) ((octets[4] << 8) | octets[5]);
     }
 
     /**
      * Returns the 32 most significant bits of this {@link EUI48}.
      * 
      * @return The 32 most significant bits.
      * @see #EUI48(int, short)
      * @see #getLeastSignificantBits()
      */
     public int getMostSignificantBits() {
         return this.mostSignificantBits;
     }
 
     /**
      * Returns the 16 least significant bits of this {@link EUI48}.
      * 
      * @return The 16 least significant bits.
      * @see #EUI48(int, short)
      * @see #getMostSignificantBits()
      */
     public short getLeastSignificantBits() {
         return this.leastSignificantBits;
     }
 
     /**
      * Returns the octet representation of this {@link EUI48} in transmission order.
      * 
      * @return The octet representation in transmission order.
      * @see #EUI48(byte[])
      */
     public byte[] getOctets() {
         return new byte[] {
                 (byte) ((this.mostSignificantBits >> 24) & 0xff),
                 (byte) ((this.mostSignificantBits >> 16) & 0xff),
                 (byte) ((this.mostSignificantBits >> 8) & 0xff),
                 (byte) (this.mostSignificantBits & 0xff),
                 (byte) ((this.leastSignificantBits >> 8) & 0xff),
                 (byte) (this.leastSignificantBits & 0xff)
         };
     }
 
     /**
      * Compares this {@link EUI48} with the specified {@link EUI48} based on their bit
      * representations.
      * 
      * @param val {@code EUI48} to which this {@code EUI48} should be compared.
      * @return {@code -1}, {@code 0} or {@code 1} if this {@link EUI48} is less than, equal to or
      *         greater than {@code val}.
      * @throws NullPointerException if {@code val} is {@code null}.
      */
     public int compareTo(EUI48 val) {
         if (this.mostSignificantBits < val.mostSignificantBits) {
             return -1;
         }
         else if (this.mostSignificantBits > val.mostSignificantBits) {
             return 1;
         }
         else if (this.leastSignificantBits < val.leastSignificantBits) {
             return -1;
         }
         else if (this.leastSignificantBits > val.leastSignificantBits) {
             return 1;
         }
         else {
             return 0;
         }
     }
 
     /**
      * Creates a {@link EUI48} from a string standard representation in {@code name}. The standard
      * representation is six groups of two hexadecimal digits, separated by hyphens ({@code -}) or
      * colons ({@code :}), in transmission order.
      * 
      * @param name The string representation of of an EUI-48.
      * @return The EUI-48 represented by the specified {@code name}.
      * @throws IllegalArgumentException if {@code name} is not a valid string representation of an
      *             EUI-48.
      * @throws NullPointerException if {@code name} is {@code null}.
      * @see #toString()
      */
     public static EUI48 fromString(String name) {
         long bits = 0;
         char sep = 0;
         for (int n = 0;; ++n) {
             if (n == name.length()) {
                 if (n == 17) {
                     return new EUI48((int) (bits >> 16), (short) bits);
                 }
                 else {
                     break;
                 }
             }
             char c = name.charAt(n);
             if (n == 2) {
                 if (c != ':' && c != '-') {
                     break;
                 }
                 sep = c;
             }
             else if ((n - 2) % 3 == 0) {
                 if (c != sep) {
                     break;
                 }
             }
             else if (c >= '0' && c <= '9') {
                 bits = (bits << 4) | (c - '0');
             }
             else if (c >= 'a' && c <= 'f') {
                 bits = (bits << 4) | (10 + c - 'a');
             }
             else if (c >= 'A' && c <= 'F') {
                 bits = (bits << 4) | (10 + c - 'A');
             }
             else {
                 break;
             }
         }
         throw new IllegalArgumentException("Invalid EUI-48 string: " + name);
     }
 
     /**
      * Compares this {@link EUI48} to the specified object. The result is {@code true} if and only
      * if {@code obj} is not {@code null} and contains the same bits as this EUI-48.
      * 
      * @param obj The object to be compared.
      * @return {@code true} if the objects are equal; {@code false} otherwise.
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         else if (obj instanceof EUI48) {
             EUI48 val = (EUI48) obj;
             return this.mostSignificantBits == val.mostSignificantBits
                    && this.leastSignificantBits == val.leastSignificantBits;
         }
         return false;
     }
 
     /**
      * Returns a hash code for this EUI-48.
      * 
      * @return A hash code value for this {@code EUI48}.
      */
     @Override
     public int hashCode() {
         return this.mostSignificantBits ^ this.leastSignificantBits;
     }
 
     /**
      * Returns the string representation of this {@link EUI48} in human-friendly form, composed of
      * six groups of two hexadecimal digits, separated by colons ({@code :}), in transmission order.
      * 
      * @return The string representation of this EUI-48.
      * @see #fromString(String)
      */
     @Override
     public String toString() {
         char[] value = new char[17];
        long bits = ((long) this.mostSignificantBits << 16) | this.leastSignificantBits;
         for (int n = value.length; --n >= 0;) {
             if ((n - 2) % 3 == 0) {
                 value[n] = ':';
             }
             else {
                 int c = (int) bits & 0xf;
                 value[n] = (char) (c + (c < 10 ? '0' : ('a' - 10)));
                 bits >>= 4;
             }
         }
         return new String(value);
     }
 }
