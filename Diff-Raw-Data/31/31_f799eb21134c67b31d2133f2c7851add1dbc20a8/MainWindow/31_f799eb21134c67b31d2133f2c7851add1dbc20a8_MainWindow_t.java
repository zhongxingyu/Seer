 /*
  * Copyright (C) 2009  Lars Pötter <Lars_Poetter@gmx.de>
  * All Rights Reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 package org.FriendsUnited.UserInterface;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.beans.EventHandler;
 import java.io.File;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.FriendsUnited.Defaults;
 import org.FriendsUnited.Util.Option.OptionCollection;
 import org.FriendsUnited.Util.Option.StringOption;
 import org.apache.log4j.Logger;
 
 /**
  * This is the Main Window of the User Interface.
  * The User Interface has to enable the User to:
  * - Configure the FriendsUnited Server(Network Layer FriendServer, Services)
  * - Read back status data of the Friends United Server ( -"- )
  * - use the Services in the FriendsUnited Network
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class MainWindow implements ActionListener, WindowListener
 {
 
     /** File Name if Message Bundle Files.
      * "res" + File.separator +
      */
     public static final String MESSAGE_BUNDLE_BASE_NAME = "MessagesBundle";
 
     /**
      *
      */
     public static final String LANGUAGE_MENUE_ITEM_PREFIX = "Lang_";
 
     private final Logger log = Logger.getLogger(this.getClass().getName());
     private final JFrame frame;
     private final OptionCollection cfg;
     private final ResourceBundle messages;
     private String Version;
     private NodeConnection NodeCon;
     private NodePanel ConnectedNode;
     private JPanel bg;
 
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from the
      * event-dispatching thread
      *
      * This function is called by Startup
      *
      * @param cfg Configuration
      */
     public static void createAndShowGUI(final OptionCollection cfg)
     {
         // Configuration
         final String LocalLanguage  = cfg.getString("LocalLanguage", "en");
         final String LocalCountry   = cfg.getString("LocalCountry", "US");
 
         // Specify the look and feel to use.  Valid values:
         // null (use the default), "Metal", "System", "Motif", "GTK+"
         final String LookAndFeel = cfg.getString("LookAndFeel", "System");
 
         // Get default Locale
         final Locale currentLocale = new Locale(LocalLanguage, LocalCountry);
 
         try
         {
             final ResourceBundle messages = ResourceBundle.getBundle(
                                                MESSAGE_BUNDLE_BASE_NAME,
                                                currentLocale);
 
             // Create Class
             final MainWindow mw = new MainWindow(cfg, messages);
 
             // Make sure we have nice window decorations
             initLookAndFeel(LookAndFeel);
             JFrame.setDefaultLookAndFeelDecorated(true);
 
             // Create and set up the window.
             mw.createGui();
         }
         catch(final MissingResourceException e )
         {
             e.printStackTrace();
             System.out.println("ERROR : Did not find the ressouce bundle !("
                                + MESSAGE_BUNDLE_BASE_NAME + ")"
                                + currentLocale.toString());
             System.exit(1);
             return;
         }
     }
 
     /**
      *
      */
     public MainWindow(final OptionCollection cfg, final ResourceBundle messages)
     {
         this.cfg = cfg;
         this.messages = messages;
         this.Version = (Package.getPackage("org.FriendsUnited.UserInterface")).getImplementationVersion();
         if( null == Version)
         {
             // Executed  without Jar File
             Version = "(Development Version)";
         }
         frame = new JFrame("Friends United User Interface " + Version);
         // frame.setIconImage(new ImageIcon(imgURL).getImage()); TODO
         frame.addWindowListener(this);
     }
 
     public final void createGui()
     {
         // Create the menu bar.
         final JMenuBar menuBar = new JMenuBar();
 
 // Node Menu Start
         // Build the Node Menu
         final JMenu Nodemenu = new JMenu(messages.getString("Nodemenu"));
         Nodemenu.setMnemonic(KeyEvent.VK_N);
         menuBar.add(Nodemenu);
 
         // OptionCollection nodesCfg = cfg.getSubSectionCreateIfAbsent("Nodes");
 
         // TODO create Menu Items for each saved Node Connection in nodesCfg
 
         Nodemenu.addSeparator();
         // MenuItems of the Node Menu
         final JMenuItem ConnectNodeMenuItem = new JMenuItem(
                 messages.getString("ConnectNodeMenuItem")
                 /* ,KeyEvent.VK_T */
                 );
 
         // displayConnectNodeDialog
         ConnectNodeMenuItem.addActionListener(
         (ActionListener)EventHandler.create(
                               ActionListener.class,
 
                               this,
                               "displayConnectNodeDialog"));
 
         Nodemenu.add(ConnectNodeMenuItem);
 // Node Menu Stop
 
 // Language Menu Start
         // Build the Language menu.
         final JMenu Languagemenu = new JMenu(messages.getString("LanguageMenue"));
         Languagemenu.setMnemonic(KeyEvent.VK_L);
         menuBar.add(Box.createHorizontalGlue());
         menuBar.add(Languagemenu);
 
         // MenuItems of Language Menu
         addLanguages(Languagemenu);
         Languagemenu.addSeparator();
 
         final JMenuItem AboutMenuItem = new JMenuItem(
                                           messages.getString("AboutMenueItem")
                                           /* ,KeyEvent.VK_T */
                                           );
         //AboutMenuItem.addActionListener(mw);
 
         // displayAboutDialog
         AboutMenuItem.addActionListener(
                 (ActionListener)EventHandler.create(
                                                 ActionListener.class,
                                                 this,
                                                 "displayAboutDialog"));
 
         Languagemenu.add(AboutMenuItem);
 // Language Menu Stop
 
         frame.setJMenuBar(menuBar);
         bg = new JPanel();
         frame.add(bg);
         //Display the window.
         frame.pack();
         frame.setSize(400, 350);
         frame.setVisible(true);
     }
 
     /**
     *
     * @param Languagemenu Menu to which Language Entries will be added
     */
     private void addLanguages(final JMenu Languagemenu)
     {
         final File root = new File("res");
         final String[] files = root.list();
         for(int i = 0; i < files.length; i++)
         {
             if(null != files[i])
             {
                 log.trace("file " + i + ". : " + files[i]);
                 if(MESSAGE_BUNDLE_BASE_NAME.length() < files[i].length())
                 {
                     if(true == files[i].startsWith(MESSAGE_BUNDLE_BASE_NAME + "_"))
                     {
                         final int end = files[i].indexOf(".properties");
                         if(end != -1)
                         {
                             final String CountryCode = files[i].substring(MESSAGE_BUNDLE_BASE_NAME.length() + 1, end);
                             log.trace("res" + File.separator + CountryCode + ".gif");
                             final JMenuItem CountryMenuItem = new JMenuItem(LANGUAGE_MENUE_ITEM_PREFIX
                                                                             + CountryCode,
                                                                             new ImageIcon("res"
                                                                                           + File.separator
                                                                                           + CountryCode
                                                                                           + ".gif"));
                             CountryMenuItem.addActionListener(this);
                             Languagemenu.add(CountryMenuItem);
                         }
                         // else file[i] is not a Message Bundle
                     }
                     // else file[i] is not a Message Bundle
                 }
                 // else String is to short
             }
             // else string is null !
         }
     }
 
     private static void initLookAndFeel(final String Skin)
     {
         String lookAndFeel = null;
 
         if (Skin != null)
         {
             if (Skin.equals("Metal"))
             {
                 lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
             }
             else
                 if (Skin.equals("System"))
                 {
                     lookAndFeel = UIManager.getSystemLookAndFeelClassName();
                 }
                 else
                     if (Skin.equals("Motif"))
                     {
                         lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
                     }
                     else
                         if (Skin.equals("GTK+"))
                         {
                             lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                         }
                         else
                         {
                             lookAndFeel = Skin;
                         }
 
             try
             {
                 UIManager.setLookAndFeel(lookAndFeel);
             }
             catch (final ClassNotFoundException e)
             {
                 System.err.println("Couldn't find class for specified look and feel:"
                                    + lookAndFeel);
                 System.err.println("Did you include the L&F library in the class path?");
                 System.err.println("Using the default look and feel.");
             }
             catch (final UnsupportedLookAndFeelException e)
             {
                 System.err.println("Can't use the specified look and feel ("
                                    + lookAndFeel
                                    + ") on this platform.");
                 System.err.println("Using the default look and feel.");
             }
             catch (final Exception e)
             {
                 System.err.println("Couldn't get specified look and feel ("
                                    + lookAndFeel
                                    + "), for some reason.");
                 System.err.println("Using the default look and feel.");
                 e.printStackTrace();
             }
         }
     }
 
     // Initialization Done
     // Now comes Event Handling
 
     /**
      * Menu->Node->New...
      */
     public final void displayConnectNodeDialog()
     {
         // TODO create dialog, ask user for IP + Port Save Data to cfg
         // NodeCon = new XmlConnection("127.0.0.1", Defaults.TCP_CONTROL_INTERFACE_PORT);
         NodeCon = new SslXmlConnection("127.0.0.1", Defaults.SSL_CONTROL_INTERFACE_PORT);
         if(true == NodeCon.canConnect())
         {
             ConnectedNode = new NodePanel(NodeCon, true, messages);
             frame.remove(bg);
             frame.add(ConnectedNode.getPanel());
             frame.validate();
         }
         else
         {
             JOptionPane.showMessageDialog(frame,
                                           "Could not Connect to Server (127.0.0.1:"
                                          + Defaults.SSL_CONTROL_INTERFACE_PORT
                                           + ") !");
         }
     }
 
     /**
      * The About Dialog in the Language Menu.
      */
     public final void displayAboutDialog()
     {
         JOptionPane.showMessageDialog(frame,
           "Friends United User Interface v " + Version + " \n"
         + "This program is free software; you can redistribute it and/or\n"
         + "modify it under the terms of the GNU General Public License version 2\n"
         + "as published by the Free Software Foundation.");
     }
 
     /**
      * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
      */
     public final void windowActivated(final WindowEvent arg0)
     {
         log.trace("Window Activated !");
     }
 
     /**
      * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
      */
     public final void windowClosed(final WindowEvent arg0)
     {
         log.trace("Window Closed !");
     }
 
     /**
      * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
      */
     public final void windowClosing(final WindowEvent arg0)
     {
         log.trace("Window Closing !");
         cfg.save(Defaults.XML_GUI_CONFIG_FILENAME);
         System.exit(0);
     }
 
     /**
      * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
      */
     public final void windowDeactivated(final WindowEvent arg0)
     {
         log.trace("Window Deactivated !");
     }
 
     /**
      * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
      */
     public final void windowDeiconified(final WindowEvent arg0)
     {
         log.trace("Window Deiconified !");
     }
 
     /**
      * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
      */
     public final void windowIconified(final WindowEvent arg0)
     {
         log.trace("Window Iconified !");
     }
 
     /**
      * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
      */
     public final void windowOpened(final WindowEvent arg0)
     {
         log.trace("Window Opened !");
     }
 
     @Override
     public final void actionPerformed(final ActionEvent e)
     {
         final int id = e.getID();
         final JMenuItem source = (JMenuItem)(e.getSource());
         String SourceText = source.getText();
         log.error("Source Text : " + SourceText);
         if(null != SourceText)
         {
             if(true == SourceText.startsWith(LANGUAGE_MENUE_ITEM_PREFIX))
             {
                 // The user selected a new Language
                 SourceText = SourceText.substring(LANGUAGE_MENUE_ITEM_PREFIX.length());
                 final int posofSeperator = SourceText.indexOf('_');
                 log.error("Index of Seperator : " + posofSeperator);
                 if(-1 == posofSeperator)
                 {
                     JOptionPane.showMessageDialog(frame,
                                                   messages.getString("FailedtoaddLanguage")
                                                   + " : "
                                                   + source.getText());
                 }
                 else
                 {
                     final String LocalLanguage  = SourceText.substring(0, posofSeperator);
                     log.error("Language : " + LocalLanguage);
                     final String LocalCountry   = SourceText.substring(posofSeperator + 1);
                     log.error("Country : " + LocalCountry);
                     final StringOption loclang = new StringOption("LocalLanguage", LocalLanguage);
                     cfg.add(loclang);
                     final StringOption loccntry = new StringOption("LocalCountry", LocalCountry);
                     cfg.add(loccntry);
                     JOptionPane.showMessageDialog(frame,
                                                   messages.getString("ChangedLanguageto")
                                                   + " : "
                                                   + LocalLanguage
                                                   + " "
                                                   + messages.getString("Changeswillbeeffectiveafterrestart"));
                 }
             }
             else
             {
                 // TODO
                 JOptionPane.showMessageDialog(frame,
                                               "Action Event detected! Source : "
                                               + source.getText()
                                               + " ID : "
                                               + id);
             }
         }
     }
 
 }
