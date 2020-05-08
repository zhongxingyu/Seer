 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import net.wgr.xenmaster.api.helpers.iSCSI;
 import net.wgr.xenmaster.api.store.Store;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import net.wgr.xenmaster.controller.Controller;
 
 /**
  * 
  * @created Oct 16, 2011
  * @author double-u
  */
 public class SR extends NamedEntity {
 
     @Fill
     protected Object[] allowedOperations;
     @Fill
     protected Map<String, String> currentOperations;
     protected long virtualAllocation, physicalUtilisation;
     protected long physicalSize;
     protected boolean shared;
     @Fill(storeExternally = true)
     protected Map<String, String> smConfig, otherConfig;
     protected String type, contentType;
     protected boolean localCache;
     public static final String ASSOCIATED_PBDS = "associatedSRPBDs";
 
     public SR(String ref, boolean autoFill) {
         super(ref, autoFill);
     }
 
     public SR(String ref) {
         super(ref);
     }
 
     public SR() {
     }
 
     public String create(Host host, Map<String, String> deviceConfig, Type type, String contentType, boolean shared, int size) throws BadAPICallException {
         if (host == null || deviceConfig == null || name == null || type == null) {
             throw new IllegalArgumentException("Some essential arguments haven't been supplied");
         }
         return create(host, deviceConfig, type.name().toLowerCase(), contentType, shared, size);
     }
 
     public String create(Host host, iSCSI cfg, String contentType, boolean shared, int size) throws BadAPICallException {
         if (cfg.getType() == null) {
             throw new IllegalArgumentException("No iSCSI type was set");
         }
         return create(host, cfg.toDeviceConfig(), cfg.getType().name().toLowerCase(), contentType, shared, size);
     }
 
     protected String create(Host host, Map<String, String> deviceConfig, String type, String contentType, boolean shared, int size) throws BadAPICallException {
         if (reference != null) {
             throw new IllegalArgumentException("Object reference is set");
         }
         if (host == null || deviceConfig == null || (name == null || name.isEmpty()) || (type == null || type.isEmpty())) {
             throw new IllegalArgumentException("Some essential arguments haven't been supplied");
         }
         if (smConfig == null) {
             smConfig = new HashMap<>();
         }
 
         this.reference = (String) dispatch("create", host.getIDString(), deviceConfig, "" + size, name, description, type.toLowerCase(), contentType, shared, smConfig);
 
         return this.reference;
     }
 
     public String introduce(Type type, String contentType, boolean shared) throws BadAPICallException {
         if (smConfig == null) {
             smConfig = new HashMap<>();
         }
 
         this.type = type.name().toLowerCase();
         this.contentType = contentType;
         this.shared = shared;
         this.reference = (String) Controller.dispatch("SR.introduce", UUID.randomUUID().toString(), name, description, this.type, contentType, shared, smConfig);
 
         return this.reference;
     }
 
     public String probe(Host host, iSCSI cfg) throws BadAPICallException {
         if (smConfig == null) {
             smConfig = new HashMap<>();
         }
         return (String) Controller.dispatch("SR.probe", host.getReference(), cfg.toDeviceConfig(), cfg.getType().name().toLowerCase(), smConfig);
     }
 
     public void setAsDefault(Pool p) throws BadAPICallException {
         Controller.dispatch("pool.set_default_SR", p.getReference(), this.reference);
     }
 
     public void destroy() throws BadAPICallException {
         dispatch("destroy");
     }
 
     public void forget() throws BadAPICallException {
         try {
             dispatch("forget");
         } catch (BadAPICallException ex) {
             if (ex.getErrorName().equals("SR_HAS_PBD")) {
                 for (String ref : Store.get().get(ASSOCIATED_PBDS, this.reference)) {
                     PBD pbd = new PBD(ref, false);
                     pbd.unplug();
                 }
                 dispatch("forget");
             } else {
                 throw ex;
             }
         }
     }
 
     public void update() throws BadAPICallException {
         dispatch("update");
     }
     
     public List<VDI> getVDIs() throws BadAPICallException {
         return getEntities(VDI.class, "get_VDIs");
     }
     
      public List<PBD> getPBDs() throws BadAPICallException {
         return getEntities(PBD.class, "get_PBDs");
     }
 
     public static List<SR> getAll() throws BadAPICallException {
         return getAllEntities(SR.class);
     }
 
     public boolean usesLocalCache() {
         return localCache;
     }
 
     public void setLocalCache(boolean useLocalCache) {
         this.localCache = useLocalCache;
     }
 
     public Map<String, String> getOtherConfig() {
         return otherConfig;
     }
 
     public void setOtherConfig(Map<String, String> otherConfig) {
         this.otherConfig = otherConfig;
     }
 
     public Map<String, String> getSmConfig() {
         return smConfig;
     }
 
     public void setSmConfig(Map<String, String> smConfig) {
         this.smConfig = smConfig;
     }
 
     public Object[] getAllowedOperations() {
         return allowedOperations;
     }
 
     public String getContentType() {
         return contentType;
     }
 
     public Map<String, String> getCurrentOperations() {
         return currentOperations;
     }
 
     public long getPhysicalUtilisation() {
         return physicalUtilisation;
     }
     
     public long getPhysicalSize() {
         return physicalSize;
     }
 
     public boolean isShared() {
         return shared;
     }
 
     public Type getType() {
         return Type.valueOf(type);
     }
 
     public long getVirtualAllocation() {
         return virtualAllocation;
     }
 
     @Override
     protected Map<String, String> interpretation() {
         HashMap<String, String> map = (HashMap<String, String>) super.interpretation();
         map.put("localCache", "local_cache_enabled");
 
         return map;
     }
 
     public static enum Type {
 
         EXT, File, LVM, NFS, ISO
     }
 }
