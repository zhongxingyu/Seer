 /*
  * Copyright 2012 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.key2gym.presentation.dialogs;
 
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Window;
 import java.util.List;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import org.key2gym.presentation.panels.forms.FormPanel;
 
 /**
  * A dialog that is displaying a panel form.
  * <p/>
  * This dialog allows to easily show panel forms in a full-featured dialog.
  * The usage of this dialog is preferred over AbstractDialog, when the only 
  * component (except buttons, etc) is a form panel.
  * 
  * @author Danylo Vashchilenko
  */
 public class FormDialog extends AbstractDialog {
 
     /**
      * Constructs a dialog with the parent, form panel and actions.
      *
      * @param parent the parent window of this dialog
      * @param formPanel the form panel to display
      * @param actions actions available in this dialog
      */
     public FormDialog(Window parent, FormPanel formPanel) {
         super(parent, true);
 
         this.formPanel = formPanel;
 
         buildDialog();
     }
 
     /**
      * Builds the dialog.
      * 
      * This method uses the provided panel as the dialog's body.
      * It then builds the buttons panel from actions and places it in the dialog's footer.
      */
     private void buildDialog() {
         setLayout(new FormLayout("4dlu, d:g, 4dlu", "4dlu, f:d:g, 4dlu, d, 4dlu"));
 
         add(formPanel, CC.xy(2, 2));
         add(createButtonsPanel(), CC.xy(2, 4));
 
         pack();
         setLocationRelativeTo(getParent());
     }
 
     public void setActions(List<Action> actions) {
         buttonsPanel.removeAll();
 
         for (Action action : actions) {
             JButton button = new JButton(action);
            button.setPreferredSize(new Dimension(115, 28));
             buttonsPanel.add(button);
         }
         
         pack();
     }
 
     private JPanel createButtonsPanel() {
         buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         return buttonsPanel;
     }
 
     public FormPanel getFormPanel() {
         return formPanel;
     }
     /**
      * Form panel displayed in this dialog.
      */
     private FormPanel formPanel;
     /**
      * Actions to show in the footer.
      */
     private List<AbstractAction> actions;
     private JPanel buttonsPanel;
 }
