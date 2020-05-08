 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant.resource;
 
 import com.kloudtek.systyrant.FQName;
 import com.kloudtek.systyrant.STAction;
 import com.kloudtek.systyrant.STContext;
 import com.kloudtek.systyrant.Stage;
 import com.kloudtek.systyrant.exception.*;
 import com.kloudtek.systyrant.host.Host;
 import org.apache.commons.beanutils.ConvertUtils;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 
 public class Resource {
     private static final Logger logger = LoggerFactory.getLogger(Resource.class);
     private Map<String, String> attributes = new HashMap<>();
     private transient STContext context;
     private ResourceFactory factory;
     private Resource parent;
     private boolean executable;
     private HashSet<STAction> actions = new HashSet<>();
     private State state;
     private Host hostOverride;
     /**
      * This contains the results of any successful verification (meaning nothing has changed). A global verification
      * will be stored as an empty string, and a specific verification will be stored as it's name.
      */
     private final HashSet<String> verification = new HashSet<>();
     final HashSet<Resource> dependencies = new HashSet<>();
     final HashSet<Resource> indirectDependencies = new HashSet<>();
     final HashSet<Resource> dependents = new HashSet<>();
 
     public Resource(STContext context, ResourceFactory factory) {
         this.context = context;
         this.factory = factory;
         reset();
     }
 
     public void reset() {
         state = State.NEW;
         verification.clear();
     }
 
     // ----------------------------------------------------------------------
     // Dependency Management
     // ----------------------------------------------------------------------
 
     public Set<Resource> getDependencies() {
         return Collections.unmodifiableSet(dependencies);
     }
 
     public ResourceDependency addDependency(Resource resource) {
         return addDependency(resource, false);
     }
 
     public ResourceDependency addDependency(String ref) throws InvalidRefException {
         return addDependency(ref, false);
     }
 
     public ResourceDependency addDependency(Resource resource, boolean optional) {
         if (resource.equals(this)) {
             throw new IllegalArgumentException("Added dependency on self: " + resource);
         }
         ResourceDependency depRef = new OneToManyResourceDependency(this, resource, optional);
         context.getResourceManager().addDependency(depRef);
         return depRef;
     }
 
     public ResourceDependency addDependency(String ref, boolean optional) throws InvalidRefException {
         ResourceDependency dep = new OneToManyResourceDependency(this, ref, optional);
         context.getResourceManager().addDependency(dep);
         return dep;
     }
 
     public Resource getParent() {
         return parent;
     }
 
     public void setParent(Resource parent) {
         this.parent = parent;
     }
 
     // ----------------------------------------------------------------------
     // Meta-Data retrieval
     // ----------------------------------------------------------------------
 
     public Host getHost() {
         if (hostOverride != null) {
             return hostOverride;
         } else if (parent != null) {
             return parent.getHost();
         } else {
             return context.getHost();
         }
     }
 
    public synchronized Host getHostOverride() {
         return hostOverride;
     }
 
    public synchronized void setHostOverride(Host hostOverride) throws STRuntimeException {
         if( this.hostOverride != null && this.hostOverride != hostOverride ) {
             this.hostOverride.stop();
         }
         this.hostOverride = hostOverride;
         if( hostOverride != null ) {
             context.inject(hostOverride);
            synchronized (hostOverride) {
                if( ! hostOverride.isStarted() ) {
                    hostOverride.start();
                }
             }
         }
     }
 
     public ResourceFactory getFactory() {
         return factory;
     }
 
     public String getName() {
         return factory.getName();
     }
 
     public String getPkg() {
         return factory.getPkg();
     }
 
     public boolean isExecutable() {
         return executable;
     }
 
     public void setExecutable(boolean executable) {
         this.executable = executable;
     }
 
     public State getState() {
         return state;
     }
 
     public void setState(State state) {
         this.state = state;
     }
 
     // ------------------------------------------------------------------------------------------
     // Attributes
     // ------------------------------------------------------------------------------------------
 
     public Map<String, String> getAttributes() {
         return Collections.unmodifiableMap(attributes);
     }
 
     public void setAttributes(Map<String, Object> attributes) throws InvalidAttributeException {
         for (Map.Entry<String, Object> entry : attributes.entrySet()) {
             set(entry.getKey(), entry.getValue());
         }
     }
 
     /**
      * Sets the value for one of the resource's properties.
      *
      * @param key      Attribute key.
      * @param valueObj Attribute value.
      * @return This resource.
      */
     public Resource set(@NotNull String key, @Nullable Object valueObj) throws InvalidAttributeException {
         String value = ConvertUtils.convert(valueObj);
         attributes.put(key.toLowerCase(), value);
         return this;
     }
 
     public String get(@NotNull String key) {
         return attributes.get(key.toLowerCase().toLowerCase());
     }
 
     public void removeAttribute(@NotNull String key) {
         attributes.remove(key.toLowerCase());
     }
 
     public String getId() {
         return get("id");
     }
 
     public void setId(@Nullable String id) throws InvalidAttributeException {
         set("id", id);
     }
 
     public String getUid() {
         return get("uid");
     }
 
     public void setUid(@Nullable String uid) throws InvalidAttributeException {
         set("uid", uid);
     }
 
     public String generateUid() {
         StringBuilder tmp = new StringBuilder();
         Resource p = getParent();
         if (p != null) {
             tmp.append(p.generateUid()).append('.');
         }
         return tmp.append(getId()).toString();
     }
 
     public void addVerification(@NotNull String name) {
         synchronized (verification) {
             verification.add(name.toLowerCase());
         }
     }
 
     public boolean containsVerification(String verificationString) {
         synchronized (verification) {
             return verification.contains(verificationString.toLowerCase());
         }
     }
 
     public Set<String> getVerification() {
         synchronized (verification) {
             return Collections.unmodifiableSet(verification);
         }
     }
 
     // ----------------------------------------------------------------------
     // Actions
     // ----------------------------------------------------------------------
 
     public List<STAction> getActions() {
         return Collections.unmodifiableList(new ArrayList<>(actions));
     }
 
     public void addAction(STAction action) {
         actions.add(action);
     }
 
     // ----------------------------------------------------------------------
     // Utility functions
     // ----------------------------------------------------------------------
 
     public String toString() {
         StringBuilder tmp = new StringBuilder();
         tmp.append(factory.getFQName());
         String uid = getUid();
         if (uid != null) {
             tmp.append(":uid:").append(uid);
         } else {
             String id = getId();
             if (id != null) {
                 tmp.append(":id:").append(id);
             }
         }
         return tmp.toString();
     }
 
     public Logger logger() {
         return logger;
     }
 
     public STContext context() {
         return context;
     }
 
     public Host host() throws InvalidServiceException {
         return context.host();
     }
 
     public FQName getType() {
         return factory.getFQName();
     }
 
     // ----------------------------------------------------------------------
     // Processing Methods
     // ----------------------------------------------------------------------
 
     public void executeActions(Stage stage, boolean postChildren) throws STRuntimeException {
         for (STAction action : actions) {
             action.execute(this, stage, postChildren);
         }
     }
 
     public enum State {
         NEW, PREPARED, EXECUTED, CLEANEDUP, SKIP, FAILED
     }
 }
