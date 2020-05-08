 package com.github.seqware.queryengine.model.test;
 
 import org.junit.Test;
 
 import com.github.seqware.queryengine.factory.CreateUpdateManager;
 import com.github.seqware.queryengine.factory.SWQEFactory;
 import com.github.seqware.queryengine.model.Feature;
 import com.github.seqware.queryengine.model.FeatureSet;
 
 public class TableSetupTest {
 	static FeatureSet aSet;
 	static Feature a1;
 	@Test
	public static void setupTest(){
 		CreateUpdateManager manager = SWQEFactory.getModelManager();
 		aSet = manager.buildFeatureSet().setReference(manager.buildReference().setName("DummyReference").build()).build();
 		a1 = manager.buildFeature().setSeqid("chr1").setStart(100).setStop(101).setScore(100.0).setStrand(Feature.Strand.NEGATIVE).setSource("human").setPhase(".").build();
 		aSet.add(a1);
 		manager.flush();
 	}
 }
