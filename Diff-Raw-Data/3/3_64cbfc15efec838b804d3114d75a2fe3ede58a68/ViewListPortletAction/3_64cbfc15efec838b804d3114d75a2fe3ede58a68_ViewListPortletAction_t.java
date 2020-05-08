 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 package org.infoglue.cms.applications.managementtool.actions;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.pluto.om.entity.PortletApplicationEntity;
 import org.apache.pluto.om.entity.PortletApplicationEntityList;
 import org.apache.pluto.om.entity.PortletEntity;
 import org.apache.pluto.portalImpl.services.portletentityregistry.PortletEntityRegistry;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 
 /**
  * @author jand
  */
 public class ViewListPortletAction extends InfoGlueAbstractAction 
 {
 
 	private Hashtable portlets = new Hashtable();
 	
 
 	protected String doExecute() throws Exception 
 	{
	    PortletApplicationEntityList pael = PortletEntityRegistry.getPortletApplicationEntityList();
 	    
 	    for(Iterator it = pael.iterator(); it.hasNext();) {
 	        PortletApplicationEntity pae = (PortletApplicationEntity) it.next();
 	        String app = pae.getId().toString();
 	        
 	        Vector list = new Vector();
 	        for(Iterator it2 = pae.getPortletEntityList().iterator(); it2.hasNext();) {
 		        PortletEntity pe = (PortletEntity) it2.next();
 		        list.add(pe.getId().toString());
 	        }
 	        portlets.put(app, list);
 	    }
 	    
     	return "success";
 	}
 	
 
 	public Map getPortlets()
 	{
 		return this.portlets;		
 	}
 	
 }
