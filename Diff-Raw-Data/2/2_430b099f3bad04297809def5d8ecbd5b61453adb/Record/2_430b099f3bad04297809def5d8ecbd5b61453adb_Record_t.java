 package edu.berkeley.gamesman.core;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * A Record stores a single game state's relevant information, possibly
  * including (but not limited to) primitive value, remoteness, etc.
  * 
  * Each Record has a list of fields specified in the Configuration object.
  * 
  * A Record is responsible for being able to write itself both to a Stream or a
  * ByteBuffer. These two different storage methods are not miscible.
  * 
  * @author Steven Schlansker
  */
 public final class Record {
 
 	private final Configuration conf;
 
 	private Map<RecordFields, Pair<Integer, Integer>> sf;
 
 
 	/*
 	 *  These three arrays are indexed by the physical on-disk
 	 *  ordering of the Record.  They store the type of the field
 	 *  (e.g. [Primitive value,Remotesness]), followed by the length
 	 *  of the field in bits (e.g. [2,6]), and finally the values
 	 *  (e.g. [Win,3] is Win in 3 moves)
 	 */
 
 	private RecordFields[] fieldNames;
 	private int[] fieldBitLength;
 	private long[] fieldValues;
 
 	private int recordBitLength; // Cached total length
 
 	/**
 	 * Convenience constructor for a Record with only a PrimitiveValue
 	 * specified.
 	 * 
 	 * @param conf2 Configuration of the database
 	 * @param primitiveValue The value of this record
 	 */
 	public Record(Configuration conf2, PrimitiveValue primitiveValue) {
 		conf = conf2;
 		setupBits();
 		set(RecordFields.VALUE, primitiveValue.value());
 	}
 
 	/**
 	 * Create a new Record
 	 * @param conf2 Configuration used to create the database
 	 */
 	public Record(Configuration conf2) {
 		conf = conf2;
 		setupBits();
 	}
 	
 	/**
 	 * Write this record to a DataOutput. Using this stream-based method causes
 	 * all output to be byte-aligned as it's not possible to easily seek/combine
 	 * adjacent records.
 	 * 
 	 * @param out The output to write to
 	 * @throws IOException Could not read from database
 	 * @see Record#readStream(Configuration,DataInput)
 	 */
 	public void writeStream(DataOutput out) throws IOException {
 		ByteBuffer b = ByteBuffer.allocate(bits2bytes(bitlength()) + 8);
 		write(b, 0);
 		byte[] arr = new byte[bits2bytes(bitlength())];
 		b.get(arr);
 		out.write(arr);
 	}
 
 	/**
 	 * Read a record from the front of a given DataInput
 	 * 
 	 * @param conf The configuration of the database
 	 * @param in The DataInput to read from
 	 * @return a new Record that was earlier stored with writeStream
 	 * @throws IOException Could not read from database
 	 * @see Record#writeStream(DataOutput)
 	 */
 	public static Record readStream(Configuration conf, DataInput in)
 			throws IOException {
 		Record r = new Record(conf);
 		r.readStream(in);
 		return r;
 	}
 
 	private void readStream(DataInput in) throws IOException {
 		ByteBuffer b = ByteBuffer.allocate(bits2bytes(bitlength()) + 8);
 		byte[] arr = new byte[bits2bytes(bitlength())];
 		in.readFully(arr);
 		b.put(arr);
 		read(b, 0);
 	}
 
 	/**
 	 * Write this record to a specific location in a ByteBuffer. This method
 	 * does not respect byte boundaries - if you have records of length 2 bits,
 	 * the 3rd record should be halfway through the first byte.
 	 * 
 	 * @param buf The buffer to write to
 	 * @param index The record number location to write
 	 * @see #read(Configuration, ByteBuffer, long)
 	 */
 	public void write(ByteBuffer buf, long index) {
 		long bitoff = index * bitlength();
 		for (int i = 0; i < fieldBitLength.length; i++) {
 			Util.debug(DebugFacility.RECORD, "Putting field " + fieldNames[i]
 					+ " (" + fieldValues[i] + ") to [" + bitoff + ":" + fieldBitLength[i]
 					+ "]");
 			BitBuffer.put(buf, bitoff, fieldBitLength[i], fieldValues[i]);
 			bitoff += fieldBitLength[i];
 		}
 	}
 
 	/**
 	 * Read a record from a specific index in a ByteBuffer.
 	 * 
 	 * @see #write(ByteBuffer, long)
 	 * @param conf The configuration of the database
 	 * @param buf The buffer to read from
 	 * @param index The location of the record
 	 * @return a new Record with the loaded data
 	 */
 	public static Record read(Configuration conf, ByteBuffer buf, long index) {
 		Record r = new Record(conf);
 		r.read(buf, index);
 		return r;
 	}
 
 	private void read(ByteBuffer buf, long index) {
 		long bitoff = index * bitlength();
 		for (int i = 0; i < fieldValues.length; i++) {
 			fieldValues[i] = BitBuffer.get(buf, bitoff, fieldBitLength[i]);
 			Util.debug(DebugFacility.BITWISE, String.format("Read field %d (bit %d) as 0x%x",i,bitoff,fieldValues[i]));
 			bitoff += fieldBitLength[i];
 		}
 	}
 
 	/**
 	 * @return the length of a single record in bits
 	 */
 	public final int bitlength() {
 		return recordBitLength;
 	}
 
 	private final int bits2bytes(int numbits) {
 		return (numbits + 7) / 8;
 	}
 
 	/**
 	 * Returns the length of a single record in bits
 	 * 
 	 * @param conf the Configuration to use for the record
 	 * @return length in bits
 	 */
 	public static int bitlength(final Configuration conf) {
 		return new Record(conf).bitlength();
 	}
 
 	/**
 	 * @return the length of a single record in bits
 	 */
 	// public final int length() {
 	// return bits.length;
 	// //return (maxbits + 7) / 8;
 	// }
 	/**
 	 * @param conf a Configuration
 	 * @return the length of any record created with the specified Configuration
 	 */
 	// public static int length(final Configuration conf){
 	// return new Record(conf).length();
 	// }
 	/**
 	 * Set a field in this Record
 	 * 
 	 * @param field which field to set
 	 * @param value the value to store
 	 */
 	public final void set(final RecordFields field, final long value) {
 		fieldValues[sf.get(field).car] = value;
 		Util.debug(DebugFacility.RECORD,"Record sets "+field+" to "+value);
 	}
 
 	/**
 	 * Get a field from this Record
 	 * 
 	 * @param field the field to get
 	 * @return the value of that field
 	 */
 	public final long get(final RecordFields field) {
 		Pair<Integer, Integer> f = sf.get(field);
 		if(f == null)
 			return -1;
 		Util.debug(DebugFacility.RECORD,"Record gets "+field+" as "+fieldValues[f.car]);
 		return fieldValues[f.car];
 	}
 	
 	/**
 	 * @return the value of this position
 	 */
 	public final PrimitiveValue get(){
 		int i = (int)get(RecordFields.VALUE);
 		if(i == -1) return null;
 		return PrimitiveValue.values()[i];
 	}
 
 	private final void setupBits() {
 		sf = conf.getStoredFields();
 
 		int[] bitOffset = new int[sf.size()];
 		fieldBitLength = new int[sf.size()];
 		fieldValues = new long[sf.size()];
 		fieldNames = new RecordFields[sf.size()];
 
 		for (RecordFields key : sf.keySet()) {
 			final Pair<Integer, Integer> info = sf.get(key);
 			fieldBitLength[info.car] = info.cdr;
 			bitOffset[info.car] = (info.car > 0 ? bitOffset[info.car - 1]
 					+ fieldBitLength[info.car - 1] : 0);
 			fieldNames[info.car] = key;
 		}
 		recordBitLength = bitOffset[bitOffset.length - 1] + fieldBitLength[fieldBitLength.length - 1];
 
 		// for(int i = 0; i < sf.size(); i++){
 		// System.out.println(RecordFields.values()[i]+" ("+bitOffset[i]+") = "+bits[i]);
 		// }
 		// Util.debug(DebugFacility.Record,
 		// "Records are "+bitlength()+" bits long");
 	}
 
 	/**
 	 * Combine a list of records into one single record at a higher level. This
 	 * is the basic operation for a game tree - the use case is that a single
 	 * record for a state is created from the records of all its child game
 	 * states
 	 * 
 	 * @param conf the Configuration of the database
 	 * @param vals the child records we'd like to combine
 	 * @return a new Record that has the fields of the child Records
 	 */
 	public static Record combine(final Configuration conf,
 			final List<Record> vals) {
 		Record rec = new Record(conf);
 
 		final EnumMap<RecordFields, ArrayList<Long>> map = new EnumMap<RecordFields, ArrayList<Long>>(
 				RecordFields.class);
 		for (RecordFields rf : conf.getStoredFields().keySet()) {
 			map.put(rf, new ArrayList<Long>(vals.size()));
 		}
 		for (Record r : vals) {
 			for (RecordFields rf : conf.getStoredFields().keySet()) {
 				map.get(rf).add(r.get(rf));
 			}
 		}
 		
 		PrimitiveValue pv;
 		if(!conf.getStoredFields().containsKey(RecordFields.VALUE))
 			pv = null;
 		else
 			pv = primitiveCombine(map.get(RecordFields.VALUE));
 
 		for (RecordFields rf : conf.getStoredFields().keySet()) {
 			switch (rf) {
 			case VALUE:
 				rec.set(rf, pv.value());
 				break;
 			case REMOTENESS:
 				if(!conf.getStoredFields().containsKey(RecordFields.VALUE))
 					rec.set(rf, Collections.min(map.get(rf)) + 1);
 				else{
 					switch(pv){
 					case TIE:
 						int max = 0;
 						for(Record r : vals)
 							if(r.get() == PrimitiveValue.TIE)
 								max = (int) Math.max(max, r.get(RecordFields.REMOTENESS));
 						rec.set(RecordFields.REMOTENESS, max+1);
 						break;
 					case LOSE:
 						rec.set(rf, Collections.min(map.get(rf)) + 1);
 						break;
 					case WIN:
 						int min = Integer.MAX_VALUE;
 						for(Record r : vals)
 							if(r.get() == PrimitiveValue.LOSE)
								min = (int) Math.min(min, r.get(RecordFields.REMOTENESS));
 						rec.set(RecordFields.REMOTENESS, min+1);
 						break;
 					case UNDECIDED:
 						break;
 					}
 				}
 				break;
 			default:
 				Util.fatalError("Default case shouldn't have been reached");
 			}
 		}
 
 		return rec;
 	}
 
 	private static PrimitiveValue primitiveCombine(List<Long> vals) {
 		boolean seentie = false;
 		for (Long iv : vals) {
 			PrimitiveValue v = PrimitiveValue.values()[iv.intValue()];
 			switch (v) {
 			case UNDECIDED:
 				return PrimitiveValue.UNDECIDED;
 			case LOSE:
 				return PrimitiveValue.WIN;
 			case TIE:
 				seentie = true;
 				break;
 			case WIN:
 				break;
 			default:
 				Util.fatalError("Illegal primitive value: " + iv);
 			}
 		}
 		if (seentie)
 			return PrimitiveValue.TIE;
 		return PrimitiveValue.LOSE;
 	}
 
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		//System.out.println(Arrays.toString(fieldValues));
 		for (int fieldnum = 0; fieldnum < fieldValues.length; fieldnum++) {
 			if (sf.get(RecordFields.VALUE).car == fieldnum)
 				b.append(PrimitiveValue.values()[(int) fieldValues[fieldnum]]);
 			else
 				b.append(fieldValues[fieldnum]);
 			b.append(" ");
 		}
 		return b.toString();
 	}
 
 	/**
 	 * Take all the field values and put them next to each other (bitwise) into a BigInteger.
 	 * 
 	 * @author Alex Trofimov
 	 * @return BigInteger of fieldValues packed together. Leading zeros truncated.
 	 */
 	public BigInteger toBigInteger() {
 		BigInteger r = BigInteger.ZERO;
 		int bitoff = 0;
 		long value;
 		for (int i = 0; i < fieldBitLength.length; i ++) {
 			r = r.shiftLeft(bitoff);
 			value = fieldValues[i] &((1 << fieldBitLength[i]) - 1);
 			r = r.add(BigInteger.valueOf(value));
 			bitoff = fieldBitLength[i];
 		}		
 		return r;
 	}
 	
 	/**
 	 * Take a BigInteger with bits correponding to fieldValues and load the bits from it.
 	 * 
 	 * @author Alex Trofimov
 	 * @param data - BigInteger with fieldValues bits contcatenated together as one number.
 	 */
 	public void loadBigInteger(BigInteger data) {
 		for (int i = this.fieldBitLength.length - 1; i >= 0; i --) {
 			this.fieldValues[i] = data.longValue() & ((1 << this.fieldBitLength[i]) - 1);
 			data = data.shiftRight(this.fieldBitLength[i]);
 		}
 	}
 	
 	/**
 	 * A class that behaves almost entirely like a ByteBuffer but with bit-indexed positions
 	 * instead.
 	 * @see ByteBuffer
 	 * @author Steven Schlansker
 	 */
 	
 	private static final class BitBuffer {
 		private BitBuffer() {
 		}
 
 		static private long mask(int firstbit, int lastbit) {
 			return -1l << (64 - lastbit + firstbit) >>> firstbit; // QuickFix by Alex
 			// return (Util.longpow(2, lastbit - firstbit) - 1) << (64 - (lastbit));
 			// return (Util.longpow(2, 64-firstbit)-1) ^
 			// (Util.longpow(2,64-lastbit)-1);
 		}
 
 		static long get(ByteBuffer b, long offa, int len) {
 			Util.assertTrue(len <= 58,
 					"Don't support more than 58-bit fields yet :(");
 			long combine = b.getLong((int) (offa / 8));
 			int off = (int) (offa % 8);
 			if (off == 0) {
 				// Do nothing
 			} else if (off + len < 64) {
 				combine &= mask(off, off + len);
 			}/*
 			 * else{ long l2 = b.getLong((int)(offa/8+(64/8))); combine &=
 			 * mask(off,off+len); combine |= l2 >>> (64-off); }
 			 */
 			return combine >>> (64 - off - len);
 		}
 
 		static void put(ByteBuffer b, long offa, int len, long vala) {
 			Util.assertTrue(len <= 58,
 					"Don't support more than 58-bit fields yet :(");
 			int off = (int) (offa % 8);
 			long val = vala & (1 << len) - 1;
 			if (off + len < 64) {
 				long l1 = b.getLong((int) (offa / 8));
 				Util.debug(DebugFacility.BITWISE, String.format("Read 0x%x from 0x%x mask=0x%x -> 0x%x",
 						l1,(offa/8),~(mask(off, off + len)),l1 & ~(mask(off, off + len))));
 				l1 &= ~(mask(off, off + len));
 				l1 |= val << (64 - len - off);
 				b.putLong((int) (offa / 8), l1);
 				Util.debug(DebugFacility.BITWISE, String.format("Putting 0x%x -> 0x%x to 0x%x",val << (64 - len - off),l1,(offa/8)));
 			} else {
 				Util.fatalError("abort");
 			}
 		}
 
 		public static void main(String args[]) throws IOException {
 			ByteBuffer buf = ByteBuffer.allocate(32 + 8);
 			LineNumberReader in = new LineNumberReader(new InputStreamReader(
 					System.in));
 			while (true) {
 				for (int i = 0; i < 32; i++) {
 					System.out.format("%02x", buf.get(i));
 					if (i % 4 == 3)
 						System.out.print(" ");
 				}
 				System.out.print("\n>");
 				String line = in.readLine();
 				String[] bits = line.split(" ");
 				if (bits[0].startsWith("g")) {
 					System.out.format("%x\n", get(buf, Long.parseLong(bits[1]),
 							Integer.parseInt(bits[2])));
 				}
 				if (bits[0].startsWith("p")) {
 					put(buf, Long.parseLong(bits[1]),
 							Integer.parseInt(bits[2]), Long.parseLong(bits[3],
 									16));
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param other Another Record
 	 * @return Checks based on primitive value and then remoteness if this
 	 *         record is "preferable to another"
 	 */
 	public boolean isPreferableTo(Record other) {
 		if (get().isPreferableTo(other.get()))
 			return true;
 		else if (other.get().isPreferableTo(get()))
 			return false;
 		else {
 			if (get().equals(PrimitiveValue.WIN))
 				if (get(RecordFields.REMOTENESS) < other
 						.get(RecordFields.REMOTENESS))
 					return true;
 				else
 					return false;
 			else if (get().equals(PrimitiveValue.LOSE))
 				if (get(RecordFields.REMOTENESS) > other
 						.get(RecordFields.REMOTENESS))
 					return true;
 				else
 					return false;
 			else
 				return false;
 		}
 	}
 }
