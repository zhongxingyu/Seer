 /***************************************************************************
  * Copyright (c) 2011, 2012 by Sonr Labs Inc (http://www.sonrlabs.com)
  * Questions/Comments: joe@sonrlabs.com
  * 
  *You can redistribute this program and/or modify it under the terms of the GNU General Public License v. 2.0 as published by the Free Software Foundation
  *This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
  *
  **************************************************************************/
 
 package com.sonrlabs.test.sonr.signal;
 
 /**
  * Synchronizes the {@link com.sonrlabs.test.sonr.MicSerialListener} with the dock.
  */
 
 public interface IDockDetector {
    /**
     * @param samples a buffer of audio data read from the microphone.
     * @param count the count of valid data in the buffer.
     * 
    * @return true if the given audio data is sufficient to sync with the dock.
     */
    public boolean findDock(short[] samples, int count);
 }
