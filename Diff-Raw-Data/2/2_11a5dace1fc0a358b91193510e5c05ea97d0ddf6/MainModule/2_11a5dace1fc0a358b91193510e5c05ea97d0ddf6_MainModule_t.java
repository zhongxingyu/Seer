 package org.mattstep.platform.samples.contact;
 
 import com.google.inject.Binder;
 import com.google.inject.Module;
 import com.google.inject.Scopes;
 import com.proofpoint.discovery.client.DiscoveryBinder;
 
 public class MainModule
         implements Module
 {
     public void configure(Binder binder)
     {
         binder.requireExplicitBindings();
         binder.disableCircularProxies();
 
         binder.bind(ContactResource.class).in(Scopes.SINGLETON);
 
        DiscoveryBinder.discoveryBinder(binder).bindHttpAnnouncement("contact");
     }
 }
