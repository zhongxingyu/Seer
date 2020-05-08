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
 
 package com.dmdirc.addons.ui_swing.components;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.EDTInvocation;
 import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
 import com.dmdirc.config.ConfigBinding;
 import com.dmdirc.events.FrameClosingEvent;
 import com.dmdirc.events.ServerAwayEvent;
 import com.dmdirc.events.ServerBackEvent;
 import com.dmdirc.interfaces.Connection;
 
 import javax.swing.JLabel;
 
 import net.engio.mbassy.listener.Handler;
 import net.engio.mbassy.listener.Invoke;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * Simple panel to show when a user is away or not.
  */
 public class AwayLabel extends JLabel {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 2;
     /** Away indicator. */
     private boolean useAwayIndicator;
     /** Parent frame container. */
     private final FrameContainer container;
 
     /**
      * Creates a new away label for the specified container.
      *
      * @param container Parent frame container
      */
     public AwayLabel(final FrameContainer container) {
         super("(away)");
 
         this.container = checkNotNull(container);
 
         container.getConfigManager().getBinder().bind(this, AwayLabel.class);
         container.getEventBus().subscribe(this);
 
        setVisible(false);
         container.getConnection().ifPresent(c -> {
             setVisible(c.isAway());
         });
     }
 
     @ConfigBinding(domain = "ui", key = "awayindicator", invocation = EDTInvocation.class)
     public void handleAwayIndicator(final String value) {
         useAwayIndicator = Boolean.valueOf(value);
         if (!useAwayIndicator) {
             setVisible(false);
         }
     }
 
     @Handler(delivery = Invoke.Asynchronously, invocation = EdtHandlerInvocation.class)
     public void handleAway(final ServerAwayEvent event) {
         container.getConnection().ifPresent(c -> updateVisibility(event.getConnection(), true));
     }
 
     @Handler(delivery = Invoke.Asynchronously, invocation = EdtHandlerInvocation.class)
     public void handleBack(final ServerBackEvent event) {
         container.getConnection().ifPresent(c -> updateVisibility(event.getConnection(), false));
     }
 
     private void updateVisibility(final Connection connection, final boolean away) {
         container.getConnection().ifPresent(c -> {
             if (connection.equals(c) && useAwayIndicator) {
                 setVisible(away);
             }
         });
     }
 
     @Handler(invocation = EdtHandlerInvocation.class)
     public void windowClosing(final FrameClosingEvent event) {
         if (event.getContainer().equals(container)) {
             container.getConfigManager().getBinder().unbind(this);
             container.getEventBus().unsubscribe(this);
         }
     }
 
 }
