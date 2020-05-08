 package cytoscape.plugin;
 
 import cytoscape.*;
 
 import java.io.*;
 import java.net.URLClassLoader;
 import java.util.*;
 import junit.framework.TestCase;
 import org.jdom.JDOMException;
 
 /**
  * @author skillcoy
  */
 @SuppressWarnings({"JavadocReference"})
 public class PluginManagerTest extends TestCase {
 	private PluginManager mgr;
 	private PluginTracker tracker;
 	private String testUrl;
 	private File transformedXML;
 
 	private static void print(String s) {
 		System.out.println(s);
 	}
 
 	private String getFileUrl() {
 		String FS = "/";
 		String UserDir = System.getProperty("user.dir");
 		if (System.getProperty("os.name").contains("Windows")) {
 			UserDir = UserDir.replaceFirst("\\w:", "");
 			UserDir = UserDir.replaceAll("\\\\", FS);
 		}
 		UserDir = UserDir.replaceFirst("/", "");
 		return "file:///" + UserDir + FS + "testData" + FS + "plugins" + FS;
 	}
 
 	private String cleanFileUrl(String url) {
 		if (System.getProperty("os.name").contains("Windows")) {
 			url = url.replaceFirst("\\w:", "");
 		}
 		return "file://" + url;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		transformedXML = PluginTestXML.transformXML("test_plugin.xml", getFileUrl());
 		testUrl = cleanFileUrl(transformedXML.getAbsolutePath());
 		 
 		PluginManager.setPluginManageDirectory(System.getProperty("java.io.tmpdir"));
 		mgr = PluginManager.getPluginManager();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() {
 		mgr.resetManager();
 		mgr.getPluginManageDirectory().delete();
 		mgr.getPluginManageDirectory().getParentFile().delete();
 		transformedXML.delete();
 		// make sure this isn't set, the webstart tests can set it themselves
 		System.setProperty("javawebstart.version", "");
 	}
 	
 	/**
 	 * Test method for {@link cytoscape.plugin.PluginManager#getPluginManager()}.
 	 */
 	public void testGetPluginManager() {
 		System.out.println("testGetPluginManager");
 		assertNotNull(mgr);
 		assertNotNull(PluginManager.getPluginManager(tracker));
 		assertEquals(mgr, PluginManager.getPluginManager(tracker));
 	}
 
 	public void testGetWebstartPluginManager() throws java.io.IOException {
 		mgr.resetManager();
 		// the manager checks this property to find out if it's webstarted
 		System.setProperty("javawebstart.version", "booya");
 
 		PluginManager.setPluginManageDirectory(System.getProperty("java.io.tmpdir"));
 		mgr = PluginManager.getPluginManager();
 		
 		assertNotNull(mgr);
 		assertTrue(PluginManager.usingWebstartManager());
 		
 		File TempTrackingFile = mgr.pluginTracker.getTrackerFile();
 		assertNotNull(TempTrackingFile);
 		assertTrue(TempTrackingFile.exists());
 		assertTrue(TempTrackingFile.canRead());
 		
 		mgr.resetManager();
 	}
 	
 	public void testDownloadPluginWebstart() throws java.io.IOException, org.jdom.JDOMException, cytoscape.plugin.ManagerException {
 		mgr.resetManager();
 		System.setProperty("javawebstart.version", "booya");
 
 		PluginManager.setPluginManageDirectory(System.getProperty("java.io.tmpdir"));
 		PluginManager mgr = PluginManager.getPluginManager();
 		assertNotNull(mgr);
 		assertTrue(PluginManager.usingWebstartManager());
 		PluginInfo TestObj = (PluginInfo) getSpecificObj(mgr.inquire(testUrl), "goodJarPlugin123", "1.0");
 		DownloadableInfo DLTestObj = mgr.download(TestObj);
 		assertNotNull(DLTestObj);
 		
 		PluginInfo testObjPlugin = (PluginInfo) DLTestObj;
 		for (String f: testObjPlugin.getFileList()) {
 			assertTrue(f.startsWith(mgr.getPluginManageDirectory().getAbsolutePath()));
 		}
 		
 		// can't delete when in webstart
 		try { 
 			mgr.delete(DLTestObj);
 		} catch (cytoscape.plugin.WebstartException wse) {
 			assertNotNull(wse);
 		}
 		mgr.resetManager();
 	}
 	
 	
 	/**
 	 * Test method for
 	 * {@link  cytoscape.plugin.PluginManager#getDownloadables(cytoscape.plugin.PluginTracker.PluginStatus)}.
 	 * TODO
 	 */
 	public void testGetPlugins() {
 		// Not sure how to test this since I can't register anything
 		// w/o a full Cytoscape startup
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 	}
 
 	/**
 	 * Test method for
 	 * {@link cytoscape.plugin.PluginManager#inquire(java.lang.String)}.
 	 */
 	public void testInquireString() throws java.io.IOException,
 			org.jdom.JDOMException {
 		String Url = "http://google.com/x.xml";
 		try {
 			mgr.inquire(Url);
 		} catch (java.io.IOException e) {
 			assertNotNull(e);
 		}
 
 		Url = testUrl;
 		assertNotNull(mgr.inquire(Url));
 		assertEquals(mgr.inquire(Url).size(), 13);
 	}
 
   public void testInquirePartialXML() {
     String Url = getFileUrl() + "partial_plugin_file.xml";
     System.out.println(Url);
     try
       {
       mgr.inquire(Url);
       }
     catch (IOException e)
       {
       fail("Should have gotten a JDOMParseException");
       e.printStackTrace();
       }
     catch (JDOMException e)
       {
       assertNotNull(e);
       e.printStackTrace();
       }
   }
 
 
   /**
 	 * NOT IMPLEMENTED
 	 * Test method for
 	 * {@link cytoscape.plugin.PluginManager#register(cytoscape.plugin.CytoscapePlugin, java.lang.String)}.
 	 */
 	public void testRegister() throws Exception {
 	}
 
 	public void testInstallTheme() throws java.io.IOException,
 		org.jdom.JDOMException, cytoscape.plugin.ManagerException, cytoscape.plugin.WebstartException {
 
 		ThemeInfo TestObj = (ThemeInfo) getSpecificObj(mgr.inquire(testUrl), "goodThemeTest123", "0.5");
 		DownloadableInfo DownloadedObj = mgr.download(TestObj);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 		mgr.install(DownloadedObj);
 		
 		List<DownloadableInfo> Current = mgr.getDownloadables(PluginStatus.CURRENT);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		assertEquals(Current.size(), 1);
 		
 		assertEquals(Current.get(0).getType(), DownloadableType.THEME);
 		assertEquals( ((ThemeInfo)DownloadedObj).getPlugins().size(), 2 );
 		
 		ThemeInfo CurrentRecorded = (ThemeInfo) Current.get(0);
 		assertEquals(CurrentRecorded.getPlugins().size(), ((ThemeInfo)DownloadedObj).getPlugins().size());
 		
 		mgr.delete(DownloadedObj);
 		mgr.delete();
 	}
 	
 	
 	/**
 	 * Test method for {@link cytoscape.plugin.PluginManager#install()}.
 	 */
 	public void testInstallPlugin() throws java.io.IOException,
 			org.jdom.JDOMException, cytoscape.plugin.ManagerException, cytoscape.plugin.WebstartException {
 		PluginInfo TestObj = (PluginInfo)getSpecificObj(mgr.inquire(testUrl),
 				"goodJarPlugin123", "1.0");
 
 		DownloadableInfo DownloadedObj = mgr.download(TestObj, null);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 		mgr.install(DownloadedObj);
 
 		List<DownloadableInfo> Current = mgr.getDownloadables(PluginStatus.CURRENT);
 		
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		assertEquals(Current.size(), 1);
 
 		PluginInfo APlugin = (PluginInfo) Current.get(0);
 		assertEquals(APlugin.getFileList().size(), 1);
 
 		for (String f: APlugin.getFileList()) {
 			assertTrue( (new File(f)).exists() );
 		}
 
 		mgr.delete(DownloadedObj);
 		mgr.delete();
 
 		PluginInfo plugin = (PluginInfo) DownloadedObj;
 		for (String f: plugin.getFileList()) {
 			assertFalse( (new File(f)).exists() );
 		}
 	}
 
 	
 	public void testInstallPluginZip() throws Exception {
 		PluginInfo TestObj = (PluginInfo)getSpecificObj(mgr.inquire(testUrl), "goodZIPPlugin777", "0.45");
 		
 		DownloadableInfo DownloadedObj = mgr.download(TestObj, null);
 		PluginInfo plugin = (PluginInfo) DownloadedObj;
 		
 		for (String f: plugin.getFileList()) {
 			assertTrue( (new File(f)).exists() );
 		}
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 		
 		mgr.install(mgr.getDownloadables(PluginStatus.INSTALL).get(0));
 		
 		String ParentDir = (new File(TestObj.getFileList().get(0)).getParent());
 		List<String> TestFileList = TestObj.getFileList();
 		for(String f: TestFileList) {
 			assertTrue( f.startsWith(ParentDir));		
 		}
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		
 		mgr.delete(DownloadedObj);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 1);
 		mgr.delete();
 
 		for (String f: TestFileList) {
 			assertFalse( (new File(f)).exists() );
 		}
 	}
 	
 	
 	public void testInstallIncorrectFileType() throws ManagerException, org.jdom.JDOMException {
 		PluginInfo TestObj = null;
 		try {
 			TestObj = (PluginInfo)getSpecificObj(mgr.inquire(testUrl),
 				"badFileType123", "1.0");
 		} catch (java.io.IOException ioe) {
 			ioe.printStackTrace();
 			fail();
 		}
 		
 		// the real plugin is actually a jar file, this should fail
 		TestObj.setFiletype(PluginInfo.FileType.ZIP);
 		
 		try {
 			mgr.download(TestObj, null);
 			fail(); // if it manages to download it means the test failed
 		} catch (java.io.IOException ioe) {
 			// nothing, this is exactly what should happen
 		} 
 
 	}
 	
 	/**
 	 * Test method for
 	 * {@link cytoscape.plugin.PluginManager#delete(cytoscape.plugin.PluginInfo)}.
 	 */
 	public void testDeletePluginInfo() throws java.io.IOException,
 			org.jdom.JDOMException, cytoscape.plugin.ManagerException,  cytoscape.plugin.WebstartException  {
 
 		List<DownloadableInfo> Downloadables = mgr.inquire(testUrl);
 		
 		PluginInfo TestObj = (PluginInfo) Downloadables.get(0);
 
 		//File Downloaded = mgr.download(TestObj, null);
 		DownloadableInfo Downloaded = mgr.download(TestObj, null);
 		
 		//assertTrue(Downloaded.exists());
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 		mgr.install(Downloaded);
 		//List<PluginInfo> Current = mgr.getDownloadables(PluginStatus.CURRENT);
 		List<DownloadableInfo> Current = mgr.getDownloadables(PluginStatus.CURRENT);
 		
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		assertEquals(Current.size(), 1);
 		
 		PluginInfo APlugin = (PluginInfo) Current.get(0);
 		assertEquals(APlugin.getFileList().size(), 1);
 
 		File InstalledPlugin = new File(APlugin.getFileList().get(0));
 		assertTrue(InstalledPlugin.exists());
 
 		mgr.delete(Current.get(0));
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 1);
 		mgr.delete();
 		assertFalse(InstalledPlugin.exists());
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.PluginManager#delete()}.
 	 */
 	public void testDeletePlugin() throws java.io.IOException,
 			org.jdom.JDOMException, cytoscape.plugin.ManagerException, cytoscape.plugin.WebstartException  {
 		List<DownloadableInfo> Downloadables = mgr.inquire(testUrl);
 		
 		PluginInfo TestObj = (PluginInfo) Downloadables.get(0);
 		
 		DownloadableInfo Downloaded = mgr.download(TestObj, null);
 
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		mgr.install(Downloaded);
 
 		//assertEquals(mgr.getPluginManageDirectory().list().length, 2);
 		
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 
 		// set for deletion
 		mgr.delete(mgr.getDownloadables(PluginStatus.CURRENT).get(0));
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 1);
 
 		List<DownloadableInfo> DeleteList = mgr.getDownloadables(PluginStatus.DELETE);
 		
 		// delete
 		mgr.delete();
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 
 		PluginInfo Plugin = (PluginInfo) DeleteList.get(0);
 		for (String FileName : Plugin.getFileList()) {
 			assertFalse((new File(FileName)).exists());
 		}
 		
 	// only the xml file should be left in this directory
 	//assertEquals(mgr.getPluginManageDirectory().list().length, 1);
 	}
 	
 	public void testDeleteTheme() throws java.io.IOException,
 	org.jdom.JDOMException, cytoscape.plugin.ManagerException, cytoscape.plugin.WebstartException  {
 		
 		ThemeInfo TestObj = (ThemeInfo) getSpecificObj(mgr.inquire(testUrl), "goodThemeTest123", "0.5");
 		
 		DownloadableInfo Downloaded = mgr.download(TestObj);
 		TestObj = (ThemeInfo) Downloaded;
 		assertEquals(TestObj.getPlugins().size(), 2);
 		
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		mgr.install(Downloaded);
 		
 		for (String x: mgr.getPluginManageDirectory().list()) {
 			System.out.println("**" + x);
 		}
 		
 		//assertEquals(mgr.getPluginManageDirectory().list().length, 2);
 		
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 
 		// set for deletion
 		mgr.delete(mgr.getDownloadables(PluginStatus.CURRENT).get(0));
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 1);
 
 		List<DownloadableInfo> DeleteList = mgr.getDownloadables(PluginStatus.DELETE);
 		
 		// delete
 		mgr.delete();
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 
 		// only the xml file should be left in this directory
 		//assertEquals(mgr.getPluginManageDirectory().list().length, 1);
 	}
 	
 
 	public void testFindThemeUpdates() throws java.io.IOException,
 	org.jdom.JDOMException, cytoscape.plugin.ManagerException,  cytoscape.plugin.WebstartException  {
 	
 		ThemeInfo TestObj = (ThemeInfo) getSpecificObj(mgr.inquire(testUrl), "goodThemeTest123", "0.5");
 		
 		DownloadableInfo Downloaded = mgr.download(TestObj);
 		assertNotNull(Downloaded);
 		mgr.install(Downloaded);
 		
 		TestObj = (ThemeInfo) Downloaded;
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		
 		List<DownloadableInfo> Updatable = mgr.findUpdates(mgr.getDownloadables(PluginStatus.CURRENT).get(0));
 		assertEquals(Updatable.size(), 1);
 
 		mgr.delete(Downloaded);
 		mgr.delete();
 	}
 	
 	/**
 	 * Test method for
 	 * {@link cytoscape.plugin.PluginManager#findUpdates(cytoscape.plugin.PluginInfo)}.
 	 */
 	public void testFindPluginUpdates() throws java.io.IOException,
 			org.jdom.JDOMException, cytoscape.plugin.ManagerException,  cytoscape.plugin.WebstartException  {
 		PluginInfo GoodJar = (PluginInfo) getSpecificObj(mgr.inquire(testUrl),
 				"goodJarPlugin123", "1.0");
 		
 		DownloadableInfo Downloaded = mgr.download(GoodJar, null);
 		
 		assertNotNull(Downloaded);
 		mgr.install(Downloaded);
 
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		List<DownloadableInfo> Updatable = mgr.findUpdates(mgr.getDownloadables(
 				PluginStatus.CURRENT).get(0));
 		assertEquals(Updatable.size(), 1);
 		
 		mgr.delete(Downloaded);
 		mgr.delete();
 	}
 
 	
 	public void testUpdateTheme() throws Exception {
 		ThemeInfo TestObj = (ThemeInfo) getSpecificObj(mgr.inquire(testUrl), "goodThemeTest123", "0.5");
 		
 		DownloadableInfo Downloaded = mgr.download(TestObj);
 		assertNotNull(Downloaded);
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		mgr.install(Downloaded);
 		
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 
 		List<DownloadableInfo> Updatable = mgr.findUpdates(mgr.getDownloadables(PluginStatus.CURRENT).get(0));
 		assertEquals(Updatable.size(), 1);
 
 		ThemeInfo TestUpdate = (ThemeInfo) Updatable.get(0);
 		
 		mgr.update(mgr.getDownloadables(PluginStatus.CURRENT).get(0), TestUpdate);
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 
 		mgr.delete();
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 
 		mgr.install(TestUpdate);
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 
 		mgr.delete( mgr.getDownloadables(PluginStatus.CURRENT).get(0) );
 		mgr.delete();
 	}
 	
 	/**
 	 * Test method for
 	 * {@link cytoscape.plugin.PluginManager#update(cytoscape.plugin.PluginInfo, cytoscape.plugin.PluginInfo)}.
 	 */
 	public void testUpdatePlugin() throws Exception {
 		PluginInfo GoodJar = (PluginInfo) getSpecificObj(mgr.inquire(testUrl),
 				"goodJarPlugin123", "1.0");
 		assertNotNull(mgr.download(GoodJar, null));
 		mgr.install(GoodJar);
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 
 		List<DownloadableInfo> Updatable = mgr.findUpdates(mgr.getDownloadables(
 				PluginStatus.CURRENT).get(0));
 		assertEquals(Updatable.size(), 1);
 
 		PluginInfo New = (PluginInfo) Updatable.get(0);
 		//New.setObjectUrl(getFileUrl() + New.getObjectUrl());
 
 		PluginInfo Current = (PluginInfo) mgr.getDownloadables(PluginStatus.CURRENT).get(0);
 		//	 update sets the old for deletion, new for installation
 		mgr.update(Current, New); 
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 
 		mgr.delete();
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 
 		mgr.install(New);
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 
 		PluginInfo Installed = (PluginInfo) mgr.getDownloadables(PluginStatus.CURRENT).get(0);
 		for (String f: Installed.getFileList()) {
 			print("Installed file: " + f);
 			assertTrue( (new File(f)).exists() );
 		}
 		
 		mgr.delete( mgr.getDownloadables(PluginStatus.CURRENT).get(0) );
 		mgr.delete();
 	}
 
 	
 	public void testDownloadGoodTheme() 
 		throws java.io.IOException, org.jdom.JDOMException, cytoscape.plugin.ManagerException,  cytoscape.plugin.WebstartException  {
 		
 		ThemeInfo GoodTheme = (ThemeInfo) getSpecificObj(mgr.inquire(testUrl), "goodThemeTest123", "0.5");
 		assertEquals(GoodTheme.getCategory(), Category.THEME.getCategoryText());
 		
 		DownloadableInfo GoodDL = mgr.download(GoodTheme);
 		assertNotNull(GoodDL);
 
 		GoodTheme = (ThemeInfo) GoodDL;
 		assertEquals(GoodTheme.getPlugins().size(), 2);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).get(0).getObjectVersion(), GoodDL.getObjectVersion());
 		
 		mgr.delete(GoodDL);
 		mgr.delete();
 	}
 	
 	/**
 	 * Test method for
 	 * {@link cytoscape.plugin.PluginManager#download(cytoscape.plugin.PluginInfo)}.
 	 */
 	public void testDownloadGoodPlugin() throws java.io.IOException,
 			org.jdom.JDOMException, cytoscape.plugin.ManagerException,  cytoscape.plugin.WebstartException  {
 		PluginInfo GoodJar = (PluginInfo) getSpecificObj(mgr.inquire(testUrl),
 				"goodJarPlugin123", "1.0");
 		DownloadableInfo GoodDL = mgr.download(GoodJar);
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 
 		DownloadableInfo CurrentInstall = mgr.getDownloadables(PluginStatus.INSTALL).get(0);
 		PluginInfo CurrentPlugin = (PluginInfo) CurrentInstall;
 		assertNotNull(CurrentPlugin.getLicenseText()); 
 		assertEquals(CurrentPlugin.getFileList().size(), 1);
 
 		mgr.delete(GoodDL);
 		mgr.delete();
 	}
 
 	/**
 	 * Test method for
 	 * {@link cytoscape.plugin.PluginManager#download(cytoscape.plugin.PluginInfo)}.
 	 * files are only bad if they fail to have an attribute Cytoscape-Plugin in
 	 * the manifest
 	 */
 	public void testDownloadBadPlugin() throws java.io.IOException,
 			org.jdom.JDOMException,  cytoscape.plugin.WebstartException  {
 		PluginInfo BadJar = (PluginInfo) getSpecificObj(mgr.inquire(testUrl),
 				"badJarPlugin123", "0.3");
 
 		try {
 			mgr.download(BadJar, null);
 		} catch (ManagerException E) {
 			assertNotNull(E);
 			assertTrue(E.getMessage().contains("Cytoscape-Plugin"));
 		}
 	}
 
 	/**
 	 * This test will test the download of duplicate plugins/themes and the download of plugins
 	 * that are already loaded in a theme.
 	 * 
 	 * @throws Exception
 	 */
 	public void testDuplicates() throws Exception {
 		ThemeInfo GoodTheme = (ThemeInfo) getSpecificObj(mgr.inquire(testUrl), "goodThemeTest123", "0.5");
 
 		DownloadableInfo ThemeDL = mgr.download(GoodTheme);
 		assertNotNull(ThemeDL);
 		GoodTheme = (ThemeInfo) ThemeDL;
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 
 		PluginInfo GoodPlugin = (PluginInfo) getSpecificObj(mgr.inquire(testUrl), "goodJarPlugin123", "2.1");
 		try {
 			DownloadableInfo PluginDL = mgr.download(GoodPlugin);
 		} catch (ManagerException me) {
 			// this is the expected result
 		}
 		// should not set a plugin to install if it's already there in a theme
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 1);
 
 		mgr.install();
 		assertEquals(mgr.getDownloadables(PluginStatus.INSTALL).size(), 0);
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 1);
 		assertEquals(mgr.getDownloadables(PluginStatus.DELETE).size(), 0);
 	}
 
 	public void testMinorCorruptedTrackerFile() {
 		mgr.resetManager();
 		PluginManager.setPluginManageDirectory(System.getProperty("java.io.tmpdir"));
 		File file = new File("testData/plugins/track_plugins_c1.xml");
 		file = copyFileToTempDir(file);
 
 		mgr = PluginManager.getPluginManager();
 		assertEquals(mgr.pluginTracker.getTrackerFile().getAbsolutePath(), file.getAbsolutePath());
 		 // errors that aren't major failures don't show up until you've tried to read the file
 		assertTrue(mgr.getDownloadables(PluginStatus.CURRENT).size() > 0);
 		assertEquals(mgr.pluginTracker.getTotalCorruptedElements(), 1);
 		assertEquals(mgr.getLoadingErrors().size(), 1);
 		assertTrue(mgr.getLoadingErrors().get(0).getClass().equals(TrackerException.class));
 		
 		file.delete();
 	}
 	
 	public void testMajorCorruptedTrackerFile() {
 		mgr.resetManager();
 		PluginManager.setPluginManageDirectory(System.getProperty("java.io.tmpdir"));
 		File file = new File("testData/plugins/track_plugins_c2.xml");
 		file = copyFileToTempDir(file);
 		mgr = PluginManager.getPluginManager();
 		assertNotNull(mgr);
 		
 		assertEquals(mgr.pluginTracker.getTrackerFile().getAbsolutePath(), file.getAbsolutePath());
 		assertEquals(mgr.getDownloadables(PluginStatus.CURRENT).size(), 0);
 		List<Throwable> LoadingErrors = mgr.getLoadingErrors();
 		assertEquals(LoadingErrors.size(), 1);
 		assertTrue(LoadingErrors.get(0).getClass().equals(TrackerException.class));
 		
 		mgr.clearErrorList();
 		assertEquals(mgr.getLoadingErrors().size(), 0);
 		
 		file.delete();
 	}
 	
 	/*--------------------------------------------------------------------*/
 	private File copyFileToTempDir(File fileToCopy) {
 		File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + 
 				new CytoscapeVersion().getMajorVersion());
 		File tempFile = new File(tempDir, "track_plugins.xml");
 		
 		assertTrue(fileToCopy.exists());
 		
 		try {
 			BufferedReader reader = new BufferedReader( new FileReader(fileToCopy) );
            try {
                BufferedWriter writer = new BufferedWriter( new FileWriter(tempFile) );
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                    }
                }
                finally {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return tempFile;
 	}
 
 	
 	private ThemeInfo setUpCorrectUrls(ThemeInfo info) {
 		for (PluginInfo plugin: info.getPlugins()) {
 			plugin.setObjectUrl(getFileUrl() + plugin.getObjectUrl());
 		}
 		return info;
 	}
 	
 	private DownloadableInfo getSpecificObj(List<DownloadableInfo> AllInfo, String Id, String Version) {
 		for (DownloadableInfo Current : AllInfo) {
 			if (Current.getID().equals(Id)
 					&& Current.getObjectVersion().equals(Version)) {
 				return Current;
 			}
 		}
 		return null;
 	}
 	
 
 	// this won't work causes ExceptionInitializerError in the CytoscapePlugin
 	private class MyPlugin extends CytoscapePlugin {
 		public MyPlugin() {
 			System.out.println("MyPlugin instantiated");
 		}
 	}
 	
 }
