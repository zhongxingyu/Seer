 /*
  * Copyright (c) 2013 George Weller
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package uk.co.zutty.glarena;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.*;
 import org.lwjgl.util.vector.Matrix4f;
 import uk.co.zutty.glarena.util.MatrixUtils;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class Game {
 
     protected Matrix4f projectionMatrix = null;
     protected Camera camera;
     private List<Entity> entities;
     private Collection<Entity> toRemove = new ArrayList<>();
     private List<ModelInstance> instances = new ArrayList<>();
 
     public Game() {
         // Initialize OpenGL (Display)
         this.setupOpenGL();
 
         setup();
         init();
         Util.checkGLError();
 
         Display.setVSyncEnabled(true);
 
         while (!Display.isCloseRequested()) {
             update();
             render();
 
             Display.update();
             Display.sync(60);
         }
 
         this.destroyOpenGL();
     }
 
     private void setup() {
         projectionMatrix = MatrixUtils.frustum(Display.getWidth(), Display.getHeight(), 60, 0.1f, 100.0f);
         camera = new Camera();
         entities = new ArrayList<>();
     }
 
     private void setupOpenGL() {
         // Setup an OpenGL context with API version 3.2
         try {
             PixelFormat pixelFormat = new PixelFormat();
             ContextAttribs contextAttributes = new ContextAttribs(3, 2)
                     .withForwardCompatible(true)
                     .withProfileCore(true);
 
             Display.setDisplayMode(new DisplayMode(1024, 768));
             Display.setTitle("3D Game");
             Display.create(pixelFormat, contextAttributes);
 
             GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
         } catch (LWJGLException e) {
             e.printStackTrace();
             System.exit(-1);
         }
 
         // Setup an XNA like background color
         //GL11.glClearColor(0.4f, 0.6f, 0.9f, 0f);
         GL11.glClearColor(0f, 0f, 0f, 0f);
 
         glEnable(GL_DEPTH_TEST);
         glEnable(GL_CULL_FACE);
         glCullFace(GL_BACK);
         glClearDepth(1);
 
         Util.checkGLError();
     }
 
     protected void init() {
     }
 
     public void add(Entity entity) {
         entities.add(entity);
         instances.add(entity.getModelInstance());
     }
 
     public void remove(Entity entity) {
         toRemove.add(entity);
     }
 
     protected void update() {
         for (Entity entity : entities) {
             entity.update();
         }
 
         // Update list
         for (Entity r : toRemove) {
             entities.remove(r);
         }
         toRemove.clear();
 
         Util.checkGLError();
     }
 
     protected void render() {
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
         for (ModelInstance instance : instances) {
             Technique technique = instance.getModel().getTechnique();
             technique.setCamera(camera);
             technique.setProjectionMatrix(projectionMatrix);
 
             technique.renderInstance(instance);
         }
 
         Util.checkGLError();
     }
 
     private void destroyOpenGL() {
         Util.checkGLError();
         Display.destroy();
     }
 }
