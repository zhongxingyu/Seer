 package com.andexp.skinmixer;
 
 import java.io.IOException;
 
 import android.content.Context;
 import android.test.InstrumentationTestCase;
 
 public class LocalAssetsTest extends InstrumentationTestCase{
	private final int SYSTEM_ASSETS_FILE_NUMBER=3;
	private final int MY_ASSETS_FILE_NUMBER = 3;
 	public static String[] SKIN_DIRECTORY_ZIP = new String[]{"scskins.zip","skins.zip"};
 	public static String[] SUPER_SKINS = new String[]{"bordering-ice-red", "golden", "icsphoenix"};
 	public static String[] CLASSIC_SKINS = new String[]{"kawaplus","sunburn"};
 	Context localContext;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		localContext = getInstrumentation().getContext();
 	}
 	
 	public void testAssetsTestFilesNumber() {
 		String testList[] = getLocalAssetsList();
		assertEquals(SYSTEM_ASSETS_FILE_NUMBER + MY_ASSETS_FILE_NUMBER, testList.length);
 	}
 
 	private String[] getLocalAssetsList() {
 		try {
 			return localContext.getAssets().list("");
 		} catch (IOException e) {
 			fail("Error accessing Assets list : "+e.getMessage());
 		}
 		return new String[]{""};
 	}
 	
 	public void testAssetsTestFilesPresent() {
 		try {
 			for (String fileString: SKIN_DIRECTORY_ZIP) {
 				assertNotNull(localContext.getAssets().open(fileString));
 			}
 		} catch (IOException e) {
 			fail("Error accessing Assets list : "+e.getMessage());
 		}
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 }
