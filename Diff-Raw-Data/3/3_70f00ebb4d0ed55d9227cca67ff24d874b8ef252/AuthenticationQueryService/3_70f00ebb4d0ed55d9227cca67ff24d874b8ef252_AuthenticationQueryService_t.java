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
 
 package org.nchelp.meteor.provider.access;
 
import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.nchelp.meteor.logging.Logger;
 import org.nchelp.meteor.provider.AuthenticationProvider;
 import org.nchelp.meteor.registry.Directory;
 import org.nchelp.meteor.registry.DirectoryFactory;
 import org.nchelp.meteor.util.XMLParser;
 import org.nchelp.meteor.util.exception.DirectoryException;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.w3c.dom.Document;
 
 /**
  *   Class AuthenticationQueryService.java
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   May 31, 2002
  */
 public class AuthenticationQueryService {
 
 	private final Logger log = Logger.create(this.getClass().getName());
 
 	/**
 	 * Constructor for AuthenticationQueryService.
 	 */
 	public AuthenticationQueryService() {
 		super();
 	}
 	
 	public String getAuthenticationProviders(int minimumLevel){
 		List aup = null;
 		String strReturn = "<AuthenticationProviders>";
 		
 		try {
 			Directory dir = DirectoryFactory.getInstance().getDirectory();
 			
 			aup = dir.getAuthenticationProviders(minimumLevel);
 		} catch (DirectoryException e) {
 			log.error("Error getting Authentication Providers", e);
			aup = new ArrayList();
 		}
 		
 		Iterator i = aup.iterator();
 		while(i.hasNext()){
 			AuthenticationProvider ap = (AuthenticationProvider)i.next();
 			if(ap.getName() == null){
 				ap.setName(ap.getUrl().toString());
 			}
 			strReturn += "<AuthenticationProvider><Name>" + 
 			             ap.getName() + "</Name>" + 
 			             "<URL>" + ap.getUrl().toString() + "</URL>" +
 			             "</AuthenticationProvider>";
 		}
 		strReturn += "</AuthenticationProviders>";
 		
 		log.debug("Authentication Providers: " + strReturn);
 		return strReturn;
 	}
 
 }
