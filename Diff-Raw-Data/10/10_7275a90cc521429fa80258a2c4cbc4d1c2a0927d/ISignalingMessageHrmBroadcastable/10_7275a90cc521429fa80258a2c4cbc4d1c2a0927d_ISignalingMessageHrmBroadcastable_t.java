 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical;
 
 /**
 * This abstract interface marks packets as broadcastable and provides some kind of type safety when sendBroadcast*() is used.
  * It is used to enforce the implementation of "duplicate()" for packets, which may be broadcasted
  */
 public interface ISignalingMessageHrmBroadcastable
 {
 	/**
 	 * Clones the packet for broadcasts
 	 */
 	abstract public SignalingMessageHrm duplicate();
 }
