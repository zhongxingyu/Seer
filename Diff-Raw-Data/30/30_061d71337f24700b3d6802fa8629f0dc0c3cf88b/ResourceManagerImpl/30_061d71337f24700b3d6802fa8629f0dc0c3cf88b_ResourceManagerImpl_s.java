 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant.context;
 
 import com.kloudtek.systyrant.*;
 import com.kloudtek.systyrant.annotation.STResource;
 import com.kloudtek.systyrant.context.java.JavaResourceDefinitionFactory;
 import com.kloudtek.systyrant.dsl.query.ResourceQuery;
 import com.kloudtek.systyrant.exception.*;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.script.ScriptException;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.locks.Lock;
 
 import static com.kloudtek.util.StringUtils.isNotEmpty;
 
 public class ResourceManagerImpl implements ResourceManager {
     private STContext context;
     private final STContextData data;
     private static final Logger logger = LoggerFactory.getLogger(ResourceManagerImpl.class);
     private boolean closed;
 
     public ResourceManagerImpl(STContext context, STContextData data) {
         this.context = context;
         this.data = data;
 
     }
 
     @Override
     public Iterator<Resource> iterator() {
         rlock();
         try {
             return data.resources.iterator();
         } finally {
             rulock();
         }
     }
 
     @Override
     public void setContext(STContext context) {
         this.context = context;
     }
 
     @Override
     public List<ResourceDefinition> getResourceDefinitions() {
         rlock();
         try {
             return Collections.unmodifiableList(data.resourceDefinitions);
         } finally {
             rulock();
         }
     }
 
     @Override
     public List<Resource> getResources() {
         rlock();
         try {
             return Collections.unmodifiableList(data.resources);
         } finally {
             rulock();
         }
     }
 
     @Override
     public List<Resource> getChildrens(Resource resource) {
         rlock();
         try {
             return Collections.unmodifiableList(data.getChildrensInternalList(resource));
         } finally {
             rulock();
         }
     }
 
     private List<Resource> getChildrenOnDemandSearch(Resource resource) {
         ArrayList<Resource> list = new ArrayList<>();
         wlock();
         for (Resource rs : data.resources) {
             Resource p = rs.getParent();
             if (p != null && p.equals(resource)) {
                 list.add(rs);
             }
         }
         wulock();
         return list;
     }
 
     @Override
     public boolean isCreateAllowed() {
         rlock();
         try {
             return data.createAllowed;
         } finally {
             rulock();
         }
     }
 
     @Override
     public void setCreateAllowed(boolean createAllowed) {
         wlock();
         try {
             this.data.createAllowed = createAllowed;
         } catch (Exception e) {
             wulock();
         }
     }
 
     // -------------------------
     // Resource Creation
     // -------------------------
 
     /**
      * Used to create an Resource instance.
      * Important notes: Must only be called before the pre-execution
      *
      * @param fqname Fully qualified resource name
      * @param id     Id of the resource or null if the id should be automatically generated.
      * @param parent If specified, the new resource will set this as it's parent
      */
     @Override
     public Resource createResource(@NotNull FQName fqname, @Nullable String id, @Nullable Resource parent,
                                    @Nullable Collection<ResourceMatcher> importPaths) throws ResourceCreationException {
         wlock();
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Creating resource {}", fqname);
             }
             if (!data.createAllowed) {
                 throw new ResourceCreationException("Resources created not allowed at this time.");
             }
             ResourceDefinition definition;
             definition = findResourceDefinition(fqname, importPaths);
             String uid = null;
             Lock lock = parent != null ? ((ResourceImpl) parent).wlock() : context.getRootResourceLock().writeLock();
             lock.lock();
             try {
                 if (id == null) {
                     id = definition.getFQName().toString();
                     String str = parent != null ? parent.getUid() + "." + id : id;
                     int count = 1;
                     while (context.findResourceByUid(str + count) != null) {
                         count++;
                     }
                     id = id + count;
                 }
                 uid = parent != null ? parent.getUid() + "." + id : id;
             } finally {
                 lock.unlock();
             }
             if (data.resourcesUidIndex.containsKey(uid)) {
                 throw new ResourceCreationException("There is already a resource with uid " + uid);
             }
             Resource resource = definition.create(context, id, uid, parent != null ? parent : context.getDefaultParent());
             data.resourcesUidIndex.put(uid, resource);
             data.resources.add(resource);
             if (logger.isDebugEnabled()) {
                 logger.debug("Created resource {}", fqname);
             }
             return resource;
         } finally {
             wulock();
         }
     }
 
     @Override
     public Resource createResource(@NotNull String fqname, String id, @Nullable Resource parent) throws ResourceCreationException {
         return createResource(new FQName(fqname), id, parent, null);
     }
 
     @Override
     public Resource createResource(@NotNull String fqname, String id) throws ResourceCreationException {
         return createResource(new FQName(fqname), id, null, null);
     }
 
     @Override
     public Resource createResource(@NotNull String fqname, String id, Map<String, String> attrs) throws ResourceCreationException, InvalidAttributeException {
         Resource resource = createResource(fqname, id);
         if (attrs != null) {
             for (Map.Entry<String, String> entry : attrs.entrySet()) {
                 resource.set(entry.getKey(), entry.getValue());
             }
         }
         return resource;
     }
 
     @Override
     public Resource createResource(@NotNull String fqname, @Nullable Collection<ResourceMatcher> importPaths) throws ResourceCreationException {
         return createResource(new FQName(fqname), null, null, importPaths);
     }
 
     @Override
     public Resource createResource(@NotNull FQName fqname) throws ResourceCreationException {
         return createResource(fqname, null, null, null);
     }
 
     @Override
     public Resource createResource(@NotNull String fqname) throws ResourceCreationException {
         return createResource(new FQName(fqname), null, null, null);
     }
 
     @Override
     public Resource createResource(@NotNull FQName fqname, @Nullable Resource parent) throws ResourceCreationException {
         return createResource(fqname, null, parent, null);
     }
 
     @Override
     public Resource createResource(@NotNull String fqname, @Nullable Resource parent) throws ResourceCreationException {
         return createResource(new FQName(fqname), null, parent, null);
     }
 
     @Override
     public Resource create(@NotNull String fqname, @Nullable String id, @Nullable Map<String, String> attrs, @Nullable Resource parent) throws ResourceCreationException, InvalidAttributeException {
         Resource resource = createResource(fqname, id, parent);
         if (attrs != null) {
             resource.set(attrs);
         }
         return resource;
     }
 
     @Override
     public Resource createResource(@NotNull Object obj) throws ResourceCreationException {
         Class<?> clazz = obj.getClass();
         STResource annotation = clazz.getAnnotation(STResource.class);
         if (annotation == null) {
             throw new ResourceCreationException("Attempted to create resource using java class which is not annotated with @STResource: " + obj.getClass().getName());
         }
         try {
             ResourceDefinition resourceDefinition = JavaResourceDefinitionFactory.create(clazz, null);
             registerResourceDefinition(resourceDefinition);
             Resource resource = createResource(resourceDefinition.getFQName());
             // TODO copy attributes
             return resource;
         } catch (InvalidResourceDefinitionException e) {
             throw new ResourceCreationException(e.getMessage(), e);
         }
     }
 
     @Override
     public Resource createResource(@NotNull String fqname, @Nullable Collection<ResourceMatcher> importPaths, @Nullable Resource parent) throws ResourceCreationException {
         return createResource(new FQName(fqname), null, parent, importPaths);
     }
 
     @Override
     @NotNull
     public List<Resource> findResources(@NotNull String query) throws InvalidQueryException {
         return new ResourceQuery(context, query, context.currentResource()).find(data.resources);
     }
 
     @Override
     @NotNull
     public List<Resource> findResources(@NotNull String query, @Nullable Resource baseResource) throws InvalidQueryException {
         return new ResourceQuery(context, query, baseResource).find(data.resources);
     }
 
     // -------------------------
     // Lookups
     // -------------------------
 
     @Override
     @NotNull
     public List<Resource> findResources(@Nullable String pkg, @Nullable String name, @Nullable String id) {
         rlock();
         try {
             ArrayList<Resource> results = new ArrayList<>();
             for (Resource resource : data.resources) {
                 if ((pkg != null && resource.getPkg().equalsIgnoreCase(pkg) ||
                         (name != null && resource.getName().equalsIgnoreCase(name)) ||
                         (isNotEmpty(id) && resource.getId().equals(id)))) {
                     results.add(resource);
                 }
             }
             return results;
         } finally {
             rulock();
         }
     }
 
     @NotNull
     @Override
     public ResourceDefinition findResourceDefinition(FQName name, @Nullable Collection<ResourceMatcher> importPaths) throws ResourceCreationException {
         rlock();
         try {
             ResourceFinder rfinder = new ResourceFinder(name, importPaths);
             if (!rfinder.found()) {
                 logger.debug("Unable to find pre-loaded resource {}, attempt to load dynamically", name);
                 // dynamically loading matching DSL file
                 if (name.getPkg() != null) {
                     dynaLoad(name.getPkg(), name.getName());
                 } else if (importPaths != null) {
                     for (ResourceMatcher importPath : importPaths) {
                         dynaLoad(importPath.getPkg(), name.getName());
                     }
                 }
                 // Retrying to find factory
                 rfinder = new ResourceFinder(name, importPaths);
             }
             return rfinder.getMatch();
         } finally {
             rulock();
         }
     }
 
     private void dynaLoad(@NotNull String pkg, @NotNull String name) throws ResourceCreationException {
         URL url = null;
         for (Library library : context.getLibraries()) {
             logger.debug("Attempting to dynamically load script in library {}", library.getLocalLocation());
             url = library.getElementScript(pkg, name);
             if (url != null) {
                 logger.debug("Script found in library {}", library.getLocalLocation());
                 break;
             }
         }
         if (url != null) {
             try {
                 context.runScript(pkg, url.toURI());
             } catch (URISyntaxException | ScriptException | IOException e) {
                 throw new ResourceCreationException(e.getMessage(), e);
             }
         }
     }
 
     @Override
     public List<Resource> findResourcesById(@NotNull String id) throws STRuntimeException {
         rlock();
         try {
             ArrayList<Resource> list = new ArrayList<>();
             for (Resource resource : data.resources) {
                 if (id.equals(resource.getId())) {
                     list.add(resource);
                 }
             }
             return list;
         } finally {
             rulock();
         }
     }
 
     @Override
     public Resource findResourcesByUid(String uid) {
         return data.resourcesUidIndex.get(uid);
     }
 
     // Resource registration
 
     @Override
     public void registerJavaResource(Class<?> clazz) throws InvalidResourceDefinitionException {
         FQName fqName = new FQName(clazz);
         if (fqName == null) {
             throw new InvalidResourceDefinitionException("No FQName specified for java resource: " + clazz.getName());
         } else {
             registerJavaResource(clazz, fqName);
         }
     }
 
     @Override
     public void registerJavaResource(Class<?> clazz, @NotNull String fqname) throws InvalidResourceDefinitionException {
         registerJavaResource(clazz, new FQName(fqname));
     }
 
     @Override
     public void registerJavaResource(Class<?> clazz, @NotNull FQName fqname) throws InvalidResourceDefinitionException {
         registerResourceDefinition(JavaResourceDefinitionFactory.create(clazz, fqname));
     }
 
 
     @Override
     public void registerResourceDefinitions(Collection<ResourceDefinition> resourceDefinitions) throws InvalidResourceDefinitionException {
         for (ResourceDefinition def : resourceDefinitions) {
             registerResourceDefinition(def);
         }
     }
 
     @Override
     public void registerResourceDefinition(ResourceDefinition resourceDefinition) throws InvalidResourceDefinitionException {
         wlock();
         try {
             ResourceDefinition existing = findResourceDefinition(resourceDefinition.getFQName());
             if (existing != null) {
                 existing.merge(resourceDefinition);
             } else {
                 resourceDefinition.validate();
                 data.resourceDefinitionsFQNIndex.put(resourceDefinition.getFQName(), resourceDefinition);
                 data.resourceDefinitions.add(resourceDefinition);
             }
         } finally {
             wulock();
         }
     }
 
     private ResourceDefinition findResourceDefinition(FQName fqname) {
         rlock();
         try {
             return data.resourceDefinitionsFQNIndex.get(fqname);
         } finally {
             rulock();
         }
     }
 
     @Override
     public void close() {
         wlock();
         try {
             for (ResourceDefinition factory : data.resourceDefinitions) {
                 try {
                     factory.close();
                 } catch (Exception e) {
                     logger.error(e.getMessage(), e);
                 }
             }
         } finally {
             wulock();
             closed = true;
         }
     }
 
     private void handleDependencyAttr(Resource resource, String attr) throws InvalidDependencyException {
         String value = resource.get(attr);
         if (isNotEmpty(value)) {
             try {
                 List<Resource> deps = context.findResources(value, resource.getParent());
                 if (deps.isEmpty()) {
                     throw new InvalidDependencyException("resource " + resource + " " + value + " attribute does not match any resources: " + value);
                 }
                 ManyToManyResourceDependency dependency;
                 if (attr.equals("after")) {
                     dependency = new ManyToManyResourceDependency(resource, deps);
                 } else if (attr.equals("before")) {
                     dependency = new ManyToManyResourceDependency(deps, resource);
                 } else {
                     throw new RuntimeException("BUG: Invalid dependency attribute " + attr);
                 }
                 addDependency(dependency);
             } catch (InvalidQueryException e) {
                 throw new InvalidDependencyException("Resource " + resource + " has an invalid " + attr + " attribute: " + value);
             }
         }
     }
 
     @Override
     public Set<ResourceDependency> getDependencies() {
         synchronized (data.m2mDependencies) {
             HashSet<ResourceDependency> set = new HashSet<>();
             set.addAll(data.o2mDependencies);
             set.addAll(data.m2mDependencies);
             return Collections.unmodifiableSet(set);
         }
     }
 
     @Override
     public void addDependency(ResourceDependency dependency) {
         synchronized (data.m2mDependencies) {
             if (dependency instanceof ManyToManyResourceDependency) {
                 data.m2mDependencies.add((ManyToManyResourceDependency) dependency);
             } else {
                 data.o2mDependencies.add((OneToManyResourceDependency) dependency);
             }
         }
     }
 
     @Override
     public void removeDependency(ResourceDependency dependency) {
         synchronized (data.m2mDependencies) {
             if (dependency instanceof ManyToManyResourceDependency) {
                 data.m2mDependencies.remove(dependency);
             } else {
                 data.o2mDependencies.remove(dependency);
             }
         }
     }
 
     @Override
     public boolean hasResources() {
         rlock();
         try {
             return !data.resources.isEmpty();
         } finally {
             rulock();
         }
     }
 
     @Override
     public void resolveDependencies(boolean strict) throws InvalidDependencyException {
         wlock();
         try {
             for (Resource resource : data.resources) {
                 ((ResourceImpl) resource).dependencies.clear();
                 ((ResourceImpl) resource).dependents.clear();
                 handleDependencyAttr(resource, "before");
                 handleDependencyAttr(resource, "after");
             }
             for (ManyToManyResourceDependency m2mDependency : data.m2mDependencies) {
                 data.o2mDependencies.addAll(m2mDependency.resolve(context));
             }
             data.m2mDependencies.clear();
             for (OneToManyResourceDependency dependency : data.o2mDependencies) {
                 Resource old = data.resourceScope.get();
                 data.resourceScope.set(dependency.getOrigin());
                 dependency.resolve(context);
                 if (old != null) {
                     data.resourceScope.set(old);
                 } else {
                     data.resourceScope.remove();
                 }
                 ResourceImpl origin = (ResourceImpl) dependency.getOrigin();
                 for (Resource target : dependency.getTargets()) {
                     if (target.isFailed()) {
                         origin.setFailed(true);
                     }
                     origin.dependencies.add(target);
                     ((ResourceImpl) target).dependents.add(origin);
                 }
             }
         } finally {
             wulock();
         }
     }
 
 
     private void rulock() {
         data.resourceListLock.readLock().unlock();
         if (closed) {
             throw new RuntimeException("Attempted to access resource manager that has already been closed.");
         }
     }
 
     private void rlock() {
         data.resourceListLock.readLock().lock();
     }
 
     private void wlock() {
         data.resourceListLock.writeLock().lock();
         if (closed) {
             throw new RuntimeException("Attempted to access resource manager that has already been closed.");
         }
     }
 
     private void wulock() {
         data.resourceListLock.writeLock().unlock();
     }
 
     public class ResourceFinder {
         private final FQName name;
         private ResourceDefinition fac;
 
         public ResourceFinder(FQName name, Collection<ResourceMatcher> importPaths) throws MultipleResourceMatchException {
             this.name = name;
             if (name.getPkg() != null) {
                 set(data.resourceDefinitionsFQNIndex.get(name));
             } else {
                 for (ResourceDefinition resourceDefinition : data.resourceDefinitions) {
                     if (ResourceMatcher.matchAll(importPaths, resourceDefinition.getFQName()) && resourceDefinition.getName().equals(name.getName())) {
                         set(resourceDefinition);
                     }
                 }
             }
         }
 
         public void set(ResourceDefinition newMatch) throws MultipleResourceMatchException {
             if (fac != null) {
                 throw new MultipleResourceMatchException("Found more than one match for " + name.getName() + ": " + fac.getFQName() + " and " + newMatch.getFQName().toString());
             } else {
                 fac = newMatch;
             }
         }
 
         public boolean found() {
             return fac != null;
         }
 
         public ResourceDefinition getMatch() throws ResourceNotFoundException {
             if (fac == null) {
                 throw new ResourceNotFoundException("Unable to find resource " + name);
             } else {
                 return fac;
             }
         }
     }
 }
