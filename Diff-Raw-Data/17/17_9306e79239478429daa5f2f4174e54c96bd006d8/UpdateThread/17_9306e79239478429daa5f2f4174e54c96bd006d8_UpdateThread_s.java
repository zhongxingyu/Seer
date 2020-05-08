 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.graphics;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import com.quartercode.disconnected.Disconnected;
 import com.quartercode.disconnected.graphics.component.GraphicsState;
 import com.quartercode.disconnected.graphics.component.RootWidget;
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.theme.ThemeManager;
 import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twleffects.lwjgl.LWJGLEffectsRenderer;
 
 /**
  * This thread executes the OpenGL update which keeps the lwjgl display alive.
  * 
  * @see GraphicsManager
  */
 public class UpdateThread extends Thread {
 
     private static final Logger LOGGER = Logger.getLogger(UpdateThread.class.getName());
 
     private final RootWidget    root;
     private GUI                 gui;
     private LWJGLRenderer       renderer;
     private ThemeManager        currentTheme;
 
     /**
      * Creates a new update thread.
      */
     public UpdateThread() {
 
         super("graphis-update");
 
         root = new RootWidget();
     }
 
     /**
      * Returns the used root widget (the parent frame of every other widget).
      * 
      * @return The used root widget (the parent frame of every other widget).
      */
     public RootWidget getRoot() {
 
         return root;
     }
 
     /**
      * Returns the twl gui object.
      * 
      * @return The twl gui object.
      */
     public GUI getGui() {
 
         return gui;
     }
 
     /**
      * Returns the twl lwjgl renderer which renders the widgets on the display.
      * 
      * @return The twl lwjgl renderer which renders the widgets on the display.
      */
     public LWJGLRenderer getRenderer() {
 
         return renderer;
     }
 
     /**
      * Returns the twl theme manager which manages the current theme.
      * 
      * @return The twl theme manager which manages the current theme.
      */
     public ThemeManager getCurrentTheme() {
 
         return currentTheme;
     }
 
     @Override
     public void run() {
 
         try {
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             // Display.setDisplayMode(new DisplayMode(1200, 700));
             Display.setDisplayMode(new DisplayMode((int) (screenSize.width * 0.75F), (int) (screenSize.height * 0.75F)));
             // Display.setFullscreen(true);
             Display.setTitle("Disconnected " + Disconnected.getVersion());
             ByteBuffer[] icons = new ByteBuffer[4];
             icons[0] = loadImage(getClass().getResource("/images/icons/icon16.png"));
             icons[1] = loadImage(getClass().getResource("/images/icons/icon32.png"));
             icons[2] = loadImage(getClass().getResource("/images/icons/icon64.png"));
             icons[3] = loadImage(getClass().getResource("/images/icons/icon128.png"));
             Display.setIcon(icons);
 
             Display.setVSyncEnabled(true);
             Display.create();
 
            renderer = new LWJGLEffectsRenderer();
             renderer.setUseSWMouseCursors(true);
             gui = new GUI(root, renderer);
             gui.setSize();
             renderer.syncViewportSize();
 
             GraphicsState lastState = null;
             while (!Display.isCloseRequested() && !interrupted()) {
                 if (lastState == null || !lastState.equals(root.getState())) {
                     if (lastState != null) {
                         currentTheme.destroy();
                         root.removeChild(lastState);
                     }
                     lastState = root.getState();
                     if (lastState != null) {
                         currentTheme = ThemeManager.createThemeManager(lastState.getClass().getResource(lastState.getThemeResource()), renderer);
                         gui.applyTheme(currentTheme);
                         root.add(lastState);
                     }
                 }
 
                 gui.update();
                 Display.sync(60);
                 Display.update();
 
                 // Reduce lag on input devices
                 GL11.glGetError();
                 Display.processMessages();
                 Mouse.poll();
                 Keyboard.poll();
             }
         }
         catch (LWJGLException e) {
             LOGGER.log(Level.SEVERE, "Error while creating lwjgl display", e);
         }
         catch (IOException e) {
             LOGGER.log(Level.SEVERE, "Error while loading files", e);
         }
 
         gui.destroy();
         if (currentTheme != null) {
             currentTheme.destroy();
         }
         Display.destroy();
     }
 
     private ByteBuffer loadImage(URL url) throws IOException {
 
         InputStream inputStream = url.openStream();
         try {
             PNGDecoder decoder = new PNGDecoder(inputStream);
             ByteBuffer buffer = ByteBuffer.allocateDirect(decoder.getWidth() * decoder.getHeight() * 4);
             decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
             buffer.flip();
             return buffer;
         }
         finally {
             inputStream.close();
         }
     }
 
 }
