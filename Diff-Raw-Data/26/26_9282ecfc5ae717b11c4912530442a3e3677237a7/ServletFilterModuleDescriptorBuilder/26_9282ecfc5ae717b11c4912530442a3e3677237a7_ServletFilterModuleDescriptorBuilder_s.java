 package com.atlassian.plugin.servlet.descriptors;
 
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.module.PrefixDelegatingModuleFactory;
 import com.atlassian.plugin.module.ModuleFactory;
 import com.atlassian.plugin.module.PrefixModuleFactory;
 import com.atlassian.plugin.servlet.ObjectFactories;
 import com.atlassian.plugin.servlet.ObjectFactory;
 import com.atlassian.plugin.servlet.PluginBuilder;
 import com.atlassian.plugin.servlet.ServletModuleManager;
 import com.atlassian.plugin.servlet.filter.FilterDispatcherCondition;
 import com.atlassian.plugin.servlet.filter.FilterLocation;
 import com.mockobjects.dynamic.Mock;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import javax.servlet.Filter;
 
 import static com.atlassian.plugin.servlet.filter.FilterTestUtils.immutableList;
 
 public class ServletFilterModuleDescriptorBuilder
 {
     private Plugin plugin = new PluginBuilder().build();
     private String key = "test.servlet.context.listener";
     private ObjectFactory<Filter> filterFactory;
     private FilterLocation location = FilterLocation.BEFORE_DISPATCH;
     private int weight = 100;
     private List<String> paths = new LinkedList<String>();
     private ServletModuleManager servletModuleManager = (ServletModuleManager) new Mock(ServletModuleManager.class).proxy();
     private Set<FilterDispatcherCondition> dispatchers = new HashSet<FilterDispatcherCondition>();
 
     public ServletFilterModuleDescriptorBuilder with(Plugin plugin)
     {
         this.plugin = plugin;
         return this;
     }
    
     public ServletFilterModuleDescriptorBuilder withKey(String key)
     {
         this.key = key;
         return this;
     }
 
     public ServletFilterModuleDescriptorBuilder with(Filter filter)
     {
         this.filterFactory = ObjectFactories.createSingleton(filter);
         return this;
     }
 
     public ServletFilterModuleDescriptorBuilder withPath(String path)
     {
         paths.add(path);
         return this;
     }
 
     public ServletFilterModuleDescriptorBuilder with(ServletModuleManager servletModuleManager)
     {
         this.servletModuleManager = servletModuleManager;
         return this;
     }
    
     public ServletFilterModuleDescriptorBuilder at(FilterLocation location)
     {
         this.location = location;
         return this;
     }
    
     public ServletFilterModuleDescriptorBuilder withWeight(int weight)
     {
         this.weight = weight;
         return this;
     }
 
     public ServletFilterModuleDescriptorBuilder withFactory(ObjectFactory<Filter> mutable)
     {
         this.filterFactory = mutable;
         return this;
     }
 
     public ServletFilterModuleDescriptorBuilder withDispatcher(FilterDispatcherCondition dispatcher) {
         dispatchers.add(dispatcher);
         return this;
     }
 
     public ServletFilterModuleDescriptor build()
     {
         return new Descriptor(plugin, key, filterFactory, location, weight, immutableList(paths), servletModuleManager,
                 new PrefixDelegatingModuleFactory(Collections.<PrefixModuleFactory>emptySet()), dispatchers);
     }
 
     static final class Descriptor extends ServletFilterModuleDescriptor
     {
         final String key;
         final ObjectFactory<Filter> filterFactory;
         final List<String> paths;
         final FilterLocation location;
         final int weight;
         final ServletModuleManager servletModuleManager;
         final Set<FilterDispatcherCondition> dispatchers;
        
         public Descriptor(
             Plugin plugin,
             String key,
             ObjectFactory<Filter> filterFactory,
             FilterLocation location,
             int weight,
             List<String> paths,
             ServletModuleManager servletModuleManager,
             ModuleFactory moduleFactory,
             Set<FilterDispatcherCondition> dispatchers)
         {
             super(moduleFactory, servletModuleManager);
             this.plugin = plugin;
             this.key = key;
             this.filterFactory = filterFactory;
             this.location = location;
             this.weight = weight;
             this.paths = paths;
             this.servletModuleManager = servletModuleManager;
             this.dispatchers = dispatchers;
         }
 
         @Override
         public Plugin getPlugin()
         {
             return plugin;
         }
        
         @Override
         public String getCompleteKey()
         {
             return getPluginKey() + ":" + key;
         }
 
         @Override
         public String getKey()
         {
             return key;
         }
 
         @Override
         public Filter getModule()
         {
             return filterFactory.create();
         }
        
         @Override
         public FilterLocation getLocation()
         {
             return location;
         }
        
         @Override
         public int getWeight()
         {
             return weight;
         }
 
         @Override
         public List<String> getPaths()
         {
             return paths;
         }
 
         @Override
         public Set<FilterDispatcherCondition> getDispatcherConditions()
         {
             return dispatchers;
         }
     }
 
 }
