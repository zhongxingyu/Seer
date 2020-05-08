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
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 import org.logicprobe.LogicMail.util.SerializationUtils;
 
 import net.rim.device.api.util.Arrays;
 
 /**
  * Multi-part message part (MIME type: "multipart/????")
  */
 public class MultiPart extends MimeMessagePart {
     private MimeMessagePart[] parts;
     private boolean partMixed;
     private boolean partAlternative;
     private boolean partRelated;
     private boolean partSigned;
     
     public static String MIXED = "mixed";
     public static String ALTERNATIVE = "alternative";
     public static String RELATED = "related";
     public static String SIGNED = "signed";
     
     /** Creates a new instance of MultiPart */
     public MultiPart(String mimeSubtype, String tag) {
         super("multipart", mimeSubtype, -1, tag);
         partMixed = false;
         partAlternative = false;
         partRelated = false;
         partSigned = false;
         if(mimeSubtype.equalsIgnoreCase(ALTERNATIVE)) {
             partAlternative = true;
         }
         else if(mimeSubtype.equalsIgnoreCase(RELATED)) {
             partRelated = true;
         }
         else if(mimeSubtype.equalsIgnoreCase(SIGNED)) {
             partSigned = true;
         }
         else {
             // If all else fails, automatically assume a "mixed" part
             partMixed = true;
         }
     }
 
     public MultiPart(String mimeSubtype) {
     	this(mimeSubtype, "");
     }
 
     /** Creates a new instance for deserialization */
     public MultiPart() {
     	this("", "");
     }
     
     public void accept(MimeMessagePartVisitor visitor) {
         visitor.visitMultiPart(this);
     }
 
     public void addPart(MimeMessagePart part) {
         if(parts == null) {
             parts = new MimeMessagePart[1];
             parts[0] = part;
         }
         else {
             Arrays.add(parts, part);
         }
         part.setParent(this);
     }
     
     public MimeMessagePart[] getParts() {
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
     
     public boolean isPartSigned() {
         return partSigned;
     }
     
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.message.MimeMessagePart#serialize(java.io.DataOutput)
 	 */
 	public void serialize(DataOutput output) throws IOException {
 		super.serialize(output);
 
 		output.writeBoolean(partMixed);
 		output.writeBoolean(partAlternative);
 		output.writeBoolean(partRelated);
 		output.writeBoolean(partSigned);
 		
		output.writeInt(parts.length);
		for(int i=0; i<parts.length; i++) {
			SerializationUtils.serializeClass(parts[i], output);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.message.MimeMessagePart#deserialize(java.io.DataInput)
 	 */
 	public void deserialize(DataInput input) throws IOException {
 		super.deserialize(input);
 		
 	    partMixed = input.readBoolean();
 	    partAlternative = input.readBoolean();
 	    partRelated = input.readBoolean();
 	    partSigned = input.readBoolean();
 		
 		int partCount = input.readInt();
 		for(int i=0; i<partCount; i++) {
 			MimeMessagePart part = (MimeMessagePart)SerializationUtils.deserializeClass(input);
 			this.addPart(part);
 		}
 	}
 }
