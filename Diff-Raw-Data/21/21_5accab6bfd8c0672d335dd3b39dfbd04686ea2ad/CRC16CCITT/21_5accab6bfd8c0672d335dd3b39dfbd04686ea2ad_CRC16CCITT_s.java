 package org.hbird.core.commons.util.crc;
 
 import java.util.Arrays;
 import java.util.zip.Checksum;
 
 public class CRC16CCITT implements Checksum {
 
 	/** initial value **/
	private static int crc = 0xFFFF;
 
 	/** 0001 0000 0010 0001 (0, 5, 12) **/
 	private static final int NORMAL_POLYNOMIAL = 0x1021;
 
 	@Override
 	public void update(int b) {
 		for (int i = 0; i < Byte.SIZE; i++) {
 			boolean bit = ((b >> (7 - i) & 1) == 1);
 			boolean c15 = ((crc >> 15 & 1) == 1);
 			crc <<= 1;
 			if (c15 ^ bit) {
 				crc ^= NORMAL_POLYNOMIAL;
 			}
 		}
 		crc &= 0xFFFF;
 	}
 
 	@Override
 	public void update(byte[] bytes, int off, int len) {
 		byte[] arraySection = Arrays.copyOfRange(bytes, off, len);
 		for (byte b : arraySection) {
 			update(b);
 		}
 	}
 
 	@Override
 	public long getValue() {
 		return crc & 0xFFFF;
 	}
 
 	@Override
 	public void reset() {
 		crc = 0xFFFF;
 	}
 
 }
