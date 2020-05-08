 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.ui;
 
 import com.google.jstestdriver.SlaveBrowser;
 
 import java.awt.Dimension;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.imageio.ImageIO;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * @author alexeagle@google.com (Alex Eagle)
  */
 @SuppressWarnings("serial")
 public class CapturedBrowsersPanel extends JPanel implements Observer {
   private BrowserIcon chrome;
   private BrowserIcon ie;
   private BrowserIcon firefox;
 //  private BrowserIcon opera;
   private BrowserIcon safari;
   private JLabel chromeLabel;
   private JLabel ieLabel;
   private JLabel firefoxLabel;
 //  private JLabel operaLabel;
   private JLabel safariLabel;
 
   public CapturedBrowsersPanel() {
     try {
       chrome = BrowserIcon.buildFromResource("Chrome.png");
       ie = BrowserIcon.buildFromResource("IE.png");
       firefox = BrowserIcon.buildFromResource("Firefox.png");
 //      opera = BrowserIcon.buildFromResource("Opera.png");
       safari = BrowserIcon.buildFromResource("Safari.png");
     } catch (IOException e) {
       throw new RuntimeException();
     }
 
     setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
     chromeLabel = new JLabel(chrome.getGreyscaleIcon());
     ieLabel = new JLabel(ie.getGreyscaleIcon());
     firefoxLabel = new JLabel(firefox.getGreyscaleIcon());
 //    operaLabel = new JLabel(opera.getGreyscaleIcon());
     safariLabel = new JLabel(safari.getGreyscaleIcon());
     add(chromeLabel);
     add(ieLabel);
     add(firefoxLabel);
     // TODO(jeremie): Support opera?
     //add(operaLabel);
     add(safariLabel);
 
    Dimension minimumSize = new Dimension(chrome.getColorIcon().getIconWidth() * 4, chrome.getColorIcon().getIconHeight());
     setMinimumSize(minimumSize);
     setPreferredSize(minimumSize);
   }
 
   public void update(Observable observable, Object o) {
     SlaveBrowser slave = (SlaveBrowser) o;
     if (slave.getBrowserInfo().getName().contains("Firefox")) {
       firefoxLabel.setIcon(firefox.getColorIcon());
     } else if (slave.getBrowserInfo().getName().contains("Chrome")) {
       chromeLabel.setIcon(chrome.getColorIcon());
     } else if (slave.getBrowserInfo().getName().contains("Safari")) {
       safariLabel.setIcon(safari.getColorIcon());
     } else if (slave.getBrowserInfo().getName().contains("MSIE")) {
       ieLabel.setIcon(ie.getColorIcon());
     }
   }
 
   private static class BrowserIcon {
     private final BufferedImage color;
     private final BufferedImage greyscale;
     private ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
 
     public BrowserIcon(BufferedImage color) {
       this.color = color;
       greyscale = op.filter(color, null);
     }
 
     public static BrowserIcon buildFromResource(String resourceName) throws IOException {
       return new BrowserIcon(ImageIO.read(BrowserIcon.class.getResourceAsStream(resourceName)));
     }
 
     public Icon getColorIcon() {
       return new ImageIcon(color);
     }
 
     public Icon getGreyscaleIcon() {
       return new ImageIcon(greyscale);
     }
   }
 }
