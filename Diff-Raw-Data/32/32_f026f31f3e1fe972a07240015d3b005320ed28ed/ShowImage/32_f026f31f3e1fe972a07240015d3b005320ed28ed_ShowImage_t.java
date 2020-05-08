 /*
  * ShowImage.java
  * Transform
  *
  * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package debug;
 
 import java.io.File;
 
 import com.flagstone.transform.Background;
 import com.flagstone.transform.Movie;
 import com.flagstone.transform.Place2;
 import com.flagstone.transform.ShowFrame;
 import com.flagstone.transform.coder.ImageTag;
 import com.flagstone.transform.datatype.Bounds;
 import com.flagstone.transform.datatype.WebPalette;
 import com.flagstone.transform.util.image.ImageFactory;
 
 /**
  * ShowImage displays an image in a Flash file.
  */
 public final class ShowImage {
     /**
      * Run the test from the command line.
      * @param args array of command line arguments.
      */
     public static void main(final String[] args) {
 
         final String sourceFile = args[0];
         final String destFile = args[1];
         Movie movie;
         ImageFactory factory;
 
         try {
             movie = new Movie();
 
            factory = new ImageFactory();
             factory.read(new File(sourceFile));
 
             final ImageTag image = factory.defineImage(movie.nextId());
             final Place2.Builder builder = new Place2.Builder();
 
             final int imageWidth = image.getWidth();
             final int imageHeight = image.getHeight();
             final int screenWidth = imageWidth * 20;
             final int screenHeight = imageHeight * 20;
             final int shapeId = movie.nextId();
 
             movie.setFrameRate(1.0f);
             movie.setSignature(Movie.Signature.FWS);
             movie.setFrameSize(new Bounds(-screenWidth / 2, -screenHeight / 2,
                     screenWidth / 2, screenHeight / 2));
 
             movie.add(new Background(WebPalette.LIGHT_BLUE.color()));
             movie.add(image);
             movie.add(factory.defineEnclosingShape(shapeId,
                     image.getIdentifier(),
                     imageWidth / 2, imageHeight / 2, null));
             movie.add(builder.show(shapeId, 1, 0, 0));
             movie.add(ShowFrame.getInstance());
 
             movie.encodeToFile(new File(destFile));
         } catch (final Exception e) {
             e.printStackTrace();
         }
     }
 
     /** Private constructor. */
     private ShowImage() {
         // Private.
     }
 }
