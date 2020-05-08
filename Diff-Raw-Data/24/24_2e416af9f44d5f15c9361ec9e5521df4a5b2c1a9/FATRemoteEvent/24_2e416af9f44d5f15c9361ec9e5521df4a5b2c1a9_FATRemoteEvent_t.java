 /*
  * Copyright (C) 2010 Daniel Jacobi
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.questmaster.fatremote.datastructures;
 
 import java.util.Arrays; 
  
 /**
  * This Datastructure represents an event send or received from/to the FAT+. 
  * 
  * @author daniel
  *
  */
 public class FATRemoteEvent {
 	
 	// FIXME: add toString and equals() and refactor tests!
 	
 	/**
 	 * Command code used by FAT+.
 	 */
 	private short[] mCommand = { 0x48, 0x12, 0x00, 0x00 };
 	
 	/**
 	 * Payload that is included in the command (optional).
 	 */
 	private short[] mPayload = new short[0];
 	
 	/**
 	 * Creates a new event with the two parameters as 3rd and 4th command byte.
 	 * 
 	 * @param cmd2 Command-byte three
 	 * @param cmd3 Command-byte four
 	 */
 	public FATRemoteEvent(short cmd2, short cmd3) {
 		this(cmd2, cmd3, null);
 	}
 
 	/**
 	 * Creates a new event with the two parameters as 3rd and 4th command byte and an optional payload.
 	 * 
 	 * @param cmd2 Command-byte three
 	 * @param cmd3 Command-byte four
 	 * @param payload Command payload
 	 */
 	public FATRemoteEvent(short cmd2, short cmd3, short[] payload) {
 		mCommand[2] = cmd2;
 		mCommand[3] = cmd3;
 		
 		if (payload != null) {
 			mPayload = payload.clone();
 		}
 	}
 
 	/**
 	 * Returns the command bytes as array.
 	 * 
 	 * @return Command-bytes in short array
 	 */
 	public short[] getRemoteCode() {
 		return mCommand.clone();
 	}
 	
 	/**
 	 * Returns the payload of the event.
 	 * 
 	 * @return Payload
 	 */
 	public short[] getCodePayload() {
 		return mPayload.clone();
 	}
 	
 	/**
 	 * Returns a string representation of the object. In general, 
 	 * the {@code toString} method returns a string that "textually represents" 
 	 * this object. The result should be a concise but informative 
 	 * representation that is easy for a person to read.
      *
 	 * The {@code toString} method for class {@code FATRemoteEvent} returns a 
 	 * string consisting of the four RemoteCode entries.
 	 * In other words, this method returns a string equal to the value of:
      *
 	 * {@code "{" + command[0] + "," + command[1] + "," + command[2] + "," + command[3] + "}"}
 	 *
 	 * @returns a string representation of the object.
 	 */
 	@Override
 	public String toString() {
 		short command[] = this.getRemoteCode();
 		return "{" + command[0] + "," + command[1] + "," + command[2] + "," + command[3] + "}";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
      * @param other {@inheritDoc}
 	 * @returns {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object other) {
 		if (this == other) {
 			return true;
 		}
 
 		if ( other instanceof FATRemoteEvent) {
 			FATRemoteEvent evt = (FATRemoteEvent) other;
			if ( Arrays.equals(this.getRemoteCode(), evt.getRemoteCode())) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @returns {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		int hash = 0;
 		
 		for (short s: this.getCodePayload()) {
 			hash += Short.valueOf(s).hashCode();	
 		}
 		
 		return hash;
 	}
 	
 }
