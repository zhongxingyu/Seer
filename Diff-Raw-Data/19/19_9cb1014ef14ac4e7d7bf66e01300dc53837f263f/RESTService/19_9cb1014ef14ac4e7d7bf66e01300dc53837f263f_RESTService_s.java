 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.lib.longhornapi.impl.rest;
 
 import java.net.URI;
 import java.util.ArrayList;
 
 import net.sf.okapi.lib.longhornapi.LonghornProject;
 import net.sf.okapi.lib.longhornapi.LonghornService;
 
 /**
  * Implementation of {@link LonghornService} for the communication with Longhorn's RESTful interface.
  */
 public class RESTService implements LonghornService {
 	private URI baseUri;
 	
 	protected RESTService() {
 	}
 	
	public RESTService(URI baseUri) {
 		this.baseUri = baseUri;
		// Check if service is reachable
		getProjects();
 	}
 	
 	public URI getBaseUri() {
 		return baseUri;
 	}
 
 	@Override
 	public LonghornProject createProject() {
 
 		try {
 			URI projUri = Util.createProject(baseUri);
 			return new RESTProject(projUri);
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public ArrayList<LonghornProject> getProjects() {
 		
 		try {
 			ArrayList<String> projIds = Util.getList(baseUri.toString() + "/projects");
 			ArrayList<LonghornProject> projects = new ArrayList<LonghornProject>();
 			
 			for (String projId : projIds) {
 				projects.add(new RESTProject(baseUri, projId));
 			}
 			
 			return projects;
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
