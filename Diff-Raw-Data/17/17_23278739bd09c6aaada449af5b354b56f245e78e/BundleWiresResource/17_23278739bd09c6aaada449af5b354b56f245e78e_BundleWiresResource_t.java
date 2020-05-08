 package org.apache.felix.ipojo.everest.osgi.bundle;
 
 import org.apache.felix.ipojo.everest.impl.DefaultReadOnlyResource;
 import org.apache.felix.ipojo.everest.impl.ImmutableResourceMetadata;
 import org.apache.felix.ipojo.everest.services.Path;
 import org.apache.felix.ipojo.everest.services.Resource;
 import org.apache.felix.ipojo.everest.services.ResourceMetadata;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.wiring.BundleCapability;
 import org.osgi.framework.wiring.BundleRequirement;
 import org.osgi.framework.wiring.BundleWiring;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.apache.felix.ipojo.everest.osgi.OsgiResourceUtils.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: ozan
  * Date: 4/21/13
  * Time: 3:52 PM
  */
 public class BundleWiresResource extends DefaultReadOnlyResource {
 
     public static final String WIRES_PATH = "wires";
 
     public static final String REQUIREMENTS_PATH = "requirements";
 
     public static final String REQUIREMENT_PATH = "requirement";
 
     public static final String CAPABILITIES_PATH = "capabilities";
 
     public static final String CAPABILITY_PATH = "capability";
 
     private final Bundle m_bundle;
 
    List<BundleCapability> capabilities = new ArrayList<BundleCapability>();
 
    List<BundleRequirement> requirements = new ArrayList<BundleRequirement>();
 
     public BundleWiresResource(Path path, Bundle bundle) {
         super(path.addElements(WIRES_PATH));
         m_bundle = bundle;
         BundleWiring wiring = m_bundle.adapt(BundleWiring.class);
        if (wiring != null) {
            // get capabilities from all namespaces
            capabilities.addAll(wiring.getCapabilities(null));
            // get requirements from all namespaces
            requirements.addAll(wiring.getRequirements(null));
        }
 //        // get provided wires from all namespaces
 //        List<BundleWire> providedWires = wiring.getProvidedWires(null);
 //        for (BundleWire providedWire : providedWires) {
 //            BundleCapability capability = providedWire.getCapability();
 //            if (capabilities.containsKey(providedWire.getCapability())) {
 //                capabilities.get(capability).add(providedWire);
 //            } else {
 //                Set<BundleWire> newWireSet = new HashSet<BundleWire>();
 //                newWireSet.add(providedWire);
 //                capabilities.put(capability, newWireSet);
 //            }
 //        }
 //
 //        // get required wires from all namespaces
 //        List<BundleWire> requiredWires = wiring.getRequiredWires(null);
 //        for (BundleWire requiredWire : requiredWires) {
 //            BundleRequirement requirement = requiredWire.getRequirement();
 //            if (requirements.containsKey(requirement)) {
 //                requirements.get(requirement).add(requiredWire);
 //            } else {
 //                Set<BundleWire> newWireSet = new HashSet<BundleWire>();
 //                newWireSet.add(requiredWire);
 //                requirements.put(requirement, newWireSet);
 //            }
 //        }
 
     }
 
     @Override
     public ResourceMetadata getMetadata() {
         ImmutableResourceMetadata.Builder metadataBuilder = new ImmutableResourceMetadata.Builder();
         for (BundleCapability capability : capabilities) {
             metadataBuilder.set(uniqueCapabilityId(capability), metadataFrom(new ImmutableResourceMetadata.Builder(), capability).build());
         }
         for (BundleRequirement requirement : requirements) {
             metadataBuilder.set(uniqueRequirementId(requirement), metadataFrom(new ImmutableResourceMetadata.Builder(), requirement).build());
         }
         return metadataBuilder.build();
     }
 
     @Override
     public List<Resource> getResources() {
         ArrayList<Resource> resources = new ArrayList<Resource>();
         for (BundleCapability capability : capabilities) {
             resources.add(new BundleCapabilityResource(getPath().addElements(CAPABILITIES_PATH), capability));
         }
         for (BundleRequirement requirement : requirements) {
             resources.add(new BundleRequirementResource(getPath().addElements(REQUIREMENTS_PATH), requirement));
         }
         return resources;
     }
 }
