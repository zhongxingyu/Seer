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
 
 package org.wings;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.swing.ImageIcon;
 import javax.imageio.ImageIO;
 
 import org.wings.session.SessionManager;
 import org.wings.externalizer.ExternalizeManager;
 
 /**
  * An SIcon of this type is externalized globally. It is not bound
  * to a session.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class FileImageIcon
     extends FileResource
     implements SIcon
 {
     /**
      * TODO: documentation
      */
     private int width = -1;
 
     /**
      * TODO: documentation
      */
     private int height = -1;
 
     /**
      * Create a new FileImageIcon from the File. This constructor extracts
      * the extension from the file to be appended to the externalized resource
      * name.
      *
      * @param resourceFileName
      */
     public FileImageIcon(String fileName) throws IOException {
         this(new File(fileName));
     }
 
     /**
      * crates a new FileImageIcon from the given file. The extension and
      * mimetype are taken from the parameters given.
      *
      * @param file      the file to construct a FileImageIcon from
      * @param extension user provided extension. The original extension of
      *                  the file is ignored, unless this paramter is
      *                  'null'.
      * @param mimetype  the user provided mimetype. If this is 'null', then
      *                  the mimetype is guessed from the extension.
      */
     public FileImageIcon(File file, String ext, String mt) {
         super(file, ext, mt);
         
         // if either of the extension or mimetype is missing, try to guess it.
         if (mimeType == null || mimeType.length() == 0) {
             if (extension == null || extension.length() == 0) {
                 extension = "";
                 mimeType = "image/png";
             }
             else if (extension.toUpperCase().equals("JPG"))
                 mimeType = "image/jpeg";
             else
                 mimeType = "image/" + extension;
         }
         else if (extension == null || extension.length() == 0) {
             int slashPos = -1;
             if (mimeType != null 
                 && (slashPos = mimeType.lastIndexOf('/')) >= 0) {
                 extension = mimeType.substring(slashPos+1);
             }
         }
         calcDimensions();
     }
 
     public FileImageIcon(File file) throws IOException {
         this(file, null, null);
     }
 
     /**
      *
      */
     protected void calcDimensions() {
         try {
             bufferResource();
             
             if ( buffer!=null && buffer.isValid()) {
                 BufferedImage image = 
                     ImageIO.read(new ByteArrayInputStream(buffer.getBytes()));
                 width = image.getWidth();
                height = image.getHeight();
             }
         } catch ( Throwable e ) {
             // is not possible to calc Dimensions
             // maybe it is not possible to buffer resource, 
             // or resource is not a
             // supported image type
         }
     }
 
     public int getIconWidth() {
         return width;
     }
 
     public int getIconHeight() {
         return height;
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
