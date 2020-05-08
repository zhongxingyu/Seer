 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 //#ifndef BlackBerry
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Image;
 //#endif
 
 public class ImageContext {
 
 //#ifdef MM.blitWithImage
     public static boolean s_blitWithImage = MiniPlayer.getJadProperty("MeMo-BlitWithImage").equals("true");
 //#endif
    // +1 : Alcatel OT-800++ bugs with drawRGB() on last pixel line
    public static char s_blitPlus = MiniPlayer.getJadProperty("MeMo-BlitPlus").equals("true") ? (char)1 : 0;
 
 //#ifdef debug.console
     private static boolean bLogImage = MiniPlayer.getJadProperty("MeMo-Debug-Image").equals("true");
 //#endif
     
     
     Image m_srcImg, m_transImg;
     int m_srcW, m_srcH, m_dstW, m_dstH, m_srcSize, m_dstSize/*RCA*/;
     private int [] m_srcD, m_dstD/*RCA*/;
     private int m_trs, m_rot/*RCA*/;
 
     // for making background transparent
     private int m_fgColor, m_bgColor;
     private boolean m_makeBgTransparent = false;
 
     private String m_name;
     private boolean m_filterMode;
     private ImageContext m_next;
     private int m_count;
 
     static ImageContext get (Context c, String name) {
         ImageContext ic = null;
         //Logger.println ("IC.get: creating ic "+fullName);
         Image img = c.getImage (name);
         ic = new ImageContext ();
         ic.m_count = 1;
 //#ifdef debug.console
         ic.m_name = name;
 //#endif
         ic.setImage (img);
         return ic;
     }
 
     void release () { 
         m_count--;
         if (m_count == 0) {
             m_name = null;
             m_srcImg = null;
             m_transImg = null;
             m_srcD = null;
             m_dstD = null;
             m_srcW = m_srcH = 0;
         } else if (m_count < 0) {
             Logger.println ("IC.release: Unexpected negative count: "+m_count+" for "+m_name);
         }
     }
 
     ImageContext () {
     }
 
     void setImage (Image image) {
         m_srcImg = image;
         if (image != null) {
             m_srcW = image.getWidth();
             m_srcH = image.getHeight();
             m_srcSize = m_srcW*m_srcH;//RCA
         } else {
             m_srcW = m_srcH = 0;
         }
         m_srcD = m_dstD = null;
         m_dstW = m_dstH = 0;
     }
 
     void setFilterMode (boolean mode) {
         m_filterMode = mode;
     }
 
     void cleanDest () {//FTE
         m_transImg = null;
         m_srcD = null;
         m_dstD = null;
         m_srcW = m_srcH = 0;
     }
 
     static void applyScale (int srcW, int srcH, int [] srcD, 
                             int dstW, int dstH, int [] dstD, int trs) { 
         int stepX = (srcW<<12) / dstW;
         int stepY = (srcH<<12) / dstH;
         int srcY = 0;
         int srcLine = 0;
         int dstIdx = 0;
         if (trs == 0) {
             if (srcW != dstW || srcH != dstH) {
                 int x;
                 for (int y = 0; y < dstH; y++) {
                     srcLine = ((srcY>>12)*srcW)<<12;
                     for (x = 0; x < dstW; x++) {
                         dstD [dstIdx++] = srcD [srcLine>>>12];
                         srcLine += stepX;
                     }
                     srcY += stepY;
                 }
             } else {
                 System.arraycopy (srcD, 0, dstD, 0, srcW*srcH);
             }
         } else {
             trs = 255-FixFloat.fix2int (trs*255);
             if (srcW != dstW || srcH != dstH) {
                 int x;
                 for (int y = 0; y < dstH; y++) {
                     srcLine = ((srcY>>12)*srcW)<<12;
                     for (x = 0; x < dstW; x++) {
                         int c = srcD [srcLine>>>12];
                         int a = ((c >>> 24)*trs) >> 8;
                         dstD [dstIdx++] = (c & 0x00FFFFFF)|(a<<24);
                         srcLine += stepX;
                     }
                     srcY += stepY;
                 }
             } else {
                 int n = srcW*srcH;
                 for (int i = 0; i < n; i++) {
                     int c = srcD [i];
                     int a = ((c >>> 24)*trs) >> 8;
                     dstD [i] = (c & 0x00FFFFFF)|(a<<24);
                 }
             }
         }
     }
     //RCA
 
     // This section is about fileterd scaling. It is a collection of static utility methods and arrays.
 
     // arrays that will contain precomputed offsets and weights
     static int [] hOffset = null;
     static int [] hWeight = null;
     static int [] vOffset = null;
     static int [] vWeight = null;
 
     // check if the above arrays are big enought to store precomputed scaling values
     //dstW is the destination image width
     static void checkArrays (int dstW, int dstH) {
         if (hOffset == null || hOffset.length < dstW) {
             hOffset = new int [dstW];
             hWeight = new int [dstW];
         }
         if (vOffset == null || vOffset.length < dstH) {
             vOffset = new int [dstH];
             vWeight = new int [dstH];
         }
     }
 
     static void computeOffsetsAndWeights (int src, int dst, int [] texOffset, int [] texWeight) {
         // pre-compute the horizontal offsets and weights
         int texFloatPoint = 0;
         int texMaxPoint = (src-1)<<16; // -2 because we may filter at the end of a line maxPoint and maxPoint+1 => maxPoint+1 = src-1 
         int texFloatInc =  texMaxPoint/dst;
         for (int l = 0; l < dst; l++) {
             //if (texFloatPoint > texMaxPoint) {
             //texOffset[l] = texMaxPoint >> 16;
             //texWeight[l] = (texMaxPoint & 0xFF00) >> 8;
             //Logger.println ("computeOffsetsAndWeights: unexpected overflow for "+l+" / "+dst+", o:"+texOffset[l]+", w:"+texWeight[l]);
             //} else {
             texOffset[l] = texFloatPoint >> 16;
             texWeight[l] = (texFloatPoint & 0xFFFF) >> 8;
             texFloatPoint += texFloatInc;
             //}
         }
     }
 
     static void applyFilteredUpscale (int srcW, int srcH, int [] srcD, 
                                       int dstW, int dstH, int [] dstD, int trs) {
         checkArrays (dstW, dstH);
         computeOffsetsAndWeights (srcW, dstW, hOffset, hWeight);
         computeOffsetsAndWeights (srcH, dstH, vOffset, vWeight);
 
         int i, j; //just loop indices
         int dstLine = 0; // offset to each begining of line
         int VWeight, oneMinusVWeight; // temporary vertical weights
         int w00, w01, w10, w11, dst, src, c00, c01, c10, c11, a, r, g, b, index;
         int alpha = 256-FixFloat.fix2int (trs*256);
         for (j = 0; j < dstH; j++) {
             VWeight = vWeight[j];
             oneMinusVWeight = 256 - VWeight;
             dst = dstLine;
             src = vOffset[j]*srcW;
             for (i = 0; i < dstW; i++, dst++) {
                 index = src+hOffset[i];
                 w00 = hWeight[i]*VWeight;
                 w01 = (256 - hWeight[i])*VWeight;
                 w10 = hWeight[i]*oneMinusVWeight;
                 w11 = (256 - hWeight[i])*oneMinusVWeight;
                 c11 = srcD[index];
                 c10 = srcD[index+1];
                 c01 = srcD[index+srcW];
                 c00 = srcD[index+srcW+1];
                 a = (((c00 & 0xFF000000) >>> 24) * w00 + ((c01 & 0xFF000000) >>> 24) * w01 + ((c10 & 0xFF000000) >>> 24) * w10 + ((c11 & 0xFF000000) >>> 24) * w11) >> 16;
                 r = (((c00 & 0xFF0000) >> 16) * w00 + ((c01 & 0xFF0000) >> 16) * w01 + ((c10 & 0xFF0000) >> 16) * w10 + ((c11 & 0xFF0000) >> 16) * w11) >> 16;; 
                 g = (((c00 & 0xFF00) >> 8) * w00 + ((c01 & 0xFF00) >> 8) * w01 + ((c10 & 0xFF00) >> 8) * w10 + ((c11 & 0xFF00) >> 8) * w11) >> 16; 
                 b = (((c00 & 0xFF) >> 0) * w00 + ((c01 & 0xFF) >> 0) * w01 + ((c10 & 0xFF) >> 0) * w10 + ((c11 & 0xFF) >> 0) * w11) >> 16;
                 dstD[dst] =  (((a*alpha) & 0xFF00) << 16) + (r << 16) + (g << 8) + b;
             }
             dstLine += dstW;
         }
     }
 
     // end of filtered section
     // =======================
 
     void applyTransparency (int [] src, int len, int bg, int fg) {
         bg = 0xFF000000 + (bg & 0xFFFFFF);
         fg = 0xFF000000 + (fg & 0xFFFFFF);
         int fg2 = fg & 0xFFFFFF; // take out the alpha component
         
         int b = bg & 0xFF;
         int coef = 0xFFFF / (b-(fg & 0xFF));
 
         for (int i = 0; i < len; i++) {
             int p = src [i]; 
             if (p == bg) {
                 src[i] = fg2;
             } else if (p != fg) {
                 int a = (coef * (b - (p & 0xFF))) >> 8;
                 //Logger.println ("IC: "+Integer.toHexString (p)+" / "+Integer.toHexString (fg)+" => "+a);
                 if (a < 250) {
                     src[i] = fg2 | (a << 24);
                 }
             }
         }
     }
     
     void makeTransparency (int bg, int fg) {
         if (m_srcD == null) {
             m_srcD = new int [m_srcSize];
             //Logger.println ("ImageContext.makeTransparency: create SRC data");
         }
         m_srcImg.getRGB (m_srcD, 0, m_srcW, 0, 0, m_srcW, m_srcH);
         m_makeBgTransparent = true;
         applyTransparency (m_srcD, m_srcSize, m_bgColor = bg, m_fgColor = fg);
         // force redisplay
         m_trs++;
     }
 
     void setMaxSize (Context c, int w, int h, boolean keepAspectRatio)  {
         // Only allow resize if destination size not 0x0 and not equal to origin size
         if (m_srcImg != null && (w>=0 && h>0 || w>0 && h>=0) && (w != m_srcW || h != m_srcH)) {
             if (keepAspectRatio || w==0 || h==0) {
                 float wr = w > 0 ? w / (float)m_srcW : Float.MAX_VALUE;
                 float hr = h > 0 ? h / (float)m_srcH : Float.MAX_VALUE;
                 float ratio = (wr < hr) ? wr : hr;
                 w = (int)(m_srcW * ratio);
                 h = (int)(m_srcH * ratio);
                 if (w<=0 || h<=0) return; // values must be strictly positive
             }
 //#ifdef platform.android
             setImage(Image.scaleImage(m_srcImg, w, h, m_filterMode));
 //#else
             checkSrc (); // build m_srcD
 //#ifdef debug.console
             if( bLogImage )
                 Logger.print("MAX: ");
 //#endif
             scaleImage(w, h, 0); // build m_destD to new size
             setImage (Image.createRGBImage (m_dstD, m_dstW, m_dstH, true));
 //#endif
         }
     }
     
     void scaleImage (int w, int h, int trs) { 
 //#ifdef debug.console
         if( bLogImage )
         	Logger.println("IC.scaleImage "+ m_srcW+"x"+m_srcH + " -> " + w+"x"+h +" "+ m_name );
 //#endif
         if (w <= 0 || h <= 0) {
             return;
         }
         m_dstW = w;
         m_dstH = h;
         m_trs = trs;
        // +1 : Alcatel OT-800++ bugs with drawRGB() on last pixel line
        int destS = m_dstW * (m_dstH+s_blitPlus);
         if (m_dstD == null) {
             //MCP: Initial destination size must not be smaller than source size
             //MCP: (so no realocation occurs during zoom in effects)
             m_dstD = new int [destS <= m_srcSize ? m_srcSize : destS];
             //Logger.println ("ImageContext.scaleImage: creating DST data at ORG size");
         } else if (destS > m_dstD.length) {
             //MCP: On next resize, realocate only if bigger (upscale support)
             m_dstD = new int [destS];
             //Logger.println ("ImageContext.scaleImage: Creating DST data at bigger dest size");
         }
 //#ifdef platform.android
         if (trs == 0){
             Image tmpImg = m_srcImg;
             // always use srcImg directly except for transparent bg
             if ((tmpImg == null || m_makeBgTransparent) && m_srcD != null) {
                 tmpImg = Image.createRGBImage (m_srcD, m_srcW, m_srcH, true);
             }
             if (tmpImg != null) {
                 tmpImg = Image.scaleImage(tmpImg, m_dstW, m_dstH, m_filterMode);
                 tmpImg.getRGB (m_dstD, 0, m_dstW, 0, 0, m_dstW, m_dstH);
             }
             return;
         }
 //#endif
         if (m_filterMode) {
             applyFilteredUpscale (m_srcW, m_srcH, m_srcD, m_dstW, m_dstH, m_dstD, m_trs);
         } else {
             applyScale (m_srcW, m_srcH, m_srcD, m_dstW, m_dstH, m_dstD, m_trs);
         }
         //m_transImg = null; // re-created atfer (potential) rotation
     }
     
     int[] applyRot (int src[], int w, int h, int rot) {
         if (rot == 0) {
             return src;
         } else if (rot == 180) {
             int s = w*h;
             int s2 = s/2;
             for (int i=0; i<s2; i++) {
                 int j = (s-1) - i;
                 int t = src[i];
                 src[i] = src[j];
                 src[j] = t;
             }
             return src;
         }
         int accs=0;
         int[] dst = new int[w*h];
         if (rot == 90) {
             for (int ys = 0; ys < h; ys++) {
                 int accd = ys + (w-1)*h;
                 for (int xs = 0; xs < w; xs++, accd -= h) {
                     dst[accd] = src[accs++];
                 }
             }
         } else { // 270
             for (int ys = 0; ys < h; ys++) {
                 int accd = h-1-ys;
                 for (int xs = 0; xs < w; xs++, accd += h) {
                     dst[accd] = src[accs++];
                 }
             }
         }
         return dst;
     }
     
     //function for encapsulating specific blitting code (i.e. samsumg)
     static void blit (Graphics g, int [] data, int offset, int stride, int x, int y, int w, int h, boolean processAlpha) {
 //#ifdef MM.blitWithImage
         if (s_blitWithImage) {
             g.drawImage(Image.createRGBImage (data, w, h, processAlpha), x, y, 0);
             return;
         }
 //#endif
 //#ifdef MM.SamsungClipBug
         // Intersect blit rect with the current clip
         int clip = g.getClipY();
         if (y < clip) {
             offset += (clip - y) * stride;
             h -= clip - y;
             y = clip;
         }
         clip += g.getClipHeight();
         if (y+h > clip) {
             h = clip - y;
         }
         clip = g.getClipX();
         if (x < clip) {
             offset += clip - x;
             w -= clip - x;
             x = clip;
         }
         clip += g.getClipWidth();
         if (x+w > clip) {
             w = clip - x;
         }
         // Check that intersection between blit rect and clip is still a valid rect
         if (h<=0 || w<=0 || offset < 0 || offset + (w-1) + (h-1) * stride >= data.length) return;
 //#endif
         try {
             g.drawRGB (data, offset, stride, x, y, w, h, processAlpha);
         } catch (Exception e) {
             Logger.println ("Exception on IC.blit: "+e);
            Logger.println ("h:"+h+" w:"+w+" o:"+offset+" s:"+stride+" l:"+data.length);
         }
     }
 
     void checkSrc () {
         if (m_srcD == null) {
             m_srcD = new int [m_srcSize];
             m_srcImg.getRGB (m_srcD, 0, m_srcW, 0, 0, m_srcW, m_srcH);
             //Logger.println ("Creating SRC data");
             if (m_makeBgTransparent) {
                 applyTransparency (m_srcD, m_srcSize, m_bgColor, m_fgColor);
                 //Logger.println ("Applying transparency to SRC data");
             }
             m_srcImg = null; // free m_srcImg, always use m_srcD from now on
         }
     }
     // RCA 13/10/07 add rotation support, warning only rotations of 0, 90, 180 and 270 degrees are taken in account 
     void drawImage (Graphics g, int x, int y, int w, int h, int trs, int rot) {
         if (m_srcImg == null && m_srcD == null) { 
             cleanDest ();//FTE
             return;
         }
         try {
 //#ifdef platform.android
             // Use native scaling (except for transparent bg)
             if (!m_makeBgTransparent) {
                 if (trs != 0) {
                     g.setAlpha (255-((trs*255)>>16));
                 }
                 g.drawScaledRotatedImage (m_srcImg, x, y, w, h, rot, m_filterMode);
                 g.setAlpha (255);
                 return;
             }
 //#endif
             if (w == m_srcW && h == m_srcH && trs == 0 && rot == 0) {
                 if (m_srcImg != null) {
                     g.drawImage (m_srcImg, x, y, Graphics.TOP|Graphics.LEFT);
                 } else {
                     blit (g, m_srcD, 0, w, x, y, w, h, true);
                 }
             } else { // we will need some image transformation to transform data
                 if (rot == 90 || rot == 270) { // invert width and height
                     int t = w; w = h; h = t;
                 }
                 if (w != m_dstW || h != m_dstH || m_trs != trs || m_dstD == null) {
                     checkSrc (); // build m_srcD if necessary
 //#ifdef debug.console
                     if( bLogImage )
                     	Logger.print("REC (!): ");
 //#endif
                     scaleImage (w, h, trs); // build m_dstD
                     m_rot = 0;
                 }
                 if (m_rot != rot) {
                     if (m_rot == 90 || m_rot == 270) {
                         m_dstD = applyRot (m_dstD, h, w, (360+rot-m_rot)%360);
                     } else {
                         m_dstD = applyRot (m_dstD, w, h, (360+rot-m_rot)%360);
                     }
                     m_rot = rot;
                 }
                 if (rot == 90 || rot == 270) {
                     blit (g, m_dstD, 0, h, x, y, h, w, true);
                 } else {
                     blit (g, m_dstD, 0, w, x, y, w, h, true);
                 }
             }
         } catch(Exception e) {
             Logger.println ("IC.drawImage: Exception: "+e+" for "+m_name);
         }
     }
 }
