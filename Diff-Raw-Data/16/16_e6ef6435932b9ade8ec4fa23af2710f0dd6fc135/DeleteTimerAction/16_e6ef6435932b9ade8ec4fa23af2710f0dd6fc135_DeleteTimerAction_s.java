 /* $Id: DeleteTimerAction.java,v 1.8 2011-05-06 13:09:57 hampelratte Exp $
  * 
  * Copyright (c) Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project (Lazy Bones) nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones.actions;
 
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import lazybones.LazyBones;
 import lazybones.LazyBonesTimer;
 import lazybones.VDRCallback;
 import lazybones.VDRConnection;
 import lazybones.actions.responses.TimersOutOfSync;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.commands.DELT;
 import org.hampelratte.svdrp.commands.LSTT;
 import org.hampelratte.svdrp.commands.UPDT;
 import org.hampelratte.svdrp.parsers.TimerParser;
 import org.hampelratte.svdrp.responses.R250;
 import org.hampelratte.svdrp.responses.highlevel.Timer;
 
 public class DeleteTimerAction extends VDRAction {
 
     private final LazyBonesTimer timer;
     private boolean forceDeletion = false;
 
     public DeleteTimerAction(LazyBonesTimer timer) {
         this(timer, null);
     }
 
     public DeleteTimerAction(LazyBonesTimer timer, VDRCallback callback) {
         setCallback(callback);
         this.timer = timer;
     }
 
     /**
      * 
      * @param timer
      *            The timer to delete.
      * @param forceDeletion
      *            If set, the timer will be deleted without user confirmation
      */
     public DeleteTimerAction(LazyBonesTimer timer, boolean forceDeletion) {
         this(timer);
         this.forceDeletion = forceDeletion;
     }
 
     @Override
     boolean execute() {
         // snychronize this timer with vdr and check if the timers on the vdr haven't changed
         response = VDRConnection.send(new LSTT(timer.getID()));
         if (response == null || !(response instanceof R250)) {
             return false;
         }
         if (response instanceof R250) {
             List<Timer> timers = TimerParser.parse(response.getMessage());
             Timer vdrTimer = timers.get(0);
             LazyBonesTimer timerFromVDR = new LazyBonesTimer(vdrTimer);
             if (!timerFromVDR.getUniqueKey().equals(timer.getUniqueKey())) {
                 response = new TimersOutOfSync();
                 return false;
             }
         }
 
         // check if timer is recording
         if (timer.hasState(LazyBonesTimer.RECORDING)) {
             if (!forceDeletion) {
                 int result = JOptionPane.showConfirmDialog(LazyBones.getInstance().getParent(),
                         LazyBones.getTranslation("recording_timer_delete", "This timer is currently recording! Do you really want to delete it?"), "",
                         JOptionPane.YES_NO_OPTION);
                 if (result != JOptionPane.OK_OPTION) {
                     // do nothing
                     return true;
                 }
             }
 
             // we have to deactivate the timer before we
             // are able to delete it
             timer.changeStateTo(LazyBonesTimer.ACTIVE, false);
             response = VDRConnection.send(new UPDT(timer));
             if (response == null || !(response instanceof R250)) {
                 return false;
             }
         }
 
         // delete timer
         response = VDRConnection.send(new DELT(timer));
         if (response == null || !(response instanceof R250)) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public Response getResponse() {
         return response;
     }
 
     @Override
     public String getDescription() {
         return "Delete timer " + timer;
     }
 }
