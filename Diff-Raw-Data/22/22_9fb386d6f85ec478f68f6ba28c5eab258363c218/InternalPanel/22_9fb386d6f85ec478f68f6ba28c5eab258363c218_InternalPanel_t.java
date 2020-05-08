 package jDistsim.ui.panel;
 
 import jDistsim.ui.control.GradientTitle;
 import jDistsim.ui.control.IconBar;
 import jDistsim.utils.logging.Logger;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 28.9.12
  * Time: 17:27
  */
 public class InternalPanel extends JComponent {
 
     private int panelThickness = 2;
     private boolean visibleIconBar = false;
     private boolean showNothing = false;
     private boolean footerBorderLine = false;
    private boolean topBorderLine = false;
 
     public InternalPanel(String titleName) {
        this(titleName, false, false);
    }

    public InternalPanel(String titleName, boolean topBorderLine, boolean footerBorderLine) {
         Logger.log("Initialize internal panel -> " + titleName);
        setTopBorderLine(topBorderLine);
        setFooterBorderLine(footerBorderLine);

         initializeComponents(titleName);
     }
 
     public void showNothing() {
         showNothing = true;
         repaint();
     }
 
     public void hideNothing() {
         showNothing = false;
         repaint();
     }
 
     public void setFooterBorderLine(boolean footerBorderLine) {
         this.footerBorderLine = footerBorderLine;
        repaint();
    }

    public void setTopBorderLine(boolean topBorderLine) {
        this.topBorderLine = topBorderLine;
        repaint();
     }
 
     private void initializeComponents(String titleName) {
         setLayout(new BorderLayout());
         setBorder(new EmptyBorder(0, panelThickness, 0, panelThickness));
         JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        titlePanel.add(new GradientTitle(this, titleName, new Color(236, 236, 236), new Color(228, 228, 228), topBorderLine));
 
         if (visibleIconBar)
             titlePanel.add(new IconBar());
 
         add(titlePanel, BorderLayout.NORTH);
     }
 
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(200, 200);
     }
 
     @Override
     protected void paintComponent(Graphics graphics) {
         super.paintComponent(graphics);
         Graphics2D graphics2D = (Graphics2D) graphics;
 
         graphics2D.setColor(new Color(192, 192, 192));
         graphics2D.fillRect(0, 0, getWidth(), getHeight());
 
         graphics2D.setColor(Color.white);
         graphics2D.fillRect(panelThickness, 0, getWidth() - (2 * panelThickness), getHeight());
 
         if (showNothing) {
             Font titleFont = new Font("Calibri", Font.PLAIN, 12);
             graphics2D.setFont(titleFont);
             graphics2D.setColor(new Color(153, 153, 153));
             drawCenteredString("Nothing to show", getWidth(), getHeight(), graphics2D);
         }
 
         if (footerBorderLine) {
             graphics2D.setColor(new Color(192, 192, 192));
             graphics2D.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
         }

         // g.setBackgroundColor(new Color(192, 192, 192));
         // g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
     }
 
     public void drawCenteredString(String string, int width, int height, Graphics graphics) {
         FontMetrics fontMetrics = graphics.getFontMetrics();
         int x = (width - fontMetrics.stringWidth(string)) / 2;
         int y = (fontMetrics.getAscent() + (height - (fontMetrics.getAscent() + fontMetrics.getDescent())) / 2);
         graphics.drawString(string, x, y);
     }
 }
