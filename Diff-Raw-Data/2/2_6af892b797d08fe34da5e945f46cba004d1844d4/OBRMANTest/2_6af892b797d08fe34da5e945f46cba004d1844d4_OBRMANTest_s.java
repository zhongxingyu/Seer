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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.PaxExam;
 import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
 import org.ops4j.pax.exam.spi.reactors.PerMethod;
 import org.osgi.framework.Bundle;
 
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.ResolutionException;
 import fr.imag.adele.apam.app1.spec.App1Spec;
 import fr.imag.adele.apam.app2.spec.App2Spec;
 import fr.imag.adele.apam.pax.test.implS7.S07Implem14;
 import fr.imag.adele.apam.pax.test.msg.device.EletronicMsg;
 import fr.imag.adele.apam.pax.test.msg.m1.producer.impl.M1ProducerImpl;
 import fr.imag.adele.apam.test.obrman.OBRMANHelper;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
 /**
  * Test Suite
  * 
  */
 @RunWith(PaxExam.class)
 @ExamReactorStrategy(PerMethod.class)
 public class OBRMANTest extends ExtensionAbstract {
 
 	static OBRMANHelper obrmanhelper;
 
 	@Override
 	public List<Option> config() {
 		List<Option> obrmanconfig = super.config(null, true);
 		return obrmanconfig;
 	}
 
 	@Before
 	public void constructor() {
 		obrmanhelper = new OBRMANHelper(context);
 	}
 
 	/**
 	 * APP1 declare two repositories in ObrMan model The composite APP1 deploy
 	 * and instantiate the composite APP2 The composite APP2 will be inside the
 	 * composite APP1
 	 * 
 	 */
 	@Test
 	public void embeddedComposite() {
 		waitForApam();
 		CompositeType app1CompoType = null;
 
 		try {
 			String[] repos = { "jar:mvn:fr.imag.adele.apam.tests.obrman.repositories/public.repository/"
 					+ obrmanhelper.getMavenVersion() + "!/app-store.xml" };
 			obrmanhelper.setObrManInitialConfig("rootAPPS", repos, 1);
 			app1CompoType = obrmanhelper.createCompositeType("APP1",
 					"APP1_MAIN", null);
 
 			CompositeType app2CompoType = obrmanhelper.createCompositeType(
 					"APP2", "APP2_MAIN", null);
 
 			for (String s : repos) {
 				System.out.println("repo : " + s);
 			}
 
 		} catch (IOException e) {
 
 			fail(e.getMessage());
 		}
 
 		App1Spec app1Spec = apam.createInstance(app1CompoType, App1Spec.class);
 
 		System.out
 				.println("\n==================Start call test=================== \n");
 
 		app1Spec.call("Call Main APP1 from Test");
 
 		System.out
 				.println("\n=================End call test====================\n");
 	}
 
 	/**
 	 * APP1 declare one repository and APP2 composite in ObrMan model Try to
 	 * create APP1 composite, but APP2 composite is missing
 	 */
 	@Test
 	public void missingAPP2Composite() {
 		waitForApam();
 		try {
 			obrmanhelper.createCompositeType("APP1.2", "APP1_MAIN", null);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 	}
 
 	/**
 	 * APP1 declare one repository and APP2 composite in ObrMan model create the
 	 * composite APP2 and call it create the composite APP1 which will call the
 	 * composite APP2 APP1 and APP2 will be on the same level of root composite.
 	 */
 	@Test
 	public void movedCompositev1() {
 		waitForApam();
 
 		simpleComposite();
 
 		CompositeType app1CompoType = null;
 		try {
 			app1CompoType = obrmanhelper.createCompositeType("APP1.2",
 					"APP1_MAIN", null);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 
 		CompositeType root = (CompositeType) app1CompoType.getInCompositeType()
 				.toArray()[0];
 
 		assertEquals(2, root.getEmbedded().size()); // the root compositeType
 		// contains two composites
 
 		App1Spec app1Spec = obrmanhelper.createInstance(app1CompoType,
 				App1Spec.class);
 
 		System.out
 				.println("\n==================Start call test=================== \n");
 
 		app1Spec.call("Call Main APP1 from Test");
 
 		System.out
 				.println("\n=================End call test====================\n");
 
 		assertEquals(1, app1CompoType.getEmbedded().size()); // app1 contains
 		// app2
 
 		assertEquals(1, root.getEmbedded().size()); // the root compositeType
 		// contains two composites
 
 	}
 
 	@Test
 	public void obrmanInstanciationWhenBundleInstalledNotStarted_tct005() {
 		waitForApam();
 
 		Implementation implementation = waitForImplByName(null,
 				"Obrman-Test-S3Impl");
 
 		Assert.assertNotNull(
 				"Obrman-Test-S3Impl cannot be resolved (cannot be found using obrman)",
 				implementation);
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		Assert.assertNotNull("Instance of Obrman-Test-S3Impl is null", instance);
 		Bundle bundle = implementation.getApformComponent().getBundle();
 
 		try {
 			bundle.stop();
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 
 		implementation = waitForImplByName(null, "Obrman-Test-S3Impl");
 
 		Assert.assertNotNull(
 				"Obrman-Test-S3Impl cannot be resolved as bundle is not started",
 				implementation);
 
 		instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		Assert.assertNotNull("Instance of Obrman-Test-S3Impl is null", instance);
 
 	}
 
 	@Test
 	public void RelationLinkResolveExternal_tct003() {
 		waitForApam();
 
 		Implementation implementation = waitForImplByName(null,
 				"S07-implementation-14ter");
 
 		Instance instance = implementation.createInstance(null,
 				Collections.<String, String> emptyMap());
 
 		// Test should success on external bundle resolution
 		auxListInstances();
 		org.junit.Assert
 				.assertFalse(
 						"No exception should be raised as the dependency can be resolved externally",
 						testResolutionException(instance, 3));
 		Assert.assertEquals("Only one relation should have been created : ", 1,
 				instance.getRawLinks().size());
 
 	}
 
 	public static boolean testResolutionException(Instance inst,
 			int methodNumber) {
 		// Force field injection (a bit akward with polymorphism)
 		S07Implem14 implem = (S07Implem14) inst.getServiceObject();
 		try {
 			switch (methodNumber) {
 			case 2:
 				if (implem.getInjected02() == null) {
 					return true;
 				}
 				break;
 			case 3:
 				if (implem.getInjected03() == null) {
 					return true;
 				}
 				break;
 			}
 
 		} catch (ResolutionException exc) {
 			exc.printStackTrace();
 			return true;
 		} catch (Exception exc) {
 			exc.printStackTrace();
 			return true;
 		}
 		return false;
 	}
 
 	@Before
 	@Override
 	public void setUp() {
 		super.setUp();
 
 		waitForInstByName(null, "OBRMAN-Instance");
 	}
 
 	/**
 	 * Simple Test : Create a compositetype with obrman model and instantiate it
 	 * then call the application service This composite must contains only the
 	 * spec and the main impl of the composite
 	 */
 	@Test
 	public void simpleComposite() {
 		waitForApam();
 		CompositeType app2CompoType = null;
 		try {
 			String[] repos = { "jar:mvn:fr.imag.adele.apam.tests.obrman.repositories/public.repository/"
 					+ obrmanhelper.getMavenVersion() + "!/app-store.xml" };
 			obrmanhelper.setObrManInitialConfig("rootAPPS", repos, 1);
 			app2CompoType = obrmanhelper.createCompositeType("APP2",
 					"APP2_MAIN", null);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 
 		App2Spec app2Spec = obrmanhelper.createInstance(app2CompoType,
 				App2Spec.class);
 
 		System.out
 				.println("\n==================Start call test=================== \n");
 
 		app2Spec.call("Call Main APP2 from Test");
 
 		System.out
 				.println("\n=================End call test====================\n");
 	}
 
 	/**
 	 * Done some initializations.
 	 */
 
 	@Test
 	public void testRootModel() {
 
 		auxListInstances();
 		int sizebefore = obrmanhelper
 				.getCompositeRepos(CST.ROOT_COMPOSITE_TYPE).size();
 		try {
 			obrmanhelper.setObrManInitialConfig("wrongfilelocation", null, 1);
 			fail("wrongfilelocation");
 		} catch (IOException e) {
 			assertEquals(sizebefore,
 					obrmanhelper.getCompositeRepos(CST.ROOT_COMPOSITE_TYPE)
 							.size());
 		}
 	}
 
 	@Test
 	public void compositeWithMainImplemUsingOBR_tct035() {
 
 		InstanceCreator creator = new InstanceCreator("compositeWithMainImplem_tct034");
 		creator.start();
 		try {
			Thread.sleep(50000);
 		} catch (Exception exc) {
 			exc.printStackTrace();
 		}
 		auxListInstances();
 
 		Assert.assertTrue(
 				"Composite should be created when main implem in another bundle",
 				creator.created != null);
 	}
 
 	class InstanceCreator extends Thread {
 
 		String myCompositeName;
 		public Instance created;
 
 		public InstanceCreator(String compositeName) {
 			myCompositeName = compositeName;
 			created = null;
 		}
 
 		@Override
 		public void run() {
 			CompositeType compo = (CompositeType) waitForComponentByName(null,
 					myCompositeName);			
 			if (compo != null) {
 				created = compo.createInstance(null, null);
 			}
 		}
 	}
 
 }
