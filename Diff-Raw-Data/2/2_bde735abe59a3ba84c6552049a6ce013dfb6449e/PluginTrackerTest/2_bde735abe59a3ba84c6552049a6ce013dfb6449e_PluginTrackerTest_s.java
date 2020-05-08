 /**
  * 
  */
 package cytoscape.plugin;
 
 import cytoscape.plugin.PluginStatus;
 
 import java.io.*;
 import org.jdom.*;
 import org.jdom.input.*;
 import java.util.*;
 import junit.framework.TestCase;
 
 /**
  * @author skillcoy
  *
  */
 public class PluginTrackerTest extends TestCase {
 	private	SAXBuilder builder;
 
 	private Document xmlDoc;
 	private PluginTracker tracker;
 	private String fileName = "test_tracker.xml";
 	private File tmpDir;
 	
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		builder = new SAXBuilder(false);
 		tmpDir = new File(System.getProperty("java.io.tmpdir"));
 		tracker = new PluginTracker(tmpDir, fileName);
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		tracker.delete();
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.PluginTracker#PluginTracker()}.
 	 */
 	public void testPluginTracker() throws Exception {
 		Document Doc = getDoc();
 		assertNotNull(Doc);
 		
 		assertEquals(Doc.getRootElement().getName(), "CytoscapePlugin");
 		assertEquals(Doc.getRootElement().getChildren().size(), 3);
 		
 		assertNotNull(Doc.getRootElement().getChild("CurrentPlugins"));
 		assertNotNull(Doc.getRootElement().getChild("InstallPlugins"));
 		assertNotNull(Doc.getRootElement().getChild("DeletePlugins"));
 		
 		assertEquals(Doc.getRootElement().getChild("CurrentPlugins").getChildren().size(), 0);
 		assertEquals(Doc.getRootElement().getChild("InstallPlugins").getChildren().size(), 0);
 		assertEquals(Doc.getRootElement().getChild("DeletePlugins").getChildren().size(), 0);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.PluginTracker#getListByStatus(cytoscape.plugin.PluginTracker.PluginStatus)}.
 	 */
 	public void testGetListByStatus() throws Exception {
 		tracker.addPlugin(getInfoObj(), PluginStatus.CURRENT);
 		
 		assertNotNull(tracker.getListByStatus(PluginStatus.CURRENT));
 		assertEquals(tracker.getListByStatus(PluginStatus.CURRENT).size(), 1);
 
 		// lets just check with the xml doc itself to be sure
 		Document Doc = getDoc();
 		Element Current = Doc.getRootElement().getChild("CurrentPlugins");
 		assertEquals(Current.getChildren().size(), 1);
 		assertEquals(Doc.getRootElement().getChild("InstallPlugins").getChildren().size(), 0);
 		assertEquals(Doc.getRootElement().getChild("DeletePlugins").getChildren().size(), 0);
 	}
 
 	/**
 	 * Test method for {@link cytoscape.plugin.PluginTracker#addPlugin(cytoscape.plugin.PluginInfo, cytoscape.plugin.PluginTracker.PluginStatus)}.
 	 */
 	public void testAddPlugin() throws Exception {
 		tracker.addPlugin(getInfoObj(), PluginStatus.CURRENT);
 		assertEquals(tracker.getListByStatus(PluginStatus.CURRENT).size(), 1);
 		
 		PluginInfo obj = getInfoObj();
 		obj.setName("myInstallTest");
 		obj.setDownloadUrl("http://booya.com/foo.xml");
 		tracker.addPlugin(obj, PluginStatus.INSTALL);
 		
 		assertEquals(tracker.getListByStatus(PluginStatus.INSTALL).size(), 1);
 		
 		// changing the name of the object will not change the object if
 		// the id/projurl stay the same
 		obj.setName("mySecondInstallTest");
 		tracker.addPlugin(obj, PluginStatus.INSTALL);
 		assertEquals(tracker.getListByStatus(PluginStatus.INSTALL).size(), 1);
 		
 		// whole new object will get added though
 		PluginInfo newObj = new PluginInfo("this is my unique key for my new plugin");
 		newObj.setName("mySecondInstallTest");
 		newObj.setProjectUrl("http://foobar.com/booya.xml");
 		newObj.setFiletype(PluginInfo.FileType.JAR);
 		tracker.addPlugin(newObj, PluginStatus.INSTALL);
 		assertEquals(tracker.getListByStatus(PluginStatus.INSTALL).size(), 2);
 		
 		// lets just check with the xml doc itself to be sure
 		Document Doc = getDoc();
 		Element Install = Doc.getRootElement().getChild("InstallPlugins");
 		assertEquals(Install.getChildren().size(), 2);
 		assertEquals(Doc.getRootElement().getChild("CurrentPlugins").getChildren().size(), 1);
 		assertEquals(Doc.getRootElement().getChild("DeletePlugins").getChildren().size(), 0);
 	}
 
 	public void testAddSamePlugin() throws Exception {
 		PluginInfo InfoObject = getInfoObj();
 		
 		tracker.addPlugin(InfoObject, PluginStatus.CURRENT);
 		assertEquals(tracker.getListByStatus(PluginStatus.CURRENT).size(), 1);
 		
 		String NewName = "DuplicatePluginTest";
 		InfoObject.setName(NewName);
 		tracker.addPlugin(InfoObject, PluginStatus.CURRENT);
 		
 		assertEquals(tracker.getListByStatus(PluginStatus.CURRENT).size(), 1);
 		
 		PluginInfo info = tracker.getListByStatus(PluginStatus.CURRENT).get(0);
 		
 		assertTrue(info.getName().equals(InfoObject.getName()));
 		
 		Document Doc = getDoc();
 		assertEquals(Doc.getRootElement().getChild("CurrentPlugins").getChild("plugin").getChildTextTrim("name"), NewName);
 	}
 	
 	
 	/**
 	 * Test method for {@link cytoscape.plugin.PluginTracker#removePlugin(cytoscape.plugin.PluginInfo, cytoscape.plugin.PluginTracker.PluginStatus)}.
 	 */
 	public void testRemovePlugin() throws Exception {
 		tracker.addPlugin(getInfoObj(), PluginStatus.CURRENT);
 		assertEquals(tracker.getListByStatus(PluginStatus.CURRENT).size(), 1);
 		
 		PluginInfo obj = new PluginInfo("999");
 		obj.setName("myInstallTest");
 		obj.setDownloadUrl("http://foobar.org/y.xml");
 		obj.setCategory("Test");
 		obj.setFiletype(PluginInfo.FileType.JAR);
 		tracker.addPlugin(obj, PluginStatus.INSTALL);
 		
 		assertEquals(tracker.getListByStatus(PluginStatus.INSTALL).size(), 1);
 
 		// won't change because this object wasn't an install object
 		tracker.removePlugin(getInfoObj(), PluginStatus.INSTALL);
 		assertEquals(tracker.getListByStatus(PluginStatus.INSTALL).size(), 1);
 
 		tracker.removePlugin(obj, PluginStatus.INSTALL);
 		assertEquals(tracker.getListByStatus(PluginStatus.INSTALL).size(), 0);
 		
 		Document Doc = getDoc();
 		assertEquals(Doc.getRootElement().getChild("CurrentPlugins").getChildren().size(), 1);
 		assertEquals(Doc.getRootElement().getChild("InstallPlugins").getChildren().size(), 0);
 		assertEquals(Doc.getRootElement().getChild("DeletePlugins").getChildren().size(), 0);
 	}
 
 	// regression test
 	public void testAddRemovePluginWithSameID() throws Exception {
 		PluginInfo objA = getInfoObj();
 		PluginInfo objB = new PluginInfo(objA.getID());
 		objB.setName("Different Test");
		objB.setDownloadUrl("http://test.com/x.xml");
 		objB.setFiletype(PluginInfo.FileType.JAR);
 		objB.setPluginClassName("some.other.class.DifferentTest");
 		
 		
 		tracker.addPlugin(objA, PluginStatus.CURRENT);
 		tracker.addPlugin(objB, PluginStatus.CURRENT);
 		assertEquals(tracker.getListByStatus(PluginStatus.CURRENT).size(), 2);
 		List<PluginInfo> CurrentList = tracker.getListByStatus(PluginStatus.CURRENT);
 		assertFalse(CurrentList.get(0).equals(CurrentList.get(1)));
 		
 		tracker.removePlugin(objA, PluginStatus.CURRENT);
 		assertEquals(tracker.getListByStatus(PluginStatus.CURRENT).size(), 1);
 		
 		// check that the correct object was actually removed
 		PluginInfo Current = tracker.getListByStatus(PluginStatus.CURRENT).get(0);
 		assertEquals(Current.getName(), objB.getName());
 	}
 	
 	
 	private Document getDoc() throws Exception {
 		File TestFile = new File( tmpDir, fileName);
 		assertTrue(TestFile.exists());
 		assertTrue(TestFile.canRead());
 		// lets just check with the xml doc itself to be sure
 		Document Doc = builder.build(TestFile);
 		assertNotNull(Doc);
 		return Doc;
 	}
 	
 	private PluginInfo getInfoObj() {
 		PluginInfo infoObj = new PluginInfo("123");
 		infoObj.setName("myTest");
 		infoObj.setCategory("Test");
 		infoObj.setCytoscapeVersion("2.5");
 		infoObj.setPluginClassName("some.class.MyTest");
 		infoObj.setDownloadUrl("http://test.com/x.xml");
 		infoObj.setFiletype(PluginInfo.FileType.JAR);
 		return infoObj;
 	}
 	
 }
