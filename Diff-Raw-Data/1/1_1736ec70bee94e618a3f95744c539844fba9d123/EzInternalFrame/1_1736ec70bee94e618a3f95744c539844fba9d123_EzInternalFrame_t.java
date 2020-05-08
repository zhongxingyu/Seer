 package plugins.adufour.ezplug;
 
 import icy.gui.frame.IcyInternalFrame;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.LayoutManager;
 import java.awt.image.BufferedImage;
 import java.lang.reflect.Field;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JInternalFrame;
 
 import org.jdesktop.swingx.graphics.GraphicsUtilities;
 import org.jdesktop.swingx.graphics.ShadowRenderer;
 import org.pushingpixels.substance.internal.ui.SubstanceInternalFrameUI;
 import org.pushingpixels.substance.internal.utils.SubstanceInternalFrameTitlePane;
 
 public class EzInternalFrame extends IcyInternalFrame
 {
     private static final long    serialVersionUID = 1L;
 
     protected static final int   SHADOW_SIZE      = 4;
 
     protected static final int   BORDER_SIZE_X    = 8;
 
     protected static final int   BORDER_SIZE_Y    = 8;
 
     protected static final int   ARC_SIZE         = 20;
 
     private final ShadowRenderer renderer         = new ShadowRenderer(SHADOW_SIZE, 0.75f, Color.BLACK);
 
     private BufferedImage        shadow;
 
     EzInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable)
     {
         super(title, resizable, closable, maximizable, iconifiable);
 
         setOpaque(false);
         setBorder(BorderFactory.createEmptyBorder(SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE));
 
         updateUI();
     }
 
     @Override
     public void doLayout()
     {
         if (isVisible()) super.doLayout();
     }
 
     @Override
     public void dispose()
     {
         // FIXME Memory leak: "this" is not destroyed properly.
         // the if test is needed to avoid the following bug:
         // 1) externalize the frame
         // 2) close the external frame => this dispose is called once
         // 3) close ICY => this dispose is called again => not normal !
         // => the UI doens't know about the frame anymore
         if (isVisible())
         {
             getUI().uninstallUI(this);
         }
         super.dispose();
     }
 
     private final class EzInternalFrameUI extends SubstanceInternalFrameUI
     {
         public EzInternalFrameUI()
         {
             super(EzInternalFrame.this);
         }
 
         @Override
         protected JComponent createNorthPane(JInternalFrame w)
         {
             // Access the private field "titlePane" via reflection
 
             try
             {
                 Field titlePane = null;
                 titlePane = SubstanceInternalFrameUI.class.getDeclaredField("titlePane");
                 titlePane.setAccessible(true);
                 titlePane.set(this, new EzInternalFrameTitlePane());
                 return (SubstanceInternalFrameTitlePane) titlePane.get(this);
             }
             catch (SecurityException e)
             {
                 e.printStackTrace();
             }
             catch (NoSuchFieldException e)
             {
                 e.printStackTrace();
             }
             catch (IllegalArgumentException e)
             {
                 e.printStackTrace();
             }
             catch (IllegalAccessException e)
             {
                 e.printStackTrace();
             }
 
             return null;
         }
     }
 
     /**
      * Custom title pane with elegant logo and title
      * 
      * @author Alexandre Dufour
      * 
      */
     private final class EzInternalFrameTitlePane extends SubstanceInternalFrameTitlePane
     {
         private static final long serialVersionUID = 1L;
 
         // final Icon icon = frame.getFrameIcon();
         // final Point iconLocation = new Point(5, (EzGUI.LOGO_HEIGHT / 2) - (icon.getIconHeight() /
         // 2));
 
         public EzInternalFrameTitlePane()
         {
             super(EzInternalFrame.this);
 
             setFont(getFont().deriveFont(Font.BOLD + Font.ITALIC, EzGUI.FONT_SIZE));
 
             FontMetrics m = getFontMetrics(getFont());
 
             int titleWidth = m.stringWidth(EzInternalFrame.this.getTitle());
 
             setPreferredSize(new Dimension(titleWidth + 100, EzGUI.LOGO_HEIGHT));
         }
 
         @Override
         protected LayoutManager createLayout()
         {
             return new EzTitlePaneLayout();
         }
 
         @Override
         public void paintComponent(Graphics g)
         {
             Graphics2D g2d = (Graphics2D) g.create();
            g2d.setFont(getFont().deriveFont(Font.BOLD + Font.ITALIC, EzGUI.FONT_SIZE));
             EzGUI.paintTitlePane(g2d, getWidth(), getHeight(), EzInternalFrame.this.getTitle(), true);
 
             // paint the icon manually, as it is not the default for internal frames
             // icon.paintIcon(frame, g, iconLocation.x, iconLocation.y);
         }
 
         /**
          * Layout manager for this title pane. Patched version of SubstanceTitlePaneLayout to adjust
          * the buttons position (they stick tighter in the upper right-hand corner)
          * 
          * @author Kirill Grouchnikov
          * @author Alexandre Dufour
          */
         protected class EzTitlePaneLayout extends SubstanceInternalFrameTitlePane.SubstanceTitlePaneLayout
         {
             @Override
             public void layoutContainer(Container c)
             {
                 boolean leftToRight = getComponentOrientation().isLeftToRight();
 
                 int w = getWidth();
                 int x;
                 int y = 4;
                 int spacing;
 
                 // assumes all buttons have the same dimensions
                 // these dimensions include the borders
                 int buttonHeight = closeButton.getIcon().getIconHeight();
                 int buttonWidth = closeButton.getIcon().getIconWidth();
 
                 spacing = 5;
                 x = leftToRight ? spacing : w - 16 - spacing;
                 menuBar.setBounds(x, y, 16, 16);
 
                 x = leftToRight ? w : 0;
 
                 if (isClosable())
                 {
                     spacing = 4;
                     x += leftToRight ? -spacing - buttonWidth : spacing;
                     closeButton.setBounds(x, y, buttonWidth, buttonHeight);
                     if (!leftToRight) x += buttonWidth;
                 }
 
                 if (isMaximizable())
                 {
                     spacing = isClosable() ? 2 : 4;
                     x += leftToRight ? -spacing - buttonWidth : spacing;
                     maxButton.setBounds(x, y, buttonWidth, buttonHeight);
                     if (!leftToRight) x += buttonWidth;
                 }
 
                 if (isIconifiable())
                 {
                     spacing = isMaximizable() ? 2 : (isClosable() ? 2 : 4);
                     x += leftToRight ? -spacing - buttonWidth : spacing;
                     iconButton.setBounds(x, y, buttonWidth, buttonHeight);
                     if (!leftToRight) x += buttonWidth;
                 }
             }
         }
 
     }
 
     @Override
     public void paint(Graphics g)
     {
         if (shadow != null) ((Graphics2D) g).drawImage(shadow, 0, 0, null);
         super.paint(g);
     }
 
     @Override
     public void setBounds(int x, int y, int width, int height)
     {
         if (width != getWidth() || height != getHeight())
         {
             shadow = GraphicsUtilities.createCompatibleTranslucentImage(width, height);
             Graphics2D g2 = shadow.createGraphics();
             g2.setColor(Color.WHITE);
             g2.fillRoundRect(SHADOW_SIZE / 2, SHADOW_SIZE, 2 + width - SHADOW_SIZE * 3, height - SHADOW_SIZE * 3, ARC_SIZE, ARC_SIZE);
             g2.fillRect(SHADOW_SIZE / 2, SHADOW_SIZE + height / 2, 2 + width - SHADOW_SIZE * 3, height / 2 - SHADOW_SIZE * 3);
             g2.dispose();
             shadow = renderer.createShadow(shadow);
         }
 
         super.setBounds(x, y, width, height);
     }
 
     @Override
     public void updateUI()
     {
         SubstanceInternalFrameUI ui = new EzInternalFrameUI();
         setUI(ui);
         updateTitlePane(ui.getTitlePane());
     }
 }
