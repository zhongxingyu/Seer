 package io.cloudsoft.cloudera.builders;
 
 import io.cloudsoft.cloudera.brooklynnodes.ClouderaManagerNode;
 import io.cloudsoft.cloudera.brooklynnodes.ClouderaService;
 import io.cloudsoft.cloudera.rest.ClouderaRestCaller;
 import io.cloudsoft.cloudera.rest.RestDataObjects;
 import io.cloudsoft.cloudera.rest.RestDataObjects.ServiceRoleHostInfo;
 import io.cloudsoft.cloudera.rest.RestDataObjects.ServiceType;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import brooklyn.entity.Entity;
 import brooklyn.entity.basic.Entities;
 import brooklyn.entity.basic.EntityInternal;
 import brooklyn.entity.proxying.BasicEntitySpec;
 import brooklyn.util.collections.MutableMap;
 import brooklyn.util.text.Identifiers;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 
 
 public abstract class ServiceTemplate<T extends ServiceTemplate<?>> extends AbstractTemplate<T> {
     
     private static final Logger log = LoggerFactory.getLogger(ServiceTemplate.class);
 
     protected String clusterName;
     protected ClouderaManagerNode manager;
     protected Set<String> hostIds = new LinkedHashSet<String>();
     
     @SuppressWarnings("unchecked")
     public T cluster(String clusterName) {
         this.clusterName = clusterName;
         return (T)this;
     }
     public String getClusterName() {
         return clusterName;
     }
 
     @SuppressWarnings("unchecked")
     public T manager(ClouderaManagerNode manager) {
         this.manager = manager;
         return (T)this;
     }
     @SuppressWarnings("unchecked")
     public T discoverHostsFromManager() {
         Preconditions.checkNotNull(manager, "manager must be specified before can discover hosts");
         hosts(manager.getAttribute(ClouderaManagerNode.MANAGED_HOSTS));
         return (T)this;
     }
 
     @SuppressWarnings("unchecked")
     public T hosts(String ...hostIds) {
         Collections.addAll(this.hostIds, hostIds);
         return (T)this;
     }
     @SuppressWarnings("unchecked")
     public T hosts(Collection<String> hostIds) {
         for (Object hostId: hostIds) {
             // TODO convert from entities etc
             this.hostIds.add(""+hostId);
         }
         return (T)this;
     }
 
     protected boolean abortIfServiceExists = false;
     @SuppressWarnings("unchecked")
     public T abortIfServiceExists() {
         abortIfServiceExists = true;
         return (T)this;
     }
 
     private int roleIndex=0;
     public class RoleAssigner<U> {
         String type;
         
         public RoleAssigner(String roleType) {
             this.type = roleType;
         }
         
         @SuppressWarnings("unchecked")
         public U to(String host) {
             apply(Arrays.asList(host));
             return (U)ServiceTemplate.this;
         }
 
         @SuppressWarnings("unchecked")
         public U toAllHosts() {
             if (ServiceTemplate.this.hostIds.isEmpty()) 
                 throw new IllegalStateException("No hosts defined, when defining "+ServiceTemplate.this);
             apply(ServiceTemplate.this.hostIds);
             return (U)ServiceTemplate.this;
         }
 
         public U toAnyHost() {
             if (ServiceTemplate.this.hostIds.isEmpty()) 
                 throw new IllegalStateException("No hosts defined, when defining "+ServiceTemplate.this);
             if (roleIndex >= ServiceTemplate.this.hostIds.size()) roleIndex = 0;
             int ri = roleIndex;
             roleIndex++;
             Iterator<String> hi = ServiceTemplate.this.hostIds.iterator();
             while (ri>0) {
                 hi.next();
                 ri--;
             }
             return to(hi.next());
         }
 
         void apply(Collection<String> hosts) {
             for (String host: hosts) {
                 hostIds.add(host);
                 ServiceTemplate.this.roles.add(ServiceRoleHostInfo.newInstance(getName(), type, host));
             }
         }
     }
     
     List<ServiceRoleHostInfo> roles = new ArrayList<ServiceRoleHostInfo>();
     
     public RoleAssigner<T> assignRole(String role) {
         return new RoleAssigner<T>(role);
     }
 
     @SuppressWarnings("unchecked")
     public T useDefaultName() {
         if (name==null) name = getServiceType().name().toLowerCase()+"-"+Identifiers.makeRandomId(8);
         return (T)this;
     }
 
     public abstract ServiceType getServiceType();
     
     @Override
     public Boolean build(ClouderaRestCaller caller) {
         if (name==null) useDefaultName();
         
         List<String> clusters = caller.getClusters();
         if (clusterName==null) {
             if (!clusters.isEmpty()) clusterName = clusters.iterator().next();
             else clusterName = "cluster-"+Identifiers.makeRandomId(6);
         }
         if (!clusters.contains(clusterName)) caller.addCluster(clusterName);
 
         List<String> services = caller.getServices(clusterName);
         if (abortIfServiceExists && services.contains(name))
             return true;
        
         preServiceAddChecks(caller);
        
         caller.addService(clusterName, name, getServiceType());
         caller.addServiceRoleHosts(clusterName, name, roles.toArray(new ServiceRoleHostInfo[roles.size()]));
         
         Object config = caller.getServiceConfig(clusterName, name);
         Map<?,?> cfgOut = convertConfig(config);
         caller.setServiceConfig(clusterName, name, cfgOut);
 
         return startOnceBuilt(caller);
     }
 
     protected void preServiceAddChecks(ClouderaRestCaller caller) {
     }
     
     protected boolean startOnceBuilt(ClouderaRestCaller caller) {
         return caller.invokeServiceCommand(clusterName, name, "start").block(60*1000);
     }
 
     protected Map<?, ?> convertConfig(Object config) {
         log.debug("Config for CDH "+clusterName+"-"+name+" is: "+config);
         return RestDataObjects.convertConfigForSetting(config, clusterName+"-"+name);
     }
     
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public ClouderaService buildWithEntity(Map flags, Entity owner) {
         MutableMap flags2 = MutableMap.copyOf(flags);
         flags2.put("template", this);
         if (manager!=null) flags2.put("manager", manager);
         String name = (String) flags2.get("name");
        if (name==null) {
            if (name==null) useDefaultName();
            name = this.name;
            flags2.put("name", name);
        }
        ClouderaService result = ((EntityInternal)owner).getManagementSupport().getManagementContext().getEntityManager().
                createEntity(BasicEntitySpec.newInstance(ClouderaService.class).
                        configure(flags2));
        ((EntityInternal)owner).addChild(result);
         Entities.manage(result);
         result.create();
         try { 
             // pause a bit after creation, to ensure it really is created
             // (not needed, i don't think...)
             Thread.sleep(3000);
         } catch (InterruptedException e) {
             throw Throwables.propagate(e);
         }
         return result;
     }
     
     @SuppressWarnings("rawtypes")
     public ClouderaService buildWithEntity(Entity owner) { return buildWithEntity(new MutableMap(), owner); }
 
 }
