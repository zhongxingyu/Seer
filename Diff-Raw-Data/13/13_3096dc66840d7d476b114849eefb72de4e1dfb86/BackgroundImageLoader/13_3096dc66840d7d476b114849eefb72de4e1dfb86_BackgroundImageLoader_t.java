 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.textpane;
 
 import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 
 import java.awt.Image;
 import java.io.IOException;
 import java.net.URL;
 import java.util.concurrent.ExecutionException;
 
 import javax.imageio.ImageIO;
 
 /**
  * Loads a background image in the background.
  */
class BackgroundImageLoader extends LoggingSwingWorker<Image, Void> {
 
     /** URL of image file to load. */
     private final URL imageURL;
     /** Textpane canvas to load image for. */
     private final TextPaneCanvas canvas;
 
     /**
      * Creates a new background image loader.
      *
      * @param canvas Canvas we're loading image for
      * @param url URL of image to load
      */
    protected BackgroundImageLoader(final TextPaneCanvas canvas,
            final URL url) {
         super();
         imageURL = url;
         this.canvas = canvas;
     }
 
     /** {@inheritDoc} */
     @Override
     protected Image doInBackground() {
         try {
             if (imageURL != null) {
                 return ImageIO.read(imageURL);
             }
             return null;
         } catch (IOException ex) {
             return null;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     protected void done() {
         if (isCancelled()) {
             return;
         }
         try {
             canvas.setBackgroundImage(get());
         } catch (InterruptedException ex) {
             canvas.setBackgroundImage(null);
         } catch (ExecutionException ex) {
             Logger.appError(ErrorLevel.MEDIUM, ex.getMessage(), ex);
         }
     }
 }
