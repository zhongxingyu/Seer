 // The Grinder
// Copyright (C) 2000  Paco Gomez
// Copyright (C) 2000  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.engine.process;
 
 import net.grinder.plugininterface.PluginProcessContext;
 import net.grinder.plugininterface.PluginThreadContext;
 import net.grinder.util.FilenameFactory;
 import net.grinder.util.GrinderException;
 import net.grinder.util.GrinderProperties;
 import net.grinder.util.ProcessContextImplementation;
 
 
 /**
  * Package scope.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 class ThreadContextImplementation
     extends ProcessContextImplementation
     implements PluginThreadContext
 {
     private GrinderThread m_grinderThread = null;
     private final int m_threadID;
 
     private boolean m_aborted;
     private boolean m_abortedCycle;
     private boolean m_errorOccurred;
     private long m_startTime;
     private long m_elapsedTime;
 
     public ThreadContextImplementation(PluginProcessContext processContext,
 				       int threadID)
     {
 	super(processContext, Integer.toString(threadID));
 
 	m_threadID = threadID;
 
 	reset();
     }
 
     /** Package scope */
     void setGrinderThread(GrinderThread grinderThread) 
     {
 	m_grinderThread = grinderThread;
     }
     
     public void reset()
     {
 	m_aborted = false;
 	m_abortedCycle = false;
 	m_errorOccurred = false;
     }
 
     public boolean getAbortedCycle() {
 	return m_abortedCycle;
     } 
 
     public boolean getAborted() {
 	return m_aborted;
     }
 
     public long getElapsedTime() {
 	return m_elapsedTime;
     }
 
     /*
      * Implementation of PluginThreadContext follows
      */
 
     public int getCurrentCycleID()
     {
 	return m_grinderThread.getCurrentCycle();
     }
 
     public int getThreadID()
     {
 	return m_threadID;
     }
 
     public void abort()
     {
 	m_aborted = true;
     }
 
     public void abortCycle()
     {
 	m_abortedCycle = true;
     }
 
     public void startTimer()
     {
 	// This is to make it more likely that the timed section has a
 	// "clear run".
 	Thread.yield();
 	m_startTime = System.currentTimeMillis();
	m_elapsedTime = 0;
     }
 
     public void stopTimer()
     {
	if (m_elapsedTime == 0)	// Not already stopped.
 	{
 	    m_elapsedTime = System.currentTimeMillis() - m_startTime;
 	}
     }
 
     protected void appendMessageContext(StringBuffer buffer) 
     {
 	buffer.append("(thread ");
 	buffer.append(getThreadID());
 
 	final int currentCycle = getCurrentCycleID();
 	final TestData currentTestData = m_grinderThread.getCurrentTestData();
 
 	if (currentCycle >= 0) {
 	    buffer.append(" cycle " + currentCycle);
 	}
 	
 	if (currentTestData != null) {
 	    buffer.append(" test " +
 			  currentTestData.getTest().getTestNumber());
 	}
 
 	buffer.append(") ");
     }
 }
 
