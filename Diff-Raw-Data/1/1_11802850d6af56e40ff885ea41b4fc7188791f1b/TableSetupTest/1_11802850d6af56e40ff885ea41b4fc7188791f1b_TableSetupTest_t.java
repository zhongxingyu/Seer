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
 	static FeatureSet aSet;
 	static Feature a1,a2,a3;
 	static File testVCFFile = null;
 	static String refName = null;
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
 	
 //  @Test
     //Write a Test the OverlapMutationsAggregationPlugin plugin
 	public void testOLapPlugin(){
 		Class<? extends PluginInterface> arbPlugin;
 		arbPlugin = OverlappingMutationsAggregationPlugin.class;
 		
 	}
 
 //	@Test
 	//loop through hbase table to retrieve feature set count
 	public void throughFeatureSets(){
 
 	}
 	
 	@Test
 	//Setup variables for importing vcf
 	public void setuptestVCFImport(){
         SecureRandom random = new SecureRandom();
 		String curDir = System.getProperty("user.dir");
         refName = "Random_ref_" + new BigInteger(20, random).toString(32);
         testVCFFile = new File(curDir + "/src/test/resources/com/github/seqware/queryengine/system/FeatureImporter/test.vcf");
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
 	//	loop through hbase table to retrieve features in feature sets
 	public void storageAndRetrieval(){
 		StorageInterface storage = SWQEFactory.getStorage();
 		Iterator<SGID> atomIter = storage.getAllAtoms().iterator();
 		Iterator<FeatureList> flIter = storage.getAllFeatureListsForFeatureSet(aSet).iterator();
 		Iterator<Feature> fIter;
 		System.out.println(flIter.hasNext());
 		while (flIter.hasNext()){
 			fIter = flIter.next().getFeatures().iterator();
 			while (fIter.hasNext()){
 				System.out.println(fIter.next().getDisplayName());
 			}
 		}
 	}
 	
 
 }
