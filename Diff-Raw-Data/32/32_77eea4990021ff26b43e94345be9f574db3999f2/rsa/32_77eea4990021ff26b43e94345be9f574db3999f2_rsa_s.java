 import java.math.BigInteger;
 
 public class rsa{
     public static void main(String[] args){
 	
 	String s = "Give me an A";
	String a = "010001110110100101110110011001010010000001101101011001010010000001100001011011100010000001000001";
	/*
	String a = "";
	for ( int i = 0; i < s.length(); ++i ) {
	    char c = s.charAt( i );
	    int j = (int) c;
	    a += j;
	}
	System.out.println(a);
 
	BigInteger n = new BigInteger(a);

	a = n.toString(2);
	*/

	/*
	byte[] b = new byte[]{(byte)0x12, (byte)0x0F, (byte)0xF0};
	
	try{
	    b = s.getBytes("ASCII");
	} catch(Exception e){}
	

	BigInteger bi = new BigInteger(b);
	String a = bi.toString(2);  
	*/
	System.out.println(a);
	
 	BigInteger key1 = new BigInteger ("46947848749720430529628739081");
 	BigInteger key2 = new BigInteger ("37267486263679235062064536973");
	BigInteger x = new BigInteger(a);
 
 	x = x.modPow(key2,key1);
 
 	System.out.println(x);
     }
 }
