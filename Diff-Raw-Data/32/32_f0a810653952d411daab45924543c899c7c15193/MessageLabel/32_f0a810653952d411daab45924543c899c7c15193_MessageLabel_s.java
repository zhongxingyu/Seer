 /*
  * Copyright (c) 2006-2012 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.components.statusbar;
 
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.interfaces.ui.StatusBarComponent;
 import com.dmdirc.ui.StatusMessage;
 
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Date;
 import java.util.Queue;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Message label handles showing messages in the status bar.
  */
 public class MessageLabel extends JPanel implements StatusBarComponent,
         MouseListener {
 
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
     private static final long serialVersionUID = 1;
     /** Default status bar message. */
     private final StatusMessage defaultMessage;
     /** Message queue. */
     private final Queue<StatusMessage> queue;
     /** Messsage label. */
     private final JLabel label;
     /** History label. */
     private final MessagePopup historyLabel;
     /** Current status messsage. */
     private StatusMessage currentMessage;
     /** Timer to clear the message. */
     private transient TimerTask messageTimer;
     /** Swing controller. */
     private final SwingController controller;
 
     /**
      * Instantiates a new message label.
      *
      * @param controller Swing controller
      * @param parentWindow Parent window
      */
     public MessageLabel(final SwingController controller,
             final Window parentWindow) {
         super(new MigLayout("fill, ins 0, gap 0  0"));
         this.controller = controller;
         queue = new ConcurrentLinkedQueue<StatusMessage>();
         defaultMessage = new StatusMessage(null, "Ready.", null, -1,
                 controller.getGlobalConfig());
         currentMessage = defaultMessage;
         label = new JLabel();
         historyLabel = new MessagePopup(this, parentWindow, controller);
         label.setText("Ready.");
         label.setBorder(new SidelessEtchedBorder(
                 SidelessEtchedBorder.Side.RIGHT));
         label.addMouseListener(this);
         add(label, "grow, push");
         add(historyLabel, "grow, gapleft 0");
     }
 
     /**
      * Sets the message for this message label.
      *
      * @param message Message object to show
      */
     public void setMessage(final StatusMessage message) {
         queue.add(message);
         if (queue.size() == 1) {
             currentMessage = message;
             updateCurrentMessage();
         }
     }
 
     /**
      * Updates the message label to show the current message info.
      */
     private void updateCurrentMessage() {
         SwingUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 if (currentMessage.getIconType() == null) {
                     label.setIcon(null);
                 } else {
                     label.setIcon(controller.getIconManager().getIcon(
                             currentMessage.getIconType()));
                 }
                 label.setText(UIUtilities.clipStringifNeeded(MessageLabel.this,
                         currentMessage.getMessage(), getWidth()));
                 if (messageTimer != null && (System.currentTimeMillis()
                         - messageTimer.scheduledExecutionTime()) <= 0) {
                     messageTimer.cancel();
                 }
                 if (!defaultMessage.equals(currentMessage)) {
                     messageTimer = new MessageTimerTask(MessageLabel.this);
                     new Timer("SwingStatusBar messageTimer").schedule(
                             messageTimer, new Date(System.currentTimeMillis()
                             + 250 + currentMessage.getTimeout() * 1000L));
                 }
             }
         });
     }
 
     /**
      * Removes the message from the status bar.
      */
     public void clearMessage() {
         historyLabel.addMessage(currentMessage);
         if (queue.size() <= 1) {
             currentMessage = defaultMessage;
         } else {
             currentMessage = queue.poll();
         }
         updateCurrentMessage();
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseClicked(final MouseEvent e) {
         if (currentMessage != null
                 && currentMessage.getMessageNotifier() != null) {
             currentMessage.getMessageNotifier().clickReceived(
                     e.getButton(), e.getClickCount());
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mousePressed(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseReleased(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseEntered(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseExited(final MouseEvent e) {
         //Ignore
     }
 }
