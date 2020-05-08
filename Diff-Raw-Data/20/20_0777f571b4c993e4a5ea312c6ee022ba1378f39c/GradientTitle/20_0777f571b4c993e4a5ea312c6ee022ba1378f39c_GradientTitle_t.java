 package jDistsim.designer.ui.control;
 
 import jDistsim.designer.ui.UIConfiguration;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.resource.Resources;
 import jDistsim.utils.resource.TextResources;
import jDistsim.utils.ui.control.IconHoverStyle;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Rectangle2D;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 25.9.12
  * Time: 22:03
  */
 public class GradientTitle extends JComponent {
 
     private JComponent parentControl;
     private IconButton minimizeIconButton;
     private JLabel labelTitle;
     private Color color1;
     private Color color2;
 
     public GradientTitle(JComponent parentControl) {
         this(parentControl, TextResources.NULL_TEXT);
     }
 
     public GradientTitle(JComponent parentControl, String titleText) {
         this(parentControl, titleText, new Color(236, 236, 236), new Color(228, 228, 228));
     }
 
     public GradientTitle(JComponent parentControl, String titleText, Color color1, Color color2) {
         this.parentControl = parentControl;
         this.color1 = color1;
         this.color2 = color2;
         initializeComponents(titleText);
     }
 
     private void initializeComponents(String titleText) {
         setLayout(new BorderLayout());
         setBorder(new EmptyBorder(0, 20, 0, 10));
        setMinimumSize(new Dimension(100, 20));
 
         labelTitle = new JLabel();
         setTitleText(titleText);
         setTitleTextFont(UIConfiguration.getInstance().getDefaultFont());
         setTitleTextForeground(new Color(96, 96, 96));
 
        minimizeIconButton = new IconButton(new ImageIcon(Resources.getImage("system/toolbar-minimize-icon.png")), new IconHoverStyle(new ImageIcon(Resources.getImage("system/toolbar-minimize-icon-hover.png"))));
         minimizeIconButton.addMouseListener(new MouseAdapter() {
 
             @Override
             public void mousePressed(MouseEvent mouseEvent) {
                 iconButtonMousePressed(mouseEvent);
             }
         });
 
         add(minimizeIconButton, BorderLayout.EAST);
         add(labelTitle, BorderLayout.WEST);
     }
 
     public void setTitleText(String text) {
         labelTitle.setText(text);
     }
 
     public void setTitleTextFont(Font font) {
         labelTitle.setFont(font);
     }
 
     public void setTitleTextForeground(Color color) {
         labelTitle.setForeground(color);
     }
 
     private void iconButtonMousePressed(MouseEvent mouseEvent) {
         Logger.log("Minimize parent control");
         parentControl.setVisible(false);
     }
 
     @Override
     public Dimension getPreferredSize() {
        return new Dimension(parentControl.getWidth(), 20);
     }
 
     @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
 
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         GradientPaint gradientPaint = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
         g2.setPaint(gradientPaint);
         g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        graphics.setColor(new Color(192, 192, 192));
         g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
 
         g2.setColor(new Color(157, 157, 157));
         g2.drawRect(5, 5, 8, 8);
     }
 }
 
