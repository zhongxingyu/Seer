 package com.andexp.skinmixer.path;
 
 import android.content.Context;
 import android.test.InstrumentationTestCase;
 
 
 public class SkinListerTest extends InstrumentationTestCase{
 	private TestingAssets assets;
 	private Context mContext;
 	private SkinLister lister;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		mContext = getInstrumentation().getContext();
 		
 		assets = new TestingAssets(mContext);
 		assets.extract();
 		
 		lister = SkinLister.getInstance();
 	}
 
 	public void testSkinListerNotNull() {
 		assertNotNull(SkinLister.getInstance());
 	}
 	
 	public void testSkinListerSingleton() {
 		assertSame(lister, SkinLister.getInstance());
 	}
 	
 	public void testSuperClockListNotNull(){
 		assertNotNull(lister.getSuperClockSkinPathList());
 	}
 	
 	public void testSuperClockListNotEmpty(){
 		assertTrue(lister.getSuperClockSkinPathList().size() >= 1);
 	}
 	
	public void testListNotAddingToItself(){
		assertEquals(lister.getSuperClockSkinPathList().size(), lister.getSuperClockSkinPathList().size());
	}
	
 	public void testSuperClockListCorrectlyFilled(){
 		assertEquals(3, lister.getSuperClockSkinPathList().size());
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		assets.delete();
 	}
 }
