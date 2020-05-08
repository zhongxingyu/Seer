 /*
  * This file is part of WebLookAndFeel library.
  *
  * WebLookAndFeel library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * WebLookAndFeel library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.*/
 package fr.free.movierenamer.ui.panel;
 
 import com.alee.laf.StyleConstants;
 import com.alee.laf.scroll.WebScrollBarUI;
 import com.alee.laf.scroll.WebScrollPane;
 import com.alee.utils.ImageUtils;
 import com.alee.utils.LafUtils;
 import com.alee.utils.SwingUtils;
 import com.alee.utils.swing.Timer;
 import fr.free.movierenamer.ui.bean.UIMediaImage;
 import fr.free.movierenamer.ui.swing.SpinningDial;
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.geom.Area;
 import java.awt.geom.RoundRectangle2D;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 /**
  * User: mgarin Date: 05.09.11 Time: 15:45
  * Source : com.alee.extended.image.WebImageGallery.java
  */
 /**
  * Class CustomWebImageGallery
  *
  *
  */
 public class CustomWebImageGallery extends JComponent {
 
   private static final long serialVersionUID = 1L;
   private int spacing = 20;
   private int borderWidth = 3;
   private int maxWidth = 0;
   private int maxHeight = 0;
   private List<UIMediaImage> images = new ArrayList<UIMediaImage>();
   private int preferredColumnCount = 4;
   private boolean scrollOnSelection = true;
   private int selectedIndex = -1;
   private int oldSelectedIndex = -1;
   private float progress = 0f;
   private Timer reflectionMover = null;
   private WebScrollPane view;
   private PropertyChangeSupport propertyChange;
   private boolean showFlag;
   private final Dimension imgDim;
 
   public CustomWebImageGallery(boolean showFlag, Dimension imgDim) {
     super();
     this.showFlag = showFlag;
     this.imgDim = imgDim;
     propertyChange = new PropertyChangeSupport(selectedIndex);
 
     SwingUtils.setOrientation(this);
     setFocusable(true);
     setFont(new JLabel().getFont().deriveFont(Font.BOLD));
 
     MouseAdapter mouseAdapter = new MouseAdapter() {
       @Override
       public void mousePressed(MouseEvent e) {
         if (SwingUtilities.isLeftMouseButton(e)) {
           CustomWebImageGallery.this.requestFocusInWindow();
           for (int i = 0; i < images.size(); i++) {
             if (getImageRect(i).contains(e.getPoint())) {
               setSelectedIndex(i);
               break;
             }
           }
         }
       }
 
       @Override
       public void mouseWheelMoved(MouseWheelEvent e) {
         int index = getSelectedIndex();
         int maxIndex = images.size() - 1;
         int wheelRotation = e.getWheelRotation();
         int newIndex;
         if (wheelRotation > 0) {
           newIndex = index + wheelRotation;
           while (newIndex > maxIndex) {
             newIndex -= images.size();
           }
         } else {
           newIndex = index + wheelRotation;
           while (newIndex < 0) {
             newIndex += images.size();
           }
         }
         setSelectedIndex(newIndex);
       }
     };
 
     addMouseListener(mouseAdapter);
     addMouseWheelListener(mouseAdapter);
 
     addKeyListener(new KeyAdapter() {
       @Override
       public void keyPressed(KeyEvent e) {
         if (images.size() > 0) {
           int si = getSelectedIndex();
           if (e.getKeyCode() == KeyEvent.VK_LEFT) {
             setSelectedIndex(si == -1 || si == 0 ? images.size() - 1 : si - 1);
           } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
             setSelectedIndex(si == -1 || si == images.size() - 1 ? 0 : si + 1);
           } else if (e.getKeyCode() == KeyEvent.VK_HOME) {
             setSelectedIndex(0);
           } else if (e.getKeyCode() == KeyEvent.VK_END) {
             setSelectedIndex(images.size() - 1);
           }
         }
       }
     });
   }
 
   public PropertyChangeSupport getPropertyChange() {
     return propertyChange;
   }
 
   public List<UIMediaImage> getImages() {
     return Collections.unmodifiableList(images);
   }
 
   public int getImagesSize() {
     return images.size();
   }
 
   public int getPreferredColumnCount() {
     return preferredColumnCount;
   }
 
   public void setPreferredColumnCount(int preferredColumnCount) {
     this.preferredColumnCount = preferredColumnCount;
   }
 
   public WebScrollPane getView() {
     return getView(true);
   }
 
   public WebScrollPane getView(boolean withBorder) {
     if (view == null) {
       view = new WebScrollPane(CustomWebImageGallery.this, withBorder) {
         @Override
         public Dimension getPreferredSize() {
           int columns = Math.min(images.size(), preferredColumnCount);
           return new Dimension(spacing * (columns + 1) + columns * maxWidth,
                   CustomWebImageGallery.this.getPreferredSize().height + WebScrollBarUI.LENGTH);
         }
       };
       view.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
       view.setVerticalScrollBarPolicy(WebScrollPane.VERTICAL_SCROLLBAR_NEVER);
 
       InputMap im = view.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
       im.put(KeyStroke.getKeyStroke("UP"), "none");
       im.put(KeyStroke.getKeyStroke("DOWN"), "none");
       im.put(KeyStroke.getKeyStroke("LEFT"), "none");
       im.put(KeyStroke.getKeyStroke("RIGHT"), "none");
     }
     return view;
   }
 
   public boolean isScrollOnSelection() {
     return scrollOnSelection;
   }
 
   public void setScrollOnSelection(boolean scrollOnSelection) {
     this.scrollOnSelection = scrollOnSelection;
   }
 
   public UIMediaImage getSelectedImage() {
     if (selectedIndex != -1 && selectedIndex < images.size()) {
       return images.get(selectedIndex);
     }
     return null;
   }
 
   public int getSelectedIndex() {
     return selectedIndex;
   }
 
   public void setSelectedIndex(int selectedIndex) {
     if (this.selectedIndex == selectedIndex) {
       return;
     }
 
     this.oldSelectedIndex = this.selectedIndex;
     this.selectedIndex = selectedIndex;
 
     propertyChange.firePropertyChange("selectedImage", oldSelectedIndex, selectedIndex);
 
     repaint();

     if (scrollOnSelection) {
       Rectangle rect = getImageRect(selectedIndex);
       SwingUtils.scrollSmoothly(getView(), rect.x + rect.width / 2 - CustomWebImageGallery.this.getVisibleRect().width / 2, rect.y);
     }
 
     moveReflection();
   }
 
   private void moveReflection() {
     if (reflectionMover != null && reflectionMover.isRunning()) {
       reflectionMover.stop();
     }
 
     progress = 0f;
     reflectionMover = new Timer("CustomWebImageGallery.reflectionMoveTimer", StyleConstants.fastAnimationDelay, new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         if (progress < 1f) {
           progress += 0.08f;
           progress = Math.min(progress, 1f);
           CustomWebImageGallery.this.repaint();
         } else {
           reflectionMover.stop();
         }
       }
     });
     reflectionMover.start();
   }
 
   public Rectangle getImageRect(int index) {
     int iconWidth = imgDim.width;
     int iconHeight = imgDim.height;
     Dimension ps = getPreferredSize();
     int x = (getWidth() > ps.width ? (getWidth() - ps.width) / 2 : 0) + spacing
             + (maxWidth + spacing) * index + maxWidth / 2;
     int y = getHeight() / 2 - spacing / 2 - iconHeight / 2;
     return new Rectangle(x - iconWidth / 2, y - iconHeight / 2, iconWidth, iconHeight);
   }
 
   public void addImages(List<UIMediaImage> uiimages) {
     for (UIMediaImage image : uiimages) {
       images.add(images.size(), image);
     }
 
     if (images.size() > 0) {
       setSelectedIndex(0);
     }
 
     recalcualteMaxSizes();
     updateContainer();
   }
 
   public void setImage(Icon icon, int index) {
     try {
       UIMediaImage uiimg = images.get(index);
       uiimg.setIcon(icon);
       images.set(index, uiimg);
     } catch (Throwable e) {
       // Out of memory
       return;
     }
 
     recalcualteMaxSizes();
     updateContainer();
   }
 
   public void removeImage(UIMediaImage image) {
     if (images.contains(image)) {
       removeImage(images.indexOf(image));
     }
   }
 
   public void removeImage(int index) {
     if (index >= 0 && index < images.size()) {
       boolean wasSelected = getSelectedIndex() == index;
 
       images.remove(index);
 
       recalcualteMaxSizes();
       updateContainer();
 
       if (wasSelected && images.size() > 0) {
         setSelectedIndex(index < images.size() ? index : index - 1);
       }
     }
   }
 
   public void removeAllImages() {
     images.clear();
    selectedIndex = -1;
 
     recalcualteMaxSizes();
     updateContainer();
   }
 
   private void updateContainer() {
     if (getParent() instanceof JComponent) {
       ((JComponent) getParent()).revalidate();
     }
     repaint();
   }
 
   private void recalcualteMaxSizes() {
     for (UIMediaImage icon : images) {
       maxWidth = Math.max(maxWidth, imgDim.width);
       maxHeight = Math.max(maxHeight, imgDim.height);
     }
   }
 
   @Override
   protected void paintComponent(final Graphics g) {
     super.paintComponent(g);
 
     int height = getHeight();
 
     Graphics2D g2d = (Graphics2D) g;
     LafUtils.setupAntialias(g2d);
 
     Rectangle vr = getVisibleRect();
     Dimension ps = getPreferredSize();
     Composite oldComposite = g2d.getComposite();
     for (int i = 0; i < images.size(); i++) {
       if (!getImageRect(i).intersects(vr)) {
         continue;
       }
 
       int imageWidth = imgDim.width;
       int imageHeight = imgDim.height;
 
       int x = (getWidth() > ps.width ? (getWidth() - ps.width) / 2 : 0) + spacing
               + (maxWidth + spacing) * i + maxWidth / 2;
       int y = spacing / 2;
       int y2 = height * (4 / 5) + spacing / 2 + imageHeight / 2;
 
       // Initial image
       UIMediaImage uiImage = images.get(i);
       Icon flag = uiImage.getImagelang().getIcon();
 
       float add = selectedIndex == i ? progress * 0.4f : (oldSelectedIndex == i ? 0.4f - progress * 0.4f : 0);
       g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f + add));
 
       if (showFlag) { // Flag
         g2d.drawImage(ImageUtils.getBufferedImage(flag), x - flag.getIconWidth() / 2, y, null);
       }
 
       Icon icon = images.get(i).getIcon();
 
       if (icon instanceof SpinningDial) {
         icon.paintIcon(this, g2d, x - 32 / 2, y + spacing / 2 + borderWidth * 2 + flag.getIconHeight());
       } else {
         BufferedImage bi = ImageUtils.getBufferedImage((ImageIcon) icon);
         g2d.drawImage(bi, x - imageWidth / 2, y + spacing / 2 + borderWidth * 2 + flag.getIconHeight(), null);// Image
       }
 
       g2d.setPaint(selectedIndex == i ? Color.BLACK : Color.GRAY);
       Area gp = new Area(new RoundRectangle2D.Double(x - imageWidth / 2 - borderWidth, y + flag.getIconHeight() + borderWidth + spacing / 2,
               imageWidth + borderWidth * 2, imageHeight + borderWidth * 2, borderWidth * 2, borderWidth * 2));
       gp.subtract(new Area(new Rectangle(x - imageWidth / 2, y + spacing / 2 + flag.getIconHeight() + borderWidth * 2, imageWidth, imageHeight)));
       g2d.fill(gp);
 
       g2d.setComposite(oldComposite);
 
       // Info text
       if (selectedIndex == i || oldSelectedIndex == i) {
         float opacity = selectedIndex == i ? progress : 1f - progress;
         g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
         g2d.setPaint(Color.BLACK);
         String infoText = uiImage.getInfo().getWidth() + " x " + uiImage.getInfo().getHeight();
         g2d.drawString(infoText, x - g2d.getFontMetrics().stringWidth(infoText) / 2,
                 height / 2 + spacing / 2 + g2d.getFontMetrics().getAscent() / 2);
         g2d.setComposite(oldComposite);
       }
     }
   }
 
   @Override
   public Dimension getPreferredSize() {
     return new Dimension(spacing * (images.size() + 1) + maxWidth * images.size(), spacing * 3 + maxHeight * 2);
   }
 }
