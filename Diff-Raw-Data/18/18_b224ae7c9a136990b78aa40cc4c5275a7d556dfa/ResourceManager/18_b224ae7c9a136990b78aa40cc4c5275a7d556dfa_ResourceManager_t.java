 /* Chronos - Game Development Toolkit for Java game developers. The
  * original source remains:
  * 
  * Copyright (c) 2013 Miguel Gonzalez http://my-reality.de
  * 
  * This source is provided under the terms of the BSD License.
  * 
  * Copyright (c) 2013, Chronos
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the following 
  * conditions are met:
  * 
  *  * Redistributions of source code must retain the above 
  *    copyright notice, this list of conditions and the 
  *    following disclaimer.
  *  * Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution.
  *  * Neither the name of the Chronos/my Reality Development nor the names of 
  *    its contributors may be used to endorse or promote products 
  *    derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
  * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
  * OF SUCH DAMAGE.
  */
 package de.myreality.chronos.resources;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.myreality.chronos.logging.ChronosLogger;
 import de.myreality.chronos.util.ClassUtils;
 
 /**
  * Singleton resource manager which provides resources. New resources can be
  * added. Additionally it is possible to add additional resource factories to
  * the manager.
  * 
  * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
  * @since 0.8alpha
  * @version 0.8alpha
  */
 public class ResourceManager implements ResourceManagerable {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final long serialVersionUID = 1L;
 	private static ResourceManager instance;
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	// Contains the resource class as key and the regarding loader as value
 	private Map<String, ResourceLoader<?>> loaders;
 
 	// Contains the resource name as key and the resource class as value
 	private Map<String, String> translations;
 
 	// Manages all groups
 	private ResourceGroupManager groupManager;
 
 	// Manages all definitions
 	private ResourceDefinitionManager definitionManager;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	static {
 		instance = new ResourceManager();
 	}
 
 	// Make manager package-private for testing purpose
 	@SuppressWarnings("unchecked")
 	ResourceManager() {
 		loaders = new HashMap<String, ResourceLoader<?>>();
 		translations = new HashMap<String, String>();
 		groupManager = new BasicResourceGroupManager();
 		definitionManager = new BasicResourceDefinitionManager(groupManager);
 
 		// Add the root group as default
 		groupManager.addElement(new BasicResourceGroup());
 
 		try {
 			Class<? extends ResourceLoader<?>>[] loaders = (Class<? extends ResourceLoader<?>>[]) ClassUtils
 					.searchForAnnotation(ResourceType.class);
 			for (Class<? extends ResourceLoader<?>> clazz : loaders) {
 				addResourceLoader(clazz);
 			}
 		} catch (ResourceException e) {
 			ChronosLogger.error(e);
 		}
 	}
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	public static ResourceManager getInstance() {
 		return instance;
 	}
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public <T> boolean hasResource(String id, Class<T> clazz) {
 		ResourceLoader<?> loader = loaders.get(clazz.getName());
 
 		if (loader != null) {
 			return loader.containsResource(id);
 		} else {
 			return false;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T> Resource<T> getResource(String id, Class<T> clazz) {
 
 		ResourceLoader<?> loader = loaders.get(clazz.getName());
 		Resource<T> resource = null;
 		if (hasResource(id, clazz)) {
 			resource = (Resource<T>) loader.getResource(id);
 
 		} else if (definitionManager.hasElement(id)) {
 			ResourceDefinition definition = definitionManager.getElement(id);
 
			if (loader != null) {
 				try {
 					resource = (Resource<T>) loader.loadResource(definition);
 				} catch (ResourceException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return resource;
 	}
 
 	@Override
 	public void load(DataSource dataSource) throws ResourceException {
 		try {
 			
 			dataSource.addListener(definitionManager);
 			dataSource.addListener(groupManager);
 			
 			dataSource.load();
 
 			for (ResourceDefinition definition : definitionManager
 					.getAllElements()) {
 
 				ResourceLoader<?> loader = getLoaderByDefinition(definition);
 
 				if (loader != null) {
 
 					if (!definition.isDeferred()) {
 						loader.loadResource(definition);
 					}
 				} else {
 					throw new ResourceException("Loader for "
 							+ definition.getType() + " resource does not exist");
 				}
 			}
 		} finally {
 			dataSource.removeListener(definitionManager);
 			dataSource.removeListener(groupManager);
 		}
 	}
 
 	@Override
 	public void addResourceLoader(Class<? extends ResourceLoader<?>> clazz)
 			throws ResourceException {
 		ChronosLogger.info("Register loader: " + clazz.getSimpleName());
 
 		// Check if the class is registered
 		ResourceType annotation = clazz.getAnnotation(ResourceType.class);
 		if (annotation != null) {
 
 			if (annotation.value().isEmpty()) {
 				throw new ResourceException("The annotation in loader class '"
 						+ clazz.getSimpleName() + "' needs a value");
 			}
 
 			ResourceLoader<?> loader = ClassUtils.createObject(clazz);
 			String resourceClass = loader.getResourceClass().getName();
 			loaders.put(resourceClass, loader);
 			translations.put(annotation.value(), resourceClass);
 		} else {
 			throw new ResourceException("loader class '"
 					+ clazz.getSimpleName()
 					+ "' needs a @RegisteredLoader annotation");
 		}
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	private ResourceLoader<?> getLoaderByDefinition(
 			ResourceDefinition definition) throws ResourceException {
 		String resourceClass = translations.get(definition.getType());
 
 		if (resourceClass == null) {
 			throw new ResourceException("Loader for " + definition.getType()
 					+ " resource does not exist");
 		}
 
 		return loaders.get(resourceClass);
 	}
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 
 }
