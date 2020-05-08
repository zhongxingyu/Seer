 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.transfer.manager;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.facade.Identity;
 
 
 /**
  * List of processes belonging to one transfer plane element.
  */
 public class ProcessList implements Iterable<Process>
 {
 	public ProcessList()
 	{
 	}
 	
	public synchronized void registerProcess(Process process)
 	{
 		Process existingProcess = getProcess(process.getOwner(), process.getID());
 		
 		// is there already a process registered for the ID?
 		if(existingProcess == null) {
 			mProcesses.addFirst(process);
 		} else {
 			// maybe it is the same process?
 			if(existingProcess != process) {
 				throw new RuntimeException(this +" - Process ID " +process.getID() +" already in use for " +existingProcess +". Can not use it for " +process +".");
 			}
 		}
 	}
 	
 	/**
 	 * Returns a process if it is changeable by the entity and has the given process number.
 	 */
	public synchronized Process getProcess(Identity owner, int processID)
 	{
 		if(mProcesses != null){
 			for(Process process : mProcesses) {
 				if(process.getID() == processID) {
 					// do we filter for owners?
 					if(owner != null) {
 						// check if the entities are the same
 						if(process.isChangableBy(owner)) {
 							return process;
 						}
 					} else {
 						return process;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
	public synchronized boolean unregisterProcess(Process process)
 	{
 		return mProcesses.remove(process);
 	}
 
	public synchronized int size()
 	{
 		return mProcesses.size();
 	}
 	
 	@Override
	public synchronized Iterator<Process> iterator()
 	{
 		if(mProcesses.size() > 0) {
 			return mProcesses.iterator();
 		} else {
 			return null;
 		}
 	}
 
 	public String toString()
 	{
 		return this.getClass().getSimpleName() + ":" + mProcesses.toString();
 	}
 	
 	private LinkedList<Process> mProcesses = new LinkedList<Process>();
 }
