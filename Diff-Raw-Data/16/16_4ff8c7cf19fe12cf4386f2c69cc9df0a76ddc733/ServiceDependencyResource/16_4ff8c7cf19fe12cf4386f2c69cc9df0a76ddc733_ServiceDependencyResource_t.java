 package org.apache.felix.ipojo.everest.ipojo;
 
 import org.apache.felix.ipojo.everest.core.Everest;
 import org.apache.felix.ipojo.everest.impl.DefaultReadOnlyResource;
 import org.apache.felix.ipojo.everest.impl.DefaultRelation;
 import org.apache.felix.ipojo.everest.impl.ImmutableResourceMetadata;
 import org.apache.felix.ipojo.everest.services.*;
 import org.apache.felix.ipojo.handlers.dependency.Dependency;
 import org.apache.felix.ipojo.handlers.dependency.DependencyDescription;
 import org.apache.felix.ipojo.util.DependencyModel;
 import org.apache.felix.ipojo.util.DependencyModelListener;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import static org.apache.felix.ipojo.everest.ipojo.IpojoRootResource.PATH_TO_OSGI_SERVICES;
 import static org.apache.felix.ipojo.util.DependencyModel.*;
 
 /**
  * '/ipojo/instance/$name/dependency/$id' resource.
  */
 public class ServiceDependencyResource extends DefaultReadOnlyResource implements DependencyModelListener {
 
     private WeakReference<DependencyDescription> m_description;
 
     public ServiceDependencyResource(InstanceResource instance, DependencyDescription description) {
         super(instance.getDependencies().getPath().addElements(description.getId()),
                 new ImmutableResourceMetadata.Builder()
                         .set("id", description.getId())
                         .set("specification", description.getSpecification()) // May become mutable is future iPOJO releases?
                         .set("isNullable", description.supportsNullable())
                         .set("isProxy", description.isProxy())
                         .set("defaultImplementation", description.getDefaultImplementation())
                                 // All other attributes are considered dynamic
                         .build());
         m_description = new WeakReference<DependencyDescription>(description);
         description.addListener(this);
     }
 
     @Override
     public ResourceMetadata getMetadata() {
         ResourceMetadata m = super.getMetadata();
         DependencyDescription d = m_description.get();
         if (d == null) {
             return m;
         } else {
             return new ImmutableResourceMetadata.Builder()
                     .set("state", stateToString(d.getState()))
                     .set("filter", d.getFilter())
                     .set("policy", policyToString(d.getPolicy()))
                     .set("comparator", d.getComparator())
                     .set("isAggregate", d.isMultiple())
                     .set("isOptional", d.isOptional())
                     .set("isFrozen", d.isFrozen())
                     .build();
         }
     }
 
     @Override
     public List<Relation> getRelations() {
         List<Relation> r = super.getRelations();
         DependencyDescription d = m_description.get();
         if (d == null) {
             return r;
         } else {
             r = new ArrayList<Relation>(r);
             @SuppressWarnings("unchecked")
             List<ServiceReference> matching = d.getServiceReferences();
             if (matching != null) {
                 for(ServiceReference<?> s : matching) {
                     String id = String.valueOf(s.getProperty(Constants.SERVICE_ID));
                     r.add(new DefaultRelation(
                             PATH_TO_OSGI_SERVICES.addElements(id),
                             Action.READ,
                             String.format("matchingService[%s]", id),
                             String.format("Matching service with id '%s'", id)));
                 }
             }
             @SuppressWarnings("unchecked")
             List<ServiceReference> used = d.getUsedServices();
            if (used != null) {
                for(ServiceReference<?> s : used) {
                    String id = String.valueOf(s.getProperty(Constants.SERVICE_ID));
                    r.add(new DefaultRelation(
                            PATH_TO_OSGI_SERVICES.addElements(id),
                            Action.READ,
                            String.format("usedService[%s]", id),
                            String.format("Used service with id '%s'", id)));
                }
             }
             return Collections.unmodifiableList(r);
         }
     }
 
     @Override
     public boolean isObservable() {
         return true;
     }
 
     @Override
     public <A> A adaptTo(Class<A> clazz) {
         if (clazz == DependencyModel.class) {
             return clazz.cast(getDependency(m_description.get()));
         } else if (clazz == DependencyDescription.class) {
             return clazz.cast(m_description.get());
         } else {
             return super.adaptTo(clazz);
         }
     }
 
     public void validate(DependencyModel dependency) {
         // Fire UPDATED event
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     public void invalidate(DependencyModel dependency) {
         // Fire UPDATED event
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     private static String policyToString(int policy) {
         switch (policy) {
             case DYNAMIC_BINDING_POLICY:
                 return "DYNAMIC_BINDING";
             case STATIC_BINDING_POLICY:
                 return "STATIC_BINDING";
             case DYNAMIC_PRIORITY_BINDING_POLICY:
                 return "DYNAMIC_PRIORITY_BINDING";
             default:
                 return "CUSTOMIZED"; // Optimistic!
         }
     }
 
     private static String stateToString(int state) {
         switch (state) {
             case BROKEN:
                 return "BROKEN";
             case UNRESOLVED:
                 return "UNRESOLVED";
             case RESOLVED:
                 return "RESOLVED";
             default:
                 return "unknown";
         }
     }
 
     // WARN: This is a hack!
     private static Dependency getDependency(DependencyDescription description) {
         if (description == null) {
             return null;
         }
         Field shunt = null;
         try {
             shunt = DependencyDescription.class.getDeclaredField("m_dependency");
             shunt.setAccessible(true);
             return (Dependency) shunt.get(description);
         } catch (Exception e) {
             throw new IllegalStateException("cannot get service dependency", e);
         } finally {
             if (shunt != null) {
                 shunt.setAccessible(false);
             }
         }
     }
 
     public void matchingServiceArrived(DependencyModel dependency, ServiceReference<?> service) {
         // Fire UPDATED event
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     public void matchingServiceModified(DependencyModel dependency, ServiceReference<?> service) {
         // Fire UPDATED event
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     public void matchingServiceDeparted(DependencyModel dependency, ServiceReference<?> service) {
         // Fire UPDATED event
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     public void serviceBound(DependencyModel dependency, ServiceReference<?> service, Object object) {
         // Fire UPDATED event
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     public void serviceUnbound(DependencyModel dependency, ServiceReference<?> service, Object object) {
         // Fire UPDATED event
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     public void reconfigured(DependencyModel dependency) {
         // Fire UPDATED event
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     public void cleanup() {
         // Remove the listener
         DependencyDescription d = m_description.get();
         if (d != null) {
             try {
                 d.removeListener(this);
             } catch (NoSuchElementException e) {
                 // Swallow
             }
         }
     }
 }
