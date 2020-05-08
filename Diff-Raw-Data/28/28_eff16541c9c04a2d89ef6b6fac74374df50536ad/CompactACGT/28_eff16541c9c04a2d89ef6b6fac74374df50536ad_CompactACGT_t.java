 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // CompactACGT.java
 // Since: Feb 22, 2010
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.format.fasta;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.utgenome.util.kmer.KmerIntegerFactory;
 
 /**
  * Compact array for ACGT (and N) sequences
  * 
  * @author leo
  * 
  */
 public class CompactACGT implements GenomeSequence {
 
 	private final byte[] sequence; // 2 bit for each char
 	private final byte[] sequenceMask; // 1 bit for each char: 0 for ACGT, 1 for otherwise including N
 	private final int length;
 	private final int offset;
 
 	private final static int CODE_SIZE = 2;
 	private final static int BYTE = 8;
 	private final static char[] ACGT = { 'A', 'C', 'G', 'T' };
 
 	/**
 	 * @param sequence
 	 *            packed sequence
 	 * @param sequenceMask
 	 *            packed sequence for N
 	 * @param bitOffset
 	 *            offset char length in the packed array
 	 * @throws UTGBException
 	 */
 	CompactACGT(byte[] sequence, byte[] sequenceMask, int length, int offset) throws UTGBException {
 		this.sequence = sequence;
 		this.sequenceMask = sequenceMask;
 
 		this.length = length;
 		this.offset = offset;
 
 		// assertion
 		if ((this.sequence.length * (BYTE / CODE_SIZE)) - offset < length) {
 			throw new UTGBException(UTGBErrorCode.INVALID_INPUT, String.format("packed array is shorter than the specified length: %d (offset=%d) < %d",
 					sequence.length * (BYTE / CODE_SIZE), offset, length));
 		}
 		if ((this.sequenceMask.length * BYTE - offset) < length) {
 			throw new UTGBException(UTGBErrorCode.INVALID_INPUT, String.format("invalid mask binary: buf size=%d (offset=%d), length=%d", sequenceMask.length,
 					offset, length));
 		}
 	}
 
 	class KmerIterator implements OverlappingKmerIterator {
 		private final int K;
 		private final KmerIntegerFactory f;
 		private int start;
 
 		public KmerIterator(int K) {
 			this.K = K;
 			this.f = new KmerIntegerFactory(K);
 			int start = offset;
 		}
 
 		public CompactACGT nextKmer() {
 
 			if (start + K > length)
 				return null;
 
 			try {
 				CompactACGT kmer = new CompactACGT(sequence, sequenceMask, K, CompactACGT.this.offset + start++);
 				return kmer;
 			}
 			catch (UTGBException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 
 	}
 
 	/**
 	 * return a subsequence of this ACGT string. The returned CompactACGT holds the references to the sequence byte
 	 * arrays, and no buffer copy will be performed.
 	 * 
 	 * @param start
 	 * @param length
 	 * @return
 	 * @throws UTGBException
 	 */
 	public CompactACGT subSequence(int start, int length) throws UTGBException {
 		return new CompactACGT(sequence, sequenceMask, length, start + this.offset);
 	}
 
 	public CompactACGT reverseComplement() throws UTGBException {
 		ByteArrayOutputStream seqOut = new ByteArrayOutputStream();
 		ByteArrayOutputStream nSeqOut = new ByteArrayOutputStream();
 		try {
 
 			CompactACGTWriter w = new CompactACGTWriter(seqOut, nSeqOut);
 			// TODO byte-wise transformation
 			for (int i = length - 1; i >= 0; i--) {
 				int c = code2bitAt(i);
 				c = ~c & 0x03;
 				w.append2bit((byte) c);
 			}
 			w.close();
 			return new CompactACGT(seqOut.toByteArray(), nSeqOut.toByteArray(), length, 0);
 		}
 		catch (IOException e) {
 			throw UTGBException.convert(e);
 		}
 	}
 
 	public OverlappingKmerIterator getKmerIterator(int K) {
 		return new KmerIterator(K);
 	}
 
 	public static CompactACGT createFromString(String seq) throws IOException, UTGBException {
 
 		ByteArrayOutputStream seqOut = new ByteArrayOutputStream();
 		ByteArrayOutputStream nSeqOut = new ByteArrayOutputStream();
 		try {
 			CompactACGTWriter w = new CompactACGTWriter(seqOut, nSeqOut);
 			w.append(seq);
 			w.close();
 			return new CompactACGT(seqOut.toByteArray(), nSeqOut.toByteArray(), seq.length(), 0);
 		}
 		catch (IOException e) {
 			throw UTGBException.convert(e);
 		}
 
 	}
 
 	public int length() {
 		return length;
 	}
 
 	public char charAt(int index) {
		int acgtIndex = code2bitAt(index);
		if (acgtIndex == -1)
			return 'N';
		else
			return ACGT[acgtIndex];
 	}
 
	/**
	 * Return 2bit code at the index on the genome. If the base character at the position is not an ACGT, returns -1
	 * 
	 * @param index
	 * @return 2bit code or -1 (when 'N')
	 */
 	int code2bitAt(int index) {
 		int x = index + offset;
 
 		int maskPos = x / 8;
 		int maskOffset = x % 8;
 		if ((sequenceMask[maskPos] >>> (7 - maskOffset) & 0x01) != 0)
			return -1;
 
 		int bPos = x / 4;
 		int bOffset = x % 4;
 
 		return (sequence[bPos] >>> (6 - bOffset * 2)) & 0x03;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder buf = new StringBuilder();
 		for (int i = 0; i < length; i++) {
 			buf.append(charAt(i));
 		}
 		return buf.toString();
 	}
 
 }
