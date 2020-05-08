 /**
  *    Copyright 2011-2012 Jim Tse
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.jtse.puzzle;
 
 import static org.lwjgl.opengl.GL11.GL_BLEND;
 import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glBlendFunc;
 import static org.lwjgl.opengl.GL11.glClear;
 import static org.lwjgl.opengl.GL11.glClearColor;
 import static org.lwjgl.opengl.GL11.glEnable;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glMatrixMode;
 import static org.lwjgl.opengl.GL11.glOrtho;
 import static org.lwjgl.opengl.GL11.glViewport;
 
 import java.awt.Point;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.nio.IntBuffer;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.BooleanUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Cursor;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.jtse.puzzle.ogl.Region;
 import com.github.jtse.puzzle.physics.Displacement;
 import com.github.jtse.puzzle.physics.PhysicsModule;
 import com.github.jtse.puzzle.ui.DeltaMouseEventFilter;
 import com.github.jtse.puzzle.ui.MouseEvent;
 import com.github.jtse.puzzle.ui.MouseModule;
 import com.github.jtse.puzzle.ui.MousePoller;
 import com.github.jtse.puzzle.ui.UI;
 import com.github.jtse.puzzle.util.ScriptModule;
 import com.github.jtse.puzzle.util.ScriptUtils;
 import com.github.jtse.puzzle.util.ScriptUtils.ScriptException;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.ProvisionException;
 import com.google.inject.name.Named;
 
 /**
  * @author jtse
  */
 public class Puzzle {
   private final Logger log = LoggerFactory.getLogger(Puzzle.class);
 
   @Inject @Named("_script-file")
   private File scriptFile;
 
   @Inject
   private Displacement displacement;
 
   @Inject
   private MousePoller mousePoller;
 
   @Inject
   private DeltaMouseEventFilter deltaMouseEventFilter;
 
   @Inject @Named("_script-config")
   private Map<String, String> config;
 
   @Inject @Named("_script-repeatable")
   private List<Map<String, String>> images;
 
   /**
    * @param args
    */
   public static void main(String[] args) {
     Logger log = LoggerFactory.getLogger(Puzzle.class);
 
     File scriptFile = args.length > 0
         ? new File(args[0])
         : UI.filePrompt(System.getProperty("user.dir") + "/puzzle");
 
     if (scriptFile == null) {
       return;
     }
 
     Puzzle puzzle = null;
     try {
       puzzle = Guice.createInjector(
               new PhysicsModule(),
               new MouseModule(),
               new ScriptModule(scriptFile, "image", "x", "y"))
           .getInstance(Puzzle.class);
     } catch (ProvisionException e) {
       log.error(e.getMessage(), e);
       UI.confirm(e.getCause().getMessage());
       return;
     }
 
     puzzle.run();
   }
 
   public void run() {
     try {
       int width = Display.getDisplayMode().getWidth();
       int height = Display.getDisplayMode().getHeight();
 
       Display.create();
       // Display.setDisplayMode(new DisplayMode(width, height));
 
       Display.setFullscreen(true);
 
       Display.setVSyncEnabled(true);
 
       // First "image" contains the header configuration
       configure(config);
 
       glEnable(GL_TEXTURE_2D);
       glEnable(GL_BLEND);
       glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
       glViewport(0, 0, width, height);
       glMatrixMode(GL11.GL_MODELVIEW);
       glMatrixMode(GL11.GL_PROJECTION);
       glLoadIdentity();
       glOrtho(0, width, height, 0, 1, -1);
       glMatrixMode(GL11.GL_MODELVIEW);
 
       // init resources
       Texture[] textures = new Texture[images.size()];
       Point[] points = new Point[images.size()];
       Region[] regions = new Region[images.size() + 4]; // 4 is for walls
 
       for (int i = 0; i < images.size(); i++) {
         InputStream in = new FileInputStream(new File(scriptFile.getParent(), images.get(i)
             .get("image")));
         textures[i] = TextureLoader.getTexture("PNG", in);
         regions[i] = Region.createRegion(textures[i]);
         points[i] = new Point(Integer.parseInt(images.get(i).get("x")), Integer.parseInt(
             images.get(i).get("y")));
       }
 
       setRegionPositions(regions, points);
 
       // Define the wall
       regions[images.size() + 0] = Region.createBlock(width, 1, 0, -1);
       regions[images.size() + 1] = Region.createBlock(1, height, width, 0);
       regions[images.size() + 2] = Region.createBlock(width, 1, 0, height);
       regions[images.size() + 3] = Region.createBlock(1, height, -1, 0);
 
       boolean quit = false;
 
       while (!Display.isCloseRequested() && !quit) {
         MouseEvent mouseEvent = mousePoller.poll();
         MouseEvent mouseDelta = deltaMouseEventFilter.apply(mouseEvent);
 
         Display.sync(60);
 
         if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
           quit = true;
         }
 
         if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
           setRegionPositions(regions, points);
         }
 
         if (mouseEvent.isButtonDown() && !mouseDelta.isButtonDown()) {
           int x = mouseEvent.getX();
           int y = height - mouseEvent.getY();
           int dx = mouseDelta.getX();
           int dy = mouseDelta.getY();
 
           for (int i = 0; i < regions.length; i++) {
             if (regions[i].contains(x, y)) {
               regions[i].setDxDy(dx, -dy);
 
               displacement.apply(regions[i], dx, -dy, regions);
 
               i = regions.length;
             }
           }
         }
 
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
         StringBuilder s = new StringBuilder("Regions ({}): ");
         // Move regions
         for (int i = 0; i < textures.length; i++) {
           int x = regions[i].getX();
           int y = regions[i].getY();
 
           s.append(x).append(",").append(y);
 
           if (i != (textures.length - 1)) {
             s.append(",");
           }
 
           renderImage(textures[i], x, y);
         }
         log.info(s.toString(), textures.length);
 
         Display.update();
        throw new RuntimeException("Soemthing");
       }
     } catch (ScriptException e) {
       UI.confirm("Script file contains errors:\n" + e.getMessage());
     } catch (Exception e) {
       log.error(e.getMessage(), e);
     } finally {
       Display.destroy();
     }
   }
 
   private static void configure(final Map<String, String> config) {
     if (config.containsKey("background-color")) {
       float[] colors = ScriptUtils.parseColor(config.get("background-color"));
       glClearColor(colors[0], colors[1], colors[2], colors[3]);
     } else {
       glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
     }
 
     if (config.containsKey("hide-mouse")) {
       boolean hide = BooleanUtils.toBoolean(config.get("hide-mouse"));
       if (hide) {
         try {
           // Create transparent cursor
           Cursor transparentCursor = new Cursor(1, 1, 0, 0, 1, IntBuffer.allocate(1), null);
           Mouse.setNativeCursor(transparentCursor);
         } catch (LWJGLException e) {
           throw new RuntimeException(e);
         }
       }
     }
   }
 
   /**
    * Renders a single image
    *
    * @param texture
    * @param x
    * @param y
    */
   private static void renderImage(Texture texture, int x, int y) {
     // Color.white.bind();
     // texture.bind(); // or GL11.glBind(texture.getTextureID());
     GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
     GL11.glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
 
     // GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,
     // GL11.GL_MODULATE );
 
     // when texture area is small, bilinear filter the closest mipmap
 
     // GL11.glTexParameterf( GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
     // GL11.GL_LINEAR_MIPMAP_NEAREST );
     // when texture area is large, bilinear filter the first mipmap
     // GL11.glTexParameterf( GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
     // GL11.GL_LINEAR );
 
     // GL11.glTexParameterf( GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
     // GL11.GL_CLAMP );
     // GL11.glTexParameterf( GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
     // GL11.GL_CLAMP );
 
     glBegin(GL11.GL_QUADS);
 
     GL11.glTexCoord2d(0.0d, 0.0d);
     GL11.glVertex2d(x, y);
 
     // It's 0.99 instead of 1.0 because 1.0 creates edge artifacts
     GL11.glTexCoord2d(0.99d, 0.0d);
     GL11.glVertex2d(x + texture.getTextureWidth(), y);
 
     GL11.glTexCoord2d(0.99d, 0.99d);
     GL11.glVertex2d(x + texture.getTextureWidth(), y + texture.getTextureHeight());
 
     GL11.glTexCoord2d(0.0d, 0.99d);
     GL11.glVertex2d(x, y + texture.getTextureHeight());
 
     glEnd();
   }
 
   private static void setRegionPositions(Region[] regions, Point[] points) {
     for (int i = 0; i < points.length; i++) {
       regions[i].setXY(points[i].x, points[i].y);
     }
   }
 }
