 /*
  * Copyright 2008 Pentaho Corporation.  All rights reserved.
  * This software was developed by Pentaho Corporation and is provided under the terms
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use
  * this file except in compliance with the license. If you need a copy of the license,
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
  * the license for the specific language governing your rights and limitations.
  */
 package org.pentaho.mondrian.publish;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Document;
 
 public class PublishToRepositoryDialog extends JDialog implements SolutionRepositorySelectionCallback {
 
   private static final long serialVersionUID = -3664835596972368205L;
   
   private static final String[] EXPORT_TYPES = new String[] { "pdf", "html", "csv", "rtf", "xls" };
   SolutionRepositoryTableView repositoryBrowser = null;
 
   JComboBox fileFormat = new JComboBox(EXPORT_TYPES);
   JTextField fileNameTextField = new JTextField("default");
   JTextField jndiDataSourceName = new JTextField("");  
   JTextField descTextField = new JTextField("");
   JCheckBox enableXmlaCheckBox = new JCheckBox("Register XMLA Data Source");
   JCheckBox forceOutputTypePromptCB = new JCheckBox("Prompt for Report Output Type");
   
   JComboBox locationCombo = new JComboBox();
   String publishLocation = "";
   Component parent;
   boolean okPressed = false;
 
   public PublishToRepositoryDialog(
           final JFrame parent, 
           Document repositoryDoc,
           boolean showFoldersOnly, 
           String webPublishURL, 
           String publishLocation, 
           String userid, 
           String password, 
           String[] filters,
           String schemaName, 
           String schemaFileName,
           String jndiName,
           boolean enableXmla
   ) throws Exception {
     super(parent, "Publish Schema");
     this.parent = parent;
     init(repositoryDoc, showFoldersOnly, webPublishURL, publishLocation, userid, password, filters, schemaName, schemaFileName, jndiName, enableXmla);
   }
 
   public void init(
           final Document repositoryDoc,
           final boolean showFoldersOnly,
           final String webPublishURL, 
           final String publishLocation,
           final String userid, 
           final String password, 
           final String[] filters,
           final String schemaName, 
           final String schemaFileName,
           final String jndiName,
           final boolean enableXmla
   ) throws Exception {
     setModal(true);
     this.publishLocation = publishLocation;
     repositoryBrowser =  new SolutionRepositoryTableView(repositoryDoc, 
             showFoldersOnly, webPublishURL, filters, userid, password, this);
     
     repositoryBrowser.setSelectedPath(publishLocation);
     repositoryBrowser.setPreferredSize(new Dimension(480, 200));
 
     JPanel buttonPanel = new JPanel(new GridBagLayout());
     JButton okButton = new JButton("Publish");
     okButton.addActionListener(new ActionListener() {
 
       public void actionPerformed(ActionEvent e) {
        String path = repositoryBrowser.getSelectedFolder();
        setPublishLocation(path);
         okPressed = true;
         setVisible(false);
       }
 
     });
     JButton cancelButton = new JButton("Cancel");
     cancelButton.addActionListener(new ActionListener() {
 
       public void actionPerformed(ActionEvent e) {
         okPressed = false;
         setVisible(false);
       }
 
     });
 
     GridBagConstraints c = new GridBagConstraints();
     c.gridx = 0;
     c.gridy = 0;
     c.fill = GridBagConstraints.NONE;
     c.weightx = 1.0;
     c.anchor = GridBagConstraints.EAST;
     buttonPanel.add(okButton, c);
     c.gridx = 1;
     c.weightx = 0;
     buttonPanel.add(cancelButton, c);
 
     JPanel publishHeaderPanel = new JPanel(new GridBagLayout());
     ImageIcon icon = new ImageIcon(SolutionRepositoryTree.class.getResource("/images/newfolder.png"));
     final JLabel newFolder = new JLabel(icon);
     newFolder.setToolTipText("Create New Folder...");
     newFolder.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
     newFolder.addMouseListener(new MouseListener() {
 
       public void mouseClicked(MouseEvent e) {
         CreateNewRepositoryFolderDialog newFolderDialog = new CreateNewRepositoryFolderDialog(PublishToRepositoryDialog.this);
         Dimension paneSize = newFolderDialog.getSize();
         Dimension screenSize = newFolderDialog.getToolkit().getScreenSize();
         newFolderDialog.setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
         newFolderDialog.setVisible(true);
         if (newFolderDialog.isOkPressed()) {
           String path = repositoryBrowser.getSelectedFolder();
           if (!StringUtils.isEmpty(newFolderDialog.getName())) {
             try {
               SwingUtilities.getRootPane(PublishToRepositoryDialog.this).getGlassPane().setVisible(true);
               SwingUtilities.getRootPane(PublishToRepositoryDialog.this).getGlassPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
               RepositoryHelper.getInstance().createNewFolder(webPublishURL, path, newFolderDialog.getName(), newFolderDialog.getDescription());
               repositoryBrowser.refresh(true);
             } catch (Exception e1) {
               e1.printStackTrace();
             } finally {
               SwingUtilities.getRootPane(PublishToRepositoryDialog.this).getGlassPane().setVisible(false);
               SwingUtilities.getRootPane(PublishToRepositoryDialog.this).getGlassPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
           }
         }
       }
 
       public void mouseEntered(MouseEvent e) {
         newFolder.setBorder(BorderFactory.createEtchedBorder());
       }
 
       public void mouseExited(MouseEvent e) {
         newFolder.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
       }
 
       public void mousePressed(MouseEvent e) {
       }
 
       public void mouseReleased(MouseEvent e) {
       }
 
     });
 
     ImageIcon levelUpIcon = new ImageIcon(SolutionRepositoryTree.class.getResource("/images/upfolder.png"));
     final JLabel levelUp = new JLabel(levelUpIcon);
     levelUp.setToolTipText("Up One Level");
     levelUp.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
     levelUp.addMouseListener(new MouseListener() {
 
       public void mouseClicked(MouseEvent e) {
         repositoryBrowser.levelUp();
         buildLocationCombo(repositoryBrowser.getSelectedFolder());
       }
 
       public void mouseEntered(MouseEvent e) {
         levelUp.setBorder(BorderFactory.createEtchedBorder());
       }
 
       public void mouseExited(MouseEvent e) {
         levelUp.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
       }
 
       public void mousePressed(MouseEvent e) {
         levelUp.setBorder(BorderFactory.createLoweredBevelBorder());
       }
 
       public void mouseReleased(MouseEvent e) {
         levelUp.setBorder(BorderFactory.createEtchedBorder());
       }
 
     });
 
     ImageIcon browseIcon = new ImageIcon(SolutionRepositoryTree.class.getResource("/images/navigate.gif"));
     final JLabel browseButton = new JLabel(browseIcon);
     browseButton.setToolTipText("Browse Repository...");
     browseButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
     browseButton.addMouseListener(new MouseListener() {
 
       public void mouseClicked(MouseEvent e) {
         try {
           SolutionRepositoryTreeDialog treeDialog = new SolutionRepositoryTreeDialog(PublishToRepositoryDialog.this, true, webPublishURL, repositoryBrowser.getSelectedFolder(), userid, password, filters);
           treeDialog.setSize(new Dimension(350, 400));
           Dimension paneSize = treeDialog.getSize();
           Dimension screenSize = treeDialog.getToolkit().getScreenSize();
           treeDialog.setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
           treeDialog.setVisible(true);
           if (treeDialog.isOkPressed()) {
             setPublishLocation(treeDialog.getPublishLocation());
             repositoryItemSelected(treeDialog.getPublishLocation());
             repositoryBrowser.refresh(true);
             repositoryBrowser.setSelectedPath(treeDialog.getPublishLocation());
           } else {
             // even though we canceled the user could have easily created a new folder
             repositoryBrowser.refresh(true);
           }
         } catch (Exception e1) {
           e1.printStackTrace();
         }
       }
 
       public void mouseEntered(MouseEvent e) {
         browseButton.setBorder(BorderFactory.createEtchedBorder());
       }
 
       public void mouseExited(MouseEvent e) {
         browseButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
       }
 
       public void mousePressed(MouseEvent e) {
         browseButton.setBorder(BorderFactory.createLoweredBevelBorder());
       }
 
       public void mouseReleased(MouseEvent e) {
         browseButton.setBorder(BorderFactory.createEtchedBorder());
       }
 
     });
 
     c.gridx = 0;
     c.gridy = 0;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.insets = new Insets(5, 5, 5, 5);
     publishHeaderPanel.add(new JLabel("Schema Name:"), c);
 
     c.gridy = 1;
     c.insets = new Insets(2, 5, 0, 5);
     JLabel label = new JLabel(schemaName);
     label.setFont(label.getFont().deriveFont(Font.ITALIC));
 //    fileNameTextField.setText(reportName);
     publishHeaderPanel.add(label, c); // fileNameTextField
 
     c.gridy = 2;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.insets = new Insets(5, 5, 5, 5);
     publishHeaderPanel.add(new JLabel("Schema File:"), c);
 
     c.gridy = 3;
     c.insets = new Insets(2, 5, 0, 5);
     JLabel fileLabel = new JLabel(schemaFileName);
     fileLabel.setFont(fileLabel.getFont().deriveFont(Font.ITALIC));
 
     publishHeaderPanel.add(fileLabel, c);
 
     c.gridy = 4;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.insets = new Insets(2, 5, 0, 5);
     publishHeaderPanel.add(new JLabel("Location:"), c);
 
     JPanel locationFieldPanel = new JPanel();
     locationFieldPanel.setLayout(new GridBagLayout());
     c.insets = new Insets(0, 5, 2, 0);
     c.gridx = 0;
     c.gridy = 0;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.weightx = 1.0;
     c.anchor = GridBagConstraints.WEST;
     locationCombo.addActionListener(new ActionListener() {
 
       public void actionPerformed(ActionEvent e) {
         repositoryBrowser.setSelectedPath((String) locationCombo.getSelectedItem());
         try {
           repositoryBrowser.refresh(false);
         } catch (Exception e1) {
         }
       }
 
     });
     locationCombo.setEditable(true);
     buildLocationCombo(repositoryBrowser.getSelectedFolder());
     locationFieldPanel.add(locationCombo, c);
 
     c.insets = new Insets(2, 0, 2, 0);
     c.gridx = 1;
     c.gridy = 0;
     c.fill = GridBagConstraints.NONE;
     c.weightx = 0.0;
     c.anchor = GridBagConstraints.WEST;
     locationFieldPanel.add(levelUp, c);
 
    c.gridx = 2;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.0;
    c.anchor = GridBagConstraints.EAST;
    locationFieldPanel.add(newFolder, c);
 
     c.gridx = 3;
     c.gridy = 0;
     c.fill = GridBagConstraints.BOTH;
     c.weightx = 0.0;
     c.anchor = GridBagConstraints.EAST;
     locationFieldPanel.add(browseButton, c);
 
     c.insets = new Insets(0, 0, 0, 0);
     c.gridx = 0;
     c.gridy = 5;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.weightx = 1.0;
     c.anchor = GridBagConstraints.WEST;
     publishHeaderPanel.add(locationFieldPanel, c);
 
     getContentPane().setLayout(new GridBagLayout());
     c.insets = new Insets(5, 5, 0, 5);
     c.gridx = 0;
     c.gridy = 0;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.anchor = GridBagConstraints.WEST;
     getContentPane().add(publishHeaderPanel, c);
 
     c.insets = new Insets(0, 10, 0, 10);
     c.gridx = 0;
     c.gridy = 1;
     c.fill = GridBagConstraints.BOTH;
     c.anchor = GridBagConstraints.WEST;
     c.weighty = 1.0;
     getContentPane().add(repositoryBrowser, c);
 
     c.weighty = 0.0;
     c.insets = new Insets(5, 5, 5, 5);
     c.gridx = 0;
     c.gridy = 2;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.anchor = GridBagConstraints.WEST;
 
     // publish settings panel
     JPanel publishSettingsPanel = new JPanel(new GridBagLayout());
     publishSettingsPanel.setBorder(BorderFactory.createTitledBorder("Publish Settings"));
 
     c.insets = new Insets(5, 5, 0, 5);
     c.gridwidth = 2;
     c.gridx = 0;
     c.gridy = 0;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.anchor = GridBagConstraints.WEST;
     publishSettingsPanel.add(new JLabel("JNDI Data Source:"), c);
 
     c.insets = new Insets(5, 5, 5, 0);
     c.gridwidth = 1;
     c.gridx = 0;
     c.gridy = 1;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.anchor = GridBagConstraints.WEST;
     c.weightx = 1.0;
     jndiDataSourceName.setText(jndiName);
     publishSettingsPanel.add(jndiDataSourceName, c);
 
     enableXmlaCheckBox.setSelected(enableXmla);
     c.gridwidth = 2;
     c.insets = new Insets(5, 5, 5, 5);
     c.fill = GridBagConstraints.HORIZONTAL;
     c.anchor = GridBagConstraints.WEST;
     c.gridx = 0;
     c.gridy = 2;
     c.weightx = 1.0;
     publishSettingsPanel.add(enableXmlaCheckBox, c);
 
     c.weightx = 0.0;
     c.insets = new Insets(5, 5, 5, 5);
     c.gridx = 0;
     c.gridy = 3;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.anchor = GridBagConstraints.WEST;
     getContentPane().add(publishSettingsPanel, c);
 
     c.insets = new Insets(5, 5, 5, 5);
     c.gridx = 0;
     c.gridy = 4;
     c.fill = GridBagConstraints.HORIZONTAL;
     c.anchor = GridBagConstraints.WEST;
     getContentPane().add(buttonPanel, c);
     pack();
     setResizable(false);
     setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   }
 
   public void buildLocationCombo(String selectedPath) {
     StringTokenizer st = new StringTokenizer(selectedPath, "/");
     List<String> pathList = new ArrayList<String>();
     pathList.add("/");
     String deepPath = "";
     while (st.hasMoreTokens()) {
       String pathElement = st.nextToken();
       deepPath += "/" + pathElement;
       pathList.add(deepPath);
     }
     locationCombo.setModel(new DefaultComboBoxModel(pathList.toArray()));
     locationCombo.setSelectedIndex(locationCombo.getItemCount() - 1);
   }
 
   public String getPublishLocation() {
     return publishLocation;
   }
 
   public void setPublishLocation(String publishLocation) {
     this.publishLocation = publishLocation;
   }
 
   public void repositoryItemSelected(String selectedPath) {
     buildLocationCombo(selectedPath);
   }
 
   public boolean isOkPressed() {
     return okPressed;
   }
 
   public void setOkPressed(boolean okPressed) {
     this.okPressed = okPressed;
   }
 
   public String getJndiDataSourceName() {
     return jndiDataSourceName.getText();
   }
 
   public boolean getEnableXmla() {
     return enableXmlaCheckBox.isSelected();
   }
 
   public boolean doesFileExist(String filePath) {
     return repositoryBrowser.getRepositoryHelper().exists(filePath);
   }
 }
