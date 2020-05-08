 /**
  * 
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Al Locklear,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ****************************************************************************/
 
 package org.nchelp.meteor.logging;
 
 
 
 /**
 * A custom category factory that returns Logger instances used to guarantee
 * log is initialized.
 * 
 * @version   $Revision$ $Date$
 * @since     Meteor 1.0
 * @author    Alfred Locklear
 * @author    Tim Bornholtz
 * @author    Priority Technologies, Inc.
 * 
 */
 
 public class LoggerFactory implements org.apache.log4j.spi.LoggerFactory {
 	/**
	 * @see CategoryFactory#makeNewCategoryInstance(String)
 	 */
 	public org.apache.log4j.Logger makeNewLoggerInstance(String name) {
 		return new Logger(name);
 	}
 }
