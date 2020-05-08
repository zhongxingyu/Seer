 /*-
  * Copyright (c) 2009, Derek Konigsberg
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
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 /**
  * Application message part (MIME type: "application/????")
  */
 public class ApplicationPart extends ContentPart {
 
 	public ApplicationPart(String mimeSubtype, String name, String encoding, String disposition, String contentId, int size, String tag) {
 		super("application", mimeSubtype, name, encoding, disposition, contentId, size, tag);
 	}
 	
 	public ApplicationPart(String mimeType, String mimeSubtype, String name, String encoding, String disposition, String contentId, int size) {
 		this(mimeSubtype, name, encoding, disposition, contentId, size, "");
 	}
 	
 	/** Creates a new instance for deserialization */
     public ApplicationPart() {
     	this("", "", "", "", "", -1, "");
     }
 
 	public void accept(MimeMessagePartVisitor visitor) {
		// TODO Auto-generated method stub
 	}
 	
     /* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.util.Serializable#serialize(java.io.DataOutputStream)
 	 */
 	public void serialize(DataOutputStream output) throws IOException {
 		super.serialize(output);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.util.Serializable#deserialize(java.io.DataInputStream)
 	 */
 	public void deserialize(DataInputStream input) throws IOException {
 		super.deserialize(input);
 	}
 }
