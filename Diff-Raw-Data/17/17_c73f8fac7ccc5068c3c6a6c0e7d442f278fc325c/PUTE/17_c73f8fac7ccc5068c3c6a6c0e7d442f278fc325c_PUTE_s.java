 /* $Id$
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
 package de.hampelratte.svdrp.commands;
 
 import de.hampelratte.svdrp.Command;
 
 /**
  * Command to put data into the EPG list of the VDR
  * 
  * @author <a href="mailto:henrik.niehaus@gmx.de">Henrik Niehaus </a>
  * 
  */
 public class PUTE extends Command {
 
     private String data = "";
 
     /**
      * Command to put data into the EPG list of the VDR
      * 
      * @param data
      *            The EPG data to enter. The data entered has to strictly follow
      *            the format defined in the man page vdr(5) for the 'epg.data'
      *            file.
      */
     public PUTE(String data) {
         this.data = data;
     }
 
     public String getCommand() {
        String cmd = "PUTE " + data + "\n.";
         return cmd.trim();
     }
 
     public String toString() {
         return "PUTE";
     }
 
     /**
      * Returns the EPG data to send to the VDR
      * 
      * @return The EPG data to send to the VDR
      */
     public String getData() {
         return data;
     }
 
     /**
      * Sets the EPG data to send to the VDR
      * 
      * @param data
      *            The EPG data to enter. The data entered has to strictly follow
      *            the format defined in the man page vdr(5) for the 'epg.data'
      *            file.
      */
     public void setData(String data) {
         this.data = data;
     }
 }
