 package com.github.seqware.queryengine.model.test;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.Iterator;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.github.seqware.queryengine.backInterfaces.StorageInterface;
 import com.github.seqware.queryengine.factory.CreateUpdateManager;
 import com.github.seqware.queryengine.factory.SWQEFactory;
 import com.github.seqware.queryengine.model.Atom;
 import com.github.seqware.queryengine.model.Feature;
 import com.github.seqware.queryengine.model.FeatureSet;
 import com.github.seqware.queryengine.plugins.PluginInterface;
 import com.github.seqware.queryengine.plugins.contribs.OverlappingMutationsAggregationPlugin;
 import com.github.seqware.queryengine.plugins.plugins.FeatureSetCountPlugin;
 import com.github.seqware.queryengine.plugins.plugins.FeaturesByAttributesPlugin;
 import com.github.seqware.queryengine.system.importers.FeatureImporter;
 import com.github.seqware.queryengine.util.FSGID;
 import com.github.seqware.queryengine.util.SGID;
 import com.github.seqware.queryengine.util.SeqWareIterable;
 import com.github.seqware.queryengine.model.QueryFuture;
 import com.github.seqware.queryengine.model.impl.FeatureList;
 import com.github.seqware.queryengine.impl.MRHBaseModelManager;
 import com.github.seqware.queryengine.impl.SimplePersistentBackEnd;
 import com.github.seqware.queryengine.kernel.RPNStack;
 import com.github.seqware.queryengine.kernel.RPNStack.Constant;
 import com.github.seqware.queryengine.kernel.RPNStack.FeatureAttribute;
 import com.github.seqware.queryengine.kernel.RPNStack.Operation;
 
 public class TableSetupTest {
 	static FeatureSet aSet, bSet;
 	static Feature a1,a2,a3;
 	static File testVCFFile = null;
 	static File testSecondVCFFile = null;
 	static String refName = null;
 	static String refName2 = null;
     private Configuration config;
     
 	@Test
 	//this will reset all the tables
 	public void tearDownBackend() throws IOException{
         config = HBaseConfiguration.create();
 		try {
 			HBaseAdmin hba = new HBaseAdmin(config);
 			hba.disableTables("b.*");
 			hba.deleteTables("b.*");
 		} catch (MasterNotRunningException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ZooKeeperConnectionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 //	@Test
 	public void setupTest(){
 		SWQEFactory.getStorage().clearStorage();
 		CreateUpdateManager manager = SWQEFactory.getModelManager();
 		aSet = manager.buildFeatureSet().setReference(manager.buildReference().setName("DummyReference").build()).build();
 		a1 = manager.buildFeature().setSeqid("chr1").setStart(100).setType("type1").setStop(101).setScore(100.0).setStrand(Feature.Strand.NEGATIVE).setSource("human").setPhase(".").build();
 		a2 = manager.buildFeature().setSeqid("chr1").setStart(101).setType("type2").setStop(102).setScore(100.0).setStrand(Feature.Strand.NEGATIVE).setSource("human").setPhase(".").build();
 		aSet.add(a1);
 		aSet.add(a2);
 		manager.flush();
 	}
 	
 //	@Test
 	//Test some implemented plugin that is working
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
 //        Assert.assertTrue("Query results wrong, expected 1 and found " + count, count == 1);
     }
 
 //	@Test
 	//loop through hbase table to retrieve feature set count
 	public void throughFeatureSets(){
 
 	}
 	
 	@Test
 	//Setup variables for importing vcf
 	public void setuptestVCFImport(){
 		String curDir = System.getProperty("user.dir");
         SecureRandom random = new SecureRandom();
         refName = "Random_ref_" + new BigInteger(20, random).toString(32);
         refName2 = "Random_ref_" + new BigInteger(20, random).toString(32);
         testVCFFile = new File(curDir + "/src/test/resources/com/github/seqware/queryengine/system/FeatureImporter/test.vcf");
        testSecondVCFFile = new File(curDir + "/src/test/resources/com/github/seqware/queryengine/system/FeatureImporter/consequences_annoated.vcf");
 	}
 	
 	@Test
 	//This imports the features from a vcf file into HBase
 	public void testVCFImport(){
         SGID main = FeatureImporter.naiveRun(new String[]{"VCFVariantImportWorker", "1", "false", refName, testVCFFile.getAbsolutePath()});        
         FeatureSet fSet = SWQEFactory.getQueryInterface().getLatestAtomBySGID(main, FeatureSet.class);
         
         CreateUpdateManager manager = SWQEFactory.getModelManager();
         Iterator<Feature> fIter = fSet.getFeatures();
         
         aSet = manager.buildFeatureSet().setReference(fSet.getReference()).build();
         while(fIter.hasNext()){
         	aSet.add(fIter.next());
         	
         }
 		manager.flush();
 	}
 	
 	@Test
 	//This imports a second vcf File into HBase
 	public void testSecondVCFImport(){
         SGID main = FeatureImporter.naiveRun(new String[]{"VCFVariantImportWorker", "1", "false", refName2, testSecondVCFFile.getAbsolutePath()});        
         FeatureSet fSet = SWQEFactory.getQueryInterface().getLatestAtomBySGID(main, FeatureSet.class);
         
         CreateUpdateManager manager = SWQEFactory.getModelManager();
         Iterator<Feature> fIter = fSet.getFeatures();
         
         bSet = manager.buildFeatureSet().setReference(fSet.getReference()).build();
         while(fIter.hasNext()){
         	bSet.add(fIter.next());
         }
 		manager.flush();
 	}
 	
 	@Test
 	//	loop through hbase table to retrieve features in feature sets
 	public void storageAndRetrieval(){
 		SWQEFactory.getModelManager();
 		StorageInterface storage = SWQEFactory.getStorage();
 		FeatureSet atomBySGID = SWQEFactory.getQueryInterface().getAtomBySGID(FeatureSet.class, aSet.getSGID());
 		for (Feature f : atomBySGID){
 			FSGID fsgid = (FSGID) f.getSGID();
 			
 			System.out.println(fsgid.getRowKey());
 		}
 	}
 	
 //  @Test
     //Write a Test the OverlapMutationsAggregationPlugin plugin
 	public void testOLapPlugin(){
 		Class<? extends PluginInterface> arbPlugin;
 		arbPlugin = OverlappingMutationsAggregationPlugin.class;
 		SWQEFactory.getQueryInterface().getFeaturesByPlugin(0, arbPlugin, null, null);
 	}
 	
 }
