 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.injector.extension;
 
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
import org.eclipse.riena.internal.core.test.collect.ManualTestCase;
 
 /**
  * Test the {@code ExtensionInjector} with respect to the wiring feature.
  */
//NonUITestCase FIXME addPluginXML does not work
@ManualTestCase
 public class ExtensionInjectorWithMultipleExtensionPointIdTest extends RienaTestCase {
 
 	public void testWithEPsAandBAndContribsAandB() {
 		printTestName();
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "pluginA.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "pluginB.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "plugin_multiple_epA.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "plugin_multiple_epB.xml");
 		ConfigurableMultipleExtensionPointThing target = new ConfigurableMultipleExtensionPointThing();
 		ExtensionInjector injector = Inject.extension("core.test.extpointA,core.test.extpointB").into(target).andStart(
 				getContext());
 		IDataMultipleExtensionPointId[] data = target.getData();
 		assertNotNull(data);
 		assertEquals(2, data.length);
 		assertEquals("A", data[0].getText());
 		assertEquals("B", data[1].getText());
 
 		removeExtension("core.test.extpoint.id1");
 		removeExtension("core.test.extpoint.id2");
 		removeExtensionPoint("core.test.extpointA");
 		removeExtensionPoint("core.test.extpointB");
 		injector.stop();
 	}
 
 	public void testWithEPsAandBAndContribA() {
 		printTestName();
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "pluginA.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "pluginB.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "plugin_multiple_epA.xml");
 		ConfigurableMultipleExtensionPointThing target = new ConfigurableMultipleExtensionPointThing();
 		ExtensionInjector injector = Inject.extension("core.test.extpointA,core.test.extpointB").into(target).andStart(
 				getContext());
 		IDataMultipleExtensionPointId[] data = target.getData();
 		assertNotNull(data);
 		assertEquals(1, data.length);
 		assertEquals("A", data[0].getText());
 
 		removeExtension("core.test.extpoint.id1");
 		removeExtensionPoint("core.test.extpointA");
 		removeExtensionPoint("core.test.extpointB");
 		injector.stop();
 	}
 
 	public void testWithEPsAandBAndContribB() {
 		printTestName();
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "pluginA.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "pluginB.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "plugin_multiple_epB.xml");
 		ConfigurableMultipleExtensionPointThing target = new ConfigurableMultipleExtensionPointThing();
 		ExtensionInjector injector = Inject.extension("core.test.extpointA,core.test.extpointB").into(target).andStart(
 				getContext());
 		IDataMultipleExtensionPointId[] data = target.getData();
 		assertNotNull(data);
 		assertEquals(1, data.length);
 		assertEquals("B", data[0].getText());
 
 		removeExtension("core.test.extpoint.id2");
 		removeExtensionPoint("core.test.extpointA");
 		removeExtensionPoint("core.test.extpointB");
 		injector.stop();
 	}
 
 	public void testWithEPsBAndContribsAandB() {
 		printTestName();
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "pluginB.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "plugin_multiple_epA.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "plugin_multiple_epB.xml");
 		ConfigurableMultipleExtensionPointThing target = new ConfigurableMultipleExtensionPointThing();
 		ExtensionInjector injector = Inject.extension("core.test.extpointA,core.test.extpointB").into(target).andStart(
 				getContext());
 		IDataMultipleExtensionPointId[] data = target.getData();
 		assertNotNull(data);
 		assertEquals(1, data.length);
 		assertEquals("B", data[0].getText());
 
 		removeExtension("core.test.extpoint.id2");
 		removeExtensionPoint("core.test.extpointB");
 		injector.stop();
 	}
 
	// TODO Have to find out why this fails.
	public void XXXtestWithEPsAAndContribsAandB() {
 		printTestName();
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "pluginA.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "plugin_multiple_epA.xml");
 		addPluginXml(ExtensionInjectorWithMultipleExtensionPointIdTest.class, "plugin_multiple_epB.xml");
 		ConfigurableMultipleExtensionPointThing target = new ConfigurableMultipleExtensionPointThing();
 		ExtensionInjector injector = Inject.extension("core.test.extpointA,core.test.extpointB").into(target).andStart(
 				getContext());
 		IDataMultipleExtensionPointId[] data = target.getData();
 		assertNotNull(data);
 		assertEquals(1, data.length);
 		assertEquals("A", data[0].getText());
 
 		removeExtension("core.test.extpoint.id1");
 		removeExtensionPoint("core.test.extpointA");
 		injector.stop();
 	}
 
 }
