 package org.paxle.se.index.lucene.impl;
 
 import org.apache.lucene.document.NumberTools;
 
 public class PaxleNumberTools extends NumberTools {
 	
 	public static byte[] toBytes(long num) {
 		final int size = 8 - Long.numberOfLeadingZeros(num) / 8;
 		final byte[] buf = new byte[size];
 		int pos = 0;
		while (pos < size) {
 		    buf[pos++] = (byte)(num & 0xFF);
 		    num >>= 8;
		}
 		return buf;
 	}
 	
 	public static byte[] toBytes(double num) {
 		return toBytes(Double.doubleToLongBits(num));
 	}
 	
 	public static long toLong(byte[] data) {
 		long x = 0;
 		for (int i=0; i<data.length; i++)
 			x |= (data[i] & 0xFFl) << (i * 8);
 		return x;
 	}
 	
 	public static double toDouble(byte[] data) {
 		return Double.longBitsToDouble(toLong(data));
 	}
 	
 	public static String toBinaryString(byte[] data, boolean sepBytes) {
 		final StringBuilder sb = new StringBuilder();
 		for (int i=data.length-1; i>=0; i--) {
 			for (int j=7; j>=0; j--)
 				sb.append(((data[i] & (1 << j)) == 0) ? '0' : '1');
 			if (sepBytes)
 				sb.append(' ');
 		}
 		if (sepBytes && sb.length() > 0)
 			sb.deleteCharAt(sb.length() - 1);
 		return sb.toString();
 	}
 	
 	public static void main(String[] args) {
 		int num = 1156 * 256 + 255;
 		byte[] d = toBytes(num);
 		System.out.println(toBinaryString(d, true));
 		int t = (int)toLong(d);
 		System.out.println(t);
 		/*
 		System.out.println(toString(fill(t)));
 		System.out.println(Long.toBinaryString(num));*/
 		System.out.println(num);
 	}
 }
