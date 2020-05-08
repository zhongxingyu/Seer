 package com.github.seqware.queryengine.model.test;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.github.seqware.queryengine.factory.CreateUpdateManager;
 import com.github.seqware.queryengine.factory.SWQEFactory;
 import com.github.seqware.queryengine.model.Feature;
 import com.github.seqware.queryengine.model.FeatureSet;
 import com.github.seqware.queryengine.plugins.PluginInterface;
 import com.github.seqware.queryengine.plugins.plugins.FeatureSetCountPlugin;
 import com.github.seqware.queryengine.plugins.plugins.FeaturesByAttributesPlugin;
 import com.github.seqware.queryengine.system.importers.FeatureImporter;
 import com.github.seqware.queryengine.util.SGID;
 import com.github.seqware.queryengine.model.QueryFuture;
 import com.github.seqware.queryengine.impl.MRHBaseModelManager;
 import com.github.seqware.queryengine.impl.SimplePersistentBackEnd;
 import com.github.seqware.queryengine.kernel.RPNStack;
 import com.github.seqware.queryengine.kernel.RPNStack.Constant;
 import com.github.seqware.queryengine.kernel.RPNStack.FeatureAttribute;
 import com.github.seqware.queryengine.kernel.RPNStack.Operation;
 
 public class TableSetupTest {
 	static FeatureSet aSet;
 	static Feature a1;
 	static File testVCFFile = null;
 	static String randomRef = null;
 	@Test
 	public void setupTest(){
 		CreateUpdateManager manager = SWQEFactory.getModelManager();
 		aSet = manager.buildFeatureSet().setReference(manager.buildReference().setName("DummyReference").build()).build();
		a1 = manager.buildFeature().setSeqid("chr1").setStart(100).setStop(101).setScore(100.0).setStrand(Feature.Strand.NEGATIVE).setSource("human").setPhase(".").build();
 		aSet.add(a1);
 		manager.flush();
 	}
 	
 	@Test
     public void testInstallAndRunArbitraryPlugin() {
         Class<? extends PluginInterface> arbitraryPlugin;
         // only use the M/R plugin for this test if using MR
         if (SWQEFactory.getModelManager() instanceof MRHBaseModelManager) {
             // pretend that the included com.github.seqware.queryengine.plugins.hbasemr.MRFeaturesByAttributesPlugin is an external plug-in
             arbitraryPlugin = FeaturesByAttributesPlugin.class;
         } else {
             // pretend the equivalent for a non-HBase back-end
             arbitraryPlugin = FeaturesByAttributesPlugin.class;
         }
         // get a FeatureSet from the back-end
         QueryFuture<FeatureSet> future = SWQEFactory.getQueryInterface().getFeaturesByPlugin(0, arbitraryPlugin, null, aSet, new RPNStack(
                 new Constant("type1"), new FeatureAttribute("type"), Operation.EQUAL));
         // check that Features are present match
         FeatureSet result = future.get();
         for (Feature f : result) {
             Assert.assertTrue(f.getType().equals("type1"));
         }
         int count = (int) result.getCount();
         Assert.assertTrue("Query results wrong, expected 1 and found " + count, count == 1);
     }
 	
 //	@Test
 //	public void testVCFImport(){
 //        SecureRandom random = new SecureRandom();
 //        SWQEFactory.getSerialization();
 //		CreateUpdateManager manager = SWQEFactory.getModelManager();
 //		randomRef = "Random_ref_" + new BigInteger(20, random).toString(32);
 //		testVCFFile = new File("/src/test/resources/com/github/seqware/queryengine/system/FeatureImporter/test.vcf");
 //		SGID main = FeatureImporter.naiveRun(new String[]{"VCFVariantImportWorker", "1", "false", randomRef, testVCFFile.getAbsolutePath()});        
 //        FeatureSet fSet = SWQEFactory.getQueryInterface().getLatestAtomBySGID(main, FeatureSet.class);
 //        
 //	}
 }
