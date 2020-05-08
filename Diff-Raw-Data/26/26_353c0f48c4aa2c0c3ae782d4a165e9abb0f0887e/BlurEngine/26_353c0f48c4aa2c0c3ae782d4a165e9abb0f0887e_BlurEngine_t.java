 package com.mintech.blur;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Picture;
 import android.graphics.Bitmap.Config;
 import android.renderscript.ScriptIntrinsicBlur;
 import android.util.DisplayMetrics;
 import android.renderscript.Allocation;
 import android.renderscript.Element;
 import android.renderscript.RenderScript;
 
 public class BlurEngine {
 	private RenderScript mRS;
 	private ScriptC_horzblur mHorizontalScript;
 	private ScriptC_vertblur mVerticalScript;
 	private ScriptC_Gaussian mGaussianScript;
 
 	private Allocation alloc1;
 	private Allocation alloc2;
 
 	private void hblur(int radius, Allocation index, Allocation in, Allocation out) {
 	    mHorizontalScript.set_W(radius);
 	    mHorizontalScript.bind_in(in);
 	    mHorizontalScript.bind_out(out);
 	    mHorizontalScript.invoke_init_calc();
 	    mHorizontalScript.forEach_root(index);
 	}
 
 	private void vblur(int radius, Allocation index, Allocation in, Allocation out) {
 	    mHorizontalScript.set_W(radius);
 	    mVerticalScript.bind_in(in);
 	    mVerticalScript.bind_out(out);
 	    mVerticalScript.invoke_init_calc();
 	    mVerticalScript.forEach_root(index);
 	}
 
 	private void gBlur(Bitmap src, Bitmap dst) {
 		
 	    Allocation in = Allocation.createFromBitmap(mRS, src);
 	    Allocation out = Allocation.createTyped(mRS, alloc1.getType());
 	    
 	    mGaussianScript.set_gIn(in);
 	    mGaussianScript.set_gOut(out);
 	    mGaussianScript.set_gScript(mGaussianScript);
 	    
 	    mGaussianScript.invoke_filter();
 	    out.copyTo(dst);
 	}
 
 	Bitmap blur(Bitmap org, int radius) {
 	    Bitmap out = Bitmap.createBitmap(org.getWidth(), org.getHeight(), org.getConfig());
 
 	    blur(org, out, radius);
 
 	    return out;
 	}
 
 	private Allocation createIndex(int size) {
 	    Element element = Element.U16(mRS);
 	    Allocation allocation = Allocation.createSized(mRS, element, size);
 	    short[] rows = new short[size];
 	    for (int i = 0; i < rows.length; i++) rows[i] = (short)i;
 	    allocation.copyFrom(rows);
 
 	    return allocation;
 	}
 
 	private void blur(Bitmap src, Bitmap dst, int r) {
 	    Allocation alloc1 = Allocation.createFromBitmap(mRS, src);
 	    Allocation alloc2 = Allocation.createTyped(mRS, alloc1.getType());
 
 	    Allocation hIndexAllocation = createIndex(alloc1.getType().getY());
 	    Allocation vIndexAllocation = createIndex(alloc1.getType().getX());
 
 	    // Iteration 1
 	    hblur(r, hIndexAllocation, alloc1, alloc2);
 	    vblur(r, vIndexAllocation, alloc2, alloc1);
 	    // Iteration 2
 	    hblur(r, hIndexAllocation, alloc1, alloc2);
 	    vblur(r, vIndexAllocation, alloc2, alloc1);
 	    // Add more iterations if you like or simply make a loop
 	    alloc1.copyTo(dst);
 	}
 	
 	private Context mContext;
 
 	public BlurEngine(Context context) {
 		mContext = context;
 		mRS = RenderScript.create(mContext);
 		mHorizontalScript = new ScriptC_horzblur(mRS);
 		mVerticalScript = new ScriptC_vertblur(mRS);
 	}
 
 	public Bitmap picture2Bitmap(Picture picture) {
 		Bitmap src = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Config.ARGB_8888);
 		Canvas canvas = new Canvas(src);
		canvas.setDensity(DisplayMetrics.DENSITY_DEFAULT/8);
 		canvas.drawPicture(picture);
 		
		src = Bitmap.createScaledBitmap(src, picture.getWidth()/8, picture.getHeight()/8, true);
 //		gBlur(src, dst);
 //		bitmap = blur(bitmap, 5);
 		blurByRenderscript(src, 5.f);
 //		blurfast(bitmap, 5);
 		return src;
 	}
 
 //  // This functions is worked on API 17
 	private void blurByRenderscript(Bitmap src, float radius) {
         final Allocation input = Allocation.createFromBitmap(mRS, src); //, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
         final Allocation output = Allocation.createTyped(mRS, input.getType() );
         final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(mRS, Element.U8_4( mRS ) );
        script.setRadius( radius );
         script.setInput(input);
         script.forEach(output);
         output.copyTo(src);
 	}
 
 	public void blurfast(Bitmap bmp, int radius) {
 		int w = bmp.getWidth();
 		int h = bmp.getHeight();
 		int[] pix = new int[w * h];
 		bmp.getPixels(pix, 0, w, 0, 0, w, h);
 
 		for(int r = radius; r >= 1; r /= 2) {
 			for(int i = r; i < h - r; i++) {
 				for(int j = r; j < w - r; j++) {
 					int tl = pix[(i - r) * w + j - r];
 					int tr = pix[(i - r) * w + j + r];
 					int tc = pix[(i - r) * w + j];
 					int bl = pix[(i + r) * w + j - r];
 					int br = pix[(i + r) * w + j + r];
 					int bc = pix[(i + r) * w + j];
 					int cl = pix[i * w + j - r];
 					int cr = pix[i * w + j + r];
 
 					pix[(i * w) + j] = 0xFF000000 |
 							(((tl & 0xFF) + (tr & 0xFF) + (tc & 0xFF) + (bl & 0xFF) + (br & 0xFF) + (bc & 0xFF) + (cl & 0xFF) + (cr & 0xFF)) >> 3) & 0xFF |
 							(((tl & 0xFF00) + (tr & 0xFF00) + (tc & 0xFF00) + (bl & 0xFF00) + (br & 0xFF00) + (bc & 0xFF00) + (cl & 0xFF00) + (cr & 0xFF00)) >> 3) & 0xFF00 |
 							(((tl & 0xFF0000) + (tr & 0xFF0000) + (tc & 0xFF0000) + (bl & 0xFF0000) + (br & 0xFF0000) + (bc & 0xFF0000) + (cl & 0xFF0000) + (cr & 0xFF0000)) >> 3) & 0xFF0000;
 				}
 			}
 		}
 		bmp.setPixels(pix, 0, w, 0, 0, w, h);
 	}
 	
 }
