/* $Id: DeleteTimerAction.java,v 1.1 2007-03-24 19:16:34 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
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
 
 import lazybones.Timer;
 import lazybones.VDRConnection;
 import lazybones.actions.responses.TimersOutOfSync;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.commands.DELT;
 import org.hampelratte.svdrp.commands.LSTT;
 import org.hampelratte.svdrp.commands.UPDT;
 import org.hampelratte.svdrp.responses.R250;
 import org.hampelratte.svdrp.responses.highlevel.VDRTimer;
 import org.hampelratte.svdrp.util.TimerParser;
 
 public class DeleteTimerAction implements VDRAction {
 
     private Timer timer;
     private Response response;
     
     public DeleteTimerAction(Timer timer) {
         this.timer = timer;
     }
     
     public boolean execute() {
         // snychronize this timer with vdr and check if the timers on the vdr haven't changed
         response = VDRConnection.send(new LSTT(Integer.toString(timer.getID())));
         if( response == null || !(response instanceof R250) ) {
             return false;
         }
         if(response instanceof R250) {
             List<VDRTimer> timers = TimerParser.parse(response.getMessage());
             VDRTimer vdrTimer = timers.get(0);
             Timer timerFromVDR = new Timer(vdrTimer);
             if(!timerFromVDR.getUniqueKey().equals(timer.getUniqueKey())) {
                 response = new TimersOutOfSync();
                 return false;
             }
         }
         
         // check if timer is recording
        if(timer.getState() == Timer.RECORDING) {
             // we have to deactivate the timer before we
             // are able to delete it
            timer.setState(Timer.INACTIVE);
             response = VDRConnection.send(new UPDT(timer));
             if( response == null || !(response instanceof R250) ) {
                 return false;
             }
         } 
         
         // delete timer
         response = VDRConnection.send(new DELT(timer));
         if( response == null || !(response instanceof R250) ) {
             return false;
         }
         
         return true;
     }
 
     public Response getResponse() {
         return response;
     }
 }
