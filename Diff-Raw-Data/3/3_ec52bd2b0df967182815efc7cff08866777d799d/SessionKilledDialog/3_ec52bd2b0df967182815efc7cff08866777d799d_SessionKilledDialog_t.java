 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client;
 
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SessionKilledDialog extends DialogBox
 {
     protected Galaxy galaxy;
 
     private DockPanel panel = new DockPanel();
 
     // used for UI updates only
     private Timer reconnectTimerUI;
     protected Label timerLabel;
     protected HTML trailingText;
     protected Button loginNowBtn;
 
     private HeartbeatTimer heartbeatTimer;
 
     public SessionKilledDialog(final Galaxy galaxy, final HeartbeatTimer timer)
     {
         heartbeatTimer = timer;
 
         setText("Connection Terminated by Server");
         setStyleName("sessionKilledDialogBox");
 
         loginNowBtn = new Button("Login Now");
         //loginNowBtn.setTitle("Ignore and try to login now (will not work if the server is down)");
         loginNowBtn.setEnabled(heartbeatTimer.isServerUp());
         loginNowBtn.addClickListener(new ClickListener()
         {
             public void onClick(final Widget widget)
             {
                 close();
                // just pointing to root doesn't always work, use the logout trick
                Window.open(GWT.getHostPageBaseURL() + "j_logout", "_self", null);
             }
         });
 
         final Button closeBtn = new Button("Close");
         closeBtn.addClickListener(new ClickListener()
         {
             public void onClick(Widget sender)
             {
                 close();
             }
         });
 
         DockPanel main = new DockPanel();
 
         InlineFlowPanel buttonRow = new InlineFlowPanel();
         buttonRow.addStyleName("buttonRow");
         buttonRow.add(loginNowBtn);
         buttonRow.add(closeBtn);
 
         main.add(buttonRow, DockPanel.SOUTH);
 
         final InlineFlowPanel mainMessage = new InlineFlowPanel();
         mainMessage.addStyleName("padding");
         final HTML text = new HTML("This client connection has been terminated by the server. This could happen due to either:" +
                                    "<ul><li>Server having crashed<li>Client session forcefully killed on the server</ul>" +
                                    "This error is <strong>unrecoverable</strong> and you'll need to re-login. Next " +
                                    "connection attempt will be made in ");
         timerLabel = new Label("" + heartbeatTimer.getIntervalSeconds());
         trailingText = new HTML("&nbsp;seconds.");
         mainMessage.add(text);
         mainMessage.add(timerLabel);
         mainMessage.add(trailingText);
 
         main.add(mainMessage, DockPanel.CENTER);
 
         setWidget(main);
 
         center();
 
         reconnectTimerUI = new Timer()
         {
             public void run()
             {
                 final int update = Integer.parseInt(timerLabel.getText()) - 1;
                 // some language formatting
                 switch (update)
                 {
                     case 1:
                         trailingText.setHTML("&nbsp;second.");
                         timerLabel.setText("" + update); break;
                     case 0:
                         trailingText.setHTML("&nbsp;seconds.");
                         timerLabel.setText("" + update); break;
                     case -1:
                         // time to ping
                         // start heartbeat timer again
                         heartbeatTimer.scheduleRepeating(heartbeatTimer.getIntervalSeconds() * 1000);
                         timerLabel.setText("" + heartbeatTimer.getIntervalSeconds());
                         break;
                     default:
                         timerLabel.setText("" + update); break;
                 }
             }
         };
         reconnectTimerUI.scheduleRepeating(1000); // every second
     }
 
     /**
      * Close, cleanup and send all signals.
      */
     protected void close()
     {
         reconnectTimerUI.cancel();
         heartbeatTimer.onDialogDismissed();
         hide();
     }
 
     public void onServerUp()
     {
         loginNowBtn.setEnabled(true);
     }
 
     public void onServerDown()
     {
         loginNowBtn.setEnabled(false);
     }
 }
