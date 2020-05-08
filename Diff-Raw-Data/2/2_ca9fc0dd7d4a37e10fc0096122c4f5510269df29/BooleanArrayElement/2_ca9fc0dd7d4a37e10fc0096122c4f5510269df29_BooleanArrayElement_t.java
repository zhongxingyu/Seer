 package arithmetic.objects;
 
 
 
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Arrays;
 
 public class BooleanArrayElement implements ByteTree {
 	
 	private boolean[] arr;
 	
 	public BooleanArrayElement (boolean[] arr) {
 		this.arr = arr;
 	}
 	
 	public BooleanArrayElement (byte[] arr) {
 		arr = Arrays.copyOfRange(arr, 5, arr.length-1);
 		boolean[] b = new boolean[arr.length];
 		for (int i=0; i<b.length; i++)
 			if (arr[i]==1) 
 				b[i] = true;
 			else b[i] = false;
 		this.arr = b;
 	}
 	
 	@Override
 	public byte[] toByteArray() {
 		byte[] b = new byte[arr.length+5];
 		b[0] = 1;
 		byte[] a = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(arr.length).array();
 		for (int i=0; i<4; i++)
 			b[i+1] = a[i];
 		for (int i=0; i<arr.length; i++)
			if (arr[i] == true) b[i+5] = 1;
 			else b[i+5] = 0;
 		return b;
 		
 	}
 
 }
