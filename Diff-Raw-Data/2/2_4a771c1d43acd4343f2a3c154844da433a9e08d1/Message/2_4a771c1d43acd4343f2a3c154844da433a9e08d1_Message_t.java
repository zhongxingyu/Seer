 package Messages;
 
 public class Message {
 	int op = -1;
 	long length = -1;
 	long reserved = 0;
 	String options = "";
 	boolean correct = true;
 	public final static int minSize = 132;
 
 	public Message(int _op, long _length, long _reserved, String _options) {
 		op = _op;
 		length = _length;
 		reserved = _reserved;
 		options = _options;
 	}
 
 	public boolean getCorrect() {
 		return correct;
 	}
 
 	protected long fromByteArray(byte[] bytes) {
 		long total = 0;
 		for (int i = 0; i < bytes.length; i++) {
 			int store = (int) (Byte.valueOf(bytes[i]).intValue() * Math.pow(2,
 					8 * i));
 			if (store < 0) {
 				store = 127 - store;
 			}
 			total += store;
 		}
 		return total;
 	}
 
 	public byte[] convert() {
 		byte[] storage = new byte[minSize];
 		int total = 0;
 		byte[] temp = numToByte(op, 1);
 		for (int i = 0; i < temp.length; i++) {
 			storage[total + i] = temp[i];
 		}
 		total += temp.length;
 		temp = numToByte((int) length, 2);
 		for (int i = 0; i < temp.length; i++) {
 			storage[total + i] = temp[i];
 		}
 		total += temp.length;
 		temp = numToByte((int) reserved, 1);
 		for (int i = 0; i < temp.length; i++) {
 			storage[total + i] = temp[i];
 		}
 		total += temp.length;
 		temp = options.getBytes();
 		for (int i = 0; i < temp.length; i++) {
 			storage[total + i] = temp[i];
 		}
 		total += temp.length;
 		return storage;
 	}
 
 	public byte[] numToByte(int num, int numBytes) {
 		/*if (num >= 128) {
 			num += 1;
 		}*/
 		String numstr = Integer.toBinaryString(num);
 		while (numstr.length() % 8 != 0) {
 			numstr = "0".concat(numstr);
 		}
 		if (numstr.startsWith("1")) {
 			numstr = "-" + numstr.substring(1);
 		}
 		byte[] storage = new byte[numBytes];
 		for (int i = 0; i < numBytes && i < numstr.length() / 8.0; i++) {
 			String currentByte=numstr.substring(numstr.length() - 8 * (i + 1),numstr.length() - 8 * i);
 			if (currentByte.startsWith("1")) {
				currentByte = "-" + currentByte.substring(1);
 			}
 			storage[i] = Byte.parseByte(currentByte, 2);
 		}
 		return storage;
 	}
 }
