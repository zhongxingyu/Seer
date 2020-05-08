 package org.apache.felix.ipojo.everest.osgi;
 
 import org.apache.felix.ipojo.everest.impl.DefaultReadOnlyResource;
 import org.apache.felix.ipojo.everest.services.Path;
 import org.apache.felix.ipojo.everest.services.Resource;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.ServiceReference;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ozan
  * Date: 4/21/13
  * Time: 4:13 PM
  */
 public class BundleServicesResource extends DefaultReadOnlyResource {
 
     public static final String BUNDLE_SERVICES_NAME = "services";
 
     public static final String BUNDLE_REGISTERED_SERVICES_NAME = "registered";
 
     public static final String BUNDLE_USE_SERVICES_NAME = "uses";
     private final Bundle m_bundle;
 
 
     public BundleServicesResource(Path path, Bundle bundle) {
         super(path.add(Path.from(Path.SEPARATOR + BUNDLE_SERVICES_NAME)));
         m_bundle = bundle;
     }
 
     @Override
     public List<Resource> getResources() {
         ArrayList<Resource> resources = new ArrayList<Resource>();
         // bundle registered services
         ServiceReference[] registered = m_bundle.getRegisteredServices();
        resources.add(new ReadOnlyServiceSymLinksResource(getPath().add(Path.from(Path.SEPARATOR + BUNDLE_REGISTERED_SERVICES_NAME)), registered));
         // bundle used services
         ServiceReference[] uses = m_bundle.getServicesInUse();
        resources.add(new ReadOnlyServiceSymLinksResource(getPath().add(Path.from(Path.SEPARATOR + BUNDLE_USE_SERVICES_NAME)), uses));
         return resources;
     }
 }
