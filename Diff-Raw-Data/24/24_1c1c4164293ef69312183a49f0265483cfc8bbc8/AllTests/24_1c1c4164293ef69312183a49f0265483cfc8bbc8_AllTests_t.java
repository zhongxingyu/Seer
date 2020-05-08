 /*
  * Copyright (c) 2005, 2010 Borland Software Corporation and others
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.gmf.tests.gef.CompartmentPropertiesTest;
 import org.eclipse.gmf.tests.gef.DiagramEditorTest;
 import org.eclipse.gmf.tests.gef.DiagramNodeTest;
 import org.eclipse.gmf.tests.gef.ParsersTest;
 import org.eclipse.gmf.tests.gef.ParsersTest.ParsersSetup;
 import org.eclipse.gmf.tests.gen.*;
 import org.eclipse.gmf.tests.migration.AllMigrationTests;
 import org.eclipse.gmf.tests.rt.AuditRulesTest;
 import org.eclipse.gmf.tests.rt.BundleActivationTest;
 import org.eclipse.gmf.tests.rt.EditHelpersTest;
 import org.eclipse.gmf.tests.rt.ElementInitializerTest;
 import org.eclipse.gmf.tests.rt.LinkChildMetaFeatureNotFromContainerTest;
 import org.eclipse.gmf.tests.rt.LinkCreationConstraintsTest;
 import org.eclipse.gmf.tests.rt.LinkCreationTest;
 import org.eclipse.gmf.tests.rt.LinkEcoreConstraintsTest;
 import org.eclipse.gmf.tests.rt.MetricRulesTest;
 import org.eclipse.gmf.tests.rt.PaletteTest;
 import org.eclipse.gmf.tests.setup.LinksSessionSetup;
 import org.eclipse.gmf.tests.setup.SessionSetup;
 import org.eclipse.gmf.tests.setup.TestAllDerivedFeatures;
 import org.eclipse.gmf.tests.setup.TestSetupTest;
 import org.eclipse.gmf.tests.setup.figures.FigureCodegenSetup;
 import org.eclipse.gmf.tests.setup.figures.FigureLayoutSetup;
 import org.eclipse.gmf.tests.setup.figures.LabelSupportSetup;
 import org.eclipse.gmf.tests.setup.figures.ShapePropertiesSetup;
 import org.eclipse.gmf.tests.tr.AuditRootTest;
 import org.eclipse.gmf.tests.tr.EcoreGenModelMatcherTest;
 import org.eclipse.gmf.tests.tr.GenModelGraphAnalyzerTest;
 import org.eclipse.gmf.tests.tr.GenModelTransformerBasicRTTest;
 import org.eclipse.gmf.tests.tr.GenModelTransformerSimpleTest;
 import org.eclipse.gmf.tests.tr.HistoryTest;
 import org.eclipse.gmf.tests.tr.LabelMappingTransformTest;
 import org.eclipse.gmf.tests.tr.ManifestMergeTest;
 import org.eclipse.gmf.tests.tr.NamingStrategyTest;
 import org.eclipse.gmf.tests.tr.PaletteTransformationTest;
 import org.eclipse.gmf.tests.tr.PluginXMLTextMergerTest;
 import org.eclipse.gmf.tests.tr.TestDefaultMergeService;
 import org.eclipse.gmf.tests.tr.TransformToGenModelOperationTest;
 import org.eclipse.gmf.tests.tr.XmlTextMergerTest;
 import org.eclipse.gmf.tests.validate.AllValidateTests;
import org.eclipse.jdt.core.JavaCore;
 
 public class AllTests {
 
 	public static Test suite() throws Exception {
 		TestSuite suite = new TestSuite("Tests for org.eclipse.gmf, tooling side");
 		final SessionSetup sessionSetup = SessionSetup.newInstance();
 		final LinksSessionSetup sessionSetup2 = LinksSessionSetup.newInstance();
 		final SessionSetup setupLinkEcoreConstraintsTest = new LinkEcoreConstraintsTest.CustomSetup();
 		final SessionSetup setupLinkChildMetaFeatureNotFromContainerTest = new LinkChildMetaFeatureNotFromContainerTest.CustomSetup();
 		// FIXME both EditHelpers BundleActivation setups may benefit from GenProjectSetup that doesn't need
 		// subclassing to generate extra code
 		final SessionSetup setupEditHelpersTest = new EditHelpersTest.EditHelpersSessionSetup();
 		final SessionSetup setupBundleActivationTest = new BundleActivationTest.CustomSetup();
 		
 		SessionSetup.disallowSingleTestCaseUse();
 		
 
		JavaCore.initializeAfterLoad(null);

 		/*
 		 * Temporary workaround: loading all the projects in the
 		 * beginning to get rid of the problems with runtime registries
 		 * reloading. In particular - ViewService.
 		 */
 		// since we force initialization, need to make sure our tests would use same initialized setup instances. 
 		Plugin.getConfig().registerInstance(SessionSetup.class, sessionSetup);
 		Plugin.getConfig().registerInstance(LinksSessionSetup.class, sessionSetup2);
 		Plugin.getConfig().registerInstance(setupLinkEcoreConstraintsTest.getClass(), setupLinkEcoreConstraintsTest);
 		Plugin.getConfig().registerInstance(setupLinkChildMetaFeatureNotFromContainerTest.getClass(), setupLinkChildMetaFeatureNotFromContainerTest);
 		Plugin.getConfig().registerInstance(setupEditHelpersTest.getClass(), setupEditHelpersTest);
 		Plugin.getConfig().registerInstance(setupBundleActivationTest.getClass(), setupBundleActivationTest);
 		try {
 			sessionSetup.getGeneratedPlugin();
 			sessionSetup2.getGeneratedPlugin();
 			setupLinkChildMetaFeatureNotFromContainerTest.getGeneratedPlugin();
 			setupLinkEcoreConstraintsTest.getGeneratedPlugin();
 			setupEditHelpersTest.getGeneratedPlugin();
 			setupBundleActivationTest.getGeneratedPlugin();
 		} catch (final Exception e) {
 			suite.addTest(new ConfigurationFailedCase("Session setup initialization problem", e));
 			return suite;
 		}
 		/* END */
 
 		//$JUnit-BEGIN$
 		suite.addTestSuite(TestSetupTest.class); // first, check sources/setups we use for the rest of the tests
 		suite.addTestSuite(HandcodedImplTest.class); // then, check handcoded implementations are in place
 		suite.addTestSuite(HandcodedGraphDefTest.class);
 		suite.addTestSuite(HandcodedPaletteTest.class);
 		suite.addTestSuite(HandcodedContributionItemTest.class);
 		suite.addTestSuite(HandcodedGMFMapItemProvidersTest.class);
 
 		suite.addTestSuite(GenModelTransformerSimpleTest.class);
 		suite.addTestSuite(TransformToGenModelOperationTest.class);
 		suite.addTestSuite(LabelMappingTransformTest.class);
 		suite.addTestSuite(PaletteTransformationTest.class);
 		suite.addTestSuite(AuditRootTest.class);
 		suite.addTestSuite(HistoryTest.class);
 		suite.addTestSuite(XmlTextMergerTest.class);
 		suite.addTestSuite(TestDefaultMergeService.class);
 		suite.addTestSuite(PluginXMLTextMergerTest.class);
 		suite.addTestSuite(ManifestMergeTest.class);
         suite.addTestSuite(OrganizeImportsPostprocessorTest.class);
 
 		suite.addTestSuite(EcoreGenModelMatcherTest.class);
 		suite.addTestSuite(ModelLoadHelperTest.class);		
 		suite.addTest(AllMigrationTests.suite());
 		suite.addTest(AllValidateTests.suite());
 
 		suite.addTestSuite(FigureCodegenTest.class);
 		suite.addTestSuite(LabelSupportTest.class);
 		suite.addTestSuite(ShapePropertiesTest.class);
 		suite.addTestSuite(FigureLayoutTest.class);
 		suite.addTestSuite(StandaloneMapModeTest.class);
 		suite.addTestSuite(StandalonePluginConverterTest.class);
 		suite.addTestSuite(RTFigureTest.class);
 		suite.addTestSuite(MapModeStrategyTest.class);
 		suite.addTestSuite(ViewmapProducersTest.class);
 		suite.addTestSuite(ToolDefHandocodedImplTest.class);
 		suite.addTestSuite(AuditHandcodedTest.class);		
 		suite.addTestSuite(AuditRulesTest.class);		
 		suite.addTestSuite(ElementInitializerTest.class);
 		suite.addTestSuite(CodegenReconcileTest.class);
 		// though it might be an overkill to check two setups, it should be fast and won't hurt.
 		suite.addTest(feed(TestAllDerivedFeatures.class, sessionSetup, "-SessionSetup"));
 		suite.addTest(feed(TestAllDerivedFeatures.class, sessionSetup2, "-LinksSessionSetup"));
 		
 		suite.addTestSuite(DiagramNodeTest.class);
 		suite.addTestSuite(CompartmentPropertiesTest.class);
 		suite.addTestSuite(NamingStrategyTest.class);
 		suite.addTestSuite(GenModelTransformerBasicRTTest.class);
 		suite.addTestSuite(DiagramEditorTest.class);
 		suite.addTestSuite(LinkChildMetaFeatureNotFromContainerTest.class);
 		suite.addTestSuite(LinkEcoreConstraintsTest.class);
 		suite.addTestSuite(PaletteTest.class);
 
 		suite.addTestSuite(BundleActivationTest.class);
 
 //		suite.addTestSuite(RunTimeModelTransformerTest.class); #113966
 //		suite.addTestSuite(PropertiesTest.class); #113965 
 //		suite.addTestSuite(CanvasTest.class); Nothing there yet
 //		suite.addTestSuite(SpecificRTPropertiesTest.class); #113965
 		
 		suite.addTestSuite(LinkCreationTest.class);
 		suite.addTestSuite(LinkCreationConstraintsTest.class);
 		suite.addTestSuite(MetricRulesTest.class);		
 		suite.addTestSuite(GenFeatureSeqInitializerTest.class);
 		suite.addTestSuite(GenModelGraphAnalyzerTest.class);
 		suite.addTestSuite(EditHelpersTest.class);
 		suite.addTest(feed(ParsersTest.class, new ParsersSetup(false), "-direct"));
 		suite.addTest(feed(ParsersTest.class, new ParsersSetup(true), "-provider"));
 
 		// slowest test goes last
 		suite.addTestSuite(RuntimeCompilationTest.class);
 
 
 		//$JUnit-END$
 		suite.addTest(new CleanupTest("testCleanup") {
 			protected void performCleanup() throws Exception {
 				sessionSetup.cleanup();
 				sessionSetup2.cleanup();
 				setupLinkChildMetaFeatureNotFromContainerTest.cleanup();
 				setupLinkEcoreConstraintsTest.cleanup();
 				setupEditHelpersTest.cleanup();
 				setupBundleActivationTest.cleanup();
 			}
 		});
 		
 		return suite;
 	}
 
 	public static Test feed(Class<? extends TestCase> theClass, TestConfiguration config, String suffix) {
 		return Configurator.feed(theClass, config, theClass.getSimpleName() + suffix);
 	}
 
 	// records our knowledge which test requires which setup
 	public static void populate(Configurator c) {
 		// SessionSetup
 		c.register(DiagramEditorTest.class, SessionSetup.class);
 		c.register(CodegenReconcileTest.class, SessionSetup.class);
 		c.register(HandcodedImplTest.class, SessionSetup.class);
 		c.register(CompartmentPropertiesTest.class, SessionSetup.class);
 		c.register(PaletteTest.class, SessionSetup.class);
 		c.register(TransformToGenModelOperationTest.class, SessionSetup.class);
 		c.register(GenModelTransformerSimpleTest.class, SessionSetup.class);
 		c.register(LabelMappingTransformTest.class, SessionSetup.class);
 		c.register(PaletteTransformationTest.class, SessionSetup.class);
 		c.register(AuditHandcodedTest.class, SessionSetup.class);		
 		c.register(CodegenReconcileTest.class, SessionSetup.class);
 		// Default configuration, TestAllDerivedFeatures also runs for LinksSessionSetup 
 		c.register(TestAllDerivedFeatures.class, SessionSetup.class);
 		c.register(DiagramNodeTest.class, SessionSetup.class);
 		c.register(NamingStrategyTest.class, SessionSetup.class);
 		c.register(GenModelTransformerBasicRTTest.class, SessionSetup.class);
 		c.register(LinkCreationTest.class, SessionSetup.class);
 		// LinksSessionSetup
 		c.register(AuditRulesTest.class, LinksSessionSetup.class);
 		c.register(ElementInitializerTest.class, LinksSessionSetup.class);
 		c.register(LinkCreationConstraintsTest.class, LinksSessionSetup.class);
 		c.register(MetricRulesTest.class, LinksSessionSetup.class);
 		// 
 		c.register(FigureCodegenTest.class, new FigureCodegenSetup());
 		c.register(LabelSupportTest.class, new LabelSupportSetup());
 		c.register(ShapePropertiesTest.class, new ShapePropertiesSetup());
 		c.register(FigureLayoutTest.class, new FigureLayoutSetup());
 		//
 		// Default configuration, ParsersTest also runs for ParsersSetup(false)
 		c.register(ParsersTest.class, new ParsersSetup(true));
 //		suite.addTest(feed(ParsersTest.class, new ParsersSetup(false), "-direct"));
 		//
 		c.register(LinkEcoreConstraintsTest.class, new LinkEcoreConstraintsTest.CustomSetup());
 		c.register(LinkChildMetaFeatureNotFromContainerTest.class, new LinkChildMetaFeatureNotFromContainerTest.CustomSetup());
 		c.register(EditHelpersTest.class, new EditHelpersTest.EditHelpersSessionSetup());
 		c.register(BundleActivationTest.class, new BundleActivationTest.CustomSetup());
 	}
 }
