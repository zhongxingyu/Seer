 /**
  * Copyright (C) 2013 Nicholas J. Little <arealityfarbetween@googlemail.com>
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package gui.components;
 
 import java.awt.BorderLayout;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import little.nj.gui.components.ImageListView;
 
 @SuppressWarnings("serial")
 public class ImagePanel extends JPanel {
 
     JButton       _extract;
 
     JButton       _set_covers;
 
     ImageListView content;
 
     JPanel        footer;
 
     public ImagePanel() {
         content = new ImageListView();
         footer = new JPanel();
         _extract = new JButton("Extract Images to...");
         _set_covers = new JButton("Set Cover & Thumbnail");
         init();
     }
 
     public ImageListView getImageView() {
         return content;
     }
 
     public void init() {
         setLayout(new BorderLayout());
         content.setModifiable(true);
         content.setMode(ImageListView.Mode.MULTI);
         footer.add(_extract);
         footer.add(_set_covers);
         content.addItemListener(new ItemListener() {
 
             @Override
             public void itemStateChanged(ItemEvent e) {
                 int sel = content.getSelectedItems().length;
                ImageListView.ImagePanel s = 
                        (ImageListView.ImagePanel) e.getItem();
                
                if (e.getStateChange() == ItemEvent.DESELECTED)
                     s.setText("");
                
                 if (sel > 0)
                     content.setText(content.getSelectedItems()[0],
                             sel < 2 ? "Cover & Thumbnail Image" : "Cover Image");
                 if (sel > 1)
                     content.setText(content.getSelectedItems()[1],
                             "Thumbnail Image");
             }
         });
        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.PAGE_END);
     }
 
     public void setCoversAction(Action a) {
         _set_covers.setAction(a);
     }
 
     public void setExtractAction(Action a) {
         _extract.setAction(a);
     }
 
     public void setImages(List<BufferedImage> list) {
         content.setList(list);
     }
 }
