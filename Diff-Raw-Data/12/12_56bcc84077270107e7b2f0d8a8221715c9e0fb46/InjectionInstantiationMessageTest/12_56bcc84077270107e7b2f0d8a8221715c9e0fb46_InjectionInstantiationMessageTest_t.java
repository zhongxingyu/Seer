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
 package fr.imag.adele.apam.test.messages;
 
 import static org.ops4j.pax.exam.CoreOptions.bundle;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Queue;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Configuration;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.ops4j.pax.exam.util.PathUtils;
 import org.osgi.framework.InvalidSyntaxException;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.declarations.AtomicImplementationDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationDeclaration;
 import fr.imag.adele.apam.pax.test.iface.device.Eletronic;
 import fr.imag.adele.apam.pax.test.implS1.S1Impl;
 import fr.imag.adele.apam.pax.test.msg.device.EletronicMsg;
 import fr.imag.adele.apam.pax.test.msg.devices.impl.GenericProducer;
 import fr.imag.adele.apam.pax.test.msg.m1.producer.impl.M1ProducerImpl;
 import fr.imag.adele.apam.tests.helpers.Constants;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(JUnit4TestRunner.class)
 public class InjectionInstantiationMessageTest extends ExtensionAbstract {
 
 	@Configuration
 	public Option[] apamConfig() {
 		List<Option> optionHerited = config();
 
 		optionHerited.add(0, bundle("file://" + PathUtils.getBaseDir()
 				+ "/bundle/wireadmin.jar"));
 		// optionHerited.add(0,bundle("file:/" + PathUtils.getBaseDir()
 		// +"/bundle/org.eclipse.equinox.util-1.0.400.jar"));
 		// optionHerited.add(0,bundle("file:/" + PathUtils.getBaseDir()
 		// +"/bundle/org.eclipse.equinox.wireadmin-1.0.400.jar"));
 
 		return optionHerited.toArray(new Option[0]);
 	}
 
 	/**
 	 * @TODO Change this code to test in case of
 	 *       fr.imag.adele.apam.core.CompositeDeclaration
 	 * 
 	 * 
 	 */
 	@Test
 	public void AtomicInstanceCreationWithoutInjection_mtc012() {
 
 		Implementation m1ProsumerImpl = CST.apamResolver.findImplByName(null,
 				"M1-ProsumerImpl");
 
 		// save the initial number of instances present in APAM
 		int counterInstanceBefore = CST.componentBroker.getInsts().size();
 
 		Instance inst = m1ProsumerImpl.createInstance(null, null);
 
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
 		Assert.assertEquals((counterInstanceBefore + 1), counterInstanceAfter);
 		// Check if its a correct type
 		Assert.assertTrue(found);
 
 	}
 
 	@Test
 	public void InjectionUpdateLinkForSetType_mtc013() {
 
 		Implementation m1ProsumerImpl = CST.apamResolver.findImplByName(null,
 				"M1-ProsumerImpl");
 
 		Instance s1Inst = m1ProsumerImpl.createInstance(null, null);
 
		apam.waitForIt(Constants.CONST_WAIT_TIME_LONG);
 
 		M1ProducerImpl m1ProdImpl = (M1ProducerImpl) s1Inst.getServiceObject();
 
 		int initialMsgSize = m1ProdImpl.getEletronicMsgQueue().size();
 
 		auxDisconectWires(s1Inst);
 
 		Implementation sansungImpl = CST.apamResolver.findImplByName(null,
 				"Samsung-ProducerImpl");
 
 		Instance sansungInst = (Instance) sansungImpl
 				.createInstance(null, null);
 
		apam.waitForIt(Constants.CONST_WAIT_TIME_LONG);
 
 		GenericProducer samsungProducer = (GenericProducer) sansungInst
 				.getServiceObject();
 
 		int finalSize = m1ProdImpl.getEletronicMsgQueue().size();
 
 		EletronicMsg eletronicMsg = samsungProducer
 				.produceEletronicMsg("New Message");
 
 		auxListInstances("instances---");
 
 		finalSize = m1ProdImpl.getEletronicMsgQueue().size();
 
 		// Make sure that one message was added
 		String messageTemplate = "We use as dependency for multiple message producer to receive all messages available of the type %s, after create a new instance the queue should receive the new message";
 
 		String message = String.format(messageTemplate,
 				EletronicMsg.class.getCanonicalName());
 
 		Assert.assertEquals(message, (finalSize - initialMsgSize), 1);
 
 		Assert.assertEquals(eletronicMsg, m1ProdImpl.getEletronicMsgQueue()
 				.poll());
 
 	}
 
 	@Test
 	public void PreferenceInjectionAttributeSingleImplementationMultipleInstance_mtc024()
 			throws InvalidSyntaxException {
 
 		Implementation lgImpl = CST.apamResolver.findImplByName(null,
 				"Lg-ProducerImpl");
 		final Instance lgInst = lgImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "100");
 					}
 				});
 
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"Samsung-ProducerImpl");
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
 				"Siemens-ProducerImpl");
 		final Instance siemensInst = siemensImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "105");
 					}
 				});
 
 		System.out.println("Instances before injection request");
 		auxListInstances("\t");
 
 		// Creates S1 instance (class that requires the injection)
 		Implementation m1ProsumerApamImpl = CST.apamResolver.findImplByName(
 				null, "M1-ProsumerImpl");
 
 		Instance m1ProsumerApamInst = m1ProsumerApamImpl.createInstance(null,
 				null);
 
 		apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 		M1ProducerImpl m1Prosumer = (M1ProducerImpl) m1ProsumerApamInst
 				.getServiceObject();
 
 		GenericProducer samsungProducer = (GenericProducer) samsungInst
 				.getServiceObject();
 		GenericProducer samsungProducer2 = (GenericProducer) samsungInst2
 				.getServiceObject();
 		GenericProducer lgProducer = (GenericProducer) lgInst
 				.getServiceObject();
 		GenericProducer siemensProducer = (GenericProducer) siemensInst
 				.getServiceObject();
 
 		System.out.println("Instances after injection request");
 		auxListInstances("\t");
 
 		Queue<EletronicMsg> queue = m1Prosumer.getDevicePreference110vQueue();
 
 		EletronicMsg msg1 = samsungProducer.produceEletronicMsg("message1");
 		EletronicMsg msg2 = samsungProducer2.produceEletronicMsg("message2");
 		EletronicMsg msg3 = lgProducer.produceEletronicMsg("message3");
 		EletronicMsg msg4 = siemensProducer.produceEletronicMsg("message4");
 
 		int queueSize = queue.size();
 		EletronicMsg received1 = queue.poll();
 		EletronicMsg received2 = queue.poll();
 
 		Assert.assertEquals(
 				String.format("The queue injected should receive message from the prefered producer (currentVoltage=500), \nsince there exist an instance in which the preference is valid."),
 				queueSize, 2);
 
 		Assert.assertTrue(msg1.equals(received1) || msg1.equals(received2));
 		Assert.assertTrue(msg2.equals(received1) || msg2.equals(received2));
 		Assert.assertTrue(!msg3.equals(received1) && !msg3.equals(received2));
 		Assert.assertTrue(!msg4.equals(received1) && !msg4.equals(received2));
 
 	}
 
 	// @Test
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
 
 	// @Test
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
 
 	// @Test
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
 
 	// @Test
 	public void AddedRemovedCallbackInDependencyDeclaration_tc023() {
 
 		String message = "Into an <implementation>, when declaring a dependency, we may specify methods to be called as soon as the dependency is wired or unwired, those are 'added' and 'removed' attributes respectively. %s";
 
 		Implementation impl = CST.apamResolver.findImplByName(null,
 				"S1Impl-added-removed-callback");
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
