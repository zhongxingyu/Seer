 package com.slsw.fiarworks.masker;
 
 import android.graphics.Bitmap;
 import android.opengl.Matrix;
 
 import com.slsw.fiarworks.bitmapTools.BlockDCT;
 import com.slsw.fiarworks.bitmapTools.PixelTools;
 
 public class AlphaMake {
 	public enum SkyPos{
 		up(),
 		down(),
 		left(),
 		right();
 	}
 	
 	public enum PhonePos{
 		sky(),
 		center(),
 		down(),
 	}
 	
 	public static final int BACKROUND =  0xffffffff;
 	public static final int OPAQUE = 0;
 	public static final int CUTOFF = 64;
 
 	public static Bitmap makeSimpleMask(byte[] image, int width,int height,
 			float[] currRot) {
 		int[] mask = new int[height*width];
 		for (int i = 0; i < mask.length; i++) {
 			
 			if (i < height * width / 2) {
 				mask[i] = BACKROUND;
 			} else {
 				mask[i] = OPAQUE;
 			}
 		}
 		return Bitmap.createBitmap(mask, width, height, Bitmap.Config.ARGB_8888);
 	}
 	
 	public static Bitmap coolMask(byte[] image, int width,int height,
 			float[] currRot) {
 		int[] mask = new int[height*width];
 		
 		int[] luma = PixelTools.YUBtoLuma(image, width, height);
 		
 		double[] norms = BlockDCT.computeNorms(luma, width, height, BlockDCT.BlockSize._8x8, BlockDCT.Norm.L1);
 		
 		for (int i = 0; i<norms.length; i++){
 			if (norms[i] < CUTOFF)
 			{
 				mask[i] = BACKROUND;
 			}
 			else {
 				mask[i] = OPAQUE;
 			}
 		}
 		
 		return Bitmap.createBitmap(mask, width, height, Bitmap.Config.ARGB_8888);
 	}
 	
 	public static Bitmap SkyFillMask(byte[] image, int width,int height,
 			float[] currRot) {
 		int[] mask = new int[height*width];
 		
 		int[] luma = PixelTools.YUBtoLuma(image, width, height);
 		
 		double[] norms = BlockDCT.computeNorms(luma, width, height, BlockDCT.BlockSize._8x8, BlockDCT.Norm.L1);
 		
 		for (int i = 0; i<norms.length; i++){
 			if (norms[i] < CUTOFF)
 			{
 				mask[i] = BACKROUND;
 			}
 			else {
 				mask[i] = OPAQUE;
 			}
 			
 		}
 		
 		
 		
 		return Bitmap.createBitmap(mask, width, height, Bitmap.Config.ARGB_8888);
 	}
 	
 	private SkyPos getSkyDir(float[] currRot){
		float[] newVecY = new float[3];
		float[] newVecX = new float[3];
		Matrix.multiplyMV(newVecY, 0, currRot, 0,  new float[]{(float)0, (float)1, (float)0}, 0);
		Matrix.multiplyMV(newVecX, 0, currRot, 0,  new float[]{(float)1, (float)0, (float)0}, 0);
		if(Math.abs(newVecY[2])<Math.abs(newVecX[2])){
			if(newVecX[2]>0){
 				return SkyPos.up;
 			} else{
 				return SkyPos.down;
 			}
 		} else{
			if(newVecY[2]>0){
 				return SkyPos.left;
 			} else{
 				return SkyPos.right;
 			}
 		}
 	}
 	private PhonePos getPhoneDir(float[] currRot){
		float[] newVec = new float[3];
		Matrix.multiplyMV(newVec, 0, currRot, 0,  new float[]{(float)0, (float)0, (float)1}, 0);
		if(newVec[2]>.5) return PhonePos.sky;
		if(newVec[2]<-.5) return PhonePos.down;
 		return PhonePos.center;
 	}

 }
