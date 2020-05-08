 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.test.testcases;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.InvalidSyntaxException;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Link;
 import fr.imag.adele.apam.declarations.AtomicImplementationDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationDeclaration;
 import fr.imag.adele.apam.pax.test.iface.S2;
 import fr.imag.adele.apam.pax.test.iface.device.Eletronic;
 import fr.imag.adele.apam.pax.test.impl.deviceSwitch.GenericSwitch;
 import fr.imag.adele.apam.pax.test.impl.deviceSwitch.HouseMeterSwitch;
 import fr.imag.adele.apam.pax.test.implS1.S1Impl;
 import fr.imag.adele.apam.pax.test.implS1.S1ImplCallbackAddedValidMethodsOnlyInvalidArguments;
 import fr.imag.adele.apam.pax.test.implS1.S1ImplCallbackRemovedValidMethodsOnlyInvalidArguments;
 import fr.imag.adele.apam.pax.test.implS1.ServiceDependencySource_tct018;
 import fr.imag.adele.apam.tests.helpers.Constants;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(JUnit4TestRunner.class)
 public class InjectionInstantiationTest extends ExtensionAbstract {
 
     /**
      * @TODO Change this code to test in case of
      *       fr.imag.adele.apam.core.CompositeDeclaration
      */
     @Test
     public void AtomicInstanceCreationWithoutInjection_tc012() {
 
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 	// save the initial number of instances present in APAM
 	int counterInstanceBefore = CST.componentBroker.getInsts().size();
 
 	Instance inst = s1Impl.createInstance(null, null);
 
 	ImplementationDeclaration initialImplDecl = inst.getImpl()
 		.getImplDeclaration();
 
 	boolean found = false;
 
 	// save the number of instances present in APAM after the creation of
 	// our own instance
 	int counterInstanceAfter = CST.componentBroker.getInsts().size();
 
 	for (Instance i : CST.componentBroker.getInsts()) {
 
 	    ImplementationDeclaration apamImplDecl = i.getImpl()
 		    .getImplDeclaration();
 
 	    if (apamImplDecl instanceof AtomicImplementationDeclaration
 		    && initialImplDecl instanceof AtomicImplementationDeclaration) {
 		AtomicImplementationDeclaration atomicInitialInstance = (AtomicImplementationDeclaration) apamImplDecl;
 		AtomicImplementationDeclaration atomicApamInstance = (AtomicImplementationDeclaration) initialImplDecl;
 
 		if (atomicInitialInstance.getClassName().equals(
 			atomicApamInstance.getClassName())) {
 		    found = true;
 		    break;
 		}
 	    }
 	}
 
 	// Checks if a new instance was added into APAM
 	Assert.assertTrue((counterInstanceBefore + 1) == counterInstanceAfter);
 	// Check if its a correct type
 	Assert.assertTrue(found);
 
     }
 
     @Test
     public void InjectionUpdateLinkForSetType_tc013() {
 
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl-tc013");
 
 	Implementation sansungImpl = CST.apamResolver.findImplByName(null,
 			"SamsungSwitch");
 
 	//This instance is created to avoid apam to instantiate automatically (done in case it doesnt find any instance available)
 	Instance sansungInstInitial = sansungImpl.createInstance(null, null);
 	
 	Instance s1Inst = s1Impl.createInstance(null, null);
 
 
 	
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 	int initialSize = s1.getEletronicInstancesInSet().size();
 
 	auxDisconectWires(s1Inst);
 
 	Instance sansungInst = sansungImpl.createInstance(null, null);
 
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	GenericSwitch samsungSwitch = (GenericSwitch) sansungInst
 		.getServiceObject();
 
 	int finalSize = s1.getEletronicInstancesInSet().size();
 
 	auxListInstances("instances---");
 
 	// Make sure that one instance was added
 	String messageTemplate = "We use as dependency a multiple field(Set type) to receive all instances available of the type %s, after create a new instance this Set should receive the new instance";
 
 	String message = String.format(messageTemplate,
 		Eletronic.class.getCanonicalName());
 
 	Assert.assertTrue(message, (finalSize - initialSize) == 1);
 
     }
 
     /**
      * @TODO Test only if the injection of the instances are working in the
      *       native array type
      */
     @Test
     public void InjectionUpdateLinkForArrayType_tc014() {
 
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl-tc014");
 
 	Instance s1Inst = s1Impl.createInstance(null, null);
 
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 	int initialSize = s1.getEletronicInstancesInArray().length;
 
 	for (Link wire : s1Inst.getRawLinks()) {
 		wire.remove();
 	}
 
 	Implementation sansungImpl = CST.apamResolver.findImplByName(null,
 		"SamsungSwitch");
 
 	Instance sansungInst = sansungImpl.createInstance(null, null);
 
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	GenericSwitch samsungSwitch = (GenericSwitch) sansungInst
 		.getServiceObject();
 
 	int finalSize = s1.getEletronicInstancesInArray().length;
 
 	String messageTemplate = "We use as dependency a multiple field(Set type) to receive all instances available of the type %s, after create a new instance this Set should receive the new instance";
 
 	String message = String.format(messageTemplate,
 		Eletronic.class.getCanonicalName());
 
 	// Make sure that one instance was added
 	Assert.assertTrue(message, (finalSize - initialSize) == 1);
 
     }
 
     @Test
     public void SingletonNotSharedInstance_tc015() {
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"HouseMeterSingletonNotShared");
 
 	Instance inst1 = impl.createInstance(null,
 		new HashMap<String, String>());
 	Instance inst2 = null;
 
 	try {
 	    inst2 = impl.createInstance(null, new HashMap<String, String>());
 	} catch (Exception e) {
 	    // Nothing to do
 	}
 
 	System.out.println("-----" + inst1);
 	System.out.println("-----" + inst2);
 
 	Assert.assertTrue(
 		"In case of a singleton not shared instance, after calling createInstance an exception should be raised",
 		inst1 != null);
 	Assert.assertTrue(
 		"In case of a singleton not shared instance, after calling createInstance an exception should be raised",
 		inst2 == null);
     }
 
     @Test
     public void SingletonSharedInstance_tc013() {
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"HouseMeterSingletonShared");
 
 	CompositeType root = (CompositeType) impl.getInCompositeType()
 		.toArray()[0];
 
 	System.out.println("IMPL:" + impl);
 
 	Composite rootComposite = null;
 
 	if (root.getInst() instanceof Composite) {
 	    rootComposite = (Composite) root.getInst();
 	}
 
 	impl.createInstance(null, null);
 
 	Instance inst1 = CST.apamResolver.resolveImpl(rootComposite, impl,
 		new HashSet<String>(), new ArrayList<String>());
 	Instance inst2 = CST.apamResolver.resolveImpl(rootComposite, impl,
 		new HashSet<String>(), new ArrayList<String>());
 	Instance inst3 = CST.apamResolver.resolveImpl(rootComposite, impl,
 		new HashSet<String>(), new ArrayList<String>());
 
 	final String message = "In case of a singleton and shared instance, all instances should be the same";
 
 	Assert.assertTrue(message, inst1 == inst2);
 	Assert.assertTrue(message, inst2 == inst3);
 
     }
 
     @Test
     public void NotSingletonNotSharedInstance_tc017() {
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"HouseMeterNotSingletonNotShared");
 
 	Instance inst1 = impl.createInstance(null, null);
 	Instance inst2 = impl.createInstance(null, null);
 	Instance inst3 = impl.createInstance(null, null);
 
 	System.out.println("1 ----- " + inst1 + "/" + inst1.getServiceObject());
 	System.out.println("2 ----- " + inst2 + "/" + inst2.getServiceObject());
 	System.out.println("3 ----- " + inst3 + "/" + inst3.getServiceObject());
 
 	HouseMeterSwitch houseMeter1 = (HouseMeterSwitch) inst1
 		.getServiceObject();
 	HouseMeterSwitch houseMeter2 = (HouseMeterSwitch) inst2
 		.getServiceObject();
 	HouseMeterSwitch houseMeter3 = (HouseMeterSwitch) inst3
 		.getServiceObject();
 
 	Assert.assertTrue(
 		"In case of a not singleton and not shared instance, all instances should be different",
 		houseMeter1 != houseMeter2);
 	Assert.assertTrue(
 		"In case of a not singleton and not shared instance, all instances should be different",
 		houseMeter2 != houseMeter3);
 	Assert.assertTrue(
 		"In case of a not singleton and not shared instance, all instances should be different",
 		houseMeter1 != houseMeter3);
 
     }
 
     @Test
     public void NotSingletonSharedInstance_tc018() {
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"HouseMeterNotSingletonShared");
 
 	CompositeType root = (CompositeType) impl.getInCompositeType()
 		.toArray()[0];
 
 	Composite rootComposite = null;
 
 	if (root.getInst() instanceof Composite) {
 	    rootComposite = (Composite) root.getInst();
 	}
 
 	impl.createInstance(null, null);
 
 	boolean success = true;
 	try {
 	    impl.createInstance(null, null);
 	    impl.createInstance(null, null);
 	} catch (Exception e) {
 	    success = false;
 	}
 
 	Assert.assertTrue(
 		"In case of a not singleton and shared instance, several instances of the same implementation can be created",
 		success);
 
 	Instance inst1 = CST.apamResolver.resolveImpl(rootComposite, impl,
 		new HashSet<String>(), new ArrayList<String>());
 	Instance inst2 = CST.apamResolver.resolveImpl(rootComposite, impl,
 		new HashSet<String>(), new ArrayList<String>());
 	Instance inst3 = CST.apamResolver.resolveImpl(rootComposite, impl,
 		new HashSet<String>(), new ArrayList<String>());
 
 	final String message = "In case of a not singleton and shared instance, a new instance is never created, always recycled";
 
 	Assert.assertTrue(message, inst1 == inst2);
 	Assert.assertTrue(message, inst2 == inst3);
 
     }
 
     @Test
     public void NotInstantiableInstance_tc019() {
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"HouseMeterNotInstantiable");
 
 	boolean failed = false;
 
 	Instance inst1 = null;
 
 	try {
 	    inst1 = impl.createInstance(null, null);
 	} catch (Exception e) {
 	    // nothing to do
 	    failed = true;
 	}
 
 	Assert.assertTrue(
 		"Not Instantiable instance shall not be instantiated by API or any other means",
 		failed || inst1 == null);
 
     }
 
     @Test
     public void InstantiableInstance_tc020() {
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"HouseMeterInstantiable");
 
 	boolean failed = false;
 
 	try {
 	    Instance inst1 = impl.createInstance(null, null);
 	} catch (Exception e) {
 	    // nothing to do
 	    failed = true;
 	}
 
 	Assert.assertFalse(
 		"Instantiable instance shall be instantiated by API or any other means",
 		failed);
 
     }
 
     @Test
     public void CallbackInit_tc021() {
 
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 	Instance s1Instance = s1Impl.createInstance(null,
 		new HashMap<String, String>());
 
 	S1Impl s1 = (S1Impl) s1Instance.getServiceObject();
 
 	Assert.assertTrue(
 		"The init method declared in <callback> tag should have been called during the bundle start",
 		s1.getIsOnInitCallbackCalled());
 
     }
 
     @Test
     public void CallbackRemove_tc022() throws BundleException {
 
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 	Instance s1Instance = s1Impl.createInstance(null,
 		new HashMap<String, String>());
 
 	S1Impl s1 = (S1Impl) s1Instance.getServiceObject();
 
 	Assert.assertFalse(
 		"The remove method declared in <callback> tag should NOT have been called during the bundle start",
 		s1.getIsOnRemoveCallbackCalled());
 
 	s1.getContext().getBundle().stop();
 
 	Assert.assertTrue(
 		"The remove method declared in <callback> tag should have been called during the bundle stop",
 		s1.getIsOnRemoveCallbackCalled());
 
     }
 
     @Test
     public void PreferenceInjectionAttributeSingleImplementationMultipleInstance_tc024()
 	    throws InvalidSyntaxException {
 
 	Implementation lgImpl = CST.apamResolver.findImplByName(null,
 		"LgSwitch");
 	final Instance lgInst = lgImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "100");
 		    }
 		});
 
 	Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 		"SamsungSwitch");
 	final Instance samsungInst = samsungImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "500");
 		    }
 		});
 
 	final Instance samsungInst2 = samsungImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "500");
 		    }
 		});
 
 	Implementation siemensImpl = CST.apamResolver.findImplByName(null,
 		"SiemensSwitch");
 	final Instance siemensInst = siemensImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "105");
 		    }
 		});
 
 	System.out.println("Instances before injection request");
 	auxListInstances("\t");
 
 	// Creates S1 instance (class that requires the injection)
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 	Instance s1Inst = s1Impl.createInstance(null, null);
 
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 	Eletronic samsungSwitch = (Eletronic) samsungInst.getServiceObject();
 	Eletronic samsungSwitch2 = (Eletronic) samsungInst2.getServiceObject();
 	Eletronic lgSwitch = (Eletronic) lgInst.getServiceObject();
 	Eletronic siemensSwitch = (Eletronic) siemensInst.getServiceObject();
 
 	System.out.println("Instances after injection request");
 	auxListInstances("\t");
 
 	Instance injectedInstance = CST.componentBroker.getInstService(s1
 		.getDevicePreference110v());
 	Assert.assertTrue(
 		String.format(
 			"The instance injected should be the prefered one (currentVoltage=500), \nsince there exist an instance in which the preference is valid. \nThe instance %s (currentVoltage:%s) was injected \ninstead of %s (currentVoltage:%s)",
 			injectedInstance.getName(), injectedInstance
 				.getAllProperties().get("currentVoltage"),
 			samsungInst.getName(), samsungInst.getAllProperties()
 				.get("currentVoltage")),
 		s1.getDevicePreference110v() == samsungSwitch
 			|| s1.getDevicePreference110v() == samsungSwitch2);
 
     }
 
     @Test
     public void PreferenceInjectionAttributeMultipleImplementationSingleInstance_tc025()
 	    throws InvalidSyntaxException {
 
 	Implementation lgImpl = CST.apamResolver.findImplByName(null,
 		"LgSwitch");
 	final Instance lgInst = lgImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "100");
 		    }
 		});
 
 	Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 		"SamsungSwitch");
 	final Instance samsungInst = samsungImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "500");
 		    }
 		});
 
 	Implementation philipsImpl = CST.apamResolver.findImplByName(null,
 		"philipsSwitch");
 
 	final Instance philipsInst = philipsImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "500");
 		    }
 		});
 
 	Implementation siemensImpl = CST.apamResolver.findImplByName(null,
 		"SiemensSwitch");
 	final Instance siemensInst = siemensImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "105");
 		    }
 		});
 
 	System.out.println("Instances before injection request");
 	auxListInstances("\t");
 
 	// Creates S1 instance (class that requires the injection)
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 	Instance s1Inst = s1Impl.createInstance(null, null);
 
 	// apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 	Eletronic samsungSwitch = (Eletronic) samsungInst.getServiceObject();
 	Eletronic philipsSwitch = (Eletronic) philipsInst.getServiceObject();
 	Eletronic lgSwitch = (Eletronic) lgInst.getServiceObject();
 	Eletronic siemensSwitch = (Eletronic) siemensInst.getServiceObject();
 
 	System.out.println("Instances after injection request");
 	auxListInstances("\t");
 
 	Instance injectedInstance = CST.componentBroker.getInstService(s1
 		.getDevicePreference110v());
 
 	System.out.println("Injected:" + injectedInstance);
 
 	Assert.assertTrue(
 		String.format(
 			"The instance injected should be the prefered one (currentVoltage=500), since there exist an instance in which the preference is valid. The instance %s (currentVoltage:%s) was injected instead of %s (currentVoltage:%s)",
 			injectedInstance.getName(), injectedInstance
 				.getAllProperties().get("currentVoltage"),
 			samsungInst.getName(), samsungInst.getAllProperties()
 				.get("currentVoltage")),
 		s1.getDevicePreference110v() == samsungSwitch
 			|| s1.getDevicePreference110v() == philipsSwitch);
 
     }
 
     @Test
     public void PreferenceResolutionAfterInjection_tct006()
 	    throws InvalidSyntaxException {
 
 	Implementation lgImpl = CST.apamResolver.findImplByName(null,
 		"LgSwitch");
 	final Instance lgInst = lgImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "100");
 		    }
 		});
 
 	System.out.println("Instances before injection request");
 	auxListInstances("\t");
 
 	// Creates S1 instance (class that requires the injection)
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 	Instance s1Inst = s1Impl.createInstance(null, null);
 
 	// apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 	Eletronic lgSwitch = (Eletronic) lgInst.getServiceObject();
 
 	System.out
 		.println("Instances after FIRST injection request (preference cannot be resolved)");
 	auxListInstances("\t");
 
 	Instance injectedInstance = CST.componentBroker.getInstService(s1
 		.getDevicePreference110v());
 
 	System.out.println("Injected:" + injectedInstance);
 
 	Assert.assertTrue(
 		String.format(
 			"The instance injected cannot be the prefered one (currentVoltage=500), since there exist no instance in which the preference is valid."
 				+ " The instance %s (currentVoltage:%s) was injected instead of %s (currentVoltage:%s)",
 			injectedInstance.getName(), injectedInstance
 				.getAllProperties().get("currentVoltage"),
 			lgInst.getName(),
 			lgInst.getAllProperties().get("currentVoltage")), s1
 			.getDevicePreference110v() == lgSwitch);
 
 	Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 		"SamsungSwitch");
 	final Instance samsungInst = samsungImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "500");
 		    }
 		});
 	Eletronic samsungSwitch = (Eletronic) samsungInst.getServiceObject();
 	Instance injectedInstance2 = CST.componentBroker.getInstService(s1
 		.getDevicePreference110v());
 
 	System.out.println("Injected (new resolution) :" + injectedInstance);
 	Assert.assertTrue(
 		String.format(
 			"The instance injected should be the prefered one (currentVoltage=500), since there exist an instance in which the preference is valid. The instance %s (currentVoltage:%s) was injected instead of %s (currentVoltage:%s)",
 			injectedInstance.getName(), injectedInstance
 				.getAllProperties().get("currentVoltage"),
 			samsungInst.getName(), samsungInst.getAllProperties()
 				.get("currentVoltage")), s1
 			.getDevicePreference110v() == samsungSwitch);
 
     }
 
     @Test
     public void ConstraintInjectionWhenEmptyPreferenceTagExistsAttribute_tc026()
 	    throws InvalidSyntaxException {
 
 	Implementation lgImpl = CST.apamResolver.findImplByName(null,
 		"LgSwitch");
 	final Instance lgInst = lgImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "100");
 		    }
 		});
 
 	Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 		"SamsungSwitch");
 	final Instance samsungInst = samsungImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "500");
 		    }
 		});
 
 	Implementation siemensImpl = CST.apamResolver.findImplByName(null,
 		"SiemensSwitch");
 	final Instance siemensInst = siemensImpl.createInstance(null,
 		new HashMap<String, String>() {
 		    {
 			put("currentVoltage", "105");
 		    }
 		});
 
 	System.out.println("Instances before injection request");
 	auxListInstances("\t");
 	// Creates S1 instance (class that requires the injection)
 	Implementation s1Impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 	Instance s1Inst = s1Impl.createInstance(null, null);
 
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 	Eletronic samsungSwitch = (Eletronic) samsungInst.getServiceObject();
 	Eletronic lgSwitch = (Eletronic) lgInst.getServiceObject();
 	Eletronic siemensSwitch = (Eletronic) siemensInst.getServiceObject();
 
 	System.out.println("Instances after injection request");
 	auxListInstances("\t");
 
 	Instance injectedInstance = CST.componentBroker.getInstService(s1
 		.getDeviceConstraint110v());
 
 	Assert.assertTrue(
 		String.format(
 			"The instance injected should obey the contraints (currentVoltage=500) given in the xml, this does not happens when there is a <preference> tag with nothing declared inside. The instance %s (currentVoltage:%s) was injected instead of %s (currentVoltage:%s)",
 			injectedInstance.getName(), injectedInstance
 				.getAllProperties().get("currentVoltage"),
 			samsungInst.getName(), samsungInst.getAllProperties()
 				.get("currentVoltage")), s1
 			.getDeviceConstraint110v() == samsungSwitch);
 
     }
 
     @Test
     public void FindImplByName_tc027() {
 
 	auxListInstances("before-");
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"fr.imag.adele.apam.pax.test.impl.S1Impl");
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 	auxListInstances("after-");
 
 	Assert.assertTrue(
 		"Should be possible to find an implementation by using its name.",
 		impl != null);
     }
 
     @Test
     public void AddedRemovedCallbackInDependencyDeclaration_tc023() {
 
 	String message = "Into an <implementation>, when declaring a dependency, we may specify methods (with an Instance type parameter) to be called as soon as the dependency is wired or unwired, those are 'added' and 'removed' attributes respectively. %s";
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"S1Impl-added-removed-callback-signature-instance");
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	Instance instance = impl.createInstance(null,
 		new HashMap<String, String>());
 
 	S1Impl s1 = (S1Impl) instance.getServiceObject();
 
 	Assert.assertTrue(
 		String.format(
 			message,
 			"Although 'added' method should not be called before the resolution of the dependency"),
 		s1.getIsOnInitCallbackCalled() == false);
 	Assert.assertTrue(
 		String.format(
 			message,
 			"Although 'remove' method should not be called before the resolution of the dependency"),
 		s1.getIsOnRemoveCallbackCalled() == false);
 
 	s1.getS2();
 
 	Assert.assertTrue(
 		String.format(
 			message,
 			"Although 'added' method was not called during the wiring process(dependency resolution)"),
 		s1.getIsOnInitCallbackCalled() == true);
 
 	Assert.assertTrue(
 		String.format(
 			message,
 			"the 'added' the callback method was called although the instance was not received as parameter"),
 		s1.getIsBindUnbindReceivedInstanceParameter() == true);
 
 	auxDisconectWires(instance);
 
 	Assert.assertTrue(
 		String.format(message,
 			"Although 'remove' method was not called during the unwiring process"),
 		s1.getIsOnRemoveCallbackCalled() == true);
 
 	Assert.assertTrue(
 		String.format(
 			message,
 			"the 'remove' the callback method was called although the instance was not received as parameter"),
 		s1.getIsBindUnbindReceivedInstanceParameter() == true);
 
     }
 
     @Test
     public void removedCallbackWhenThereAreNOConstraints_tc111() {
 
 	String messageTemplate = "In case of a remove callback, when a property of a service A change and the service B do NOT use a constraint to inject A on its instance, the link from A->B should NOT be brocken and restablished, thus remove callback should NOT have been called";
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"S1Impl-removed-callback-with-no-constraint");
 
 	Instance instance = impl.createInstance(null,
 		new HashMap<String, String>());
 
 	S1Impl s1 = (S1Impl) instance.getServiceObject();
 
 	s1.getS2();
 
 	// The line s1.getS2() creates the link and sets the
 	// UnbindReceivedInstanceParameter to true, so we roll it back to false
 
 	s1.setIsBindUnbindReceivedInstanceParameter(false);
 
 	// after change the variable we expect apam NOT to call the remove
 	// method
 
 	instance.getLinkDest("s2").setProperty("defined-property", "ups");
 
 	Assert.assertTrue(messageTemplate,
 		s1.getIsBindUnbindReceivedInstanceParameter() == false);
 
     }
 
     @Test
     public void removedCallbackWhenThereAreConstraints_tc112() {
 
 	String messageTemplate = "In case of a remove callback, when a property of a service A change and the service B use a constraint to inject A on its instance, the link from A->B should be brocken and restablished, thus remove callback should have been called";
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"S1Impl-removed-callback-with-constraint");
 
 	Instance instance = impl.createInstance(null,
 		new HashMap<String, String>());
 
 	S1Impl s1 = (S1Impl) instance.getServiceObject();
 
 	S2 first = s1.getS2();
 
 	// The line s1.getS2() creates the link and sets the
 	// UnbindReceivedInstanceParameter to true, so we roll it back to false
 
 	s1.setIsBindUnbindReceivedInstanceParameter(false);
 
 	// after change the variable we expect apam to call the remove method
 
 	instance.getLinkDest("s2").setProperty("defined-property", "invalid");
 
 	S2 second = s1.getS2();
 
 	Assert.assertTrue(messageTemplate,
 		s1.getIsBindUnbindReceivedInstanceParameter() == true);
 
 	Assert.assertNotSame("Must be a new instance", first, second);
 
     }
 
     @Test
     public void AddedRemovedCallbackInDependencyDeclarationEmptySignature_tc085() {
 
 	String message = "Into an <implementation>, when declaring a dependency, we may specify methods (without parameters) to be called as soon as the dependency is wired or unwired, those are 'added' and 'removed' attributes respectively. %s";
 
 	Implementation impl = CST.apamResolver.findImplByName(null,
 		"S1Impl-added-removed-callback-signature-empty");
 	apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 	Instance instance = impl.createInstance(null,
 		new HashMap<String, String>());
 
 	S1Impl s1 = (S1Impl) instance.getServiceObject();
 
 	Assert.assertTrue(
 		String.format(
 			message,
 			"Although 'added' method should not be called before the resolution of the dependency"),
 		s1.getIsOnInitCallbackCalled() == false);
 	Assert.assertTrue(
 		String.format(
 			message,
 			"Although 'remove' method should not be called before the resolution of the dependency"),
 		s1.getIsOnRemoveCallbackCalled() == false);
 
 	s1.getS2();
 
 	Assert.assertTrue(
 		String.format(
 			message,
 			"Although 'added' method was not called during the wiring process(dependency resolution)"),
 		s1.getIsOnInitCallbackCalled() == true);
 
 	auxDisconectWires(instance);
 
 	Assert.assertTrue(
 		String.format(message,
 			"Although 'remove' method was not called during the unwiring process"),
 		s1.getIsOnRemoveCallbackCalled() == true);
 
     }
 
 }
