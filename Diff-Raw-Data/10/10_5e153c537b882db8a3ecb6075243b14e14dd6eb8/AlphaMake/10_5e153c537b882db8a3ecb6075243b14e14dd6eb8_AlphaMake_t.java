 package com.slsw.fiarworks.masker;
 
 import java.util.Arrays;
 
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
 	
 	public static final int BACKGROUND =  0xffffffff;
 	public static final int OPAQUE = 0;
 	public static final int CUTOFF = 64;
 	private static BlockDCT.BlockSize bsize = BlockDCT.BlockSize._8x8;
 	
 	public static Bitmap makeSimpleMask(byte[] image, int width,int height,
 			float[] currRot) {
 		int[] mask = new int[height*width];
 		for (int i = 0; i < mask.length; i++) {
 			
 			if (i < height * width / 2) {
 				mask[i] = BACKGROUND;
 			} else {
 				mask[i] = OPAQUE;
 			}
 		}
 		return Bitmap.createBitmap(mask, width, height, Bitmap.Config.ARGB_8888);
 	}
 
 	
 	
 	public static Bitmap coolMask(byte[] image, int width,int height,
 			float[] currRot) {
 		
 		int n=1<<bsize.getLog();
 		int mx=width/n;
 		int my=height/n;
 		int[] mask = new int[height*width];
 		
 		int[] luma = PixelTools.YUBtoLuma(image, width, height);
 		
 		double[] norms = BlockDCT.computeNorms(luma, width, height, bsize, BlockDCT.Norm.L1);
 		
		for (int y = 0; y<height; y++){
			for (int x = 0; x<width; x++){
 			int bx=x/n;
 			int by=y/n;
 			if (norms[by * mx + bx] < CUTOFF)
 			{
				mask[y * width + x] = BACKGROUND;
 			}
 			else {
				mask[y * width + x] = OPAQUE;
 			}
 			}
 		}
 		
 		return Bitmap.createBitmap(mask, width, height, Bitmap.Config.ARGB_8888);
 	}
 	
 	public static Bitmap SkyFillMask(byte[] image, int width, int height,
 			float[] currRot) {
 
 		int[] mask = new int[height * width];
 
 		int[] luma = PixelTools.YUBtoLuma(image, width, height);
 
 		double[] norms = BlockDCT.computeNorms(luma, width, height,
 				BlockDCT.BlockSize._8x8, BlockDCT.Norm.L1);
 
 		for (int i = 0; i < norms.length; i++) {
 			if (norms[i] < CUTOFF) {
 				mask[i] = BACKGROUND;
 			} else {
 				mask[i] = OPAQUE;
 			}
 		}
 		switch (getPhoneDir(currRot)) {
 		case center:
 			switch (getSkyDir(currRot)) {
 			case down:
 				break;
 			case left:
 				break;
 			case right:
 				break;
 			case up:
 				break;
 			default:
 				break;
 
 			}
 			break;
 		case down:
 			mask = new int[height * width];
 			break;
 		case sky:
 			Arrays.fill(mask, BACKGROUND);
 			System.err.println("I am looking up");
 			break;
 		default:
 			break;
 		}
 
 		return Bitmap
 				.createBitmap(mask, width, height, Bitmap.Config.ARGB_8888);
 	}
 	
 
 	private static SkyPos getSkyDir(float[] currRot){
 		float newY = currRot[7];
 		float newX = currRot[6];
 		if(Math.abs(newY)<Math.abs(newX)){
 			if(newX>0){
 				return SkyPos.up;
 			} else{
 				return SkyPos.down;
 			}
 		} else{
 			if(newY>0){
 				return SkyPos.left;
 			} else{
 				return SkyPos.right;
 			}
 		}
 	}
 
 	private static PhonePos getPhoneDir(float[] currRot){
 		float newZ = currRot[8];
 		if(newZ>-.5) return PhonePos.sky;
 		if(newZ<.5) return PhonePos.down;
 		return PhonePos.center;
 	}
 }
