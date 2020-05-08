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
 
 import org.exoplatform.services.cms.CmsService;
 import org.exoplatform.services.cms.jcrext.activity.ActivityCommonService;
 import org.exoplatform.services.listener.Event;
 import org.exoplatform.services.listener.Listener;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.wcm.core.NodetypeConstant;
 import org.exoplatform.services.wcm.utils.WCMCoreUtils;
 import org.exoplatform.webui.application.WebuiRequestContext;
 import org.exoplatform.webui.application.portlet.PortletRequestContext;
 
 import javax.jcr.Node;
 import javax.jcr.Value;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Mar
  * 15, 2011
  */
 public class FileUpdateActivityListener extends Listener<Node, String> {
 
   private static final Log LOG = ExoLogger.getLogger(FileUpdateActivityListener.class);
 
   private String[]  editedField     = {"exo:title", "exo:summary", "exo:language", "dc:title", "dc:description", "dc:creator", "dc:source", "jcr:data"};
   private String[]  bundleMessage   = {"SocialIntegration.messages.editName",
                                        "SocialIntegration.messages.editSummary",
                                        "SocialIntegration.messages.editLanguage",
                                        "SocialIntegration.messages.editTitle",
                                        "SocialIntegration.messages.editDescription",
                                        "SocialIntegration.messages.singleCreator",
                                        "SocialIntegration.messages.addSource",
                                        "SocialIntegration.messages.editFile"};
   private String[]  bundleRemoveMessage = {"SocialIntegration.messages.removeName",
       																 	   "SocialIntegration.messages.removeSummary",
       																 	  "SocialIntegration.messages.removeLanguage",
                                            "SocialIntegration.messages.removeTitle",
                                            "SocialIntegration.messages.removeDescription",
                                            "SocialIntegration.messages.removeCreator",
                                            "SocialIntegration.messages.addSource",
                                            "SocialIntegration.messages.editFile"};
   
   private boolean[] needUpdate      = {true, true, false, true, true, false, false, true};
   private int consideredFieldCount = editedField.length;
   /**
    * Instantiates a new post edit content event listener.
    */
   public FileUpdateActivityListener() {
 	  
   }
 
   @Override
   public void onEvent(Event<Node, String> event) throws Exception {
   	CmsService cmsService = WCMCoreUtils.getService(CmsService.class);
   	Map<String, Object> properties = cmsService.getPreProperties(); 
     Node currentNode = event.getSource();
     String nodeUUID = "";
     if(currentNode.isNodeType(NodetypeConstant.MIX_REFERENCEABLE)) nodeUUID = currentNode.getUUID();
     else nodeUUID = currentNode.getName();
     String propertyName = event.getData();
     String oldValue = "";
     String newValue = "";
     String commentValue = "";
     try {
     	if(!propertyName.equals(NodetypeConstant.JCR_DATA)) {
 	    	if(currentNode.getProperty(propertyName).getDefinition().isMultiple()){
 	    		Value[] values = currentNode.getProperty(propertyName).getValues();
 	    		if(values != null && values.length > 0) {
 	    			for (Value value : values) {
                            newValue= new StringBuffer(newValue).append(value.getString()).append(ActivityCommonService.METADATA_VALUE_SEPERATOR).toString();
                            commentValue=new StringBuffer(commentValue).append(value.getString()).append(", ").toString();
 						}
 	    			if(newValue.length() >= ActivityCommonService.METADATA_VALUE_SEPERATOR.length()) 
 	    				newValue = newValue.substring(0, newValue.length() - ActivityCommonService.METADATA_VALUE_SEPERATOR.length());
 	    			if(commentValue.length() >=2) commentValue = commentValue.substring(0, commentValue.length() - 2);
 	    		}
 	    		values = (Value[]) properties.get(nodeUUID + "_" + propertyName);
 	    		if(values != null && values.length > 0) {
 	    			for (Value value : values) {
 	    				oldValue += value.getString() + ActivityCommonService.METADATA_VALUE_SEPERATOR;
 						}
 	    			if(oldValue.length() >= ActivityCommonService.METADATA_VALUE_SEPERATOR.length()) 
 	    				oldValue = oldValue.substring(0, oldValue.length() - ActivityCommonService.METADATA_VALUE_SEPERATOR.length());
 	    		}
 	    	} else {
 	    		newValue= currentNode.getProperty(propertyName).getString();
 	    		commentValue = newValue;
 	    		if(properties.containsKey(nodeUUID + "_" + propertyName)) 
 	    			oldValue = properties.get(nodeUUID + "_" + propertyName).toString();
 	    	}
     	}
     }catch (Exception e) {
         LOG.info("Cannot get old value");
     }
     newValue = newValue.trim();
     oldValue = oldValue.trim();
     commentValue = commentValue.trim();
     
     if(currentNode.isNodeType(NodetypeConstant.NT_RESOURCE)) currentNode = currentNode.getParent();
     String resourceBundle = "";
     boolean hit = false;
     for (int i=0; i< consideredFieldCount; i++) {
       if (propertyName.equals(editedField[i])) {
       	hit = true;
       	if(newValue.length() > 0) {
       		
       		resourceBundle = bundleMessage[i];
       		//Post activity when update dc:creator property
       		if(propertyName.equals(NodetypeConstant.DC_CREATOR))
       		{
       			List<String> lstOld = Arrays.asList(oldValue.split(ActivityCommonService.METADATA_VALUE_SEPERATOR));
     				List<String> lstNew = Arrays.asList(newValue.split(ActivityCommonService.METADATA_VALUE_SEPERATOR));
     				String itemsRemoved = "";
     				int removedCount = 0;
     				int addedCount = 0;
     				StringBuffer sb = new StringBuffer();
     				for (String item : lstOld) {
 							if(!lstNew.contains(item)) {
 								sb.append(item).append(", ");
 								removedCount++;
 							}
 						}
     				if(sb.length() > 0) {
     				  itemsRemoved = sb.toString();
     				  itemsRemoved = itemsRemoved.substring(0, itemsRemoved.length()-2);
     				}
     				sb.delete(0, sb.length());
     				String itemsAdded = "";
     				for (String item : lstNew) {
 							if(!lstOld.contains(item)) {
 								sb.append(item).append(", ");
 								addedCount++;
 							}
 						}
     				if(sb.length() > 0) {
     					itemsAdded = sb.toString();
     					itemsAdded = itemsAdded.substring(0, itemsAdded.length()-2);
     				}
     				
     				if(itemsRemoved.length() > 0 && itemsAdded.length() > 0){ 
     					resourceBundle = (removedCount > 1) ?
     							"SocialIntegration.messages.removeMultiCreator" : "SocialIntegration.messages.removeCreator";
     					Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, itemsRemoved);
     					
     					resourceBundle = (addedCount > 1) ?
     							"SocialIntegration.messages.multiCreator" : "SocialIntegration.messages.singleCreator";
     					Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, commentValue);
     	        break;
     				}      				  
     				else if(itemsRemoved.length() > 0) {
     					resourceBundle = (removedCount > 1) ?
     							"SocialIntegration.messages.removeMultiCreator" : "SocialIntegration.messages.removeCreator";
     					newValue = itemsRemoved;
     					Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, newValue);
     	        break;
     				}
     				else if(itemsAdded.length() > 0) {
     					resourceBundle = (commentValue.split(",").length > 1) ?
     							"SocialIntegration.messages.multiCreator" : "SocialIntegration.messages.singleCreator";
     					Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, commentValue);
     	        break;
     				}     			
       		}
       	  //Post activity when update dc:source property
       		if(propertyName.equals(NodetypeConstant.DC_SOURCE)) {      			
       				List<String> lstOld = Arrays.asList(oldValue.split(ActivityCommonService.METADATA_VALUE_SEPERATOR));
       				List<String> lstNew = Arrays.asList(newValue.split(ActivityCommonService.METADATA_VALUE_SEPERATOR));
       				String itemsRemoved = "";
       				int removedCount = 0;
       				int addedCount = 0;
       				StringBuffer sb = new StringBuffer();
       				for (String item : lstOld) {
 								if(!lstNew.contains(item)) {
 									sb.append(item).append(", ");
 									removedCount++;
 								}
 							}
       				if(sb.length() > 0) {
       				  itemsRemoved = sb.toString();
       				  itemsRemoved = itemsRemoved.substring(0, itemsRemoved.length()-2);
       				}
       				sb.delete(0, sb.length());
       				String itemsAdded = "";
       				for (String item : lstNew) {
 								if(!lstOld.contains(item)) {
 									sb.append(item).append(", ");
 									addedCount++;
 								}
 							}
       				if(sb.length() > 0) {
       					itemsAdded = sb.toString();
       					itemsAdded = itemsAdded.substring(0, itemsAdded.length()-2);
       				}
       				if(itemsRemoved.length() > 0 && itemsAdded.length() > 0){  					
       					resourceBundle = (removedCount > 1) ?
       							"SocialIntegration.messages.removeMultiSource" : "SocialIntegration.messages.removeSource";
       					Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, itemsRemoved);
       					
       					resourceBundle = (addedCount > 1) ?
       							"SocialIntegration.messages.addMultiSource" : "SocialIntegration.messages.addSource";
       					Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, itemsAdded);
       	        break;
       				}      				  
       				else if(itemsRemoved.length() > 0) {
       					resourceBundle = (removedCount > 1) ?
       							"SocialIntegration.messages.removeMultiSource" : "SocialIntegration.messages.removeSource";
       					newValue = itemsRemoved;
       					Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, newValue);
       	        break;
       				}
       				else if(itemsAdded.length() > 0) {
       					resourceBundle = (addedCount > 1) ?
       							"SocialIntegration.messages.addMultiSource" : "SocialIntegration.messages.addSource";
       					newValue = itemsAdded;
       					Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, newValue);
       	        break;
       				}      			
       		}
       		Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, commentValue);
 	        break;
       	} else if(!propertyName.equals(NodetypeConstant.EXO_LANGUAGE)){ //Remove the property
       		resourceBundle = bundleRemoveMessage[i];      		
       		if(propertyName.equals(NodetypeConstant.DC_CREATOR)) {
       			resourceBundle = (oldValue.split(ActivityCommonService.METADATA_VALUE_SEPERATOR).length > 1) ?
   							"SocialIntegration.messages.removeMultiCreator" : "SocialIntegration.messages.removeCreator";
       		} else if(propertyName.equals(NodetypeConstant.DC_SOURCE)) {
       			resourceBundle = (oldValue.split(ActivityCommonService.METADATA_VALUE_SEPERATOR).length > 1) ?
   							"SocialIntegration.messages.removeMultiSource" : "SocialIntegration.messages.removeSource";
       		}
       		
       		if(propertyName.equals(NodetypeConstant.DC_SOURCE) || propertyName.equals(NodetypeConstant.DC_CREATOR)) {
       			commentValue = oldValue.replaceAll(ActivityCommonService.METADATA_VALUE_SEPERATOR, ", ");
       		}      		
       		Utils.postFileActivity(currentNode, resourceBundle, needUpdate[i], true, commentValue);
           break;
       	} else break;
       	        
       }
     }
     if(!hit && propertyName.startsWith("dc:") && !propertyName.equals("dc:date")) {
     	PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
     	String dcProperty = propertyName;
     	try {
     		dcProperty = portletRequestContext.getApplicationResourceBundle().getString("ElementSet.dialog.label." + 
     	  		propertyName.substring(propertyName.lastIndexOf(":") + 1, propertyName.length()));
     	} catch(Exception ex) {
             LOG.info("Cannot get propertyName");
     	}
     	if(newValue.length() > 0) resourceBundle = "SocialIntegration.messages.updateMetadata";
     	else resourceBundle = "SocialIntegration.messages.removeMetadata";    	
     	resourceBundle = portletRequestContext.getApplicationResourceBundle().getString(resourceBundle);
     	resourceBundle = resourceBundle.replace("{0}", dcProperty);
     	resourceBundle = resourceBundle.replace("{1}", commentValue);
     	Utils.postFileActivity(currentNode, resourceBundle, false, true, commentValue);
     }
   }
 }
