 //package wiremap;
 
 import processing.core.*;
 
 /** 
  * Wiremap
  *
  * All measurements are in inches
  */
 public class Wiremap {
     private PApplet mParent;
     private int mWireCount;
     private int mDepthThickness;
     private int mDepth;
     private int mMaplineLength;
     private int mHeight;
     private double mDepthUnit;
     private double mMaplineUnit;
     private int mPixelsPerInch; //TODO can these be more easily calculated?
     private int mPixelsPerWire;
 
     private int[] mWireDepths;
     private int[] mWireX;
     private int[] mWireZ;
 
     //TODO
     int dot_height = 15; // height of surface pixels.
     int colorval_r = 0;                  // red
     int colorval_g = 0;                  // green
     int colorval_b = 255;                // blue
 
     private void loadDepths(String wireDepthsFile) {
         String lines[] = loadStrings(wireDepthsFile);
         for(int i = 0; i < mWireCount; i++) {
             mWireDepths[i] = Integer.valueOf(lines[i]);
             float base = (float)(-mMaplineLength / 2 + i * mMaplineUnit);
             float hyp = sqrt(sq(base) + sq(mDepth));
             mWireZ[i] = (int)(mDepth - (mWireDepths[i] * mDepthUnit));
             mWireX[i] = (int)(base - (base * mWireDepths[i] / hyp * mDepthUnit));
         }
        println(mWireZ);
        println("");
        println(mWireX);
     }
 
     public Wiremap(PApplet parent, int wireCount, int depth, int depthThickness,
             int height, int mapline, double depthUnit, double maplineUnit,
             int pixelsPerInch, int pixelsPerWire, String wireDepthsFile) {
         mParent = parent;
         mWireCount = wireCount;
         mDepthThickness = depthThickness;
         mDepth = depth;
         mHeight = height;
         mMaplineLength = mapline;
         mDepthUnit = depthUnit;
         mMaplineUnit = maplineUnit;
         mPixelsPerInch = pixelsPerInch;
         mPixelsPerWire = pixelsPerWire;
         mWireDepths = new int[mWireCount];
         mWireX = new int[mWireCount];
         mWireZ = new int[mWireCount];
         loadDepths(wireDepthsFile);
     }
 
     /**
    ** y is flipping from processing - 0 to ? some lowish number
    ** z >= 0, z <= mDepthThickness
    */
     public void sphere(float x, float y, float z, float radius) {
         x = x / mParent.width * mMaplineLength  - (mMaplineLength / 2);
        //y = mParent.height - y;  //TODO how to convert to globe fomrat?
        y = y / mParent.width * mHeight;
         z = mDepth - z;
         for(int i = 0; i < mWireCount; i++) {
             // if a wire's x coord is close enough to the globe's center
             if((mWireX[i] >= (x - radius)) && (mWireX[i] <= (x + radius))) {                  
                 // find the distance from the wire to the globe's center
                 float local_hyp = sqrt(sq(mWireX[i] - x) + sq(mWireZ[i] - z));           
                 // if the wire's xz coord is close enough to the globe's center
                 if(local_hyp <= radius) {                                                        
                     // find the height of the globe at that point
                     float y_abs = sqrt(sq(radius) - sq(local_hyp));                      
                     // find the top & bottom coords
                     float y_top_coord = y + y_abs;                                          
                     float y_bot_coord = y - y_abs;                                          
                     // compensate for projection morphing
                     float y_top_proj = y_top_coord * mDepth / mWireZ[i];                  
                     float y_bot_proj = y_bot_coord * mDepth / mWireZ[i];
                     float y_height_proj = y_top_proj - y_bot_proj;
 
                     /* Top dot
                     ---------------------------------------------------------*/
                     // Fill the globe pixels this color
                     fill(colorval_r, colorval_g, colorval_b);                                   
                     float left = i * mParent.width / mWireCount;
                     float top = (mParent.height / mPixelsPerInch 
                             - y_top_proj) * mPixelsPerInch + dot_height;    
                     float height = y_height_proj * mPixelsPerInch -
                             (dot_height * 2);
                     mParent.rect(left, top, mPixelsPerWire, height);
 
                     /* Top Surface
                     ---------------------------------------------------------*/
                     mParent.pushMatrix();
                     fill(255);
                     top = (mParent.height / mPixelsPerInch - y_top_proj)
                             * mPixelsPerInch;
                     height = dot_height;
                     mParent.rect(left, top, mPixelsPerWire, height);
                     mParent.popMatrix();
 
                     /* Bottom Surface
                     ---------------------------------------------------------*/
                     top = (mParent.height / mPixelsPerInch - y_bot_proj)
                             * mPixelsPerInch - dot_height;
                     mParent.rect(left, top, mPixelsPerWire, height);
                 }
             }
         }
     }
 
     public void rect(int x, int y, int z, int width, int height, int depth) {
 
     }
 
     public void line(int x1, int y1, int z1, int x2, int y2, int z2) {
 
     }
 
 }
