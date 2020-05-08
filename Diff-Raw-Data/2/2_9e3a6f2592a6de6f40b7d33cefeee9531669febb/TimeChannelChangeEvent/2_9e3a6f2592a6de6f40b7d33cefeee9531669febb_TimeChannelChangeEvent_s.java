 /*
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.model;
 
 import org.nees.rpi.vis.DVEvent;
import org.nees.rpi.vis.model.DVDataChannel;
 
 /**
  * An event to be triggered when a time channel change occurs.
  * Intended for use with the DVDataFile class. 
  */
 public class TimeChannelChangeEvent extends DVEvent
 {
 	/** the old time channel before the event was triggered */
 	public final DVDataChannel oldTimeChannel;
 
 	/** the new time channel set that triggered the event */
 	public final DVDataChannel newTimeChannel;
 
 	public TimeChannelChangeEvent(DVDataFile source, DVDataChannel oldTimeChannel, DVDataChannel newTimeChannel)
 	{
 		super(source);
 		this.oldTimeChannel = oldTimeChannel;
 		this.newTimeChannel = newTimeChannel;
 	}
 
 	public DVDataFile getSource()
 	{
 		return (DVDataFile) source;
 	}
 }
