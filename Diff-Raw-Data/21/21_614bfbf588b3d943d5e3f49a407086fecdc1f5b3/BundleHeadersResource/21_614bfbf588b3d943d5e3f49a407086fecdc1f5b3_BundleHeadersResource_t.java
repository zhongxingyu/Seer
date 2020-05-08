 package org.apache.felix.ipojo.everest.osgi.bundle;
 
 import org.apache.felix.ipojo.everest.impl.ImmutableResourceMetadata;
 import org.apache.felix.ipojo.everest.osgi.AbstractResourceCollection;
 import org.apache.felix.ipojo.everest.osgi.ReadOnlyLeafCollectionResource;
 import org.apache.felix.ipojo.everest.osgi.packages.PackageResourceManager;
 import org.apache.felix.ipojo.everest.services.IllegalResourceException;
 import org.apache.felix.ipojo.everest.services.Path;
 import org.apache.felix.ipojo.everest.services.Resource;
 import org.apache.felix.ipojo.everest.services.ResourceMetadata;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.wiring.BundleCapability;
 import org.osgi.framework.wiring.BundleRequirement;
 import org.osgi.framework.wiring.BundleRevision;
 
 import java.util.*;
 
 import static org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils.PackageNamespace.REQUIREMENT_RESOLUTION_DIRECTIVE;
 import static org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils.PackageNamespace.RESOLUTION_DYNAMIC;
 import static org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils.metadataFrom;
import static org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils.uniqueCapabilityId;
 import static org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils.uniqueRequirementId;
 
 /**
  * Resource representing the bundle headers.
  */
 public class BundleHeadersResource extends AbstractResourceCollection {
 
     /**
      * Name for headers resource
      */
     public static final String HEADERS_PATH = "headers";
 
     /**
      * Name for export package
      */
     public static final String EXPORT_PACKAGE = "export-package";
 
     /**
      * Name for import package
      */
     public static final String IMPORT_PACKAGE = "import-package";
 
     /**
      * Name for dynamic import package
      */
     public static final String DYNAMIC_IMPORT_PACKAGE = "dynamicimport-package";
 
     /**
      * Name for require bundle
      */
     public static final String REQUIRE_BUNDLE = "require-bundle";
 
     /**
      * Name for fragment hosts
      */
     public static final String FRAGMENT_HOST = "fragment-host";
 
     /**
      * Bundle having the headers
      */
     private final Bundle m_bundle;
 
     /**
      * Static Metadata
      */
     private final ImmutableResourceMetadata metadata;
 
     /**
      * Constructor for bundle headers resource
      *
      * @param path   path of the resource
      * @param bundle concerned bundle
      */
     public BundleHeadersResource(Path path, Bundle bundle) {
         super(path.addElements(HEADERS_PATH));
         m_bundle = bundle;
         ImmutableResourceMetadata.Builder metadataBuilder = new ImmutableResourceMetadata.Builder();
         Dictionary<String, String> headers = m_bundle.getHeaders();
         //
         Enumeration<String> keys = headers.keys();
         while (keys.hasMoreElements()) {
             String key = keys.nextElement();
             //omitting those
             if (!(key.equals(Constants.EXPORT_PACKAGE) ||
                     key.equals(Constants.IMPORT_PACKAGE) ||
                     key.equals(Constants.DYNAMICIMPORT_PACKAGE) ||
                     key.equals(Constants.REQUIRE_BUNDLE) ||
                     key.equals(Constants.BUNDLE_NATIVECODE) ||
                     key.equals(Constants.FRAGMENT_HOST))) {
                 metadataBuilder.set(key, headers.get(key));
             }
         }
         metadata = metadataBuilder.build();
     }
 
     @Override
     public ResourceMetadata getMetadata() {
         return metadata;
     }
 
     @Override
     public List<Resource> getResources() {
         ArrayList<Resource> resources = new ArrayList<Resource>();
         BundleRevision rev = m_bundle.adapt(BundleRevision.class);
         // package export
         List<BundleCapability> capabilities = rev.getDeclaredCapabilities(BundleRevision.PACKAGE_NAMESPACE);
        Map<String, ResourceMetadata> exports = new HashMap<String, ResourceMetadata>();
        for (BundleCapability capability : capabilities) {
            String capabilityId = uniqueCapabilityId(capability);
            ResourceMetadata metadata = metadataFrom(new ImmutableResourceMetadata.Builder(), capability).build();
            exports.put(capabilityId,metadata);
         }
        Builder builder = PackageResourceManager.relationsBuilder(getPath().addElements(EXPORT_PACKAGE), capabilities);
        //builder.with();
//        try {
//            resources.add(builder.build());
//        } catch (IllegalResourceException e) {
//            // should never happen
//        }
        resources.add(new ReadOnlyLeafCollectionResource(getPath().addElements(EXPORT_PACKAGE),exports));
 
         // package import / dynamic import
         List<BundleRequirement> requirements = rev.getDeclaredRequirements(BundleRevision.PACKAGE_NAMESPACE);
         Map<String, ResourceMetadata> imports = new HashMap<String, ResourceMetadata>();
         Map<String, ResourceMetadata> dynamicImports = new HashMap<String, ResourceMetadata>();
         for (BundleRequirement req : requirements) {
 
             ResourceMetadata metadata = metadataFrom(new ImmutableResourceMetadata.Builder(), req).build();
             String reqId = uniqueRequirementId(req);
             if (RESOLUTION_DYNAMIC.equals(req.getDirectives().get(REQUIREMENT_RESOLUTION_DIRECTIVE))) {
                 dynamicImports.put(reqId, metadata);
             } else {
                 imports.put(reqId, metadata);
             }
         }
         // Dynamic import package leaf resource collection
         resources.add(new ReadOnlyLeafCollectionResource(getPath().addElements(DYNAMIC_IMPORT_PACKAGE), dynamicImports));
         // Import package leaf resource collection
         resources.add(new ReadOnlyLeafCollectionResource(getPath().addElements(IMPORT_PACKAGE), imports));
 
         // Bundle requirements
         List<BundleRequirement> bundleRequirements = rev.getDeclaredRequirements(BundleRevision.BUNDLE_NAMESPACE);
         Map<String, ResourceMetadata> bundleRequires = new HashMap<String, ResourceMetadata>();
         for (BundleRequirement req : bundleRequirements) {
             bundleRequires.put(uniqueRequirementId(req), metadataFrom(new ImmutableResourceMetadata.Builder(), req).build());
         }
         resources.add(new ReadOnlyLeafCollectionResource(getPath().addElements(REQUIRE_BUNDLE), bundleRequires));
         //fragment host requirements
         List<BundleRequirement> fragmentRequirements = rev.getDeclaredRequirements(BundleRevision.HOST_NAMESPACE);
         Map<String, ResourceMetadata> hosts = new HashMap<String, ResourceMetadata>();
         for (BundleRequirement fragmentRequirement : fragmentRequirements) {
             hosts.put(uniqueRequirementId(fragmentRequirement), metadataFrom(new ImmutableResourceMetadata.Builder(),fragmentRequirement).build());
         }
         resources.add(new ReadOnlyLeafCollectionResource(getPath().addElements(FRAGMENT_HOST), hosts));
 
         // TODO Native-Code
 
         return resources;
     }
 }
