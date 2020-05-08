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
 import com.dmdirc.config.prefs.validator.URIValidator;
 import com.dmdirc.config.prefs.validator.Validator;
 import com.dmdirc.serverlists.ServerGroup;
 
 import java.awt.Window;
 import java.awt.Dialog.ModalityType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.URI;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTree;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Standard input dialog.
  */
 public class AddEntryInputDialog extends StandardDialog {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Entry name Validator. */
     private final Validator<String> entryValidator;
     /** URI Validator. */
     private final Validator<String> uriValidator;
     /** Group name. */
     private ValidatingJTextField entryName;
     /** Network name. */
     private ValidatingJTextField uri;
     /** Blurb label. */
     private TextLabel blurb;
     /** Message. */
     private final String message;
     /** Parent tree. */
     private final JTree items;
     /** Parent model. */
     private final ServerListModel model;
 
     /**
      * Instantiates a new standard input dialog.
      *
      * @param items Parent tree
      * @param owner Dialog owner
      * @param model Server list model
      */
     public AddEntryInputDialog(final Window owner, final JTree items,
             final ServerListModel model) {
         super(owner, ModalityType.MODELESS);
 
         this.items = items;
         this.model = model;
         this.entryValidator = new NotEmptyValidator();
         this.uriValidator = new URIValidator();
         this.message = "Please fill in the entry name and its address";
 
         setTitle("Add new server entry");
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
         DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) items.
                 getSelectionPath().getLastPathComponent();
         while (!((groupNode.getUserObject()) instanceof ServerGroup)) {
             groupNode = (DefaultMutableTreeNode) groupNode.getParent();
         }
         model.addEntry((ServerGroup) groupNode.getUserObject(), getEntryName(),
                 URI.create(getURI()));
         return true;
     }
 
     /**
      * Initialises the components.
      */
     private void initComponents() {
         orderButtons(new JButton(), new JButton());
         entryName = new ValidatingJTextField(entryValidator);
         uri = new ValidatingJTextField(uriValidator);
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
                 entryName.requestFocusInWindow();
             }
 
             /** {@inheritDoc} */
             @Override
             public void windowClosed(final WindowEvent e) {
                 //Ignore
             }
         });
         final DocumentListener dl = new DocumentListener() {
 
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
         };
         entryName.getDocument().addDocumentListener(dl);
         uri.getDocument().addDocumentListener(dl);
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
         getOkButton().setEnabled(!entryValidator.validate(getEntryName())
                 .isFailure() && !uriValidator.validate(getURI()).isFailure());
     }
 
     /**
      * Lays out the components.
      */
     private void layoutComponents() {
         setLayout(new MigLayout("fill, wrap 2"));
 
         add(blurb, "growx, spanx 2");
        add(new JLabel("Item name: "));
         add(entryName, "growx");
         add(new JLabel("Server URI: "));
         add(uri, "growx");
         add(getLeftButton(), "split 3, skip, right");
         add(getRightButton(), "right");
     }
 
     /**
      * Returns the text in the entry name.
      *
      * @return Entry name
      */
     public String getEntryName() {
         return entryName.getText();
     }
 
     /**
      * Returns the URI in the URI field.
      *
      * @return server URI
      */
     public String getURI() {
         return uri.getText();
     }
 }
