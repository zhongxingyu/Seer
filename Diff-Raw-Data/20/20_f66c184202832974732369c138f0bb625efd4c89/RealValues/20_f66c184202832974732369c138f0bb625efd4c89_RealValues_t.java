 package com.pjf.mat.sandbox;
 
 public class RealValues {
 	String msg;
 	
 	public RealValues(String msg) {
 		this.msg = msg;
 	}
 	
 	void doDouble() {
 		System.out.println(Long.toHexString(Double.doubleToLongBits(300)));
 		System.out.println(Long.toHexString(Double.doubleToLongBits(400)));
 		System.out.println(Double.longBitsToDouble(Long.parseLong("4079000000000000", 16)));
 		System.out.println(Double.longBitsToDouble(Long.parseLong("4059000000000000", 16)));
 		System.out.println(Long.parseLong("2000000000000000", 16));		
 	}
 	
 	void doSingle() {
 		System.out.println(Integer.toHexString(Float.floatToIntBits(0.4f)));
 		System.out.println(Integer.toHexString(Float.floatToIntBits(100f)));
 		System.out.println(Integer.toHexString(Float.floatToIntBits(25f)));
 		System.out.println(Integer.toHexString(Float.floatToIntBits(30f)));
		System.out.println(Float.intBitsToFloat(Integer.parseInt("41a1c000", 16)));
		System.out.println(Float.intBitsToFloat(Integer.parseInt("41ade000", 16)));
		System.out.println(Float.intBitsToFloat(Integer.parseInt("41b3f000", 16)));
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		RealValues hw = new RealValues("test 1");
 		hw.doSingle();
 
 	}
 
 }
