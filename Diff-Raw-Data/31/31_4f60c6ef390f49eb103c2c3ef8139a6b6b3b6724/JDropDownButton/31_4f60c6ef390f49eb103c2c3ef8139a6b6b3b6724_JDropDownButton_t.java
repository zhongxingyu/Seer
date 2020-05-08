 // Copyright (C) 2005 Mammoth Software LLC
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 //
 // Contact the author at: info@mammothsoftware.com
 package javax.swing.origamist;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JToolBar;
 import javax.swing.border.Border;
 
 /**
  * A Drop Down Button.
  * 
  * @author m. bangham
  * @author Martin Pecka - added Javadoc and some minor modifications.
  *         Copyright 2005 Mammoth Software LLC
  */
 public class JDropDownButton extends JButton
 {
 
     /** */
     private static final long serialVersionUID   = 8583671910964778527L;
 
     protected JPopupMenu      popup              = new JPopupMenu();
     protected JToolBar        tb                 = new ToolBar();
     protected JRolloverButton mainButton;
     protected JRolloverButton arrowButton;
     protected ActionListener  mainButtonListener = new ActionListener() {
                                                      public void actionPerformed(ActionEvent e)
                                                      {
                                                          if (popup.getComponentCount() > 0) {
                                                              Component component = popup.getComponent(0);
                                                              if (component instanceof JMenuItem) {
                                                                  JMenuItem item = (JMenuItem) component;
                                                                  item.doClick(0);
                                                              }
                                                          }
                                                      }
                                                  };
 
     /**
      * Create a dropdown button with the given icon and size set to 25x25 for the icon and 11x11 for the arrow.
      * 
      * @param icon The icon to set.
      */
     public JDropDownButton(Icon icon)
     {
         this(icon, new Dimension(25, 25));
     }
 
     /**
      * Create a dropdown button with the given icon and size set to 25x25 for the icon and 11x11 for the arrow. Set the
      * size of the main button to <code>size</code>.
      * 
      * @param icon The icon to set.
      * @param size The size of the main button.
      */
     public JDropDownButton(Icon icon, Dimension size)
     {
         this();
         mainButton = new JRolloverButton(icon, size, false);
         arrowButton = new JRolloverButton(new DownArrow(), new Dimension(11, 11), false);
         init();
     }
 
     /**
      * Creates a dropdown button from the given <code>mainButton</code> and <code>arrowButton</code>.
      * 
      * @param mainButton The main button of this dropdown.
      * @param arrowButton The arrow button.
      */
     public JDropDownButton(JRolloverButton mainButton, JRolloverButton arrowButton)
     {
         this();
         this.mainButton = mainButton;
         this.arrowButton = arrowButton;
         init();
     }
 
     /**
      * Creates a dropdown button with the given <code>mainButton</code> and the default arrow button.
      * 
      * @param mainButton The main button of this dropdown.
      */
     public JDropDownButton(JRolloverButton mainButton)
     {
         this(mainButton, new JRolloverButton(new DownArrow(), new Dimension(11, 11), false));
     }
 
     /**
      * Creates a dropdown button with the given <code>mainButton</code> and the default arrow button.
      * 
      * @param mainButton The main button of this dropdown.
      */
     public JDropDownButton(AbstractButton mainButton)
     {
         this(new JRolloverButton(mainButton, false), new JRolloverButton(new DownArrow(), new Dimension(11, 11), false));
     }
 
     private JDropDownButton()
     {
         super();
         setBorder(null);
     }
 
     @Override
     public void setText(String text)
     {
         mainButton.setText(text);
         setFixedSize(mainButton, arrowButton);
     }
 
     /**
      * Returns <code>null</code> always. If you want to get the text of the <code>mainButton</code>, call
      * <code>getMainButton().getText()</code>.
      */
     @Override
     public String getText()
     {
         return null;
     }
 
     @Override
     public void setToolTipText(String text)
     {
         super.setToolTipText(text);
         mainButton.setToolTipText(text);
         arrowButton.setToolTipText(text);
     }
 
     /**
      * Returns <code>null</code> always. If you want to get the icon of the <code>mainButton</code>, call
      * <code>getMainButton().getIcon()</code>.
      */
     @Override
     public Icon getIcon()
     {
         return null;
     }
 
     @Override
     public void setIcon(Icon defaultIcon)
     {
         mainButton.setIcon(defaultIcon);
     }
 
     @Override
     public int getMnemonic()
     {
         if (mainButton != null)
             return mainButton.getMnemonic();
         else
             return super.getMnemonic();
     }
 
     @Override
     public void setMnemonic(int mnemonic)
     {
         if (mainButton != null)
             mainButton.setMnemonic(mnemonic);
         else
             super.setMnemonic(mnemonic);
     }
 
     @Override
     public void setEnabled(boolean enabled)
     {
         super.setEnabled(enabled);
         mainButton.setEnabled(enabled);
         arrowButton.setEnabled(enabled);
     }
 
     @Override
     public void setOpaque(boolean isOpaque)
     {
         super.setOpaque(isOpaque);
         if (mainButton != null)
             mainButton.setOpaque(isOpaque);
         if (arrowButton != null)
             arrowButton.setOpaque(isOpaque);
     }
 
     @Override
     public void updateUI()
     {
         super.updateUI();
         setBorder(null);
     }
 
     /**
      * @return the mainButton
      */
     public JButton getMainButton()
     {
         return mainButton;
     }
 
     /**
      * @return the arrowButton
      */
     public JButton getArrowButton()
     {
         return arrowButton;
     }
 
     /**
      * @return The border to be added to the button when the mouse is placed over the button.
      */
     protected Border getRolloverBorder()
     {
         return BorderFactory.createEmptyBorder();
     }
 
     /**
      * Add a mouse listener to the main and arrow buttons.
      */
     protected void initRolloverListener()
     {
         MouseListener l = new MouseAdapter() {
 
             @Override
             public void mouseEntered(MouseEvent e)
             {
                 mainButton.setRollover(true);
                 arrowButton.setRollover(true);
             }
 
             @Override
             public void mouseExited(MouseEvent e)
             {
                 mainButton.setRollover(false);
                 arrowButton.setRollover(false);
             }
         };
         mainButton.addMouseListener(l);
         arrowButton.addMouseListener(l);
     }
 
     /**
      * Initialize this button.
      */
     protected void init()
     {
         initRolloverListener();
 
         this.setAction(new PopupAction());
         mainButton.setAction(new PopupAction());
         // using setAction() would delete the arrow icon
         arrowButton.addActionListener(new PopupAction());
 
         super.setText(null);
         super.setIcon(null);
 
         arrowButton.setFocusable(false);
        setFocusable(true);

        // these two focus listeners are responsible for focusing only the mainButton while preserving isFocusable() for
        // the whole JDropDownButton
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e)
            {
                if (e.getOppositeComponent() != mainButton)
                    mainButton.requestFocusInWindow();
            }
        });
        // without this listener, it would be impossible to traverse focus back
        mainButton.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e)
            {
                setFocusable(true);
            }

            @Override
            public void focusGained(FocusEvent e)
            {
                setFocusable(false);
            }
        });
 
         mainButton.setRolloverBorder(null);
         arrowButton.setRolloverBorder(null);
 
         Icon disDownArrow = new DisabledDownArrow();
         arrowButton.setDisabledIcon(disDownArrow);
         arrowButton.setMaximumSize(new Dimension(11, 100));
         mainButton.setOpaque(isOpaque());
         arrowButton.setOpaque(isOpaque());
 
         setMargin(new Insets(0, 0, 0, 0));
 
         // Windows draws border around buttons, but not toolbar buttons
         // Using a toolbar keeps the look consistent.
         tb.setBorder(null);
         tb.setMargin(new Insets(0, 0, 0, 0));
         tb.setFloatable(false);
         tb.add(mainButton);
         tb.add(arrowButton);
         tb.setOpaque(false);
         add(tb);
 
         setFixedSize(mainButton, arrowButton);
 
     }
 
     /**
      * Forces the width of this button to be the sum of the widths of the main
      * button and the arrow button. The height is the max of the main button or
      * the arrow button.
      */
     protected void setFixedSize(JButton mainButton, JButton arrowButton)
     {
         int width = (int) (mainButton.getPreferredSize().getWidth() + arrowButton.getPreferredSize().getWidth());
         int height = (int) Math.max(mainButton.getPreferredSize().getHeight(), arrowButton.getPreferredSize()
                 .getHeight());
 
         setMaximumSize(new Dimension(width, height));
         setMinimumSize(new Dimension(width, height));
         setPreferredSize(new Dimension(width, height));
     }
 
     /**
      * Removes a component from the popup.
      * 
      * @param component The component to remove.
      */
     public void removeComponent(Component component)
     {
         popup.remove(component);
     }
 
     /**
      * Adds a component to the popup.
      * 
      * @param component The component to add.
      * @return The <code>component</code> argument.
      */
     public Component addComponent(Component component)
     {
         return popup.add(component);
     }
 
     /**
      * Indicates that the first item in the menu should be executed when the main button is clicked.
      * 
      * @param isRunFirstItem True for on, false for off.
      */
     public void setRunFirstItem(boolean isRunFirstItem)
     {
         if (isRunFirstItem) {
             mainButton.setAction(null);
             mainButton.addActionListener(mainButtonListener);
         }
     }
 
     public void setPopupVisible(boolean visible)
     {
         if (visible)
             getPopupMenu().show(this, 0, this.getHeight());
         else
             getPopupMenu().setVisible(false);
     }
 
     public JPopupMenu getPopupMenu()
     {
         return popup;
     }
 
     /**
      * Pops up the drop-down list.
      * 
      * @author Martin Pecka
      */
     private class PopupAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = 3939022490935483876L;
 
         @Override
         public void actionPerformed(ActionEvent ae)
         {
             setPopupVisible(true);
         }
     }
 
     private static class DownArrow implements Icon
     {
 
         Color arrowColor = Color.black;
 
         @Override
         public void paintIcon(Component c, Graphics g, int x, int y)
         {
             g.setColor(arrowColor);
             g.drawLine(x, y, x + 4, y);
             g.drawLine(x + 1, y + 1, x + 3, y + 1);
             g.drawLine(x + 2, y + 2, x + 2, y + 2);
         }
 
         @Override
         public int getIconWidth()
         {
             return 6;
         }
 
         @Override
         public int getIconHeight()
         {
             return 4;
         }
 
     }
 
     private static class DisabledDownArrow extends DownArrow
     {
 
         public DisabledDownArrow()
         {
             arrowColor = new Color(140, 140, 140);
         }
 
         @Override
         public void paintIcon(Component c, Graphics g, int x, int y)
         {
             super.paintIcon(c, g, x, y);
             g.setColor(Color.white);
             g.drawLine(x + 3, y + 2, x + 4, y + 1);
             g.drawLine(x + 3, y + 3, x + 5, y + 1);
         }
     }
 
     private static class ToolBar extends JToolBar
     {
 
         /** */
         private static final long serialVersionUID = -7995181550031357697L;
 
         @Override
         public void updateUI()
         {
             super.updateUI();
             setBorder(null);
         }
     }
 
 }
