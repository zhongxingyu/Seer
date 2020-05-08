 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 
 /**
  * A class to deal with the fact that a lot of the screens are going to be, to
  * some degree, based on a template so this removes a lot of code repetition.
  *
  * @author jonathanrainer
  */
 public class Template
 {
 
     /**
      * Code to load in the logos necessary for the template from external
      * sources.
      */
     ClassLoader cldr = this.getClass().getClassLoader();
     java.net.URL nwlogoBig = cldr.getResource("gui/nwlogo.png");
     java.net.URL nwlogoSmall = cldr.getResource("gui/nwsmalllogo.png");
     ImageIcon newWineLogoSmall = new ImageIcon(nwlogoSmall);
     ImageIcon newWineLogoBig = new ImageIcon(nwlogoBig);
 
     /**
      * Empty constructor, the object is only instantiated for the methods it
      * contains as opposed to being useful as an object in its own right.
      */
     public Template()
     {
     }
 
     /**
      * Creates a new JFrame based on the title provided, applies a layout, sets
      * size and adds the logo to it. This frame is then returned.
      *
      * @param title The title that you wish the JFrame to have.
      * @return A JFrame with the template applied to it, so other components can
      * be added on top.
      */
     public JFrame giveTemplatedJFrame(String title)
     {
         final JFrame templatedFrame = new JFrame(title);
        templatedFrame.setExtendedState(templatedFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
 
         Dimension frameDimensions = new Dimension(1024, 768);
         templatedFrame.setPreferredSize(frameDimensions);
 
         // Create the menu to go across the top of the frame.
         JMenuBar menuBar = new JMenuBar();
         JMenu fileMenu = new JMenu("File");
         JMenuItem quit = new JMenuItem("Quit");
         quit.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 System.exit(0);
             }
         });
         fileMenu.add(quit, -1);
         menuBar.add(fileMenu);
         templatedFrame.setJMenuBar(menuBar);
 
         return templatedFrame;
     }
 
     public JPanel giveMenuTemplatedJPanel(String title)
     {
         JPanel templatedPanel = new JPanel();
 
         GridBagLayout layout;
         layout = new GridBagLayout();
         templatedPanel.setLayout(layout);
 
         JLabel logo = new JLabel(newWineLogoBig);
         GridBagConstraints logoConstraints = createGridBagConstraints(
                 0, 0, GridBagConstraints.BOTH, 0, 0, new Insets(0, 0, 0, 0),
                 GridBagConstraints.CENTER, 0.0, 0.0, 1, 5);
         templatedPanel.add(logo, logoConstraints);
 
         return templatedPanel;
     }
 
     public JPanel giveFormTemplatedJPanel(String title)
     {
         JPanel templatedPanel = new JPanel();
 
         GridBagLayout layout;
         layout = new GridBagLayout();
         templatedPanel.setLayout(layout);
 
         JLabel logoLeft = new JLabel(newWineLogoSmall);
         JLabel logoRight = new JLabel(newWineLogoSmall);
         GridBagConstraints logoLeftConstraints = createGridBagConstraints(
                 0, 0, GridBagConstraints.NONE, 0, 0, new Insets(0, 0, 0, 0),
                 GridBagConstraints.WEST, 0.0, 0.0, 1, 1);
         GridBagConstraints logoRightConstraints = createGridBagConstraints(
                 4, 0, GridBagConstraints.NONE, 0, 0, new Insets(0, 0, 0, 0),
                 GridBagConstraints.EAST, 0.0, 0.0, 1, 1);
         templatedPanel.add(logoLeft, logoLeftConstraints);
         templatedPanel.add(logoRight, logoRightConstraints);
 
         JLabel titleLabel = new JLabel("<html><b><center><font size = \"50\">" + title);
         GridBagConstraints titleConstraints = createGridBagConstraints(
                 1, 0, GridBagConstraints.NONE, 0, 0, new Insets(0, 0, 15, 0),
                 GridBagConstraints.CENTER, 0.0, 0.0, 1, 3);
         templatedPanel.add(titleLabel, titleConstraints);
 
 
         return templatedPanel;
     }
 
     /**
      * This function removes the tedious process of setting each part of the
      * constraints on an objects placement in a GridBayLayout, this could be a
      * future point of weakness too if the implementation of GridBagLayout
      * changes but at least the changes are then confined to one function rather
      * than littered across the code. Refer to the documentation for
      * GridBagLayout to understand what each parameter does.
      *
      * @param gridx Omitted.
      * @param gridy Omitted.
      * @param fill Omitted.
      * @param ipadx Omitted.
      * @param ipady Omitted.
      * @param insets Omitted.
      * @param anchor Omitted.
      * @param weightx Omitted.
      * @param weighty Omitted.
      * @param gridheight Omitted.
      * @param gridwidth Omitted.
      * @return
      */
     public GridBagConstraints createGridBagConstraints(int gridx, int gridy,
             int fill, int ipadx, int ipady, Insets insets, int anchor,
             double weightx, double weighty, int gridheight, int gridwidth)
     {
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.gridx = gridx;
         constraints.gridy = gridy;
         constraints.fill = fill;
         constraints.ipadx = ipadx;
         constraints.ipady = ipady;
         constraints.insets = insets;
         constraints.anchor = anchor;
         constraints.weightx = weightx;
         constraints.weighty = weighty;
         constraints.gridheight = gridheight;
         constraints.gridwidth = gridwidth;
         return constraints;
     }
 
     /**
      * This takes in strings and a size and outputs the correctly formatted
      * string so that when it's used in a GUI it displays as a heading, akin to
      * HTML.
      *
      * @param input The string that will appear as a heading
      * @param size The size of the heading you want to appear. As always in HTML
      * checking is employed to make sure the number is between 1 and 6.
      * @return The input string, correctly formatted for display. If the number
      * entered for size was invalid then the original string is returned.
      */
     public String headingString(String input, int size)
     {
         // Check if the input size is between 1 and 6
         if (size <= 0 || size > 6)
         {
             return input;
         } else
         {
             return "<html><h" + size + "><b>" + input + "</b></h" + size
                     + "</html>";
         }
 
     }
 }
