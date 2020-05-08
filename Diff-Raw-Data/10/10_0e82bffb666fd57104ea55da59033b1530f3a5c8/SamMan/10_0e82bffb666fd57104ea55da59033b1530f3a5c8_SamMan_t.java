 package fr.imag.adele.samMan;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.osgi.framework.Filter;
 
 import fr.imag.adele.am.exception.ConnectionException;
 import fr.imag.adele.am.query.Query;
 import fr.imag.adele.am.query.QueryLDAPImpl;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.ManagerModel;
 import fr.imag.adele.apam.apamAPI.ASMImpl;
 import fr.imag.adele.apam.apamAPI.ASMInst;
 import fr.imag.adele.apam.apamAPI.ASMSpec;
 import fr.imag.adele.apam.apamAPI.CompositeType;
 import fr.imag.adele.apam.apamAPI.Composite;
 import fr.imag.adele.apam.apamAPI.Manager;
 import fr.imag.adele.apam.apamAPI.ManagersMng;
 import fr.imag.adele.apam.util.AttributesImpl;
 import fr.imag.adele.apam.util.Util;
 import fr.imag.adele.sam.Implementation;
 import fr.imag.adele.sam.Instance;
 import fr.imag.adele.sam.Specification;
 import fr.imag.adele.sam.broker.ImplementationBroker;
 import fr.imag.adele.sam.broker.InstanceBroker;
 
 public class SamMan implements Manager {
 
     /*
      * The reference to APAM, injected by iPojo
      */
     private ManagersMng apam;
 
     @Override
     public String getName() {
         return "SAMMAN";
     }
 
     /**
      * SANMAN activated, register with APAM
      */
     public void start() {
         apam.addManager(this, 0);
     }
 
     public void stop() {
         apam.removeManager(this);
     }
 
     // TODO Must read the opportunist model and build the list of opportunist specs.
     // If empty, all is supposed to be opportunist ???
     private static Set<String> opportunismNames = new HashSet<String>();
     private static boolean     specOpportunist  = true;                 // if the model says all specifications are
     // opportunist
     private static boolean     implOpportunist  = true;                 // if the model says all implementations are
 
     // opportunist
 
     private boolean opportunistSpec(String specName) {
         if (SamMan.specOpportunist)
             return true;
         return true; // TODO waiting for the models
     }
 
     private boolean opportunistImpl(String implName) {
         if (SamMan.implOpportunist)
             return true;
         return SamMan.opportunismNames.contains(implName);
     }
 
     @Override
     public void getSelectionPathSpec(CompositeType compTypeFrom, String interfaceName, String[] interfaces,
             String specName, Set<Filter> constraints, List<Filter> preferences, List<Manager> selPath) {
         if (opportunistSpec(specName)) {
             selPath.add(this);
         }
     }
 
     @Override
     public void getSelectionPathImpl(CompositeType compTypeFrom, String implName, List<Manager> selPath) {
         if (opportunistImpl(implName))
             selPath.add(this);
     }
 
     @Override
     public void getSelectionPathInst(Composite compoFrom, ASMImpl impl,
             Set<Filter> constraints, List<Filter> preferences, List<Manager> selPath) {
         if (opportunistImpl(impl.getName()))
             selPath.add(this);
     }
 
     private Set<Instance> getSamInstanceInterf(String interfaceName) {
         Set<Instance> insts = new HashSet<Instance>();
         try {
             for (Instance instance : CST.SAMInstBroker.getInstances()) {
                 Specification samSpec = instance.getSpecification();
                 if (samSpec != null) {
                     String[] interfs = samSpec.getInterfaceNames();
                     for (String interf : interfs) {
                         if (interf.equals(interfaceName)) { // we found one
                             insts.add(instance);
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return insts;
     }
 
     private Set<Instance> getSamInstanceInterfaces(String[] interfaces) {
         Set<Instance> insts = new HashSet<Instance>();
         try {
             for (Instance instance : CST.SAMInstBroker.getInstances()) {
                 Specification samSpec = instance.getSpecification();
                 if (samSpec != null) {
                     String[] interfs = samSpec.getInterfaceNames();
                     if (Util.sameInterfaces(interfs, interfaces)) {
                         insts.add(instance);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return insts;
     }
 
     private boolean samSpecMatchInterface(Specification samSpec, String interfaceName, String[] interfaces) {
         if (samSpec == null)
             return false;
         if (interfaceName != null) {
             for (String interf : samSpec.getInterfaceNames()) {
                 if (interf.equals(interfaceName)) {
                     return true;
                 }
             }
         }
         return Util.sameInterfaces(samSpec.getInterfaceNames(), interfaces);
     }
 
     // WARNING : to be used by AmpamMan to find sam instances
     // if allInst != null, all the instances are to be returned.
     public ASMInst findSamInstForImpl(Composite compo, ASMImpl impl, Set<Filter> constraints,
             List<Filter> preferences, Set<ASMInst> allInst) {
 
        // composite type do not have unknown instances in SAM. Further, their samImpl is the main.
        if (impl instanceof CompositeType)
            return null;
         Set<Instance> allInstances = new HashSet<Instance>();
         try {
             for (Instance inst : impl.getSamImpl().getInstances()) {
                 // ignore the Apam instances, they have been checked by ApamMan
                 if (CST.ASMInstBroker.getInst(inst) == null) {
                     allInstances.add(inst);
                 }
             }
 
             // check if it satisfies the constraints
             boolean match = false;
             Query query = null;
             if ((constraints == null) || constraints.isEmpty()) {
                 match = true;
             } else {
                 Filter filter = Util.buildFilter(constraints);
                 query = new QueryLDAPImpl(filter.toString());
             }
             // Set <ASMInst> matchInsts = new HashSet<ASMInst>();
             for (Instance inst : allInstances) {
                 if (match || inst.match(query))
                     if (allInst == null) {
                         // return the first that matches
                         return CST.ASMInstBroker.addSamInst(compo, inst, null, null);
                     } else
                         allInst.add(CST.ASMInstBroker.addSamInst(compo, inst, null, null));
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public ASMInst resolveImpl(Composite compo, ASMImpl impl, Set<Filter> constraints,
             List<Filter> preferences) {
         return findSamInstForImpl(compo, impl, constraints, preferences, null);
     }
 
     @Override
     public Set<ASMInst> resolveImpls(Composite compo, ASMImpl impl, Set<Filter> constraints) {
         Set<ASMInst> allInsts = new HashSet<ASMInst>();
         findSamInstForImpl(compo, impl, constraints, null, allInsts);
         return allInsts;
     }
 
     @Override
     public int getPriority() {
         return 0;
     }
 
     @Override
     public void newComposite(ManagerModel model, CompositeType composite) {
 
     }
 
     @Override
     public ASMImpl findImplByName(CompositeType compoType, String implName) {
         try {
             // already in Apam, and not visible !
             if (CST.ASMImplBroker.getImpl(implName) != null)
                 return null;
             Implementation samImpl = CST.SAMImplBroker.getImplementation(implName);
             // In Sam but not in Apam. Create it in Apam.
             if (samImpl != null) {
                 return CST.ASMImplBroker.addImpl(compoType, implName, null, null);
             }
         } catch (ConnectionException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public ASMImpl resolveSpecByName(CompositeType compoType, String specName,
             Set<Filter> constraints, List<Filter> preferences) {
         if (specName == null) {
             new Exception("no name provided").printStackTrace();
         }
 
         try {
             ASMSpec asmSpec = CST.ASMSpecBroker.getSpec(specName);
             // Look by its specification
             Set<Implementation> implInterf = new HashSet<Implementation>();
             if (asmSpec != null) { // Look by its sam interface
                 Specification spec = asmSpec.getSamSpec();
                 if (spec != null) { // Is sam spec known ?
                     spec = CST.SAMSpecBroker.getSpecification(specName);
                     if (spec == null)
                         return null;
                     implInterf = spec.getImplementations();
                 }
             }
             // eliminate those implems that have an Apam impl. it has already been checked by ApamMan
             Set<Implementation> allImplementations = new HashSet<Implementation>();
             for (Implementation in : implInterf) {
                 if (CST.ASMImplBroker.getImpl(in.getName()) == null)
                     allImplementations.add(in);
             }
 
             // check if it satisfies the constraints
             Set<Implementation> matchImpls;
             if ((constraints == null) || constraints.isEmpty()) {
                 matchImpls = allImplementations;
             } else {
                 matchImpls = new HashSet<Implementation>();
                 Filter filter = Util.buildFilter(constraints);
                 Query query = new QueryLDAPImpl(filter.toString());
                 for (Implementation impl : allImplementations) {
                     // warning : match on an arbitrary instance !
                     if (impl.getInstance().match(query))
                         matchImpls.add(impl);
                 }
             }
 
             Implementation impl = getPreferedImpl(matchImpls, preferences);
             return CST.ASMImplBroker.addImpl(compoType, impl.getName(), specName, null);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public Implementation getPreferedImpl(Set<Implementation> candidates, List<Filter> preferences) {
         if ((preferences == null) || preferences.isEmpty()) {
             if (candidates.isEmpty())
                 return null;
             else
                 return (Implementation) candidates.toArray()[0];
         }
 
         Implementation winner = null;
         try {
             int maxMatch = -1;
             for (Implementation impl : candidates) {
                 int match = 0;
                 for (Filter filter : preferences) {
                     if (!filter.match((AttributesImpl) impl.getProperties()))
                         break;
                     match++;
                 }
                 if (match > maxMatch) {
                     maxMatch = match;
                     winner = impl;
                 }
             }
             System.out.println("   Selected : " + winner);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return winner;
     }
 
     @Override
     public ASMImpl resolveSpecByInterface(CompositeType compoType, String interfaceName, String[] interfaces,
             Set<Filter> constraints, List<Filter> preferences) {
         try {
             if ((interfaceName != null) || (interfaces != null)) {
                 for (Implementation impl : CST.SAMImplBroker.getImplementations()) {
                     // if it is an Apam impl, it has already been checked by ApamMan
                     if (CST.ASMImplBroker.getImpl(impl) != null)
                         continue;
                     Specification samSpec = impl.getSpecification();
                     if (samSpecMatchInterface(samSpec, interfaceName, interfaces)) {
                         String apamSpecName = (String) impl
                                     .getProperty(CST.PROPERTY_COMPOSITE_MAIN_SPECIFICATION);
                         // activate the implementation in APAM.
                         // This will take care of the case of composites
                         return CST.ASMImplBroker.addImpl(compoType, impl.getName(),
                                     apamSpecName, null);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
    @Override
    public void notifySelection(ASMInst client, String resName, String depName, ASMImpl impl, ASMInst inst,
            Set<ASMInst> insts) {
        // samMan does not care
    }

 }
