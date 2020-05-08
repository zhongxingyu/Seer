 package org.apache.felix.ipojo.everest.ipojo.test;
 
 import org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils;
 import org.apache.felix.ipojo.everest.osgi.bundle.BundleCapabilityResource;
 import org.apache.felix.ipojo.everest.osgi.bundle.BundleRequirementResource;
 import org.apache.felix.ipojo.everest.osgi.bundle.BundleResource;
 import org.apache.felix.ipojo.everest.osgi.bundle.BundleWireResource;
 import org.apache.felix.ipojo.everest.osgi.packages.PackageResource;
 import org.apache.felix.ipojo.everest.services.IllegalActionOnResourceException;
 import org.apache.felix.ipojo.everest.services.Relation;
 import org.apache.felix.ipojo.everest.services.Resource;
 import org.apache.felix.ipojo.everest.services.ResourceNotFoundException;
 import org.junit.Assert;
 import org.junit.Test;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.framework.Version;
 import org.osgi.framework.wiring.BundleCapability;
 import org.osgi.framework.wiring.BundleRequirement;
 import org.osgi.framework.wiring.BundleWire;
 import org.osgi.service.event.Event;
 import org.osgi.service.packageadmin.ExportedPackage;
 
 import java.util.List;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: ozan
  * Date: 4/22/13
  * Time: 4:24 PM
  */
 public class TestOsgiResources extends EverestOsgiTest {
 
     /**
      * Check that the '/osgi' resource is present.
      */
     @Test
     public void testOsgiRootIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource osgi = get("/osgi");
         Assert.assertNotNull(osgi);
     }
 
     @Test
     public void testBundlesResourceIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Assert.assertNotNull(get("/osgi/bundles"));
     }
 
     @Test
     public void testPackagesResourceIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Assert.assertNotNull(get("/osgi/packages"));
     }
 
     @Test
     public void testServicesResourceIsPresent() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Assert.assertNotNull(get("/osgi/services"));
     }
 
     /**
      * Check that first level resourcemanagers are present.
      */
     @Test
     public void testOsgiRootResources() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource osgi = get("/osgi");
         assertThat(osgi).isNotNull();
         List<Resource> resources = osgi.getResources();
         assertThat(resources.size()).isGreaterThanOrEqualTo(4).describedAs("We must have at least 4 resources as we added config admin in tests ");
         for (Resource r : resources) {
             Assert.assertEquals(r.getPath(), r.getCanonicalPath());
         }
     }
 
     @Test
     public void testBundles() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource bundles = get("/osgi/bundles");
         for (Resource resource : bundles.getResources()) {
             Bundle bundle = resource.adaptTo(Bundle.class);
             BundleResource bundleResource = resource.adaptTo(BundleResource.class);
             assertThat(bundle).isNotNull();
             assertThat(bundleResource).isNotNull();
             assertThat(bundleResource.getBundle()).isEqualTo(bundle);
         }
     }
 
     @Test
     public void testUsedPackages() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource packages = get("/osgi/packages");
         assertThat(osgiHelper.getPackageAdmin()).isNotNull();
         for (Resource pkg : packages.getResources()) {
             PackageResource packageResource = pkg.adaptTo(PackageResource.class);
             String packageName = packageResource.getPackageName();
             boolean used = pkg.getMetadata().get("in-use", Boolean.class);
             assertThat(used).isEqualTo(packageResource.isUsed());
             ExportedPackage exportedPackage = osgiHelper.getPackageAdmin().getExportedPackage(packageName);
             assertThat(exportedPackage).isNotNull();
             if (used) {
                 assertThat(pkg.getMetadata().get("version", Version.class)).isEqualTo(exportedPackage.getVersion());
                 assertThat(exportedPackage.getImportingBundles()).isNotEmpty();
             }
         }
     }
 
     @Test
     public void testServicesList() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource r = get("/osgi/services");
         int size = r.getResources().size();
         ServiceRegistration reg = osgiHelper.getContext().registerService(this.getClass().getName(), this, null);
         assertThat(r.getResources().size()).isEqualTo(size + 1);
         Event last = createdEvents.getLast();
         System.out.println(last.getTopic() + " " + last.getProperty("eventType"));
     }
 
     @Test
     public void testBundleWiring() throws ResourceNotFoundException, IllegalActionOnResourceException {
         Resource bundles = get("/osgi/bundles");
         for (int i = 0; i < bundles.getResources().size(); i++) {
             Resource wires = get("/osgi/bundles/" + i + "/wires");
             for (Resource wire : wires.getResources()) {
                 assertThat(wire).isNotNull();
                 //System.out.println(wire.getPath().toString());
                 BundleWire bundleWire = wire.adaptTo(BundleWire.class);
                 BundleWireResource bundleWireResource = wire.adaptTo(BundleWireResource.class);
                 // check capability requirement relations
                 for (Relation capsReqs : wire.getRelations()) {
                     Resource resource = get(capsReqs.getHref().toString());
                     assertThat(resource).isNotNull();
                     BundleCapabilityResource bundleCapabilityResource = resource.adaptTo(BundleCapabilityResource.class);
                    BundleRequirementResource bundleRequirementResource = resource.adaptTo(BundleRequirementResource.class);
 
                     if (bundleCapabilityResource != null) { // then it is a capability
                         BundleCapability capability = bundleCapabilityResource.adaptTo(BundleCapability.class);
                         long bundleId = capability.getRevision().getBundle().getBundleId();
                         String capabilityId = OsgiResourceUtils.uniqueCapabilityId(capability);
                         Resource capabilityResource = get("/osgi/bundles/" + bundleId + "/capabilities/" + capabilityId);
                         assertThat(capabilityResource).isEqualTo(bundleCapabilityResource);
                         assertThat(bundleWire.getCapability()).isEqualTo(capability);
                         if (bundleCapabilityResource.isPackage()) {
                             //bundleCapabilityResource
                             for (Relation relation : bundleCapabilityResource.getRelations()) {
                                 if (relation.getName().equals("package")) {
                                     Resource pkg = get(relation.getHref().toString());
                                     PackageResource packageResource = pkg.adaptTo(PackageResource.class);
                                     assertThat(packageResource).isNotNull();
                                     assertThat(packageResource.isUsed()).isTrue();
                                 }
                             }
 
                         }
                     }
 
                     if (bundleRequirementResource != null) { // then it is a requirement
                         BundleRequirement requirement = bundleRequirementResource.adaptTo(BundleRequirement.class);
                         long bundleId = requirement.getRevision().getBundle().getBundleId();
                         String requirementId = OsgiResourceUtils.uniqueRequirementId(requirement);
                         Resource requirementResource = get("/osgi/bundles/" + bundleId + "/requirements/" + requirementId);
                         assertThat(requirementResource).isEqualTo(bundleRequirementResource);
                         assertThat(bundleWire.getRequirement()).isEqualTo(requirement);
                         if (bundleRequirementResource.isBundle()) {
                             for (Relation relation : bundleRequirementResource.getRelations()) {
                                 if (relation.getName().equals("require-bundle")) {
                                     Resource bundle = get(relation.getHref().toString());
                                     assertThat(bundle).isNotNull();
                                     //System.out.println(bundle.getPath());
                                 }
                             }
 
                         }
                         if (bundleRequirementResource.isPackage()) {
                             for (Relation relation : bundleRequirementResource.getRelations()) {
                                 if (relation.getName().equals("import-package")) {
                                     Resource pkgImport = get(relation.getHref().toString());
                                     assertThat(pkgImport).isNotNull();
                                     //System.out.println(pkgImport.getPath());
                                 }
 
                             }
 
                         }
                     }
                     //System.out.println(capsReqs.getName()+" "+capsReqs.getHref());
                 }
             }
         }
     }
 
 }
