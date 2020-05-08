 package heapManagement;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 /*
  * Reference: http://www.daniweb.com/software-development/java/code/216874/primitive-types-as-byte-arrays
  */
 
 public class Utilities {
 	
	public static final int bucketSize = 104; 
 	public static final int overflowPointerSize = 4;
 	public static final int overFlowPointerOffset = bucketSize-overflowPointerSize;
 
 	public static int toInt(byte[] data) {
 		if (data == null || data.length != 4) 
 		{
 			return 0x0;
 		}
 
 		return (int)( // NOTE: type cast not necessary for int
 				(0xff & data[0]) << 24 |
 				(0xff & data[1]) << 16 |
 				(0xff & data[2]) << 8 |
 				(0xff & data[3]) << 0
 		);
 	}
 
 	public static byte[] toByta(char[] data) {
 		if (data == null) return null;
 		// ----------
 		byte[] byts = new byte[data.length * 2];
 		for (int i = 0; i < data.length; i++)
 			System.arraycopy(toByta(data[i]), 0, byts, i * 2, 2);
 		return byts;
 	}
 
 	public static long toLong(byte[] data) {
 		if (data == null || data.length != 8) return 0x0;
 		// ----------
 		return (long)(
 				// (Below) convert to longs before shift because digits
 				// are lost with ints beyond the 32-bit limit
 				(long)(0xff & data[0]) << 56 |
 				(long)(0xff & data[1]) << 48 |
 				(long)(0xff & data[2]) << 40 |
 				(long)(0xff & data[3]) << 32 |
 				(long)(0xff & data[4]) << 24 |
 				(long)(0xff & data[5]) << 16 |
 				(long)(0xff & data[6]) << 8 |
 				(long)(0xff & data[7]) << 0
 		);
 	}
 
 
 
 	public static int[] toIntA(byte[] data) {
 		if (data == null || data.length % 4 != 0) return null;
 		// ----------
 		int[] ints = new int[data.length / 4];
 		for (int i = 0; i < ints.length; i++)
 			ints[i] = toInt( new byte[] {
 					data[(i*4)],
 					data[(i*4)+1],
 					data[(i*4)+2],
 					data[(i*4)+3],
 			} );
 		return ints;
 	}
 
 	public static short toShort(byte[] data) {
 		if (data == null || data.length != 2) return 0x0;
 		// ----------
 		return (short)(
 				(0xff & data[0]) << 8 |
 				(0xff & data[1]) << 0
 		);
 	}
 
 	public static double toDouble(byte[] data) {
 		if (data == null || data.length != 8) return 0x0;
 		// ---------- simple:
 		return Double.longBitsToDouble(toLong(data));
 	}
 
 	public static float toFloat(byte[] data) {
 		if (data == null || data.length != 4) return 0x0;
 		// ---------- simple:
 		return Float.intBitsToFloat(toInt(data));
 	}
 	public static byte[] toByta(char data) {
 		return new byte[] {
 				(byte)((data >> 8) & 0xff),
 				(byte)((data >> 0) & 0xff),
 		};
 	}
 
 	public static byte[] toByta(short data) {
 		return new byte[] {
 				(byte)((data >> 8) & 0xff),
 				(byte)((data >> 0) & 0xff),
 		};
 	}
 
 	public static byte[] toByta(int data) {
 		return new byte[] {
 				(byte)((data >> 24) & 0xff),
 				(byte)((data >> 16) & 0xff),
 				(byte)((data >> 8) & 0xff),
 				(byte)((data >> 0) & 0xff),
 		};
 	}
 
 	public static byte[] toByta(long data) {
 		return new byte[] {
 				(byte)((data >> 56) & 0xff),
 				(byte)((data >> 48) & 0xff),
 				(byte)((data >> 40) & 0xff),
 				(byte)((data >> 32) & 0xff),
 				(byte)((data >> 24) & 0xff),
 				(byte)((data >> 16) & 0xff),
 				(byte)((data >> 8) & 0xff),
 				(byte)((data >> 0) & 0xff),
 		};
 	}
 
 	public static byte[] toByta(float data) {
 		return toByta(Float.floatToRawIntBits(data));
 	}
 
 	public static byte[] toByta(double data) {
 		return toByta(Double.doubleToRawLongBits(data));
 	}
 
 	/*
 	 * Encode header schema
 	 */
 	public static String formatHeaderSchema(ArrayList<Attribute> attributeList) {
 
 		
 		StringBuilder encodedSchema = new StringBuilder();
 		for(int j = 0; j< attributeList.size(); j++){
 			int attributeCode = Utilities.getIntDatatypeCode(attributeList.get(j).getType(),attributeList.get(j).getSize());
 			encodedSchema.append(attributeCode);
 		}
 
 		return encodedSchema.toString();
 	}
 	
 	public static ArrayList<Attribute> makeAttributeList(String schema){
 		StringTokenizer stringTok = new StringTokenizer(schema, ",");
 		ArrayList<Attribute> attrList = new ArrayList<Attribute>();
 		while(stringTok.hasMoreTokens()){
 			String nextTok = stringTok.nextToken();
 			char[] k = nextTok.toCharArray();
 			Attribute dataType = new Attribute();
 			dataType.setType(k[0]);
 			int size = Integer.parseInt(nextTok.substring(1));
 			dataType.setSize(size);
 			attrList.add(dataType);
 		}
 		return attrList;
 	}
 
 	public static int getSize(char ch){
 		if(ch == '0'){
 			return 1;
 		}else if(ch == '1'){
 			return 2;
 		}else if(ch == '2'){
 			return 4;
 		}else if(ch == '3'){
 			return 8;
 		}else if(ch == '4'){
 			return 4;
 		}else if(ch == '5'){
 			return 8;
 		}else if(ch == '6'){
 			return 0;
 		}
 		return ch;
 	}
 	
 
 	public static int getIntDatatypeCode(char type, int size){
 
 		if(type == 'i'){
 
 			if(size == 1){
 				return 0;
 			}else if(size == 2){
 				return 1;
 			}else if(size == 4){
 				return 2;
 			}else if(size == 8){
 				return 3;
 			}
 
 		}else if(type == 'r'){
 
 			if(size == 4){
 				return 4;
 			}else if(size == 8){
 				return 5;
 			}
 
 		} else if(type == 'c'){
 			return 6;
 
 		}else{
 			System.out.println("Parsing error in getDatatypeCode in Utils.java");
 		}
 		return -1;
 	}
 }
 
