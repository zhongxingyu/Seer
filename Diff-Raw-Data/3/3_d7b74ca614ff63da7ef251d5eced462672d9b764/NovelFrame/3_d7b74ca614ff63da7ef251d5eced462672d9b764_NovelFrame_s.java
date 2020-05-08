 /*
  * [The "New BSD" license]
  * Copyright (c) 2012 The Board of Trustees of The University of Alabama
  * All rights reserved.
  *
  * See LICENSE for details.
  */
 package edu.ua.eng.software.novel;
 
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 
 /**
  * Creates the main UI frame and menu
  * 
  * @author Colin C. Hemphill <colin@hemphill.us>
  */
 @SuppressWarnings("serial")
 public class NovelFrame extends JFrame implements ActionListener
 {
     private NovelPanel panel;
 
     public final void initUI() {
 
         // top level menu items
         JMenuBar menu = new JMenuBar();
         JMenu file = new JMenu("File");
         JMenu edit = new JMenu("Edit");
         JMenu source = new JMenu("Source");
         JMenu help = new JMenu("Help");
         menu.add(file);
         menu.add(edit);
         menu.add(source);
         menu.add(help);
 
         // sub menu items
         JMenuItem fileImport = new JMenuItem("Import/Run");
         JMenuItem filePrefs = new JMenuItem("Preferences");
         JMenuItem fileExit = new JMenuItem("Exit");
         JMenuItem editDif = new JMenuItem("Difference");
         JMenuItem editFilter = new JMenuItem("Filter");
         JMenuItem sourceSelect = new JMenuItem("Select All");
         JMenuItem sourceCopy = new JMenuItem("Copy Selection");
         JMenuItem helpTut = new JMenuItem("Tutorials");
         JMenuItem helpAbout = new JMenuItem("About");
 
         // gray out currently unavailable menu items
         filePrefs.setEnabled(false);
         editDif.setEnabled(false);
         editFilter.setEnabled(false);
         sourceSelect.setEnabled(false);
         sourceCopy.setEnabled(false);
         helpTut.setEnabled(false);
 
         // set action commands
         fileImport.setActionCommand("OPEN");
         fileImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
                 Event.CTRL_MASK));
         fileImport.addActionListener(this);
         filePrefs.setActionCommand("PREFS");
         filePrefs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                 Event.CTRL_MASK));
         filePrefs.addActionListener(this);
         fileExit.setActionCommand("EXIT");
         fileExit.addActionListener(this);
 
         editDif.setActionCommand("DIFF");
         editDif.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                 Event.CTRL_MASK + Event.SHIFT_MASK));
         editDif.addActionListener(this);
         editFilter.setActionCommand("FILTER");
         editFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                 Event.CTRL_MASK + Event.SHIFT_MASK));
         editFilter.addActionListener(this);
 
         sourceSelect.setActionCommand("SELECT");
         sourceSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                 Event.CTRL_MASK));
         sourceSelect.addActionListener(this);
         sourceCopy.setActionCommand("COPY");
         sourceCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                 Event.CTRL_MASK));
         sourceCopy.addActionListener(this);
 
         helpTut.setActionCommand("TUTORIAL");
         helpTut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
         helpTut.addActionListener(this);
         helpAbout.setActionCommand("ABOUT");
         helpAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
         helpAbout.addActionListener(this);
 
         // add the sub menus
         file.add(fileImport);
         file.add(filePrefs);
         file.add(fileExit);
         edit.add(editDif);
         edit.add(editFilter);
         source.add(sourceSelect);
         source.add(sourceCopy);
         help.add(helpTut);
         help.add(helpAbout);
         setJMenuBar(menu);
 
         // initiate the window
         setTitle("NoVEL");
         setSize(1024, 768);
         setLocationRelativeTo(null);
         setMinimumSize(new Dimension(800, 600));
         // setExtendedState(MAXIMIZED_BOTH);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
         panel = new NovelPanel();
         setContentPane(panel);
         NovelPanelController.getInstance().setPanel(panel);
     }
 
     /**
      * Handle menu interactions
      * 
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent e) {
 
         if (e.getActionCommand().equals("OPEN")) {
             NovelImportDialog importDialog = new NovelImportDialog(this);
         } else if (e.getActionCommand().equals("PREFS"))
             prefsDialog();
         else if (e.getActionCommand().equals("EXIT"))
             System.exit(0);
         else if (e.getActionCommand().equals("TUTORIAL"))
             try {
                 tutorialDialog();
             } catch (IOException e1) {
                 e1.printStackTrace();
             } catch (URISyntaxException e1) {
                 e1.printStackTrace();
             }
         else if (e.getActionCommand().equals("ABOUT"))
             aboutDialog();
     }
 
     /**
      * Create dialog to handle user preferences
      */
     public void prefsDialog() {
 
         // will use java.util.prefs package to handle preference storage
         // will most likely use a new "NovelPrefs" class to display
     }
 
     /**
      * Open software tutorials, most likely to be hosted online
      * 
      * @throws IOException
      * @throws URISyntaxException
      */
     public void tutorialDialog() throws IOException, URISyntaxException {
 
         Desktop.getDesktop().browse(new URI("about:blank"));
     }
 
     /**
      * Create "About" dialog
      */
     public void aboutDialog() {
 
         JOptionPane.showMessageDialog(this, "N.o.V.E.L. © Copyright 2012"
                 + "\nVersion 1.0" + "\n\nBlake Bassett, Casey Ferris"
                 + "\nColin Hemphill, Conor Kirkman"
                 + "\nNicholas Kraft, Paige Rodeghero", "About N.o.V.E.L.",
                 JOptionPane.INFORMATION_MESSAGE);
     }
 }
