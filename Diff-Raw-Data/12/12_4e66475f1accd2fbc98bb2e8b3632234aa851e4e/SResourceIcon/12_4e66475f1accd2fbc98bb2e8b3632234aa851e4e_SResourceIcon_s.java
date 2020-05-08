 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.resource.ClassPathResource;
 import org.wings.util.ImageInfo;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 
 /*
  * Diese Klasse ist nur ein Wrapper, um Eingabestroeme von Grafiken mit dem
  * ExternalizeManager mit der richtigen Endung und ohne Umweg einer neuen
  * Codierung (die z.B. keine Transparenz unterstuetzt) uebers WWW zugreifbar zu
  * machen. Zugleich muss diese Klasse aber auch zu der API der Componenten
  * passen, also ein Image bzw. ImageIcon sein. ImageIcon ist einfacher zu
  * benutzen und implementiert schon alles was benoetigt wird...
  */
 
 /**
  * SIcon that gets it's content from an image found in the classpath.
  * i.e. an image that is deployed in the classes/jar/war file.
  * <p/>
  * An SIcon of this type is externalized globally. It is not bound
  * to a session.
  * 
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 public class SResourceIcon extends ClassPathResource implements SIcon {
 
     private final transient static Log log = LogFactory.getLog(SResourceIcon.class);
 
     /**
      * Width of icon, <code>-1</code> if not set.
      */
     private int width = -1;
 
     /**
      * Height of icon, <code>-1</code> if not set.
      */
     private int height = -1;
 
     /**
      * Title of icon, <code>""</code> if not set.
      */
     private String title = null;
 
 
     public SResourceIcon(String resourceFileName) {
        this(SResourceIcon.class.getClassLoader(), resourceFileName);
     }
 
     public SResourceIcon(ClassLoader classLoader, String resourceFileName) {
         super(classLoader, resourceFileName);
 
         try {
             bufferResource();
         } catch (Throwable e) {
             log.fatal("Can not buffer resource " + resourceFileName);
         }
 
         if (buffer != null && buffer.isValid()) {
             ImageInfo tImageInfo = new ImageInfo();
             ByteArrayInputStream tImageInput = new ByteArrayInputStream(buffer.getBytes());
             tImageInfo.setInput(tImageInput);
 
             if (tImageInfo.check()) {
                 extension = tImageInfo.getFormatName();
                 mimeType = tImageInfo.getMimeType();
                 width = tImageInfo.getWidth();
                 height = tImageInfo.getHeight();
             }
 
             try {
                 tImageInput.close();
             } catch (IOException ex) {
                 // ignore it, we don't need it anymore...
             }
         }
     }
 
     public int getIconWidth() {
         return width;
     }
 
     public int getIconHeight() {
         return height;
     }
 
     public void setIconWidth(int width) {
         this.width = width;
     }
 
     public void setIconHeight(int height) {
         this.height = height;
     }
 
     public String getIconTitle() {
         return (title!=null) ? title : "";
     }
     
     public void setIconTitle(String title) {
         this.title = title;
     }
 }
