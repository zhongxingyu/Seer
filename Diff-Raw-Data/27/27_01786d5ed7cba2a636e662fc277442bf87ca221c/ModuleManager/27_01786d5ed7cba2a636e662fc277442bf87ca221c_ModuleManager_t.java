 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.romaframework.core.module;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.core.CoreAspect;
 import org.romaframework.core.config.Configurable;
 import org.romaframework.core.config.Serviceable;
 
 /**
  * Manage Module implementations.
  * 
  * @author Luca Garulli (luca.garulli--at--assetdata.it)
  * 
  */
 public class ModuleManager extends Configurable<Module> implements Serviceable {
 	protected static ModuleManager	instance	= new ModuleManager();
 	protected static Log						log				= LogFactory.getLog(ModuleManager.class);
 
 	protected ModuleManager() {
 	}
 
 	/**
 	 * Register the module already started. It' used by SelfRegistrantModule.
 	 * 
 	 * @param iModule
 	 *          Module to register
 	 */
 	protected void register(Module iModule) {
 		addConfiguration(iModule.moduleName(), iModule);
 	}
 
 	public void startup() {
 		// START THE CORE MODULE FIRST OF ALL
 		Module coreAspect = configuration.get(CoreAspect.ASPECT_NAME);
 		coreAspect.startup();
 
 		for (Module module : configuration.values()) {
 			module.startup();
 		}
 	}
 
 	public void shutdown() {
 		for (Module module : configuration.values()) {
			log.warn("Shutting down module " + module.moduleName() + "...");
 			module.shutdown();
 		}
		log.warn("All modules shut down.");
 
 		configuration.clear();
 	}
 
 	public String getStatus() {
 		return null;
 	}
 
 	public static ModuleManager getInstance() {
 		return instance;
 	}
 }
