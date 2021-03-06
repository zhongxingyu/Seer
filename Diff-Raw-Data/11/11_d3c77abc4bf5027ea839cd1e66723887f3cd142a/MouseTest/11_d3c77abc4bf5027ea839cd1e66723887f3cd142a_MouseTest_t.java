 /*
  * Copyright (c) 2002 Lightweight Java Game Library Project
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'Lightweight Java Game Library' nor the names of
  *   its contributors may be used to endorse or promote products derived
  *   from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.lwjgl.test.input;
 
 import org.lwjgl.DisplayMode;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL;
 import org.lwjgl.opengl.GLU;
 import org.lwjgl.vector.Vector2f;
 
 /**
  * $Id$
  * <br>
  * Mouse test
  *
  * @author Brian Matzon <brian@matzon.dk>
  * @version $Revision$
  */
 public class MouseTest {
 
   /** OpenGL instance */
   private GL gl;
 
   /** GLU instance */
   private GLU glu;
 
   /** position of quad to draw */
   private Vector2f position = new Vector2f(320.0f, 240.0f);
   
   /** Display mode selected */
   private DisplayMode displayMode;
 
   /** Creates a new instance of MouseTest */
   public MouseTest() {
   }
 
   private void initialize() {
     // create display and opengl
     setupDisplay(false);
 
     try {
       Keyboard.create();
     } catch (Exception e) {
       e.printStackTrace();
       System.exit(-1);
     }
   }
   
   private void setupDisplay(boolean fullscreen) {
     try {
       gl = new GL("MouseTest", 50, 50, 640, 480, 32, 0, 0, 0);
       gl.create();
 
       glu = new GLU(gl);
     } catch (Exception e) {
       e.printStackTrace();
       System.exit(-1);
     }
 
     initializeOpenGL();    
   }
 
   private void initializeOpenGL() {
     gl.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
     glu.ortho2D(0.0, 640, 0, 480);
   }
 
   public void executeTest() {
     initialize();
 
     createMouse();
 
     wiggleMouse();
 
     Mouse.destroy();
     Keyboard.destroy();
     gl.destroy();
   }
 
   private void createMouse() {
     try {
       Mouse.create();
     } catch (Exception e) {
       e.printStackTrace();
       System.exit(-1);
     }
   }
 
   private void wiggleMouse() {
     while (!gl.isCloseRequested()) {
      gl.tick();
      
       if(gl.isMinimized()) {
        try {
					Thread.sleep(100);
				} catch (InterruptedException inte) {
					inte.printStackTrace();
				}
         continue;
       }
 
       Mouse.poll();
       Keyboard.poll();
       
       if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
         return;
       }
 
       position.x += Mouse.dx;
       position.y -= Mouse.dy;
       
       if(position.x<0) {
         position.x = 0;
       } else if (position.x>640-60) {
         position.x = 640-60;
       }
       
       if(position.y < 0) {
         position.y = 0;
       } else if (position.y>480-30) {
         position.y = 480-30;
       }
       
 
       render();
 
       gl.paint();
     }
   }
   
   private void render() {
     gl.clear(GL.COLOR_BUFFER_BIT);
 
     gl.begin(GL.POLYGON);
     {
       float color = 1.0f;
       int buttonDown = 0;
       
       for(int i=0;i<Mouse.buttonCount; i++) {
         if(Mouse.isButtonDown(i)) {
           color = (1.0f / Mouse.buttonCount) * (i+1); 
           break; 
         }
       }
       gl.color3f(color, color, color);
       
       gl.vertex2f(position.x + 0.0f, position.y + 0.0f);
       gl.vertex2f(position.x + 0.0f, position.y + 30.0f);
       gl.vertex2f(position.x + 40.0f, position.y + 30.0f);
       gl.vertex2f(position.x + 60.0f, position.y + 15.f);
       gl.vertex2f(position.x + 40.0f, position.y + 0.0f);
     }
     gl.end();
   }
 
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
     MouseTest mt = new MouseTest();
     mt.executeTest();
   }
 }
