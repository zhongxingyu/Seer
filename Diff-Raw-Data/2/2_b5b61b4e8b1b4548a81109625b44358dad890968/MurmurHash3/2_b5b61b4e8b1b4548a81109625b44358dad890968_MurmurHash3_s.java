 package org.ilumbo.giantsnail.cryptography;
 
 /**
  * The MurmurHash3 algorithm was created by Austin Appleby.
  * http://code.google.com/p/smhasher/wiki/MurmurHash3
  *
  * This Java port is heavily based on a port by Yonik Seeley.
  * http://github.com/yonik/java_util
  *
  * This class should work identical to the final C++ version of MurmurHash3. I have, however, not tested this.
  */
 public final class MurmurHash3 {
 	private static final int c1 = 0xCC9E2D51;
 	private static final int c2 = 0x1B873593;
 	/**
 	 * Returns the MurmurHash3_x86_32 hash of the data in the passed integer array from indexes offset (inclusive) to offset +
 	 * length (exclusive).
 	 */
 	public final static int calculate(int[] data, int offset, int length, int seed) {
 		int h1 = seed;
 		final int end = offset + length;
 		for (int index = offset; end != index; index++) {
 			int k1 = data[index];
 			k1 *= c1;
 			// Inlined ROTL32(k1, 15)
 			k1 = (k1 << 15) | (k1 >>> 17);
 			k1 *= c2;
 			h1 ^= k1;
 			// Inlined ROTL32(h1,13)
 			h1 = (h1 << 13) | (h1 >>> 19);
 			h1 = h1 * 5 + 0xE6546B64;
 		}
 		// Finalization.
		h1 ^= length;
 		// Inlined fmix(h1)
 		h1 ^= h1 >>> 16;
 		h1 *= 0x85EBCA6B;
 		h1 ^= h1 >>> 13;
 		h1 *= 0xC2B2AE35;
 		h1 ^= h1 >>> 16;
 		return h1;
 	}
 	/**
 	 * Returns the MurmurHash3_x86_32 hash of the data in the passed byte array from indexes offset (inclusive) to offset +
 	 * length (exclusive).
 	 */
 	public final static int calculate(byte[] data, int offset, int length, int seed) {
 		int h1 = seed;
 		// Round down to 4 byte block.
 		final int roundedEnd = offset + (length & 0xFFFFFFFC);
 		for (int index = offset; roundedEnd != index; index += 4) {
 			// Little endian load order. The and operator turns the bytes into an unsigned integer (0…0xFF, inclusive).
 			int k1 = ((data[index /* | 0 */] & 0xFF) /* << 0 */) | ((data[index | 1] & 0xFF) << 8) |
 					((data[index | 2] & 0xFF) << 16) | ((data[index | 3] & 0xFF) << 24);
 			k1 *= c1;
 			// Inlined ROTL32(k1, 15)
 			k1 = (k1 << 15) | (k1 >>> 17);
 			k1 *= c2;
 			h1 ^= k1;
 			// Inlined ROTL32(h1,13)
 			h1 = (h1 << 13) | (h1 >>> 19);
 			h1 = h1 * 5 + 0xE6546B64;
 		}
 		// Tail.
 		int k1 = 0;
 		switch (length & 0x03) {
 		case 3:
 			k1 = (data[roundedEnd + 2] & 0xFF) << 16;
 			// Fallthrough (no break).
 		case 2:
 			k1 |= (data[roundedEnd + 1] & 0xFF) << 8;
 			// Fallthrough (no break).
 		case 1:
 			k1 |= (data[roundedEnd] & 0xFF);
 			k1 *= c1;
 			// Inlined ROTL32(k1,15)
 			k1 = (k1 << 15) | (k1 >>> 17);
 			k1 *= c2;
 			h1 ^= k1;
 		}
 		// Finalization.
 		h1 ^= length;
 		// Inlined fmix(h1)
 		h1 ^= h1 >>> 16;
 		h1 *= 0x85EBCA6B;
 		h1 ^= h1 >>> 13;
 		h1 *= 0xC2B2AE35;
 		h1 ^= h1 >>> 16;
 		return h1;
 	}
 	/**
 	 * Returns the MurmurHash3_x86_32 hash of the data that is the passed integer.
 	 */
 	public final static int calculate(int data, int seed) {
 		int h1 = seed;
 		int k1 = data;
 		k1 *= c1;
 		// Inlined ROTL32(k1, 15)
 		k1 = (k1 << 15) | (k1 >>> 17);
 		k1 *= c2;
 		h1 ^= k1;
 		// Inlined ROTL32(h1,13)
 		h1 = (h1 << 13) | (h1 >>> 19);
 		h1 = h1 * 5 + 0xE6546B64;
 		// Finalization.
 		h1 ^= 4;
 		// Inlined fmix(h1)
 		h1 ^= h1 >>> 16;
 		h1 *= 0x85EBCA6B;
 		h1 ^= h1 >>> 13;
 		h1 *= 0xC2B2AE35;
 		h1 ^= h1 >>> 16;
 		return h1;
 	}
 }
