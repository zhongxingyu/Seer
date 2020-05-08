 /*
  * Copyright (c) 2006-2013 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.components.inputfields;
 
 import com.dmdirc.WritableFrameContainer;
 import com.dmdirc.addons.ui_swing.Apple;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
 import com.dmdirc.commandparser.parsers.CommandParser;
 import com.dmdirc.interfaces.ui.InputField;
 import com.dmdirc.interfaces.ui.UIController;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.ui.input.InputHandler;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.text.JTextComponent;
 
 /**
  * Swing input handler.
  */
 public class SwingInputHandler extends InputHandler implements KeyListener {
 
     /**
      * Creates a new instance of InputHandler. Adds listeners to the target
      * that we need to operate.
      *
      * @param controller The UIController that owns this input handler.
      * @param target The text field this input handler is dealing with.
      * @param commandParser The command parser to use for this text field.
      * @param parentWindow The window that owns this input handler
      */
     public SwingInputHandler(final UIController controller,
             final InputField target,
             final CommandParser commandParser,
             final WritableFrameContainer parentWindow) {
         super(controller, target, commandParser, parentWindow);
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addUpHandler() {
         JTextComponent localTarget = null;
         if (target instanceof JTextComponent) {
             localTarget = (JTextComponent) target;
         } else if (target instanceof SwingInputField) {
             localTarget = ((SwingInputField) target).getTextField();
         } else {
             throw new IllegalArgumentException("Unknown target");
         }
 
         localTarget.getActionMap().put("upArrow", new AbstractAction() {
 
             /** Serial version UID. */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 doBufferUp();
             }
         });
         if (Apple.isAppleUI()) {
             localTarget.getInputMap(JComponent.WHEN_FOCUSED).
                     put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upArrow");
         } else {
             localTarget.getInputMap(
                     JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                     put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upArrow");
         }
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addDownHandler() {
         JTextComponent localTarget = null;
         if (target instanceof JTextComponent) {
             localTarget = (JTextComponent) target;
         } else if (target instanceof SwingInputField) {
             localTarget = ((SwingInputField) target).getTextField();
         } else {
             throw new IllegalArgumentException("Unknown target");
         }
 
         localTarget.getActionMap().put("downArrow", new AbstractAction() {
 
             /** Serial version UID. */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 doBufferDown();
             }
         });
         if (Apple.isAppleUI()) {
             localTarget.getInputMap(JComponent.WHEN_FOCUSED).
                     put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                     "downArrow");
         } else {
             localTarget.getInputMap(
                     JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                     put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
                     "downArrow");
         }
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addTabHandler() {
         final JTextComponent localTarget;
         if (target instanceof JTextComponent) {
             localTarget = (JTextComponent) target;
         } else if (target instanceof SwingInputField) {
             localTarget = ((SwingInputField) target).getTextField();
         } else {
             throw new IllegalArgumentException("Unknown target");
         }
 
         localTarget.getActionMap().put("insert-tab", new AbstractAction() {
 
             /** Serial version UID. */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 new LoggingSwingWorker<Object, Void>() {
 
                     /** {@inheritDoc} */
                     @Override
                     protected Object doInBackground() {
                         localTarget.setEditable(false);
                         doTabCompletion(false);
                         return null;
                     }
 
                     /** {@inheritDoc} */
                     @Override
                     protected void done() {
                         localTarget.setEditable(true);
                     }
                 }.executeInExecutor();
             }
         });
         localTarget.getActionMap().put("insert-shift-tab",
                 new AbstractAction() {
 
             /** Serial version UID. */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 new LoggingSwingWorker<Object, Void>() {
 
                     /** {@inheritDoc} */
                     @Override
                     protected Object doInBackground() {
                         localTarget.setEditable(false);
                         doTabCompletion(true);
                         return null;
                     }
 
                     /** {@inheritDoc} */
                     @Override
                     protected void done() {
                         localTarget.setEditable(true);
                     }
                 }.executeInExecutor();
             }
         });
         localTarget.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "insert-tab");
         localTarget.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                 KeyEvent.SHIFT_MASK), "insert-shift-tab");
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addEnterHandler() {
         JTextComponent localTarget = null;
         if (target instanceof JTextComponent) {
             localTarget = (JTextComponent) target;
         } else if (target instanceof SwingInputField) {
             localTarget = ((SwingInputField) target).getTextField();
         } else {
             throw new IllegalArgumentException("Unknown target");
         }
 
         localTarget.getActionMap().put("enterButton", new AbstractAction() {
 
             /** Serial version UID. */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 final String line = target.getText();
                 target.setText("");
                 UIUtilities.invokeLater(new Runnable() {
 
                     /** {@inheritDoc} */
                     @Override
                     public void run() {
                         final JTextField source;
                         if (e.getSource() instanceof SwingInputField) {
                             source = ((SwingInputField) e.getSource())
                                     .getTextField();
                         } else if (e.getSource() instanceof JTextField) {
                             source = (JTextField) e.getSource();
                         } else {
                             Logger.appError(ErrorLevel.HIGH, "Unable to send "
                                     + "line", new IllegalArgumentException(
                                     "Event is not from known source."));
                             return;
                         }
                         if (source.isEditable()) {
                             new LoggingSwingWorker<Object, Void>() {
 
                                 /** {@inheritDoc} */
                                 @Override
                                 protected Object doInBackground() {
                                     enterPressed(line);
                                     return null;
                                 }
                             }.executeInExecutor();
                         }
                     }
                 });
             }
         });
         localTarget.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                 "enterButton");
     }
 
     /** {@inheritDoc} */
     @Override
     protected void addKeyHandler() {
         target.addKeyListener(this);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Key event
      */
     @Override
     public void keyTyped(final KeyEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Key event
      */
     @Override
     public void keyPressed(final KeyEvent e) {
         if (e.getKeyCode() != KeyEvent.VK_TAB && e.getKeyCode()
                 != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
             final String line = target.getText();
             if (UIUtilities.isCtrlDown(e) && e.getKeyCode() == KeyEvent.VK_ENTER
                     && (flags & HANDLE_RETURN) == HANDLE_RETURN) {
                 target.setText("");
             }
             handleKeyPressed(line, target.getCaretPosition(), e.getKeyCode(),
                     e.isShiftDown(), UIUtilities.isCtrlDown(e));
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Key event
      */
     @Override
     public void keyReleased(final KeyEvent e) {
        validateText();
     }
 }
