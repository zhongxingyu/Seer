 import javax.media.opengl.GL;
 import java.nio.*;
 import java.util.*;
 
 public class Cache {
     private Func f;
 
     private int xBegin = -20;
     private int xEnd   = +20;
     //======================|
     private int zBegin = -20;
     private int zEnd   = +20;
 
     private int xSize;
     private int zSize;
 
     private IntBuffer    indexBuffer;
     private FloatBuffer  colorBuffer;
     private FloatBuffer normalBuffer;
     private FloatBuffer vertexBuffer;
 
     private float[][]   values;
     private float[][][] normals;
     private float[][][] colors;
     private float[][][] vertices;
 
     public Cache(Func f) {
         this.f = f;
 
         xSize = rangeLength(xBegin, xEnd);
         zSize = rangeLength(zBegin, zEnd);
 
         Util.ensure(xEnd > xBegin);
         Util.ensure(zEnd > zBegin);
 
         int xs1 = xSize - 1;
         int zs1 = zSize - 1;
 
        indexBuffer  =   IntBuffer.allocate(xs1   * zs1   * 4);
        colorBuffer  = FloatBuffer.allocate(xSize * zSize * 4);
        normalBuffer = FloatBuffer.allocate(xSize * zSize * 3);
        vertexBuffer = FloatBuffer.allocate(xSize * zSize * 3);
 
         values   = new float[xSize][zSize];
         //==================================|
         normals  = new float[xSize][zSize][3];
         colors   = new float[xSize][zSize][4];
         vertices = new float[xSize][zSize][4];
 
         fillInValues();
         calcVertices();
         calcBuffers();
 
         //Debug.printAry(values);
         //Debug.printSep();
         //Debug.printAry(normals);
         //Debug.printSep();
         //Debug.printAry(colors);
         //Debug.printSep();
         //Debug.printAry(vertices);
     }
 
     private void calcVertices() {
         for (int x=0; x < xSize; ++x) {
             for (int z=0; z < zSize; ++z) {
                 float thisX = mapOutOfZeroBasedRange(x, xBegin);
                 float thisZ = mapOutOfZeroBasedRange(z, zBegin);
 
                 vertices[x][z] = new float[] {
                     thisX,
                     f.valueAt(thisX, thisZ),
                     thisZ
                 };
             }
         }
     }
 
     public void vertexArraySetup(GL gl) {
         Debug.println("Begin vertexArraySetup");
         gl.glEnableClientState(gl.GL_COLOR_ARRAY);
         gl.glEnableClientState(gl.GL_NORMAL_ARRAY);
         gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
 
         gl.glVertexPointer(3, gl.GL_FLOAT, 0, vertexBuffer);
         gl.glNormalPointer(   gl.GL_FLOAT, 0, normalBuffer);
         gl.glColorPointer (4, gl.GL_FLOAT, 0,  colorBuffer);
 
         //gl.glColor4f(1, 1, 1, 1);
         Debug.println("End vertexArraySetup");
     }
 
     public void vertexArrayUnsetup(GL gl) {
         Debug.println("Begin vertexArrayUnsetup");
         gl.glDisableClientState(gl.GL_COLOR_ARRAY);
         gl.glDisableClientState(gl.GL_NORMAL_ARRAY);
         gl.glDisableClientState(gl.GL_VERTEX_ARRAY);
         Debug.println("End vertexArrayUnsetup");
     }
 
     public void calcBuffers() {
         for (int x=1; x < xSize; ++x) {
             for (int z=1; z < zSize; ++z) {
                 indexBuffer.put((x-0)*xSize + (z-0));
                 indexBuffer.put((x-1)*xSize + (z-0));
                 indexBuffer.put((x-0)*xSize + (z-1));
                 indexBuffer.put((x-1)*xSize + (z-1));
             }
         }
 
         for (int x=0; x < xSize; ++x) {
             for (int z=0; z < zSize; ++z) {
                 //int index = x*xSize + z;
                 colorBuffer .put(colors  [x][z]);
                 normalBuffer.put(normals [x][z]);
                 vertexBuffer.put(vertices[x][z]);
             }
         }
 
         indexBuffer .rewind();
         colorBuffer .rewind();
         normalBuffer.rewind();
         vertexBuffer.rewind();
     }
 
     public void draw(GL gl) {
         vertexArraySetup(gl);
         Debug.println("Doing glDrawElements");
         gl.glDrawElements(
             gl.GL_QUAD_STRIP,
             indexBuffer.capacity(),
             gl.GL_UNSIGNED_INT,
             indexBuffer
         );
         Debug.println("Done doing glDrawElements");
         vertexArrayUnsetup(gl);
     }
 
     public void drawImmediate(GL gl) {
         for (int x=1; x < xSize; ++x) {
             gl.glBegin(gl.GL_QUAD_STRIP);
             for (int z=1; z < zSize; ++z) {
                 drawPoint(gl, x-0, z-0);
                 drawPoint(gl, x-1, z-0);
                 drawPoint(gl, x-0, z-1);
                 drawPoint(gl, x-1, z-1);
             }
             gl.glEnd();
         }
     }
 
     public void drawPoint(GL gl, int x, int z) {
         float thisX = mapOutOfZeroBasedRange(x, xBegin);
         float thisZ = mapOutOfZeroBasedRange(z, zBegin);
         float thisY = values[x][z];
 
         float[] thisNormal = normals[x][z];
         float[] thisColor  =  colors[x][z];
 
         gl.glNormal3fv(thisNormal, 0);
         gl.glColor3fv(thisColor, 0);
         gl.glVertex3f(thisX, thisY, thisZ);
     }
 
     private void fillInValues() {
         for (int x=xBegin; x <= xEnd; ++x) {
             for (int z=zBegin; z <= zEnd; ++z) {
                 int xIndex = mapIntoZeroBasedRange(x, xBegin);
                 int zIndex = mapIntoZeroBasedRange(z, zBegin);
 
                 float   thisValue  = f. valueAt(x, z);
                 float[] thisNormal = f.normalAt(x, z);
                 float[] thisColor  = colorAt(thisValue);
 
                 values [xIndex][zIndex] = thisValue;
                 normals[xIndex][zIndex] = thisNormal;
                 colors [xIndex][zIndex] = thisColor;
             }
         }
     }
 
     public float[] colorAt(float y) {
         return new float[] {1, 1, 1, 1};
     }
 
     int mapIntoZeroBasedRange(int n, int begin) {
         return n - begin;
     }
 
     int mapOutOfZeroBasedRange(int n, int begin) {
         return n + begin;
     }
 
     int rangeLength(int begin, int end) {
         return Math.abs(begin - end) + 1;
     }
 }
