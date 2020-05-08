 /* Copyright 2012 Sabre Holdings */
 package com.fteams.pinger.ui;
 
 import com.fteams.pinger.PropertyLoader;
 import com.fteams.pinger.applied.AmiAmiPinger;
 import com.fteams.pinger.base.Pinger;
 import com.fteams.pinger.base.PingerPropertyUpdater;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 public class MainFrame extends JFrame implements ActionListener
 {
     private Pinger pinger;
     private PingerPropertyUpdater updater;
     String CONFIG_PATH;
 
     DefaultListModel modelAvailable;
     DefaultListModel modelUnavailable;
     JScrollPane availableItemPanel;
     JScrollPane unavailableItemPanel;
     PropertyLoader loader;
     JPanel panelContainer;
     JPanel codesPanel;
     JScrollPane consolePanel;
 
     JPanel buttonPanel;
     JButton refreshButton;
     JButton startButton;
     JMenuBar menuBar;
     JMenu fileMenu;
     JMenu toolsMenu;
     JMenu infoMenu;
     JMenuItem exitItem;
     JMenuItem settingsItem;
     JMenuItem aboutItem;
     JTextArea consoleOutput;
     JList availableList;
     JList unavailableList;
     ConfigurationFrame config_frame;
     AboutFrame about_frame;
 
     public MainFrame(String config_path) throws IOException
     {
         this.CONFIG_PATH = config_path;
         menuBar = new JMenuBar();
 
         panelContainer = new JPanel();
         codesPanel = new JPanel();
         buttonPanel = new JPanel();
 
         consoleOutput = new JTextArea();
 
         fileMenu = new JMenu("File");
         exitItem =  new JMenuItem("Exit");
         exitItem.addActionListener(this);
 
         toolsMenu = new JMenu("Tools");
         settingsItem = new JMenuItem("Settings");
         settingsItem.addActionListener(this);
 
         infoMenu = new JMenu("Help");
         aboutItem =  new JMenuItem("About");
         aboutItem.addActionListener(this);
 
         refreshButton = new JButton("Refresh");
         refreshButton.addActionListener(this);
 
         startButton = new JButton("Start");
         startButton.addActionListener(this);
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         modelAvailable = new DefaultListModel();
         modelUnavailable = new DefaultListModel();
         availableList = new JList(modelAvailable);
         unavailableList = new JList(modelUnavailable);
 
         availableItemPanel = new JScrollPane(availableList);
         unavailableItemPanel = new JScrollPane(unavailableList);
         availableItemPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Available Items"));
         unavailableItemPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Unavailable Items"));
 
         codesPanel.add(unavailableItemPanel);
         codesPanel.add(availableItemPanel);
 
         codesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Lookup IDs"));
         codesPanel.setLayout(new GridLayout(1,2));
 
         consolePanel = new JScrollPane(consoleOutput);
         consolePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Console output"));
         consolePanel.getVerticalScrollBar().setUnitIncrement(25);
         consoleOutput.setEditable(false);
        consoleOutput.setWrapStyleWord(true);
        consoleOutput.setLineWrap(true);
 
         fileMenu.add(exitItem);
         toolsMenu.add(settingsItem);
         infoMenu.add(aboutItem);
 
         menuBar.add(fileMenu);
         menuBar.add(toolsMenu);
         menuBar.add(infoMenu);
 
         setTitle("AmiAmi Pinger Interface");
 
         setJMenuBar(menuBar);
 
         panelContainer.setLayout(new GridLayout(2,1));
         panelContainer.add(codesPanel);
 
 
         panelContainer.add(consolePanel);
 
         add(panelContainer, BorderLayout.CENTER);
 
         buttonPanel.add(startButton);
         buttonPanel.add(refreshButton);
 
         add(buttonPanel, BorderLayout.SOUTH);
 
         setSize(new Dimension(450, 400));
 //        pack();
         setVisible(true);
 //        setResizable(false);
     }
     public void updateConsole(String text)
     {
         consoleOutput.setText(text+"\n"+consoleOutput.getText());
     }
     public void updateListContent()
     {
 //        modelAvailable.ensureCapacity(200);
 //        modelUnavailable.ensureCapacity(200);
         List<String> avFigs = pinger.getAvailableItems();
         List<String> unFigs = pinger.getUnavailableItems();
         modelAvailable.removeAllElements();
         for (String item : avFigs)
         {
             modelAvailable.addElement(item);
         }
         modelUnavailable.removeAllElements();
         for (String item : unFigs)
         {
             modelUnavailable.addElement(item);
         }
     }
     @Override
     public void actionPerformed(ActionEvent e)
     {
         String command = e.getActionCommand().toLowerCase();
         if (command.equals("settings"))
         {
             if (config_frame != null)
             {
                 config_frame.setVisible(true);
             }
             else
                 try
                 {
                     config_frame = new ConfigurationFrame(CONFIG_PATH);
                 } catch (IOException e1)
                 {
                     e1.printStackTrace();
                 }
         }
         else if (command.equals("exit"))
         {
             System.exit(0);
         }
         else if (command.equals("about"))
         {
 
             if (about_frame != null)
             {
                 about_frame.setVisible(true);
             }
             else
             {
                 about_frame = new AboutFrame();
             }
         }
         else if (command.equals("start"))
         {
             if (loader == null)
             {
                 loader = new PropertyLoader();
                 try
                 {
                     loader.loadProperties(new File(CONFIG_PATH));
                 } catch (IOException e1)
                 {
                     e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
             if (pinger == null)
             {
                 pinger = new AmiAmiPinger();
                 pinger.loadProperties(loader);
                 pinger.setUserInterface(this);
                 pinger.start();
             }
             if (updater == null)
             {
                 updater = new PingerPropertyUpdater(CONFIG_PATH, loader, pinger);
                 updater.start();
             }
         }
         else if (command.equals("refresh"))
         {
             try
             {
                 if (pinger == null)
                 {
                     updateConsole("Press START before attempting to refresh.");
                     return;
                 }
                 loader.loadProperties(new File(CONFIG_PATH));
             } catch (IOException e1)
             {
                 e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             pinger.refreshConfig(loader);
 
         }
     }
 }
