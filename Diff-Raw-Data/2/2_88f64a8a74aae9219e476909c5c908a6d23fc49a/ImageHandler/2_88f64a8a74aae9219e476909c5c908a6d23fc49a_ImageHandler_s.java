 /*
  * Copyright 2010 Raffael Herzog
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
 
 package ch.raffael.util.i18n.impl.handlers;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.IndexColorModel;
 import java.io.BufferedInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 
 import org.slf4j.Logger;
 
 import ch.raffael.util.common.logging.LogUtil;
 import ch.raffael.util.i18n.ResourceBundle;
 import ch.raffael.util.i18n.impl.ResourcePointer;
 
 
 /**
  * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
  */
 public class ImageHandler extends NoParametersHandler {
 
     @SuppressWarnings("UnusedDeclaration")
     private static final Logger log = LogUtil.getLogger();
 
     private static BufferedImage notFound = null;
 
     @Override
     public Object resolve(Class<? extends ResourceBundle> bundleClass, ResourcePointer ptr, URL baseUrl, String value) throws Exception {
         URL url = new URL(baseUrl, value);
        log.debug("Loading image from {}");
         InputStream input = null;
         try {
             input = new BufferedInputStream(url.openStream());
             return ImageIO.read(input);
         }
         catch( FileNotFoundException e ) {
             log.debug("No image data at {}", url);
             return null;
         }
         finally {
             if ( input != null ) {
                 try {
                     input.close();
                 }
                 catch ( Exception e ) {
                     log.error("Error closing stream from " + url, e);
                 }
             }
         }
     }
 
     @Override
     public Object notFound(Class<? extends ResourceBundle> bundleClass, ResourcePointer ptr, URL baseUrl) {
         return notFound();
     }
 
     private static synchronized Image notFound() {
         if ( notFound == null ) {
             notFound = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_BINARY,
                                          new IndexColorModel(1, 2,
                                                              new byte[] { 0, (byte)255 },
                                                              new byte[] { 0, (byte)255 },
                                                              new byte[] { 0, (byte)255 }
                                                              ));
             Graphics gfx = notFound.getGraphics();
             for ( int i = 0; i < 16; i++ ) {
                 boolean set = i % 2 == 0;
                 for ( int j = 0; j < 16; j++ ) {
                     if ( set ) {
                         gfx.setColor(Color.WHITE);
                     }
                     else {
                         gfx.setColor(Color.BLACK);
                     }
                     gfx.drawLine(i, j, i, j);
                     set = !set;
                 }
             }
         }
         return notFound;
     }
 
 }
