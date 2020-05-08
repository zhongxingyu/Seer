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
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.PaxExam;
 import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
 import org.ops4j.pax.exam.spi.reactors.PerMethod;
 import org.osgi.framework.InvalidSyntaxException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.impl.APAMImpl;
 import fr.imag.adele.apam.pax.test.impl.deviceSwitch.PropertyChangeNotificationSwitch;
 import fr.imag.adele.apam.pax.test.impl.deviceSwitch.PropertyInjectionTypeSwitch;
 import fr.imag.adele.apam.pax.test.impl.deviceSwitch.PropertyTypeBooleanChangeNotificationSwitch;
 import fr.imag.adele.apam.pax.test.impl.deviceSwitch.PropertyTypeIntChangeNotificationSwitch;
 import fr.imag.adele.apam.pax.test.implS1.S1Impl;
 import fr.imag.adele.apam.pax.test.implS1.S1Impl_tct021;
 import fr.imag.adele.apam.pax.test.implS1.S1Impl_tct025;
 import fr.imag.adele.apam.tests.helpers.Constants;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 @RunWith(PaxExam.class)
 @ExamReactorStrategy(PerMethod.class)
 public class PropertyTest extends ExtensionAbstract {
 	
 	private static Logger logger = LoggerFactory.getLogger(PropertyTest.class);
 
 	@Override
 	public List<Option> config() {
 		Map<String, String> mapOfRequiredArtifacts = new HashMap<String, String>();
 		mapOfRequiredArtifacts.put("apam-pax-samples-impl-s1",
 				"fr.imag.adele.apam.tests.services");
 		mapOfRequiredArtifacts.put("apam-pax-samples-iface",
 				"fr.imag.adele.apam.tests.services");
 		mapOfRequiredArtifacts.put("specs-s1",
 				"fr.imag.adele.apam.tests.services");
 		mapOfRequiredArtifacts.put("implem-s1",
 				"fr.imag.adele.apam.tests.services");
 		mapOfRequiredArtifacts.put("instance-s1",
 		 "fr.imag.adele.apam.tests.services");
 
 		List<Option> addon = super.config(mapOfRequiredArtifacts, false);
 		return addon;
 	}
 
 	@Test
 	public void PropertiesDataTypeAndLDAPFilteringForBoolean_tc007()
 			throws InvalidSyntaxException {
 
 		Implementation samsungImpl = waitForImplByName(null, "SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "95");
 					}
 				});
 
 		apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 		auxListProperties("\t", samsungInst);
 
 		String message = "";
 
 		message = String
 				.format("Calling match method with filter = %s, should result in True since hasDisplay is %b",
 						"(hasDisplay=false)",
 						samsungInst.getProperty("hasDisplay"));
 
 		Assert.assertTrue(message, samsungInst.match("(hasDisplay=false)"));
 
 		message = String
 				.format("Calling match method with filter = %s, should result in False since hasDisplay is %b",
 						"(hasDisplay=true)",
 						samsungInst.getProperty("hasDisplay"));
 
 		Assert.assertFalse(message, samsungInst.match("(hasDisplay=true)"));
 
 	}
 
 	@Test
 	public void PropertiesDataTypeAndLDAPFilteringForIntegers_tc006()
 			throws InvalidSyntaxException {
 
 		Implementation samsungImpl = waitForImplByName(null, "SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "95");
 					}
 				});
 
 		apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 		auxListProperties("\t", samsungInst);
 
 		// int
 
 		String templateMessage = "Calling match method with filter = %s, should result in True since currentVoltage is %n";
 		String message = "";
 
 		message = String.format(templateMessage, "(currentVoltage>=95)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage>=95)"));
 
 		message = String.format(templateMessage, "(currentVoltage<=95)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage<=95)"));
 
 		message = String.format(templateMessage, "(currentVoltage<=101)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage<=101)"));
 
 		message = String.format(templateMessage, "(currentVoltage<=96)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage<=96)"));
 
 		message = String.format(templateMessage, "(currentVoltage>=94)",
 				samsungInst.getProperty("currentVoltage"));
 
 		Assert.assertTrue(message, samsungInst.match("(currentVoltage>=94)"));
 
 	}
 
 	@Test
 	public void PropertiesDataTypeAndLDAPFilteringForString_tc008()
 			throws InvalidSyntaxException {
 
 		Implementation samsungImpl = waitForImplByName(null, "SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null,
 				new HashMap<String, String>() {
 					{
 						put("currentVoltage", "95");
 					}
 				});
 
 		apam.waitForIt(Constants.CONST_WAIT_TIME);
 
 		auxListProperties("\t", samsungInst);
 
 		String templateMessage = "Calling match method with filter = %s, should result in True since impl-name is %s";
 		String message = "";
 
 		message = String.format(templateMessage, "(impl-name=Samsung*)",
 				samsungInst.getProperty("impl-name"));
 
 		Assert.assertTrue(message, samsungInst.match("(impl-name=Samsung*)"));
 
 		message = String.format(templateMessage, "(impl-name=*amsungSwitch)",
 				samsungInst.getProperty("impl-name"));
 
 		Assert.assertTrue(message,
 				samsungInst.match("(impl-name=*amsungSwitch)"));
 
 		message = String.format(templateMessage, "(impl-name=*amsung*)",
 				samsungInst.getProperty("impl-name"));
 
 		Assert.assertTrue(message, samsungInst.match("(impl-name=*amsung*)"));
 
 		templateMessage = "Calling match method with filter = %s, should result in False since impl-name is %s";
 
 		message = String.format(templateMessage, "(impl-name=SamsunG*)",
 				samsungInst.getProperty("impl-name"));
 
 		Assert.assertFalse(message, samsungInst.match("(impl-name=SamsunG*)"));
 
 	}
 
 	@Test
 	public void PropertiesDataTypeListInt_tc053() {
 
 		final String propertyName = "setInt";
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 
 		Object RawType = implementation.getPropertyObject(propertyName);
 
 		String message = "Retrieving a set of (%s) from the properties, but the return do not correspond to a type that represents a set of elements(List,Set nor array).";
 
 		Assert.assertTrue(String.format(message, "Int"),
 				RawType instanceof Integer[] || RawType instanceof Collection);
 
 		String messageCollection = "Retrieving a set of (%s) from the properties, the return correspond to a Collection type but not a Collection containing the type %s.";
 
 		if (RawType instanceof Collection) {
 			Object sample = ((Collection) RawType).iterator().next();
 			Class properType = String.class;
 			Assert.assertTrue(
 					String.format(messageCollection, propertyName, properType),
 					properType.isInstance(sample));
 		}
 
 	}
 
 	@Test
 	public void PropertiesDataTypeListInteger_tc054() {
 
 		final String propertyName = "setInteger";
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 
 		Object RawType = implementation.getPropertyObject(propertyName);
 
 		String message = "Retrieving a set of (%s) from the properties, but the return do not correspond to a type that represents a set of elements(List,Set nor array).";
 
 		Assert.assertTrue(String.format(message, "Integer"),
 				RawType instanceof Integer[] || RawType instanceof Collection);
 
 		String messageCollection = "Retrieving a set of (%s) from the properties, the return correspond to a Collection type but not a Collection containing the type %s.";
 
 		if (RawType instanceof Collection) {
 			Object sample = ((Collection) RawType).iterator().next();
 			Class properType = String.class;
 			Assert.assertTrue(
 					String.format(messageCollection, propertyName, properType),
 					properType.isInstance(sample));
 		}
 
 	}
 
 	@Test
 	public void PropertiesDataTypeListString_tc055() {
 
 		final String propertyName = "setString";
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 
 		Object RawType = implementation.getPropertyObject(propertyName);
 
 		String message = "Retrieving a set of (%s) from the properties, but the return do not correspond to a type that represents a set of elements(List,Set nor array).";
 
 		Assert.assertTrue(String.format(message, "String"),
 				RawType instanceof String[] || RawType instanceof Collection);
 
 		String messageCollection = "Retrieving a set of (%s) from the properties, the return correspond to a Collection type but not a Collection containing the type %s.";
 
 		if (RawType instanceof Collection) {
 			Object sample = ((Collection) RawType).iterator().next();
 			Class properType = String.class;
 			Assert.assertTrue(
 					String.format(messageCollection, propertyName, properType),
 					properType.isInstance(sample));
 		}
 
 	}
 
 	@Test
 	public void PropertiesInjectionEnumIntoSetDataType_tc081() {
 
 		Implementation implementation = waitForImplByName(null,
 				"PropertyInjectionSwitch");
 
 		Instance instance = implementation.createInstance(null, null);
 
 		PropertyInjectionTypeSwitch switchRef = (PropertyInjectionTypeSwitch) instance
 				.getServiceObject();
 
 		final String messageTemplate = "%s (value declared into <apam> ... <definition type='{%s}'/> ...</apam>)";
 
 		Set<String> original = new HashSet<String>() {
 			{
 				add("Linux");
 				add("Windows");
 				add("Android");
 				add("IOS");
 			}
 		};
 
 		Set<String> injected = switchRef.getOS();
 
 		// check we dont inject null
 		Assert.assertTrue(
 				String.format(
 						messageTemplate,
 						"The value injected into the java.util.Set field was null, instead of set declared in the property",
 						"string"), injected != null);
 
 		// check that all the values injected were present in the original set
 		for (String value : injected) {
 			Assert.assertTrue(
 					String.format(
 							messageTemplate,
 							"The value injected in the field do not exist in the original set of values",
 							"string"), original.contains(value));
 		}
 
 		// ensure that both, the injected and declared, do have the same size.
 		Assert.assertTrue(
 				String.format(
 						messageTemplate,
 						String.format(
 								"The size of the set declarared is %d and exactly %d were injected, those values should be equals",
 								original.size(), injected.size()), "string"),
 				original.size() == injected.size());
 	}
 
 	@Test
 	public void PropertiesInjectionIntegerIntoSetDataType_tc079() {
 
 		Implementation implementation = waitForImplByName(null,
 				"PropertyInjectionSwitch");
 
 		Instance instance = implementation.createInstance(null, null);
 
 		PropertyInjectionTypeSwitch switchRef = (PropertyInjectionTypeSwitch) instance
 				.getServiceObject();
 
 		final String messageTemplate = "%s (value declared into <apam> ... <definition type='{%s}'/> ...</apam>)";
 
 		Set<Integer> original = new HashSet<Integer>() {
 			{
 				add(12);
 				add(15);
 				add(254);
 				add(100);
 			}
 		};
 
 		Set<Integer> injected = switchRef.getSetInt();
 
 		// check we dont inject null
 		Assert.assertTrue(
 				String.format(
 						messageTemplate,
 						"The value injected into the java.util.Set field was null, instead of set declared in the property",
 						"int"), injected != null);
 
 		// check that all the values injected were present in the original set
 		for (Integer val : injected) {
 			Assert.assertTrue(
 					String.format(
 							messageTemplate,
 							"The value injected in the field do not exist in the original set of values",
 							"int"), original.contains(val));
 		}
 
 		// ensure that both, the injected and declared, do have the same size.
 		Assert.assertTrue(
 				String.format(
 						messageTemplate,
 						String.format(
 								"The size of the set declarared is %d and exactly %d were injected, those values should be equals",
 								original.size(), injected.size()), "int"),
 				original.size() == injected.size());
 	}
 
 	@Test
 	public void PropertiesInjectionStringIntoSetDataType_tc080() {
 
 		Implementation implementation = waitForImplByName(null,
 				"PropertyInjectionSwitch");
 
 		Instance instance = implementation.createInstance(null, null);
 
 		PropertyInjectionTypeSwitch switchRef = (PropertyInjectionTypeSwitch) instance
 				.getServiceObject();
 
 		final String messageTemplate = "%s (value declared into <apam> ... <definition type='{%s}'/> ...</apam>)";
 
 		Set<String> original = new HashSet<String>() {
 			{
 				add("doubt");
 				add("grows");
 				add("with");
 				add("knowledge");
 			}
 		};
 
 		Set<String> injected = switchRef.getSetString();
 
 		// check we dont inject null
 		Assert.assertTrue(
 				String.format(
 						messageTemplate,
 						"The value injected into the java.util.Set field was null, instead of set declared in the property",
 						"string"), injected != null);
 
 		// check that all the values injected were present in the original set
 		for (String value : injected) {
 			Assert.assertTrue(
 					String.format(
 							messageTemplate,
 							"The value injected in the field do not exist in the original set of values",
 							"string"), original.contains(value));
 		}
 
 		// ensure that both, the injected and declared, do have the same size.
 		Assert.assertTrue(
 				String.format(
 						messageTemplate,
 						String.format(
 								"The size of the set declarared is %d and exactly %d were injected, those values should be equals",
 								original.size(), injected.size()), "string"),
 				original.size() == injected.size());
 	}
 
 	@Test
 	public void PropertyChangeNoticationCallback_tc063() {
 
 		Implementation implementation = waitForImplByName(null,
 				"PropertyChangeNotification");
 		Instance inst = implementation.createInstance(null, null);
 
 		final String PROPERTY_NAME = "state";
 		final String VALUE_NEW = "new value";
 		final String message_fail_set = "Property being declared but after using instance.setAttribute the value inside the component did not correspont to the configured value";
 		final String message_fail_callback = "The callback for property change notifications(using 'method' attribute) was not called";
 
 		inst.setProperty(PROPERTY_NAME, VALUE_NEW);
 
 		PropertyChangeNotificationSwitch switchdevice = (PropertyChangeNotificationSwitch) inst
 				.getServiceObject();
 
 		Assert.assertTrue(message_fail_set,
 				switchdevice.getState().equals(VALUE_NEW));
 
 		Assert.assertTrue(message_fail_callback,
 				switchdevice.getStateChangedCounter() > 0);
 
 	}
 
 	@Test
 	public void PropertyChangeNoticationCallbackCalledOnce_tc064() {
 
 		Implementation implementation = waitForImplByName(null,
 				"PropertyChangeNotification");
 		Instance inst = implementation.createInstance(null, null);
 
 		final String PROPERTY_NAME = "state";
 		final String VALUE_NEW = "new value";
 		final String message_fail_set = "Property being declared but after using instance.setAttribute the value inside the component did not correspont to the configured value";
 		final String message_fail_callback = "The callback for property change notifications(using 'method' attribute) was called %s times, and should have been called only once";
 
 		inst.setProperty(PROPERTY_NAME, VALUE_NEW);
 
 		PropertyChangeNotificationSwitch switchdevice = (PropertyChangeNotificationSwitch) inst
 				.getServiceObject();
 
 		Assert.assertTrue(message_fail_set,
 				switchdevice.getState().equals(VALUE_NEW));
 
 		Assert.assertTrue(
 				String.format(message_fail_callback,
 						switchdevice.getStateChangedCounter()),
 				switchdevice.getStateChangedCounter() == 1);
 
 	}
 
 	/**
 	 * Ensures that initial properties are configured in the instance properly
 	 */
 	@Test
 	public void PropertyConfiguredWithInitialParameter_tc002() {
 
 		Implementation samsungImpl = waitForImplByName(null, "SamsungSwitch");
 
 		Map<String, String> initialProperties = new HashMap<String, String>() {
 			{
 				put("property-01", "configured");
 				put("property-02", "configured");
 				put("property-03", "configured");
 				put("property-04", "configured");
 				put("property-05", "configured");
 			}
 		};
 
 		Instance samsungInst = samsungImpl.createInstance(null,
 				initialProperties);
 
 		Assert.assertNotNull("Instance could not be create through the API",
 				samsungInst);
 
 		// all the initial properties should be inside of the instance
 		for (String key : initialProperties.keySet()) {
 
 			Assert.assertNotNull(
 					"Instance did not receive the initial property",
 					samsungInst.getAllProperties().containsKey(key));
 
 			Assert.assertNotNull(
 					"Instance did not receive the initial property",
 					samsungInst.getAllProperties().get(key));
 
 			Assert.assertTrue(samsungInst.getAllProperties().get(key)
 					.equals(initialProperties.get(key)));
 		}
 	}
 
 	/**
 	 * Ensures that initial properties are configured in the instance properly
 	 */
 	@Test
 	public void PropertyConfiguredWithSetProperty_tc003() {
 
 		Implementation samsungImpl = waitForImplByName(null, "SamsungSwitch");
 
 		Map<String, String> initialProperties = new HashMap<String, String>() {
 			{
 				put("property-01", "configured-01");
 				put("property-02", "configured-02");
 				put("property-03", "configured-03");
 				put("property-04", "configured-04");
 				put("property-05", "configured-05");
 
 			}
 		};
 
 		Instance samsungInst = samsungImpl.createInstance(null, null);
 
 		samsungInst.setProperty("property-01", "configured-01");
 		samsungInst.setProperty("property-02", "configured-02");
 		samsungInst.setProperty("property-03", "configured-03");
 		samsungInst.setProperty("property-04", "configured-04");
 		samsungInst.setProperty("property-05", "configured-05");
 
 		final String message = "Instance did not receive the property defined by setProperty method call";
 
 		for (String key : initialProperties.keySet()) {
 
 			logger.debug(key + ":"
 					+ samsungInst.getAllProperties().get(key));
 
 			Assert.assertTrue(message, samsungInst.getAllProperties()
 					.containsKey(key));
 			Assert.assertTrue(
 					message,
 					samsungInst.getProperty(key).equals(
 							initialProperties.get(key)));
 		}
 	}
 
 	@Test
 	public void PropertyDefinitionInjectedBothByDefaultProperty_tct022() {
 
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl_tct021");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl_tct021 s1 = (S1Impl_tct021) s1Inst.getServiceObject();
 
 		String messageTemplace = "for a property whose type injected not specified (should be both by default), the %s";
 
 		Assert.assertTrue(
 				String.format(messageTemplace,
 						"initial value declared in the xml should NOT be ignored for both"),
 				s1.getInjectedBothByDefault().equals("default"));
 		s1Inst.setProperty("injectedBothByDefault", "changedByApamAPI");
 		Assert.assertTrue(
 				String.format(messageTemplace,
 						" value should be changeable by ApamInstance.setProperty"),
 				(s1.getInjectedBothByDefault() == null ? "" : s1
 						.getInjectedBothByDefault()).equals("changedByApamAPI"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						" value should be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("injectedBothByDefault").equals(
 						"changedByApamAPI"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						" value should be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("injectedBothByDefault")
 						.equals("changedByApamAPI"));
 
 		s1.setInjectedBothByDefault("changedByJavaInstance");
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking the java instance value"),
 				(s1.getInjectedBothByDefault() == null ? "" : s1
 						.getInjectedBothByDefault())
 						.equals("changedByJavaInstance"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("injectedBothByDefault").equals(
 						"changedByJavaInstance"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("injectedBothByDefault")
 						.equals("changedByJavaInstance"));
 	}
 
 	@Test
 	public void PropertyDefinitionInjectedBothProperty_tct021() {
 
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl_tct021");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl_tct021 s1 = (S1Impl_tct021) s1Inst.getServiceObject();
 
 		String messageTemplace = "for a property type injected='both', the %s";
 
 		Assert.assertTrue(
 				String.format(messageTemplace,
 						"initial value declared in the xml should NOT be ignored for both"),
 				s1.getInjectedBoth().equals("default"));
 		s1Inst.setProperty("injectedBoth", "changedByApamAPI");
 		Assert.assertTrue(String.format(messageTemplace,
 				" value should be changeable by ApamInstance.setProperty"), (s1
 				.getInjectedBoth() == null ? "" : s1.getInjectedBoth())
 				.equals("changedByApamAPI"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						" value should be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("injectedBoth").equals("changedByApamAPI"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						" value should be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("injectedBoth")
 						.equals("changedByApamAPI"));
 
 		s1.setInjectedBoth("changedByJavaInstance");
 		Assert.assertNotNull(s1.getInjectedBoth());
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking the java instance value"),
 				s1.getInjectedBoth().equals("changedByJavaInstance"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("injectedBoth").equals(
 						"changedByJavaInstance"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("injectedBoth")
 						.equals("changedByJavaInstance"));
 	}
 
 	@Test
 	public void PropertyDefinitionInjectedExternalProperty_tct023() {
 
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl_tct021");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl_tct021 s1 = (S1Impl_tct021) s1Inst.getServiceObject();
 
 		String messageTemplace = "for a property type injected='external', the %s";
 
 		Assert.assertTrue(
 				String.format(messageTemplace,
 						"initial value declared in the xml should NOT be ignored for external"),
 				s1.getInjectedExternal().equals("default"));
 		s1Inst.setProperty("injectedExternal", "changedByApamAPI");
 		Assert.assertTrue(String.format(messageTemplace,
 				" value should be changeable by ApamInstance.setProperty"), (s1
 				.getInjectedExternal() == null ? "" : s1.getInjectedExternal())
 				.equals("changedByApamAPI"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						" value should be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("injectedExternal").equals(
 						"changedByApamAPI"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						" value should be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("injectedExternal")
 						.equals("changedByApamAPI"));
 
 		s1.setInjectedExternal("changedByJavaInstance");
 
 		Assert.assertNotNull(s1.getInjectedExternal());
 
 		logger.debug("-----s1.getInjectedExternal():"
 				+ s1.getInjectedExternal());
 
 		// Property should remain unchanged
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should NOT be changeable by java instance, but it changed when checking JavaInstance.myfield"),
 				s1.getInjectedExternal().equals("changedByApamAPI"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should NOT be changeable by java instance, but it changed when checking ApamInstance.getProperty()"),
 				s1Inst.getProperty("injectedExternal").equals(
 						"changedByApamAPI"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should NOT be changeable by java instance, but it changed when checking ApamInstance.getAllProperties()"),
 				s1Inst.getAllProperties().get("injectedExternal")
 						.equals("changedByApamAPI"));
 	}
 
 	@Test
 	public void PropertyDefinitionInjectedInternalProperty_tct024() {
 
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl_tct021");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl_tct021 s1 = (S1Impl_tct021) s1Inst.getServiceObject();
 
 		String messageTemplace = "for a property type injected='internal', the %s";
 
 		Assert.assertNull(
 				String.format(messageTemplace,
 						"initial value declared in the xml should be ignored for internal"),
 				s1.getInjectedInternal());
 		s1Inst.setProperty("injectedInternal", "changedByApamAPI");
 		Assert.assertTrue(String.format(messageTemplace,
 				" value should NOT be changeable by ApamInstance.setProperty"),
 				s1.getInjectedInternal() == null
 						|| s1.getInjectedInternal().equals("changedByApamAPI"));
 		Assert.assertNull(
 				String.format(
 						messageTemplace,
 						" value should NOT be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("injectedInternal"));
 		Assert.assertNull(
 				String.format(
 						messageTemplace,
 						" value should NOT be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("injectedInternal"));
 
 		s1.setInjectedInternal("changedByJavaInstance");
 		Assert.assertNotNull(s1.getInjectedInternal());
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking the java instance value"),
 				s1.getInjectedInternal().equals("changedByJavaInstance"));
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("injectedInternal").equals(
 						"changedByJavaInstance"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("injectedInternal")
 						.equals("changedByJavaInstance"));
 	}
 
 	@Test
 	public void PropertyDefinitionInjectedPropertyDefinedInConstructor_tct025() {
 
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl_tct025");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl_tct025 s1 = (S1Impl_tct025) s1Inst.getServiceObject();
 		String messageTemplace = "for a property defined in constructor, the %s";
 
 		Assert.assertNotNull(String.format(messageTemplace,
 				"value declared in the xml should be ignored for internal"), s1
 				.getInjectedInternal());
 		Assert.assertTrue(String.format(messageTemplace,
 				"value declared in the xml should be ignored for internal"), s1
 				.getInjectedInternal().equals("Constructor defined value"));
 
 		Assert.assertTrue(String.format(messageTemplace,
 				"value declared in the xml should be used for external"), s1
 				.getInjectedExternal().equals("default"));
 
 		Assert.assertTrue(String.format(messageTemplace,
 				"value declared in the xml should be used for both"), s1
 				.getInjectedBothSetted().equals("default"));
 
 		Assert.assertNotNull(
 				String.format(messageTemplace,
 						"value declared in the constructor should be kept if not configured in XML"),
 				s1.getInjectedBothUnsetted());
 		Assert.assertTrue(
 				String.format(messageTemplace,
 						"value declared in the constructor should be kept if not configured in XML"),
 				s1.getInjectedBothUnsetted()
 						.equals("Constructor defined value"));
 	}
 
 	@Test
 	public void PropertyDefinitionInternalFalseTypeStringProperty_tc004() {
 
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 		String messageTemplace = "for a property type injected='external', the %s";
 
 		Assert.assertTrue(String.format(messageTemplace,
 				"initial value declared in the xml should NOT be ignored"), s1
 				.getStateNotInternal().equals("default"));
 
 		s1Inst.setProperty("stateNotInternal", "changedByApamAPI");
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by ApamInstance.setProperty, which is not true when checking the java instance property value"),
 				(s1.getStateNotInternal() == null ? "" : s1
 						.getStateNotInternal()).equals("changedByApamAPI"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("stateNotInternal").equals(
 						"changedByApamAPI"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by ApamInstance.setProperty, which is not true when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("stateNotInternal")
 						.equals("changedByApamAPI"));
 
 		s1.setStateNotInternal("changedByJavaInstance");
 
 		// All the following situation should remains unchanged since the field
 		// is an external (check the spec:
 		// https://docs.google.com/document/d/1JNffl2oNeS26HFbJ2OT-KnpvZnEYm5sja2XprsYG3Mo/edit#)
 
 		Assert.assertNotNull(s1.getStateNotInternal());
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking the java instance value"),
 				s1.getStateNotInternal().equals("changedByApamAPI"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("stateNotInternal").equals(
 						"changedByApamAPI"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("stateNotInternal")
 						.equals("changedByApamAPI"));
 
 	}
 
 	@Test
 	public void PropertyDefinitionInternalTrueTypeStringProperty_tc004() {
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 		String messageTemplace = "for a property type internal='true', %s";
 
 		Assert.assertTrue(String.format(messageTemplace,
 				"initial value declared in the xml should be ignored"), s1
 				.getStateInternal() == null);
 
 		s1Inst.setProperty("stateInternal", "changedByApamAPI");
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should NOT be changeable by ApamInstance.setProperty, although the value remains un altered java instance property value"),
 				s1.getStateInternal() == null);
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should NOT be changeable by ApamInstance.setProperty, although the value remains un altered when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("stateInternal") == null);
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should NOT be changeable by ApamInstance.setProperty,  although the value remains un altered when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("stateInternal") == null);
 
 		s1.setStateInternal("changedByJavaInstance");
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking the java instance property value"),
 				s1.getStateInternal().equals("changedByJavaInstance"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getProperty"),
 				s1Inst.getProperty("stateInternal").equals(
 						"changedByJavaInstance"));
 
 		Assert.assertTrue(
 				String.format(
 						messageTemplace,
 						"value should be changeable by java instance, although the value remains un altered when checking ApamInstance.getAllProperties"),
 				s1Inst.getAllProperties().get("stateInternal")
 						.equals("changedByJavaInstance"));
 
 	}
 
 	@Test
 	public void PropertyDefinitionIsVisibleWithValPropertySetXML_tc005() {
 
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 		Assert.assertTrue(
 				"Internal property value should be null, since the xml should be ignore.",
 				s1Inst.getAllProperties().get("stateInternal") == null);
 
 		Assert.assertTrue("Non-Internal property not visible through API",
 				s1Inst.getAllProperties().get("stateNotInternal") != null);
 
 		Assert.assertTrue(
 				"Non-Internal property value not visible through API",
 				s1Inst.getAllProperties().get("stateNotInternal")
 						.equals("default"));
 
 	}
 
 	@Test
 	public void PropertyEnumTypeSimpleValueNoTrick_tc072() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "barEnumValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(barEnumValuedSimple=Linux)";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertyEnumTypeSimpleValueSpaceAfter_tc073() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "barEnumValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(barEnumValuedSimple=Linux )";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertyEnumTypeSimpleValueSpaceBefore_tc074() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "barEnumValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(barEnumValuedSimple= Linux)";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertyFilterOSGiImplementationSubSet_Enum_tc061() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A subset('<*') B means that all A elements must be in B.";
 
 		String expression = "(OS <* {Linux, Windows, Android, IOS})";
 		String message = String.format(messageTemplate, expression,
 				inst.getProperty("OS"), "<*", inst.getProperty("OS"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS <* {IOS, Windows, Linux,Android})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("OS"), "<*", inst.getProperty("OSUnordered"),
 				true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS <* {Linux, Windows, IOS})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("OS"), "<*",
 				inst.getProperty("OSLessElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 		expression = "(OS <* {Linux, Windows, Android,IOS,AmigaOS})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("OS"), "<*",
 				inst.getProperty("OSMoreElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 	}
 
 	@Test
 	public void PropertyFilterOSGiImplementationSubSet_Integer_tc060() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A subset('<*') B means that all A elements must be in B.";
 
 		String expression = "(setInt <* {12,15,254, 0})";
 		String message = String.format(messageTemplate, expression,
 				inst.getProperty("setInt"), "<*", inst.getProperty("setInt"),
 				true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt <* {254,15,12,0})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setInt"), "<*",
 				inst.getProperty("setIntUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt <* {12,15, 0})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setInt"), "<*",
 				inst.getProperty("setIntLessElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 		expression = "(setInt <* {12,15,254, 0,27})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setInt"), "<*",
 				inst.getProperty("setIntMoreElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 	}
 
 	@Test
 	public void PropertyFilterOSGiImplementationSubSet_String_tc062() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A subset('<*') B means that all A elements must be in B.";
 
 		String expression = "(setString <* {doubt,grows,with,knowledge})";
 		String message = String.format(messageTemplate, expression,
 				inst.getProperty("setString"), "<*",
 				inst.getProperty("setString"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString <* {with,doubt,knowledge,grows})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setString"), "<*",
 				inst.getProperty("setStringUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString <* {doubt,grows,knowledge})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setString"), "<*",
 				inst.getProperty("setStringLessElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 		expression = "(setString <* {doubt,and,uncertainties,grows,with,knowledge})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setString"), "<*",
 				inst.getProperty("setStringMoreElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 	}
 
 	@Test
 	public void PropertyFilterOSGiImplementationSuperSet_Enum_tc058() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A superset('*>') B operator means that A must contain all B elements, although it may contain more.";
 
 		String expression = "(OS *> {Linux, Windows, Android, IOS})";
 		String message = String.format(messageTemplate, expression,
 				inst.getProperty("OS"), "*>", inst.getProperty("OS"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS *> {IOS, Windows, Linux,Android})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("OS"), "*>", inst.getProperty("OSUnordered"),
 				true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS *> {Linux, Windows, IOS})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("OS"), "*>",
 				inst.getProperty("OSLessElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(OS *> {Linux, Windows, Android,IOS,AmigaOS})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("OS"), "*>",
 				inst.getProperty("OSMoreElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 	}
 
 	@Test
 	public void PropertyFilterOSGiImplementationSuperSet_Integer_tc057() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A superset('*>') B operator means that A must contain all B elements, although it may contain more.";
 
 		String expression = "(setInt *> {12,15,254, 0})";
 		String message = String.format(messageTemplate, expression,
 				inst.getProperty("setInt"), "*>", inst.getProperty("setInt"),
 				true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt *> {254,15,12,0})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setInt"), "*>",
 				inst.getProperty("setIntUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt *> {12,15, 0})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setInt"), "*>",
 				inst.getProperty("setIntLessElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setInt *> {12,15,254, 0,27})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setInt"), "*>",
 				inst.getProperty("setIntMoreElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 	}
 
 	@Test
 	public void PropertyFilterOSGiImplementationSuperSet_String_tc059() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecFilterSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String messageTemplate = "%s [expanded expression: %s %s %s] should be %b. By definition the A superset('*>') B operator means that A must contain all B elements, although it may contain more.";
 
 		String expression = "(setString *> {doubt,grows,with,knowledge})";
 		String message = String.format(messageTemplate, expression,
 				inst.getProperty("setString"), "*>",
 				inst.getProperty("setString"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString *> {with,doubt,knowledge,grows})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setString"), "*>",
 				inst.getProperty("setStringUnordered"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString *> {doubt,grows,knowledge})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setString"), "*>",
 				inst.getProperty("setStringLessElements"), true);
 		Assert.assertTrue(message, inst.match(expression));
 
 		expression = "(setString *> {doubt,and,uncertainties,grows,with,knowledge})";
 		message = String.format(messageTemplate, expression,
 				inst.getProperty("setString"), "*>",
 				inst.getProperty("setStringMoreElements"), false);
 		Assert.assertTrue(message, !inst.match(expression));
 
 	}
 
 	/**
 	 * Ensures that inherited properties cannot be changed and inherited
 	 * definitions can change
 	 */
 	@Test
 	public void PropertyInheritedCannotBeChanged_tc001() {
 
 		Implementation samsungImpl = waitForImplByName(null, "SamsungSwitch");
 		final Instance samsungInst = samsungImpl.createInstance(null, null);
 
 		Implementation s1Impl = waitForImplByName(null,
 				"fr.imag.adele.apam.pax.test.impl.S1Impl");
 
 		Instance s1Inst = s1Impl.createInstance(null, null);
 
 		S1Impl s1 = (S1Impl) s1Inst.getServiceObject();
 
 		// this should be updated correctly
 		samsungInst.setProperty("currentVoltage", "999");
 		// this should stay with the old value
 		samsungInst.setProperty("made", "deutschland");
 
 		// this property should be updated since its not inherited
 		Assert.assertTrue("Non-inherited properties shall be updateable",
 				samsungInst.getProperty("currentVoltage").equals("999"));
 
 		Assert.assertTrue("Inherited property shall not be changed",
 				samsungInst.getProperty("made").equals("china"));
 
 	}
 
 	@Test
 	public void PropertySetTypeBorderElements_tc067() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "fooSetValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(fooSetValuedSimple *> {Linux, IOS})";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertySetTypeBracesCommaInTheEnd_tc066() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "fooSetValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(fooSetValuedSimple *> {Android, Windows,})";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertySetTypeBracesNoCommaInTheEnd_tc065() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "fooSetValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(fooSetValuedSimple *> {Android, Windows})";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertySetTypeMiddleElements_tc068() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "fooSetValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(fooSetValuedSimple *> {Android, Windows})";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertySetTypeNoBracesComma_tc070() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "fooSetValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(fooSetValuedSimple *> Android, Windows,)";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertySetTypeNoBracesNoComma_tc071() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "fooSetValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(fooSetValuedSimple *> Android, Windows)";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertySetTypeOneMiddleOneBorderElement_tc069() {
 
 		Implementation implementation = waitForImplByName(null,
 				"SpecEnumVersusSetTestSwitch");
 		Instance inst = implementation.createInstance(null, null);
 
 		String propertyName = "fooSetValuedSimple";
 		String propertyValue = inst.getProperty(propertyName);
 		String expression = "(fooSetValuedSimple *> {Android, IOS})";
 		boolean result = inst.match(expression);
 		boolean expected = true;
 
 		String message = String
 				.format("The result of the expression %s was %s, but was expected %s, because the property %s had the value %s",
 						expression, result, expected, propertyName,
 						propertyValue);
 
 		if (expected) {
 			Assert.assertTrue(message, result);
 		} else {
 			Assert.assertFalse(message, result);
 		}
 	}
 
 	@Test
 	public void PropertyTypeBooleanChangeNoticationCallback_tc116() {
 
 		final String prologTemplate = "Declaring a 'method' attribute into a boolean type property, should invoke the declared method with the right type and value. %s";
 
 		Boolean propertyValue = Boolean.TRUE;
 
 		Implementation implementation = waitForImplByName(null,
 				"PropertyTypeBooleanChangeNotification");
 		Instance inst = implementation.createInstance(null, null);
 
 		inst.setProperty("state", propertyValue);
 
 		PropertyTypeBooleanChangeNotificationSwitch switchdevice = (PropertyTypeBooleanChangeNotificationSwitch) inst
 				.getServiceObject();
 
 		Assert.assertTrue(String.format(prologTemplate,
 				"Although the invocation was not performed."), switchdevice
 				.getObjectReceivedInNotification() != null);
 
 		String messageWrongType = String
 				.format(prologTemplate,
 						"Although the invocation was not performed with the right type. ");
 		String messageWrongTypeDetail = String.format(messageWrongType
 				+ " %s given instead of %s", switchdevice
 				.getObjectReceivedInNotification().getClass()
 				.getCanonicalName(), Boolean.class.getCanonicalName());
 
 		Assert.assertTrue(
 				messageWrongTypeDetail,
 				switchdevice.getObjectReceivedInNotification() instanceof Boolean);
 
 		String messageWrongValue = String
 				.format(prologTemplate,
 						"Although the invocation was not performed with the right value. ");
 		String messageWrongValueDetail = String.format(messageWrongValue
 				+ " %s given instead of %s",
 				switchdevice.getObjectReceivedInNotification(), propertyValue);
 
 		Assert.assertTrue(messageWrongValueDetail, switchdevice
 				.getObjectReceivedInNotification().equals(propertyValue));
 
 	}
 
 	@Test
 	public void PropertyTypeIntChangeNoticationCallback_tc115() {
 
 		final String prologTemplate = "Declaring a 'method' attribute into a integer type property, should invoke the declared method with the right type and value. %s";
 
 		Integer propertyValue = Integer.valueOf(734);
 
 		Implementation implementation = waitForImplByName(null,
 				"PropertyTypeIntChangeNotification");
 		Instance inst = implementation.createInstance(null, null);
 
 		inst.setProperty("state", propertyValue);
 
 		PropertyTypeIntChangeNotificationSwitch switchdevice = (PropertyTypeIntChangeNotificationSwitch) inst
 				.getServiceObject();
 
 		Assert.assertTrue(String.format(prologTemplate,
 				"Although the invocation was not performed."), switchdevice
 				.getObjectReceivedInNotification() != null);
 
 		String messageWrongType = String
 				.format(prologTemplate,
 						"Although the invocation was not performed with the right type. ");
 		String messageWrongTypeDetail = String.format(messageWrongType
 				+ " %s given instead of %s", switchdevice
 				.getObjectReceivedInNotification().getClass()
 				.getCanonicalName(), Integer.class.getCanonicalName());
 
 		Assert.assertTrue(
 				messageWrongTypeDetail,
 				switchdevice.getObjectReceivedInNotification() instanceof Integer);
 
 		String messageWrongValue = String
 				.format(prologTemplate,
 						"Although the invocation was not performed with the right value. ");
 		String messageWrongValueDetail = String.format(messageWrongValue
 				+ " %s given instead of %s",
 				switchdevice.getObjectReceivedInNotification(), propertyValue);
 
 		Assert.assertTrue(messageWrongValueDetail, switchdevice
 				.getObjectReceivedInNotification().equals(propertyValue));
 
 	}
 
 	@Test
 	public void testPropertyDefinitionImplem_tct027() {
 
 		logger.debug("Testing Implementation properties");
 		Implementation implemS1 = waitForImplByName(null, "implem-s1-tct026");
		logger.debug("*********-->" + implemS1.getAllPropertiesString());
 		for (int i = 0; i <= 6; i++) {
 			if (!implemS1.setProperty("def" + i + "-specs-s1", "test-prop"))
 			// if property is not settable, it must be final
 			{
 				logger.debug("def" + i + "-specs-s1 is not settable");
 				Assert.assertEquals(" Property def" + i
 						+ "-specs-s1 is not defined", "final-value",
 						implemS1.getProperty("def" + i + "-specs-s1"));
 			}
 		}
 		for (int i = 0; i <= 1; i++) {
 			if (!implemS1.setProperty("def" + i + "-implem-s1", "test-prop"))
 			// if property is not settable, it must be final
 			{
 				logger.debug("def" + i + "-implem-s1 is not settable");
 				Assert.assertEquals(" Property def" + i
 						+ "-implem-s1 is not defined", "final-value",
 						implemS1.getProperty("def" + i + "-implem-s1"));
 			}
 		}
 
 		// TODO test the properties defined at this level
 	}
 
 	@Test
 	public void testPropertyDefinitionInstance_tct028() {
 		logger.debug("Testing Instance properties");
 
 		auxListInstances();
 		Implementation implemS1 = waitForImplByName(null, "implem-s1-tct026");
 		auxListInstances();
 		Instance instanceS1 = waitForInstByName(null, "instance-s1-tct026");
 		auxListInstances();
 
 		// for (int i = 0; i <= 6; i++)
 		// if (!instanceS1.setProperty("def" + i + "-specs-s1", "test-prop"))
 		// // if property is not settable, it must be final
 		// {
 		// logger.debug("def" + i + "-specs-s1 is not settable");
 		// Assert.assertEquals(" Property def" + i
 		// + "-specs-s1 is not defined", "final-value",
 		// instanceS1.getProperty("def" + i + "-specs-s1"));
 		// }
 		// TODO test the properties defined at this level
 
 		// logger.debug(specsS1.getProperty("def"+i+"-specs-s1"));//getDeclaration().getPropertyDefinition("def"+i+"-specs-s1"));
 		//
 		// for(int i=0; i<=7; i++) {
 		//
 		// System.out.print("--> "+specsS1.getProperty("def"+i+"-specs-s1"));
 		// specsS1.setProperty("def"+i+"-specs-s1", "test-prop");
 		// logger.debug(" <--> "+specsS1.getProperty("def"+i+"-specs-s1"));
 		// }
 
 		//
 		// ComponentDeclaration implemS1 = waitForImplByName(null,
 		// "implem-s1-tct026").getDeclaration();
 		// for(int i=0; i<=6; i++)
 		// Assert.assertNotNull(" Property def"+i+"-specs-s1 is not defined",
 		// implemS1.getPropertyDefinition("def"+i+"-specs-s1"));
 
 	}
 
 	@Test
 	public void testPropertyDefinitionSpec_tct026() {
 
 		logger.debug("Testing Specification properties defined");
 		Specification specsS1 = waitForSpecByName(null, "specs-s1-tct026");
 
 		for (int i = 0; i <= 6; i++) {
 			Assert.assertEquals(
 					" Property def" + i + "-specs-s1 is not defined",
 					"def" + i + "-specs-s1",
 					specsS1.getDeclaration()
 							.getPropertyDefinition("def" + i + "-specs-s1")
 							.getName());
 		}
 
 		logger.debug("Testing Specification properties setted");
 		Assert.assertEquals(" Property def0-specs-s1 is not setted",
 				"final-value", specsS1.getProperty("def0-specs-s1"));
 		Assert.assertEquals(" Property def1-specs-s1 is not setted",
 				"final-value", specsS1.getProperty("def1-specs-s1"));
 
 	}
 
 	@Test
 	public void testPropertyDefinitionFloat_tct029() {
 
 		double myval=12.34;
 		logger.debug("Testing defintion of Float ");
 		Specification specsS1 = waitForSpecByName(null, "specs-s1-tct026");
 		Implementation implemS1 = waitForImplByName(null, "implem-s1-tct026");
 		Instance instanceS1 = waitForInstByName(null, "instance-s1-tct026");
 		
 		for(int i=0; i<3; i++) {
 			logger.debug("Testing for "+specsS1.getName()+" : "+specsS1.getPropertyDefinition("prop-def"+i)+", "+specsS1.getPropertyObject("prop-def"+i));
 			logger.debug("Testing for "+implemS1.getName()+" : "+implemS1.getPropertyDefinition("prop-def"+i)+", "+implemS1.getPropertyObject("prop-def"+i));
 			logger.debug("Testing for "+implemS1.getName()+" : "+implemS1.getPropertyDefinition("prop-impl"+i)+", "+implemS1.getPropertyObject("prop-impl"+i));
 			logger.debug("Testing for "+instanceS1.getName()+" : "+instanceS1.getPropertyDefinition("prop-def"+i)+", "+instanceS1.getPropertyObject("prop-def"+i));
 			logger.debug("Testing for "+instanceS1.getName()+" : "+instanceS1.getPropertyDefinition("prop-impl"+i)+", "+instanceS1.getPropertyObject("prop-impl"+i));
 			logger.debug("Testing for "+instanceS1.getName()+" : "+instanceS1.getPropertyDefinition("prop-inst"+i)+", "+instanceS1.getPropertyObject("prop-inst"+i));
 			
 			Assert.assertEquals("expected type is float in spec", "float",specsS1
 					.getPropertyDefinition("prop-def"+i).getType());
 			Assert.assertEquals("expected type is float in implem", "float",implemS1
 					.getPropertyDefinition("prop-def"+i).getType());
 			Assert.assertEquals("expected type is float in implem", "float",implemS1
 					.getPropertyDefinition("prop-impl"+i).getType());			
 			Assert.assertEquals("expected type is float in instance", "float",instanceS1
 					.getPropertyDefinition("prop-def"+i).getType());
 			Assert.assertEquals("expected type is float in instance", "float",instanceS1
 					.getPropertyDefinition("prop-impl"+i).getType());
 			Assert.assertEquals("expected type is float in instance", "float",instanceS1
 					.getPropertyDefinition("prop-inst"+i).getType());			
 			
 			if(specsS1.getPropertyObject("prop-def"+i) != null)
 				Assert.assertTrue(specsS1.getPropertyObject("prop-def"+i) instanceof Float);
 			
 			if(implemS1.getPropertyObject("prop-def"+i) != null)
 				Assert.assertTrue(implemS1.getPropertyObject("prop-def"+i) instanceof Float);
 			if(implemS1.getPropertyObject("prop-impl"+i) != null)
 				Assert.assertTrue(implemS1.getPropertyObject("prop-impl"+i) instanceof Float);
 			
 			if(instanceS1.getPropertyObject("prop-def"+i) != null)
 				Assert.assertTrue(instanceS1.getPropertyObject("prop-def"+i) instanceof Float);
 			if(instanceS1.getPropertyObject("prop-impl"+i) != null)
 				Assert.assertTrue(instanceS1.getPropertyObject("prop-impl"+i) instanceof Float);
 			if(instanceS1.getPropertyObject("prop-inst"+i) != null)
 				Assert.assertTrue(instanceS1.getPropertyObject("prop-inst"+i) instanceof Float);
 		}
 		
 //		Assert.assertEquals("expected type is float", Float.parseFloat("12.34"), specsS1.getPropertyObject("prop-def0"));
 	}	
 	
 	@Test
 	public void testPropertySettingFloat_tct030() {
 		//TODO
 	}
 	
 	@Test
 	public void testPropertyInjectionFloat_tct031() {
 		// TODO
 	}
 	
 	@Test
 	public void testPropertyInheritanceFloat_tct032() {
 		//TODO
 	}
 	
 	
 	@Test
 	public void testPropertyCompareFloat_tct033() {
 		// TODO
 	}
 
 }
