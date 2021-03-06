 /*
  * Copyright (c) 2004-2009 XMLVM --- An XML-based Programming Language
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
  * Ave, Cambridge, MA 02139, USA.
  * 
  * For more information, visit the XMLVM Home Page at http://www.xmlvm.org
  */
 
 package org.xmlvm.demo.java.photovm.ui;
 
 import org.xmlvm.demo.java.photovm.data.Photo;
 import org.xmlvm.demo.java.photovm.event.UpdatePhotosListener;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Panel;
 import java.awt.ScrollPane;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 /**
  * Displays a grid of thumbnails. The number of columns is automatically adapted
  * to the available horizontal space.
  * 
  * @author haeberling@google.com (Sascha Haeberling)
  * 
  */
 public class ThumbnailGridPanel extends ScrollPane implements UpdatePhotosListener {
   private Panel contentWrapperPanel = new Panel();
   private Panel contentPanel = new Panel();
   private final GridLayout layout = new GridLayout(0, 5);
   private int thumbnailWidth = 1;
 
   public ThumbnailGridPanel() {
     setBackground(Color.BLACK);
     contentWrapperPanel.setBackground(Color.BLACK);
     contentPanel.setBackground(Color.BLACK);
     contentWrapperPanel.setLayout(new BorderLayout());
     add(contentWrapperPanel);
   }
 
   @Override
   public void doLayout() {
     int thumbnailsPerRow = (getViewportSize().width - 20) / thumbnailWidth;
     layout.setColumns(Math.max(thumbnailsPerRow, 1));
     contentPanel.doLayout();
     super.doLayout();
   }
 
   public void updatePhotos(List<Photo> photos) {
     contentWrapperPanel.removeAll();
     contentPanel = new Panel();
     contentPanel.setLayout(layout);
     for (Photo photo : photos) {
       // Update to highest width value.
       thumbnailWidth =
           (photo.getThumbnailWidth() > thumbnailWidth) ? photo
               .getThumbnailWidth() : thumbnailWidth;
       ImagePanel thumbnail;
       try {
         thumbnail = new ImagePanel(new URL(photo.getThumbnailUrl()));
         thumbnail.setPreferredSize(new Dimension(photo.getThumbnailWidth(),
             photo.getThumbnailHeight()));
         contentPanel.add(thumbnail);
       } catch (MalformedURLException e) {
         e.printStackTrace();
       }
     }
     contentWrapperPanel.add(contentPanel, BorderLayout.NORTH);
     doLayout();
    contentPanel.repaint();
   }
 }
