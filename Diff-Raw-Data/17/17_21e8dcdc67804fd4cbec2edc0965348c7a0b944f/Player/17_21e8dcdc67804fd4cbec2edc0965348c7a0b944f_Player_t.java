 package drawable;
 
 import java.util.Collections;
 import org.lwjgl.opengl.GL11;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector4f;
 import shader.ShaderManager;
 import shader.Shader;
 
 public class Player extends Movable {
 
     private static final float[] VERTS = {
         0.0f, 0.05f, 0.0f,
         -0.03f, -0.05f, 0.0f,
         0.03f, -0.05f, 0.0f
     };
     private static final float r = 0;
     private static final float g = .5f;
     private static final float b = .5f;
     private static final float[] COLORS = {
         r, g, b,
         r, g, b,
         r, g, b
     };
     private static final float Y_SPEED = 0.001f;
     private static final float X_SPEED = 0.0008f;
 
     private float currentYSpeed, currentXSpeed;
 
     public Player() {
         super(VERTS, COLORS);
         calculateBoundingBox();
         currentYSpeed = 0;
         currentXSpeed = 0;
     }
 
     @Override
     public void draw() {
         super.draw();

         calculateBoundingBox();
 
         ShaderManager.useShader(Shader.FLAT, modelViewBuffer);
         GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
     }
 
     private void calculateBoundingBox() {
         Vector4f vertex1 = new Vector4f(VERTS[0], VERTS[1], VERTS[2], 1);
         Vector4f vertex2 = new Vector4f(VERTS[3], VERTS[4], VERTS[5], 1);
         Vector4f vertex3 = new Vector4f(VERTS[6], VERTS[7], VERTS[8], 1);
         Vector4f position = new Vector4f(modelViewMatrix.m30, modelViewMatrix.m31, modelViewMatrix.m32, modelViewMatrix.m33);

         Matrix4f.transform(modelViewMatrix, vertex1, vertex1);
         Matrix4f.transform(modelViewMatrix, vertex2, vertex2);
         Matrix4f.transform(modelViewMatrix, vertex3, vertex3);

         float minX = Math.min(Math.min(vertex1.x, vertex2.x), vertex3.x) - position.x;
         float minY = Math.min(Math.min(vertex1.y, vertex2.y), vertex3.y) - position.y;
         float maxX = Math.max(Math.max(vertex1.x, vertex2.x), vertex3.x) - position.x;
         float maxY = Math.max(Math.max(vertex1.y, vertex2.y), vertex3.y) - position.y;
        
         boundingBoxX = Math.max(Math.abs(minX), Math.abs(maxX));
         boundingBoxY = Math.max(Math.abs(minY), Math.abs(maxY));
     }
 
     public void moveVertically(boolean forward) {
         if (forward) {
             currentYSpeed += Y_SPEED;
         } else {
             currentYSpeed -= Y_SPEED;
         }
     }
 
     public void moveSideways(boolean left) {
         if (left) {
             currentXSpeed -= X_SPEED;
         } else {
             currentXSpeed += X_SPEED;
         }
     }
 
     @Override
     public void move(int coefficient) {
         this.translate(currentXSpeed * coefficient, currentYSpeed * coefficient);
     }
 }
