 package org.wdbuilder.service;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.wdbuilder.domain.Entity;
 import org.wdbuilder.plugin.IPluginFacade;
 
 public class PluginFacadeRepository<T extends Entity, S extends IPluginFacade<T>>
 		implements IPluginFacadeRepository<T, S> {
 
 	private final Map<Class<?>, S> plugins = new LinkedHashMap<Class<?>, S>(2);
 
	PluginFacadeRepository(Collection<S> plugins) {
 		for (S plugin : plugins) {
 			this.plugins.put(plugin.getEntityClass(), plugin);
 		}
 	}
 
 	@Override
 	public Iterable<S> getPlugins() {
 		return this.plugins.values();
 	}
 
 	@Override
 	public S getFacade(Class<?> klass) {
 		return this.plugins.get(klass);
 	}
 
 	@Override
 	public Collection<Class<?>> getEntityClasses() {
 		return this.plugins.keySet();
 	}
 
 }
