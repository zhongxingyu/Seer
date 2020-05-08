 package cat.atridas.antagonista.graphics.gl2;
 
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL15.*;
 import static org.lwjgl.opengl.GL20.*;
 
 import java.nio.BufferOverflowException;
 import java.nio.FloatBuffer;
 
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Quat4f;
 import javax.vecmath.Vector3f;
 
 import org.lwjgl.BufferUtils;
 
 import cat.atridas.antagonista.Quality;
 import cat.atridas.antagonista.Utils;
 import cat.atridas.antagonista.graphics.DebugRender;
 import cat.atridas.antagonista.graphics.RenderManager;
 import cat.atridas.antagonista.graphics.SceneData;
 import cat.atridas.antagonista.graphics.Technique;
 import cat.atridas.antagonista.graphics.TechniquePass;
 import cat.atridas.antagonista.graphics.Effect.TechniqueType;
 
 /**
  * OpenGL 2.1 implementation of the DebugRender class.
  * 
  * @author Isaac 'Atridas' Serrano Guasch.
  * @since 0.1
  *
  */
 public class DebugRenderGL2 extends DebugRender {
   
   /**
    * Auxiliar buffers.
    * @since 0.1
    */
   private FloatBuffer buffer1, buffer2, bufferAux16;
   
   /**
    * OpenGL buffer identifier to render lines & triangles.
    * @since 0.1
    */
   private int linesBuffer = -1;
   
   /**
    * OpenGL buffer identifier to render spheres.
    * @since 0.1
    */
   private int sphereIndexBuffer = -1, sphereVertexBuffer = -1;
 
   /**
    * OpenGL buffer identifier to render crosses.
    * @since 0.1
    */
   private int crossesVertexBuffer = -1;
 
   /**
    * OpenGL buffer identifier to render circles.
    * @since 0.1
    */
   private int circlesVertexBuffer = -1;
 
   /**
    * OpenGL buffer identifier to render axes.
    * @since 0.1
    */
   private int axesVertexBuffer = -1;
 
   /**
    * OpenGL buffer identifier to render bounding boxes.
    * @since 0.1
    */
   private int bbIndexBuffer = -1, bbVertexBuffer = -1;
   
   {
     buffer1 = BufferUtils.createFloatBuffer(POS_COL_VERTEX_SIZE * 50);
     buffer2 = BufferUtils.createFloatBuffer(POS_COL_VERTEX_SIZE * 50);
     
     bufferAux16 = BufferUtils.createFloatBuffer(16);
   }
 
   /**
    * Initializes the buffers to render crosses.
    * @since 0.1
    */
   private void initCrossesBuffers() {
     assert !cleaned;
     
     
     
     crossesVertexBuffer = glGenBuffers();
     glBindBuffer(GL_ARRAY_BUFFER, crossesVertexBuffer);
     glBufferData(GL_ARRAY_BUFFER, createCrossVertexBuffer(), GL_STATIC_DRAW);
     
     glBindBuffer(GL_ARRAY_BUFFER, 0);
     
     assert !Utils.hasGLErrors();
   }
 
   /**
    * Initializes the buffers to render spheres.
    * @since 0.1
    */
   private void initSphereBuffers() {
     assert !cleaned;
     
     
     
     sphereVertexBuffer = glGenBuffers();
     glBindBuffer(GL_ARRAY_BUFFER, sphereVertexBuffer);
     glBufferData(GL_ARRAY_BUFFER, createSphereVertexBuffer(), GL_STATIC_DRAW);
     
     sphereIndexBuffer = glGenBuffers();
     glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sphereIndexBuffer);
     glBufferData(GL_ELEMENT_ARRAY_BUFFER, createSphereIndexBuffer(), GL_STATIC_DRAW);
     
     
     glBindBuffer(GL_ARRAY_BUFFER, 0);
     glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
     
     assert !Utils.hasGLErrors();
   }
 
   /**
    * Initializes the buffers to render circles.
    * @since 0.1
    */
   private void initCirclesBuffers() {
     assert !cleaned;
     
     circlesVertexBuffer = glGenBuffers();
     glBindBuffer(GL_ARRAY_BUFFER, circlesVertexBuffer);
     glBufferData(GL_ARRAY_BUFFER,createCircleVertexBuffer(), GL_STATIC_DRAW);
 
 
     glBindBuffer(GL_ARRAY_BUFFER, 0);
     
     assert !Utils.hasGLErrors();
   }
 
   /**
    * Initializes the buffers to render axes.
    * @since 0.1
    */
   private void initAxesBuffers() {
     assert !cleaned;
     
     
     axesVertexBuffer = glGenBuffers();
     glBindBuffer(GL_ARRAY_BUFFER, axesVertexBuffer);
     glBufferData(GL_ARRAY_BUFFER,createAxesVertexBuffer(), GL_STATIC_DRAW);
     
     
     glBindBuffer(GL_ARRAY_BUFFER, 0);
     
     assert !Utils.hasGLErrors();
   }
 
 
   /**
    * Initializes the buffers to render bounding boxes.
    * @since 0.1
    */
   private void initBBsBuffers() {
     assert !cleaned;
     
     
     
     bbVertexBuffer = glGenBuffers();
     glBindBuffer(GL_ARRAY_BUFFER, bbVertexBuffer);
     glBufferData(GL_ARRAY_BUFFER,createBBVertexBuffer(), GL_STATIC_DRAW);
     
     bbIndexBuffer = glGenBuffers();
     glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bbIndexBuffer);
     glBufferData(GL_ELEMENT_ARRAY_BUFFER, createBBIndexBuffer(), GL_STATIC_DRAW);
 
 
     
     glBindBuffer(GL_ARRAY_BUFFER, 0);
     glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
     
     assert !Utils.hasGLErrors();
   }
   
 
   /**
    * Initializes the buffers to render lines & triangles and calls all other buffer initializers.
    * 
    * @param rm Render Manager reference.
    * @since 0.1
    */
   private void initBuffers(RenderManager rm) {
     assert !cleaned;
     if(linesBuffer < 0) {
       linesBuffer = glGenBuffers();
       glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
       glBufferData(GL_ARRAY_BUFFER, POS_COL_VERTEX_SIZE * 50, GL_DYNAMIC_DRAW);
 
       assert !Utils.hasGLErrors();
 
       initCrossesBuffers();
       initSphereBuffers();
       initCirclesBuffers();
       initAxesBuffers();
       initBBsBuffers();
     }
   }
 
   /**
    * Renders primitives specified uniquely by a vertex array.
    * 
    * @param mode primitive type.
    * @param numElements number of vertices to render.
    * @param model model to world matrix transformation.
    * @param rm Render Manager reference.
    * @since 0.1
    */
   private void renderArrays(int mode, int numElements, Matrix4f model, RenderManager rm) {
     debugMaterial.setUpUniforms(rm);
 
     Technique technique = debugMaterial.getEffect().getTechnique(TechniqueType.FORWARD, Quality.MID);
     for(TechniquePass pass: technique.getPasses()) {
       pass.activate(rm);
       setGlobalMatrixes(pass, model, rm);
       debugMaterial.setUpUniforms(pass, rm);
 
       glDrawArrays(mode, 0, numElements);
     }
     assert !Utils.hasGLErrors();
   }
   
   /**
    * Renders primitives specified by a vertex array and an index array.
    * 
    * @param mode primitive type.
    * @param numIndices number of vertices to render.
    * @param model model to world matrix transformation.
    * @param rm Render Manager reference.
    * @since 0.1
    */
   private void renderElements(int mode, int numIndices, Matrix4f model, RenderManager rm) {
     debugMaterial.setUpUniforms(rm);
 
     Technique technique = debugMaterial.getEffect().getTechnique(TechniqueType.FORWARD, Quality.MID);
     for(TechniquePass pass: technique.getPasses()) {
       pass.activate(rm);
       setGlobalMatrixes(pass, model, rm);
       debugMaterial.setUpUniforms(pass, rm);
 
       glDrawElements(mode, numIndices, GL_UNSIGNED_SHORT, 0);
     }
     assert !Utils.hasGLErrors();
   }
   
   /**
    * Increases the size of the auxiliar buffers.
    */
   private void growBuffers() {
     int newCapacity = buffer1.capacity() + POS_COL_VERTEX_SIZE * 50;
     buffer1 = BufferUtils.createFloatBuffer( newCapacity );
     buffer2 = BufferUtils.createFloatBuffer( newCapacity );
 
     glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
     glBufferData(GL_ARRAY_BUFFER, newCapacity * Utils.FLOAT_SIZE, GL_DYNAMIC_DRAW);
   }
   /*
   @Override
   protected void renderLines(RenderManager rm) {
     assert !cleaned;
     
 
     if(lines.size() == 0)
       return;
     
     try {      
       float[] vertex = new float[POS_COL_VERTEX_SIZE * 2];
 
       int linesToDraw1 = 0;
       int linesToDraw2 = 0;
       buffer1.clear();
       buffer2.clear();
 
       
       for(Line line: lines) {
         
         vertex[0] = line.origin.x;
         vertex[1] = line.origin.y;
         vertex[2] = line.origin.z;
 
         vertex[3] = line.color.x;
         vertex[4] = line.color.y;
         vertex[5] = line.color.z;
         
         vertex[6] = line.destination.x;
         vertex[7] = line.destination.y;
         vertex[8] = line.destination.z;
 
         vertex[9]  = line.color.x;
         vertex[10] = line.color.y;
         vertex[11] = line.color.z;
         
         if(line.depthEnabled) {
           buffer1.put(vertex);
           linesToDraw1++;
         } else {
           buffer2.put(vertex);
           linesToDraw2++;
         }
       }
       
       Matrix4f identityMatrix = null;
       if(linesToDraw1 + linesToDraw2 > 0) {
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         assert !Utils.hasGLErrors();
 
         glEnableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
         glEnableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE);
 
         glVertexAttribPointer(TechniquePass.POSITION_ATTRIBUTE, 3, GL_FLOAT, false, 6*Utils.FLOAT_SIZE, 0);
         glVertexAttribPointer(TechniquePass.COLOR_ATTRIBUTE, 3, GL_FLOAT, false, 6*Utils.FLOAT_SIZE, 3*Utils.FLOAT_SIZE);
 
         assert !Utils.hasGLErrors();
         
         identityMatrix = new Matrix4f();
         identityMatrix.setIdentity();
       }
       
       if(linesToDraw1>0) {
         buffer1.flip();
       
         glEnable(GL_DEPTH_TEST);
 
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         glBufferSubData(GL_ARRAY_BUFFER, 0, buffer1);
 
         renderArrays(GL_LINES, linesToDraw1 * 2, identityMatrix, rm);
 
         assert !Utils.hasGLErrors();
       }
       
       if(linesToDraw2>0) {
         buffer2.flip();
       
         glDisable(GL_DEPTH_TEST);
 
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         glBufferSubData(GL_ARRAY_BUFFER, 0, buffer2);
       
         renderArrays(GL_LINES, linesToDraw2 * 2, identityMatrix, rm);
 
         assert !Utils.hasGLErrors();
       }
       
     } catch(BufferOverflowException e) {
       //fem creixer el buffer i rellancem el métode.
       //brut pq és una classe per fer debug.
       growBuffers();
       renderLines(rm);
     }
     
   }
   */
   @Override
   protected void renderLinesAndTriangles(RenderManager rm) {
     assert !cleaned;
     
 
     if(lines.size() == 0)
       return;
     
     try {      
       float[] vertexL = new float[POS_COL_VERTEX_SIZE * 2];
      float[] vertexT = new float[POS_COL_VERTEX_SIZE * 6];
 
       int linesToDraw1 = 0;
       int linesToDraw2 = 0;
       buffer1.clear();
       buffer2.clear();
 
       
       for(Line line: lines) {
         
         vertexL[0] = line.origin.x;
         vertexL[1] = line.origin.y;
         vertexL[2] = line.origin.z;
 
         vertexL[3] = line.color.x;
         vertexL[4] = line.color.y;
         vertexL[5] = line.color.z;
         
         vertexL[6] = line.destination.x;
         vertexL[7] = line.destination.y;
         vertexL[8] = line.destination.z;
 
         vertexL[9]  = line.color.x;
         vertexL[10] = line.color.y;
         vertexL[11] = line.color.z;
         
         if(line.depthEnabled) {
           buffer1.put(vertexL);
           linesToDraw1++;
         } else {
           buffer2.put(vertexL);
           linesToDraw2++;
         }
       }
       
       for(Triangle triangle: triangles) {
 
         ///////////////////////////////////////
         vertexT[0] = triangle.v0.x;
         vertexT[1] = triangle.v0.y;
         vertexT[2] = triangle.v0.z;
 
         vertexT[3] = triangle.color.x;
         vertexT[4] = triangle.color.y;
         vertexT[5] = triangle.color.z;
         
         vertexT[6] = triangle.v1.x;
         vertexT[7] = triangle.v1.y;
         vertexT[8] = triangle.v1.z;
 
         vertexT[9]  = triangle.color.x;
         vertexT[10] = triangle.color.y;
         vertexT[11] = triangle.color.z;
         
         
         ///////////////////////////////////////
         vertexT[12] = triangle.v0.x;
         vertexT[13] = triangle.v0.y;
         vertexT[14] = triangle.v0.z;
 
         vertexT[15] = triangle.color.x;
         vertexT[16] = triangle.color.y;
         vertexT[17] = triangle.color.z;
         
         vertexT[18] = triangle.v2.x;
         vertexT[19] = triangle.v2.y;
         vertexT[20] = triangle.v2.z;
 
         vertexT[21] = triangle.color.x;
         vertexT[22] = triangle.color.y;
         vertexT[23] = triangle.color.z;
         
 
         ///////////////////////////////////////
         vertexT[24] = triangle.v2.x;
         vertexT[25] = triangle.v2.y;
         vertexT[26] = triangle.v2.z;
 
         vertexT[27] = triangle.color.x;
         vertexT[28] = triangle.color.y;
         vertexT[29] = triangle.color.z;
         
         vertexT[30] = triangle.v1.x;
         vertexT[31] = triangle.v1.y;
         vertexT[32] = triangle.v1.z;
 
         vertexT[33] = triangle.color.x;
         vertexT[34] = triangle.color.y;
         vertexT[35] = triangle.color.z;
 
         ///////////////////////////////////////
         if(triangle.depthEnabled) {
           buffer1.put(vertexT);
           linesToDraw1 += 3;
         } else {
           buffer2.put(vertexT);
           linesToDraw2 += 3;
         }
       }
       
       Matrix4f identityMatrix = null;
       if(linesToDraw1 + linesToDraw2 > 0) {
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         assert !Utils.hasGLErrors();
 
         glEnableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
         glEnableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE);
 
         glVertexAttribPointer(TechniquePass.POSITION_ATTRIBUTE, 3, GL_FLOAT, false, 6*Utils.FLOAT_SIZE, 0);
         glVertexAttribPointer(TechniquePass.COLOR_ATTRIBUTE, 3, GL_FLOAT, false, 6*Utils.FLOAT_SIZE, 3*Utils.FLOAT_SIZE);
 
         assert !Utils.hasGLErrors();
         
         identityMatrix = new Matrix4f();
         identityMatrix.setIdentity();
       }
       
       if(linesToDraw1>0) {
         buffer1.flip();
       
         glEnable(GL_DEPTH_TEST);
 
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         glBufferSubData(GL_ARRAY_BUFFER, 0, buffer1);
 
         renderArrays(GL_LINES, linesToDraw1 * 2, identityMatrix, rm);
 
         assert !Utils.hasGLErrors();
       }
       
       if(linesToDraw2>0) {
         buffer2.flip();
       
         glDisable(GL_DEPTH_TEST);
 
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         glBufferSubData(GL_ARRAY_BUFFER, 0, buffer2);
       
         renderArrays(GL_LINES, linesToDraw2 * 2, identityMatrix, rm);
 
         assert !Utils.hasGLErrors();
       }
       
     } catch(BufferOverflowException e) {
       //fem creixer el buffer i rellancem el métode.
       //brut pq és una classe per fer debug.
       growBuffers();
       renderLinesAndTriangles(rm);
     }
     
   }
   
   @Override
   protected void renderCrosses(RenderManager rm) {
     assert !cleaned;
 
     
     if(crosses.size() == 0)
       return;
     
     try {
 
       //////////////////////////////////////////////
       Vector3f v3Aux = new Vector3f();
 
       Matrix4f model              = new Matrix4f();
       ///////////////////////////////////////////////
 
       glEnableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
       glDisableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE);
 
       glBindBuffer(GL_ARRAY_BUFFER, crossesVertexBuffer);
       glVertexAttribPointer(TechniquePass.POSITION_ATTRIBUTE, 3, GL_FLOAT, false, 0, 0);
       
       for(Cross cross: crosses) {
 
         model.setIdentity();
         v3Aux.set(cross.center);
         model.setTranslation(v3Aux);
         model.setScale(cross.size);
 
 
 
         
         glVertexAttrib3f(TechniquePass.COLOR_ATTRIBUTE, 
             cross.color.x, 
             cross.color.y, 
             cross.color.z);
         
         if(cross.depthEnabled) {
           glEnable(GL_DEPTH_TEST);
         } else {
           glDisable(GL_DEPTH_TEST);
         }
         
         renderArrays(GL_LINES, crossesNumVertexs, model, rm);
       }
 
         
     } catch(BufferOverflowException e) {
       //fem creixer el buffer i rellancem el métode.
       //brut pq és una classe per fer debug.
       growBuffers();
       renderCrosses(rm);
     }
     
   }
   
   @Override
   protected void renderSpheres(RenderManager rm) {
     assert !cleaned;
 
     
     if(spheres.size() == 0)
       return;
     
     try {
 
       //////////////////////////////////////////////
       Vector3f v3Aux = new Vector3f();
 
       Matrix4f model              = new Matrix4f();
       ///////////////////////////////////////////////
 
       
       glEnableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
       glDisableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE);
 
       glBindBuffer(GL_ARRAY_BUFFER, sphereVertexBuffer);
       glVertexAttribPointer(TechniquePass.POSITION_ATTRIBUTE, 3, GL_FLOAT, false, 0, 0);
 
       glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sphereIndexBuffer);
       
       for(Sphere sphere: spheres) {
 
         model.setIdentity();
         v3Aux.set(sphere.center);
         model.setTranslation(v3Aux);
         model.setScale(sphere.radius);
 
         
         if(sphere.depthEnabled) {
           glEnable(GL_DEPTH_TEST);
         } else {
           glDisable(GL_DEPTH_TEST);
         }
         
         glVertexAttrib3f(TechniquePass.COLOR_ATTRIBUTE, 
             sphere.color.x, 
             sphere.color.y, 
             sphere.color.z);
         
         
         renderElements(GL_LINE_STRIP, sphereNumIndices, model, rm);
       }
         
     } catch(BufferOverflowException e) {
       //fem creixer el buffer i rellancem el métode.
       //brut pq és una classe per fer debug.
       growBuffers();
       renderSpheres(rm);
     }
     
   }
   
   @Override
   protected void renderCircles(RenderManager rm) {
     assert !cleaned;
 
     
     if(circles.size() == 0)
       return;
     
     try {
       
 
       Vector3f v3Aux = new Vector3f();
       Quat4f   qAux  = new Quat4f();
 
       //////////////////////////////////////////////
       Matrix4f model              = new Matrix4f();
       ///////////////////////////////////////////////
 
       
       glEnableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
       glDisableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE);
 
       glBindBuffer(GL_ARRAY_BUFFER, circlesVertexBuffer);
       glVertexAttribPointer(TechniquePass.POSITION_ATTRIBUTE, 3, GL_FLOAT, false, 0, 0);
       
       for(Circle circle: circles) {
 
         model.setIdentity();
         v3Aux.set(circle.center);
         model.setTranslation(v3Aux);
         model.setScale(circle.radius);
 
         v3Aux.set(0,0,1);
         Utils.getClosestRotation(v3Aux, circle.planeNormal, qAux);
         model.setRotation(qAux);
 
         
         if(circle.depthEnabled) {
           glEnable(GL_DEPTH_TEST);
         } else {
           glDisable(GL_DEPTH_TEST);
         }
         
         glVertexAttrib3f(TechniquePass.COLOR_ATTRIBUTE, 
             circle.color.x, 
             circle.color.y, 
             circle.color.z);
         
 
         renderArrays(GL_LINE_LOOP, circlesNumVertexs, model, rm);
       }
 
         
     } catch(BufferOverflowException e) {
       //fem creixer el buffer i rellancem el métode.
       //brut pq és una classe per fer debug.
       growBuffers();
       renderCircles(rm);
     }
   }
   
   @Override
   protected void renderAxes(RenderManager rm) {
     assert !cleaned;
 
     
     if(axes.size() == 0)
       return;
     
     try {
 
       //////////////////////////////////////////////
       Matrix4f model              = new Matrix4f();
       ///////////////////////////////////////////////
       
 
       glBindBuffer(GL_ARRAY_BUFFER, axesVertexBuffer);
       
       glEnableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
       glEnableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE   );
 
       glVertexAttribPointer(TechniquePass.POSITION_ATTRIBUTE, 3, GL_FLOAT, false, 6 * Utils.FLOAT_SIZE, 0                   );
       glVertexAttribPointer(TechniquePass.COLOR_ATTRIBUTE   , 3, GL_FLOAT, false, 6 * Utils.FLOAT_SIZE, 3 * Utils.FLOAT_SIZE);
 
       
       for(Axes axe: axes) {
 
         model.set(axe.transformation);
         model.setScale(axe.size);
 
         
         if(axe.depthEnabled) {
           glEnable(GL_DEPTH_TEST);
         } else {
           glDisable(GL_DEPTH_TEST);
         }
 
         renderArrays(GL_LINES, axesNumVertexs, model, rm);
       }
         
     } catch(BufferOverflowException e) {
       //fem creixer el buffer i rellancem el métode.
       //brut pq és una classe per fer debug.
       growBuffers();
       renderCircles(rm);
     }
     
   }
   /*
   @Override
   protected void renderTriangles(RenderManager rm) {
     assert !cleaned;
     
     
     if(triangles.size() == 0)
       return;
     
     try {      
       float[] vertex = new float[POS_COL_VERTEX_SIZE * 6];
 
       int trianglesToDraw1 = 0;
       int trianglesToDraw2 = 0;
       buffer1.clear();
       buffer2.clear();
 
       
       for(Triangle triangle: triangles) {
 
         ///////////////////////////////////////
         vertex[0] = triangle.v0.x;
         vertex[1] = triangle.v0.y;
         vertex[2] = triangle.v0.z;
 
         vertex[3] = triangle.color.x;
         vertex[4] = triangle.color.y;
         vertex[5] = triangle.color.z;
         
         vertex[6] = triangle.v1.x;
         vertex[7] = triangle.v1.y;
         vertex[8] = triangle.v1.z;
 
         vertex[9]  = triangle.color.x;
         vertex[10] = triangle.color.y;
         vertex[11] = triangle.color.z;
         
         
         ///////////////////////////////////////
         vertex[12] = triangle.v0.x;
         vertex[13] = triangle.v0.y;
         vertex[14] = triangle.v0.z;
 
         vertex[15] = triangle.color.x;
         vertex[16] = triangle.color.y;
         vertex[17] = triangle.color.z;
         
         vertex[18] = triangle.v2.x;
         vertex[19] = triangle.v2.y;
         vertex[20] = triangle.v2.z;
 
         vertex[21] = triangle.color.x;
         vertex[22] = triangle.color.y;
         vertex[23] = triangle.color.z;
         
 
         ///////////////////////////////////////
         vertex[24] = triangle.v2.x;
         vertex[25] = triangle.v2.y;
         vertex[26] = triangle.v2.z;
 
         vertex[27] = triangle.color.x;
         vertex[28] = triangle.color.y;
         vertex[29] = triangle.color.z;
         
         vertex[30] = triangle.v1.x;
         vertex[31] = triangle.v1.y;
         vertex[32] = triangle.v1.z;
 
         vertex[33] = triangle.color.x;
         vertex[34] = triangle.color.y;
         vertex[35] = triangle.color.z;
 
         ///////////////////////////////////////
         if(triangle.depthEnabled) {
           buffer1.put(vertex);
           trianglesToDraw1++;
         } else {
           buffer2.put(vertex);
           trianglesToDraw2++;
         }
       }
 
       Matrix4f identityMatrix = null;
       if(trianglesToDraw1 + trianglesToDraw2 > 0) {
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         assert !Utils.hasGLErrors();
 
         glEnableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
         glEnableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE);
 
         glVertexAttribPointer(TechniquePass.POSITION_ATTRIBUTE, 3, GL_FLOAT, false, 6*Utils.FLOAT_SIZE, 0);
         glVertexAttribPointer(TechniquePass.COLOR_ATTRIBUTE, 3, GL_FLOAT, false, 6*Utils.FLOAT_SIZE, 3*Utils.FLOAT_SIZE);
 
         assert !Utils.hasGLErrors();
         
         identityMatrix = new Matrix4f();
         identityMatrix.setIdentity();
       }
       
       if(trianglesToDraw1>0) {
         buffer1.flip();
       
         glEnable(GL_DEPTH_TEST);
 
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         glBufferSubData(GL_ARRAY_BUFFER, 0, buffer1);
       
         renderArrays(GL_LINES, trianglesToDraw1 * 6, identityMatrix, rm);
 
         assert !Utils.hasGLErrors();
       }
       
       if(trianglesToDraw2>0) {
         buffer2.flip();
       
         glDisable(GL_DEPTH_TEST);
 
         glBindBuffer(GL_ARRAY_BUFFER, linesBuffer);
         glBufferSubData(GL_ARRAY_BUFFER, 0, buffer2);
       
         renderArrays(GL_LINES, trianglesToDraw2 * 6, identityMatrix, rm);
 
         assert !Utils.hasGLErrors();
       }
       
     } catch(BufferOverflowException e) {
       //fem creixer el buffer i rellancem el métode.
       //brut pq és una classe per fer debug.
       growBuffers();
       renderTriangles(rm);
     }
     
   }
   */
   @Override
   protected void renderBBs(RenderManager rm) {
     assert !cleaned;
 
     
     if(aabbs.size() == 0)
       return;
     
     try {
 
       //////////////////////////////////////////////
       Vector3f v3Aux = new Vector3f();
 
       Matrix4f model              = new Matrix4f();
       ///////////////////////////////////////////////
         
       glEnableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
       glDisableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE);
 
       glBindBuffer(GL_ARRAY_BUFFER, bbVertexBuffer);
       glVertexAttribPointer(TechniquePass.POSITION_ATTRIBUTE, 3, GL_FLOAT, false, 0, 0);
 
       glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bbIndexBuffer);
         
       for(AABB aabb: aabbs) {
 
         model.setIdentity();
         v3Aux.interpolate(aabb.minCoords, aabb.maxCoords, .5f);
         model.setTranslation(v3Aux);
         v3Aux.sub(aabb.maxCoords, aabb.minCoords);
         model.setM00(v3Aux.x);
         model.setM11(v3Aux.y);
         model.setM22(v3Aux.z);
 
         
         glVertexAttrib3f(TechniquePass.COLOR_ATTRIBUTE, 
             aabb.color.x, 
             aabb.color.y, 
             aabb.color.z);
         
         renderElements(GL_LINES, bbNumIndices, model, rm);
       }
       
       for(OBB obb: obbs) {
 
         model.set(obb.centerTransformation);
         model.setM00(obb.scaleXYZ.x * model.getM00());
         model.setM01(obb.scaleXYZ.y * model.getM01());
         model.setM02(obb.scaleXYZ.z * model.getM02());
         
         model.setM10(obb.scaleXYZ.x * model.getM10());
         model.setM11(obb.scaleXYZ.y * model.getM11());
         model.setM12(obb.scaleXYZ.z * model.getM12());
         
         model.setM20(obb.scaleXYZ.x * model.getM20());
         model.setM21(obb.scaleXYZ.y * model.getM21());
         model.setM22(obb.scaleXYZ.z * model.getM22());
 
         
         glVertexAttrib3f(TechniquePass.COLOR_ATTRIBUTE, 
             obb.color.x, 
             obb.color.y, 
             obb.color.z);
         
         renderElements(GL_LINES, bbNumIndices, model, rm);
       }
     } catch(BufferOverflowException e) {
       //fem creixer el buffer i rellancem el métode.
       //brut pq és una classe per fer debug.
       growBuffers();
       renderSpheres(rm);
     }
     
   }
   
   /**
    * Sends matrix information to the OpenGL driver.
    * 
    * @param pass program reference.
    * @param model model to world matrix.
    * @param rm RenderManager reference.
    * @since 0.1
    */
   private void setGlobalMatrixes(TechniquePass pass, Matrix4f model, RenderManager rm) {
     SceneData sd = rm.getSceneData();
     
     Matrix4f mat = new Matrix4f();
     mat.setIdentity();
     
 
     bufferAux16.clear();
     sd.getViewProjectionMatrix(mat);
     mat.mul(model);
     Utils.matrixToBuffer(mat, bufferAux16);
     bufferAux16.flip();
     glUniformMatrix4(pass.getModelViewProjectionUniform(), false, bufferAux16);
  // a partir d'aqui no se si realment cal
     bufferAux16.clear();
     mat.setIdentity();
     sd.getViewMatrix(mat);
     mat.mul(model);
     Utils.matrixToBuffer(mat, bufferAux16);
     bufferAux16.flip();
     glUniformMatrix4(pass.getModelViewUniform(), false, bufferAux16);
     bufferAux16.clear();
     mat.invert();
     mat.transpose();
     Utils.matrixToBuffer(mat, bufferAux16);
     bufferAux16.flip();
     glUniformMatrix4(pass.getModelViewITUniform(), false, bufferAux16);
  // fins aqui
   }
 
   /**
    * Depth test state prior to starting the rendering phase of this class.
    * @since 0.1
    */
   private boolean prevDepthMask;
   
   /**
    * Depth function  prior to starting the rendering phase of this class.
    * @since 0.2
    */
   private int prevDepthFunction;
   
   @Override
   protected void beginRender(RenderManager rm) {
     assert !cleaned;
     initBuffers(rm);
     prevDepthMask = glGetBoolean(GL_DEPTH_WRITEMASK);
     prevDepthFunction = glGetInteger(GL_DEPTH_FUNC);
     glDepthMask(false);
     glDepthFunc(GL_LEQUAL);
     glDepthRange(0, 0.9999);
     
 
     assert !Utils.hasGLErrors();
   }
 
   @Override
   protected void endRender() {
     assert !cleaned;
     glDepthMask(prevDepthMask);
     glDepthFunc(prevDepthFunction);
     glDepthRange(0, 1);
 
 
     glBindBuffer(GL_ARRAY_BUFFER, 0);
     glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
 
     glDisableVertexAttribArray(TechniquePass.POSITION_ATTRIBUTE);
     glDisableVertexAttribArray(TechniquePass.COLOR_ATTRIBUTE);
     
     assert !Utils.hasGLErrors();
   }
 
   /**
    * Contains information concerning if this object had been cleared.
    * @since 0.1
    */
   protected boolean cleaned = false;
   
   @Override
   public void finalize() {
     if(!cleaned) {
       if(linesBuffer > 0) {
         glDeleteBuffers(linesBuffer);
         
         glDeleteBuffers(sphereIndexBuffer);
         glDeleteBuffers(sphereVertexBuffer);
         
         glDeleteBuffers(crossesVertexBuffer);
         
         glDeleteBuffers(circlesVertexBuffer);
 
         glDeleteBuffers(bbIndexBuffer);
         glDeleteBuffers(bbVertexBuffer);
       }
       
       cleaned = true;
     }
   }
 }
