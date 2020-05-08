 /**
  * CCCamp07 ScoringSystem
  * A CTF scoring bot & flag+advisory reporting system
  *
  * (C) 2007, Hans-Christian Esperer
  * hc at hcespererorg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the following
  *   disclaimer in the documentation and/or other materials provided
  *   with the distribution.
  * * Neither the name of the H. Ch. Esperer nor the names of his
  *   contributors may be used to endorse or promote products derived
  *   from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * POSSIBILITY OF SUCH DAMAGE
  **************************************************************************/
 
 package de.sectud.ctf07.scoringsystem;
 
 public final class ComparableBA implements Comparable<ComparableBA> {
 	private final byte[] ipAddress;
 
 	public ComparableBA(byte[] addr) {
 		this.ipAddress = new byte[addr.length];
 		System.arraycopy(addr, 0, ipAddress, 0, addr.length);
 	}
 
 	public int compareTo(ComparableBA o) {
 		/*
 		 * System.out.print("Compare "); for (int i = 0; i < 4; i++) {
 		 * System.out.print((int) ipAddress[i]); } System.out.print(" with ");
 		 * for (int i = 0; i < 4; i++) { System.out.print((int) ipAddress[i]); }
 		 * System.out.println();
 		 */
		for (int i = 0; i < 4; i++) {
 			if (ipAddress[i] < o.ipAddress[i]) {
 				return -1;
 			} else if (ipAddress[i] > o.ipAddress[i]) {
 				return 1;
 			}
 		}
 		return 0;
 	}
 
 }
