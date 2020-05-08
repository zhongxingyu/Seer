 package descent.internal.compiler.parser;
 
 import java.util.Arrays;
 
 import descent.core.compiler.CharOperation;
 
 /**
  * Hashtable of {String --> int }
  */
 public final class HashtableOfCharArrayAndObject implements Cloneable {
 	public static final Object MISSING_ELEMENT = null;
 
 	// to avoid using Enumerations, walk the individual tables skipping nulls
 	private char[] keyTable[];
 	private Object valueTable[];
 
 	private int elementSize; // number of elements in the table
 	private int threshold;
 	private static final float GROWTH_FACTOR = 1.33f;
 
 	public HashtableOfCharArrayAndObject() {
 		this(13);
 	}
 
 	public HashtableOfCharArrayAndObject(int size) {
 		this.elementSize = 0;
 		this.threshold = size; // size represents the expected number of elements
 		int extraRoom = (int) (size * 1.75f);
 		if (this.threshold == extraRoom)
 			extraRoom++;
 		this.keyTable = new char[extraRoom][];
 		this.valueTable = new Object[extraRoom];
 	}
 
 	public Object clone() throws CloneNotSupportedException {
 		throw new CloneNotSupportedException();
 		//		HashtableOfStringAndInt result = (HashtableOfStringAndInt) super.clone();
 		//		result.elementSize = this.elementSize;
 		//		result.threshold = this.threshold;
 		//
 		//		int length = this.keyTable.length;
 		//		result.keyTable = new char[length][];
 		//		System.arraycopy(this.keyTable, 0, result.keyTable, 0, length);
 		//
 		//		length = this.valueTable.length;
 		//		result.valueTable = new Object[length];
 		//		System.arraycopy(this.valueTable, 0, result.valueTable, 0, length);
 		//		return result;
 	}
 
 	public boolean containsKey(char[] key) {
		int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
 		int keyLength = key.length;
 		char[] currentKey;
 		while ((currentKey = keyTable[index]) != null) {
 			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
 				return true;
 			index = (index + 1) % keyTable.length;
 		}
 		return false;
 	}
 
 	public Object get(char[] key) {
		int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
 		int keyLength = key.length;
 		char[] currentKey;
 		while ((currentKey = keyTable[index]) != null) {
 			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
 				return valueTable[index];
 			index = (index + 1) % keyTable.length;
 		}
 		return MISSING_ELEMENT;
 	}
 
 	public Object put(char[] key, Object value) {
		int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
 		int keyLength = key.length;
 		char[] currentKey;
 		while ((currentKey = keyTable[index]) != null) {
 			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key))
 				return valueTable[index] = value;
 			index = (index + 1) % keyTable.length;
 		}
 		keyTable[index] = key;
 		valueTable[index] = value;
 
 		// assumes the threshold is never equal to the size of the table
 		if (++elementSize > threshold)
 			rehash();
 		return value;
 	}
 
 	public Object removeKey(char[] key) {
 		int index = (Arrays.hashCode(key) & 0x7FFFFFFF) % valueTable.length;
 		int keyLength = key.length;
 		char[] currentKey;
 		while ((currentKey = keyTable[index]) != null) {
 			if (currentKey.length == keyLength && CharOperation.equals(currentKey, key)) {
 				Object value = valueTable[index];
 				elementSize--;
 				keyTable[index] = null;
 				valueTable[index] = MISSING_ELEMENT;
 				rehash();
 				return value;
 			}
 			index = (index + 1) % keyTable.length;
 		}
 		return MISSING_ELEMENT;
 	}
 
 	private void rehash() {
 		HashtableOfCharArrayAndObject newHashtable = new HashtableOfCharArrayAndObject((int) (elementSize * GROWTH_FACTOR));
 		char[] currentKey;
 		for (int i = keyTable.length; --i >= 0;)
 			if ((currentKey = keyTable[i]) != null)
 				newHashtable.put(currentKey, valueTable[i]);
 
 		this.keyTable = newHashtable.keyTable;
 		this.valueTable = newHashtable.valueTable;
 		this.threshold = newHashtable.threshold;
 	}
 
 	public int size() {
 		return elementSize;
 	}
 
 	public String toString() {
 		String s = ""; //$NON-NLS-1$
 		Object object;
 		for (int i = 0, length = valueTable.length; i < length; i++)
 			if ((object = valueTable[i]) != MISSING_ELEMENT)
 				s += new String(keyTable[i]) + " -> " + object + "\n"; //$NON-NLS-2$ //$NON-NLS-1$
 		return s;
 	}
 
 	public Object[] getValues() {
 		int keyTableLength = keyTable.length;
 		Object[] result = new Object[size()];
 		int j = 0;
 		for (int i = 0; i < keyTableLength; i++) {
 			if (keyTable[i] != null)
 				result[j++] = valueTable[i];
 		}
 		return result;
 	}
 
 }
