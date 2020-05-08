 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  /*
  * Created on 25/Jan/2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.continuent.appia.xml.utils;
 
 import java.util.LinkedList;
 
 import org.continuent.appia.core.EventScheduler;
 import org.continuent.appia.core.memoryManager.MemoryManager;
 
 
 /**
  * @author Jose Mocito
  */
 public class ChannelInfo {
 
 	private String name;
 	private String templateName;
 	private String label;
 	private ChannelProperties params;
 	private boolean initialized;
 	private EventScheduler eventScheduler;
 	private MemoryManager memoryManager = null;
 	private boolean managed;
     
 	private LinkedList dependencies;
 	
 	/**
 	 * @param name
 	 * @param templateName
 	 * @param label
 	 * @param params
 	 * @param initialized
 	 */
 	public ChannelInfo(String name, String templateName, String label,
 			ChannelProperties params, boolean initialized) {
 		super();
 		this.name = name;
 		this.templateName = templateName;
 		this.label = label;
 		this.params = params;
 		this.initialized = initialized;
 		
 		this.dependencies = new LinkedList();
 	}
 
 	/**
 	 * @param name
 	 * @param templateName
 	 * @param label
 	 * @param params
 	 * @param initialized
 	 * @param memoryManager
 	 */
 	public ChannelInfo(String name, String templateName, String label,
 			ChannelProperties params, boolean initialized, MemoryManager memoryManager) {
 		super();
 		this.name = name;
 		this.templateName = templateName;
 		this.label = label;
 		this.params = params;
 		this.initialized = initialized;
 		this.dependencies = new LinkedList();
 		this.memoryManager = memoryManager;
 	}
 
 	/**
 	 * @return Returns the initialized.
 	 */
 	public boolean isInitialized() {
 		return initialized;
 	}
 	/**
 	 * @param initialized The initialized to set.
 	 */
 	public void setInitialized(boolean initialized) {
 		this.initialized = initialized;
 	}
 	/**
 	 * @return Returns the label.
 	 */
 	public String getLabel() {
 		return label;
 	}
 	/**
 	 * @param label The label to set.
 	 */
 	public void setLabel(String label) {
 		this.label = label;
 	}
 	/**
 	 * @return Returns the name.
 	 */
 	public String getName() {
 		return name;
 	}
 	/**
 	 * @param name The name to set.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	/**
 	 * @return Returns the params.
 	 */
 	public ChannelProperties getParams() {
 		return params;
 	}
 	/**
 	 * @param params The params to set.
 	 */
 	public void setParams(ChannelProperties params) {
 		this.params = params;
 	}
 	/**
 	 * @return Returns the templateName.
 	 */
 	public String getTemplateName() {
 		return templateName;
 	}
 	/**
 	 * @param templateName The templateName to set.
 	 */
 	public void setTemplateName(String templateName) {
 		this.templateName = templateName;
 	}
 	
 	/**
 	 * @return Returns the eventScheduler.
 	 */
 	public EventScheduler getEventScheduler() {
 		return eventScheduler;
 	}
 	/**
 	 * @param eventScheduler The eventScheduler to set.
 	 */
 	public void setEventScheduler(EventScheduler eventScheduler) {
 		this.eventScheduler = eventScheduler;
 	}
 	
     /**
      * Gets the memory manager instance.
      * @return the memory manager instance.
      */
 	public MemoryManager getMemoryManager(){
 		return memoryManager;
 	}
 	
 	/**
 	 * @return Returns the dependencies.
 	 */
 	public LinkedList getDependencies() {
 		return dependencies;
 	}
     
     /**
      * 
      * @param channel
      */
 	public void addDependency(ChannelInfo channel) {
 		if (!dependencies.contains(channel))
 			dependencies.add(channel);
 	}
 	
     /**
      * @param channel
     * @return
      */
 	public boolean depends(ChannelInfo channel) {
 		if (dependencies.contains(channel))
 			return true;
 		else {
 			for (int i = 0; i < dependencies.size(); i++) {
 				final ChannelInfo cinfo = (ChannelInfo) dependencies.get(i);
 				if (cinfo.depends(channel))
 					return true;
 			}
 		}
 		return false;
 	}
 	
     /**
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
 	public boolean equals(Object arg) {
 	    if(arg instanceof ChannelInfo){
 	        final ChannelInfo cinfo = (ChannelInfo)arg;
 	        return name.equals(cinfo.name);
 	    }
         else
             return false;
 	}
 
     /**
      * Gets the managed state.
      * @return true if this channel is managed.
      */
     public boolean isManaged() {
         return managed;
     }
 
     /**
      * Sets the managed state.
      * @param managed
      */
     public void setManaged(boolean managed) {
         this.managed = managed;
     }
 }
