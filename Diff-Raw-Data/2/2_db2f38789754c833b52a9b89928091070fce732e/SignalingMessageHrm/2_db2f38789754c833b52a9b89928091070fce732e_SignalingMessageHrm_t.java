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
 
 import java.io.Serializable;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.packets.LoggableElement;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 public class SignalingMessageHrm extends LoggableElement implements Serializable
 {
 
 	/**
 	 * The name of the message sender. This is always a name of a physical node.
 	 */
 	private Name mSenderName = null;
 	
 	/**
 	 * The name of the message receiver. This always a name of a physical node. 
 	 */
 	private Name mReceiverName = null;
 
 	/**
 	 * For using the class within (de-)serialization. 
 	 */
 	private static final long serialVersionUID = 7253912074438961613L;
 	
 	public SignalingMessageHrm(Name pSenderName, Name pReceiverName)
 	{
 		mSenderName = pSenderName;
 		mReceiverName = pReceiverName;
 		
 		if (HRMConfig.DebugOutput.GUI_SHOW_SIGNALING){
			Logging.log(getClass().getSimpleName() + "(Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + "): CREATED");
 		}
 	}
 	
 	/**
 	 * Determine the name of the message sender
 	 * 
 	 * @return name of the sender
 	 */
 	public Name getSenderName()
 	{
 		return mSenderName;
 	}
 
 	/**
 	 * Determine the name of the message sender
 	 * 
 	 * @return name of the sender
 	 */
 	public Name getReceiverName()
 	{
 		return mReceiverName;
 	}
 	
 	/**
 	 * Returns an object describing string
 	 * 
 	 *  @return the describing string
 	 */
 	@Override
 	public String toString()
 	{
 		return getClass().getSimpleName() + "(Sender=" + getSenderName()  + ", Receiver=" + getReceiverName() + ")";
 	}
 }
