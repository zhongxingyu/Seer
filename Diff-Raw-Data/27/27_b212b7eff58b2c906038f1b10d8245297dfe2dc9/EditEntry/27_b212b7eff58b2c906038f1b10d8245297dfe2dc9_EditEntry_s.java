 /*
 Copyright (c) 2005, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.ctl;
 
 import com.justjournal.db.*;
 
 import java.util.Collection;
 
 /**
  * Created by IntelliJ IDEA.
  * User: laffer1
  * Date: Jan 1, 2004
  * Time: 8:46:20 PM
  */
 public class EditEntry extends Protected {
 
     protected int entryId;
     protected EntryTo entry;
     protected Collection security;
     protected Collection location;
     protected Collection moods;
 
     public int getEntryId() {
         return this.entryId;
     }
 
     public void setEntryId(int entryId) {
         this.entryId = entryId;
     }
 
     public EntryTo getEntry() {
         return this.entry;
     }
 
     public Collection getSecurity() {
         return this.security;
     }
 
     public Collection getLocation() {
         return this.location;
     }
 
     public Collection getMoods() {
         return this.moods;
     }
 
     public String getMyLogin() {
         return this.currentLoginName();
     }
 
     protected String insidePerform() throws Exception {
        final EntryDAO edao = new EntryDAO();
        final SecurityDao sdao = new SecurityDao();
        final MoodDao mdao = new MoodDao();
        final LocationDao ldao = new LocationDao();
 
         if (this.entryId < 1)
             addError("entryId", "The entry id was invalid.");
 
         if (this.currentLoginId() < 1)
             addError("login", "The login timed out or is invalid.");
 
        if (this.hasErrors() == false) {
            this.entry = edao.viewSingle(this.entryId, this.currentLoginId());
            this.security = sdao.view();
            this.moods = mdao.view();
            this.location = ldao.view();
         }
 
         if (this.hasErrors())
             return ERROR;
         else
             return SUCCESS;
     }
 
 }
