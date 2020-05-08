 package cz.urbangaming.galgs;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.opengl.GLES20;
 import android.util.Log;
 import cz.urbangaming.galgs.utils.Point2D;
 import cz.urbangaming.galgs.utils.Utils;
 
 /**
  * 
  * @author Michal Karm Babacek
  * @license GNU GPL 3.0
  * 
  */
 public class Scene {
     private final Algorithms algorithms = new Algorithms();
     private final String vertexShaderCode =
             "uniform   mat4 uMVPMatrix;" +
                     "attribute vec4 vPosition;" +
                     "void main() {" +
                     "    gl_Position = uMVPMatrix * vPosition;" +
                     "    gl_PointSize = " + GAlg.POINT_SIZE + ";" +
                     "}";
     private final String fragmentShaderCode =
             "precision mediump float;" +
                     "uniform   vec4    vColor;" +
                     "void main() {" +
                     "    gl_FragColor = vColor;" +
                     "}";
 
     private final int mProgram;
     private final int mLinesProgram;
     private int mPositionHandle;
     private int mLinesPositionHandle;
     private int mColorHandle;
     private int mLinesColorHandle;
     private FloatBuffer vertexBuffer = null;
     private FloatBuffer linesVertexBuffer = null;
 
     public static final int COORDS_PER_VERTEX = 3;
     private List<Point2D> verticesCoords = new ArrayList<Point2D>();
     private List<Point2D> linesCoords = new ArrayList<Point2D>();
     private int vertexCount = 0;
     private int linesVertexCount = 0;
     private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
     private static final float colorVertices[] = { 1f, 0f, 0f, 1.0f };
     private static final float colorLines[] = { 0f, 1f, 0f, 1.0f };
     private boolean drawLines = false;
     private PointsRenderer pointsRenderer = null;
 
     // -1 means "none selected" X Y Z index
     private int selectedVertexIndex = -1;
     private boolean vertexSelected = false;
 
     public Scene(PointsRenderer pointsRenderer) {
         this.pointsRenderer = pointsRenderer;
         newVertexBufferToDraw();
         // prepare shaders and OpenGL program
         int vertexShader = PointsRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
         int fragmentShader = PointsRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
         mProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
         mLinesProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
         GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader to program
         GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
         GLES20.glLinkProgram(mProgram); // create OpenGL program executables
         // Hmm, let'suse the same shaders for this. TODO: Really?
         // TODO: This is weird... :-(
         GLES20.glAttachShader(mLinesProgram, vertexShader); // add the vertex shader to program
         GLES20.glAttachShader(mLinesProgram, fragmentShader); // add the fragment shader to program
         GLES20.glLinkProgram(mLinesProgram); // create OpenGL program executables
     }
 
     public void clearScene() {
         verticesCoords.clear();
         linesCoords.clear();
         drawLines = false;
         newVertexBufferToDraw();
     }
 
     public void addRandomPoints() {
         verticesCoords.addAll(Utils.generateSomeVertices(GAlg.HOW_MANY_POINTS_GENERATE,
                 GAlg.BORDER_POINT_POSITION,
                 GAlg.BORDER_POINT_POSITION,
                 pointsRenderer.getSurfaceWidth() - GAlg.BORDER_POINT_POSITION,
                 pointsRenderer.getSurfaceHeight() - GAlg.BORDER_POINT_POSITION));
         newVertexBufferToDraw();
     }
 
     public void addVertex(Point2D point2d) {
         verticesCoords.add(point2d);
         newVertexBufferToDraw();
     }
 
     public void selectVertex(Point2D point2d) {
         if (!vertexSelected) {
             // Brutal force :-)
             for (int i = 0; i < verticesCoords.size(); i++) {
                 if (Utils.isInRectangle(point2d, GAlg.FINGER_ACCURACY, verticesCoords.get(i))) {
                     selectedVertexIndex = i;
                     vertexSelected = true;
                     break;
                 }
             }
         }
     }
 
     public void moveSelectedVertexTo(float x, float y) {
         if (vertexSelected) {
             // Update location x,y
             // Really? Isn't it a copy? Might be an epic one...  #fail
             verticesCoords.get(selectedVertexIndex).updateWith(x, y);
             newVertexBufferToDraw();
         }
     }
 
     public void deselectVertex() {
         vertexSelected = false;
         selectedVertexIndex = -1;
     }
 
     public void removeVertex(Point2D point2d) {
         List<Point2D> newSceneCoords = verticesCoords;
         // Brutal force...
         for (int i = 0; i < verticesCoords.size(); i++) {
             if (Utils.isInRectangle(point2d, GAlg.FINGER_ACCURACY, verticesCoords.get(i))) {
                 newSceneCoords.remove(i);
                 break;
             }
         }
         verticesCoords = newSceneCoords;
         newVertexBufferToDraw();
     }
 
     public void renderLines(int algorithmUsed) {
         linesCoords.clear();
         List<Point2D> results = null;
         switch (algorithmUsed) {
         case GAlg.CONVEX_HULL_GW:
             results = algorithms.convexHullGiftWrapping(verticesCoords);
             break;
         case GAlg.CONVEX_HULL_GS:
             results = algorithms.convexHullGrahamScan(verticesCoords);
             break;
 
         default:
             // silence is golden
             break;
         }
         // Doesn't make any sense with less than 2 vertices.
         if (results != null && results.size() >= 2) {
             linesCoords.addAll(results);
             drawLines = true;
             newVertexBufferToDraw();
         }
     }
 
     private void newVertexBufferToDraw() {
         // (number of points) * (number of coordinate values) * 4 bytes per float)
         ByteBuffer bb = ByteBuffer.allocateDirect(verticesCoords.size() * COORDS_PER_VERTEX * 4);
         // use the device hardware's native byte order
         bb.order(ByteOrder.nativeOrder());
         // create a floating point buffer from the ByteBuffer
         vertexBuffer = bb.asFloatBuffer();
         // add the coordinates to the FloatBuffer
         vertexBuffer.put(Utils.pointVectorToArray(verticesCoords));
         // set the buffer to read the first coordinate
         vertexBuffer.position(0);
         vertexCount = verticesCoords.size();
         // Log.d(GAlg.DEBUG_TAG, "Scene coords to draw: " + verticesCoords.toString());
         GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexCount, vertexBuffer);
 
         if (drawLines) {
             bb = ByteBuffer.allocateDirect(linesCoords.size() * COORDS_PER_VERTEX * 4);
             bb.order(ByteOrder.nativeOrder());
             linesVertexBuffer = bb.asFloatBuffer();
             linesVertexBuffer.put(Utils.pointVectorToArray(linesCoords));
             linesVertexBuffer.position(0);
             linesVertexCount = linesCoords.size();
             Log.d(GAlg.DEBUG_TAG, "Drawing lines between: " + linesCoords.toString());
             GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, linesVertexCount, linesVertexBuffer);
         }
     }
 
     public void draw() {
         // Add program to OpenGL environment
         GLES20.glUseProgram(mProgram);
 
         // get handle to vertex shader's vPosition member
         mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
 
         // Enable a handle to the scene vertices
         GLES20.glEnableVertexAttribArray(mPositionHandle);
 
         // Prepare the scene coordinate data
         GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
 
         // get handle to fragment shader's vColor member
         mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
         // Set colorVertices for drawing the scene
         GLES20.glUniform4fv(mColorHandle, 1, colorVertices, 0);
 
         // Get handle to shape's transformation matrix
         int mtrxhandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
 
         // Apply the projection and view transformation
         GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, pointsRenderer.mtrxProjectionAndView, 0);
 
         // Draw the scene
         GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);
 
         // Disable vertex array
         GLES20.glDisableVertexAttribArray(mPositionHandle);
 
         if (drawLines) {
             GLES20.glUseProgram(mLinesProgram);
             mLinesPositionHandle = GLES20.glGetAttribLocation(mLinesProgram, "vPosition");
             GLES20.glEnableVertexAttribArray(mLinesPositionHandle);
             GLES20.glVertexAttribPointer(mLinesPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, linesVertexBuffer);
             mLinesColorHandle = GLES20.glGetUniformLocation(mLinesProgram, "vColor");
             GLES20.glUniform4fv(mLinesColorHandle, 1, colorLines, 0);
             int mtrxLineshandle = GLES20.glGetUniformLocation(mLinesProgram, "uMVPMatrix");
             GLES20.glUniformMatrix4fv(mtrxLineshandle, 1, false, pointsRenderer.mtrxProjectionAndView, 0);
             GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, linesVertexCount);
             GLES20.glDisableVertexAttribArray(mLinesPositionHandle);
         }
     }
 
 }
