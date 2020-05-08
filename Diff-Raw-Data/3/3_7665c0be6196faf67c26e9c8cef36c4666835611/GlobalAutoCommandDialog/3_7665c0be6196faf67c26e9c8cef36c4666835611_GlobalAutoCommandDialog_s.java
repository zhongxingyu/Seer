 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.dialogs.globalautocommand;
 
 import com.dmdirc.addons.ui_swing.components.IconManager;
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.addons.ui_swing.components.validating.ValidationFactory;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.commandparser.auto.AutoCommand;
 import com.dmdirc.interfaces.ui.GlobalAutoCommandsDialogModel;
 
 import java.awt.Dimension;
 import java.awt.Window;
 
 import javax.inject.Inject;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Command to edit the global {@link AutoCommand} for the client.
  */
 public class GlobalAutoCommandDialog extends StandardDialog {
 
     private final GlobalAutoCommandsDialogModel model;
     private final IconManager iconManager;
     private JScrollPane scrollPane;
     private JTextArea response;
 
     /**
      * Creates a new instance of StandardDialog.
      *
      * @param owner The frame that owns this dialog
      */
     @Inject
     public GlobalAutoCommandDialog(@MainWindow final Window owner,
             final GlobalAutoCommandsDialogModel model,
             final IconManager iconManager) {
         super(owner, ModalityType.MODELESS);
         this.model = model;
         this.iconManager = iconManager;
 
         initComponents();
         layoutComponents();
     }
 
     private void initComponents() {
         scrollPane = new JScrollPane();
         response = new JTextArea();
         scrollPane.setViewportView(response);
     }
 
     private void layoutComponents() {
         setLayout(new MigLayout("fill"));
        add(new TextLabel("These commands will be executed when the client stars."), "wrap, span 2");
         add(ValidationFactory
                 .getValidatorPanel(scrollPane, response, model.getResponseValidator(), iconManager),
                 "span 2, grow, push, wrap");
         add(getLeftButton(), " right");
         add(getRightButton(), "");
         setMinimumSize(new Dimension(500, 550));
     }
 
     @Override
     public void display() {
         new GlobalAutoCommandController()
                 .init(this, model, response, getOkButton(), getCancelButton());
         super.display();
     }
 }
