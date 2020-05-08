 import java.util.Arrays;
 
 
 public class BinaryNum {
 	private final boolean[] bits;
 	
 	public BinaryNum(final boolean[] bits){
 		this.bits = bits;
 	}
 	
 	public BinaryNum(final String bitString){
 		this.bits = new boolean[bitString.length()];
 		for(int i = 0 ; i < bitString.length() ; i++){
 			if(bitString.substring(i,i+1).equals("1")){
 				this.bits[i] = true;
 			} else if(bitString.substring(i,i+1).equals("0")){
 				this.bits[i] = false;
 			} else {
 				throw new RuntimeException("Invalid bit string: " + bitString);
 			}
 		}
 	}
 	
 	public final BinaryNum setIfLessThan(final BinaryNum other){
		if(this.toInt() < other.toInt()){
 			return new BinaryNum("1");
 		} else {
 			return new BinaryNum("0");
 		}
 	}
 	
	public final int toInt(){
		return Integer.parseInt(this.toString(),2);
 	}
 	
 	public final BinaryNum extend(final int length){
 		if(bits.length > length){
 			throw new RuntimeException(toString() + " is already longer than " + length + " bits.");
 		} else if(bits.length == length){
 			return this;
 		} else {
 			final boolean[] newBits = new boolean[length];
 			final int diff = length - bits.length;
 			for(int i = 0; i < length ; i++){
 				if(i < diff ){
 					newBits[i] = bits[0];
 				} else {
 					newBits[i] = bits[i-diff];
 				}
 			}
 			return new BinaryNum(newBits);
 		}
 	}
 	
 	public final BinaryNum pad(final int length){
 		if(bits.length > length){
 			throw new RuntimeException(toString() + " is already longer than " + length + " bits.");
 		} else if(bits.length == length){
 			return this;
 		} else {
 			final boolean[] newBits = new boolean[length];
 			final int diff = length - bits.length;
 			for(int i = 0; i < length ; i++){
 				if(i < diff ){
 					newBits[i] = false;
 				} else {
 					newBits[i] = bits[i-diff];
 				}
 			}
 			return new BinaryNum(newBits);
 		}
 	}
 	
 	public final BinaryNum twosComplement(){
 		return this.not().add(new BinaryNum("1").pad(this.length()));
 	}
 	
 	public final BinaryNum not(){
 		final boolean[] newBits = new boolean[bits.length];
 		for(int i = 0 ; i < bits.length ; i++){
 			newBits[i] = !bits[i];
 		}
 		return new BinaryNum(newBits);
 	}
 	
 	public final int length(){
 		return bits.length;
 	}
 	
 	public final boolean[] getBits(){
 		return bits;
 	}
 	
 	public final BinaryNum getRange(final int from, final int to){
 		final int start = (bits.length - 1) - from;
 		final int end = (bits.length - 1) - to;
 		boolean[] newBits = new boolean[from-to+1];
 		int newIndex = 0;
		for(int i = start ; i < end ; i++){
 			newBits[newIndex] = bits[i];
 			newIndex++;
 		}
 		return new BinaryNum(newBits);
 	}
 	
 	public final BinaryNum or(final BinaryNum other){
 		if(other.length() != bits.length){
 			throw new RuntimeException("Bit lengths do not match: " + this.toString() + ", " + other.toString());
 		} else {
 			boolean[] newBits = new boolean[bits.length];
 			for(int i = 0 ; i < bits.length ; i++){
 				newBits[i] = bits[i] || other.getBits()[i];
 			}
 			return new BinaryNum(newBits);
 		}
 	}
 	
 	public final BinaryNum and(final BinaryNum other){
 		if(other.length() != bits.length){
 			throw new RuntimeException("Bit lengths do not match: " + this.toString() + ", " + other.toString());
 		} else {
 			boolean[] newBits = new boolean[bits.length];
 			for(int i = 0 ; i < bits.length ; i++){
 				newBits[i] = bits[i] && other.getBits()[i];
 			}
 			return new BinaryNum(newBits);
 		}
 	}
 	
 	public final BinaryNum shiftLeft(){
 		final boolean[] newBits = new boolean[bits.length];
 		for(int i = 1 ; i < bits.length ; i++){
 			newBits[i-1] = bits[i];
 		}
 		newBits[bits.length-1] = false;
 		return new BinaryNum(newBits);
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public final int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + Arrays.hashCode(bits);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public final boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		BinaryNum other = (BinaryNum) obj;
 		if (!Arrays.equals(bits, other.bits)) {
 			return false;
 		}
 		return true;
 	}
 
 	public final BinaryNum add(final BinaryNum other){
 		if(other.length() != bits.length){
 			throw new RuntimeException("Bit lengths do not match: " + this.toString() + ", " + other.toString());
 		} else {
 			boolean carry = false;
 			boolean[] newBits = new boolean[bits.length];
 			for(int i = bits.length-1 ; i >= 0 ; i--){
 				final boolean a = bits[i];
 				final boolean b = other.getBits()[i];
 				final boolean c = carry;
 				newBits[i] = (a ^ b) ^ c;
 				carry = (a && b) || (c && (a ^ b));
 			}
 			return new BinaryNum(newBits);
 		}
 	}
 	
 	public final BinaryNum sub(final BinaryNum other){
 		return this.add(other.twosComplement());
 	}
 	
 	public final BinaryNum nor(final BinaryNum other){
 		return this.or(other).not();
 	}
 	
 	@Override
 	public final String toString(){
 		String s = "";
 		for(int i = 0 ; i < bits.length;i++){
 			if(bits[i]){
 				s += "1";
 			} else {
 				s += "0";
 			}
 		}
 		return s;
 	}
 	
 	/*	
 	public static int testNum = 1;
 	
 	public static void test(Object o1, Object o2){
 		if(o1.equals(o2)){
 			//System.out.println("Test " + testNum + ": PASS");
 		} else {
 			System.out.println("Test " + testNum + ": FAIL");
 			System.out.println("  Expected: " + o2.toString());
 			System.out.println("  Actual: " + o1.toString());
 		}
 		testNum++;
 	}
 	
 	public static void main(String[] args){
 		//
 		BinaryNum.test(new BinaryNum("010101").toString(),"010101"); // 1
 		BinaryNum.test(new BinaryNum("010101").extend(7).toString(),"0010101"); // 2
 		BinaryNum.test(new BinaryNum("101").pad(7).toString(),"0000101"); // 3
 		BinaryNum.test(new BinaryNum("101").not().toString(),"010"); // 4
 		BinaryNum.test(new BinaryNum("101").shiftLeft().toString(),"010"); // 5
 		//
 		BinaryNum.test(new BinaryNum("101").nor(new BinaryNum("110")).toString(),"000");
 		BinaryNum.test(new BinaryNum("101").length(),3);
 		BinaryNum.test(new BinaryNum("101").and(new BinaryNum("010")).toString(),"000");
 		BinaryNum.test(new BinaryNum("10110011").getRange(5,3).toString(),"110");
 		BinaryNum.test(new BinaryNum("101").equals(new BinaryNum("010")),false);
 		//
 		BinaryNum.test(new BinaryNum("101").equals(new BinaryNum("101")),true);
 		BinaryNum.test(new BinaryNum("101").add(new BinaryNum("110")).toString(),"011");
 		BinaryNum.test(new BinaryNum("101").add(new BinaryNum("001")).toString(),"110");
 		BinaryNum.test(new BinaryNum("1011").twosComplement().toString(),"0101");
 		BinaryNum.test(new BinaryNum("101110").twosComplement().toString(),"010010");
 		//
 		BinaryNum.test(new BinaryNum("01001").sub(new BinaryNum("00001")).toString(),"01000");
 		BinaryNum.test(new BinaryNum("01001").nor(new BinaryNum("00001")).toString(),"10110");
 		BinaryNum.test(new BinaryNum("01001").or(new BinaryNum("00001")).toString(),"01001");
 	}
 	*/
 }
