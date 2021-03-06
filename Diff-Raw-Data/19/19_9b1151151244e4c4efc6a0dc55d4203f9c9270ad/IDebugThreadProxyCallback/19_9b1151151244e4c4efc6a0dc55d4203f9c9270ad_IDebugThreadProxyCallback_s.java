 /*******************************************************************************
  * Copyright (c) 2009, 2011 Overture Team and others.
  *
  * Overture is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Overture is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Overture.  If not, see <http://www.gnu.org/licenses/>.
  * 	
  * The Overture Tool web-site: http://overturetool.org/
  *******************************************************************************/
 package org.overture.ide.debug.utils.communication;
 
 import org.overture.ide.debug.logging.LogItem;
import org.overturetool.vdmj.scheduler.RunState;
 
 
 public interface IDebugThreadProxyCallback
 {
 	/**
 	 * Step start detail. Indicates a thread was resumed by a step
 	 * into action.
 	 * @since 2.0
 	 */
 	public static final int STEP_INTO= 0x0001;
 	
 	/**
 	 * Step start detail. Indicates a thread was resumed by a step
 	 * over action.
 	 * @since 2.0
 	 */
 	public static final int STEP_OVER= 0x0002;
 	
 	/**
 	 * Step start detail. Indicates a thread was resumed by a step
 	 * return action.
 	 * @since 2.0
 	 */
 	public static final int STEP_RETURN= 0x0004;		
 
 	/**
 	 * Step end detail. Indicates a thread was suspended due
 	 * to the completion of a step action.
 	 */
 	public static final int STEP_END= 0x0008;
 	
 //	/*
 //	 * Called on console output
 //	 * 
 //	 * @param output true on sending and false on receive
 //	 * 
 //	 * @param message the message send
 //	 */
 //	void firePrintMessage(LogItem item);
 //	/*
 //	 * Called on console output
 //	 * 
 //	 * @param output true on sending and false on receive
 //	 * 
 //	 * @param message the message send
 //	 */
 //	void firePrintErrorMessage(LogItem item);
 	
 	void fireLogEvent(LogItem item);
 
 	/*
 	 * Prints stdout
 	 * 
 	 * @param text the text to print
 	 */
 	void firePrintOut(String text);
 
 	/*
 	 * Prints stderr
 	 * 
 	 * @param text the error to print
 	 */
 	void firePrintErr(String text);
 
 	/*
 	 * Raised on breakpoint hit event
 	 */
 	void fireBreakpointHit();
 
 	/*
 	 * Called when debugging is stopped
 	 */
 	void fireStopped();
 
 	/*
 	 * Called when debugging is started
 	 */
 	void fireStarted();
 
 	void fireBreakpointSet(Integer tid, Integer breakpointId);
 
 	void suspended();
 	
 	void deadlockDetected();
 	
 	/**
 	 * Update info about internal state of debugger thread
 	 * @param id The internal debugger id
 	 * @param name The internal name
 	 * @param state The current internal state
 	 */
 	void updateInternalState(String id, String name, RunState state);
 
 	
 
 }
