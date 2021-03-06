 /*
   BoardSettingsFrame.java / Frost
   Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>
 
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of
   the License, or (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
 
 package frost.boards;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.logging.*;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 import frost.*;
 import frost.fcp.*;
 import frost.gui.objects.*;
 import frost.util.gui.*;
 import frost.util.gui.translation.*;
 
 /**
  * Settingsdialog for a single Board or a folder.
  */
 public class BoardSettingsFrame extends JDialog {
 
     private class Listener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
             if (e.getSource() == publicBoardRadioButton) { // Public board radio button
                 radioButton_actionPerformed(e);
             } else if (e.getSource() == secureBoardRadioButton) { // Private board radio button
                 radioButton_actionPerformed(e);
             } else if (e.getSource() == generateKeyButton) { // Generate key
                 generateKeyButton_actionPerformed(e);
             } else if (e.getSource() == okButton) { // Ok
                 okButton_actionPerformed(e);
             } else if (e.getSource() == cancelButton) { // Cancel
                 cancelButton_actionPerformed(e);
             } else if (e.getSource() == overrideSettingsCheckBox) { // Override settings
                 overrideSettingsCheckBox_actionPerformed(e);
             }
         }
     }
 
     private static Logger logger = Logger.getLogger(BoardSettingsFrame.class.getName());
 
     private Language language;
     private Board board;
     private JFrame parentFrame;
 
     private Listener listener = new Listener();
 
     private JCheckBox autoUpdateEnabled = new JCheckBox();
     private JButton cancelButton = new JButton();
     private boolean exitState;
     private JButton generateKeyButton = new JButton();
 
     private JRadioButton hideBad_default = new JRadioButton();
     private JRadioButton hideBad_false = new JRadioButton();
     private JRadioButton hideBad_true = new JRadioButton();
     private JLabel hideBadMessagesLabel = new JLabel();
 
     private JRadioButton hideCheck_default = new JRadioButton();
     private JRadioButton hideCheck_false = new JRadioButton();
     private JRadioButton hideCheck_true = new JRadioButton();
     private JLabel hideCheckMessagesLabel = new JLabel();
 
     private JRadioButton hideObserve_default = new JRadioButton();
     private JRadioButton hideObserve_false = new JRadioButton();
     private JRadioButton hideObserve_true = new JRadioButton();
     private JLabel hideObserveMessagesLabel = new JLabel();
     private JLabel hideUnsignedMessagesLabel = new JLabel();
 
     private JRadioButton maxMsg_default = new JRadioButton();
     private JRadioButton maxMsg_set = new JRadioButton();
     private JTextField maxMsg_value = new JTextField(6);
     private JLabel messageDisplayDaysLabel = new JLabel();
 
     private JButton okButton = new JButton();
 
     private JCheckBox overrideSettingsCheckBox = new JCheckBox();
     private JLabel privateKeyLabel = new JLabel();
 
     private JTextField privateKeyTextField = new JTextField();
 
     private JRadioButton publicBoardRadioButton = new JRadioButton();
 
     private JLabel publicKeyLabel = new JLabel();
     private JTextField publicKeyTextField = new JTextField();
 
     private JRadioButton secureBoardRadioButton = new JRadioButton();
 
     private JRadioButton signedOnly_default = new JRadioButton();
     private JRadioButton signedOnly_false = new JRadioButton();
     private JRadioButton signedOnly_true = new JRadioButton();
 
     JPanel settingsPanel = new JPanel(new GridBagLayout());
 
     private JLabel descriptionLabel = new JLabel();
     private JTextArea descriptionTextArea = new JTextArea(3, 40);
     private JScrollPane descriptionScrollPane;
 
     /**
      * @param parentFrame
      * @param board
      */
     public BoardSettingsFrame(JFrame parentFrame, Board board) {
         super(parentFrame);
 
         this.parentFrame = parentFrame;
         this.board = board;
         this.language = Language.getInstance();
 
         setModal(true);
         enableEvents(AWTEvent.WINDOW_EVENT_MASK);
         initialize();
         pack();
         setLocationRelativeTo(parentFrame);
     }
 
     /**
      * Close window and do not save settings
      */
     private void cancel() {
         exitState = false;
         dispose();
     }
 
     /**
      * cancelButton Action Listener (Cancel)
      * @param e
      */
     private void cancelButton_actionPerformed(ActionEvent e) {
         cancel();
     }
 
     /**
      * generateKeyButton Action Listener (OK)
      * @param e
      */
     private void generateKeyButton_actionPerformed(ActionEvent e) {
         FcpConnection connection = FcpFactory.getFcpConnectionInstance();
         if (connection == null)
             return;
 
         try {
             String[] keyPair = connection.getKeyPair();
             privateKeyTextField.setText(keyPair[0]);
             publicKeyTextField.setText(keyPair[1]);
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(parentFrame, ex.toString(), // message
                     language.getString("Warning"), JOptionPane.WARNING_MESSAGE);
         }
     }
 
     /**
      * @param e
      */
     private void overrideSettingsCheckBox_actionPerformed(ActionEvent e) {
         setPanelEnabled(settingsPanel, overrideSettingsCheckBox.isSelected());
     }
 
     //------------------------------------------------------------------------
 
     /**Return exitState
      * @return
      */
     public boolean getExitState() {
         return exitState;
     }
 
     /**
      * @return
      */
     private JPanel getSettingsPanel() {
         settingsPanel.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5,5,5,5)));
         settingsPanel.setLayout(new GridBagLayout());
 
         ButtonGroup bg2 = new ButtonGroup();
         bg2.add(maxMsg_default);
         bg2.add(maxMsg_set);
         ButtonGroup bg3 = new ButtonGroup();
         bg3.add(signedOnly_default);
         bg3.add(signedOnly_false);
         bg3.add(signedOnly_true);
         ButtonGroup bg4 = new ButtonGroup();
         bg4.add(hideBad_default);
         bg4.add(hideBad_true);
         bg4.add(hideBad_false);
         ButtonGroup bg5 = new ButtonGroup();
         bg5.add(hideCheck_default);
         bg5.add(hideCheck_true);
         bg5.add(hideCheck_false);
         ButtonGroup bg6 = new ButtonGroup();
         bg6.add(hideObserve_default);
         bg6.add(hideObserve_true);
         bg6.add(hideObserve_false);
 
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.anchor = GridBagConstraints.WEST;
         constraints.insets = new Insets(5, 5, 5, 5);
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.weightx = 1;
         constraints.weighty = 1;
 
         constraints.gridwidth = 3;
         settingsPanel.add(overrideSettingsCheckBox, constraints);
         constraints.gridy = 1;
         constraints.insets = new Insets(5, 25, 0, 5);
         settingsPanel.add(autoUpdateEnabled, constraints);
         constraints.gridy = 2;
 
         constraints.gridwidth = 3;
         constraints.gridx = 0;
         constraints.insets = new Insets(3, 25, 0, 5);
         settingsPanel.add(messageDisplayDaysLabel, constraints);
         constraints.insets = new Insets(0, 35, 0, 5);
         constraints.gridwidth = 1;
         constraints.gridy = 3;
         constraints.gridx = 0;
         settingsPanel.add(maxMsg_default, constraints);
         constraints.gridx = 1;
         settingsPanel.add(maxMsg_set, constraints);
         constraints.gridx = 2;
         settingsPanel.add(maxMsg_value, constraints);
         constraints.gridy = 4;
 
         constraints.gridwidth = 3;
         constraints.gridx = 0;
         constraints.insets = new Insets(3, 25, 0, 5);
         settingsPanel.add(hideUnsignedMessagesLabel, constraints);
         constraints.insets = new Insets(0, 35, 0, 5);
         constraints.gridwidth = 1;
         constraints.gridy = 5;
         constraints.gridx = 0;
         settingsPanel.add(signedOnly_default, constraints);
         constraints.gridx = 1;
         settingsPanel.add(signedOnly_true, constraints);
         constraints.gridx = 2;
         settingsPanel.add(signedOnly_false, constraints);
         constraints.gridy = 6;
 
         constraints.gridwidth = 3;
         constraints.gridx = 0;
         constraints.insets = new Insets(3, 25, 0, 5);
         settingsPanel.add(hideBadMessagesLabel, constraints);
         constraints.insets = new Insets(0, 35, 0, 5);
         constraints.gridwidth = 1;
         constraints.gridy = 7;
         constraints.gridx = 0;
         settingsPanel.add(hideBad_default, constraints);
         constraints.gridx = 1;
         settingsPanel.add(hideBad_true, constraints);
         constraints.gridx = 2;
         settingsPanel.add(hideBad_false, constraints);
         constraints.gridy = 8;
 
         constraints.gridwidth = 3;
         constraints.gridx = 0;
         constraints.insets = new Insets(3, 25, 0, 5);
         settingsPanel.add(hideCheckMessagesLabel, constraints);
         constraints.insets = new Insets(0, 35, 0, 5);
         constraints.gridwidth = 1;
         constraints.gridy = 9;
         constraints.gridx = 0;
         settingsPanel.add(hideCheck_default, constraints);
         constraints.gridx = 1;
         settingsPanel.add(hideCheck_true, constraints);
         constraints.gridx = 2;
         settingsPanel.add(hideCheck_false, constraints);
         constraints.gridy = 10;
 
         constraints.gridwidth = 3;
         constraints.gridx = 0;
         constraints.insets = new Insets(3, 25, 0, 5);
         settingsPanel.add(hideObserveMessagesLabel, constraints);
         constraints.insets = new Insets(0, 35, 5, 5);
         constraints.gridwidth = 1;
         constraints.gridy = 11;
         constraints.gridx = 0;
         settingsPanel.add(hideObserve_default, constraints);
         constraints.gridx = 1;
         settingsPanel.add(hideObserve_true, constraints);
         constraints.gridx = 2;
         settingsPanel.add(hideObserve_false, constraints);
 
         // Adds listeners
         overrideSettingsCheckBox.addActionListener(listener);
 
         setPanelEnabled(settingsPanel, board.isConfigured());
 
         return settingsPanel;
     }
 
     private void initialize() {
         JPanel contentPanel = new JPanel();
         contentPanel.setBorder(new EmptyBorder(10,10,10,10));
         setContentPane(contentPanel);
         contentPanel.setLayout(new GridBagLayout());
         refreshLanguage();
 
         // Adds all of the components
         new TextComponentClipboardMenu(maxMsg_value, language);
         new TextComponentClipboardMenu(privateKeyTextField, language);
         new TextComponentClipboardMenu(publicKeyTextField, language);
         new TextComponentClipboardMenu(descriptionTextArea, language);
 
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.BOTH;
         constraints.insets = new Insets(5, 5, 5, 5);
         constraints.weightx = 1;
         constraints.weighty = 1;
         constraints.gridwidth = 3;
 
         constraints.weightx = 2;
         constraints.gridx = 0;
         constraints.gridy = 0;
         contentPanel.add(getKeysPanel(), constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 1;
         contentPanel.add(descriptionLabel, constraints);
         constraints.gridx = 0;
         constraints.gridy = 2;
         descriptionScrollPane = new JScrollPane(descriptionTextArea);
         contentPanel.add(descriptionScrollPane, constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 3;
         contentPanel.add(getSettingsPanel(), constraints);
 
         constraints.fill = GridBagConstraints.NONE;
         constraints.anchor = GridBagConstraints.EAST;
         constraints.gridwidth = 1;
         constraints.weightx = 2;
         constraints.gridx = 0;
         constraints.gridy = 4;
         contentPanel.add(okButton, constraints);
         constraints.weightx = 0;
         constraints.gridx = 1;
         constraints.gridy = 4;
         contentPanel.add(cancelButton, constraints);
 
         descriptionTextArea.setEditable(false);
         publicBoardRadioButton.setSelected(true);
         privateKeyTextField.setEnabled(false);
         publicKeyTextField.setEnabled(false);
         generateKeyButton.setEnabled(false);
 
         // Adds listeners
         okButton.addActionListener(listener);
         cancelButton.addActionListener(listener);
 
         loadKeypair();
         loadBoardSettings();
     }
 
     /**
      * @return
      */
     private JPanel getKeysPanel() {
         JPanel keysPanel = new JPanel();
         keysPanel.setLayout(new GridBagLayout());
 
         ButtonGroup isSecureGroup = new ButtonGroup();
         isSecureGroup.add(publicBoardRadioButton);
         isSecureGroup.add(secureBoardRadioButton);
 
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.insets = new Insets(5, 5, 5, 5);
         constraints.gridwidth = 1;
         constraints.weighty = 1;
 
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.weightx = 0.2;
         keysPanel.add(publicBoardRadioButton, constraints);
 
         constraints.weightx = 0.2;
         constraints.gridy = 1;
         keysPanel.add(secureBoardRadioButton, constraints);
         constraints.gridx = 1;
         constraints.weightx = 0.8;
         constraints.fill = GridBagConstraints.NONE;
         constraints.anchor = GridBagConstraints.EAST;
         keysPanel.add(generateKeyButton, constraints);
 
         constraints.anchor = GridBagConstraints.WEST;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.gridx = 0;
         constraints.gridy = 2;
         constraints.weightx = 0.2;
         keysPanel.add(privateKeyLabel, constraints);
         constraints.gridx = 1;
         constraints.weightx = 0.8;
         keysPanel.add(privateKeyTextField, constraints);
 
         constraints.gridx = 0;
         constraints.gridy = 3;
         constraints.weightx = 0.2;
         keysPanel.add(publicKeyLabel, constraints);
         constraints.gridx = 1;
         constraints.weightx = 0.8;
         keysPanel.add(publicKeyTextField, constraints);
 
         // Adds listeners
         publicBoardRadioButton.addActionListener(listener);
         secureBoardRadioButton.addActionListener(listener);
         generateKeyButton.addActionListener(listener);
 
         return keysPanel;
     }
 
     /**
      * Set initial values for board settings.
      */
     private void loadBoardSettings() {
         if( board.isFolder() ) {
 
             descriptionTextArea.setEnabled(false);
             overrideSettingsCheckBox.setSelected(false);
 
         } else {
             // its a single board
             if (board.getDescription() != null) {
                 descriptionTextArea.setText(board.getDescription());
             }
 
             overrideSettingsCheckBox.setSelected(board.isConfigured());
 
             if (!board.isConfigured() || board.getMaxMessageDisplayObj() == null)
                 maxMsg_default.setSelected(true);
             else {
                 maxMsg_set.setSelected(true);
                 maxMsg_value.setText("" + board.getMaxMessageDisplay());
             }
 
             if (!board.isConfigured())
                 autoUpdateEnabled.setSelected(true); // default
             else if (board.getAutoUpdateEnabled())
                 autoUpdateEnabled.setSelected(true);
             else
                 autoUpdateEnabled.setSelected(false);
 
             if (!board.isConfigured() || board.getShowSignedOnlyObj() == null)
                 signedOnly_default.setSelected(true);
             else if (board.getShowSignedOnly())
                 signedOnly_true.setSelected(true);
             else
                 signedOnly_false.setSelected(true);
 
             if (!board.isConfigured() || board.getHideBadObj() == null)
                 hideBad_default.setSelected(true);
             else if (board.getHideBad())
                 hideBad_true.setSelected(true);
             else
                 hideBad_false.setSelected(true);
 
             if (!board.isConfigured() || board.getHideCheckObj() == null)
                 hideCheck_default.setSelected(true);
             else if (board.getHideCheck())
                 hideCheck_true.setSelected(true);
             else
                 hideCheck_false.setSelected(true);
 
             if (!board.isConfigured() || board.getHideObserveObj() == null)
                 hideObserve_default.setSelected(true);
             else if (board.getHideObserve())
                 hideObserve_true.setSelected(true);
             else
                 hideObserve_false.setSelected(true);
         }
     }
 
     /**
      * Loads keypair from file
      */
     private void loadKeypair() {
 
         if( board.isFolder() ) {
             privateKeyTextField.setEnabled(false);
             publicKeyTextField.setEnabled(false);
             generateKeyButton.setEnabled(false);
             publicBoardRadioButton.setEnabled(false);
             secureBoardRadioButton.setEnabled(false);
 
         } else {
             String privateKey = board.getPrivateKey();
             String publicKey = board.getPublicKey();
 
             if (privateKey != null) {
                 privateKeyTextField.setText(privateKey);
             } else {
                 privateKeyTextField.setText(language.getString("Not available"));
             }
 
             if (publicKey != null) {
                 publicKeyTextField.setText(publicKey);
             } else {
                 publicKeyTextField.setText(language.getString("Not available"));
             }
 
             if (board.isWriteAccessBoard() || board.isReadAccessBoard()) {
                 privateKeyTextField.setEnabled(true);
                 publicKeyTextField.setEnabled(true);
                 generateKeyButton.setEnabled(true);
                 secureBoardRadioButton.setSelected(true);
             } else { // its a public board
                 privateKeyTextField.setEnabled(false);
                 publicKeyTextField.setEnabled(false);
                 generateKeyButton.setEnabled(false);
                 publicBoardRadioButton.setSelected(true);
             }
         }
     }
 
 
     /**
      * Close window and save settings
      */
     private void ok() {
 
         if( board.isFolder() == false ) {
             // if board was secure before and now its public, ask user if ok to remove the keys
             if( publicBoardRadioButton.isSelected() && board.isPublicBoard() == false ) {
                 int result = JOptionPane.showConfirmDialog(
                         this,
                         language.getString("BoardSettingsFrame.confirmBody"),
                         language.getString("BoardSettingsFrame.confirmTitle"),
                         JOptionPane.YES_NO_OPTION,
                         JOptionPane.WARNING_MESSAGE);
                 if (result == JOptionPane.NO_OPTION) {
                     return;
                 }
             }
             applySettingsToBoard();
         } else {
             // apply settings to all boards in a folder
             applySettingsToFolder(board);
         }
 
         // finally update all involved boards before we close the dialog
         updateBoard(board); // board or folder
 
         exitState = true;
         dispose();
     }
 
     private void applySettingsToFolder(Board b) {
 
         // process all childs recursiv
         if( b.isFolder() ) {
             for(int x=0; x < b.getChildCount(); x++) {
                 Board b2 = (Board)b.getChildAt(x);
                 applySettingsToFolder(b2);
             }
             return;
         }
         // apply set settings to the board, unset options are not changed
         if (overrideSettingsCheckBox.isSelected()) {
             b.setConfigured(true);
 
             b.setAutoUpdateEnabled(autoUpdateEnabled.isSelected());
 
             if( maxMsg_default.isSelected() || maxMsg_set.isSelected() ) {
                 if (maxMsg_default.isSelected() == false) {
                     b.setMaxMessageDays(new Integer(maxMsg_value.getText()));
                 } else {
                     b.setMaxMessageDays(null);
                 }
             }
             if( signedOnly_default.isSelected() || signedOnly_true.isSelected() || signedOnly_false.isSelected() ) {
                 if (signedOnly_default.isSelected() == false) {
                     b.setShowSignedOnly(Boolean.valueOf(signedOnly_true.isSelected()));
                 } else {
                     b.setShowSignedOnly(null);
                 }
             }
             if( hideBad_default.isSelected() || hideBad_true.isSelected() || hideBad_false.isSelected() ) {
                 if (hideBad_default.isSelected() == false) {
                     b.setHideBad(Boolean.valueOf(hideBad_true.isSelected()));
                 } else {
                     b.setHideBad(null);
                 }
             }
             if( hideCheck_default.isSelected() || hideCheck_true.isSelected() || hideCheck_false.isSelected() ) {
                 if (hideCheck_default.isSelected() == false) {
                     b.setHideCheck(Boolean.valueOf(hideCheck_true.isSelected()));
                 } else {
                     b.setHideCheck(null);
                 }
             }
             if( hideObserve_default.isSelected() || hideObserve_true.isSelected() || hideObserve_false.isSelected() ) {
                 if (hideObserve_default.isSelected() == false) {
                     b.setHideObserve(Boolean.valueOf(hideObserve_true.isSelected()));
                 } else {
                     b.setHideObserve(null);
                 }
             }
         } else {
             b.setConfigured(false);
         }
 
     }
 
     private void applySettingsToBoard() {
         if (secureBoardRadioButton.isSelected()) {
             String privateKey = privateKeyTextField.getText();
             String publicKey = publicKeyTextField.getText();
             if (publicKey.startsWith("SSK@")) {
                 board.setPublicKey(publicKey);
             } else {
                 board.setPublicKey(null);
             }
             if (privateKey.startsWith("SSK@")) {
                 board.setPrivateKey(privateKey);
             } else {
                 board.setPrivateKey(null);
             }
         } else {
             board.setPublicKey(null);
             board.setPrivateKey(null);
         }
 
         if (overrideSettingsCheckBox.isSelected()) {
             board.setConfigured(true);
             board.setAutoUpdateEnabled(autoUpdateEnabled.isSelected());
             if (maxMsg_default.isSelected() == false) {
                 board.setMaxMessageDays(new Integer(maxMsg_value.getText()));
             } else {
                 board.setMaxMessageDays(null);
             }
             if (signedOnly_default.isSelected() == false) {
                 board.setShowSignedOnly(Boolean.valueOf(signedOnly_true.isSelected()));
             } else {
                 board.setShowSignedOnly(null);
             }
             if (hideBad_default.isSelected() == false) {
                 board.setHideBad(Boolean.valueOf(hideBad_true.isSelected()));
             } else {
                 board.setHideBad(null);
             }
             if (hideCheck_default.isSelected() == false) {
                 board.setHideCheck(Boolean.valueOf(hideCheck_true.isSelected()));
             } else {
                 board.setHideCheck(null);
             }
             if (hideObserve_default.isSelected() == false) {
                 board.setHideObserve(Boolean.valueOf(hideObserve_true.isSelected()));
             } else {
                 board.setHideObserve(null);
             }
         } else {
             board.setConfigured(false);
         }
     }
 
     private void updateBoard(Board b) {
         if( b.isFolder() == false ) {
             MainFrame.getInstance().updateTofTree(b);
             // update the new msg. count for board
             TOF.getInstance().initialSearchNewMessages(b);
 
             if (b == MainFrame.getInstance().getTofTreeModel().getSelectedNode()) {
                 // reload all messages if board is shown
                 MainFrame.getInstance().tofTree_actionPerformed(null);
             }
         } else {
             for(int x=0; x < b.getChildCount(); x++) {
                 Board b2 = (Board)b.getChildAt(x);
                 updateBoard(b2);
             }
         }
     }
 
     /**
      * okButton Action Listener (OK)
      * @param e
      */
     private void okButton_actionPerformed(ActionEvent e) {
         ok();
     }
 
     /* (non-Javadoc)
      * @see java.awt.Window#processWindowEvent(java.awt.event.WindowEvent)
      */
     protected void processWindowEvent(WindowEvent e) {
         if (e.getID() == WindowEvent.WINDOW_CLOSING) {
             dispose();
         }
         super.processWindowEvent(e);
     }
 
     /**
      * radioButton Action Listener (OK)
      * @param e
      */
     private void radioButton_actionPerformed(ActionEvent e) {
         if (publicBoardRadioButton.isSelected()) {
             privateKeyTextField.setEnabled(false);
             publicKeyTextField.setEnabled(false);
             generateKeyButton.setEnabled(false);
         } else {
             privateKeyTextField.setEnabled(true);
             publicKeyTextField.setEnabled(true);
             generateKeyButton.setEnabled(true);
         }
     }
 
     /**
      *
      */
     private void refreshLanguage() {
         if( board.isFolder() ) {
            setTitle("Settings for all boards in folder '"+board.getName()+"'");
         } else {
             setTitle(language.getString("Settings for board") + " '" + board.getName() + "'");
         }
 
         publicBoardRadioButton.setText(language.getString("Public board"));
         secureBoardRadioButton.setText(language.getString("Secure board"));
         okButton.setText(language.getString("OK"));
         cancelButton.setText(language.getString("Cancel"));
         generateKeyButton.setText(language.getString("Generate new keypair"));
 
         overrideSettingsCheckBox.setText(language.getString("Override default settings"));
         maxMsg_default.setText(language.getString("Use default"));
         maxMsg_set.setText(language.getString("Set to") + ":");
         signedOnly_default.setText(language.getString("Use default"));
         signedOnly_true.setText(language.getString("Yes"));
         signedOnly_false.setText(language.getString("No"));
         hideBad_default.setText(language.getString("Use default"));
         hideBad_true.setText(language.getString("Yes"));
         hideBad_false.setText(language.getString("No"));
         hideCheck_default.setText(language.getString("Use default"));
         hideCheck_true.setText(language.getString("Yes"));
         hideCheck_false.setText(language.getString("No"));
         hideObserve_default.setText(language.getString("Use default"));
         hideObserve_true.setText(language.getString("Yes"));
         hideObserve_false.setText(language.getString("No"));
         autoUpdateEnabled.setText(language.getString("Enable automatic board update"));
 
         publicKeyLabel.setText(language.getString("Public key") + " :");
         privateKeyLabel.setText(language.getString("Private key") + " :");
        messageDisplayDaysLabel.setText(
                language.getString("Maximum message display (days)"));
         hideUnsignedMessagesLabel.setText(language.getString("Hide unsigned messages"));
         hideBadMessagesLabel.setText(language.getString("Hide messages flagged BAD"));
         hideCheckMessagesLabel.setText(language.getString("Hide messages flagged CHECK"));
         hideObserveMessagesLabel.setText(language.getString("Hide messages flagged OBSERVE"));
 
         descriptionLabel.setText(language.getString("BoardSettingsFrame.description"));
     }
 
     /**
      * @return
      */
     public boolean runDialog() {
         setModal(true); // paranoia
         setVisible(true);
         return exitState;
     }
 
     /**
      * @param panel
      * @param enabled
      */
     private void setPanelEnabled(JPanel panel, boolean enabled) {
         int componentCount = panel.getComponentCount();
         for (int x = 0; x < componentCount; x++) {
             Component c = panel.getComponent(x);
             if (c != overrideSettingsCheckBox) {
                 c.setEnabled(enabled);
             }
         }
     }
 }
