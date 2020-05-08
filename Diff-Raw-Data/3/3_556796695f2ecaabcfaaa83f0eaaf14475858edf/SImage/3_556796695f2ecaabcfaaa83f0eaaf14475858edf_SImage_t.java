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
 
 import java.awt.Image;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 
 import org.wings.plaf.*;
 import org.wings.io.Device;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SImage
     extends SComponent
 {
     /**
      * TODO: documentation
      */
     protected String imagePath = "";
 
     /**
      * TODO: documentation
      */
     protected ImageIcon image = null;
 
     /**
      * TODO: documentation
      */
     protected String text = null;
 
     /**
      * TODO: documentation
      */
     protected String descr = null;
 
     /**
      * TODO: documentation
      */
     protected int alignment = SConstants.NO_ALIGN;
 
     /**
      * TODO: documentation
      */
     protected int width;
 
     /**
      * TODO: documentation
      */
     protected int height;
 
     /**
      * TODO: documentation
      */
     protected int border;
 
 
     /**
      * TODO: documentation
      *
      * @param img
      */
     public SImage(String img) {
         setImage(img);
     }
 
     /**
      * TODO: documentation
      *
      * @param img
      */
     public SImage(Image img) {
         setImage(img);
     }
 
     /**
      * TODO: documentation
      *
      * @param img
      */
     public SImage(ImageIcon img) {
         setImage(img);
     }
 
 
     /**
      * TODO: documentation
      *
      * @param al
      */
     public void setAlignment(int al) {
         alignment = al;
     }
 
     /**
      * TODO: documentation
      *
      * @param img
      */
     public void setImage(String img) {
         if ( img!=null)
             imagePath = img;
     }
 
     /**
      * TODO: documentation
      *
      * @param img
      */
     public void setImage(URL img) {
         if ( img!=null)
             setImage(img.toString());
     }
 
     /**
      * TODO: documentation
      *
      * @param img
      */
     public void setImage(Image img) {
         if ( img!=null)
             setImage(new ImageIcon(img));
     }
 
     /**
      * TODO: documentation
      *
      * @param img
      */
     public void setImage(ImageIcon img) {
         image = img;
     }
 
     /**
      * TODO: documentation
      *
      * @param text
      */
     public void setDescription(String text) {
         descr = text;
         if ( descr!=null )
             descr = descr.trim();
     }
 
     /**
      * TODO: documentation
      *
      * @param at
      */
     public void setAlternativeText(String at) {
         text = at;
         if ( text!=null )
             text = text.trim();
     }
 
     /**
      * TODO: documentation
      *
      * @param w
      */
     public void setWidth(int w) {
         if ( w>0 )
             width = w;
     }
 
     /**
      * TODO: documentation
      *
      * @param h
      */
     public void setHeight(int h) {
         if ( h>0 )
             height = h;
     }
 
     /**
      * TODO: documentation
      *
      * @param b
      */
     public void setBorder(int b) {
         if ( b>-1 )
             border = b;
     }
 
 
     /**
      * TODO: documentation
      *
      * @param s
      */
     public void appendPrefix(Device s) {
         String imagePath = this.imagePath;
 
         if ( image!=null ) {
             try {
                 imagePath = getExternalizeManager().externalize(image);
             } catch ( Exception e) {}
         }
 
         s.append("<img src=\""+imagePath+"\"");
         if ( text!=null && text.length()>0 )
             s.append(" alt=\"" + text + "\"");
 
         s.append(" width=\"");
         if ( width>0 )
             s.append(width);
         else
        if ( image != null )
             s.append(image.getIconWidth());
 
         s.append("\"");
         s.append(" height=\"");
         if ( height>0 )
             s.append(height);
         else
        if ( image != null )
             s.append(image.getIconHeight());
 
         s.append("\"");
 
         if ( alignment==SConstants.RIGHT_ALIGN )
             s.append(" align=\"right\"");
         else if ( alignment==SConstants.LEFT_ALIGN )
             s.append(" align=\"\"left\"");
         else if ( alignment==SConstants.CENTER_ALIGN )
             s.append(" align=\"middle\"");
         else if ( alignment==SConstants.TOP_ALIGN )
             s.append(" align=\"top\"");
         else if ( alignment==SConstants.BOTTOM_ALIGN )
             s.append(" align=\"bottom\"");
 
         s.append(" border=\"" + border + "\" />");
     }
 
     /**
      * TODO: documentation
      *
      * @param s
      */
     public void appendBody(Device s) {
         if ( descr!=null && descr.length()>0 )
             s.append(descr);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
