 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings.externalizer;
 
 import java.awt.Image;
 import Acme.JPM.Encoders.GifEncoder;
 import com.keypoint.*;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @author <a href="mailto:mreinsch@to.com">Michael Reinsch</a>
  * @version $Revision$
  */
 public class ImageObjectHandler
     implements ObjectHandler
 {
     public static final String FORMAT_PNG = "png";
     public static final String FORMAT_GIF = "gif";
 
     protected String format = FORMAT_PNG;
 
     public ImageObjectHandler() {}
 
     public ImageObjectHandler(String format) {
         this.format = format;
     }
 
     public String getExtension(Object obj) {
         return format;
     }
 
     public String getMimeType(Object obj) {
         return "image/" + format;
     }
 
     public boolean isStable(Object obj) {
         return false;
     }
 
     public Class getSupportedClass() {
         return Image.class;
     }
 
     public void write(Object obj, java.io.OutputStream out)
         throws java.io.IOException
     {
         Image img = (Image)obj;
         if (FORMAT_PNG.equals(format))
             writePNG(img, out);
         else
             writeGIF(img, out);
     }
 
     public void writeGIF(Image img, java.io.OutputStream out)
         throws java.io.IOException
     {
         GifEncoder encoder = new GifEncoder(img, out, true);
         encoder.encode();
     }
 
     public void writePNG(Image img, java.io.OutputStream out)
         throws java.io.IOException
     {
         PngEncoder png =  new PngEncoder(img, PngEncoder.ENCODE_ALPHA);
         byte[] pngbytes = png.pngEncode();
         if (pngbytes == null) {
             System.err.println("Null image");
         }
         else {
             out.write(pngbytes);
         }
         out.flush();
     }
     
	public java.util.Set getHeaders(Object obj) { return null; }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
