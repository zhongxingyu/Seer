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
 
 package com.dmdirc.addons.nickcolours;
 
 import com.dmdirc.addons.ui_swing.components.GenericTableModel;
 import com.dmdirc.addons.ui_swing.components.IconManager;
 import com.dmdirc.addons.ui_swing.components.colours.ColourChooser;
 import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
 import com.dmdirc.ui.messages.ColourManager;
 
 import java.awt.Color;
 import java.awt.Window;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.WindowConstants;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * New nick colour input dialog.
  */
 public class NickColourInputDialog extends StandardDialog {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 1;
     /** Whether or not this is a new entry (as opposed to editing an old one). */
     private boolean isnew;
     /** The row we're editing, if this isn't a new entry. */
     private final int row;
     private final ColourManager colourManager;
     /** The table model to modify entries in. */
     private final GenericTableModel<NickColourEntry> model;
     /** nickname textfield. */
     private JTextField nickname;
     /** network textfield. */
     private JTextField network;
     /** text colour input. */
     private ColourChooser textColour;
 
     /**
      * Creates a new instance of NickColourInputDialog.
      *
      * @param parentWindow  The window that owns this dialog.
      * @param colourManager The colour manager to use to retrieve colours.
      * @param iconManager   The icon manager to use for the dialog icon.
      * @param model         The table model to modify entries in
      * @param row           The row of the table we're editing
      * @param nickname      The nickname that's currently set
      * @param network       The network that's currently set
      * @param textcolour    The text colour that's currently set
      */
     public NickColourInputDialog(
             final Window parentWindow,
             final ColourManager colourManager,
             final IconManager iconManager,
             final GenericTableModel<NickColourEntry> model, final int row,
             final String nickname, final String network,
             final Color textcolour) {
         super(parentWindow, ModalityType.MODELESS);
         this.colourManager = colourManager;
 
         this.model = model;
         this.row = row;
 
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
         initComponents(colourManager, iconManager, nickname, network, textcolour);
         initListeners();
         layoutComponents();
 
         setTitle("Nick colour editor");
         display();
     }
 
     /**
      * Creates a new instance of NickColourInputDialog.
      *
      * @param parentWindow  The window that owns this dialog.
      * @param colourManager The colour manager to use to retrieve colours.
      * @param iconManager   The icon manager to use for the dialog icon.
      * @param model         The table model to modify entries in
      */
     public NickColourInputDialog(
             final Window parentWindow,
             final ColourManager colourManager,
             final IconManager iconManager,
             final GenericTableModel<NickColourEntry> model) {
         this(parentWindow, colourManager, iconManager, model, -1, "", "", null);
 
         isnew = true;
     }
 
     /**
      * Initialises the components.
      *
      * @param defaultNickname   The default value for the nickname text field
      * @param defaultNetwork    The default value for the network text field
      * @param defaultTextColour The default value for the text colour option
      */
     private void initComponents(
             final ColourManager colourManager,
             final IconManager iconManager,
             final String defaultNickname,
             final String defaultNetwork, final Color defaultTextColour) {
         orderButtons(new JButton(), new JButton());
 
         nickname = new JTextField(defaultNickname);
         network = new JTextField(defaultNetwork);
         textColour = new ColourChooser(colourManager, iconManager, NickColourUtils
                 .getStringFromColor(defaultTextColour), true, true);
     }
 
     /** Initialises the listeners. */
     private void initListeners() {
         getOkButton().addActionListener(e -> { saveSettings(); dispose(); });
         getCancelButton().addActionListener(e -> dispose());
     }
 
     /** Lays out the components. */
     private void layoutComponents() {
         setLayout(new MigLayout("wrap 2"));
 
         add(new JLabel("Nickname: "));
         add(nickname, "growx");
 
         add(new JLabel("Network: "));
         add(network, "growx");
 
         add(new JLabel("Text colour: "));
         add(textColour, "growx");
 
         add(getLeftButton(), "right");
         add(getRightButton(), "right");
 
         pack();
     }
 
     /** Saves settings. */
     public void saveSettings() {
         final Color colour = NickColourUtils.getColorFromString(colourManager,
                 textColour.getColour());
         final NickColourEntry entry = NickColourEntry.create(network.getText().toLowerCase(),
                 nickname.getText().toLowerCase(),
                 new Color(colour.getRed(), colour.getGreen(), colour.getBlue()));
         if (isnew) {
            model.replaceValueAt(entry, row);
        } else {
             model.addValue(entry);
         }
     }
 
 }
