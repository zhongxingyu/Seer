 package elw.dp.mips;
 
 import gnu.trove.TIntArrayList;
 import gnu.trove.TIntByteHashMap;
 import gnu.trove.TIntIntHashMap;
 
 import java.util.*;
 
 public class Memory {
 	protected TIntByteHashMap contentMap = new TIntByteHashMap();
 
 	protected TIntArrayList readAdresses = new TIntArrayList();
 	protected TIntArrayList writeAdresses = new TIntArrayList();
 
 	public int getWord(int address) {
 		readAdresses.add(address);
 
 		return getWordInternal(address);
 	}
 
 	public int getWordInternal(final int address) {
 		final byte byte0 = getByteInternal(address);
 		final byte byte1 = getByteInternal(address + 1);
 		final byte byte2 = getByteInternal(address + 2);
 		final byte byte3 = getByteInternal(address + 3);
 
 		return byte0 << 24 | byte1 << 16 | byte2 << 8 | byte3;
 	}
 
 	public int setWord(int address, int newWord) {
 		writeAdresses.add(address);
 		writeAdresses.add(address + 1);
 		writeAdresses.add(address + 2);
 		writeAdresses.add(address + 3);
 
 		return setWordInternal(address, newWord);
 	}
 
 	public int setWordInternal(int address, int newWord) {
 		final int oldWord = getWordInternal(address);
 
 		setByteInternal(address, (byte) (newWord >> 24));
 		setByteInternal(address + 1, (byte) (newWord >> 16 & 0xFF));
 		setByteInternal(address + 2, (byte) (newWord >> 8 & 0xFF));
 		setByteInternal(address + 3, (byte) (newWord & 0xFF));
 
 		return oldWord;
 	}
 
 	public byte getByteInternal(final int address) {
		final Byte contentByte = contentMap.get(address);

		if (contentByte != null) {
			return contentByte;
		}

		final byte newValue = getDefaultByte();

		contentMap.put(address, newValue);

		return newValue;
	}

	static byte getDefaultByte() {
		return (byte) (new Random().nextInt() & 0xFF);
 	}
 
 	byte setByteInternal(int address, byte value) {
		final Byte oldByte = contentMap.put(address, value);
		return oldByte != null ? oldByte : getDefaultByte();
 	}
 
 	public byte getByte(int address) {
 		readAdresses.add(address);
 
 		return getByteInternal(address);
 	}
 
 	public byte setByte(int address, byte newByte) {
 		writeAdresses.add(address);
 
 		return setByteInternal(address, newByte);
 	}
 
 	public TIntArrayList getReadAdresses() {
 		return readAdresses;
 	}
 
 	public TIntArrayList getWriteAdresses() {
 		return writeAdresses;
 	}
 
 	public void resetAccess() {
 		readAdresses.clear();
 		writeAdresses.clear();
 	}
 
 	public int getSize() {
 		return contentMap.size();
 	}
 
 	public int getAddressAt(int index) {
 		//  LATER optimize this
 		final int[] keys = contentMap.keys();
 		Arrays.sort(keys);
 
		return contentMap.get(keys[index]);
 	}
 
 	public void setData(final TIntIntHashMap newData) {
 		resetAccess();
 
 		final int[] addresses = newData.keys();
 		for (final int addr : addresses) {
 			setWord(addr, newData.get(addr));
 		}
 	}
 
 	public boolean hasWord(final int address) {
 		return hasByte(address) && hasByte(address + 1) && hasByte(address + 2) && hasByte(address + 3);
 	}
 
 	public boolean hasByte(final int address) {
 		return contentMap.containsKey(address);
 	}
 }
