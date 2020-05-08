 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.ui_swing.components.frames;
 
 import com.dmdirc.WritableFrameContainer;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.CopyAction;
 import com.dmdirc.addons.ui_swing.actions.CutAction;
 import com.dmdirc.addons.ui_swing.actions.InputFieldCopyAction;
 import com.dmdirc.addons.ui_swing.actions.InputTextFramePasteAction;
 import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
 import com.dmdirc.addons.ui_swing.dialogs.paste.PasteDialog;
 import com.dmdirc.config.ConfigManager;
 import com.dmdirc.interfaces.AwayStateListener;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.ui.input.InputHandler;
 import com.dmdirc.ui.interfaces.InputWindow;
 
 import java.awt.BorderLayout;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.KeyStroke;
 
 import net.miginfocom.layout.PlatformDefaults;
 
 /**
  * Frame with an input field.
  */
 public abstract class InputTextFrame extends TextFrame implements InputWindow,
         AwayStateListener, MouseListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 2;
     /** Input field panel. */
     protected JPanel inputPanel;
     /** Away label. */
     protected JLabel awayLabel;
     /** The InputHandler for our input field. */
     private InputHandler inputHandler;
     /** Frame input field. */
     private SwingInputField inputField;
     /** Popupmenu for this frame. */
     private JPopupMenu inputFieldPopup;
     /** Nick popup menu. */
     protected JPopupMenu nickPopup;
     /** Away indicator. */
     private boolean useAwayIndicator;
 
     /**
      * Creates a new instance of InputFrame.
      *
      * @param owner WritableFrameContainer owning this frame.
      * @param controller Swing controller
      */
     public InputTextFrame(final SwingController controller,
             final WritableFrameContainer<? extends InputWindow> owner) {
         super(owner, controller);
 
         initComponents();
 
         final ConfigManager config = owner.getConfigManager();
 
         if (!UIUtilities.isGTKUI()) {
             //GTK users appear to dislike choice, ignore them if they want some.
             getInputField().setBackground(config.getOptionColour(
                     "ui", "inputbackgroundcolour",
                     "ui", "backgroundcolour"));
             getInputField().setForeground(config.getOptionColour(
                     "ui", "inputforegroundcolour",
                     "ui", "foregroundcolour"));
             getInputField().setCaretColor(config.getOptionColour(
                     "ui", "inputforegroundcolour",
                     "ui", "foregroundcolour"));
         }
         useAwayIndicator = config.getOptionBool("ui", "awayindicator");
 
         config.addChangeListener("ui", "inputforegroundcolour", this);
         config.addChangeListener("ui", "inputbackgroundcolour", this);
         config.addChangeListener("ui", "awayindicator", this);
         if (getContainer().getServer() != null) {
             getContainer().getServer().addAwayStateListener(this);
         }
 
         getInputField().getTextField().getInputMap().put(KeyStroke.getKeyStroke(
                 KeyEvent.VK_C, UIUtilities.getCtrlMask()), "textpaneCopy");
         getInputField().getTextField().getInputMap().put(KeyStroke.getKeyStroke(
                 KeyEvent.VK_C, UIUtilities.getCtrlMask()
                 | KeyEvent.SHIFT_DOWN_MASK), "textpaneCopy");
         getInputField().getTextField().getActionMap().put("textpaneCopy",
                 new InputFieldCopyAction(getTextPane(),
                 getInputField().getTextField()));
     }
 
     /**
      * Initialises the components for this frame.
      */
     private void initComponents() {
         setInputField(new SwingInputField(getController().getMainFrame()));
 
         getInputField().addMouseListener(this);
 
         initPopupMenu();
         nickPopup = new JPopupMenu();
 
         awayLabel = new JLabel();
         awayLabel.setText("(away)");
         awayLabel.setVisible(false);
 
         inputPanel = new JPanel(new BorderLayout(
                 (int) PlatformDefaults.getUnitValueX("related").getValue(),
                 (int) PlatformDefaults.getUnitValueX("related").getValue()));
         inputPanel.add(awayLabel, BorderLayout.LINE_START);
         inputPanel.add(inputField, BorderLayout.CENTER);
 
         initInputField();
     }
 
     /** Initialises the popupmenu. */
     private void initPopupMenu() {
         inputFieldPopup = new JPopupMenu();
 
         inputFieldPopup.add(new CutAction(getInputField().getTextField()));
         inputFieldPopup.add(new CopyAction(getInputField().getTextField()));
         inputFieldPopup.add(new InputTextFramePasteAction(this));
         inputFieldPopup.setOpaque(true);
         inputFieldPopup.setLightWeightPopupEnabled(true);
     }
 
     /**
      * Initialises the input field.
      */
     private void initInputField() {
         UIUtilities.addUndoManager(getInputField().getTextField());
 
         getInputField().getActionMap().put("paste",
                 new InputTextFramePasteAction(this));
         getInputField().getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(
                 "shift INSERT"),
                 "paste");
         getInputField().getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(
                 "ctrl V"),
                 "paste");
     }
 
     /**
      * Returns the container associated with this frame.
      *
      * @return This frame's container.
      */
     @Override
     @SuppressWarnings("unchecked")
     public WritableFrameContainer<? extends InputWindow> getContainer() {
         return (WritableFrameContainer<? extends InputWindow>) super.
                 getContainer();
     }
 
     /**
      * Returns the input handler associated with this frame.
      *
      * @return Input handlers for this frame
      */
     @Override
     public final InputHandler getInputHandler() {
         return inputHandler;
     }
 
     /**
      * Sets the input handler for this frame.
      *
      * @param newInputHandler input handler to set for this frame
      */
     public final void setInputHandler(final InputHandler newInputHandler) {
         this.inputHandler = newInputHandler;
         inputHandler.addValidationListener(inputField);
         inputHandler.setTabCompleter(((WritableFrameContainer<?>) frameParent).
                 getTabCompleter());
     }
 
     /**
      * Returns the input field for this frame.
      *
      * @return SwingInputField input field for the frame.
      */
     public final SwingInputField getInputField() {
         return inputField;
     }
 
     /**
      * Sets the frames input field.
      *
      * @param newInputField new input field to use
      */
     protected final void setInputField(final SwingInputField newInputField) {
         this.inputField = newInputField;
     }
 
     /**
      * Returns the away label for this server connection.
      *
      * @return JLabel away label
      */
     public JLabel getAwayLabel() {
         return awayLabel;
     }
 
     /**
      * Sets the away indicator on or off.
      *
      * @param awayState away state
      *
      * @deprecated Use {@link AwayStateListener}s to listen for changes instead
      */
     @Override
     @Deprecated
     public void setAwayIndicator(final boolean awayState) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param mouseEvent Mouse event
      */
     @Override
     public void mouseClicked(final MouseEvent mouseEvent) {
         if (mouseEvent.getSource() == getTextPane()) {
             processMouseEvent(mouseEvent);
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param mouseEvent Mouse event
      */
     @Override
     public void mousePressed(final MouseEvent mouseEvent) {
         processMouseEvent(mouseEvent);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param mouseEvent Mouse event
      */
     @Override
     public void mouseReleased(final MouseEvent mouseEvent) {
         processMouseEvent(mouseEvent);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param mouseEvent Mouse event
      */
     @Override
     public void mouseExited(final MouseEvent mouseEvent) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param mouseEvent Mouse event
      */
     @Override
     public void mouseEntered(final MouseEvent mouseEvent) {
         //Ignore
     }
 
     /**
      * Processes every mouse button event to check for a popup trigger.
      *
      * @param e mouse event
      */
     @Override
     public void processMouseEvent(final MouseEvent e) {
         if (e.isPopupTrigger() && e.getSource() == getInputField()) {
             final Point point = getInputField().getMousePosition();
 
             if (point != null) {
                 initPopupMenu();
                 inputFieldPopup.show(this, (int) point.getX(),
                         (int) point.getY() + getTextPane().getHeight()
                         + (int) PlatformDefaults.
                         getUnitValueX("related").getValue());
             }
         }
     }
 
     /** Checks and pastes text. */
     public void doPaste() {
         String clipboard = null;
 
         try {
             if (!Toolkit.getDefaultToolkit().getSystemClipboard().
                     isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                 return;
             }
         } catch (IllegalStateException ex) {
             Logger.userError(ErrorLevel.LOW, "Unable to paste from clipboard.");
             return;
         }
 
         try {
             //get the contents of the input field and combine it with the
             //clipboard
             clipboard = (String) Toolkit.getDefaultToolkit().
                     getSystemClipboard().getData(DataFlavor.stringFlavor);
             doPaste(clipboard);
         } catch (IOException ex) {
             Logger.userError(ErrorLevel.LOW,
                     "Unable to get clipboard contents: " + ex.getMessage());
         } catch (UnsupportedFlavorException ex) {
             Logger.userError(ErrorLevel.LOW, "Unsupported clipboard type", ex);
         }
     }
 
     /**
      * Pastes the specified content into the input area.
      *
      * @param clipboard The contents of the clipboard to be pasted
      * @since 0.6.3m1
      */
     protected void doPaste(final String clipboard) {
         final String inputFieldText = getInputField().getText();
         //Get the text that would result from the paste (inputfield
         //- selection + clipboard)
         final String text = inputFieldText.substring(0, getInputField().
                 getSelectionStart()) + clipboard + inputFieldText.substring(
                 getInputField().getSelectionEnd());
         final String[] clipboardLines = getSplitLine(text);
         //check theres something to paste
         if (clipboardLines.length > 1) {
             //Clear the input field
             inputField.setText("");
             final Integer pasteTrigger = getContainer().getConfigManager().
                     getOptionInt("ui", "pasteProtectionLimit", false);
             //check whether the number of lines is over the limit
             if (pasteTrigger != null && getContainer().getNumLines(text)
                     > pasteTrigger) {
                 //show the multi line paste dialog
                 new PasteDialog(this, text, getController().getMainFrame()).
                         display();
             } else {
                 //send the lines
                 for (String clipboardLine : clipboardLines) {
                     getContainer().sendLine(clipboardLine);
                 }
             }
         } else {
             //put clipboard text in input field
             inputField.replaceSelection(clipboard);
         }
     }
 
     /**
      * Splits the line on all line endings.
      *
      * @param line Line that will be split
      *
      * @return Split line array
      */
     private String[] getSplitLine(final String line) {
         return line.replace("\r\n", "\n").replace('\r', '\n').split("\n");
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         super.configChanged(domain, key);
 
         if ("ui".equals(domain) && getContainer().getConfigManager() != null) {
             if (getInputField() != null && !UIUtilities.isGTKUI()) {
                 if ("inputbackgroundcolour".equals(key)
                         || "backgroundcolour".equals(key)) {
                     getInputField().setBackground(getContainer().
                             getConfigManager().getOptionColour(
                             "ui", "inputbackgroundcolour",
                             "ui", "backgroundcolour"));
                 } else if ("inputforegroundcolour".equals(key)
                         || "foregroundcolour".equals(key)) {
                     getInputField().setForeground(getContainer().
                             getConfigManager().getOptionColour(
                             "ui", "inputforegroundcolour",
                             "ui", "foregroundcolour"));
                     getInputField().setCaretColor(getContainer().
                             getConfigManager().getOptionColour(
                             "ui", "inputforegroundcolour",
                             "ui", "foregroundcolour"));
                 }
             }
             if ("awayindicator".equals(key)) {
                 useAwayIndicator = getContainer().getConfigManager().
                         getOptionBool("ui", "awayindicator");
             }
         }
     }
 
     /** Request input field focus. */
     public void requestInputFieldFocus() {
         if (inputField != null) {
             inputField.requestFocusInWindow();
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void onAway(final String reason) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 if (useAwayIndicator) {
                     awayLabel.setVisible(true);
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void onBack() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 if (useAwayIndicator) {
                     awayLabel.setVisible(false);
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void close() {
         super.close();
 
         if (getContainer() != null && getContainer().getServer() != null) {
             getContainer().getServer().removeAwayStateListener(this);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void activateFrame() {
         super.activateFrame();
         if (useAwayIndicator && getContainer().getServer() != null) {
             awayLabel.setVisible(getContainer().getServer().isAway());
         }
         inputField.requestFocusInWindow();
     }
 
 
 }
