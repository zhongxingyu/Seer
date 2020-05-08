 /*
  * Copyright (C) 2003-2011 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.wcm.ext.component.activity.listener;
 
 import javax.jcr.Node;
 
 import org.exoplatform.services.cms.jcrext.activity.ActivityCommonService;
 import org.exoplatform.services.listener.Event;
 import org.exoplatform.services.listener.Listener;
 import org.exoplatform.services.wcm.core.NodetypeConstant;
 import org.exoplatform.webui.application.WebuiRequestContext;
 import org.exoplatform.webui.application.portlet.PortletRequestContext;
 
 import javax.jcr.Value;
 
 /**
  * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Mar
  * 15, 2011
  */
 public class FileAddPropertyActivityListener extends Listener<Node, String> {
 
   private String[]  editedField     = {"dc:title", "dc:description", "dc:creator", "dc:source"};
   private String[]  bundleMessage   = {"SocialIntegration.messages.editTitle",
                                        "SocialIntegration.messages.editDescription",
                                        "SocialIntegration.messages.singleCreator",
                                        "SocialIntegration.messages.addSource"};
   private boolean[] needUpdate      = {false, false, false, false};
   private int consideredFieldCount = editedField.length;
   /**
    * Instantiates a new post edit content event listener.
    */
   public FileAddPropertyActivityListener() {
 	  
   }
 
   @Override
   public void onEvent(Event<Node, String> event) throws Exception {
     Node currentNode = event.getSource();
     String propertyName = event.getData();
     String newValue = "";
     String commentValue = "";
     try {      
     	if(currentNode.getProperty(propertyName).getDefinition().isMultiple()){
     		Value[] values = currentNode.getProperty(propertyName).getValues();
     		if(values != null) {
     			for (Value value : values) {
 						newValue += value.getString() + ActivityCommonService.METADATA_VALUE_SEPERATOR;
 						commentValue += value.getString() + ", ";
 					}
     			if(newValue.length() >= ActivityCommonService.METADATA_VALUE_SEPERATOR.length())
     			  newValue = newValue.substring(0, newValue.length() - ActivityCommonService.METADATA_VALUE_SEPERATOR.length());
     			if(commentValue.length() >=2) commentValue = commentValue.substring(0, commentValue.length() - 2);
     		}
    	} else newValue= currentNode.getProperty(propertyName).getString();      
     }catch (Exception e) {
       newValue = "";
       commentValue = "";
     }
     newValue = newValue.trim();
     commentValue = commentValue.trim();
     
     if(newValue != null && newValue.length() > 0) {
 	    if(currentNode.isNodeType(NodetypeConstant.NT_RESOURCE)) currentNode = currentNode.getParent();
 	    String resourceBundle = "";
 	    boolean hit = false;
 	    for (int i=0; i< consideredFieldCount; i++) {
 	      if (propertyName.equals(editedField[i])) {
 	      	resourceBundle = bundleMessage[i];
 	      	if(propertyName.equals(NodetypeConstant.DC_CREATOR) && 
 	      			newValue.split(ActivityCommonService.METADATA_VALUE_SEPERATOR).length > 1)
 	      		resourceBundle = "SocialIntegration.messages.multiCreator";	      
 	      	if(propertyName.equals(NodetypeConstant.DC_SOURCE) &&
 	      			newValue.split(ActivityCommonService.METADATA_VALUE_SEPERATOR).length > 1)
 	      		resourceBundle = "SocialIntegration.messages.addMultiSource";
 	      	Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, commentValue);
 	      	hit = true;
 	        break;
 	      }
 	    }
 	    if(!hit && propertyName.startsWith("dc:") && !propertyName.equals("dc:date")) {
 	    	PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
 	    	String dcProperty = propertyName;
 	    	try {
 	    		dcProperty = portletRequestContext.getApplicationResourceBundle().getString("ElementSet.dialog.label." + 
 	    	  		propertyName.substring(propertyName.lastIndexOf(":") + 1, propertyName.length()));
 	    	} catch(Exception ex) {
 	    		//nothing
 	    	}
 	    	resourceBundle = "SocialIntegration.messages.updateMetadata";
 	    	resourceBundle = portletRequestContext.getApplicationResourceBundle().getString(resourceBundle);
 	    	resourceBundle = resourceBundle.replace("{0}", dcProperty);
 	    	resourceBundle = resourceBundle.replace("{1}", commentValue);
 	    	Utils.postFileActivity(currentNode, resourceBundle, false, true, commentValue);
 	    }
     }
   }
 }
