 package suite.primitive;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Iterator;
 
 import suite.util.Copy;
 import suite.util.Util;
 
 public class Chars implements Iterable<Character> {
 
 	private char vector[]; // Immutable
 	private int start, end;
 
 	private static final char emptyCharArray[] = new char[0];
 	private static final int reallocSize = 65536;
 
 	public static final Chars emptyChars = new Chars(emptyCharArray);
 
 	public static final Comparator<Chars> comparator = new Comparator<Chars>() {
 		public int compare(Chars chars0, Chars chars1) {
 			int start0 = chars0.start, start1 = chars1.start;
 			int size0 = chars0.size(), size1 = chars1.size(), minSize = Math.min(size0, size1);
 			int index = 0, c = 0;
 
 			while (c == 0 && index < minSize) {
 				char b0 = chars0.vector[start0 + index];
 				char b1 = chars1.vector[start1 + index];
 				c = b0 == b1 ? 0 : b0 > b1 ? 1 : -1;
 				index++;
 			}
 
 			return c != 0 ? c : size0 - size1;
 		}
 	};
 
 	public Chars(Chars chars) {
 		this(chars.vector, chars.start, chars.end);
 	}
 
 	public Chars(char chars[]) {
 		this(chars, 0);
 	}
 
 	public Chars(char chars[], int start) {
 		this(chars, start, chars.length);
 	}
 
 	public Chars(char chars[], int start, int end) {
 		this.vector = chars;
 		this.start = start;
 		this.end = end;
 	}
 
 	public Chars append(Chars a) {
 		int size0 = size(), size1 = a.size(), newSize = size0 + size1;
 		char nb[] = new char[newSize];
 		System.arraycopy(vector, start, nb, 0, size0);
 		System.arraycopy(a.vector, a.start, nb, size0, size1);
 		return new Chars(nb);
 	}
 
 	public static Chars asList(char... in) {
 		return new Chars(in);
 	}
 
 	public char get(int index) {
 		int i1 = index + start;
 		checkClosedBounds(i1);
 		return vector[i1];
 	}
 
 	public boolean isEmpty() {
 		return start >= end;
 	}
 
 	public int size() {
 		return end - start;
 	}
 
 	public Chars subchars(int s) {
 		return subchars0(start + s, end);
 	}
 
 	public Chars subchars(int s, int e) {
 		if (s < 0)
 			s += size();
 		if (e < s)
 			e += size();
 
 		return subchars0(start + s, start + e);
 	}
 
 	@Override
 	public Iterator<Character> iterator() {
 		return new Iterator<Character>() {
 			private int pos = start;
 
 			public boolean hasNext() {
 				return pos < end;
 			}
 
 			public Character next() {
 				return vector[pos++];
 			}
 
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	@Override
 	public int hashCode() {
 		int result = 1;
 		for (int i = start; i < end; i++)
 			result = 31 * result + vector[i];
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		if (Util.clazz(object) == Chars.class) {
 			Chars other = (Chars) object;
 			int diff = other.start - start;
 
 			for (int i = start; i < end; i++)
 				if (vector[i] != other.vector[i + diff])
 					return false;
 
 			return true;
 		} else
 			return false;
 	}
 
 	private Chars subchars0(int start, int end) {
 		checkOpenBounds(start);
 		checkOpenBounds(end);
 		Chars result = new Chars(vector, start, end);
 
 		// Avoid small pack of chars object keeping a large buffer
 		if (vector.length >= reallocSize && end - start < reallocSize / 4)
 			result = emptyChars.append(result); // Do not share reference
 
 		return result;
 	}
 
 	private void checkOpenBounds(int index) {
 		if (index < start || index > end)
 			throw new IndexOutOfBoundsException("Index " + (index - start) + " is not within [0-" + (end - start) + "}");
 	}
 
 	private void checkClosedBounds(int index) {
 		if (index < start || index >= end)
 			throw new IndexOutOfBoundsException("Index " + (index - start) + " is not within [0-" + (end - start) + "]");
 	}
 
 	@Override
 	public String toString() {
		return new String(vector);
 	}
 
 	public char[] getChars() {
 		if (start != 0 || end != vector.length)
 			return Arrays.copyOfRange(vector, start, end);
 		else
 			return vector;
 	}
 
 	public static class CharsBuilder {
 		private char chars[] = emptyCharArray;
 		private int size;
 
 		public CharsBuilder append(Chars b) {
 			return append(b.vector, b.start, b.end);
 		}
 
 		public CharsBuilder append(char b) {
 			extendBuffer(size + 1);
 			chars[size++] = b;
 			return this;
 		}
 
 		public CharsBuilder append(char b[]) {
 			return append(b, 0, b.length);
 		}
 
 		public CharsBuilder append(char b[], int start, int end) {
 			int inc = end - start;
 			extendBuffer(size + inc);
 			Copy.primitiveArray(b, start, chars, size, inc);
 			size += inc;
 			return this;
 		}
 
 		public void clear() {
 			size = 0;
 		}
 
 		public void extend(int size1) {
 			extendBuffer(size1);
 			size = size1;
 		}
 
 		public int size() {
 			return size;
 		}
 
 		public Chars toChars() {
 			return new Chars(chars, 0, size);
 		}
 
 		private void extendBuffer(int capacity1) {
 			int capacity0 = chars.length;
 
 			if (capacity0 < capacity1) {
 				int capacity = Math.max(capacity0, 4);
 				while (capacity < capacity1)
 					capacity = capacity < 4096 ? capacity << 1 : capacity * 3 / 2;
 
 				char chars1[] = new char[capacity];
 				Copy.primitiveArray(chars, 0, chars1, 0, size);
 				chars = chars1;
 			}
 		}
 	}
 
 }
