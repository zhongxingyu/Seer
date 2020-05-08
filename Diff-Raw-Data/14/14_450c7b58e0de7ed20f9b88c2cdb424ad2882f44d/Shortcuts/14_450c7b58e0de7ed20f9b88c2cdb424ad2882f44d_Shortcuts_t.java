 package com.optaros.alfresco.docasu.wcs;
 
 /*
  *    Copyright (C) 2008 Optaros, Inc. All rights reserved.
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program. If not, see <http://www.gnu.org/licenses/>.
  *    
  */
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.alfresco.model.ApplicationModel;
 import org.alfresco.model.ContentModel;
 import org.alfresco.service.cmr.repository.ChildAssociationRef;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.repository.NodeService;
 import org.alfresco.service.namespace.NamespaceService;
 import org.alfresco.service.namespace.QName;
 import org.alfresco.web.scripts.DeclarativeWebScript;
 import org.alfresco.web.scripts.WebScriptRequest;
 import org.alfresco.web.scripts.WebScriptStatus;
 import org.apache.commons.logging.LogFactory;
 
 public class Shortcuts extends DeclarativeWebScript {
 	@SuppressWarnings("unused")
 	private static final org.apache.commons.logging.Log log = LogFactory.getLog(Shortcuts.class);
 	
 	private static final QName TYPE_PREFERENCES = QName.createQName(NamespaceService.APP_MODEL_1_0_URI,
 			"preferences");
 	
 	private static final QName PROP_SHORTCUTS = QName.createQName(NamespaceService.APP_MODEL_1_0_URI,
 	"shortcuts");
 	
 	private NodeService nodeService;
 	
 	@SuppressWarnings("unchecked")
 	public Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status) {
 
 		NodeRef person = getRepositoryContext().getPerson();
 		
 		List<ChildAssociationRef> configurationList =
 			nodeService.getChildAssocs(person, ApplicationModel.ASSOC_CONFIGURATIONS,
 					ApplicationModel.ASSOC_CONFIGURATIONS);
 		
 		NodeRef configurations = null;
 		
 		if (configurationList.isEmpty()) {
 			log.debug("Creating configurations");
 			configurations = nodeService.createNode(person, ApplicationModel.ASSOC_CONFIGURATIONS,
 					ApplicationModel.ASSOC_CONFIGURATIONS, ApplicationModel.TYPE_CONFIGURATIONS,
 					null).getChildRef(); // no props
 		} else {
 			configurations = configurationList.get(0).getChildRef();
 		}
 		
 		List<ChildAssociationRef> preferencesList =
 			nodeService.getChildAssocs(configurations, ContentModel.ASSOC_CONTAINS, TYPE_PREFERENCES);
 		
 		NodeRef preferences = null;
 		
 		if (preferencesList.isEmpty()) {
 			log.debug("Creating preferences");
 			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
 			
 			properties.put(PROP_SHORTCUTS, new ArrayList<String>());
 			preferences = nodeService.createNode(configurations, ContentModel.ASSOC_CONTAINS,
 					TYPE_PREFERENCES, ContentModel.TYPE_CMOBJECT, properties).getChildRef(); 
 		} else {
 			preferences = preferencesList.get(0).getChildRef();
 		}
 		
 		List<String> shortcuts = (List<String>)nodeService.getProperty(preferences, PROP_SHORTCUTS);
 		
		List<NodeRef> shortcutRefs = new ArrayList<NodeRef>();
 		
 		if (null != shortcuts) {
 			for (String n: shortcuts) {
				shortcutRefs.add(new NodeRef("workspace://SpacesStore/" + n));
 			}
 		} else {
 			nodeService.setProperty(preferences, PROP_SHORTCUTS, new ArrayList<String>());
 		}
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("shortcuts", shortcutRefs);
 				
 		return model;
 		
 	}
 
 	public void setNodeService(NodeService nodeService) {
 		this.nodeService = nodeService;
 	}
 
 }
