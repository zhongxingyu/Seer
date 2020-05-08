 /*
  * 
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
 
 package com.dmdirc.addons.ui_swing.dialogs;
 
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JButton;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Abstract standard dialog for showing messages.
  */
 public class StandardMessageDialog extends StandardDialog {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Blurb label. */
     private TextLabel blurb;
     /** Message. */
     private String message;
 
     /**
      * Instantiates a new standard input dialog.
      *
      * @param owner Dialog owner
      * @param modal modality type
      * @param title Dialog title
      * @param message Dialog message
      */
     public StandardMessageDialog(Window owner, ModalityType modal,
             final String title, final String message) {
         super(owner, modal);
 
         this.message = message;
 
         setTitle(title);
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
         return true;
     }
 
     /**
      * Called when the dialog's cancel button is clicked, or otherwise closed.
      */
     public void cancelled() {
     }
 
     /**
      * Initialises the components.
      */
     private final void initComponents() {
         orderButtons(new JButton(), new JButton());
         getOkButton().setText("OK");
         blurb = new TextLabel(message);
     }
 
     /**
      * Adds the listeners
      */
     private final void addListeners() {
         final ActionListener listener = new ActionListener() {
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (save()) {
                     dispose();
                 }
             }
         };
         getCancelButton().addActionListener(listener);
         getOkButton().addActionListener(listener);
         addWindowListener(new WindowAdapter() {
 
             /** {@inheritDoc} */
             @Override
             public void windowOpened(WindowEvent e) {
                 //Ignore
             }
 
             /** {@inheritDoc} */
             @Override
             public void windowClosed(WindowEvent e) {
                 cancelled();
             }
         });
     }
 
     /**
      * Lays out the components.
      */
     private final void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1, hidemode 3, wmax min(80sp, 500),"
                + " wmin min(80sp, 500)"));
 
         add(blurb, "growx");
         add(getOkButton(), "right");
     }
 }
