 import java.math.BigInteger;
 
 public class rsa{
     public static void main(String[] args){
 	
 	String s = "Give me an A";
	String a = "010001110110100101110110011001010010000001101101011001010010000001100001011011100010000001000001";		
 
 	BigInteger key1 = new BigInteger ("46947848749720430529628739081");
 	BigInteger key2 = new BigInteger ("37267486263679235062064536973");
	BigInteger x = new BigInteger(a,2);
 
 	x = x.modPow(key2,key1);
 
 	System.out.println(x);
     }
 }
