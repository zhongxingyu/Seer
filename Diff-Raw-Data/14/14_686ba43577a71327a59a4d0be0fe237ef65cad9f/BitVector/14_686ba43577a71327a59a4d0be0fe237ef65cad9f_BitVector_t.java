 package ahs.util;
 
 import ahs.io.*;
 import ahs.io.codec.*;
 import ahs.io.codec.eon.*;
 import ahs.io.codec.json.*;
 
 import java.util.BitSet;
 
 public class BitVector {
 	public BitVector() {
 		$bs = new BitSet();
 		$len = 0;
 	}
 	
 	public BitVector(int $nbits) {
 		$bs = new BitSet($nbits);
 		$len = $nbits;
 	}
 	
 	public BitVector(String $s) {
 		$len = $s.length();
 		$bs = new BitSet($len);
 		for (int $i = 0; $i < $len; $i++)
 			if ($s.charAt($i) != '0') $bs.set($i);
 	}
 	
 	/**
 	 * @param $b
 	 *                array to glean bits from
 	 * @param $offset
 	 *                offset from start IN BITS
 	 * @param $len
 	 *                number of bits to get
 	 */
 	public BitVector(byte[] $b, int $offset, int $len) {
 		this.$len = $len;
 		$bs = getBSfromBA($b).get($offset, $offset + $len);
 	}
 	
 	/**
 	 * Same as BitVector($b, 0, $b.length*8); but more efficient.
 	 */
 	public BitVector(byte[] $b) {
 		$len = $b.length * 8;
 		$bs = getBSfromBA($b);
 	}
 	
 	private BitSet getBSfromBA(byte[] $ba) {
 		BitSet $b = new BitSet($ba.length * 8);
 		for (int $i = 0; $i < $ba.length; $i++) {
 			int $o = $i * 8;
 			$b.set($o + 0, 0 != ($ba[$i] & 128));
 			$b.set($o + 1, 0 != ($ba[$i] & 64));
 			$b.set($o + 2, 0 != ($ba[$i] & 32));
 			$b.set($o + 3, 0 != ($ba[$i] & 16));
 			$b.set($o + 4, 0 != ($ba[$i] & 8));
 			$b.set($o + 5, 0 != ($ba[$i] & 4));
 			$b.set($o + 6, 0 != ($ba[$i] & 2));
 			$b.set($o + 7, 0 != ($ba[$i] & 1));
 //			$b.set($o + 7, 0 != ($ba[$i] & 128));
 //			$b.set($o + 6, 0 != ($ba[$i] & 64));
 //			$b.set($o + 5, 0 != ($ba[$i] & 32));
 //			$b.set($o + 4, 0 != ($ba[$i] & 16));
 //			$b.set($o + 3, 0 != ($ba[$i] & 8));
 //			$b.set($o + 2, 0 != ($ba[$i] & 4));
 //			$b.set($o + 1, 0 != ($ba[$i] & 2));
 //			$b.set($o + 0, 0 != ($ba[$i] & 1));
 		}
 		return $b;
 	}
 	
 	/**
 	 * This constructor is a bit sketchy. Due to the stupid nature of the
 	 * implementation of BitSet, we can't guarantee anything about the size of this
 	 * BitVector if the given BitSet ends in zero.
 	 */
 	public BitVector(BitSet $set) {
 		$bs = $set;
 		$len = $set.length();
 	}
 	
 	public BitVector(BitVector $bv) {
 		$bs = (BitSet) $bv.$bs.clone();
 		$len = $bv.$len;
 	}
 	
 	public BitSet	$bs;
 	private int	$len;
 	
 	@Deprecated
 	public BitVector(JsonObject $jo) throws JsonException {
 		$jo.assertKlass(BitVector.class);
 		$len = $jo.getInt("l");
 		$bs = BitSet.valueOf($jo.getByteData());
 	}
 
 	@Deprecated
 	public JsonObject toJSON() throws TranslationException {
 		return (JsonObject)ENCODER.encode(JsonCodec.X, this);
 	}
 
 	/* BEGIN EON CODEC BLOCK */
 	public static final Encoder<EonCodec,EonObject,BitVector> ENCODER;
 	public static final Decoder<EonCodec,EonObject,BitVector> DECODER;
 	static { EonDencoder $t = new EonDencoder(); ENCODER = $t; DECODER = $t; }
 	public static class EonDencoder implements ahs.io.codec.Dencoder<EonCodec,EonObject,BitVector> {
 		public EonObject encode(EonCodec $codec, BitVector $x) throws TranslationException {
			EonObject $jo = $codec.simple("BiV", null, $x.toByteArray());
 			$jo.put("l", $x.$len);
 			return $jo;
 		}
 		public BitVector decode(EonCodec $codec, EonObject $x) throws TranslationException {
 			$x.assertKlass("BiV");
 			return new BitVector($x.getByteData(),0,$x.getInt("l"));
 		}
 	}
 	/* END EON CODEC BLOCK */
 	
 	
 	// pad		-- extend to size
 	// chop		-- limit to size
 	// shift	-- shift one off the beginning
 	// unshift	-- prepend one to the beginning
 	// pop		-- pop one off the end
 	// push		-- push one onto the end
 	// slide	-- unshift and pop equal amounts
 	// rslide	-- reverse slide (push and shift equal amounts)
 	
 	public void pad(int $size) {
 		if ($len >= $size) return;
 		$len = $size;
 	}
 	
 	/**
 	 * @return a reference to self.
 	 */
 	public BitVector chop(int $size) {
 		if ($len <= $size) return this;
 		$bs.clear($size, $len);
 		$len = $size;
 		return this;
 	}
 	
 	/**
 	 * Shift a bit off the beginning of the vector
 	 * 
 	 * @return the first bit
 	 */
 	public boolean shift() {
 		if (length() <= 0) throw new IndexOutOfBoundsException("cannot shift from an empty list");
 		boolean $v = $bs.get(0);
 		$bs = $bs.get(1, $bs.length());
 		$len--;
 		return $v;
 	}
 	
 	/**
 	 * Shift some number of bits off the beginning of the vector
 	 * 
 	 * @return a BitVector of the shifted bits
 	 * @param $bits
 	 *                the number of bits to shift
 	 */
 	public BitVector shift(int $bits) {
 		BitVector $v = get(0, $bits);
 		$bs = $bs.get($bits, $bs.length());
 		$len -= $bits;
 		return $v;
 	}
 	
 	/**
 	 * Prepends the given value to the beginning of the vector (effectively
 	 * incrementing the index of all other values).
 	 * 
 	 * As currently implemented, this is almost painfully inefficient. If at all
 	 * possible, PLEASE use the other methods that can do things in batches.
 	 * 
 	 * @param $b
 	 *                the value to prepend
 	 */
 	public void unshift(boolean $b) {
 		emptyUnshift(1);
 		$bs.set(0, $b);
 	}
 	
 	/**
 	 * Functions as per unshift, but prepending the given number of 'off' bits.
 	 * 
 	 * @param $size
 	 */
 	public void emptyUnshift(int $size) {
 		BitSet $old = $bs;
 		$bs = new BitSet();
 		for (int $i = 0; $i < $old.length(); $i++)
 			$bs.set($i + $size, $old.get($i));
 		$len += $size;
 	}
 	
 	public void unshift(String $s) {
 		int $n = $s.length();
 		emptyUnshift($n);
 		for (int $i = 0; $i < $n; $i++)
 			if ($s.charAt($i) != '0') $bs.set($i);
 	}
 	
 	public void unshift(BitVector $bv) {
 		int $n = $bv.length();
 		emptyUnshift($n);
 		for (int $i = 0; $i < $n; $i++)
 			set($i, $bv.get($i));
 	}
 	
 	/**
 	 * Pops a value off the end of the vector.
 	 * 
 	 * @return the last value
 	 */
 	public boolean pop() {
 		boolean $v = get(length() - 1);
 		set(length() - 1, false);
 		$len--;
 		return $v;
 	}
 	
 	/**
 	 * Pop some number of bits off the end of the vector
 	 * 
 	 * @return a BitVector of the popped bits
 	 * @param $bits
 	 *                the number of bits to pop
 	 */
 	public BitVector pop(int $bits) {
 		int $k = length() - $bits;
 		BitVector $v = get($k, length());
 		$bs = $bs.get(0, $k);
 		$len -= $bits;
 		return $v;
 	}
 	
 	/**
 	 * Appends the given value to the end of the vector.
 	 * 
 	 * @param $b
 	 *                the value to append
 	 */
 	public void push(boolean $b) {
 		$bs.set($len, $b);
 		$len++;
 	}
 	
 	/**
 	 * Functions as per push, but appending the given number of 'off' bits.
 	 * 
 	 * @param $size
 	 */
 	public void emptyPush(int $size) {
 		$len += $size;
 	}
 	
 	public void push(String $s) {
 		int $n = $s.length();
 		for (int $i = 0; $i < $n; $i++) {
 			if ($s.charAt($i) != '0') $bs.set($len);
 			$len++;
 		}
 	}
 	
 	public void push(BitVector $bv) {
 		int $n = $bv.length();
 		for (int $i = 0; $i < $n; $i++) {
 			$bs.set($len, $bv.get($i));
 			$len++;
 		}
 	}
 	
 	/**
 	 * Effectively unshifts $size 'off' bits to the beginning of the the vector, while
 	 * simultaneously removing $size bits from the end.
 	 * 
 	 * Same as calling emptyUnshift($size) followed by pop($size), but more efficient.
 	 * 
 	 * @param $size
 	 *                number of bits to move
 	 */
 	public void emptySlide(int $size) {
 		BitSet $old = $bs;
 		int $end = $old.length() - $size;
 		$bs = new BitSet();
 		for (int $i = 0; $i < $end; $i++)
 			$bs.set($i + $size, $old.get($i));
 	}
 	
 	/**
 	 * Unshift and pop equal amounts.
 	 * 
 	 * @param $bv
 	 *                the bits to add; its length is how much will be popped off and
 	 *                returned.
 	 * @return the BitVector of those bits popped of the end
 	 */
 	public BitVector slide(BitVector $bv) {
 		BitVector $v = pop($bv.length());
 		unshift($bv);
 		return $v;
 	}
 	
 	/**
 	 * Reverse slide; push and shift in equal amounts.
 	 * 
 	 * @param $bv
 	 *                the bits to append; its length is how much will be shifted off
 	 *                and returned.
 	 * @return the BitVector of those bits shifted off the front
 	 */
 	public BitVector rslide(BitVector $bv) {
 		BitVector $v = shift($bv.length());
 		push($bv);
 		return $v;
 	}
 	
 	/**
 	 * Randomly flips bits in the given range.
 	 * 
 	 * @param $fromIndex
 	 *                index to start flipping at (inclusive)
 	 * @param $toIndex
 	 *                index to stop flipping at (exclusive)
 	 * @param $rand
 	 *                seed
 	 */
 	public void randomize(int $fromIndex, int $toIndex, java.util.Random $rand) {
 		for (int $i = $fromIndex; $i < $toIndex; $i++)
 			if ($rand.nextBoolean()) $bs.flip($i);
 		if ($toIndex > $len) $len = $toIndex;
 	}
 	
 	/**
 	 * Randomly flips all bits in the vector.
 	 * 
 	 * Same as calling this.randomize(0,this.length(),new java.util.Random());
 	 */
 	public void randomize() {
 		randomize(0, $len, new java.util.Random());
 	}
 	
 	
 
 	// converters
 	
 	public String toString() {
 		StringBuilder $sb = new StringBuilder(length());
 		for (int i = 0; i < length(); i++)
 			if (get(i)) $sb.append('1');
 			else $sb.append('0');
 		return $sb.toString();
 	}
 	
 	public byte[] toByteArray() {
 		int $n = (($len - 1) / 8) + 1;
 		byte[] $eax = new byte[$n];
 		for (int $i = 0; $i < $n; $i++) {
 			int $o = $i * 8;
 			if (get($o + 0)) $eax[$i] |= 128;
 			if (get($o + 1)) $eax[$i] |= 64;
 			if (get($o + 2)) $eax[$i] |= 32;
 			if (get($o + 3)) $eax[$i] |= 16;
 			if (get($o + 4)) $eax[$i] |= 8;
 			if (get($o + 5)) $eax[$i] |= 4;
 			if (get($o + 6)) $eax[$i] |= 2;
 			if (get($o + 7)) $eax[$i] |= 1;
 		}
 		return $eax;
 	}
 	
 	
 
 	// delegates
 	
 	public void and(BitVector $bv) {
 		$bs.and($bv.$bs);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public void andNot(BitVector $bv) {
 		$bs.andNot($bv.$bs);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public int cardinality() {
 		return $bs.cardinality();
 	}
 	
 	public void clear() {
 		$bs.clear();
 	}
 	
 	public void clear(int $fromIndex, int $toIndex) {
 		$bs.clear($fromIndex, $toIndex);
 	}
 	
 	public void clear(int $bitIndex) {
 		$bs.clear($bitIndex);
 	}
 	
 	public boolean equals(Object $obj) {
 		if (!($obj instanceof BitVector)) return false;
 		BitVector $bv = (BitVector) $obj;
 		if ($bv.length() != this.length()) return false;
 		return $bs.equals($bv.$bs);
 	}
 	
 	public int hashCode() {
 		return $bs.hashCode();
 	}
 	
 	public void flip(int $fromIndex, int $toIndex) {
 		$bs.flip($fromIndex, $toIndex);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public void flip(int $bitIndex) {
 		$bs.flip($bitIndex);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public BitVector get(int $fromIndex, int $toIndex) {
 		BitSet $t = $bs.get($fromIndex, $toIndex);
 		BitVector $v = new BitVector($t);
 		$v.$len = $toIndex - $fromIndex;
 		return $v;
 	}
 	
 	public boolean get(int $bitIndex) {
 		return $bs.get($bitIndex);
 	}
 	
 	public boolean intersects(BitVector $bv) {
 		return $bs.intersects($bv.$bs);
 	}
 	
 	public boolean isEmpty() {
 		return $bs.isEmpty();
 	}
 	
 	public int length() {
 		return $len;
 	}
 	
 	public int nextClearBit(int $fromIndex) {
 		return $bs.nextClearBit($fromIndex);
 	}
 	
 	public int nextSetBit(int $fromIndex) {
 		return $bs.nextSetBit($fromIndex);
 	}
 	
 	public void or(BitVector $bv) {
 		$bs.or($bv.$bs);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public void set(int $bitIndex, boolean $value) {
 		$bs.set($bitIndex, $value);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public void set(int $fromIndex, int $toIndex, boolean $value) {
 		$bs.set($fromIndex, $toIndex, $value);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public void set(int $fromIndex, int $toIndex) {
 		$bs.set($fromIndex, $toIndex);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public void set(int $bitIndex) {
 		$bs.set($bitIndex);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 	
 	public void xor(BitVector $bv) {
 		$bs.xor($bv.$bs);
 		if ($bs.length() > $len) $len = $bs.length();
 	}
 }
