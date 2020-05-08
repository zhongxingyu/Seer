 package plugins.adufour.ezplug;
 
 import icy.gui.component.IcyLogo;
 import icy.gui.frame.IcyExternalFrame;
 import icy.gui.frame.IcyFrame;
 import icy.gui.frame.IcyInternalFrame;
 import icy.gui.util.GuiUtil;
 import icy.main.Icy;
 import icy.system.IcyHandledException;
 import icy.system.thread.ThreadUtil;
 
 import java.awt.AlphaComposite;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dialog.ModalityType;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.RoundRectangle2D;
 import java.io.File;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JInternalFrame;
 import javax.swing.JPanel;
 
 import org.pushingpixels.substance.api.ComponentState;
 import org.pushingpixels.substance.api.SubstanceColorScheme;
 import org.pushingpixels.substance.api.SubstanceLookAndFeel;
 
 import plugins.adufour.ezplug.EzGroup.FoldListener;
 
 public class EzDialog extends IcyFrame implements FoldListener
 {
     public static final int      FONT_SIZE             = 16;
     
     private static final boolean USE_SKIN_COLOR_SCHEME = true;
     
     protected static File        currentParametersPath = null;
     
     protected Color              logoTitleColor;
     
     private JPanel               jPanelParameters;
     
     private final List<Object>   components            = new ArrayList<Object>();
     
     private JDialog              dialog;
     
     private boolean              isDialog              = false;
     
     public EzDialog(String title)
     {
         this(title, true);
     }
     
     protected EzDialog(String title, final boolean isDialog)
     {
         super(title, true, true, false, true);
         
         this.isDialog = isDialog;
         
         ThreadUtil.invokeNow(new Runnable()
         {
             public void run()
             {
                 jPanelParameters = new JPanel(new GridBagLayout());
                 jPanelParameters.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
                 
                 if (isDialog)
                 {
                     dialog = new JDialog();
                     dialog.setTitle(getTitle());
                     dialog.setLayout(new BorderLayout());
                     dialog.add(jPanelParameters, BorderLayout.CENTER);
                     dialog.setModalityType(ModalityType.APPLICATION_MODAL);
                     dialog.setLocationRelativeTo(Icy.getMainInterface().getMainFrame().getDesktopPane());
                     dialog.pack();
                 }
                 else
                 {
                     getContentPane().add(jPanelParameters, BorderLayout.CENTER);
                     pack();
                     setOptimalLocation();
                 }
             }
         });
     }
     
     protected void setOptimalLocation()
     {
         // place the window beside other EzPlugs
         Point location = new Point();
         
         if (isExternalized())
         {
             location = Icy.getMainInterface().getMainFrame().getLocationOnScreen();
             location.y += Icy.getMainInterface().getMainFrame().getHeight();
         }
         
         boolean validLocation = false;
         
         Dimension screenDim = getToolkit().getScreenSize();
         
         loop: while (!validLocation)
         {
             AbstractList<? extends Component> comps;
             
             if (isInternalized())
             {
                 comps = new ArrayList<JInternalFrame>(Arrays.asList(Icy.getMainInterface().getDesktopPane().getAllFrames()));
             }
             else
             {
                 comps = Icy.getMainInterface().getExternalFrames();
             }
             
             for (Component gui : comps)
             {
                 if (this.getFrame() == gui || !gui.isShowing()) continue;
                 
                 // final Point wLoc = isInternalized() ? ezGUIs.get(gui) :
                 // gui.getFrame().getLocationOnScreen();
                 final Point wLoc = isInternalized() ? gui.getLocation() : gui.getLocationOnScreen();
                 
                 if (wLoc.distanceSq(location) < 400)
                 {
                     Dimension dim = gui.getPreferredSize();
                     location.x += dim.width;
                     if (location.x > screenDim.width - dim.width)
                     {
                         // no more space in x, move along y
                         location.x = 0;
                         location.y += dim.height;
                         
                         if (location.y > screenDim.height - dim.height)
                         {
                             // no more space on screen, break
                             location.x = 0;
                             location.y = 0;
                             break;
                         }
                     }
                     validLocation = false;
                     continue loop;
                 }
             }
             validLocation = true;
         }
         setLocation(location);
     }
     
     @Override
     protected IcyInternalFrame createInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable)
     {
         return new EzInternalFrame(title, resizable, closable, maximizable, iconifiable);
     }
     
     @Override
     protected IcyExternalFrame createExternalFrame(String title)
     {
         return new EzExternalFrame(title);
     }
     
     public void addComponent(Component component)
     {
         components.add(component);
     }
     
     public void addEzComponent(EzComponent component)
     {
         addEzComponent(component, true);
     }
     
     @SuppressWarnings("unchecked")
     protected void addEzComponent(EzComponent component, boolean isSingle)
     {
         // if the component is a group, add its internal components recursively
         if (component instanceof EzGroup)
         {
             EzGroup group = (EzGroup) component;
             
             group.addFoldListener(this);
             
             for (EzComponent groupComponent : group)
                 addEzComponent(groupComponent, false);
         }
         
         // set the parent UI of this component
         if (component instanceof EzComponent) component.setUI(this);
         
         // if the component is an EzVar, activate the internal listener
         // => this will cause the UI to refresh if a var gets shown/hidden
         if (component instanceof EzVar)
         {
             @SuppressWarnings("rawtypes")
             EzVar var = (EzVar) component;
             var.variable.addListener(var);
         }
         
         if (isSingle) components.add(component);
     }
     
     @Override
     public void foldStateChanged(boolean state)
     {
         repack(true);
     }
     
     /**
      * Re-packs the user interface. This method should be called if one of the components was
     * changed either in size or visibility state
      * 
      * @param updateParametersPanel
      *            Set to true if the visibility of some parameters has been changed and the panel
      *            should be redrawn before re-packing the frame
      */
     public void repack(boolean updateParametersPanel)
     {
         if (updateParametersPanel)
         {
             jPanelParameters.removeAll();
             
             for (Object object : components)
                 if (object instanceof EzComponent)
                 {
                     ((EzComponent) object).addToContainer(jPanelParameters);
                 }
                 else if (object instanceof Component)
                 {
                     Component component = (Component) object;
                     
                     GridBagLayout gridbag = (GridBagLayout) jPanelParameters.getLayout();
                     
                     GridBagConstraints gbc = new GridBagConstraints();
                     gbc.insets = new Insets(2, 5, 2, 5);
                     gbc.fill = GridBagConstraints.BOTH;
                     gbc.gridwidth = GridBagConstraints.REMAINDER;
                     // resize behavior
                     gbc.weightx = 1;
                     gbc.weighty = 1;
                     
                     component.setFocusable(false);
                     gridbag.setConstraints(component, gbc);
                     
                     jPanelParameters.add(component);
                 }
                 else throw new UnsupportedOperationException("Cannot add a " + object.getClass().getSimpleName() + " to the graphical interface");
             
             jPanelParameters.validate();
             jPanelParameters.repaint();
         }
         
         if (isDialog)
         {
             dialog.getContentPane().validate();
             dialog.getContentPane().repaint();
             dialog.pack();
         }
         else
         {
             pack();
         }
     }
     
     /**
      * Highlights the plug's title bar
      * 
      * @param state
      */
     public void setHighlightedState(boolean state)
     {
         JPanel jPanel = (JPanel) getContentPane().getComponent(0);
         
         for (Component component : jPanel.getComponents())
         {
             if (!(component instanceof IcyLogo)) continue;
             
             IcyLogo logo = (IcyLogo) component;
             Component logoTitle = logo.getComponent(1);
             
             if (state)
             {
                 logoTitleColor = logoTitle.getForeground();
                 logoTitle.setForeground(Color.cyan);
             }
             else
             {
                 logoTitle.setForeground(logoTitleColor);
                 
             }
             logoTitle.repaint();
         }
     }
     
     /**
      * Shows the dialog on the screen and returns only when the dialog is closed (either via the
      * close button, or by calling the {@link #hideDialog()} method). By default, the dialog is
      * modal (other windows are blocked until this dialog is closed)
      */
     public void showDialog()
     {
         showDialog(true);
     }
     
     /**
      * Shows the dialog on the screen and returns only when the dialog is closed (either via the
      * close button, or by calling the {@link #hideDialog()} method)
      * 
      * @param modal
      *            <code>true</code> if the dialog should block other windows until it is closed,
      *            <code>false</code> otherwise
      */
     public void showDialog(final boolean modal)
     {
         if (!isDialog) throw new IcyHandledException("Cannot show an IcyFrame as a Dialog. Use setVisible(true) instead.");
         
         ThreadUtil.invokeNow(new Runnable()
         {
             public void run()
             {
                 repack(true);
                 dialog.setModal(modal);
                 dialog.setVisible(true);
             }
         });
     }
     
     @Override
     public void close()
     {
         if (isDialog)
             hideDialog();
         else super.close();
     }
     
     /**
      * Closes the dialog
      */
     public void hideDialog()
     {
         if (!isDialog) return;
         
         ThreadUtil.invokeNow(new Runnable()
         {
             public void run()
             {
                 dialog.setVisible(false);
             }
         });
     }
     
     public void onClosed()
     {
         // dispose all components
         
         for (Object object : components)
             if (object instanceof EzComponent)
             {
                 ((EzComponent) object).dispose();
             }
         
         components.clear();
         
         jPanelParameters.removeAll();
         
         super.onClosed();
     }
     
     // Title pane creation
     
     public static void paintTitlePane(Graphics2D graphics, int width, int height, String title, boolean internal)
     {
         paintBackground(width, height, internal, graphics);
         
         if (USE_SKIN_COLOR_SCHEME)
         {
             SubstanceColorScheme colors = SubstanceLookAndFeel.getCurrentSkin().getColorScheme(new JButton(), ComponentState.PRESSED_SELECTED);
             graphics.setColor(colors.isDark() ? Color.white : colors.getUltraDarkColor().darker().darker());
         }
         else
         {
             graphics.setColor(Color.white);
         }
         
         graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         
         FontMetrics m = graphics.getFontMetrics();
         int titleWidth = m.stringWidth(title);
         int titleHeight = m.getHeight() - 6;
         
         graphics.drawString(title, (width - titleWidth) / 2, (height + titleHeight) / 2);
         // graphics.dispose();
     }
     
     /**
      * Modified version of the {@link GuiUtil#paintBackGround(int, int, Graphics)} method for the
      * purpose of EzGUI. Changes include:<br>
      * <ul>
      * <li>Shade and lighting effects adjusted</li>
      * <li>Border outline removed</li>
      * <li>Bottom corners of the rounded rectangle masked to better stick to the rest of the frame</li>
      * </ul>
      * 
      * @param width
      * @param height
      * @param g
      */
     private static void paintBackground(int width, int height, boolean internal, Graphics2D graphics)
     {
         graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         
         Color brightColor, darkColor;
         
         if (USE_SKIN_COLOR_SCHEME)
         {
             SubstanceColorScheme colors = SubstanceLookAndFeel.getCurrentSkin().getColorScheme(new JButton(), ComponentState.PRESSED_SELECTED);
             brightColor = colors.getUltraLightColor().brighter();
             darkColor = colors.getDarkColor();
         }
         else
         {
             brightColor = new Color(72, 72, 72);
             darkColor = new Color(4, 4, 4);
         }
         graphics.setPaint(new GradientPaint(0, 0, brightColor, 0, height, darkColor));
         
         if (internal)
         {
             // Fill a rounded rectangle with gradient paint
             float ray = Math.max(width, height) * 0.05f;
             float finalRay = Math.min(ray * 2, 20);
             graphics.fill(new RoundRectangle2D.Double(0, 0, width, height, finalRay, finalRay));
             // mask the bottom corners of the rounded rectangle
             graphics.fillRect(0, height / 2, width, height / 2);
         }
         else
         {
             graphics.fill(new Rectangle2D.Double(0, 0, width, height));
         }
         
         // add a bright oval portion to simulate a glass reflection
         graphics.setPaint(new GradientPaint(0, 0, brightColor, 0, height * 2, darkColor));
         graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
         graphics.fillOval(-width + (width / 2), height / 3, width * 2, height * 3);
         
         // graphics.dispose();
     }
 }
