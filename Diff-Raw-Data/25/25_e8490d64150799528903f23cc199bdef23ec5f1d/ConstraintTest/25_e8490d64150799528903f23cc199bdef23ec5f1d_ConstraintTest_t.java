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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 import org.osgi.framework.InvalidSyntaxException;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.pax.test.iface.device.Eletronic;
 import fr.imag.adele.apam.pax.test.implS1.S1Impl;
 import fr.imag.adele.apam.tests.helpers.Constants;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(JUnit4TestRunner.class)
 public class ConstraintTest extends ExtensionAbstract{
 	
 	@Test
 	public void ConstraintsCheckingImplementation_tc009() {
 
 		
 		Implementation s1Impl = CST.apamResolver.findImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 		Instance philipsSwitch = CST.componentBroker.getInstService(s1
 				.getSimpleDevice110v());
 
 		philipsSwitch.setProperty("currentVoltage", "110");
 		
		String manufacturer=philipsSwitch.getProperty("manufacturer");
 		String currentVoltage=philipsSwitch.getProperty("currentVoltage");
 		String voltage=philipsSwitch.getProperty("voltage");
 		
		String messageTemplate="The filter %s should result in a %s statement since manufacturer:%s, currentVoltage:%s and voltage:%s";
 		
 		String expression="(manufacturer=philips)";
 		Boolean result=true;
 		
		Assert.assertTrue(String.format(messageTemplate, expression,result,manufacturer,currentVoltage,voltage),philipsSwitch.match(expression));
 		
 		expression="(voltage=110)";
 		result=true;
 		
		Assert.assertTrue(String.format(messageTemplate, expression,result,manufacturer,currentVoltage,voltage),philipsSwitch.match(expression));
 		
 		expression="(currentVoltage <= 110)";
 		result=true;
 		
		Assert.assertTrue(String.format(messageTemplate, expression,result,manufacturer,currentVoltage,voltage),philipsSwitch.match(expression));
 		
 		expression="(currentVoltage >= 111)";
 		result=false;
 		
		Assert.assertFalse(String.format(messageTemplate, expression,result,manufacturer,currentVoltage,voltage),philipsSwitch.match(expression));
 		
 		expression="(futfut)";
 		result=false;
 		
		Assert.assertFalse(String.format(messageTemplate, expression,result,manufacturer,currentVoltage,voltage),philipsSwitch.match(expression));
 		
 		expression="(&(false)(manufacturer=philips))";
 		result=false;
 		
		Assert.assertFalse(String.format(messageTemplate, expression,result,manufacturer,currentVoltage,voltage),philipsSwitch.match(expression));
 		
 		expression="(&(true)(manufacturer=philips))";
 		result=true;
 		
		Assert.assertTrue(String.format(messageTemplate, expression,result,manufacturer,currentVoltage,voltage),philipsSwitch.match(expression));
 
 	}
 	
 	@Test
 	public void ConstraintsCheckingInstanceFilteringByInitialProperty_tc010()
 			throws InvalidSyntaxException {
 
 		
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "95");
 					}
 				});
 
 		Implementation lgImpl = CST.apamResolver.findImplByName(null,
 				"LgSwitch");
 		final Instance lgInst = lgImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "100");
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
 
 		Implementation boschImpl = CST.apamResolver.findImplByName(null,
 				"BoschSwitch");
 		final Instance boschInst = boschImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "110");
 					}
 				});
 
 		Implementation philipsImpl = CST.apamResolver.findImplByName(null,
 				"philipsSwitch");
 		final Instance philipsInst = philipsImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "117");
 					}
 				});
 		
 		Set<Instance> validInstances = new HashSet<Instance>() {
 			{
 				add(siemensInst);
 				add(lgInst);
 				add(boschInst);
 				add(samsungInst);
 			}
 		};
 
 		Implementation s1Impl = CST.apamResolver.findImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 //		apam.waitForIt(Constants.CONST_WAIT_TIME);
 		
 		Instance s1Inst = s1Impl.createInstance(null, null);
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 //		apam.waitForIt(Constants.CONST_WAIT_TIME);
 		
 		for (Eletronic e : s1.getEletronicInstancesConstraintsInstance()) {
 			Instance p = CST.componentBroker.getInstService(e);
 			System.out.println("---- Voltage:"
 					+ p.getProperty("currentVoltage") + " / Name:"
 					+ p.getName());
 
 			boolean found = false;
 
 			for (Instance l : validInstances)
 				if (l.getName().equals(p.getName())) {
 					found = true;
 					break;
 				}
 
 			// Check if all valid instances were injected 
 			Assert.assertTrue(String.format("Instance %s (currentVoltage:%s) was injected even if its does not obey the constraint (currentVoltage<=110)", p.getName(),p.getProperty("currentVoltage")),
 					p.match("(currentVoltage<=110)"));
 			Assert.assertTrue(String.format("Instance %s (currentVoltage:%s) was not found in the list of valid instances for the constraint (currentVoltage<=110)", p.getName(),p.getProperty("currentVoltage")),
 					found);
 
 		}
 
 		auxListInstances("--------------");
 		
 		// check if there is no other instance injected
 		Assert.assertTrue(String.format("The number of valid instances and the number of injected instances differ, instances not expected were injected. %d injected instead of %d",s1.getEletronicInstancesConstraintsInstance().size(),validInstances.size()),s1.getEletronicInstancesConstraintsInstance().size() == validInstances
 				.size());
 		
 	}
 
 	@Test
 	public void ConstraintsCheckingInstanceFilteringBySetProperty_tc011()
 			throws InvalidSyntaxException {
 
 		
 		Implementation samsungImpl = CST.apamResolver.findImplByName(null,
 				"SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null, null);
 
 		Implementation lgImpl = CST.apamResolver.findImplByName(null,
 				"LgSwitch");
 		final Instance lgInst = lgImpl.createInstance(null, null);
 
 		Implementation siemensImpl = CST.apamResolver.findImplByName(null,
 				"SiemensSwitch");
 		final Instance siemensInst = siemensImpl.createInstance(null, null);
 
 		Implementation boschImpl = CST.apamResolver.findImplByName(null,
 				"BoschSwitch");
 		final Instance boschInst = boschImpl.createInstance(null, null);
 
 		Implementation philipsImpl = CST.apamResolver.findImplByName(null,
 				"philipsSwitch");
 		final Instance philipsInst = philipsImpl.createInstance(null, null);
 		
 		samsungInst.setProperty("currentVoltage", "95");
 		lgInst.setProperty("currentVoltage", "100");
 		siemensInst.setProperty("currentVoltage", "105");
 		boschInst.setProperty("currentVoltage", "110");
 		philipsInst.setProperty("currentVoltage", "117");
 		
 		apam.waitForIt(Constants.CONST_WAIT_TIME);
 		
 		Set<Instance> validInstances = new HashSet<Instance>() {
 			{
 				add(siemensInst);
 				add(lgInst);
 				add(boschInst);
 				add(samsungInst);
 			}
 		};
 
 		Implementation s1Impl = CST.apamResolver.findImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 		
 		Instance s1Inst = s1Impl.createInstance(null, null);
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 		
 		//auxListInstanceReferencedBy("#################",s1.getEletronicInstancesConstraintsInstance());
 		auxListInstances("-------Available instances before using the list-------");	
 		
 		System.out.println("Size of the injected list:"+s1.getEletronicInstancesConstraintsInstance().size());
 		
 		for (Eletronic e : s1.getEletronicInstancesConstraintsInstance()) {
 			Instance p = CST.componentBroker.getInstService(e);
 			System.out.println("---- Voltage:"
 					+ p.getProperty("currentVoltage") + " / Name:"
 					+ p.getName());
 
 			boolean found = false;
 
 			for (Instance l : validInstances)
 				if (l.getName().equals(p.getName())) {
 					found = true;
 					break;
 				}
 			
 			// Check if all valid instances were injected 
 			Assert.assertTrue(String.format("Instance %s (currentVoltage:%s) was injected even if its does not obey the constraint (currentVoltage <= 110)", p.getName(),p.getProperty("currentVoltage")),p.match("(currentVoltage <= 110)"));
 			Assert.assertTrue(String.format("Instance %s (currentVoltage:%s) was not found in the list of valid instances for the constraint (currentVoltage <= 110)", p.getName(),p.getProperty("currentVoltage")),found);
 
 		}
 
 		auxListInstances("-------Available instances after using the list-------");
 
 		// check if there is no other instance injected
 		Assert.assertTrue(String.format("The number of valid instances and the number of injected instances differ, instances not expected were injected. %d injected instead of %d",s1.getEletronicInstancesConstraintsInstance().size(),validInstances.size()),s1.getEletronicInstancesConstraintsInstance().size() == validInstances
 				.size());
 
 	}
 	
 }
