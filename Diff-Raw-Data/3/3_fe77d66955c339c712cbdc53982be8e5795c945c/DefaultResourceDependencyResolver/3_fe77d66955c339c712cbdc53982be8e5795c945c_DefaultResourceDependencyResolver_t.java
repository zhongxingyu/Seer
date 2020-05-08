 package com.atlassian.plugin.webresource;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.collect.Iterables.contains;
 import static java.util.Collections.emptyMap;
 import static java.util.Collections.unmodifiableCollection;
 
 import com.atlassian.plugin.ModuleDescriptor;
 import com.atlassian.util.concurrent.ResettableLazyReference;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Iterables;
 
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 class DefaultResourceDependencyResolver implements ResourceDependencyResolver
 {
     private static final Logger log = LoggerFactory.getLogger(DefaultResourceDependencyResolver.class);
 
     private final WebResourceIntegration webResourceIntegration;
     private final ResourceBatchingConfiguration batchingConfiguration;
     private final Cache cached = new Cache();
 
     public DefaultResourceDependencyResolver(final WebResourceIntegration webResourceIntegration, final ResourceBatchingConfiguration batchingConfiguration)
     {
         this.webResourceIntegration = webResourceIntegration;
         this.batchingConfiguration = batchingConfiguration;
     }
 
     public Iterable<WebResourceModuleDescriptor> getSuperBatchDependencies()
     {
         return cached.resourceMap().values();
     }
 
     private Iterable<String> getSuperBatchDependencyKeys()
     {
         return cached.resourceMap().keySet();
     }
 
     public Iterable<WebResourceModuleDescriptor> getDependencies(final String moduleKey, final boolean excludeSuperBatchedResources)
     {
         final LinkedHashMap<String, WebResourceModuleDescriptor> orderedResources = new LinkedHashMap<String, WebResourceModuleDescriptor>();
         final Iterable<String> superBatchResources = excludeSuperBatchedResources ? getSuperBatchDependencyKeys() : Collections.<String> emptyList();
         resolveDependencies(moduleKey, orderedResources, superBatchResources, new Stack<String>(), null);
         return orderedResources.values();
     }
 
     public Iterable<WebResourceModuleDescriptor> getDependenciesInContext(final String context)
     {
         return getDependenciesInContext(context, new LinkedHashSet<String>());
     }
 
     public Iterable<WebResourceModuleDescriptor> getDependenciesInContext(final String context, final Set<String> skippedResources)
     {
         final Set<WebResourceModuleDescriptor> contextResources = new LinkedHashSet<WebResourceModuleDescriptor>();
         final Class<WebResourceModuleDescriptor> clazz = WebResourceModuleDescriptor.class;
         for (final WebResourceModuleDescriptor descriptor : webResourceIntegration.getPluginAccessor().getEnabledModuleDescriptorsByClass(clazz))
         {
             if (descriptor.getContexts().contains(context))
             {
                 final LinkedHashMap<String, WebResourceModuleDescriptor> dependencies = new LinkedHashMap<String, WebResourceModuleDescriptor>();
                 resolveDependencies(descriptor.getCompleteKey(), dependencies, getSuperBatchDependencyKeys(), new Stack<String>(), skippedResources);
                 for (final WebResourceModuleDescriptor dependency : dependencies.values())
                 {
                     contextResources.add(dependency);
                 }
             }
         }
         return unmodifiableCollection(contextResources);
     }
 
     /**
      * Adds the resources as well as its dependencies in order to the given ordered set. This method uses recursion
      * to add a resouce's dependent resources also to the set. You should call this method with a new stack
      * passed to the last parameter.
      *
      * Note that resources already in the given super batch will be exluded when resolving dependencies. You
      * should pass in an empty set for the super batch to include super batch resources.
      *
      * @param moduleKey the module complete key to add as well as its dependencies
      * @param orderedResourceKeys an ordered list set where the resources are added in order
      * @param superBatchResources the set of super batch resources to exclude when resolving dependencies
      * @param stack where we are in the dependency tree
      * @param skippedResources if not null, all resources with conditions are skipped and added to this set.
      */
     private void resolveDependencies(final String moduleKey, final Map<String, WebResourceModuleDescriptor> orderedResourceKeys, final Iterable<String> superBatchResources, final Stack<String> stack, final Set<String> skippedResources)
     {
         if (contains(superBatchResources, moduleKey))
         {
             log.debug("Not requiring resource: {0} because it is part of a super-batch", moduleKey);
             return;
         }
         if (stack.contains(moduleKey))
         {
             log.warn("Cyclic plugin resource dependency has been detected with: {0} \nStack trace: {1}", moduleKey, stack);
             return;
         }
 
         final ModuleDescriptor<?> moduleDescriptor = webResourceIntegration.getPluginAccessor().getEnabledPluginModule(moduleKey);
         if (!(moduleDescriptor instanceof WebResourceModuleDescriptor))
         {
             if (webResourceIntegration.getPluginAccessor().getPluginModule(moduleKey) != null)
             {
                 log.warn("Cannot include disabled web resource module: " + moduleKey);
             }
             else
             {
                 log.warn("Cannot find web resource module for: " + moduleKey);
             }
             return;
         }
 
         final WebResourceModuleDescriptor webResourceModuleDescriptor = (WebResourceModuleDescriptor) moduleDescriptor;
 
         if ((skippedResources != null) && (webResourceModuleDescriptor.getCondition() != null))
         {
             skippedResources.add(moduleKey);
             return;
         }
         else if (!webResourceModuleDescriptor.shouldDisplay())
         {
             log.debug("Cannot include web resource module {0} as its condition fails", moduleDescriptor.getCompleteKey());
             return;
         }
 
         final List<String> dependencies = webResourceModuleDescriptor.getDependencies();
         if (log.isDebugEnabled())
         {
             log.debug("About to add resource [{0}] and its dependencies: {1}", moduleKey, dependencies);
         }
         stack.push(moduleKey);
         try
         {
             for (final String dependency : dependencies)
             {
                 if (orderedResourceKeys.get(dependency) == null)
                 {
                     resolveDependencies(dependency, orderedResourceKeys, superBatchResources, stack, skippedResources);
                 }
             }
         }
         finally
         {
             stack.pop();
         }
         orderedResourceKeys.put(moduleKey, webResourceModuleDescriptor);
     }
 
     final class Cache
     {
         ResettableLazyReference<SuperBatch> lazy = new ResettableLazyReference<SuperBatch>()
         {
             @Override
             protected SuperBatch create() throws Exception
             {
                 // The linked hash map ensures that order is preserved
                 final String version = webResourceIntegration.getSuperBatchVersion();
                 return new SuperBatch(version, loadDescriptors(batchingConfiguration.getSuperBatchModuleCompleteKeys()));
             }
 
             Map<String, WebResourceModuleDescriptor> loadDescriptors(final Iterable<String> moduleKeys)
             {
                 if (Iterables.isEmpty(moduleKeys))
                 {
                     return emptyMap();
                 }
                 final Map<String, WebResourceModuleDescriptor> resources = new LinkedHashMap<String, WebResourceModuleDescriptor>();
                 for (final String moduleKey : moduleKeys)
                 {
                     resolveDependencies(moduleKey, resources, Collections.<String> emptyList(), new Stack<String>(), null);
                 }
                 return resources;
                 //return unmodifiableMap(resources);
             }
         };
 
         Map<String, WebResourceModuleDescriptor> resourceMap()
         {
             if (!batchingConfiguration.isSuperBatchingEnabled())
             {
                 log.warn("Super batching not enabled, but getSuperBatchDependencies() called. Returning empty.");
                 return emptyMap();
             }
             while (true)
             {
                 final SuperBatch batch = lazy.get();
                 if (batch.version.equals(webResourceIntegration.getSuperBatchVersion()))
                 {
                     return batch.resources;
                 }

                // The super batch has been updated so recreate the batch
                lazy.reset();
             }
         }
     }
 
     static final class SuperBatch
     {
         final String version;
         final Map<String, WebResourceModuleDescriptor> resources;
 
         SuperBatch(final String version, final Map<String, WebResourceModuleDescriptor> resources)
         {
             this.version = checkNotNull(version);
             this.resources = checkNotNull(resources);
         }
     }
 }
