 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.dialogs.updater;
 
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 import com.dmdirc.interfaces.LifecycleController;
 import com.dmdirc.updater.components.LauncherComponent;
 
 import java.awt.Dialog.ModalityType;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Prompts the user to restart the client, and restarts the client.
  */
 public class SwingRestartDialog extends StandardDialog implements ActionListener {
 
     /** Serial version UID. */
     private static final long serialVersionUID = -7446499281414990074L;
     /** Life cycle controller. */
     private final LifecycleController lifecycleController;
     /** Informational label. */
     private TextLabel info;
     /** Info text. */
     private final String cause;
 
     /**
      * Dialog to restart the client.
      *
      * @param parentWindow        Parent window
      * @param lifecycleController Life cycle controller, for restarting client
      * @param cause               Reason for restart
      */
     public SwingRestartDialog(final Window parentWindow,
             final LifecycleController lifecycleController, final String cause) {
         super(parentWindow, ModalityType.APPLICATION_MODAL);
         this.cause = cause;
         this.lifecycleController = lifecycleController;
 
         setTitle("Restart needed");
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
         initComponents();
         layoutComponents();
     }
 
     /** Initialise components. */
     private void initComponents() {
         orderButtons(new JButton(), new JButton());
         if (LauncherComponent.isUsingLauncher()) {
             info = new TextLabel("Your client needs to be restarted to " + cause + ".");
         } else {
             info = new TextLabel("Your client needs to be restarted to " + cause
                     + ", but as you do not seem to be using "
                     + "the launcher you will have to restart the client "
                     + "manually, do you wish to close the client?");
         }
         getOkButton().setText("Now");
         getCancelButton().setText("Later");
 
         getOkButton().addActionListener(this);
         getCancelButton().addActionListener(this);
 
         setResizable(false);
     }
 
     /** Layout Components. */
     private void layoutComponents() {
         setLayout(new MigLayout("fill, wrap 2, wmax " + getOwner().getSize().getWidth() + ", pack"));
 
         add(info, "grow, pushx, span 2");
         add(getLeftButton(), "split, right");
         add(getRightButton(), "right");
     }
 
     @Override
     public void validate() {
         super.validate();
         setLocationRelativeTo(getOwner());
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (getOkButton().equals(e.getSource())) {
             lifecycleController.quit(42);
         }
         dispose();
     }
 
 }
