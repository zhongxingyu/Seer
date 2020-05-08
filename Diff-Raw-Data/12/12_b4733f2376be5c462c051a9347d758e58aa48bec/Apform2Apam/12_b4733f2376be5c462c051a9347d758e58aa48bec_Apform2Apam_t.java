 package fr.imag.adele.apam.apform;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.ApamManagers;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.apamImpl.CompositeImpl;
 import fr.imag.adele.apam.apamImpl.CompositeTypeImpl;
 import fr.imag.adele.apam.apamImpl.ImplementationBrokerImpl;
 import fr.imag.adele.apam.apamImpl.InstanceBrokerImpl;
 import fr.imag.adele.apam.apamImpl.SpecificationBrokerImpl;
 import fr.imag.adele.apam.apamImpl.SpecificationImpl;
 
 public class Apform2Apam {
     //    static Set<String>  expectedDeployedImpls = new HashSet<String>();
 	
     static final CompositeType    rootType      = CompositeTypeImpl.getRootCompositeType();
     static final Composite        rootInst      = CompositeImpl.getRootAllComposites();
     static Set<Implementation>    unusedImplems = CompositeTypeImpl.getRootCompositeType().getImpls();
     private static Logger logger = LoggerFactory.getLogger(Apform2Apam.class);
     /**
      * The event executor. We use a pool of a threads to handle notification to APAM of underlying platform
      * events, without blocking the platform thread.
      */
     static private final Executor executor      = Executors.newCachedThreadPool();
 
     /**
      * The base class of all the event processors. This handle exception and context management
      * 
      * @author vega
      * 
      */
     private static abstract class ApformEventProcessing implements Runnable {
 
         @Override
         public void run() {
             try {
                 process();
             } catch (Exception unhandledException) {
                 logger.error("Error handling Apform event :");
                 unhandledException.printStackTrace(System.err);
             } finally {
             }
 
         }
 
         /**
          * The processing method
          */
         protected abstract void process();
 
     }
 
     /**
      * Wait for a future implementation to be deployed
      */
     public static void waitForDeployedImplementation(String implementationName) {
 
         synchronized (Apform2Apam.expectedImpls) {
             Apform2Apam.expectedImpls.add(implementationName);
             try {
                 while (Apform2Apam.expectedImpls.contains(implementationName))
                     Apform2Apam.expectedImpls.wait();
             } catch (InterruptedException interrupted) {
                 interrupted.printStackTrace();
             }
             return;
         }
     }
 
     /**
      * Wait for a future specification to be deployed
      */
     public static void waitForDeployedSpecification(String specificationName) {
 
         synchronized (Apform2Apam.expectedSpecs) {
             Apform2Apam.expectedSpecs.add(specificationName);
             try {
                 while (Apform2Apam.expectedSpecs.contains(specificationName))
                     Apform2Apam.expectedSpecs.wait();
             } catch (InterruptedException interrupted) {
                 interrupted.printStackTrace();
             }
             return;
         }
     }
 
 
     private static Set<String> expectedImpls = new HashSet<String>();
     private static Set<String> expectedSpecs = new HashSet<String>();
 
     /**
      * Task to handle instance appearance
      * 
      * @author vega
      * 
      */
     private static class InstanceAppearenceProcessing extends ApformEventProcessing {
 
         private final ApformInstance instance;
 
         public InstanceAppearenceProcessing(ApformInstance instance) {
             this.instance = instance;
         }
 
         @Override
         public void process() {
 
             String implementationName = instance.getDeclaration().getImplementation().getName();
             if (CST.ImplBroker.getImpl(implementationName) == null)
                 Apform2Apam.waitForDeployedImplementation(implementationName);
 
             Instance inst = CST.InstBroker.addInst(Apform2Apam.rootInst, instance, instance.getDeclaration().getProperties());
 
             /*
              * Notify dynamic manager of instance appearance
              */
             ApamManagers.notifyExternal(inst);
         }
 
     }
 
     /**
      * Task to handle implementation deployment
      * 
      * @author vega
      * 
      */
     private static class ImplementationDeploymentProcessing extends ApformEventProcessing {
 
         private final String               implementationName;
         private final ApformImplementation implementation;
 
         public ImplementationDeploymentProcessing(String implementationName, ApformImplementation implementation) {
             this.implementationName = implementationName;
             this.implementation = implementation;
         }
 
         @Override
         public void process() {
 
             Implementation impl = Apform.getUnusedImplem(implementationName);
             if (impl != null) {
                 logger.error("Implementation already existing: " + implementationName);
                 return;
             }
 
             impl = ((ImplementationBrokerImpl) CST.ImplBroker).addImpl(Apform2Apam.rootType, implementation,
                     implementation.getDeclaration().getProperties());
 
             // wake up any threads waiting for this implementation to be deployed
             synchronized (Apform2Apam.expectedImpls) {
                 if (Apform2Apam.expectedImpls.contains(implementationName)) { // it is expected
                     Apform2Apam.expectedImpls.remove(implementationName);
                     Apform2Apam.expectedImpls.notifyAll(); // wake up the thread waiting in waitForDeployedImplementation
                 }
             }
 
             /*
              * Notify dynamic manager of implementation deployment
              * 
              * TODO How to know in which composite type the implementation was deployed
              */
             //            ApamManagers.notifyDeployed(impl.getInCompositeType().iterator().next(), impl);
             //            ApamManagers.notifyR(impl.getInCompositeType().iterator().next(), impl);
 
         }
 
     }
 
     /**
      * Task to handle specification deployment
      * 
      * @author vega
      * 
      */
     private static class SpecificationDeploymentProcessing extends ApformEventProcessing {
 
         private final String              specificationName;
         private final ApformSpecification specification;
 
         public SpecificationDeploymentProcessing(String specificationName, ApformSpecification specification) {
             this.specificationName = specificationName;
             this.specification = specification;
         }
 
         @Override
         public void process() {
 
             Specification spec = CST.SpecBroker.getSpec(specificationName);
             if (spec != null) {
                 logger.error("Specification already existing: merging with " + specificationName);
                 ((SpecificationImpl) spec).setSpecApform(specification);
                 return;
             }
 
             spec = CST.SpecBroker.addSpec(specificationName, specification, specification.getDeclaration().getProperties());
 
             // wake up any threads waiting for this specification to be deployed
             synchronized (Apform2Apam.expectedSpecs) {
                 if (Apform2Apam.expectedSpecs.contains(specificationName)) { // it is expected
                     Apform2Apam.expectedSpecs.remove(specificationName);
                     Apform2Apam.expectedSpecs.notifyAll(); // wake up the thread waiting in waitForDeployedSpecification
                 }
             }          
         }
     }
 
     /**
      * A new instance, represented by object "client" just appeared in the platform.
      */
     public static void newInstance(String instanceName, ApformInstance client) {
         Apform2Apam.executor.execute(new InstanceAppearenceProcessing(client));
     }
 
     /**
      * A new implementation, represented by object "client" just appeared in the platform.
      * 
      * @param implemName : the symbolic name.
      * @param client
      */
     public static void newImplementation(String implemName, ApformImplementation client) {
         Apform2Apam.executor.execute(new ImplementationDeploymentProcessing(implemName, client));
     }
 
     /**
      * A new specification, represented by object "client" just appeared in the platform.
      * 
      * @param specName
      * @param client
      */
     public static void newSpecification(String specName, ApformSpecification client) {
         Apform2Apam.executor.execute(new SpecificationDeploymentProcessing(specName, client));
     }
 
     /**
      * The instance called "instance name" just disappeared from the platform.
      * 
      * @param instanceName
      */
     public static void vanishInstance(String instanceName) {
         Instance inst = CST.InstBroker.getInst(instanceName);
         if (inst == null) {
          // previous remove of the factory removed instances
        	logger.warn("Unable to remove instance '{}' : Innexistance instance", instanceName);
             return;
         }
         ((InstanceBrokerImpl)CST.InstBroker).removeInst(inst);
         
     }
 
     /**
      * * The implementation called "implementation name" just disappeared from the platform.
      * 
      * @param implementationName
      */
     public static void vanishImplementation(String implementationName) {
         Implementation impl = CST.ImplBroker.getImpl(implementationName);
         if (impl == null) {
             logger.error("Vanish implementation does not exists: " + implementationName);
             return;
         }
 
         ((ImplementationBrokerImpl)CST.ImplBroker).removeImpl(impl);
     }
 
     /**
      * 
      * @param specificationName
      */
     public static void vanishSpecification(String specificationName) {
         Specification spec = CST.SpecBroker.getSpec(specificationName);
         if (spec == null) {
             System.err.println("Vanish specification does not exists: " + specificationName);
             return;
         }
     	
         ((SpecificationBrokerImpl)CST.SpecBroker).removeSpec(spec);
     }
 
 }
