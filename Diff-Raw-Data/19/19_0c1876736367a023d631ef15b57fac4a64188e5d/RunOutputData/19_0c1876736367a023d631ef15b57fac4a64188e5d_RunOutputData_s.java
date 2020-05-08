 /*
  * Copyright (c) 2011 The University of Manchester, UK.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice,
  *   this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  *
  * * Neither the names of The University of Manchester nor the names of its
  *   contributors may be used to endorse or promote products derived from this
  *   software without specific prior written permission. 
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
 
 package uk.org.taverna.server.client;
 
 /**
  * The RunOutputData class holds the raw byte data from an output port.
  * 
  * @author Robert Haines
  */
 public final class RunOutputData extends RunOutput<byte[]> {
 
 	/**
	 * Create a new list of URIs.
 	 */
 	public RunOutputData() {
 		super();
 	}
 
 	/**
 	 * Create a new list item with specified URI.
 	 * 
	 * @param uri
	 *            the URI
 	 */
 	public RunOutputData(byte[] data) {
 		super(data);
 	}
 
	// public RunOutputData(String data) {
	// super(data.getBytes());
	// }

 	/**
 	 * Returns a string representation of this RunOutputData. If this node is a
 	 * leaf it simply returns a representation of its value. If it is a branch
 	 * then it recursively calls toString() on each child. The byte data stored
 	 * at each leaf is converted to a String.
 	 * 
 	 * @return a string representation of this RunOutputData.
 	 */
 	@Override
 	public String toString() {
 		String result = "";
 
 		if (isLeaf()) {
 			result = leaf == null ? "null" : new String(leaf);
 		} else {
 			if (branch.size() == 0) {
 				result += "[null]";
 			} else {
 				result += "[" + branch.get(0);
 				for (int i = 1; i < branch.size(); i++) {
 					result += ", " + branch.get(i);
 				}
 				result += "]";
 			}
 		}
 
 		return result;
 	}
 }
