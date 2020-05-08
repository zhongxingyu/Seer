 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig integrity.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 5th October 2009
  *
  * Changelog:
  * - 05/10/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.rig;
 
 /**
  * Interface for rig exerciser / monitoring operations. The exerciser is a
  * rig <strong>watch-dog</strong> to ensure the rig is within operational
  * tolerance. Exercisers are built chaining together multiple exerciser
  * tests and should test the following rig attributes:
  * 
  * <ul>
  *    <li>Existence (network, device nodes) - can the device be contacted.</li>
  *    <li>Environment (power, air pressure) - does the current environment
  *              support the operating needs of the rig.</li>
  *    <li>Hardware (pistons, actuators, solenoids, motors) - can be the hardware
  *              be commanded to be perform its desired actions.</li>
  *    <li>Sensor (LVDS, magnetostrictive, temperature, time) - does a movement 
  *              of the hardware generate the desired measurable results
  *              within a tolerance of the expected results.</li>
  * </ul>
  *  
  * The <code>setInterval</code> method provides an indication of how often the
  * test should run and the test implementor has discretion to either honour
  * or ignore the test interval. It is suggested, tests which provide a noticeable
  * affect or yield the rig should run at the test interval period, tests which
  * do not, should run often to detect errors and problems at the earliest
  * possible time.
  */
 public interface IRigExerciser
 {
     /**
      * Starts the exerciser tests.
      */
     public void startTests();
     
     /**
      * Stops the exerciser tests.
      */
     public void stopTests();
     
     /**
      * True if the rig exerciser / monitor detects the experiment is within
      * operation tolerance.
      *  
      * @return true if rig monitor is good, false otherwise
      */
     public boolean isMonitorStatusGood();
     
     /**
      * True if the rig has NOT been set to a maintenance
      * state.
      *  
      * @return true if rig not in maintenance, false otherwise
      */
     public boolean isNotInMaintenance();
     
     /**
      * Gets the reason the rig exerciser / monitor is bad. If the rig
      * exerciser is good <code>null</code> is returned.
      * 
      * @return monitor bad reason
      */
     public String getMonitorReason();
     
     /**
      * Gets the reason provided for the rig to be in maintenance mode.
      * If the rig is not in maintenance mode or not reason was provided,
      * <code>null</code> is returned. 
      * 
      * @return in maintenance reason
      */
     public String getMaintenanceReason();
     
     /**
      * Sets the rig test interval. The interval value is how often
      * the tests should be run in minutes.
      * 
      * @param interval test interval in minutes
      * @return true if operation successful, false otherwise
      */
     public boolean setInterval(int interval);
     
     
     /**
      * Sets the maintenance state of the rig. If the passed 
      * <code>offline</code> flag is true, the rig is put into maintenance, 
      * otherwise the rig is taken out of maintenance mode and
      * the rig maintenance reason is cleared if it set.
     * 
      * If the rig is to be put into maintenance, the <code>stopTests</code>
      * flag specifies if the exerciser tests should be run in maintenance mode.
      * If the rig is being taken out of maintenance mode, the
      * <code>stopTests</code> flag is ignored (if the rig is operational, the
      * exerciser should always run).
      *  
      * @param offline true if the rig is put into maintenance mode
      * @param reason a reason why the rig is being put in maintenance mode
      * @param runTests true if rig exerciser tests should be run in 
      *              maintenance mode
      *              
      * @return true if operation successful, false otherwise
      */
     public boolean setMaintenance(boolean offline, String reason, boolean runTests);
 }
