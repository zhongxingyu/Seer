 package com.jackmaney.IntroductionToAlgorithms.Chapter2;
 
 import java.util.Arrays;
 
 /**
  * This class represents an integer as an array of zeroes and ones
  * and is used in Exercise 2.1-4 of Introduction to Algorithms.
  * 
  * <strong>Note:</strong> We are using two's complement binary representations.
  * @author jack.maney
  *
  */
 public class BinaryInteger extends Number{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6775381012765819559L;
 	private int numberOfBits;
 	private int[] bitArray;
 	
 	public BinaryInteger(int[] array){
 		
 		checkArray(array);
 		
 		this.bitArray = array;
 		this.numberOfBits = array.length;
 		
 	}
 	
 	public BinaryInteger(int n,int numberOfBits){
 		
 		this.bitArray = intToBitArray(n, numberOfBits);
 		this.numberOfBits = numberOfBits;
 		
 	}
 	
 	public BinaryInteger(int n){
 		this(n,32);
 	}
 	
 	public static int[] intToBitArray(int n,int numberOfBits){
 		
 		if(numberOfBits <= 0){
 			throw new IllegalArgumentException("Can't have a negative number of bits!");
 		}
 		else if(numberOfBits > 32){
 			throw new IllegalArgumentException("Sorry, 32 bits or less...this isn't BigInteger, pal!");
 		}
 		
 		
 		
 		if(
 			(n < -1 * Math.pow(2, numberOfBits - 1))
 			||
 			(n > Math.pow(2, numberOfBits - 1) - 1)
 		){
 			throw new IllegalArgumentException("Can't express " 
 					+ n + " using " + numberOfBits + " bits");
 		}
 		
 		int[] result = new int[numberOfBits];
 		
 		for(int i = 0; i < numberOfBits; i++){
 			result[i] = 0;
 		}
 		
 		if(n > 0){
 			
 			//We iteratively subtract powers of 2 from n...
 			
 			while(n > 0){
 				//find largest power of 2 that is less than n:
 				int powerOfTwo = 1;
 				
 				for(int i = 0; i <= Math.ceil(Math.log(n)/Math.log(2));i++){
 					if(2 * powerOfTwo > n){
 						break;
 					}
 					else{
 						powerOfTwo *= 2;
 					}
 				}
 				
 				int index = numberOfBits - 1 - (int)(Math.log(powerOfTwo)/Math.log(2));
 				
 				result[index] = 1;
 				
 				n -= powerOfTwo;
 			}
 				
 		}
 		else if(n < 0){
 			//We'll cheat a bit and use the int[] constructor
 			//along with the complement() method.
 			
 			int[] bitsForNegN = intToBitArray(-n, numberOfBits);
 			
 			BinaryInteger negN = new BinaryInteger(bitsForNegN);
 			
 			result = negN.complement().getBitArray();	
 		}
 		
 		return result;
 		
 	}
 	
 	public int asInt(){
 		
 		int result = 0;
 		
 		int[] bits = getBitArray();
 		
 		result -= bits[0] * Math.pow(2, numberOfBits - 1);
 		
 		for(int i = 1; i < bits.length; i++){
 			result += bits[i] * Math.pow(2, bits.length - 1 - i);
 		}
 		
 		return result;
 	}
 	
 	private void checkArray(int[] array){
 		for(int i = 0; i < array.length; i++){
 			if(array[i] != 1 && array[i] != 0){
 				throw new IllegalArgumentException();
 			}
 		}
 	}
 	
 	public BinaryInteger add(BinaryInteger other){
 		
 		if(getNumberOfBits() != other.getNumberOfBits()){
 			throw new IllegalArgumentException();
 		}
 		
 		int[] bits = new int[getNumberOfBits()];
 		
 		for(int i = 0; i < getNumberOfBits(); i++){
 			bits[i] = 0;
 		}
 		
 		int carryOver = 0;
 		
 		for(int i = getNumberOfBits() - 1; i >= 0; i--){
 			
 			int bit = getBitArray()[i];
 			int otherBit = other.getBitArray()[i];
 			
 			if(bit == 1 && otherBit == 1){
 				bits[i] = 0;
 				carryOver++;
 			}
 			else if(carryOver > 0){
 				if(bit + otherBit == 1){
 					bits[i] = 0;
 				}
 				else{
 					bits[i] = 1;
 					carryOver--;
 				}
 			}
 			else{
 				bits[i] = bit + otherBit;
 			}
 		}
 		
 		return new BinaryInteger(bits);
 	}
 	
 	public BinaryInteger complement(){
 		
 		int[] bits = new int[getNumberOfBits()];
 		
 		for(int i = 0; i < getNumberOfBits(); i++){
 			bits[i] = getBitArray()[i] == 1 ? 0 : 1;
 		}
 		
 		BinaryInteger result = new BinaryInteger(bits);
 		
 		BinaryInteger one = new BinaryInteger(1,getNumberOfBits());
 		
 		return result.add(one);
 	}
 	
 	@Override
 	public String toString() {
 
 		StringBuilder sb = new StringBuilder();
 		
 		for(int i = 0; i < getNumberOfBits(); i++){
 			sb.append(getBitArray()[i]);
 			
 			if(i % 4 == 3){
 				sb.append(" ");
 			}
 		}
 		
 		return sb.toString();
 		
 	}
 	
 	
 	
 	/**
 	 * @return the n
 	 */
 	public int getNumberOfBits() {
 		return numberOfBits;
 	}
 
 
 	/**
 	 * @return the bitArray
 	 */
 	public int[] getBitArray() {
 		return bitArray;
 	}
 
 
 
 	@Override
 	public int hashCode() {
 		return asInt();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof BinaryInteger))
 			return false;
 		BinaryInteger other = (BinaryInteger) obj;
 		if (!Arrays.equals(bitArray, other.bitArray))
 			return false;
 		if (numberOfBits != other.numberOfBits)
 			return false;
 		return true;
 	}
 
 	@Override
 	public int intValue() {
		// TODO Auto-generated method stub
		return numberOfBits;
 	}
 
 	@Override
 	public long longValue() {
		// TODO Auto-generated method stub
		return (long)numberOfBits;
 	}
 
 	@Override
 	public float floatValue() {
		// TODO Auto-generated method stub
		return (float)numberOfBits;
 	}
 
 	@Override
 	public double doubleValue() {
		// TODO Auto-generated method stub
		return (double)numberOfBits;
 	}
 
 }
