 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package main;
 
 import static opengl.GL.GL_COLOR_BUFFER_BIT;
 import static opengl.GL.GL_DEPTH_BUFFER_BIT;
 import static opengl.GL.GL_LINE_LOOP;
 import static opengl.GL.GL_STENCIL_BUFFER_BIT;
 import static opengl.GL.glBindVertexArray;
 import static opengl.GL.glClear;
 import static opengl.GL.glClearColor;
 import static opengl.GL.glDrawArrays;
 import static opengl.GL.glGetUniformLocation;
 import static opengl.GL.glPointSize;
 import static opengl.GL.glUniformMatrix4;
 import static opengl.GL.glUseProgram;
 import static opengl.GL.init;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.*;
 import org.lwjgl.*;
 
 import util.Camera;
 import util.Util;
 
 /**
  *
  * @author Sascha Kolodzey, Nico Marniok
  */
 public class CubeAndPyramid {
     private static int programID;       // shader program id
     private static int quadVAID;        // vertexarrayobject id of the cube
     private static int triangleVAID;    // vertexarrayobject id of the pyramid
     private static int modelLocation;   // uniform location of "model"
     private static final Camera cam = new Camera();
     private static final Matrix4f cubeModel[] = new Matrix4f[] {
         new Matrix4f(), new Matrix4f(), new Matrix4f(), new Matrix4f(),
     };
     private static final Matrix4f pyramidModel[] = new Matrix4f[] {
         new Matrix4f(), new Matrix4f(), new Matrix4f(), new Matrix4f(),
     };
     
     private static final Vector3f moveDir = new Vector3f(0.0f, 0.0f, 0.0f);
     private static boolean bContinue = true;
     
     public static void main(String[] argv) {
         try {
             init();
             programID = Util.createShaderProgram("shader/Main_VS.glsl", "shader/VertexColor_FS.glsl");
             modelLocation = glGetUniformLocation(programID, "model");
             quadVAID = Util.createQuad();
             triangleVAID = Util.createTriangle();
             glPointSize(16.0f);
             //glLineWidth(4.0f);
             initCube();
             initPyramid();
             render();
             Display.destroy();
         } catch (LWJGLException ex) {
             Logger.getLogger(CubeAndPyramid.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public static void render() throws LWJGLException {
         glClearColor(0.0f, 0.0f, 0.25f, 1.0f); // dark blue
         long last = System.currentTimeMillis();
         long now;
         while(bContinue && !Display.isCloseRequested()) {            
             glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
             glUseProgram(programID);
             
             for(Matrix4f cubeSide : cubeModel) {
                 cubeSide.store(Util.MAT_BUFFER);
                 Util.MAT_BUFFER.position(0);
                 glUniformMatrix4(modelLocation, false, Util.MAT_BUFFER);
                 glBindVertexArray(quadVAID);
                 glDrawArrays(GL_LINE_LOOP, 0, 4);
             }
             
             for(Matrix4f pyramidSide : pyramidModel) {
                 pyramidSide.store(Util.MAT_BUFFER);
                 Util.MAT_BUFFER.position(0);
                 glUniformMatrix4(modelLocation, false, Util.MAT_BUFFER);
                 glBindVertexArray(triangleVAID);
                 glDrawArrays(GL_LINE_LOOP, 0, 3);
             }
             
             Display.update();
             Display.sync(60);
             
             now = System.currentTimeMillis();
             handleInput(now - last);
             updateUniforms();
             last = now;
         }
     }
     
     public static void initCube()
     {
        Util.mul(cubeModel[0], Util.translationX(  0.5f, null), Util.translationZ(-0.25f, null), Util.scale(0.25f, null));
        Util.mul(cubeModel[1], Util.translationX( 0.25f, null), Util.rotationY( Util.PI_DIV2, null),  Util.scale(0.25f, null));
        Util.mul(cubeModel[2], Util.translationX( 0.75f, null), Util.rotationY( Util.PI_DIV2, null),  Util.scale(0.25f, null));
        Util.mul(cubeModel[3], Util.translationX(  0.5f, null), Util.translationZ( 0.25f, null), Util.scale(0.25f, null));	
     }
     
     public static void initPyramid()
     {
        Util.mul(pyramidModel[0], Util.translationX(-0.5f, null), Util.translationX(-0.125f, null), Util.rotationY(Util.PI_DIV2, null), Util.rotationX(Util.PI / 6.0f, null), Util.scale(0.25f, null));
        Util.mul(pyramidModel[1], Util.translationX(-0.5f, null), Util.translationZ(+0.125f, null), Util.rotationY(Util.PI, null),      Util.rotationX(Util.PI / 6.0f, null), Util.scale(0.25f, null));
        Util.mul(pyramidModel[2], Util.translationX(-0.5f, null), Util.translationX(+0.125f, null), Util.rotationY(3f * Util.PI_DIV2, null) , Util.rotationX(Util.PI / 6.0f, null), Util.scale(0.25f, null));
        Util.mul(pyramidModel[3], Util.translationX(-0.5f, null), Util.translationZ(-0.125f, null), Util.rotationX(Util.PI / 6.0f, null), Util.scale(0.25f, null));
     }
     
     public static void handleInput(long millis) {
         float moveSpeed = 2e-3f*(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 2.0f : 1.0f)*(float)millis;
         
         while(Keyboard.next()) {
             if(Keyboard.getEventKeyState()) {
                 switch(Keyboard.getEventKey()) {
                     case Keyboard.KEY_W:        moveDir.z += 1.0f; break;
                     case Keyboard.KEY_S:        moveDir.z -= 1.0f; break;
                     case Keyboard.KEY_A:        moveDir.x += 1.0f; break;
                     case Keyboard.KEY_D:        moveDir.x -= 1.0f; break;
                     case Keyboard.KEY_LCONTROL: moveDir.y += 1.0f; break;
                     case Keyboard.KEY_SPACE:    moveDir.y -= 1.0f; break;
                 }
             } else {
                 switch(Keyboard.getEventKey()) {
                     case Keyboard.KEY_W:        moveDir.z -= 1.0f; break;
                     case Keyboard.KEY_S:        moveDir.z += 1.0f; break;
                     case Keyboard.KEY_A:        moveDir.x -= 1.0f; break;
                     case Keyboard.KEY_D:        moveDir.x += 1.0f; break;
                     case Keyboard.KEY_LCONTROL: moveDir.y -= 1.0f; break;
                     case Keyboard.KEY_SPACE:    moveDir.y += 1.0f; break;
                     case Keyboard.KEY_F1:  cam.changeProjection(); break;
                 }
             }
         }
         
         cam.move(moveSpeed * moveDir.z , moveSpeed * moveDir.x, moveSpeed * moveDir.y);
 
 		float camSpeed = (float) 5e-3;
 		        
 		while(Mouse.next())
 		{
 			if(Mouse.getEventButton() == 0)
 			{
 				Mouse.setGrabbed(Mouse.getEventButtonState());
 			}
 			if(Mouse.isGrabbed())
 			{
 				float phi =   -camSpeed * Mouse.getDX();
 				float theta = -camSpeed * Mouse.getDY();
 				cam.rotate(phi, theta);
 			}
 		}
 		        
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) bContinue = false;
     }
     
     public static void updateUniforms() {  
         
         Matrix4f view = cam.getView();
         Matrix4f projection = cam.getProjection();
         //Util.orthographicRH(projection);
         Matrix4f viewProj = Matrix4f.mul(projection, view, null);
         viewProj.store(Util.MAT_BUFFER);
         Util.MAT_BUFFER.position(0);
         int viewProjLoc = glGetUniformLocation(programID, "viewProj");
         glUniformMatrix4(viewProjLoc, false, Util.MAT_BUFFER);
     }
 }
