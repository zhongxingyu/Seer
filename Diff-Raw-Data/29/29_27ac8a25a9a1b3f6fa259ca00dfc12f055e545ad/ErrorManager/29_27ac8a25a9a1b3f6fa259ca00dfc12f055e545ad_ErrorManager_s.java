 /**
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
 
 package org.nees.rpi.vis;
 
 /**
 * Singleton class to manage errors throughout the app.
  */
public class ErrorManager
 {
	private static ErrorManager instance = new ErrorManager();
	private ErrorManager( ) { }

	public enum ErrorLevels
	{
		SEVERE,
		NOTIFYUSER,
		LOGONLY
	}
 	
	/** Returns the singleton instance of the ErrorManager class */
	public static ErrorManager getInstance( ) { return instance; }
	
	public void reportError(ErrorLevels errorLevel, String errorMsg)
 	{
		System.out.println(errorMsg);
 	}
 }
