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
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.rpc.StatusCodeException;

 import org.mule.galaxy.web.client.util.LightBox;
 
 
 class HeartbeatTimer extends Timer
 {
     private Galaxy galaxy;
 
     private volatile boolean dialogVisible = false;
 
     // TODO get it from some more global config value?
     private int intervalSeconds = 5;
     protected SessionKilledDialog dialog;
     protected boolean serverUp = true;
     protected boolean logged = false;
     
     public HeartbeatTimer(final Galaxy galaxy)
     {
         this.galaxy = galaxy;
         scheduleRepeating(intervalSeconds * 1000); // accepts ms
     }
 
     public void run()
     {
         // TODO check how we can provide more client info, and if it makes sense to
         galaxy.getHeartbeatService().ping("web", new AsyncCallback() {
 
             public void onFailure(final Throwable throwable)
             {
                 serverUp = isSessionKilled(throwable);
 
                 if (!dialogVisible)
                 {
                     dialog = new SessionKilledDialog(galaxy, HeartbeatTimer.this);
                     new LightBox(dialog).show();
                 }
                 else
                 {
                     // cancel, the dialog will trigger this heartbeat timer again periodically
                     cancel();
                 }
 
                 if (serverUp)
                 {
                     dialog.onServerUp();
                 }
                 else
                 {
                     dialog.onServerDown();
                 }
 
                 dialogVisible = true;
 
                 // problem
                 if (!logged) {
                     GWT.log("Server is down or session has been killed by the server", throwable);
                     logged = true;
                 }
             }
 
             public void onSuccess(final Object o)
             {
                 // everything is fine, do nothing
                 // TODO hide panel, go to app root for login
                 logged = false;
             }
         });
     }
 
     public void scheduleRepeating(final int i)
     {
         super.scheduleRepeating(i);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     public int getIntervalSeconds()
     {
         return intervalSeconds;
     }
 
     public void onDialogDismissed()
     {
         dialogVisible = false;
     }
 
     /**
      * A small hack to workaround GWT 1.4.x limitations. Try to guess from the stacktrace if
      * the server is still up and client session has got killed only.
      */
     protected boolean isSessionKilled(Throwable t)
     {
        if (t instanceof StatusCodeException) {
            return false;
        } else if (t instanceof InvocationException) {
            return true;
        }

        // else follow the old method, not too a reliable one
         final String msg = t.getMessage();
         if (msg.indexOf("/j_acegi_security_check") > -1)
         {
             return true;
         }
 
         return false;
     }
 
     public boolean isServerUp()
     {
         return serverUp;
     }
 }
