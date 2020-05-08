 package com.luzi82.chitanda.game.logic;
 
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.utils.Logger;
 
 public class Board {
 
 	public static final int WIDTH = 2048 * 4;
 	public static final int HEIGHT = 1024 * 4;
 
 	public static final int CELL0_COUNT = WIDTH * HEIGHT;
 	public static final int DATA0_SIZE = CELL0_COUNT / Byte.SIZE;
 	private final byte[] mData0 = new byte[DATA0_SIZE];
 
 	public static final int CELL1_COUNT = CELL0_COUNT / 16;
 	public static final int DATA1_SIZE = CELL1_COUNT * 4 / Byte.SIZE;
 	private final byte[] mData1 = new byte[DATA1_SIZE];
 
 	public static final int CELL2_COUNT = CELL0_COUNT / 256;
 	public static final int DATA2_SIZE = CELL2_COUNT;
 	private final byte[] mData2 = new byte[DATA2_SIZE];
 
 	public boolean get(int aX, int aY) {
 		int i0 = xyToIndex(aX, aY);
 		int o0 = xyToOffset(aX, aY);
 		return (mData0[i0] & (1 << o0)) != 0;
 	}
 
 	public int get1(int aX1, int aY1) {
 		int i1 = xyToIndex1(aX1, aY1);
 		int o1 = xyToOffset1(aX1, aY1);
 		return (mData1[i1] >> o1) & 0xf;
 	}
 
 	public int get2(int aX2, int aY2) {
 		int i2 = xyToIndex2(aX2, aY2);
 		return ((int) (mData2[i2])) & 0xff;
 	}
 
 	public void set(int aX, int aY, boolean aV) {
 		int i0 = xyToIndex(aX, aY);
 		int o0 = xyToOffset(aX, aY);
 		boolean v = (mData0[i0] & (1 << o0)) != 0;
 		if (v == aV)
 			return;
 		int i1 = xyToIndex1(aX >> 2, aY >> 2);
 		int o1 = xyToOffset1(aX >> 2, aY >> 2);
 		int i2 = xyToIndex2(aX >> 4, aY >> 4);
 		byte d1 = mData1[i1];
 		int v1 = (d1 >> o1) & 0xf;
 		if (aV) {
 			mData0[i0] |= 1 << o0;
 			++v1;
 			++mData2[i2];
 		} else {
 			mData0[i0] &= ~(1 << o0);
 			--v1;
 			--mData2[i2];
 		}
 		d1 &= ~(0xf << o1);
 		d1 |= v1 << o1;
 		mData1[i1] = d1;
 	}
 
 	public void setAll(boolean aValue) {
 		byte v = (byte) (aValue ? 0xff : 0x00);
 		for (int i = 0; i < DATA0_SIZE; ++i) {
 			mData0[i] = v;
 		}
 		for (int i = 0; i < DATA1_SIZE; ++i) {
 			mData1[i] = v;
 		}
 		for (int i = 0; i < DATA2_SIZE; ++i) {
 			mData2[i] = v;
 		}
 	}
 
 	private static int xyToIndex(int aX, int aY) {
 		return (aX + aY * WIDTH) / Byte.SIZE;
 	}
 
 	private static int xyToOffset(int aX, int aY) {
 		return (aX + aY * WIDTH) % Byte.SIZE;
 	}
 
 	private static int xyToIndex1(int aX1, int aY1) {
 		return (aX1 + aY1 * (WIDTH / 4)) / 2;
 	}
 
 	private static int xyToOffset1(int aX1, int aY1) {
 		return ((aX1 + aY1 * (WIDTH / 4)) % 1) * 4;
 	}
 
 	private static int xyToIndex2(int aX2, int aY2) {
 		return (aX2 + aY2 * (WIDTH / 16)) / Byte.SIZE;
 	}
 
 	static byte[] BB = new byte[64 * 64 * 4];
 
 	public void writePixmap0(Pixmap aPixmap, int aX0, int aY0) {
 		byte c = (byte) (((((aX0 + aY0) / 64) & 1) == 0) ? 0x00 : 0x0f);
 
 		ByteBuffer bb = aPixmap.getPixels();
 		bb.rewind();
 		int yStep = WIDTH / Byte.SIZE;
 
 		int offset0 = xyToIndex(aX0, aY0);
 		int out = 0;
 
 		byte b;
 		for (int i = 0; i < 64; ++i) {
 			int j0 = offset0;
 			int j1 = offset0 + 8;
 			for (int j = j0; j < j1; ++j) {
 				b = mData0[j];
 				for (int k = 0; k < Byte.SIZE; ++k) {
 					BB[out++] = c;
 					BB[out++] = c;
 					BB[out++] = c;
 					if ((b & 1) == 1) {
 						BB[out++] = (byte) 0x7f;
 					} else {
 						BB[out++] = (byte) 0x00;
 					}
					b >>= 0;
 				}
 			}
 			offset0 += yStep;
 		}
 
 		bb.put(BB);
 		bb.rewind();
 	}
 
 	@SuppressWarnings("all")
 	private static void check() {
 		if ((WIDTH % 16) != 0)
 			throw new RuntimeException();
 		if ((HEIGHT % 16) != 0)
 			throw new RuntimeException();
 	}
 
 	static {
 		check();
 	}
 
 }
