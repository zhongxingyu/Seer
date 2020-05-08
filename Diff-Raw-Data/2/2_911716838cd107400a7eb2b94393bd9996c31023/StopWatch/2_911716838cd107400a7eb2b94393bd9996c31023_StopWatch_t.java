 /*
  * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.util;
 
 /**
  * Class encapsulating measuring time durations just as a stop watch.
  * 
  * @author bastafidli
  */
public class StopWatch 
 {
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Remember start time here.
     */
    private long m_lStartTime;
    
    /**
     * Remember stop time here.
     */
    private long m_lStopTime;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Default constructor.
     * Starts counting from the moment it is constructed.
     */
    public StopWatch(
    )
    {
       reset();
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * @return long - start time;
     */
    public long getStartTime()
    {
       return m_lStartTime;
    }
 
    /**
     * @return - stop time;
     */
    public long getStopTime()
    {
       return m_lStopTime;
    }
 
    /**
     * Reset the counter and start counting from scratch.
     */
    public final void reset(
    )
    {
       m_lStartTime = System.currentTimeMillis();
       m_lStopTime = 0;
    }
 
    /**
     * Stop the timer.
     */
    public void stop(
    )
    {
       m_lStopTime = System.currentTimeMillis();
    }
 
    /**
     * Get timer duration (the timer doesn't stop) in milliseconds.
     * 
     * @return long - difference between stop and start time.
     */
    public long getDuration(
    )
    {
       long lStopTime;
 
       if (m_lStopTime == 0)
       {
          lStopTime = System.currentTimeMillis();
       }
       else
       {
          lStopTime = m_lStopTime;
       }
 
       return lStopTime - m_lStartTime;
    }
 
    /**
     * Print the state of the timer without stopping it.
     * @return String - timing information
     */
    @Override
    public String toString(
    )
    {
       long lTotalMS   = getDuration();
       long lMS        = lTotalMS % 1000;
       long lTotalSecs = lTotalMS / 1000;
       long lSecs      = lTotalSecs % 60;
       long lTotalMins = lTotalSecs / 60;
       long lMinutes   = lTotalMins % 60;
       long lHours     = lTotalMins / 60;
       StringBuilder sbBuffer = new StringBuilder();
 
       if (lHours > 0)
       {
          sbBuffer.append(lHours);
          sbBuffer.append(":");
          sbBuffer.append(lMinutes);
          sbBuffer.append(":");
          sbBuffer.append(lSecs);
          sbBuffer.append(".");
          sbBuffer.append(lMS);
       }
       else if (lMinutes > 0)
       {
          sbBuffer.append(lMinutes);
          sbBuffer.append(":");
          sbBuffer.append(lSecs);
          sbBuffer.append(".");
          sbBuffer.append(lMS);
       }
       else if (lSecs > 0)
       {
          sbBuffer.append(lSecs);
          sbBuffer.append(".");
          sbBuffer.append(lMS);
          sbBuffer.append(" seconds");
       }
       else
       {
          sbBuffer.append(lMS);
          sbBuffer.append(" ms");
       }
       
       return sbBuffer.toString();
    }
 }
