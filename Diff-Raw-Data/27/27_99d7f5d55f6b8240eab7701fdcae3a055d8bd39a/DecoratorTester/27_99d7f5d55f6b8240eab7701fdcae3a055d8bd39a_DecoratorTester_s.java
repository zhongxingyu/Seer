 package edu.iastate.se339.text;
 
 public class DecoratorTester {
 	
 	public static void main(String args[]){
 		
 		byte[] thing1 = "lololol its a thing!".getBytes();
 		byte[] thing2 = "i wish i wish i was a fish".getBytes();
 		byte[] thing3 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
 		
 		AsciiRepresentation as1 = new AsciiRepresentation(thing1);
 		AsciiRepresentation as2 = new AsciiRepresentation(thing2);
 		AsciiRepresentation as3 = new AsciiRepresentation(thing3);
 		
 		HexRepresentation ox1 = new HexRepresentation(thing1);
 		HexRepresentation ox2 = new HexRepresentation(thing2);
 		HexRepresentation ox3 = new HexRepresentation(thing3);
 		
 		BinaryRepresentation bin1 = new BinaryRepresentation(thing1);
 		BinaryRepresentation bin2 = new BinaryRepresentation(thing2);
 		BinaryRepresentation bin3 = new BinaryRepresentation(thing3);
 		
 		System.out.println(as1);
 		System.out.println(as2);
 		System.out.println(as3);
 		System.out.println("BREAK");
 		System.out.println(ox1);
 		System.out.println(ox2);
 		System.out.println(ox3);
 		System.out.println("BREAK");
 		System.out.println(bin1);
 		System.out.println(bin2);
 		System.out.println(bin3);
 		
 		System.out.println(new WordLengthDecorator(ox1));
 		System.out.println(new WordLengthDecorator(ox2));
 		System.out.println(new WordLengthDecorator(ox3));
 		System.out.println(new WordLengthDecorator(bin1));
 		System.out.println(new WordLengthDecorator(bin2));
 		System.out.println(new WordLengthDecorator(bin3));
 	}
 
 }
