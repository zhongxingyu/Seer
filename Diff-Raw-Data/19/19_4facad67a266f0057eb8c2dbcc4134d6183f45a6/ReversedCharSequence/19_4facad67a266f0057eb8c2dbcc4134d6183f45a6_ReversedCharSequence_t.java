 package dfh.grammar;
 
 /**
  * {@link CharSequence} that allows the iteration over another
  * {@link CharSequence} in reverse. This is required for variable-length look
  * behind assertions.
  * 
  * Note that this class merely tinkers with the index math to retrieve
  * characters from the underlying sequence.
  * 
  * <b>Creation date:</b> Apr 10, 2011
  * 
  * @author David Houghton
  * 
  */
 public class ReversedCharSequence implements CharSequence {
 
 	private final int zero, end, length;
 	private final CharSequence s;
 	private final boolean reversed;
 
 	private ReversedCharSequence(int zero, int end, int length, CharSequence s,
 			boolean reversed) {
 		this.zero = zero;
 		this.end = end;
 		this.length = length;
 		this.s = s;
 		this.reversed = reversed;
 	}
 
 	/**
 	 * @param s
 	 * @param offset
 	 *            the index of the character in the parent sequence immediately
 	 *            after the zero index character in the reversed sequence
 	 * @param end
 	 *            the index of the character in the parent sequence which is the
 	 *            earliest character that should appear in the reversed
 	 *            sequence; this is 0 if the reversed sequence should cover the
 	 *            entire beginning of the parent sequence
 	 */
 	public ReversedCharSequence(CharSequence s, int offset, int end) {
 		this.s = s;
 		this.end = end - 1;
 		this.reversed = s instanceof ReversedCharSequence ? !((ReversedCharSequence) s)
 				.isReversed() : true;
 		if (end < -1 || offset > s.length()) {
 			String baseError = "reversed character sequence must be a subsequence of parent sequence";
 			if (end < -1)
 				throw new IndexOutOfBoundsException(baseError
 						+ "; specified end is before parent sequence beginning");
 			else
 				throw new IndexOutOfBoundsException(
 						baseError
 								+ "; specified start offset is after parent sequence end");
 		}
 		zero = offset - 1;
 		length = zero - this.end;
 	}
 
 	/**
 	 * Reverses input from character before offset to beginning.
 	 * 
 	 * @param s
 	 *            sequence to reverse
 	 * @param offset
 	 *            character immediately *before* first character appearing in
 	 *            reversed sequence, which means index after last character
 	 *            reversed
 	 */
 	public ReversedCharSequence(CharSequence s, int offset) {
 		this(s, offset, 0);
 	}
 
 	/**
 	 * Reverses input sequence.
 	 * 
 	 * @param s
 	 *            sequence to reverse
 	 */
 	public ReversedCharSequence(CharSequence s) {
 		this(s, s.length(), 0);
 	}
 
 	@Override
 	public int length() {
 		return length;
 	}
 
 	@Override
 	public char charAt(int index) {
 		return s.charAt(zero - index);
 	}
 
 	/**
 	 * Returns underlying index and reading direction relative to reversed
 	 * sequence.
 	 * 
 	 * @param index
 	 * @return corresponding index in bottommost character sequence and whether
 	 *         this sequence is in the same (0) or opposite (1) order to the
 	 *         base sequence
 	 */
 	public int[] deepTranslation(int index) {
 		ReversedCharSequence rcs = this;
 		int[] i = { index, 1 };
 		while (true) {
 			i[0] = rcs.zero - i[0];
 			if (rcs.underlyingSequence() instanceof ReversedCharSequence) {
 				rcs = (ReversedCharSequence) rcs.underlyingSequence();
 				i[1] = i[1] == 1 ? 0 : 1;
 			} else
 				break;
 		}
 		return i;
 	}
 
 	/**
 	 * Returns index of the corresponding character in the sequence upon which
 	 * the reversed sequence is based.
 	 * 
 	 * @param index
 	 *            index of some character in the reversed sequence
 	 * @return index of the corresponding character in the sequence upon which
 	 *         the reversed sequence is based
 	 */
 	public int translate(int index) {
 		return zero - index;
 	}
 
 	@Override
 	public CharSequence subSequence(int start, int end) {
		return new ReversedCharSequence(zero - start, zero - end, end - start,
				s, reversed);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		for (int i = zero; i > end; i--)
 			b.append(s.charAt(i));
 		return b.toString();
 	}
 
 	/**
 	 * @return {@link CharSequence} that has been reversed
 	 */
 	public CharSequence underlyingSequence() {
 		return s;
 	}
 
 	/**
 	 * @return {@link CharSequence} underlying this and any intervening reversed
 	 *         sequences
 	 */
 	public CharSequence bottomSequence() {
 		CharSequence seq = s;
 		while (s instanceof ReversedCharSequence) {
 			seq = ((ReversedCharSequence) seq).s;
 		}
 		return seq;
 	}
 
 	/**
 	 * Returns whether this sequence is reversed relative to the base sequence.
 	 * This will necessarily be true except within nested backwards assertions.
 	 * 
 	 * @return whether this sequence is reversed relative to the base sequence
 	 */
 	public boolean isReversed() {
 		return reversed;
 	}
 }
