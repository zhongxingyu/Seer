 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.serverlist;
 
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.config.prefs.validator.NotEmptyValidator;
 import com.dmdirc.config.prefs.validator.Validator;
 import com.dmdirc.serverlists.ServerGroup;
 
 import java.awt.Window;
 import java.awt.Dialog.ModalityType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Standard input dialog.
  */
 public class AddGroupInputDialog extends StandardDialog {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Validator. */
     private final Validator<String> validator;
     /** Group name. */
     private ValidatingJTextField groupName;
     /** Network name. */
     private JTextField networkName;
     /** Blurb label. */
     private TextLabel blurb;
     /** Message. */
     private final String message;
     /** Parent tree. */
     private final JTree tree;
     /** Parent model. */
     private final ServerListModel serverListModel;
 
     /**
      * Instantiates a new standard input dialog.
      *
      * @param items Parent tree
      * @param owner Dialog owner
      * @param model Server list model
      */
     public AddGroupInputDialog(final Window owner, final JTree items,
             final ServerListModel model) {
         super(owner, ModalityType.MODELESS);
 
         this.tree = items;
         this.serverListModel = model;
         this.validator = new NotEmptyValidator();
         this.message = "Please fill in the group name and its network name";
 
         setTitle("Add new server group");
         setDefaultCloseOperation(StandardInputDialog.DISPOSE_ON_CLOSE);
 
         initComponents();
         addListeners();
         layoutComponents();
     }
 
     /**
      * Called when the dialog's OK button is clicked.
      *
      * @return whether the dialog can close
      */
     public boolean save() {
         DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) tree.
                 getSelectionPath().getLastPathComponent();
         if (groupNode == null) {
             groupNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
         } else {
             while (!((groupNode.getUserObject()) instanceof ServerGroup)) {
                if (groupNode.getParent() == null) {
                    groupNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
                    break;
                } else {
                    groupNode = (DefaultMutableTreeNode) groupNode.getParent();
                }
             }
         }
         if (groupNode == tree.getModel().getRoot()) {
             serverListModel.addGroup(null, getGroupName(), getNetworkName());
         } else {
             serverListModel.addGroup((ServerGroup) groupNode.getUserObject(),
                     getGroupName(), getNetworkName());
         }
         return true;
     }
 
     /**
      * Initialises the components.
      */
     private void initComponents() {
         orderButtons(new JButton(), new JButton());
         groupName = new ValidatingJTextField(validator);
         networkName = new JTextField();
         blurb = new TextLabel(message);
         validateText();
     }
 
     /**
      * Adds the listeners.
      */
     private void addListeners() {
         getOkButton().addActionListener(new ActionListener() {
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 if (save()) {
                     dispose();
                 }
             }
         });
         getCancelButton().addActionListener(new ActionListener() {
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 dispose();
             }
         });
         addWindowListener(new WindowAdapter() {
 
             /** {@inheritDoc} */
             @Override
             public void windowOpened(final WindowEvent e) {
                 groupName.requestFocusInWindow();
             }
 
             /** {@inheritDoc} */
             @Override
             public void windowClosed(final WindowEvent e) {
                 //Ignore
             }
         });
         groupName.getDocument().addDocumentListener(new DocumentListener() {
 
             /** {@inheritDoc} */
             @Override
             public void insertUpdate(final DocumentEvent e) {
                 validateText();
             }
 
             /** {@inheritDoc} */
             @Override
             public void removeUpdate(final DocumentEvent e) {
                 validateText();
             }
 
             /** {@inheritDoc} */
             @Override
             public void changedUpdate(final DocumentEvent e) {
                 //Ignore
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean enterPressed() {
         executeAction(getOkButton());
         return true;
     }
 
     /**
      * Validates the change.
      */
     private void validateText() {
         getOkButton().setEnabled(!validator.validate(getGroupName())
                 .isFailure());
     }
 
     /**
      * Lays out the components.
      */
     private void layoutComponents() {
         setLayout(new MigLayout("fill, wrap 2"));
 
         add(blurb, "growx, spanx 2");
         add(new JLabel("Group name: "));
         add(groupName, "growx");
         add(new JLabel("Network name: "));
         add(networkName, "growx");
         add(getLeftButton(), "split 3, skip, right");
         add(getRightButton(), "right");
     }
 
     /**
      * Returns the text in the group name.
      *
      * @return Group name
      */
     public String getGroupName() {
         return groupName.getText();
     }
 
     /**
      * Returns the text in the network name.
      *
      * @return Network name
      */
     public String getNetworkName() {
         return networkName.getText();
     }
 }
