 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
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
 
 package net.sf.okapi.common.resource;
 
import net.sf.okapi.common.IParameters;

 /**
  * Resource associated with the filter event START_SUBDOCUMENT.
  */
 public class StartSubDocument extends BaseNameable {
 
 	private String parentId;
	protected IParameters params;
 	
 	public StartSubDocument (String parentId) {
 		super();
 		this.parentId = parentId;
 	}
 
 	public StartSubDocument (String parentId,
 		String id)
 	{
 		super();
 		this.parentId = parentId;
 		this.id = id;
 	}
 
 	public String getParentId () {
 		return parentId;
 	}
 	
 	public void setParentId (String parentId) {
 		this.parentId = parentId;
 	}
 	
	/**
	 * Sets the parameters for this document.
	 * @param params the object containing the parameters for this document.
	 */
	public void setFilterParameters (IParameters params) { // needed for OpenXMLContentFilter
		this.params = params;
	}

	public IParameters getFilterParameters() {
		return this.params;
	}
 }
