 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
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
  ********************************************************************************/
 
 package org.nchelp.meteor.provider;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.util.exception.DataException;
 
 /**
 * This is the object that allows the Data Provider calls to be multi threaded
 * 
 * @version   $Revision$ $Date$
 * @since     Meteor1.0
 * 
 */
 public class DataProviderThread extends Thread {
 
 	private final Log log = LogFactory.getLog(this.getClass());
 
 	private DataProvider provider;
 
 	public DataProviderThread(ThreadGroup grp, DataProvider provider) {
 		super (grp, "");
 		this.provider = provider;
 	}
 	
 	public void start() {
 		String name = provider.getName();
 		if(name != null){
 			setName (name);
 		}
 		log.debug ("Starting Thread " + this.toString());
 		super.start();
 	}
 	
 	public void run(){
 		try {
 			provider.getData();
 		} catch(DataException e) {
 			// What can I do here with this exception??
			log.error("Error in Thread [" + provider.getURL() + "] Message [ " + this.toString() + "]", e);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public DataProvider getProvider() {
 		return provider;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return this.getClass().getName() + " Data Provider URL: " + provider.getURL().toString();
 	}
 
 }
