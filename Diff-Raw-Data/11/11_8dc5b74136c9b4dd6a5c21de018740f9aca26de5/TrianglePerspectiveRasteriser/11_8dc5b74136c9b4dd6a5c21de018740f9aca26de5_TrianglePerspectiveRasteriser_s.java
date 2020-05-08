 package initial3d.renderer;
 
 import sun.misc.Unsafe;
 import static java.lang.Math.min;
 import static java.lang.Math.max;
 import static java.lang.Math.abs;
 import static initial3d.renderer.Util.*;
 
 @SuppressWarnings("restriction")
 final class TrianglePerspectiveRasteriser {
 
 	private TrianglePerspectiveRasteriser() {
 		throw new AssertionError();
 	}
 
 	public static final void verifyTriangle(Unsafe unsafe, long pBase, long pTri) {
 		if (pTri < pBase || pTri >= pBase + Initial3DImpl.MEM_SIZE) {
 			throw new AssertionError(Util.sprintf("Pointer verification failed for pTri. Value was 0x%016x.", pTri));
 		}
 		verifyPointer(unsafe, pBase, pTri + 64, "v0");
 		verifyPointer(unsafe, pBase, pTri + 72, "vt0");
 		verifyPointer(unsafe, pBase, pTri + 80, "vn0");
 		verifyPointer(unsafe, pBase, pTri + 88, "vv0");
 		verifyPointer(unsafe, pBase, pTri + 128, "v1");
 		verifyPointer(unsafe, pBase, pTri + 136, "vt1");
 		verifyPointer(unsafe, pBase, pTri + 144, "vn1");
 		verifyPointer(unsafe, pBase, pTri + 152, "vv1");
 		verifyPointer(unsafe, pBase, pTri + 192, "v2");
 		verifyPointer(unsafe, pBase, pTri + 200, "vt2");
 		verifyPointer(unsafe, pBase, pTri + 208, "vn2");
 		verifyPointer(unsafe, pBase, pTri + 216, "vv2");
 	}
 
 	private static final void verifyPointer(Unsafe unsafe, long pBase, long pp, String description) {
 		long p = unsafe.getLong(pp);
 		if (p < pBase || p >= pBase + Initial3DImpl.MEM_SIZE) {
 			throw new AssertionError(Util.sprintf(
 					"Pointer verification failed for: %s. Value was 0x%016x. QPointer was 0x%08x", description, p, pp
 							- pBase));
 		}
 	}
 
 	/**
 	 * Rasterise a triangle with nothing interpolated. Algorithm based (heavily) on <a
 	 * href=http://devmaster.net/forums/topic/1145-advanced-rasterization/>this article</a>.
 	 */
 	static final void rasteriseTriangle(Unsafe unsafe, long pBase, long pTri, int Yi, int Yf) {
 		// THIS LINE IS ONLY FOR DEBUGGING SEGFAULTS
 		// verifyTriangle(unsafe, pBase, pTri);
 
 		// [ (int) tri_norm_zsign, (int) flags, {56} | (long) pv0, (long) pvt0, (long) pvn0, (long) pvv0, {16},
 		// (float4) vlight0 | <v*1> | <v*2> ]
 
 		// check deleted
 		if ((unsafe.getInt(pTri + 4) & 0x1) != 0) return;
 
 		final int height = unsafe.getInt(pBase + 0x00000004);
 		final int width = unsafe.getInt(pBase + 0x00000000);
 		final long flags = unsafe.getLong(pBase + 0x00000008);
 		final boolean frontface = unsafe.getInt(pTri) < 0;
 
 		double vscale = -(height >> 1);
 		double hscale = -(width >> 1);
 
 		// swap vertex order if backface (makes half-space functions behave)
 		final double X1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192))) - 1d) * hscale;
 		final double Y1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192)) + 8) - 1d) * vscale;
 		final double X2d = (unsafe.getDouble(unsafe.getLong(pTri + 128)) - 1d) * hscale;
 		final double Y2d = (unsafe.getDouble(unsafe.getLong(pTri + 128) + 8) - 1d) * vscale;
 		final double X3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64))) - 1d) * hscale;
 		final double Y3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64)) + 8) - 1d) * vscale;
 
 		// use 28.4 fixed point values for x and y
 		final int X1 = (int) (X1d * 16d + 0.5d);
 		final int Y1 = (int) (Y1d * 16d + 0.5d);
 		final int X2 = (int) (X2d * 16d + 0.5d);
 		final int Y2 = (int) (Y2d * 16d + 0.5d);
 		final int X3 = (int) (X3d * 16d + 0.5d);
 		final int Y3 = (int) (Y3d * 16d + 0.5d);
 
 		// block size
 		final int q = 8;
 
 		// bounding rectangle, clamped to screen bounds
 		// also skip if no visible area (this implements x,y view frustum culling)
 		int minx = (min(X1, min(X2, X3)) + 0xF) >> 4;
 		int maxx = (max(X1, max(X2, X3)) + 0xF) >> 4;
 		minx = max(minx, 0);
 		maxx = min(maxx, width & ~(q - 1));
 		if (minx >= maxx) return;
 		int miny = (min(Y1, min(Y2, Y3)) + 0xF) >> 4;
 		int maxy = (max(Y1, max(Y2, Y3)) + 0xF) >> 4;
 		miny = max(miny, Yi);
 		maxy = min(maxy, Yf & ~(q - 1));
 		if (miny >= maxy) return;
 
 		// pixel buffer pointers
 		final long pColor = pBase + 0x01DC0900;
 		final long pZ = pBase + 0x05DC0900;
 		final long pStencil = pBase + 0x06E00900;
 		final long pID = pBase + 0x07E00900;
 
 		// triangle color
 		final float col_r = unsafe.getFloat(pTri + 116);
 		final float col_g = unsafe.getFloat(pTri + 120);
 		final float col_b = unsafe.getFloat(pTri + 124);
 
 		// deltas 28.4
 		final int DX12 = X1 - X2;
 		final int DX23 = X2 - X3;
 		final int DX31 = X3 - X1;
 		final int DY12 = Y1 - Y2;
 		final int DY23 = Y2 - Y3;
 		final int DY31 = Y3 - Y1;
 
 		// deltas 24.8, because multiplying two 28.4's gives a 24.8
 		final int FDX12 = DX12 << 4;
 		final int FDX23 = DX23 << 4;
 		final int FDX31 = DX31 << 4;
 		final int FDY12 = DY12 << 4;
 		final int FDY23 = DY23 << 4;
 		final int FDY31 = DY31 << 4;
 
 		// start in corner of block
 		minx &= ~(q - 1);
 		miny &= ~(q - 1);
 		int blockoffset = miny * width;
 
 		// half-edge constants 24:8
 		int C1 = DY12 * X1 - DX12 * Y1;
 		int C2 = DY23 * X2 - DX23 * Y2;
 		int C3 = DY31 * X3 - DX31 * Y3;
 
 		// System.out.printf("%d\t%d\t%d\n", C1, C2, C3);
 
 		// correct for fill convention
 		if (DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
 		if (DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
 		if (DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;
 
 		// loop through blocks
 		for (int y = miny; y < maxy; y += q) {
 
 			for (int x = minx; x < maxx; x += q) {
 
 				// corners of block
 				int x0 = x << 4;
 				int x1 = (x + q - 1) << 4;
 				int y0 = y << 4;
 				int y1 = (y + q - 1) << 4;
 
 				// eval half-space functions
 				int a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? 1 : 0;
 				int a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? 2 : 0;
 				int a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? 4 : 0;
 				int a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? 8 : 0;
 				int a = a00 | a10 | a01 | a11;
 
 				int b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? 1 : 0;
 				int b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? 2 : 0;
 				int b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? 4 : 0;
 				int b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? 8 : 0;
 				int b = b00 | b10 | b01 | b11;
 
 				int c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? 1 : 0;
 				int c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? 2 : 0;
 				int c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? 4 : 0;
 				int c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? 8 : 0;
 				int c = c00 | c10 | c01 | c11;
 
 				// skip block when outside an edge
 				if (a == 0 || b == 0 || c == 0) continue;
 
 				int offset = blockoffset;
 
 				// FIXME proper ztest, stencil test, alpha test...
 
 				// accept whole block when totally covered
 				if (a == 0xF && b == 0xF && c == 0xF) {
 					for (int iy = 0; iy < q; ++iy) {
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 
 							// hold on...
 							// if flat or gourard, write predetermined color ( * texture color) to color buffer
 							// if phong, run phong equation
 
 							// colorwrite, if enabled
 							if ((flags & 0x80000L) != 0) {
 								unsafe.putFloat(pColor + ix * 16 + 4, col_r);
 								unsafe.putFloat(pColor + ix * 16 + 8, col_g);
 								unsafe.putFloat(pColor + ix * 16 + 12, col_b);
 							}
 
 						}
 						offset += width;
 					}
 				} else {
 					// partially covered block
 					int CY1 = C1 + DX12 * y0 - DY12 * x0;
 					int CY2 = C2 + DX23 * y0 - DY23 * x0;
 					int CY3 = C3 + DX31 * y0 - DY31 * x0;
 
 					for (int iy = 0; iy < q; ++iy) {
 						int CX1 = CY1;
 						int CX2 = CY2;
 						int CX3 = CY3;
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (CX1 > 0 && CX2 > 0 && CX3 > 0) {
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, col_r);
 									unsafe.putFloat(pColor + ix * 16 + 8, col_g);
 									unsafe.putFloat(pColor + ix * 16 + 12, col_b);
 								}
 							}
 
 							CX1 -= FDY12;
 							CX2 -= FDY23;
 							CX3 -= FDY31;
 						}
 
 						CY1 += FDX12;
 						CY2 += FDX23;
 						CY3 += FDX31;
 
 						offset += width;
 					}
 
 				}
 
 			}
 
 			blockoffset += q * width;
 		}
 	}
 
 	/**
 	 * Rasterise a triangle with only inverse z interpolated. Algorithm based (heavily) on <a
 	 * href=http://devmaster.net/forums/topic/1145-advanced-rasterization/>this article</a>.
 	 */
 	static final void rasteriseTriangle_z(Unsafe unsafe, long pBase, long pTri, int Yi, int Yf) {
 		// THIS LINE IS ONLY FOR DEBUGGING SEGFAULTS
 		// verifyTriangle(unsafe, pBase, pTri);
 
 		// [ (int) tri_norm_zsign, (int) flags, {56} | (long) pv0, (long) pvt0, (long) pvn0, (long) pvv0, {16},
 		// (float4) vlight0 | <v*1> | <v*2> ]
 
 		// check deleted
 		if ((unsafe.getInt(pTri + 4) & 0x1) != 0) return;
 
 		final int height = unsafe.getInt(pBase + 0x00000004);
 		final int width = unsafe.getInt(pBase + 0x00000000);
 		final long flags = unsafe.getLong(pBase + 0x00000008);
 		final int zsign = unsafe.getInt(pBase + 0x00000068);
 		final boolean frontface = unsafe.getInt(pTri) < 0;
 
 		double vscale = -(height >> 1);
 		double hscale = -(width >> 1);
 
 		// swap vertex order if backface (makes half-space functions behave)
 		final double X1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192))) - 1d) * hscale;
 		final double Y1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192)) + 8) - 1d) * vscale;
 		final double X2d = (unsafe.getDouble(unsafe.getLong(pTri + 128)) - 1d) * hscale;
 		final double Y2d = (unsafe.getDouble(unsafe.getLong(pTri + 128) + 8) - 1d) * vscale;
 		final double X3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64))) - 1d) * hscale;
 		final double Y3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64)) + 8) - 1d) * vscale;
 
 		// use 28.4 fixed point values for x and y
 		final int X1 = (int) (X1d * 16d + 0.5d);
 		final int Y1 = (int) (Y1d * 16d + 0.5d);
 		final int X2 = (int) (X2d * 16d + 0.5d);
 		final int Y2 = (int) (Y2d * 16d + 0.5d);
 		final int X3 = (int) (X3d * 16d + 0.5d);
 		final int Y3 = (int) (Y3d * 16d + 0.5d);
 
 		// block size
 		final int q = 8;
 
 		// bounding rectangle, clamped to screen bounds
 		// also skip if no visible area (this implements x,y view frustum culling)
 		int minx = (min(X1, min(X2, X3)) + 0xF) >> 4;
 		int maxx = (max(X1, max(X2, X3)) + 0xF) >> 4;
 		minx = max(minx, 0);
 		maxx = min(maxx, width & ~(q - 1));
 		if (minx >= maxx) return;
 		int miny = (min(Y1, min(Y2, Y3)) + 0xF) >> 4;
 		int maxy = (max(Y1, max(Y2, Y3)) + 0xF) >> 4;
 		miny = max(miny, Yi);
 		maxy = min(maxy, Yf & ~(q - 1));
 		if (miny >= maxy) return;
 
 		// pixel buffer pointers
 		final long pColor = pBase + 0x01DC0900;
 		final long pZ = pBase + 0x05DC0900;
 		final long pStencil = pBase + 0x06E00900;
 		final long pID = pBase + 0x07E00900;
 
 		// 1/z, remembering vertex order...
 		double iZ1 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 88 : 216)) + 16);
 		double iZ2 = 1d / unsafe.getDouble(unsafe.getLong(pTri + 152) + 16);
 		double iZ3 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 216 : 88)) + 16);
 
 		// triangle color
 		final float col_r = unsafe.getFloat(pTri + 116);
 		final float col_g = unsafe.getFloat(pTri + 120);
 		final float col_b = unsafe.getFloat(pTri + 124);
 
 		// barycentric stuff for interpolation
 		// maybs don't do this for polys with a small number of pixels?
 		final double DY23d = Y2d - Y3d;
 		final double DX32d = X3d - X2d;
 		final double DY31d = Y3d - Y1d;
 		final double DX13d = X1d - X3d;
 		final double idet = 1 / (DY23d * (X1d - X3d) + DX32d * (Y1d - Y3d));
 		// compute barycentric coords at top left, 1 px down and 1 px right
 		// use diff of only 1 px to avoid having to divide to get the deltas
 		final double L00_1 = (DY23d * (-X3d) + DX32d * (-Y3d)) * idet;
 		final double L00_2 = (DY31d * (-X3d) + DX13d * (-Y3d)) * idet;
 		final double L00_3 = 1 - L00_1 - L00_2;
 		final double L10_1 = (DY23d * (1 - X3d) + DX32d * (-Y3d)) * idet;
 		final double L10_2 = (DY31d * (1 - X3d) + DX13d * (-Y3d)) * idet;
 		final double L10_3 = 1 - L10_1 - L10_2;
 		final double L01_1 = (DY23d * (-X3d) + DX32d * (1 - Y3d)) * idet;
 		final double L01_2 = (DY31d * (-X3d) + DX13d * (1 - Y3d)) * idet;
 		final double L01_3 = 1 - L01_1 - L01_2;
 
 		// 1/z for delta calculation
 		final float iZ00 = (float) (L00_1 * iZ1 + L00_2 * iZ2 + L00_3 * iZ3);
 		final float iZ10 = (float) (L10_1 * iZ1 + L10_2 * iZ2 + L10_3 * iZ3);
 		final float iZ01 = (float) (L01_1 * iZ1 + L01_2 * iZ2 + L01_3 * iZ3);
 
 		// interpolation deltas
 		final float diZ_dx = (iZ10 - iZ00);
 		final float diZ_dy = (iZ01 - iZ00);
 		final float diZ_dqy = diZ_dy * q;
 
 		// deltas 28.4
 		final int DX12 = X1 - X2;
 		final int DX23 = X2 - X3;
 		final int DX31 = X3 - X1;
 		final int DY12 = Y1 - Y2;
 		final int DY23 = Y2 - Y3;
 		final int DY31 = Y3 - Y1;
 
 		// deltas 24.8, because multiplying two 28.4's gives a 24.8
 		final int FDX12 = DX12 << 4;
 		final int FDX23 = DX23 << 4;
 		final int FDX31 = DX31 << 4;
 		final int FDY12 = DY12 << 4;
 		final int FDY23 = DY23 << 4;
 		final int FDY31 = DY31 << 4;
 
 		// start in corner of block
 		minx &= ~(q - 1);
 		miny &= ~(q - 1);
 		int blockoffset = miny * width;
 		float iZblockoffset = iZ00 + miny * diZ_dy;
 
 		// half-edge constants 24:8
 		int C1 = DY12 * X1 - DX12 * Y1;
 		int C2 = DY23 * X2 - DX23 * Y2;
 		int C3 = DY31 * X3 - DX31 * Y3;
 
 		// System.out.printf("%d\t%d\t%d\n", C1, C2, C3);
 
 		// correct for fill convention
 		if (DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
 		if (DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
 		if (DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;
 
 		// loop through blocks
 		for (int y = miny; y < maxy; y += q) {
 
 			for (int x = minx; x < maxx; x += q) {
 
 				// corners of block
 				int x0 = x << 4;
 				int x1 = (x + q - 1) << 4;
 				int y0 = y << 4;
 				int y1 = (y + q - 1) << 4;
 
 				// eval half-space functions
 				int a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? 1 : 0;
 				int a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? 2 : 0;
 				int a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? 4 : 0;
 				int a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? 8 : 0;
 				int a = a00 | a10 | a01 | a11;
 
 				int b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? 1 : 0;
 				int b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? 2 : 0;
 				int b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? 4 : 0;
 				int b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? 8 : 0;
 				int b = b00 | b10 | b01 | b11;
 
 				int c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? 1 : 0;
 				int c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? 2 : 0;
 				int c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? 4 : 0;
 				int c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? 8 : 0;
 				int c = c00 | c10 | c01 | c11;
 
 				// skip block when outside an edge
 				if (a == 0 || b == 0 || c == 0) continue;
 
 				int offset = blockoffset;
 				float iZoffset = iZblockoffset + diZ_dx * x;
 				float iZ = iZoffset;
 
 				// FIXME proper ztest, stencil test, alpha test...
 
 				// accept whole block when totally covered
 				if (a == 0xF && b == 0xF && c == 0xF) {
 					for (int iy = 0; iy < q; ++iy) {
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// hold on...
 								// if flat or gourard, write predetermined color ( * texture color) to color buffer
 								// if phong, run phong equation
 
 								// zwrite, if enabled and TODO depth test enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, col_r);
 									unsafe.putFloat(pColor + ix * 16 + 8, col_g);
 									unsafe.putFloat(pColor + ix * 16 + 12, col_b);
 								}
 
 							}
 
 							iZ += diZ_dx;
 						}
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 					}
 				} else {
 					// partially covered block
 					int CY1 = C1 + DX12 * y0 - DY12 * x0;
 					int CY2 = C2 + DX23 * y0 - DY23 * x0;
 					int CY3 = C3 + DX31 * y0 - DY31 * x0;
 
 					for (int iy = 0; iy < q; ++iy) {
 						int CX1 = CY1;
 						int CX2 = CY2;
 						int CX3 = CY3;
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (CX1 > 0 && CX2 > 0 && CX3 > 0 && iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// zwrite, if enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, col_r);
 									unsafe.putFloat(pColor + ix * 16 + 8, col_g);
 									unsafe.putFloat(pColor + ix * 16 + 12, col_b);
 								}
 							}
 
 							CX1 -= FDY12;
 							CX2 -= FDY23;
 							CX3 -= FDY31;
 							iZ += diZ_dx;
 						}
 
 						CY1 += FDX12;
 						CY2 += FDX23;
 						CY3 += FDX31;
 
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 					}
 
 				}
 
 			}
 
 			blockoffset += q * width;
 			iZblockoffset += diZ_dqy;
 		}
 	}
 
 	/** Interpolate vertex colors. */
 	static final void rasteriseTriangle_z_color(Unsafe unsafe, long pBase, long pTri, int Yi, int Yf) {
 		// [ (int) tri_norm_zsign, (int) flags, {56} | (long) pv0, (long) pvt0, (long) pvn0, (long) pvv0, {16},
 		// (float4) vlight0 | <v*1> | <v*2> ]
 
 		// check deleted
 		if ((unsafe.getInt(pTri + 4) & 0x1) != 0) return;
 
 		final int height = unsafe.getInt(pBase + 0x00000004);
 		final int width = unsafe.getInt(pBase + 0x00000000);
 		final long flags = unsafe.getLong(pBase + 0x00000008);
 		final int zsign = unsafe.getInt(pBase + 0x00000068);
 		final boolean frontface = unsafe.getInt(pTri) < 0;
 
 		double vscale = -(height >> 1);
 		double hscale = -(width >> 1);
 
 		// swap vertex order if backface (makes half-space functions behave)
 		final double X1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192))) - 1d) * hscale;
 		final double Y1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192)) + 8) - 1d) * vscale;
 		final double X2d = (unsafe.getDouble(unsafe.getLong(pTri + 128)) - 1d) * hscale;
 		final double Y2d = (unsafe.getDouble(unsafe.getLong(pTri + 128) + 8) - 1d) * vscale;
 		final double X3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64))) - 1d) * hscale;
 		final double Y3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64)) + 8) - 1d) * vscale;
 
 		// use 28.4 fixed point values for x and y
 		final int X1 = (int) (X1d * 16d + 0.5d);
 		final int Y1 = (int) (Y1d * 16d + 0.5d);
 		final int X2 = (int) (X2d * 16d + 0.5d);
 		final int Y2 = (int) (Y2d * 16d + 0.5d);
 		final int X3 = (int) (X3d * 16d + 0.5d);
 		final int Y3 = (int) (Y3d * 16d + 0.5d);
 
 		// block size
 		final int q = 8;
 		final float iq = 1f / q;
 
 		// bounding rectangle, clamped to screen bounds
 		// also skip if no visible area (this implements (limited) x,y view frustum culling)
 		int minx = (min(X1, min(X2, X3)) + 0xF) >> 4;
 		int maxx = (max(X1, max(X2, X3)) + 0xF) >> 4;
 		minx = max(minx, 0);
 		maxx = min(maxx, width & ~(q - 1));
 		if (minx >= maxx) return;
 		int miny = (min(Y1, min(Y2, Y3)) + 0xF) >> 4;
 		int maxy = (max(Y1, max(Y2, Y3)) + 0xF) >> 4;
 		miny = max(miny, Yi);
 		maxy = min(maxy, Yf & ~(q - 1));
 		if (miny >= maxy) return;
 
 		// pixel buffer pointers
 		final long pColor = pBase + 0x01DC0900;
 		final long pZ = pBase + 0x05DC0900;
 		final long pStencil = pBase + 0x06E00900;
 		final long pID = pBase + 0x07E00900;
 
 		// 1/z, remembering vertex order...
 		double iZ1 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 88 : 216)) + 16);
 		double iZ2 = 1d / unsafe.getDouble(unsafe.getLong(pTri + 152) + 16);
 		double iZ3 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 216 : 88)) + 16);
 
 		// vertex colors
 		final float cR1 = unsafe.getFloat(pTri + (frontface ? 116 : 244));
 		final float cG1 = unsafe.getFloat(pTri + (frontface ? 120 : 248));
 		final float cB1 = unsafe.getFloat(pTri + (frontface ? 124 : 252));
 		final float cR2 = unsafe.getFloat(pTri + 180);
 		final float cG2 = unsafe.getFloat(pTri + 184);
 		final float cB2 = unsafe.getFloat(pTri + 188);
 		final float cR3 = unsafe.getFloat(pTri + (frontface ? 244 : 116));
 		final float cG3 = unsafe.getFloat(pTri + (frontface ? 248 : 120));
 		final float cB3 = unsafe.getFloat(pTri + (frontface ? 252 : 124));
 
 		// 1 / color
 		final double cRiZ1 = cR1 * iZ1;
 		final double cGiZ1 = cG1 * iZ1;
 		final double cBiZ1 = cB1 * iZ1;
 		final double cRiZ2 = cR2 * iZ2;
 		final double cGiZ2 = cG2 * iZ2;
 		final double cBiZ2 = cB2 * iZ2;
 		final double cRiZ3 = cR3 * iZ3;
 		final double cGiZ3 = cG3 * iZ3;
 		final double cBiZ3 = cB3 * iZ3;
 
 		// max / min color for clamping
 		final float cRmax = min(max(cR1, max(cR2, cR3)), 1f);
 		final float cGmax = min(max(cG1, max(cG2, cG3)), 1f);
 		final float cBmax = min(max(cB1, max(cB2, cB3)), 1f);
 		final float cRmin = max(min(cR1, min(cR2, cR3)), 0f);
 		final float cGmin = max(min(cG1, min(cG2, cG3)), 0f);
 		final float cBmin = max(min(cB1, min(cB2, cB3)), 0f);
 
 		// barycentric stuff for interpolation
 		// maybs don't do this for polys with a small number of pixels?
 		final double DY23d = Y2d - Y3d;
 		final double DX32d = X3d - X2d;
 		final double DY31d = Y3d - Y1d;
 		final double DX13d = X1d - X3d;
 		final double idet = 1 / (DY23d * (X1d - X3d) + DX32d * (Y1d - Y3d));
 		// compute barycentric coords at top left, 1 px down and 1 px right
 		// use diff of only 1 px to avoid having to divide to get the deltas
 		final double L00_1 = (DY23d * (-X3d) + DX32d * (-Y3d)) * idet;
 		final double L00_2 = (DY31d * (-X3d) + DX13d * (-Y3d)) * idet;
 		final double L00_3 = 1 - L00_1 - L00_2;
 		final double L10_1 = (DY23d * (1 - X3d) + DX32d * (-Y3d)) * idet;
 		final double L10_2 = (DY31d * (1 - X3d) + DX13d * (-Y3d)) * idet;
 		final double L10_3 = 1 - L10_1 - L10_2;
 		final double L01_1 = (DY23d * (-X3d) + DX32d * (1 - Y3d)) * idet;
 		final double L01_2 = (DY31d * (-X3d) + DX13d * (1 - Y3d)) * idet;
 		final double L01_3 = 1 - L01_1 - L01_2;
 
 		// 1/z, color/z for delta calculation
 		final float iZ00 = (float) (L00_1 * iZ1 + L00_2 * iZ2 + L00_3 * iZ3);
 		final float iZ10 = (float) (L10_1 * iZ1 + L10_2 * iZ2 + L10_3 * iZ3);
 		final float iZ01 = (float) (L01_1 * iZ1 + L01_2 * iZ2 + L01_3 * iZ3);
 
 		final float cRiZ00 = (float) (L00_1 * cRiZ1 + L00_2 * cRiZ2 + L00_3 * cRiZ3);
 		final float cRiZ10 = (float) (L10_1 * cRiZ1 + L10_2 * cRiZ2 + L10_3 * cRiZ3);
 		final float cRiZ01 = (float) (L01_1 * cRiZ1 + L01_2 * cRiZ2 + L01_3 * cRiZ3);
 		final float cGiZ00 = (float) (L00_1 * cGiZ1 + L00_2 * cGiZ2 + L00_3 * cGiZ3);
 		final float cGiZ10 = (float) (L10_1 * cGiZ1 + L10_2 * cGiZ2 + L10_3 * cGiZ3);
 		final float cGiZ01 = (float) (L01_1 * cGiZ1 + L01_2 * cGiZ2 + L01_3 * cGiZ3);
 		final float cBiZ00 = (float) (L00_1 * cBiZ1 + L00_2 * cBiZ2 + L00_3 * cBiZ3);
 		final float cBiZ10 = (float) (L10_1 * cBiZ1 + L10_2 * cBiZ2 + L10_3 * cBiZ3);
 		final float cBiZ01 = (float) (L01_1 * cBiZ1 + L01_2 * cBiZ2 + L01_3 * cBiZ3);
 
 		// interpolation deltas
 		final float diZ_dx = (iZ10 - iZ00);
 		final float diZ_dy = (iZ01 - iZ00);
 		final float diZ_dqy = diZ_dy * q;
 		final float diZ_dqx = diZ_dx * q;
 		final float dcRiZ_dx = cRiZ10 - cRiZ00;
 		final float dcRiZ_dy = cRiZ01 - cRiZ00;
 		final float dcRiZ_dqy = dcRiZ_dy * q;
 		final float dcRiZ_dqx = dcRiZ_dx * q;
 		final float dcGiZ_dx = cGiZ10 - cGiZ00;
 		final float dcGiZ_dy = cGiZ01 - cGiZ00;
 		final float dcGiZ_dqy = dcGiZ_dy * q;
 		final float dcGiZ_dqx = dcGiZ_dx * q;
 		final float dcBiZ_dx = cBiZ10 - cBiZ00;
 		final float dcBiZ_dy = cBiZ01 - cBiZ00;
 		final float dcBiZ_dqy = dcBiZ_dy * q;
 		final float dcBiZ_dqx = dcBiZ_dx * q;
 
 		// deltas 28.4
 		final int DX12 = X1 - X2;
 		final int DX23 = X2 - X3;
 		final int DX31 = X3 - X1;
 		final int DY12 = Y1 - Y2;
 		final int DY23 = Y2 - Y3;
 		final int DY31 = Y3 - Y1;
 
 		// deltas 24.8, because multiplying two 28.4's gives a 24.8
 		final int FDX12 = DX12 << 4;
 		final int FDX23 = DX23 << 4;
 		final int FDX31 = DX31 << 4;
 		final int FDY12 = DY12 << 4;
 		final int FDY23 = DY23 << 4;
 		final int FDY31 = DY31 << 4;
 
 		// start in corner of block
 		minx &= ~(q - 1);
 		miny &= ~(q - 1);
 		int blockoffset = miny * width;
 		float iZblockoffset = iZ00 + miny * diZ_dy;
 		float cRiZblockoffset = cRiZ00 + miny * dcRiZ_dy;
 		float cGiZblockoffset = cGiZ00 + miny * dcGiZ_dy;
 		float cBiZblockoffset = cBiZ00 + miny * dcBiZ_dy;
 
 		// half-edge constants 24:8
 		int C1 = DY12 * X1 - DX12 * Y1;
 		int C2 = DY23 * X2 - DX23 * Y2;
 		int C3 = DY31 * X3 - DX31 * Y3;
 
 		// correct for fill convention
 		if (DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
 		if (DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
 		if (DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;
 
 		// loop through blocks
 		for (int y = miny; y < maxy; y += q) {
 
 			for (int x = minx; x < maxx; x += q) {
 
 				// corners of block
 				int x0 = x << 4;
 				int x1 = (x + q - 1) << 4;
 				int y0 = y << 4;
 				int y1 = (y + q - 1) << 4;
 
 				// eval half-space functions
 				int a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? 1 : 0;
 				int a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? 2 : 0;
 				int a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? 4 : 0;
 				int a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? 8 : 0;
 				int a = a00 | a10 | a01 | a11;
 
 				int b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? 1 : 0;
 				int b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? 2 : 0;
 				int b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? 4 : 0;
 				int b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? 8 : 0;
 				int b = b00 | b10 | b01 | b11;
 
 				int c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? 1 : 0;
 				int c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? 2 : 0;
 				int c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? 4 : 0;
 				int c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? 8 : 0;
 				int c = c00 | c10 | c01 | c11;
 
 				// skip block when outside an edge
 				if (a == 0 || b == 0 || c == 0) continue;
 
 				int offset = blockoffset;
 				float iZoffset = iZblockoffset + diZ_dx * x;
 				float iZ = iZoffset;
 
 				// setup for affine interpolation over this block
 				// have to clamp calculated colors to avoid 'sparkly edges'
 				float iiZ00 = 1 / iZ;
 				float iiZq0 = 1 / (iZ + diZ_dqx);
 				float iiZ0q = 1 / (iZ + diZ_dqy);
 				float cRiZoffset = cRiZblockoffset + dcRiZ_dx * x;
 				float cGiZoffset = cGiZblockoffset + dcGiZ_dx * x;
 				float cBiZoffset = cBiZblockoffset + dcBiZ_dx * x;
 
 				float cRoffset = cRiZoffset * iiZ00;
 				float cGoffset = cGiZoffset * iiZ00;
 				float cBoffset = cBiZoffset * iiZ00;
 
 				float cR = cRoffset;
 				float cG = cGoffset;
 				float cB = cBoffset;
 
 				float dcR_dx = ((cRiZoffset + dcRiZ_dqx) * iiZq0 - cRoffset) * iq;
 				float dcR_dy = ((cRiZoffset + dcRiZ_dqy) * iiZ0q - cRoffset) * iq;
 				float dcG_dx = ((cGiZoffset + dcGiZ_dqx) * iiZq0 - cGoffset) * iq;
 				float dcG_dy = ((cGiZoffset + dcGiZ_dqy) * iiZ0q - cGoffset) * iq;
 				float dcB_dx = ((cBiZoffset + dcBiZ_dqx) * iiZq0 - cBoffset) * iq;
 				float dcB_dy = ((cBiZoffset + dcBiZ_dqy) * iiZ0q - cBoffset) * iq;
 
 				// TODO proper ztest, stencil test, alpha test...
 
 				// accept whole block when totally covered
 				if (a == 0xF && b == 0xF && c == 0xF) {
 					for (int iy = 0; iy < q; ++iy) {
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// hold on...
 								// if flat or gourard, write predetermined color ( * texture color) to color buffer
 								// if phong, run phong equation
 
 								// zwrite, if enabled and TODO depth test enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, clamp(cR, cRmin, cRmax));
 									unsafe.putFloat(pColor + ix * 16 + 8, clamp(cG, cGmin, cGmax));
 									unsafe.putFloat(pColor + ix * 16 + 12, clamp(cB, cBmin, cBmax));
 								}
 
 							}
 
 							iZ += diZ_dx;
 							cR += dcR_dx;
 							cG += dcG_dx;
 							cB += dcB_dx;
 						}
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 						cRoffset += dcR_dy;
 						cGoffset += dcG_dy;
 						cBoffset += dcB_dy;
 						cR = cRoffset;
 						cG = cGoffset;
 						cB = cBoffset;
 					}
 				} else {
 					// partially covered block
 					int CY1 = C1 + DX12 * y0 - DY12 * x0;
 					int CY2 = C2 + DX23 * y0 - DY23 * x0;
 					int CY3 = C3 + DX31 * y0 - DY31 * x0;
 
 					for (int iy = 0; iy < q; ++iy) {
 						int CX1 = CY1;
 						int CX2 = CY2;
 						int CX3 = CY3;
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (CX1 > 0 && CX2 > 0 && CX3 > 0 && iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// zwrite, if enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, clamp(cR, cRmin, cRmax));
 									unsafe.putFloat(pColor + ix * 16 + 8, clamp(cG, cGmin, cGmax));
 									unsafe.putFloat(pColor + ix * 16 + 12, clamp(cB, cBmin, cBmax));
 								}
 
 							}
 
 							CX1 -= FDY12;
 							CX2 -= FDY23;
 							CX3 -= FDY31;
 							iZ += diZ_dx;
 							cR += dcR_dx;
 							cG += dcG_dx;
 							cB += dcB_dx;
 						}
 
 						CY1 += FDX12;
 						CY2 += FDX23;
 						CY3 += FDX31;
 
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 						cRoffset += dcR_dy;
 						cGoffset += dcG_dy;
 						cBoffset += dcB_dy;
 						cR = cRoffset;
 						cG = cGoffset;
 						cB = cBoffset;
 					}
 
 				}
 
 			}
 
 			blockoffset += q * width;
 			iZblockoffset += diZ_dqy;
 			cRiZblockoffset += dcRiZ_dqy;
 			cGiZblockoffset += dcGiZ_dqy;
 			cBiZblockoffset += dcBiZ_dqy;
 		}
 	}
 
 	/**
 	 * Rasterise a triangle with inverse z and texture u,v interpolated. Algorithm based (heavily) on <a
 	 * href=http://devmaster.net/forums/topic/1145-advanced-rasterization/>this article</a>.
 	 */
 	static final void rasteriseTriangle_z_uv(Unsafe unsafe, long pBase, long pTri, int Yi, int Yf) {
 		// THIS LINE IS ONLY FOR DEBUGGING SEGFAULTS
 		// verifyTriangle(unsafe, pBase, pTri);
 
 		// [ (int) tri_norm_zsign, (int) flags, {56} | (long) pv0, (long) pvt0, (long) pvn0, (long) pvv0, {16},
 		// (float4) vlight0 | <v*1> | <v*2> ]
 
 		// check deleted
 		if ((unsafe.getInt(pTri + 4) & 0x1) != 0) return;
 
 		final int height = unsafe.getInt(pBase + 0x00000004);
 		final int width = unsafe.getInt(pBase + 0x00000000);
 		final long flags = unsafe.getLong(pBase + 0x00000008);
 		final int zsign = unsafe.getInt(pBase + 0x00000068);
 		final boolean frontface = unsafe.getInt(pTri) < 0;
 
 		double vscale = -(height >> 1);
 		double hscale = -(width >> 1);
 
 		// swap vertex order if backface (makes half-space functions behave)
 		final double X1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192))) - 1d) * hscale;
 		final double Y1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192)) + 8) - 1d) * vscale;
 		final double X2d = (unsafe.getDouble(unsafe.getLong(pTri + 128)) - 1d) * hscale;
 		final double Y2d = (unsafe.getDouble(unsafe.getLong(pTri + 128) + 8) - 1d) * vscale;
 		final double X3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64))) - 1d) * hscale;
 		final double Y3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64)) + 8) - 1d) * vscale;
 
 		// use 28.4 fixed point values for x and y
 		final int X1 = (int) (X1d * 16d + 0.5d);
 		final int Y1 = (int) (Y1d * 16d + 0.5d);
 		final int X2 = (int) (X2d * 16d + 0.5d);
 		final int Y2 = (int) (Y2d * 16d + 0.5d);
 		final int X3 = (int) (X3d * 16d + 0.5d);
 		final int Y3 = (int) (Y3d * 16d + 0.5d);
 
 		// block size
 		final int q = 8;
 		final float iq = 1f / q;
 
 		// bounding rectangle, clamped to screen bounds
 		// also skip if no visible area (this implements x,y view frustum culling)
 		int minx = (min(X1, min(X2, X3)) + 0xF) >> 4;
 		int maxx = (max(X1, max(X2, X3)) + 0xF) >> 4;
 		minx = max(minx, 0);
 		maxx = min(maxx, width & ~(q - 1));
 		if (minx >= maxx) return;
 		int miny = (min(Y1, min(Y2, Y3)) + 0xF) >> 4;
 		int maxy = (max(Y1, max(Y2, Y3)) + 0xF) >> 4;
 		miny = max(miny, Yi);
 		maxy = min(maxy, Yf & ~(q - 1));
 		if (miny >= maxy) return;
 
 		// pixel buffer pointers
 		final long pColor = pBase + 0x01DC0900;
 		final long pZ = pBase + 0x05DC0900;
 		final long pStencil = pBase + 0x06E00900;
 		final long pID = pBase + 0x07E00900;
 
 		// material -> textures
 		long pMtl = frontface ? pBase + 0x00000900 : pBase + 0x00040900;
 		long pMap_kd = unsafe.getLong(pMtl + 64);
 		int map_kd_maxlevel = unsafe.getInt(pMap_kd);
 		long pMap_ks = unsafe.getLong(pMtl + 72);
 		int map_ks_maxlevel = unsafe.getInt(pMap_ks);
 		long pMap_ke = unsafe.getLong(pMtl + 80);
 		int map_ke_maxlevel = unsafe.getInt(pMap_ke);
 
 		// mip-maps
 		boolean mipmap_kd = unsafe.getInt(pMap_kd + 4) == 1 && ((flags & 0x4000L) != 0);
 		boolean mipmap_ks = unsafe.getInt(pMap_ks + 4) == 1 && ((flags & 0x4000L) != 0);
 		boolean mipmap_ke = unsafe.getInt(pMap_ke + 4) == 1 && ((flags & 0x4000L) != 0);
 
 		// 1/z, remembering vertex order...
 		double iZ1 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 88 : 216)) + 16);
 		double iZ2 = 1d / unsafe.getDouble(unsafe.getLong(pTri + 152) + 16);
 		double iZ3 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 216 : 88)) + 16);
 
 		// 1/u, 1/v
 		final double UiZ1 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 72 : 200))) * iZ1;
 		final double ViZ1 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 72 : 200)) + 8) * iZ1;
 		final double UiZ2 = unsafe.getDouble(unsafe.getLong(pTri + 136)) * iZ2;
 		final double ViZ2 = unsafe.getDouble(unsafe.getLong(pTri + 136) + 8) * iZ2;
 		final double UiZ3 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 200 : 72))) * iZ3;
 		final double ViZ3 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 200 : 72)) + 8) * iZ3;
 
 		// triangle color
 		final float col_r = unsafe.getFloat(pTri + 116);
 		final float col_g = unsafe.getFloat(pTri + 120);
 		final float col_b = unsafe.getFloat(pTri + 124);
 
 		// barycentric stuff for interpolation
 		// maybs don't do this for polys with a small number of pixels?
 		final double DY23d = Y2d - Y3d;
 		final double DX32d = X3d - X2d;
 		final double DY31d = Y3d - Y1d;
 		final double DX13d = X1d - X3d;
 		final double idet = 1 / (DY23d * (X1d - X3d) + DX32d * (Y1d - Y3d));
 		// compute barycentric coords at top left, 1 px down and 1 px right
 		// use diff of only 1 px to avoid having to divide to get the deltas
 		final double L00_1 = (DY23d * (-X3d) + DX32d * (-Y3d)) * idet;
 		final double L00_2 = (DY31d * (-X3d) + DX13d * (-Y3d)) * idet;
 		final double L00_3 = 1 - L00_1 - L00_2;
 		final double L10_1 = (DY23d * (1 - X3d) + DX32d * (-Y3d)) * idet;
 		final double L10_2 = (DY31d * (1 - X3d) + DX13d * (-Y3d)) * idet;
 		final double L10_3 = 1 - L10_1 - L10_2;
 		final double L01_1 = (DY23d * (-X3d) + DX32d * (1 - Y3d)) * idet;
 		final double L01_2 = (DY31d * (-X3d) + DX13d * (1 - Y3d)) * idet;
 		final double L01_3 = 1 - L01_1 - L01_2;
 
 		// 1/z, u/z, v/z for delta calculation
 		final float iZ00 = (float) (L00_1 * iZ1 + L00_2 * iZ2 + L00_3 * iZ3);
 		final float iZ10 = (float) (L10_1 * iZ1 + L10_2 * iZ2 + L10_3 * iZ3);
 		final float iZ01 = (float) (L01_1 * iZ1 + L01_2 * iZ2 + L01_3 * iZ3);
 
 		final float UiZ00 = (float) (L00_1 * UiZ1 + L00_2 * UiZ2 + L00_3 * UiZ3);
 		final float UiZ10 = (float) (L10_1 * UiZ1 + L10_2 * UiZ2 + L10_3 * UiZ3);
 		final float UiZ01 = (float) (L01_1 * UiZ1 + L01_2 * UiZ2 + L01_3 * UiZ3);
 		final float ViZ00 = (float) (L00_1 * ViZ1 + L00_2 * ViZ2 + L00_3 * ViZ3);
 		final float ViZ10 = (float) (L10_1 * ViZ1 + L10_2 * ViZ2 + L10_3 * ViZ3);
 		final float ViZ01 = (float) (L01_1 * ViZ1 + L01_2 * ViZ2 + L01_3 * ViZ3);
 
 		// interpolation deltas
 		final float diZ_dx = (iZ10 - iZ00);
 		final float diZ_dy = (iZ01 - iZ00);
 		final float diZ_dqy = diZ_dy * q;
 		final float diZ_dqx = diZ_dx * q;
 		final float dUiZ_dx = (UiZ10 - UiZ00);
 		final float dUiZ_dy = (UiZ01 - UiZ00);
 		final float dUiZ_dqy = dUiZ_dy * q;
 		final float dUiZ_dqx = dUiZ_dx * q;
 		final float dViZ_dx = (ViZ10 - ViZ00);
 		final float dViZ_dy = (ViZ01 - ViZ00);
 		final float dViZ_dqy = dViZ_dy * q;
 		final float dViZ_dqx = dViZ_dx * q;
 
 		// deltas 28.4
 		final int DX12 = X1 - X2;
 		final int DX23 = X2 - X3;
 		final int DX31 = X3 - X1;
 		final int DY12 = Y1 - Y2;
 		final int DY23 = Y2 - Y3;
 		final int DY31 = Y3 - Y1;
 
 		// deltas 24.8, because multiplying two 28.4's gives a 24.8
 		final int FDX12 = DX12 << 4;
 		final int FDX23 = DX23 << 4;
 		final int FDX31 = DX31 << 4;
 		final int FDY12 = DY12 << 4;
 		final int FDY23 = DY23 << 4;
 		final int FDY31 = DY31 << 4;
 
 		// start in corner of block
 		minx &= ~(q - 1);
 		miny &= ~(q - 1);
 		int blockoffset = miny * width;
 		float iZblockoffset = iZ00 + miny * diZ_dy;
 		float UiZblockoffset = UiZ00 + miny * dUiZ_dy;
 		float ViZblockoffset = ViZ00 + miny * dViZ_dy;
 
 		// half-edge constants 24:8
 		int C1 = DY12 * X1 - DX12 * Y1;
 		int C2 = DY23 * X2 - DX23 * Y2;
 		int C3 = DY31 * X3 - DX31 * Y3;
 
 		// System.out.printf("%d\t%d\t%d\n", C1, C2, C3);
 
 		// correct for fill convention
 		if (DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
 		if (DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
 		if (DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;
 
 		// loop through blocks
 		for (int y = miny; y < maxy; y += q) {
 
 			for (int x = minx; x < maxx; x += q) {
 
 				// corners of block
 				int x0 = x << 4;
 				int x1 = (x + q - 1) << 4;
 				int y0 = y << 4;
 				int y1 = (y + q - 1) << 4;
 
 				// eval half-space functions
 				int a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? 1 : 0;
 				int a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? 2 : 0;
 				int a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? 4 : 0;
 				int a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? 8 : 0;
 				int a = a00 | a10 | a01 | a11;
 
 				int b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? 1 : 0;
 				int b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? 2 : 0;
 				int b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? 4 : 0;
 				int b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? 8 : 0;
 				int b = b00 | b10 | b01 | b11;
 
 				int c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? 1 : 0;
 				int c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? 2 : 0;
 				int c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? 4 : 0;
 				int c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? 8 : 0;
 				int c = c00 | c10 | c01 | c11;
 
 				// skip block when outside an edge
 				if (a == 0 || b == 0 || c == 0) continue;
 
 				int offset = blockoffset;
 				float iZoffset = iZblockoffset + diZ_dx * x;
 				float iZ = iZoffset;
 
 				// setup for affine interpolation over this block
 				float iiZ00 = 1 / iZ;
 				float iiZq0 = 1 / (iZ + diZ_dqx);
 				float iiZ0q = 1 / (iZ + diZ_dqy);
 				float UiZoffset = UiZblockoffset + dUiZ_dx * x;
 				float ViZoffset = ViZblockoffset + dViZ_dx * x;
 				float Uoffset = UiZoffset * iiZ00;
 				float Voffset = ViZoffset * iiZ00;
 				float U = Uoffset;
 				float V = Voffset;
 				float dU_dx = ((UiZoffset + dUiZ_dqx) * iiZq0 - Uoffset) * iq;
 				float dU_dy = ((UiZoffset + dUiZ_dqy) * iiZ0q - Uoffset) * iq;
 				float dV_dx = ((ViZoffset + dViZ_dqx) * iiZq0 - Voffset) * iq;
 				float dV_dy = ((ViZoffset + dViZ_dqy) * iiZ0q - Voffset) * iq;
 
 				// want mipmap level where this corresponds to at most 1 and greater than 0.5 texels
				float dUV_dxy_min = max(max(abs(dU_dx), abs(dU_dy)), max(abs(dV_dx), abs(dV_dy)));
 				// float exponent -1 => level 0, exponent -2 => level 1
				int mmlevel = 126 - (((Float.floatToRawIntBits(dUV_dxy_min) + 0x00140000) & 0x7F800000) >>> 23);
 
 				int map_kd_level = mipmap_kd ? clamp(mmlevel, 0, map_kd_maxlevel) : map_kd_maxlevel;
 				int map_ke_level = mipmap_ke ? clamp(mmlevel, 0, map_ke_maxlevel) : map_ke_maxlevel;
 
 				// FIXME proper ztest, stencil test, alpha test...
 
 				// accept whole block when totally covered
 				if (a == 0xF && b == 0xF && c == 0xF) {
 					for (int iy = 0; iy < q; ++iy) {
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// texture fetch
 								int qTx_kd = TextureImpl.getTextureOffset(U, V, map_kd_level);
 								float kd_r = unsafe.getFloat(pMap_kd + qTx_kd + 4);
 								float kd_g = unsafe.getFloat(pMap_kd + qTx_kd + 8);
 								float kd_b = unsafe.getFloat(pMap_kd + qTx_kd + 12);
 
 								int qTx_ke = TextureImpl.getTextureOffset(U, V, map_ke_level);
 								float ke_r = unsafe.getFloat(pMap_ke + qTx_ke + 4);
 								float ke_g = unsafe.getFloat(pMap_ke + qTx_ke + 8);
 								float ke_b = unsafe.getFloat(pMap_ke + qTx_ke + 12);
 
 								// zwrite, if enabled and TODO depth test enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, col_r * kd_r + ke_r);
 									unsafe.putFloat(pColor + ix * 16 + 8, col_g * kd_g + ke_g);
 									unsafe.putFloat(pColor + ix * 16 + 12, col_b * kd_b + ke_b);
 								}
 
 							}
 
 							iZ += diZ_dx;
 							U += dU_dx;
 							V += dV_dx;
 						}
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 						Uoffset += dU_dy;
 						Voffset += dV_dy;
 						U = Uoffset;
 						V = Voffset;
 					}
 				} else {
 					// partially covered block
 					int CY1 = C1 + DX12 * y0 - DY12 * x0;
 					int CY2 = C2 + DX23 * y0 - DY23 * x0;
 					int CY3 = C3 + DX31 * y0 - DY31 * x0;
 
 					for (int iy = 0; iy < q; ++iy) {
 						int CX1 = CY1;
 						int CX2 = CY2;
 						int CX3 = CY3;
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (CX1 > 0 && CX2 > 0 && CX3 > 0 && iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// texture fetch
 								int qTx_kd = TextureImpl.getTextureOffset(U, V, map_kd_level);
 								float kd_r = unsafe.getFloat(pMap_kd + qTx_kd + 4);
 								float kd_g = unsafe.getFloat(pMap_kd + qTx_kd + 8);
 								float kd_b = unsafe.getFloat(pMap_kd + qTx_kd + 12);
 
 								int qTx_ke = TextureImpl.getTextureOffset(U, V, map_ke_level);
 								float ke_r = unsafe.getFloat(pMap_ke + qTx_ke + 4);
 								float ke_g = unsafe.getFloat(pMap_ke + qTx_ke + 8);
 								float ke_b = unsafe.getFloat(pMap_ke + qTx_ke + 12);
 
 								// zwrite, if enabled and TODO depth test enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, col_r * kd_r + ke_r);
 									unsafe.putFloat(pColor + ix * 16 + 8, col_g * kd_g + ke_g);
 									unsafe.putFloat(pColor + ix * 16 + 12, col_b * kd_b + ke_b);
 								}
 
 							}
 
 							CX1 -= FDY12;
 							CX2 -= FDY23;
 							CX3 -= FDY31;
 							iZ += diZ_dx;
 							U += dU_dx;
 							V += dV_dx;
 						}
 
 						CY1 += FDX12;
 						CY2 += FDX23;
 						CY3 += FDX31;
 
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 						Uoffset += dU_dy;
 						Voffset += dV_dy;
 						U = Uoffset;
 						V = Voffset;
 					}
 
 				}
 
 			}
 
 			blockoffset += q * width;
 			iZblockoffset += diZ_dqy;
 			UiZblockoffset += dUiZ_dqy;
 			ViZblockoffset += dViZ_dqy;
 		}
 	}
 
 	/**
 	 * Rasterise a triangle with inverse z, vertex colors and texture u,v interpolated. Algorithm based (heavily) on <a
 	 * href=http://devmaster.net/forums/topic/1145-advanced-rasterization/>this article</a>.
 	 */
 	static final void rasteriseTriangle_z_color_uv(Unsafe unsafe, long pBase, long pTri, int Yi, int Yf) {
 		// [ (int) tri_norm_zsign, (int) flags, {56} | (long) pv0, (long) pvt0, (long) pvn0, (long) pvv0, {16},
 		// (float4) vlight0 | <v*1> | <v*2> ]
 
 		// check deleted
 		if ((unsafe.getInt(pTri + 4) & 0x1) != 0) return;
 
 		final int height = unsafe.getInt(pBase + 0x00000004);
 		final int width = unsafe.getInt(pBase + 0x00000000);
 		final long flags = unsafe.getLong(pBase + 0x00000008);
 		final int zsign = unsafe.getInt(pBase + 0x00000068);
 		final boolean frontface = unsafe.getInt(pTri) < 0;
 
 		double vscale = -(height >> 1);
 		double hscale = -(width >> 1);
 
 		// swap vertex order if backface (makes half-space functions behave)
 		final double X1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192))) - 1d) * hscale;
 		final double Y1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192)) + 8) - 1d) * vscale;
 		final double X2d = (unsafe.getDouble(unsafe.getLong(pTri + 128)) - 1d) * hscale;
 		final double Y2d = (unsafe.getDouble(unsafe.getLong(pTri + 128) + 8) - 1d) * vscale;
 		final double X3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64))) - 1d) * hscale;
 		final double Y3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64)) + 8) - 1d) * vscale;
 
 		// use 28.4 fixed point values for x and y
 		final int X1 = (int) (X1d * 16d + 0.5d);
 		final int Y1 = (int) (Y1d * 16d + 0.5d);
 		final int X2 = (int) (X2d * 16d + 0.5d);
 		final int Y2 = (int) (Y2d * 16d + 0.5d);
 		final int X3 = (int) (X3d * 16d + 0.5d);
 		final int Y3 = (int) (Y3d * 16d + 0.5d);
 
 		// block size
 		final int q = 8;
 		final float iq = 1f / q;
 
 		// bounding rectangle, clamped to screen bounds
 		// also skip if no visible area (this implements (limited) x,y view frustum culling)
 		int minx = (min(X1, min(X2, X3)) + 0xF) >> 4;
 		int maxx = (max(X1, max(X2, X3)) + 0xF) >> 4;
 		minx = max(minx, 0);
 		maxx = min(maxx, width & ~(q - 1));
 		if (minx >= maxx) return;
 		int miny = (min(Y1, min(Y2, Y3)) + 0xF) >> 4;
 		int maxy = (max(Y1, max(Y2, Y3)) + 0xF) >> 4;
 		miny = max(miny, Yi);
 		maxy = min(maxy, Yf & ~(q - 1));
 		if (miny >= maxy) return;
 
 		// pixel buffer pointers
 		final long pColor = pBase + 0x01DC0900;
 		final long pZ = pBase + 0x05DC0900;
 		final long pStencil = pBase + 0x06E00900;
 		final long pID = pBase + 0x07E00900;
 
 		// material -> textures
 		long pMtl = frontface ? pBase + 0x00000900 : pBase + 0x00040900;
 		long pMap_kd = unsafe.getLong(pMtl + 64);
 		int map_kd_maxlevel = unsafe.getInt(pMap_kd);
 		long pMap_ks = unsafe.getLong(pMtl + 72);
 		int map_ks_maxlevel = unsafe.getInt(pMap_ks);
 		long pMap_ke = unsafe.getLong(pMtl + 80);
 		int map_ke_maxlevel = unsafe.getInt(pMap_ke);
 
 		// mip-maps
 		boolean mipmap_kd = unsafe.getInt(pMap_kd + 4) == 1 && ((flags & 0x4000L) != 0);
 		boolean mipmap_ks = unsafe.getInt(pMap_ks + 4) == 1 && ((flags & 0x4000L) != 0);
 		boolean mipmap_ke = unsafe.getInt(pMap_ke + 4) == 1 && ((flags & 0x4000L) != 0);
 
 		// 1/z, remembering vertex order...
 		double iZ1 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 88 : 216)) + 16);
 		double iZ2 = 1d / unsafe.getDouble(unsafe.getLong(pTri + 152) + 16);
 		double iZ3 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 216 : 88)) + 16);
 
 		// 1/u, 1/v
 		final double UiZ1 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 72 : 200))) * iZ1;
 		final double ViZ1 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 72 : 200)) + 8) * iZ1;
 		final double UiZ2 = unsafe.getDouble(unsafe.getLong(pTri + 136)) * iZ2;
 		final double ViZ2 = unsafe.getDouble(unsafe.getLong(pTri + 136) + 8) * iZ2;
 		final double UiZ3 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 200 : 72))) * iZ3;
 		final double ViZ3 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 200 : 72)) + 8) * iZ3;
 
 		// vertex colors
 		final float cR1 = unsafe.getFloat(pTri + (frontface ? 116 : 244));
 		final float cG1 = unsafe.getFloat(pTri + (frontface ? 120 : 248));
 		final float cB1 = unsafe.getFloat(pTri + (frontface ? 124 : 252));
 		final float cR2 = unsafe.getFloat(pTri + 180);
 		final float cG2 = unsafe.getFloat(pTri + 184);
 		final float cB2 = unsafe.getFloat(pTri + 188);
 		final float cR3 = unsafe.getFloat(pTri + (frontface ? 244 : 116));
 		final float cG3 = unsafe.getFloat(pTri + (frontface ? 248 : 120));
 		final float cB3 = unsafe.getFloat(pTri + (frontface ? 252 : 124));
 
 		// 1 / color
 		final double cRiZ1 = cR1 * iZ1;
 		final double cGiZ1 = cG1 * iZ1;
 		final double cBiZ1 = cB1 * iZ1;
 		final double cRiZ2 = cR2 * iZ2;
 		final double cGiZ2 = cG2 * iZ2;
 		final double cBiZ2 = cB2 * iZ2;
 		final double cRiZ3 = cR3 * iZ3;
 		final double cGiZ3 = cG3 * iZ3;
 		final double cBiZ3 = cB3 * iZ3;
 
 		// max / min color for clamping
 		final float cRmax = min(max(cR1, max(cR2, cR3)), 1f);
 		final float cGmax = min(max(cG1, max(cG2, cG3)), 1f);
 		final float cBmax = min(max(cB1, max(cB2, cB3)), 1f);
 		final float cRmin = max(min(cR1, min(cR2, cR3)), 0f);
 		final float cGmin = max(min(cG1, min(cG2, cG3)), 0f);
 		final float cBmin = max(min(cB1, min(cB2, cB3)), 0f);
 
 		// barycentric stuff for interpolation
 		// maybs don't do this for polys with a small number of pixels?
 		final double DY23d = Y2d - Y3d;
 		final double DX32d = X3d - X2d;
 		final double DY31d = Y3d - Y1d;
 		final double DX13d = X1d - X3d;
 		final double idet = 1 / (DY23d * (X1d - X3d) + DX32d * (Y1d - Y3d));
 		// compute barycentric coords at top left, 1 px down and 1 px right
 		// use diff of only 1 px to avoid having to divide to get the deltas
 		final double L00_1 = (DY23d * (-X3d) + DX32d * (-Y3d)) * idet;
 		final double L00_2 = (DY31d * (-X3d) + DX13d * (-Y3d)) * idet;
 		final double L00_3 = 1 - L00_1 - L00_2;
 		final double L10_1 = (DY23d * (1 - X3d) + DX32d * (-Y3d)) * idet;
 		final double L10_2 = (DY31d * (1 - X3d) + DX13d * (-Y3d)) * idet;
 		final double L10_3 = 1 - L10_1 - L10_2;
 		final double L01_1 = (DY23d * (-X3d) + DX32d * (1 - Y3d)) * idet;
 		final double L01_2 = (DY31d * (-X3d) + DX13d * (1 - Y3d)) * idet;
 		final double L01_3 = 1 - L01_1 - L01_2;
 
 		// 1/z, color/z, u/z, v/z for delta calculation
 		final float iZ00 = (float) (L00_1 * iZ1 + L00_2 * iZ2 + L00_3 * iZ3);
 		final float iZ10 = (float) (L10_1 * iZ1 + L10_2 * iZ2 + L10_3 * iZ3);
 		final float iZ01 = (float) (L01_1 * iZ1 + L01_2 * iZ2 + L01_3 * iZ3);
 
 		final float cRiZ00 = (float) (L00_1 * cRiZ1 + L00_2 * cRiZ2 + L00_3 * cRiZ3);
 		final float cRiZ10 = (float) (L10_1 * cRiZ1 + L10_2 * cRiZ2 + L10_3 * cRiZ3);
 		final float cRiZ01 = (float) (L01_1 * cRiZ1 + L01_2 * cRiZ2 + L01_3 * cRiZ3);
 		final float cGiZ00 = (float) (L00_1 * cGiZ1 + L00_2 * cGiZ2 + L00_3 * cGiZ3);
 		final float cGiZ10 = (float) (L10_1 * cGiZ1 + L10_2 * cGiZ2 + L10_3 * cGiZ3);
 		final float cGiZ01 = (float) (L01_1 * cGiZ1 + L01_2 * cGiZ2 + L01_3 * cGiZ3);
 		final float cBiZ00 = (float) (L00_1 * cBiZ1 + L00_2 * cBiZ2 + L00_3 * cBiZ3);
 		final float cBiZ10 = (float) (L10_1 * cBiZ1 + L10_2 * cBiZ2 + L10_3 * cBiZ3);
 		final float cBiZ01 = (float) (L01_1 * cBiZ1 + L01_2 * cBiZ2 + L01_3 * cBiZ3);
 
 		final float UiZ00 = (float) (L00_1 * UiZ1 + L00_2 * UiZ2 + L00_3 * UiZ3);
 		final float UiZ10 = (float) (L10_1 * UiZ1 + L10_2 * UiZ2 + L10_3 * UiZ3);
 		final float UiZ01 = (float) (L01_1 * UiZ1 + L01_2 * UiZ2 + L01_3 * UiZ3);
 		final float ViZ00 = (float) (L00_1 * ViZ1 + L00_2 * ViZ2 + L00_3 * ViZ3);
 		final float ViZ10 = (float) (L10_1 * ViZ1 + L10_2 * ViZ2 + L10_3 * ViZ3);
 		final float ViZ01 = (float) (L01_1 * ViZ1 + L01_2 * ViZ2 + L01_3 * ViZ3);
 
 		// interpolation deltas
 		final float diZ_dx = (iZ10 - iZ00);
 		final float diZ_dy = (iZ01 - iZ00);
 		final float diZ_dqy = diZ_dy * q;
 		final float diZ_dqx = diZ_dx * q;
 		final float dcRiZ_dx = cRiZ10 - cRiZ00;
 		final float dcRiZ_dy = cRiZ01 - cRiZ00;
 		final float dcRiZ_dqy = dcRiZ_dy * q;
 		final float dcRiZ_dqx = dcRiZ_dx * q;
 		final float dcGiZ_dx = cGiZ10 - cGiZ00;
 		final float dcGiZ_dy = cGiZ01 - cGiZ00;
 		final float dcGiZ_dqy = dcGiZ_dy * q;
 		final float dcGiZ_dqx = dcGiZ_dx * q;
 		final float dcBiZ_dx = cBiZ10 - cBiZ00;
 		final float dcBiZ_dy = cBiZ01 - cBiZ00;
 		final float dcBiZ_dqy = dcBiZ_dy * q;
 		final float dcBiZ_dqx = dcBiZ_dx * q;
 		final float dUiZ_dx = (UiZ10 - UiZ00);
 		final float dUiZ_dy = (UiZ01 - UiZ00);
 		final float dUiZ_dqy = dUiZ_dy * q;
 		final float dUiZ_dqx = dUiZ_dx * q;
 		final float dViZ_dx = (ViZ10 - ViZ00);
 		final float dViZ_dy = (ViZ01 - ViZ00);
 		final float dViZ_dqy = dViZ_dy * q;
 		final float dViZ_dqx = dViZ_dx * q;
 
 		// deltas 28.4
 		final int DX12 = X1 - X2;
 		final int DX23 = X2 - X3;
 		final int DX31 = X3 - X1;
 		final int DY12 = Y1 - Y2;
 		final int DY23 = Y2 - Y3;
 		final int DY31 = Y3 - Y1;
 
 		// deltas 24.8, because multiplying two 28.4's gives a 24.8
 		final int FDX12 = DX12 << 4;
 		final int FDX23 = DX23 << 4;
 		final int FDX31 = DX31 << 4;
 		final int FDY12 = DY12 << 4;
 		final int FDY23 = DY23 << 4;
 		final int FDY31 = DY31 << 4;
 
 		// start in corner of block
 		minx &= ~(q - 1);
 		miny &= ~(q - 1);
 		int blockoffset = miny * width;
 		float iZblockoffset = iZ00 + miny * diZ_dy;
 		float cRiZblockoffset = cRiZ00 + miny * dcRiZ_dy;
 		float cGiZblockoffset = cGiZ00 + miny * dcGiZ_dy;
 		float cBiZblockoffset = cBiZ00 + miny * dcBiZ_dy;
 		float UiZblockoffset = UiZ00 + miny * dUiZ_dy;
 		float ViZblockoffset = ViZ00 + miny * dViZ_dy;
 
 		// half-edge constants 24:8
 		int C1 = DY12 * X1 - DX12 * Y1;
 		int C2 = DY23 * X2 - DX23 * Y2;
 		int C3 = DY31 * X3 - DX31 * Y3;
 
 		// correct for fill convention
 		if (DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
 		if (DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
 		if (DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;
 
 		// loop through blocks
 		for (int y = miny; y < maxy; y += q) {
 
 			for (int x = minx; x < maxx; x += q) {
 
 				// corners of block
 				int x0 = x << 4;
 				int x1 = (x + q - 1) << 4;
 				int y0 = y << 4;
 				int y1 = (y + q - 1) << 4;
 
 				// eval half-space functions
 				int a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? 1 : 0;
 				int a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? 2 : 0;
 				int a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? 4 : 0;
 				int a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? 8 : 0;
 				int a = a00 | a10 | a01 | a11;
 
 				int b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? 1 : 0;
 				int b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? 2 : 0;
 				int b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? 4 : 0;
 				int b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? 8 : 0;
 				int b = b00 | b10 | b01 | b11;
 
 				int c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? 1 : 0;
 				int c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? 2 : 0;
 				int c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? 4 : 0;
 				int c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? 8 : 0;
 				int c = c00 | c10 | c01 | c11;
 
 				// skip block when outside an edge
 				if (a == 0 || b == 0 || c == 0) continue;
 
 				int offset = blockoffset;
 				float iZoffset = iZblockoffset + diZ_dx * x;
 				float iZ = iZoffset;
 
 				// setup for affine interpolation over this block
 				// have to clamp calculated colors to avoid 'sparkly edges'
 				float iiZ00 = 1 / iZ;
 				float iiZq0 = 1 / (iZ + diZ_dqx);
 				float iiZ0q = 1 / (iZ + diZ_dqy);
 				float cRiZoffset = cRiZblockoffset + dcRiZ_dx * x;
 				float cGiZoffset = cGiZblockoffset + dcGiZ_dx * x;
 				float cBiZoffset = cBiZblockoffset + dcBiZ_dx * x;
 				float UiZoffset = UiZblockoffset + dUiZ_dx * x;
 				float ViZoffset = ViZblockoffset + dViZ_dx * x;
 
 				float cRoffset = cRiZoffset * iiZ00;
 				float cGoffset = cGiZoffset * iiZ00;
 				float cBoffset = cBiZoffset * iiZ00;
 				float Uoffset = UiZoffset * iiZ00;
 				float Voffset = ViZoffset * iiZ00;
 
 				float cR = cRoffset;
 				float cG = cGoffset;
 				float cB = cBoffset;
 				float U = Uoffset;
 				float V = Voffset;
 
 				float dcR_dx = ((cRiZoffset + dcRiZ_dqx) * iiZq0 - cRoffset) * iq;
 				float dcR_dy = ((cRiZoffset + dcRiZ_dqy) * iiZ0q - cRoffset) * iq;
 				float dcG_dx = ((cGiZoffset + dcGiZ_dqx) * iiZq0 - cGoffset) * iq;
 				float dcG_dy = ((cGiZoffset + dcGiZ_dqy) * iiZ0q - cGoffset) * iq;
 				float dcB_dx = ((cBiZoffset + dcBiZ_dqx) * iiZq0 - cBoffset) * iq;
 				float dcB_dy = ((cBiZoffset + dcBiZ_dqy) * iiZ0q - cBoffset) * iq;
 				float dU_dx = ((UiZoffset + dUiZ_dqx) * iiZq0 - Uoffset) * iq;
 				float dU_dy = ((UiZoffset + dUiZ_dqy) * iiZ0q - Uoffset) * iq;
 				float dV_dx = ((ViZoffset + dViZ_dqx) * iiZq0 - Voffset) * iq;
 				float dV_dy = ((ViZoffset + dViZ_dqy) * iiZ0q - Voffset) * iq;
 
 				// want mipmap level where this corresponds to at most 1 and greater than 0.5 texels
				float dUV_dxy_min = max(max(abs(dU_dx), abs(dU_dy)), max(abs(dV_dx), abs(dV_dy)));
 				// float exponent -1 => level 0, exponent -2 => level 1
				int mmlevel = 126 - (((Float.floatToRawIntBits(dUV_dxy_min) + 0x00140000) & 0x7F800000) >>> 23);
 
 				int map_kd_level = mipmap_kd ? clamp(mmlevel, 0, map_kd_maxlevel) : map_kd_maxlevel;
 				int map_ke_level = mipmap_ke ? clamp(mmlevel, 0, map_ke_maxlevel) : map_ke_maxlevel;
 
 				// TODO proper ztest, stencil test, alpha test...
 
 				// accept whole block when totally covered
 				if (a == 0xF && b == 0xF && c == 0xF) {
 					for (int iy = 0; iy < q; ++iy) {
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// texture fetch
 								int qTx_kd = TextureImpl.getTextureOffset(U, V, map_kd_level);
 								float kd_r = unsafe.getFloat(pMap_kd + qTx_kd + 4);
 								float kd_g = unsafe.getFloat(pMap_kd + qTx_kd + 8);
 								float kd_b = unsafe.getFloat(pMap_kd + qTx_kd + 12);
 
 								int qTx_ke = TextureImpl.getTextureOffset(U, V, map_ke_level);
 								float ke_r = unsafe.getFloat(pMap_ke + qTx_ke + 4);
 								float ke_g = unsafe.getFloat(pMap_ke + qTx_ke + 8);
 								float ke_b = unsafe.getFloat(pMap_ke + qTx_ke + 12);
 
 								// zwrite, if enabled and TODO depth test enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, clamp(cR, cRmin, cRmax) * kd_r + ke_r);
 									unsafe.putFloat(pColor + ix * 16 + 8, clamp(cG, cGmin, cGmax) * kd_g + ke_g);
 									unsafe.putFloat(pColor + ix * 16 + 12, clamp(cB, cBmin, cBmax) * kd_b + ke_b);
 								}
 
 							}
 
 							iZ += diZ_dx;
 							cR += dcR_dx;
 							cG += dcG_dx;
 							cB += dcB_dx;
 							U += dU_dx;
 							V += dV_dx;
 						}
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 						cRoffset += dcR_dy;
 						cGoffset += dcG_dy;
 						cBoffset += dcB_dy;
 						cR = cRoffset;
 						cG = cGoffset;
 						cB = cBoffset;
 						Uoffset += dU_dy;
 						Voffset += dV_dy;
 						U = Uoffset;
 						V = Voffset;
 					}
 				} else {
 					// partially covered block
 					int CY1 = C1 + DX12 * y0 - DY12 * x0;
 					int CY2 = C2 + DX23 * y0 - DY23 * x0;
 					int CY3 = C3 + DX31 * y0 - DY31 * x0;
 
 					for (int iy = 0; iy < q; ++iy) {
 						int CX1 = CY1;
 						int CX2 = CY2;
 						int CX3 = CY3;
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (CX1 > 0 && CX2 > 0 && CX3 > 0 && iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// texture fetch
 								int qTx_kd = TextureImpl.getTextureOffset(U, V, map_kd_level);
 								float kd_r = unsafe.getFloat(pMap_kd + qTx_kd + 4);
 								float kd_g = unsafe.getFloat(pMap_kd + qTx_kd + 8);
 								float kd_b = unsafe.getFloat(pMap_kd + qTx_kd + 12);
 
 								int qTx_ke = TextureImpl.getTextureOffset(U, V, map_ke_level);
 								float ke_r = unsafe.getFloat(pMap_ke + qTx_ke + 4);
 								float ke_g = unsafe.getFloat(pMap_ke + qTx_ke + 8);
 								float ke_b = unsafe.getFloat(pMap_ke + qTx_ke + 12);
 
 								// zwrite, if enabled and TODO depth test enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 									unsafe.putFloat(pColor + ix * 16 + 4, clamp(cR, cRmin, cRmax) * kd_r + ke_r);
 									unsafe.putFloat(pColor + ix * 16 + 8, clamp(cG, cGmin, cGmax) * kd_g + ke_g);
 									unsafe.putFloat(pColor + ix * 16 + 12, clamp(cB, cBmin, cBmax) * kd_b + ke_b);
 								}
 
 							}
 
 							CX1 -= FDY12;
 							CX2 -= FDY23;
 							CX3 -= FDY31;
 							iZ += diZ_dx;
 							cR += dcR_dx;
 							cG += dcG_dx;
 							cB += dcB_dx;
 							U += dU_dx;
 							V += dV_dx;
 						}
 
 						CY1 += FDX12;
 						CY2 += FDX23;
 						CY3 += FDX31;
 
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 						cRoffset += dcR_dy;
 						cGoffset += dcG_dy;
 						cBoffset += dcB_dy;
 						cR = cRoffset;
 						cG = cGoffset;
 						cB = cBoffset;
 						Uoffset += dU_dy;
 						Voffset += dV_dy;
 						U = Uoffset;
 						V = Voffset;
 					}
 
 				}
 
 			}
 
 			blockoffset += q * width;
 			iZblockoffset += diZ_dqy;
 			cRiZblockoffset += dcRiZ_dqy;
 			cGiZblockoffset += dcGiZ_dqy;
 			cBiZblockoffset += dcBiZ_dqy;
 			UiZblockoffset += dUiZ_dqy;
 			ViZblockoffset += dViZ_dqy;
 		}
 	}
 
 	/** Interpolate vertex normals and positions (phong N and V). */
 	static final void rasteriseTriangle_nv(Unsafe unsafe, long pBase, long pTri, int Yi, int Yf) {
 		// [ (int) tri_norm_zsign, (int) flags, {56} | (long) pv0, (long) pvt0, (long) pvn0, (long) pvv0, {16},
 		// (float4) vlight0 | <v*1> | <v*2> ]
 		// check deleted
 		if ((unsafe.getInt(pTri + 4) & 0x1) != 0) return;
 		final int height = unsafe.getInt(pBase + 0x00000004);
 		final int width = unsafe.getInt(pBase + 0x00000000);
 		final long flags = unsafe.getLong(pBase + 0x00000008);
 		final int zsign = unsafe.getInt(pBase + 0x00000068);
 		final boolean frontface = unsafe.getInt(pTri) < 0;
 		final long pColor = pBase + 0x01DC0900;
 		final long pZ = pBase + 0x05DC0900;
 		final long pStencil = pBase + 0x06E00900;
 		final long pID = pBase + 0x07E00900;
 
 		double vscale = -(height >> 1);
 		double hscale = -(width >> 1);
 
 		// swap vertex order if backface (makes half-space functions behave)
 		final double X1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192))) - 1d) * hscale;
 		final double Y1d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 64 : 192)) + 8) - 1d) * vscale;
 		final double X2d = (unsafe.getDouble(unsafe.getLong(pTri + 128)) - 1d) * hscale;
 		final double Y2d = (unsafe.getDouble(unsafe.getLong(pTri + 128) + 8) - 1d) * vscale;
 		final double X3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64))) - 1d) * hscale;
 		final double Y3d = (unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 192 : 64)) + 8) - 1d) * vscale;
 
 		// use 28.4 fixed point values for x and y
 		final int X1 = (int) (X1d * 16d + 0.5d);
 		final int Y1 = (int) (Y1d * 16d + 0.5d);
 		final int X2 = (int) (X2d * 16d + 0.5d);
 		final int Y2 = (int) (Y2d * 16d + 0.5d);
 		final int X3 = (int) (X3d * 16d + 0.5d);
 		final int Y3 = (int) (Y3d * 16d + 0.5d);
 
 		// block size
 		final int q = 8;
 		final float iq = 1f / q;
 
 		// bounding rectangle, clamped to screen bounds
 		// also skip if no visible area (this implements x,y view frustum culling)
 		int minx = (Math.min(X1, Math.min(X2, X3)) + 0xF) >> 4;
 		int maxx = (Math.max(X1, Math.max(X2, X3)) + 0xF) >> 4;
 		minx = Math.max(minx, 0);
 		maxx = Math.min(maxx, width & ~(q - 1));
 		if (minx >= maxx) return;
 		int miny = (Math.min(Y1, Math.min(Y2, Y3)) + 0xF) >> 4;
 		int maxy = (Math.max(Y1, Math.max(Y2, Y3)) + 0xF) >> 4;
 		miny = Math.max(miny, Yi);
 		maxy = Math.min(maxy, Yf & ~(q - 1));
 		if (miny >= maxy) return;
 
 		final long pMtl = pBase + (frontface ? 0x00000900 : 0x00040900);
 
 		// 1/z, remembering vertex order...
 		double iZ1 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 88 : 216)) + 16);
 		double iZ2 = 1d / unsafe.getDouble(unsafe.getLong(pTri + 152) + 16);
 		double iZ3 = 1d / unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 216 : 88)) + 16);
 
 		// 1/normal, 1/position
 		final double NxiZ1 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 80 : 208))) * iZ1;
 		final double NyiZ1 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 80 : 208)) + 8) * iZ1;
 		final double NziZ1 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 80 : 208)) + 16) * iZ1;
 		final double VxiZ1 = -unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 88 : 216))) * iZ1;
 		final double VyiZ1 = -unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 88 : 216)) + 8) * iZ1;
 		final double VziZ1 = -unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 88 : 216)) + 16) * iZ1;
 
 		final double NxiZ2 = unsafe.getDouble(unsafe.getLong(pTri + 144)) * iZ2;
 		final double NyiZ2 = unsafe.getDouble(unsafe.getLong(pTri + 144) + 8) * iZ2;
 		final double NziZ2 = unsafe.getDouble(unsafe.getLong(pTri + 144) + 16) * iZ2;
 		final double VxiZ2 = -unsafe.getDouble(unsafe.getLong(pTri + 152)) * iZ2;
 		final double VyiZ2 = -unsafe.getDouble(unsafe.getLong(pTri + 152) + 8) * iZ2;
 		final double VziZ2 = -unsafe.getDouble(unsafe.getLong(pTri + 152) + 16) * iZ2;
 
 		final double NxiZ3 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 208 : 80))) * iZ2;
 		final double NyiZ3 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 208 : 80)) + 8) * iZ2;
 		final double NziZ3 = unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 208 : 80)) + 16) * iZ2;
 		final double VxiZ3 = -unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 216 : 88))) * iZ2;
 		final double VyiZ3 = -unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 216 : 88)) + 8) * iZ2;
 		final double VziZ3 = -unsafe.getDouble(unsafe.getLong(pTri + (frontface ? 216 : 88)) + 16) * iZ2;
 
 		// barycentric stuff for interpolation
 		// maybs don't do this for polys with a small number of pixels?
 		final double DY23d = Y2d - Y3d;
 		final double DX32d = X3d - X2d;
 		final double DY31d = Y3d - Y1d;
 		final double DX13d = X1d - X3d;
 		final double idet = 1 / (DY23d * (X1d - X3d) + DX32d * (Y1d - Y3d));
 		// compute barycentric coords at top left, 1 px down and 1 px right
 		// use diff of only 1 px to avoid having to divide to get the deltas
 		final double L00_1 = (DY23d * (-X3d) + DX32d * (-Y3d)) * idet;
 		final double L00_2 = (DY31d * (-X3d) + DX13d * (-Y3d)) * idet;
 		final double L00_3 = 1 - L00_1 - L00_2;
 		final double L10_1 = (DY23d * (1 - X3d) + DX32d * (-Y3d)) * idet;
 		final double L10_2 = (DY31d * (1 - X3d) + DX13d * (-Y3d)) * idet;
 		final double L10_3 = 1 - L10_1 - L10_2;
 		final double L01_1 = (DY23d * (-X3d) + DX32d * (1 - Y3d)) * idet;
 		final double L01_2 = (DY31d * (-X3d) + DX13d * (1 - Y3d)) * idet;
 		final double L01_3 = 1 - L01_1 - L01_2;
 
 		// 1/z, normal/z, position/z for delta calculation
 		final float iZ00 = (float) (L00_1 * iZ1 + L00_2 * iZ2 + L00_3 * iZ3);
 		final float iZ10 = (float) (L10_1 * iZ1 + L10_2 * iZ2 + L10_3 * iZ3);
 		final float iZ01 = (float) (L01_1 * iZ1 + L01_2 * iZ2 + L01_3 * iZ3);
 
 		final float NxiZ00 = (float) (L00_1 * NxiZ1 + L00_2 * NxiZ2 + L00_3 * NxiZ3);
 		final float NxiZ10 = (float) (L10_1 * NxiZ1 + L10_2 * NxiZ2 + L10_3 * NxiZ3);
 		final float NxiZ01 = (float) (L01_1 * NxiZ1 + L01_2 * NxiZ2 + L01_3 * NxiZ3);
 		final float NyiZ00 = (float) (L00_1 * NyiZ1 + L00_2 * NyiZ2 + L00_3 * NyiZ3);
 		final float NyiZ10 = (float) (L10_1 * NyiZ1 + L10_2 * NyiZ2 + L10_3 * NyiZ3);
 		final float NyiZ01 = (float) (L01_1 * NyiZ1 + L01_2 * NyiZ2 + L01_3 * NyiZ3);
 		final float NziZ00 = (float) (L00_1 * NziZ1 + L00_2 * NziZ2 + L00_3 * NziZ3);
 		final float NziZ10 = (float) (L10_1 * NziZ1 + L10_2 * NziZ2 + L10_3 * NziZ3);
 		final float NziZ01 = (float) (L01_1 * NziZ1 + L01_2 * NziZ2 + L01_3 * NziZ3);
 
 		final float VxiZ00 = (float) (L00_1 * VxiZ1 + L00_2 * VxiZ2 + L00_3 * VxiZ3);
 		final float VxiZ10 = (float) (L10_1 * VxiZ1 + L10_2 * VxiZ2 + L10_3 * VxiZ3);
 		final float VxiZ01 = (float) (L01_1 * VxiZ1 + L01_2 * VxiZ2 + L01_3 * VxiZ3);
 		final float VyiZ00 = (float) (L00_1 * VyiZ1 + L00_2 * VyiZ2 + L00_3 * VyiZ3);
 		final float VyiZ10 = (float) (L10_1 * VyiZ1 + L10_2 * VyiZ2 + L10_3 * VyiZ3);
 		final float VyiZ01 = (float) (L01_1 * VyiZ1 + L01_2 * VyiZ2 + L01_3 * VyiZ3);
 		final float VziZ00 = (float) (L00_1 * VziZ1 + L00_2 * VziZ2 + L00_3 * VziZ3);
 		final float VziZ10 = (float) (L10_1 * VziZ1 + L10_2 * VziZ2 + L10_3 * VziZ3);
 		final float VziZ01 = (float) (L01_1 * VziZ1 + L01_2 * VziZ2 + L01_3 * VziZ3);
 
 		// interpolation deltas
 		final float diZ_dx = (iZ10 - iZ00);
 		final float diZ_dy = (iZ01 - iZ00);
 		final float diZ_dqx = diZ_dx * q;
 		final float diZ_dqy = diZ_dy * q;
 
 		final float dNxiZ_dx = (NxiZ10 - NxiZ00);
 		final float dNxiZ_dy = (NxiZ01 - NxiZ00);
 		final float dNxiZ_dqx = dNxiZ_dx * q;
 		final float dNxiZ_dqy = dNxiZ_dy * q;
 		final float dNyiZ_dx = (NyiZ10 - NyiZ00);
 		final float dNyiZ_dy = (NyiZ01 - NyiZ00);
 		final float dNyiZ_dqx = dNyiZ_dx * q;
 		final float dNyiZ_dqy = dNyiZ_dy * q;
 		final float dNziZ_dx = (NziZ10 - NziZ00);
 		final float dNziZ_dy = (NziZ01 - NziZ00);
 		final float dNziZ_dqx = dNziZ_dx * q;
 		final float dNziZ_dqy = dNziZ_dy * q;
 
 		final float dVxiZ_dx = (VxiZ10 - VxiZ00);
 		final float dVxiZ_dy = (VxiZ01 - VxiZ00);
 		final float dVxiZ_dqx = dVxiZ_dx * q;
 		final float dVxiZ_dqy = dVxiZ_dy * q;
 		final float dVyiZ_dx = (VyiZ10 - VyiZ00);
 		final float dVyiZ_dy = (VyiZ01 - VyiZ00);
 		final float dVyiZ_dqx = dVyiZ_dx * q;
 		final float dVyiZ_dqy = dVyiZ_dy * q;
 		final float dVziZ_dx = (VziZ10 - VziZ00);
 		final float dVziZ_dy = (VziZ01 - VziZ00);
 		final float dVziZ_dqx = dVziZ_dx * q;
 		final float dVziZ_dqy = dVziZ_dy * q;
 
 		// deltas 28.4
 		final int DX12 = X1 - X2;
 		final int DX23 = X2 - X3;
 		final int DX31 = X3 - X1;
 		final int DY12 = Y1 - Y2;
 		final int DY23 = Y2 - Y3;
 		final int DY31 = Y3 - Y1;
 
 		// deltas 24.8, because multiplying two 28.4's gives a 24.8
 		final int FDX12 = DX12 << 4;
 		final int FDX23 = DX23 << 4;
 		final int FDX31 = DX31 << 4;
 		final int FDY12 = DY12 << 4;
 		final int FDY23 = DY23 << 4;
 		final int FDY31 = DY31 << 4;
 
 		// start in corner of block
 		minx &= ~(q - 1);
 		miny &= ~(q - 1);
 		int blockoffset = miny * width;
 		float iZblockoffset = iZ00 + miny * diZ_dy;
 		float NxiZblockoffset = NxiZ00 + miny * dNxiZ_dy;
 		float NyiZblockoffset = NyiZ00 + miny * dNyiZ_dy;
 		float NziZblockoffset = NziZ00 + miny * dNziZ_dy;
 		float VxiZblockoffset = VxiZ00 + miny * dVxiZ_dy;
 		float VyiZblockoffset = VyiZ00 + miny * dVyiZ_dy;
 		float VziZblockoffset = VziZ00 + miny * dVziZ_dy;
 
 		// half-edge constants 24:8
 		int C1 = DY12 * X1 - DX12 * Y1;
 		int C2 = DY23 * X2 - DX23 * Y2;
 		int C3 = DY31 * X3 - DX31 * Y3;
 
 		// correct for fill convention
 		if (DY12 < 0 || (DY12 == 0 && DX12 > 0)) C1++;
 		if (DY23 < 0 || (DY23 == 0 && DX23 > 0)) C2++;
 		if (DY31 < 0 || (DY31 == 0 && DX31 > 0)) C3++;
 
 		// loop through blocks
 		for (int y = miny; y < maxy; y += q) {
 
 			for (int x = minx; x < maxx; x += q) {
 
 				// corners of block
 				int x0 = x << 4;
 				int x1 = (x + q - 1) << 4;
 				int y0 = y << 4;
 				int y1 = (y + q - 1) << 4;
 
 				// eval half-space functions
 				int a00 = C1 + DX12 * y0 - DY12 * x0 > 0 ? 1 : 0;
 				int a10 = C1 + DX12 * y0 - DY12 * x1 > 0 ? 2 : 0;
 				int a01 = C1 + DX12 * y1 - DY12 * x0 > 0 ? 4 : 0;
 				int a11 = C1 + DX12 * y1 - DY12 * x1 > 0 ? 8 : 0;
 				int a = a00 | a10 | a01 | a11;
 
 				int b00 = C2 + DX23 * y0 - DY23 * x0 > 0 ? 1 : 0;
 				int b10 = C2 + DX23 * y0 - DY23 * x1 > 0 ? 2 : 0;
 				int b01 = C2 + DX23 * y1 - DY23 * x0 > 0 ? 4 : 0;
 				int b11 = C2 + DX23 * y1 - DY23 * x1 > 0 ? 8 : 0;
 				int b = b00 | b10 | b01 | b11;
 
 				int c00 = C3 + DX31 * y0 - DY31 * x0 > 0 ? 1 : 0;
 				int c10 = C3 + DX31 * y0 - DY31 * x1 > 0 ? 2 : 0;
 				int c01 = C3 + DX31 * y1 - DY31 * x0 > 0 ? 4 : 0;
 				int c11 = C3 + DX31 * y1 - DY31 * x1 > 0 ? 8 : 0;
 				int c = c00 | c10 | c01 | c11;
 
 				// skip block when outside an edge
 				if (a == 0 || b == 0 || c == 0) continue;
 
 				int offset = blockoffset;
 				float iZoffset = iZblockoffset + diZ_dx * x;
 				float iZ = iZoffset;
 
 				// setup for affine interpolation over this block
 				float NxiZoffset = NxiZblockoffset + dNxiZ_dx * x;
 				float NyiZoffset = NyiZblockoffset + dNyiZ_dx * x;
 				float NziZoffset = NziZblockoffset + dNziZ_dx * x;
 				float VxiZoffset = VxiZblockoffset + dVxiZ_dx * x;
 				float VyiZoffset = VyiZblockoffset + dVyiZ_dx * x;
 				float VziZoffset = VziZblockoffset + dVziZ_dx * x;
 
 				float NxiZ = NxiZoffset;
 				float NyiZ = NyiZoffset;
 				float NziZ = NziZoffset;
 				float VxiZ = VxiZoffset;
 				float VyiZ = VyiZoffset;
 				float VziZ = VziZoffset;
 
 				// TODO proper ztest, stencil test, alpha test...
 
 				// accept whole block when totally covered
 				if (a == 0xF && b == 0xF && c == 0xF) {
 					for (int iy = 0; iy < q; ++iy) {
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// zwrite, if enabled and TODO depth test enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 
 									float iiZ = Util.fastInverse(iZ);
 									float Nx = NxiZ * iiZ;
 									float Ny = NyiZ * iiZ;
 									float Nz = NziZ * iiZ;
 									float Vx = VxiZ * iiZ;
 									float Vy = VyiZ * iiZ;
 									float Vz = VziZ * iiZ;
 
 									// normalise N, V
 									float imN = Util.fastInverseSqrt(Nx * Nx + Ny * Ny + Nz * Nz);
 									Nx *= imN;
 									Ny *= imN;
 									Nz *= imN;
 									float imV = Util.fastInverseSqrt(Vx * Vx + Vy * Vy + Vz * Vz);
 
 									// phong equation
 									LightingEquations.runPhong(unsafe, pBase, pColor + ix * 16 + 4,
 											unsafe.getFloat(pMtl + 4), unsafe.getFloat(pMtl + 8),
 											unsafe.getFloat(pMtl + 12), unsafe.getFloat(pMtl + 20),
 											unsafe.getFloat(pMtl + 24), unsafe.getFloat(pMtl + 28),
 											unsafe.getFloat(pMtl + 36), unsafe.getFloat(pMtl + 40),
 											unsafe.getFloat(pMtl + 44), unsafe.getFloat(pMtl + 32), Nx, Ny, Nz, Vx
 													* imV, Vy * imV, Vz * imV, Vx, Vy, Vz);
 
 								}
 
 							}
 
 							iZ += diZ_dx;
 							NxiZ += dNxiZ_dx;
 							NyiZ += dNyiZ_dx;
 							NziZ += dNziZ_dx;
 							VxiZ += dVxiZ_dx;
 							VyiZ += dVyiZ_dx;
 							VziZ += dVziZ_dx;
 						}
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 						NxiZoffset += dNxiZ_dy;
 						NyiZoffset += dNyiZ_dy;
 						NziZoffset += dNziZ_dy;
 						VxiZoffset += dVxiZ_dy;
 						VyiZoffset += dVyiZ_dy;
 						VziZoffset += dVziZ_dy;
 						NxiZ = NxiZoffset;
 						NyiZ = NyiZoffset;
 						NziZ = NziZoffset;
 						VxiZ = VxiZoffset;
 						VyiZ = VyiZoffset;
 						VziZ = VziZoffset;
 					}
 				} else {
 					// partially covered block
 					int CY1 = C1 + DX12 * y0 - DY12 * x0;
 					int CY2 = C2 + DX23 * y0 - DY23 * x0;
 					int CY3 = C3 + DX31 * y0 - DY31 * x0;
 
 					for (int iy = 0; iy < q; ++iy) {
 						int CX1 = CY1;
 						int CX2 = CY2;
 						int CX3 = CY3;
 						int j = x + offset + q;
 						for (int ix = x + offset; ix < j; ++ix) {
 							if (CX1 > 0 && CX2 > 0 && CX3 > 0 && iZ > zsign * unsafe.getFloat(pZ + ix * 4)) {
 
 								// zwrite, if enabled
 								if ((flags & 0x100000L) != 0) unsafe.putFloat(pZ + ix * 4, iZ * zsign);
 
 								// colorwrite, if enabled
 								if ((flags & 0x80000L) != 0) {
 
 									float iiZ = Util.fastInverse(iZ);
 									float Nx = NxiZ * iiZ;
 									float Ny = NyiZ * iiZ;
 									float Nz = NziZ * iiZ;
 									float Vx = VxiZ * iiZ;
 									float Vy = VyiZ * iiZ;
 									float Vz = VziZ * iiZ;
 
 									// normalise N, V
 									float imN = Util.fastInverseSqrt(Nx * Nx + Ny * Ny + Nz * Nz);
 									Nx *= imN;
 									Ny *= imN;
 									Nz *= imN;
 									float imV = Util.fastInverseSqrt(Vx * Vx + Vy * Vy + Vz * Vz);
 
 									// phong equation
 									LightingEquations.runPhong(unsafe, pBase, pColor + ix * 16 + 4,
 											unsafe.getFloat(pMtl + 4), unsafe.getFloat(pMtl + 8),
 											unsafe.getFloat(pMtl + 12), unsafe.getFloat(pMtl + 20),
 											unsafe.getFloat(pMtl + 24), unsafe.getFloat(pMtl + 28),
 											unsafe.getFloat(pMtl + 36), unsafe.getFloat(pMtl + 40),
 											unsafe.getFloat(pMtl + 44), unsafe.getFloat(pMtl + 32), Nx, Ny, Nz, Vx
 													* imV, Vy * imV, Vz * imV, Vx, Vy, Vz);
 
 								}
 							}
 
 							CX1 -= FDY12;
 							CX2 -= FDY23;
 							CX3 -= FDY31;
 							iZ += diZ_dx;
 							NxiZ += dNxiZ_dx;
 							NyiZ += dNyiZ_dx;
 							NziZ += dNziZ_dx;
 							VxiZ += dVxiZ_dx;
 							VyiZ += dVyiZ_dx;
 							VziZ += dVziZ_dx;
 						}
 
 						CY1 += FDX12;
 						CY2 += FDX23;
 						CY3 += FDX31;
 
 						offset += width;
 						iZoffset += diZ_dy;
 						iZ = iZoffset;
 						NxiZoffset += dNxiZ_dy;
 						NyiZoffset += dNyiZ_dy;
 						NziZoffset += dNziZ_dy;
 						VxiZoffset += dVxiZ_dy;
 						VyiZoffset += dVyiZ_dy;
 						VziZoffset += dVziZ_dy;
 						NxiZ = NxiZoffset;
 						NyiZ = NyiZoffset;
 						NziZ = NziZoffset;
 						VxiZ = VxiZoffset;
 						VyiZ = VyiZoffset;
 						VziZ = VziZoffset;
 					}
 
 				}
 
 			}
 
 			blockoffset += q * width;
 			iZblockoffset += diZ_dqy;
 			NxiZblockoffset += dNxiZ_dqy;
 			NyiZblockoffset += dNyiZ_dqy;
 			NziZblockoffset += dNziZ_dqy;
 			VxiZblockoffset += dVxiZ_dqy;
 			VyiZblockoffset += dVyiZ_dqy;
 			VziZblockoffset += dVziZ_dqy;
 		}
 	}
 
 }
