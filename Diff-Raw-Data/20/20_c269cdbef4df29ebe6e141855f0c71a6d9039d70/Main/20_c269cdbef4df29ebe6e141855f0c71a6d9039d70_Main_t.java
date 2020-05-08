 /*
  * Copyright (c) 2013 devnewton <devnewton@bci.im>
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
  * * Neither the name of 'devnewton <devnewton@bci.im>' nor the names of
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
 package org.geekygoblin.nedetlesmaki.game;
 
 import im.bci.lwjgl.nuit.utils.LwjglHelper;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 import org.geekygoblin.nedetlesmaki.game.assets.Assets;
 import org.geekygoblin.nedetlesmaki.game.assets.VirtualFileSystem;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Controllers;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 public class Main {
 
     static void setupLibraryPath() {
         if (System.getProperty("javawebstart.version") != null) {
             return;
         }
 
         String libraryPath = System.getProperty("java.library.path");
         if (libraryPath != null && libraryPath.contains("natives")) {
             return;
         }
 
         try {
             File nativeDir = new File(getApplicationDir(), "natives");
             if (nativeDir.exists() && nativeDir.isDirectory() && nativeDir.list().length > 0) {
                 String nativePath = nativeDir.getCanonicalPath();
                 System.setProperty("org.lwjgl.librarypath", nativePath);
                 System.setProperty("net.java.games.input.librarypath", nativePath);
             }
         } catch (IOException e) {
             Logger.getLogger(Main.class.getName()).log(Level.WARNING, "error", e);
         }
         Logger.getLogger(Main.class.getName()).log(Level.INFO, "Cannot find 'natives' library folder, try system libraries");
     }
 
     public static File getApplicationDir() throws IOException {
         try {
             return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
         } catch (URISyntaxException uriEx) {
             Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Cannot find application directory, try current", uriEx);
             return new File(".");
         }
     }
 
     public static void main(String[] args) throws IOException, ClassNotFoundException, Exception {
 
         try {
             setupLibraryPath();
         } catch (Throwable e) {
             handleError(e, "Unexpected error during startup. Check your java version and your opengl driver.\n");
             return;
         }
 
         try {
             setVideoMode();
            initGamepads();
 
             File applicationDir = getApplicationDir();
             VirtualFileSystem vfs = new VirtualFileSystem(new File(applicationDir, "data"), new File(applicationDir.getParentFile(), "data"));
             try (Assets assets = new Assets(vfs)) {
                 assets.setIcon();
                 Game game = new Game(assets);
                 while (!game.isCloseRequested()) {
                     game.setDelta(1.0f / 60.0f);
                     game.process();
                     Display.update(false);
                     Display.sync(60);
                     Display.processMessages();
                     Mouse.poll();
                     Keyboard.poll();
                     Controllers.poll();
                 }
             }
             saveVideoModePreferences();
         } catch (Throwable e) {
             handleError(e, "Unexpected error during execution.\n");
         }
     }
 
     public static void handleError(Throwable e, final String defaultMessage) {
         Logger.getLogger(Main.class.getName()).log(Level.SEVERE, defaultMessage, e);
         JOptionPane.showMessageDialog(null, defaultMessage + "\n" + e.getMessage() + (e.getCause() != null ? "\nCause: " + e.getCause().getMessage() : ""), "Error", JOptionPane.ERROR_MESSAGE);
     }
 
     private static void saveVideoModePreferences() {
         Preferences pref = Game.getPreferences();
         pref.putInt("video.width", Display.getDisplayMode().getWidth());
         pref.putInt("video.height", Display.getDisplayMode().getHeight());
         pref.putBoolean("video.fullscreen", Display.isFullscreen());
         pref.saveConfig();
 
     }
 
     private static void setVideoMode() throws LWJGLException {
         Preferences pref = Game.getPreferences();
         DisplayMode mode = new DisplayMode(pref.getInt("video.width", 800), pref.getInt("video.height", 600));
         if (pref.getBoolean("video.fullscreen", false)) {
             Display.setDisplayModeAndFullscreen(mode);
         } else {
             Display.setFullscreen(false);
             Display.setDisplayMode(mode);
         }
         LwjglHelper.setResizable(true);
         Display.setTitle("Ned et les maki");
         if (!Display.isCreated()) {
             Display.create();
         }
     }

    private static void initGamepads() {
        try {
            Controllers.create();
        } catch (LWJGLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Cannot init gamepad support", ex);
        }
    }
 }
