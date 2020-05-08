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
 
 package com.dmdirc.addons.ui_swing.dialogs.aliases;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.addons.ui_swing.components.validating.ValidationFactory;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.commandparser.aliases.Alias;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.interfaces.ui.AliasDialogModel;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.util.validators.NotEmptyValidator;
 
 import java.awt.Dimension;
 import java.awt.Window;
 
 import javax.inject.Inject;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Dialog to list and change command aliases.
  */
 public class AliasManagerDialog extends StandardDialog {
 
     private static final long serialVersionUID = 1;
 
     @Inject
     public AliasManagerDialog(@MainWindow final Window mainFrame,
             final AliasDialogModel model,
             @GlobalConfig final IconManager iconManager,
             final CommandController commandController) {
         super(mainFrame, ModalityType.DOCUMENT_MODAL);
         final AliasManagerLinker linker = new AliasManagerLinker(model, this, iconManager);
         setTitle("Alias Manager");
         final JList<Alias> aliasList = new JList<>();
         final JTextField command = new JTextField();
         final JSpinner argumentsNumber = new JSpinner();
         final JTextArea response = new JTextArea();
         final JButton addAlias = new JButton("Add Alias");
         final JButton deleteAlias = new JButton("Delete Alias");
         getOkButton();
         getCancelButton();
         setMinimumSize(new Dimension(800, 400));
         setPreferredSize(new Dimension(800, 400));
         setLayout(new MigLayout("flowy, fill", "[][][grow]", "[][][][grow][]"));
 
         add(new TextLabel("Aliases allow you to rename commands, to aggregate multiple commands"
                 + "into a single command, or to make shortcuts to common commands."), "spanx, pushy");
         add(new JScrollPane(aliasList),
                 "growy, pushy, spany 3, split 3, wmin 200, wmax 200");
         add(addAlias, "growx");
         add(deleteAlias, "growx, wrap");
 
         add(new JLabel("Command: "), "align label, sgx label");
         add(new JLabel("Minimum Arguments: "), "align label, sgx label");
         add(new JLabel("Response: "), "align label, sgx label, wrap");
 
         add(ValidationFactory.getValidatorPanel(command, model.getCommandValidator(), iconManager),
                 "growx, pushx");
         add(argumentsNumber, "growx, pushx");
        add(ValidationFactory.getValidatorPanel(new JScrollPane(response), response,
                 new NotEmptyValidator(), iconManager), "spanx 2, grow, push");
 
         add(getLeftButton(), "flowx, split 3, right, sg button");
         add(getRightButton(), "sg button");
 
         linker.bindCommandList(aliasList);
         linker.bindCommand(command);
         linker.bindArgumentsNumber(argumentsNumber);
        linker.bindResponse(response);
         linker.bindAddAlias(addAlias);
         linker.bindDeleteAlias(deleteAlias);
         linker.bindOKButton(getOkButton());
         linker.bindCancelButton(getCancelButton());
         model.loadModel();
     }
 
 }
