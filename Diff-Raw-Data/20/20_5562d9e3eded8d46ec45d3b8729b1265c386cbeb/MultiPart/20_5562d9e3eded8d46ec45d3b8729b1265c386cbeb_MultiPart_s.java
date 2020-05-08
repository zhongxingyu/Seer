 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.message;
 
 import net.rim.device.api.util.Arrays;
 
 /**
  * Multi-part message part (MIME type: "multipart/????")
  */
 public class MultiPart extends MessagePart {
     private MessagePart[] parts;
     private boolean partMixed;
     private boolean partAlternative;
     private boolean partRelated;
     
     /** Creates a new instance of MultiPart */
     public MultiPart(String mimeSubtype) {
         super("multipart", mimeSubtype);
         partMixed = false;
         partAlternative = false;
         partRelated = false;
         if(mimeSubtype.equalsIgnoreCase("alternative")) {
             partAlternative = true;
         }
         else if(mimeSubtype.equalsIgnoreCase("related")) {
             partRelated = true;
         }
         else {
             // If all else fails, automatically assume a "mixed" part
             partMixed = true;
         }
     }
 
     public void accept(MessagePartVisitor visitor) {
         visitor.visitMultiPart(this);
         if(parts != null) {
             for(int i=0;i<parts.length;i++)
                 parts[i].accept(visitor);
         }
     }
 
     public void addPart(MessagePart part) {
         if(parts == null) {
             parts = new MessagePart[1];
             parts[0] = part;
         }
         else {
             Arrays.add(parts, part);
         }
         part.setParent(this);
     }
     
     public MessagePart[] getParts() {
         return parts;
     }
 
     public boolean isPartMixed() {
         return partMixed;
     }
 
     public boolean isPartAlternative() {
         return partAlternative;
     }
 
     public boolean isPartRelated() {
         return partRelated;
     }
 }
