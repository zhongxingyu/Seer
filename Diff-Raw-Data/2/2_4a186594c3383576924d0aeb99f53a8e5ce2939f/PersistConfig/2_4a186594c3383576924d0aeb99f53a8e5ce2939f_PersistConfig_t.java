 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.oobium.build.workspace.Application;
 import org.oobium.persist.PersistService;
 import org.oobium.utils.Config;
 import org.oobium.utils.Config.Mode;
 
 public class PersistConfig {
 	
 	private static boolean isDb(String service) {
 		return service != null && service.startsWith("org.oobium.persist.db.");
 	}
 	
 	public static class Service {
 		
 		private String name;
 		private List<String> models;
 		
 		private Service(Map<?,?> map) {
 			Object service = map.get(PersistService.SERVICE);
 			if(service instanceof String) {
 				this.name = (String) service;
 			} else {
 				throw new IllegalArgumentException(service + " is not a valid \"" + PersistService.SERVICE + "\" property value");
 			}
 			
 			Object models = map.get(PersistService.MODELS);
 			if(models instanceof String) {
 				this.models = Collections.singletonList((String) models);
 			} else if(models instanceof List<?>) {
 				this.models = new ArrayList<String>();
 				for(Object o : (List<?>) models) {
 					this.models.add(String.valueOf(o));
 				}
 			} else {
 				this.models = new ArrayList<String>(0);
 			}
 		}
 		
 		public List<String> getModels() {
 			return models;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public boolean isDb() {
 			return PersistConfig.isDb(name);
 		}
 		
 	}
 
 	
 	private String module;
 	private String service;
 	private List<Service> services;
 	private List<PersistConfig> modConfigs;
 	
 	// obj can be:
 	//	null (use the NullPersistorService)
 	//	String (a single persistor)
 	//	List (Strings and Maps - 1st String is the default, Maps are secondaries that are mapped to classes)
 	//	Map (mapped to classes; no default means use the NullPersistService for any classes not in the Map)
 	//		(Maps -> { service:"persistor", models: [ "Model1", "Model2", "etc." ] }
 	//		(not mapped to classes: the map is the default)
 	/**
 	 * @param config
 	 * @param mode
 	 * @throws IllegalArgumentException
 	 */
 	public PersistConfig(Application app, Mode mode) {
 		module = app.getName();
 
 		Config config = app.loadConfiguration();
 		
 		Object obj = config.get(Config.PERSIST, mode);
 		init(obj);
 		
 		modConfigs = new ArrayList<PersistConfig>();
 		modConfigs.add(this);
 		
 		Object modules = config.get(Config.MODULES, mode);
 		if(modules instanceof List<?>) {
 			for(Object o : (List<?>) modules) {
 				if(o instanceof Map<?,?>) {
 					modConfigs.add(new PersistConfig((Map<?,?>) o));
 				} else {
					System.out.println("skipping " + o + " (only maps are added to PersistConfig because a persistor must be specified)");
 				}
 			}
 		} else if(modules instanceof Map<?,?>) {
 			modConfigs.add(new PersistConfig((Map<?,?>) modules));
 		}
 	}
 	
 	private PersistConfig(Map<?, ?> map) {
 		modConfigs = new ArrayList<PersistConfig>(0);
 
 		Entry<?, ?> entry = map.entrySet().iterator().next();
 		module = (String) entry.getKey();
 
 		Object pmap = entry.getValue();
 		if(pmap instanceof Map<?,?>) {
 			Object obj = ((Map<?,?>) pmap).get(Config.PERSIST);
 			init(obj);
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 	
 	private void init(Object obj) {
 		if(obj instanceof String) {
 			if(service == null) service = (String) obj;
 			if(services == null) services = new ArrayList<PersistConfig.Service>(0);
 		} else if(obj instanceof List<?>) {
 			for(Object o : (List<?>) obj) {
 				init(o);
 			}
 		} else if(obj instanceof Map<?,?>) {
 			Service s = new Service((Map<?,?>) obj);
 			if(service == null) service = s.getName();
 			if(services == null) services = new ArrayList<PersistConfig.Service>();
 			services.add(s);
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public String getModule() {
 		return module;
 	}
 	
 	public List<PersistConfig> getModuleConfigs() {
 		return modConfigs;
 	}
 	
 	public String getService() {
 		return service;
 	}
 	
 	public List<Service> getServices() {
 		return services;
 	}
 	
 	public boolean isDb() {
 		return isDb(service);
 	}
 	
 }
