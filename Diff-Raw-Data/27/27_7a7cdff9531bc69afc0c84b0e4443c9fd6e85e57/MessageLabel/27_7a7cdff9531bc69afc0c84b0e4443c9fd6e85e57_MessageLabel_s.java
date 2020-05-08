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
 
 package com.dmdirc.addons.ui_swing.components.statusbar;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.events.StatusBarMessageClearEvent;
 import com.dmdirc.events.StatusBarMessageEvent;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.ui.StatusBarComponent;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.StatusMessage;
 
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Date;
 import java.util.Queue;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.inject.Inject;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Message label handles showing messages in the status bar.
  */
 public class MessageLabel extends JPanel implements StatusBarComponent,
         MouseListener {
 
     private static final Logger LOG = LoggerFactory.getLogger(MessageLabel.class);
     /** Serial version UID. */
     private static final long serialVersionUID = 1;
     /** Default status bar message. */
     private final StatusMessage defaultMessage;
     /** Message queue. */
     private final Queue<StatusMessage> queue;
     /** Message label. */
     private final JLabel label;
     /** History label. */
     private final MessagePopup historyLabel;
     /** Current status message. */
     private StatusMessage currentMessage;
     /** Timer to clear the message. */
     private transient TimerTask messageTimer;
     /** Icon manager to retrieve icons from. */
     private final IconManager iconManager;
     /** The event bus to post status messages on. */
     private final DMDircMBassador eventBus;
 
     /**
      * Instantiates a new message label.
      *
      * @param iconManager  Icon manager to retrieve icons from
      * @param config       Config to read settings from
      * @param parentWindow Parent window
      * @param eventBus     The event bus to post events on.
      */
     @Inject
     public MessageLabel(
             @GlobalConfig final AggregateConfigProvider config,
             @GlobalConfig final IconManager iconManager,
             @MainWindow final Window parentWindow,
             final DMDircMBassador eventBus) {
         super(new MigLayout("fill, ins 0, gap 0  0"));
         this.iconManager = iconManager;
         this.eventBus = eventBus;
         queue = new ConcurrentLinkedQueue<>();
         defaultMessage = new StatusMessage(null, "Ready.", null, -1, config);
         currentMessage = defaultMessage;
         label = new JLabel();
         historyLabel = new MessagePopup(this, parentWindow, iconManager);
         label.setText("Ready.");
         label.setBorder(new SidelessEtchedBorder(SidelessEtchedBorder.Side.RIGHT));
         label.addMouseListener(this);
         add(label, "grow, push");
         add(historyLabel, "grow, gapleft 0");
     }
 
     /**
      * Sets the message for this message label.
      *
      * @param event Event containing message object to show
      */
     @Handler
     public void setMessage(final StatusBarMessageEvent event) {
         LOG.info("Adding message to queue {}", event.getMessage());
         queue.add(event.getMessage());
         LOG.debug("Queue size: {}", queue.size());
         if (queue.size() == 1) {
             LOG.info("Showing only message {}", event.getMessage());
             currentMessage = event.getMessage();
             updateCurrentMessage();
         }
     }
 
     /**
      * Updates the message label to show the current message info.
      */
     private void updateCurrentMessage() {
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 LOG.info("Updating current message: {}", currentMessage);
                 if (currentMessage.getIconType() == null) {
                     label.setIcon(null);
                 } else {
                     label.setIcon(iconManager.getIcon(currentMessage.getIconType()));
                 }
                 label.setText(UIUtilities.clipStringifNeeded(MessageLabel.this,
                         currentMessage.getMessage(), getWidth()));
                 if (messageTimer != null && System.currentTimeMillis()
                         - messageTimer.scheduledExecutionTime() <= 0) {
                     LOG.debug("Cancelling message timer.");
                     messageTimer.cancel();
                 }
                 if (!defaultMessage.equals(currentMessage)) {
                     LOG.debug("Starting new message timer.");
                     messageTimer = new MessageTimerTask(eventBus);
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
     @Handler
     public void clearMessage(final StatusBarMessageClearEvent event) {
         LOG.info("Adding message to history {}", currentMessage);
         historyLabel.addMessage(currentMessage);
         LOG.debug("Queue size: {}", queue.size());
         if (queue.size() <= 1) {
             queue.remove();
             LOG.info("Reverting to default message.");
             currentMessage = defaultMessage;
         } else {
             currentMessage = queue.poll();
             LOG.info("Showing next message in queue: {}", currentMessage);
         }
         updateCurrentMessage();
     }
 
     @Override
     public void mouseClicked(final MouseEvent e) {
         if (currentMessage != null
                 && currentMessage.getMessageNotifier() != null) {
             currentMessage.getMessageNotifier().clickReceived(
                     e.getButton(), e.getClickCount());
         }
     }
 
     @Override
     public void mousePressed(final MouseEvent e) {
         //Ignore
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         //Ignore
     }
 
     @Override
     public void mouseEntered(final MouseEvent e) {
         //Ignore
     }
 
     @Override
     public void mouseExited(final MouseEvent e) {
         //Ignore
     }
 
 }
