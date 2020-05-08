 package com.raygroupintl.m.token;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.raygroupintl.m.struct.MRefactorSettings;
 
 public class RoutineBeautifyTest {
 	private static MTFSupply supplyStd95;
 	private static MTFSupply supplyCache;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		supplyStd95 = MTFSupply.getInstance(MVersion.ANSI_STD_95);
 		supplyCache = MTFSupply.getInstance(MVersion.CACHE);
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		supplyStd95 = null;
 		supplyCache = null;
 	}
 
	public void testrefactor(MTFSupply m) {
		MRoutine original = TFCommonTest.getRoutineToken(this.getClass(), "resource/BEAT0SRC.m", m);
		MRoutine source = TFCommonTest.getRoutineToken(this.getClass(), "resource/BEAT0SRC.m", m);
		MRoutine result = TFCommonTest.getRoutineToken(this.getClass(), "resource/BEAT0RST.m", m);
 		
 		source.refactor(new MRefactorSettings());
 		List<MLine> originalLines = original.asList();
 		List<MLine> sourceLines = source.asList();
 		List<MLine> resultLines = result.asList();
 		int n = resultLines.size();
 		Assert.assertEquals( originalLines.size(), resultLines.size());
 		Assert.assertEquals(sourceLines.size(), resultLines.size());
 		for (int i=1; i<n; ++i) {
 			String sourceLineValue = sourceLines.get(i).toValue().toString();
 			String resultLineValue = resultLines.get(i).toValue().toString();
 			Assert.assertEquals(sourceLineValue, resultLineValue);
 			if ((i > 4) && (i < 12)) {
 				Assert.assertFalse(sourceLineValue.equals(originalLines.get(i).toValue().toString()));				
 			}
 		}
 	}
 	
 	@Test
 	public void testrefactor() {
 		testrefactor(supplyCache);
 		testrefactor(supplyStd95);
 	}
 }
